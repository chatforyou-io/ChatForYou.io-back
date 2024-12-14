package com.chatforyou.io.controller;

import com.chatforyou.io.client.*;
import com.chatforyou.io.entity.ChatRoom;
import com.chatforyou.io.entity.User;
import com.chatforyou.io.models.JwtPayload;
import com.chatforyou.io.models.OpenViduDto;
import com.chatforyou.io.models.in.ChatRoomInVo;
import com.chatforyou.io.models.out.ChatRoomOutVo;
import com.chatforyou.io.models.out.ConnectionOutVo;
import com.chatforyou.io.models.out.SessionOutVo;
import com.chatforyou.io.models.out.UserOutVo;
import com.chatforyou.io.repository.ChatRoomRepository;
import com.chatforyou.io.services.ChatRoomService;
import com.chatforyou.io.services.OpenViduService;
import com.chatforyou.io.services.UserService;
import com.chatforyou.io.utils.RedisUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
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
	private final ChatRoomService chatRoomService;
    private final UserService userService;
	private final RedisUtils redisUtils;

    private final ChatRoomRepository chatRoomRepository;

    private final int cookieAdminMaxAge = 24 * 60 * 60;

    // TODO front 와 코드 정리가 완료된 후 삭제 필요!
    @PostMapping("/sessions")
    @Deprecated
    public ResponseEntity<Map<String, Object>> createConnection(
            @RequestBody(required = true) Map<String, Object> params,
            @CookieValue(name = OpenViduService.MODERATOR_TOKEN_NAME, defaultValue = "") String moderatorCookie,
            @CookieValue(name = OpenViduService.PARTICIPANT_TOKEN_NAME, defaultValue = "") String participantCookie,
            HttpServletResponse res) throws BadRequestException, OpenViduJavaClientException, OpenViduHttpException {

		Map<String, Object> response = new HashMap<>();
		String sessionId = params.get("sessionId").toString();
        UserOutVo creatorUser = userService.findUserByIdx(2L);
        ChatRoomInVo testChatRoomInVo = ChatRoomInVo.builder()
                .userIdx(creatorUser.getIdx())
				.roomName(params.get("sessionId").toString())
				.usePwd(false)
				.usePrivate(true)
				.useRtc(true)
				.maxUserCount(999)
                .desc("test 디스크립션")
				.build();
        Optional<ChatRoom> chatRoomByName = chatRoomRepository.findChatRoomByName(sessionId);
		if (chatRoomByName.isEmpty()) {
			ChatRoomOutVo chatRoomByRoomName = chatRoomService.createChatRoom(testChatRoomInVo, JwtPayload.of(creatorUser));

            chatRoomService.joinChatRoom(chatRoomByRoomName.getSessionId(), creatorUser.getIdx(), JwtPayload.of(creatorUser));
            OpenViduDto openViduDto = chatRoomService.getOpenviduDataBySessionId(chatRoomByRoomName.getSessionId());
            SessionOutVo session = openViduDto.getSession();
            response.put("recordingEnabled", openViduDto.isRecordingEnabled());
            response.put("recordings", new ArrayList<Recording>());
            response.put("broadcastingEnabled", openViduDto.isBroadcastingEnabled());
            response.put("isRecordingActive", openViduDto.isRecordingActive());
            response.put("isBroadcastingActive", openViduDto.isBroadcastingActive());
            Map<String, ConnectionOutVo> connection = openviduService.getConnection(session.getSessionId(), creatorUser.getIdx());
            response.put("cameraToken", connection.get("cameraToken").getToken());
            response.put("screenToken", connection.get("screenToken").getToken());
		} else {
            UserOutVo joinUserVo = userService.findUserById("test@mail.test.com");
            User user = User.builder()
                    .idx(joinUserVo.getIdx())
                    .nickName(joinUserVo.getNickName())
                    .build();
//            openviduService.joinOpenviduRoom(chatRoomByName.get().getSessionId(), user);
            chatRoomService.joinChatRoom(chatRoomByName.get().getSessionId(), user.getIdx(), JwtPayload.of(user));
            OpenViduDto openViduDto = chatRoomService.getOpenviduDataBySessionId(chatRoomByName.get().getSessionId());
            SessionOutVo session = openViduDto.getSession();
            response.put("recordingEnabled", openViduDto.isRecordingEnabled());
            response.put("recordings", new ArrayList<Recording>());
            response.put("broadcastingEnabled", openViduDto.isBroadcastingEnabled());
            response.put("isRecordingActive", openViduDto.isRecordingActive());
            response.put("isBroadcastingActive", openViduDto.isBroadcastingActive());
            Map<String, ConnectionOutVo> connection = openviduService.getConnection(session.getSessionId(), joinUserVo.getIdx());
            response.put("cameraToken", connection.get("cameraToken").getToken());
            response.put("screenToken", connection.get("screenToken").getToken());
        }

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

}