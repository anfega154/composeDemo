package com.mantum.cmms.domain;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InspeccionElectrica {

    @SerializedName("idot")
    private Long idot;

    @SerializedName("token")
    private String token;

    @SerializedName("aspectos")
    private List<Aspectos> aspectos;

    public InspeccionElectrica() {
        this.token = UUID.randomUUID().toString();
        this.aspectos = new ArrayList<>();
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

    public List<Aspectos> getAspectos() {
        return aspectos;
    }

    public void setAspectos(List<Aspectos> aspectos) {
        this.aspectos = aspectos;
    }

    public String toJson() {
        return new GsonBuilder().serializeNulls().create().toJson(this);
    }

    public static class Aspectos {

        private Long id;

        private Boolean aplica;

        private String valor;

        private String condiciones;

        public Aspectos() {
            this.id = null;
            this.aplica = null;
            this.valor = null;
            this.condiciones = null;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Boolean getAplica() {
            return aplica;
        }

        public void setAplica(Boolean aplica) {
            this.aplica = aplica;
        }

        public String getValor() {
            return valor;
        }

        public void setValor(String valor) {
            this.valor = valor;
        }

        public String getCondiciones() {
            return condiciones;
        }

        public void setCondiciones(String condiciones) {
            this.condiciones = condiciones;
        }
    }
}