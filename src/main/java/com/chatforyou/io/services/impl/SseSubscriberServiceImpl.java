package com.chatforyou.io.services.impl;

import com.chatforyou.io.models.sse.SseSubscriber;
import com.chatforyou.io.repository.SubscriberRepository;
import com.chatforyou.io.services.SseSubscriberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SseSubscriberServiceImpl implements SseSubscriberService {
    private final SubscriberRepository<Long, SseSubscriber> dashboardRepository;
    private final SubscriberRepository<String, Map<Long, SseSubscriber>> roomInfoRepository;

    @Override
    public void addDashboardSubscriber(Long userIdx, SseSubscriber subscriber) {
        dashboardRepository.add(userIdx, subscriber);
    }

    @Override
    public void addRoomInfoSubscriber(String roomId, SseSubscriber subscriber) {
        Map<Long, SseSubscriber> subscribers = roomInfoRepository.get(roomId);

        // 만약 구독자 목록이 없다면 새로 생성하여 추가
        if (subscribers == null) {
            subscribers = new ConcurrentHashMap<>();
            roomInfoRepository.add(roomId, subscribers);
        }

        // userIdx를 키로 사용하여 중복 체크 없이 바로 추가 또는 교체
        subscribers.put(subscriber.getUserIdx(), subscriber);
    }

    @Override
    public Map<Long, SseSubscriber> getDashboardSubscribers() {
        return dashboardRepository.getAll();
    }

    @Override
    public Collection<SseSubscriber> getRoomInfoSubscribersByRoomId(String sessionId) {
        return roomInfoRepository.get(sessionId).values();
    }

    @Override
    public void removeDashboardSubscriber(Long userIdx) {
        this.dashboardRepository.remove(userIdx);
    }

    @Override
    public void removeRoomInfoSubscriber(String roomId, SseSubscriber subscriber) {
        this.roomInfoRepository.get(roomId).remove(subscriber.getUserIdx());
    }
}
