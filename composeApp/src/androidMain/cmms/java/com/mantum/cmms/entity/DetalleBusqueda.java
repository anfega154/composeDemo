package com.mantum.cmms.entity;

import com.mantum.cmms.database.Model;

import java.io.Serializable;

import io.realm.RealmObject;

public class DetalleBusqueda extends RealmObject implements Model, Serializable {

    private Long id;

    private String title;

    private String value;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public DetalleBusqueda setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getValue() {
        return value;
    }

    public DetalleBusqueda setValue(String value) {
        this.value = value;
        return this;
    }
}