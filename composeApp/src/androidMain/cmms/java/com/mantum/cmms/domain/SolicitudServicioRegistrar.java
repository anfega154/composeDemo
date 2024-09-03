package com.mantum.cmms.domain;

import com.mantum.cmms.Multipart;
import com.mantum.component.service.Photo;
import com.mantum.component.util.Tool;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class SolicitudServicioRegistrar implements Multipart {

    private Long idEntidad;

    private String tipoEntidad;

    private String nombreEntidad;

    private String fechaInicio;

    private String horaInicio;

    private Long idArea;

    private String area;

    private String descripcion;

    private String token;

    private List<Photo> files;

    private String prioridad;

    private String tipo;

    private Long idTipo;

    public SolicitudServicioRegistrar() {
        this.files = new ArrayList<>();
        this.token = UUID.randomUUID().toString();
    }

    public Long getIdEntidad() {
        return idEntidad;
    }

    public void setIdEntidad(Long idEntidad) {
        this.idEntidad = idEntidad;
    }

    public String getTipoEntidad() {
        return tipoEntidad;
    }

    public void setTipoEntidad(String tipoEntidad) {
        this.tipoEntidad = tipoEntidad;
    }

    public String getNombreEntidad() {
        return nombreEntidad;
    }

    public void setNombreEntidad(String nombreEntidad) {
        this.nombreEntidad = nombreEntidad;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public Long getIdArea() {
        return idArea;
    }

    public void setIdArea(Long idArea) {
        this.idArea = idArea;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<Photo> getFiles() {
        return files;
    }

    public void setFiles(List<Photo> files) {
        this.files = files;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Long getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(Long idTipo) {
        this.idTipo = idTipo;
    }

    @Override
    public MultipartBody.Builder builder() {
        MultipartBody.Builder multipart = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("entityid", Tool.formData(getIdEntidad()))
                .addFormDataPart("createdate", Tool.formData(getFechaInicio() + " " + getHoraInicio()))
                .addFormDataPart("entitytype", Tool.formData(getTipoEntidad()))
                .addFormDataPart("entitylabel", Tool.formData(getNombreEntidad()))
                .addFormDataPart("priority", Tool.formData(getPrioridad()))
                .addFormDataPart("type", Tool.formData(getIdTipo()))
                .addFormDataPart("areaid", Tool.formData(getIdArea()))
                .addFormDataPart("description", Tool.formData(getDescripcion()))
                .addFormDataPart("token", Tool.formData(getToken()));

        for (Photo photo : getFiles()) {
            if (photo.exists()) {
                multipart.addFormDataPart(photo.getNaturalName(), Tool.formData(photo.getDescription()));
                multipart.addFormDataPart("files[]", photo.getName(),
                        RequestBody.create(MediaType.parse(photo.getMime()), photo.getFile()));
            }
        }

        return multipart;
    }
}