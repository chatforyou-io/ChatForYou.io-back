package com.chatforyou.io.models;

public enum DataType {
    CHATROOM("chatroom", 01),
    OPENVIDU("openvidu", 02),
//    CONNECTION_TOKEN(03),
    CONNECTION_CAMERA("camera", 03),
    CONNECTION_SCREEN("screen", 04),
    USER_COUNT("user_count", 05),
    USER_LIST("user_list", 06)
    ;

    private final int code;
    private final String type;

    DataType(String type, int code){
        this.code = code;
        this.type = type;
    }

    public static String redisDataType(String key, DataType dataType){
        return key + "_" + dataType.toString();
    }

    public static String redisDataTypeConnection(String sessionId, String key, DataType dataType){
        return sessionId + "_"+ dataType.type + "_" + key;
    }
}
