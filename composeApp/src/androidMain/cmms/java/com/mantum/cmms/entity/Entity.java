package com.mantum.cmms.entity;

import io.realm.RealmObject;

public class Entity extends RealmObject {

    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
