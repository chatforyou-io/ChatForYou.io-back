package com.chatforyou.io.services.impl;

import com.chatforyou.io.client.OpenViduHttpException;
import com.chatforyou.io.client.OpenViduJavaClientException;
import com.chatforyou.io.entity.User;
import com.chatforyou.io.models.DataType;
import com.chatforyou.io.models.JwtPayload;
import com.chatforyou.io.models.OpenViduDto;
import com.chatforyou.io.models.OpenViduWebhookData;
import com.chatforyou.io.models.out.ChatRoomOutVo;
import com.chatforyou.io.models.out.ConnectionOutVo;
import com.chatforyou.io.models.out.SessionOutVo;
import com.chatforyou.io.models.out.UserOutVo;
import com.chatforyou.io.repository.UserRepository;
import com.chatforyou.io.services.ChatRoomService;
import com.chatforyou.io.services.OpenViduService;
import com.chatforyou.io.services.OpenViduWebhookService;
import com.chatforyou.io.utils.RedisUtils;
import com.chatforyou.io.utils.ThreadUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenViduWebhookServiceImpl implements OpenViduWebhookService {
    private final UserRepository userRepository;

    private final RedisUtils redisUtils;
    private final ChatRoomService chatRoomService;
    private final OpenViduService openViduService;

    @Override
    public void processWebhookEvent(OpenViduWebhookData webhookData) throws OpenViduJavaClientException, OpenViduHttpException, BadRequestException {
        log.info("====== WebHookData ::: {}", webhookData.toString());

        String sessionId = webhookData.getSessionId();
        ChatRoomOutVo chatRoom = null;
        OpenViduDto openViduDto = null;
        try {
            chatRoom = chatRoomService.findChatRoomBySessionId(sessionId);
            openViduDto = redisUtils.getRedisDataByDataType(sessionId, DataType.OPENVIDU, OpenViduDto.class);

        } catch (Exception e) {
            log.warn("Does not Exist ChatRoom or OpenViduData :: {} :: {}", sessionId, e.getMessage());
        }

        switch (webhookData.getEvent()) {
            case PARTICIPANT_LEFT:  // 유저 접속 종료
                String connectionId = webhookData.getConnectionId();
                Long userIdx = Long.parseLong(connectionId.split("_")[2]);
                processParticipantLeftEvent(userIdx, sessionId, openViduDto);
                break;
            case SESSION_DESTROYED: // session 삭제
                chatRoomService.deleteChatRoom(sessionId, JwtPayload.builder().build(), true);
                break;
        }
    }

    private void processParticipantLeftEvent(Long userIdx, String sessionId, OpenViduDto openViduDto) {
        User leftUser = userRepository.findUserByIdx(userIdx)
                .orElseThrow(() -> new EntityNotFoundException("Can not find User"));

        // redis 에서 유저 connection token 정보 삭제
        ThreadUtils.runTask(() -> {
            try{
                redisUtils.deleteConnectionTokens(sessionId, String.valueOf(userIdx));
                return true;
            } catch (Exception e){
                log.error("Unknown Internal Server Error occurred :: {} :: {}", e.getMessage(), e);
                return false;
            }
                },10, 100, "Delete Participant Connection " + DataType.CONNECTION_CAMERA.toString()
        );

        Map<String, ConnectionOutVo> updatedConnections = openViduDto.getSession().getConnections();
        updatedConnections.remove("con_camera_"+userIdx);
        updatedConnections.remove("con_screen_"+userIdx);
        OpenViduDto updatedOpenViduData = OpenViduDto.builder()
                .creator(openViduDto.getCreator())
                .isBroadcastingActive(openViduDto.isBroadcastingActive())
                .isRecordingActive(openViduDto.isRecordingActive())
                .broadcastingEnabled(openViduDto.isBroadcastingEnabled())
                .recordingEnabled(openViduDto.isRecordingEnabled())
                .session(SessionOutVo.of(openViduDto.getSession(), updatedConnections))
                .build();

        ThreadUtils.runTask(() -> {
            try {
                redisUtils.leftUserJob(sessionId, updatedOpenViduData, UserOutVo.of(leftUser, false));
                return true;
            } catch (Exception e) {
                log.error("Unknown Exception :: {} : {}", e.getMessage(), e);
                return false;
            }
        }, 10, 100, "Left User");

    }
}
