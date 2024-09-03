package com.mantum.cmms.service;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.net.ClientManager;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.http.MicroServices;

import java.io.IOException;

import io.reactivex.Observable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DeviceServices extends MicroServices {

    private static final String TAG = DeviceServices.class.getSimpleName();

    private static final String VERSION = "000";

    public DeviceServices(@NonNull Context context, @NonNull Cuenta cuenta) {
        super(context, cuenta.getServidor().getUrl(), cuenta.getToken(context), ClientManager.prepare(
                new OkHttpClient.Builder(), context
        ));
    }

    public Observable<String> registrar(final String instanceId) {
        return Observable.create(subscriber -> {
            if (instanceId == null || "".equals(instanceId)) {
                subscriber.onError(new Exception("No se ha generado el ID de la notificacion!"));
                return;
            }

            if (!Mantum.isConnectedOrConnecting(context)) {
                subscriber.onError(new Exception("No tiene una conexion a internet"));
                return;
            }

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, new DeviceServices.Device(instanceId).toJson());

            String enpoint = String.format("%s/restapp/app/saveiddevice", url);
            Request request = new Request.Builder().post(body).url(enpoint)
                    .addHeader("token", token)
                    .addHeader("accept", Version.build(VERSION))
                    .addHeader("cache-control", "no-cache")
                    .addHeader("accept-language", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (call.isCanceled()) {
                        return;
                    }

                    ResponseBody body = response.body();
                    if (body == null) {
                        subscriber.onError(new Exception("Ocurrio un error a la hora de realizar la petición"));
                        return;
                    }

                    subscriber.onNext(body.string());
                    subscriber.onComplete();
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!call.isCanceled() && !subscriber.isDisposed()) {
                        subscriber.onError(e);
                    }
                }
            });
        });
    }

    private static class Device {

        private final String iddevice;

        private final String system;

        private Device(String iddevice) {
            this.iddevice = iddevice;
            this.system = "Android";
        }

        public String toJson() {
            return new Gson().toJson(this);
        }

        public String getIddevice() {
            return iddevice;
        }

        public String getSystem() {
            return system;
        }
    }
}