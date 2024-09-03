package com.mantum.cmms.service;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Calendario;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.net.ClientManager;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.http.MicroServices;
import com.mantum.component.service.Calendar;
import com.mantum.component.service.Event;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import io.reactivex.Observable;
import io.realm.RealmResults;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class CalendarioService extends MicroServices {

    private static final String TAG = CalendarioService.class.getSimpleName();

    private final String version;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private final Database database;

    public CalendarioService(@NonNull Context context, @NonNull Cuenta cuenta) {
        super(context, cuenta.getServidor().getUrl(), cuenta.getToken(context), ClientManager.prepare(
                new OkHttpClient.Builder(), context
        ));
        this.database = new Database(context);
        version = cuenta.getServidor().getVersion();
    }

    public Observable<Calendario.Request> fetch(final Long idpersonal) {
        return Observable.create(subscriber -> {
            if (!Mantum.isConnectedOrConnecting(context)) {
                subscriber.onComplete();
                return;
            }

            Date dateIni = new Date();
            Date dateFin = new Date();
            java.util.Calendar calIni = java.util.Calendar.getInstance();
            java.util.Calendar calFin = java.util.Calendar.getInstance();
            calIni.setTime(dateIni);
            calFin.setTime(dateFin);
            dateIni = calIni.getTime();
            calFin.add(java.util.Calendar.DATE, 30);
            dateFin = calFin.getTime();

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd", Locale.getDefault());

            Request request = new Request.Builder().get()
                    .url(url + "/restapp/app/obtenercalendariopersonal?idpersonal=" + idpersonal + "&fechainicio=" + simpleDateFormat.format(dateIni) + "&fechafin=" + simpleDateFormat.format(dateFin))
                    .addHeader("token", token)
                    .addHeader("accept", Version.build(version))
                    .addHeader("cache-control", "no-cache")
                    .addHeader("accept-language", "application/json")
                    .build();

            Log.i(TAG, String.format("GET -> %s", request.url()));
            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                    if (call.isCanceled()) {
                        return;
                    }

                    ResponseBody body = response.body();
                    if (body == null) {
                        subscriber.onError(new Exception("Ocurrio un error a la hora de realizar la petición"));
                        return;
                    }

                    String json = body.string();
                    try {
                        if (response.isSuccessful()) {
                            Version.save(context, response.header("Max-Version"));
                            Response content = new Gson().fromJson(json, Response.class);
                            Calendario.Request request = content.getBody(Calendario.Request.class);

                            subscriber.onNext(request);
                            subscriber.onComplete();
                        } else {
                            subscriber.onError(new Exception(context.getString(R.string.error_get_calendario)));
                        }
                    } catch (Exception e) {
                        subscriber.onError(new Exception(context.getString(R.string.error_calendario)));
                    }
                    response.close();
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!call.isCanceled() && !subscriber.isDisposed()) {
                        subscriber.onError(new Exception(context.getString(R.string.error_pendientes)));
                    }
                }
            });
        });
    }

    public Observable<List<Calendario>> save(List<Calendario> values) {
        Calendar calendar = new Calendar(context);
        return Observable.create(subscriber -> database.executeTransactionAsync(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            List<Long> skip = new ArrayList<>();
            for (Calendario value : values) {
                skip.add(value.getId());
                Calendario temporal = self.where(Calendario.class)
                        .equalTo("id", value.getId())
                        .findFirst();

                try {
                    if (temporal == null) {
                        Event event = new Event();
                        event.setBegin(simpleDateFormat, value.getStart());
                        event.setEnd(simpleDateFormat, value.getEnd());
                        event.setTitle(value.getTitulo());
                        event.setDescription(value.getDescripcion());
                        event.setDiaCompleto(value.isDiaCompleto());
                        event.setColor(value.getColor());

                        Long idEvento = calendar.createEvent(cuenta.getIdCalendario(), event);
                        if (idEvento != null) {
                            value.setUuid(UUID.randomUUID().toString());
                            value.setCuenta(cuenta);
                            value.setIdCalendario(idEvento);
                            self.insert(value);
                        }
                    } else {
                        temporal.setStart(value.getStart());
                        temporal.setEnd(value.getEnd());
                        temporal.setTitulo(value.getTitulo());
                        temporal.setDescripcion(value.getDescripcion());
                        temporal.setDiaCompleto(value.isDiaCompleto());

                        Event event = new Event();
                        event.setBegin(simpleDateFormat, temporal.getStart());
                        event.setEnd(simpleDateFormat, temporal.getEnd());
                        event.setTitle(temporal.getTitulo());
                        event.setDescription(temporal.getDescripcion());
                        event.setDiaCompleto(temporal.isDiaCompleto());
                        event.setColor(temporal.getColor());

                        calendar.updateEvent(temporal.getIdCalendario(), event);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "save: ", e);
                }
            }

            RealmResults<Calendario> eventos = self.where(Calendario.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .not().in("id", skip.toArray(new Long[]{}))
                    .findAll();

            for (Calendario evento : eventos) {
                calendar.deleteEvent(evento.getIdCalendario());
                evento.deleteFromRealm();
            }

            subscriber.onNext(values);
        }, subscriber::onComplete, subscriber::onError));
    }
}