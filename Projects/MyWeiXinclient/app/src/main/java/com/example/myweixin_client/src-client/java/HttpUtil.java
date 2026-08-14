package com.example.myweixin_client;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpUtil {
    private static final String BASE_URL = "http://YOUR_SERVER_IP:8082";
    private static final OkHttpClient CLIENT = new OkHttpClient();

    public static void postJson(String path, String jsonBody, Callback callback){
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody, mediaType);
        Request request = new Request.Builder()
                .url(BASE_URL + path)
                .post(body)
                .build();
        CLIENT.newCall(request).enqueue(callback);
    }
}
