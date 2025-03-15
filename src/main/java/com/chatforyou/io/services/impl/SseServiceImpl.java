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

        SseSubscriber sseSubscriber = this.createSseSubscriber(userIdx, SseType.ROOM_LIST);
        sseSubscriberService.addRoomListSubscriber(userIdx, sseSubscriber);
        return sseSubscriber.getSseEmitter();
    }

    @Override
    public SseEmitter subscribeRoomInfo(Long userIdx, String sessionId) {
        SseSubscriber sseSubscriber = this.createSseSubscriber(userIdx, SseType.ROOM_INFO);
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
                    } catch (IOException e) {
                        subscriber.cleanupSubscriber();
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
            } catch (IOException e) {
                throw new RuntimeException("Unknown sseEmitter error", e);
            }
        });
    }

    private SseSubscriber createSseSubscriber(Long userIdx, SseType type) {
        SseEmitter sseEmitter = new SseEmitter(type.getTimeOut());
        SseSubscriber sseSubscriber = SseSubscriber.of(userIdx, sseEmitter, schedulerConfig.scheduledExecutorService());
        sseSubscriber.scheduleKeepAliveTask();
        return sseSubscriber;
    }
}
