package com.mantum.cmms.entity;

import io.realm.RealmObject;

public class EstadoEquipo extends RealmObject {

    private String nombre;

    public String getEstado() {
        return nombre;
    }

    public void setEstado(String nombre) {
        this.nombre = nombre;
    }

}
