package com.mantum.cmms.domain;

import com.google.gson.Gson;
import com.mantum.cmms.Multipart;
import com.mantum.component.service.Photo;
import com.mantum.component.util.Tool;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class Novedad implements Multipart {
    private String token;
    private String tipoentidad;
    private Long identidad;
    private String fechaInicial;
    private String fechaFinal;
    private String entidad;
    private String observacion;
    private Long idpersonal;
    private Integer idestado;
    private String estado;
    private Long idcategoria;
    private String categoria;
    private Double longitude;
    private Double altitude;
    private Float accuracy;
    private Double latitude;
    private String tipoSS;
    private String prioridadSS;
    private List<Photo> image;
    private boolean finalizanovedad;

    public Novedad() {
        this.image = new ArrayList<>();
        this.finalizanovedad = false;
        this.token = UUID.randomUUID().toString();
    }

    public List<Photo> getImage() {
        return image;
    }

    public void setImage(List<Photo> image) {
        this.image = image;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipoentidad() {
        return tipoentidad;
    }

    public void setTipoentidad(String tipoentidad) {
        this.tipoentidad = tipoentidad;
    }

    public Long getIdentidad() {
        return identidad;
    }

    public void setIdentidad(Long identidad) {
        this.identidad = identidad;
    }

    public String getFechaInicial() {
        return fechaInicial;
    }

    public void setFechaInicial(String fechaInicial) {
        this.fechaInicial = fechaInicial;
    }

    public String getFechaFinal() {
        return fechaFinal;
    }

    public void setFechaFinal(String fechaFinal) {
        this.fechaFinal = fechaFinal;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public Long getIdpersonal() {
        return idpersonal;
    }

    public void setIdpersonal(Long idpersonal) {
        this.idpersonal = idpersonal;
    }

    public Integer getIdestado() {
        return idestado;
    }

    public void setIdestado(Integer idestado) {
        this.idestado = idestado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Long getIdcategoria() {
        return idcategoria;
    }

    public void setIdcategoria(Long idcategoria) {
        this.idcategoria = idcategoria;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public Float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Float accuracy) {
        this.accuracy = accuracy;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public boolean isFinalizanovedad() {
        return finalizanovedad;
    }

    public void setFinalizanovedad(boolean finalizanovedad) {
        this.finalizanovedad = finalizanovedad;
    }

    public String getTipoSS() {
        return tipoSS;
    }

    public void setTipoSS(String tipoSS) {
        this.tipoSS = tipoSS;
    }

    public String getPrioridadSS() {
        return prioridadSS;
    }

    public void setPrioridadSS(String prioridadSS) {
        this.prioridadSS = prioridadSS;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    @Override
    public MultipartBody.Builder builder() {
        MultipartBody.Builder multipart = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("token", getToken())
                .addFormDataPart("fecha", Tool.formData(getFechaInicial()))
                .addFormDataPart("fechainicial", Tool.formData(getFechaInicial()))
                .addFormDataPart("fechafinal", Tool.formData(getFechaFinal()))
                .addFormDataPart("idpersonal", Tool.formData(getIdpersonal()))
                .addFormDataPart("tipoentidad", Tool.formData(getTipoentidad()))
                .addFormDataPart("identidad", Tool.formData(getIdentidad()))
                .addFormDataPart("idestado", Tool.formData(getIdestado()))
                .addFormDataPart("estado", Tool.formData(getEstado()))
                .addFormDataPart("observacion", Tool.formData(getObservacion()))
                .addFormDataPart("latitud", Tool.formData(getLatitude()))
                .addFormDataPart("longitud", Tool.formData(getLongitude()))
                .addFormDataPart("altitud", Tool.formData(getAltitude()))
                .addFormDataPart("exactitud", Tool.formData(getAccuracy()))
                .addFormDataPart("idcategoria", Tool.formData(getIdcategoria()))
                .addFormDataPart("finalizanovedad", Tool.formData(isFinalizanovedad()))
                .addFormDataPart("tipoSS", Tool.formData(getTipoSS()))
                .addFormDataPart("prioridadSS", Tool.formData(getPrioridadSS()));

        if (getImage() != null) {
            for (Photo photo : getImage()) {
                if (photo.exists()) {
                    multipart.addFormDataPart(photo.getNaturalName(), Tool.formData(photo.getDescription()));
                    multipart.addFormDataPart("files[]", photo.getName(),
                            RequestBody.create(MediaType.parse(photo.getMime()), photo.getFile()));
                }
            }
        }

        return multipart;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static class Response {
        private Long idhistorico;

        public Long getIdhistorico() {
            return idhistorico;
        }

        public void setIdhistorico(Long idhistorico) {
            this.idhistorico = idhistorico;
        }
    }
}
