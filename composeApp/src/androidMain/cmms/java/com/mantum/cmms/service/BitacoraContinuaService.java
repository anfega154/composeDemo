package com.mantum.cmms.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.BitacoraContinua;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Recorrido;

import java.util.Calendar;
import java.util.TimeZone;

import io.reactivex.Observable;
import io.realm.RealmResults;

public class BitacoraContinuaService {

    private static final String TAG = BitacoraContinuaService.class.getSimpleName();

    private final Tipo tipo;

    private final Context context;

    private final Database database;

    public enum Tipo {
        RUTA_TRABAJO, OT
    }

    public BitacoraContinuaService(@NonNull Context context, Tipo tipo) {
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
    public BitacoraContinua obtener(long idmodulo) {
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            Log.e(TAG, context.getString(R.string.error_authentication));
            return null;
        }

        return database.where(BitacoraContinua.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("tipo", tipo.name())
                .equalTo("idmodulo", idmodulo)
                .isNotNull("fechainicio")
                .isNull("fechafin")
                .findFirst();
    }

    @Nullable
    public BitacoraContinua obtenerPendiente(long idmodulo) {
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            Log.e(TAG, context.getString(R.string.error_authentication));
            return null;
        }

        return database.where(BitacoraContinua.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("tipo", tipo.name())
                .notEqualTo("idmodulo", idmodulo)
                .isNotNull("fechainicio")
                .isNull("fechafin")
                .findFirst();
    }

    public boolean existe(long idmodulo) {
        return obtener(idmodulo) != null;
    }

    public boolean pendientes(long idmodulo) {
        return obtenerPendiente(idmodulo) != null;
    }

    public boolean pendientes(@NonNull Tipo tipo) {
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            Log.e(TAG, context.getString(R.string.error_authentication));
            return true;
        }

        Recorrido recorrido = database.where(Recorrido.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("tipo", tipo.name())
                .isNotNull("fechainicio")
                .isNull("fechafin")
                .findFirst();

        return recorrido != null;
    }

    public void eliminar(long idmodulo) {
        database.executeTransaction(self -> {
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                Log.e(TAG, context.getString(R.string.error_authentication));
                return;
            }

            BitacoraContinua bitacoraContinua = database.where(BitacoraContinua.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .equalTo("tipo", tipo.name())
                    .equalTo("idmodulo", idmodulo)
                    .isNotNull("fechainicio")
                    .isNull("fechafin")
                    .findFirst();

            if (bitacoraContinua == null) {
                return;
            }

            bitacoraContinua.deleteFromRealm();
        });
    }

    public boolean eliminar() {
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            Log.e(TAG, context.getString(R.string.error_authentication));
            return true;
        }

        RealmResults<BitacoraContinua> bitacoraContinuas = database.where(BitacoraContinua.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("tipo", tipo.name())
                .isNotNull("fechainicio")
                .isNull("fechafin")
                .findAll();

        database.beginTransaction();
        bitacoraContinuas.deleteAllFromRealm();
        database.commitTransaction();
        return false;
    }

    public void terminar(long idmodulo, @Nullable String value) {
        Log.i(TAG, "Terminar");
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

            BitacoraContinua bitacoraContinua = self.where(BitacoraContinua.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .equalTo("tipo", tipo.name())
                    .equalTo("idmodulo", idmodulo)
                    .isNotNull("fechainicio")
                    .isNull("fechafin")
                    .findFirst();

            if (bitacoraContinua == null) {
                return;
            }

            bitacoraContinua.setFechafin(Calendar.getInstance(TimeZone.getDefault()).getTime());
            bitacoraContinua.setValue(value);
        });
    }

    public void procesando(long idmodulo, @Nullable String value) {
        database.executeTransaction(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                Log.e(TAG, context.getString(R.string.error_authentication));
                return;
            }

            BitacoraContinua bitacoraContinua = self.where(BitacoraContinua.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .equalTo("tipo", tipo.name())
                    .equalTo("idmodulo", idmodulo)
                    .isNotNull("fechainicio")
                    .isNull("fechafin")
                    .findFirst();

            if (bitacoraContinua == null) {
                return;
            }

            bitacoraContinua.setValue(value);
        });
    }

    public Observable<BitacoraContinua> iniciar(long idmodulo, @NonNull BitacoraContinua.Data data) {
        return Observable.create(subscriber -> database.executeTransactionAsync(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            BitacoraContinua recorrido = new BitacoraContinua();
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

    public void actualizar(long idmodulo, @NonNull BitacoraContinua.Data data) {
        database.executeTransaction(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                Log.e(TAG, context.getString(R.string.error_authentication));
                return;
            }

            BitacoraContinua bitacoraContinua = self.where(BitacoraContinua.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .equalTo("idmodulo", idmodulo)
                    .equalTo("tipo", tipo.name())
                    .isNotNull("fechainicio")
                    .isNull("fechafin")
                    .findFirst();

            if (bitacoraContinua == null) {
                return;
            }

            bitacoraContinua.setEstado(data.getEstado());
            bitacoraContinua.setValue(data.getValue());
        });
    }

    public void close() {
        if (!database.isClosed()) {
            database.close();
        }
    }
}
