package com.mantum.cmms.service;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AlertDialog;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.mantum.R;
import com.mantum.cmms.broadcast.ActividadTecnicoBroadcast;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.BitacoraOrdenTrabajo;
import com.mantum.cmms.domain.Novedad;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.cmms.entity.Recorrido;
import com.mantum.cmms.entity.RecorridoHistorico;
import com.mantum.cmms.entity.Servidor;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.component.Mantum;
import com.mantum.component.OnResult;
import com.mantum.component.service.Geolocation;
import com.mantum.component.service.Notification;
import com.mantum.component.service.Photo;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.internal.functions.Functions;

public class ATNotificationService {

    public final static int ID_NOTIFICATION = 4446886;

    private final static String TAG_ESTADO = "Estado";
    private final static String TAG = ATNotificationService.class.getSimpleName();

    public final static String PERSONAL = "Personal";
    public final static String OT = "OT-SS";

    public final static Estado DISPONIBLE = new Estado(1, "DISPONIBLE", R.string.disponible);
    public final static Estado EN_MOVIMIENTO = new Estado(2, "EN_MOVIMIENTO", R.string.en_camino);
    public final static Estado EN_SITIO = new Estado(3, "EN_SITIO", R.string.en_sitio);
    public final static Estado EN_EJECUCION = new Estado(4, "EN_EJECUCION", R.string.iniciar);
    public final static Estado NO_DISPONIBLE = new Estado(5, "NO_DISPONIBLE", R.string.no_disponible);
    public final static Estado EN_SITIO_AUTOMATICO = new Estado(6, "EN_SITIO_AUTOMATICO", R.string.en_sitio_automatico);
    public final static Estado COMPLETAR = new Estado(7, "COMPLETAR", R.string.completar);
    public final static Estado FIN_EJECUCION = new Estado(8, "FIN_EJECUCION", R.string.fin_ejecucion);

    public final static String ALTA = "Alta";
    public final static String MEDIA = "Media";
    public final static String BAJA = "Baja";

    private final Context context;

    private final Database database;

    private final RecorridoService recorridoService;

    private final CaptureGeolocationService captureGeolocationService;

    private final RecorridoHistoricoService recorridoHistoricoService;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private String observacion = null;

    public void setDirectTransaction(boolean directTransaction) {
        this.directTransaction = directTransaction;
    }

    private boolean directTransaction = false;

    public ATNotificationService(@NonNull Context context) {
        this.context = context;
        this.database = new Database(context);
        this.captureGeolocationService = new CaptureGeolocationService(context);
        this.recorridoService = new RecorridoService(context, RecorridoService.Tipo.OT);
        this.recorridoHistoricoService = new RecorridoHistoricoService(context);
    }

    public void close() {
        database.close();
        recorridoService.close();
        captureGeolocationService.close();
        compositeDisposable.clear();
    }

    public void enSitioAutomatico(long identidad) {
        compositeDisposable.add(captureGeolocationService.obtener()
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(location -> save(identidad, OT, location, EN_SITIO_AUTOMATICO, null, null, null, null, false))
                .subscribe(Functions.emptyConsumer(), Mantum::ignoreError, () -> {
                }));
    }

    public void finEjecucion(long identidad) {
        compositeDisposable.add(captureGeolocationService.obtener()
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(location -> save(identidad, OT, location, FIN_EJECUCION, null, null, null, null, false))
                .subscribe(Functions.emptyConsumer(), Mantum::ignoreError, () -> {
                }));
    }

    public void disponible() {
        compositeDisposable.add(captureGeolocationService.obtener()
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(location -> save(PERSONAL, location))
                .subscribe(Functions.emptyConsumer(), Mantum::ignoreError, () -> {
                })
        );
    }

    public void disponible(@NonNull OnResult<String> callback, boolean isLogin) {
        if (isLogin && (!Geolocation.isEnabled(context) || !Geolocation.checkPermission(context))) {
            compositeDisposable.add(save(PERSONAL, null)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(Functions.emptyConsumer(), throwable -> error(throwable, callback), () -> success(callback)));
        } else {
            compositeDisposable.add(captureGeolocationService.obtener()
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap(location -> save(PERSONAL, location))
                    .subscribe(Functions.emptyConsumer(), throwable -> error(throwable, callback), () -> success(callback)));
        }
    }

    public void noDisponible(@NonNull OnResult<String> callback) {
        View form = View.inflate(context, com.mantum.component.R.layout.observation_form, null);
        TextInputEditText observacion = form.findViewById(com.mantum.component.R.id.observation);

        AlertDialog builder = new AlertDialog.Builder(context)
                .setTitle(R.string.title_observacion_tecnico)
                .setPositiveButton(R.string.aceptar, null)
                .setView(form)
                .create();

        builder.setOnShowListener(dialog -> {
            Button button = builder.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                if (observacion.getText() == null || observacion.getText().length() == 0) {
                    observacion.setError(context.getString(R.string.motivo_requerido));
                    return;
                }
                dialog.dismiss();

                recorridoService.eliminar();
                compositeDisposable.add(captureGeolocationService.obtener()
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap(location -> save(PERSONAL, location, NO_DISPONIBLE, observacion.getText().toString()))
                        .subscribe(self -> {
                            enMovimiento(null);
                            enviar(-1, NO_DISPONIBLE);
                        }, throwable -> error(throwable, callback), () -> success(callback)));
            });
        });

        builder.show();
    }

    public void homeNoDisponible(@NonNull OnResult<String> callback) {
        if (!Geolocation.isEnabled(context) || !Geolocation.checkPermission(context)) {
            compositeDisposable.add(save(PERSONAL, null, NO_DISPONIBLE, context.getString(R.string.cierre_sesion))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(self -> {
                        enMovimiento(null);
                        enviar(-1, NO_DISPONIBLE);
                    }, throwable -> error(throwable, callback), () -> success(callback)));
        } else {
            compositeDisposable.add(captureGeolocationService.obtener()
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap(location -> save(PERSONAL, location, NO_DISPONIBLE, context.getString(R.string.cierre_sesion)))
                    .subscribe(self -> {
                        enMovimiento(null);
                        enviar(-1, NO_DISPONIBLE);
                    }, throwable -> error(throwable, callback), () -> success(callback)));
        }
    }

    public void cancelar(@NonNull OnResult<String> callback, Estado estado, boolean logout) {
        View form = View.inflate(context, com.mantum.component.R.layout.observation_form, null);
        TextInputEditText observacion = form.findViewById(com.mantum.component.R.id.observation);

        AlertDialog builder = new AlertDialog.Builder(context)
                .setTitle(R.string.title_observacion_tecnico_cancelar)
                .setNegativeButton(R.string.no, ((dialog, which) -> dialog.dismiss()))
                .setPositiveButton(R.string.si, null)
                .setView(form)
                .create();

        builder.setOnShowListener(dialog -> {
            Button button = builder.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                if (observacion.getText() == null || observacion.getText().length() == 0) {
                    observacion.setError(context.getString(R.string.motivo_requerido));
                    return;
                }
                dialog.dismiss();

                this.observacion = observacion.getText().toString();
                borrarRecorridoSimple(callback, estado, logout);
            });
        });

        builder.show();
    }

    public void borrarRecorridoSimple(@NonNull OnResult<String> callback, Estado estado, boolean isLogout) {
        recorridoService.eliminar();
        if (isLogout && (!Geolocation.isEnabled(context) || !Geolocation.checkPermission(context))) {
            compositeDisposable.add(save(PERSONAL, null, estado, this.observacion)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(self -> enMovimiento(null), throwable -> error(throwable, callback), () -> success(callback)));
        } else {
            compositeDisposable.add(captureGeolocationService.obtener()
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap(location -> save(PERSONAL, location, estado, this.observacion))
                    .subscribe(self -> enMovimiento(null), throwable -> error(throwable, callback), () -> success(callback)));
        }
    }

    public void cambiarEstado(
            @NonNull Long identidad,
            @Nullable String codigo,
            @NonNull String tipoEntidad,
            @NonNull Estado estado,
            @Nullable String observacion,
            @Nullable Long idcategoria,
            @Nullable String categoria,
            @Nullable List<Photo> images,
            @NonNull OnResult<String> callback,
            boolean finalizanovedad
    ) {
        compositeDisposable.add(captureGeolocationService.obtener()
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(location -> save(identidad, tipoEntidad, location, estado, observacion, idcategoria, categoria, images, finalizanovedad))
                .subscribe(self -> {
                    enMovimiento(identidad);
                    save(identidad, codigo, estado);
                    enviar(identidad, estado);
                }, throwable -> error(throwable, callback), () -> success(callback)));
    }

    private void success(OnResult<String> callback) {
        if (callback != null) {
            callback.call(context.getString(R.string.estado_personal_info), false);
        }
    }

    private void error(Throwable throwable, OnResult<String> callback) {
        if (callback != null) {
            callback.call(throwable.getMessage(), true);
        }
    }

    private Observable<List<Transaccion>> save(
            @NonNull String tipoEntidad,
            @Nullable Location location
    ) {
        return save(null, tipoEntidad, location, ATNotificationService.DISPONIBLE, null, null, null, null, false);
    }

    private Observable<List<Transaccion>> save(@NonNull String tipoEntidad, @Nullable Location location, @NonNull Estado estado, @Nullable String observacion) {
        return save(null, tipoEntidad, location, estado, observacion, null, null, null, false);
    }

    private Observable<List<Transaccion>> save(
            @Nullable Long identidad,
            @NonNull String tipoEntidad,
            @Nullable Location location,
            @NonNull Estado estado,
            @Nullable String observacion,
            @Nullable Long idcategoria,
            @Nullable String categoria,
            @Nullable List<Photo> images,
            boolean finalizanovedad
    ) {
        return Observable.create(subscriber -> {
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception("La cuenta de usuario no esta autenticada"));
                return;
            }

            if (estado.isDiponible() || estado.isNoDisponible()) {
                database.executeTransaction(realm -> {
                    cuenta.setDisponible(estado.isDiponible());
                    realm.insertOrUpdate(cuenta);
                });
            }

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat fecha = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date time = calendar.getTime();

            // Obtiene el estado actual
            RecorridoHistorico historico = recorridoHistoricoService.getUltimoEstado();

            if (historico != null) {
                Estado estadoactual = Estado.getEstado(context, historico.getEstado());

                if (estadoactual != null) {
                    Novedad novedad = new Novedad();
                    novedad.setIdpersonal(cuenta.getId());
                    novedad.setIdentidad(historico.getIdentidad());
                    novedad.setTipoentidad(historico.getTipoentidad());
                    novedad.setIdestado(estadoactual.getId());
                    novedad.setEstado(estadoactual.getMostrar(context));
                    novedad.setObservacion(historico.getComentario());
                    novedad.setIdcategoria(historico.getIdcategoria());
                    novedad.setCategoria(historico.getCategoria());
                    novedad.setFinalizanovedad(historico.isFinalizanovedad());
                    novedad.setFechaInicial(fecha.format(historico.getFecha()));
                    novedad.setFechaFinal(fecha.format(time));

                    if (location != null) {
                        novedad.setLatitude(location.getLatitude());
                        novedad.setLongitude(location.getLongitude());
                        novedad.setAccuracy(location.getAccuracy());
                        novedad.setAltitude(location.getAltitude());
                    }

                    Log.d(TAG_ESTADO, "Before - Estado: " + estadoactual.getMostrar(context));
                    Log.d(TAG_ESTADO, "Before - Fecha inicial: " + novedad.getFechaInicial());
                    Log.d(TAG_ESTADO, "Before - Fecha final: " + novedad.getFechaFinal());
                    Log.d(TAG_ESTADO, "Before - Novedad: " + novedad.getCategoria() + " - Finaliza: " + novedad.isFinalizanovedad());

                    Servidor servidor = cuenta.getServidor();

                    Transaccion transaccion = new Transaccion();
                    transaccion.setCuenta(cuenta);
                    transaccion.setUrl(servidor.getUrl() + "/restapp/app/actualizarestadopersonal");
                    transaccion.setVersion(servidor.getVersion());
                    transaccion.setValue(novedad.toJson());
                    transaccion.setModulo(Transaccion.MODULO_ESTADO_USUARIO);
                    transaccion.setAccion(Transaccion.ACCION_ESTADO_USUARIO);
                    transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);

                    TransaccionService transaccionService = new TransaccionService(context);
                    transaccionService.setSesionTransaction(directTransaction);

                    compositeDisposable.add(transaccionService.save(transaccion)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(subscriber::onNext, subscriber::onError, subscriber::onComplete));
                }
            }

            recorridoHistoricoService.save(
                    identidad, tipoEntidad, observacion, estado,
                    idcategoria, categoria, finalizanovedad
            );

            Novedad novedad = new Novedad();
            novedad.setIdpersonal(cuenta.getId());
            novedad.setTipoentidad(tipoEntidad);
            novedad.setIdentidad(identidad);
            novedad.setIdestado(estado.getId());
            novedad.setEstado(estado.getMostrar(context));
            novedad.setObservacion(observacion);
            novedad.setIdcategoria(idcategoria);
            novedad.setImage(images);
            novedad.setFinalizanovedad(finalizanovedad);
            novedad.setCategoria(categoria);
            novedad.setFechaInicial(fecha.format(time));
            novedad.setFechaFinal(null);

            if (location != null) {
                novedad.setLatitude(location.getLatitude());
                novedad.setLongitude(location.getLongitude());
                novedad.setAccuracy(location.getAccuracy());
                novedad.setAltitude(location.getAltitude());
            }

            Log.d(TAG_ESTADO, "After - Estado: " + estado.getMostrar(context));
            Log.d(TAG_ESTADO, "After - Fecha inicial: " + novedad.getFechaInicial());
            Log.d(TAG_ESTADO, "After - Fecha final: " + novedad.getFechaFinal());
            Log.d(TAG_ESTADO, "After - Novedad: " + novedad.getCategoria() + " - Finaliza: " + novedad.isFinalizanovedad());

            Transaccion transaccion = new Transaccion();
            transaccion.setCuenta(cuenta);
            transaccion.setUrl(cuenta.getServidor().getUrl() + "/restapp/app/actualizarestadopersonal");
            transaccion.setVersion(cuenta.getServidor().getVersion());
            transaccion.setValue(novedad.toJson());
            transaccion.setModulo(Transaccion.MODULO_ESTADO_USUARIO);
            transaccion.setAccion(Transaccion.ACCION_ESTADO_USUARIO);
            transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);

            TransaccionService transaccionService = new TransaccionService(context);
            transaccionService.setSesionTransaction(directTransaction);
            compositeDisposable.add(transaccionService.save(transaccion)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(subscriber::onNext, subscriber::onError, subscriber::onComplete));
        });
    }

    public void saveRecalculoANS(@Nullable Long identidad, @NonNull String tipoEntidad, @NonNull Estado estado, @Nullable String observacion, Long idcategoria, String tipoSS, String prioridad, @NonNull OnResult<String> onInvoke) {
        compositeDisposable.add(captureGeolocationService.obtener()
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(location -> recalcularANS(identidad, tipoEntidad, estado, observacion, idcategoria, tipoSS, prioridad))
                .subscribe(self -> {
                }, throwable -> onInvoke.call(throwable.getMessage(), true), () -> onInvoke.call(context.getString(R.string.recalculo_ans_info), false)));
    }

    private Observable<List<Transaccion>> recalcularANS(@Nullable Long identidad, @NonNull String tipoEntidad, @NonNull Estado estado, @Nullable String observacion, Long idcategoria, String tipoSS, String prioridad) {
        return Observable.create(subscriber -> {
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception("La cuenta de usuario no esta autenticada"));
                return;
            }

            if (estado.isDiponible()) {
                database.beginTransaction();
                cuenta.setDisponible(true);
                database.commitTransaction();
            } else if (estado.isNoDisponible()) {
                database.beginTransaction();
                cuenta.setDisponible(false);
                database.commitTransaction();
            }

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat fecha = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            Novedad novedad = new Novedad();
            novedad.setIdpersonal(cuenta.getId());
            novedad.setTipoentidad(tipoEntidad);
            novedad.setIdentidad(identidad);
            novedad.setFechaInicial(fecha.format(calendar.getTime()));
            novedad.setIdestado(estado.getId());
            novedad.setObservacion(observacion);
            novedad.setIdcategoria(idcategoria);
            novedad.setTipoSS(tipoSS);
            novedad.setPrioridadSS(prioridad);

            Transaccion transaccion = new Transaccion();
            transaccion.setCuenta(cuenta);
            transaccion.setUrl(cuenta.getServidor().getUrl() + "/restapp/app/recalcularans");
            transaccion.setVersion(cuenta.getServidor().getVersion());
            transaccion.setValue(novedad.toJson());
            transaccion.setModulo(Transaccion.MODULO_ORDEN_TRABAJO);
            transaccion.setAccion(Transaccion.ACCION_ANS_OT);
            transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);

            TransaccionService transaccionService = new TransaccionService(context);
            compositeDisposable.add(transaccionService.save(transaccion)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(subscriber::onNext, subscriber::onError, subscriber::onComplete));
        });
    }

    public static class Estado implements Serializable {

        private int id;

        private String nombre;

        private int mostrar;

        public Estado() {
        }

        public Estado(int id, String nombre, @StringRes int mostrar) {
            this.id = id;
            this.nombre = nombre;
            this.mostrar = mostrar;
        }

        public int getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }

        public String getMostrar(@NonNull Context context) {
            return context.getString(mostrar);
        }

        public boolean is(@NonNull Estado estado) {
            return id == estado.getId();
        }

        public boolean is(@NonNull String estado) {
            return nombre.equals(estado);
        }

        public boolean isDiponible() {
            return is(DISPONIBLE);
        }

        public boolean isNoDisponible() {
            return is(NO_DISPONIBLE);
        }

        @Nullable
        public static Estado getEstado(Context context, String nombre) {
            if (nombre.equals(DISPONIBLE.getNombre()) || nombre.equals(DISPONIBLE.getMostrar(context))) {
                return DISPONIBLE;
            } else if (nombre.equals(EN_MOVIMIENTO.getNombre()) || nombre.equals(EN_MOVIMIENTO.getMostrar(context))) {
                return EN_MOVIMIENTO;
            } else if (nombre.equals(EN_SITIO.getNombre()) || nombre.equals(EN_SITIO.getMostrar(context))) {
                return EN_SITIO;
            } else if (nombre.equals(EN_EJECUCION.getNombre()) || nombre.equals(EN_EJECUCION.getMostrar(context))) {
                return EN_EJECUCION;
            } else if (nombre.equals(NO_DISPONIBLE.getNombre()) || nombre.equals(NO_DISPONIBLE.getMostrar(context))) {
                return NO_DISPONIBLE;
            } else if (nombre.equals(EN_SITIO_AUTOMATICO.getNombre()) || nombre.equals(EN_SITIO_AUTOMATICO.getMostrar(context))) {
                return EN_SITIO_AUTOMATICO;
            } else if (nombre.equals(COMPLETAR.getNombre()) || nombre.equals(COMPLETAR.getMostrar(context))) {
                return COMPLETAR;
            } else if (nombre.equals(FIN_EJECUCION.getNombre()) || nombre.equals(FIN_EJECUCION.getMostrar(context))) {
                return FIN_EJECUCION;
            } else {
                return null;
            }
        }

        @Nullable
        public static Estado getEstado(long estado) {
            if (DISPONIBLE.getId() == estado) {
                return DISPONIBLE;
            }

            if (EN_MOVIMIENTO.getId() == estado) {
                return EN_MOVIMIENTO;
            }

            if (EN_SITIO.getId() == estado) {
                return EN_SITIO;
            }

            if (EN_EJECUCION.getId() == estado) {
                return EN_EJECUCION;
            }

            if (NO_DISPONIBLE.getId() == estado) {
                return NO_DISPONIBLE;
            }

            if (COMPLETAR.getId() == estado) {
                return COMPLETAR;
            }

            return null;
        }
    }

    private void save(long identidad, String codigo, @NonNull Estado estado) {
        if (recorridoService.pendientes(identidad)) {
            Recorrido recorrido = recorridoService.obtenerPendiente(identidad);
            String codigoExistente = "";
            if (recorrido != null) {
                codigoExistente = recorrido.getCodigo();
            }

            AlertDialog builder = new AlertDialog.Builder(context)
                    .setMessage(String.format(context.getString(R.string.orden_trabajo_pendiente_tecnico), codigoExistente))
                    .setPositiveButton(R.string.aceptar, (dialog, which) -> dialog.dismiss())
                    .create();

            builder.show();
            return;
        }

        if (!recorridoService.existe(identidad)) {
            Recorrido.Data data = new Recorrido.Data();
            data.setCodigo(codigo);
            data.setEstado(estado.getNombre());

            compositeDisposable.add(recorridoService.iniciar(identidad, data)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(Functions.emptyConsumer(), com.mantum.component.Mantum::ignoreError, Functions.EMPTY_ACTION));
        }

        Recorrido.Data data = new Recorrido.Data();
        data.setEstado(estado.getNombre());
        if (estado.is(ATNotificationService.EN_EJECUCION)) {
            Calendar calendar = Calendar.getInstance();
            BitacoraOrdenTrabajo bitacoraOrdenTrabajo = new BitacoraOrdenTrabajo();

            SimpleDateFormat fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            bitacoraOrdenTrabajo.setDate(fecha.format(calendar.getTime()));

            SimpleDateFormat hora = new SimpleDateFormat("HH:mm", Locale.getDefault());
            bitacoraOrdenTrabajo.setTimestart(hora.format(calendar.getTime()));

            data.setValue(bitacoraOrdenTrabajo.toJson());
            recorridoService.actualizar(identidad, data);
        }

        recorridoService.actualizar(identidad, data);
    }

    public void enviar(long identidad, @NonNull Estado estado) {
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return;
        }

        OrdenTrabajo ordenTrabajo = database.where(OrdenTrabajo.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("id", identidad)
                .findFirst();

        if (ordenTrabajo == null) {
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putLong(Mantum.KEY_ID, ordenTrabajo.getId());
        bundle.putString(Mantum.KEY_UUID, ordenTrabajo.getUUID());

        Intent intent = new Intent(context, ActividadTecnicoBroadcast.class);
        intent.setAction(estado.getNombre());
        intent.putExtras(bundle);

        int title = R.string.en_sitio;
        int message = R.string.en_camino;
        int icon = R.drawable.ic_location_on_white_24dp;

        if (estado.is(EN_SITIO)) {
            title = R.string.iniciar;
            message = R.string.en_sitio;
            icon = R.drawable.ic_play_arrow_white_24dp;
        }

        if (estado.is(EN_EJECUCION)) {
            title = R.string.completar;
            message = R.string.en_ejecucion;
            icon = R.drawable.ic_alarm_white_24dp;
        }

        Notification.Action action = new Notification.Action(icon, context.getString(title), intent);
        Notification.Model model = new Notification.Model(
                ordenTrabajo.getCodigo(), context.getString(message));
        model.setAutoCancel(false);
        model.setAction(action);

        Notification notification = new Notification(context, Notification.CHANNEL_ID, ID_NOTIFICATION);
        notification.show(model);
    }

    private void enMovimiento(@Nullable Long id) {
        database.executeTransaction(self -> {
            List<OrdenTrabajo> results = self.where(OrdenTrabajo.class)
                    .equalTo("movimiento", true)
                    .findAll();

            for (OrdenTrabajo result : results) {
                result.setMovimiento(false);
            }

            if (id != null) {
                results = self.where(OrdenTrabajo.class)
                        .equalTo("id", id)
                        .findAll();

                if (results.isEmpty()) {
                    return;
                }

                for (OrdenTrabajo result : results) {
                    result.setMovimiento(true);
                }
            }
        });
    }

    public Estado getEstadoActual() {
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return null;
        }

        Recorrido recorrido = recorridoService.obtenerActual();
        if (recorrido != null && recorrido.getEstado() != null) {
            return Estado.getEstado(context, recorrido.getEstado());
        }
        return cuenta.isDisponible() ? DISPONIBLE : NO_DISPONIBLE;
    }

    public String getTipoEntidad(Estado estado) {
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return null;
        }

        if (estado.isDiponible() || estado.isNoDisponible()) {
            return PERSONAL;
        } else {
            return OT;
        }
    }
}