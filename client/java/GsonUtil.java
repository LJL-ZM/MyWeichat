package com.example.myweixin_client;

import com.google.gson.Gson;
public class GsonUtil{
    public static final Gson GSON = new Gson();
    public static String toJson(Object o){
        return GSON.toJson(o);
    }

    public static <T> T fromJson(String json, Class<T> clazz){
        return GSON.fromJson(json, clazz);
    }
}