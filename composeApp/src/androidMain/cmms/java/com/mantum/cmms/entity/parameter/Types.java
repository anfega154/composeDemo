package com.mantum.cmms.entity.parameter;

import com.mantum.cmms.database.Model;

import java.io.Serializable;

import io.realm.RealmObject;

/**
 * Contiene los tipos de la solicitud de servicio
 *
 * @author Jonattan Velásquez
 * @see RealmObject
 * @see Model
 * @see Serializable
 */
public class Types extends RealmObject implements Model, Serializable {

    private String label;

    private Long value;

    private boolean entityrequired;

    /**
     * Obtiene el nombre del tipo
     * @return {@link String}
     */
    public String getLabel() {
        return label;
    }

    /**
     * Setea el nombre del tipo
     * @param label {@link String}
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Obtiene el valor del tipo
     * @return {@link Long}
     */
    public Long getValue() {
        return this.value;
    }

    /**
     * Setea el valor del tipo
     * @param value {@link Long}
     */
    public void setValue(Long value) {
        this.value = value;
    }

    public boolean isEntityrequired() {
        return entityrequired;
    }

    @Override
    public String toString() {
        return this.label;
    }
}
