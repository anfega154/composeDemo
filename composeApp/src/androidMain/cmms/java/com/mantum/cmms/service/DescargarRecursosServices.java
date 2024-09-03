package com.mantum.cmms.service;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.net.ClientManager;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.http.MicroServices;

import java.io.IOException;

import io.reactivex.Observable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class DescargarRecursosServices extends MicroServices {

    public final String version;

    private final Database database;

    private final Gson gson = new Gson();

    public DescargarRecursosServices(@NonNull Context context, @NonNull Cuenta cuenta) {
        super(context, cuenta.getServidor().getUrl(), cuenta.getToken(context), ClientManager.prepare(
                new OkHttpClient.Builder(), context
        ));
        this.database = new Database(context);
        version = cuenta.getServidor().getVersion();
    }

    public Observable<Response> get() {
        return Observable.create(subscriber -> {
            if (!Mantum.isConnectedOrConnecting(context)) {
                subscriber.onError(new Exception(context.getString(R.string.offline)));
                return;
            }

            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            Bundle bundle = Mantum.bundle(context);
            if (bundle == null) {
                subscriber.onError(new Exception(context.getString(R.string.token_error)));
                return;
            }

            String seed = bundle.getString("Mantum.Authentication.Token");
            String url = cuenta.getServidor().getUrl() + "/restapp/app/obtenerrecursos";

            Request request = new Request.Builder().get().url(url)
                    .addHeader("token", cuenta.getToken(seed))
                    .addHeader("cache-control", "no-cache")
                    .addHeader("accept-language", "application/json")
                    .addHeader("accept", Version.build(version))
                    .build();

            Log.i("Http", String.format("GET  -> %s", request.url().url()));
            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!call.isCanceled()) {
                        subscriber.onError(new Exception(context.getString(R.string.request_error_search)));
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                    if (call.isCanceled()) {
                        return;
                    }

                    ResponseBody body = response.body();
                    if (body == null) {
                        subscriber.onError(new Exception(context.getString(R.string.request_error_search)));
                        return;
                    }

                    String json = body.string();
                    Log.i("Http", String.format("GET  <- %s is %s", request.url().url(), response.isSuccessful() ? "Ok" : "Error"));
                    try {
                        if (response.isSuccessful()) {
                            Response content = gson.fromJson(json, Response.class);
                            content.setVersion(response.header("Max-Version"));

                            subscriber.onNext(content);
                            subscriber.onComplete();
                        } else {
                            Response content = gson.fromJson(json, Response.class);
                            subscriber.onError(new Exception(content.getMessage()));
                        }
                    } catch (Exception e) {
                        subscriber.onError(new Exception(context.getString(R.string.request_reading_error)));
                    }
                    response.close();
                }
            });
        });
    }
}
