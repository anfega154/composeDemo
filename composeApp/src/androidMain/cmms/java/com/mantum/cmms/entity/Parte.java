package com.mantum.cmms.entity;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class Parte extends RealmObject {
    private Long id;
    private Cuenta cuenta;
    @SerializedName("nombre")
    private String name;
    @SerializedName("rutaimagen")
    private String image;
    @SerializedName("codigo")
    private String ubicacion;
    private Integer orden;

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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    @Override
    public String toString() {
        return "Parte{" +
                "id=" + id +
                ", cuenta=" + cuenta +
                ", name='" + name + '\'' +
                ", image='" + image + '\'' +
                ", ubicacion='" + ubicacion + '\'' +
                ", orden=" + orden +
                '}';
    }
}
