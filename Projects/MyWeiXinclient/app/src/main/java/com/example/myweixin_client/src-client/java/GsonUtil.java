package com.example.myweixin_client;

import com.google.gson.Gson;
public class GsonUtil{
    public static final Gson GSON = new Gson();
    //对象转成json
    public static String toJson(Object o){
        return GSON.toJson(o);
    }

    //json转成对象
    public static <T> T fromJson(String json, Class<T> clazz){
        return GSON.fromJson(json, clazz);
    }
}
