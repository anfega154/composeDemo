package com.mantum.cmms.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mantum.R;
import com.mantum.cmms.adapter.handler.ListadoRepuestosHandler;
import com.mantum.component.adapter.handler.ViewAdapter;

import java.io.Serializable;

import io.realm.RealmObject;

public class RepuestoManual extends RealmObject implements ViewAdapter<RepuestoManual> {

    private Long id;

    private Cuenta cuenta;

    @SerializedName("repuesto")
    private String nombre;

    private String serial;

    @SerializedName("serialretiro")
    private String serialRetiro;

    private int idfalla;

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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getSerialRetiro() {
        return serialRetiro;
    }

    public void setSerialRetiro(String serialRetiro) {
        this.serialRetiro = serialRetiro;
    }

    public int getIdfalla() {
        return idfalla;
    }

    public void setIdfalla(int idfalla) {
        this.idfalla = idfalla;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getNombre() != null ? getNombre() : "";
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return getSerial() != null ? "Serial: " + getSerial() : "";
    }

    @Nullable
    @Override
    public String getSummary() {
        return getSerialRetiro() != null ? "Serial retiro: " + getSerialRetiro() : "";
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
    public boolean compareTo(RepuestoManual value) {
            return getId().equals(value.id);
    }

    public static class Repuesto implements ListadoRepuestosHandler<Repuesto>, Serializable {

        private Long id;

        private String serial;

        private String nombre;

        private String serialRetiro;

        public Repuesto() {
        }

        public Repuesto(String serial, String nombre, String serialRetiro) {
            this.serial = serial;
            this.nombre = nombre;
            this.serialRetiro = serialRetiro;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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

        public String getSerialRetiro() {
            return serialRetiro;
        }

        public void setSerialRetiro(String serialRetiro) {
            this.serialRetiro = serialRetiro;
        }

        @Override
        public boolean compareTo(Repuesto value) {
            return false;
        }
    }
}
