package com.chatforyou.io.services.impl;

import com.chatforyou.io.models.sse.SseSubscriber;
import com.chatforyou.io.models.sse.SseType;
import com.chatforyou.io.models.out.ChatRoomOutVo;
import com.chatforyou.io.services.JwtService;
import com.chatforyou.io.services.SseService;
import com.chatforyou.io.services.SseSubscriberService;
import com.chatforyou.io.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SseServiceImpl implements SseService {

    private final JwtService jwtService;
    // TODO 만약 레디스를 사용하면 유저 로그아웃 시 해당 정보를 sse 정보에서도 삭제 필요!! 방에 대한 처리도 마찬가지
    private final RedisUtils redisUtils;
    private final SseSubscriberService sseSubscriberService;

    @Override
    public SseEmitter subscribeRoomList(Long userIdx) {
        SseEmitter sseEmitter = this.createSseSubscriber(userIdx, SseType.ROOM_LIST);
        sseSubscriberService.addRoomListSubscriber(userIdx, SseSubscriber.of(userIdx, sseEmitter));
        return sseEmitter;
    }

    @Override
    public SseEmitter subscribeRoomInfo(Long userIdx, String sessionId) {
        SseEmitter sseEmitter = this.createSseSubscriber(userIdx, SseType.ROOM_INFO);
        sseSubscriberService.addRoomInfoSubscriber(sessionId, SseSubscriber.of(userIdx, sseEmitter));
        return sseEmitter;
    }

    private void notifyConnection(SseEmitter sseEmitter) {
        Map<String, String> result = new ConcurrentHashMap<>();
        result.put("status", "connected");
        try {
            sseEmitter.send(SseEmitter.event().name("connectionStatus").data(result));
        } catch (IOException e) {
            // TODO 예외처리 작업 필요
            throw new RuntimeException(e);
        }
    }

    @Override
    public void notifyChatRoomList(List<ChatRoomOutVo> chatRoomList) {
        Map<String, List<ChatRoomOutVo>> result = new ConcurrentHashMap<>();
        result.put("data", chatRoomList);
        sseSubscriberService.getAllRoomListSubscribers().values()
                .forEach((subscriber) -> {
                    try {
                        subscriber.getSseEmitter().send(SseEmitter.event().name("updateChatRoomList").data(result));
                    } catch (IOException e) {
                        throw new RuntimeException("Unknown sseEmitter error", e);
                    }
                });
    }

    @Override
    public void notifyChatRoomInfo(ChatRoomOutVo chatRoomInfo) {


//        roomInfoSubscribers.values()
//                .forEach(subscriber -> {
//                    try {
//                        subscriber.getSseEmitter().send(SseEmitter.event().name("update_chatroom_info").data(chatRoomInfo));
//                    } catch (IOException e) {
//                        throw new RuntimeException("Unknown sseEmitter error", e);
//                    }
//                });
    }

    private SseEmitter createSseSubscriber(Long userIdx, SseType type) {
        SseEmitter sseEmitter = new SseEmitter(type.getTimeOut());
        // timeout 과 complete 구현 필요
        sseEmitter.onCompletion(() -> sseSubscriberService.getAllRoomListSubscribers().remove(userIdx));
        sseEmitter.onTimeout(() -> sseSubscriberService.getAllRoomListSubscribers().remove(userIdx));
        this.notifyConnection(sseEmitter);
        this.notifyKeepAlive();
        return sseEmitter;
    }

    private void notifyKeepAlive() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                Map<String, String> result = new ConcurrentHashMap<>();
                result.put("status", "ping");
                for (SseSubscriber subscriber : sseSubscriberService.getAllRoomListSubscribers().values()) {
                    subscriber.getSseEmitter().send(SseEmitter.event().name("keepAlive").data(result));
                }
            } catch (IOException e) {

            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
        }, 25, 25, TimeUnit.SECONDS);
    }
}
