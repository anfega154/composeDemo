package com.mantum.cmms.service;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;

import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Autorizacion;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Autorizaciones;
import com.mantum.cmms.entity.Personal;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class AutorizacionAccesoService extends Service {

    private static final String TAG = AutorizacionAccesoService.class.getSimpleName();

    private final Realm realm;

    public AutorizacionAccesoService(@NonNull Context context) {
        super(context);
        this.realm = new Database(context).instance();
    }

    @Override
    public void cancel() {
        super.cancel();
        if (realm != null) {
            realm.close();
        }
    }

    private static class MisAutorizaciones {

        private final List<Autorizaciones> autorizaciones;

        private final boolean acceso;

        private MisAutorizaciones(List<Autorizaciones> autorizaciones, boolean acceso) {
            this.autorizaciones = autorizaciones;
            this.acceso = acceso;
        }
    }

    @NonNull
    private MisAutorizaciones getAutorizacion(@NonNull String cedula, @NonNull Cuenta cuenta) {
        RealmResults<Autorizaciones> autorizaciones = realm.where(Autorizaciones.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("personal.cedula", cedula)
                .findAll();

        Date now = new Date();
        List<Autorizaciones> results = new ArrayList<>();
        for (Autorizaciones result : autorizaciones) {
            if (result.getModulo().equals(Autorizaciones.MODULO_MARCAS)) {
                for (Personal personal : result.getPersonal()) {
                    if (personal.getCedula().equals(cedula)) {
                        results.add(realm.copyFromRealm(result));
                        break;
                    }
                }
                break;
            }

            if (result.getFechainicio().compareTo(now) * now.compareTo(result.getFechafin()) >= 0) {
                results.add(realm.copyFromRealm(result));
            }
        }

        boolean acceso = true;
        if (results.size() == 0) {
            acceso = false;
            if (autorizaciones.size() > 0) {
                results = realm.copyFromRealm(autorizaciones);
            }
        }

        return new MisAutorizaciones(results, acceso);
    }

    public Observable<Response> get() {
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
            Log.e(TAG, "get:  " + cuenta.getToken(seed) );
            Request request = new Request.Builder()
                    .addHeader("token", cuenta.getToken(seed))
                    .addHeader("accept", Version.build(cuenta.getServidor().getVersion()))
                    .addHeader("cache-control", "no-cache")
                    .addHeader("accept-language", "application/json")
                    .url(cuenta.getServidor().getUrl() + "/restapp/app/getallinfo")
                    .get().build();

            Log.i(TAG, "GET -> " + request.url());
            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!call.isCanceled()) {
                        subscriber.onError(new Mantum.HttpException(e.getMessage()));
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response)
                        throws IOException {
                    if (call.isCanceled()) {
                        return;
                    }

                    Log.i(TAG, "GET <- " + request.url());
                    ResponseBody body = response.body();
                    if (body == null) {
                        subscriber.onError(new Exception(context.getString(R.string.error_request_autorizaciones)));
                        return;
                    }

                    String json = body.string();
                    try {
                        Response content = gson.fromJson(json, Response.class);
                        if (response.isSuccessful()) {
                            content.setVersion(response.header("Max-Version"));

                            subscriber.onNext(content);
                            subscriber.onComplete();
                        } else {
                            subscriber.onError(new Exception(Response.buildMessage(content)));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onResponse: ", e);
                        subscriber.onError(new Exception(context.getString(R.string.error_request_autorizaciones)));
                    }

                    response.close();
                }
            });
        });
    }

    public Observable<Response> fetch(String cedula) {
        return Observable.create(subscriber -> {
            Cuenta cuenta = realm.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            if (!Mantum.isConnectedOrConnecting(context)) {
                MisAutorizaciones misAutorizaciones
                        = getAutorizacion(cedula, cuenta);

                String nombre = "";
                if (misAutorizaciones.autorizaciones.size() > 0) {
                    for (Personal personal : misAutorizaciones.autorizaciones.get(0).getPersonal()) {
                        if (cedula.equals(personal.getCedula())) {
                            nombre = personal.getNombre();
                            break;
                        }
                    }
                }

                if (misAutorizaciones.acceso) {
                    Autorizacion autorizacion = new Autorizacion();
                    autorizacion.setAcceso(true);
                    autorizacion.setNombre(nombre);
                    autorizacion.setAutorizaciones(misAutorizaciones.autorizaciones);

                    Response response = new Response("", autorizacion, new ArrayList<>());
                    subscriber.onNext(response);
                    subscriber.onComplete();
                } else {
                    Autorizacion autorizacion = new Autorizacion();
                    autorizacion.setAcceso(false);
                    autorizacion.setNombre(nombre);
                    autorizacion.setAutorizaciones(misAutorizaciones.autorizaciones);

                    Response response = new Response("", autorizacion, new ArrayList<>());
                    subscriber.onNext(response);
                    subscriber.onComplete();
                }
                return;
            }

            Bundle bundle = Mantum.bundle(context);
            if (bundle == null) {
                subscriber.onError(new Exception(context.getString(R.string.token_error)));
                return;
            }

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"), "{ \"cedula\" : " + cedula + " }");

            String seed = bundle.getString("Mantum.Authentication.Token");
            Request request = new Request.Builder()
                    .addHeader("token", cuenta.getToken(seed))
                    .addHeader("accept", Version.build(cuenta.getServidor().getVersion()))
                    .addHeader("cache-control", "no-cache")
                    .addHeader("accept-language", "application/json")
                    .url(cuenta.getServidor().getUrl() + "/restapp/app/obtenerautorizacionesvalidas")
                    .post(body).build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!call.isCanceled()) {
                        subscriber.onError(new Mantum.HttpException(e.getMessage()));
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
                        subscriber.onError(new Exception(context.getString(R.string.error_request_lecturas)));
                        return;
                    }

                    String json = body.string();
                    try {
                        Response content = gson.fromJson(json, Response.class);
                        if (response.isSuccessful()) {
                            content.setVersion(response.header("Max-Version"));

                            subscriber.onNext(content);
                            subscriber.onComplete();
                        } else {
                            subscriber.onError(new Exception(Response.buildMessage(content)));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onResponse: ", e);
                        subscriber.onError(new Exception(context.getString(R.string.error_response_autorizaciones)));
                    }

                    response.close();
                }
            });
        });
    }
}