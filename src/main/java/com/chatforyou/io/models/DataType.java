package com.chatforyou.io.models;

import lombok.Getter;

@Getter
public enum DataType {
    CHATROOM("chatroom", 1),
    OPENVIDU("openvidu", 2),
//    CONNECTION_TOKEN(03),
    CONNECTION_CAMERA("camera", 3),
    CONNECTION_SCREEN("screen", 4),
    CONNECTION_TOKENS("tokens", 5),
    USER_COUNT("user_count", 6),
    USER_LIST("user_list", 7),
    FAVORITES("favorites", 8),
    LOGIN_USER("login_user", 9),
    USER_REFRESH_TOKEN("user_refresh_token", 10),
    USER_LAST_LOGIN_DATE("user_last_login_date", 11)
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
