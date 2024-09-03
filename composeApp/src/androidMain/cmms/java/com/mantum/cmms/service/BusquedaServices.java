package com.mantum.cmms.service;

import android.content.Context;

import androidx.annotation.NonNull;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mantum.demo.R;
import com.mantum.cmms.convert.BusquedaConvert;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.BusquedaAvanzada;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Accion;
import com.mantum.cmms.entity.Busqueda;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.DetalleBusqueda;
import com.mantum.cmms.entity.Entidad;
import com.mantum.cmms.entity.Equipo;
import com.mantum.cmms.entity.InstalacionLocativa;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.cmms.entity.SolicitudServicio;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.util.Tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.internal.functions.Functions;
import io.realm.RealmList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import static com.mantum.component.Mantum.isConnectedOrConnecting;

public class BusquedaServices extends Service {

    private static final String TAG = BusquedaServices.class.getSimpleName();

    private final Database database;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public enum Type {
        BARCODE, QRCODE, NFC
    }

    public BusquedaServices(@NonNull Context context) {
        super(context);
        this.database = new Database(context);
    }

    public void onDestroy() {
        if (database != null) {
            database.close();
        }

        compositeDisposable.clear();
    }

    @NonNull
    private List<Busqueda> save(@NonNull Response response) {
        Version.save(context, response.getVersion());

        try {
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(context.getString(R.string.error_authentication));
            }

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(new TypeToken<Busqueda.Request>() {
                    }.getType(), new BusquedaConvert())
                    .create();

            InstalacionLocativaServices instalacionLocativaServices
                    = new InstalacionLocativaServices(context, cuenta);
            EquipoServices equipoServices = new EquipoServices(context, cuenta);
            OrdenTrabajoService ordenTrabajoService = new OrdenTrabajoService(context, cuenta);
            SolicitudServicioService solicitudServicioService
                    = new SolicitudServicioService(context, cuenta);

            Busqueda.Request content = gson.fromJson(response.getBody(), Busqueda.Request.class);
            for (Busqueda busqueda : content.getEntities()) {
                switch (busqueda.getType()) {
                    case Equipo.SELF:
                        Equipo equipo = busqueda.getDetalle(Equipo.class);
                        if (equipo != null) {
                            equipo.setId(busqueda.getId());
                            equipo.setVariables(busqueda.getVariables());
                            equipo.setGmap(busqueda.getGmap());
                            equipo.setOrdenTrabajos(busqueda.getHistoricoOT());

                            compositeDisposable.add(equipoServices.save(equipo)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(this::guardarEquipos, Mantum::ignoreError, Functions.EMPTY_ACTION));
                        }
                        break;

                    case InstalacionLocativa.SELF:
                        InstalacionLocativa instalacionLocativa = busqueda.getDetalle(InstalacionLocativa.class);
                        if (instalacionLocativa != null) {
                            instalacionLocativa.setId(busqueda.getId());
                            instalacionLocativa.setVariables(busqueda.getVariables());
                            instalacionLocativa.setGmap(busqueda.getGmap());
                            instalacionLocativa.setOrdenTrabajos(busqueda.getHistoricoOT());

                            compositeDisposable.add(instalacionLocativaServices.save(instalacionLocativa)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(this::guardarInstalacionesLocativas, Mantum::ignoreError, Functions.EMPTY_ACTION));
                        }
                        break;

                    case OrdenTrabajo.SELF:
                        compositeDisposable.add(ordenTrabajoService.save(busqueda.getDetalle(OrdenTrabajo.class))
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(this::guardarOrdenTrabajo, Mantum::ignoreError, Functions.EMPTY_ACTION));
                        break;

                    case SolicitudServicio.SELF:
                        compositeDisposable.add(solicitudServicioService.save(busqueda.getDetalle(SolicitudServicio.class))
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(this::guardarSolicitudServicio, Mantum::ignoreError, Functions.EMPTY_ACTION));
                        break;
                }

            }

        } catch (Exception e) {
            Log.e(TAG, "save: ", e);
        }

        return new ArrayList<>();
    }

    public void guardarEquipos(@NonNull List<Equipo> equipos) {
        for (Equipo equipo : equipos) {
            save(equipo);
        }
    }

    public void guardarInstalacionesLocativas(@NonNull List<InstalacionLocativa> instalacionesLocativas) {
        for (InstalacionLocativa instalacionLocativa : instalacionesLocativas) {
            save(instalacionLocativa);
        }
    }

    private void guardarOrdenTrabajo(@NonNull List<OrdenTrabajo> ordenesDeTrabajo) {
        for (OrdenTrabajo ordenTrabajo : ordenesDeTrabajo) {
            save(ordenTrabajo);
        }
    }

    private void guardarSolicitudServicio(@NonNull List<SolicitudServicio> solicitudesDeServicio) {
        for (SolicitudServicio solicitudServicio : solicitudesDeServicio) {
            save(solicitudServicio);
        }
    }

    public void save(@NonNull Equipo equipo) {
        database.executeTransaction(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                return;
            }

            Busqueda busqueda = self.where(Busqueda.class)
                    .equalTo("id", equipo.getId())
                    .equalTo("type", Equipo.SELF)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findFirst();

            if (busqueda == null) {
                busqueda = new Busqueda();
                busqueda.generateUUID();
                busqueda.setCuenta(cuenta);
                busqueda.setId(equipo.getId());
                busqueda.setCode(equipo.getCodigo());
                busqueda.setName(equipo.getNombre());
                busqueda.setType(Equipo.SELF);
                busqueda.setReference(equipo.getUUID());
                busqueda.setMostrar(true);
                self.insert(busqueda);
            } else {
                busqueda.setId(equipo.getId());
                busqueda.setCode(equipo.getCodigo());
                busqueda.setName(equipo.getNombre());
                busqueda.setReference(equipo.getUUID());
                busqueda.setType(Equipo.SELF);
                busqueda.setMostrar(true);
            }
        });
    }

    public void save(@NonNull InstalacionLocativa instalacionLocativa) {
        database.executeTransaction(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                return;
            }

            Busqueda busqueda = self.where(Busqueda.class)
                    .equalTo("id", instalacionLocativa.getId())
                    .equalTo("type", InstalacionLocativa.SELF)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findFirst();

            if (busqueda == null) {
                busqueda = new Busqueda();
                busqueda.generateUUID();
                busqueda.setCuenta(cuenta);
                busqueda.setId(instalacionLocativa.getId());
                busqueda.setCode(instalacionLocativa.getCodigo());
                busqueda.setName(instalacionLocativa.getNombre());
                busqueda.setReference(instalacionLocativa.getUuid());
                busqueda.setType(InstalacionLocativa.SELF);
                busqueda.setMostrar(true);
                self.insert(busqueda);
            } else {
                busqueda.setId(instalacionLocativa.getId());
                busqueda.setCode(instalacionLocativa.getCodigo());
                busqueda.setName(instalacionLocativa.getNombre());
                busqueda.setReference(instalacionLocativa.getUuid());
                busqueda.setType(InstalacionLocativa.SELF);
                busqueda.setMostrar(true);
            }
        });
    }

    public void save(@NonNull SolicitudServicio solicitudServicio) {
        database.executeTransaction(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                return;
            }

            Busqueda busqueda = self.where(Busqueda.class)
                    .equalTo("id", solicitudServicio.getId())
                    .equalTo("type", SolicitudServicio.SELF)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findFirst();

            if (busqueda == null) {
                busqueda = new Busqueda();
                busqueda.generateUUID();
                busqueda.setCuenta(cuenta);
                busqueda.setId(solicitudServicio.getId());
                busqueda.setCode(solicitudServicio.getCodigo());
                busqueda.setName("");
                busqueda.setType(SolicitudServicio.SELF);
                busqueda.setReference(solicitudServicio.getUUID());
                busqueda.setMostrar(true);
                self.insert(busqueda);
            } else {
                busqueda.setId(solicitudServicio.getId());
                busqueda.setCode(solicitudServicio.getCodigo());
                busqueda.setName("");
                busqueda.setType(SolicitudServicio.SELF);
                busqueda.setReference(solicitudServicio.getUUID());
                busqueda.setMostrar(true);
            }
        });
    }

    public void save(@NonNull OrdenTrabajo ordenTrabajo) {
        database.executeTransaction(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                return;
            }

            Busqueda busqueda = self.where(Busqueda.class)
                    .equalTo("id", ordenTrabajo.getId())
                    .equalTo("type", OrdenTrabajo.SELF)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findFirst();

            if (busqueda == null) {
                busqueda = new Busqueda();
                busqueda.generateUUID();
                busqueda.setCuenta(cuenta);
                busqueda.setId(ordenTrabajo.getId());
                busqueda.setCode(ordenTrabajo.getCodigo());
                busqueda.setName("");
                busqueda.setReference(ordenTrabajo.getUUID());
                busqueda.setMostrar(true);
                busqueda.setType(OrdenTrabajo.SELF);

                self.insert(busqueda);
            } else {
                busqueda.setId(ordenTrabajo.getId());
                busqueda.setCode(ordenTrabajo.getCodigo());
                busqueda.setName("");
                busqueda.setReference(ordenTrabajo.getUUID());
                busqueda.setType(OrdenTrabajo.SELF);
                busqueda.setMostrar(true);
            }
        });
    }

    public Observable<Response> avanzada(@NonNull BusquedaAvanzada busquedaAvanzada) {
        return Observable.create(subscriber -> {
            if (!isConnectedOrConnecting(context)) {
                subscriber.onError(new Exception(context.getString(R.string.offline)));
                return;
            }

            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            String token = cuenta.getToken(context);
            if (token == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            String url = cuenta.getServidor().getUrl()
                    + "/restapp/app/buscarentidadesjerarquicas?entidadpadre=" + Tool.formData(busquedaAvanzada.getParent());

            String type = Tool.formData(busquedaAvanzada.getType());
            if (!type.isEmpty()) {
                url = url + "&tipoentidadpadre=" + type;
            }

            String code = Tool.formData(busquedaAvanzada.getCode());
            if (!code.isEmpty()) {
                url = url + "&codigo=" + code;
            }

            String external = Tool.formData(busquedaAvanzada.getExternal());
            if (!external.isEmpty()) {
                url = url + "&codigoexterno=" + external;
            }

            String name = Tool.formData(busquedaAvanzada.getName());
            if (!name.isEmpty()) {
                url = url + "&nombre=" + name;
            }

            String family = Tool.formData(busquedaAvanzada.getFamily());
            if (!family.isEmpty()) {
                url = url + "&familia=" + family;
            }

            String longitude = Tool.formData(busquedaAvanzada.getLongitude());
            if (!longitude.isEmpty()) {
                url = url + "&longitud=" + longitude;
            }

            String latitude = Tool.formData(busquedaAvanzada.getLatitude());
            if (!longitude.isEmpty()) {
                url = url + "&latitud=" + latitude;
            }

            String altitude = Tool.formData(busquedaAvanzada.getAltitude());
            if (!longitude.isEmpty()) {
                url = url + "&altitud=" + altitude;
            }

            Request request = new Request.Builder().get().url(url)
                    .addHeader("token", token)
                    .addHeader("cache-control", "no-cache")
                    .addHeader("accept-language", "application/json")
                    .addHeader("accept", Version.build(cuenta.getServidor().getVersion()))
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!call.isCanceled() && !subscriber.isDisposed()) {
                        subscriber.onError(new Exception(context.getString(R.string.request_error_search)));
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                    if (call.isCanceled()) {
                        return;
                    }

                    ResponseBody body = response.body();
                    if (body == null) {
                        subscriber.onError(new Exception(context.getString(R.string.request_error_search)));
                        return;
                    }

                    String json = body.string();
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
                        subscriber.onError(new Exception(context.getString(R.string.request_reading_error)));
                    }
                    response.close();
                }
            });
        });
    }

    public Observable<Response> buscar(String criterio, String tipo) {
        return Observable.create(subscriber -> {
            if (!isConnectedOrConnecting(context) || criterio.length() < 2) {
                subscriber.onComplete();
                return;
            }

            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            String token = cuenta.getToken(context);
            if (token == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            Request request;
            if (Version.check(context, 13)) {
                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json"), getInformacionParaAsociar(criterio, tipo));

                String url = cuenta.getServidor().getUrl() + "/restapp/app/searchentity";
                request = new Request.Builder().get().url(url)
                        .addHeader("token", token)
                        .addHeader("cache-control", "no-cache")
                        .addHeader("accept-language", "application/json")
                        .addHeader("accept", Version.build(cuenta.getServidor().getVersion()))
                        .post(body).build();
            } else {
                String url = cuenta.getServidor().getUrl() + "/restapp/app/searchentity?limit=100&code=" + criterio + "&typecode=" + tipo;
                request = new Request.Builder().get().url(url)
                        .addHeader("token", token)
                        .addHeader("cache-control", "no-cache")
                        .addHeader("accept-language", "application/json")
                        .addHeader("accept", Version.build(cuenta.getServidor().getVersion()))
                        .build();
            }

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!call.isCanceled()) {
                        subscriber.onError(new Exception(context.getString(R.string.request_error_search)));
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                    if (call.isCanceled()) {
                        return;
                    }

                    ResponseBody body = response.body();
                    if (body == null) {
                        subscriber.onError(new Exception(context.getString(R.string.request_error_search)));
                        return;
                    }

                    String json = body.string();
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
                        subscriber.onError(new Exception(context.getString(R.string.request_reading_error)));
                    }
                    response.close();
                }
            });
        });
    }

    public Observable<Response> buscar(Long id, String newText) {
        return Observable.create(subscriber -> {
            if (!isConnectedOrConnecting(context) || newText.length() < 2) {
                subscriber.onComplete();
                return;
            }

            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            String token = cuenta.getToken(context);
            if (token == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            Request request;
            if (Version.check(context, 13)) {
                RequestBody body = RequestBody.create(MediaType.parse("application/json"), id == null
                        ? getBodyBySearch(newText)
                        : getBodyByEntity(id, newText));

                String url = cuenta.getServidor().getUrl() + "/restapp/app/searchentity";
                request = new Request.Builder().get().url(url)
                        .addHeader("token", token)
                        .addHeader("cache-control", "no-cache")
                        .addHeader("accept-language", "application/json")
                        .addHeader("accept", Version.build(cuenta.getServidor().getVersion()))
                        .post(body).build();
            } else {
                String endpoint = id == null
                        ? "/restapp/app/searchentity?limit=100&search=" + newText
                        : "/restapp/app/searchentity?limit=100&entitytype=" + newText + "&entityid=" + id;

                String url = cuenta.getServidor().getUrl() + endpoint;
                request = new Request.Builder().get().url(url)
                        .addHeader("token", token)
                        .addHeader("cache-control", "no-cache")
                        .addHeader("accept-language", "application/json")
                        .addHeader("accept", Version.build(cuenta.getServidor().getVersion()))
                        .build();
            }

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!call.isCanceled() && !subscriber.isDisposed()) {
                        subscriber.onError(new Exception(context.getString(R.string.request_error_search)));
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                    if (call.isCanceled()) {
                        return;
                    }

                    ResponseBody body = response.body();
                    if (body == null) {
                        subscriber.onError(new Exception(context.getString(R.string.request_error_search)));
                        return;
                    }

                    String json = body.string();
                    try {
                        if (response.isSuccessful()) {
                            Response content = gson.fromJson(json, Response.class);
                            content.setVersion(response.header("Max-Version"));

                            subscriber.onNext(content);
                            subscriber.onComplete();
                        } else {
                            Response content = gson.fromJson(json, Response.class);
                            if (!subscriber.isDisposed()) {
                                subscriber.onError(new Exception(content.getMessage()));
                            }
                        }
                    } catch (Exception e) {
                        if (!subscriber.isDisposed()) {
                            subscriber.onError(new Exception(context.getString(R.string.request_reading_error)));
                        }
                    }
                    response.close();
                }
            });
        });
    }

    public Observable<Response> buscar(Long id, String newText, String entitytype) {
        return Observable.create(subscriber -> {
            if (!isConnectedOrConnecting(context) || newText.length() < 2) {
                subscriber.onComplete();
                return;
            }

            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            String token = cuenta.getToken(context);
            if (token == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            Request request;
            if (Version.check(context, 13)) {
                RequestBody body = RequestBody.create(MediaType.parse("application/json"), id == null
                        ? getBodyBySearch(newText,entitytype)
                        : getBodyByEntity(id, newText));

                String url = cuenta.getServidor().getUrl() + "/restapp/app/searchentity";
                request = new Request.Builder().get().url(url)
                        .addHeader("token", token)
                        .addHeader("cache-control", "no-cache")
                        .addHeader("accept-language", "application/json")
                        .addHeader("accept", Version.build(cuenta.getServidor().getVersion()))
                        .post(body).build();
            } else {
                String endpoint = id == null
                        ? "/restapp/app/searchentity?limit=100&search=" + newText
                        : "/restapp/app/searchentity?limit=100&entitytype=" + newText + "&entityid=" + id;

                String url = cuenta.getServidor().getUrl() + endpoint;
                request = new Request.Builder().get().url(url)
                        .addHeader("token", token)
                        .addHeader("cache-control", "no-cache")
                        .addHeader("accept-language", "application/json")
                        .addHeader("accept", Version.build(cuenta.getServidor().getVersion()))
                        .build();
            }

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!call.isCanceled() && !subscriber.isDisposed()) {
                        subscriber.onError(new Exception(context.getString(R.string.request_error_search)));
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                    if (call.isCanceled()) {
                        return;
                    }

                    ResponseBody body = response.body();
                    if (body == null) {
                        subscriber.onError(new Exception(context.getString(R.string.request_error_search)));
                        return;
                    }

                    String json = body.string();
                    try {
                        if (response.isSuccessful()) {
                            Response content = gson.fromJson(json, Response.class);
                            content.setVersion(response.header("Max-Version"));

                            subscriber.onNext(content);
                            subscriber.onComplete();
                        } else {
                            Response content = gson.fromJson(json, Response.class);
                            if (!subscriber.isDisposed()) {
                                subscriber.onError(new Exception(content.getMessage()));
                            }
                        }
                    } catch (Exception e) {
                        if (!subscriber.isDisposed()) {
                            subscriber.onError(new Exception(context.getString(R.string.request_reading_error)));
                        }
                    }
                    response.close();
                }
            });
        });
    }

    public void guardarBusqueda(@NonNull Entidad entidad) {
        guardarBusqueda(Collections.singletonList(entidad));
    }

    private void guardarBusqueda(@NonNull List<Entidad> entidades) {
        if (entidades.size() == 0) {
            return;
        }

        database.executeTransaction(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                return;
            }

            for (Entidad entidad : entidades) {
                Busqueda busqueda = self.where(Busqueda.class)
                        .equalTo("id", entidad.getId())
                        .equalTo("type", entidad.getTipo())
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();

                if (busqueda == null) {
                    busqueda = new Busqueda();
                    busqueda.setId(entidad.getId());
                    busqueda.generateUUID();
                    busqueda.setCuenta(cuenta);
                    busqueda.setType(entidad.getTipo());
                    busqueda.setCode(entidad.getCodigo());
                    busqueda.setName(entidad.getNombre());

                    RealmList<Accion> actions = new RealmList<>();
                    Accion bitacoraAccion = self.where(Accion.class)
                            .equalTo("name", "SS")
                            .findFirst();

                    if (bitacoraAccion == null) {
                        bitacoraAccion = new Accion();
                        bitacoraAccion.setName("SS");
                    }
                    actions.add(bitacoraAccion);

                    Accion solicitudServicioAccion = self.where(Accion.class)
                            .equalTo("name", "Bit")
                            .findFirst();

                    if (solicitudServicioAccion == null) {
                        solicitudServicioAccion = new Accion();
                        solicitudServicioAccion.setName("Bit");
                    }
                    actions.add(solicitudServicioAccion);

                    busqueda.setActions(actions);
                    RealmList<DetalleBusqueda> detalles = new RealmList<>();
                    detalles.add(new DetalleBusqueda().setTitle("Código").setValue(entidad.getCodigo()));
                    detalles.add(new DetalleBusqueda().setTitle("Nombre").setValue(entidad.getNombre()));
                    busqueda.setData(detalles);

                    self.insert(busqueda);
                }
            }
        });
    }

    @NonNull
    public String getInformacionParaAsociar(@NonNull String code, @NonNull String tipo) {
        JsonObject dataset = new JsonObject();
        dataset.addProperty("code", code);
        dataset.addProperty("typecode", tipo);
        dataset.addProperty("limit", "100");

        return new Gson().toJson(dataset);
    }

    public String getBodyBySearch(String search) {
        JsonObject body = new JsonObject();
        body.addProperty("search", search);
        body.addProperty("limit", "100");

        return new Gson().toJson(body);
    }

    public String getBodyBySearch(String search, String entitytype) {
        JsonObject body = new JsonObject();
        body.addProperty("search", search);
        body.addProperty("limit", "100");
        body.addProperty("entitytype", entitytype);

        return new Gson().toJson(body);
    }

    public String getBodyByEntity(Long entityid, String entitytype) {
        JsonObject body = new JsonObject();
        body.addProperty("entityid", entityid);
        body.addProperty("entitytype", entitytype);
        body.addProperty("limit", "100");

        return new Gson().toJson(body);
    }
}