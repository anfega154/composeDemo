package com.mantum.cmms.entity.parameter;

import com.mantum.cmms.database.Model;

import java.io.Serializable;

import io.realm.RealmObject;

public class TypeArea extends RealmObject implements Model, Serializable {

    private String label;

    private Long value;

    private boolean entityrequired;

    /**
     * Obtiene el nombre del tipo de area
     * @return {@link String}
     */
    public String getLabel() {
        return label;
    }

    /**
     * Setea el nombre del tipo de area
     * @param label {@link String}
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Obtiene el valor del tipo de area
     * @return {@link String}
     */
    public Long getValue() {
        return value;
    }

    /**
     * Setea el valor del tipo de area
     * @param value {@link Long}
     */
    public void setValue(Long value) {
        this.value = value;
    }

    /**
     * Obtiene si el tipo de area requiere entidad
     * @return Verdadero si requeire de lo contrario falso
     */
    public boolean isEntityrequired() {
        return entityrequired;
    }

    /**
     * Cuidado esto se usa en un spinner
     * @return {@link String}
     */
    @Override
    public String toString() {
        return this.label;
    }
}