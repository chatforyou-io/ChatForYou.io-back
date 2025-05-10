package com.chatforyou.io.utils;

import com.chatforyou.io.controller.ExceptionController;
import com.chatforyou.io.entity.User;
import com.chatforyou.io.models.JwtPayload;
import com.chatforyou.io.models.out.UserOutVo;
import com.chatforyou.io.services.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.apache.coyote.BadRequestException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.SignatureException;
import java.util.Date;


@SpringBootTest
class jwtServiceTest {
    Logger logger = LoggerFactory.getLogger(jwtServiceTest.class);

    @Value("${spring.jwt.issuer}")
    private String issuer;

    @Value("${spring.jwt.access-expiration}")
    private Long accessExpiration;

    @Value("${spring.jwt.refresh-expiration}")
    private Long refreshExpiration;
    @Value("${spring.jwt.secret-key}")
    private String secretKey;

    @Autowired
    JwtService jwtService;

    JwtPayload createPayload(){
        return JwtPayload.builder()
                .idx(25L)
                .userId("sejon@test.com")
                .isAdmin(false)
                .createDate(new Date(System.currentTimeMillis()).getTime())
                .build();
    }

    UserOutVo createUserVo(){
        User user = User.builder()
                .id("test")
                .pwd("test")
                .usePwd(true)
                .createDate(new Date().getTime())
                .name("test")
                .nickName("testnick")
                .build();

        return UserOutVo.of(user, false);
    }

    @DisplayName("토큰 생성")
    @Test
    public void createToken(){
        JwtPayload payload = createPayload();
        String accessToken = jwtService.createAccessToken(payload);
        String refreshToken = jwtService.createRefreshToken(payload);

        logger.info("accessToken ::: {}", accessToken);
        logger.info("refreshToken ::: {}", refreshToken);
    }

    @DisplayName("토큰 검증 :: 만료 X secretKey 일치")
    @Test
    public void verifyTokenEqualSecretKey() throws BadRequestException {
        JwtPayload payload = createPayload();
        UserOutVo userVo = createUserVo();
        String accessToken = jwtService.createAccessToken(payload);

        JwtPayload resultPayload = jwtService.validateAccessToken("Bearer "+accessToken);
        logger.info("result ::: {}", resultPayload.toString());
    }

    @DisplayName("토큰 검증 :: 만료 X secretKey 불일치")
    @Test
    public void verifyTokenUnequalSecretKey(){
        JwtPayload payload = createPayload();
        UserOutVo userVo = createUserVo();


        String someJwt = Jwts.builder()
                .subject(payload.getUserId())
                .claim("idx", payload.getIdx())
                .claim("userId", payload.getUserId())
                .claim("isAdmin", payload.isAdmin())
                .issuer(issuer)
                .expiration(new Date(payload.getCreateDate() + refreshExpiration))
                .signWith(Jwts.SIG.HS256.key().build()) // secret 불일치
                .compact();

        Assertions.assertThatThrownBy(() -> jwtService.validateAccessToken("Bearer "+someJwt))
                .isInstanceOf(SignatureException.class);
    }

    @DisplayName("토큰 검증 :: 만료 O secretKey 일치")
    @Test
    public void verifyTokenExpiredToken() throws ExceptionController.JwtExpiredException, ExceptionController.JwtBearerException, ExceptionController.JwtSignatureException {
        JwtPayload payload = createPayload();
        UserOutVo userVo = createUserVo();
        String expiredToken = jwtService.createAccessToken(payload);

        Assertions.assertThatThrownBy(() -> jwtService.validateAccessToken("Bearer "+expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }
}