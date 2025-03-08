package com.chatforyou.io.services.impl;

import com.chatforyou.io.models.sse.SseSubscriber;
import com.chatforyou.io.repository.SubscriberRepository;
import com.chatforyou.io.services.SseSubscriberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SseSubscriberServiceImpl implements SseSubscriberService {
    // SseSubscriber 이 아닌 각각 나누면...?
    private final SubscriberRepository<Long, SseSubscriber> roomListRepository;
    private final SubscriberRepository<String, SseSubscriber> roomInfoRepository;

    @Override
    public void addRoomListSubscriber(Long userIdx, SseSubscriber subscriber) {
        roomListRepository.add(userIdx, subscriber);
    }

    @Override
    public void addRoomInfoSubscriber(String roomId, SseSubscriber subscriber) {
        roomInfoRepository.add(roomId, subscriber);
    }

    @Override
    public Map<Long, SseSubscriber> getAllRoomListSubscribers() {
        return roomListRepository.getAll();
    }

    @Override
    public Map<String, SseSubscriber> getAllRoomInfoSubscribers() {
        return roomInfoRepository.getAll();
    }
}
