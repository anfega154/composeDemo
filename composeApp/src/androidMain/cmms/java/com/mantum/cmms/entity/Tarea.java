package com.mantum.cmms.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mantum.demo.R;
import com.mantum.cmms.adapter.handler.TareaHandler;
import com.mantum.component.adapter.handler.ViewAdapter;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmObject;

public class Tarea extends RealmObject implements ViewAdapter<Tarea> {

    private String uuid;

    private Long id;

    private String codigo;

    @SerializedName("nombre")
    private String tarea;

    private String descripcion;

    private int tiempobase;

    private String tiempobasetexto;

    private boolean critica;

    private int orden;

    private boolean ejecutada;

    @SerializedName("ultimoejecutor")
    private String ejecutor;

    @SerializedName("fechaultimaejecucion")
    private String fechaejecucion;

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

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getTarea() {
        return tarea;
    }

    public void setTarea(String tarea) {
        this.tarea = tarea;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getTiempobase() {
        return tiempobase;
    }

    public void setTiempobase(int tiempobase) {
        this.tiempobase = tiempobase;
    }

    public String getTiempobasetexto() {
        return tiempobasetexto;
    }

    public void setTiempobasetexto(String tiempobasetexto) {
        this.tiempobasetexto = tiempobasetexto;
    }

    public boolean isCritica() {
        return critica;
    }

    public void setCritica(boolean critica) {
        this.critica = critica;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public boolean isEjecutada() {
        return ejecutada;
    }

    public void setEjecutada(boolean ejecutada) {
        this.ejecutada = ejecutada;
    }

    public String getEjecutor() {
        return ejecutor;
    }

    public void setEjecutor(String ejecutor) {
        this.ejecutor = ejecutor;
    }

    public String getFechaejecucion() {
        return fechaejecucion;
    }

    public void setFechaejecucion(String fechaejecucion) {
        this.fechaejecucion = fechaejecucion;
    }

    @NonNull
    @Override
    public String getTitle() {
        if (getCodigo() != null && getTarea() != null) {
            return getCodigo() + " | " + getTarea();
        }
        return "";
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return getEjecutor() != null ? "Último ejecutor: " + getEjecutor() : "";
    }

    @Nullable
    @Override
    public String getSummary() {
        return getFechaejecucion() != null ? "Última fecha ejecución: " + getFechaejecucion() : "";
    }

    @Nullable
    @Override
    public String getIcon() {
        return null;
    }

    @Nullable
    @Override
    public Integer getDrawable() {
        return isEjecutada() ? R.drawable.ic_check_orange : R.drawable.ic_circle_transparent;
    }

    @Override
    public boolean compareTo(Tarea value) {
        return getId().equals(value.id);
    }

    public static class TareaHelper implements TareaHandler<TareaHelper> {

        private Long id;

        private String codigo;

        private String tarea;

        private String descripcion;

        private boolean critica;

        private int orden;

        private boolean ejecutada;

        private String ejecutor;

        private String fechaejecucion;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        @Override
        public String getCodigo() {
            return codigo;
        }

        public void setCodigo(String codigo) {
            this.codigo = codigo;
        }

        @Override
        public String getTarea() {
            return tarea;
        }

        public void setTarea(String tarea) {
            this.tarea = tarea;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        @Override
        public boolean isCritica() {
            return critica;
        }

        public void setCritica(boolean critica) {
            this.critica = critica;
        }

        public int getOrden() {
            return orden;
        }

        public void setOrden(int orden) {
            this.orden = orden;
        }

        @Override
        public boolean isEjecutada() {
            return ejecutada;
        }

        public void setEjecutada(boolean ejecutada) {
            this.ejecutada = ejecutada;
        }

        public String getEjecutor() {
            return ejecutor;
        }

        public void setEjecutor(String ejecutor) {
            this.ejecutor = ejecutor;
        }

        public String getFechaejecucion() {
            return fechaejecucion;
        }

        public void setFechaejecucion(String fechaejecucion) {
            this.fechaejecucion = fechaejecucion;
        }

        @Override
        public boolean compareTo(TareaHelper value) {
            return getId().equals(value.getId());
        }

        public static ArrayList<TareaHelper> factory(List<Tarea> values) {
            ArrayList<TareaHelper> factory = new ArrayList<>();
            for (Tarea value : values) {
                TareaHelper tareaHelper = new TareaHelper();
                tareaHelper.setId(value.getId());
                tareaHelper.setCodigo(value.getCodigo());
                tareaHelper.setTarea(value.getTarea());
                tareaHelper.setDescripcion(value.getDescripcion());
                tareaHelper.setCritica(value.isCritica());
                tareaHelper.setOrden(value.getOrden());
                tareaHelper.setEjecutada(value.isEjecutada());
                tareaHelper.setEjecutor(value.getEjecutor());
                tareaHelper.setFechaejecucion(value.getFechaejecucion());
                factory.add(tareaHelper);
            }
            return factory;
        }
    }
}
