package com.mantum.cmms.domain;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.mantum.cmms.entity.Movimiento;
import com.mantum.component.util.Tool;

import okhttp3.MultipartBody;

public class BitacoraOrdenTrabajo extends Bitacora {

    private Long idot;

    private Long idam;

    private String tipoparo;

    private String variables;

    private String tareas;

    private boolean verficiada;

    private String estadoEquipo;

    private Movimiento movimiento;

    public BitacoraOrdenTrabajo() {
        super();
        this.verficiada = false;
    }

    public Long getIdot() {
        return idot;
    }

    public void setIdot(Long idot) {
        this.idot = idot;
    }

    public Long getIdam() {
        return idam;
    }

    public void setIdam(Long idam) {
        this.idam = idam;
    }

    public String getTipoparo() {
        return tipoparo;
    }

    public void setTipoparo(String tipoparo) {
        this.tipoparo = tipoparo;
    }

    public String getVariables() {
        return variables;
    }

    public void setVariables(String variables) {
        this.variables = variables;
    }

    public String getTareas() {
        return tareas;
    }

    public void setTareas(String tareas) {
        this.tareas = tareas;
    }

    public boolean isVerficiada() {
        return verficiada;
    }

    public void setVerficiada(boolean verficiada) {
        this.verficiada = verficiada;
    }

    public String getEstadoEquipo() {
        return estadoEquipo;
    }

    public void setEstadoEquipo(String estadoEquipo) {
        this.estadoEquipo = estadoEquipo;
    }

    public Movimiento getMovimiento() { return movimiento; }

    public void setMovimiento(Movimiento movimiento) { this.movimiento = movimiento; }

    @Override
    public String title() {
        return "Orden de trabajo (Bitácora)";
    }

    @Override
    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public MultipartBody.Builder builder() {
        MultipartBody.Builder multipart = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("token", getToken())
                .addFormDataPart("date", Tool.formData(getDate()))
                .addFormDataPart("fechaAnterior", Tool.formData(getFechaAnterior()))
                .addFormDataPart("timestart", Tool.formData(getTimestart()))
                .addFormDataPart("timeend", Tool.formData(getTimeend()))
                .addFormDataPart("idot", Tool.formData(getIdot()))
                .addFormDataPart("idam", Tool.formData(getIdam()))
                .addFormDataPart("tipotiempo", Tool.formData(getTipotiempo()))
                .addFormDataPart("executionrate", Tool.formData(getExecutionrate()))
                .addFormDataPart("description", Tool.formData(getDescription()))
                .addFormDataPart("pendientepmtto", Tool.formData(getPendientepmtto() != null ? getPendientepmtto().getDescripcion() : ""))
                .addFormDataPart("actividadpmtto", Tool.formData(getPendientepmtto() != null ? getPendientepmtto().getActividad() : ""))
                .addFormDataPart("tiempoestimadopmtto", Tool.formData(getPendientepmtto() != null ? getPendientepmtto().getTiempoestimado() : ""))
                .addFormDataPart("nota", Tool.formData(getNota()))
                .addFormDataPart("tipoparo", Tool.formData(getTipoparo()))
                .addFormDataPart("observacionactivos", Tool.formData(getObservacionActivos()))
                .addFormDataPart("variables", Tool.formData(getVariables()))
                .addFormDataPart("tareas", Tool.formData(getTareas() != null ? getTareas() : "[]"))
                .addFormDataPart("estadoEquipo", Tool.formData(getEstadoEquipo()));

        return includeResource(multipart);
    }

    @NonNull
    @Override
    public String toString() {
        return "BitacoraOrdenTrabajo{" +
                "idot=" + idot +
                ", idam=" + idam +
                ", tipoparo='" + tipoparo + '\'' +
                ", variables='" + variables + '\'' +
                ", tareas='" + tareas + '\'' +
                ", verficiada=" + verficiada +
                ", estadoEquipo='" + estadoEquipo + '\'' +
                ", movimiento=" + movimiento +
                ", parent='" + super.toString() + '\'' +
                '}';
    }
}
