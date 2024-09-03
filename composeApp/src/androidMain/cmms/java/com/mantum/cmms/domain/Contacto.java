package com.mantum.cmms.domain;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.mantum.cmms.Multipart;
import com.mantum.component.util.Tool;

import java.util.UUID;

import okhttp3.MultipartBody;

public class Contacto implements Multipart {

    private Long idot;

    @SerializedName("identification")
    private String identificacion;

    @SerializedName("name")
    private String nombre;

    @SerializedName("lastname")
    private String apellido;

    @SerializedName("position")
    private String cargo;

    @SerializedName("ingineer")
    private String ingeniero;

    @SerializedName("phone")
    private String telefono;

    @SerializedName("movil")
    private String celular;

    private String token;

    @SerializedName("address")
    private String direccion;

    public Contacto() {
        token = UUID.randomUUID().toString();
    }

    public Long getIdot() {
        return idot;
    }

    public void setIdot(Long idot) {
        this.idot = idot;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getIngeniero() {
        return ingeniero;
    }

    public void setIngeniero(String ingeniero) {
        this.ingeniero = ingeniero;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public MultipartBody.Builder builder() {
        return new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("token", getToken())
                .addFormDataPart("idot", Tool.formData(String.valueOf(getIdot())))
                .addFormDataPart("identification", Tool.formData(getIdentificacion()))
                .addFormDataPart("name", Tool.formData(getNombre()))
                .addFormDataPart("lastname", Tool.formData(getApellido()))
                .addFormDataPart("position", Tool.formData(getCargo()))
                .addFormDataPart("ingineer", Tool.formData(getIngeniero()))
                .addFormDataPart("phone", Tool.formData(getTelefono()))
                .addFormDataPart("movil", Tool.formData(getCelular()))
                .addFormDataPart("address", Tool.formData(getDireccion()));
    }
}