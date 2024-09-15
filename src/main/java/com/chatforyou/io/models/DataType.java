package com.chatforyou.io.models;

import lombok.Getter;

@Getter
public enum DataType {
    CHATROOM("chatroom", 01),
    OPENVIDU("openvidu", 02),
//    CONNECTION_TOKEN(03),
    CONNECTION_CAMERA("camera", 03),
    CONNECTION_SCREEN("screen", 04),
    CONNECTION_TOKENS("tokens", 05),
    USER_COUNT("user_count", 06),
    USER_LIST("user_list", 07)
    ;

    private final int code;
    private final String type;

    DataType(String type, int code){
        this.code = code;
        this.type = type;
    }

    public static String redisDataTypeConnection(String key, DataType dataType){
        return dataType.type + "_" + key;
    }
}
