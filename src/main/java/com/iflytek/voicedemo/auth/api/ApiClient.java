package com.iflytek.voicedemo.auth.api;

import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;

public class ApiClient {
    private static ApiClient instance;
    private final OkHttpClient httpClient;
    private final Gson gson;

    private ApiClient() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
        gson = new Gson();
    }

    public static synchronized ApiClient getInstance() {
        if (instance == null) instance = new ApiClient();
        return instance;
    }

    public OkHttpClient getHttpClient() { return httpClient; }

    public Gson getGson() { return gson; }

    public static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");
}
