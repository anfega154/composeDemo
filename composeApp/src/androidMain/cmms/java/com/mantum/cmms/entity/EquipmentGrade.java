package com.mantum.cmms.entity;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class EquipmentGrade extends RealmObject {

    @PrimaryKey
    private String key;
    private Long id;
    private Cuenta cuenta;
    @SerializedName("clasificacion")
    private String name;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

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

    @NonNull
    @Override
    public String toString() {
        return "EquipmentGrade{" +
                "key='" + key + '\'' +
                ", id=" + id +
                ", cuenta=" + cuenta +
                ", name='" + name + '\'' +
                '}';
    }
}
