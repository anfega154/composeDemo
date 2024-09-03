package com.mantum.cmms.domain;

import com.google.gson.Gson;
import com.mantum.cmms.Multipart;
import com.mantum.component.service.Photo;
import com.mantum.component.util.Tool;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class FirmaxEntidad implements Multipart {

    public FirmaxEntidad() {
        super();
    }

    private Long identidad;
    private Long idcategoria;
    private String token;

    private Long idpersonal;
    private Long idproveedor;
    private Long idcliente;
    private String cedula;
    private String nombre;
    private String tipoentidad;
    private String muestra;
    private String resultado;
    private String hora;

    private String nombrepersonal;
    private String nombreproveedor;
    private String nombrecliente;

    private Photo firma;
    private String locatefirma;

    private String cargo;

    private String empresa;

    public String getMuestra() { return muestra; }

    public void setMuestra(String muestra) { this.muestra = muestra; }

    public String getResultado() { return resultado; }

    public void setResultado(String resultado) { this.resultado = resultado; }

    public String getHora() { return hora; }

    public void setHora(String hora) { this.hora = hora; }

    public String getLocatefirma() { return locatefirma; }

    public void setLocatefirma(String locatefirma) { this.locatefirma = locatefirma; }

    public void setFirma(Photo firma) { this.firma = firma; }

    public void setIdcategoria(Long idcategoria) {
        this.idcategoria = idcategoria;
    }

    public Long getIdcategoria() { return idcategoria; }

    public String getNombrepersonal() {
        return nombrepersonal;
    }

    public void setNombrepersonal(String nombrepersonal) {
        this.nombrepersonal = nombrepersonal;
    }

    public String getNombreproveedor() {
        return nombreproveedor;
    }

    public void setNombreproveedor(String nombreproveedor) { this.nombreproveedor = nombreproveedor; }

    public String getNombrecliente() {
        return nombrecliente;
    }

    public void setNombrecliente(String nombrecliente) {
        this.nombrecliente = nombrecliente;
    }

    public Long getIdentidad() { return identidad; }

    public void setIdentidad(Long identidad) { this.identidad = identidad; }

    public String getTipoentidad() { return tipoentidad; }

    public void setTipoentidad(String tipoentidad) { this.tipoentidad = tipoentidad; }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getIdpersonal () {
        return idpersonal;
    }

    public void setIdpersonal (Long idpersonal){
        this.idpersonal = idpersonal;
    }

    public Long getIdproveedor () {
        return idproveedor;
    }

    public void setIdproveedor (Long idproveedor){
        this.idproveedor = idproveedor;
    }

    public Long getIdcliente () {
        return idcliente;
    }

    public void setIdcliente (Long idcliente){ this.idcliente = idcliente; }

    public String getCedula () {
        return cedula;
    }

    public void setCedula (String cedula){
        this.cedula = cedula;
    }

    public String getNombre () {
        return nombre;
    }

    public void setNombre (String nombre){
        this.nombre = nombre;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public String getCargo() {return cargo;}
    public void setCargo(String cargo) {this.cargo = cargo;}

    public String getEmpresa() {return empresa;}
    public void setEmpresa(String empresa) {this.empresa = empresa;}

    @Override
    public MultipartBody.Builder builder() {
        MultipartBody.Builder multipart = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("token", getToken())
                .addFormDataPart("idpersonal", Tool.formData(getIdpersonal()))
                .addFormDataPart("idproveedor", Tool.formData(getIdproveedor()))
                .addFormDataPart("idcliente", Tool.formData(getIdcliente()))
                .addFormDataPart("idcategoria", Tool.formData(getIdcategoria()))
                .addFormDataPart("nombre", Tool.formData(getNombre()))
                .addFormDataPart("cedula", Tool.formData(getCedula()))
                .addFormDataPart("identidad", Tool.formData(getIdentidad()))
                .addFormDataPart("muestra", Tool.formData(getMuestra()))
                .addFormDataPart("resultado", Tool.formData(getResultado()))
                .addFormDataPart("hora", Tool.formData(getHora()))
                .addFormDataPart("cargo", Tool.formData(getCargo()))
                .addFormDataPart("empresa", Tool.formData(getEmpresa()))
                .addFormDataPart("tipoentidad", Tool.formData(getTipoentidad()));

        if (firma != null) {
            multipart.addFormDataPart("files[]", firma.getName(),
                    RequestBody.create(MediaType.parse(firma.getMime()), firma.getFile()));
        }

        return multipart;
    }

}