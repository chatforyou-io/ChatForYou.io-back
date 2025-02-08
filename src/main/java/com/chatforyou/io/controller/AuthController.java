package com.chatforyou.io.controller;

import com.chatforyou.io.client.OpenViduHttpException;
import com.chatforyou.io.client.OpenViduJavaClientException;
import com.chatforyou.io.client.Recording;
import com.chatforyou.io.config.SecurityConfig;
import com.chatforyou.io.models.AdminSessionData;
import com.chatforyou.io.models.JwtPayload;
import com.chatforyou.io.models.ValidateType;
import com.chatforyou.io.models.in.SocialUserInVo;
import com.chatforyou.io.models.in.UserInVo;
import com.chatforyou.io.models.out.UserOutVo;
import com.chatforyou.io.services.*;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	// 관리자 쿠키의 최대 유효 시간 (24시간)
	private int cookieAdminMaxAge = 24 * 60 * 60;

	@Value("${CALL_USER}")
	private String CALL_USER;

	@Value("${CALL_SECRET}")
	private String CALL_SECRET;

	@Value("${CALL_ADMIN_SECRET}")
	private String CALL_ADMIN_SECRET;

	@Value("${CALL_OPENVIDU_CERTTYPE}")
	private String CALL_OPENVIDU_CERTTYPE;

	private final MailService mailService;
	private final OpenViduService openviduService;
	private final AuthService authService;
	private final JwtService jwtService;

	/**
	 * 사용자 로그인 처리 메서드
	 * @param
	 * @param
	 * @return 로그인 성공 여부에 따른 응답 (HTTP 상태 코드 포함)
	 */
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody UserInVo user, HttpServletResponse response) {

		// 요청으로부터 사용자명과 비밀번호를 가져옴
		Map<String, Object> result = new ConcurrentHashMap<>();
		UserOutVo loginUserInfo = authService.getLoginUserInfo(user.getId(), user.getPwd());
		result.put("result", "success");
		result.put("userData", loginUserInfo);

		response.addHeader("AccessToken", jwtService.createAccessToken(JwtPayload.of(loginUserInfo)));
		response.addHeader("RefreshToken", jwtService.createRefreshToken(JwtPayload.of(loginUserInfo)));

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PostMapping("/login/social")
	public ResponseEntity<?> socialLogin(@RequestBody SocialUserInVo socialUser, HttpServletResponse response) throws BadRequestException {

		// 요청으로부터 사용자명과 비밀번호를 가져옴
		Map<String, Object> result = new ConcurrentHashMap<>();
		UserOutVo loginUserInfo = authService.getSocialLoginUserInfo(socialUser);
		result.put("result", "success");
		result.put("userData", loginUserInfo);

		response.addHeader("AccessToken", jwtService.createAccessToken(JwtPayload.of(loginUserInfo)));
		response.addHeader("RefreshToken", jwtService.createRefreshToken(JwtPayload.of(loginUserInfo)));

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout(
			@RequestHeader("Authorization") String bearerToken,
			@RequestBody UserInVo user, HttpServletRequest request, HttpServletResponse response) throws BadRequestException {
		Map<String, Object> result = new ConcurrentHashMap<>();
		jwtService.verifyAccessToken(bearerToken);
		authService.logoutUser(user);
		result.put("result", "success");
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PostMapping("/refresh_token")
	public ResponseEntity<?> refreshToken(
			@RequestHeader("Authorization") String bearerToken,
			@RequestBody UserInVo user, HttpServletRequest request, HttpServletResponse response) throws BadRequestException {

		Map<String, String> tokenResult = jwtService.reissueToken(user.getIdx(), bearerToken);
		response.addHeader("AccessToken", tokenResult.get("accessToken"));
		response.addHeader("RefreshToken", tokenResult.get("refreshToken"));

		Map<String, Object> result = new ConcurrentHashMap<>();
		result.put("result", "success");
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/validate")
	public ResponseEntity<?> checkEmailValidation(
			@RequestParam("email") String email,
			HttpServletResponse response) throws MessagingException, UnsupportedEncodingException, BadRequestException {
		// 이메일 중복 체크
		boolean isDuplicate = authService.validateStrByType(ValidateType.ID, email);
		if (isDuplicate) {
			throw new BadRequestException("already exist user ID");
		}

		// 이메일 전송
		String mailCode = mailService.sendEmailValidate(email);

		// mailCode를 쿠키로 설정
		Cookie cookie = new Cookie("mailCode", mailCode);
		cookie.setHttpOnly(true); // client 가 js 로 접근 할 수 없도록 체크
		cookie.setSecure(true); // HTTPS를 사용할 경우에만
		cookie.setPath("/");
		cookie.setMaxAge(60 * 5); // 쿠키 유효 기간 설정 (예: 5분)
		response.addCookie(cookie);

		Map<String, String> result = new HashMap<>();
		result.put("result", "send success");
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
}