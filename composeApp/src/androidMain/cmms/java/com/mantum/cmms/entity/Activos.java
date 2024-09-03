package com.mantum.cmms.entity;

import com.mantum.cmms.database.Model;

import java.io.Serializable;

import io.realm.RealmObject;

public class Activos extends RealmObject implements Model, Serializable {

    private Long id;

    private Long idequipo;

    private String equipo;

    private String entidadenvio;

    private String responsable;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdequipo() {
        return idequipo;
    }

    public void setIdequipo(Long idequipo) {
        this.idequipo = idequipo;
    }

    public String getEquipo() {
        return equipo;
    }

    public void setEquipo(String equipo) {
        this.equipo = equipo;
    }

    public String getEntidadenvio() {
        return entidadenvio;
    }

    public void setEntidadenvio(String entidadenvio) {
        this.entidadenvio = entidadenvio;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

}
