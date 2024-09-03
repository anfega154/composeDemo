package com.mantum.cmms.domain;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InstalacionPlantaExterna {

    private Long idot;

    private String token;

    @SerializedName("requisitosingresocliente")
    private String requisitos;

    @SerializedName("cursoadicionalcliente")
    private boolean curso;

    @SerializedName("empresaemitecurso")
    private String empresa;

    @SerializedName("fechacurso")
    private String fecha;

    private String duracion;

    private String vigencia;

    @SerializedName("personaautorizaequipo")
    private String personal;

    @SerializedName("tiempoautorizacion")
    private String tiempo;

    @SerializedName("telefonoautorizaequipo")
    private String telefono;

    @SerializedName("infraestructuracliente1")
    private List<Infraestructura> infraestructura;

    @SerializedName("infraestructuracliente2")
    private List<IngenieriaRed> ingenieriaRed;

    public InstalacionPlantaExterna() {
        this.token = UUID.randomUUID().toString();
        this.curso = false;
        this.infraestructura = new ArrayList<>();
        this.ingenieriaRed = new ArrayList<>();
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

    public String getRequisitos() {
        return requisitos;
    }

    public void setRequisitos(String requisitos) {
        this.requisitos = requisitos;
    }

    public boolean isCurso() {
        return curso;
    }

    public void setCurso(boolean curso) {
        this.curso = curso;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public String getVigencia() {
        return vigencia;
    }

    public void setVigencia(String vigencia) {
        this.vigencia = vigencia;
    }

    public String getPersonal() {
        return personal;
    }

    public void setPersonal(String personal) {
        this.personal = personal;
    }

    public String getTiempo() {
        return tiempo;
    }

    public void setTiempo(String tiempo) {
        this.tiempo = tiempo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void addInfraestructura(Infraestructura value) {
        infraestructura.add(value);
    }

    public List<Infraestructura> getInfraestructura() {
        return infraestructura;
    }

    public void setInfraestructura(List<Infraestructura> infraestructura) {
        this.infraestructura = infraestructura;
    }

    public void addIngenieriaRed(IngenieriaRed value) {
        ingenieriaRed.add(value);
    }

    public List<IngenieriaRed> getIngenieriaRed() {
        return ingenieriaRed;
    }

    public void setIngenieriaRed(List<IngenieriaRed> ingenieriaRed) {
        this.ingenieriaRed = ingenieriaRed;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return "InstalacionPlantaExterna{" +
                "idot=" + idot +
                ", token='" + token + '\'' +
                ", requisitos='" + requisitos + '\'' +
                ", curso=" + curso +
                ", empresa='" + empresa + '\'' +
                ", fecha='" + fecha + '\'' +
                ", duracion='" + duracion + '\'' +
                ", vigencia='" + vigencia + '\'' +
                ", personal='" + personal + '\'' +
                ", tiempo='" + tiempo + '\'' +
                ", telefono='" + telefono + '\'' +
                ", infraestructura=" + infraestructura +
                '}';
    }

    public static class IngenieriaRed {

        private Integer id;

        private String cliente;

        private String marca;

        private String cantidad;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getCliente() {
            return cliente;
        }

        public void setCliente(String cliente) {
            this.cliente = cliente;
        }

        public String getMarca() {
            return marca;
        }

        public void setMarca(String marca) {
            this.marca = marca;
        }

        public String getCantidad() {
            return cantidad;
        }

        public void setCantidad(String cantidad) {
            this.cantidad = cantidad;
        }

        @Override
        public String toString() {
            return "IngenieriaRed{" +
                    "id=" + id +
                    ", cliente='" + cliente + '\'' +
                    ", marca='" + marca + '\'' +
                    ", cantidad='" + cantidad + '\'' +
                    '}';
        }
    }

    public static class Infraestructura {

        @SerializedName("item")
        private String nombre;

        private Boolean convenio;

        private Boolean radio;

        private Boolean satelite;

        private Boolean fibra;

        private Boolean gsmgprs;

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public Boolean getConvenio() {
            return convenio;
        }

        public void setConvenio(Boolean convenio) {
            this.convenio = convenio;
        }

        public Boolean getRadio() {
            return radio;
        }

        public void setRadio(Boolean radio) {
            this.radio = radio;
        }

        public Boolean getSatelite() {
            return satelite;
        }

        public void setSatelite(Boolean satelite) {
            this.satelite = satelite;
        }

        public Boolean getFibra() {
            return fibra;
        }

        public void setFibra(Boolean fibra) {
            this.fibra = fibra;
        }

        public Boolean getGsmgprs() {
            return gsmgprs;
        }

        public void setGsmgprs(Boolean gsmgprs) {
            this.gsmgprs = gsmgprs;
        }

        @Override
        public String toString() {
            return "Infraestructura{" +
                    "nombre='" + nombre + '\'' +
                    ", convenio=" + convenio +
                    ", radio=" + radio +
                    ", satelite=" + satelite +
                    ", fibra=" + fibra +
                    ", gsmgprs=" + gsmgprs +
                    '}';
        }
    }
}