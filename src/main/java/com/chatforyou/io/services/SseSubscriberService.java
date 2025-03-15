package com.chatforyou.io.services;

import com.chatforyou.io.models.sse.SseSubscriber;
import java.util.Collection;
import java.util.Map;

public interface SseSubscriberService {
    void addRoomListSubscriber(Long userIdx, SseSubscriber subscriber);
    void addRoomInfoSubscriber(String roomId, SseSubscriber subscriber);
    Map<Long, SseSubscriber> getAllRoomListSubscribers();
    Collection<SseSubscriber> getRoomInfoSubscribersByRoomId(String sessionId);
    void removeRoomListSubscriber(Long userIdx);
    void removeRoomInfoSubscriber(String roomId, SseSubscriber subscriber);
}
