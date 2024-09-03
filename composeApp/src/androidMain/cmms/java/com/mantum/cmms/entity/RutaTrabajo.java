package com.mantum.cmms.entity;

import com.google.gson.annotations.SerializedName;
import com.mantum.component.mapped.IgnoreField;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RutaTrabajo extends RealmObject {

    public static final String SELF = "RT";

    @PrimaryKey
    private String UUID;

    //id del grupo de RTs
    private Long id;

    @IgnoreField
    private Cuenta cuenta;

    //id de la RT
    private Long idejecucion;

    private String codigo;

    private String nombre;

    private String fecha;

    private String especialidad;

    private String descripcion;

    private RealmList<Entidad> entidades;

    private RealmList<Recurso> recursos;

    @SerializedName("imagenes")
    private RealmList<Adjuntos> imagenes;

    @SerializedName("adjuntos")
    private RealmList<Adjuntos> adjuntos;

    private RealmList<Entity> amxgrupos;

    @Nullable
    private String tipogrupo = null;

    @IgnoreField
    private boolean diligenciada;

    @IgnoreField
    private Long idot;

    private String idFirma;

    private boolean show; // Si es true se debe de mostrar en descargar de rutas de trabajo

    @IgnoreField
    private boolean multiple;

    public RutaTrabajo() {
        this.entidades = new RealmList<>();
        this.recursos = new RealmList<>();
        this.imagenes = new RealmList<>();
        this.adjuntos = new RealmList<>();
        this.amxgrupos = new RealmList<>();
        this.idFirma = null;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public Long getId() {
        return this.id;
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

    public Long getIdejecucion() {
        return this.idejecucion;
    }

    public void setIdejecucion(Long idejecucion) {
        this.idejecucion = idejecucion;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public RealmList<Entidad> getEntidades() {
        return entidades;
    }

    public void setEntidades(RealmList<Entidad> entidades) {
        this.entidades = entidades;
    }

    public RealmList<Recurso> getRecursos() {
        return recursos;
    }

    public void setRecursos(RealmList<Recurso> recursos) {
        this.recursos = recursos;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public RealmList<Adjuntos> getImagenes() {
        return imagenes;
    }

    public void setImagenes(RealmList<Adjuntos> imagenes) {
        this.imagenes = imagenes;
    }

    public RealmList<Adjuntos> getAdjuntos() {
        return adjuntos;
    }

    public void setAdjuntos(RealmList<Adjuntos> adjuntos) {
        this.adjuntos = adjuntos;
    }

    public RealmList<Entity> getAmxgrupos() {
        return amxgrupos;
    }

    public void setAmxgrupos(RealmList<Entity> amxgrupos) {
        this.amxgrupos = amxgrupos;
    }

    public String getTipogrupo() {
        return tipogrupo;
    }

    public void setTipogrupo(String tipogrupo) {
        this.tipogrupo = tipogrupo;
    }

    public boolean isDiligenciada() { return diligenciada; }

    public void setDiligenciada(boolean diligenciada) { this.diligenciada = diligenciada; }

    public Long getIdot() { return idot; }

    public void setIdot(Long idot) { this.idot = idot; }

    public boolean isMultiple() { return multiple; }

    public void setMultiple(boolean multiple) { this.multiple = multiple; }

    public String getIdFirma() {
        return idFirma;
    }

    public void setIdFirma(String idFirma) {
        this.idFirma = idFirma;
    }

    public static class Request implements Serializable {

        private Integer version;

        @SerializedName("tabRT")
        private Tab tab;

        private List<RutaTrabajo> listaRT;

        public Request() {
            this.listaRT = new ArrayList<>();
        }

        public List<RutaTrabajo> getPendientes() {
            return listaRT;
        }

        public void setPendientes(List<RutaTrabajo> listaRT) {
            this.listaRT = listaRT;
        }

        public Tab getTab() {
            return tab;
        }

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
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

    @Override
    public String toString() {
        return "RutaTrabajo{" +
                "UUID='" + UUID + '\'' +
                ", id=" + id +
                ", cuenta=" + cuenta +
                ", idejecucion=" + idejecucion +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", fecha='" + fecha + '\'' +
                ", especialidad='" + especialidad + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", entidades=" + entidades +
                ", recursos=" + recursos +
                ", imagenes=" + imagenes +
                ", adjuntos=" + adjuntos +
                ", amxgrupos=" + amxgrupos +
                ", tipogrupo='" + tipogrupo + '\'' +
                ", diligenciada='" + diligenciada + '\'' +
                ", idot='" + idot + '\'' +
                ", show=" + show +
                ", idfirma=" + idFirma +
                '}';
    }
}