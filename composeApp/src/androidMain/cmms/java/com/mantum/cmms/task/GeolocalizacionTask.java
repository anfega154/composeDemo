package com.mantum.cmms.task;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.View;

import com.mantum.cmms.activity.ConfiguracionActivity;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Coordenada;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.cmms.entity.Sitio;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.service.ATNotificationService;
import com.mantum.cmms.service.SocketService;
import com.mantum.cmms.service.TransaccionService;
import com.mantum.component.service.Geolocation;
import com.mantum.component.service.handler.OnLocationListener;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class GeolocalizacionTask extends Service {

    private static final int NOTIFY_INTERVAL = 1000 * 60 * 5;

    private Timer timer = new Timer();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        timer = timer != null ? timer : new Timer();
        timer.scheduleAtFixedRate(new GeolocalizacionTask.Task(this), 0, NOTIFY_INTERVAL);
    }

    public static class Task extends TimerTask implements OnLocationListener {

        private final Context context;

        private final boolean showMessages;

        private final SharedPreferences sharedPreferences;

        private final Handler handler = new Handler();

        private final CompositeDisposable compositeDisposable = new CompositeDisposable();

        private final boolean force;

        Task(@NonNull Context context) {
            this(context, false);
        }

        public Task(@NonNull Context context, boolean showMessages) {
            this(context, showMessages, false);
        }

        public Task(@NonNull Context context, boolean showMessages, boolean force) {
            this.context = context;
            this.showMessages = showMessages;
            this.force = force;
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }

        @Override
        public void run() {
            handler.postDelayed(this::process, 2000);
        }

        public void process() {
            Geolocation geolocation = new Geolocation(context, this);
            geolocation.start();
        }

        @Override
        public void onLocationChanged(@NonNull Geolocation geolocation, @NonNull Location location) {
            Database database = new Database(context);
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                return;
            }

            if (!force) {
                boolean synchronize = sharedPreferences.getBoolean(
                        ConfiguracionActivity.PREFERENCIA_UBICACION, true);

                if (!synchronize) {
                    return;
                }
            }

            Coordenada coordenada = new Coordenada();
            coordenada.setLatitude(location.getLatitude());
            coordenada.setAltitude(location.getAltitude());
            coordenada.setAccuracy(location.getAccuracy());
            coordenada.setLongitude(location.getLongitude());

            SocketService socket = SocketService.getInstance();
            socket.setLatitude(location.getLatitude());
            socket.setLongitude(location.getLongitude());

            Transaccion transaccion = new Transaccion();
            transaccion.setCuenta(cuenta);
            transaccion.setUrl(cuenta.getServidor().getUrl() + "/restapp/app/savelocationuser");
            transaccion.setVersion(cuenta.getServidor().getVersion());
            transaccion.setValue(coordenada.toJson());
            transaccion.setModulo(Transaccion.MODULO_GEOLOCALIZACION);
            transaccion.setAccion(Transaccion.ACCION_UBICACION);
            transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);

            TransaccionService transaccionService = new TransaccionService(context);
            compositeDisposable.add(transaccionService.save(transaccion)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(self -> {
                    }, this::onError, () -> {
                        geolocation.stop();
                        transaccionService.close();
                    }));

            List<OrdenTrabajo> results = database.where(OrdenTrabajo.class)
                    .equalTo("movimiento", true)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findAll();

            if (results.isEmpty()) {
                database.close();
                return;
            }

            for (OrdenTrabajo result : results) {
                Sitio sitio = result.getSitio();
                if (sitio == null || !sitio.isCoordenada()) {
                    continue;
                }

                Location destination = new Location("sitio");
                destination.setLatitude(Double.parseDouble(sitio.getLongitud()));
                destination.setLongitude(Double.parseDouble(sitio.getLatitud()));

                float distance = location.distanceTo(destination);
                if (distance <= 10) {
                    ATNotificationService atNotificationService = new ATNotificationService(context);
                    atNotificationService.enSitioAutomatico(result.getId());
                }
            }

            database.close();
        }

        @Override
        public void onError(Throwable throwable) {
            if (showMessages) {
                View view = ((Activity) context).findViewById(android.R.id.content);
                if (throwable.getMessage() != null) {
                    Snackbar.make(view, throwable.getMessage(), Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        }
    }
}