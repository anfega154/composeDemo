package com.mantum.cmms.service;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import com.mantum.demo.R;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.util.Preferences;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;

import java.io.IOException;

import io.reactivex.Observable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class RecursoService extends Service {

    private static final String TAG = RecursoService.class.getSimpleName();

    private final String version;

    public RecursoService(@NonNull Context context, Cuenta cuenta) {
        super(context);
        version = cuenta.getServidor().getVersion();
    }

    public Observable<Response> search(String value) {
        return Observable.create(subscriber -> {
            if (!Mantum.isConnectedOrConnecting(context)) {
                subscriber.onComplete();
                return;
            }

            String url = Preferences.url(context, "/restapp/app/searchrec/") + value;
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
                    subscriber.onError(e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response)
                        throws IOException {
                    ResponseBody body = response.body();
                    if (body == null) {
                        subscriber.onError(new Exception(context.getString(R.string.request_error_search)));
                        return;
                    }

                    String json = body.string();
                    try {
                        if (response.isSuccessful()) {
                            Response content = gson.fromJson(json, Response.class);
                            content.setVersion(response.header("Max-Version"));

                            subscriber.onNext(content);
                            subscriber.onComplete();
                        } else {
                            subscriber.onError(new Exception(context.getString(R.string.error_request_recursos)));
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