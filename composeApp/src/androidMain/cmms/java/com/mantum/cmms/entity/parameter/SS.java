package com.mantum.cmms.entity.parameter;

import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.database.Model;

import java.io.Serializable;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class SS extends RealmObject implements Model, Serializable {

    @PrimaryKey
    private String UUID;

    private Cuenta cuenta;

    private RealmList<Priorities> priorities;

    private RealmList<Types> types;

    private boolean arearequired;

    private boolean modifycreatedate;

    private RealmList<StateReceive> statereceive;

    private RealmList<AspectEval> aspecteval;

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

    public RealmList<Priorities> getPriorities() {
        return priorities;
    }

    public void setPriorities(RealmList<Priorities> priorities) {
        this.priorities = priorities;
    }

    public RealmList<Types> getTypes() {
        return types;
    }

    public void setTypes(RealmList<Types> types) {
        this.types = types;
    }

    public boolean isArearequired() {
        return arearequired;
    }

    public void setArearequired(boolean arearequired) {
        this.arearequired = arearequired;
    }

    public boolean isModifycreatedate() {
        return modifycreatedate;
    }

    public void setModifycreatedate(boolean modifycreatedate) {
        this.modifycreatedate = modifycreatedate;
    }

    public RealmList<StateReceive> getStatereceive() {
        return statereceive;
    }

    public void setStatereceive(RealmList<StateReceive> statereceive) {
        this.statereceive = statereceive;
    }

    public RealmList<AspectEval> getAspecteval() {
        return aspecteval;
    }

    public void setAspecteval(RealmList<AspectEval> aspecteval) {
        this.aspecteval = aspecteval;
    }

    @Override
    public String toString() {
        return "SS{" +
                "UUID='" + UUID + '\'' +
                ", cuenta=" + cuenta +
                ", priorities=" + priorities +
                ", types=" + types +
                ", arearequired=" + arearequired +
                ", modifycreatedate=" + modifycreatedate +
                ", statereceive=" + statereceive +
                ", aspecteval=" + aspecteval +
                '}';
    }
}