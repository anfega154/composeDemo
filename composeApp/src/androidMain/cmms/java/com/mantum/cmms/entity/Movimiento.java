package com.mantum.cmms.entity;

import com.google.gson.Gson;
import com.mantum.cmms.Multipart;
import com.mantum.component.util.Tool;

import java.io.Serializable;
import java.util.List;

import okhttp3.MultipartBody;

//Clase para movimientos desde OT
public class Movimiento implements Multipart {

    public Movimiento() { super(); }

    private Long idot;
    private String tipomovimiento;
    private Long identidad;
    private List<TrasladoItems> entrantes;
    private List<TrasladoItems> salientes;
    private List<TrasladoItems> nuevos;

    public Long getIdot() { return idot; }

    public void setIdot(Long idot) { this.idot = idot; }

    public String getTipomovimiento() { return tipomovimiento; }

    public void setTipomovimiento(String tipomovimiento) { this.tipomovimiento = tipomovimiento; }

    public Long getIdentidad() { return identidad; }

    public void setIdentidad(Long identidad) { this.identidad = identidad; }

    public List<TrasladoItems> getEntrantes() { return entrantes; }

    public void setEntrantes(List<TrasladoItems> entrantes) { this.entrantes = entrantes; }

    public List<TrasladoItems> getSalientes() { return salientes; }

    public void setSalientes(List<TrasladoItems> salientes) { this.salientes = salientes; }

    public List<TrasladoItems> getNuevos() { return nuevos; }

    public void setNuevos(List<TrasladoItems> nuevos) { this.nuevos = nuevos; }

    @Override
    public MultipartBody.Builder builder() {
        MultipartBody.Builder multipart = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("idot", Tool.formData(getIdot()))
                .addFormDataPart("tipomovimiento", Tool.formData(getTipomovimiento()))
                .addFormDataPart("identidad", Tool.formData(getIdentidad()))
                .addFormDataPart("entrantes", Tool.formData(getEntrantes()))
                .addFormDataPart("salientes", Tool.formData(getSalientes()))
                .addFormDataPart("nuevos", Tool.formData(getNuevos()));

        return multipart;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public class Request implements Serializable {

        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Request() {
            this.id = new Long(0);
        }

    }

}
