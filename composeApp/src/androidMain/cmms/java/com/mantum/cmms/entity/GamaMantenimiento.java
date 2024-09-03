package com.mantum.cmms.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.component.adapter.handler.ViewAdapter;

import io.realm.RealmObject;

public class GamaMantenimiento extends RealmObject implements ViewAdapter<GamaMantenimiento> {
    private Long id;
    private Cuenta cuenta;
    private String codigo;
    private String actividad;
    private String descripcion;
    private String tipo;
    private String especialidad;
    private String idreclasificacion;
    private String idtiporeparacion;
    private String idsubtiporeparacion;
    private Boolean requierefotos;
    private Boolean requiererepuestos;
    private Boolean eir;
    private Boolean pti;

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

    public String getActividad() {
        return actividad;
    }

    public void setActividad(String actividad) {
        this.actividad = actividad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getIdreclasificacion() {
        return idreclasificacion;
    }

    public void setIdreclasificacion(String idreclasificacion) {
        this.idreclasificacion = idreclasificacion;
    }

    public String getIdtiporeparacion() {
        return idtiporeparacion;
    }

    public void setIdtiporeparacion(String idtiporeparacion) {
        this.idtiporeparacion = idtiporeparacion;
    }

    public String getIdsubtiporeparacion() {
        return idsubtiporeparacion;
    }

    public void setIdsubtiporeparacion(String idsubtiporeparacion) {
        this.idsubtiporeparacion = idsubtiporeparacion;
    }

    public Boolean getRequierefotos() {
        return requierefotos;
    }

    public void setRequierefotos(Boolean requierefotos) {
        this.requierefotos = requierefotos;
    }

    public Boolean getRequiererepuestos() {
        return requiererepuestos;
    }

    public void setRequiererepuestos(Boolean requiererepuestos) {
        this.requiererepuestos = requiererepuestos;
    }

    public Boolean getEir() {
        return eir;
    }

    public void setEir(Boolean eir) {
        this.eir = eir;
    }

    public Boolean getPti() {
        return pti;
    }

    public void setPti(Boolean pti) {
        this.pti = pti;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getCodigo() != null ? getCodigo() : "";
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return getActividad() != null ? getActividad() : "";
    }

    @Nullable
    @Override
    public String getSummary() {
        return "Gama";
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
    public boolean compareTo(@NonNull GamaMantenimiento value) {
        return getId().equals(value.id);
    }

    @NonNull
    @Override
    public String toString() {
        return "GamaMantenimiento{" +
                "id=" + id +
                ", cuenta=" + cuenta +
                ", codigo='" + codigo + '\'' +
                ", actividad='" + actividad + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", tipo='" + tipo + '\'' +
                ", especialidad='" + especialidad + '\'' +
                ", idreclasificacion='" + idreclasificacion + '\'' +
                ", idtiporeparacion='" + idtiporeparacion + '\'' +
                ", idsubtiporeparacion='" + idsubtiporeparacion + '\'' +
                ", requierefotos=" + requierefotos +
                ", requiererepuestos=" + requiererepuestos +
                ", eir=" + eir +
                ", pti=" + pti +
                '}';
    }
}
