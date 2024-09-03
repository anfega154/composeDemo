package com.mantum.cmms.entity;

import io.realm.RealmObject;

public class InformacionTecnica extends RealmObject {

    private String fabricante;

    private String pais;

    private String fechafabricacion;

    private String modelo;

    private String nroserie;

    private Long idfabricante;

    private String color;

    private Double largo;

    private String medidalargo;

    private Double ancho;

    private String medidaancho;

    private Double alto;

    private String medidaalto;

    private Double peso;

    private String medidapeso;

    public String getFabricante() {
        return fabricante;
    }

    public void setFabricante(String fabricante) {
        this.fabricante = fabricante;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getFechafabricacion() {
        return fechafabricacion;
    }

    public void setFechafabricacion(String fechafabricacion) {
        this.fechafabricacion = fechafabricacion;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getNroserie() {
        return nroserie;
    }

    public void setNroserie(String nroserie) {
        this.nroserie = nroserie;
    }

    public Long getIdfabricante() {
        return idfabricante;
    }

    public void setIdfabricante(Long idfabricante) {
        this.idfabricante = idfabricante;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Double getLargo() {
        return largo;
    }

    public void setLargo(Double largo) {
        this.largo = largo;
    }

    public String getMedidalargo() {
        return medidalargo;
    }

    public void setMedidalargo(String medidalargo) {
        this.medidalargo = medidalargo;
    }

    public Double getAncho() {
        return ancho;
    }

    public void setAncho(Double ancho) {
        this.ancho = ancho;
    }

    public String getMedidaancho() {
        return medidaancho;
    }

    public void setMedidaancho(String medidaancho) {
        this.medidaancho = medidaancho;
    }

    public Double getAlto() {
        return alto;
    }

    public void setAlto(Double alto) {
        this.alto = alto;
    }

    public String getMedidaalto() {
        return medidaalto;
    }

    public void setMedidaalto(String medidaalto) {
        this.medidaalto = medidaalto;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public String getMedidapeso() {
        return medidapeso;
    }

    public void setMedidapeso(String medidapeso) {
        this.medidapeso = medidapeso;
    }
}
