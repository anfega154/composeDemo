package com.mantum.cmms.entity;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Sitio extends RealmObject {

    @PrimaryKey
    private String uuid;

    private Cuenta cuenta;

    private String telefono;

    private String referenciatelefono;

    private String planta;

    private String direccion;

    private String referenciadireccion;

    private String cliente;

    private String codigo;

    private String nombre;

    private String departamento;

    private String pais;

    private String ciudad;

    private String tipoenlace;

    private String contacto;

    private String cargo;

    private String ingenieroresponsable;

    private String latitud;

    private String longitud;

    private String codigoss;

    private String codigoexterno;

    private String codigoexterno2;

    public Sitio() {
        this.uuid = UUID.randomUUID().toString();
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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getPlanta() {
        return planta;
    }

    public void setPlanta(String planta) {
        this.planta = planta;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
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

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getTipoenlace() {
        return tipoenlace;
    }

    public void setTipoenlace(String tipoenlace) {
        this.tipoenlace = tipoenlace;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getIngenieroresponsable() {
        return ingenieroresponsable;
    }

    public void setIngenieroresponsable(String ingenieroresponsable) {
        this.ingenieroresponsable = ingenieroresponsable;
    }

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
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

    public boolean isCoordenada() {
        return this.latitud != null && this.longitud != null;
    }

    public String getReferenciatelefono() {
        return referenciatelefono;
    }

    public void setReferenciatelefono(String referenciatelefono) {
        this.referenciatelefono = referenciatelefono;
    }

    public String getReferenciadireccion() {
        return referenciadireccion;
    }

    public void setReferenciadireccion(String referenciadireccion) {
        this.referenciadireccion = referenciadireccion;
    }
}