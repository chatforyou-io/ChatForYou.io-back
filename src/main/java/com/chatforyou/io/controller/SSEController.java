package com.chatforyou.io.controller;

import com.chatforyou.io.services.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/sse")
@Slf4j
@RequiredArgsConstructor
public class SSEController {

    private final SseService sseService;

    @GetMapping(path = "/chatroom/list/{userIdx}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<?>> notifyChatRoomList(@PathVariable Long userIdx) throws BadRequestException {
        return sseService.subscribeRoomList(userIdx);

    }

    @GetMapping(path = "/user/list/{userIdx}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<?>> notifyUserList(@PathVariable Long userIdx) throws BadRequestException {
        return sseService.subscribeUserList(userIdx);
    }

    @GetMapping(path = "/chatroom/{sessionId}/user/{userIdx}")
    public Flux<ServerSentEvent<?>> notifyChatRoomInfo(@PathVariable String sessionId,
                                         @PathVariable Long userIdx
                                         ) throws BadRequestException {
        return sseService.subscribeRoomInfo(userIdx, sessionId);
    }
}
