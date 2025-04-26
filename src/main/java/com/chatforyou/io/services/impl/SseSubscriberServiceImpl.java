package com.chatforyou.io.services.impl;

import com.chatforyou.io.models.sse.SseType;
import com.chatforyou.io.repository.SubscriberRepository;
import com.chatforyou.io.services.SseSubscriberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SseSubscriberServiceImpl implements SseSubscriberService {

    private final SubscriberRepository<Long, Sinks.Many<ServerSentEvent<?>>> roomListSubscriberRepository;
    private final SubscriberRepository<Long, Sinks.Many<ServerSentEvent<?>>> userListSubscriberRepository;
    private final SubscriberRepository<String, Map<Long, Sinks.Many<ServerSentEvent<?>>>> roomInfoSubscriberRepository;

    @Override
    public void addRoomListSubscriber(Long userIdx, Sinks.Many<ServerSentEvent<?>> sink) {
        roomListSubscriberRepository.add(userIdx, sink);
    }

    @Override
    public void addRoomInfoSubscriber(String roomId, Long userIdx, Sinks.Many<ServerSentEvent<?>> sink) {
        Map<Long, Sinks.Many<ServerSentEvent<?>>> subscribers = roomInfoSubscriberRepository.get(roomId);
        if (subscribers == null) {
            subscribers = new ConcurrentHashMap<>();
            roomInfoSubscriberRepository.add(roomId, subscribers);
        }
        subscribers.put(userIdx, sink);
    }

    @Override
    public void addUserListSubscriber(Long userIdx, Sinks.Many<ServerSentEvent<?>> sink) {
        userListSubscriberRepository.add(userIdx, sink);
    }

    @Override
    public Map<Long, Sinks.Many<ServerSentEvent<?>>> getRoomListSubscribers() {
        return roomListSubscriberRepository.getAll();
    }

    @Override
    public Map<Long, Sinks.Many<ServerSentEvent<?>>> getRoomInfoSubscribersByRoomId(String roomId) {
        return roomInfoSubscriberRepository.get(roomId);
    }

    @Override
    public Map<Long, Sinks.Many<ServerSentEvent<?>>> getUserListSubscribers() {
        return userListSubscriberRepository.getAll();
    }

    @Override
    public void removeRoomListSubscriber(Long userIdx) {
        roomListSubscriberRepository.remove(userIdx);
    }

    @Override
    public void removeRoomInfoSubscriber(String roomId, Long userIdx) {
        Map<Long, Sinks.Many<ServerSentEvent<?>>> subscribers = roomInfoSubscriberRepository.get(roomId);
        if (subscribers != null) {
            subscribers.remove(userIdx);
        }
    }

    @Override
    public void removeUserListSubscriber(Long userIdx) {
        userListSubscriberRepository.remove(userIdx);
    }

    @Override
    public void removeSubscriber(SseType type, Long userIdx, String sessionId) {
        switch (type) {
            case ROOM_LIST -> this.removeRoomListSubscriber(userIdx);
            case USER_LIST -> this.removeUserListSubscriber(userIdx);
            case ROOM_INFO -> this.removeRoomInfoSubscriber(sessionId, userIdx);
        }
    }
}
