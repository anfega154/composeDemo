package com.mantum.component.adapter;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

public class FormatoAdapter implements Serializable {

    private Long id;

    private String formato;

    private List<CategoryAdapter> categorias;

    public FormatoAdapter() {
    }

    public FormatoAdapter(Long id, String formato) {
        this.id = id;
        this.formato = formato;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public List<CategoryAdapter> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<CategoryAdapter> categorias) {
        this.categorias = categorias;
    }

    @NonNull
    @Override
    public String toString() {
        return this.formato;
    }
}