package com.mantum.cmms.entity;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ClasificacionCEM extends RealmObject {
    private Long id;
    private Cuenta cuenta;
    @SerializedName("tipocontenedor")
    private RealmList<Entity> type;
    @SerializedName("clasificacion")
    private String classification;
    @SerializedName("descripcion")
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public RealmList<Entity> getType() {
        return type;
    }

    public void setType(RealmList<Entity> type) {
        this.type = type;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ClasificacionCEM{" +
                "id=" + id +
                ", cuenta=" + cuenta +
                ", type=" + type +
                ", classification='" + classification + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
