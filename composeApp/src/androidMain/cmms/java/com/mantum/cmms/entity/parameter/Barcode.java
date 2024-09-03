package com.mantum.cmms.entity.parameter;

import io.realm.RealmObject;

public class Barcode extends RealmObject {

    private String codigo;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}
