package com.mantum.cmms.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mantum.demo.R;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Certificado;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.net.ClientManager;
import com.mantum.component.util.Timeout;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import static com.mantum.component.Mantum.isConnectedOrConnecting;

public class AutenticarServices {

    private static final String VERSION = "000";

    private final Context context;
    private final CertificadoServices certificadoServices;
    private final Gson gson = new GsonBuilder().create();

    public AutenticarServices(@NonNull Context context) {
        this.context = context;
        this.certificadoServices = new CertificadoServices(context);
    }

    public Observable<Response> autenticar(
            @NonNull String url,
            @NonNull String username,
            @NonNull String password,
            @NonNull String basename
    ) {
        return Observable.create(subscriber -> {
            if (!isConnectedOrConnecting(context)) {
                subscriber.onError(new Exception(
                        context.getString(R.string.offline)));
                return;
            }

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(
                    mediaType, "{\"token\" : \"" + password + "\"}");

            String endpoint = String.format("%s/restapp/app/login", url);
            Request request = new Request.Builder().url(endpoint).post(body)
                    .addHeader("cache-control", "no-cache")
                    .addHeader("content-type", "application/json")
                    .addHeader("accept", "application/vnd.mantum.app-v" + VERSION + "+json")
                    .build();

            Certificado certificado = certificadoServices.find(url, username, basename);
            OkHttpClient client = ClientManager.prepare(
                    new OkHttpClient.Builder()
                            .connectTimeout(Timeout.CONNECT, TimeUnit.SECONDS)
                            .writeTimeout(Timeout.WRITE, TimeUnit.SECONDS)
                            .readTimeout(Timeout.READ, TimeUnit.SECONDS), certificado ).build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!call.isCanceled() && !subscriber.isDisposed()) {
                        subscriber.onError(new Exception(
                                context.getString(R.string.request_error)));
                    }
                }

                @Override
                public void onResponse(
                        @NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                    if (call.isCanceled()) {
                        return;
                    }

                    ResponseBody body = response.body();
                    if (body == null) {
                        subscriber.onError(new Exception(
                                context.getString(R.string.authentication_error_connection)));
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
                            if (!subscriber.isDisposed()) {
                                if (response.code() == 400) {
                                    subscriber.onError(new Exception(
                                            context.getString(R.string.authentication_error_credentials)));
                                } else {
                                    subscriber.onError(new Exception(
                                            context.getString(R.string.authentication_error_connection)));
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (!subscriber.isDisposed()) {
                            subscriber.onError(new Exception(
                                    context.getString(R.string.request_reading_error)));
                        }

                    }
                    response.close();
                }
            });
        });
    }

    public Observable<Response> autenticar(@NonNull Cuenta cuenta, String token) {
        return autenticar(
                cuenta.getServidor().getUrl(),
                cuenta.getUsername(),
                token,
                cuenta.getServidor().getNombre()
        );
    }

    public void close() {
        certificadoServices.close();
    }
}
