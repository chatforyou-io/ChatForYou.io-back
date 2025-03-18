package com.chatforyou.io.services.impl;

import com.chatforyou.io.client.OpenViduHttpException;
import com.chatforyou.io.client.OpenViduJavaClientException;
import com.chatforyou.io.entity.User;
import com.chatforyou.io.models.DataType;
import com.chatforyou.io.models.JwtPayload;
import com.chatforyou.io.models.OpenViduDto;
import com.chatforyou.io.models.OpenViduWebhookData;
import com.chatforyou.io.models.out.ConnectionOutVo;
import com.chatforyou.io.models.out.SessionOutVo;
import com.chatforyou.io.models.out.UserOutVo;
import com.chatforyou.io.repository.UserRepository;
import com.chatforyou.io.services.ChatRoomService;
import com.chatforyou.io.services.OpenViduWebhookService;
import com.chatforyou.io.services.SseService;
import com.chatforyou.io.utils.RedisUtils;
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
    private final SseService sseService;

    @Override
    public void processWebhookEvent(OpenViduWebhookData webhookData) throws OpenViduJavaClientException, OpenViduHttpException, BadRequestException {
        log.info("====== WebHookData ::: {}", webhookData.toString());

        String sessionId = webhookData.getSessionId();
        OpenViduDto openViduDto = null;
        try {
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

    private void processParticipantLeftEvent(Long userIdx, String sessionId, OpenViduDto openViduDto) throws BadRequestException {
        User leftUser = userRepository.findUserByIdx(userIdx)
                .orElseThrow(() -> new EntityNotFoundException("Can not find User"));

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

        // redis 에서 유저 connection token 및 방에서 유저 삭제
        redisUtils.deleteConnectionTokens(sessionId, String.valueOf(userIdx));
        redisUtils.leftUserJob(sessionId, updatedOpenViduData, UserOutVo.of(leftUser, false));

        try{
            // sse 이벤트 전송
            sseService.notifyChatRoomInfo(chatRoomService.findChatRoomBySessionId(sessionId));
        } catch (Exception e){
            throw new BadRequestException("Unknown Error :: {}", e);
        }


        log.info("Delete Participant Connection success");
        log.info("Left User success");
    }
}
