package com.mantum.cmms.domain;

public class QRgenerico {

    private final Integer usuario;
    private final Integer idbodega;
    private final String expiracion;

    public QRgenerico() {
        this.usuario = new Integer(0);
        this.idbodega = new Integer(0);
        this.expiracion = new String();
    }

    public Integer getUsuario() { return usuario; }
    public Integer getIdbodega() {
        return idbodega;
    }
    public String getExpiracion() { return expiracion; }

}