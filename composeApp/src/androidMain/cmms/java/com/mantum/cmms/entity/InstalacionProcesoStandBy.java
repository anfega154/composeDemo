package com.mantum.cmms.entity;

import io.realm.RealmObject;

public class InstalacionProcesoStandBy extends RealmObject {

    private Long id;
    private Cuenta cuenta;
    private String nombre;
    private String codigo;
    private String descripcion;
    private String infomaps;
    private String externo;
    private Boolean standby;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Cuenta getCuenta() { return cuenta; }

    public void setCuenta(Cuenta cuenta) { this.cuenta = cuenta; }

    public String getNombre() { return nombre; }

    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCodigo() { return codigo; }

    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getDescripcion() { return descripcion; }

    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getInfomaps() { return infomaps; }

    public void setInfomaps(String infomaps) { this.infomaps = infomaps; }

    public String getExterno() { return externo; }

    public void setExterno(String externo) { this.externo = externo; }

    public Boolean getStandby() { return standby; }

    public void setStandby(Boolean standby) { this.standby = standby; }

}

