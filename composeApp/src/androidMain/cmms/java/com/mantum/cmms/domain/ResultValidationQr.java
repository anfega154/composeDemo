package com.mantum.cmms.domain;

public class ResultValidationQr {

    private String identificacion;
    private String nombre;
    private String empresa;
    private String marca;
    private String instalacionLocativa;
    private String tipo;
    private String fechaIngreso;
    private String codigo;

    private String fechaInicio;

    private String fechaFin;

    public ResultValidationQr(String identificacion, String nombre, String empresa, String marca, String instalacionLocativa, String tipo, String fechaIngreso, String codigo) {
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.empresa = empresa;
        this.marca = marca;
        this.instalacionLocativa = instalacionLocativa;
        this.tipo = tipo;
        this.fechaIngreso = fechaIngreso;
        this.codigo = codigo;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmpresa() {
        return empresa;
    }

    public String getMarca() {
        return marca;
    }

    public String getInstalacionLocativa() {
        return instalacionLocativa;
    }

    public String getTipo() {
        return tipo;
    }

    public String getFechaIngreso() {
        return fechaIngreso;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public void setInstalacionLocativa(String instalacionLocativa) {
        this.instalacionLocativa = instalacionLocativa;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setFechaIngreso(String fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getFechaInicio() {return fechaInicio;}

    public void setFechaInicio(String fechaInicio) {this.fechaInicio = fechaInicio;}

    public String getFechaFin() {return fechaFin;}

    public void setFechaFin(String fechaFin) {this.fechaFin = fechaFin;}

}
