package com.chatforyou.io.services.impl;

import com.chatforyou.io.client.OpenViduHttpException;
import com.chatforyou.io.client.OpenViduJavaClientException;
import com.chatforyou.io.models.DataType;
import com.chatforyou.io.models.OpenViduDto;
import com.chatforyou.io.models.OpenViduEvent;
import com.chatforyou.io.models.OpenViduWebhookData;
import com.chatforyou.io.models.out.ChatRoomOutVo;
import com.chatforyou.io.services.ChatRoomService;
import com.chatforyou.io.services.OpenViduService;
import com.chatforyou.io.services.OpenViduWebhookService;
import com.chatforyou.io.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenViduWebhookServiceImpl implements OpenViduWebhookService {

    private final RedisUtils redisUtils;
    private final ChatRoomService chatRoomService;
    private final OpenViduService openViduService;

    @Override
    public void processWebhookEvent(OpenViduWebhookData webhookData) throws OpenViduJavaClientException, OpenViduHttpException {
        String sessionId = webhookData.getSessionId();
        OpenViduDto openViduDto = redisUtils.getObject(DataType.redisDataKey(sessionId, DataType.OPENVIDU), OpenViduDto.class);
        ChatRoomOutVo chatRoomOutVo = redisUtils.getObject(DataType.redisDataKey(sessionId, DataType.CHATROOM), ChatRoomOutVo.class);
        log.info("====== WebHookData ::: {}", webhookData.toString());

//        switch (webhookData.getEvent()){
//            case PARTICIPANT_LEFT:  // 유저 접속 종료
//                String connectionId = webhookData.getConnectionId();
//                String userIdx = connectionId.split("_")[2];
//                if (connectionId.contains("camera")) {
//
//                } else {
//
//                }
//
//                break;
//            case SESSION_DESTROYED: // session 삭제
//                chatRoomService.deleteChatRoom(sessionId);
//                break;
//        }

    }
}
