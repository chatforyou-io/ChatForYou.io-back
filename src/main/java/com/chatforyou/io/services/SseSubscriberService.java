package com.chatforyou.io.services;

import com.chatforyou.io.models.sse.SseType;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Sinks;

import java.util.Map;

public interface SseSubscriberService {
    void addRoomListSubscriber(Long userIdx, Sinks.Many<ServerSentEvent<?>> sink);
    void addRoomInfoSubscriber(String roomId, Long userIdx, Sinks.Many<ServerSentEvent<?>> sink);
    void addUserListSubscriber(Long userIdx, Sinks.Many<ServerSentEvent<?>> sink);
    Map<Long, Sinks.Many<ServerSentEvent<?>>> getRoomListSubscribers();
    Map<Long, Sinks.Many<ServerSentEvent<?>>> getRoomInfoSubscribersByRoomId(String roomId);
    Map<Long, Sinks.Many<ServerSentEvent<?>>> getUserListSubscribers();
    void removeRoomListSubscriber(Long userIdx);
    void removeRoomInfoSubscriber(String roomId, Long userIdx);
    void removeUserListSubscriber(Long userIdx);
    void removeSubscriber(SseType type, Long userIdx, String sessionId);
}
