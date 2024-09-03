package com.mantum.cmms.entity;

import io.realm.RealmObject;

public class EstadoCategoria extends RealmObject {

    private int id;

    private String nombre;

    private int orden;

    private boolean afectatiempoans;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public boolean isAfectatiempoans() { return afectatiempoans; }

    public void setAfectatiempoans(boolean afectatiempoans) { this.afectatiempoans = afectatiempoans; }
}
