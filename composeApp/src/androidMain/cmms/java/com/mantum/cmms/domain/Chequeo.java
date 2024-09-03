package com.mantum.cmms.domain;

import com.mantum.component.adapter.handler.ViewAdapterHandler;

import java.util.List;

public class Chequeo implements ViewAdapterHandler<Chequeo> {

    private final Long id;

    private final String titulo;

    private final String descripcion;

    private Boolean aplica;

    private String condiciones;

    private boolean operacion; // Necesario para no inclir la condición sino el valor!!

    public Chequeo(Long id, String titulo, String descripcion) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.operacion = false;
    }

    public Chequeo(Long id, String titulo, String descripcion, boolean operacion) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.operacion = operacion;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Boolean getAplica() {
        return aplica;
    }

    public void setAplica(Boolean aplica) {
        this.aplica = aplica;
    }

    public String getCondiciones() {
        return condiciones;
    }

    public void setCondiciones(String condiciones) {
        this.condiciones = condiciones;
    }

    public boolean isOperacion() {
        return operacion;
    }

    public void setOperacion(boolean operacion) {
        this.operacion = operacion;
    }

    @Override
    public boolean compareTo(Chequeo value) {
        return id.equals(value.id);
    }

    public static class ListaChequeo implements ViewAdapterHandler<ListaChequeo> {

        private final String titulo;

        private final List<Chequeo> chequeos;

        public ListaChequeo(String key, List<Chequeo> chequeos) {
            this.titulo = key;
            this.chequeos = chequeos;
        }

        public String getTitulo() {
            return titulo;
        }

        public List<Chequeo> getChequeos() {
            return chequeos;
        }

        @Override
        public boolean compareTo(ListaChequeo value) {
            return titulo.equals(value.titulo);
        }
    }
}