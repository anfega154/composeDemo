package com.mantum.cmms.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.CategoriaEquipo;
import com.mantum.cmms.entity.CentroCostoEquipo;
import com.mantum.cmms.entity.Ciudad;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Departamento;
import com.mantum.cmms.entity.EstadoActualEquipo;
import com.mantum.cmms.entity.EstadoTransferenciaEquipo;
import com.mantum.cmms.entity.MedidasEquipo;
import com.mantum.cmms.entity.Pais;
import com.mantum.cmms.entity.UbicacionPredeterminada;
import com.mantum.cmms.net.ClientManager;
import com.mantum.cmms.util.Preferences;
import com.mantum.cmms.util.Version;
import com.mantum.component.http.MicroServices;
import com.mantum.component.util.Timeout;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.RealmList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import static com.mantum.component.Mantum.isConnectedOrConnecting;

public class ActivoService extends MicroServices {

    private final static String TAG = ActivoService.class.getSimpleName();

    private final Database database;

    private final Cuenta cuenta;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(Timeout.CONNECT, TimeUnit.SECONDS)
            .writeTimeout(Timeout.WRITE, TimeUnit.SECONDS)
            .readTimeout(Timeout.READ, TimeUnit.SECONDS)
            .build();

    public static class Builder extends Service {
        public Builder(@NonNull Context context) {
            super(context);
        }
    }

    public ActivoService(@NonNull Context context, Cuenta cuenta) {
        super(context, cuenta.getServidor().getUrl(), cuenta.getToken(context), ClientManager.prepare(
                new OkHttpClient.Builder(), context
        ));
        this.database = new Database(context);
        this.cuenta = cuenta;
    }

    public Observable<Response> searchByEntity(String criterio, String tipoEntidad) {
        return Observable.create(subscriber -> {
            if (!isConnectedOrConnecting(context)) {
                subscriber.onError(new Exception(context.getString(R.string.offline)));
                return;
            }

            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            String token = cuenta.getToken(context);
            if (token == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            Request request;
            if (Version.check(context, 13)) {
                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json"), getBodyBySearch(criterio, tipoEntidad));

                String url = cuenta.getServidor().getUrl() + "/restapp/app/searchentity";
                request = new Request.Builder().get().url(url)
                        .addHeader("token", token)
                        .addHeader("cache-control", "no-cache")
                        .addHeader("accept-language", "application/json")
                        .addHeader("accept", Version.build(cuenta.getServidor().getVersion()))
                        .post(body).build();
            } else {
                String url = cuenta.getServidor().getUrl() + "/restapp/app/searchentity?search=" + criterio + "&entitytype=" + tipoEntidad;
                request = new Request.Builder().get().url(url)
                        .addHeader("token", token)
                        .addHeader("cache-control", "no-cache")
                        .addHeader("accept-language", "application/json")
                        .addHeader("accept", Version.build(cuenta.getServidor().getVersion()))
                        .build();
            }

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!call.isCanceled()) {
                        subscriber.onError(new Exception(context.getString(R.string.request_error_search)));
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
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
                            Response content = gson.fromJson(json, Response.class);
                            if (!subscriber.isDisposed()) {
                                subscriber.onError(new Exception(content.getMessage()));
                            }
                        }
                    } catch (Exception e) {
                        if (!subscriber.isDisposed()) {
                            subscriber.onError(new Exception(context.getString(R.string.request_reading_error)));
                        }

                    }
                    response.close();
                }
            });
        });
    }

    public Observable<ResponseBodyGet> getAllVariables() {
        return Observable.create(subscriber -> {
            if (!isConnectedOrConnecting(context)) {
                subscriber.onError(new Exception(context.getString(R.string.offline)));
                return;
            }

            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            String token = cuenta.getToken(context);
            if (token == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            String url = cuenta.getServidor().getUrl() + "/restapp/app/getdataauxequipo";
            Request request = new Request.Builder().get().url(url)
                    .addHeader("token", token)
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

    public Observable<List<String>> getNombresEquipo(String nombre, @Nullable Long idFamilia) {
        return Observable.create(subscriber -> {
            if (!isConnectedOrConnecting(context)) {
                subscriber.onError(new Exception(context.getString(R.string.offline)));
                return;
            }

            String endpoint = "restapp/app/searchequiponombre?nombre=" + nombre;
            if (idFamilia != null) {
                endpoint += "&familia=" + idFamilia;
            }

            String url = Preferences.url(context, endpoint);
            Request request = new Request.Builder().get().url(url)
                    .addHeader("token", Preferences.token(context))
                    .addHeader("cache-control", "no-cache")
                    .addHeader("accept-language", "application/json")
                    .addHeader("accept", Version.build(cuenta.getServidor().getVersion()))
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!call.isCanceled() && !subscriber.isDisposed()) {
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

                            subscriber.onNext(content.getData().getNombres());
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

    public void saveAllVariables(ResponseBodyGet responseBodyGet) {
        BodyGetAllVariables variables = responseBodyGet.getData();
        database.executeTransaction(realm -> {

            RealmList<Pais> paises = new RealmList<>();
            paises.addAll(variables.getPaises());

            RealmList<Departamento> departamentos = new RealmList<>();
            departamentos.addAll(variables.getDepartamentos());

            RealmList<Ciudad> ciudades = new RealmList<>();
            ciudades.addAll(variables.getCiudades());

            RealmList<CentroCostoEquipo> centroCostoEquipos = new RealmList<>();
            for (CentroCostoEquipo centroCostoEquipo : variables.getCentrocostos()) {
                centroCostoEquipo.setCuenta(cuenta);
                centroCostoEquipos.add(realm.copyToRealm(centroCostoEquipo));
            }

            RealmList<MedidasEquipo> medidasEquipos = new RealmList<>();
            medidasEquipos.addAll(variables.getMedidas());

            RealmList<CategoriaEquipo> categoriaEquipos = new RealmList<>();
            categoriaEquipos.addAll(variables.getCategorias());

            RealmList<EstadoTransferenciaEquipo> estadoTransferenciaEquipos = new RealmList<>();
            estadoTransferenciaEquipos.addAll(variables.getEstados());

            RealmList<EstadoActualEquipo> estadoActualEquipos = new RealmList<>();
            estadoActualEquipos.addAll(variables.getEstadosactuales());

            UbicacionPredeterminada ubicacionPredeterminada = variables.getUbicaciones_predeterminadas();


            realm.where(Pais.class)
                    .findAll()
                    .deleteAllFromRealm();
            realm.insertOrUpdate(paises);

            realm.where(Departamento.class)
                    .findAll()
                    .deleteAllFromRealm();
            realm.insertOrUpdate(departamentos);

            realm.where(Ciudad.class)
                    .findAll()
                    .deleteAllFromRealm();
            realm.insertOrUpdate(ciudades);

            realm.insertOrUpdate(centroCostoEquipos);

            realm.where(MedidasEquipo.class)
                    .findAll()
                    .deleteAllFromRealm();
            realm.insertOrUpdate(medidasEquipos);

            realm.where(CategoriaEquipo.class)
                    .findAll()
                    .deleteAllFromRealm();
            realm.insertOrUpdate(categoriaEquipos);

            realm.where(EstadoTransferenciaEquipo.class)
                    .findAll()
                    .deleteAllFromRealm();
            realm.insertOrUpdate(estadoTransferenciaEquipos);

            realm.where(EstadoActualEquipo.class)
                    .findAll()
                    .deleteAllFromRealm();
            realm.insertOrUpdate(estadoActualEquipos);

            realm.where(UbicacionPredeterminada.class)
                    .findAll()
                    .deleteAllFromRealm();
            realm.insertOrUpdate(ubicacionPredeterminada);
        });
    }

    public static class ResponseBodyGet {

        BodyGetAllVariables body;

        public BodyGetAllVariables getData() {
            return body;
        }
    }

    public static class BodyGetAllVariables {
        List<Pais> paises;
        List<Departamento> departamentos;
        List<Ciudad> ciudades;
        List<CentroCostoEquipo> centrocostos;
        List<MedidasEquipo> medidas;
        List<CategoriaEquipo> categorias;
        List<EstadoTransferenciaEquipo> estados;
        List<EstadoActualEquipo> estadosactuales;
        UbicacionPredeterminada ubicaciones_predeterminadas;

        List<String> nombres;

        public List<Pais> getPaises() {
            return paises;
        }

        public List<Departamento> getDepartamentos() {
            return departamentos;
        }

        public List<Ciudad> getCiudades() {
            return ciudades;
        }

        public List<CentroCostoEquipo> getCentrocostos() {
            return centrocostos;
        }

        public List<MedidasEquipo> getMedidas() {
            return medidas;
        }

        public List<CategoriaEquipo> getCategorias() {
            return categorias;
        }

        public List<EstadoTransferenciaEquipo> getEstados() {
            return estados;
        }

        public List<EstadoActualEquipo> getEstadosactuales() {
            return estadosactuales;
        }

        public UbicacionPredeterminada getUbicaciones_predeterminadas() {
            return ubicaciones_predeterminadas;
        }

        public List<String> getNombres() {
            return nombres;
        }
    }

    public void onDestroy() {
        if (database != null) {
            database.close();
        }
    }

    public String getBodyBySearch(String search, String entitytype) {
        JsonObject body = new JsonObject();
        body.addProperty("search", search);
        body.addProperty("entitytype", entitytype);

        return new Gson().toJson(body);
    }
}
