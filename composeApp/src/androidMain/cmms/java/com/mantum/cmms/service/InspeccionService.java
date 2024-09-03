package com.mantum.cmms.service;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mantum.R;
import com.mantum.cmms.convert.ContenedorConvert;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Contenedor;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.EquipmentGrade;
import com.mantum.cmms.entity.parameter.UserParameter;
import com.mantum.cmms.net.ClientManager;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.http.MicroServices;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.realm.Case;
import io.realm.RealmQuery;
import io.realm.Sort;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class InspeccionService extends MicroServices {

    private final Cuenta cuenta;
    private final Context context;
    private final Database database;

    private final SimpleDateFormat simpleDateFormat
            = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    protected final Gson gsonBuilder = new GsonBuilder()
            .registerTypeAdapter(new TypeToken<Contenedor.Response>() {
            }.getType(), new ContenedorConvert())
            .create();

    public static class Builder extends Service {
        public Builder(@NonNull Context context) {
            super(context);
        }
    }

    public InspeccionService(@NonNull Context context, @NonNull Cuenta cuenta) {
        super(context, cuenta.getServidor().getUrl(), cuenta.getToken(context), ClientManager.prepare(
                new OkHttpClient.Builder(), context
        ));

        this.cuenta = cuenta;
        this.context = context;
        this.database = new Database(context);
    }

    public Observable<Long> count() {
        return Observable.create(subscriber -> {
            Database database = new Database(context);
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            long count = database.where(Contenedor.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .count();

            subscriber.onNext(count);
            subscriber.onComplete();
        });
    }

    public Observable<List<Contenedor>> search(String code, String location) {
        return Observable.create(subscriber -> {
            Database database = new Database(context);
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            RealmQuery<Contenedor> query = database.where(Contenedor.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID());

            if (code.trim().length() > 0 && location.trim().length() > 0) {
                query.and()
                        .contains("codigo", code, Case.INSENSITIVE)
                        .and()
                        .contains("ubicacion", location, Case.INSENSITIVE);
            } else if (code.trim().length() > 0 && location.trim().length() == 0) {
                query.and().contains("codigo", code, Case.INSENSITIVE);
            } else {
                query.and().contains("ubicacion", location, Case.INSENSITIVE);
            }

            List<Contenedor> contenedores = query.sort("ubicacion", Sort.ASCENDING).findAll();
            if (contenedores.isEmpty()) {
                subscriber.onError(new Exception(context.getString(R.string.contenedores_encontrados)));
                return;
            }

            // A1, B1, C2, A2, B2, C3
            List<Contenedor> resultados = database.copyFromRealm(contenedores);
            Collections.sort(resultados, (o1, o2) -> {
                String x = o1.getUbicacion();
                if (x != null && x.length() > 1) {
                    x = x.substring(1, 2);
                }

                String y = o2.getUbicacion();
                if (y != null && y.length() > 1) {
                    y = y.substring(1, 2);
                }

                if (x == null || y == null) {
                    return -1;
                }
                return x.compareTo(y);
            });

            subscriber.onNext(resultados);
            subscriber.onComplete();
        });
    }

    public Observable<Contenedor.Response> save(@NonNull Contenedor.Response response) {
        return save(response.getBody())
                .map(completed -> {
                    response.setBody(completed);
                    return response;
                });
    }

    public Observable<List<Contenedor>> save(List<Contenedor> contenedores) {
        return Observable.create(subscriber -> database.executeTransactionAsync(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            for (Contenedor contenedor : contenedores) {
                contenedor.setKey(UUID.randomUUID().toString());
                contenedor.setCuenta(cuenta);
                for (EquipmentGrade equipmentgradevalido : contenedor.getEquipmentgradevalidos()) {
                    equipmentgradevalido.setKey(UUID.randomUUID().toString());
                    equipmentgradevalido.setCuenta(cuenta);
                }
                self.insert(contenedor);
            }

            UserParameter userParameter = self.where(UserParameter.class)
                    .equalTo("name", UserParameter.ULTIMA_ACTUALIZACION_INSPECCION_PROGRAMADA)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findFirst();

            String value = simpleDateFormat.format(new Date());
            if (userParameter == null) {
                userParameter = new UserParameter();
                userParameter.setCuenta(cuenta);
                userParameter.setName(UserParameter.ULTIMA_ACTUALIZACION_INSPECCION_PROGRAMADA);
                userParameter.setValue(value);
                self.insert(userParameter);
            } else {
                userParameter.setValue(value);
            }

            subscriber.onNext(contenedores);
            subscriber.onComplete();
        }));
    }

    public Observable<Boolean> clear() {
        return Observable.create(subscriber -> {
            Database database = new Database(context);
            database.executeTransaction(self -> {
                Cuenta cuenta = self.where(Cuenta.class)
                        .equalTo("active", true)
                        .findFirst();

                if (cuenta == null) {
                    subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                    return;
                }

                self.where(EquipmentGrade.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findAll()
                        .deleteAllFromRealm();

                self.where(Contenedor.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findAll()
                        .deleteAllFromRealm();

                subscriber.onNext(true);
                subscriber.onComplete();
            });
        });
    }

    public Observable<Contenedor.Response> download() {
        String accept = cuenta.getServidor().getVersion();
        AtomicInteger counter = new AtomicInteger(1);

        return Observable.create(subscriber -> {
            if (!Mantum.isConnectedOrConnecting(context)) {
                if (!subscriber.isDisposed()) {
                    subscriber.onError(new Exception(context.getString(R.string.error_conexion)));
                }
                return;
            }

            String endpoint = url + "/restapp/app/getinitialdowload?page=" + counter.get();
            Request request = new Request.Builder().get()
                    .url(endpoint)
                    .addHeader("token", token)
                    .addHeader("accept", Version.build(accept))
                    .addHeader("cache-control", "no-cache")
                    .addHeader("accept-language", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                    if (call.isCanceled()) {
                        return;
                    }

                    ResponseBody body = response.body();
                    if (body == null) {
                        subscriber.onError(new Exception(context.getString(R.string.error_conexion)));
                        return;
                    }

                    String json = body.string();
                    try {
                        if (!response.isSuccessful()) {
                            if (!subscriber.isDisposed()) {
                                subscriber.onError(new Exception(context.getString(R.string.error_descargando_contenedores)));
                            }
                            return;
                        }

                        Version.save(context, response.header("Max-Version"));
                        Response content = gson.fromJson(json, Response.class);

                        int value = counter.incrementAndGet();
                        Contenedor.Response result = content.getBody(Contenedor.Response.class, gsonBuilder);

                        if (result.getNext() != null) {
                            result.setNext(value);
                        }

                        subscriber.onNext(result);
                        subscriber.onComplete();
                    } catch (Exception e) {
                        if (!subscriber.isDisposed()) {
                            subscriber.onError(new Exception(context.getString(R.string.error_descargando_contenedores)));
                        }
                    }

                    response.close();
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!call.isCanceled() && !subscriber.isDisposed()) {
                        subscriber.onError(new Exception(context.getString(R.string.error_descargando_contenedores)));
                    }
                }
            });
        });
    }

    public void close() {
        if (database != null) {
            database.close();
        }
    }
}
