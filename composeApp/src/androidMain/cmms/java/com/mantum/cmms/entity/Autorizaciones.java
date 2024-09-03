package com.mantum.cmms.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.R;
import com.mantum.component.adapter.handler.ViewGroupAdapter;
import com.mantum.component.mapped.IgnoreField;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Autorizaciones extends RealmObject implements Serializable,
        ViewGroupAdapter<Autorizaciones, Personal> {

    public static final String MODULO_AUTORIZACION = "Autorizacion";

    public static final String MODULO_MARCAS = "Marca";

    @PrimaryKey
    private String uuid;

    @IgnoreField
    private Cuenta cuenta;

    private Long id;

    private String codigo;

    private Date fechainicio;

    private Date fechafin;

    private String tipo;

    private String locacion;

    private String empresa;

    private String descripcion;

    private String marca;

    private String modulo;

    private String detalle;

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    private RealmList<Personal> personal;

    public Autorizaciones() {
        this.personal = new RealmList<>();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Date getFechainicio() {
        return fechainicio;
    }

    public void setFechainicio(Date fechainicio) {
        this.fechainicio = fechainicio;
    }

    public Date getFechafin() {
        return fechafin;
    }

    public void setFechafin(Date fechafin) {
        this.fechafin = fechafin;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getLocacion() {
        return locacion;
    }

    public void setLocacion(String locacion) {
        this.locacion = locacion;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public RealmList<Personal> getPersonal() {
        return personal;
    }

    public void setPersonal(RealmList<Personal> personal) {
        this.personal = personal;
    }

    @NonNull
    @Override
    public String getTitle() {
        return MODULO_AUTORIZACION.equals(getModulo()) ? getCodigo() : getMarca();
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return MODULO_AUTORIZACION.equals(getModulo()) ? (getMarca() != null ? getMarca() : "") + " - " + (getLocacion() != null ? getLocacion() : "") : getTipo();
    }

    @Nullable
    @Override
    public String getIcon() {
        return null;
    }

    @Nullable
    @Override
    public String getState() {
        return getDetalle();
    }

    @Nullable
    @Override
    public Integer getDrawable() {
        return MODULO_AUTORIZACION.equals(getModulo()) ? R.drawable.police : R.drawable.store;
    }

    @Override
    public List<Personal> getChildren() {
        return getPersonal();
    }

    @Override
    public boolean compareTo(Autorizaciones value) {
        return getId().equals(value.getId()) && getModulo().equals(value.getModulo());
    }
}