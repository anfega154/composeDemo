package com.mantum.cmms.entity;

import androidx.annotation.Nullable;

import java.util.Date;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class BitacoraContinua extends RealmObject {

    @PrimaryKey
    private String uuid;

    private Cuenta cuenta;

    private Long idmodulo;

    private String codigo;

    private Date fechainicio;

    @Nullable
    private Date fechafin;

    @Nullable
    private String fecharegistro;

    @Nullable
    private String value;

    @Nullable
    private String tipo;

    private String estado;

    public BitacoraContinua() {
        this.uuid = UUID.randomUUID().toString();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public Long getIdmodulo() {
        return idmodulo;
    }

    public void setIdmodulo(Long idmodulo) {
        this.idmodulo = idmodulo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Date getFechainicio() {
        return fechainicio;
    }

    public void setFechainicio(Date fechainicio) {
        this.fechainicio = fechainicio;
    }

    @Nullable
    public Date getFechafin() {
        return fechafin;
    }

    public void setFechafin(@Nullable Date fechafin) {
        this.fechafin = fechafin;
    }

    @Nullable
    public String getFecharegistro() {
        return fecharegistro;
    }

    public void setFecharegistro(@Nullable String fecharegistro) {
        this.fecharegistro = fecharegistro;
    }

    @Nullable
    public String getValue() {
        return value;
    }

    public void setValue(@Nullable String value) {
        this.value = value;
    }

    @Nullable
    public String getTipo() {
        return tipo;
    }

    public void setTipo(@Nullable String tipo) {
        this.tipo = tipo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public static class Data {

        private String codigo;

        private String fecharegistro;

        private String value;

        private String estado;

        @Nullable
        public String getCodigo() {
            return codigo;
        }

        public void setCodigo(String codigo) {
            this.codigo = codigo;
        }

        @Nullable
        public String getFecharegistro() {
            return fecharegistro;
        }

        public void setFecharegistro(String fecharegistro) {
            this.fecharegistro = fecharegistro;
        }

        @Nullable
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Nullable
        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }
    }
}
