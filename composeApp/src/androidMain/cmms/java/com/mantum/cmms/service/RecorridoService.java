package com.mantum.cmms.service;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Recorrido;

import java.util.Calendar;
import java.util.TimeZone;

import io.reactivex.Observable;
import io.realm.RealmResults;

public class RecorridoService {

    private static final String TAG = RecorridoService.class.getSimpleName();

    private final Tipo tipo;

    private final Context context;

    private final Database database;

    public enum Tipo {
        RUTA_TRABAJO, OT
    }

    public RecorridoService(@NonNull Context context, Tipo tipo) {
        this.tipo = tipo;
        this.context = context;
        this.database = new Database(context);
    }

    @Nullable
    public Recorrido obtener() {
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            Log.e(TAG, context.getString(R.string.error_authentication));
            return null;
        }

        return database.where(Recorrido.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("tipo", tipo.name())
                .isNotNull("fechainicio")
                .isNull("fechafin")
                .findFirst();
    }

    @Nullable
    public Recorrido obtener(long idmodulo) {
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            Log.e(TAG, context.getString(R.string.error_authentication));
            return null;
        }

        return database.where(Recorrido.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("tipo", tipo.name())
                .equalTo("idmodulo", idmodulo)
                .isNotNull("fechainicio")
                .isNull("fechafin")
                .findFirst();
    }

    @Nullable
    public Recorrido obtenerPendiente(long idmodulo) {
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            Log.e(TAG, context.getString(R.string.error_authentication));
            return null;
        }

        return database.where(Recorrido.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("tipo", tipo.name())
                .notEqualTo("idmodulo", idmodulo)
                .isNotNull("fechainicio")
                .isNull("fechafin")
                .findFirst();
    }

    public void actualizarEstadoSincronizado(long idmodulo) {
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            Log.e(TAG, context.getString(R.string.error_authentication));
            return;
        }

        Recorrido recorrido = database.where(Recorrido.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("tipo", tipo.name())
                .equalTo("idmodulo", idmodulo)
                .findFirst();

        if(recorrido != null) {
            database.executeTransaction(self -> recorrido.setSincronizado(true));
        }
    }

    public boolean existe(long idmodulo) {
        return obtener(idmodulo) != null;
    }

    public boolean pendientes(long idmodulo) {
        return obtenerPendiente(idmodulo) != null;
    }

    public void eliminar() {
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            Log.e(TAG, context.getString(R.string.error_authentication));
            return;
        }

        RealmResults<Recorrido> recorrido = database.where(Recorrido.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("tipo", tipo.name())
                .isNotNull("fechainicio")
                .isNull("fechafin")
                .findAll();

        database.beginTransaction();
        recorrido.deleteAllFromRealm();
        database.commitTransaction();
    }

    public void terminar(long idmodulo, @Nullable String value) {
        if (!existe(idmodulo)) {
            return;
        }

        database.executeTransaction(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                Log.e(TAG, context.getString(R.string.error_authentication));
                return;
            }

            Recorrido recorrido = self.where(Recorrido.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .equalTo("tipo", tipo.name())
                    .equalTo("idmodulo", idmodulo)
                    .isNotNull("fechainicio")
                    .isNull("fechafin")
                    .findFirst();

            if (recorrido == null) {
                return;
            }

            recorrido.setFechafin(Calendar.getInstance(TimeZone.getDefault()).getTime());
            recorrido.setValue(value);
        });
    }

    public Observable<Recorrido> iniciar(long idmodulo, @NonNull Recorrido.Data data) {
        return Observable.create(subscriber -> database.executeTransactionAsync(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            Recorrido recorrido = new Recorrido();
            recorrido.setCuenta(cuenta);
            recorrido.setCodigo(data.getCodigo());
            recorrido.setIdmodulo(idmodulo);
            recorrido.setFechainicio(Calendar.getInstance(TimeZone.getDefault()).getTime());
            recorrido.setFechafin(null);
            recorrido.setFecharegistro(data.getFecharegistro());
            recorrido.setValue(data.getValue());
            recorrido.setTipo(tipo.name());
            recorrido.setEstado(data.getEstado());

            self.insert(recorrido);
            subscriber.onNext(recorrido);
        }, subscriber::onComplete, subscriber::onError));
    }

    public void actualizar(long idmodulo, @NonNull Recorrido.Data data) {
        database.executeTransaction(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                Log.e(TAG, context.getString(R.string.error_authentication));
                return;
            }

            Recorrido recorrido = self.where(Recorrido.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .equalTo("idmodulo", idmodulo)
                    .equalTo("tipo", tipo.name())
                    .isNotNull("fechainicio")
                    .isNull("fechafin")
                    .findFirst();

            if (recorrido == null) {
                return;
            }

            recorrido.setEstado(data.getEstado());
            recorrido.setValue(data.getValue());
            Log.i(TAG, "actualizar: " + idmodulo);
        });
    }

    @Nullable
    public Recorrido obtenerActual() {
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            Log.e(TAG, context.getString(R.string.error_authentication));
            return null;
        }

        return database.where(Recorrido.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("tipo", tipo.name())
                .isNotNull("fechainicio")
                .isNull("fechafin")
                .findFirst();
    }

    public void close() {
        if (!database.isClosed()) {
            database.close();
        }
    }
}
