package com.mantum.cmms.entity;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class EstadosInspeccion extends RealmObject {

    private Long id;
    private Cuenta cuenta;
    @SerializedName("nombre")
    private String name;
    @SerializedName("bloqueaformulario")
    private boolean validate;

    private boolean requierefalla;

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

    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }

    public boolean isRequierefalla() {
        return requierefalla;
    }

    public void setRequierefalla(boolean requierefalla) {
        this.requierefalla = requierefalla;
    }

    @NonNull
    @Override
    public String toString() {
        return "EstadosInspeccion{" +
                "id=" + id +
                ", cuenta=" + cuenta +
                ", name='" + name + '\'' +
                ", validate=" + validate +
                ", requierefalla=" + requierefalla +
                '}';
    }
}
