package com.mantum.cmms.service;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Certificado;
import com.mantum.cmms.entity.Cuenta;

import java.util.Date;
import java.util.UUID;

import io.realm.Sort;

public class CertificadoServices {

    private final Database database;

    private final Context context;

    public CertificadoServices(@NonNull Context context) {
        this.database = new Database(context);
        this.context = context;
    }

    public void save(@NonNull Certificado certificado) {
        database.executeTransaction(realm -> {
            certificado.setKey(UUID.randomUUID().toString());
            certificado.setCreation(new Date());
            realm.insert(certificado);
        });
    }

    public void update(@NonNull Certificado certificado) {
        database.executeTransaction(realm -> {
            Cuenta cuenta = realm.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                return;
            }

            realm.where(Certificado.class)
                    .equalTo("url", cuenta.getServidor().getUrl())
                    .equalTo("username", cuenta.getUsername())
                    .equalTo("database", cuenta.getServidor().getNombre())
                    .findAll()
                    .deleteAllFromRealm();

            certificado.setKey(UUID.randomUUID().toString());
            certificado.setCreation(new Date());
            certificado.setUrl(cuenta.getServidor().getUrl());
            certificado.setUsername(cuenta.getUsername());
            certificado.setDatabase(cuenta.getServidor().getNombre());

            realm.insert(certificado);
        });
    }

    @Nullable
    public Certificado find(@NonNull Cuenta cuenta) {
        return database.where(Certificado.class)
                .equalTo("username", cuenta.getUsername())
                .equalTo("url", cuenta.getServidor().getUrl())
                .equalTo("database", cuenta.getServidor().getNombre())
                .sort("creation", Sort.DESCENDING)
                .findFirst();
    }

    @Nullable
    public Certificado find(@NonNull String url, @NonNull String username, @NonNull String basename) {
        Database database = new Database(context);
        return database.where(Certificado.class)
                .equalTo("url", url)
                .equalTo("username", username)
                .equalTo("database", basename)
                .sort("creation", Sort.DESCENDING)
                .findFirst();
    }

    public void close() {
        database.close();
    }
}