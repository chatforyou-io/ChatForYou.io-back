package com.chatforyou.io.repository.impl;

import com.chatforyou.io.models.sse.SseSubscriber;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

@Component
public class RoomListSubscriberRepository extends InMemorySubscriberRepository<Long, Sinks.Many<ServerSentEvent<?>>>{
}
