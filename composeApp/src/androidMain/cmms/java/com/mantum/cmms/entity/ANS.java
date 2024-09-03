package com.mantum.cmms.entity;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import io.realm.RealmObject;

public class ANS extends RealmObject {

    private String nombre;

    @SerializedName("fechavencimientoans")
    private Date vencimiento;

    private Date fechafin;

    private String prioridad;

    private String tipo;

    private String SS;

    private Integer ejecucioninicial;

    private Integer ejecucionfinal;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Date getVencimiento() {
        return vencimiento;
    }

    public void setVencimiento(Date vencimiento) {
        this.vencimiento = vencimiento;
    }

    public Integer getEjecucioninicial() {
        return ejecucioninicial;
    }

    public void setEjecucioninicial(Integer ejecucioninicial) {
        this.ejecucioninicial = ejecucioninicial;
    }

    public Integer getEjecucionfinal() {
        return ejecucionfinal;
    }

    public void setEjecucionfinal(Integer ejecucionfinal) {
        this.ejecucionfinal = ejecucionfinal;
    }

    public Date getFechafin() {
        return fechafin;
    }

    public void setFechafin(Date fechafin) {
        this.fechafin = fechafin;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getSS() {
        return SS;
    }

    public void setSS(String SS) {
        this.SS = SS;
    }
}