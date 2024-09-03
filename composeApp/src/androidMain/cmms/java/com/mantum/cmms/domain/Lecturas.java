package com.mantum.cmms.domain;

import java.util.UUID;

public class Lecturas {

    private final Long id;

    private final String fecha;

    private final String nombre;

    private final String valor;

    private final String descripcion;

    private final String token;

    public Lecturas(Long id, String fecha, String nombre, String valor, String descripcion) {
        this.id = id;
        this.fecha = fecha;
        this.nombre = nombre;
        this.valor = valor;
        this.descripcion = descripcion;
        this.token = UUID.randomUUID().toString();
    }

    public Long getId() {
        return id;
    }

    public String getFecha() {
        return fecha;
    }

    public String getNombre() {
        return nombre;
    }

    public String getValor() {
        return valor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getToken() {
        return token;
    }
}