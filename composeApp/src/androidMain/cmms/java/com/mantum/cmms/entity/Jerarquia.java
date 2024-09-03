package com.mantum.cmms.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.R;
import com.mantum.component.adapter.handler.ViewAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmObject;

public class Jerarquia extends RealmObject implements Serializable, ViewAdapter<Jerarquia> {

    private Long id;
    private Cuenta cuenta;
    private String nombre;
    private Long entidadfiltro;
    private Integer orden;
    private String tipo;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Cuenta getCuenta() { return cuenta; }

    public void setCuenta(Cuenta cuenta) { this.cuenta = cuenta; }

    public String getNombre() { return nombre; }

    public void setNombre(String nombre) { this.nombre = nombre; }

    public Long getEntidadfiltro() { return entidadfiltro; }

    public void setEntidadfiltro(Long entidadfiltro) { this.entidadfiltro = entidadfiltro; }

    public Integer getOrden() { return orden; }

    public void setOrden(Integer orden) { this.orden = orden; }

    public String getTipo() { return tipo; }

    public void setTipo(String tipo) { this.tipo = tipo; }

    @NonNull
    @Override
    public String getTitle() {
        return getNombre() != null ? getNombre().trim() : "";
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return getNombre();
    }

    @Nullable
    @Override
    public String getSummary() {
        return null;
    }

    @Nullable
    @Override
    public String getIcon() { return null; }

    @Nullable
    @Override
    public Integer getDrawable() {
        switch (getTipo()) {
            case  "equipo" :
                return  R.drawable.equipo;
            case  "instalacion" :
                return  R.drawable.entidades;
            default:
                return null;
        }
    }

    @Override
    public boolean compareTo(Jerarquia value) {
        return getId().equals(value.id);
    }

    @Override
    public String toString() {
        return "Jerarquia{" +
                "id=" + id +
                ", cuenta=" + cuenta +
                ", nombre='" + nombre + '\'' +
                ", entidadfiltro=" + entidadfiltro +
                ", orden=" + orden +
                ", tipo='" + tipo + '\'' +
                '}';
    }

    public class Request implements Serializable {

        private List<Jerarquia> jerarquia;

        public Request() {
            this.jerarquia = new ArrayList<>();
        }

        public List<Jerarquia> getJerarquia() { return jerarquia; }

    }

    public static class JerarquiaHelper {
        private Long id;
        private String nombre;
        private Long entidadfiltro;
        private Integer orden;
        private String tipo;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public Long getEntidadfiltro() {
            return entidadfiltro;
        }

        public void setEntidadfiltro(Long entidadfiltro) {
            this.entidadfiltro = entidadfiltro;
        }

        public Integer getOrden() {
            return orden;
        }

        public void setOrden(Integer orden) {
            this.orden = orden;
        }

        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }
    }
}
