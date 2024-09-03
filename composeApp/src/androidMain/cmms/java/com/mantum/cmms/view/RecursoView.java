package com.mantum.cmms.view;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.R;
import com.mantum.cmms.entity.Recurso;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.component.adapter.handler.ViewAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract class RecursoView implements ViewAdapter<RecursoView> {

    private Long id;

    private String codigo;

    private String nombre;

    private String cantidad;

    private String cantidadasignada;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCantidad() {
        return cantidad;
    }

    public void setCantidad(String cantidad) {
        this.cantidad = cantidad;
    }

    public String getCantidadasignada() {
        return cantidadasignada;
    }

    public void setCantidadasignada(String cantidadasignada) {
        this.cantidadasignada = cantidadasignada;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getCodigo() != null ? getCodigo().trim() : "";
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return getNombre();
    }

    @Nullable
    @Override
    public String getSummary() {
        return getCantidad() + getCantidadasignada();
    }

    @Nullable
    @Override
    public String getIcon() {
        return null;
    }

    @Nullable
    @Override
    public Integer getDrawable() {
        return R.drawable.recursos;
    }

    @Override
    public boolean compareTo(RecursoView value) {
        return getId().equals(value.id);
    }

    public static class View extends RecursoView {

        private Context context;

        public Context getContext() {
            return context;
        }

        public void setContext(Context context) {
            this.context = context;
        }

        @Nullable
        @Override
        public String getSummary() {
            if (UserPermission.check(context, UserPermission.VER_CANTIDAD_ASIGNADA_RECURSO_OT)) {
                return  "Cantidad real: " + (getCantidad() == null || getCantidad().isEmpty() ? "0" : getCantidad()) + "\n" +
                        "Cantidad asignada: " + (getCantidadasignada() == null || getCantidadasignada().isEmpty() ? "0" : getCantidadasignada());
            }
            else {
                return "Cantidad real: " + (getCantidad() == null || getCantidad().isEmpty() ? "0" : getCantidad());
            }
        }

        public static List<RecursoView> factory(List<Recurso> recursos) {
            List<RecursoView> results = new ArrayList<>();
            for (Recurso recurso : recursos) {
                View view = new View();
                view.setId(recurso.getId());
                view.setCodigo(recurso.getCodigo());
                view.setNombre(recurso.getNombre());
                view.setCantidad(recurso.getCantidadreal());
                view.setCantidadasignada(recurso.getCantidadasignada());
                results.add(view);
            }
            return results;
        }
    }

    public static class Detail extends RecursoView {

        @Nullable
        @Override
        public String getSummary() {
            return "Cantidad: " + (getCantidad().isEmpty() ? "0" : getCantidad());
        }

        public static List<RecursoView> factory(List<Recurso> recursos) {
            List<RecursoView> results = new ArrayList<>();
            for (Recurso recurso : recursos) {
                Detail detail = new Detail();
                detail.setId(recurso.getId());
                detail.setCodigo(recurso.getCodigo());
                detail.setNombre(recurso.getNombre());
                detail.setCantidad(recurso.getCantidad());
                results.add(detail);
            }
            return results;
        }
    }
}