package com.chatforyou.io.controller;

import com.chatforyou.io.services.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/sse")
@Slf4j
@RequiredArgsConstructor
public class SSEController {

    private final SseService sseService;

    @GetMapping(path = "/chatroom/list/{userIdx}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter notifyChatRoomList(@PathVariable Long userIdx) throws BadRequestException {

        return sseService.subscribeRoomList(userIdx);

    }

    @GetMapping(path = "/user/list/{userIdx}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter notifyUserList(@PathVariable Long userIdx) throws BadRequestException {
        return sseService.subscribeUserList(userIdx);
    }

    @GetMapping(path = "/chatroom/{sessionId}/user/{userIdx}")
    public SseEmitter notifyChatRoomInfo(@PathVariable String sessionId,
                                         @PathVariable Long userIdx
                                         ) throws BadRequestException {

        return sseService.subscribeRoomInfo(userIdx, sessionId);

    }
}
