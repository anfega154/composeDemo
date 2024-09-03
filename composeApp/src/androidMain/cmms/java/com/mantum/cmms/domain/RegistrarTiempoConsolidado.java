package com.mantum.cmms.domain;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.UUID;

public class RegistrarTiempoConsolidado {

    private Long idot;

    private String token;

    @SerializedName("fecha_inicio")
    private String fechaInicio;

    @SerializedName("fecha_fin")
    private String fechaFin;

    @SerializedName("hora_llegada")
    private String horaLlegada;

    @SerializedName("hora_inicio")
    private String horaInicio;

    @SerializedName("hora_fin")
    private String horaFin;

    @SerializedName("hora_salida")
    private String horaSalida;

    @SerializedName("tipos")
    private ArrayList<Tipo> tipos;

    public ArrayList<Tipo> getTipos() {
        return tipos;
    }

    public void setTipos(ArrayList<Tipo> tipos) {
        this.tipos = tipos;
    }

    public RegistrarTiempoConsolidado() {
        this.token = UUID.randomUUID().toString();
    }

    public Long getIdot() {
        return idot;
    }

    public void setIdot(Long idot) {
        this.idot = idot;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getHoraLlegada() {
        return horaLlegada;
    }

    public void setHoraLlegada(String horaLlegada) {
        this.horaLlegada = horaLlegada;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }

    public String getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(String horaSalida) {
        this.horaSalida = horaSalida;
    }

    public void setTipos(String tiempoTotal, String tiempoCliente, String tiempoPorL3, String tiempoFactoresExternos) {
        ArrayList<Tipo> listaTipos = new ArrayList<>();

        Tipo tipo1 = new Tipo();
        tipo1.idtipo = 1;
        tipo1.horas = tiempoTotal;

        Tipo tipo2 = new Tipo();
        tipo2.idtipo = 2;
        tipo2.horas = tiempoCliente;

        Tipo tipo3 = new Tipo();
        tipo3.idtipo = 3;
        tipo3.horas = tiempoPorL3;

        Tipo tipo4 = new Tipo();
        tipo4.idtipo = 4;
        tipo4.horas = tiempoFactoresExternos;

        listaTipos.add(tipo1);
        listaTipos.add(tipo2);
        listaTipos.add(tipo3);
        listaTipos.add(tipo4);

        this.setTipos(listaTipos);
    }

    public class Tipo {

        Integer idtipo;

        String horas;

    }


    public String toJson() {
        return new Gson().toJson(this);
    }
}
