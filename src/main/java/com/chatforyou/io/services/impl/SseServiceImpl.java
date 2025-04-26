package com.chatforyou.io.services.impl;

import com.chatforyou.io.models.out.ChatRoomOutVo;
import com.chatforyou.io.models.out.UserOutVo;
import com.chatforyou.io.models.sse.SseSubscriber;
import com.chatforyou.io.models.sse.SseType;
import com.chatforyou.io.services.SseService;
import com.chatforyou.io.services.SseSubscriberService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SseServiceImpl implements SseService {
    private final SseSubscriberService sseSubscriberService;
    private final int keepAliveTimeout;

    @Autowired
    public SseServiceImpl(SseSubscriberService sseSubscriberService, @Value("${sse.keep-alive-timeout:10}") int keepAliveTimeout) {
        this.sseSubscriberService = sseSubscriberService;
        this.keepAliveTimeout = keepAliveTimeout;
    }

    @Override
    public Flux<ServerSentEvent<?>> subscribeRoomList(Long userIdx) {
        SseSubscriber subscriber = SseSubscriber.of(userIdx, SseType.ROOM_LIST, null, sseSubscriberService, this.keepAliveTimeout)
                .buildFlux()
                .startKeepAlive();
        sseSubscriberService.addRoomListSubscriber(userIdx, subscriber.getSink());
        return subscriber.getFlux();
    }

    @Override
    public Flux<ServerSentEvent<?>> subscribeUserList(Long userIdx) {
        SseSubscriber subscriber = SseSubscriber.of(userIdx, SseType.USER_LIST, null, sseSubscriberService, this.keepAliveTimeout)
                .buildFlux()
                .startKeepAlive();
        sseSubscriberService.addUserListSubscriber(userIdx, subscriber.getSink());
        return subscriber.getFlux();
    }

    @Override
    public Flux<ServerSentEvent<?>> subscribeRoomInfo(Long userIdx, String roomId) {
        SseSubscriber subscriber = SseSubscriber.of(userIdx, SseType.ROOM_INFO, roomId, sseSubscriberService, this.keepAliveTimeout)
                .buildFlux()
                .startKeepAlive();
        sseSubscriberService.addRoomInfoSubscriber(roomId, userIdx, subscriber.getSink());
        return subscriber.getFlux();
    }

    @Override
    public void notifyChatRoomList(List<ChatRoomOutVo> chatRoomList) {
        Map<String, List<ChatRoomOutVo>> result = Map.of("data", chatRoomList);
        ServerSentEvent<Map<String, List<ChatRoomOutVo>>> event = ServerSentEvent.<Map<String, List<ChatRoomOutVo>>>builder()
                .event("updateChatroomList")
                .data(result)
                .build();

        sseSubscriberService.getRoomListSubscribers().forEach((userId, sink) -> {
            if (sink.tryEmitNext(event).isFailure()) {
                log.warn("[SSE] Failed to emit chat room list update event");
                sseSubscriberService.removeRoomListSubscriber(userId);
            } else {
                log.debug("[SSE] Notified chat room list update event");
            }
        });
    }

    @Override
    public void notifyChatRoomInfo(ChatRoomOutVo chatRoomInfo) {
        Map<String, ChatRoomOutVo> result = Map.of("data", chatRoomInfo);
        ServerSentEvent<Map<String, ChatRoomOutVo>> event = ServerSentEvent.<Map<String, ChatRoomOutVo>>builder()
                .event("updateChatroomInfo")
                .data(result)
                .build();

        Map<Long, Sinks.Many<ServerSentEvent<?>>> sinks =
                sseSubscriberService.getRoomInfoSubscribersByRoomId(chatRoomInfo.getSessionId());
        if (!CollectionUtils.isEmpty(sinks)) {
            sinks.forEach(((userId, sink) -> {
                if (sink.tryEmitNext(event).isFailure()) {
                    log.warn("[SSE] Failed to emit chat room info update event");
                    sseSubscriberService.removeRoomInfoSubscriber(chatRoomInfo.getSessionId(), userId);
                } else {
                    log.debug("[SSE] Notified chat room info update event");
                }
            }));
        }
    }

    @Override
    public void notifyUserList(List<UserOutVo> userList, List<UserOutVo> loginUserList) {
        Map<String, Object> userListData = Map.of(
                "userList", userList,
                "loginUserList", loginUserList
        );
        ServerSentEvent<Map<String, Object>> event = ServerSentEvent.<Map<String, Object>>builder()
                .event("updateUserList")
                .data(Map.of("data", userListData))
                .build();

        sseSubscriberService.getUserListSubscribers().forEach((userId, sink) -> {
            if (sink.tryEmitNext(event).isFailure()) {
                log.warn("[SSE] Failed to emit user list update event");
                sseSubscriberService.removeUserListSubscriber(userId);
            } else {
                log.debug("[SSE] Notified user list update event");
            }
        });
    }
}