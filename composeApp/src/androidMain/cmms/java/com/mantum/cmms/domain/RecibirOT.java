package com.mantum.cmms.domain;

import com.google.gson.GsonBuilder;
import com.mantum.cmms.Multipart;
import com.mantum.component.mapped.IgnoreExclusionStrategy;
import com.mantum.component.service.Photo;
import com.mantum.component.util.Tool;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class RecibirOT implements Multipart {

    private Long id;

    private Long idot;

    private String statereceive;

    private String reason;

    private String evaluation;

    private List<Photo> files;

    private String token;

    public RecibirOT() {
        this.token = UUID.randomUUID().toString();
        this.files = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdot() {
        return idot;
    }

    public void setIdot(Long idot) {
        this.idot = idot;
    }

    public String getStatereceive() {
        return statereceive;
    }

    public void setStatereceive(String statereceive) {
        this.statereceive = statereceive;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<Photo> getFiles() {
        return files;
    }

    public void setFiles(Photo image) {
        this.files.add(image);
    }

    public String getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(String evaluation) {
        this.evaluation = evaluation;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public MultipartBody.Builder builder() {
        MultipartBody.Builder multipart = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("token", Tool.formData(getToken()))
                .addFormDataPart("idot", Tool.formData(getIdot()))
                .addFormDataPart("statereceive", Tool.formData(getStatereceive()))
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

    public String toJson() {
        return new GsonBuilder().setExclusionStrategies(new IgnoreExclusionStrategy())
                .create().toJson(this);
    }
}