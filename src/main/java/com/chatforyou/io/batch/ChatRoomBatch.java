package com.chatforyou.io.batch;

import com.chatforyou.io.client.OpenViduHttpException;
import com.chatforyou.io.client.OpenViduJavaClientException;
import com.chatforyou.io.services.ChatRoomService;
import com.chatforyou.io.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatRoomBatch {
    private final RedisUtils redisUtils;
    private final ChatRoomService chatRoomService;

    @Scheduled(cron = "0 0,30 * * * *", zone = "Asia/Seoul")
    public void chatRoomScheduledJob() throws OpenViduJavaClientException, OpenViduHttpException {
        List<String> sessionList = redisUtils.getSessionListForDelete();
        for (String sessionId : sessionList) {
            chatRoomService.deleteChatRoom(sessionId);
        }
    }
}
