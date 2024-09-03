package com.mantum.cmms.domain;

import androidx.annotation.NonNull;

import com.mantum.cmms.Multipart;
import com.mantum.cmms.entity.Consumible;
import com.mantum.cmms.entity.Falla;
import com.mantum.cmms.entity.Mochila;
import com.mantum.cmms.entity.Paro;
import com.mantum.cmms.entity.PendienteMantenimiento;
import com.mantum.cmms.entity.Personal;
import com.mantum.cmms.entity.Recurso;
import com.mantum.cmms.entity.RepuestoManual;
import com.mantum.component.service.Photo;
import com.mantum.component.util.Tool;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public abstract class Bitacora implements Multipart {

    private String code;

    private String date;

    private String token;

    private String type;

    private Long typeevent;

    private String timestart;

    private String timeend;

    private String executionrate;

    private String description;

    private List<Photo> files;

    private List<Recurso> recursos;

    private List<Mochila> mochila;

    private PendienteMantenimiento.Request pendientepmtto;

    private Coordenada location;

    private String tipoTiempoTexto;

    private String tipotiempo;

    private String nota;

    private String observacionActivos;

    private Float horashabilesdia;

    private List<Falla.Request> fallas;

    private List<Paro.ParoHelper> paros;

    private List<Personal> grupos;

    public String getFechaAnterior() {
        return fechaAnterior;
    }

    public void setFechaAnterior(String fechaAnterior) {
        this.fechaAnterior = fechaAnterior;
    }

    private String fechaAnterior;

    Bitacora() {
        this.token = UUID.randomUUID().toString();
        this.files = new ArrayList<>();
        this.recursos = new ArrayList<>();
        this.mochila = new ArrayList<>();
        this.fallas = new ArrayList<>();
        this.grupos = new ArrayList<>();
    }

    public List<Personal> getGroups() {
        return grupos;
    }

    public void setGroup(List<Personal> grupos) {
        this.grupos = grupos;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTypeevent() {
        return typeevent;
    }

    public void setTypeevent(Long typeevent) {
        this.typeevent = typeevent;
    }

    public String getTimestart() {
        return timestart;
    }

    public void setTimestart(String timestart) {
        this.timestart = timestart;
    }

    public String getTimeend() {
        return timeend;
    }

    public void setTimeend(String timeend) {
        this.timeend = timeend;
    }

    public String getExecutionrate() {
        return executionrate;
    }

    public void setExecutionrate(String executionrate) {
        this.executionrate = executionrate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NonNull
    public List<Photo> getFiles() {
        if (files != null) {
            return files;
        }
        return new ArrayList<>();
    }

    public void setFiles(List<Photo> files) {
        this.files = files;
    }

    @NonNull
    public List<Recurso> getRecursos() {
        if (recursos != null) {
            return recursos;
        }
        return new ArrayList<>();
    }

    public void setRecursos(List<Recurso> recursos) {
        this.recursos = recursos;
    }

    public PendienteMantenimiento.Request getPendientepmtto() {
        return pendientepmtto;
    }

    public void setPendientepmtto(PendienteMantenimiento.Request pendientepmtto) {
        this.pendientepmtto = pendientepmtto;
    }

    @NonNull
    public List<Mochila> getMochila() {
        if (mochila != null) {
            return mochila;
        }
        return new ArrayList<>();
    }

    public void setMochila(List<Mochila> mochila) {
        this.mochila = mochila;
    }

    public Coordenada getLocation() {
        return location;
    }

    public void setLocation(Coordenada location) {
        this.location = location;
    }

    public String getTipotiempo() {
        return tipotiempo;
    }

    public void setTipotiempo(String tipotiempo) {
        this.tipotiempo = tipotiempo;
    }

    public String getTipoTiempoTexto() {
        return tipoTiempoTexto;
    }

    public void setTipoTiempoTexto(String tipoTiempoTexto) {
        this.tipoTiempoTexto = tipoTiempoTexto;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

    public String getObservacionActivos() {
        return observacionActivos;
    }

    public void setObservacionActivos(String observacionActivos) {
        this.observacionActivos = observacionActivos;
    }

    public Float getHorashabilesdia() {
        return horashabilesdia;
    }

    public void setHorashabilesdia(Float horashabilesdia) {
        this.horashabilesdia = horashabilesdia;
    }

    public List<Falla.Request> getFallas() {
        if (fallas != null) {
            return fallas;
        }
        return new ArrayList<>();
    }

    public void setFallas(List<Falla.Request> fallas) {
        this.fallas = fallas;
    }

    public List<Paro.ParoHelper> getParos() {
        if (paros != null) {
            return paros;
        }
        return new ArrayList<>();
    }

    public void setParos(List<Paro.ParoHelper> paros) {
        this.paros = paros;
    }

    public abstract String title();

    public abstract String toJson();

    MultipartBody.Builder includeResource(MultipartBody.Builder multipart) {
        for (Photo photo : getFiles()) {
            if (photo.exists()) {
                multipart.addFormDataPart(photo.getNaturalName(), Tool.formData(photo.getDescription()));
                multipart.addFormDataPart("files[]", photo.getName(),
                        RequestBody.create(MediaType.parse(photo.getMime()), photo.getFile()));
            }
        }

        int key = 1;
        for (Recurso recurso : getRecursos()) {
            if (recurso.isOk()) {
                multipart.addFormDataPart("resources_" + key + "[]", Tool.formData(recurso.getId()));
                multipart.addFormDataPart("resources_" + key + "[]", Tool.formData(recurso.getCantidad()));
                multipart.addFormDataPart("resources_" + key + "[]", Tool.formData(recurso.getUbicacion()));
                multipart.addFormDataPart("resources_" + key + "[]", Tool.formData(recurso.getObservaciones()));
                key = key + 1;
            }
        }

        key = 1;
        for (Mochila recurso : getMochila()) {
            if (recurso.isOk()) {
                multipart.addFormDataPart("resources_ph_" + key + "[]", Tool.formData(recurso.getId()));
                multipart.addFormDataPart("resources_ph_" + key + "[]", Tool.formData(recurso.getIdphrec()));
                multipart.addFormDataPart("resources_ph_" + key + "[]", Tool.formData(recurso.getCantidad()));
                multipart.addFormDataPart("resources_ph_" + key + "[]", Tool.formData(recurso.getUbicacion()));
                multipart.addFormDataPart("resources_ph_" + key + "[]", Tool.formData(recurso.getObservaciones()));
                key = key + 1;
            }
        }

        if (getLocation() != null) {
            multipart.addFormDataPart("datetime", Tool.formData(location.getDatetime()));
            multipart.addFormDataPart("latitude", Tool.formData(location.getLatitude()));
            multipart.addFormDataPart("longitude", Tool.formData(location.getLongitude()));
            multipart.addFormDataPart("altitude", Tool.formData(location.getAltitude()));
            multipart.addFormDataPart("accuracy", Tool.formData(location.getAccuracy()));
        }

        key = 1;
        for (Falla.Request falla : getFallas()) {
            multipart.addFormDataPart("failure_" + key + "[]", Tool.formData(falla.getId()));
            multipart.addFormDataPart("failure_" + key + "[]", Tool.formData(falla.getFechafin()));
            multipart.addFormDataPart("failure_" + key + "[]", Tool.formData(falla.getHorafin()));

            if (falla.getRepuestos() != null && !falla.getRepuestos().isEmpty()) {
                int fallaExtraKey = 1;
                for (RepuestoManual.Repuesto repuesto : falla.getRepuestos()) {
                    multipart.addFormDataPart("replacement_" + fallaExtraKey + "[]", Tool.formData(repuesto.getNombre()));
                    multipart.addFormDataPart("replacement_" + fallaExtraKey + "[]", Tool.formData(repuesto.getSerial()));
                    multipart.addFormDataPart("replacement_" + fallaExtraKey + "[]", Tool.formData(repuesto.getSerialRetiro()));
                    multipart.addFormDataPart("replacement_" + fallaExtraKey + "[]", Tool.formData(falla.getId()));

                    fallaExtraKey = fallaExtraKey + 1;
                }
            }

            if (falla.getConsumibles() != null && !falla.getConsumibles().isEmpty()) {
                int fallaExtraKey = 1;
                for (Consumible.ConsumibleHelper consumible : falla.getConsumibles()) {
                    multipart.addFormDataPart("consumable_" + fallaExtraKey + "[]", Tool.formData(consumible.getNombre()));
                    multipart.addFormDataPart("consumable_" + fallaExtraKey + "[]", Tool.formData(consumible.getCantidadreal()));
                    multipart.addFormDataPart("consumable_" + fallaExtraKey + "[]", Tool.formData("0"));
                    multipart.addFormDataPart("consumable_" + fallaExtraKey + "[]", Tool.formData(falla.getId()));

                    fallaExtraKey = fallaExtraKey + 1;
                }
            }

            if (falla.getImagenesPrevias() != null && !falla.getImagenesPrevias().isEmpty()) {
                for (Photo photo : falla.getImagenesPrevias()) {
                    if (photo.exists()) {
                        String[] fileNameSplit = photo.getName().split("\\.");
                        String newFileName = fileNameSplit[0] + "_categoria_98." + fileNameSplit[1];

                        multipart.addFormDataPart(photo.getNaturalName(), Tool.formData(photo.getDescription()));
                        multipart.addFormDataPart("filesfail_" + falla.getId() + "[]", newFileName,
                                RequestBody.create(MediaType.parse(photo.getMime()), photo.getFile()));
                    }
                }
            }

            if (falla.getImagenesPosteriores() != null && !falla.getImagenesPosteriores().isEmpty()) {
                for (Photo photo : falla.getImagenesPosteriores()) {
                    if (photo.exists()) {
                        String[] fileNameSplit = photo.getName().split("\\.");
                        String newFileName = fileNameSplit[0] + "_categoria_99." + fileNameSplit[1];

                        multipart.addFormDataPart(photo.getNaturalName(), Tool.formData(photo.getDescription()));
                        multipart.addFormDataPart("filesfail_" + falla.getId() + "[]", newFileName,
                                RequestBody.create(MediaType.parse(photo.getMime()), photo.getFile()));
                    }
                }
            }

            key = key + 1;
        }

        key = 1;
        if (!getParos().isEmpty()) {
            for (Paro.ParoHelper paro : getParos()) {
                multipart.addFormDataPart("paros_" + key + "[]", Tool.formData(paro.getHoraInicio()));
                multipart.addFormDataPart("paros_" + key + "[]", Tool.formData(paro.getHoraFin()));
                multipart.addFormDataPart("paros_" + key + "[]", Tool.formData(paro.getTipo() == 0 ? "" : paro.getTipo()));
                multipart.addFormDataPart("paros_" + key + "[]", Tool.formData(paro.getClasificacion()));
                key = key + 1;
            }
        }

        key = 1;
        if (!getGroups().isEmpty()) {
            for (Personal  personal: getGroups()) {
                multipart.addFormDataPart("staff_" + key + "[]", Tool.formData(personal.getId()));
                multipart.addFormDataPart("staff_" + key + "[]", Tool.formData(personal.getNombre()));
                key = key + 1;
            }
        }

        multipart.addFormDataPart("horashabilesdia", Tool.formData(getHorashabilesdia()));
        return multipart;
    }

    @NonNull
    @Override
    public String toString() {
        return "Bitacora{" +
                "code='" + code + '\'' +
                ", date='" + date + '\'' +
                ", token='" + token + '\'' +
                ", type='" + type + '\'' +
                ", typeevent=" + typeevent +
                ", timestart='" + timestart + '\'' +
                ", timeend='" + timeend + '\'' +
                ", executionrate='" + executionrate + '\'' +
                ", description='" + description + '\'' +
                ", files=" + files +
                ", recursos=" + recursos +
                ", mochila=" + mochila +
                ", pendientepmtto=" + pendientepmtto +
                ", location=" + location +
                ", tipoTiempoTexto='" + tipoTiempoTexto + '\'' +
                ", tipotiempo='" + tipotiempo + '\'' +
                ", nota='" + nota + '\'' +
                ", observacionActivos='" + observacionActivos + '\'' +
                ", horashabilesdia=" + horashabilesdia +
                ", fallas=" + fallas +
                ", paros=" + paros +
                ", grupos=" + grupos +
                ", fechaAnterior='" + fechaAnterior + '\'' +
                '}';
    }
}