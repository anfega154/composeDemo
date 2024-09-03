package com.mantum.cmms.domain;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.mantum.cmms.Multipart;
import com.mantum.component.service.Photo;
import com.mantum.component.util.Tool;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class Comentar implements Multipart {

    private Long id;

    private String codigo;

    @SerializedName("comment")
    private String descripcion;

    private List<Photo> files;

    private String token;

    public Comentar() {
        this.files = new ArrayList<>();
        this.token = UUID.randomUUID().toString();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<Photo> getFiles() {
        return files;
    }

    public void setFiles(List<Photo> files) {
        this.files = files;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public MultipartBody.Builder builder() {
        MultipartBody.Builder multipart = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("token", Tool.formData(getToken()))
                .addFormDataPart("id", Tool.formData(getId()))
                .addFormDataPart("comment", Tool.formData(getDescripcion()));

        for (Photo photo : getFiles()) {
            multipart.addFormDataPart(photo.getNaturalName(), Tool.formData(photo.getDescription()));
            multipart.addFormDataPart("files[]", photo.getName(),
                    RequestBody.create(MediaType.parse(photo.getMime()), photo.getFile()));
        }

        return multipart;
    }
}