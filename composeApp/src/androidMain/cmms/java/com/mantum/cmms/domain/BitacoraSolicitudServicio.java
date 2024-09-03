package com.mantum.cmms.domain;

import com.google.gson.Gson;
import com.mantum.component.util.Tool;

import okhttp3.MultipartBody;

public class BitacoraSolicitudServicio extends Bitacora {

    private Long idss;

    public BitacoraSolicitudServicio() {
        super();
    }

    public void setIdss(Long idss) {
        this.idss = idss;
    }

    public Long getIdss() {
        return idss;
    }

    @Override
    public String title() {
        return "Solicitud de servicio (Bitácora)";
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
                .addFormDataPart("idss", Tool.formData(getIdss()))
                .addFormDataPart("executionrate", Tool.formData(getExecutionrate()))
                .addFormDataPart("description", Tool.formData(getDescription()))
                .addFormDataPart("tipotiempo", Tool.formData(getTipotiempo()))
                .addFormDataPart("pendientepmtto", Tool.formData(getPendientepmtto() != null ? getPendientepmtto().getDescripcion() : ""))
                .addFormDataPart("actividadpmtto", Tool.formData(getPendientepmtto() != null ? getPendientepmtto().getActividad() : ""))
                .addFormDataPart("tiempoestimadopmtto", Tool.formData(getPendientepmtto() != null ? getPendientepmtto().getTiempoestimado() : ""));

        return includeResource(multipart);
    }
}