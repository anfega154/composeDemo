package com.mantum.cmms.entity.parameter;

import com.mantum.cmms.database.Model;

import java.io.Serializable;

import io.realm.RealmObject;

/**
 * Contiene los estados de la orden de trabajo y solicitud de servicio
 *
 * @author Jonattan Velásquez
 * @see RealmObject
 * @see Model
 * @see Serializable
 */
public class StateReceive extends RealmObject implements Model, Serializable {

    private String id;

    private String nombre;

    /**
     * Obtiene el identificador del objeto
     * @return {@link String}
     */
    public String getId() {
        return id;
    }

    /**
     * Setea el identificador del objeto
     * @param id {@link String}
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Obtiene el nombre del estado
     * @return {@link String}
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Setea el nombre del estado
     * @param nombre {@link String}
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return this.nombre;
    }
}
