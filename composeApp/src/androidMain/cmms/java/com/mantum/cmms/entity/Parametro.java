package com.mantum.cmms.entity;

import com.google.gson.annotations.SerializedName;
import com.mantum.cmms.database.Model;
import com.mantum.cmms.entity.parameter.Area;
import com.mantum.cmms.entity.parameter.LogBook;
import com.mantum.cmms.entity.parameter.OT;
import com.mantum.cmms.entity.parameter.SS;
import com.mantum.cmms.entity.parameter.UserParameter;

import java.io.Serializable;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Parametro extends RealmObject implements Model, Serializable {

    @PrimaryKey
    private String UUID;
    private Cuenta cuenta;
    private boolean ejecutado;

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public boolean isEjecutado() {
        return ejecutado;
    }

    public void setEjecutado(boolean ejecutado) {
        this.ejecutado = ejecutado;
    }

    public static class Request {
        private SS ss;
        private RealmList<Area> areas;
        private LogBook logbook;
        private OT ot;
        private RealmList<UserParameter> parameters;
        private RealmList<Categoria> categories;
        private RealmList<Estado> estados;
        private RealmList<EstadoEquipo> estadoEquipos;
        private RealmList<Bodega> bodegas;
        private RealmList<InstalacionProcesoStandBy> standbyprocesos;
        private Recorrido estadoActualTecnico;
        private RealmList<Formato> formatos;
        private RealmList<TipoParo> tipoparos;
        private RealmList<ClasificacionParo> clasificacionparos;
        private RealmList<Seccion> secciones;
        private RealmList<GroupCode> groupcode;
        private RealmList<Proposito> propositos;
        private RealmList<Parte> partes;
        @SerializedName("marcascem")
        private RealmList<MarcaCEM> marcaCEM;
        @SerializedName("modeloscem")
        private RealmList<ModeloCEM> modeloCEM;
        @SerializedName("gamascem")
        private RealmList<GamaCEM> gamaCEM;
        @SerializedName("tiposfallacem")
        private RealmList<TipoFallaCEM> tipoFallaCEM;
        @SerializedName("tiposcontenedorescem")
        private RealmList<TipoContenedorCEM> tipoContenedorCEM;
        @SerializedName("clasificacionescem")
        private RealmList<ClasificacionCEM> clasificacionCEM;
        @SerializedName("estadosinspeccion")
        private RealmList<EstadosInspeccion> estadosinspeccion;
        @SerializedName("elementcodes")
        private RealmList<ElementCode> elementcodes;
        @SerializedName("damagecodes")
        private RealmList<DamageCode> damagecodes;
        private Modulos estadomodulos;

        public Request() {
            this.areas = new RealmList<>();
            this.parameters = new RealmList<>();
            this.categories = new RealmList<>();
            this.estados = new RealmList<>();
            this.estadoEquipos = new RealmList<>();
            this.bodegas = new RealmList<>();
            this.standbyprocesos = new RealmList<>();
            this.estadoActualTecnico = new Recorrido();
            this.formatos = new RealmList<>();
            this.tipoparos = new RealmList<>();
            this.clasificacionparos = new RealmList<>();
            this.marcaCEM = new RealmList<>();
            this.modeloCEM = new RealmList<>();
            this.gamaCEM = new RealmList<>();
            this.tipoFallaCEM = new RealmList<>();
            this.clasificacionCEM = new RealmList<>();
            this.tipoContenedorCEM = new RealmList<>();
            this.secciones = new RealmList<>();
            this.groupcode = new RealmList<>();
            this.propositos = new RealmList<>();
            this.estadosinspeccion = new RealmList<>();
            this.elementcodes = new RealmList<>();
            this.damagecodes = new RealmList<>();
        }

        public RealmList<DamageCode> getDamagecodes() {
            return damagecodes;
        }

        public void setDamagecodes(RealmList<DamageCode> damagecodes) {
            this.damagecodes = damagecodes;
        }

        public RealmList<ElementCode> getElementcodes() {
            return elementcodes;
        }

        public void setElementcodes(RealmList<ElementCode> elementcodes) {
            this.elementcodes = elementcodes;
        }

        public RealmList<ClasificacionCEM> getClasificacionCEM() {
            return clasificacionCEM;
        }

        public void setClasificacionCEM(RealmList<ClasificacionCEM> clasificacionCEM) {
            this.clasificacionCEM = clasificacionCEM;
        }

        public RealmList<TipoContenedorCEM> getTipoContenedorCEM() {
            return tipoContenedorCEM;
        }

        public void setTipoContenedorCEM(RealmList<TipoContenedorCEM> tipoContenedorCEM) {
            this.tipoContenedorCEM = tipoContenedorCEM;
        }

        public RealmList<TipoFallaCEM> getTipoFallaCEM() {
            return tipoFallaCEM;
        }

        public void setTipoFallaCEM(RealmList<TipoFallaCEM> tipoFallaCEM) {
            this.tipoFallaCEM = tipoFallaCEM;
        }

        public RealmList<GamaCEM> getGamaCEM() {
            return gamaCEM;
        }

        public void setGamaCEM(RealmList<GamaCEM> gamaCEM) {
            this.gamaCEM = gamaCEM;
        }

        public RealmList<ModeloCEM> getModeloCEM() {
            return modeloCEM;
        }

        public void setModeloCEM(RealmList<ModeloCEM> modeloCEM) {
            this.modeloCEM = modeloCEM;
        }

        public RealmList<MarcaCEM> getMarcaCEM() {
            return marcaCEM;
        }

        public void setMarcaCEM(RealmList<MarcaCEM> marcaCEM) {
            this.marcaCEM = marcaCEM;
        }

        public RealmList<Area> getAreas() {
            return this.areas;
        }

        public SS getSs() {
            return this.ss;
        }

        public Recorrido getEstadoActualTecnico() {
            return estadoActualTecnico;
        }

        public void setEstadoActualTecnico(Recorrido estadoActualTecnico) {
            this.estadoActualTecnico = estadoActualTecnico;
        }

        public LogBook getLogBook() {
            return this.logbook;
        }

        public OT getOt() {
            return ot;
        }

        public void setSs(SS ss) {
            this.ss = ss;
        }

        public void setAreas(RealmList<Area> areas) {
            this.areas = areas;
        }

        public void setParametros(RealmList<UserParameter> parameters) {
            this.parameters = parameters;
        }

        public RealmList<UserParameter> getParametros() {
            return this.parameters;
        }


        public LogBook getLogbook() {
            return logbook;
        }

        public void setLogbook(LogBook logbook) {
            this.logbook = logbook;
        }

        public void setOt(OT ot) {
            this.ot = ot;
        }

        public RealmList<Categoria> getCategories() {
            return categories;
        }

        public void setCategories(RealmList<Categoria> categories) {
            this.categories = categories;
        }

        public RealmList<Estado> getEstados() {
            return estados;
        }

        public void setEstados(RealmList<Estado> estados) {
            this.estados = estados;
        }

        public RealmList<EstadoEquipo> getEstadoEquipos() {
            return estadoEquipos;
        }

        public RealmList<Bodega> getBodegas() {
            return bodegas;
        }

        public RealmList<InstalacionProcesoStandBy> getStandbyprocesos() {
            return standbyprocesos;
        }

        public RealmList<Formato> getFormatos() {
            return formatos;
        }

        public void setFormatos(RealmList<Formato> formatos) {
            this.formatos = formatos;
        }

        public RealmList<TipoParo> getTipoparos() {
            return tipoparos;
        }

        public void setTipoparos(RealmList<TipoParo> tipoparos) {
            this.tipoparos = tipoparos;
        }

        public RealmList<ClasificacionParo> getClasificacionparos() {
            return clasificacionparos;
        }

        public void setClasificacionparos(RealmList<ClasificacionParo> clasificacionparos) {
            this.clasificacionparos = clasificacionparos;
        }

        public RealmList<Seccion> getSecciones() {
            return secciones;
        }

        public void setSecciones(RealmList<Seccion> secciones) {
            this.secciones = secciones;
        }

        public RealmList<GroupCode> getGroupcode() {
            return groupcode;
        }

        public void setGroupcode(RealmList<GroupCode> groupcode) {
            this.groupcode = groupcode;
        }

        public RealmList<Proposito> getPropositos() {
            return propositos;
        }

        public void setPropositos(RealmList<Proposito> propositos) {
            this.propositos = propositos;
        }

        public RealmList<Parte> getPartes() {
            return partes;
        }

        public void setPartes(RealmList<Parte> partes) {
            this.partes = partes;
        }

        public RealmList<EstadosInspeccion> getEstadosinspeccion() {
            return estadosinspeccion;
        }

        public void setEstadosinspeccion(RealmList<EstadosInspeccion> estadosinspeccion) {
            this.estadosinspeccion = estadosinspeccion;
        }

        public Modulos getEstadoModulos() {
            return estadomodulos;
        }

        public void setEstadoModulos(Modulos estadomodulos) {
            this.estadomodulos = estadomodulos;
        }
    }
}