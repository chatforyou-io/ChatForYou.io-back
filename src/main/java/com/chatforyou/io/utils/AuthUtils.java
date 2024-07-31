package com.chatforyou.io.utils;

import java.util.Base64;

public class AuthUtils {

    public static String getEncodeStr(String str){
        return new String(Base64.getEncoder().encode(str.getBytes()));
    }

    public static String getDecodeStr(byte[] str){
        return new String(Base64.getDecoder().decode(str));
    }
}
