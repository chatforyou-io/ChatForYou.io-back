package com.chatforyou.io.services;

import com.chatforyou.io.models.sse.SseSubscriber;
import java.util.Collection;
import java.util.Map;

public interface SseSubscriberService {
    void addRoomListSubscriber(Long userIdx, SseSubscriber subscriber);
    void addRoomInfoSubscriber(String roomId, SseSubscriber subscriber);
    void addUserListSubscriber(Long userIdx, SseSubscriber subscriber);
    Map<Long, SseSubscriber> getRoomListSubscribers();
    Collection<SseSubscriber> getRoomInfoSubscribersByRoomId(String sessionId);
    Map<Long, SseSubscriber> getUserListSubscribers();
    void removeRoomListSubscriber(Long userIdx);
    void removeRoomInfoSubscriber(String roomId, SseSubscriber subscriber);
    void removeUserListSubscriber(Long userIdx);
}
