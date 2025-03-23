package com.chatforyou.io.repository.impl;

import com.chatforyou.io.models.sse.SseSubscriber;
import org.springframework.stereotype.Component;

@Component
public class RoomListSubscriberRepository extends InMemorySubscriberRepository<Long, SseSubscriber>{
}
