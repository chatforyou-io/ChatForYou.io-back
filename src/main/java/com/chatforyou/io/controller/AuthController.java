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

	/**
	 * 관리자 로그인 처리 메서드
	 *
	 * @param params 요청 본문에서 받은 비밀번호
	 * @param adminToken 현재 관리자 세션 토큰
	 * @param res 응답 객체, 쿠키 추가에 사용
	 * @return 로그인 성공 시 녹화 정보 포함된 응답, 실패 시 에러 메시지
	 */
	@PostMapping("/admin/login")
	public ResponseEntity<?> adminLogin(@RequestBody(required = true) Map<String, String> params,
										@CookieValue(name = SecurityConfig.ADMIN_COOKIE_NAME, defaultValue = "") String adminToken,
										HttpServletResponse res) {

		String message = "";
		Map<String, Object> response = new HashMap<String, Object>();

		// 요청으로부터 비밀번호를 가져옴
		String password = params.get("password");
		// 현재 관리자 세션이 유효한지 확인
		boolean isAdminSessionValid = authService.isAdminSessionValid(adminToken);

		// 비밀번호가 맞거나 현재 세션이 유효한지 확인
		boolean isAuthValid = password.equals(CALL_ADMIN_SECRET) || isAdminSessionValid;
		if (isAuthValid) {
			try {
				// 세션이 유효하지 않다면 새로운 세션 쿠키를 생성
				if (!isAdminSessionValid) {
					// 새로운 세션 ID 생성
					String id = UUID.randomUUID().toString();

					// 관리자 쿠키 생성 및 설정
					Cookie cookie = new Cookie(SecurityConfig.ADMIN_COOKIE_NAME, id);
					cookie.setPath("/");
					cookie.setMaxAge(cookieAdminMaxAge);
					cookie.setSecure(CALL_OPENVIDU_CERTTYPE.equals("selfsigned"));
					res.addCookie(cookie);

					// 기존 모더레이터와 참여자 쿠키 제거
					Cookie moderatorCookie = new Cookie(OpenViduService.MODERATOR_TOKEN_NAME, "");
					moderatorCookie.setPath("/");
					moderatorCookie.setMaxAge(0);
					res.addCookie(moderatorCookie);

					Cookie participantCookie = new Cookie(OpenViduService.PARTICIPANT_TOKEN_NAME, "");
					participantCookie.setPath("/");
					participantCookie.setMaxAge(0);
					res.addCookie(participantCookie);

					// 새로운 관리자 세션 데이터를 저장
					AdminSessionData data = new AdminSessionData(System.currentTimeMillis() + cookieAdminMaxAge * 1000);
					authService.putAdminSession(id, data);
				}
				// 모든 녹화 정보를 가져옴
				List<Recording> recordings = openviduService.listAllRecordings();
				System.out.println("Login succeeded");
				System.out.println(recordings.size() + " Recordings found");
				response.put("recordings", recordings);

				return new ResponseEntity<>(response, HttpStatus.OK);
			} catch (OpenViduJavaClientException | OpenViduHttpException error) {

				if (Integer.parseInt(error.getMessage()) == 501) {
					System.err.println(error.getMessage() + ". OpenVidu Server recording module is disabled.");
					return new ResponseEntity<>(response, HttpStatus.OK);
				} else {
					message = error.getMessage() + " Unexpected error getting recordings";
					error.printStackTrace();
					System.err.println(message);
					return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
				}

			}
		} else {
			message = "Permissions denied";
			System.err.println(message);
			return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
		}
	}

	/**
	 * 관리자 로그아웃 처리 메서드
	 *
	 * @param adminToken 현재 관리자 세션 토큰
	 * @param req 요청 객체
	 * @param res 응답 객체, 쿠키 제거에 사용
	 * @return 로그아웃 성공 시 빈 응답
	 */
	@PostMapping("/admin/logout")
	public ResponseEntity<Void> adminLogout(@CookieValue(name = SecurityConfig.ADMIN_COOKIE_NAME, defaultValue = "") String adminToken,
											HttpServletResponse res) {

		// 관리자 세션 토큰을 세션 목록에서 제거
		authService.delAdminSession(adminToken);
		// 관리자 쿠키 무효화
		Cookie cookie = new Cookie(SecurityConfig.ADMIN_COOKIE_NAME, "");
		cookie.setPath("/");
		cookie.setMaxAge(0);
		res.addCookie(cookie);

		return ResponseEntity.ok().build();
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