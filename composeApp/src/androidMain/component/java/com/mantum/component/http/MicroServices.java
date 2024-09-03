package com.mantum.component.http;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;

public abstract class MicroServices {

    private static final int CONNECT = 60 * 5;
    private static final int WRITE = 60 * 5;
    private static final int READ = 60 * 5;

    protected final Gson gson = new GsonBuilder().create();

    protected final Context context;
    protected final String url;
    protected final String token;
    protected final OkHttpClient client;

    public MicroServices(@NonNull Context context, String url, String token) {
        this.context = context;
        this.url = url;
        this.token = token;
        this.client = new OkHttpClient.Builder().connectTimeout(CONNECT, TimeUnit.SECONDS)
                .writeTimeout(WRITE, TimeUnit.SECONDS)
                .readTimeout(READ, TimeUnit.SECONDS)
                .build();
    }

    public MicroServices(
            @NonNull Context context, String url, String token, @NonNull OkHttpClient.Builder client) {
        this.context = context;
        this.url = url;
        this.token = token;
        this.client = client.connectTimeout(CONNECT, TimeUnit.SECONDS)
                .writeTimeout(WRITE, TimeUnit.SECONDS)
                .readTimeout(READ, TimeUnit.SECONDS)
                .build();
    }

    public void cancel() {
        for (Call call : client.dispatcher().runningCalls()) {
            call.cancel();
        }

        for (Call call : client.dispatcher().queuedCalls()) {
            call.cancel();
        }
    }
}