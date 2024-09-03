package com.mantum.cmms.domain;

import com.mantum.cmms.Multipart;
import com.mantum.component.service.Photo;
import com.mantum.component.util.Tool;

import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class Transferencia implements Multipart {

    private final String token;

    private final Long idtransferencia;

    private final String fechaaceptacion;
    private final double latitud;
    private final double longitud;

    private final List<Photo> files;

    public Transferencia(List<Photo> files, Long idtransferencia, String fechaaceptacion, double latitud, double longitud) {
        this.files = files;
        this.idtransferencia = idtransferencia;
        this.fechaaceptacion = fechaaceptacion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.token = UUID.randomUUID().toString();
    }

    public String getToken() {
        return token;
    }

    private Long getIdtransferencia() {
        return idtransferencia;
    }

    private String getFechaaceptacion() {
        return fechaaceptacion;
    }

    public List<Photo> getFiles() {
        return files;
    }

    public double getLatitud() { return latitud; }

    public double getLongitud() { return longitud; }

    public MultipartBody.Builder builder() {
        MultipartBody.Builder multipart = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("token", Tool.formData(getToken()))
                .addFormDataPart("idtransferencia", Tool.formData(getIdtransferencia()))
                .addFormDataPart("latitud", Tool.formData(getLatitud()))
                .addFormDataPart("longitud", Tool.formData(getLongitud()))
                .addFormDataPart("fechaaceptacion", Tool.formData(getFechaaceptacion()));

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