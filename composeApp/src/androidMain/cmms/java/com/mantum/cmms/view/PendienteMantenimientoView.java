package com.mantum.cmms.view;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.cmms.entity.Pendiente;
import com.mantum.component.OnInvoke;
import com.mantum.component.adapter.handler.ViewInformationAdapter;

import java.util.ArrayList;
import java.util.List;

public class PendienteMantenimientoView implements ViewInformationAdapter<PendienteMantenimientoView, EntidadView> {

    private Long id;

    private String codigo;

    private String fecha;

    private String descripcion;

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

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getCodigo();
    }

    @Nullable
    @Override
    public String getSummary() {
        return null;
    }

    @Nullable
    @Override
    public Integer getColorSummary() {
        return null;
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return getFecha();
    }

    @Nullable
    @Override
    public String getDescription() {
        return getDescripcion();
    }

    @Override
    public List<EntidadView> getChildren() {
        return new ArrayList<>();
    }

    @Nullable
    @Override
    public String getState() {
        return null;
    }

    @Override
    public boolean isShowAction() {
        return false;
    }

    @Nullable
    @Override
    public String getActionName() {
        return null;
    }

    @Nullable
    @Override
    public OnInvoke<PendienteMantenimientoView> getAction(@NonNull Context context) {
        return null;
    }

    @Override
    public boolean compareTo(PendienteMantenimientoView value) {
        return getId().equals(value.getId());
    }

    @NonNull
    public static List<PendienteMantenimientoView> factory(@NonNull List<Pendiente> values) {
        List<PendienteMantenimientoView> results = new ArrayList<>();
        for (Pendiente value : values) {
            PendienteMantenimientoView pendienteMantenimientoView = new PendienteMantenimientoView();
            pendienteMantenimientoView.setId(value.getId());
            pendienteMantenimientoView.setCodigo(value.getCodigo());
            pendienteMantenimientoView.setFecha(value.getFecha());
            pendienteMantenimientoView.setDescripcion(value.getDescripcion());
            results.add(pendienteMantenimientoView);
        }
        return results;
    }
}