package com.mantum.cmms.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Pendiente extends RealmObject {

    @PrimaryKey
    private String UUID;

    private Cuenta cuenta;

    private Long id;

    private String fecha;

    private String codigo;

    private String estado;

    private String criticidad;

    private String personal;

    private String descripcion;

    private String color;

    @SerializedName("imagenes")
    private RealmList<Adjuntos> adjuntos;

    private String actividadpmtto;

    private String tiempoestimadopmtto;

    public Pendiente() {
        this.adjuntos = new RealmList<>();
    }

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCriticidad() {
        return criticidad;
    }

    public void setCriticidad(String criticidad) {
        this.criticidad = criticidad;
    }

    public String getPersonal() {
        return personal;
    }

    public void setPersonal(String personal) {
        this.personal = personal;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public RealmList<Adjuntos> getAdjuntos() {
        return adjuntos;
    }

    public void setAdjuntos(RealmList<Adjuntos> adjuntos) {
        this.adjuntos = adjuntos;
    }

    public String getActividadpmtto() {
        return actividadpmtto;
    }

    public void setActividadpmtto(String actividadpmtto) {
        this.actividadpmtto = actividadpmtto;
    }

    public String getTiempoestimadopmtto() {
        return tiempoestimadopmtto;
    }

    public void setTiempoestimadopmtto(String tiempoestimadopmtto) {
        this.tiempoestimadopmtto = tiempoestimadopmtto;
    }

    public static class Request implements Serializable {

        private Integer version;

        @SerializedName("tabPendientes")
        private Tab tab;

        private List<Pendiente> listaPendientes;

        public Request() {
            this.listaPendientes = new ArrayList<>();
        }

        public List<Pendiente> getPendientes() {
            return listaPendientes;
        }

        public void setPendientes(List<Pendiente> pendientes) {
            this.listaPendientes = pendientes;
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
}