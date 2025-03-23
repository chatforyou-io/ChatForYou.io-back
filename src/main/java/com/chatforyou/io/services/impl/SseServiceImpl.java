package com.chatforyou.io.services.impl;

import com.chatforyou.io.config.SchedulerConfig;
import com.chatforyou.io.models.out.UserOutVo;
import com.chatforyou.io.models.sse.SseSubscriber;
import com.chatforyou.io.models.sse.SseType;
import com.chatforyou.io.models.out.ChatRoomOutVo;
import com.chatforyou.io.services.SseService;
import com.chatforyou.io.services.SseSubscriberService;
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

    private final SseSubscriberService sseSubscriberService;
    private final SchedulerConfig schedulerConfig;

    @Override
    public SseEmitter subscribeRoomList(Long userIdx) {
        SseSubscriber sseSubscriber = this.createSseSubscriber(userIdx, SseType.ROOM_LIST);
        sseSubscriberService.addRoomListSubscriber(userIdx, sseSubscriber);
        return sseSubscriber.getSseEmitter();
    }

    @Override
    public SseEmitter subscribeUserList(Long userIdx) {
        SseSubscriber sseSubscriber = this.createSseSubscriber(userIdx, SseType.USER_LIST);
        sseSubscriberService.addUserListSubscriber(userIdx, sseSubscriber);
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
        sseSubscriberService.getRoomListSubscribers().values()
                .forEach((subscriber) -> {
                    try {
                        subscriber.getSseEmitter().send(SseEmitter.event().name("updateChatroomList").data(result));
                        log.debug("notifyChatRoomList To {}", subscriber.getUserIdx());
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
                log.debug("notifyChatRoomInfo To {}", subscriber.getUserIdx());
            } catch (IOException | RuntimeException e){
                subscriber.cleanupSubscriber();
                sseSubscriberService.removeRoomInfoSubscriber(chatRoomInfo.getSessionId(), subscriber);
            }
        });
    }

    @Override
    public void notifyUserList(List<UserOutVo> userList, List<UserOutVo> loginUserList) {

        Map<String, Object> result = Map.of(
                "userList", userList,      // 전체 유저
                "loginUserList", loginUserList // 로그인된 유저
        );

        sseSubscriberService.getUserListSubscribers().values().forEach(subscriber -> {
            try {
                subscriber.getSseEmitter().send(
                        SseEmitter.event()
                                .name("updateUserList")
                                .data(result)
                );
                log.debug("notifyUserList To {}", subscriber.getUserIdx());
            } catch (IOException | RuntimeException e) {
                subscriber.cleanupSubscriber();
                sseSubscriberService.removeUserListSubscriber(subscriber.getUserIdx());
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
            sseSubscriberService.removeRoomInfoSubscriber(sessionId, sseSubscriber);
        }
        return sseSubscriber;
    }

    private SseSubscriber createSseSubscriber(Long userIdx, SseType type) {
        SseEmitter sseEmitter = new SseEmitter(type.getTimeOut());
        SseSubscriber sseSubscriber = SseSubscriber.of(userIdx, sseEmitter, schedulerConfig.scheduledExecutorService());
        try{
            sseSubscriber.scheduleKeepAliveTask();
        } catch (IOException | RuntimeException e){
            log.error("Runtime error while scheduling keep-alive task for user {}: {}", userIdx, e.getMessage(), e);
            sseSubscriber.cleanupSubscriber();
            if(type.equals(SseType.ROOM_INFO)){
                sseSubscriberService.removeRoomListSubscriber(userIdx);
            } else if(type.equals(SseType.USER_LIST)){
                sseSubscriberService.removeUserListSubscriber(userIdx);
            }
        }
        return sseSubscriber;
    }
}
