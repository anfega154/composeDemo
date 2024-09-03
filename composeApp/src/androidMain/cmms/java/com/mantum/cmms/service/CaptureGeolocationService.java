package com.mantum.cmms.service;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.gson.GsonBuilder;
import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Coordenada;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.entity.parameter.UserParameter;
import com.mantum.component.component.Progress;
import com.mantum.component.service.Geolocation;
import com.mantum.component.service.handler.OnLocationListener;

import java.util.Calendar;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Sort;

public class CaptureGeolocationService {

    private final static int COUNT_DOWN_INTERVAL = 1000;

    private final Context context;

    private final Database database;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Progress progress;

    private Geolocation geolocation;

    private CountDownTimer countDownTimer;

    public CaptureGeolocationService(Context context) {
        this.context = context;
        this.database = new Database(context);
    }

    public void close() {
        database.close();
        compositeDisposable.clear();
    }

    public Observable<Location> obtener() {
        return obtener(true);
    }

    public Observable<Location> obtener(boolean cache) {
        return Observable.create(subscriber -> {
            if (!Geolocation.isEnabled(context)) {
                subscriber.onError(new Exception("El GPS no se encuentra activo"));
                return;
            }

            progress = new Progress(this.context);
            geolocation = new Geolocation(this.context, new OnLocationListener() {

                @Override
                public void onLocationChanged(@NonNull Geolocation geolocation, @NonNull Location location) {
                    geolocation.stop();
                    if (!((Activity) context).isFinishing()) {
                        if (progress != null) {
                            progress.hidden();
                        }

                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }
                    }

                    subscriber.onNext(location);
                    subscriber.onComplete();
                }

                @Override
                public void onError(Throwable throwable) {
                    subscriber.onError(throwable);
                }
            });

            Integer millisInFuture = UserParameter.NUMBER_SECONDS_TIMER_GPS;
            String milisecondsGPSString = UserParameter.getValue(this.context, UserParameter.SECONDS_TIMER_GPS);
            if (milisecondsGPSString != null) {
                millisInFuture = Integer.parseInt(milisecondsGPSString);
            }

            countDownTimer = new CountDownTimer(millisInFuture, COUNT_DOWN_INTERVAL) {

                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    if (progress != null && !progress.isShowing()) {
                        return;
                    }

                    geolocation.stop();
                    Location location = geolocation.getLastKnownLocation();
                    if (location == null) {
                        if (progress != null) {
                            progress.hidden();
                        }

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                        alertDialogBuilder.setCancelable(false);
                        alertDialogBuilder.setTitle(R.string.ubicacion_titulo);
                        alertDialogBuilder.setMessage(R.string.ubicacion_error_requiere);

                        alertDialogBuilder.setNegativeButton(R.string.ubicacion_reintentar, (dialogInterface, i) -> {
                            geolocation.start(0, 0);

                            start();
                            dialogInterface.cancel();
                            if (progress != null) {
                                progress.show();
                            }
                        });

                        alertDialogBuilder.setPositiveButton(R.string.cancelar, (dialogInterface, i) -> {
                            dialogInterface.cancel();

                            if (!((Activity) context).isFinishing()) {
                                if (progress != null) {
                                    progress.hidden();
                                }
                            }
                        });

                        if (!((Activity) context).isFinishing()) {
                            alertDialogBuilder.show();
                        }

                        return;
                    }

                    if (progress != null) {
                        progress.hidden();
                    }

                    subscriber.onNext(location);
                    subscriber.onComplete();
                }
            };

            long diffMinutes = 0;
            Location location = null;

            if (cache) {
                location = geolocation.getLastKnownLocation();
                if (location != null) {
                    long now = Calendar.getInstance().getTime().getTime();
                    diffMinutes = ((now - location.getTime()) / 1000) / 60;
                }

                if (location == null || diffMinutes >= 10) {
                    Cuenta cuenta = database.where(Cuenta.class)
                            .equalTo("active", true)
                            .findFirst();

                    if (cuenta == null) {
                        subscriber.onError(new Exception("La cuenta de usuario no esta autenticada"));
                    }

                    List<Transaccion> transaccions = database.where(Transaccion.class)
                            .equalTo("modulo", Transaccion.MODULO_GEOLOCALIZACION)
                            .equalTo("accion", Transaccion.ACCION_UBICACION)
                            .sort("creation", Sort.DESCENDING)
                            .limit(1)
                            .findAll();

                    if (transaccions != null && transaccions.size() > 0) {
                        Transaccion transaccion = transaccions.get(0);

                        if (transaccion != null) {
                            Coordenada coordenada = new GsonBuilder()
                                    .setDateFormat("yyyy-MM-dd HH:mm:ss")
                                    .create().fromJson(transaccion.getValue(), Coordenada.class);

                            location = new Location("temporal");
                            location.setAccuracy(coordenada.getAccuracy());
                            location.setLatitude(coordenada.getLatitude());
                            location.setLongitude(coordenada.getLongitude());
                            location.setAltitude(coordenada.getAltitude());
                            location.setTime(coordenada.getDatetime().getTime());

                            long now = Calendar.getInstance().getTime().getTime();
                            diffMinutes = ((now - location.getTime()) / 1000) / 60;
                        }
                    }
                }
            }

            if (location == null || diffMinutes >= 10) {
                geolocation.start(0, 0);

                progress.show(R.string.titulo_ubicacion_progreso, R.string.mensaje_ubicacion_progreso);
                countDownTimer.start();
            } else {
                subscriber.onNext(location);
                subscriber.onComplete();
            }
        });
    }
}