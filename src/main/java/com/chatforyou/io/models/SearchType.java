package com.chatforyou.io.models;

import lombok.Getter;

@Getter
public enum SearchType {
    CHATROOM(1, "chatRoomIndex"),
    LOGIN_USER(2, "userIndex")
    ;

    SearchType(int code, String indexName){
        this.code = code;
        this.indexName = indexName;
    }
    private final int code;
    private final String indexName;
}
