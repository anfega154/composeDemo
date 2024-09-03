package com.mantum.cmms.entity;

import io.realm.RealmList;
import io.realm.RealmObject;

public class SubtipoReparacionGama extends RealmObject {

    private String id;
    private Cuenta cuenta;
    private String nombre;
    private RealmList<GamaMantenimiento> gamas;

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

    public RealmList<GamaMantenimiento> getGamas() {
        return gamas;
    }

    public void setGamas(RealmList<GamaMantenimiento> gamas) {
        this.gamas = gamas;
    }

    @Override
    public String toString() {
        return "SubtipoReparacionGama{" +
                "id='" + id + '\'' +
                ", cuenta=" + cuenta +
                ", nombre='" + nombre + '\'' +
                ", gamas=" + gamas +
                '}';
    }
}
