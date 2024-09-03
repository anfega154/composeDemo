package com.mantum.cmms.domain;

import com.mantum.cmms.Multipart;
import com.mantum.component.service.Photo;
import com.mantum.component.util.Tool;

import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class Recibir implements Multipart {

    private Long id;

    private Long idot;

    private String statereceive;

    private String reason;

    private String evaluation;

    private Photo image;

    private String token;

    public Recibir() {
        this.token = UUID.randomUUID().toString();
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

    public Photo getImage() {
        return image;
    }

    public void setImage(Photo image) {
        this.image = image;
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
                .addFormDataPart("idss", Tool.formData(getIdot()))
                .addFormDataPart("statereceive", Tool.formData(getStatereceive()))
                .addFormDataPart("reason", Tool.formData(getReason()))
                .addFormDataPart("eval", Tool.formData(getEvaluation()));

        if (getImage() != null) {
            if (getImage() != null && getImage().exists()) {
                multipart.addFormDataPart(getImage().getNaturalName(), Tool.formData(getImage().getDescription()));
                multipart.addFormDataPart("files[]", getImage().getName(),
                        RequestBody.create(MediaType.parse(getImage().getMime()), getImage().getFile()));
            }
        }

        return multipart;
    }
}
