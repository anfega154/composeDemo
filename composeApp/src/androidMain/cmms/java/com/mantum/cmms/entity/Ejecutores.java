package com.mantum.cmms.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.R;
import com.mantum.component.adapter.handler.ViewAdapter;

import java.io.Serializable;

import io.realm.RealmObject;

public class Ejecutores extends RealmObject implements Serializable, ViewAdapter<Ejecutores> {

    private Long id;

    private String codigo;

    private String nombre;

    private String tiempo;

    private String costo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTiempo() {
        return tiempo;
    }

    public void setTiempo(String tiempo) {
        this.tiempo = tiempo;
    }

    public String getCosto() {
        return costo;
    }

    public void setCosto(String costo) {
        this.costo = costo;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getNombre() != null ? getNombre() : "";
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return getCodigo();
    }

    @Nullable
    @Override
    public String getSummary() {
        if (getTiempo() != null && !getTiempo().isEmpty()) {
            return "Duración: " + String.valueOf(getTiempo()) + " h";
        }
        return "";
    }

    @Nullable
    @Override
    public String getIcon() {
        return null;
    }

    @Nullable
    @Override
    public Integer getDrawable() {
        return R.drawable.persona;
    }

    @Override
    public boolean compareTo(Ejecutores value) {
        return getId().equals(value.id);
    }
}