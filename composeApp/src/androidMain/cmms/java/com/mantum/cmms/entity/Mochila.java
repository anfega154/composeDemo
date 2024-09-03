package com.mantum.cmms.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.R;
import com.mantum.cmms.database.Model;
import com.mantum.component.adapter.handler.ViewAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmObject;

public class Mochila extends RealmObject implements Model, Serializable, ViewAdapter<Mochila> {

    private Long id;

    private Cuenta cuenta;

    private Long idphrec;

    private String codigoph;

    private String tipo;

    private String codigo;

    private String nombre;

    private String cantidad;

    private String cantidaddisponible;

    private String sigla;

    private String ubicacion;

    private String observaciones;

    public Long getIdphrec() {
        return idphrec;
    }

    public void setIdphrec(Long idphrec) {
        this.idphrec = idphrec;
    }

    public String getCodigoph() {
        return codigoph;
    }

    public void setCodigoph(String codigoph) {
        this.codigoph = codigoph;
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
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

    public String getCantidad() {
        return cantidad;
    }

    public void setCantidad(String cantidad) {
        this.cantidad = cantidad;
    }

    public String getCantidaddisponible() {
        return cantidaddisponible;
    }

    public void setCantidaddisponible(String cantidaddisponible) {
        this.cantidaddisponible = cantidaddisponible;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public boolean isOk() {
        return this.id != null && this.cantidad != null && !this.cantidad.isEmpty();
    }

    @NonNull
    @Override
    public String getTitle() {
        return getCodigo() != null ? getCodigo().trim() : "";
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return getNombre();
    }

    @Nullable
    @Override
    public String getSummary() {
        return "Cantidad: " + (getCantidad().isEmpty() ? "0" : getCantidad());
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
    public boolean compareTo(Mochila value) {
        return getId().equals(value.id);
    }

    public class Request implements Serializable {

        private List<Mochila> recursos;

        public Request() {
            this.recursos = new ArrayList<>();
        }

        public List<Mochila> getRecursos() {
            return recursos;
        }

        public void setRecursos(List<Mochila> recursos) {
            this.recursos = recursos;
        }
    }
}