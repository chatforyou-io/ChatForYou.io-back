package com.chatforyou.io.repository.impl;

import com.chatforyou.io.models.sse.SseSubscriber;
import org.springframework.stereotype.Component;

@Component
public class RoomInfoSubscriberRepository extends InMemorySubscriberRepository<String, SseSubscriber> {
}
