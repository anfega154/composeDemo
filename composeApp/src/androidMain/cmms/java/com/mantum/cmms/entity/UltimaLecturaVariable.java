package com.mantum.cmms.entity;

import com.mantum.cmms.database.Model;

import java.io.Serializable;

import io.realm.RealmObject;

public class UltimaLecturaVariable extends RealmObject implements Model, Serializable {

    private Long id;

    private String fecha;

    private String valor;

    /**
     * Obtiene el id de la ultima lectura
     * @return {@link Long}
     */
    public Long getId() {
        return id;
    }

    /**
     * Setea el id de la utlima lectura
     * @param id {@link Long}
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtiene la fecha de la ultima lectura
     * @return {@link String}
     */
    public String getFecha() {
        return fecha;
    }

    /**
     * Setea la fecha de la ultima lectura
     * @param fecha {@link String}
     */
    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    /**
     * Obtiene el valor de la ultima lectura
     * @return {@link String}
     */
    public String getValor() {
        return valor;
    }

    /**
     * Setea el valor de la utlima lectura
     * @param valor {@link String}
     */
    public void setValor(String valor) {
        this.valor = valor;
    }
}