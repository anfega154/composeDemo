package com.mantum.cmms.helper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.mantum.cmms.entity.Mochila;
import com.mantum.cmms.entity.Recurso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RecursoHelper implements Serializable {

    private final List<Helper> recursos;

    public RecursoHelper() {
        this.recursos = new ArrayList<>();
    }

    private RecursoHelper(List<Helper> recursos) {
        this.recursos = recursos;
    }

    public List<Recurso> getRecursos() {
        List<Recurso> recursos = new ArrayList<>();
        for (Helper helper : this.recursos) {
            Recurso recurso = new Recurso();
            recurso.setId(helper.getId());
            recurso.setCodigo(helper.getCodigo());
            recurso.setNombre(helper.getNombre());
            recurso.setCantidad(helper.getCantidad());
            recurso.setSigla(helper.getSigla());
            recurso.setObservaciones(helper.getObservaciones());
            recurso.setUbicacion(helper.getUbicacion());
            recursos.add(recurso);
        }
        return recursos;
    }

    public List<Mochila> getMochilas() {
        List<Mochila> recursos = new ArrayList<>();
        for (Helper helper : this.recursos) {
            Mochila mochila = new Mochila();
            mochila.setId(helper.getId());
            mochila.setIdphrec(helper.getIdphrec());
            mochila.setCodigo(helper.getCodigo());
            mochila.setCodigoph(helper.getCodigoph());
            mochila.setNombre(helper.getNombre());
            mochila.setCantidad(helper.getCantidad());
            mochila.setSigla(helper.getSigla());
            mochila.setObservaciones(helper.getObservaciones());
            mochila.setUbicacion(helper.getUbicacion());
            recursos.add(mochila);
        }
        return recursos;
    }

    public boolean isEmpty() {
        return this.recursos.isEmpty();
    }

    public void clear() {
        this.recursos.clear();
    }

    public void add(Helper recurso) {
        boolean ok = true;
        for (Helper include : this.recursos) {
            if (recurso.getId().equals(include.getId())) {
                ok = false;
                break;
            }
        }

        if (ok) {
            this.recursos.add(recurso);
        }
    }

    public void remove(Helper helper) {
        this.recursos.remove(helper);
    }

    public int size() {
        return this.recursos.size();
    }

    public Helper get(int index) {
        return this.recursos.get(index);
    }

    @NonNull
    public static RecursoHelper recursoAdapter(@NonNull List<Recurso> recursos) {
        List<Helper> helpers = new ArrayList<>();
        for (Recurso recurso : recursos) {
            Helper helper = new Helper();
            helper.setId(recurso.getId());
            helper.setCodigo(recurso.getCodigo());
            helper.setNombre(recurso.getNombre());
            helper.setCantidad(recurso.getCantidad());
            helper.setSigla(recurso.getSigla());
            helper.setUbicacion(recurso.getUbicacion());
            helper.setObservaciones(recurso.getObservaciones());
            helpers.add(helper);
        }
        return new RecursoHelper(helpers);
    }
    @NonNull
    public static RecursoHelper recursoAdapter(Recurso recurso) {
        List<Helper> helpers = new ArrayList<>();
        Helper helper = new Helper();
        helper.setId(recurso.getId());
        helper.setCodigo(recurso.getCodigo());
        helper.setNombre(recurso.getNombre());
        helper.setCantidad(recurso.getCantidad());
        helper.setSigla(recurso.getSigla());
        helper.setUbicacion(recurso.getUbicacion());
        helper.setObservaciones(recurso.getObservaciones());
        helpers.add(helper);
        return new RecursoHelper(helpers);
    }


    public static RecursoHelper mochilaAdapter(List<Mochila> recursos) {
        List<Helper> helpers = new ArrayList<>();
        for (Mochila recurso : recursos) {
            Helper helper = new Helper();
            helper.setId(recurso.getId());
            helper.setIdphrec(recurso.getIdphrec());
            helper.setCodigo(recurso.getCodigo());
            helper.setCodigoph(recurso.getCodigoph());
            helper.setNombre(recurso.getNombre());
            helper.setCantidad(recurso.getCantidad());
            helper.setSigla(recurso.getSigla());
            helper.setUbicacion(recurso.getUbicacion());
            helper.setObservaciones(recurso.getObservaciones());
            helpers.add(helper);
        }
        return new RecursoHelper(helpers);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Nullable
    public static RecursoHelper fromJson(@NonNull String recurso) {
        try {
            return new Gson().fromJson(recurso, RecursoHelper.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static class Helper implements Serializable {

        private Long id;

        private Long idphrec;

        private String codigo;

        private String codigoph;

        private String nombre;

        private String cantidad;

        private String sigla;

        private String ubicacion;

        private String observaciones;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getIdphrec() {
            return idphrec;
        }

        public void setIdphrec(Long idphrec) {
            this.idphrec = idphrec;
        }

        public String getCodigo() {
            return codigo;
        }

        public void setCodigo(String codigo) {
            this.codigo = codigo;
        }

        public String getCodigoph() {
            return codigoph;
        }

        public void setCodigoph(String codigoph) {
            this.codigoph = codigoph;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getCantidad() {
            return cantidad;
        }

        public void setCantidad(String cantidad) {
            this.cantidad = cantidad;
        }

        public String getSigla() {
            return sigla;
        }

        public void setSigla(String sigla) {
            this.sigla = sigla;
        }

        public String getUbicacion() {
            return ubicacion;
        }

        public void setUbicacion(String ubicacion) {
            this.ubicacion = ubicacion;
        }

        public String getObservaciones() {
            return observaciones;
        }

        public void setObservaciones(String observaciones) {
            this.observaciones = observaciones;
        }
    }

    public static class ViewRecurso {

        private final List<Recurso> recursos;

        public ViewRecurso(List<Recurso> recursos) {
            this.recursos = recursos;
        }

        public List<Recurso> getRecursos() {
            return recursos;
        }
    }

    public static class ViewMochila {

        private final List<Mochila> mochila;

        public ViewMochila(List<Mochila> mochila) {
            this.mochila = mochila;
        }

        public List<Mochila> getMochila() {
            return mochila;
        }
    }
}