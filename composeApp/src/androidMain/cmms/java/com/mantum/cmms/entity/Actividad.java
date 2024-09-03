package com.mantum.cmms.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mantum.component.adapter.handler.ViewAdapter;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Actividad extends RealmObject implements ViewAdapter<Actividad> {

    private String uuid;

    private Long id;

    private Cuenta cuenta;

    private String codigo;

    private String nombre;

    private Long porcentaje;

    private String descripcion;

    private RealmList<Variable> variables;

    private RealmList<Tarea> tareas;

    private String requisitos;

    @SerializedName("imagenes")
    private RealmList<Adjuntos> imagenes;

    @SerializedName("adjuntos")
    private RealmList<Adjuntos> adjuntos;

    private String duracion;

    private int orden;

    private String tipo;

    private String fechaultimaejecucion;

    private String fechaproximaejecucion;

    private String frecuencia;

    /**
     * Si se puede registrar bitácora ot
     */
    private boolean programable;

    public Actividad() {
        this.variables = new RealmList<>();
        this.tareas = new RealmList<>();
        this.imagenes = new RealmList<>();
        this.adjuntos = new RealmList<>();
    }

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

    public Long getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(Long porcentaje) {
        this.porcentaje = porcentaje;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public RealmList<Variable> getVariables() {
        return variables;
    }

    public void setVariables(RealmList<Variable> variables) {
        this.variables = variables;
    }

    public RealmList<Tarea> getTareas() {
        return tareas;
    }

    public void setTareas(RealmList<Tarea> tareas) {
        this.tareas = tareas;
    }

    public String getRequisitos() {
        return requisitos;
    }

    public void setRequisitos(String requisitos) {
        this.requisitos = requisitos;
    }

    public RealmList<Adjuntos> getImagenes() {
        return imagenes;
    }

    public void setImagenes(RealmList<Adjuntos> imagenes) {
        this.imagenes = imagenes;
    }

    public RealmList<Adjuntos> getAdjuntos() {
        return adjuntos;
    }

    public void setAdjuntos(RealmList<Adjuntos> adjuntos) {
        this.adjuntos = adjuntos;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getFechaultimaejecucion() {
        return fechaultimaejecucion;
    }

    public void setFechaultimaejecucion(String fechaultimaejecucion) {
        this.fechaultimaejecucion = fechaultimaejecucion;
    }

    public String getFechaproximaejecucion() {
        return fechaproximaejecucion;
    }

    public void setFechaproximaejecucion(String fechaproximaejecucion) {
        this.fechaproximaejecucion = fechaproximaejecucion;
    }

    @NonNull
    @Override
    public String getTitle() {
        if (getPorcentaje() == null) {
            return getCodigo();
        }
        return getNombre();
    }

    @Nullable
    @Override
    public String getSubtitle() {
        if (getPorcentaje() != null) {
            return String.valueOf(getPorcentaje()) + " %";
        }
        return getNombre();
    }

    @Nullable
    @Override
    public String getSummary() {
        return getFechaproximaejecucion();
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
    public boolean compareTo(Actividad value) {
        return getId().equals(value.id);
    }

    public String getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(String frecuencia) {
        this.frecuencia = frecuencia;
    }

    public boolean isProgramable() {
        return programable;
    }

    public void setProgramable(boolean programable) {
        this.programable = programable;
    }
}