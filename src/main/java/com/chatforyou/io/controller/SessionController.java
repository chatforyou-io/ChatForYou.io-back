package com.chatforyou.io.controller;

import com.chatforyou.io.client.*;
import com.chatforyou.io.models.RecordingData;
import com.chatforyou.io.services.OpenViduService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
public class SessionController {

	@Value("${CALL_RECORDING}")
	private String CALL_RECORDING;

	@Value("${CALL_BROADCAST}")
	private String CALL_BROADCAST;

	private final OpenViduService openviduService;

	private final int cookieAdminMaxAge = 24 * 60 * 60;

	@PostMapping("/sessions")
	public ResponseEntity<Map<String, Object>> createConnection(
			@RequestBody(required = true) Map<String, Object> params,
			@CookieValue(name = OpenViduService.MODERATOR_TOKEN_NAME, defaultValue = "") String moderatorCookie,
			@CookieValue(name = OpenViduService.PARTICIPANT_TOKEN_NAME, defaultValue = "") String participantCookie,
			HttpServletResponse res) {

		Map<String, Object> response = new HashMap<>();
		try {
			long date = -1;
			String nickName = "";
			String sessionId = "";
			// sessionId 는 일종의 roomId
			if (params.containsKey("sessionId")) {
				sessionId = params.get("sessionId").toString();
			} else if (params.containsKey("customSessionId))")) {
				sessionId = params.get("customSessionId").toString();
			}

			if (params.containsKey("nickName")) {
				nickName = params.get("nickName").toString();
			}

			Session sessionCreated = this.openviduService.createSession(sessionId);
			String MODERATOR_TOKEN_NAME = OpenViduService.MODERATOR_TOKEN_NAME;
			String PARTICIPANT_TOKEN_NAME = OpenViduService.PARTICIPANT_TOKEN_NAME;
			boolean IS_RECORDING_ENABLED = CALL_RECORDING.toUpperCase().equals("ENABLED");
			boolean IS_BROADCAST_ENABLED = CALL_BROADCAST.toUpperCase().equals("ENABLED");
			boolean PRIVATE_FEATURES_ENABLED = IS_RECORDING_ENABLED || IS_BROADCAST_ENABLED;

			boolean hasModeratorValidToken = this.openviduService.isModeratorSessionValid(sessionId, moderatorCookie);
			boolean hasParticipantValidToken = this.openviduService.isParticipantSessionValid(sessionId,
					participantCookie);
			boolean hasValidToken = hasModeratorValidToken || hasParticipantValidToken;
			boolean iAmTheFirstConnection = sessionCreated.getActiveConnections().size() == 0;
			boolean isSessionCreator = hasModeratorValidToken || iAmTheFirstConnection;

			OpenViduRole role = isSessionCreator ? OpenViduRole.MODERATOR : OpenViduRole.PUBLISHER;

			response.put("recordingEnabled", IS_RECORDING_ENABLED);
			response.put("recordings", new ArrayList<Recording>());
			response.put("broadcastingEnabled", IS_BROADCAST_ENABLED);
			response.put("isRecordingActive", sessionCreated.isBeingRecorded());
			response.put("isBroadcastingActive", sessionCreated.isBeingBroadcasted());

			Connection cameraConnection = this.openviduService.createConnection(sessionCreated, nickName, role);
			Connection screenConnection = this.openviduService.createConnection(sessionCreated, nickName, role);

			response.put("cameraToken", cameraConnection.getToken());
			response.put("screenToken", screenConnection.getToken());

			if (!hasValidToken && PRIVATE_FEATURES_ENABLED) {
				/**
				 * ! *********** WARN *********** !
				 *
				 해당 코드에서는 세션 녹화 및 스트리밍을 관리할 수 있는 사용자를 식별하기 위해 세션 생성자에게 토큰이 포함된 쿠키를 전송함
				 이때 쿠키와 세션 간의 관계는 백엔드 메모리에 저장.
				 아래 코드는 기본적인 인증 및 권한 부여 코드로 실제 운영 환경에서 사용시 변경 필요!
				 *
				 * ! *********** WARN *********** !
				 **/
				String uuid = UUID.randomUUID().toString();
				date = System.currentTimeMillis();

				if (isSessionCreator) {
					String moderatorToken = cameraConnection.getToken() + "&" + MODERATOR_TOKEN_NAME + "="
							+ uuid + "&createdAt=" + date;

					Cookie cookie = new Cookie(MODERATOR_TOKEN_NAME, moderatorToken);
					cookie.setMaxAge(cookieAdminMaxAge);
					res.addCookie(cookie);
					// Remove participant cookie if exists
					Cookie oldCookie = new Cookie(PARTICIPANT_TOKEN_NAME, "");
					oldCookie.setMaxAge(0);
					res.addCookie(oldCookie);

					RecordingData recData = new RecordingData(moderatorToken, "");
					this.openviduService.moderatorsCookieMap.put(sessionId, recData);

				} else {
					String participantToken = cameraConnection.getToken() + "&" + PARTICIPANT_TOKEN_NAME + "="
							+ uuid + "&createdAt=" + date;

					Cookie cookie = new Cookie(PARTICIPANT_TOKEN_NAME, participantToken);
					cookie.setMaxAge(cookieAdminMaxAge);
					res.addCookie(cookie);
					// Remove moderator cookie if exists
					Cookie oldCookie = new Cookie(MODERATOR_TOKEN_NAME, "");
					oldCookie.setMaxAge(0);
					res.addCookie(oldCookie);

					List<String> tokens = this.openviduService.participantsCookieMap.containsKey(sessionId)
							? this.openviduService.participantsCookieMap.get(sessionId)
							: new ArrayList<String>();
					tokens.add(participantToken);
					this.openviduService.participantsCookieMap.put(sessionId, tokens);
				}
			}

			if (IS_RECORDING_ENABLED) {
				try {
					if (date == -1) {
						date = openviduService.getDateFromCookie(moderatorCookie);
					}
					List<Recording> recordings = openviduService.listRecordingsBySessionIdAndDate(sessionId, date);
					response.put("recordings", recordings);
				} catch (OpenViduHttpException e) {
					if (e.getStatus() == 501) {
						System.out.println("Recording is disabled in OpenVidu Server.");
					}
				} catch (Exception e) {
					System.out.println("Unknown error listing recordings");
					e.printStackTrace();
				}
			}

			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (OpenViduJavaClientException | OpenViduHttpException e) {

			if (e.getMessage() != null && Integer.parseInt(e.getMessage()) == 501) {
				System.err.println("OpenVidu Server recording module is disabled");
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else if (e.getMessage() != null && Integer.parseInt(e.getMessage()) == 401) {
				System.err.println("OpenVidu credentials are wrong.");
				return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
			} else {
				e.printStackTrace();
				System.err.println(e.getMessage());
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/sessions")
	public ResponseEntity<Map<String, Object>> createConnection() throws OpenViduJavaClientException, OpenViduHttpException {
		Map<String, Object> response = new HashMap<>();
		response.put("chatRooms", openviduService.getActiveSessionList());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
