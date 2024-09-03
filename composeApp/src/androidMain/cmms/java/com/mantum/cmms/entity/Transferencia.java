package com.mantum.cmms.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.demo.R;
import com.mantum.cmms.database.Model;
import com.mantum.component.adapter.handler.ViewAdapter;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Transferencia extends RealmObject
        implements Model, Serializable, ViewAdapter<Transferencia> {

    @PrimaryKey
    private String UUID;

    private Long id;

    private Cuenta cuenta;

    private String codigo;

    private String fecha;

    private String personal;

    private RealmList<Activos> activos;

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
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

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getPersonal() {
        return personal;
    }

    public void setPersonal(String personal) {
        this.personal = personal;
    }

    public RealmList<Activos> getActivos() {
        return activos;
    }

    public void setActivos(RealmList<Activos> activos) {
        this.activos = activos;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getCodigo() != null ? getCodigo().trim() : "";
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return getFecha();
    }

    @Nullable
    @Override
    public String getSummary() {
        return null;
    }

    @Nullable
    @Override
    public String getIcon() {
        return null;
    }

    @Nullable
    @Override
    public Integer getDrawable() {
        return R.drawable.transfer;
    }

    @Override
    public boolean compareTo(Transferencia value) {
        return getId().equals(value.id);
    }

    public static class Request {

        private Integer version;

        private List<Transferencia> datos;

        public Request() {
            this.datos = Collections.emptyList();
        }

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }

        public List<Transferencia> getTransferencias() {
            return datos;
        }
    }
}
