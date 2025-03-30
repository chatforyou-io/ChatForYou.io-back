package com.chatforyou.io.repository.impl;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

@Component
public class UserListSubscriberRepository extends InMemorySubscriberRepository<Long, Sinks.Many<ServerSentEvent<?>>> {
}
