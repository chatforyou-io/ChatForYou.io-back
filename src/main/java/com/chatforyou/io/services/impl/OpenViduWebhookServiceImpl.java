package com.chatforyou.io.services.impl;

import com.chatforyou.io.client.OpenViduHttpException;
import com.chatforyou.io.client.OpenViduJavaClientException;
import com.chatforyou.io.entity.User;
import com.chatforyou.io.models.DataType;
import com.chatforyou.io.models.OpenViduWebhookData;
import com.chatforyou.io.models.out.ChatRoomOutVo;
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
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenViduWebhookServiceImpl implements OpenViduWebhookService {
    private final UserRepository userRepository;

    private final RedisUtils redisUtils;
    private final ChatRoomService chatRoomService;
    private final OpenViduService openViduService;

    @Override
    public void processWebhookEvent(OpenViduWebhookData webhookData) throws OpenViduJavaClientException, OpenViduHttpException {
        String sessionId = webhookData.getSessionId();
        ChatRoomOutVo chatRoom = null;
        try {
            chatRoomService.findChatRoomBySessionId(sessionId);
        } catch (Exception e) {
            log.warn("ChatRoom does not Exist :: {}", sessionId);
        }

        log.info("====== WebHookData ::: {}", webhookData.toString());

        switch (webhookData.getEvent()) {
            case PARTICIPANT_LEFT:  // 유저 접속 종료
                String connectionId = webhookData.getConnectionId();
                Long userIdx = Long.parseLong(connectionId.split("_")[2]);
                if (connectionId.contains("camera")) {

                } else {

                }
                processParticipantLeftEvent(sessionId, chatRoom, userIdx);
                break;
            case SESSION_DESTROYED: // session 삭제
                chatRoomService.deleteChatRoom(sessionId);
                break;
        }

    }

    private void processParticipantLeftEvent(String sessionId, ChatRoomOutVo chatRoom, Long userIdx) {
        User user = userRepository.findUserByIdx(userIdx)
                .orElseThrow(() -> new EntityNotFoundException("Can not find User"));

        //
        ThreadUtils.runTask(() ->
                        redisUtils.deleteKeysByKey(DataType.redisDataTypeConnection(sessionId, String.valueOf(userIdx), DataType.CONNECTION_CAMERA)),
                10, 100, "Delete Participant Connection " + DataType.CONNECTION_CAMERA.toString()
        );

        ThreadUtils.runTask(() ->
                        redisUtils.deleteKeysByKey(DataType.redisDataTypeConnection(sessionId, String.valueOf(userIdx), DataType.CONNECTION_SCREEN)),
                10, 100, "Delete Participant Connection " + DataType.CONNECTION_SCREEN.toString()
        );


        // chatroom 에서 유저 정보 제거
        List<UserOutVo> userList = redisUtils.getObject(DataType.redisDataType(sessionId, DataType.USER_LIST), List.class);
        boolean isDelete = false;
        if (!CollectionUtils.isEmpty(userList)) {
            Iterator<UserOutVo> iterator = userList.iterator();

            // Iterator 사용하여 리스트 순회
            while (iterator.hasNext()) {
                UserOutVo userOutVo = iterator.next();

                // 유저의 IDX가 일치하는지 확인
                if (userOutVo.getIdx() == user.getIdx()) {
                    iterator.remove();
                    isDelete = true;
                    break;
                }
            }

            // 변경된 유저 리스트를 Redis에 다시 저장
            if (isDelete) {
                chatRoom.setUserList(userList);
                chatRoom.setCurrentUserCount(CollectionUtils.isEmpty(userList) ? 0 : userList.size());
                ThreadUtils.runTask(() -> {
                    try {
                        redisUtils.leftUserJob(sessionId, chatRoom, userList);
                        return true;
                    } catch (Exception e) {
                        log.error("Unknown Exception :: {} : {}", e.getMessage(), e);
                        return false;
                    }
                }, 10, 10, "Left User");
            }
        }
    }
}
