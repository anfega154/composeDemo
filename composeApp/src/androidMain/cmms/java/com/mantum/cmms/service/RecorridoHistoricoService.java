package com.mantum.cmms.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;

import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.RecorridoHistorico;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.RealmResults;
import io.realm.Sort;

public class RecorridoHistoricoService {

    private static final String TAG = RecorridoService.class.getSimpleName();
    private static final String TAG_ESTADO = "Estado";

    private final Context context;

    private final Database database;

    public RecorridoHistoricoService(@NonNull Context context) {
        this.context = context;
        this.database = new Database(context);
    }

    public void save(
            Long identidad,
            String tipoentidad,
            String comentario,
            ATNotificationService.Estado estado,
            Long idcategoria,
            String categoria,
            boolean finalizanovedad
    ) {
        database.executeTransaction(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                Log.e(TAG, context.getString(R.string.error_authentication));
                return;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MILLISECOND, 0); // <-- OJO!
            Date now = calendar.getTime();

            RecorridoHistorico recorridoHistorico = new RecorridoHistorico();
            recorridoHistorico.setCuenta(cuenta);
            recorridoHistorico.setFecha(now);
            recorridoHistorico.setIdentidad(identidad);
            recorridoHistorico.setTipoentidad(tipoentidad);
            recorridoHistorico.setComentario(comentario);
            recorridoHistorico.setEstado(estado != null ? estado.getMostrar(context) : null);
            recorridoHistorico.setIdcategoria(idcategoria);
            recorridoHistorico.setCategoria(categoria);
            recorridoHistorico.setMostrar(false);
            recorridoHistorico.setFinalizanovedad(finalizanovedad);

            self.insertOrUpdate(recorridoHistorico);
        });
    }

    @Nullable
    public RecorridoHistorico getUltimoEstado() {
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return null;
        }

        return database.where(RecorridoHistorico.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .sort("fecha", Sort.DESCENDING)
                .findFirst();
    }

    @Nullable
    public RecorridoHistorico getUltimaNovedad() {
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return null;
        }

        RecorridoHistorico recorrido = database.where(RecorridoHistorico.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .beginGroup()
                .isNotNull("categoria").or().isNotNull("idcategoria")
                .endGroup()
                .sort("fecha", Sort.DESCENDING)
                .findFirst();

        Log.d(TAG_ESTADO, "Ultima novedad: ");
        return recorrido;
    }


    @Nullable
    public RecorridoHistorico find(long identidad) {
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return null;
        }

        return database.where(RecorridoHistorico.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("identidad", identidad)
                .beginGroup()
                .isNull("categoria").and().isNull("idcategoria")
                .endGroup()
                .sort("fecha", Sort.DESCENDING)
                .findFirst();
    }

    @Nullable
    public RecorridoHistorico findRecorridoIdNotNull(long identidad) {
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return null;
        }

        return database.where(RecorridoHistorico.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("identidad", identidad)
                .isNotNull("id")
                .isNull("categoria")
                .notEqualTo("comentario", context.getString(R.string.mensaje_finalizacion_novedad))
                .sort("fecha", Sort.DESCENDING)
                .findFirst();
    }

    @Nullable
    public RecorridoHistorico findNovedad(long identidad) {
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return null;
        }

        return database.where(RecorridoHistorico.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("identidad", identidad)
                .beginGroup()
                .isNotNull("categoria").or().isNotNull("idcategoria")
                .endGroup()
                .sort("fecha", Sort.DESCENDING)
                .findFirst();
    }

    @Nullable
    public RecorridoHistorico findNovedad() {
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return null;
        }

        return database.where(RecorridoHistorico.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .sort("fecha", Sort.DESCENDING)
                .findFirst();
    }

    public List<RecorridoHistorico> findAll(long identidad) {
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return new ArrayList<>();
        }

        RealmResults<RecorridoHistorico> elements = database.where(RecorridoHistorico.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("identidad", identidad)
                .equalTo("mostrar", true)
                .sort("fecha", Sort.DESCENDING)
                .findAll();

        return elements.isManaged()
                ? database.copyFromRealm(elements)
                : elements;
    }

    public void close() {
        database.close();
    }
}