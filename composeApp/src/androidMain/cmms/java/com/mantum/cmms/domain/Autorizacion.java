package com.mantum.cmms.domain;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.mantum.cmms.entity.Autorizaciones;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Autorizacion implements Serializable {

    private String nombre;

    private boolean acceso;

    @SerializedName("data")
    private List<Autorizaciones> autorizaciones;

    public Autorizacion() {
        autorizaciones = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isAcceso() {
        return acceso;
    }

    public void setAcceso(boolean acceso) {
        this.acceso = acceso;
    }

    public List<Autorizaciones> getAutorizaciones() {
        return autorizaciones;
    }

    public void setAutorizaciones(List<Autorizaciones> autorizaciones) {
        this.autorizaciones = autorizaciones;
    }

    public static class Request implements Serializable {

        private final Long id;

        private final String cedula;

        private final String modulo;

        private final String token;

        private final String fechafinal;

        public Request(Long id, String cedula, String modulo, String fechafinal) {
            this.id = id;
            this.cedula = cedula;
            this.modulo = modulo;
            this.token = UUID.randomUUID().toString();
            this.fechafinal = fechafinal;
        }

        public Long getId() {
            return id;
        }

        public String getCedula() {
            return cedula;
        }

        public String getModulo() {
            return modulo;
        }

        public String getToken() {
            return token;
        }

        public String getFechafinal() {
            return fechafinal;
        }

        public String toJson() {
            return new Gson().toJson(this);
        }
    }
}