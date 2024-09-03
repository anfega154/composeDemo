package com.mantum.cmms.entity;

import java.util.List;

import io.realm.RealmObject;

public class TipoMovimiento extends RealmObject {

    private String id;

    private String nombre;

    private String movimientovalido;

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getMovimientovalido() {
        return movimientovalido;
    }

    public class Request {

        private final List<TipoMovimiento> tipoMovimientos;

        public Request(List<TipoMovimiento> tipoMovimientos) {
            this.tipoMovimientos = tipoMovimientos;
        }

        public List<TipoMovimiento> getTipoMovimientos() {
            return tipoMovimientos;
        }
    }
}
