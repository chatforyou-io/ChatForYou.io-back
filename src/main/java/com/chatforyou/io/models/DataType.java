package com.chatforyou.io.models;

public enum DataType {
    CHATROOM(01),
    OPENVIDU(02),
//    CONNECTION_TOKEN(03),
    CONNECTION_CAMERA(03),
    CONNECTION_SCREEN(04),
    USER_COUNT(05),
    USER_LIST(06)
    ;

    private final int code;

    DataType(int code){
        this.code = code;
    }

    public static String redisDataKey(String key, DataType dataType){
        return key + "_" + dataType.toString();
    }
}
