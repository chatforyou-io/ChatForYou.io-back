package com.chatforyou.io.models;

public enum ValidateType {

    ID(01),
    NICKNAME(02),
    PASSWORD(03),
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
