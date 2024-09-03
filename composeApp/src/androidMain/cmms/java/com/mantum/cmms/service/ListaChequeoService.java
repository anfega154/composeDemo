package com.mantum.cmms.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Actividad;
import com.mantum.cmms.entity.ClienteListaChequeo;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Entidad;
import com.mantum.cmms.entity.EntidadesClienteListaChequeo;
import com.mantum.cmms.entity.ListaChequeo;
import com.mantum.cmms.entity.PersonalListaChequeo;
import com.mantum.cmms.entity.Tarea;
import com.mantum.cmms.entity.Variable;
import com.mantum.cmms.net.ClientManager;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.http.MicroServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class ListaChequeoService extends MicroServices {

    private static final String TAG = ListaChequeoService.class.getSimpleName();

    private final Cuenta cuenta;
    private final Context context;
    private final Database database;

    public ListaChequeoService(@NonNull Context context, @NonNull Cuenta cuenta) {
        super(context, cuenta.getServidor().getUrl(), cuenta.getToken(context), ClientManager.prepare(
                new OkHttpClient.Builder(), context
        ));

        this.cuenta = cuenta;
        this.context = context;
        this.database = new Database(context);
    }

    public void close() {
        if (database != null) {
            database.close();
        }
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

                self.where(ListaChequeo.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findAll()
                        .deleteAllFromRealm();

                self.where(ClienteListaChequeo.class)
                        .findAll()
                        .deleteAllFromRealm();

                self.where(EntidadesClienteListaChequeo.class)
                        .findAll()
                        .deleteAllFromRealm();

                self.where(PersonalListaChequeo.class)
                        .findAll()
                        .deleteAllFromRealm();

                subscriber.onNext(true);
                subscriber.onComplete();
            });
        });
    }

    public Observable<ListaChequeo.Response> download() {
        String accept = cuenta.getServidor().getVersion();
        AtomicInteger counter = new AtomicInteger(1);

        return Observable.create(subscriber -> {
            if (!Mantum.isConnectedOrConnecting(context)) {
                if (!subscriber.isDisposed()) {
                    subscriber.onError(new Exception(context.getString(R.string.error_conexion)));
                }
                return;
            }

            String endpoint = url + "/restapp/app/getdownloadlistcheckentity?page=" + counter.get();
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
                                subscriber.onError(new Exception(context.getString(R.string.error_descargando_lista_chequeo)));
                            }
                            return;
                        }

                        Version.save(context, response.header("Max-Version"));
                        Response content = gson.fromJson(json, Response.class);

                        int value = counter.incrementAndGet();
                        ListaChequeo.Response result = content.getBody(ListaChequeo.Response.class, gson);

                        if (result.getNext() != null) {
                            result.setNext(value);
                        }

                        subscriber.onNext(result);
                        subscriber.onComplete();
                    } catch (Exception e) {
                        if (!subscriber.isDisposed()) {
                            subscriber.onError(new Exception(context.getString(R.string.error_descargando_lista_chequeo)));
                        }
                    }

                    response.close();
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!call.isCanceled() && !subscriber.isDisposed()) {
                        subscriber.onError(new Exception(context.getString(R.string.error_descargando_lista_chequeo)));
                    }
                }
            });
        });
    }

    public Observable<ListaChequeo.Response> findAll(Integer pages) {
        String accept = cuenta.getServidor().getVersion();

        return Observable.create(subscriber -> {
            if (!Mantum.isConnectedOrConnecting(context)) {
                subscriber.onNext(new ListaChequeo.Response());
                subscriber.onComplete();
                return;
            }

            String endpoint = url + "/restapp/app/getlistcheckentity?page=" + pages;
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
                        subscriber.onError(new Exception("Ocurrio un error a la hora de realizar la petición"));
                        return;
                    }

                    String json = body.string();
                    try {
                        if (response.isSuccessful()) {
                            Version.save(context, response.header("Max-Version"));
                            Response content = gson.fromJson(json, Response.class);

                            ListaChequeo.Response request = content.getBody(ListaChequeo.Response.class, gson);
                            subscriber.onNext(request);
                            subscriber.onComplete();
                        } else {
                            if (!subscriber.isDisposed()) {
                                subscriber.onError(new Exception(context.getString(R.string.error_lista_chequeo)));
                            }

                        }
                    } catch (Exception e) {
                        if (!subscriber.isDisposed()) {
                            subscriber.onError(new Exception(context.getString(R.string.error_lista_chequeo)));
                        }
                    }
                    response.close();
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!call.isCanceled() && !subscriber.isDisposed()) {
                        subscriber.onError(new Exception(context.getString(R.string.error_lista_chequeo)));
                    }
                }
            });
        });
    }

    public Observable<ListaChequeo> fetchById(long id) {
        String accept = cuenta.getServidor().getVersion();

        return Observable.create(subscriber -> {
            ListaChequeo result = getById(id);
            if (result != null && !result.getEntidades().isEmpty()) {
                Log.i(TAG, "Se carga la lista de chequeo del dispositivo");
                subscriber.onNext(result);
            }

            if (!Mantum.isConnectedOrConnecting(context)) {
                subscriber.onComplete();
                return;
            }

            String endpoint = url + "/restapp/app/getlcentity?idlc=" + id;
            Request request = new Request.Builder().get()
                    .addHeader("token", token)
                    .addHeader("accept", Version.build(accept))
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

                            ListaChequeo data = content.getBody(ListaChequeo.class, gson);
                            subscriber.onNext(data);
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

    public Observable<List<ListaChequeo>> saveSimple(List<ListaChequeo> chequeos) {
        return Observable.create(subscriber -> database.executeTransactionAsync(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            List<ListaChequeo> results = new ArrayList<>();
            for (ListaChequeo value : chequeos) {
                ListaChequeo current = self.where(ListaChequeo.class)
                        .equalTo("id", value.getId())
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();

                if (current == null) {
                    value.setCuenta(cuenta);
                    self.insert(value);

                    results.add(value.isManaged() ? self.copyFromRealm(value) : value);
                } else {
                    current.setCodigo(value.getCodigo());
                    current.setNombre(value.getNombre());
                    current.setEspecialidad(value.getEspecialidad());
                    current.setDescripcion(value.getDescripcion());
                    String currentTime = String.valueOf(System.currentTimeMillis());
                    String lastFourDigits = currentTime.length() > 4 ? currentTime.substring(currentTime.length() - 4) : currentTime;
                    current.setIdFirma(String.valueOf(value.getId()) + lastFourDigits);

                    results.add(current.isManaged() ? self.copyFromRealm(current) : current);
                }
            }
            subscriber.onNext(results);
        }, subscriber::onComplete, subscriber::onError));
    }

    public Observable<ListaChequeo.Response> save(@NonNull ListaChequeo.Response response) {
        return save(response.getData())
                .map(completed -> {
                    response.setData(completed);
                    return response;
                });
    }

    public Observable<List<ListaChequeo>> save(ListaChequeo values) {
        ArrayList<ListaChequeo> temp = new ArrayList<>();
        temp.add(values);

        return save(temp);
    }

    private Observable<List<ListaChequeo>> save(List<ListaChequeo> chequeos) {
        return Observable.create(subscriber -> database.executeTransactionAsync(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            List<ListaChequeo> results = new ArrayList<>();
            for (ListaChequeo value : chequeos) {
                ListaChequeo current = self.where(ListaChequeo.class)
                        .equalTo("id", value.getId())
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();

                if (current == null) {
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

                    value.setCuenta(cuenta);
                    self.insert(value);

                    results.add(value.isManaged() ? self.copyFromRealm(value) : value);
                } else {
                    current.setCodigo(value.getCodigo());
                    current.setNombre(value.getNombre());
                    current.setEspecialidad(value.getEspecialidad());
                    current.setDescripcion(value.getDescripcion());
                    String currentTime = String.valueOf(System.currentTimeMillis());
                    String lastFourDigits = currentTime.length() > 4 ? currentTime.substring(currentTime.length() - 4) : currentTime;
                    current.setIdFirma(String.valueOf(value.getId()) + lastFourDigits);

                    if (current.getPersonal() != null) {
                        for (int i = current.getPersonal().size() - 1; i >= 0; i--) {
                            PersonalListaChequeo item = current.getPersonal().get(i);
                            if (item != null) {
                                item.deleteFromRealm();
                            }
                        }
                    }

                    if (current.getEntidadesCliente() != null) {
                        for (int i = current.getEntidadesCliente().size() - 1; i >= 0; i--) {
                            EntidadesClienteListaChequeo item = current.getEntidadesCliente().get(i);
                            if (item != null) {
                                item.deleteFromRealm();
                            }
                        }
                    }

                    if (current.getClientes() != null) {
                        for (int i = current.getClientes().size() - 1; i >= 0; i--) {
                            ClienteListaChequeo item = current.getClientes().get(i);
                            if (item != null) {
                                item.deleteFromRealm();
                            }
                        }
                    }

                    // Guarda las entidades de la orden de trabajo
                    if (current.getEntidades() != null && !current.getEntidades().isEmpty()) {
                        for (Entidad entidad : current.getEntidades()) {
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
                        current.getEntidades().deleteAllFromRealm();
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

                    current.setEntidades(entidades);

                    RealmList<ClienteListaChequeo> clientes = new RealmList<>();
                    for (ClienteListaChequeo clienteListaChequeo : value.getClientes()) {
                        clientes.add(self.copyToRealm(clienteListaChequeo));
                    }

                    RealmList<PersonalListaChequeo> personal = new RealmList<>();
                    for (PersonalListaChequeo personalListaChequeo : value.getPersonal()) {
                        personal.add(self.copyToRealm(personalListaChequeo));
                    }

                    RealmList<EntidadesClienteListaChequeo> entidadesCliente = new RealmList<>();
                    for (EntidadesClienteListaChequeo entidadesClienteListaChequeo : value.getEntidadesCliente()) {
                        entidadesCliente.add(self.copyToRealm(entidadesClienteListaChequeo));
                    }

                    current.setEntidades(entidades);
                    current.setClientes(clientes);
                    current.setPersonal(personal);
                    current.setEntidadesCliente(entidadesCliente);

                    results.add(current.isManaged() ? self.copyFromRealm(current) : current);
                }
            }
            subscriber.onNext(results);
        }, subscriber::onComplete, subscriber::onError));
    }

    public List<ListaChequeo> pagination(int page) {
        RealmResults<ListaChequeo> results = database.where(ListaChequeo.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .sort(new String[]{"codigo"}, new Sort[]{Sort.ASCENDING})
                .findAll();

        return database.pagination(results, page);
    }

    @Nullable
    public ListaChequeo getById(long id) {
        Database database = new Database(context);
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return null;
        }

        ListaChequeo result = database.where(ListaChequeo.class)
                .equalTo("id", id)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .findFirst();

        if (result == null) {
            return null;
        }

        ListaChequeo value = result.isManaged() ? database.copyFromRealm(result) : result;
        database.close();
        return value;
    }

    public static class Builder extends Service {

        public Builder(@NonNull Context context) {
            super(context);
        }
    }
}
