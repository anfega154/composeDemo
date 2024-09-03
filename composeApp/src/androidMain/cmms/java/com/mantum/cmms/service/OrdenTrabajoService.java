package com.mantum.cmms.service;

import android.content.Context;

import androidx.annotation.NonNull;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.ANS;
import com.mantum.cmms.entity.Actividad;
import com.mantum.cmms.entity.Adjuntos;
import com.mantum.cmms.entity.Asignada;
import com.mantum.cmms.entity.Consumible;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Ejecutores;
import com.mantum.cmms.entity.ElementoFalla;
import com.mantum.cmms.entity.Entidad;
import com.mantum.cmms.entity.Falla;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.cmms.entity.RecorridoHistorico;
import com.mantum.cmms.entity.Recurso;
import com.mantum.cmms.entity.RepuestoManual;
import com.mantum.cmms.entity.RutaTrabajo;
import com.mantum.cmms.entity.Tarea;
import com.mantum.cmms.entity.Variable;
import com.mantum.cmms.entity.Yarda;
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
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class OrdenTrabajoService extends MicroServices {

    private final Database database;

    private final String version;

    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    public OrdenTrabajoService(@NonNull Context context, @NonNull Cuenta cuenta) {
        super(context, cuenta.getServidor().getUrl(), cuenta.getToken(context), ClientManager.prepare(
                new OkHttpClient.Builder(), context
        ));
        database = new Database(context);
        version = cuenta.getServidor().getVersion();
    }

    public Observable<OrdenTrabajo.Request> fetch(final Integer pages) {
        return Observable.create(subscriber -> {
            if (!Mantum.isConnectedOrConnecting(context)) {
                subscriber.onNext(new OrdenTrabajo.Request());
                subscriber.onComplete();
                return;
            }

            String endpoint = url + "/restapp/app/getmytodo?typetodo=OT&page=" + pages;
            Request request = new Request.Builder().get()
                    .url(endpoint)
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
                            Version.save(context, response.header("Max-Version"));
                            Response content = gson.fromJson(json, Response.class);

                            OrdenTrabajo.Request request = content.getBody(OrdenTrabajo.Request.class, gson);
                            subscriber.onNext(request);
                            subscriber.onComplete();
                        } else {
                            if (!subscriber.isDisposed()) {
                                subscriber.onError(new Exception(context.getString(R.string.error_get_ordenes_trabajo)));
                            }

                        }
                    } catch (Exception e) {
                        if (!subscriber.isDisposed()) {
                            subscriber.onError(new Exception(context.getString(R.string.error_pendientes_ot)));
                        }
                    }
                    response.close();
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!call.isCanceled() && !subscriber.isDisposed()) {
                        subscriber.onError(new Exception(context.getString(R.string.error_pendientes_ot)));
                    }
                }
            });
        });
    }

    public Observable<List<OrdenTrabajo>> fetchById(long id) {
        return Observable.create(subscriber -> {
            if (!Mantum.isConnectedOrConnecting(context)) {
                subscriber.onError(new Exception(context.getString(R.string.offline)));
                return;
            }

            String endpoint = url + "/restapp/app/refreshtodo?typetodo=OT&idtodo=" + id;
            if (Integer.parseInt(version) >= 10) {
                endpoint = url + "/restapp/app/refreshtodo?typetodo=OT&id=" + id;
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
                        subscriber.onError(new Exception(context.getString(R.string.error_request_detail_ot)));
                        return;
                    }

                    String json = body.string();
                    try {
                        if (response.isSuccessful()) {
                            Response content = gson.fromJson(json, Response.class);
                            content.setVersion(response.header("Max-Version"));

                            OrdenTrabajo.Request data = content.getBody(OrdenTrabajo.Request.class, gson);
                            subscriber.onNext(!data.getPendientes().isEmpty() ? data.getPendientes() : Collections.emptyList());
                            subscriber.onComplete();
                        } else {
                            if (!subscriber.isDisposed()) {
                                subscriber.onError(new Exception(context.getString(R.string.error_get_orden_trabajo)));
                            }
                        }
                    } catch (Exception e) {
                        if (!subscriber.isDisposed()) {
                            subscriber.onError(new Exception(context.getString(R.string.error_get_response_orden_trabajo)));
                        }
                    }
                    response.close();
                }
            });
        });
    }

    public Observable<OrdenTrabajo.Request> fetchPendingAT(final Integer pages) {
        return Observable.create(subscriber -> {
            if (!Mantum.isConnectedOrConnecting(context)) {
                subscriber.onNext(new OrdenTrabajo.Request());
                subscriber.onComplete();
                return;
            }

            String endpoint = url + "/restapp/app/getmytodo?typetodo=OT_Asignadas&page=" + pages;
            Request request = new Request.Builder().get()
                    .url(endpoint)
                    .addHeader("token", token)
                    .addHeader("accept", Version.build(version))
                    .addHeader("cache-control", "no-cache")
                    .addHeader("accept-language", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                    if (call.isCanceled() || subscriber.isDisposed()) {
                        return;
                    }

                    ResponseBody body = response.body();
                    if (body == null) {
                        subscriber.onError(new Exception("Ocurrio un error a la hora de realizar la petición"));
                        return;
                    }

                    String json = body.string();
                    try {
                        Response content = gson.fromJson(json, Response.class);
                        if (response.isSuccessful()) {
                            Version.save(context, response.header("Max-Version"));

                            OrdenTrabajo.Request request = content.getBody(OrdenTrabajo.Request.class, gson);
                            subscriber.onNext(request);
                            subscriber.onComplete();
                        } else {
                            if (!subscriber.isDisposed()) {
                                subscriber.onError(new Exception(Response.buildMessage(content)));
                            }
                        }
                    } catch (Exception e) {
                        if (!subscriber.isDisposed()) {
                            subscriber.onError(new Exception(e.getMessage()));
                        }
                    }
                    response.close();
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!call.isCanceled() && !subscriber.isDisposed()) {
                        subscriber.onError(new Exception(context.getString(R.string.error_pendientes_ot_asignadas)));
                    }
                }
            });
        });
    }

    public Observable<List<OrdenTrabajo>> save(@NonNull OrdenTrabajo value) {
        return save(Collections.singletonList(value));
    }

    public Observable<List<OrdenTrabajo>> save(@NonNull List<OrdenTrabajo> values) {
        return Observable.create(subscriber -> database.executeTransactionAsync(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            List<OrdenTrabajo> results = procesarOrdenes(values, cuenta, self);

            subscriber.onNext(results);
        }, subscriber::onComplete, subscriber::onError));
    }

    public void newSave(List<OrdenTrabajo> values) {
        database.executeTransaction(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                return;
            }

            procesarOrdenes(values, cuenta, self);
        });
    }

    public Observable<List<Long>> remove(final Long[] id, boolean asignada) {
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

                try {
                    RealmResults<OrdenTrabajo> pendientes = self.where(OrdenTrabajo.class)
                            .equalTo("cuenta.UUID", cuenta.getUUID())
                            .equalTo("asignada", asignada)
                            .not().in("id", id)
                            .findAll();

                    List<Long> eliminados = new ArrayList<>();
                    for (OrdenTrabajo pendiente : pendientes) {
                        eliminados.add(pendiente.getId());
                        if (asignada) {
                            Asignada results = self.where(Asignada.class)
                                    .isNull("ordenTrabajo")
                                    .or()
                                    .equalTo("ordenTrabajo.id", pendiente.getId())
                                    .findFirst();

                            if (results != null) {
                                results.deleteFromRealm();
                            }
                        }

                    }
                    pendientes.deleteAllFromRealm();

                    subscriber.onNext(eliminados);
                    subscriber.onComplete();
                } catch (Exception e) {
                    Log.e("TAG", "remove: ", e);
                    subscriber.onError(e);
                }
            });
            database.close();
        });
    }

    public Observable<List<OrdenTrabajo>> remove(Long id) {
        return Observable.create(subscriber -> {
            if (id == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_remove_detail_ot)));
                return;
            }

            Database database = new Database(context);
            database.executeTransactionAsync(self -> {
                Cuenta cuenta = self.where(Cuenta.class)
                        .equalTo("active", true)
                        .findFirst();

                if (cuenta == null) {
                    subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                    return;
                }

                self.where(OrdenTrabajo.class)
                        .equalTo("id", id)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findAll()
                        .deleteAllFromRealm();

                subscriber.onNext(Collections.emptyList());

            }, subscriber::onComplete, subscriber::onError);
            database.close();
        });
    }

    public Observable<List<OrdenTrabajo>> getOtsByYardas(List<String> yardas) {
        return Observable.create(subscriber -> {
            if (!Mantum.isConnectedOrConnecting(context)) {
                subscriber.onError(new Exception(context.getString(R.string.offline)));
                return;
            }

            Yarda yarda = new Yarda();
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"), yarda.bodyGetOtsByYardas(yardas));

            Request request = new Request.Builder().get()
                    .addHeader("token", token)
                    .addHeader("accept", Version.build(version))
                    .addHeader("cache-control", "no-cache")
                    .addHeader("accept-language", "application/json")
                    .url(url + "/restapp/app/getotsbyyardas")
                    .post(body)
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
                        subscriber.onError(new Exception(context.getString(R.string.error_request_detail_ot)));
                        return;
                    }

                    String json = body.string();
                    try {
                        if (response.isSuccessful()) {
                            Response content = gson.fromJson(json, Response.class);
                            content.setVersion(response.header("Max-Version"));

                            OrdenTrabajo.Request data = content.getBody(OrdenTrabajo.Request.class, gson);
                            subscriber.onNext(!data.getPendientes().isEmpty() ? data.getPendientes() : Collections.emptyList());
                            subscriber.onComplete();
                        } else {
                            subscriber.onError(new Exception(context.getString(R.string.error_get_orden_trabajo)));
                        }
                    } catch (Exception e) {
                        subscriber.onError(new Exception(context.getString(R.string.error_get_response_orden_trabajo)));
                    }

                    response.close();
                }
            });
        });
    }

    private List<OrdenTrabajo> procesarOrdenes(List<OrdenTrabajo> values, Cuenta cuenta, Realm self) {
        List<OrdenTrabajo> results = new ArrayList<>();

        for (OrdenTrabajo value : values) {
            OrdenTrabajo temporal = self.where(OrdenTrabajo.class)
                    .equalTo("id", value.getId())
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findFirst();

            if (temporal == null) {
                for (Entidad entidad : value.getEntidades()) {
                    entidad.setCuenta(cuenta);
                    for (Actividad actividad : entidad.getActividades()) {
                        for (Tarea tarea : actividad.getTareas()) {
                            tarea.setUuid(UUID.randomUUID().toString());
                        }
                        actividad.setUuid(UUID.randomUUID().toString());
                        actividad.setCuenta(cuenta);
                    }
                }

                for (RecorridoHistorico recorrido : value.getRecorridos()) {
                    recorrido.setCuenta(cuenta);
                }

                for (RutaTrabajo chequeo : value.getListachequeo()) {
                    for (Entidad entidad : chequeo.getEntidades()) {
                        for (Actividad actividad : entidad.getActividades()) {
                            actividad.setUuid(UUID.randomUUID().toString());
                            actividad.setCuenta(cuenta);
                        }
                        entidad.setCuenta(cuenta);
                    }
                    chequeo.setUUID(UUID.randomUUID().toString());
                    chequeo.setCuenta(cuenta);
                }

                for (Falla falla : value.getFallas()) {
                    falla.setUUID(UUID.randomUUID().toString());

                    for (RepuestoManual repuestoManual : falla.getRepuestos()) {
                        repuestoManual.setCuenta(cuenta);
                    }

                    for (Consumible consumible : falla.getConsumibles()) {
                        consumible.setCuenta(cuenta);
                    }

                    for (ElementoFalla elementoFalla : falla.getElementos()) {
                        elementoFalla.setCuenta(cuenta);
                    }

                    falla.setCuenta(cuenta);
                }

                for (RepuestoManual repuestoManual : value.getRepuestos()) {
                    repuestoManual.setCuenta(cuenta);
                }

                for (Consumible consumible : value.getConsumibles()) {
                    consumible.setCuenta(cuenta);
                }

                value.setUUID(UUID.randomUUID().toString());
                value.setCuenta(cuenta);
                self.insert(value);

                // SE INCLUYE ESTO PARA SOLUCIONAR ERROR QUE
                // CIERRA LA APLICACIÓN
                results.add(value.isManaged() ? self.copyFromRealm(value) : value);
            } else {
                value.setUUID(temporal.getUUID());
                temporal.setCodigo(value.getCodigo());
                temporal.setPrioridad(value.getPrioridad());
                temporal.setFechainicio(value.getFechainicio());
                temporal.setFechafin(value.getFechafin());
                temporal.setEstado(value.getEstado());
                temporal.setDescripcion(value.getDescripcion());
                temporal.setRealimentacion(value.getRealimentacion());
                temporal.setColor(value.getColor());
                temporal.setPorcentaje(value.getPorcentaje());
                temporal.setDuracion(value.getDuracion());
                temporal.setOrden(value.getOrden());
                temporal.setAsignada(value.isAsignada());
                temporal.setCliente(value.getCliente());

                // Guarda los ANS de la orden de trabajo
                if (temporal.getAns() != null && !temporal.getAns().isEmpty()) {
                    temporal.getAns().deleteAllFromRealm();
                }

                RealmList<ANS> ans = new RealmList<>();
                for (ANS data : value.getAns()) {
                    ans.add(self.copyToRealm(data));
                }
                temporal.setAns(ans);

                // Guarda los recursos de la orden de trabajo
                if (temporal.getRecursos() != null && !temporal.getRecursos().isEmpty()) {
                    temporal.getRecursos().deleteAllFromRealm();
                }

                RealmList<Recurso> recursos = new RealmList<>();
                for (Recurso recurso : value.getRecursos()) {
                    recurso.setCuenta(cuenta);
                    recursos.add(self.copyToRealm(recurso));
                }
                temporal.setRecursos(recursos);

                //Guarda las fallas de la orden de trabajo
                if (temporal.getFallas() != null && !temporal.getFallas().isEmpty()) {
                    for (Falla falla : temporal.getFallas()) {
                        if (falla.getRepuestos() != null && !falla.getRepuestos().isEmpty()) {
                            falla.getRepuestos().deleteAllFromRealm();
                        }

                        if (falla.getConsumibles() != null && !falla.getConsumibles().isEmpty()) {
                            falla.getConsumibles().deleteAllFromRealm();
                        }

                        if (falla.getElementos() != null && !falla.getElementos().isEmpty()) {
                            falla.getElementos().deleteAllFromRealm();
                        }

                        if (falla.getImagenes() != null && !falla.getImagenes().isEmpty()) {
                            falla.getImagenes().deleteAllFromRealm();
                        }

                        if (falla.getAdjuntos() != null && !falla.getAdjuntos().isEmpty()) {
                            falla.getAdjuntos().deleteAllFromRealm();
                        }
                    }
                    temporal.getFallas().deleteAllFromRealm();
                }

                RealmList<Falla> fallas = new RealmList<>();
                for (Falla falla : value.getFallas()) {
                    falla.setUUID(UUID.randomUUID().toString());
                    falla.setCuenta(cuenta);

                    RealmList<RepuestoManual> repuestos = new RealmList<>();
                    for (RepuestoManual repuestoManual : falla.getRepuestos()) {
                        repuestoManual.setCuenta(cuenta);
                        repuestos.add(self.copyToRealm(repuestoManual));
                    }
                    falla.setRepuestos(repuestos);

                    RealmList<Consumible> consumibles = new RealmList<>();
                    for (Consumible consumible : falla.getConsumibles()) {
                        consumible.setCuenta(cuenta);
                        consumibles.add(self.copyToRealm(consumible));
                    }
                    falla.setConsumibles(consumibles);

                    RealmList<ElementoFalla> elementos = new RealmList<>();
                    for (ElementoFalla elementoFalla : falla.getElementos()) {
                        elementoFalla.setCuenta(cuenta);
                        elementos.add(self.copyToRealm(elementoFalla));
                    }
                    falla.setElementos(elementos);

                    RealmList<Adjuntos> imagenes = new RealmList<>();
                    for (Adjuntos imagen : falla.getImagenes()) {
                        imagenes.add(self.copyToRealm(imagen));
                    }
                    falla.setImagenes(imagenes);

                    RealmList<Adjuntos> adjuntos = new RealmList<>();
                    for (Adjuntos adjunto : falla.getAdjuntos()) {
                        adjuntos.add(self.copyToRealm(adjunto));
                    }
                    falla.setAdjuntos(adjuntos);

                    fallas.add(self.copyToRealm(falla));
                }
                temporal.setFallas(fallas);

                // Guarda los repuestos manuales
                if (temporal.getRepuestos() != null && !temporal.getRepuestos().isEmpty()) {
                    temporal.getRepuestos().deleteAllFromRealm();
                }

                RealmList<RepuestoManual> repuestos = new RealmList<>();
                for (RepuestoManual repuestoManual : value.getRepuestos()) {
                    repuestoManual.setCuenta(cuenta);
                    repuestos.add(self.copyToRealm(repuestoManual));
                }
                temporal.setRepuestos(repuestos);

                // Guarda los consumibles manuales
                if (temporal.getConsumibles() != null && !temporal.getConsumibles().isEmpty()) {
                    temporal.getConsumibles().deleteAllFromRealm();
                }

                RealmList<Consumible> consumibles = new RealmList<>();
                for (Consumible consumible : value.getConsumibles()) {
                    consumible.setCuenta(cuenta);
                    consumibles.add(self.copyToRealm(consumible));
                }
                temporal.setConsumibles(consumibles);

                // Guarda las rutas de trabajo
                if (temporal.getListachequeo() != null && !temporal.getListachequeo().isEmpty()) {
                    for (RutaTrabajo rutaTrabajo : temporal.getListachequeo()) {
                        // Elimina entidades
                        for (Entidad entidad : rutaTrabajo.getEntidades()) {
                            for (Variable variable : entidad.getVariables()) {
                                variable.getValores().deleteAllFromRealm();
                                if (variable.getUltimalectura() != null) {
                                    variable.getUltimalectura().deleteFromRealm();
                                }
                            }
                            entidad.getVariables().deleteAllFromRealm();

                            for (Actividad actividad : entidad.getActividades()) {
                                actividad.getAdjuntos().deleteAllFromRealm();
                                actividad.getImagenes().deleteAllFromRealm();
                                for (Variable variable : actividad.getVariables()) {
                                    variable.getValores().deleteAllFromRealm();
                                    if (variable.getUltimalectura() != null) {
                                        variable.getUltimalectura().deleteFromRealm();
                                    }
                                }
                                actividad.getVariables().deleteAllFromRealm();
                            }
                            entidad.getActividades().deleteAllFromRealm();
                        }
                        rutaTrabajo.getEntidades().deleteAllFromRealm();

                        // Elimina adjuntos e imagenes
                        rutaTrabajo.getImagenes().deleteAllFromRealm();
                        rutaTrabajo.getAdjuntos().deleteAllFromRealm();

                        // Elimina recursos
                        rutaTrabajo.getRecursos().deleteAllFromRealm();

                        // AM x grupos
                        rutaTrabajo.getAmxgrupos().deleteAllFromRealm();
                    }
                    temporal.getListachequeo().deleteAllFromRealm();
                }

                RealmList<RutaTrabajo> rutasTrabajo = new RealmList<>();
                for (RutaTrabajo rutaTrabajo : value.getListachequeo()) {
                    RealmList<Entidad> entidades = new RealmList<>();
                    for (Entidad entidad : rutaTrabajo.getEntidades()) {
                        RealmList<Actividad> actividades = new RealmList<>();
                        for (Actividad actividad : entidad.getActividades()) {
                            actividad.setUuid(UUID.randomUUID().toString());
                            actividad.setCuenta(cuenta);
                            actividades.add(self.copyToRealm(actividad));
                        }

                        entidad.setCuenta(cuenta);
                        entidad.setActividades(actividades);
                        entidades.add(self.copyToRealm(entidad));
                    }

                    rutaTrabajo.setUUID(UUID.randomUUID().toString());
                    rutaTrabajo.setCuenta(cuenta);
                    rutaTrabajo.setEntidades(entidades);
                    rutasTrabajo.add(self.copyToRealm(rutaTrabajo));
                }
                temporal.setListachequeo(rutasTrabajo);

                // Guarda las entidades de la orden de trabajo
                if (temporal.getEntidades() != null && !temporal.getEntidades().isEmpty()) {
                    for (Entidad entidad : temporal.getEntidades()) {
                        for (Variable variable : entidad.getVariables()) {
                            variable.getValores().deleteAllFromRealm();
                            if (variable.getUltimalectura() != null) {
                                variable.getUltimalectura().deleteFromRealm();
                            }
                        }
                        entidad.getVariables().deleteAllFromRealm();

                        for (Actividad actividad : entidad.getActividades()) {
                            actividad.getAdjuntos().deleteAllFromRealm();
                            actividad.getImagenes().deleteAllFromRealm();
                            for (Variable variable : actividad.getVariables()) {
                                variable.getValores().deleteAllFromRealm();
                                if (variable.getUltimalectura() != null) {
                                    variable.getUltimalectura().deleteFromRealm();
                                }
                            }
                            actividad.getVariables().deleteAllFromRealm();

                            actividad.getTareas().deleteAllFromRealm();
                        }
                        entidad.getActividades().deleteAllFromRealm();
                    }
                    temporal.getEntidades().deleteAllFromRealm();
                }

                RealmList<Entidad> entidades = new RealmList<>();
                for (Entidad entidad : value.getEntidades()) {
                    RealmList<Actividad> actividades = new RealmList<>();
                    for (Actividad actividad : entidad.getActividades()) {
                        RealmList<Tarea> tareas = new RealmList<>();
                        for (Tarea tarea : actividad.getTareas()) {
                            tarea.setUuid(UUID.randomUUID().toString());
                            tareas.add(self.copyToRealm(tarea));
                        }
                        actividad.setUuid(UUID.randomUUID().toString());
                        actividad.setCuenta(cuenta);
                        actividad.setTareas(tareas);
                        actividades.add(self.copyToRealm(actividad));
                    }
                    entidad.setCuenta(cuenta);
                    entidad.setActividades(actividades);
                    entidades.add(self.copyToRealm(entidad));
                }
                temporal.setEntidades(entidades);

                // Guarda los ejecutores de la orden de trabajo
                if (temporal.getEjecutores() != null && !temporal.getEjecutores().isEmpty()) {
                    temporal.getEjecutores().deleteAllFromRealm();
                }

                RealmList<Ejecutores> ejecutores = new RealmList<>();
                for (Ejecutores ejecutor : value.getEjecutores()) {
                    ejecutores.add(self.copyToRealm(ejecutor));
                }
                temporal.setEjecutores(ejecutores);

                // Guarda las imagenes de la orden de trabajo
                if (temporal.getImagenes() != null && !temporal.getImagenes().isEmpty()) {
                    temporal.getImagenes().deleteAllFromRealm();
                }

                RealmList<Adjuntos> imagenes = new RealmList<>();
                for (Adjuntos adjunto : value.getImagenes()) {
                    imagenes.add(self.copyToRealm(adjunto));
                }
                temporal.setImagenes(imagenes);

                // Guarda los adjuntos de la orden de trabajo
                if (temporal.getAdjuntos() != null && !temporal.getAdjuntos().isEmpty()) {
                    temporal.getAdjuntos().deleteAllFromRealm();
                }

                RealmList<Adjuntos> adjuntos = new RealmList<>();
                for (Adjuntos adjunto : value.getAdjuntos()) {
                    adjuntos.add(self.copyToRealm(adjunto));
                }
                temporal.setAdjuntos(adjuntos);

                // Hitorico recorrido
                if (temporal.getRecorridos() != null && !temporal.getRecorridos().isEmpty()) {
                    temporal.getRecorridos().deleteAllFromRealm();
                }

                RealmList<RecorridoHistorico> recorridos = new RealmList<>();
                for (RecorridoHistorico recorrido : value.getRecorridos()) {
                    recorrido.setCuenta(cuenta);
                    recorridos.add(self.copyToRealm(recorrido));
                }
                temporal.setRecorridos(recorridos);

                // Guarda las variables de la orden de trabajo
                if (temporal.getVariables() != null && !temporal.getVariables().isEmpty()) {
                    for (Variable variable : temporal.getVariables()) {
                        variable.getValores().deleteAllFromRealm();
                        if (variable.getUltimalectura() != null) {
                            variable.getUltimalectura().deleteFromRealm();
                        }
                    }
                    temporal.getVariables().deleteAllFromRealm();
                }

                RealmList<Variable> variables = new RealmList<>();
                for (Variable variable : value.getVariables()) {
                    variables.add(self.copyToRealm(variable));
                }
                temporal.setVariables(variables);

                // SE INCLUYE ESTO PARA SOLUCIONAR ERROR QUE
                // CIERRA LA APLICACIÓN
                results.add(temporal.isManaged() ? self.copyFromRealm(temporal) : temporal);
            }

            if (value.isAsignada()) {
                Asignada asignada = self.where(Asignada.class)
                        .equalTo("ordenTrabajo.id", value.getId())
                        .findFirst();

                OrdenTrabajo reference = self.where(OrdenTrabajo.class)
                        .equalTo("UUID", value.getUUID())
                        .equalTo("id", value.getId())
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();

                if (reference == null) {
                    continue;
                }

                if (asignada == null) {
                    asignada = new Asignada();
                    asignada.setCuenta(cuenta);
                    asignada.setOrdenTrabajo(reference);
                    asignada.setOrden(value.getOrden());
                    asignada.setTerminada(value.getTerminada());
                    self.insert(asignada);
                } else {
                    asignada.setCuenta(cuenta);
                    asignada.setOrdenTrabajo(reference);
                    asignada.setOrden(value.getOrden());
                    asignada.setTerminada(value.getTerminada());
                }
            }
        }

        return results;
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