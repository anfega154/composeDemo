package com.mantum.cmms.entity;

import io.realm.RealmList;
import io.realm.RealmObject;

public class TipoReparacionGama extends RealmObject {

    private String id;
    private Cuenta cuenta;
    private String nombre;
    private RealmList<SubtipoReparacionGama> subtiposreparacion;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public RealmList<SubtipoReparacionGama> getSubtiposreparacion() {
        return subtiposreparacion;
    }

    public void setSubtiposreparacion(RealmList<SubtipoReparacionGama> subtiposreparacion) {
        this.subtiposreparacion = subtiposreparacion;
    }

    @Override
    public String toString() {
        return "TipoReparacionGama{" +
                "id='" + id + '\'' +
                ", cuenta=" + cuenta +
                ", nombre='" + nombre + '\'' +
                ", subtiposreparacion=" + subtiposreparacion +
                '}';
    }
}
