package com.mantum.cmms.entity;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class EstadoInicial extends RealmObject implements Serializable {

    @PrimaryKey
    private String uuid;

    private Long idss;

    private String codigo;

    private Cuenta cuenta;

    @SerializedName("tipo")
    private String tipofalla;

    private String marca;

    @SerializedName("cliente")
    private String denominacion;

    private String numeroproducto;

    @SerializedName("numeroserie")
    private String numeroserial;

    private String caracteristicas;

    @SerializedName("antecedentes")
    private String estado;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getIdss() {
        return idss;
    }

    public void setIdss(Long idss) {
        this.idss = idss;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getTipofalla() {
        return tipofalla;
    }

    public void setTipofalla(String tipofalla) {
        this.tipofalla = tipofalla;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getDenominacion() {
        return denominacion;
    }

    public void setDenominacion(String denominacion) {
        this.denominacion = denominacion;
    }

    public String getNumeroproducto() {
        return numeroproducto;
    }

    public void setNumeroproducto(String numeroproducto) {
        this.numeroproducto = numeroproducto;
    }

    public String getNumeroserial() {
        return numeroserial;
    }

    public void setNumeroserial(String numeroserial) {
        this.numeroserial = numeroserial;
    }

    public String getCaracteristicas() {
        return caracteristicas;
    }

    public void setCaracteristicas(String caracteristicas) {
        this.caracteristicas = caracteristicas;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public static class Request {

        private Long idss;

        private String codigo;

        @SerializedName("tipo")
        private String tipofalla;

        private String marca;

        @SerializedName("cliente")
        private String denominacion;

        private String numeroproducto;

        @SerializedName("numeroserie")
        private String numeroserial;

        private String caracteristicas;

        @SerializedName("antecedentes")
        private String estado;

        private String token;

        public Request() {
            this.token = UUID.randomUUID().toString();
        }

        public String getMarca() {
            return marca;
        }

        public void setMarca(String marca) {
            this.marca = marca;
        }

        public Long getIdss() {
            return idss;
        }

        public void setIdss(Long idss) {
            this.idss = idss;
        }

        public String getTipofalla() {
            return tipofalla;
        }

        public void setTipofalla(String tipofalla) {
            this.tipofalla = tipofalla;
        }

        public String getDenominacion() {
            return denominacion;
        }

        public void setDenominacion(String denominacion) {
            this.denominacion = denominacion;
        }

        public String getNumeroproducto() {
            return numeroproducto;
        }

        public void setNumeroproducto(String numeroproducto) {
            this.numeroproducto = numeroproducto;
        }

        public String getNumeroserial() {
            return numeroserial;
        }

        public void setNumeroserial(String numeroserial) {
            this.numeroserial = numeroserial;
        }

        public String getCaracteristicas() {
            return caracteristicas;
        }

        public void setCaracteristicas(String caracteristicas) {
            this.caracteristicas = caracteristicas;
        }

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
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

        public String toJson() {
            return new Gson().toJson(this);
        }
    }
}