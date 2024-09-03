package com.mantum.cmms.service.temporal;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mantum.cmms.domain.RecibirOT;
import com.mantum.cmms.domain.Terminar;
import com.mantum.cmms.net.ClientManager;
import com.mantum.core.util.MaxVersion;
import com.mantum.core.Mantum;
import com.mantum.core.event.OnOffline;
import com.mantum.core.exception.ServiceException;
import com.mantum.core.service.Authentication;
import com.mantum.core.util.Assert;
import com.mantum.component.util.Timeout;

import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Deprecated
public final class OrdenTrabajoService extends Mantum.Service {

    private static final String TAG = OrdenTrabajoService.class.getSimpleName();

    private final boolean authenticate;

    private final Mantum.Callback callback;

    private final OnOffline onOffline;

    private final String token;

    private final Integer base;

    private final Integer timestamp;

    private OrdenTrabajoService(Context context, String url, String endpoint,
                                String version, boolean offline,
                                boolean authenticate, Mantum.Callback callback,
                                OnOffline onOffline, String token,
                                Integer base, Integer timestamp) {
        super(context, url, endpoint, version, offline);
        this.authenticate = authenticate;
        this.callback = callback;
        this.onOffline = onOffline;
        this.token = token;
        this.base = base;
        this.timestamp = timestamp;
    }

    public void register(@NonNull RecibirOT recibir) {
        Send send = new Send(this, recibir);
        send.execute();
    }

    public void finish(@NonNull Terminar terminar) {
        Finish finish = new Finish(this, terminar);
        finish.execute();
    }

    //region Builder

    /**
     * Contiene los metodos necesarios para configurar
     * el servicio de orden de trabajo
     *
     * @author Jonattan Velásquez
     * @see Mantum.Builder
     */
    public static class Builder extends Mantum.Builder<OrdenTrabajoService> {

        public Builder(@NonNull Context context) {
            super(context);
        }

        @Override
        public OrdenTrabajoService build() throws ServiceException {
            this.ok();
            return new OrdenTrabajoService(
                    this.context, this.url, this.endPoint,
                    this.version, this.offline, this.authenticate,
                    this.callback, this.onOffline, this.token,
                    this.base, this.timestamp
            );
        }
    }

    //endregion

    private static class Send extends Mantum.ServiceTask<OrdenTrabajoService> {

        private final RecibirOT recibir;

        private Send(@NonNull OrdenTrabajoService instance, RecibirOT recibir) {
            super(instance);
            this.recibir = recibir;
            this.execution = false;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (!com.mantum.component.Mantum.isConnectedOrConnecting(this.instance.getContext()) || this.instance.isOffline()) {
                    return null;
                }

                this.execution = true;
                OkHttpClient client = ClientManager.prepare(
                        new OkHttpClient.Builder()
                                .connectTimeout(Timeout.CONNECT, TimeUnit.SECONDS)
                                .writeTimeout(Timeout.WRITE, TimeUnit.SECONDS)
                                .readTimeout(Timeout.READ, TimeUnit.SECONDS), instance.getContext()
                ).build();

                MultipartBody.Builder multipart = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idot", String.valueOf(this.recibir.getIdot()))
                        .addFormDataPart("statereceive", this.recibir.getStatereceive())
                        .addFormDataPart("reason", this.recibir.getReason());

                RequestBody body = multipart.build();
                Request.Builder builder = new Request.Builder()
                        .url(this.instance.getPath())
                        .addHeader("accept", "application/vnd.mantum.app-v" + this.instance.getVersion() + "+json")
                        .addHeader("cache-control", "no-cache")
                        .addHeader("accept-language", "application/json")
                        .addHeader("content-type", "multipart/form-data")
                        .post(body);

                if (this.instance.authenticate) {
                    Authentication.Model authentication = Authentication.Model.getInstace();
                    String token = Assert.isNull(this.instance.token)
                            ? authentication.getToken()
                            : this.instance.token;
                    builder.addHeader("token", token);
                }

                Request request = builder.build();
                Response response = client.newCall(request).execute();

                Gson gson = new GsonBuilder().create();
                String json = response.body().string();
                Log.d(TAG, "doInBackground: respuesta -> " + json);
                if (response.isSuccessful()) {
                    Mantum.Success success = gson.fromJson(json, Mantum.Success.class);
                    success.setVersion(MaxVersion.get(response.networkResponse()));
                    this.instance.callback.success(success, true);
                } else {
                    Mantum.Error error = gson.fromJson(json, Mantum.Error.class);
                    this.instance.callback.error(error, true);
                }
            } catch (Exception e) {
                Log.e(TAG, "doInBackground: ", e);
                Mantum.Error error = new Mantum.Error();
                error.message(e.getMessage());
                this.instance.callback.error(error, true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (!this.execution && !Assert.isNull(this.instance.onOffline)) {
                Log.d(TAG, "onPostExecute: Recibiendo orden de trabajo!");
                Gson gson = new GsonBuilder().create();
                Mantum.Response response = this.instance.onOffline.onRequest(
                        gson.toJson(this.recibir),
                        this.instance.getUrl(),
                        this.instance.getEndpoint(),
                        this.instance.getVersion());

                if (!Assert.isNull(response)) {
                    if (response.isOk()) {
                        this.instance.callback.success((Mantum.Success) response, true);
                    } else {
                        this.instance.callback.error((Mantum.Error) response, true);
                    }
                }
            }
        }
    }

    private static class Finish extends Mantum.ServiceTask<OrdenTrabajoService> {

        private final Terminar terminar;

        Finish(@NonNull OrdenTrabajoService instance, Terminar terminar) {
            super(instance);
            this.terminar = terminar;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (!com.mantum.component.Mantum.isConnectedOrConnecting(this.instance.getContext()) || this.instance.isOffline()) {
                    return null;
                }

                this.execution = true;
                OkHttpClient client = ClientManager.prepare(
                        new OkHttpClient.Builder()
                                .connectTimeout(Timeout.CONNECT, TimeUnit.SECONDS)
                                .writeTimeout(Timeout.WRITE, TimeUnit.SECONDS)
                                .readTimeout(Timeout.READ, TimeUnit.SECONDS), instance.getContext()
                ).build();

                MultipartBody.Builder multipart = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("token", terminar.getToken())
                        .addFormDataPart("idot", String.valueOf(this.terminar.getIdot()))
                        .addFormDataPart("datetime", this.terminar.getDatetime())
                        .addFormDataPart("reason", this.terminar.getReason());


                RequestBody body = multipart.build();
                Request.Builder builder = new Request.Builder()
                        .url(this.instance.getPath())
                        .addHeader("cache-control", "no-cache")
                        .addHeader("accept-language", "application/json")
                        .addHeader("content-type", "multipart/form-data")
                        .addHeader("accept", "application/vnd.mantum.app-v" + this.instance.getVersion() + "+json")
                        .post(body);

                if (this.instance.authenticate) {
                    Authentication.Model authentication = Authentication.Model.getInstace();
                    String token = Assert.isNull(this.instance.token)
                            ? authentication.getToken()
                            : this.instance.token;
                    builder.addHeader("token", token);
                }

                Request request = builder.build();
                Response response = client.newCall(request).execute();

                Gson gson = new GsonBuilder().create();
                String json = response.body().string();
                if (response.isSuccessful()) {
                    response.close();
                    Mantum.Success success = gson.fromJson(json, Mantum.Success.class);
                    success.setVersion(MaxVersion.get(response.networkResponse()));
                    this.instance.callback.success(success, true);
                } else {
                    response.close();
                    Mantum.Error error = gson.fromJson(json, Mantum.Error.class);
                    this.instance.callback.error(error, true);
                }
            } catch (Exception e) {
                Mantum.Error error = new Mantum.Error();
                error.message(e.getMessage());
                this.instance.callback.error(error, true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (!this.execution && !Assert.isNull(this.instance.onOffline)) {
                Gson gson = new GsonBuilder().create();
                Mantum.Response response = this.instance.onOffline.onRequest(
                        gson.toJson(this.terminar),
                        this.instance.getUrl(),
                        this.instance.getEndpoint(),
                        this.instance.getVersion());

                if (!Assert.isNull(response)) {
                    if (response.isOk()) {
                        this.instance.callback.success((Mantum.Success) response, true);
                    } else {
                        this.instance.callback.error((Mantum.Error) response, true);
                    }
                }
            }
        }
    }
}
