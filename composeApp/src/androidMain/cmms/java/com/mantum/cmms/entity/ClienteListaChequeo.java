package com.mantum.cmms.entity;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class ClienteListaChequeo extends RealmObject {

    @SerializedName("idclientecmms")
    private Long id;

    @SerializedName("cliente")
    private String nombre;

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
}
