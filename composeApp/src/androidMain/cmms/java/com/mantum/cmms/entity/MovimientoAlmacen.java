package com.mantum.cmms.entity;

import com.google.gson.Gson;
import com.mantum.cmms.Multipart;
import com.mantum.component.util.Tool;

import java.util.ArrayList;

import okhttp3.MultipartBody;

//Clase para movimientos desde almacén y salidas RT
public class MovimientoAlmacen implements Multipart {

    private String token;

    private String date;

    private String movement;

    private int idmovementtype;

    private Long idstore;

    private Long idrt;

    private Long idgrouprt;

    private Long idot;

    private ArrayList<Resources> resources;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMovement() {
        return movement;
    }

    public void setMovement(String movement) {
        this.movement = movement;
    }

    public int getIdmovementtype() {
        return idmovementtype;
    }

    public void setIdmovementtype(int idmovementtype) {
        this.idmovementtype = idmovementtype;
    }

    public Long getIdstore() {
        return idstore;
    }

    public void setIdstore(Long idstore) {
        this.idstore = idstore;
    }

    public Long getIdrt() {
        return idrt;
    }

    public void setIdrt(Long idrt) {
        this.idrt = idrt;
    }

    public Long getIdgrouprt() {
        return idgrouprt;
    }

    public void setIdgrouprt(Long idgrouprt) {
        this.idgrouprt = idgrouprt;
    }

    public Long getIdot() {
        return idot;
    }

    public void setIdot(Long idot) {
        this.idot = idot;
    }

    public ArrayList<Resources> getResources() {
        return resources;
    }

    public void setResources(ArrayList<Resources> resources) {
        this.resources = resources;
    }

    public static class Resources {
        private Long idarticle;
        private Float quantity;

        public Long getIdarticle() {
            return idarticle;
        }

        public void setIdarticle(Long idarticle) {
            this.idarticle = idarticle;
        }

        public Float getQuantity() {
            return quantity;
        }

        public void setQuantity(Float quantity) {
            this.quantity = quantity;
        }
    }

    @Override
    public MultipartBody.Builder builder() {
        return new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("token", Tool.formData(getToken()))
                .addFormDataPart("date", Tool.formData(getDate()))
                .addFormDataPart("movement", Tool.formData(getMovement()))
                .addFormDataPart("idmovementtype", Tool.formData(getIdmovementtype()))
                .addFormDataPart("idstore", Tool.formData(getIdstore()))
                .addFormDataPart("resources", Tool.formData(getResources()));
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}