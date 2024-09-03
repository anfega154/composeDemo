package com.mantum.cmms.entity.parameter;

import androidx.annotation.NonNull;

import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.database.Model;

import java.io.Serializable;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class OT extends RealmObject implements Model, Serializable {

    @PrimaryKey
    private String UUID;

    private Cuenta cuenta;

    private RealmList<StateReceive> statereceive;

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

    public RealmList<StateReceive> getStatereceive() {
        return statereceive;
    }

    public void setStatereceive(RealmList<StateReceive> statereceive) {
        this.statereceive = statereceive;
    }

    @NonNull
    @Override
    public String toString() {
        return "OT{" +
                "UUID='" + UUID + '\'' +
                ", cuenta=" + cuenta +
                ", statereceive=" + statereceive +
                '}';
    }
}