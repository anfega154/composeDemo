package com.mantum.cmms.service;

import android.content.Context;

import androidx.annotation.NonNull;

import android.util.Log;

import com.google.gson.Gson;
import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.GamaMantenimiento;
import com.mantum.cmms.entity.ReclasificacionGama;
import com.mantum.cmms.entity.SubtipoReparacionGama;
import com.mantum.cmms.entity.TipoReparacionGama;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

import static com.mantum.component.Mantum.isConnectedOrConnecting;

public class GamaMantenimientoService extends MicroServices {

    private final static String TAG = GamaMantenimientoService.class.getSimpleName();

    private final Database database;

    private final Cuenta cuenta;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(Timeout.CONNECT, TimeUnit.SECONDS)
            .writeTimeout(Timeout.WRITE, TimeUnit.SECONDS)
            .readTimeout(Timeout.READ, TimeUnit.SECONDS)
            .build();

    public GamaMantenimientoService(@NonNull Context context, Cuenta cuenta) {
        super(context, cuenta.getServidor().getUrl(), cuenta.getToken(context), ClientManager.prepare(
                new OkHttpClient.Builder(), context
        ));
        this.database = new Database(context);
        this.cuenta = cuenta;
    }

    public Observable<ResponseBodyGet> getClasificacionesGama() {
        return Observable.create(subscriber -> {
            if (!isConnectedOrConnecting(context)) {
                subscriber.onError(new Exception(context.getString(R.string.offline)));
                return;
            }

            String url = Preferences.url(context, "restapp/app/getclasificacionesgama");
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

                            subscriber.onNext(content);
                            subscriber.onComplete();
                        } else {
                            if (!subscriber.isDisposed()) {
                                subscriber.onError(new Exception(context.getString(R.string.error_app)));
                            }
                        }
                    } catch (Exception e) {
                        if (!call.isCanceled() && !subscriber.isDisposed()) {
                            subscriber.onError(e);
                        }
                    }
                    response.close();
                }
            });
        });
    }

    public void saveClasificacionesGama(ResponseBodyGet responseBodyGet) {
        BodyGetAllVariables variables = responseBodyGet.getBody();
        database.executeTransaction(realm -> {
            realm.where(ReclasificacionGama.class)
                    .findAll()
                    .deleteAllFromRealm();

            realm.where(TipoReparacionGama.class)
                    .findAll()
                    .deleteAllFromRealm();

            realm.where(SubtipoReparacionGama.class)
                    .findAll()
                    .deleteAllFromRealm();

            realm.where(GamaMantenimiento.class)
                    .findAll()
                    .deleteAllFromRealm();

            RealmList<ReclasificacionGama> reclasificacionGamas = new RealmList<>();
            for (ReclasificacionGama reclasificacionGama : variables.getReclasificaciones()) {
                reclasificacionGama.setCuenta(cuenta);

                RealmList<TipoReparacionGama> tipoReparacionGamas = new RealmList<>();
                for (TipoReparacionGama tipoReparacionGama : reclasificacionGama.getTiposreparacion()) {
                    tipoReparacionGama.setCuenta(cuenta);

                    RealmList<SubtipoReparacionGama> subtipoReparacionGamas = new RealmList<>();
                    for (SubtipoReparacionGama subtipoReparacionGama : tipoReparacionGama.getSubtiposreparacion()) {
                        subtipoReparacionGama.setCuenta(cuenta);

                        RealmList<GamaMantenimiento> gamaMantenimientos = new RealmList<>();
                        for (GamaMantenimiento gamaMantenimiento : subtipoReparacionGama.getGamas()) {
                            gamaMantenimiento.setCuenta(cuenta);
                            gamaMantenimiento.setIdreclasificacion(reclasificacionGama.getId());
                            gamaMantenimiento.setIdtiporeparacion(tipoReparacionGama.getId());
                            gamaMantenimiento.setIdsubtiporeparacion(subtipoReparacionGama.getId());
                            gamaMantenimientos.add(realm.copyToRealm(gamaMantenimiento));
                        }
                        subtipoReparacionGama.setGamas(gamaMantenimientos);
                        subtipoReparacionGamas.add(realm.copyToRealm(subtipoReparacionGama));
                    }
                    tipoReparacionGama.setSubtiposreparacion(subtipoReparacionGamas);
                    tipoReparacionGamas.add(realm.copyToRealm(tipoReparacionGama));
                }
                reclasificacionGama.setTiposreparacion(tipoReparacionGamas);
                reclasificacionGamas.add(realm.copyToRealm(reclasificacionGama));
            }
            realm.insertOrUpdate(reclasificacionGamas);
        });
    }

    public static class ResponseBodyGet {
        BodyGetAllVariables body;

        public BodyGetAllVariables getBody() {
            return body;
        }
    }

    public static class BodyGetAllVariables {
        List<ReclasificacionGama> reclasificaciones;

        public List<ReclasificacionGama> getReclasificaciones() {
            return reclasificaciones;
        }
    }

    public void onDestroy() {
        if (database != null) {
            database.close();
        }
    }
}
