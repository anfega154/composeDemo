package com.mantum.cmms.domain;

import com.google.gson.Gson;
import com.mantum.cmms.entity.Equipo;
import com.mantum.cmms.entity.InstalacionLocativa;
import com.mantum.cmms.entity.InstalacionProceso;
import com.mantum.component.util.Tool;

import okhttp3.MultipartBody;

public class BitacoraEvento extends Bitacora {

    private Long identity;

    private String entity;

    private String typeentity;

    public BitacoraEvento() {
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

    @Override
    public String title() {
        return "Evento (Bitácora)";
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
                .addFormDataPart("date",  Tool.formData(getDate()))
                .addFormDataPart("typeevent", Tool.formData(getTypeevent()))
                .addFormDataPart("timestart", Tool.formData(getTimestart()))
                .addFormDataPart("timeend", Tool.formData(getTimeend()))
                .addFormDataPart("description", Tool.formData(getDescription()))
                .addFormDataPart("tipotiempo", Tool.formData(getTipotiempo()))
                .addFormDataPart("pendientepmtto", Tool.formData(getPendientepmtto() != null ? getPendientepmtto().getDescripcion() : ""))
                .addFormDataPart("actividadpmtto", Tool.formData(getPendientepmtto() != null ? getPendientepmtto().getActividad() : ""))
                .addFormDataPart("tiempoestimadopmtto", Tool.formData(getPendientepmtto() != null ? getPendientepmtto().getTiempoestimado() : ""))
                .addFormDataPart("identityevent", Tool.formData(getIdentity()))
                .addFormDataPart("typeentityevent", Tool.formData(getTypeentity() != null ? castTypeEntity(getTypeentity()) : ""));

        return includeResource(multipart);
    }

    private String castTypeEntity(String type) {
        switch (type) {
            case Equipo.SELF:
                return "equipo";

            case InstalacionLocativa.SELF:
                return "instalacionlocativa";

            case InstalacionProceso.SELF:
                return "instalacion";

            default:
                return "";
        }
    }
}