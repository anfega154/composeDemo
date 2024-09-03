package com.mantum.cmms.entity;

import com.mantum.cmms.database.Model;

import java.io.Serializable;

import io.realm.RealmObject;

public class SegmentoVariable extends RealmObject implements Model, Serializable {

    private Long id;
    private String nombre;
    private String color;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }

    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getColor() { return color; }

    public void setColor(String color) { this.color = color; }
}
