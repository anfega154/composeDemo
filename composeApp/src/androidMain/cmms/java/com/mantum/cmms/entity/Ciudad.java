package com.mantum.cmms.entity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Ciudad extends RealmObject {

    @PrimaryKey
    private Long id;

    private String nombre;

    private Long iddepartamento;

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

    public Long getIddepartamento() {
        return iddepartamento;
    }

    public void setIddepartamento(Long iddepartamento) {
        this.iddepartamento = iddepartamento;
    }
}
