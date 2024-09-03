package com.mantum.cmms.service;

import static com.mantum.component.Mantum.isConnectedOrConnecting;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Paro;
import com.mantum.cmms.net.ClientManager;
import com.mantum.cmms.util.Preferences;
import com.mantum.cmms.util.Version;
import com.mantum.component.http.MicroServices;

import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.RealmList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class ParoService extends MicroServices {

    private final static String TAG = ParoService.class.getSimpleName();

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final Database database;

    private final Cuenta cuenta;

    public ParoService(@NonNull Context context, Cuenta cuenta) {
        super(context, cuenta.getServidor().getUrl(), cuenta.getToken(context), ClientManager.prepare(
                new OkHttpClient.Builder(), context
        ));
        this.database = new Database(context);
        this.cuenta = cuenta;
    }

    public Observable<ResponseBodyGet> getHistoricoParos(Long idEquipo) {
        return Observable.create(subscriber -> {
            if (!isConnectedOrConnecting(context)) {
                subscriber.onError(new Exception(context.getString(R.string.offline)));
                return;
            }

            String url = Preferences.url(context, "restapp/app/obtenerhistorialparos?id=" + idEquipo);
            Request request = new Request.Builder().get().url(url)
                    .addHeader("token", Preferences.token(context))
                    .addHeader("cache-control", "no-cache")
                    .addHeader("accept-language", "application/json")
                    .addHeader("accept", Version.build(cuenta.getServidor().getVersion()))
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
                            ResponseBodyGet content = new Gson().fromJson(json, ResponseBodyGet.class);

                            subscriber.onNext(content);
                            subscriber.onComplete();
                        } else {
                            subscriber.onError(new Exception(context.getString(R.string.error_app)));
                        }
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                    response.close();
                }
            });
        });
    }

    public void saveHistorico(ResponseBodyGet responseBodyGet, Long idEquipo) {
        BodyGetHistoricoParos body = responseBodyGet.getBody();
        database.executeTransaction(realm -> {
            realm.where(Paro.class)
                    .equalTo("idequipo", idEquipo)
                    .findAll()
                    .deleteAllFromRealm();

            RealmList<Paro> paros = new RealmList<>();
            for (Paro paro : body.getParos()) {
                paro.setCuenta(cuenta);
                paro.setIdequipo(idEquipo);
                paros.add(realm.copyToRealm(paro));
            }
            realm.insertOrUpdate(paros);
        });
    }

    public static class ResponseBodyGet {
        BodyGetHistoricoParos body;

        public BodyGetHistoricoParos getBody() {
            return body;
        }
    }

    public static class BodyGetHistoricoParos {
        List<Paro> paros;

        public List<Paro> getParos() {
            return paros;
        }
    }

    public void onDestroy() {
        compositeDisposable.clear();
        if (database != null) {
            database.close();
        }
    }
}
