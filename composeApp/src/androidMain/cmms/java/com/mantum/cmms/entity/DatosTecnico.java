package com.mantum.cmms.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.component.adapter.handler.ViewAdapter;

import io.realm.RealmObject;

public class DatosTecnico extends RealmObject implements ViewAdapter<DatosTecnico> {

    private String nombre;

    private String idtipodatotecnico;

    private String otronombre;

    private String valor;

    private String tolerancia;

    private String descripcion;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getIdtipodatotecnico() {
        return idtipodatotecnico;
    }

    public void setIdtipodatotecnico(String idtipodatotecnico) {
        this.idtipodatotecnico = idtipodatotecnico;
    }

    public String getOtronombre() {
        return otronombre;
    }

    public void setOtronombre(String otronombre) {
        this.otronombre = otronombre;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getTolerancia() {
        return tolerancia;
    }

    public void setTolerancia(String tolerancia) {
        this.tolerancia = tolerancia;
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
        return getNombre();
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return getValor();
    }

    @Nullable
    @Override
    public String getSummary() {
        return getIdtipodatotecnico();
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
    public boolean compareTo(DatosTecnico value) {
        return false;
    }
}
