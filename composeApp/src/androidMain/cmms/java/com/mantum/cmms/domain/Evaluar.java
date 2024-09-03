package com.mantum.cmms.domain;

import java.util.ArrayList;

public class Evaluar {

    private Long id;

    private String codigo;

    private String description;

    private ArrayList<Detalle> aspecteval;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Detalle> getAspecteval() {
        return aspecteval;
    }

    public void setAspecteval(ArrayList<Detalle> aspecteval) {
        this.aspecteval = aspecteval;
    }

    public static class Detalle {

        private Long id;

        private Integer score;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }

    }
}
