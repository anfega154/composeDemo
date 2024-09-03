package com.mantum.cmms.entity;

import com.google.gson.Gson;
import com.mantum.cmms.adapter.handler.ListadoYardasHandler;

import org.json.JSONArray;

import java.util.List;

import io.realm.RealmObject;

public class Yarda extends RealmObject implements ListadoYardasHandler<Yarda> {

    private Long id;
    private Cuenta cuenta;
    private String nombre;
    private boolean xpti;
    private boolean xeir;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isXpti() {
        return xpti;
    }

    public void setXpti(boolean xpti) {
        this.xpti = xpti;
    }

    public boolean isXeir() {
        return xeir;
    }

    public void setXeir(boolean xeir) {
        this.xeir = xeir;
    }

    @Override
    public boolean compareTo(Yarda value) {
        return getId().equals(value.id);
    }

    public String bodyGetOtsByYardas(List<String> yardasList) {
        JSONArray yardas = new JSONArray();
        for (int i = 0; i < yardasList.size(); i++) {
            yardas.put(yardasList.get(i));
        }
        return new Gson().toJson(yardas);
    }
}
