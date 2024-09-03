package com.mantum.cmms.entity;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class PendienteMantenimiento extends RealmObject implements Serializable {

    @PrimaryKey
    private String uuid;

    private Float tiempoestimado;

    private String actividad;

    private String descripcion;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Float getTiempoestimado() {
        return tiempoestimado;
    }

    public void setTiempoestimado(Float tiempoestimado) {
        this.tiempoestimado = tiempoestimado;
    }

    public String getActividad() {
        return actividad;
    }

    public void setActividad(String actividad) {
        this.actividad = actividad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public static class Request implements Serializable {

        private Float tiempoestimado;

        private String actividad;

        private String descripcion;

        public Float getTiempoestimado() {
            return tiempoestimado;
        }

        public void setTiempoestimado(Float tiempoestimado) {
            this.tiempoestimado = tiempoestimado;
        }

        public String getActividad() {
            return actividad;
        }

        public void setActividad(String actividad) {
            this.actividad = actividad;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }
    }
}