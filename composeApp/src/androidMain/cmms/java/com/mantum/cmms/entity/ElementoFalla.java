package com.mantum.cmms.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mantum.demo.R;
import com.mantum.component.adapter.handler.ViewAdapter;

import io.realm.RealmObject;

public class ElementoFalla extends RealmObject implements ViewAdapter<ElementoFalla> {

    private Long id;

    private Cuenta cuenta;

    @SerializedName("serialelemento")
    private String serial;

    private String nombre;

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

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getNombre() != null ? getNombre() : "";
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return getSerial() != null ? getSerial() : "";
    }

    @Nullable
    @Override
    public String getSummary() {
        return null;
    }

    @Nullable
    @Override
    public String getIcon() {
        return null;
    }

    @Nullable
    @Override
    public Integer getDrawable() {
        return R.drawable.recursos;
    }

    @Override
    public boolean compareTo(ElementoFalla value) {
        return getId().equals(value.id);
    }
}
