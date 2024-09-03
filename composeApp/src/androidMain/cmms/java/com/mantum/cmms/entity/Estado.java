package com.mantum.cmms.entity;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Estado extends RealmObject {

    @PrimaryKey
    private String uuid;

    private Cuenta cuenta;

    private int id;

    private String estado;

    private RealmList<EstadoCategoria> categorias;

    private int ejecucion;

    public Estado() {
        this.uuid = java.util.UUID.randomUUID().toString();
        this.categorias = new RealmList<>();
    }

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public RealmList<EstadoCategoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(RealmList<EstadoCategoria> categorias) {
        this.categorias = categorias;
    }

    public int getEjecucion() {
        return ejecucion;
    }

    public void setEjecucion(int ejecucion) {
        this.ejecucion = ejecucion;
    }

    @Override
    public String toString() {
        return "Estado{" +
                "uuid='" + uuid + '\'' +
                ", id=" + id +
                ", estado='" + estado + '\'' +
                ", categorias=" + categorias +
                ", ejecucion=" + ejecucion +
                '}';
    }
}
