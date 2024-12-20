package com.chatforyou.io.batch;

import com.chatforyou.io.client.OpenViduHttpException;
import com.chatforyou.io.client.OpenViduJavaClientException;
import com.chatforyou.io.models.JwtPayload;
import com.chatforyou.io.services.ChatRoomService;
import com.chatforyou.io.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchJob {
    private final RedisUtils redisUtils;
    private final ChatRoomService chatRoomService;

    @Scheduled(cron = "0 0,30 * * * *", zone = "Asia/Seoul")
//    @Scheduled(cron = "*/10 * * * * *", zone = "Asia/Seoul")
    public void chatRoomScheduleJob() throws OpenViduJavaClientException, OpenViduHttpException, BadRequestException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        log.info("=== ChatRoom Batch Job Start :: {} ==== ", sdf.format(new Date().getTime()));
        List<String> sessionList = redisUtils.getSessionListForDelete();
        for (String sessionId : sessionList) {
            chatRoomService.deleteChatRoom(sessionId, JwtPayload.builder().build(), true);
        }
        log.info("=== ChatRoom Batch Job End :: {} ==== ", sdf.format(new Date().getTime()));
    }

    @Scheduled(cron = "0 0 */3 * * *", zone = "Asia/Seoul")
//    @Scheduled(cron = "*/10 * * * * *", zone = "Asia/Seoul")
    public void deleteInactiveUsersJob() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        log.info("=== login User Batch Job Start :: {} ==== ", sdf.format(new Date().getTime()));
        redisUtils.deleteInactiveUsers();
        log.info("=== login User Job End :: {} ==== ", sdf.format(new Date().getTime()));
    }
}
