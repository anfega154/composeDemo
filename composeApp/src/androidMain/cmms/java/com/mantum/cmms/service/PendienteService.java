package com.mantum.cmms.service;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Adjuntos;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Pendiente;
import com.mantum.cmms.net.ClientManager;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.http.MicroServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import io.reactivex.Observable;
import io.realm.RealmList;
import io.realm.RealmResults;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class PendienteService extends MicroServices {

    private static final String TAG = PendienteService.class.getSimpleName();

    private final Database database;

    private final String version;

    public PendienteService(@NonNull Context context, @NonNull Cuenta cuenta) {
        super(context, cuenta.getServidor().getUrl(), cuenta.getToken(context), ClientManager.prepare(
                new OkHttpClient.Builder(), context
        ));
        this.database = new Database(context);
        version = cuenta.getServidor().getVersion();
    }

    public Observable<Pendiente.Request> fetch(final Integer pages) {
        return Observable.create(subscriber -> {
            if (!Mantum.isConnectedOrConnecting(context)) {
                subscriber.onNext(new Pendiente.Request());
                subscriber.onComplete();
                return;
            }

            Request request = new Request.Builder().get()
                    .url(url + "/restapp/app/getmytodo?typetodo=PEN&page=" + pages)
                    .addHeader("token", token)
                    .addHeader("accept", Version.build(version))
                    .addHeader("cache-control", "no-cache")
                    .addHeader("accept-language", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                    if (call.isCanceled()) {
                        return;
                    }

                    ResponseBody body = response.body();
                    if (body == null) {
                        subscriber.onError(new Exception("Ocurrio un error a la hora de realizar la petición"));
                        return;
                    }

                    String json = body.string();
                    try {
                        if (response.isSuccessful()) {
                            Response content = new Gson().fromJson(json, Response.class);
                            Pendiente.Request request = content.getBody(Pendiente.Request.class);
                            Version.save(context, request.getVersion());

                            subscriber.onNext(request);
                            subscriber.onComplete();
                        } else {
                            if (!subscriber.isDisposed()) {
                                subscriber.onError(new Exception(context.getString(R.string.error_get_pendiente)));
                            }
                        }
                    } catch (Exception e) {
                        if (!subscriber.isDisposed()) {
                            subscriber.onError(new Exception(context.getString(R.string.error_pendientes)));
                        }
                    }
                    response.close();
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!call.isCanceled() && !subscriber.isDisposed()) {
                        subscriber.onError(new Exception(context.getString(R.string.error_pendientes)));
                    }
                }
            });
        });
    }

    public Observable<List<Pendiente>> fetchById(long id) {
        return Observable.create(subscriber -> {
            if (!Mantum.isConnectedOrConnecting(context)) {
                subscriber.onError(new Exception(context.getString(R.string.offline)));
                return;
            }

            String endpoint = url + "/restapp/app/refreshtodo?typetodo=PEN&idtodo=" + id;
            if (Integer.parseInt(version) >= 10) {
                endpoint = url + "/restapp/app/refreshtodo?typetodo=PEN&id=" + id;
            }

            Request request = new Request.Builder().get()
                    .addHeader("token", token)
                    .addHeader("accept", Version.build(version))
                    .addHeader("cache-control", "no-cache")
                    .addHeader("accept-language", "application/json")
                    .url(endpoint)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                    if (!call.isCanceled()) {
                        subscriber.onError(e);
                    }
                }

                @Override
                public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                    if (call.isCanceled()) {
                        return;
                    }

                    ResponseBody body = response.body();
                    if (body == null) {
                        subscriber.onError(new Exception(context.getString(R.string.error_request_detail_pendiente)));
                        return;
                    }

                    String json = body.string();
                    try {
                        if (response.isSuccessful()) {
                            Response content = new Gson().fromJson(json, Response.class);
                            content.setVersion(response.header("Max-Version"));

                            Pendiente.Request data = content.getBody(Pendiente.Request.class);
                            subscriber.onNext(!data.getPendientes().isEmpty() ? data.getPendientes() : Collections.emptyList());
                            subscriber.onComplete();
                        } else {
                            subscriber.onError(new Exception(context.getString(R.string.error_get_pendiente)));
                        }
                    } catch (Exception e) {
                        if (!call.isCanceled() && !subscriber.isDisposed()) {
                            subscriber.onError(new Exception(context.getString(R.string.error_get_response_pendiente)));
                        }
                    }

                    response.close();
                }
            });
        });
    }

    public Observable<List<Pendiente>> save(@NonNull Pendiente value) {
        return save(Collections.singletonList(value));
    }

    public Observable<List<Pendiente>> save(@NonNull List<Pendiente> values) {
        return Observable.create(subscriber -> database.executeTransactionAsync(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            for (Pendiente value : values) {
                Pendiente temporal = self.where(Pendiente.class)
                        .equalTo("id", value.getId())
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();

                if (temporal == null) {
                    value.setUUID(UUID.randomUUID().toString());
                    value.setCuenta(cuenta);
                    self.insert(value);
                } else {
                    value.setUUID(temporal.getUUID());
                    temporal.setCodigo(value.getCodigo());
                    temporal.setCriticidad(value.getCriticidad());
                    temporal.setEstado(value.getEstado());
                    temporal.setFecha(value.getFecha());
                    temporal.setPersonal(value.getPersonal());
                    temporal.setDescripcion(value.getDescripcion());
                    temporal.setColor(value.getColor());

                    // Guarda los adjuntos de la solicitud de servicio
                    if (temporal.getAdjuntos() != null && !temporal.getAdjuntos().isEmpty()) {
                        temporal.getAdjuntos().deleteAllFromRealm();
                    }

                    RealmList<Adjuntos> adjuntos = new RealmList<>();
                    for (Adjuntos adjunto : value.getAdjuntos()) {
                        adjuntos.add(self.copyToRealm(adjunto));
                    }
                    temporal.setAdjuntos(adjuntos);
                }
            }

            subscriber.onNext(values);

        }, subscriber::onComplete, subscriber::onError));
    }

    public Observable<List<Long>> remove(final Long[] id) {
        return Observable.create(subscriber -> {
            Database database = new Database(context);
            database.executeTransaction(realm -> {
                Cuenta cuenta = realm.where(Cuenta.class)
                        .equalTo("active", true)
                        .findFirst();

                if (cuenta == null) {
                    subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                    return;
                }

                try {
                    RealmResults<Pendiente> pendientes = realm.where(Pendiente.class)
                            .equalTo("cuenta.UUID", cuenta.getUUID())
                            .not().in("id", id)
                            .findAll();

                    List<Long> eliminados = new ArrayList<>();
                    for (Pendiente pendiente : pendientes) {
                        eliminados.add(pendiente.getId());
                    }
                    pendientes.deleteAllFromRealm();

                    subscriber.onNext(eliminados);
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            });
            database.close();
        });
    }

    public Observable<List<Pendiente>> remove(final Long id) {
        return Observable.create(subscriber -> {
            if (id == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_elminar_pendiente)));
                return;
            }

            database.executeTransactionAsync(self -> {
                Cuenta cuenta = self.where(Cuenta.class)
                        .equalTo("active", true)
                        .findFirst();

                if (cuenta == null) {
                    subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                    return;
                }

                self.where(Pendiente.class)
                        .equalTo("id", id)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findAll()
                        .deleteAllFromRealm();

                subscriber.onNext(Collections.emptyList());

            }, subscriber::onComplete, subscriber::onError);
        });
    }

    public void close() {
        if (database != null) {
            database.close();
        }
    }

    public static class Builder extends Service {

        public Builder(@NonNull Context context) {
            super(context);
        }
    }
}