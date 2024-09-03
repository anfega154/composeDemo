package com.mantum.cmms.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.demo.R;
import com.mantum.cmms.adapter.handler.ListadoConsumiblesHandler;
import com.mantum.component.adapter.handler.ViewAdapter;

import java.io.Serializable;

import io.realm.RealmObject;

public class Consumible extends RealmObject implements ViewAdapter<Consumible> {

    private Long id;

    private Cuenta cuenta;

    private String nombre;

    private Double cantidadestimada;

    private Double costoestimado;

    private Double cantidad;

    private Double costoreal;

    private Double valorunitario;

    private int idunidadmedida;

    private int idfalla;

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

    public Double getCantidadestimada() {
        return cantidadestimada;
    }

    public void setCantidadestimada(Double cantidadestimada) {
        this.cantidadestimada = cantidadestimada;
    }

    public Double getCostoestimado() {
        return costoestimado;
    }

    public void setCostoestimado(Double costoestimado) {
        this.costoestimado = costoestimado;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public void setCantidad(Double cantidad) {
        this.cantidad = cantidad;
    }

    public Double getCostoreal() {
        return costoreal;
    }

    public void setCostoreal(Double costoreal) {
        this.costoreal = costoreal;
    }

    public Double getValorunitario() {
        return valorunitario;
    }

    public void setValorunitario(Double valorunitario) {
        this.valorunitario = valorunitario;
    }

    public int getIdunidadmedida() {
        return idunidadmedida;
    }

    public void setIdunidadmedida(int idunidadmedida) {
        this.idunidadmedida = idunidadmedida;
    }

    public int getIdfalla() {
        return idfalla;
    }

    public void setIdfalla(int idfalla) {
        this.idfalla = idfalla;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getNombre() != null ? getNombre() : "";
    }

    @Nullable
    @Override
    public String getSubtitle() {
        if (getCantidad() != null && getCantidad() > 0) {
            return "Cantidad: " + getCantidad();
        }
        return "";
    }

    @Nullable
    @Override
    public String getSummary() {
        return null;
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
    public boolean compareTo(Consumible value) {
        return getId().equals(value.id);
    }

    public static class ConsumibleHelper implements ListadoConsumiblesHandler<ConsumibleHelper>, Serializable {

        private Long id;

        private String nombre;

        private Double cantidadreal;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        @Override
        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        @Override
        public Double getCantidadreal() {
            return cantidadreal;
        }

        public void setCantidadreal(Double cantidadreal) {
            this.cantidadreal = cantidadreal;
        }

        @Override
        public boolean compareTo(ConsumibleHelper value) {
            return false;
        }
    }
}
