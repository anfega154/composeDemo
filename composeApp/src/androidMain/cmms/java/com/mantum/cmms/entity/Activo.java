package com.mantum.cmms.entity;

import com.google.gson.Gson;

import java.util.ArrayList;

public class Activo {

    private Long id;

    private String token;

    //General
    private String codigo;

    private String nombre;

    private Long idfamilia;

    private Long idinstalacionproceso;

    private Long idinstalacionlocativa;

    private boolean provocaparo;

    private String ubicacion;

    private Long idpais;

    private Long iddepartamento;

    private Long idciudad;

    private String observaciones;

    //Información técnica
    private String serie;

    private String modelo;

    private String color;

    private Long idfabricante;

    private Double largo;

    private Long idmedidalargo;

    private Double ancho;

    private Long idmedidaancho;

    private Double alto;

    private Long idmedidaalto;

    private Double peso;

    private Long idmedidapeso;

    //Información histórica
    private String estadoactual;

    //Personal
    private Long idResponsable;

    private Long idcategoria;

    //Información financiera
    private String codigocontable;

    private Long idestado;

    private ArrayList<CentroCosto> centrocostos;

    //Información extra
    private String familia;

    private String pais;

    private String departamento;

    private String ciudad;

    private String instalacionproceso;

    private String instalacionlocativa;

    private String fabricante;

    private String medidalargo;

    private String medidaancho;

    private String medidaalto;

    private String medidapeso;

    private String estado;

    private String categoria;

    private String responsable;
    private String annosInventario;

    public String getAnnosInventario() {
        return annosInventario;
    }

    public void setAnnosInventario(String annosInventario) {
        this.annosInventario = annosInventario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getIdfamilia() {
        return idfamilia;
    }

    public void setIdfamilia(Long idfamilia) {
        this.idfamilia = idfamilia;
    }

    public Long getIdinstalacionproceso() {
        return idinstalacionproceso;
    }

    public void setIdinstalacionproceso(Long idinstalacionproceso) {
        this.idinstalacionproceso = idinstalacionproceso;
    }

    public Long getIdinstalacionlocativa() {
        return idinstalacionlocativa;
    }

    public void setIdinstalacionlocativa(Long idinstalacionlocativa) {
        this.idinstalacionlocativa = idinstalacionlocativa;
    }

    public boolean isProvocaparo() {
        return provocaparo;
    }

    public void setProvocaparo(boolean provocaparo) {
        this.provocaparo = provocaparo;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Long getIdpais() {
        return idpais;
    }

    public void setIdpais(Long idpais) {
        this.idpais = idpais;
    }

    public Long getIddepartamento() {
        return iddepartamento;
    }

    public void setIddepartamento(Long iddepartamento) {
        this.iddepartamento = iddepartamento;
    }

    public Long getIdciudad() {
        return idciudad;
    }

    public void setIdciudad(Long idciudad) {
        this.idciudad = idciudad;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Long getIdfabricante() {
        return idfabricante;
    }

    public void setIdfabricante(Long idfabricante) {
        this.idfabricante = idfabricante;
    }

    public Double getLargo() {
        return largo;
    }

    public void setLargo(Double largo) {
        this.largo = largo;
    }

    public Long getIdmedidalargo() {
        return idmedidalargo;
    }

    public void setIdmedidalargo(Long idmedidalargo) {
        this.idmedidalargo = idmedidalargo;
    }

    public Double getAncho() {
        return ancho;
    }

    public void setAncho(Double ancho) {
        this.ancho = ancho;
    }

    public Long getIdmedidaancho() {
        return idmedidaancho;
    }

    public void setIdmedidaancho(Long idmedidaancho) {
        this.idmedidaancho = idmedidaancho;
    }

    public Double getAlto() {
        return alto;
    }

    public void setAlto(Double alto) {
        this.alto = alto;
    }

    public Long getIdmedidaalto() {
        return idmedidaalto;
    }

    public void setIdmedidaalto(Long idmedidaalto) {
        this.idmedidaalto = idmedidaalto;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public Long getIdmedidapeso() {
        return idmedidapeso;
    }

    public void setIdmedidapeso(Long idmedidapeso) {
        this.idmedidapeso = idmedidapeso;
    }

    public String getEstadoactual() {
        return estadoactual;
    }

    public void setEstadoactual(String estadoactual) {
        this.estadoactual = estadoactual;
    }

    public Long getIdResponsable() {
        return idResponsable;
    }

    public void setIdResponsable(Long idResponsable) {
        this.idResponsable = idResponsable;
    }

    public Long getIdcategoria() {
        return idcategoria;
    }

    public void setIdcategoria(Long idcategoria) {
        this.idcategoria = idcategoria;
    }

    public String getCodigocontable() {
        return codigocontable;
    }

    public void setCodigocontable(String codigocontable) {
        this.codigocontable = codigocontable;
    }

    public Long getIdestado() {
        return idestado;
    }

    public void setIdestado(Long idestado) {
        this.idestado = idestado;
    }

    public ArrayList<CentroCosto> getCentrocostos() {
        return centrocostos;
    }

    public void setCentrocostos(ArrayList<CentroCosto> centrocostos) {
        this.centrocostos = centrocostos;
    }

    public String getFamilia() {
        return familia;
    }

    public void setFamilia(String familia) {
        this.familia = familia;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getInstalacionproceso() {
        return instalacionproceso;
    }

    public void setInstalacionproceso(String instalacionproceso) {
        this.instalacionproceso = instalacionproceso;
    }

    public String getInstalacionlocativa() {
        return instalacionlocativa;
    }

    public void setInstalacionlocativa(String instalacionlocativa) {
        this.instalacionlocativa = instalacionlocativa;
    }

    public String getFabricante() {
        return fabricante;
    }

    public void setFabricante(String fabricante) {
        this.fabricante = fabricante;
    }

    public String getMedidalargo() {
        return medidalargo;
    }

    public void setMedidalargo(String medidalargo) {
        this.medidalargo = medidalargo;
    }

    public String getMedidaancho() {
        return medidaancho;
    }

    public void setMedidaancho(String medidaancho) {
        this.medidaancho = medidaancho;
    }

    public String getMedidaalto() {
        return medidaalto;
    }

    public void setMedidaalto(String medidaalto) {
        this.medidaalto = medidaalto;
    }

    public String getMedidapeso() {
        return medidapeso;
    }

    public void setMedidapeso(String medidapeso) {
        this.medidapeso = medidapeso;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public static class CentroCosto {
        private Long id;

        private String codigo;

        private String nombre;

        private float porcentaje;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getCodigo() {
            return codigo;
        }

        public void setCodigo(String codigo) {
            this.codigo = codigo;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public float getPorcentaje() {
            return porcentaje;
        }

        public void setPorcentaje(float porcentaje) {
            this.porcentaje = porcentaje;
        }
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
