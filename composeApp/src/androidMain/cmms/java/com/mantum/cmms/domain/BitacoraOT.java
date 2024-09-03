package com.mantum.cmms.domain;

import com.google.gson.Gson;
import com.mantum.component.util.Tool;

import okhttp3.MultipartBody;

public class BitacoraOT extends Bitacora {

    private Long identity;

    private String entity;

    private String typeentity;

    private String stateot;

    private String tipoparo;

    private String estadoEquipo;

    private String variables;

    private String tareas;

    private Long idam;

    public BitacoraOT() {
        super();
    }

    public Long getIdentity() {
        return identity;
    }

    public void setIdentity(Long identity) {
        this.identity = identity;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getTypeentity() {
        return typeentity;
    }

    public void setTypeentity(String typeentity) {
        this.typeentity = typeentity;
    }

    public String getStateot() {
        return stateot;
    }

    public void setStateot(String stateot) {
        this.stateot = stateot;
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

    public Long getIdam() {
        return idam;
    }

    public void setIdam(Long idam) {
        this.idam = idam;
    }

    public String getEstadoEquipo() { return estadoEquipo; }

    public void setEstadoEquipo(String estadoEquipo) { this.estadoEquipo = estadoEquipo; }

    @Override
    public String title() {
        return "O.T. Bitácora";
    }

    @Override
    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public MultipartBody.Builder builder() {
        MultipartBody.Builder multipart = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("token", Tool.formData(getToken()))
                .addFormDataPart("date", Tool.formData(getDate()))
                .addFormDataPart("fechaAnterior", Tool.formData(getFechaAnterior()))
                .addFormDataPart("timestart", Tool.formData(getTimestart()))
                .addFormDataPart("timeend", Tool.formData(getTimeend()))
                .addFormDataPart("identity", Tool.formData(getIdentity()))
                .addFormDataPart("typeentity", Tool.formData(getTypeentity()))
                .addFormDataPart("tipotiempo", Tool.formData(getTipotiempo()))
                .addFormDataPart("stateot", Tool.formData(getStateot()))
                .addFormDataPart("executionrate", Tool.formData(getExecutionrate()))
                .addFormDataPart("description", Tool.formData(getDescription()))
                .addFormDataPart("tipoparo", Tool.formData(getTipoparo()))
                .addFormDataPart("variables", Tool.formData(getVariables()))
                .addFormDataPart("tareas", Tool.formData(getTareas() != null ? getTareas() : "[]"))
                .addFormDataPart("idam", Tool.formData(getIdam()))
                .addFormDataPart("pendientepmtto", Tool.formData(getPendientepmtto() != null ? getPendientepmtto().getDescripcion() : ""))
                .addFormDataPart("actividadpmtto", Tool.formData(getPendientepmtto() != null ? getPendientepmtto().getActividad() : ""))
                .addFormDataPart("tiempoestimadopmtto", Tool.formData(getPendientepmtto() != null ? getPendientepmtto().getTiempoestimado() : ""));

        return includeResource(multipart);
    }
}