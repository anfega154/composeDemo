package com.mantum.cmms.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.ResponseInventarioActivos;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Validation;
import com.mantum.cmms.entity.parameter.Barcode;
import com.mantum.cmms.entity.parameter.UserParameter;
import com.mantum.cmms.net.ClientManager;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.http.MicroServices;

import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmList;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import com.mantum.cmms.entity.parameter.UserParameter;

public class InventarioActivosService extends MicroServices {
    private final Database database;

    private final String version;
    private String urlApi;

    public InventarioActivosService(@NonNull Context context, @NonNull Cuenta cuenta) {
        super(context, cuenta.getServidor().getUrl(), cuenta.getToken(context), ClientManager.prepare(
                new OkHttpClient.Builder(), context
        ));
        String urlMantumLaravelParam = UserParameter.getValue(null, UserParameter.URL_MANTUM_FUTURE);
        database = new Database(context);
        version = cuenta.getServidor().getVersion();
        urlApi = (urlMantumLaravelParam == null || urlMantumLaravelParam.isEmpty()) ? url.replace("/publico","/future/public/") : urlMantumLaravelParam;
    }

    public Observable<ResponseInventarioActivos> getValidations(Long idExecutor) {
        return Observable.create(subscriber -> {
            if (!Mantum.isConnectedOrConnecting(context)) {
                subscriber.onError(new Exception(context.getString(R.string.offline)));
                return;
            }
            String endpoint = urlApi + "api/v1/validation/assets/list/validations/executors/"+idExecutor;
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
                        subscriber.onError(new Exception(context.getString(R.string.error_request_body_null)));
                        return;
                    }

                    String json = body.string();
                    Realm realm = Realm.getDefaultInstance();
                    try {
                        realm.executeTransaction(r -> {
                            realm.where(Validation.class).findAll().deleteAllFromRealm();
                        });

                        if (response.isSuccessful()) {
                            ResponseInventarioActivos content = gson.fromJson(json, ResponseInventarioActivos.class);
                            List<Validation> listaValidations = content.getValidations();
                            realm.executeTransaction(r -> {
                                for (Validation validation : listaValidations) {
                                    r.copyToRealmOrUpdate(validation);
                                }
                            });

                            subscriber.onNext(content);
                            subscriber.onComplete();
                        } else {
                            ResponseInventarioActivos content = gson.fromJson(json, ResponseInventarioActivos.class);
                            if (content.getCode()==404){
                                subscriber.onError(new Exception(content.getMessage()));
                            }else{
                                if (!subscriber.isDisposed()) {
                                    subscriber.onError(new Exception(context.getString(R.string.error_get_inventario_activos)));
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (!subscriber.isDisposed()) {
                            subscriber.onError(new Exception(context.getString(R.string.error_get_response_inventario_activos)));
                        }
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

    public String getUrlApi(){
        return this.urlApi;
    }

    public static class Builder extends Service {
        public Builder(@NonNull Context context) {
            super(context);
        }
    }
}
