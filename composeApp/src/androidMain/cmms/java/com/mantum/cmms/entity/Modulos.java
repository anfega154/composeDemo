package com.mantum.cmms.entity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Modulos extends RealmObject {

    @PrimaryKey
    private String UUID;

    private Cuenta cuenta;
    private boolean clientecmms;

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

    public boolean isClientecmms() {
        return clientecmms;
    }

    public void setClientecmms(boolean clientecmms) {
        this.clientecmms = clientecmms;
    }
}
