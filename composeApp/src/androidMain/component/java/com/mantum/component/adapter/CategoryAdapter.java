package com.mantum.component.adapter;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class CategoryAdapter implements Serializable, Comparable<CategoryAdapter> {

    Boolean firma;
    private Long id;
    private String nombre;

    public CategoryAdapter() {
    }

    public CategoryAdapter(Long id, String formato, Boolean firma) {
        this.id = id;
        this.nombre = formato;
        this.firma = firma;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getFirma() {
        return firma;
    }

    public void setFirma(Boolean firma) {
        this.firma = firma;
    }

    @NonNull
    @Override
    public String toString() {
        return nombre;
    }

    @Override
    public int compareTo(CategoryAdapter categoryAdapter) {
        return 0;
    }
}