package com.mantum.cmms.service;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Actividad;
import com.mantum.cmms.entity.Adjuntos;
import com.mantum.cmms.entity.Busqueda;
import com.mantum.cmms.entity.Consumible;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.DatosTecnico;
import com.mantum.cmms.entity.ElementoFalla;
import com.mantum.cmms.entity.Entidad;
import com.mantum.cmms.entity.Equipo;
import com.mantum.cmms.entity.Falla;
import com.mantum.cmms.entity.InstalacionLocativa;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.cmms.entity.Recurso;
import com.mantum.cmms.entity.RepuestoManual;
import com.mantum.cmms.entity.Tarea;
import com.mantum.cmms.entity.Variable;
import com.mantum.cmms.entity.parameter.Barcode;
import com.mantum.cmms.net.ClientManager;
import com.mantum.component.http.MicroServices;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import static com.mantum.component.Mantum.isConnectedOrConnecting;

public class InstalacionLocativaServices extends MicroServices {

    private static final String TAG = EquipoServices.class.getSimpleName();

    private final Database database;

    public InstalacionLocativaServices(@NonNull Context context, @NonNull Cuenta cuenta) {
        super(context, cuenta.getServidor().getUrl(), cuenta.getToken(context), ClientManager.prepare(
                new OkHttpClient.Builder(), context
        ));
        this.database = new Database(context);
    }

    public Observable<Response> asociar(@NonNull InstalacionLocativa instalacionLocativa, @NonNull String content, @NonNull String tipo) {
        return Observable.create(subscriber -> {
            if (!isConnectedOrConnecting(context)) {
                subscriber.onError(new Exception(context.getString(R.string.offline)));
                return;
            }

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"), instalacionLocativa.getInformacionParaAsociar(content, tipo));

            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            String endpoint = url + "/restapp/app/actualizarmarcacionentidad";
            Request request = new Request.Builder().url(endpoint)
                    .addHeader("token", token)
                    .addHeader("accept", cuenta.getServidor().getVersion())
                    .addHeader("cache-control", "no-cache")
                    .addHeader("accept-language", "application/json")
                    .post(body).build();

            Log.i(TAG, "POST -> " + request.url());
            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "onFailure: ", e);
                    if (!call.isCanceled()) {
                        subscriber.onError(new Exception(context.getString(R.string.request_error_search)));
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                    if (call.isCanceled()) {
                        subscriber.onComplete();
                        return;
                    }

                    ResponseBody body = response.body();
                    if (body == null) {
                        subscriber.onError(new Exception(context.getString(R.string.request_error_search)));
                        return;
                    }

                    String json = body.string();
                    Log.i(TAG, String.format("POST <- %s", request.url()));
                    try {
                        if (response.isSuccessful()) {
                            Response content = gson.fromJson(json, Response.class);
                            content.setVersion(response.header("Max-Version"));

                            subscriber.onNext(content);
                            subscriber.onComplete();
                        } else {
                            Response content = gson.fromJson(json, Response.class);
                            subscriber.onError(new Exception(content.getMessage()));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onResponse: ", e);
                        subscriber.onError(new Exception(context.getString(R.string.request_reading_error)));
                    }
                    response.close();
                }
            });
        });
    }

    public Observable<List<InstalacionLocativa>> save(@NonNull InstalacionLocativa value) {
        return save(Collections.singletonList(value));
    }

    public Observable<List<InstalacionLocativa>> save(@NonNull List<InstalacionLocativa> values) {
        return Observable.create(subscriber -> database.executeTransactionAsync(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception(
                        context.getString(R.string.error_authentication)));
                return;
            }

            procesarInstalacionesLocativas(values, cuenta, self, true);

            subscriber.onNext(values);
        }, subscriber::onComplete, subscriber::onError));
    }

    public void update(InstalacionLocativa value) {
        database.executeTransaction(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                return;
            }

            List<InstalacionLocativa> values = Collections.singletonList(value);
            procesarInstalacionesLocativas(values, cuenta, self, false);
        });
    }

    private void procesarInstalacionesLocativas(List<InstalacionLocativa> values, Cuenta cuenta, Realm self, boolean isSave) {
        for (InstalacionLocativa value : values) {
            InstalacionLocativa temporal = self.where(InstalacionLocativa.class)
                    .equalTo("id", value.getId())
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findFirst();

            if (temporal == null) {
                for (Actividad actividad : value.getActividades()) {
                    for (Tarea tarea : actividad.getTareas()) {
                        tarea.setUuid(UUID.randomUUID().toString());
                    }
                    actividad.setUuid(UUID.randomUUID().toString());
                    actividad.setCuenta(cuenta);
                }

                for (OrdenTrabajo ordenTrabajo : value.getOrdenTrabajos()) {
                    ordenTrabajo.setUUID(UUID.randomUUID().toString());
                    ordenTrabajo.setCuenta(cuenta);
                    for (Entidad entidad : ordenTrabajo.getEntidades()) {
                        entidad.setCuenta(cuenta);
                    }

                    for (Recurso recurso : ordenTrabajo.getRecursos()) {
                        recurso.setCuenta(cuenta);
                    }
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

                value.setUUID(UUID.randomUUID().toString());
                value.setCuenta(cuenta);
                self.insert(value);
            } else {
                value.setUUID(temporal.getUuid());
                temporal.setCodigo(value.getCodigo());
                temporal.setNombre(value.getNombre());
                temporal.setInstalacionpadre(value.getInstalacionpadre());
                temporal.setTipodeinstalacion(value.getTipodeinstalacion());
                temporal.setEstado(value.getEstado());
                temporal.setCriticidad(value.getCriticidad());
                temporal.setFamilia1(value.getFamilia1());
                temporal.setFamilia2(value.getFamilia2());
                temporal.setFamilia3(value.getFamilia3());
                temporal.setDireccion(value.getDireccion());
                temporal.setGmap(value.getGmap());
                temporal.setNfctoken(value.getNfctoken());

                // Historico OT
                if (temporal.getOrdenTrabajos() != null && !temporal.getOrdenTrabajos().isEmpty()) {
                    for (OrdenTrabajo ordenTrabajo : temporal.getOrdenTrabajos()) {
                        ordenTrabajo.getAdjuntos().deleteAllFromRealm();
                        ordenTrabajo.getImagenes().deleteAllFromRealm();
                        ordenTrabajo.getEjecutores().deleteAllFromRealm();
                        ordenTrabajo.getRecursos().deleteAllFromRealm();

                        for (Variable variable : ordenTrabajo.getVariables()) {
                            variable.getValores().deleteAllFromRealm();
                            if (variable.getUltimalectura() != null) {
                                variable.getUltimalectura().deleteFromRealm();
                            }
                        }
                        ordenTrabajo.getVariables().deleteAllFromRealm();

                        for (Entidad entidad : ordenTrabajo.getEntidades()) {
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
                        }
                        ordenTrabajo.getEntidades().deleteAllFromRealm();
                    }
                    temporal.getOrdenTrabajos().deleteAllFromRealm();
                }

                RealmList<OrdenTrabajo> ordenTrabajos = new RealmList<>();
                for (OrdenTrabajo ordenTrabajo : value.getOrdenTrabajos()) {
                    ordenTrabajo.setUUID(UUID.randomUUID().toString());
                    ordenTrabajo.setCuenta(cuenta);

                    for (Entidad entidad : ordenTrabajo.getEntidades()) {
                        entidad.setCuenta(cuenta);
                    }

                    for (Recurso recurso : ordenTrabajo.getRecursos()) {
                        recurso.setCuenta(cuenta);
                    }

                    ordenTrabajos.add(self.copyToRealm(ordenTrabajo));
                }
                temporal.setOrdenTrabajos(ordenTrabajos);

                // Datos tecnicos
                if (temporal.getDatostecnicos() != null && !temporal.getDatostecnicos().isEmpty()) {
                    temporal.getDatostecnicos().deleteAllFromRealm();
                }

                RealmList<DatosTecnico> datosTecnicos = new RealmList<>();
                for (DatosTecnico datosTecnico : value.getDatostecnicos()) {
                    datosTecnicos.add(self.copyToRealm(datosTecnico));
                }
                temporal.setDatostecnicos(datosTecnicos);

                //Guarda las fallas
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

                // Guarda las actividades
                if (temporal.getActividades() != null && !temporal.getActividades().isEmpty()) {
                    for (Actividad actividad : temporal.getActividades()) {
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
                    temporal.getActividades().deleteAllFromRealm();
                }

                RealmList<Actividad> actividades = new RealmList<>();
                for (Actividad actividad : value.getActividades()) {
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
                temporal.setActividades(actividades);

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

                //Actualizar busqueda
                if (!isSave) {
                    Busqueda busqueda = self.where(Busqueda.class)
                            .equalTo("id", temporal.getId())
                            .equalTo("type", Equipo.SELF)
                            .equalTo("cuenta.UUID", cuenta.getUUID())
                            .findFirst();

                    if (busqueda != null) {
                        busqueda.setId(temporal.getId());
                        busqueda.setCode(temporal.getCodigo());
                        busqueda.setName(temporal.getNombre());
                        busqueda.setReference(temporal.getUuid());
                        busqueda.setType(Equipo.SELF);
                        busqueda.setNfc(temporal.getNfctoken());
                        busqueda.setQrcode(temporal.getQrcode());
                        busqueda.setMostrar(true);
                        busqueda.setData(null);

                        Barcode barcode = new Barcode();
                        barcode.setCodigo(temporal.getBarcode());
                        RealmList<Barcode> barcodes = new RealmList<>();
                        barcodes.add(self.copyToRealm(barcode));
                        busqueda.setBarcode(barcodes);
                    }
                }
            }
        }
    }

    public void onDestroy() {
        if (database != null) {
            database.close();
        }
        cancel();
    }
}
