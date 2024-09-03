package com.mantum.cmms.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mantum.R;
import com.mantum.component.adapter.handler.ViewAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmObject;

public class Cliente extends RealmObject implements Serializable, ViewAdapter<Cliente> {

    @SerializedName("id")
    private Long id;

    @SerializedName("cedula")
    private String cedula;

    @SerializedName("nombre")
    private String nombre;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getNombre();
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return getCedula();
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
        return R.drawable.persona;
    }

    @Override
    public boolean compareTo(Cliente value) {
        return getCedula().equals(value.getCedula());
    }

    public static class Request implements Serializable {

        private Integer version;

        private List<Cliente> listaCliente;

        public Request() {
            this.listaCliente = new ArrayList<>();
        }

        public List<Cliente> getCliente() {
            return listaCliente;
        }

        public void setCliente(List<Cliente> cliente) {
            this.listaCliente = cliente;
        }

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }
    }
}