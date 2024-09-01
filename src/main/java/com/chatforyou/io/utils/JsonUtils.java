package com.chatforyou.io.utils;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String getStrOrNull(JsonObject obj, String key){
        if (!obj.isJsonNull() && obj.has(key)) {
            return obj.get(key).getAsString();
        }
        return null;
    }


    public static <T> T jsonToObj(String jsonString, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON to Object", e);
        }
    }

}
