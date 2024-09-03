package com.mantum.cmms.service;

import android.content.Context;

import androidx.annotation.NonNull;

import android.util.Log;

import com.google.gson.Gson;
import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Evento;
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
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class EventoService extends MicroServices {

    private final Database database;

    private final String version;

    public EventoService(@NonNull Context context, @NonNull Cuenta cuenta) {
        super(context, cuenta.getServidor().getUrl(), cuenta.getToken(context), ClientManager.prepare(
                new OkHttpClient.Builder(), context
        ));
        this.database = new Database(context);
        version = cuenta.getServidor().getVersion();
    }

    public Observable<Evento.Request> fetch(final Integer pages) {
        return Observable.create(subscriber -> {
            if (!Mantum.isConnectedOrConnecting(context)) {
                subscriber.onNext(new Evento.Request());
                subscriber.onComplete();
                return;
            }

            Request request = new Request.Builder().get()
                    .url(url + "/restapp/app/getmytodo?typetodo=EVEN&page=" + pages)
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
                            Evento.Request request = content.getBody(Evento.Request.class);

                            subscriber.onNext(request);
                            subscriber.onComplete();
                        } else {
                            subscriber.onError(new Exception(context.getString(R.string.error_get_pendiente)));
                        }
                    } catch (Exception e) {
                        subscriber.onError(new Exception(context.getString(R.string.error_pendientes)));
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

    public Observable<List<Evento>> fetchById(long id) {
        return Observable.create(subscriber -> {
            if (!Mantum.isConnectedOrConnecting(context)) {
                subscriber.onError(new Exception(context.getString(R.string.offline)));
                return;
            }

            String endpoint = url + "/restapp/app/refreshtodo?typetodo=EVEN&idtodo=" + id;
            if (Integer.parseInt(version) >= 10) {
                endpoint = url + "/restapp/app/refreshtodo?typetodo=EVEN&id=" + id;
            }

            Request request = new Request.Builder().get()
                    .addHeader("token", token)
                    .addHeader("accept", Version.build(version))
                    .addHeader("cache-control", "no-cache")
                    .addHeader("accept-language", "application/json")
                    .url(endpoint)
                    .build();

            Log.i("Http", String.format("GET -> %s", request.url().url()));
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
                        subscriber.onError(new Exception(context.getString(R.string.error_request_detail_ot)));
                        return;
                    }

                    String json = body.string();
                    Log.i("Http", String.format("GET  <- %s is %s", request.url().url(), response.isSuccessful() ? "Ok" : "Error"));
                    try {
                        if (response.isSuccessful()) {
                            Response content = new Gson().fromJson(json, Response.class);
                            content.setVersion(response.header("Max-Version"));

                            Evento.Request data = content.getBody(Evento.Request.class);
                            subscriber.onNext(!data.getPendientes().isEmpty() ? data.getPendientes() : Collections.emptyList());
                            subscriber.onComplete();
                        } else {
                            subscriber.onError(new Exception(context.getString(R.string.error_get_orden_trabajo)));
                        }
                    } catch (Exception e) {
                        subscriber.onError(new Exception(context.getString(R.string.error_get_response_orden_trabajo)));
                    }

                    response.close();
                }
            });
        });
    }

    public Observable<List<Evento>> save(@NonNull Evento value) {
        return save(Collections.singletonList(value));
    }

    public Observable<List<Evento>> save(@NonNull List<Evento> values) {
        return Observable.create(subscriber -> database.executeTransactionAsync(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            for (Evento evento : values) {
                Evento busqueda = self.where(Evento.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .equalTo("id", evento.getId())
                        .findFirst();

                if (busqueda == null) {
                    evento.setUUID(UUID.randomUUID().toString());
                    evento.setCuenta(cuenta);
                    self.insert(evento);
                } else {
                    busqueda.setDescripcion(evento.getDescripcion());
                    busqueda.setEvento(evento.getEvento());
                    busqueda.setFecha(evento.getFecha());
                    busqueda.setLugar(evento.getLugar());
                    busqueda.setPersonal(evento.getPersonal());
                    busqueda.setColor(evento.getColor());
                }
            }

            subscriber.onNext(values);
        }, subscriber::onComplete, subscriber::onError));
    }

    public Observable<List<Evento>> remove(final Long[] id) {
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
                    realm.where(Evento.class)
                            .equalTo("cuenta.UUID", cuenta.getUUID())
                            .not().in("id", id)
                            .findAll().deleteAllFromRealm();

                    subscriber.onNext(new ArrayList<>());
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            });
            database.close();
        });
    }

    public Observable<List<Evento>> remove(final Long id) {
        return Observable.create(subscriber -> {
            if (id == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_remove_detail_ot)));
                return;
            }

            Database database = new Database(context);
            database.executeTransactionAsync(self -> {
                Cuenta cuenta = self.where(Cuenta.class)
                        .equalTo("active", true)
                        .findFirst();

                if (cuenta == null) {
                    subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                    return;
                }

                self.where(Evento.class)
                        .equalTo("id", id)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findAll()
                        .deleteAllFromRealm();

                subscriber.onNext(Collections.emptyList());

            }, subscriber::onComplete, subscriber::onError);
            database.close();
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
