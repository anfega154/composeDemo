package com.mantum.cmms.domain;

import com.google.gson.Gson;
import com.mantum.cmms.Multipart;
import com.mantum.cmms.util.Date;
import com.mantum.component.service.Photo;
import com.mantum.component.util.Tool;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class Terminar implements Multipart {

    private Long idot;

    private String code;

    private String datetime;

    private String reason;

    private List<Photo> files;

    private String token;

    public Terminar() {
        this.files = new ArrayList<>();
        this.token = UUID.randomUUID().toString();
        this.datetime = Date.now();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getIdot() {
        return idot;
    }

    public void setIdot(Long idot) {
        this.idot = idot;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public MultipartBody.Builder builder() {
        MultipartBody.Builder multipart = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("token", Tool.formData(getToken()))
                .addFormDataPart("idot", Tool.formData(getIdot()))
                .addFormDataPart("datetime", Tool.formData(getDatetime()))
                .addFormDataPart("reason", Tool.formData(getReason()));

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
