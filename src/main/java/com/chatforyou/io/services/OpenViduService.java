package com.chatforyou.io.services;


import ch.qos.logback.core.util.StringUtil;
import com.chatforyou.io.client.*;
import com.chatforyou.io.entity.ChatRoom;
import com.chatforyou.io.entity.User;
import com.chatforyou.io.models.DataType;
import com.chatforyou.io.models.OpenViduDto;
import com.chatforyou.io.models.RecordingData;
import com.chatforyou.io.models.out.ConnectionOutVo;
import com.chatforyou.io.models.out.SessionOutVo;
import com.chatforyou.io.utils.RedisUtils;
import com.chatforyou.io.utils.RetryException;
import com.chatforyou.io.utils.RetryOptions;
import com.chatforyou.io.utils.ThreadUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenViduService {

	public static final String MODERATOR_TOKEN_NAME = "ovCallModeratorToken";
	public static final String PARTICIPANT_TOKEN_NAME = "ovCallParticipantToken";
	private final RedisUtils redisUtils;

	@Value("${CALL_RECORDING}")
	private String CALL_RECORDING;

	@Value("${CALL_BROADCAST}")
	private String CALL_BROADCAST;

	private final int cookieAdminMaxAge = 24 * 60 * 60;

	public static Map<String, RecordingData> moderatorsCookieMap = new ConcurrentHashMap<>();
	public static Map<String, List<String>> participantsCookieMap = new ConcurrentHashMap<>();

	@Value("${OPENVIDU_URL}")
	public String OPENVIDU_URL;

	@Value("${OPENVIDU_SECRET}")
	private String OPENVIDU_SECRET;

	private OpenVidu openvidu;
	private String edition;

	@PostConstruct
	public void init() {
		this.openvidu = new OpenVidu(OPENVIDU_URL, OPENVIDU_SECRET);
	}

	public String getBasicAuth() {
		String stringToEncode = "OPENVIDUAPP:" + OPENVIDU_SECRET;
		String encodedString = Base64.getEncoder().encodeToString(stringToEncode.getBytes());
		return "Basic " + new String(encodedString);
	}

	public OpenViduDto createOpenViduRoom(ChatRoom chatRoom) throws BadRequestException {
		OpenViduDto openViduDto = null;

		try {
			long date = -1;
			Long userIdx = chatRoom.getUser().getIdx();
			String sessionId = chatRoom.getSessionId();

			Session openViduSession = this.createSession(sessionId);

			String MODERATOR_TOKEN_NAME = OpenViduService.MODERATOR_TOKEN_NAME;
			String PARTICIPANT_TOKEN_NAME = OpenViduService.PARTICIPANT_TOKEN_NAME;
			boolean IS_RECORDING_ENABLED = CALL_RECORDING.toUpperCase().equals("ENABLED");
			boolean IS_BROADCAST_ENABLED = CALL_BROADCAST.toUpperCase().equals("ENABLED");
			boolean PRIVATE_FEATURES_ENABLED = IS_RECORDING_ENABLED || IS_BROADCAST_ENABLED;

			boolean hasModeratorValidToken = this.isModeratorSessionValid(sessionId, "");
			boolean hasParticipantValidToken = this.isParticipantSessionValid(sessionId, "");
			boolean hasValidToken = hasModeratorValidToken || hasParticipantValidToken;
			ConnectionOutVo cameraConnection = this.createConnection(openViduSession, userIdx, OpenViduRole.MODERATOR, "camera");
			ConnectionOutVo screenConnection = this.createConnection(openViduSession, userIdx, OpenViduRole.MODERATOR, "screen");
			saveUserConnection(userIdx, sessionId, cameraConnection, screenConnection);

			openViduDto = OpenViduDto.builder()
					.creator(chatRoom.getUser().getNickName())
					.recordingEnabled(IS_RECORDING_ENABLED)
					.isRecordingActive(openViduSession.isBeingRecorded())
					.broadcastingEnabled(IS_BROADCAST_ENABLED)
					.isBroadcastingActive(openViduSession.isBeingBroadcasted())
					.session(SessionOutVo.of(openViduSession, getConnectionInfoList(openViduSession.getConnections())))
					.build();

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
				initSessionCreator(sessionId, cameraConnection.getToken(), uuid, date);

			}

		} catch (OpenViduJavaClientException | OpenViduHttpException e) {

			if (e.getMessage() != null && Integer.parseInt(e.getMessage()) == 501) {
				System.err.println("OpenVidu Server recording module is disabled");
				throw new BadRequestException("OpenVidu Server recording module is disabled");
//				return new ResponseEntity<>(response, HttpStatus.OK);
			} else if (e.getMessage() != null && Integer.parseInt(e.getMessage()) == 401) {
				System.err.println("OpenVidu credentials are wrong.");
				throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
//				return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
			} else {
				e.printStackTrace();
				System.err.println(e.getMessage());
				throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
//				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
			throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
//			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
//		redisUtils.setObject(DataType.redisDataType(chatRoom.getSessionId(), DataType.OPENVIDU), openViduDto);
		return openViduDto;
	}

	private void saveUserConnection(Long userIdx, String sessionId, ConnectionOutVo cameraConnection, ConnectionOutVo screenConnection) {
		ThreadUtils.runTask(()->{
			try{
				redisUtils.saveConnectionTokens(sessionId, String.valueOf(userIdx), cameraConnection, screenConnection);
				return true;
			}catch (Exception e){
				log.error("Unknown Exception occurred :: {} : {}", e.getMessage(), e);
				return false;
			}
		}, 10, 100, "Save User Token");
	}

	public OpenViduDto joinOpenviduRoom(String sessionId, User joinUser) throws BadRequestException, OpenViduJavaClientException, OpenViduHttpException {
		Session openViduSession = this.openvidu.getActiveSession(sessionId);
		OpenViduDto openViduDto = redisUtils.getRedisDataByDataType(sessionId, DataType.OPENVIDU, OpenViduDto.class);
		if (Objects.isNull(openViduSession) || Objects.isNull(openViduDto)) {
			throw new BadRequestException("Unknown Openvidu Session");
		}
		String creator = openViduDto.getCreator();
		try {
			long date = -1;
			// sessionId 는 일종의 roomId
//			if (params.containsKey("sessionId")) {
//				sessionId = params.get("sessionId").toString();
//			} else if (params.containsKey("customSessionId))")) {
//				sessionId = params.get("customSessionId").toString();
//			}
//
//			if (params.containsKey("nickName")) {
//				nickName = params.get("nickName").toString();
//			}

			String MODERATOR_TOKEN_NAME = OpenViduService.MODERATOR_TOKEN_NAME;
			String PARTICIPANT_TOKEN_NAME = OpenViduService.PARTICIPANT_TOKEN_NAME;
			boolean IS_RECORDING_ENABLED = CALL_RECORDING.toUpperCase().equals("ENABLED");
			boolean IS_BROADCAST_ENABLED = CALL_BROADCAST.toUpperCase().equals("ENABLED");
			boolean PRIVATE_FEATURES_ENABLED = IS_RECORDING_ENABLED || IS_BROADCAST_ENABLED;

			boolean hasModeratorValidToken = this.isModeratorSessionValid(sessionId, "");
			boolean hasParticipantValidToken = this.isParticipantSessionValid(sessionId, "");
			boolean hasValidToken = hasModeratorValidToken || hasParticipantValidToken;
			boolean iAmTheFirstConnection = !StringUtil.isNullOrEmpty(creator) && creator.equals(joinUser.getNickName());
			boolean isSessionCreator = hasModeratorValidToken || iAmTheFirstConnection;
			OpenViduRole role = isSessionCreator ? OpenViduRole.MODERATOR : OpenViduRole.PUBLISHER;
			ConnectionOutVo cameraConnection = this.createConnection(openViduSession, joinUser.getIdx(), role, "camera");
			ConnectionOutVo screenConnection = this.createConnection(openViduSession, joinUser.getIdx(), role, "screen");
			saveUserConnection(joinUser.getIdx(), sessionId, cameraConnection, screenConnection);
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
				initSessionParticipant(sessionId, cameraConnection.getToken(), uuid, date);
			}

		} catch (OpenViduJavaClientException | OpenViduHttpException e) {

			if (e.getMessage() != null && Integer.parseInt(e.getMessage()) == 501) {
				System.err.println("OpenVidu Server recording module is disabled");
				throw new BadRequestException("OpenVidu Server recording module is disabled");
//				return new ResponseEntity<>(response, HttpStatus.OK);
			} else if (e.getMessage() != null && Integer.parseInt(e.getMessage()) == 401) {
				System.err.println("OpenVidu credentials are wrong.");
				throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
//				return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
			} else {
				e.printStackTrace();
				System.err.println(e.getMessage());
				throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
//				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
			throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
//			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		OpenViduDto newOpenViduDto = OpenViduDto.builder()
				.creator(openViduDto.getCreator())
				.recordingEnabled(openViduDto.isRecordingEnabled())
				.isRecordingActive(openViduDto.isRecordingActive())
				.broadcastingEnabled(openViduDto.isBroadcastingEnabled())
				.isBroadcastingActive(openViduDto.isBroadcastingActive())
				.session(SessionOutVo.of(openViduSession, getConnectionInfoList(openViduSession.getConnections())))
				.build();

		return newOpenViduDto;
	}

	private void initSessionCreator(String sessionId, String cameraConnectionToken, String uuid, long date){
		String moderatorToken = cameraConnectionToken + "&" + MODERATOR_TOKEN_NAME + "="
				+ uuid + "&createdAt=" + date;

		Cookie cookie = new Cookie(MODERATOR_TOKEN_NAME, moderatorToken);
		cookie.setMaxAge(cookieAdminMaxAge);
//					res.addCookie(cookie);
		// Remove participant cookie if exists
		Cookie oldCookie = new Cookie(PARTICIPANT_TOKEN_NAME, "");
		oldCookie.setMaxAge(0);
//					res.addCookie(oldCookie);

		RecordingData recData = new RecordingData(moderatorToken, "");
		OpenViduService.moderatorsCookieMap.put(sessionId, recData);
	}

	private void initSessionParticipant(String sessionId, String cameraConnectionToken, String uuid, long date){
		String participantToken = cameraConnectionToken + "&" + PARTICIPANT_TOKEN_NAME + "="
				+ uuid + "&createdAt=" + date;

		Cookie cookie = new Cookie(PARTICIPANT_TOKEN_NAME, participantToken);
		cookie.setMaxAge(cookieAdminMaxAge);
//					res.addCookie(cookie);
		// Remove moderator cookie if exists
		Cookie oldCookie = new Cookie(MODERATOR_TOKEN_NAME, "");
		oldCookie.setMaxAge(0);
//					res.addCookie(oldCookie);

		List<String> tokens = OpenViduService.participantsCookieMap.containsKey(sessionId)
				? OpenViduService.participantsCookieMap.get(sessionId)
				: new ArrayList<String>();
		tokens.add(participantToken);
		OpenViduService.participantsCookieMap.put(sessionId, tokens);
	}

	public long getDateFromCookie(String recordingToken) {
		try {
			if (!recordingToken.isEmpty()) {
				MultiValueMap<String, String> cookieTokenParams = UriComponentsBuilder.fromUriString(recordingToken)
						.build()
						.getQueryParams();
				String date = cookieTokenParams.get("createdAt").get(0);
				return Long.parseLong(date);
			} else {
				return System.currentTimeMillis();
			}
		} catch (Exception e) {
			return System.currentTimeMillis();
		}
	}

	public String getSessionIdFromCookie(String cookie) {
		try {

			if (!cookie.isEmpty()) {
				MultiValueMap<String, String> cookieTokenParams = UriComponentsBuilder.fromUriString(cookie)
						.build().getQueryParams();
				return cookieTokenParams.get("sessionId").get(0);
			}

		} catch (Exception error) {
			System.out.println("Session cookie not found");
			System.err.println(error);
		}
		return "";

	}

	public String getSessionIdFromRecordingId(String recordingId) {
		return recordingId.split("~")[0];
	}

	public boolean isModeratorSessionValid(String sessionId, String token) {
		try {

			if(token.isEmpty()) return false;
			if(!this.moderatorsCookieMap.containsKey(sessionId)) return false;

			MultiValueMap<String, String> storedTokenParams = UriComponentsBuilder
					.fromUriString(this.moderatorsCookieMap.get(sessionId).getToken()).build().getQueryParams();

			MultiValueMap<String, String> cookieTokenParams = UriComponentsBuilder
					.fromUriString(token).build().getQueryParams();

			String cookieSessionId = cookieTokenParams.get("sessionId").get(0);
			String cookieToken = cookieTokenParams.get(MODERATOR_TOKEN_NAME).get(0);
			String cookieDate = cookieTokenParams.get("createdAt").get(0);

			String storedToken = storedTokenParams.get(MODERATOR_TOKEN_NAME).get(0);
			String storedDate = storedTokenParams.get("createdAt").get(0);

			return sessionId.equals(cookieSessionId) && cookieToken.equals(storedToken)
					&& cookieDate.equals(storedDate);

		} catch (Exception e) {
			return false;
		}
	}

	public boolean isParticipantSessionValid(String sessionId, String cookie) {

		try {
			if (!this.participantsCookieMap.containsKey(sessionId))	return false;
			if(cookie.isEmpty()) return false;


			MultiValueMap<String, String> cookieTokenParams = UriComponentsBuilder
					.fromUriString(cookie).build().getQueryParams();

			List<String> storedTokens = this.participantsCookieMap.get(sessionId);

			String cookieSessionId = cookieTokenParams.get("sessionId").get(0);
			String cookieToken = cookieTokenParams.get(PARTICIPANT_TOKEN_NAME).get(0);
			String cookieDate = cookieTokenParams.get("createdAt").get(0);

			for (String token : storedTokens) {
				MultiValueMap<String, String> storedTokenParams = UriComponentsBuilder
					.fromUriString(token).build().getQueryParams();

				String storedToken = storedTokenParams.get(PARTICIPANT_TOKEN_NAME).get(0);
				String storedDate = storedTokenParams.get("createdAt").get(0);

				if (sessionId.equals(cookieSessionId) && cookieToken.equals(storedToken) && cookieDate.equals(storedDate)) {
					return true;
				}
			}

			return false;

		} catch (Exception e) {
			return false;
		}
	}

	public Session createSession(String sessionId)
			throws OpenViduJavaClientException, OpenViduHttpException, InterruptedException, RetryException {
		RetryOptions retryOptions = new RetryOptions();
		return createSession(sessionId, retryOptions);
	}

	public List<SessionOutVo> getActiveSessionOutVoList() throws OpenViduJavaClientException, OpenViduHttpException {
		// 최신 active session 가져오기
		openvidu.fetch();
		List<Session> activeSessions = openvidu.getActiveSessions();
		List<SessionOutVo> sessionOutVoList = new ArrayList<>();
		if (CollectionUtils.isEmpty(activeSessions)) {
			return sessionOutVoList;
		}
		for (Session session : activeSessions) {
			sessionOutVoList.add(SessionOutVo.of(session, getConnectionInfoList(session.getConnections())));
		}
		return sessionOutVoList;
	}

	public Optional<Session> getSession(String sessionId) throws OpenViduJavaClientException, OpenViduHttpException, BadRequestException {
		// 최신 active session 가져오기
		openvidu.fetch();
		List<Session> activeSessions = openvidu.getActiveSessions();

		return activeSessions.stream()
				.filter(s -> sessionId.equals(s.getSessionId()))
				.findFirst();
	}

	public boolean closeSession(String sessionId) throws OpenViduJavaClientException, OpenViduHttpException, BadRequestException {
		try {
			Optional<Session> session = this.getSession(sessionId);
			if (session.isEmpty()) {
				return true;
			}

			session.get().close();
			return true;
		} catch (Exception e) {
			log.error("close session Exception ::: {}", e.getMessage());
			return false;
		}
	}

	private Map<String, ConnectionOutVo> getConnectionInfoList(List<Connection> connectionList){
		Map<String, ConnectionOutVo> connectionInfoMap = new ConcurrentHashMap<>();
		for (Connection connection : connectionList) {
			connectionInfoMap.put(connection.getConnectionId(), ConnectionOutVo.of(connection));
		}
		return connectionInfoMap;
	}

	/**
	 * openvidu 와 연결되어 세션 생성
	 * @param sessionId
	 * @param retryOptions
	 * @return
	 * @throws OpenViduJavaClientException
	 * @throws OpenViduHttpException
	 * @throws InterruptedException
	 * @throws RetryException
	 */
	private Session createSession(String sessionId, RetryOptions retryOptions)
			throws OpenViduJavaClientException, OpenViduHttpException, InterruptedException, RetryException {
		while(retryOptions.canRetry()) {
			try {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("customSessionId", sessionId);
				RecordingProperties.Builder builder = new RecordingProperties.Builder();
				SessionProperties properties = SessionProperties.fromJson(params)
						.defaultRecordingProperties(builder.build())
						.build();
//				SessionProperties properties = SessionProperties.fromJson(params).build();
				Session session = openvidu.createSession(properties);
				session.fetch();
				return session;
			} catch (OpenViduHttpException e) {
				if ((e.getStatus() >= 500 && e.getStatus() <= 504) || e.getStatus() == 404) {
					// Retry is used for OpenVidu Enterprise High Availability for reconnecting purposes
					// to allow fault tolerance
					// 502 to 504 are returned when OpenVidu Server is not available (stopped, not reachable, etc...)
					// 404 is returned when the session does not exist which is returned by fetch operation in createSession
					// and it is not a possible error after session creation
					System.err.println("Error creating session: " + e.getMessage()
						+ ". Retrying session creation..." + retryOptions.toString());
					retryOptions.retrySleep();
				} else {
					System.err.println("Error creating session: " + e.getMessage());
					throw e;
				}
			}
		}
		throw new RetryException("Max retries exceeded");
	}

	public Map<String, ConnectionOutVo> getConnection(String sessionId, Long userIdx){
		return redisUtils.getConnectionTokens(sessionId, String.valueOf(userIdx));
	}

	public ConnectionOutVo createConnection(Session session, Long userIdx, OpenViduRole role, String tokenType)
			throws OpenViduJavaClientException, OpenViduHttpException, RetryException, InterruptedException {
		ConnectionOutVo connectionOutVo = ConnectionOutVo.of(createConnection(session, tokenType + "_" + userIdx, role, new RetryOptions()));
		return connectionOutVo;
	}

	private Connection createConnection(Session session, String nickname, OpenViduRole role, RetryOptions retryOptions)
			throws OpenViduJavaClientException, OpenViduHttpException, RetryException, InterruptedException {
		Map<String, Object> params = new HashMap<String, Object>();
		Map<String, Object> connectionData = new HashMap<String, Object>();

		if (!nickname.isEmpty()) {
			connectionData.put("openviduCustomConnectionId", nickname);
		}
		params.put("role", role.name());
		params.put("data", connectionData.toString());
		ConnectionProperties properties = ConnectionProperties.fromJson(params).build();

		Connection connection = null;
		while (retryOptions.canRetry()) {
			try {
				connection = session.createConnection(properties);
				break;
			} catch (OpenViduHttpException e) {
				if (e.getStatus() >= 500 && e.getStatus() <= 504) {
					// Retry is used for OpenVidu Enterprise High Availability for reconnecting purposes
					// to allow fault tolerance
					System.err.println("Error creating connection: " + e.getMessage()
						+ ". Retrying connection creation..." + retryOptions.toString());
					retryOptions.retrySleep();
				} else {
					System.err.println("Error creating connection: " + e.getMessage());
					throw e;
				}
			}
		}

		if (connection == null) {
			throw new RetryException("Max retries exceeded");
		}

		MultiValueMap<String, String> tokenParams = UriComponentsBuilder
				.fromUriString(connection.getToken()).build().getQueryParams();

		if (tokenParams.containsKey("edition")) {
			this.edition = tokenParams.get("edition").get(0);
		} else {
			this.edition = "ce";
		}

		return connection;

	}

	public Recording startRecording(String sessionId) throws OpenViduJavaClientException, OpenViduHttpException {
		return this.openvidu.startRecording(sessionId);
	}

	public Recording stopRecording(String recordingId) throws OpenViduJavaClientException, OpenViduHttpException {
		return this.openvidu.stopRecording(recordingId);
	}

	public void deleteRecording(String recordingId) throws OpenViduJavaClientException, OpenViduHttpException {
		this.openvidu.deleteRecording(recordingId);
	}

	public Recording getRecording(String recordingId) throws OpenViduJavaClientException, OpenViduHttpException {
		return this.openvidu.getRecording(recordingId);
	}

	public List<Recording> listAllRecordings() throws OpenViduJavaClientException, OpenViduHttpException {
		return this.openvidu.listRecordings();
	}

	public List<Recording> listRecordingsBySessionIdAndDate(String sessionId, long date)
			throws OpenViduJavaClientException, OpenViduHttpException {
		List<Recording> recordings = this.listAllRecordings();
		List<Recording> recordingsAux = new ArrayList<Recording>();
		for (Recording recording : recordings) {
			if (recording.getSessionId().equals(sessionId) && recording.getCreatedAt() + recording.getDuration() * 1000 >= date) {
				recordingsAux.add(recording);
			}
		}
		return recordingsAux;
	}

}
