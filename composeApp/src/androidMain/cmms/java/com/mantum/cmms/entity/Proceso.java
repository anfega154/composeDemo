package com.mantum.cmms.entity;

import androidx.annotation.NonNull;

import com.mantum.cmms.database.Model;
import com.mantum.component.adapter.handler.ViewTimeLineAdapter;

import io.realm.RealmObject;

public class Proceso extends RealmObject implements Model, ViewTimeLineAdapter<Proceso> {

    private String UUID;

    private String estado;

    private String fecharegistro;

    private Long idresponsable;

    private String responsable;

    private String fechaejecucion;

    private String descripcion;

    private String nota;

    public Proceso() {
        this.UUID = java.util.UUID.randomUUID().toString();
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFecharegistro() {
        return fecharegistro;
    }

    public void setFecharegistro(String fecharegistro) {
        this.fecharegistro = fecharegistro;
    }

    public Long getIdresponsable() {
        return idresponsable;
    }

    public void setIdresponsable(Long idresponsable) {
        this.idresponsable = idresponsable;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public String getFechaejecucion() {
        return fechaejecucion;
    }

    public void setFechaejecucion(String fechaejecucion) {
        this.fechaejecucion = fechaejecucion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

    @Override
    public boolean compareTo(Proceso value) {
        return getUUID().equals(value.getUUID());
    }

    @NonNull
    @Override
    public String getDate() {
        return getFechaejecucion();
    }

    @NonNull
    @Override
    public String getTitle() {
        return getEstado();
    }

    @NonNull
    @Override
    public String getMessage() {
        return getDescripcion();
    }
}