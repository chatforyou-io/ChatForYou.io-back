package com.chatforyou.io.models;

import lombok.Getter;

@Getter
public enum RedisIndex {
    CHATROOM(1, "chatRoomIndex"),
    LOGIN_USER(2, "userIndex")
    ;

    RedisIndex(int code, String type){
        this.code = code;
        this.type = type;
    }
    private final int code;
    private final String type;
}
