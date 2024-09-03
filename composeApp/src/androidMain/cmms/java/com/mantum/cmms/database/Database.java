package com.mantum.cmms.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Case;
import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmModel;
import io.realm.RealmObjectSchema;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.RealmSchema;

public class Database {

    private Realm realm;

    public Database(@NonNull Context context) {
        try {
            this.realm = Realm.getDefaultInstance();
        } catch (Exception e) {
            Realm.init(context);
            Migration migration = new Migration();
            RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                    .name("mantum.realm")
                    .allowWritesOnUiThread(true)
                    .schemaVersion(234)
                    .migration(migration)
                    .build();

            Realm.setDefaultConfiguration(realmConfiguration);
            this.realm = Realm.getDefaultInstance();
        }
    }

    @Nullable
    public String getPath() {
        if (realm.getConfiguration() == null) {
            return null;
        }

        return realm.getConfiguration().getPath();
    }

    public <E extends RealmModel> List<E> pagination(List<E> realmResults, int page) {
        int total = realmResults.size();
        if (total == 0 || page < 0) {
            return new ArrayList<>();
        }

        int limit = 10;
        page = page == 0 || page == 1 ? 0 : page - 1;
        int init = page * limit;
        if (init >= total) {
            return new ArrayList<>();
        }

        limit = init + limit;
        limit = limit > total ? total : limit;
        return realmResults.subList(init, limit);
    }

    public <E extends RealmModel> List<E> pagination(RealmResults<E> realmResults, int page) {
        int total = realmResults.size();
        if (total == 0 || page < 0) {
            return new ArrayList<>();
        }

        int limit = 10;
        page = page == 0 || page == 1 ? 0 : page - 1;
        int init = page * limit;
        if (init >= total) {
            return new ArrayList<>();
        }

        limit = init + limit;
        limit = limit > total ? total : limit;
        return realmResults.subList(init, limit);
    }

    public void executeTransaction(Realm.Transaction transaction) {
        realm.executeTransaction(transaction);
    }

    public void executeTransactionAsync(Realm.Transaction transaction) {
        realm.executeTransactionAsync(transaction);
    }

    public void executeTransactionAsync(Realm.Transaction transaction, Realm.Transaction.OnSuccess onSuccess) {
        realm.executeTransactionAsync(transaction, onSuccess);
    }

    public void executeTransactionAsync(Realm.Transaction transaction, Realm.Transaction.OnSuccess onSuccess, Realm.Transaction.OnError onError) {
        realm.executeTransactionAsync(transaction, onSuccess, onError);
    }

    public <E extends RealmModel> RealmQuery<E> where(Class<E> clazz) {
        return realm.where(clazz);
    }

    public <E extends RealmModel> E copyFromRealm(E realmObject) {
        return realm.copyFromRealm(realmObject);
    }

    public <E extends RealmModel> List<E> copyFromRealm(Iterable<E> realmObjects) {
        return realm.copyFromRealm(realmObjects);
    }

    public void beginTransaction() {
        realm.beginTransaction();
    }

    public void commitTransaction() {
        realm.commitTransaction();
    }

    @SuppressWarnings("unused")
    public void cancelTransaction() {
        realm.cancelTransaction();
    }

    public void close() {
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }

    public boolean isClosed() {
        return realm.isClosed();
    }

    private static class Migration implements RealmMigration {

        @Override
        public void migrate(@NonNull DynamicRealm realm, long oldVersion, long newVersion) {
            RealmSchema schema = realm.getSchema();
            if (oldVersion == 1) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null) {
                    schema.create("Parametro")
                            .addField("UUID", String.class, FieldAttribute.PRIMARY_KEY)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("ejecutado", boolean.class);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 2) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null) {
                    RealmObjectSchema recurso = schema.get("Recurso");
                    if (recurso != null) {
                        recurso.addRealmObjectField("cuenta", cuenta);
                    }
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 3) {
                schema.create("Accion")
                        .addField("name", String.class);

                RealmObjectSchema busqueda = schema.get("Busqueda");
                if (busqueda != null) {
                    RealmObjectSchema accion = schema.get("Accion");
                    if (accion != null) {
                        busqueda.addRealmListField("actions", accion);
                    }
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 4) {
                realm.delete("Busqueda");
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 5) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null) {
                    schema.create("UltimoRegistroBitacora")
                            .addField("UUID", String.class, FieldAttribute.PRIMARY_KEY)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("horainicial", Long.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 6) {
                RealmObjectSchema ultimoRegistroBitacora = schema.get("UltimoRegistroBitacora");
                if (ultimoRegistroBitacora != null) {
                    ultimoRegistroBitacora.addField("fecha", String.class)
                            .addField("horafinal", Long.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 7) {
                RealmObjectSchema ordenTrabajo = schema.get("OrdenTrabajo");
                if (ordenTrabajo != null) {
                    ordenTrabajo.addField("realimentacion", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 8) {
                realm.delete("Busqueda");

                RealmObjectSchema evento = schema.get("Evento");
                if (evento != null) {
                    evento.addField("personal", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 9) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null) {
                    schema.create("Mochila")
                            .addField("idphrec", Long.class)
                            .addField("codigoph", String.class)
                            .addField("id", Long.class)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("tipo", String.class)
                            .addField("codigo", String.class)
                            .addField("nombre", String.class)
                            .addField("cantidad", String.class)
                            .addField("cantidaddisponible", String.class)
                            .addField("sigla", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 10) {
                RealmObjectSchema solicitudServicio = schema.get("SolicitudServicio");
                if (solicitudServicio != null) {
                    solicitudServicio.addField("show", boolean.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 11) {
                RealmObjectSchema ordenTrabajo = schema.get("OrdenTrabajo");
                if (ordenTrabajo != null) {
                    ordenTrabajo.addField("show", boolean.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 12) {
                schema.create("Version")
                        .addField("version", Integer.class);
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 13) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null) {
                    RealmObjectSchema version = schema.get("Version");
                    if (version != null) {
                        version.addRealmObjectField("cuenta", cuenta);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 14) {
                RealmObjectSchema evento = schema.get("Evento");
                if (evento != null) {
                    evento.addField("show", boolean.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 15) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null) {
                    schema.create("Pendiente")
                            .addField("UUID", String.class, FieldAttribute.PRIMARY_KEY)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("id", Long.class)
                            .addField("fecha", String.class)
                            .addField("codigo", String.class)
                            .addField("estado", String.class)
                            .addField("criticidad", String.class)
                            .addField("personal", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 16) {
                RealmObjectSchema pendiente = schema.get("Pendiente");
                if (pendiente != null) {
                    pendiente.addField("descripcion", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 17) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null) {
                    cuenta.removeField("token");
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 18) {
                RealmObjectSchema pendiente = schema.get("Pendiente");
                if (pendiente != null) {
                    pendiente.addField("color", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 19) {
                RealmObjectSchema ordenTrabajo = schema.get("OrdenTrabajo");
                if (ordenTrabajo != null) {
                    if (!ordenTrabajo.hasField("porcentaje")) {
                        ordenTrabajo.addField("porcentaje", String.class);
                    }

                    if (!ordenTrabajo.hasField("color")) {
                        ordenTrabajo.addField("color", String.class);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 20) {
                RealmObjectSchema evento = schema.get("Evento");
                if (evento != null && !evento.hasField("color")) {
                    evento.addField("color", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 21) {
                schema.create("Activos")
                        .addField("id", Long.class)
                        .addField("idequipo", Long.class)
                        .addField("equipo", String.class)
                        .addField("entidadenvio", String.class)
                        .addField("responsable", String.class);

                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null) {
                    RealmObjectSchema activos = schema.get("Activos");
                    if (activos != null) {
                        schema.create("Transferencia")
                                .addRealmObjectField("cuenta", cuenta)
                                .addField("id", Long.class)
                                .addField("fecha", String.class)
                                .addField("codigo", String.class)
                                .addField("personal", String.class)
                                .addRealmListField("activos", activos);
                    }
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 22) {
                RealmObjectSchema transferencia = schema.get("Transferencia");
                if (transferencia != null && !transferencia.hasField("UUID")) {
                    transferencia.addField("UUID", String.class, FieldAttribute.PRIMARY_KEY);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 23) {
                if (schema.contains("Ubicacion")) {
                    schema.remove("Ubicacion");
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 24) {
                RealmObjectSchema ordenTrabajo = schema.get("OrdenTrabajo");
                if (ordenTrabajo != null && ordenTrabajo.hasField("show")) {
                    ordenTrabajo.removeField("show");
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 25) {
                RealmObjectSchema evento = schema.get("Evento");
                if (evento != null && evento.hasField("show")) {
                    evento.removeField("show");
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 26) {
                schema.create("Ejecutores")
                        .addField("id", Long.class)
                        .addField("codigo", String.class)
                        .addField("nombre", String.class)
                        .addField("tiempo", Long.class)
                        .addField("costo", String.class);

                RealmObjectSchema ejecutores = schema.get("Ejecutores");
                if (ejecutores != null) {
                    RealmObjectSchema ordenTrabajo = schema.get("OrdenTrabajo");
                    if (ordenTrabajo != null && !ordenTrabajo.hasField("ejecutores")) {
                        ordenTrabajo.addRealmListField("ejecutores", ejecutores);
                    }
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 27) {
                RealmObjectSchema busqueda = schema.get("Busqueda");
                if (busqueda != null && !busqueda.hasField("detalle")) {
                    busqueda.addField("detalle", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 28) {
                RealmObjectSchema recurso = schema.get("Recurso");
                if (recurso != null && !recurso.hasField("cantidadreal")) {
                    recurso.addField("cantidadreal", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 29) {
                RealmObjectSchema transaccion = schema.get("Transaccion");
                if (transaccion != null && transaccion.hasField("endpoint")) {
                    transaccion.removeField("endpoint");
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 30) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null) {
                    schema.create("UserPermission")
                            .addField("UUID", String.class, FieldAttribute.PRIMARY_KEY)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("name", String.class)
                            .addField("value", Boolean.class);
                }


                RealmObjectSchema adjuntos = schema.create("Adjuntos")
                        .addField("idfile", Long.class)
                        .addField("path", String.class);

                RealmObjectSchema ordenTrabajo = schema.get("OrdenTrabajo");
                if (ordenTrabajo != null && !ordenTrabajo.hasField("adjuntos")) {
                    ordenTrabajo.addRealmListField("adjuntos", adjuntos);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 31) {
                RealmObjectSchema ordenTrabajo = schema.get("OrdenTrabajo");
                if (ordenTrabajo != null && !ordenTrabajo.hasField("variables")) {
                    RealmObjectSchema variable = schema.get("Variable");
                    if (variable != null) {
                        ordenTrabajo.addRealmListField("variables", variable);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 32) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null) {
                    schema.create("UserParameter")
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("name", String.class)
                            .addField("value", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 33) {
                RealmObjectSchema solicitudServicio = schema.get("SolicitudServicio");
                if (solicitudServicio != null) {
                    RealmObjectSchema adjuntos = schema.get("Adjuntos");
                    if (adjuntos != null) {
                        solicitudServicio.addRealmListField("adjuntos", adjuntos);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 34) {
                RealmObjectSchema proceso = schema.get("Proceso");
                if (proceso != null && !proceso.hasField("UUID")) {
                    proceso.addField("UUID", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 35) {
                RealmObjectSchema pendiente = schema.get("Pendiente");
                if (pendiente != null) {
                    RealmObjectSchema adjuntos = schema.get("Adjuntos");
                    if (adjuntos != null) {
                        pendiente.addRealmListField("adjuntos", adjuntos);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 36) {
                RealmObjectSchema adjuntos = schema.get("Adjuntos");
                if (adjuntos != null) {
                    adjuntos.addField("external", boolean.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 37) {
                realm.delete("Variable");
                realm.delete("Accion");
                realm.delete("VariableCualitativa");
                realm.delete("DetalleBusqueda");
                realm.delete("UltimaLecturaVariable");
                realm.delete("Busqueda");

                RealmObjectSchema accion = schema.get("Accion");
                if (accion != null) {
                    accion.addPrimaryKey("name");
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 38) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null) {
                    schema.create("EstadoInicial")
                            .addField("uuid", String.class, FieldAttribute.PRIMARY_KEY)
                            .addField("idss", Long.class)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("tipofalla", String.class)
                            .addField("denominacion", String.class)
                            .addField("numeroproducto", String.class)
                            .addField("numeroserial", String.class)
                            .addField("caracteristicas", String.class)
                            .addField("estado", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 39) {
                RealmObjectSchema estadoInicial = schema.get("EstadoInicial");
                if (estadoInicial != null) {
                    estadoInicial.addField("codigo", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 40) {
                schema.create("PendienteMantenimiento")
                        .addField("uuid", String.class, FieldAttribute.PRIMARY_KEY)
                        .addField("idss", Long.class)
                        .addField("codigo", String.class)
                        .addField("tiempoestimado", Float.class)
                        .addField("actividad", String.class)
                        .addField("descripcion", String.class);
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 41) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null) {
                    schema.create("InformeTecnico")
                            .addField("uuid", String.class, FieldAttribute.PRIMARY_KEY)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("idss", Long.class)
                            .addField("codigo", String.class)
                            .addField("actividades", String.class)
                            .addField("recomendaciones", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 42) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null) {
                    schema.create("RecursoAdicional")
                            .addField("uuid", String.class, FieldAttribute.PRIMARY_KEY)
                            .addField("id", Long.class)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("nombre", String.class)
                            .addField("cantidad", String.class)
                            .addField("unidad", String.class)
                            .addField("referencia", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 43) {
                RealmObjectSchema recursoAdicional = schema.get("RecursoAdicional");
                if (recursoAdicional != null) {
                    recursoAdicional.addField("utilizado", boolean.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 44) {
                RealmObjectSchema solicitudServicio = schema.get("SolicitudServicio");
                if (solicitudServicio != null) {
                    RealmObjectSchema recursoAdicional = schema.get("RecursoAdicional");
                    if (recursoAdicional != null) {
                        solicitudServicio.addRealmListField("recursosadicionales", recursoAdicional);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 45) {
                RealmObjectSchema solicitudServicio = schema.get("SolicitudServicio");
                if (solicitudServicio != null) {
                    RealmObjectSchema informeTecnico = schema.get("InformeTecnico");
                    if (informeTecnico != null) {
                        solicitudServicio.addRealmObjectField("informeTecnico", informeTecnico);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 46) {
                RealmObjectSchema solicitudServicio = schema.get("SolicitudServicio");
                if (solicitudServicio != null) {
                    RealmObjectSchema estadoInicial = schema.get("EstadoInicial");
                    if (estadoInicial != null) {
                        solicitudServicio.addRealmObjectField("estadoInicial", estadoInicial);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 47) {
                RealmObjectSchema estadoInicial = schema.get("EstadoInicial");
                if (estadoInicial != null) {
                    estadoInicial.addField("marca", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 48) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null) {
                    RealmObjectSchema sitio = schema.create("Sitio")
                            .addField("uuid", String.class, FieldAttribute.PRIMARY_KEY)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("telefono", String.class)
                            .addField("planta", String.class)
                            .addField("direccion", String.class)
                            .addField("cliente", String.class);

                    RealmObjectSchema solicitudServicio = schema.get("SolicitudServicio");
                    if (solicitudServicio != null) {
                        solicitudServicio.addRealmObjectField("sitio", sitio);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 49) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null) {
                    schema.create("TipoTiempo")
                            .addField("uuid", String.class, FieldAttribute.PRIMARY_KEY)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("id", Long.class)
                            .addField("nombre", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 50) {
                RealmObjectSchema logBook = schema.get("LogBook");
                if (logBook != null) {
                    RealmObjectSchema tipoTiempo = schema.get("TipoTiempo");
                    if (tipoTiempo != null) {
                        logBook.addRealmListField("tiempos", tipoTiempo);
                    }
                }

                realm.delete("EventType");
                realm.delete("LogBook");
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 51) {
                RealmObjectSchema pendiente = schema.get("Pendiente");
                if (pendiente != null) {
                    pendiente.addField("actividadpmtto", String.class);
                    pendiente.addField("tiempoestimadopmtto", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 52) {
                RealmObjectSchema pendienteMantenimiento = schema.get("PendienteMantenimiento");
                if (pendienteMantenimiento != null) {
                    if (pendienteMantenimiento.hasField("idss")) {
                        pendienteMantenimiento.removeField("idss");
                    }

                    if (pendienteMantenimiento.hasField("codigo")) {
                        pendienteMantenimiento.removeField("codigo");
                    }

                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 53) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null) {
                    RealmObjectSchema personal = schema.create("Personal")
                            .addField("uuid", String.class, FieldAttribute.PRIMARY_KEY)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("cedula", String.class)
                            .addField("nombre", String.class);

                    schema.create("Autorizaciones")
                            .addField("uuid", String.class, FieldAttribute.PRIMARY_KEY)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("id", Long.class)
                            .addField("codigo", String.class)
                            .addField("fechainicio", Date.class)
                            .addField("fechafin", Date.class)
                            .addField("tipo", String.class)
                            .addField("locacion", String.class)
                            .addField("empresa", String.class)
                            .addField("descripcion", String.class)
                            .addField("marca", String.class)
                            .addField("modulo", String.class)
                            .addRealmListField("personal", personal);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 54) {
                RealmObjectSchema busqueda = schema.get("Busqueda");
                if (busqueda != null) {
                    busqueda.addField("gmap", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 55) {
                RealmObjectSchema solicitudServicio = schema.get("SolicitudServicio");
                if (solicitudServicio != null) {
                    solicitudServicio.addField("gmap", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 56) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null) {
                    schema.create("Categoria")
                            .addField("uuid", String.class, FieldAttribute.PRIMARY_KEY)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("id", Long.class)
                            .addField("nombre", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 57) {
                RealmObjectSchema mochila = schema.get("Mochila");
                if (mochila != null) {
                    mochila.addField("ubicacion", String.class);
                    mochila.addField("observaciones", String.class);
                }

                RealmObjectSchema recurso = schema.get("Recurso");
                if (recurso != null) {
                    recurso.addField("ubicacion", String.class);
                    recurso.addField("observaciones", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 58) {
                RealmObjectSchema sitio = schema.get("Sitio");
                if (sitio != null) {
                    sitio.addField("codigo", String.class)
                            .addField("nombre", String.class)
                            .addField("departamento", String.class)
                            .addField("pais", String.class)
                            .addField("ciudad", String.class)
                            .addField("tipoenlace", String.class)
                            .addField("contacto", String.class)
                            .addField("cargo", String.class)
                            .addField("ingenieroresponsable", String.class);

                    RealmObjectSchema ordenTrabajo = schema.get("OrdenTrabajo");
                    if (ordenTrabajo != null) {
                        ordenTrabajo.addRealmObjectField("sitio", sitio);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 59) {
                RealmObjectSchema adjuntos = schema.get("Adjuntos");
                if (adjuntos != null) {
                    RealmObjectSchema actividad = schema.get("Actividad");
                    if (actividad != null) {
                        if (!actividad.hasField("imagenes")) {
                            actividad.addRealmListField("imagenes", adjuntos);
                        }
                    }

                    RealmObjectSchema ordenTrabajo = schema.get("OrdenTrabajo");
                    if (ordenTrabajo != null) {
                        if (!ordenTrabajo.hasField("imagenes")) {
                            ordenTrabajo.addRealmListField("imagenes", adjuntos);
                        }
                    }

                    RealmObjectSchema rutaTrabajo = schema.get("RutaTrabajo");
                    if (rutaTrabajo != null) {
                        if (!rutaTrabajo.hasField("imagenes")) {
                            rutaTrabajo.addRealmListField("imagenes", adjuntos);
                        }

                        if (!rutaTrabajo.hasField("adjuntos")) {
                            rutaTrabajo.addRealmListField("adjuntos", adjuntos);
                        }
                    }

                    RealmObjectSchema solicitudServicio = schema.get("SolicitudServicio");
                    if (solicitudServicio != null) {
                        if (!solicitudServicio.hasField("imagenes")) {
                            solicitudServicio.addRealmListField("imagenes", adjuntos);
                        }
                    }
                }

                RealmObjectSchema actividad = schema.get("Actividad");
                if (actividad != null) {
                    actividad.addField("descripcion", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 60) {
                RealmObjectSchema servidor = schema.get("Servidor");
                if (servidor != null) {
                    servidor.addField("version", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 61) {
                realm.delete("Variable");
                realm.delete("Accion");
                realm.delete("VariableCualitativa");
                realm.delete("DetalleBusqueda");
                realm.delete("UltimaLecturaVariable");
                realm.delete("Busqueda");
                realm.delete("Actividad");
                realm.delete("Entidad");

                RealmObjectSchema actividad = schema.get("Actividad");
                if (actividad != null) {
                    RealmObjectSchema cuenta = schema.get("Cuenta");
                    if (cuenta != null) {
                        actividad.addRealmObjectField("cuenta", cuenta);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 62) {
                realm.delete("OrdenTrabajo");
                realm.delete("SolicitudServicio");
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 63) {
                realm.delete("RutaTrabajo");
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 64) {
                RealmObjectSchema actividad = schema.get("Actividad");
                if (actividad != null) {
                    actividad.addField("requisitos", String.class);
                    RealmObjectSchema adjuntos = schema.get("Adjuntos");
                    if (adjuntos != null && !actividad.hasField("imagenes")) {
                        actividad.addRealmListField("imagenes", adjuntos);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 65) {
                RealmObjectSchema actividad = schema.get("Actividad");
                if (actividad != null) {
                    RealmObjectSchema adjuntos = schema.get("Adjuntos");
                    if (adjuntos != null && !actividad.hasField("adjuntos")) {
                        actividad.addRealmListField("adjuntos", adjuntos);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 66) {
                RealmObjectSchema adjuntos = schema.get("Adjuntos");
                if (adjuntos != null) {
                    adjuntos.addField("text", String.class)
                            .addField("type", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 67 || oldVersion == 68) {
                RealmObjectSchema adjuntos = schema.get("Adjuntos");
                if (adjuntos != null) {
                    RealmObjectSchema actividad = schema.get("Actividad");
                    if (actividad != null) {
                        if (!actividad.hasField("imagenes")) {
                            actividad.addRealmListField("imagenes", adjuntos);
                        }
                    }

                    RealmObjectSchema ordenTrabajo = schema.get("OrdenTrabajo");
                    if (ordenTrabajo != null) {
                        if (!ordenTrabajo.hasField("imagenes")) {
                            ordenTrabajo.addRealmListField("imagenes", adjuntos);
                        }
                    }

                    RealmObjectSchema rutaTrabajo = schema.get("RutaTrabajo");
                    if (rutaTrabajo != null) {
                        if (!rutaTrabajo.hasField("imagenes")) {
                            rutaTrabajo.addRealmListField("imagenes", adjuntos);
                        }

                        if (!rutaTrabajo.hasField("adjuntos")) {
                            rutaTrabajo.addRealmListField("adjuntos", adjuntos);
                        }
                    }

                    RealmObjectSchema solicitudServicio = schema.get("SolicitudServicio");
                    if (solicitudServicio != null) {
                        if (!solicitudServicio.hasField("imagenes")) {
                            solicitudServicio.addRealmListField("imagenes", adjuntos);
                        }

                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 69 || oldVersion == 70) {
                RealmObjectSchema logBook = schema.get("LogBook");
                if (logBook != null && !logBook.hasField("turnosmanualesbitacora")) {
                    logBook.addField("turnosmanualesbitacora", boolean.class);
                }

                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null && !cuenta.hasField("calendario")) {
                    cuenta.addField("idCalendario", Long.class);
                }

                RealmObjectSchema entidad = schema.get("Entidad");
                if (entidad != null) {
                    if (!entidad.hasField("cuenta")) {
                        if (cuenta != null) {
                            entidad.addRealmObjectField("cuenta", cuenta);
                        }
                    }

                    RealmObjectSchema variable = schema.get("Variable");
                    if (variable != null) {
                        entidad.addRealmListField("variables", variable);
                    }
                }

                RealmObjectSchema busqueda = schema.get("Busqueda");
                if (busqueda != null) {
                    busqueda.removeField("timestamp");
                    if (!busqueda.hasField("actividades")) {
                        RealmObjectSchema actividades = schema.get("Actividad");
                        if (actividades != null) {
                            busqueda.addRealmListField("actividades", actividades);
                        }
                    }

                    if (!busqueda.hasField("mostrar")) {
                        busqueda.addField("mostrar", boolean.class);
                    }
                }

                RealmObjectSchema variable = schema.get("Variable");
                if (variable != null) {
                    if (!variable.hasField("descripcion")) {
                        variable.addField("descripcion", String.class);
                    }

                    if (!variable.hasField("identidad")) {
                        variable.addField("identidad", Long.class);
                    }

                    if (!variable.hasField("tipoentidad")) {
                        variable.addField("tipoentidad", String.class);
                    }

                    if (!variable.hasField("observacion")) {
                        variable.addField("observacion", String.class);
                    }
                }

                RealmObjectSchema calendario = schema.get("Calendario");
                if (cuenta != null && calendario == null) {
                    calendario = schema.create("Calendario")
                            .addField("uuid", String.class, FieldAttribute.PRIMARY_KEY)
                            .addField("id", Long.class)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("start", String.class)
                            .addField("end", String.class)
                            .addField("titulo", String.class)
                            .addField("descripcion", String.class)
                            .addField("tipo", String.class)
                            .addField("idCalendario", Long.class);

                    if (calendario != null && !calendario.hasField("color")) {
                        calendario.addField("color", String.class);
                    }

                    if (calendario != null && !calendario.hasField("diaCompleto")) {
                        calendario.addField("diaCompleto", boolean.class);
                    }
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 71) {
                RealmObjectSchema busqueda = schema.get("Busqueda");
                if (busqueda != null) {
                    if (!busqueda.hasField("actividades")) {
                        RealmObjectSchema actividades = schema.get("Actividad");
                        if (actividades != null) {
                            busqueda.addRealmListField("actividades", actividades);
                        }
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 72) {
                RealmObjectSchema variable = schema.get("Variable");
                if (variable != null) {
                    if (!variable.hasField("descripcion")) {
                        variable.addField("descripcion", String.class);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 73) {
                RealmObjectSchema busqueda = schema.get("Busqueda");
                if (busqueda != null) {
                    if (!busqueda.hasField("mostrar")) {
                        busqueda.addField("mostrar", boolean.class);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 74) {
                RealmObjectSchema variable = schema.get("Variable");
                if (variable != null) {
                    if (!variable.hasField("identidad")) {
                        variable.addField("identidad", Long.class);
                    }

                    if (!variable.hasField("tipoentidad")) {
                        variable.addField("tipoentidad", String.class);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 75) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null && !cuenta.hasField("calendario")) {
                    cuenta.addField("idCalendario", Long.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 76) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                RealmObjectSchema calendario = schema.get("Calendario");
                if (cuenta != null && calendario == null) {
                    schema.create("Calendario")
                            .addField("uuid", String.class, FieldAttribute.PRIMARY_KEY)
                            .addField("id", Long.class)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("start", String.class)
                            .addField("end", String.class)
                            .addField("titulo", String.class)
                            .addField("descripcion", String.class)
                            .addField("tipo", String.class)
                            .addField("idCalendario", Long.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 77) {
                RealmObjectSchema calendario = schema.get("Calendario");
                if (calendario != null && !calendario.hasField("diaCompleto")) {
                    calendario.addField("diaCompleto", boolean.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 78) {
                RealmObjectSchema calendario = schema.get("Calendario");
                if (calendario != null && !calendario.hasField("color")) {
                    calendario.addField("color", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 79) {
                RealmObjectSchema logBook = schema.get("LogBook");
                if (logBook != null && !logBook.hasField("turnosmanualesbitacora")) {
                    logBook.addField("turnosmanualesbitacora", boolean.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 80) {
                RealmObjectSchema variable = schema.get("Variable");
                if (variable != null && !variable.hasField("observacion")) {
                    variable.addField("observacion", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 81) {
                realm.delete("RutaTrabajo");
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 82) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null) {
                    RealmObjectSchema equipo = schema.create("Equipo")
                            .addField("uuid", String.class, FieldAttribute.PRIMARY_KEY)
                            .addField("id", Long.class)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("codigo", String.class)
                            .addField("nombre", String.class)
                            .addField("instalacionproceso", String.class)
                            .addField("instalacionlocativa", String.class)
                            .addField("familia1", String.class)
                            .addField("familia2", String.class)
                            .addField("familia3", String.class)
                            .addField("provocaparo", String.class)
                            .addField("ubicacion", String.class)
                            .addField("observaciones", String.class);

                    RealmObjectSchema adjuntos = schema.get("Adjuntos");
                    if (adjuntos != null) {
                        equipo.addRealmListField("adjuntos", adjuntos);
                        equipo.addRealmListField("imagenes", adjuntos);
                    }

                    RealmObjectSchema variable = schema.get("Variable");
                    if (variable != null) {
                        equipo.addRealmListField("variables", variable);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 83) {
                RealmObjectSchema equipo = schema.get("Equipo");
                if (equipo != null) {
                    equipo.addField("fabricante", String.class);
                    equipo.addField("pais", String.class);
                    equipo.addField("fechafabricacion", String.class);
                    equipo.addField("nroserie", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 84) {
                RealmObjectSchema equipo = schema.get("Equipo");
                if (equipo != null) {
                    RealmObjectSchema actividad = schema.get("Actividad");
                    if (actividad != null) {
                        equipo.addRealmListField("actividades", actividad);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 85) {
                RealmObjectSchema equipo = schema.get("Equipo");
                if (equipo != null) {
                    equipo.addField("gmap", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 86) {
                if (schema.get("InformacionTecnica") == null) {
                    schema.create("InformacionTecnica")
                            .addField("fabricante", String.class)
                            .addField("pais", String.class)
                            .addField("fechafabricacion", String.class)
                            .addField("modelo", String.class)
                            .addField("nroserie", String.class);
                }

                RealmObjectSchema equipo = schema.get("Equipo");
                if (equipo != null) {
                    if (equipo.hasField("fabricante")) {
                        equipo.removeField("fabricante");
                    }

                    if (equipo.hasField("pais")) {
                        equipo.removeField("pais");
                    }

                    if (equipo.hasField("fechafabricacion")) {
                        equipo.removeField("fechafabricacion");
                    }

                    if (equipo.hasField("modelo")) {
                        equipo.removeField("modelo");
                    }

                    if (equipo.hasField("nroserie")) {
                        equipo.removeField("nroserie");
                    }

                    if (!equipo.hasField("informacionTecnica")) {
                        RealmObjectSchema informacionTecnica = schema.get("InformacionTecnica");
                        if (informacionTecnica != null) {
                            equipo.addRealmObjectField("informacionTecnica", informacionTecnica);
                        }
                    }
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 87) {
                RealmObjectSchema datostecnicos = schema.create("DatosTecnico")
                        .addField("nombre", String.class)
                        .addField("idtipodatotecnico", String.class)
                        .addField("otronombre", String.class)
                        .addField("valor", String.class)
                        .addField("tolerancia", String.class)
                        .addField("descripcion", String.class);

                RealmObjectSchema equipo = schema.get("Equipo");
                if (equipo != null && !equipo.hasField("datostecnicos")) {
                    equipo.addRealmListField("datostecnicos", datostecnicos);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 88) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null) {
                    RealmObjectSchema instalacionLocativa = schema.create("InstalacionLocativa")
                            .addField("uuid", String.class, FieldAttribute.PRIMARY_KEY)
                            .addField("id", Long.class)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("codigo", String.class)
                            .addField("nombre", String.class)
                            .addField("instalacionpadre", String.class)
                            .addField("tipodeinstalacion", String.class)
                            .addField("estado", String.class)
                            .addField("criticidad", String.class)
                            .addField("familia1", String.class)
                            .addField("familia2", String.class)
                            .addField("familia3", String.class)
                            .addField("direccion", String.class)
                            .addField("gmap", String.class);

                    RealmObjectSchema adjuntos = schema.get("Adjuntos");
                    if (adjuntos != null) {
                        instalacionLocativa.addRealmListField("adjuntos", adjuntos);
                        instalacionLocativa.addRealmListField("imagenes", adjuntos);
                    }

                    RealmObjectSchema variable = schema.get("Variable");
                    if (variable != null) {
                        instalacionLocativa.addRealmListField("variables", variable);
                    }

                    RealmObjectSchema datostecnicos = schema.get("DatosTecnico");
                    if (datostecnicos != null) {
                        instalacionLocativa.addRealmListField("datostecnicos", datostecnicos);
                    }

                    RealmObjectSchema actividad = schema.get("Actividad");
                    if (actividad != null) {
                        instalacionLocativa.addRealmListField("actividades", actividad);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 89) {
                RealmObjectSchema ordenTrabajo = schema.get("OrdenTrabajo");
                if (ordenTrabajo != null) {
                    RealmObjectSchema equipo = schema.get("Equipo");
                    if (equipo != null && !equipo.hasField("ordenTrabajos")) {
                        equipo.addRealmListField("ordenTrabajos", ordenTrabajo);
                    }

                    RealmObjectSchema instalacionLocativa = schema.get("InstalacionLocativa");
                    if (instalacionLocativa != null && !instalacionLocativa.hasField("ordenTrabajos")) {
                        instalacionLocativa.addRealmListField("ordenTrabajos", ordenTrabajo);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 90) {
                RealmObjectSchema ordenTrabajo = schema.get("OrdenTrabajo");
                if (ordenTrabajo != null) {
                    RealmObjectSchema busqueda = schema.get("Busqueda");
                    if (busqueda != null && !busqueda.hasField("historicoOT")) {
                        busqueda.addRealmListField("historicoOT", ordenTrabajo);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 91) {
                RealmObjectSchema actividad = schema.get("Actividad");
                if (actividad != null && !actividad.hasField("orden")) {
                    actividad.addField("orden", int.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 92) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null) {
                    schema.create("Recorrido")
                            .addField("uuid", String.class, FieldAttribute.PRIMARY_KEY)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("idmodulo", Long.class)
                            .addField("fachainicio", Date.class)
                            .addField("fechafin", Date.class)
                            .addField("value", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 93) {
                RealmObjectSchema recorrido = schema.get("Recorrido");
                if (recorrido != null && !recorrido.hasField("tipo")) {
                    recorrido.addField("tipo", String.class);
                }

                RealmObjectSchema categoria = schema.get("Categoria");
                if (categoria != null) {
                    if (!categoria.hasField("tipo"))
                        categoria.addField("tipo", String.class);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 94) {
                RealmObjectSchema variable = schema.get("Variable");
                if (variable != null && !variable.hasField("idActividad")) {
                    variable.addField("idActividad", Long.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 95) {
                RealmObjectSchema recorrido = schema.get("Recorrido");
                if (recorrido != null && !recorrido.hasField("codigo")) {
                    recorrido.addField("codigo", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 96) {
                RealmObjectSchema recorrido = schema.get("Recorrido");
                if (recorrido != null) {
                    recorrido.renameField("fachainicio", "fechainicio");
                    recorrido.addField("fecharegistro", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 97) {
                RealmObjectSchema ordenTrabajo = schema.get("OrdenTrabajo");
                if (ordenTrabajo != null) {
                    ordenTrabajo.addField("duracion", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 98) {
                RealmObjectSchema actividad = schema.get("Actividad");
                if (actividad != null) {
                    actividad.addField("duracion", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 99) {
                RealmObjectSchema busqueda = schema.get("Busqueda");
                if (busqueda != null) {
                    busqueda.addField("referencia", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 100) {
                RealmObjectSchema ejecutores = schema.get("Ejecutores");
                if (ejecutores != null && ejecutores.hasField("tiempo")) {
                    ejecutores.removeField("tiempo");
                    ejecutores.addField("tiempo", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 101) {
                RealmObjectSchema transaccion = schema.get("Transaccion");
                if (transaccion != null) {
                    transaccion.addField("fecharespuesta", Date.class);
                    transaccion.addField("respuesta", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 102) {
                RealmObjectSchema variable = schema.get("Variable");
                if (variable != null) {
                    variable.addField("orden", int.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 103) {
                RealmObjectSchema rutaTrabajo = schema.get("RutaTrabajo");
                RealmObjectSchema entity = schema.create("Entity")
                        .addField("id", Long.class);

                if (rutaTrabajo != null) {
                    rutaTrabajo.addRealmListField("amxgrupos", entity);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 104) {
                RealmObjectSchema rutaTrabajo = schema.get("RutaTrabajo");
                if (rutaTrabajo != null) {
                    rutaTrabajo.removeField("amxgrupos");
                }

                RealmObjectSchema actividad = schema.get("Actividad");
                if (actividad != null) {
                    RealmObjectSchema entity = schema.get("Entity");
                    if (entity != null) {
                        actividad.addRealmListField("amxgrupos", entity);
                    }
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 105) {
                RealmObjectSchema actividad = schema.get("Actividad");
                if (actividad != null) {
                    actividad.removeField("amxgrupos");
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 106) {
                RealmObjectSchema rutaTrabajo = schema.get("RutaTrabajo");
                if (rutaTrabajo != null) {
                    RealmObjectSchema entity = schema.get("Entity");
                    if (entity != null) {
                        rutaTrabajo.addRealmListField("amxgrupos", entity);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 107) {
                if (!schema.contains("Cliente")) {
                    schema.create("Cliente")
                            .addField("id", Long.class)
                            .addField("cedula", String.class)
                            .addField("nombre", String.class);
                }

                if (!schema.contains("Proveedor")) {
                    schema.create("Proveedor")
                            .addField("id", Long.class)
                            .addField("cedula", String.class)
                            .addField("nombre", String.class);
                }

                RealmObjectSchema categoria = schema.get("Categoria");
                if (categoria != null) {
                    categoria.addField("tipo", String.class);
                }

                RealmObjectSchema personal = schema.get("Personal");
                if (personal != null) {
                    personal.addField("id", Long.class);
                }

                RealmObjectSchema ordenTrabajo = schema.get("OrdenTrabajo");
                if (ordenTrabajo != null) {
                    ordenTrabajo.addField("movimiento", boolean.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 108) {
                RealmObjectSchema sitio = schema.get("Sitio");
                if (sitio != null) {

                    if (!sitio.hasField("latitud")) {
                        sitio.addField("latitud", String.class);
                    }

                    if (!sitio.hasField("longitud")) {
                        sitio.addField("longitud", String.class);
                    }
                }

                RealmObjectSchema ultimoRegistroBitacora = schema.get("UltimoRegistroBitacora");
                if (ultimoRegistroBitacora != null) {
                    if (ultimoRegistroBitacora.hasField("estadoEquipo")) {
                        ultimoRegistroBitacora.removeField("estadoEquipo");
                    }
                }

                RealmObjectSchema ordenTrabajo = schema.get("OrdenTrabajo");
                if (ordenTrabajo != null) {
                    if (!ordenTrabajo.hasField("asignada")) {
                        ordenTrabajo.addField("asignada", boolean.class);
                    }

                    if (!ordenTrabajo.hasField("orden")) {
                        ordenTrabajo.addField("orden", int.class);
                    }

                    if (!ordenTrabajo.hasField("movimiento")) {
                        ordenTrabajo.addField("movimiento", boolean.class);
                    }
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 109) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                RealmObjectSchema ordenTrabajo = schema.get("OrdenTrabajo");
                if (cuenta != null && ordenTrabajo != null) {
                    schema.create("Asignada")
                            .addRealmObjectField("cuenta", cuenta)
                            .addRealmObjectField("ordenTrabajo", ordenTrabajo)
                            .addField("orden", int.class);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 110) {
                RealmObjectSchema recorrido = schema.get("Recorrido");
                if (recorrido != null && !recorrido.hasField("estado")) {
                    recorrido.addField("estado", String.class);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 111) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null) {
                    schema.create("RecorridoHistorico")
                            .addField("uuid", String.class, FieldAttribute.PRIMARY_KEY)
                            .addRealmObjectField("cuenta", cuenta);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 112) {
                RealmObjectSchema recorridoHistorico = schema.get("RecorridoHistorico");
                if (recorridoHistorico != null) {
                    if (!recorridoHistorico.hasField("identidad")) {
                        recorridoHistorico.addField("identidad", long.class);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 113) {
                RealmObjectSchema recorridoHistorico = schema.get("RecorridoHistorico");
                if (recorridoHistorico != null) {
                    recorridoHistorico.addField("fecha", Date.class);
                    recorridoHistorico.addField("comentario", String.class);
                    recorridoHistorico.addField("estado", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 114) {
                RealmObjectSchema estadoCategoria = schema.create("EstadoCategoria")
                        .addField("id", int.class)
                        .addField("nombre", String.class)
                        .addField("orden", int.class);

                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null) {
                    schema.create("Estado")
                            .addField("uuid", String.class, FieldAttribute.PRIMARY_KEY)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("id", int.class)
                            .addField("estado", String.class)
                            .addRealmListField("categorias", estadoCategoria);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 115) {
                RealmObjectSchema recorridoHistorico = schema.get("RecorridoHistorico");
                if (recorridoHistorico != null && !recorridoHistorico.hasField("novedad")) {
                    recorridoHistorico.addField("novedad", boolean.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 116) {
                RealmObjectSchema recorridoHistorico = schema.get("RecorridoHistorico");
                if (recorridoHistorico != null && !recorridoHistorico.hasField("personal")) {
                    recorridoHistorico.addField("personal", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 117) {
                RealmObjectSchema recorridoHistorico = schema.get("RecorridoHistorico");
                if (recorridoHistorico != null && !recorridoHistorico.hasField("categoria")) {
                    recorridoHistorico.addField("categoria", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 118) {
                RealmObjectSchema recorridoHistorico = schema.get("RecorridoHistorico");
                if (recorridoHistorico != null) {
                    RealmObjectSchema ordenTrabajo = schema.get("OrdenTrabajo");
                    if (ordenTrabajo != null && !ordenTrabajo.hasField("recorridos")) {
                        ordenTrabajo.addRealmListField("recorridos", recorridoHistorico);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 119) {
                RealmObjectSchema recorridoHistorico = schema.get("RecorridoHistorico");
                if (recorridoHistorico != null && recorridoHistorico.hasField("novedad")) {
                    recorridoHistorico.removeField("novedad");
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 120) {
                RealmObjectSchema sitio = schema.get("Sitio");
                if (sitio != null) {
                    if (!sitio.hasField("codigoss")) {
                        sitio.addField("codigoss", String.class);
                    }

                    if (!sitio.hasField("codigoexterno")) {
                        sitio.addField("codigoexterno", String.class);
                    }

                    if (!sitio.hasField("codigoexterno2")) {
                        sitio.addField("codigoexterno2", String.class);
                    }
                }

                if (!schema.contains("EstadoEquipo")) {
                    schema.create("EstadoEquipo")
                            .addField("nombre", String.class);
                }

                RealmObjectSchema entidad = schema.get("Entidad");
                if (entidad != null && !entidad.hasField("estado")) {
                    entidad.addField("estado", String.class);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 121) {
                RealmObjectSchema recorridoHistorico = schema.get("RecorridoHistorico");
                if (recorridoHistorico != null) {
                    if (!recorridoHistorico.hasField("id")) {
                        recorridoHistorico.addField("id", Long.class);
                    }

                    if (!recorridoHistorico.hasField("mostrar")) {
                        recorridoHistorico.addField("mostrar", boolean.class);
                    }
                }

                if (!schema.contains("EstadoEquipo")) {
                    schema.create("EstadoEquipo")
                            .addField("nombre", String.class);
                }

                RealmObjectSchema entidad = schema.get("Entidad");
                if (entidad != null && !entidad.hasField("estado")) {
                    entidad.addField("estado", String.class);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 122) {
                RealmObjectSchema entidad = schema.get("Entidad");
                if (entidad != null) {
                    entidad.addField("orden", int.class);
                    if (!entidad.hasField("estado")) {
                        entidad.addField("estado", String.class);
                    }
                }

                if (!schema.contains("EstadoEquipo")) {
                    schema.create("EstadoEquipo")
                            .addField("nombre", String.class);
                }

                RealmObjectSchema ultimoRegistroBitacora = schema.get("UltimoRegistroBitacora");
                if (ultimoRegistroBitacora != null) {
                    if (!ultimoRegistroBitacora.hasField("estadoEquipo")) {
                        ultimoRegistroBitacora.addField("estadoEquipo", String.class);
                    }
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 123) {
                RealmObjectSchema entity = schema.get("Entity");
                if (entity != null) {
                    RealmObjectSchema ans = schema.create("ANS")
                            .addField("nombre", String.class)
                            .addField("vencimiento", Date.class)
                            .addRealmListField("estados", entity);

                    RealmObjectSchema ordenTrabajo = schema.get("OrdenTrabajo");
                    if (ordenTrabajo != null) {
                        if (!ordenTrabajo.hasField("ans")) {
                            ordenTrabajo.addRealmListField("ans", ans);
                        }
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 124) {
                RealmObjectSchema estado = schema.get("Estado");
                if (estado != null) {
                    if (!estado.hasField("ejecucion")) {
                        estado.addField("ejecucion", int.class);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 125) {
                RealmObjectSchema ans = schema.get("ANS");
                if (ans != null) {
                    if (ans.hasField("estados")) {
                        ans.removeField("estados");
                    }

                    if (!ans.hasField("ejecucioninicial")) {
                        ans.addField("ejecucioninicial", Integer.class);
                    }

                    if (!ans.hasField("ejecucionfinal")) {
                        ans.addField("ejecucionfinal", Integer.class);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 126) {
                RealmObjectSchema ans = schema.get("ANS");
                if (ans != null) {
                    if (!ans.hasField("fechafin")) {
                        ans.addField("fechafin", Date.class);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 127) {
                RealmObjectSchema equipo = schema.get("Equipo");
                if (equipo != null) {
                    if (!equipo.hasField("nfctoken")) {
                        equipo.addField("nfctoken", String.class);
                    }
                }

                RealmObjectSchema instalacionLocativa = schema.get("InstalacionLocativa");
                if (instalacionLocativa != null) {
                    if (!instalacionLocativa.hasField("nfctoken")) {
                        instalacionLocativa.addField("nfctoken", String.class);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 128) {
                RealmObjectSchema busqueda = schema.get("Busqueda");
                if (busqueda != null) {
                    if (!busqueda.hasField("nf")) {
                        busqueda.addField("nfc", String.class);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 129) {
                RealmObjectSchema equipo = schema.get("Equipo");
                if (equipo != null) {
                    if (!equipo.hasField("qrcode")) {
                        equipo.addField("qrcode", String.class);
                    }

                    if (!equipo.hasField("barcode")) {
                        equipo.addField("barcode", String.class);
                    }
                }

                RealmObjectSchema instalacionLocativa = schema.get("InstalacionLocativa");
                if (instalacionLocativa != null) {
                    if (!instalacionLocativa.hasField("qrcode")) {
                        instalacionLocativa.addField("qrcode", String.class);
                    }

                    if (!instalacionLocativa.hasField("barcode")) {
                        instalacionLocativa.addField("barcode", String.class);
                    }
                }

                RealmObjectSchema busqueda = schema.get("Busqueda");
                if (busqueda != null) {
                    if (!busqueda.hasField("qrcode")) {
                        busqueda.addField("qrcode", String.class);
                    }

                    if (!busqueda.hasField("barcode")) {
                        busqueda.addField("barcode", String.class);
                    }
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 130) {
                RealmObjectSchema ordenTrabajo = schema.get("OrdenTrabajo");
                if (ordenTrabajo != null && !ordenTrabajo.hasField("terminada")) {
                    ordenTrabajo.addField("terminada", Boolean.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 131) {
                RealmObjectSchema asignada = schema.get("Asignada");
                if (asignada != null && !asignada.hasField("terminada")) {
                    asignada.addField("terminada", Boolean.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 132) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null && !cuenta.hasField("disponible")) {
                    cuenta.addField("disponible", boolean.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 133) {
                RealmObjectSchema transaccion = schema.get("Transaccion");
                if (transaccion != null && !transaccion.hasField("identidad")) {
                    transaccion.addField("identidad", Long.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 134) {
                RealmObjectSchema actividad = schema.get("Actividad");
                if (actividad != null) {
                    actividad.addField("tipo", String.class);
                    actividad.addField("fechaultimaejecucion", String.class);
                    actividad.addField("fechaproximaejecucion", String.class);
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 135) {
                RealmObjectSchema actividad = schema.get("Actividad");
                if (actividad != null && !actividad.hasField("frecuencia")) {
                    actividad.addField("frecuencia", String.class);
                }

                RealmObjectSchema equipo = schema.get("Equipo");
                if (equipo != null && !equipo.hasField("estado")) {
                    equipo.addField("estado", String.class);
                }
            }

            if (oldVersion == 136) {
                RealmObjectSchema sitio = schema.get("Sitio");
                if (sitio != null) {
                    if (!sitio.hasField("referenciatelefono")) {
                        sitio.addField("referenciatelefono", String.class);
                    }

                    if (!sitio.hasField("referenciadireccion")) {
                        sitio.addField("referenciadireccion", String.class);
                    }
                }

                RealmObjectSchema autorizaciones = schema.get("Autorizaciones");
                autorizaciones.addField("detalle", String.class);

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 134) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null) {

                    RealmObjectSchema bodega = schema.get("Bodega");
                    if (bodega == null) {
                        schema.create("Bodega")
                                .addField("id", Long.class)
                                .addField("codigo", String.class)
                                .addRealmObjectField("cuenta", cuenta)
                                .addField("almacenista", boolean.class)
                                .addField("nombre", String.class);
                    }

                    RealmObjectSchema almacen = schema.get("Almacen");
                    if (almacen == null) {
                        schema.create("Almacen")
                                .addField("id", Long.class)
                                .addRealmObjectField("cuenta", cuenta)
                                .addField("tipo", String.class)
                                .addField("codigo", String.class)
                                .addField("nombre", String.class)
                                .addField("cantidaddisponible", Float.class)
                                .addField("unidadconsumo", String.class)
                                .addField("estado", String.class)
                                .addField("nfc", String.class)
                                .addField("qrcode", String.class)
                                .addField("barcode", String.class)
                                .addField("bodega", String.class);
                    }

                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 135) {
                RealmObjectSchema ordenTrabajo = schema.get("OrdenTrabajo");
                if (ordenTrabajo != null) {
                    RealmObjectSchema rutaTrabajo = schema.get("RutaTrabajo");
                    if (rutaTrabajo != null) {
                        ordenTrabajo.addRealmListField("listachequeo", rutaTrabajo);
                    }
                }
                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 136) {
                RealmObjectSchema rutaTrabajo = schema.get("RutaTrabajo");
                if (rutaTrabajo != null) {
                    rutaTrabajo.addField("tipogrupo", String.class)
                            .addField("diligenciada", boolean.class);
                }

                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (cuenta != null) {
                    RealmObjectSchema jerarquia = schema.get("Jerarquia");
                    if (jerarquia == null) {
                        schema.create("Jerarquia")
                                .addField("id", Long.class)
                                .addField("nombre", String.class)
                                .addRealmObjectField("cuenta", cuenta)
                                .addField("orden", Integer.class)
                                .addField("tipo", String.class)
                                .addField("entidadfiltro", Long.class);
                    }

                    RealmObjectSchema ipStandby = schema.get("IPStandby");
                    if (ipStandby == null) {
                        schema.create("InstalacionProcesoStandBy")
                                .addField("id", Long.class)
                                .addField("nombre", String.class)
                                .addField("codigo", String.class)
                                .addField("descripcion", String.class)
                                .addField("infomaps", String.class)
                                .addField("externo", String.class)
                                .addField("standby", Boolean.class);
                    }
                    RealmObjectSchema ot = schema.get("OrdenTrabajo");
                    if (ot != null) {
                        ot.addField("entidadValida", Long.class);
                    }

                    RealmObjectSchema entidad = schema.get("Entidad");
                    if (entidad != null) {
                        entidad.addField("nfc", String.class)
                                .addField("qrcode", String.class)
                                .addField("barcode", String.class);
                    }

                    RealmObjectSchema segmento = schema.get("SegmentoVariable");
                    if (segmento == null) {
                        schema.create("SegmentoVariable")
                                .addField("id", Long.class)
                                .addField("nombre", String.class)
                                .addField("color", String.class);
                    }

                    RealmObjectSchema variable = schema.get("Variable");
                    if (variable != null) {
                        variable.addRealmObjectField("segmento", segmento);
                    }

                    RealmObjectSchema estadoCategoria = schema.create("EstadoCategoria");
                    if (estadoCategoria != null) {
                        estadoCategoria.addField("afectatiempoans", boolean.class);
                    }

                    RealmObjectSchema ruta = schema.create("RutaTrabajo");
                    if (ruta != null) {
                        ruta.addField("multiple", boolean.class)
                                .addField("idot", Long.class);
                    }

                    RealmObjectSchema lcxot = schema.create("LCxOT");
                    if (lcxot == null) {
                        schema.create("LCxOT")
                                .addField("idrt", Long.class)
                                .addField("idot", Long.class)
                                .addField("idgrupoam", Long.class);
                    }

                    RealmObjectSchema ans = schema.get("ANS");
                    if (ans != null) {
                        ans.addField("prioridad", String.class)
                                .addField("tipo", String.class);
                    }

                    RealmObjectSchema recorrido = schema.get("Recorrido");
                    if (recorrido != null) {
                        recorrido.addField("sincronizado", Boolean.class);
                    }
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 137) {
                RealmObjectSchema ordenTrabajo = schema.get("OrdenTrabajo");
                if (ordenTrabajo != null) {
                    ordenTrabajo.addField("fechainicioreal", String.class)
                            .addField("fechafinreal", String.class);
                }

                RealmObjectSchema recurso = schema.get("Recurso");
                if (recurso != null) {
                    recurso.addField("cantidadasignada", String.class);
                }

                RealmObjectSchema ssxot = schema.create("SSxOT")
                        .addField("id", String.class)
                        .addField("codigoss", String.class)
                        .addField("codigoexterno", String.class)
                        .addField("codigoexterno2", String.class);

                if (ordenTrabajo != null) {
                    ordenTrabajo.addRealmObjectField("ss", ssxot);
                }

                schema.create("Certificado")
                        .addField("key", String.class, FieldAttribute.PRIMARY_KEY)
                        .addField("file", String.class)
                        .addField("url", String.class)
                        .addField("creation", Date.class)
                        .addField("username", String.class)
                        .addField("database", String.class);

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 138) {
                RealmObjectSchema almacen = schema.get("Almacen");
                if (almacen != null && almacen.hasField("cantidaddisponible") && almacen.hasField("bodega") && !almacen.hasField("activo")) {
                    almacen.renameField("cantidaddisponible", "cantidad");
                    almacen.removeField("bodega");
                    almacen.addField("activo", Boolean.class);
                }

                RealmObjectSchema bodega = schema.get("Bodega");
                if (bodega != null && bodega.hasField("almacenista")) {
                    bodega.removeField("almacenista");
                }

                if (schema.get("tipoMovimiento") == null) {
                    schema.create("tipoMovimiento")
                            .addField("id", String.class)
                            .addField("nombre", String.class)
                            .addField("movimientovalido", String.class);
                }

                RealmObjectSchema certificado = schema.get("Certificado");
                if (certificado != null) {
                    certificado.addField("password", String.class);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 139) {
                if (schema.get("Familia") == null) {
                    schema.create("Familia")
                            .addField("id", int.class, FieldAttribute.PRIMARY_KEY)
                            .addField("codigo", String.class)
                            .addField("nombre", String.class);
                }

                if (schema.get("InstalacionProceso") == null) {
                    schema.create("InstalacionProceso")
                            .addField("id", int.class, FieldAttribute.PRIMARY_KEY)
                            .addField("codigo", String.class)
                            .addField("nombre", String.class);
                }

                if (schema.get("MarcaEquipo") == null) {
                    schema.create("MarcaEquipo")
                            .addField("id", int.class, FieldAttribute.PRIMARY_KEY)
                            .addField("codigo", String.class)
                            .addField("nombre", String.class);
                }

                if (schema.get("MedidasEquipo") == null) {
                    schema.create("MedidasEquipo")
                            .addField("id", int.class, FieldAttribute.PRIMARY_KEY)
                            .addField("descripcion", String.class);
                }

                if (schema.get("CentroCostoEquipo") == null) {
                    schema.create("CentroCostoEquipo")
                            .addField("id", int.class, FieldAttribute.PRIMARY_KEY)
                            .addField("codigo", String.class)
                            .addField("nombre", String.class);
                }

                if (schema.get("RepuestoManual") == null) {
                    RealmObjectSchema repuestoManual = schema.create("RepuestoManual")
                            .addField("id", Long.class)
                            .addField("codigo", String.class)
                            .addField("nombre", String.class);

                    RealmObjectSchema cuenta = schema.get("Cuenta");
                    if (cuenta != null) {
                        repuestoManual.addRealmObjectField("cuenta", cuenta);
                    }

                    RealmObjectSchema ordenTrabajo = schema.get("OrdenTrabajo");
                    if (ordenTrabajo != null && !ordenTrabajo.hasField("repuestosManuales")) {
                        ordenTrabajo.addRealmListField("repuestosManuales", repuestoManual);
                    }
                }

                RealmObjectSchema certificado = schema.get("Certificado");
                if (certificado != null) {
                    certificado.addField("server", String.class);
                    certificado.renameField("file", "client");
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 140) {
                RealmObjectSchema centroCostoEquipo = schema.get("CentroCostoEquipo");
                if (centroCostoEquipo != null) {
                    centroCostoEquipo.addField("porcentaje", Double.class);
                }

                RealmObjectSchema repuestoManual = schema.get("RepuestoManual");
                if (repuestoManual != null && repuestoManual.hasField("codigo")) {
                    repuestoManual.renameField("codigo", "serial");
                }

                RealmObjectSchema novedad = schema.get("Novedad");
                if (novedad != null && !novedad.hasField("fechafin")) {
                    novedad.addField("fechafin", String.class);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 141) {
                if (schema.get("Responsable") == null) {
                    schema.create("Responsable")
                            .addField("id", int.class, FieldAttribute.PRIMARY_KEY)
                            .addField("codigo", String.class)
                            .addField("nombre", String.class)
                            .addField("apellido", String.class)
                            .addField("cargo", String.class);
                }

                if (schema.get("Falla") == null) {
                    RealmObjectSchema falla = schema.create("Falla")
                            .addField("id", Long.class)
                            .addField("codigo", String.class)
                            .addField("falla", String.class);

                    RealmObjectSchema cuenta = schema.get("Cuenta");
                    if (cuenta != null) {
                        falla.addRealmObjectField("cuenta", cuenta);
                    }

                    RealmObjectSchema ordenTrabajo = schema.get("OrdenTrabajo");
                    if (ordenTrabajo != null && !ordenTrabajo.hasField("fallas")) {
                        ordenTrabajo.addRealmListField("fallas", falla);
                    }
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 142) {
                RealmObjectSchema instalacionProceso = schema.get("InstalacionProceso");
                if (instalacionProceso != null) {
                    if (instalacionProceso.hasField("id")) {
                        instalacionProceso.removeField("id");
                        instalacionProceso.addField("newId", Long.class, FieldAttribute.PRIMARY_KEY);
                        instalacionProceso.renameField("newId", "id");
                    }
                }

                RealmObjectSchema responsable = schema.get("Responsable");
                if (responsable != null) {
                    if (responsable.hasField("id")) {
                        responsable.removeField("id");
                        responsable.addField("newId", Long.class, FieldAttribute.PRIMARY_KEY);
                        responsable.renameField("newId", "id");
                    }
                }

                if (schema.get("ElementoFalla") == null) {
                    RealmObjectSchema elementoFalla = schema.create("ElementoFalla")
                            .addField("id", Long.class)
                            .addField("serial", String.class)
                            .addField("nombre", String.class);

                    RealmObjectSchema falla = schema.get("Falla");
                    if (falla != null) {
                        falla.removeField("codigo")
                                .removeField("falla")
                                .addField("tipo", String.class)
                                .addField("fechaInspeccion", String.class)
                                .addField("procesoRealizado", String.class)
                                .addField("descripcion", String.class)
                                .addRealmListField("elementos", elementoFalla);

                        RealmObjectSchema imagenes = schema.get("Adjuntos");
                        if (imagenes != null) {
                            falla.addRealmListField("imagenes", imagenes);
                        }
                    }
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 143) {
                RealmObjectSchema centroCostoEquipo = schema.get("CentroCostoEquipo");
                if (centroCostoEquipo != null) {
                    if (centroCostoEquipo.hasField("id")) {
                        centroCostoEquipo.removeField("id");
                        centroCostoEquipo.addField("newId", Long.class, FieldAttribute.PRIMARY_KEY);
                        centroCostoEquipo.renameField("newId", "id");
                    }
                }

                RealmObjectSchema falla = schema.get("Falla");
                if (falla != null && !falla.hasField("UUID")) {
                    falla.addField("UUID", String.class, FieldAttribute.PRIMARY_KEY);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 144) {

                RealmObjectSchema centroCostoEquipo = schema.get("CentroCostoEquipo");
                if (centroCostoEquipo != null) {
                    centroCostoEquipo.removeField("porcentaje");
                    centroCostoEquipo.addField("newPorcentaje", double.class);
                    centroCostoEquipo.renameField("newPorcentaje", "porcentaje");
                }

                RealmObjectSchema repuestoManual = schema.get("RepuestoManual");
                if (repuestoManual != null && !repuestoManual.hasField("serialRetiro")) {
                    repuestoManual.addField("serialRetiro", String.class);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 145) {
                if (schema.get("Pais") == null) {
                    schema.create("Pais")
                            .addField("id", Long.class, FieldAttribute.PRIMARY_KEY)
                            .addField("nombre", String.class);
                }

                if (schema.get("Departamento") == null) {
                    schema.create("Departamento")
                            .addField("id", Long.class, FieldAttribute.PRIMARY_KEY)
                            .addField("nombre", String.class)
                            .addField("idpais", Long.class);
                }

                if (schema.get("Ciudad") == null) {
                    schema.create("Ciudad")
                            .addField("id", Long.class, FieldAttribute.PRIMARY_KEY)
                            .addField("nombre", String.class)
                            .addField("iddepartamento", Long.class);
                }

                RealmObjectSchema falla = schema.get("Falla");
                if (falla != null && !falla.hasField("entidad") && !falla.hasField("nombre") && !falla.hasField("resumen") && !falla.hasField("fechainicio") && !falla.hasField("fechafin") && !falla.hasField("horafin") && !falla.hasField("adjuntos")
                        && falla.hasField("tipo") && falla.hasField("fechaInspeccion") && falla.hasField("procesoRealizado") && falla.hasField("descripcion") && falla.hasField("elementos")) {
                    falla.addField("entidad", String.class);
                    falla.addField("nombre", String.class);
                    falla.addField("resumen", String.class);
                    falla.addField("fechainicio", String.class);
                    falla.addField("fechafin", String.class);
                    falla.addField("horafin", String.class);

                    RealmObjectSchema adjuntos = schema.get("Adjuntos");
                    if (adjuntos != null) {
                        falla.addRealmListField("adjuntos", adjuntos);
                    }

                    falla.removeField("tipo");
                    falla.removeField("fechaInspeccion");
                    falla.removeField("procesoRealizado");
                    falla.removeField("descripcion");
                    falla.removeField("elementos");

                    RealmObjectSchema repuestoManual = schema.get("RepuestoManual");
                    if (repuestoManual != null) {
                        if (!repuestoManual.hasField("idfalla")) {
                            repuestoManual.addField("idfalla", int.class);
                        }

                        falla.addRealmListField("repuestos", repuestoManual);
                    }

                    RealmObjectSchema cuenta = schema.get("Cuenta");
                    if (schema.get("Consumible") == null && cuenta != null) {
                        RealmObjectSchema consumible = schema.create("Consumible")
                                .addField("id", Long.class)
                                .addField("nombre", String.class)
                                .addField("cantidadestimada", Double.class)
                                .addField("costoestimado", Double.class)
                                .addField("cantidadreal", Double.class)
                                .addField("costoreal", Double.class)
                                .addField("valorunitario", Double.class)
                                .addField("idunidadmedida", int.class)
                                .addField("idfalla", int.class)
                                .addRealmObjectField("cuenta", cuenta);

                        falla.addRealmListField("consumibles", consumible);
                    }
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 146) {
                RealmObjectSchema medidasEquipo = schema.get("MedidasEquipo");
                if (medidasEquipo != null) {
                    if (!medidasEquipo.hasField("tipo")) {
                        medidasEquipo.addField("tipo", String.class);
                    }

                    medidasEquipo.removeField("id");
                    medidasEquipo.addField("newId", Long.class, FieldAttribute.PRIMARY_KEY);
                    medidasEquipo.renameField("newId", "id");
                }

                RealmObjectSchema ordenTrabajo = schema.get("OrdenTrabajo");
                if (ordenTrabajo != null && ordenTrabajo.hasField("repuestosManuales")) {
                    ordenTrabajo.removeField("repuestosManuales");
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 147) {
                RealmObjectSchema medidasEquipo = schema.get("MedidasEquipo");
                if (medidasEquipo != null) {
                    if (medidasEquipo.hasField("descripcion")) {
                        medidasEquipo.renameField("descripcion", "sigla");
                    }
                    if (medidasEquipo.hasField("tipo")) {
                        medidasEquipo.renameField("tipo", "idtipounidad");
                    }
                }

                RealmObjectSchema ordenTrabajo = schema.get("OrdenTrabajo");
                if (ordenTrabajo != null && !ordenTrabajo.hasField("repuestos") && !ordenTrabajo.hasField("consumibles")) {
                    RealmObjectSchema repuestoManual = schema.get("RepuestoManual");
                    RealmObjectSchema consumible = schema.get("Consumible");

                    if (repuestoManual != null && consumible != null) {
                        ordenTrabajo.addRealmListField("repuestos", repuestoManual);
                        ordenTrabajo.addRealmListField("consumibles", consumible);
                    }
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 148) {
                RealmObjectSchema familia = schema.get("Familia");
                if (familia != null) {
                    familia.removeField("id");
                    familia.addField("newId", Long.class, FieldAttribute.PRIMARY_KEY);
                    familia.renameField("newId", "id");
                }

                RealmObjectSchema marcaEquipo = schema.get("MarcaEquipo");
                if (marcaEquipo != null) {
                    marcaEquipo.removeField("id");
                    marcaEquipo.addField("newId", Long.class, FieldAttribute.PRIMARY_KEY);
                    marcaEquipo.renameField("newId", "id");
                }

                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (schema.get("TipoFalla") == null && schema.get("GamaMantenimiento") == null && cuenta != null) {
                    schema.create("TipoFalla")
                            .addField("tipo", String.class)
                            .addRealmObjectField("cuenta", cuenta);

                    schema.create("GamaMantenimiento")
                            .addField("id", Long.class)
                            .addField("codigo", String.class)
                            .addField("actividad", String.class)
                            .addField("descripcion", String.class)
                            .addField("tipo", String.class)
                            .addField("especialidad", String.class)
                            .addRealmObjectField("cuenta", cuenta);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 149) {
                if (schema.get("CategoriaEquipo") == null) {
                    schema.create("CategoriaEquipo")
                            .addField("id", Long.class, FieldAttribute.PRIMARY_KEY)
                            .addField("nombre", String.class);
                }

                if (schema.get("EstadoTransferenciaEquipo") == null) {
                    schema.create("EstadoTransferenciaEquipo")
                            .addField("id", Long.class, FieldAttribute.PRIMARY_KEY)
                            .addField("nombre", String.class);
                }

                RealmObjectSchema elementoFalla = schema.get("ElementoFalla");
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (elementoFalla != null && cuenta != null && !elementoFalla.hasField("cuenta")) {
                    elementoFalla.addRealmObjectField("cuenta", cuenta);

                    RealmObjectSchema falla = schema.get("Falla");
                    if (falla != null && !falla.hasField("elementos")) {
                        falla.addRealmListField("elementos", elementoFalla);
                    }
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 150) {
                if (schema.get("InformacionFinanciera") == null) {
                    RealmObjectSchema centroCostoEquipo = schema.get("CentroCostoEquipo");
                    if (centroCostoEquipo != null) {
                        schema.create("InformacionFinanciera")
                                .addField("codigocontable", String.class)
                                .addField("categoria", String.class)
                                .addRealmListField("centrocostos", centroCostoEquipo);
                    }
                }

                RealmObjectSchema informacionTecnica = schema.get("InformacionTecnica");
                if (informacionTecnica != null) {
                    if (!informacionTecnica.hasField("idfabricante"))
                        informacionTecnica.addField("idfabricante", Long.class);
                    if (!informacionTecnica.hasField("color"))
                        informacionTecnica.addField("color", String.class);
                    if (!informacionTecnica.hasField("largo"))
                        informacionTecnica.addField("largo", Double.class);
                    if (!informacionTecnica.hasField("medidalargo"))
                        informacionTecnica.addField("medidalargo", String.class);
                    if (!informacionTecnica.hasField("ancho"))
                        informacionTecnica.addField("ancho", Double.class);
                    if (!informacionTecnica.hasField("medidaancho"))
                        informacionTecnica.addField("medidaancho", String.class);
                    if (!informacionTecnica.hasField("alto"))
                        informacionTecnica.addField("alto", Double.class);
                    if (!informacionTecnica.hasField("medidaalto"))
                        informacionTecnica.addField("medidaalto", String.class);
                    if (!informacionTecnica.hasField("peso"))
                        informacionTecnica.addField("peso", Double.class);
                    if (!informacionTecnica.hasField("medidapeso"))
                        informacionTecnica.addField("medidapeso", String.class);
                }

                RealmObjectSchema equipo = schema.get("Equipo");
                if (equipo != null) {
                    if (!equipo.hasField("idfamilia1")) equipo.addField("idfamilia1", Long.class);
                    if (!equipo.hasField("idinstalacionproceso"))
                        equipo.addField("idinstalacionproceso", Long.class);
                    if (!equipo.hasField("idinstalacionlocativa"))
                        equipo.addField("idinstalacionlocativa", Long.class);
                    if (!equipo.hasField("pais")) equipo.addField("pais", String.class);
                    if (!equipo.hasField("departamento"))
                        equipo.addField("departamento", String.class);
                    if (!equipo.hasField("ciudad")) equipo.addField("ciudad", String.class);

                    RealmObjectSchema informacionFinanciera = schema.get("InformacionFinanciera");
                    if (informacionFinanciera != null) {
                        if (!equipo.hasField("informacionfinanciera"))
                            equipo.addRealmObjectField("informacionfinanciera", informacionFinanciera);
                    }

                    RealmObjectSchema responsable = schema.get("Responsable");
                    if (responsable != null) {
                        if (responsable.hasField("codigo")) responsable.removeField("codigo");
                        if (responsable.hasField("apellido")) responsable.removeField("apellido");
                        if (responsable.hasField("cargo")) responsable.removeField("cargo");
                        if (!responsable.hasField("estado"))
                            responsable.addField("estado", String.class);

                        if (!equipo.hasField("personal")) {
                            equipo.addRealmObjectField("personal", responsable);
                        }
                    }

                    RealmObjectSchema instalacionLocativa = schema.get("InstalacionLocativa");
                    RealmObjectSchema falla = schema.get("Falla");
                    if (falla != null) {
                        if (!equipo.hasField("fallas")) {
                            equipo.addRealmListField("fallas", falla);
                        }
                        if (instalacionLocativa != null && !instalacionLocativa.hasField("fallas")) {
                            instalacionLocativa.addRealmListField("fallas", falla);
                        }
                    }
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 151) {
                RealmObjectSchema equipo = schema.get("Equipo");
                if (equipo != null) {
                    if (!equipo.hasField("idpais")) equipo.addField("idpais", Long.class);
                    if (!equipo.hasField("iddepartamento"))
                        equipo.addField("iddepartamento", Long.class);
                    if (!equipo.hasField("idciudad")) equipo.addField("idciudad", Long.class);
                }

                RealmObjectSchema busqueda = schema.get("Busqueda");
                RealmObjectSchema falla = schema.get("Falla");
                if (falla != null && busqueda != null && !busqueda.hasField("fallas")) {
                    busqueda.addRealmListField("fallas", falla);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 152) {
                RealmObjectSchema centroCostoEquipo = schema.get("CentroCostoEquipo");
                if (centroCostoEquipo != null) {
                    centroCostoEquipo.removeField("porcentaje");
                    centroCostoEquipo.addField("newPorcentaje", int.class);
                    centroCostoEquipo.renameField("newPorcentaje", "porcentaje");
                }

                RealmObjectSchema falla = schema.get("Falla");
                if (falla != null && !falla.hasField("am") && !falla.hasField("descripcion") && !falla.hasField("idtipofalla")) {
                    falla.addField("am", String.class);
                    falla.addField("descripcion", String.class);
                    falla.addField("idtipofalla", String.class);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 153) {
                RealmObjectSchema centroCostoEquipo = schema.get("CentroCostoEquipo");
                if (centroCostoEquipo != null) {
                    if (centroCostoEquipo.hasPrimaryKey()) {
                        centroCostoEquipo.removePrimaryKey();
                    }
                }

                RealmObjectSchema falla = schema.get("Falla");
                if (falla != null && !falla.hasField("ot") && !falla.hasField("idot")) {
                    falla.addField("ot", String.class);
                    falla.addField("idot", Long.class);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 154) {
                RealmObjectSchema centroCostoEquipo = schema.get("CentroCostoEquipo");
                if (centroCostoEquipo != null) {
                    if (!centroCostoEquipo.hasPrimaryKey()) {
                        centroCostoEquipo.addPrimaryKey("id");
                    }
                }

                RealmObjectSchema cuenta = schema.get("Cuenta");
                RealmObjectSchema gamaMantenimiento = schema.get("GamaMantenimiento");
                if (schema.get("ReclasificacionGama") == null && schema.get("TipoReparacionGama") == null &&
                        schema.get("SubtipoReparacionGama") == null && cuenta != null && gamaMantenimiento != null) {

                    RealmObjectSchema subtipoReparacionGama = schema.create("SubtipoReparacionGama")
                            .addField("id", Long.class)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("nombre", String.class)
                            .addRealmListField("gamas", gamaMantenimiento);

                    RealmObjectSchema tipoReparacionGama = schema.create("TipoReparacionGama")
                            .addField("id", Long.class)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("nombre", String.class)
                            .addRealmListField("subtiposreparacion", subtipoReparacionGama);

                    schema.create("ReclasificacionGama")
                            .addField("id", Long.class)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("nombre", String.class)
                            .addRealmListField("tiposreparacion", tipoReparacionGama);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 155) {
                RealmObjectSchema centroCostoEquipo = schema.get("CentroCostoEquipo");
                if (centroCostoEquipo != null) {
                    if (centroCostoEquipo.hasPrimaryKey()) {
                        centroCostoEquipo.removePrimaryKey();
                    }
                }

                RealmObjectSchema gamaMantenimiento = schema.get("GamaMantenimiento");
                if (gamaMantenimiento != null && !gamaMantenimiento.hasField("idreclasificacion")
                        && !gamaMantenimiento.hasField("idtiporeparacion") && !gamaMantenimiento.hasField("idsubtiporeparacion")) {
                    gamaMantenimiento.addField("idreclasificacion", Long.class);
                    gamaMantenimiento.addField("idtiporeparacion", Long.class);
                    gamaMantenimiento.addField("idsubtiporeparacion", Long.class);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 156) {
                RealmObjectSchema centroCostoEquipo = schema.get("CentroCostoEquipo");
                if (centroCostoEquipo != null) {
                    centroCostoEquipo.removeField("porcentaje");
                    centroCostoEquipo.addField("newPorcentaje", double.class);
                    centroCostoEquipo.renameField("newPorcentaje", "porcentaje");
                }

                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (schema.get("Yarda") == null && cuenta != null) {
                    schema.create("Yarda")
                            .addField("id", Long.class)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("nombre", String.class);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 157) {
                RealmObjectSchema centroCostoEquipo = schema.get("CentroCostoEquipo");
                if (centroCostoEquipo != null) {
                    centroCostoEquipo.removeField("porcentaje");
                    centroCostoEquipo.addField("newPorcentaje", int.class);
                    centroCostoEquipo.renameField("newPorcentaje", "porcentaje");
                }

                RealmObjectSchema falla = schema.get("Falla");
                if (falla != null && !falla.hasField("requiererepuesto") && !falla.hasField("requierefoto")) {
                    falla.addField("requiererepuesto", boolean.class)
                            .addField("requierefoto", boolean.class);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 158) {
                RealmObjectSchema responsable = schema.get("Responsable");
                if (responsable != null) {
                    if (responsable.hasPrimaryKey()) {
                        responsable.removePrimaryKey();
                    }
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 159) {
                if (schema.get("UbicacionPredeterminada") == null) {
                    schema.create("UbicacionPredeterminada")
                            .addField("idPais", Long.class)
                            .addField("idDepartamento", Long.class)
                            .addField("idCiudad", Long.class);
                }

                RealmObjectSchema consumible = schema.get("Consumible");
                if (consumible != null && consumible.hasField("cantidadreal")) {
                    consumible.renameField("cantidadreal", "cantidad");
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 160) {
                RealmObjectSchema equipo = schema.get("Equipo");
                if (equipo != null) {
                    equipo.removeField("barcode");
                    equipo.addRealmListField("newBarcode", String.class);
                    equipo.renameField("newBarcode", "barcode");
                }

                RealmObjectSchema busqueda = schema.get("Busqueda");
                if (busqueda != null) {
                    busqueda.removeField("barcode");
                    busqueda.addRealmListField("newBarcode", String.class);
                    busqueda.renameField("newBarcode", "barcode");
                }

                RealmObjectSchema tipoFalla = schema.get("TipoFalla");
                if (tipoFalla != null && !tipoFalla.hasField("descripcion")) {
                    tipoFalla.addField("descripcion", String.class);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 161) {
                if (schema.get("Barcode") == null) {
                    schema.create("Barcode")
                            .addField("codigo", String.class);
                }

                RealmObjectSchema barcode = schema.get("Barcode");
                if (barcode != null) {
                    RealmObjectSchema equipo = schema.get("Equipo");
                    if (equipo != null) {
                        equipo.removeField("barcode");
                        equipo.addRealmListField("newBarcode", barcode);
                        equipo.renameField("newBarcode", "barcode");
                    }

                    RealmObjectSchema busqueda = schema.get("Busqueda");
                    if (busqueda != null) {
                        busqueda.removeField("barcode");
                        busqueda.addRealmListField("newBarcode", barcode);
                        busqueda.renameField("newBarcode", "barcode");
                    }
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 162) {
                RealmObjectSchema ubicacionPredeterminada = schema.get("UbicacionPredeterminada");
                if (ubicacionPredeterminada != null && !ubicacionPredeterminada.hasField("ubicacion")) {
                    ubicacionPredeterminada.addField("ubicacion", String.class);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 163) {
                RealmObjectSchema estadoTransferenciaEquipo = schema.get("EstadoTransferenciaEquipo");
                if (estadoTransferenciaEquipo != null && !estadoTransferenciaEquipo.hasField("crear") && !estadoTransferenciaEquipo.hasField("editar")) {
                    estadoTransferenciaEquipo.addField("crear", boolean.class);
                    estadoTransferenciaEquipo.addField("editar", boolean.class);
                }

                RealmObjectSchema reclasificacionGama = schema.get("ReclasificacionGama");
                if (reclasificacionGama != null) {
                    reclasificacionGama.removeField("id");
                    reclasificacionGama.addField("id", String.class);
                }

                RealmObjectSchema tipoReparacionGama = schema.get("TipoReparacionGama");
                if (tipoReparacionGama != null) {
                    tipoReparacionGama.removeField("id");
                    tipoReparacionGama.addField("id", String.class);
                }

                RealmObjectSchema subtipoReparacionGama = schema.get("SubtipoReparacionGama");
                if (subtipoReparacionGama != null) {
                    subtipoReparacionGama.removeField("id");
                    subtipoReparacionGama.addField("id", String.class);
                }

                RealmObjectSchema gamaMantenimiento = schema.get("GamaMantenimiento");
                if (gamaMantenimiento != null) {
                    gamaMantenimiento.removeField("idreclasificacion");
                    gamaMantenimiento.removeField("idtiporeparacion");
                    gamaMantenimiento.removeField("idsubtiporeparacion");

                    gamaMantenimiento.addField("idreclasificacion", String.class);
                    gamaMantenimiento.addField("idtiporeparacion", String.class);
                    gamaMantenimiento.addField("idsubtiporeparacion", String.class);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 164) {
                RealmObjectSchema estadoTransferenciaEquipo = schema.get("EstadoTransferenciaEquipo");
                if (estadoTransferenciaEquipo != null && estadoTransferenciaEquipo.hasField("crear") && estadoTransferenciaEquipo.hasField("editar")) {
                    estadoTransferenciaEquipo.removeField("crear");
                    estadoTransferenciaEquipo.removeField("editar");
                }

                if (schema.get("EstadoActualEquipo") == null) {
                    schema.create("EstadoActualEquipo")
                            .addField("id", Long.class, FieldAttribute.PRIMARY_KEY)
                            .addField("nombre", String.class)
                            .addField("crear", boolean.class)
                            .addField("editar", boolean.class);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 165) {
                RealmObjectSchema estadoActualEquipo = schema.get("EstadoActualEquipo");
                if (estadoActualEquipo != null && estadoActualEquipo.hasPrimaryKey() && !estadoActualEquipo.hasField("orden")) {
                    estadoActualEquipo.removePrimaryKey();
                    estadoActualEquipo.removeField("id");
                    estadoActualEquipo.addField("orden", int.class);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 166) {
                RealmObjectSchema centroCostoEquipo = schema.get("CentroCostoEquipo");
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (centroCostoEquipo != null && cuenta != null && !centroCostoEquipo.hasField("cuenta")) {
                    centroCostoEquipo.addRealmObjectField("cuenta", cuenta);
                }

                RealmObjectSchema variable = schema.get("Variable");
                if (variable != null && !variable.hasField("unidadmedida")) {
                    variable.addField("unidadmedida", String.class);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 167) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (schema.get("Category") == null && schema.get("Formato") == null && cuenta != null) {
                    RealmObjectSchema category = schema.create("Category")
                            .addField("uuid", String.class)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("id", Long.class)
                            .addField("nombre", String.class)
                            .addField("firma", Boolean.class);

                    schema.create("Formato")
                            .addField("uuid", String.class, FieldAttribute.PRIMARY_KEY)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("id", Long.class)
                            .addField("formato", String.class)
                            .addRealmListField("categorias", category);
                }

                RealmObjectSchema recorridoHistorico = schema.get("RecorridoHistorico");
                if (recorridoHistorico != null && !recorridoHistorico.hasField("idcategoria") && !recorridoHistorico.hasField("fechaFin")) {
                    recorridoHistorico.addField("idcategoria", long.class);
                    recorridoHistorico.addField("fechaFin", Date.class);
                }

                RealmObjectSchema recurso = schema.get("Recurso");
                if (recurso != null && !recurso.hasField("cantidaddisponible")) {
                    recurso.addField("cantidaddisponible", String.class);
                }

                RealmObjectSchema almacen = schema.get("Almacen");
                if (almacen != null && !almacen.hasField("tiposalida")) {
                    almacen.addField("tiposalida", String.class);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 168) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (schema.get("TipoParo") == null && schema.get("ClasificacionParo") == null && schema.get("Paro") == null && cuenta != null) {
                    schema.create("TipoParo")
                            .addField("id", Long.class)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("nombre", String.class);

                    schema.create("ClasificacionParo")
                            .addField("id", String.class)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("nombre", String.class);

                    schema.create("Paro")
                            .addField("id", Long.class)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("equipo", String.class)
                            .addField("fechainicio", String.class)
                            .addField("fechafin", String.class)
                            .addField("duracion", String.class)
                            .addField("tipo", String.class)
                            .addField("tipoparo", String.class);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 169) {
                RealmObjectSchema paro = schema.get("Paro");
                if (paro != null && !paro.hasField("idequipo")) {
                    paro.addField("idequipo", Long.class);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 170) {
                RealmObjectSchema eventType = schema.get("EventType");
                if (eventType != null && !eventType.hasField("requiereentidad")) {
                    eventType.addField("requiereentidad", boolean.class);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 171) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (schema.get("Tarea") == null && cuenta != null) {
                    RealmObjectSchema tarea = schema.create("Tarea")
                            .addField("id", Long.class)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("codigo", String.class)
                            .addField("tarea", String.class)
                            .addField("critica", boolean.class)
                            .addField("orden", int.class)
                            .addField("ejecutada", boolean.class);

                    RealmObjectSchema actividad = schema.get("Actividad");
                    if (actividad != null && !actividad.hasField("tareas")) {
                        actividad.addRealmListField("tareas", tarea);
                    }
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 172) {
                RealmObjectSchema tarea = schema.get("Tarea");
                if (tarea != null && !tarea.hasField("descripcion") && !tarea.hasField("ejecutor") && !tarea.hasField("fechaejecucion")) {
                    tarea.addField("descripcion", String.class);
                    tarea.addField("ejecutor", String.class);
                    tarea.addField("fechaejecucion", String.class);
                }

                oldVersion = oldVersion + 1;
            }

            if (oldVersion == 173) {
                RealmObjectSchema tarea = schema.get("Tarea");
                if (tarea != null && tarea.hasField("cuenta")) {
                    tarea.removeField("cuenta");
                }

                oldVersion++;
            }

            if (oldVersion == 174) {
                RealmObjectSchema actividad = schema.get("Actividad");
                if (actividad != null && !actividad.hasField("uuid")) {
                    actividad.addField("uuid", String.class);
                }

                oldVersion++;
            }

            if (oldVersion == 175) {
                RealmObjectSchema tarea = schema.get("Tarea");
                if (tarea != null && !tarea.hasField("uuid") && !tarea.hasField("tiempobase")) {
                    tarea.addField("uuid", String.class);
                    tarea.addField("tiempobase", int.class);
                }

                oldVersion++;
            }

            if (oldVersion == 176) {
                RealmObjectSchema tarea = schema.get("Tarea");
                if (tarea != null && !tarea.hasField("tiempobasetexto")) {
                    tarea.addField("tiempobasetexto", String.class);
                }

                oldVersion++;
            }

            if (oldVersion == 177) {
                RealmObjectSchema busqueda = schema.get("Busqueda");
                if (busqueda != null && !busqueda.hasField("entidadesrelacionadas")) {
                    busqueda.addField("entidadesrelacionadas", String.class);
                }

                oldVersion++;
            }

            if (oldVersion == 178) {
                RealmObjectSchema busqueda = schema.get("Busqueda");
                if (busqueda != null && !busqueda.hasField("informacionvisualextra")) {
                    busqueda.addField("informacionvisualextra", String.class);
                }

                oldVersion++;
            }

            if (oldVersion == 179) {
                RealmObjectSchema busqueda = schema.get("InstalacionLocativa");
                if (busqueda != null && !busqueda.hasField("locacionpadre")) {
                    busqueda.addField("locacionpadre", String.class);
                }

                oldVersion++;
            }

            if (oldVersion == 180) {
                RealmObjectSchema busqueda = schema.get("InstalacionLocativa");
                if (busqueda != null && busqueda.hasField("locacionpadre")) {
                    busqueda.removeField("locacionpadre");
                }

                oldVersion++;
            }

            if (oldVersion == 181) {
                RealmObjectSchema busqueda = schema.get("Busqueda");
                if (busqueda != null && !busqueda.hasField("idejecucion")) {
                    busqueda.addField("idejecucion", Long.class);
                }

                oldVersion++;
            }

            if (oldVersion == 182) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (schema.get("BitacoraContinua") == null && cuenta != null) {
                    schema.create("BitacoraContinua")
                            .addField("uuid", String.class, FieldAttribute.PRIMARY_KEY)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("idmodulo", Long.class)
                            .addField("codigo", String.class)
                            .addField("fechainicio", Date.class)
                            .addField("fechafin", Date.class)
                            .addField("fecharegistro", String.class)
                            .addField("value", String.class)
                            .addField("tipo", String.class)
                            .addField("estado", String.class);
                }

                oldVersion++;
            }

            if (oldVersion == 183) {
                RealmObjectSchema rutaTrabajo = schema.get("RutaTrabajo");
                if (rutaTrabajo != null && rutaTrabajo.hasField("idejecucion")) {
                    if (!rutaTrabajo.isNullable("idejecucion")) {
                        rutaTrabajo.setNullable("idejecucion", true);
                    }
                }

                oldVersion++;
            }

            if (oldVersion == 184) {
                RealmObjectSchema recorridoHistorico = schema.get("RecorridoHistorico");
                if (recorridoHistorico != null) {

                    if (recorridoHistorico.hasField("identidad")) {
                        if (!recorridoHistorico.isNullable("identidad")) {
                            recorridoHistorico.setNullable("identidad", true);
                        }
                    }

                    if (recorridoHistorico.hasField("idcategoria")) {
                        if (!recorridoHistorico.isNullable("idcategoria")) {
                            recorridoHistorico.setNullable("idcategoria", true);
                        }
                    }

                    if (!recorridoHistorico.hasField("tipoentidad")) {
                        recorridoHistorico.addField("tipoentidad", String.class);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 185) {
                RealmObjectSchema recorridoHistorico = schema.get("RecorridoHistorico");
                if (recorridoHistorico != null && !recorridoHistorico.hasField("finalizanovedad")) {
                    recorridoHistorico.addField("finalizanovedad", boolean.class);
                }
                oldVersion++;
            }

            if (oldVersion == 186) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (schema.get("Contenedor") == null && cuenta != null) {
                    schema.create("Contenedor")
                            .addField("key", String.class, FieldAttribute.PRIMARY_KEY)
                            .addField("id", Long.class)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("codigo", String.class)
                            .addField("nombre", String.class)
                            .addField("ubicacion", String.class)
                            .addField("pti", Boolean.class)
                            .addField("eir", Boolean.class);
                }
                oldVersion++;
            }

            if (oldVersion == 187) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (schema.get("MarcaCEM") == null && cuenta != null) {
                    schema.create("MarcaCEM")
                            .addField("id", Long.class)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("name", String.class);
                }

                if (schema.get("ModeloCEM") == null && cuenta != null) {
                    schema.create("ModeloCEM")
                            .addField("id", Long.class)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("name", String.class)
                            .addField("idCuenta", Long.class);
                }

                if (schema.get("GamaCEM") == null && cuenta != null) {
                    schema.create("GamaCEM")
                            .addField("id", Long.class)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("code", String.class)
                            .addField("name", String.class);
                }

                if (schema.get("TipoFallaCEM") == null && cuenta != null) {
                    schema.create("TipoFallaCEM")
                            .addField("id", Long.class)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("code", String.class)
                            .addField("description", String.class);
                }

                if (schema.get("TipoContenedorCEM") == null && cuenta != null) {
                    schema.create("TipoContenedorCEM")
                            .addField("id", Long.class)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("name", String.class);
                }

                if (schema.get("ClasificacionCEM") == null && cuenta != null) {
                    RealmObjectSchema entity = schema.get("Entity");
                    if (entity != null) {
                        schema.create("ClasificacionCEM")
                                .addField("id", Long.class)
                                .addRealmObjectField("cuenta", cuenta)
                                .addRealmListField("type", entity)
                                .addField("classification", String.class)
                                .addField("description", String.class);
                    }
                }

                oldVersion++;
            }

            if (oldVersion == 188) {
                RealmObjectSchema cotainer = schema.get("Contenedor");
                if (cotainer != null) {
                    if (!cotainer.hasField("estado")) {
                        cotainer.addField("estado", String.class);
                    }

                    if (!cotainer.hasField("personalprogramacioneir")) {
                        cotainer.addField("personalprogramacioneir", String.class);
                    }

                    if (!cotainer.hasField("personalprogramacionpti")) {
                        cotainer.addField("personalprogramacionpti", String.class);
                    }

                    if (!cotainer.hasField("fechaultimaprogramacioneir")) {
                        cotainer.addField("fechaultimaprogramacioneir", String.class);
                    }

                    if (!cotainer.hasField("fechaultimaprogramacionpti")) {
                        cotainer.addField("fechaultimaprogramacionpti", String.class);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 189) {
                RealmObjectSchema cuenta = schema.get("Cuenta");
                if (schema.get("Seccion") == null && cuenta != null) {
                    schema.create("Seccion")
                            .addField("id", Long.class)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("code", String.class)
                            .addField("spanish", String.class)
                            .addField("english", String.class);
                }

                if (schema.get("GroupCode") == null && cuenta != null) {
                    schema.create("GroupCode")
                            .addField("id", Long.class)
                            .addRealmObjectField("cuenta", cuenta)
                            .addField("name", String.class);
                }

                oldVersion++;
            }

            if (oldVersion == 190) {
                RealmObjectSchema yarda = schema.get("Yarda");
                if (yarda != null) {
                    if (!yarda.hasField("xpti")) {
                        yarda.addField("xpti", boolean.class);
                    }

                    if (!yarda.hasField("xeir")) {
                        yarda.addField("xeir", boolean.class);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 191) {
                if (schema.get("Checklist") == null) {
                    RealmObjectSchema checklist = schema.create("Checklist")
                            .addField("id", Long.class)
                            .addField("spanish", String.class)
                            .addField("english", String.class);

                    RealmObjectSchema section = schema.get("Seccion");
                    if (section != null) {
                        section.addRealmListField("checklist", checklist);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 192) {
                RealmObjectSchema contenedor = schema.get("Contenedor");
                if (contenedor != null) {
                    if (!contenedor.hasField("fechaultimaejecucionpti")) {
                        contenedor.addField("fechaultimaejecucionpti", String.class);
                    }

                    if (!contenedor.hasField("fechaultimaejecucioneir")) {
                        contenedor.addField("fechaultimaejecucioneir", String.class);
                    }

                    if (!contenedor.hasField("personalultimaejecucioneir")) {
                        contenedor.addField("personalultimaejecucioneir", String.class);
                    }

                    if (!contenedor.hasField("personalultimaejecucionpti")) {
                        contenedor.addField("personalultimaejecucionpti", String.class);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 193) {
                RealmObjectSchema ordenTrabajo = schema.get("OrdenTrabajo");
                if (ordenTrabajo != null) {
                    if (!ordenTrabajo.hasField("cliente")) {
                        ordenTrabajo.addField("cliente", String.class);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 194) {
                RealmObjectSchema equipo = schema.get("Equipo");
                if (equipo != null) {
                    if (!equipo.hasField("cliente")) {
                        equipo.addField("cliente", String.class);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 195) {
                RealmObjectSchema realmObjectSchema = schema.get("TipoFalla");
                if (realmObjectSchema != null) {
                    if (!realmObjectSchema.hasField("xeir")) {
                        realmObjectSchema.addField("xeir", Boolean.class);
                    }

                    if (!realmObjectSchema.hasField("xpti")) {
                        realmObjectSchema.addField("xpti", Boolean.class);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 196) {
                RealmObjectSchema realmObjectSchema = schema.get("Contenedor");
                if (realmObjectSchema != null) {
                    if (!realmObjectSchema.hasField("lineanaviera")) {
                        realmObjectSchema.addField("lineanaviera", String.class);
                    }

                    if (!realmObjectSchema.hasField("equipmentgrade")) {
                        realmObjectSchema.addField("equipmentgrade", String.class);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 197) {
                RealmObjectSchema realmObjectSchema = schema.get("Checklist");
                if (realmObjectSchema != null) {
                    if (!realmObjectSchema.hasField("checked")) {
                        realmObjectSchema.addField("checked", Boolean.class);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 198) {
                RealmObjectSchema realmObjectSchema = schema.get("Seccion");
                if (realmObjectSchema != null) {
                    if (!realmObjectSchema.hasField("checked")) {
                        realmObjectSchema.addField("checked", Boolean.class);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 199) {
                RealmObjectSchema realmObjectSchema = schema.get("GamaMantenimiento");
                if (realmObjectSchema != null) {
                    if (!realmObjectSchema.hasField("requierefotos")) {
                        realmObjectSchema.addField("requierefotos", Boolean.class);
                    }

                    if (!realmObjectSchema.hasField("requiererepuestos")) {
                        realmObjectSchema.addField("requiererepuestos", Boolean.class);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 200) {
                RealmObjectSchema realmObjectSchema = schema.get("Contenedor");
                if (realmObjectSchema != null) {
                    if (!realmObjectSchema.hasField("idtipo")) {
                        realmObjectSchema.addField("idtipo", Long.class);
                    }

                    if (!realmObjectSchema.hasField("tipo")) {
                        realmObjectSchema.addField("tipo", String.class);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 201) {
                RealmObjectSchema realmObjectSchema = schema.get("Proposito");
                if (realmObjectSchema == null) {
                    RealmObjectSchema cuenta = schema.get("Cuenta");
                    if (cuenta != null) {
                        schema.create("Proposito")
                                .addField("id", Long.class)
                                .addRealmObjectField("cuenta", cuenta)
                                .addField("name", String.class);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 202) {
                RealmObjectSchema realmObjectSchema = schema.get("Parte");
                if (realmObjectSchema == null) {
                    RealmObjectSchema cuenta = schema.get("Cuenta");
                    if (cuenta != null) {
                        schema.create("Parte")
                                .addField("id", Long.class)
                                .addRealmObjectField("cuenta", cuenta)
                                .addField("name", String.class)
                                .addField("image", String.class);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 203) {
                RealmObjectSchema realmObjectSchema = schema.get("Parte");
                if (realmObjectSchema != null) {
                    if (!realmObjectSchema.hasField("ubicacion")) {
                        realmObjectSchema.addField("ubicacion", String.class);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 204) {
                RealmObjectSchema realmObjectSchema = schema.get("ModeloCEM");
                if (realmObjectSchema != null) {
                    if (realmObjectSchema.hasField("idCuenta")) {
                        realmObjectSchema.removeField("idCuenta");
                    }

                    if (!realmObjectSchema.hasField("idMarca")) {
                        realmObjectSchema.addField("idMarca", Long.class);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 205) {
                RealmObjectSchema realmObjectSchema = schema.get("Parte");
                if (realmObjectSchema != null) {
                    if (!realmObjectSchema.hasField("orden")) {
                        realmObjectSchema.addField("orden", Integer.class);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 206) {
                RealmObjectSchema realmObjectSchema = schema.get("GamaMantenimiento");
                if (realmObjectSchema != null) {
                    if (!realmObjectSchema.hasField("eir")) {
                        realmObjectSchema.addField("eir", Boolean.class);
                    }

                    if (!realmObjectSchema.hasField("pti")) {
                        realmObjectSchema.addField("pti", Boolean.class);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 207) {
                RealmObjectSchema realmObjectSchema = schema.get("Cuenta");
                if (realmObjectSchema != null) {
                    schema.create("EquipmentGrade")
                            .addField("key", String.class, FieldAttribute.PRIMARY_KEY)
                            .addField("id", Long.class)
                            .addRealmObjectField("cuenta", realmObjectSchema)
                            .addField("name", String.class);
                }

                oldVersion++;
            }

            if (oldVersion == 208) {
                RealmObjectSchema realmObjectSchema = schema.get("Contenedor");
                if (realmObjectSchema != null) {
                    RealmObjectSchema equipmentGrade = schema.get("EquipmentGrade");
                    if (equipmentGrade != null) {
                        if (!realmObjectSchema.hasField("equipmentgradevalidos")) {
                            realmObjectSchema.addRealmListField("equipmentgradevalidos", equipmentGrade);
                        }
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 209) {
                RealmObjectSchema realmObjectSchema = schema.get("Cuenta");
                if (realmObjectSchema != null && !schema.contains("EstadosInspeccion")) {
                    schema.create("EstadosInspeccion")
                            .addField("id", Long.class)
                            .addRealmObjectField("cuenta", realmObjectSchema)
                            .addField("name", String.class)
                            .addField("validate", boolean.class);
                }
                oldVersion++;
            }

            if (oldVersion == 210) {
                RealmObjectSchema realmObjectSchema = schema.get("Contenedor");
                if (realmObjectSchema != null) {
                    if (!realmObjectSchema.hasField("detalleinspeccion")) {
                        realmObjectSchema.addField("detalleinspeccion", String.class);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 211) {
                RealmObjectSchema realmObjectSchema = schema.get("Transaccion");
                if (realmObjectSchema != null) {
                    if (!realmObjectSchema.hasField("information")) {
                        realmObjectSchema.addField("information", String.class);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 212) {
                RealmObjectSchema realmObjectSchema = schema.get("Contenedor");
                if (realmObjectSchema != null) {
                    if (!realmObjectSchema.hasField("idmodelo")) {
                        realmObjectSchema.addField("idmodelo", Long.class);
                    }

                    if (!realmObjectSchema.hasField("modelo")) {
                        realmObjectSchema.addField("modelo", String.class);
                    }

                    if (!realmObjectSchema.hasField("idmarca")) {
                        realmObjectSchema.addField("idmarca", Long.class);
                    }

                    if (!realmObjectSchema.hasField("marca")) {
                        realmObjectSchema.addField("marca", String.class);
                    }

                    if (!realmObjectSchema.hasField("serial")) {
                        realmObjectSchema.addField("serial", String.class);
                    }

                    if (!realmObjectSchema.hasField("software")) {
                        realmObjectSchema.addField("software", String.class);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 213) {
                RealmObjectSchema realmObjectSchema = schema.get("Contenedor");
                if (realmObjectSchema != null) {
                    if (!realmObjectSchema.hasField("fechafabricacion")) {
                        realmObjectSchema.addField("fechafabricacion", String.class);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 214) {
                RealmObjectSchema realmObjectSchema = schema.get("RecorridoHistorico");
                if (realmObjectSchema != null) {
                    if (realmObjectSchema.hasField("fechaFin")) {
                        realmObjectSchema.removeField("fechaFin");
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 215) {
                RealmObjectSchema realmObjectSchema = schema.get("EstadosInspeccion");
                if (realmObjectSchema != null) {
                    if (!realmObjectSchema.hasField("requierefalla")) {
                        realmObjectSchema.addField("requierefalla", boolean.class);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 216) {
                RealmObjectSchema elementCode = schema.get("ElementCode");
                if (elementCode == null) {
                    RealmObjectSchema realmObjectSchema = schema.get("Cuenta");
                    if (realmObjectSchema != null) {
                        schema.create("ElementCode")
                                .addField("id", Long.class)
                                .addRealmObjectField("cuenta", realmObjectSchema)
                                .addField("name", String.class)
                                .addField("description", String.class);
                    }
                }
                oldVersion++;
            }


            if (oldVersion == 217) {
                RealmObjectSchema damageCode = schema.get("DamageCode");
                if (damageCode == null) {
                    RealmObjectSchema realmObjectSchema = schema.get("Cuenta");
                    if (realmObjectSchema != null) {
                        schema.create("DamageCode")
                                .addField("id", Long.class)
                                .addRealmObjectField("cuenta", realmObjectSchema)
                                .addField("name", String.class)
                                .addField("description", String.class)
                                .addField("require", boolean.class);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 218) {
                RealmObjectSchema realmObjectSchema = schema.get("Contenedor");
                if (realmObjectSchema != null) {
                    if (!realmObjectSchema.hasField("idclasificacion")) {
                        realmObjectSchema.addField("idclasificacion", Long.class);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 219) {
                if (!schema.contains("ListaChequeo")) {
                    RealmObjectSchema entidad = schema.get("Entidad");
                    if (entidad != null) {
                        RealmObjectSchema cuenta = schema.get("Cuenta");
                        if (cuenta != null) {
                            schema.create("ListaChequeo")
                                    .addField("id", Long.class)
                                    .addRealmObjectField("cuenta", cuenta)
                                    .addField("codigo", String.class)
                                    .addField("nombre", String.class)
                                    .addField("especialidad", String.class)
                                    .addField("descripcion", String.class)
                                    .addRealmListField("entidades", entidad);
                        }
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 220) {
                if (!schema.contains("ClienteListaChequeo")) {
                    RealmObjectSchema cliente = schema.create("ClienteListaChequeo")
                            .addField("id", Long.class)
                            .addField("nombre", String.class);

                    if (cliente != null) {
                        RealmObjectSchema chequeo = schema.get("ListaChequeo");
                        if (chequeo != null) {
                            chequeo.addRealmListField("clientes", cliente);
                        }
                    }
                }

                oldVersion++;
            }

            if (oldVersion == 221) {
                if (!schema.contains("EntidadesClienteListaChequeo")) {
                    RealmObjectSchema table = schema.create("EntidadesClienteListaChequeo")
                            .addField("id", Long.class)
                            .addField("nombre", String.class)
                            .addField("tipo", String.class)
                            .addField("prerrequisito", boolean.class)
                            .addField("idcliente", Long.class);

                    if (table != null) {
                        RealmObjectSchema chequeo = schema.get("ListaChequeo");
                        if (chequeo != null) {
                            chequeo.addRealmListField("entidadescliente", table);
                        }
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 222) {
                if (!schema.contains("PersonalListaChequeo")) {
                    RealmObjectSchema table = schema.create("PersonalListaChequeo")
                            .addField("id", Long.class)
                            .addField("idplc", Long.class)
                            .addField("codigo", String.class)
                            .addField("nombre", String.class)
                            .addField("apellido", String.class)
                            .addField("cargo", String.class)
                            .addField("tipocargo", String.class);

                    if (table != null) {
                        RealmObjectSchema chequeo = schema.get("ListaChequeo");
                        if (chequeo != null) {
                            chequeo.addRealmListField("personal", table);
                        }
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 223) {
                if (!schema.contains("Modulos")) {
                    RealmObjectSchema cuenta = schema.get("Cuenta");
                    if (cuenta != null) {
                        schema.create("Modulos")
                                .addField("UUID", String.class, FieldAttribute.PRIMARY_KEY)
                                .addRealmObjectField("cuenta", cuenta)
                                .addField("clientecmms", boolean.class);
                    }
                }
                oldVersion++;
            }

            if (oldVersion == 224) {
                RealmObjectSchema table = schema.get("EntidadesClienteListaChequeo");
                if (table != null && !table.hasField("seleccionado")) {
                    table.addField("seleccionado", boolean.class);
                }
                oldVersion++;
            }

            if (oldVersion == 225) {
                RealmObjectSchema table = schema.get("PersonalListaChequeo");
                if (table != null && !table.hasField("seleccionado")) {
                    table.addField("seleccionado", boolean.class);
                }
                oldVersion++;
            }

            if (oldVersion == 226) {
                RealmObjectSchema table = schema.get("InformacionFinanciera");
                if (table != null && !table.hasField("annosInventario")) {
                    table.addField("annosInventario", String.class);
                }
                oldVersion++;
            }

            if (oldVersion == 227) {
                RealmObjectSchema table = schema.get("EntidadesClienteListaChequeo");
                if (table != null && !table.hasField("codigo")) {
                    table.addField("codigo", String.class);
                }
                oldVersion++;
            }

            if (oldVersion == 228) {
                RealmObjectSchema table = schema.get("Transaccion");
                if (table != null && !table.hasField("prioridad")) {
                    table.addField("prioridad", int.class);
                }
                oldVersion++;
            }

            if (oldVersion == 229) {
                if (!schema.contains("Validation")) {
                    schema.create("Validation")
                            .addField("id", int.class)
                            .addField("code", String.class)
                            .addField("name", String.class)
                            .addField("expectedStartDate", String.class)
                            .addField("expectedFinishDate", String.class);
                }
                oldVersion++;
            }

            if (oldVersion == 230) {
                RealmObjectSchema table = schema.get("Validation");
                if (table != null && !table.hasPrimaryKey()) {
                    table.addPrimaryKey("id");
                }
                oldVersion++;
            }

            if (oldVersion == 231) {
                RealmObjectSchema table = schema.get("Personal");
                if (table != null && !table.hasField("grupo")) {
                    table.addField("grupo", String.class);
                }
                oldVersion++;
            }

            if (oldVersion == 232) {
                RealmObjectSchema table = schema.get("CentroCostoEquipo");
                if (table != null && table.hasField("porcentaje")) {
                    table.removeField("porcentaje");
                    table.addField("newPorcentaje", float.class);
                    table.renameField("newPorcentaje", "porcentaje");

                }
                oldVersion++;
            }

            if (oldVersion == 233) {
                RealmObjectSchema table = schema.get("ListaChequeo");
                if (table != null && !table.hasField("idFirma")) {
                    table.addField("idFirma", String.class);
                }
                oldVersion++;
            }

            if (oldVersion == 234) {
                RealmObjectSchema table = schema.get("RutaTrabajo");
                if (table != null && !table.hasField("idFirma")) {
                    table.addField("idFirma", String.class);
                }
                oldVersion++;
            }
        }

        @Override
        public int hashCode() {
            return Migration.class.hashCode();
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj == null) {
                return false;
            }
            return obj instanceof Migration;
        }
    }

    @Deprecated
    public Realm instance() {
        return realm;
    }

    @Deprecated
    public void insert(Model model) {
        this.realm.beginTransaction();
        this.realm.insert(model);
        this.realm.commitTransaction();
    }

    @Deprecated
    public void update(onQuery onQuery) {
        this.realm.beginTransaction();
        onQuery.execute();
        this.realm.commitTransaction();
    }

    @Deprecated
    public <T extends RealmModel> Model findOne(Where where, Class<T> clazz) {
        RealmQuery query = this.where(where, clazz);
        return (Model) query.findFirst();
    }

    @Deprecated
    private <T extends RealmModel> RealmQuery where(Where where, Class<T> clazz) {
        RealmQuery query = this.realm.where(clazz);
        if (where == null) {
            return query;
        }

        for (String key : where.equalTo().keySet()) {
            Object data = where.equalTo().get(key);
            if (data instanceof Boolean) {
                query.equalTo(key, (Boolean) data);
            } else if (data instanceof String) {
                query.equalTo(key, (String) data);
            } else if (data instanceof Date) {
                query.equalTo(key, (Date) data);
            } else if (data instanceof Long) {
                query.equalTo(key, (Long) data);
            }
        }

        for (String key : where.greaterThanOrEqualTo().keySet()) {
            Object data = where.equalTo().get(key);
            if (data instanceof Date) {
                query.greaterThanOrEqualTo(key, (Date) data);
            }
        }

        for (String key : where.contains().keySet()) {
            Object data = where.contains().get(key);
            if (data instanceof String) {
                query.contains(key, (String) data, Case.INSENSITIVE);
            }
        }

        for (String key : where.isNotNull()) {
            query.isNotNull(key);
        }

        return query;
    }

    @Deprecated
    public interface onQuery {
        void execute();
    }
}