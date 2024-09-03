package com.mantum.cmms.service.temporal;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mantum.cmms.domain.Recibir;
import com.mantum.cmms.domain.Evaluar;
import com.mantum.cmms.domain.SolicitudServicioRegistrar;
import com.mantum.cmms.net.ClientManager;
import com.mantum.core.util.MaxVersion;
import com.mantum.core.Mantum;
import com.mantum.core.event.OnOffline;
import com.mantum.core.exception.ServiceException;
import com.mantum.core.service.Authentication;
import com.mantum.core.util.Assert;
import com.mantum.component.util.Timeout;

import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Deprecated
public final class SolicitudServicioService extends Mantum.Service {

    private final static String TAG = SolicitudServicioService.class.getSimpleName();

    private final boolean authenticate;

    private final Mantum.Callback callback;

    private final OnOffline onOffline;

    private final String token;

    private SolicitudServicioService(Context context, String url, String endpoint, String version, boolean offline,
                                     boolean authenticate, Mantum.Callback callback, OnOffline onOffline, String token) {
        super(context, url, endpoint, version, offline);
        this.authenticate = authenticate;
        this.callback = callback;
        this.onOffline = onOffline;
        this.token = token;
    }

    public void register(@NonNull SolicitudServicioRegistrar register) {
        Create create = new Create(this, register);
        create.execute();
    }

    public void evaluate(@NonNull Evaluar evaluar) {
        Evaluate evaluate = new Evaluate(this, evaluar);
        evaluate.execute();
    }

    public void toReceive(@NonNull Recibir recibir) {
        Receive receive = new Receive(this, recibir);
        receive.execute();
    }

    public static class Builder extends Mantum.Builder<SolicitudServicioService> {

        public Builder(@NonNull Context context) {
            super(context);
        }

        @Override
        public SolicitudServicioService build() throws ServiceException {
            this.ok();
            return new SolicitudServicioService(
                    this.context, this.url, this.endPoint,
                    this.version, this.offline, this.authenticate,
                    this.callback, this.onOffline, this.token);
        }
    }

    private static class Create extends Mantum.ServiceTask<SolicitudServicioService> {

        private final SolicitudServicioRegistrar register;

        private Create(SolicitudServicioService instance, SolicitudServicioRegistrar register) {
            super(instance);
            this.register = register;
        }

        @Override
        protected Void doInBackground(Void... voids) {
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
                        .addFormDataPart("entityid", String.valueOf(this.register.getIdEntidad()))
                        .addFormDataPart("createdate", this.register.getFechaInicio() + " " + register.getHoraInicio())
                        .addFormDataPart("entitytype", Assert.isNull(this.register.getTipoEntidad()) ? "" : this.register.getTipoEntidad())
                        .addFormDataPart("entitylabel", Assert.isNull(this.register.getNombreEntidad()) ? "" : this.register.getNombreEntidad())
                        .addFormDataPart("priority", this.register.getPrioridad())
                        .addFormDataPart("type", String.valueOf(this.register.getIdTipo()))
                        .addFormDataPart("areaid", String.valueOf(this.register.getIdArea()))
                        .addFormDataPart("description", this.register.getDescripcion())
                        .addFormDataPart("token", this.register.getToken());


                RequestBody body = multipart.build();
                Request.Builder builder = new Request.Builder()
                        .url(this.instance.getPath())
                        .addHeader("cache-control", "no-cache")
                        .addHeader("accept", "application/vnd.mantum.app-v" + this.instance.getVersion() + "+json")
                        .addHeader("content-type", "multipart/form-data; boundary=---011000010111000001101001")
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

                String json = response.body().string();
                Gson gson = new GsonBuilder().create();
                if (response.isSuccessful()) {
                    Mantum.Success success = gson.fromJson(json, Mantum.Success.class);
                    success.setVersion(MaxVersion.get(response.networkResponse()));
                    this.instance.callback.success(success, true);
                } else {
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
                        gson.toJson(this.register),
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

    private static class Evaluate extends Mantum.ServiceTask<SolicitudServicioService> {

        private final Evaluar evaluar;

        private Evaluate(@NonNull SolicitudServicioService instance, Evaluar evaluar) {
            super(instance);
            this.evaluar = evaluar;
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

                Gson gson = new GsonBuilder().create();
                String data = gson.toJson(this.evaluar);

                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, data);

                Request.Builder builder = new Request.Builder()
                        .url(this.instance.getPath())
                        .addHeader("accept", "application/vnd.mantum.app-v" + this.instance.getVersion() + "+json")
                        .addHeader("accept-language", "application/json")
                        .addHeader("cache-control", "no-cache")
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

                String json = response.body().string();
                if (response.isSuccessful()) {
                    Mantum.Success success = gson.fromJson(json, Mantum.Success.class);
                    success.setVersion(MaxVersion.get(response.networkResponse()));
                    this.instance.callback.success(success, true);
                } else {
                    Mantum.Error error = gson.fromJson(json, Mantum.Error.class);
                    error.message(response.message());
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
                        gson.toJson(this.evaluar),
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

    private static class Receive extends Mantum.ServiceTask<SolicitudServicioService> {

        private final Recibir recibir;

        private Receive(@NonNull SolicitudServicioService instance, Recibir recibir) {
            super(instance);
            this.recibir = recibir;
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
                        .addFormDataPart("idss", String.valueOf(this.recibir.getIdot()))
                        .addFormDataPart("statereceive", this.recibir.getStatereceive())
                        .addFormDataPart("reason", this.recibir.getReason())
                        .addFormDataPart("eval", this.recibir.getEvaluation());

                RequestBody body = multipart.build();
                Request.Builder builder = new Request.Builder()
                        .url(this.instance.getPath())
                        .addHeader("accept", "application/vnd.mantum.app-v" + this.instance.getVersion() + "+json")
                        .addHeader("cache-control", "no-cache")
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
                if (response.isSuccessful()) {
                    Mantum.Success success = gson.fromJson(json, Mantum.Success.class);
                    success.setVersion(MaxVersion.get(response.networkResponse()));
                    this.instance.callback.success(success, true);
                } else {
                    Mantum.Error error = gson.fromJson(json, Mantum.Error.class);
                    error.message(response.message());
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
}