package com.mantum.cmms.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Calendario extends RealmObject {

    @PrimaryKey
    private String uuid;

    private Long id;

    private Cuenta cuenta;

    @SerializedName("start_date")
    private String start;

    @SerializedName("end_date")
    private String end;

    @SerializedName("title_tooltip")
    private String titulo;

    @SerializedName("text_tooltip")
    private String descripcion;

    @SerializedName("type_tooltip")
    private String tipo;

    @SerializedName("all_day")
    private boolean diaCompleto;

    private Long idCalendario;

    @SerializedName("color")
    private String color;

    public Calendario() {
        this.uuid = UUID.randomUUID().toString();
        this.diaCompleto = false;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

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

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Long getIdCalendario() {
        return idCalendario;
    }

    public void setIdCalendario(Long idCalendario) {
        this.idCalendario = idCalendario;
    }

    public boolean isDiaCompleto() {
        return diaCompleto;
    }

    public void setDiaCompleto(boolean diaCompleto) {
        this.diaCompleto = diaCompleto;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public static class Request {

        @SerializedName("eventos")
        private final List<Calendario> calendario;

        public Request(List<Calendario> calendario) {
            this.calendario = calendario;
        }

        public List<Calendario> getCalendario() {
            return calendario;
        }
    }
}
