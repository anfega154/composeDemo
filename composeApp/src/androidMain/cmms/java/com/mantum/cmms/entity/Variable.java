package com.mantum.cmms.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.demo.R;
import com.mantum.component.adapter.handler.ViewAdapter;
import com.mantum.component.mapped.IgnoreField;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Variable extends RealmObject implements ViewAdapter<Variable> {

    public static final String DESCRIPTIVA = "Descriptiva";

    public static final String CUALITATIVA = "Cualitativa";

    private Long id;

    private String entidad;

    private String codigo;

    private String nombre;

    private String unidadmedida;

    private String tipo;

    private String valor;

    private String rangoinferior;

    private String rangosuperior;

    private RealmList<VariableCualitativa> valores;

    private UltimaLecturaVariable ultimalectura;

    private String descripcion;

    private Long identidad;

    private String tipoentidad;

    private String observacion;

    private Long idActividad;

    private int orden;

    @IgnoreField
    @Nullable
    private SegmentoVariable segmento;

    public Variable() {
        this.valores = new RealmList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
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

    public String getUnidadmedida() {
        return unidadmedida;
    }

    public void setUnidadmedida(String unidadmedida) {
        this.unidadmedida = unidadmedida;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getRangoinferior() {
        return rangoinferior;
    }

    public void setRangoinferior(String rangoinferior) {
        this.rangoinferior = rangoinferior;
    }

    public String getRangosuperior() {
        return rangosuperior;
    }

    public void setRangosuperior(String rangosuperior) {
        this.rangosuperior = rangosuperior;
    }

    public RealmList<VariableCualitativa> getValores() {
        return valores;
    }

    public void setValores(RealmList<VariableCualitativa> valores) {
        this.valores = valores;
    }

    public UltimaLecturaVariable getUltimalectura() {
        return ultimalectura;
    }

    public void setUltimalectura(UltimaLecturaVariable ultimalectura) {
        this.ultimalectura = ultimalectura;
    }

    public boolean isRango() {
        return getRangoinferior() != null && !getRangoinferior().isEmpty() &&
                getRangosuperior() != null && !getRangosuperior().isEmpty();
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Long getIdentidad() {
        return identidad;
    }

    public void setIdentidad(Long identidad) {
        this.identidad = identidad;
    }

    public String getTipoentidad() {
        return tipoentidad;
    }

    public void setTipoentidad(String tipoentidad) {
        this.tipoentidad = tipoentidad;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public SegmentoVariable getSegmento() { return segmento; }

    public void setSegmento(SegmentoVariable segmento) { this.segmento = segmento; }

    @Override
    public boolean compareTo(Variable value) {
        return getId().equals(value.getId());
    }

    @NonNull
    @Override
    public String getTitle() {
        if (getNombre() != null) {
            return getNombre().trim();

        }
        return "";
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return getCodigo();
    }

    @Nullable
    @Override
    public String getSummary() {
        return getTipo();
    }

    @Nullable
    @Override
    public String getIcon() {
        return null;
    }

    @Nullable
    @Override
    public Integer getDrawable() {
        return R.drawable.variable;
    }

    public Long getIdActividad() {
        return idActividad;
    }

    public void setIdActividad(Long idActividad) {
        this.idActividad = idActividad;
    }

    public static List<Variable> incluirValor(@NonNull List<Variable> variables) {
        for (Variable variable : variables) {
            if (variable.getUltimalectura() != null && variable.getUltimalectura().getValor() != null) {
                variable.setNombre(variable.getNombre() + " - " + variable.getUltimalectura().getValor());
            }
        }
        return variables;
    }
}