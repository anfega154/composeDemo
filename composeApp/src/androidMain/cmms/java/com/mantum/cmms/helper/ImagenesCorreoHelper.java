package com.mantum.cmms.helper;

import com.google.gson.Gson;
import com.mantum.cmms.Multipart;
import com.mantum.component.service.Photo;
import com.mantum.component.util.Tool;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ImagenesCorreoHelper implements Multipart {

    private String codigoContenedor;

    private List<Photo> imagenes;

    public String getCodigoContenedor() {
        return codigoContenedor;
    }

    public void setCodigoContenedor(String codigoContenedor) {
        this.codigoContenedor = codigoContenedor;
    }

    public List<Photo> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<Photo> imagenes) {
        this.imagenes = imagenes;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public MultipartBody.Builder builder() {
        MultipartBody.Builder multipart = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("codigo", Tool.formData(getCodigoContenedor()));

        int key = 1;
        if (getImagenes() != null && !getImagenes().isEmpty()) {
            for (Photo photo : getImagenes()) {
                if (photo.exists()) {
                    multipart.addFormDataPart(photo.getNaturalName(), Tool.formData(photo.getDescription()));
                    multipart.addFormDataPart("files_" + key + "[]", photo.getName(),
                            RequestBody.create(MediaType.parse(photo.getMime()), photo.getFile()));

                    key = key + 1;
                }
            }
        }

        return multipart;
    }
}
