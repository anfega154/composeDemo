package com.mantum.cmms.entity.parameter;

import com.mantum.cmms.database.Model;

import java.io.Serializable;

import io.realm.RealmObject;

/**
 * Contiene las prioridades de la solicitud de servicio
 *
 * @author Jonattan Velásquez
 * @see RealmObject
 * @see Model
 * @see Serializable
 */
public class Priorities extends RealmObject implements Model, Serializable {

    private String label;

    private String value;

    /**
     * Obtiene el nombre de la prioridad
     * @return {@link String}
     */
    public String getLabel() {
        return label;
    }

    /**
     * Setea el nombre de la prioridad
     * @param label {@link String}
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Obtiene el valor de la prioridad
     * @return {@link String}
     */
    public String getValue() {
        return value;
    }

    /**
     * Setea el valor de la prioridad
     * @param value {@link String}
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.label;
    }
}
