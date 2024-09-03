package com.mantum.cmms.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.cmms.adapter.handler.CentroCostoHandler;
import com.mantum.component.adapter.handler.ViewAdapter;

import io.realm.RealmObject;

public class CentroCostoEquipo extends RealmObject implements CentroCostoHandler<CentroCostoEquipo>, ViewAdapter<CentroCostoEquipo> {

    private Long id;

    private Cuenta cuenta;

    private String codigo;

    private String nombre;

    private float porcentaje;

    public CentroCostoEquipo() {
    }

    public CentroCostoEquipo(Long id, String codigo, String nombre, float porcentaje) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.porcentaje = porcentaje;
    }

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

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public float getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(float porcentaje) {
        this.porcentaje = porcentaje;
    }

    @Override
    public boolean compareTo(CentroCostoEquipo value) {
        return getId().equals(value.id);
    }

    @NonNull
    @Override
    public String getTitle() {
        return getCodigo();
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return getNombre();
    }

    @Nullable
    @Override
    public String getSummary() {
        return "Centro de costo";
    }

    @Nullable
    @Override
    public String getIcon() {
        return null;
    }

    @Nullable
    @Override
    public Integer getDrawable() {
        return null;
    }

    @Override
    public String toString() {
        return "CentroCostoEquipo{" +
                "id=" + id +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", porcentaje=" + porcentaje +
                '}';
    }
}
