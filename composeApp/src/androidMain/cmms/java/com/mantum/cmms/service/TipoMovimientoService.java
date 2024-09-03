package com.mantum.cmms.service;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.mantum.demo.R;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.net.ClientManager;
import com.mantum.cmms.util.Preferences;
import com.mantum.cmms.util.Version;
import com.mantum.component.http.MicroServices;

import java.io.IOException;

import io.reactivex.Observable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

import static com.mantum.component.Mantum.isConnectedOrConnecting;

public class TipoMovimientoService extends MicroServices {

    private final String version;

    private final static String TAG = AlmacenService.class.getSimpleName();

    public TipoMovimientoService(@NonNull Context context, @NonNull Cuenta cuenta) {
        super(context, cuenta.getServidor().getUrl(), cuenta.getToken(context), ClientManager.prepare(
                new OkHttpClient.Builder(), context
        ));
        version = cuenta.getServidor().getVersion();
    }

    public Observable<Response> get() {
        return Observable.create(subscriber -> {
            if (!isConnectedOrConnecting(context)) {
                subscriber.onError(new Exception(context.getString(R.string.offline)));
                return;
            }

            String url = Preferences.url(context, "/restapp/app/buscartipomovimientos");
            Request request = new Request.Builder().get().url(url)
                    .addHeader("token", Preferences.token(context))
                    .addHeader("cache-control", "no-cache")
                    .addHeader("accept-language", "application/json")
                    .addHeader("accept", Version.build(version))
                    .build();

            Log.e(TAG, "build: " + request);
            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!call.isCanceled()) {
                        subscriber.onError(e);
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
                        subscriber.onError(new Exception(context.getString(R.string.request_error_search)));
                        return;
                    }

                    String json = body.string();
                    try {
                        if (response.isSuccessful()) {
                            Response content = new Gson().fromJson(json, Response.class);
                            content.setVersion(response.header("Max-Version"));

                            subscriber.onNext(content);
                            subscriber.onComplete();
                        } else {
                            subscriber.onError(new Exception(context.getString(R.string.error_request_almacen)));
                        }
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                    response.close();
                }
            });
        });
    }
}
