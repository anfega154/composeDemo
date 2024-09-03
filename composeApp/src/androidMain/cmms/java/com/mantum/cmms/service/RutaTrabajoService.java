package com.mantum.cmms.service;

import android.content.Context;

import androidx.annotation.NonNull;

import android.util.Log;

import com.google.gson.Gson;
import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Actividad;
import com.mantum.cmms.entity.Adjuntos;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Entidad;
import com.mantum.cmms.entity.Entity;
import com.mantum.cmms.entity.Recurso;
import com.mantum.cmms.entity.RutaTrabajo;
import com.mantum.cmms.entity.Variable;
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

public class RutaTrabajoService extends MicroServices {

    private static final String TAG = RutaTrabajoService.class.getSimpleName();

    private final String version;

    private final Database database;

    public RutaTrabajoService(@NonNull Context context, @NonNull Cuenta cuenta) {
        super(context, cuenta.getServidor().getUrl(), cuenta.getToken(context), ClientManager.prepare(
                new OkHttpClient.Builder(), context
        ));
        this.database = new Database(context);
        version = cuenta.getServidor().getVersion();
    }

    public Observable<RutaTrabajo.Request> fetch(final Integer pages) {
        return Observable.create(subscriber -> {
            String endpoint = url + "/restapp/app/getmytodo?typetodo=RT&page=" + pages;
            if (!Mantum.isConnectedOrConnecting(context)) {
                Log.i("Http", String.format("GET -> %s - Sin conexión a internet", endpoint));
                subscriber.onNext(new RutaTrabajo.Request());
                subscriber.onComplete();
                return;
            }

            Request request = new Request.Builder().get()
                    .url(endpoint)
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
                            Version.save(context, response.header("Max-Version"));
                            Response content = new Gson().fromJson(json, Response.class);
                            RutaTrabajo.Request request = content.getBody(RutaTrabajo.Request.class);

                            subscriber.onNext(request);
                            subscriber.onComplete();
                        } else {
                            if (!subscriber.isDisposed()) {
                                subscriber.onError(new Exception(context.getString(R.string.error_get_ruta_trabajo)));
                            }
                        }
                    } catch (Exception e) {
                        if (!subscriber.isDisposed()) {
                            subscriber.onError(new Exception(context.getString(R.string.error_get_ruta_trabajo)));
                        }
                    }
                    response.close();
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!call.isCanceled() && !subscriber.isDisposed()) {
                        subscriber.onError(new Exception(context.getString(R.string.error_get_ruta_trabajo)));
                    }
                }
            });
        });
    }

    public Observable<List<RutaTrabajo>> fetchById(final Long id, final Long idEjecucion) {
        return Observable.create(subscriber -> {
            if (!Mantum.isConnectedOrConnecting(context)) {
                subscriber.onError(new Exception(context.getString(R.string.offline)));
                return;
            }

            String endpoint = url + "/restapp/app/refreshtodo?typetodo=RT&idtodo=" + idEjecucion;
            if (Integer.parseInt(version) >= 10) {
                endpoint = url + "/restapp/app/refreshtodo?typetodo=RT&id=" + id + "&idejecucion=" + idEjecucion;
            }

            Request request = new Request.Builder().get()
                    .addHeader("token", token)
                    .addHeader("accept", Version.build(this.version))
                    .addHeader("cache-control", "no-cache")
                    .addHeader("accept-language", "application/json")
                    .url(endpoint)
                    .build();

            Log.i(TAG, "GET -> " + request.url());
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
                        subscriber.onError(new Exception(context.getString(R.string.error_get_ruta_trabajo)));
                        return;
                    }

                    String json = body.string();
                    Log.i(TAG, "GET <- " + request.url());
                    try {
                        if (response.isSuccessful()) {
                            Response content = new Gson().fromJson(json, Response.class);
                            Version.save(context, response.header("Max-Version"));

                            RutaTrabajo.Request request = content.getBody(RutaTrabajo.Request.class);
                            subscriber.onNext(!request.getPendientes().isEmpty() ? request.getPendientes() : Collections.emptyList());
                            subscriber.onComplete();
                        } else {
                            subscriber.onError(new Exception(context.getString(R.string.error_get_ruta_trabajo)));
                        }
                    } catch (Exception e) {
                        subscriber.onError(new Exception(context.getString(R.string.error_get_response_ruta_trabajo)));
                    }
                    response.close();
                }
            });
        });
    }

    public Observable<List<RutaTrabajo>> save(@NonNull RutaTrabajo value) {
        return save(Collections.singletonList(value));
    }

    public Observable<List<RutaTrabajo>> save(@NonNull List<RutaTrabajo> values) {
        return Observable.create(subscriber -> database.executeTransactionAsync(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            for (RutaTrabajo value : values) {
                RutaTrabajo temporal;
                if (value.getIdejecucion() != null) {
                    temporal = self.where(RutaTrabajo.class)
                            .equalTo("id", value.getId())
                            .equalTo("idejecucion", value.getIdejecucion())
                            .equalTo("cuenta.UUID", cuenta.getUUID())
                            .findFirst();
                } else {
                    temporal = self.where(RutaTrabajo.class)
                            .equalTo("id", value.getId())
                            .isNull("idejecucion")
                            .equalTo("cuenta.UUID", cuenta.getUUID())
                            .findFirst();
                }

                if (temporal == null) {
                    for (Entidad entidad : value.getEntidades()) {
                        entidad.setCuenta(cuenta);
                        for (Actividad actividad : entidad.getActividades()) {
                            actividad.setUuid(UUID.randomUUID().toString());
                            actividad.setCuenta(cuenta);
                        }
                    }

                    value.setUUID(UUID.randomUUID().toString());
                    value.setCuenta(cuenta);
                    self.insert(value);
                } else {
                    value.setUUID(temporal.getUUID());
                    temporal.setIdejecucion(value.getIdejecucion());
                    temporal.setCodigo(value.getCodigo());
                    temporal.setNombre(value.getNombre());
                    temporal.setFecha(value.getFecha());
                    temporal.setEspecialidad(value.getEspecialidad());
                    temporal.setDescripcion(value.getDescripcion());
                    temporal.setTipogrupo(value.getTipogrupo());

                    // Guarda los recursos de la ruta de trabajo
                    if (temporal.getRecursos() != null && !temporal.getRecursos().isEmpty()) {
                        temporal.getRecursos().deleteAllFromRealm();
                    }

                    RealmList<Recurso> recursos = new RealmList<>();
                    for (Recurso recurso : value.getRecursos()) {
                        recursos.add(self.copyToRealm(recurso));
                    }
                    temporal.setRecursos(recursos);

                    // Guarda las entidades de la ruta de trabajo
                    if (temporal.getEntidades() != null && !temporal.getEntidades().isEmpty()) {
                        for (Entidad entidad : temporal.getEntidades()) {
                            for (Variable variable : entidad.getVariables()) {
                                variable.getValores().deleteAllFromRealm();
                                if (variable.getUltimalectura() != null) {
                                    variable.getUltimalectura().deleteFromRealm();
                                }
                            }
                            entidad.getVariables().deleteAllFromRealm();

                            for (Actividad actividad : entidad.getActividades()) {
                                actividad.getAdjuntos().deleteAllFromRealm();
                                actividad.getImagenes().deleteAllFromRealm();
                                for (Variable variable : actividad.getVariables()) {
                                    variable.getValores().deleteAllFromRealm();
                                    if (variable.getUltimalectura() != null) {
                                        variable.getUltimalectura().deleteFromRealm();
                                    }
                                }
                                actividad.getVariables().deleteAllFromRealm();
                            }
                            entidad.getActividades().deleteAllFromRealm();
                        }
                        temporal.getEntidades().deleteAllFromRealm();
                    }

                    RealmList<Entidad> entidades = new RealmList<>();
                    for (Entidad entidad : value.getEntidades()) {
                        RealmList<Actividad> actividades = new RealmList<>();
                        for (Actividad actividad : entidad.getActividades()) {
                            actividad.setUuid(UUID.randomUUID().toString());
                            actividad.setCuenta(cuenta);
                            actividades.add(self.copyToRealm(actividad));
                        }
                        entidad.setActividades(actividades);
                        entidades.add(self.copyToRealm(entidad));
                    }
                    temporal.setEntidades(entidades);

                    // Guarda las imagenes de la orden de trabajo
                    if (temporal.getImagenes() != null && !temporal.getImagenes().isEmpty()) {
                        temporal.getImagenes().deleteAllFromRealm();
                    }

                    RealmList<Adjuntos> imagenes = new RealmList<>();
                    for (Adjuntos adjunto : value.getImagenes()) {
                        imagenes.add(self.copyToRealm(adjunto));
                    }
                    temporal.setImagenes(imagenes);

                    // Guarda los adjuntos de la orden de trabajo
                    if (temporal.getAdjuntos() != null && !temporal.getAdjuntos().isEmpty()) {
                        temporal.getAdjuntos().deleteAllFromRealm();
                    }

                    RealmList<Adjuntos> adjuntos = new RealmList<>();
                    for (Adjuntos adjunto : value.getAdjuntos()) {
                        adjuntos.add(self.copyToRealm(adjunto));
                    }
                    temporal.setAdjuntos(adjuntos);

                    // Guarda am x ruta
                    if (temporal.getAmxgrupos() != null && !temporal.getAmxgrupos().isEmpty()) {
                        temporal.getAmxgrupos().deleteAllFromRealm();
                    }

                    RealmList<Entity> amxgrupos = new RealmList<>();
                    for (Entity entity : value.getAmxgrupos()) {
                        amxgrupos.add(self.copyToRealm(entity));
                    }
                    temporal.setAmxgrupos(amxgrupos);
                }
            }

            subscriber.onNext(values);

        }, subscriber::onComplete, subscriber::onError));
    }

    public Observable<List<Long>> removeById(final Long[] id) {
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
                    RealmResults<RutaTrabajo> pendientes = realm.where(RutaTrabajo.class)
                            .equalTo("cuenta.UUID", cuenta.getUUID())
                            .isNull("idejecucion")
                            .not().in("id", id)
                            .findAll();

                    List<Long> eliminados = new ArrayList<>();
                    for (RutaTrabajo pendiente : pendientes) {
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

    public Observable<List<Long>> removeByIdEjecucion(final Long[] id) {
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
                    RealmResults<RutaTrabajo> pendientes = realm.where(RutaTrabajo.class)
                            .equalTo("cuenta.UUID", cuenta.getUUID())
                            .isNotNull("idejecucion")
                            .not().in("idejecucion", id)
                            .findAll();

                    List<Long> eliminados = new ArrayList<>();
                    for (RutaTrabajo pendiente : pendientes) {
                        eliminados.add(pendiente.getIdejecucion());
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

    public Observable<List<RutaTrabajo>> remove(final Long id, final Long idEjecucion) {
        return Observable.create(subscriber -> {
            if (id == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_remove_ruta_trabajo)));
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

                RealmResults<RutaTrabajo> results = self.where(RutaTrabajo.class)
                        .equalTo("id", id)
                        .equalTo("idejecucion", idEjecucion)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findAll();

                results.deleteAllFromRealm();

                subscriber.onNext(Collections.emptyList());

            }, subscriber::onComplete, subscriber::onError);
        });
    }

    public Observable<RutaTrabajo.Request> download() {
        return Observable.create(subscriber -> {
            if (!Mantum.isConnectedOrConnecting(context)) {
                subscriber.onError(new Exception(context.getString(R.string.offline)));
                return;
            }

            Request request = new Request.Builder().get()
                    .addHeader("token", token)
                    .addHeader("accept", Version.build(version))
                    .addHeader("cache-control", "no-cache")
                    .addHeader("accept-language", "application/json")
                    .url(url + "/restapp/app/getmytodo?typetodo=LC")
                    .build();

            Log.i(TAG, "GET -> " + request.url());
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
                        subscriber.onError(new Exception(context.getString(R.string.error_get_ruta_trabajo)));
                        return;
                    }

                    String json = body.string();
                    Log.i(TAG, "GET <-" + request.url());
                    try {
                        if (response.isSuccessful()) {
                            Version.save(context, response.header("Max-Version"));
                            Response content = new Gson().fromJson(json, Response.class);
                            RutaTrabajo.Request request = content.getBody(RutaTrabajo.Request.class);

                            subscriber.onNext(request);
                            subscriber.onComplete();
                        } else {
                            subscriber.onError(new Exception(context.getString(R.string.error_get_ruta_trabajo)));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onResponse: ", e);
                        subscriber.onError(new Exception(context.getString(R.string.error_get_response_ruta_trabajo)));
                    }
                    response.close();
                }
            });
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