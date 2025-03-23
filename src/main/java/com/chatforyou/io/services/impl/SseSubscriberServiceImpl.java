package com.chatforyou.io.services.impl;

import com.chatforyou.io.models.sse.SseSubscriber;
import com.chatforyou.io.repository.SubscriberRepository;
import com.chatforyou.io.services.SseSubscriberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SseSubscriberServiceImpl implements SseSubscriberService {
    @Qualifier("roomListSubscriberRepository")
    private final SubscriberRepository<Long, SseSubscriber> roomListSubscriberRepository;
    @Qualifier("userListSubscriberRepository")
    private final SubscriberRepository<Long, SseSubscriber> userListSubscriberRepository;
    private final SubscriberRepository<String, Map<Long, SseSubscriber>> roomInfoSubscriberRepository;

    @Override
    public void addRoomListSubscriber(Long userIdx, SseSubscriber subscriber) {
        this.roomListSubscriberRepository.add(userIdx, subscriber);
    }

    @Override
    public void addRoomInfoSubscriber(String roomId, SseSubscriber subscriber) {
        Map<Long, SseSubscriber> subscribers = this.roomInfoSubscriberRepository.get(roomId);

        // 만약 구독자 목록이 없다면 새로 생성하여 추가
        if (subscribers == null) {
            subscribers = new ConcurrentHashMap<>();
            this.roomInfoSubscriberRepository.add(roomId, subscribers);
        }

        // userIdx를 키로 사용하여 중복 체크 없이 바로 추가 또는 교체
        subscribers.put(subscriber.getUserIdx(), subscriber);
    }

    @Override
    public void addUserListSubscriber(Long userIdx, SseSubscriber subscriber) {
        this.userListSubscriberRepository.add(userIdx, subscriber);
    }

    @Override
    public Map<Long, SseSubscriber> getRoomListSubscribers() {
        return this.roomListSubscriberRepository.getAll();
    }

    @Override
    public Collection<SseSubscriber> getRoomInfoSubscribersByRoomId(String sessionId) {
        return this.roomInfoSubscriberRepository.get(sessionId).values();
    }

    @Override
    public Map<Long, SseSubscriber> getUserListSubscribers() {
        return this.userListSubscriberRepository.getAll();
    }

    @Override
    public void removeRoomListSubscriber(Long userIdx) {
        this.roomListSubscriberRepository.remove(userIdx);
    }

    @Override
    public void removeRoomInfoSubscriber(String roomId, SseSubscriber subscriber) {
        this.roomInfoSubscriberRepository.get(roomId).keySet()
                .removeIf(u -> u.longValue() == subscriber.getUserIdx());
    }

    @Override
    public void removeUserListSubscriber(Long userIdx) {
        this.userListSubscriberRepository.remove(userIdx);
    }
}
