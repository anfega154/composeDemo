package com.mantum.cmms.entity;

import io.realm.RealmObject;

public class SSxOT extends RealmObject {

    private String id;

    private String codigoss;

    private String codigoexterno;

    private String codigoexterno2;

    public String getId() {
        return id;
    }

    public String getCodigoss() {
        return codigoss;
    }

    public String getCodigoexterno() {
        return codigoexterno;
    }

    public String getCodigoexterno2() {
        return codigoexterno2;
    }
}