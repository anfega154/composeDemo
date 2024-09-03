package com.mantum.cmms.entity;

import com.mantum.cmms.database.Model;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class UltimoRegistroBitacora extends RealmObject implements Model, Serializable {

    @PrimaryKey
    private String UUID;

    private Cuenta cuenta;

    private String fecha;

    private Long horainicial;

    private Long horafinal;

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

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public Long getHorainicial() {
        return horainicial;
    }

    public void setHorainicial(Long horainicial) {
        this.horainicial = horainicial;
    }

    public Long getHorafinal() {
        return horafinal;
    }

    public void setHorafinal(Long horafinal) {
        this.horafinal = horafinal;
    }
}