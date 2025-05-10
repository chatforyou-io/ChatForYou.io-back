package com.chatforyou.io.services.impl;

import ch.qos.logback.core.util.StringUtil;
import com.chatforyou.io.controller.ExceptionController;
import com.chatforyou.io.entity.User;
import com.chatforyou.io.models.DataType;
import com.chatforyou.io.models.JwtPayload;
import com.chatforyou.io.repository.UserRepository;
import com.chatforyou.io.services.JwtService;
import com.chatforyou.io.utils.RedisUtils;
import com.chatforyou.io.utils.ThreadUtils;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

    @Value("${spring.jwt.issuer}")
    private String issuer;

    @Value("${spring.jwt.access-expiration}")
    private Long accessExpiration;

    @Value("${spring.jwt.refresh-expiration}")
    private Long refreshExpiration;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserRepository userRepository;

    private final SecretKey secretKey;
    private final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private final String REFRESH_TOKEN = "REFRESH_TOKEN";

    public JwtServiceImpl(@Value("${spring.jwt.secret-key}") String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey));
    }

    @Override
    public String createAccessToken(JwtPayload jwtPayload) {
        return Jwts.builder()
                .subject(ACCESS_TOKEN)
                .claim("idx", jwtPayload.getIdx())
                .claim("userId", jwtPayload.getUserId())
//                .claim("isAdmin", jwtPayload.isAdmin())
                .claim("issuedAt", jwtPayload.getCreateDate())
                .claim("lastLoginAt", jwtPayload.getLastLoginDate())
                .issuer(issuer)
                .expiration(new Date(jwtPayload.getCreateDate() + accessExpiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String createRefreshToken(JwtPayload jwtPayload) {
        Long userIdx = jwtPayload.getIdx();
        String refreshToken = Jwts.builder()
                .subject(REFRESH_TOKEN)
                .claim("idx", userIdx)
                .claim("userId", jwtPayload.getUserId())
//                .claim("isAdmin", jwtPayload.isAdmin())
//                .claim("issuedAt", jwtPayload.getCreateDate())
                .claim("issuedAt", jwtPayload.getCreateDate())
                .claim("lastLoginAt", jwtPayload.getLastLoginDate())
                .issuer(issuer)
                .expiration(new Date(jwtPayload.getCreateDate() + refreshExpiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        ThreadUtils.runTask(()->{
            try {
                redisUtils.saveRefreshToken(userIdx, refreshToken);
                return true;
            } catch (Exception e) {
                log.error("Unknown Exception :: {} : {}", e.getMessage(), e);
                return false;
            }
        }, 10, 10, "Save Login User Token");

        return refreshToken;

    }

    @Override
    public void saveLastLoginDate(JwtPayload jwtPayload) {
        Long lastLoginDate = jwtPayload.getLastLoginDate() == null ?
                userRepository.findUserByIdx(jwtPayload.getIdx()).get().getLastLoginDate() : jwtPayload.getLastLoginDate();

        redisUtils.saveLastLoginDate(jwtPayload.getIdx(), lastLoginDate);
    }

    @Override
    public JwtPayload validateAccessToken(String jwtToken) throws BadRequestException {
        // TODO DB 에서 유저 찾아서 검증하는 로직은 모두 이쪽을 이동 필요?? >> 근데 db 에서 가져오면서 바로 예외처리하는데 굳이 2번해야함?
        String token = this.subStrBearerToken(jwtToken);
        try{
            Jws<Claims> claimsJws = Jwts.parser()
                    .setSigningKey(secretKey)
                    .requireIssuer(issuer)
                    .requireSubject(ACCESS_TOKEN)
                    .build()
                    .parseClaimsJws(token);

            Claims claims = claimsJws.getPayload();
            Long userIdx = claims.get("idx", Long.class);
            String userId = claims.get("userId", String.class);
            Long lastLoginAt = claims.get("lastLoginAt", Long.class);

            // 유저의 lastLoginDate 체크
            Long savedLastLoginDate = redisUtils.getRedisDataByDataType(String.valueOf(userIdx), DataType.USER_LAST_LOGIN_DATE, Long.class);
            if (!Objects.equals(lastLoginAt, savedLastLoginDate)) {
                throw new ExceptionController.UnauthorizedException("Session expired due to login from another device");
            }

            return JwtPayload.builder()
                    .idx(userIdx)
                    .userId(userId)
//                    .isAdmin(claims.get("isAdmin", Boolean.class))
                    .createDate(claims.get("issuedAt", Long.class))
                    .lastLoginDate(claims.get("lastLoginAt", Long.class))
                    .build();
        }catch (SignatureException e) {
            // 비밀 키 검증 실패 시 처리
            throw new ExceptionController.JwtSignatureException("The provided token signature is invalid.", e);
        } catch (ExpiredJwtException e) {
            // 만료 exception 처리
            throw new ExceptionController.JwtExpiredException(e);
        }
    }

    @Override
    public JwtPayload validateRefreshToken(Long userIdx, String jwtToken) throws BadRequestException {
        String tokenInRedis = redisUtils.getRedisDataByDataType(String.valueOf(userIdx), DataType.USER_REFRESH_TOKEN, String.class);
        if (tokenInRedis == null) {
            throw new ExceptionController.UnauthorizedException("Refresh token not found for the logged-in user");
        }

        String token = this.subStrBearerToken(jwtToken);
        if (StringUtil.isNullOrEmpty(token) || !tokenInRedis.equals(token)) {
            throw new ExceptionController.UnauthorizedException("Refresh Token validation failed");
        }

        try{
            Jws<Claims> claimsJws = Jwts.parser()
                    .setSigningKey(secretKey)
                    .requireIssuer(issuer)
                    .requireSubject(REFRESH_TOKEN)
                    .build()
                    .parseSignedClaims(token);

            Claims claims = claimsJws.getBody();
            Long userIdxInToken = claims.get("idx", Long.class);
            if (userIdx.longValue() != userIdxInToken.longValue()) {
                throw new BadRequestException("Does not match User Info :: USER_IDX");
            }

            User user = userRepository.findUserByIdx(userIdx)
                    .orElseThrow(() -> new EntityNotFoundException("can not find user"));

            return JwtPayload.of(user);
        }catch (SignatureException e) {
            // 비밀 키 검증 실패 시 처리
            throw new ExceptionController.JwtSignatureException("The provided token signature is invalid.", e);
        } catch (ExpiredJwtException e) {
            // 만료 exception 처리
            throw new ExceptionController.JwtExpiredException(e);
        }
    }

    @Override
    public Map<String, String> reissueToken(Long userIdx, String refreshToken) throws BadRequestException {
        Map<String, String> result = new ConcurrentHashMap<>();
        JwtPayload payload = this.validateRefreshToken(userIdx, refreshToken);

        this.saveLastLoginDate(payload);
        result.put("accessToken", this.createAccessToken(payload));
        result.put("refreshToken", this.createRefreshToken(payload));
        // 유저 데이터 유효시간 업데이트
        redisUtils.updateExpiredDate(userIdx);
        return result;
    }

    @Override
    public void validateUserIdx(Long requestedUserIdx, Long tokenUserIdx) throws BadRequestException {
        if (!Objects.equals(requestedUserIdx, tokenUserIdx)) {
            throw new BadRequestException("The user ID in the token does not match the user ID in the request.");
        }
    }

    private String subStrBearerToken(String token) throws ExceptionController.JwtBearerException {
        String[] subStrToken = token.split("Bearer ");
        if (subStrToken.length != 2) {
            throw new ExceptionController.JwtBearerException();
        }
        return subStrToken[1];
    }
}
