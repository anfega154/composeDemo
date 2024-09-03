package com.mantum.cmms.service;

import android.content.Context;
import androidx.annotation.NonNull;

import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Actividad;
import com.mantum.cmms.entity.Busqueda;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Entidad;
import com.mantum.cmms.entity.Variable;
import com.mantum.cmms.net.ClientManager;
import com.mantum.component.http.MicroServices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import io.reactivex.Observable;
import io.realm.RealmList;
import okhttp3.OkHttpClient;

public class EntidadServices extends MicroServices {

    private final Database database;

    public EntidadServices(@NonNull Context context, @NonNull Cuenta cuenta) {
        super(context, cuenta.getServidor().getUrl(), cuenta.getToken(context), ClientManager.prepare(
                new OkHttpClient.Builder(), context
        ));
        this.database = new Database(context);
    }

    public Observable<List<Entidad>> save(@NonNull Busqueda busqueda) {
        Entidad entidad = new Entidad();
        entidad.setId(busqueda.getId());
        entidad.setCodigo(busqueda.getCode());
        entidad.setNombre(busqueda.getName());
        entidad.setTipo(busqueda.getType());
        entidad.setVariables(busqueda.getVariables());

        List<Entidad> entidades = new ArrayList<>();
        entidades.add(entidad);
        return save(entidades);
    }

    public Observable<List<Entidad>> save(@NonNull Entidad entidad) {
        return save(Collections.singletonList(entidad));
    }

    public Observable<List<Entidad>> save(@NonNull List<Entidad> entidades) {
        return Observable.create(subscriber -> database.executeTransactionAsync(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            for (Entidad entidad : entidades) {
                Entidad busqueda = self.where(Entidad.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .equalTo("id", entidad.getId())
                        .equalTo("tipo", entidad.getTipo())
                        .findFirst();

                if (busqueda == null) {
                    entidad.setCuenta(cuenta);
                    self.insert(entidad);
                } else {
                    // Guardar las actividades de la entidad
                    if (busqueda.getActividades() != null && !busqueda.getActividades().isEmpty()) {
                        for (Actividad actividad : busqueda.getActividades()) {
                            actividad.getVariables().deleteAllFromRealm();
                            actividad.getAdjuntos().deleteAllFromRealm();
                            actividad.getImagenes().deleteAllFromRealm();
                        }
                        busqueda.getActividades().deleteAllFromRealm();
                    }

                    RealmList<Actividad> actividades = new RealmList<>();
                    for (Actividad actividad : entidad.getActividades()) {
                        actividad.setUuid(UUID.randomUUID().toString());
                        actividad.setCuenta(cuenta);
                        actividades.add(self.copyToRealm(actividad));
                    }
                    entidad.setActividades(actividades);

                    // Guarda las variables de la entidad
                    if (busqueda.getVariables() != null && !busqueda.getVariables().isEmpty()) {
                        for (Variable variable : busqueda.getVariables()) {
                            variable.getValores().deleteAllFromRealm();
                        }
                        busqueda.getVariables().deleteAllFromRealm();
                    }

                    RealmList<Variable> variables = new RealmList<>();
                    for (Variable variable : entidad.getVariables()) {
                        variables.add(self.copyToRealm(variable));
                    }
                    entidad.setVariables(variables);

                    entidad = entidad.isManaged() ? self.copyToRealm(entidad) : entidad;
                    entidades.add(entidad);
                }
            }

            subscriber.onNext(entidades);
        }, subscriber::onComplete, subscriber::onError));
    }

    public void close() {
        if (database != null) {
            database.close();
        }
    }
}