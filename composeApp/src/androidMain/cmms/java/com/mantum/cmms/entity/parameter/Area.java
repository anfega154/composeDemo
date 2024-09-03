package com.mantum.cmms.entity.parameter;

import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.database.Model;

import java.io.Serializable;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Area extends RealmObject implements Model, Serializable {

    @PrimaryKey
    private String UUID;

    private Cuenta cuenta;

    private String label;

    private Long value;

    private RealmList<TypeArea> types;

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public RealmList<TypeArea> getTypes() {
        return types;
    }

    public void setTypes(RealmList<TypeArea> types) {
        this.types = types;
    }

    public Area() {
    }

    public Area(String label, Long value) {
        this.label = label;
        this.value = value;
    }

    @Override
    public String toString() {
        return label; // OJO AL CAMBIAR
    }
}