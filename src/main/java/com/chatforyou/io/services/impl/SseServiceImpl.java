package com.chatforyou.io.services.impl;

import com.chatforyou.io.config.SchedulerConfig;
import com.chatforyou.io.models.sse.SseSubscriber;
import com.chatforyou.io.models.sse.SseType;
import com.chatforyou.io.models.out.ChatRoomOutVo;
import com.chatforyou.io.services.JwtService;
import com.chatforyou.io.services.SseService;
import com.chatforyou.io.services.SseSubscriberService;
import com.chatforyou.io.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseServiceImpl implements SseService {

    private final JwtService jwtService;
    // TODO 만약 레디스를 사용하면 유저 로그아웃 시 해당 정보를 sse 정보에서도 삭제 필요!! 방에 대한 처리도 마찬가지
    private final RedisUtils redisUtils;
    private final SseSubscriberService sseSubscriberService;
    private final SchedulerConfig schedulerConfig;

    @Override
    public SseEmitter subscribeRoomList(Long userIdx) {

        SseSubscriber sseSubscriber = this.createSseSubscriber(userIdx, "", SseType.ROOM_LIST);
        sseSubscriberService.addRoomListSubscriber(userIdx, sseSubscriber);
        return sseSubscriber.getSseEmitter();
    }

    @Override
    public SseEmitter subscribeRoomInfo(Long userIdx, String sessionId) {
        SseSubscriber sseSubscriber = this.createSseSubscriber(userIdx, sessionId, SseType.ROOM_INFO);
        sseSubscriberService.addRoomInfoSubscriber(sessionId, sseSubscriber);
        return sseSubscriber.getSseEmitter();
    }

    @Override
    public void notifyChatRoomList(List<ChatRoomOutVo> chatRoomList) {
        Map<String, List<ChatRoomOutVo>> result = Map.of("data", chatRoomList);
        sseSubscriberService.getAllRoomListSubscribers().values()
                .forEach((subscriber) -> {
                    try {
                        subscriber.getSseEmitter().send(SseEmitter.event().name("updateChatroomList").data(result));
                        log.info("notifyChatRoomList To {}", subscriber.getUserIdx());
                    } catch (IOException | RuntimeException e){
                        subscriber.cleanupSubscriber();
                        sseSubscriberService.removeRoomListSubscriber(subscriber.getUserIdx());
                    }
                });
    }

    @Override
    public void notifyChatRoomInfo(ChatRoomOutVo chatRoomInfo) {
        Collection<SseSubscriber> subscribers = sseSubscriberService.getRoomInfoSubscribersByRoomId(chatRoomInfo.getSessionId());
        Map<String, ChatRoomOutVo> result = Map.of("data", chatRoomInfo);
        subscribers.forEach(subscriber -> {
            try {
                subscriber.getSseEmitter().send(SseEmitter.event().name("updateChatroomInfo").data(result));
                log.info("notifyChatRoomInfo To {}", subscriber.getUserIdx());
            } catch (IOException | RuntimeException e){
                subscriber.cleanupSubscriber();
                sseSubscriberService.removeRoomInfoSubscriber(chatRoomInfo.getSessionId(), subscriber);
            }
        });
    }

    private SseSubscriber createSseSubscriber(Long userIdx, String sessionId, SseType type) {
        SseEmitter sseEmitter = new SseEmitter(type.getTimeOut());
        SseSubscriber sseSubscriber = SseSubscriber.of(userIdx, sseEmitter, schedulerConfig.scheduledExecutorService());
        try{
            sseSubscriber.scheduleKeepAliveTask();
        } catch (IOException | RuntimeException e){
            log.error(e.getMessage());
            sseSubscriber.cleanupSubscriber();
            sseSubscriberService.removeRoomListSubscriber(userIdx);
            if(!sessionId.isEmpty()) {
                sseSubscriberService.removeRoomInfoSubscriber(sessionId, sseSubscriber);
            }
        }

        return sseSubscriber;
    }
}
