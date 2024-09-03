package com.mantum.cmms.service;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mantum.R;
import com.mantum.cmms.Multipart;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.factory.SparseArrayTypeAdapterFactory;
import com.mantum.cmms.net.ClientManager;
import com.mantum.component.Mantum;
import com.mantum.component.util.Timeout;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public abstract class Service {

    protected final Context context;

    protected final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(SparseArrayTypeAdapterFactory.INSTANCE)
            .create();

    protected final OkHttpClient client;

    public Service(@NonNull Context context) {
        this.context = context;
        client = ClientManager.prepare(new OkHttpClient.Builder()
                .connectTimeout(Timeout.CONNECT, TimeUnit.SECONDS)
                .writeTimeout(Timeout.WRITE, TimeUnit.SECONDS)
                .readTimeout(Timeout.READ, TimeUnit.SECONDS), context
        ).build();
    }

    public Service(@NonNull Context context, @NonNull OkHttpClient.Builder client) {
        this.context = context;
        this.client = client.build();
    }

    public void cancel() {
        for (Call call : client.dispatcher().runningCalls()) {
            call.cancel();
        }

        for (Call call : client.dispatcher().queuedCalls()) {
            call.cancel();
        }
    }

    private String creationDate(@NonNull Date date) {
        try {
            SimpleDateFormat formatter
                    = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return formatter.format(date);
        } catch (Exception e) {
            return "";
        }
    }

    public <T extends Multipart> Observable<Response> pushToMultipart(@NonNull Transaccion transaccion, Class<T> clazz) {
        return Observable.create(subscriber -> {
            if (!Mantum.isConnectedOrConnecting(context)) {
                subscriber.onError(new Exception(context.getString(R.string.offline)));
                return;
            }

            Bundle bundle = Mantum.bundle(context);
            if (bundle == null) {
                subscriber.onError(new Exception(context.getString(R.string.token_error)));
                return;
            }

            try {
                T value = gson.fromJson(transaccion.getValue(), clazz);
                MultipartBody.Builder multipart = value.builder();

                RequestBody body = multipart.build();
                String seed = bundle.getString("Mantum.Authentication.Token");
                Request.Builder builder = new Request.Builder().url(transaccion.getUrl())
                        .addHeader("token", transaccion.getCuenta().getToken(seed))
                        .addHeader("cache-control", "no-cache")
                        .addHeader("content-type", "multipart/form-data")
                        .addHeader("creation-date", creationDate(transaccion.getCreation()))
                        .post(body);

                if (transaccion.getVersion() != null && !transaccion.getVersion().isEmpty()) {
                    builder.addHeader("accept", transaccion.getVersion());
                }

                Request request = builder.build();
                client.newCall(request).enqueue(new Callback() {

                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        if (!call.isCanceled() && !subscriber.isDisposed()) {
                            subscriber.onError(new Mantum.HttpException(e.getMessage()));
                        }
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                        if (call.isCanceled()) {
                            return;
                        }

                        ResponseBody body = response.body();
                        if (body == null) {
                            subscriber.onError(new Exception(context.getString(R.string.error_request)));
                            return;
                        }

                        String json = body.string();
                        try {
                            Response content = gson.fromJson(json, Response.class);
                            if (response.isSuccessful()) {
                                content.setVersion(response.header("Max-Version"));

                                subscriber.onNext(content);
                                subscriber.onComplete();
                            } else {
                                subscriber.onError(new Mantum.ResponseException(
                                        Response.buildMessage(content), json));
                            }
                        } catch (Exception e) {
                            subscriber.onError(new Mantum.ResponseException(
                                    context.getString(R.string.error_response_request), json));
                        }

                        response.close();
                    }
                });
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }

    public Observable<Response> pushToJson(@NonNull Transaccion transaccion) {
        return Observable.create(subscriber -> {
            if (!Mantum.isConnectedOrConnecting(context)) {
                subscriber.onError(new Exception(context.getString(R.string.offline)));
                return;
            }

            Bundle bundle = Mantum.bundle(context);
            if (bundle == null) {
                subscriber.onError(new Exception(context.getString(R.string.token_error)));
                return;
            }

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"), transaccion.getValue());

            String seed = bundle.getString("Mantum.Authentication.Token");
            Request request = new Request.Builder().url(transaccion.getUrl())
                    .addHeader("token", transaccion.getCuenta().getToken(seed))
                    .addHeader("accept", transaccion.getVersion())
                    .addHeader("cache-control", "no-cache")
                    .addHeader("accept-language", "application/json")
                    .addHeader("creation-date", creationDate(transaccion.getCreation()))
                    .post(body).build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!call.isCanceled()) {
                        subscriber.onError(new Mantum.HttpException(e.getMessage()));
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response)
                        throws IOException {
                    if (call.isCanceled()) {
                        return;
                    }

                    ResponseBody body = response.body();
                    if (body == null) {
                        subscriber.onError(new Exception(context.getString(R.string.error_request)));
                        return;
                    }

                    String json = body.string();
                    try {
                        Response content = gson.fromJson(json, Response.class);
                        if (response.isSuccessful()) {
                            content.setVersion(response.header("Max-Version"));

                            subscriber.onNext(content);
                            subscriber.onComplete();
                        } else {
                            subscriber.onError(new Mantum.ResponseException(
                                    Response.buildMessage(content), json));
                        }
                    } catch (Exception e) {
                        subscriber.onError(new Mantum.ResponseException(
                                context.getString(R.string.error_response_request), json));
                    }

                    response.close();
                }
            });
        });
    }
}