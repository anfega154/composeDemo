package com.mantum.cmms.entity;

import com.google.gson.annotations.SerializedName;
import com.mantum.cmms.database.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Evento extends RealmObject implements Model, Serializable {

    @PrimaryKey
    private String UUID;

    private Cuenta cuenta;

    private Long id;

    private String fecha;

    private String evento;

    private String personal;

    private String lugar;

    private String descripcion;

    private String color;

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

    public String getEvento() {
        return evento;
    }

    public void setEvento(String evento) {
        this.evento = evento;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPersonal() {
        return personal;
    }

    public void setPersonal(String personal) {
        this.personal = personal;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public static class Request implements Serializable {

        private Integer version;

        @SerializedName("tabEventos")
        private Evento.Tab tab;

        private List<Evento> listadoEventos;

        public Request() {
            this.listadoEventos = new ArrayList<>();
        }

        public List<Evento> getPendientes() {
            return listadoEventos;
        }

        public Tab getTab() {
            return tab;
        }

        public void setTab(Tab tab) {
            this.tab = tab;
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