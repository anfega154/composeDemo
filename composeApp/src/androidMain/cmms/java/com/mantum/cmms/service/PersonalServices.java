package com.mantum.cmms.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mantum.R;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.util.Preferences;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;

import java.io.IOException;
import java.util.ArrayList;

import io.reactivex.Observable;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class PersonalServices extends Service {

    private final String version;

    public PersonalServices(@NonNull Context context, @NonNull Cuenta cuenta) {
        super(context);
        version = cuenta.getServidor().getVersion();
    }

    public Observable<Response> get(String query) {
        return Observable.create(subscriber -> {
            if (!Mantum.isConnectedOrConnecting(context)) {
                subscriber.onNext(new Response(context.getString(R.string.offline), null, new ArrayList<>()));
                subscriber.onComplete();
                return;
            }

            String url = Preferences.url(context, "restapp/app/filterpersonal?filter=" + query);
            Request request = new Request.Builder().get().url(url)
                    .addHeader("token", Preferences.token(context))
                    .addHeader("cache-control", "no-cache")
                    .addHeader("accept-language", "application/json")
                    .addHeader("accept", Version.build(version))
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {

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
                            Response content = gson.fromJson(json, Response.class);
                            content.setVersion(response.header("Max-Version"));

                            subscriber.onNext(content);
                            subscriber.onComplete();
                        } else {
                            subscriber.onError(new Exception(context.getString(R.string.error_request_autocomplete_personal)));
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