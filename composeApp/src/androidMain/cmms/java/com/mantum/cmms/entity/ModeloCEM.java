package com.mantum.cmms.entity;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class ModeloCEM extends RealmObject {
    private Long id;
    private Cuenta cuenta;
    private String name;
    @SerializedName("idmarca")
    private Long idMarca;

    public Long getId() {
        return id;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getIdMarca() {
        return idMarca;
    }

    public void setIdMarca(Long idMarca) {
        this.idMarca = idMarca;
    }

    @Override
    public String toString() {
        return "ModeloCEM{" +
                "id=" + id +
                ", cuenta=" + cuenta +
                ", name='" + name + '\'' +
                ", idMarca=" + idMarca +
                '}';
    }
}
