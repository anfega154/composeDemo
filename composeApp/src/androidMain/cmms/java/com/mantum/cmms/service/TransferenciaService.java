package com.mantum.cmms.service;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;

import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Transferencia;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;

import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import io.realm.Realm;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class TransferenciaService extends Service {

    private static final String TAG = TransferenciaService.class.getSimpleName();

    private final Realm realm;

    public TransferenciaService(@NonNull Context context) {
        super(context);
        this.realm = new Database(context).instance();
    }

    public Observable<List<Transferencia>> get() {
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
            String url = cuenta.getServidor().getUrl() + "/restapp/app/listardocumentostransferencia";
            Request request = new Request.Builder().get().url(url)
                    .addHeader("token", cuenta.getToken(seed))
                    .addHeader("accept", Version.build(cuenta.getServidor().getVersion()))
                    .addHeader("cache-control", "no-cache")
                    .addHeader("accept-language", "application/json")
                    .build();

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
                        subscriber.onError(new Exception(context.getString(R.string.error_request_detail_ot)));
                        return;
                    }

                    String json = body.string();
                    try {
                        if (response.isSuccessful()) {
                            Response content = gson.fromJson(json, Response.class);
                            content.setVersion(response.header("Max-Version"));

                            Transferencia.Request request = content.getBody(Transferencia.Request.class);
                            subscriber.onNext(request.getTransferencias());
                            subscriber.onComplete();
                        } else {
                            subscriber.onError(new Exception(context.getString(R.string.error_request_mochila)));
                        }
                    } catch (Exception e) {
                        subscriber.onError(new Exception(context.getString(R.string.error_pendientes)));
                    }

                    response.close();
                }
            });
        });
    }
}