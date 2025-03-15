package com.chatforyou.io.repository.impl;

import com.chatforyou.io.models.sse.SseSubscriber;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RoomInfoSubscriberRepository extends InMemorySubscriberRepository<String, Map<Long, SseSubscriber>> {
}
