package com.mantum.cmms.entity.parameter;

import io.realm.RealmObject;

public class EventType extends RealmObject {

    private Long id;

    private String nombre;

    private boolean requiereentidad;

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

    @Override
    public String toString() {
        return this.nombre;
    }

    public boolean isRequiereentidad() {
        return requiereentidad;
    }

    public void setRequiereentidad(boolean requiereentidad) {
        this.requiereentidad = requiereentidad;
    }
}
