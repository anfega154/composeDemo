package com.mantum.cmms.entity;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class MarcaCEM extends RealmObject {
    private Long id;
    private Cuenta cuenta;
    @SerializedName("marca")
    private String name;

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

    @Override
    public String toString() {
        return "MarcaCEM{" +
                "id=" + id +
                ", cuenta=" + cuenta +
                ", name='" + name + '\'' +
                '}';
    }
}
