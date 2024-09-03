package com.mantum.cmms.entity;

import com.google.gson.Gson;
import com.mantum.cmms.Multipart;
import com.mantum.component.util.Tool;

import java.util.List;

import okhttp3.MultipartBody;

public class TrasladoAlmacen  implements Multipart {

    public TrasladoAlmacen() {
        super();
    }

    private Long idalmacenista;

    private Long idbodegaorigen;

    private Long idbodegadestino;

    private boolean activosIP;

    private  String observacion;

    private List<TrasladoItems> elementos;


    public Long getIdalmacenista() { return idalmacenista; }

    public void setIdalmacenista(Long idalmacenista) {
        this.idalmacenista = idalmacenista;
    }

    public Long getIdbodegaorigen() {
        return idbodegaorigen;
    }

    public void setIdbodegaorigen(Long idbodegaorigen) { this.idbodegaorigen = idbodegaorigen; }

    public Long getIdbodegadestino() {
        return idbodegadestino;
    }

    public void setIdbodegadestino(Long idbodegadestino) { this.idbodegadestino = idbodegadestino; }

    public List<TrasladoItems> getElementos() {
        return elementos;
    }

    public void setElementos(List<TrasladoItems> elementos) { this.elementos = elementos; }

    public boolean getActivosIP() { return activosIP; }

    public void setActivosIP(boolean activosIP) { this.activosIP = activosIP; }

    public String getObservacion() { return observacion; }

    public void setObservacion(String observacion) { this.observacion = observacion; }

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public MultipartBody.Builder builder() {
        MultipartBody.Builder multipart = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("idalmacenista", Tool.formData(getIdalmacenista()))
                .addFormDataPart("idbodegaorigen", Tool.formData(getIdbodegaorigen()))
                .addFormDataPart("activosIP", Tool.formData(getActivosIP()))
                .addFormDataPart("observacion", Tool.formData(getObservacion()))
                .addFormDataPart("elementos", Tool.formData(getElementos()));

        return multipart;
    }

}
