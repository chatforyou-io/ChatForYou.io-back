package com.chatforyou.io.batch;

import com.chatforyou.io.models.JwtPayload;
import com.chatforyou.io.models.out.SessionOutVo;
import com.chatforyou.io.services.ChatRoomService;
import com.chatforyou.io.services.OpenViduService;
import com.chatforyou.io.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchJob {
    private final RedisUtils redisUtils;
    private final ChatRoomService chatRoomService;
    private final OpenViduService openViduService;

//    @Scheduled(cron = "*/10 * * * * *", zone = "Asia/Seoul")
    @Scheduled(cron = "0 0,30 * * * *", zone = "Asia/Seoul")
    public void chatRoomScheduleJob() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startTime = sdf.format(new Date());
        log.info("=== ChatRoom Batch Job Start :: {} ==== ", startTime);

        int totalProcessed = 0;
        int successCount = 0;
        int failCount = 0;

        try {
            List<String> emptyRooms = redisUtils.getSessionListForDelete(); // 사용자가 0 인 세션 확인
            List<String> overflowRooms = redisUtils.getSessionListForOverflow(); // 현재 사용자 수가 최대 유저 수보다 많은 세션 확인
            List<String> activeSessionList = openViduService.getActiveSessionOutVoList()
                    .stream()
                    .map(SessionOutVo::getSessionId)
                    .collect(Collectors.toList());
            List<String> inactiveRooms = redisUtils.getSessionListForInactive(activeSessionList); // redis 에는 active 상태이나 openvidu 에서는 inactive 세션

            Set<String> uniqueSessions = new HashSet<>();
            uniqueSessions.addAll(emptyRooms);
            uniqueSessions.addAll(overflowRooms);
            uniqueSessions.addAll(inactiveRooms);
            List<String> sessionList = new ArrayList<>(uniqueSessions); // set 에 넣어서 중복제거

            totalProcessed = sessionList.size();

            if (totalProcessed > 0) {
                log.info("Total {} chat rooms to delete (Empty rooms: {}, Overflow rooms: {}, inactive rooms: {})",
                        totalProcessed, emptyRooms.size(), overflowRooms.size(), inactiveRooms.size());

                for (String sessionId : sessionList) {
                    try {
                        chatRoomService.deleteChatRoom(sessionId, JwtPayload.builder().build(), true);
                        successCount++;
                    } catch (Exception e) {
                        failCount++;
                        log.error("Error while deleting session {}: {}", sessionId, e.getMessage(), e);
                    }
                }
            } else {
                log.info("No chat rooms to delete.");
            }
        } catch (Exception e) {
            log.error("Error occurred during chat room batch job: {}", e.getMessage(), e);
        }

        String endTime = sdf.format(new Date());
        log.info("=== ChatRoom Batch Job Completed :: {} ==== ", endTime);
        log.info("Processing result: Total {} attempted, {} succeeded, {} failed", totalProcessed, successCount, failCount);
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
