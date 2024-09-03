package com.mantum.cmms.entity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Departamento extends RealmObject {

    @PrimaryKey
    private Long id;

    private String nombre;

    private Long idpais;

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

    public Long getIdpais() {
        return idpais;
    }

    public void setIdpais(Long idpais) {
        this.idpais = idpais;
    }
}
