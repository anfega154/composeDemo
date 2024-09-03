package com.mantum.cmms.domain;

import com.google.gson.Gson;
import com.mantum.cmms.Multipart;
import com.mantum.component.service.Photo;
import com.mantum.component.util.Tool;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class Archivo implements Multipart {

    private Long idEntidad;

    private String entidad;

    private List<Photo> photos;

    private boolean predeterminada;

    public Archivo() {
        this.photos = new ArrayList<>();
    }

    public Long getIdEntidad() {
        return idEntidad;
    }

    public void setIdEntidad(Long idEntidad) {
        this.idEntidad = idEntidad;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    public boolean isPredeterminada() {
        return predeterminada;
    }

    public void setPredeterminada(boolean predeterminada) {
        this.predeterminada = predeterminada;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public MultipartBody.Builder builder() {
        MultipartBody.Builder multipart = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("iIdEntidad", Tool.formData(getIdEntidad()))
                .addFormDataPart("sEntidad", Tool.formData(getEntidad()))
                .addFormDataPart("bDefecto", Tool.formData(isPredeterminada() ? "1" : "0"));

        for (Photo photo : getPhotos()) {
            if (photo.exists()) {
                multipart.addFormDataPart(photo.getNaturalName(), Tool.formData(photo.getDescription()));
                multipart.addFormDataPart("files[]", photo.getName(),
                        RequestBody.create(MediaType.parse(photo.getMime()), photo.getFile()));
            }
        }

        return multipart;
    }
}