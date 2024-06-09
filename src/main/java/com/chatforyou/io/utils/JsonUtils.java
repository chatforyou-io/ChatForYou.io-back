package com.chatforyou.io.utils;


import com.google.gson.JsonObject;

public class JsonUtils {
    public static String getStrOrNull(JsonObject obj, String key){
        if (!obj.isJsonNull() && obj.has(key)) {
            return obj.get(key).getAsString();
        }
        return null;
    }
}
