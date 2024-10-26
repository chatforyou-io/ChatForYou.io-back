package com.chatforyou.io.models;

public enum ValidateType {

    ID(1),
    NICKNAME(2),
    PASSWORD(3),
    CHATROOM_NAME(4)
    ;

    private final int code;

    ValidateType(int code){
        this.code = code;
    }

    ValidateType fromCode(int code){
        for(ValidateType type : ValidateType.values()){
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }
}
