package com.mantum.cmms.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.component.adapter.handler.ViewAdapter;

public class FallaEquipo implements ViewAdapter<FallaEquipo> {

    private String UUID;

    private Long id;

    private String resumen;

    private String am;

    private String descripcion;

    public FallaEquipo(String UUID, Long id, String resumen, String am, String descripcion) {
        this.UUID = UUID;
        this.id = id;
        this.resumen = resumen;
        this.am = am;
        this.descripcion = descripcion;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResumen() {
        return resumen;
    }

    public void setResumen(String resumen) {
        this.resumen = resumen;
    }

    public String getAm() {
        return am;
    }

    public void setAm(String am) {
        this.am = am;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getResumen() != null ? getResumen() : "";
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return getAm() != null ? getAm() : "";
    }

    @Nullable
    @Override
    public String getSummary() {
        return getDescripcion() != null ? getDescripcion() : "";
    }

    @Nullable
    @Override
    public String getIcon() {
        return null;
    }

    @Nullable
    @Override
    public Integer getDrawable() {
        return null;
    }

    @Override
    public boolean compareTo(FallaEquipo value) {
        return getId().equals(value.id);
    }
}
