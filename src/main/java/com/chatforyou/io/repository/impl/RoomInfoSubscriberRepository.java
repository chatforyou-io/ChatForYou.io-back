package com.chatforyou.io.repository.impl;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.util.Map;

@Component
public class RoomInfoSubscriberRepository extends InMemorySubscriberRepository<String, Map<Long, Sinks.Many<ServerSentEvent<?>>>> {
}
