package com.mantum.cmms.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.cmms.database.Model;
import com.mantum.component.adapter.handler.ViewTrasladoAdapter;

import java.io.Serializable;

public class TrasladoItems implements Model, Serializable, ViewTrasladoAdapter<TrasladoItems> {

    private Long idelemento;
    private Float cantidad;
    private String codigo;
    private String nombre;
    private Float cantidaddisponible;
    private Long idalmacen;
    private Jerarquia.JerarquiaHelper jerarquia;
    private String tiposalida;

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

    public Float getCantidaddisponible() {
        return cantidaddisponible;
    }

    public void setCantidaddisponible(Float cantidaddisponible) { this.cantidaddisponible = cantidaddisponible; }

    public Long getIdelemento() {
        return idelemento;
    }

    public void setIdelemento(Long idelemento) {
        this.idelemento = idelemento;
    }

    public void setCantidad(Float cantidad) {
        this.cantidad = cantidad;
    }

    public Float getCantidad() {
        return cantidad;
    }

    public Long getIdalmacen() { return idalmacen; }

    public void setIdalmacen(Long idalmacen) { this.idalmacen = idalmacen; }

    public Jerarquia.JerarquiaHelper getJerarquia() { return jerarquia; }

    public void setJerarquia(Jerarquia.JerarquiaHelper jerarquia) { this.jerarquia = jerarquia; }

    public String getTiposalida() {
        return tiposalida;
    }

    public void setTiposalida(String tiposalida) {
        this.tiposalida = tiposalida;
    }

    @Override
    public boolean compareTo(TrasladoItems value) {
        return getIdelemento().equals(value.idelemento);
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
        return "Disponible: " + getCantidaddisponible();
    }

    @Nullable
    @Override
    public String getNombeBodega() { return null; }

    @Nullable
    @Override
    public String getPadreItem() { return null; }

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

    @NonNull
    @Override
    public Float getQuantity() { return getCantidad(); }

    @NonNull
    @Override
    public String getEstadoItem() { return null; }

    @NonNull
    @Override
    public String getItemType() { return null; }
}