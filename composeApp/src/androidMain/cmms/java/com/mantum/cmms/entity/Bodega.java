package com.mantum.cmms.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Bodega extends RealmObject {

    @PrimaryKey
    private Long id;

    private String codigo;

    private String nombre;

    private Cuenta cuenta;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Cuenta getCuenta() { return cuenta; }

    public void setCuenta(Cuenta cuenta) { this.cuenta = cuenta; }

    @NonNull
    public String getTitle() { return null; }

    @Nullable
    public String getSubtitle() { return null; }

    public class Request {

        private final List<Bodega> stores;

        public Request(List<Bodega> stores) {
            this.stores = stores;
        }

        public List<Bodega> getBodegas() {
            return stores;
        }
    }
}
