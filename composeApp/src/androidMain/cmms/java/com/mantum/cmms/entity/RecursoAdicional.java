package com.mantum.cmms.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.R;
import com.mantum.cmms.database.Model;
import com.mantum.component.adapter.handler.ViewAdapter;
import com.mantum.component.mapped.IgnoreField;

import java.io.Serializable;
import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RecursoAdicional extends RealmObject
        implements Model, Serializable, ViewAdapter<RecursoAdicional> {

    @PrimaryKey
    @IgnoreField
    private String uuid;

    private Long id;

    @IgnoreField
    private Cuenta cuenta;

    private String nombre;

    private String cantidad;

    private String unidad;

    private String referencia;

    private boolean utilizado;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCantidad() {
        return cantidad;
    }

    public void setCantidad(String cantidad) {
        this.cantidad = cantidad;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public boolean isUtilizado() {
        return utilizado;
    }

    public void setUtilizado(boolean utilizado) {
        this.utilizado = utilizado;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getNombre();
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return "Cantidad: " + getCantidad();
    }

    @Nullable
    @Override
    public String getSummary() {
        return getUnidad();
    }

    @Nullable
    @Override
    public String getIcon() {
        return null;
    }

    @Nullable
    @Override
    public Integer getDrawable() {
        return R.drawable.recursos;
    }

    @Override
    public boolean compareTo(RecursoAdicional value) {
        return false;
    }

    public static class Request {

        private final List<RecursoAdicional> recursos;

        public Request(List<RecursoAdicional> recursos) {
            this.recursos = recursos;
        }

        public List<RecursoAdicional> getRecursos() {
            return recursos;
        }
    }
}