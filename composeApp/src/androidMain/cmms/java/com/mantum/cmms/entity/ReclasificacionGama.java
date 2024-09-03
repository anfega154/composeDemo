package com.mantum.cmms.entity;

import io.realm.RealmList;
import io.realm.RealmObject;

public class ReclasificacionGama extends RealmObject {

    private String id;
    private Cuenta cuenta;
    private String nombre;
    private RealmList<TipoReparacionGama> tiposreparacion;

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

    public RealmList<TipoReparacionGama> getTiposreparacion() {
        return tiposreparacion;
    }

    public void setTiposreparacion(RealmList<TipoReparacionGama> tiposreparacion) {
        this.tiposreparacion = tiposreparacion;
    }
}
