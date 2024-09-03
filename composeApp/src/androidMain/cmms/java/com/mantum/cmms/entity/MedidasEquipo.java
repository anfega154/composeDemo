package com.mantum.cmms.entity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class MedidasEquipo extends RealmObject {

    @PrimaryKey
    private Long id;

    private String sigla;

    private String idtipounidad;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescripcion() {
        return sigla;
    }

    public void setDescripcion(String descripcion) {
        this.sigla = descripcion;
    }

    public String getTipo() {
        return idtipounidad;
    }

    public void setTipo(String tipo) {
        this.idtipounidad = tipo;
    }
}
