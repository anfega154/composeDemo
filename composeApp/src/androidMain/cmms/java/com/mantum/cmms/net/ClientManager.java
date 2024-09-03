package com.mantum.cmms.net;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;

import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Certificado;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.net.util.LoggingInterceptor;
import com.mantum.cmms.service.CertificadoServices;

import okhttp3.OkHttpClient;

import static com.mantum.cmms.net.CertificateManager.Builder.read;

import java.io.InputStream;

public class ClientManager {

    private final static String TAG = ClientManager.class.getSimpleName();

    private final Context context;
    private final CertificadoServices certificadoServices;

    private ClientManager(@NonNull Context context) {
        this.context = context;
        this.certificadoServices = new CertificadoServices(context);
    }

    private Certificado find() {
        Database database = new Database(context);
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            throw new IllegalArgumentException("The user is not authenticated");
        }

        return certificadoServices.find(cuenta);
    }

    @NonNull
    public static OkHttpClient.Builder prepare(
            @NonNull OkHttpClient.Builder builder,
            @Nullable Certificado certificado) {
        try {
            builder.addInterceptor(new LoggingInterceptor());
            if (certificado == null) {
                return builder;
            }

            InputStream server = read(certificado.getServer());
            if (server == null) {
                Log.e(TAG, "No fue posible leer el certificado del servidor");
                return builder;
            }

            InputStream client = read(certificado.getClient());
            if (client == null) {
                Log.e(TAG, "No fue posible leer el certificado del cliente");
                return builder;
            }

            CertificateManager certificateManager = new CertificateManager.Builder()
                    .server(server)
                    .client(client, certificado.getPassword())
                    .build();

            builder.sslSocketFactory(
                    certificateManager.getSSLSocketFactory(), certificateManager.getTrustManager());

            return builder;
        } catch (Exception e) {
            Log.e(TAG, "prepare: ", e);
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @NonNull
    public static OkHttpClient.Builder prepare(
            @NonNull OkHttpClient.Builder builder, @NonNull Context context) {
        ClientManager clientManager = new ClientManager(context);
        Certificado certificado = clientManager.find();
        return prepare(builder, certificado);
    }
}