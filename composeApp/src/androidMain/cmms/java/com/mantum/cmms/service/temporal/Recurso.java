package com.mantum.cmms.service.temporal;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mantum.cmms.net.ClientManager;
import com.mantum.core.util.MaxVersion;
import com.mantum.core.Mantum;
import com.mantum.core.exception.ServiceException;
import com.mantum.core.service.Authentication;
import com.mantum.component.util.Timeout;

import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Deprecated
public final class Recurso {

    private static final String TAG = Recurso.class.getSimpleName();

    private final String url;

    private final boolean authenticate;

    private final Mantum.Callback callback;

    private final String version;

    private final Context context;

    private Send send;

    public Context getContext() {
        return context;
    }

    /**
     * Obtiene una nueva instancia del objeto
     *
     * @param url {@link String}
     * @param url          {@link String}
     * @param authenticate Indica si se necesita autenticar
     * @param callback {@link Mantum.Callback}
     * @param callback     {@link Mantum.Callback}
     */
    private Recurso(Context context, String url, boolean authenticate, Mantum.Callback callback, String version) {
        this.context = context;
        this.url = url;
        this.authenticate = authenticate;
        this.callback = callback;
        this.send = null;
        this.version = version;
    }

    /**
     * Obtiene los recursos que coincidan con el argumento
     *
     * @param value {@link String}
     */
    public void search(String value) {
        this.send = new Send(this, value);
        this.send.execute();
    }

    /**
     * Realiza una petición HTTP para obtener los recursos
     *
     * @see Mantum.ServiceTask
     */
    private final static class Send extends Mantum.ServiceTask<Recurso> {

        private final String value;

        /**
         * Obtiene una nueva instancia del objeto
         * @param instance {@link Recurso}
         * @param value    {@link String}
         */
        private Send(@NonNull Recurso instance, String value) {
            super(instance);
            this.value = value;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                OkHttpClient client = ClientManager.prepare(
                        new OkHttpClient.Builder()
                                .connectTimeout(Timeout.CONNECT, TimeUnit.SECONDS)
                                .writeTimeout(Timeout.WRITE, TimeUnit.SECONDS)
                                .readTimeout(Timeout.READ, TimeUnit.SECONDS), instance.getContext()
                ).build();

                Request.Builder builder = new Request.Builder()
                        .url(this.instance.url + "/" + this.value)
                        .addHeader("accept", "application/vnd.mantum.app-v" + this.instance.version + "+json")
                        .addHeader("accept-language", "application/json")
                        .addHeader("cache-control", "no-cache")
                        .get();

                if (this.instance.authenticate) {
                    Authentication.Model authentication = Authentication.Model.getInstace();
                    builder.addHeader("token", authentication.getToken());
                }

                Request request = builder.build();
                Response response = client.newCall(request).execute();

                Gson gson = new GsonBuilder().create();
                String json = response.body().string();

                if (response.isSuccessful()) {
                    Mantum.Success success = gson.fromJson(json, Mantum.Success.class);
                    success.setVersion(MaxVersion.get(response.networkResponse()));
                    success.message(response.message());
                    this.instance.callback.success(success, false);
                } else {
                    Mantum.Error error = gson.fromJson(json, Mantum.Error.class);
                    this.instance.callback.error(error, false);
                }
            } catch (InterruptedIOException ignored) {
                // Esta expecion es lanzada cuando la peticion es cancelada!
            } catch (Exception e) {
                Mantum.Error error = new Mantum.Error();
                error.message(e.getMessage());
                this.instance.callback.error(error, false);
            }
            return null;
        }
    }

    public static class Builder extends Mantum.Builder<Recurso> {

        /**
         * Obtiene una nueva instancia del objeto
         *
         * @param context {@link Context}
         */
        public Builder(@NonNull Context context) {
            super(context);
        }

        @Override
        public Recurso build() throws ServiceException {
            this.ok();
            return new Recurso(this.context, this.url + "/" + this.endPoint, this.authenticate, this.callback,
                    this.version);
        }
    }
}