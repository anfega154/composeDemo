package com.mantum.cmms.domain;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class RecorridoPlantaExterna {

    private Long idot;

    private String token;

    private String fecharecorrido;

    @SerializedName("direccionobra")
    private String direccion;

    private String tramo;

    @SerializedName("ing_residente_interventor")
    private String residente;

    @SerializedName("empresaresponsable")
    private String empresa;

    private String fechainicio;

    private String fechafin;

    @SerializedName("tiempoejecucion")
    private String tiempoestimado;

    @SerializedName("porcentajeobra")
    private String porcentajeobra;

    private String estado;

    private String observaciones;

    private String telefono;

    public RecorridoPlantaExterna() {
        this.token = UUID.randomUUID().toString();
    }

    public Long getIdot() {
        return idot;
    }

    public void setIdot(Long idot) {
        this.idot = idot;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getFecharecorrido() {
        return fecharecorrido;
    }

    public void setFecharecorrido(String fecharecorrido) {
        this.fecharecorrido = fecharecorrido;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTramo() {
        return tramo;
    }

    public void setTramo(String tramo) {
        this.tramo = tramo;
    }

    public String getResidente() {
        return residente;
    }

    public void setResidente(String residente) {
        this.residente = residente;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getFechainicio() {
        return fechainicio;
    }

    public void setFechainicio(String fechainicio) {
        this.fechainicio = fechainicio;
    }

    public String getFechafin() {
        return fechafin;
    }

    public void setFechafin(String fechafin) {
        this.fechafin = fechafin;
    }

    public String getTiempoestimado() {
        return tiempoestimado;
    }

    public void setTiempoestimado(String tiempoestimado) {
        this.tiempoestimado = tiempoestimado;
    }

    public String getPorcentajeobra() {
        return porcentajeobra;
    }

    public void setPorcentajeobra(String porcentajeobra) {
        this.porcentajeobra = porcentajeobra;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return "RecorridoPlantaExterna{" +
                "idot=" + idot +
                ", token='" + token + '\'' +
                ", fecharecorrido='" + fecharecorrido + '\'' +
                ", direccion='" + direccion + '\'' +
                ", tramo='" + tramo + '\'' +
                ", residente='" + residente + '\'' +
                ", empresa='" + empresa + '\'' +
                ", fechainicio='" + fechainicio + '\'' +
                ", fechafin='" + fechafin + '\'' +
                ", tiempoestimado='" + tiempoestimado + '\'' +
                ", porcentajeobra='" + porcentajeobra + '\'' +
                ", estado='" + estado + '\'' +
                ", observaciones='" + observaciones + '\'' +
                ", telefono='" + telefono + '\'' +
                '}';
    }
}