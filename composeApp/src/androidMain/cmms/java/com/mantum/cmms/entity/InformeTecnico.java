package com.mantum.cmms.entity;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.mantum.component.mapped.IgnoreExclusionStrategy;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class InformeTecnico extends RealmObject implements Serializable {

    @PrimaryKey
    private String uuid;

    private Cuenta cuenta;

    private Long idss;

    private String codigo;

    @SerializedName("actividadesrealizadas")
    private String actividades;

    private String recomendaciones;

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

    public Long getIdss() {
        return idss;
    }

    public void setIdss(Long idss) {
        this.idss = idss;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getActividades() {
        return actividades;
    }

    public void setActividades(String actividades) {
        this.actividades = actividades;
    }

    public String getRecomendaciones() {
        return recomendaciones;
    }

    public void setRecomendaciones(String recomendaciones) {
        this.recomendaciones = recomendaciones;
    }

    public static class Request {

        private Long idss;

        private String codigo;

        @SerializedName("actividadesrealizadas")
        private String actividades;

        private String recomendaciones;

        @SerializedName("recursosadicionales")
        private List<RecursoAdicional> recursosadicionales;

        private String token;

        public Request() {
            this.token = UUID.randomUUID().toString();
        }

        public Long getIdss() {
            return idss;
        }

        public void setIdss(Long idss) {
            this.idss = idss;
        }

        public String getCodigo() {
            return codigo;
        }

        public void setCodigo(String codigo) {
            this.codigo = codigo;
        }

        public String getActividades() {
            return actividades;
        }

        public void setActividades(String actividades) {
            this.actividades = actividades;
        }

        public String getRecomendaciones() {
            return recomendaciones;
        }

        public void setRecomendaciones(String recomendaciones) {
            this.recomendaciones = recomendaciones;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public List<RecursoAdicional> getRecursosAdicionales() {
            return recursosadicionales;
        }

        public void setRecursosAdicionales(List<RecursoAdicional> recursosAdicionales) {
            this.recursosadicionales = recursosAdicionales;
        }

        public String toJson() {
            return new GsonBuilder().setExclusionStrategies(new IgnoreExclusionStrategy())
                    .create().toJson(this);
        }
    }
}