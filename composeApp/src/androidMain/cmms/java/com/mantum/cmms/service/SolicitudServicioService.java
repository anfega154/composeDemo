package com.mantum.cmms.service;

import android.content.Context;

import androidx.annotation.NonNull;

import android.util.Log;

import com.google.gson.Gson;
import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.EstadoInicial;
import com.mantum.cmms.entity.InformeTecnico;
import com.mantum.cmms.entity.RecursoAdicional;
import com.mantum.cmms.entity.Sitio;
import com.mantum.cmms.entity.Adjuntos;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Proceso;
import com.mantum.cmms.entity.SolicitudServicio;
import com.mantum.cmms.net.ClientManager;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.http.MicroServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import io.reactivex.Observable;
import io.realm.RealmList;
import io.realm.RealmResults;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class SolicitudServicioService extends MicroServices {

    private final Database database;

    private final String version;

    public SolicitudServicioService(@NonNull Context context, @NonNull Cuenta cuenta) {
        super(context, cuenta.getServidor().getUrl(), cuenta.getToken(context), ClientManager.prepare(
                new OkHttpClient.Builder(), context
        ));
        this.database = new Database(context);
        version = cuenta.getServidor().getVersion();
    }

    public Observable<SolicitudServicio.Request> fetch(final Integer pages) {
        return Observable.create(subscriber -> {
            if (!Mantum.isConnectedOrConnecting(context)) {
                subscriber.onNext(new SolicitudServicio.Request());
                subscriber.onComplete();
                return;
            }

            Request request = new Request.Builder().get()
                    .url(url + "/restapp/app/getmytodo?typetodo=SS&page=" + pages)
                    .addHeader("token", token)
                    .addHeader("accept", Version.build(version))
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
                        subscriber.onError(new Exception("Ocurrio un error a la hora de realizar la petición"));
                        return;
                    }

                    String json = body.string();
                    try {
                        if (response.isSuccessful()) {
                            Response content = new Gson().fromJson(json, Response.class);
                            SolicitudServicio.Request request = content.getBody(SolicitudServicio.Request.class);
                            Version.save(context, request.getVersion());

                            subscriber.onNext(request);
                            subscriber.onComplete();
                        } else {
                            subscriber.onError(new Exception(context.getString(R.string.error_obtener_solicitud_servicio)));
                        }
                    } catch (Exception e) {
                        subscriber.onError(new Exception(context.getString(R.string.error_pendientes)));
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

    public Observable<List<SolicitudServicio>> fetchById(Long id) {
        return Observable.create(subscriber -> {
            if (!Mantum.isConnectedOrConnecting(context)) {
                subscriber.onError(new Exception(
                        context.getString(R.string.offline)
                ));
                return;
            }

            if (id == null) {
                subscriber.onError(new Exception(
                        context.getString(R.string.sin_identificador)
                ));
                return;
            }

            String endpoint = url + "/restapp/app/refreshtodo?typetodo=SS&idtodo=" + id;
            if (Integer.parseInt(version) >= 10) {
                endpoint = url + "/restapp/app/refreshtodo?typetodo=SS&id=" + id;
            }

            Request request = new Request.Builder().get()
                    .addHeader("token", token)
                    .addHeader("accept", Version.build(version))
                    .addHeader("cache-control", "no-cache")
                    .addHeader("accept-language", "application/json")
                    .url(endpoint)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                    if (!call.isCanceled()) {
                        subscriber.onError(e);
                    }
                }

                @Override
                public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                    if (call.isCanceled()) {
                        return;
                    }

                    ResponseBody body = response.body();
                    if (body == null) {
                        subscriber.onError(new Exception(context.getString(R.string.error_peticion_solicitud_servicio)));
                        return;
                    }

                    String json = body.string();
                    try {
                        if (response.isSuccessful()) {
                            Response content = new Gson().fromJson(json, Response.class);
                            content.setVersion(response.header("Max-Version"));

                            SolicitudServicio.Request data = content.getBody(SolicitudServicio.Request.class);
                            subscriber.onNext(!data.getPendientes().isEmpty() ? data.getPendientes() : Collections.emptyList());
                            subscriber.onComplete();
                        } else {
                            subscriber.onError(new Exception(context.getString(R.string.error_obtener_solicitud_servicio)));
                        }
                    } catch (Exception e) {
                        subscriber.onError(new Exception(context.getString(R.string.error_obtener_respuesta_solicitud_servicio)));
                    }

                    response.close();
                }
            });
        });
    }

    public Observable<List<SolicitudServicio>> save(@NonNull SolicitudServicio value) {
        return save(Collections.singletonList(value));
    }

    public Observable<List<SolicitudServicio>> save(@NonNull List<SolicitudServicio> values) {
        return Observable.create(subscriber -> database.executeTransactionAsync(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            for (SolicitudServicio value : values) {
                SolicitudServicio temporal = self.where(SolicitudServicio.class)
                        .equalTo("id", value.getId())
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();

                if (temporal == null) {
                    value.setUUID(UUID.randomUUID().toString());
                    value.setCuenta(cuenta);

                    Sitio sitio = value.getSitio();
                    if (sitio != null) {
                        sitio.setUuid(UUID.randomUUID().toString());
                        sitio.setCuenta(cuenta);
                        value.setSitio(sitio);
                    }

                    InformeTecnico informeTecnico = value.getInformeTecnico();
                    if (informeTecnico != null) {
                        informeTecnico.setUuid(UUID.randomUUID().toString());
                        informeTecnico.setIdss(value.getId());
                        informeTecnico.setCodigo(value.getCodigo());
                        informeTecnico.setCuenta(cuenta);
                        value.setInformeTecnico(informeTecnico);
                    }

                    EstadoInicial estadoInicial = value.getEstadoInicial();
                    if (estadoInicial != null) {
                        estadoInicial.setUuid(UUID.randomUUID().toString());
                        estadoInicial.setIdss(value.getId());
                        estadoInicial.setCodigo(value.getCodigo());
                        estadoInicial.setCuenta(cuenta);
                        value.setEstadoInicial(estadoInicial);
                    }

                    for (RecursoAdicional recursoAdicional : value.getRecursosadicionales()) {
                        recursoAdicional.setUuid(UUID.randomUUID().toString());
                        recursoAdicional.setCuenta(cuenta);
                    }

                    for (Proceso proceso : value.getProcesos()) {
                        proceso.setUUID(UUID.randomUUID().toString());
                    }

                    self.insert(value);
                } else {
                    value.setUUID(temporal.getUUID());
                    temporal.setCodigo(value.getCodigo());
                    temporal.setFecha(value.getFecha());
                    temporal.setTipo(value.getTipo());
                    temporal.setIdentidad(value.getIdentidad());
                    temporal.setTipoentidad(value.getTipoentidad());
                    temporal.setEntidad(value.getEntidad());
                    temporal.setArea(value.getArea());
                    temporal.setFechaesperada(value.getFechaesperada());
                    temporal.setDescripcion(value.getDescripcion());
                    temporal.setFechavencimiento(value.getFechavencimiento());
                    temporal.setPrioridad(value.getPrioridad());
                    temporal.setEstado(value.getEstado());
                    temporal.setSolicitante(value.getSolicitante());

                    Sitio sitio = value.getSitio();
                    if (sitio != null) {
                        // La solicitud de servicio que esta guardada en base de datos
                        // no tiene sitio
                        Sitio temporalSitio = temporal.getSitio();
                        if (temporalSitio == null) {
                            sitio.setUuid(UUID.randomUUID().toString());
                            sitio.setCuenta(cuenta);
                            temporal.setSitio(self.copyToRealm(sitio));
                        } else {
                            // Actualiza el sitio de la solicitud de servicio
                            temporal.getSitio().setTelefono(sitio.getTelefono());
                            temporal.getSitio().setPlanta(sitio.getPlanta());
                            temporal.getSitio().setDireccion(sitio.getDireccion());
                            temporal.getSitio().setCliente(sitio.getCliente());
                        }
                    }

                    InformeTecnico informeTecnico = value.getInformeTecnico();
                    if (informeTecnico != null) {
                        // La solicitud de servicio que esta guardada en base de datos
                        // no tiene informe tecnico
                        InformeTecnico temporalInformeTecnico = temporal.getInformeTecnico();
                        if (temporalInformeTecnico == null) {
                            informeTecnico.setIdss(value.getId());
                            informeTecnico.setCodigo(value.getCodigo());
                            informeTecnico.setUuid(UUID.randomUUID().toString());
                            informeTecnico.setCuenta(cuenta);
                            temporal.setInformeTecnico(self.copyToRealm(informeTecnico));
                        } else {
                            // Actualiza el informe tecnico de la solicitud de servicio
                            temporal.getInformeTecnico().setIdss(value.getId());
                            temporal.getInformeTecnico().setCodigo(value.getCodigo());
                            temporal.getInformeTecnico().setRecomendaciones(informeTecnico.getRecomendaciones());
                            temporal.getInformeTecnico().setActividades(informeTecnico.getActividades());
                        }
                    }

                    EstadoInicial estadoInicial = value.getEstadoInicial();
                    if (estadoInicial != null) {
                        // La solicitud de servicio que esta guardada en base de datos
                        // no tiene estado inicial
                        EstadoInicial temporalEstadoInicial = temporal.getEstadoInicial();
                        if (temporalEstadoInicial == null) {
                            estadoInicial.setIdss(value.getId());
                            estadoInicial.setCodigo(value.getCodigo());
                            estadoInicial.setUuid(UUID.randomUUID().toString());
                            estadoInicial.setCuenta(cuenta);
                            temporal.setEstadoInicial(self.copyToRealm(estadoInicial));
                        } else {
                            // Actualiza el estado inicial de la solicitud de servicio
                            temporal.getEstadoInicial().setIdss(value.getId());
                            temporal.getEstadoInicial().setCodigo(value.getCodigo());
                            temporal.getEstadoInicial().setMarca(estadoInicial.getMarca());
                            temporal.getEstadoInicial().setTipofalla(estadoInicial.getTipofalla());
                            temporal.getEstadoInicial().setDenominacion(estadoInicial.getDenominacion());
                            temporal.getEstadoInicial().setNumeroproducto(estadoInicial.getNumeroproducto());
                            temporal.getEstadoInicial().setNumeroserial(estadoInicial.getNumeroserial());
                            temporal.getEstadoInicial().setCaracteristicas(estadoInicial.getCaracteristicas());
                            temporal.getEstadoInicial().setEstado(estadoInicial.getEstado());
                        }
                    }

                    // Guarda los recursos adicionales de la solicitud de servicio
                    if (temporal.getRecursosadicionales() != null
                            && !temporal.getRecursosadicionales().isEmpty()) {
                        temporal.getRecursosadicionales().deleteAllFromRealm();
                    }

                    RealmList<RecursoAdicional> recursoAdicional = new RealmList<>();
                    for (RecursoAdicional recurso : value.getRecursosadicionales()) {
                        recurso.setUuid(UUID.randomUUID().toString());
                        recurso.setCuenta(cuenta);
                        recursoAdicional.add(self.copyToRealm(recurso));
                    }
                    temporal.setRecursosadicionales(recursoAdicional);

                    // Guarda los procesos de la solicitud de servicio
                    if (temporal.getProcesos() != null && !temporal.getProcesos().isEmpty()) {
                        temporal.getProcesos().deleteAllFromRealm();
                    }

                    RealmList<Proceso> procesos = new RealmList<>();
                    for (Proceso proceso : value.getProcesos()) {
                        proceso.setUUID(UUID.randomUUID().toString());
                        procesos.add(self.copyToRealm(proceso));
                    }
                    temporal.setProcesos(procesos);

                    // Guarda los adjuntos de la solicitud de servicio
                    if (temporal.getAdjuntos() != null && !temporal.getAdjuntos().isEmpty()) {
                        temporal.getAdjuntos().deleteAllFromRealm();
                    }

                    RealmList<Adjuntos> adjuntos = new RealmList<>();
                    for (Adjuntos adjunto : value.getAdjuntos()) {
                        adjuntos.add(self.copyToRealm(adjunto));
                    }
                    temporal.setAdjuntos(adjuntos);

                    // Guarda las imagenes de la solicitud de servicio
                    if (temporal.getImagenes() != null && !temporal.getImagenes().isEmpty()) {
                        temporal.getImagenes().deleteAllFromRealm();
                    }

                    RealmList<Adjuntos> imagenes = new RealmList<>();
                    for (Adjuntos imagen : value.getImagenes()) {
                        imagenes.add(self.copyToRealm(imagen));
                    }
                    temporal.setImagenes(imagenes);
                }
            }

            subscriber.onNext(values);

        }, subscriber::onComplete, subscriber::onError));
    }

    public Observable<List<Long>> remove(final Long[] id) {
        return Observable.create(subscriber -> {
            Database database = new Database(context);
            database.executeTransaction(realm -> {
                Cuenta cuenta = realm.where(Cuenta.class)
                        .equalTo("active", true)
                        .findFirst();

                if (cuenta == null) {
                    subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                    return;
                }

                try {
                    RealmResults<SolicitudServicio> pendientes = realm.where(SolicitudServicio.class)
                            .equalTo("cuenta.UUID", cuenta.getUUID())
                            .not().in("id", id)
                            .findAll();

                    List<Long> eliminados = new ArrayList<>();
                    for (SolicitudServicio pendiente : pendientes) {
                        eliminados.add(pendiente.getId());
                    }
                    pendientes.deleteAllFromRealm();

                    subscriber.onNext(eliminados);
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            });
            database.close();
        });
    }

    public Observable<List<SolicitudServicio>> remove(final Long id) {
        return Observable.create(subscriber -> {
            if (id == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_eliminar_solicitud_servicio)));
                return;
            }

            database.executeTransactionAsync(self -> {
                Cuenta cuenta = self.where(Cuenta.class)
                        .equalTo("active", true)
                        .findFirst();

                if (cuenta == null) {
                    subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                    return;
                }

                self.where(SolicitudServicio.class)
                        .equalTo("id", id)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findAll()
                        .deleteAllFromRealm();

                subscriber.onNext(Collections.emptyList());

            }, subscriber::onComplete, subscriber::onError);
        });
    }

    public void close() {
        if (database != null) {
            database.close();
        }
    }

    public static class Builder extends Service {

        public Builder(@NonNull Context context) {
            super(context);
        }
    }
}