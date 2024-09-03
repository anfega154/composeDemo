package com.mantum.cmms.entity;

import com.mantum.cmms.database.Model;

import java.io.Serializable;

import io.realm.RealmObject;

public class VariableCualitativa extends RealmObject implements Model, Serializable {

    public static final String NAME = "Cualitativa";

    private Long id;

    private String valor;

    private String descripcion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return this.getValor();
    }
}