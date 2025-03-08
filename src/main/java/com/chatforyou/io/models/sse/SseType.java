package com.chatforyou.io.models.sse;

import lombok.Getter;

@Getter
public enum SseType {
    ROOM_LIST(180000L),
    ROOM_INFO(3600000L),
    ;

    private final long timeOut;

    SseType(long timeOut){
        this.timeOut = timeOut;
    }
}
