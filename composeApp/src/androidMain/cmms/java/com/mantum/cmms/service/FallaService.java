package com.mantum.cmms.service;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.GamaMantenimiento;
import com.mantum.cmms.entity.TipoFalla;
import com.mantum.cmms.net.ClientManager;
import com.mantum.cmms.util.Preferences;
import com.mantum.cmms.util.Version;
import com.mantum.component.http.MicroServices;

import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import io.realm.RealmList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

import static com.mantum.component.Mantum.isConnectedOrConnecting;

public class FallaService extends MicroServices {

    private final Database database;

    private final Cuenta cuenta;

    public static class Builder extends Service {
        public Builder(@NonNull Context context) {
            super(context);
        }
    }

    public FallaService(@NonNull Context context, Cuenta cuenta) {
        super(context, cuenta.getServidor().getUrl(), cuenta.getToken(context), ClientManager.prepare(
                new OkHttpClient.Builder(), context
        ));
        this.database = new Database(context);
        this.cuenta = cuenta;
    }

    public Observable<ResponseBodyGet> getAllTiposFalla() {
        return Observable.create(subscriber -> {
            if (!isConnectedOrConnecting(context)) {
                subscriber.onError(new Exception(context.getString(R.string.offline)));
                return;
            }

            String url = Preferences.url(context, "restapp/app/searchtypefailure");
            Request request = new Request.Builder().get().url(url)
                    .addHeader("token", Preferences.token(context))
                    .addHeader("cache-control", "no-cache")
                    .addHeader("accept-language", "application/json")
                    .addHeader("accept", Version.build(cuenta.getServidor().getVersion()))
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
                        if (!subscriber.isDisposed()) {
                            subscriber.onError(new Exception(context.getString(R.string.request_error_search)));
                        }
                        return;
                    }

                    String json = body.string();
                    try {
                        if (response.isSuccessful()) {
                            ResponseBodyGet content = new Gson().fromJson(json, ResponseBodyGet.class);

                            subscriber.onNext(content);
                            subscriber.onComplete();
                        } else {
                            if (!subscriber.isDisposed()) {
                                subscriber.onError(new Exception(context.getString(R.string.error_app)));
                            }
                        }
                    } catch (Exception e) {
                        if (!subscriber.isDisposed()) {
                            subscriber.onError(e);
                        }
                    }
                    response.close();
                }
            });
        });
    }

    public void saveAllTiposFalla(ResponseBodyGet responseBodyGet) {
        BodyGetAllVariables variables = responseBodyGet.getBody();
        database.executeTransaction(realm -> {
            realm.where(TipoFalla.class)
                    .findAll()
                    .deleteAllFromRealm();

            RealmList<TipoFalla> tiposFalla = new RealmList<>();
            for (TipoFalla tipoFalla : variables.getTipofallas()) {
                tipoFalla.setCuenta(cuenta);
                tiposFalla.add(realm.copyToRealm(tipoFalla));
            }
            realm.insertOrUpdate(tiposFalla);
        });
    }

    public static class ResponseBodyGet {
        BodyGetAllVariables body;

        public BodyGetAllVariables getBody() {
            return body;
        }
    }

    public static class BodyGetAllVariables {
        List<TipoFalla> tipofallas;
        List<GamaMantenimiento> gamasMantenimiento;

        public List<TipoFalla> getTipofallas() {
            return tipofallas;
        }

        public List<GamaMantenimiento> getGamasMantenimiento() {
            return gamasMantenimiento;
        }
    }

    public void onDestroy() {
        if (database != null) {
            database.close();
        }
    }
}