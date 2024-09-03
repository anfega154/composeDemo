package com.mantum.cmms.entity;

import io.realm.RealmObject;

public class TipoFalla extends RealmObject {

    private Cuenta cuenta;
    private String tipo;
    private String descripcion;
    private Boolean xeir;
    private Boolean xpti;

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }


    public Boolean getXeir() {
        return xeir;
    }

    public void setXeir(Boolean xeir) {
        this.xeir = xeir;
    }

    public Boolean getXpti() {
        return xpti;
    }

    public void setXpti(Boolean xpti) {
        this.xpti = xpti;
    }
}
