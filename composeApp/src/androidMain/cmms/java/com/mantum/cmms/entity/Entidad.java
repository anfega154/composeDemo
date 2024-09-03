package com.mantum.cmms.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.demo.R;
import com.mantum.component.adapter.handler.ViewGroupAdapter;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Entidad extends RealmObject implements ViewGroupAdapter<Entidad, Actividad> {

    private Long id;

    private Cuenta cuenta;

    private String codigo;

    private String nombre;

    private String tipo;

    private String estado;

    private RealmList<Actividad> actividades;

    private RealmList<Variable> variables;

    private int orden;

    @Nullable
    private String nfc;
    @Nullable
    private String qrcode;
    @Nullable
    private String barcode;

    @Nullable
    public String getNfc() { return nfc; }

    public void setNfc(@Nullable String nfc) { this.nfc = nfc; }

    @Nullable
    public String getQrcode() { return qrcode; }

    public void setQrcode(@Nullable String qrcode) { this.qrcode = qrcode; }

    @Nullable
    public String getBarcode() { return barcode; }

    public void setBarcode(@Nullable String barcode) { this.barcode = barcode; }

    public Entidad() {
        this.actividades = new RealmList<>();
        this.variables = new RealmList<>();
        this.orden = 0;
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public RealmList<Actividad> getActividades() {
        return actividades;
    }

    public void setActividades(RealmList<Actividad> actividades) {
        this.actividades = actividades;
    }

    public void setEstado(String estado) { this.estado = estado; }

    public String getEstado() { return estado; }
    public String getDetail() { return null; }

    public RealmList<Variable> getVariables() {
        return variables;
    }

    public void setVariables(RealmList<Variable> variables) {
        this.variables = variables;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
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
    public String getIcon() {
        return null;
    }

    @Nullable
    @Override
    public Integer getDrawable() {
        if (getTipo() == null) {
            return null;
        }

        switch (getTipo()) {
            case "Equipo":
                return R.drawable.equipo;
            case "InstalacionLocativa":
                return R.drawable.locativa;
            case "InstalacionProceso":
                return R.drawable.proceso;
            case "Pieza":
                return R.drawable.pieza;
            case "Componente":
                return R.drawable.componente;
            default:
                return null;
        }
    }

    @Override
    public List<Actividad> getChildren() {
        return getActividades();
    }

    public String getState() { return getEstado() != null ? "Estado: " + getEstado() : null; }

    @Override
    public boolean compareTo(Entidad value) {
        return getId().equals(value.id);
    }

    @Override
    public String toString() {
        return "Entidad{" +
                "id=" + id +
                ", cuenta=" + cuenta +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", tipo='" + tipo + '\'' +
                ", estado='" + estado + '\'' +
                ", actividades=" + actividades +
                ", variables=" + variables +
                ", orden=" + orden +
                ", nfc='" + nfc + '\'' +
                ", qrcode='" + qrcode + '\'' +
                ", barcode='" + barcode + '\'' +
                '}';
    }
}