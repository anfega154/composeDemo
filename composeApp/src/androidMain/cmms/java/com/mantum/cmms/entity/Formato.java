package com.mantum.cmms.entity;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Formato extends RealmObject {

    @PrimaryKey
    private String uuid;

    private Cuenta cuenta;

    private Long id;

    private String formato;

    private RealmList<Category> categorias;

    public Formato() {
    }

    public Formato(String uuid, Cuenta cuenta, Long id, String formato, RealmList<Category> categorias) {
        this.uuid = uuid;
        this.cuenta = cuenta;
        this.id = id;
        this.formato = formato;
        this.categorias = categorias;
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

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public RealmList<Category> getCategorias() {
        return categorias;
    }

    public void setCategorias(RealmList<Category> categorias) {
        this.categorias = categorias;
    }


    @Override
    public String toString() {
        return this.formato;
    }

}