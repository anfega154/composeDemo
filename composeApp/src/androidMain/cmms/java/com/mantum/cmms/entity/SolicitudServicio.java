package com.mantum.cmms.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class SolicitudServicio extends RealmObject {

    public static final String SELF = "SS";

    @PrimaryKey
    private String UUID;

    private Long id;

    private Cuenta cuenta;

    private String codigo;

    private String fecha;

    private String tipo;

    private Long identidad;

    private String tipoentidad;

    private String entidad;

    private String area;

    private String fechaesperada;

    private String descripcion;

    private String fechavencimiento;

    private String prioridad;

    private String estado;

    private String solicitante;

    private RealmList<Proceso> procesos;

    private Sitio sitio;

    @SerializedName("fichatecnica")
    private InformeTecnico informeTecnico;

    @SerializedName("estadoinicial")
    private EstadoInicial estadoInicial;

    private RealmList<RecursoAdicional> recursosadicionales;

    private boolean show;

    @SerializedName("adjuntos")
    private RealmList<Adjuntos> adjuntos;

    @SerializedName("imagenes")
    private RealmList<Adjuntos> imagenes;

    private String gmap;

    public SolicitudServicio() {
        this.procesos = new RealmList<>();
        this.adjuntos = new RealmList<>();
        this.imagenes = new RealmList<>();
        this.recursosadicionales = new RealmList<>();
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Long getIdentidad() {
        return identidad;
    }

    public void setIdentidad(Long identidad) {
        this.identidad = identidad;
    }

    public String getTipoentidad() {
        return tipoentidad;
    }

    public void setTipoentidad(String tipoentidad) {
        this.tipoentidad = tipoentidad;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getFechaesperada() {
        return fechaesperada;
    }

    public void setFechaesperada(String fechaesperada) {
        this.fechaesperada = fechaesperada;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFechavencimiento() {
        return fechavencimiento;
    }

    public void setFechavencimiento(String fechavencimiento) {
        this.fechavencimiento = fechavencimiento;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(String solicitante) {
        this.solicitante = solicitante;
    }

    public RealmList<Proceso> getProcesos() {
        return procesos;
    }

    public void setProcesos(RealmList<Proceso> procesos) {
        this.procesos = procesos;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public RealmList<Adjuntos> getAdjuntos() {
        return adjuntos;
    }

    public void setAdjuntos(RealmList<Adjuntos> adjuntos) {
        this.adjuntos = adjuntos;
    }

    public RealmList<RecursoAdicional> getRecursosadicionales() {
        return recursosadicionales;
    }

    public void setRecursosadicionales(RealmList<RecursoAdicional> recursosadicionales) {
        this.recursosadicionales = recursosadicionales;
    }

    public InformeTecnico getInformeTecnico() {
        return informeTecnico;
    }

    public void setInformeTecnico(InformeTecnico informeTecnico) {
        this.informeTecnico = informeTecnico;
    }

    public EstadoInicial getEstadoInicial() {
        return estadoInicial;
    }

    public void setEstadoInicial(EstadoInicial estadoInicial) {
        this.estadoInicial = estadoInicial;
    }

    public Sitio getSitio() {
        return sitio;
    }

    public void setSitio(Sitio sitio) {
        this.sitio = sitio;
    }

    public String getGmap() {
        return gmap;
    }

    public void setGmap(String gmap) {
        this.gmap = gmap;
    }

    public RealmList<Adjuntos> getImagenes() {
        return imagenes;
    }

    public void setImagenes(RealmList<Adjuntos> imagenes) {
        this.imagenes = imagenes;
    }

    public static class Request implements Serializable {

        private Integer version;

        private List<SolicitudServicio> listaSSAsignadas;

        @SerializedName("tabSS")
        private Tab tab;

        public Request() {
            this.listaSSAsignadas = new ArrayList<>();
        }

        public List<SolicitudServicio> getPendientes() {
            return listaSSAsignadas;
        }

        public void setPendientes(List<SolicitudServicio> listaSSAsignadas) {
            this.listaSSAsignadas = listaSSAsignadas;
        }

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }

        public Tab getTab() {
            return tab;
        }
    }

    public static class Tab implements Serializable {

        private final String title;

        private final Integer value;

        private final String ordenamiento;

        private final List<Long> ids;

        public Tab(String title, Integer value, String ordenamiento, List<Long> ids) {
            this.title = title;
            this.value = value;
            this.ordenamiento = ordenamiento;
            this.ids = ids;
        }

        public String getTitle() {
            return title;
        }

        public Integer getValue() {
            return value;
        }

        public String getOrdenamiento() {
            return ordenamiento;
        }

        public List<Long> getIds() {
            return ids;
        }
    }
}