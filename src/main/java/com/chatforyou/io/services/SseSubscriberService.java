package com.chatforyou.io.services;

import com.chatforyou.io.models.sse.SseSubscriber;
import java.util.Collection;
import java.util.Map;

public interface SseSubscriberService {
    void addDashboardSubscriber(Long userIdx, SseSubscriber subscriber);
    void addRoomInfoSubscriber(String roomId, SseSubscriber subscriber);
    Map<Long, SseSubscriber> getDashboardSubscribers();
    Collection<SseSubscriber> getRoomInfoSubscribersByRoomId(String sessionId);
    void removeDashboardSubscriber(Long userIdx);
    void removeRoomInfoSubscriber(String roomId, SseSubscriber subscriber);
}
