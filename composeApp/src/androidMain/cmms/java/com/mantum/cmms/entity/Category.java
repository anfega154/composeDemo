package com.mantum.cmms.entity;

import io.realm.RealmObject;

public class Category extends RealmObject {

    private String uuid;

    private Cuenta cuenta;

    private Long id;

    private String nombre;

    private Boolean firma;

    public Category() {
    }

    public Category(String uuid, Cuenta cuenta, Long id, String nombre, Boolean firma) {
        this.uuid = uuid;
        this.cuenta = cuenta;
        this.id = id;
        this.nombre = nombre;
        this.firma = firma;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getFirma() {
        return firma;
    }

    public void setFirma(Boolean firma) {
        this.firma = firma;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
