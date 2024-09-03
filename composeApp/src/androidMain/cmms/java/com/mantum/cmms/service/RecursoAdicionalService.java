package com.mantum.cmms.service;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;

import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;

import java.io.IOException;

import io.reactivex.Observable;
import io.realm.Realm;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class RecursoAdicionalService extends Service {

    private static final String TAG = RecursoAdicionalService.class.getSimpleName();

    private final Realm realm;

    public RecursoAdicionalService(@NonNull Context context) {
        super(context);
        this.realm = new Database(context).instance();
    }

    @Override
    public void cancel() {
        if (realm != null) {
            realm.close();
        }

        super.cancel();
    }

    public Observable<Response> search(String value) {
        return Observable.create(subscriber -> {
            if (!Mantum.isConnectedOrConnecting(context)) {
                subscriber.onError(new Exception(context.getString(R.string.offline)));
                return;
            }

            Cuenta cuenta = realm.where(Cuenta.class)
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
            String url = cuenta.getServidor().getUrl() + "/restapp/app/searchrecadicional?nombre=" + value;
            Request request = new Request.Builder().get().url(url)
                    .addHeader("token", cuenta.getToken(seed))
                    .addHeader("accept", Version.build(cuenta.getServidor().getVersion()))
                    .addHeader("cache-control", "no-cache")
                    .addHeader("accept-language", "application/json")
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