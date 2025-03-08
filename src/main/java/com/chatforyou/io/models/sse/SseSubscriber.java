package com.chatforyou.io.models.sse;

import lombok.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class SseSubscriber {
    private final Long userIdx;
    private final SseEmitter sseEmitter;

    public static SseSubscriber of(long userIdx, SseEmitter sseEmitter) {
        return builder()
                .userIdx(userIdx)
                .sseEmitter(sseEmitter)
                .build();
    }
}
