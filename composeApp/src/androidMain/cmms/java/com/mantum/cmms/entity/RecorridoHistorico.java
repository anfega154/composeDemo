package com.mantum.cmms.entity;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.mantum.component.adapter.handler.ViewTimeLineAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RecorridoHistorico extends RealmObject implements ViewTimeLineAdapter<RecorridoHistorico> {

    @PrimaryKey
    private String uuid;
    private Long id;
    private Cuenta cuenta;
    private Long identidad;
    private String tipoentidad;
    private Long idcategoria;
    private String categoria;
    private Date fecha;
    @SerializedName("observacion")
    private String comentario;
    private String estado;
    private String personal;
    private boolean mostrar;
    private boolean finalizanovedad;

    public RecorridoHistorico() {
        this.uuid = UUID.randomUUID().toString();
        this.mostrar = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getIdentidad() {
        return identidad;
    }

    public void setIdentidad(Long identidad) {
        this.identidad = identidad;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPersonal() {
        return personal;
    }

    public void setPersonal(String personal) {
        this.personal = personal;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Long getIdcategoria() {
        return idcategoria;
    }

    public void setIdcategoria(Long idcategoria) {
        this.idcategoria = idcategoria;
    }

    public boolean isMostrar() {
        return mostrar;
    }

    public void setMostrar(boolean mostrar) {
        this.mostrar = mostrar;
    }

    public String getTipoentidad() {
        return tipoentidad;
    }

    public void setTipoentidad(String tipoentidad) {
        this.tipoentidad = tipoentidad;
    }

    public boolean isFinalizanovedad() {
        return finalizanovedad;
    }

    public void setFinalizanovedad(boolean finalizanovedad) {
        this.finalizanovedad = finalizanovedad;
    }

    @NonNull
    @Override
    public String getDate() {
        SimpleDateFormat simpleDateFormat
                = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return simpleDateFormat.format(getFecha());
    }

    @NonNull
    @Override
    public String getTitle() {
        if (getCategoria() != null && !getCategoria().isEmpty()) {
            return getEstado() + " - " + getCategoria();
        }
        return getEstado();
    }

    @NonNull
    @Override
    public String getMessage() {
        String comentario = getComentario() != null ? getComentario() : "";
        if (getPersonal() != null && !getPersonal().isEmpty()) {
            return getPersonal() + "\n" + comentario;
        }

        return comentario;
    }

    @Override
    public boolean compareTo(RecorridoHistorico value) {
        return getId().equals(value.getId()) && getUuid().equals(value.getUuid());
    }
}