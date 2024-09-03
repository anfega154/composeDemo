package com.mantum.cmms.view;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.cmms.entity.SolicitudServicio;
import com.mantum.component.OnInvoke;
import com.mantum.component.adapter.handler.ViewInformationAdapter;

import java.util.ArrayList;
import java.util.List;

public class SolicitudServicioView implements ViewInformationAdapter<SolicitudServicioView, EntidadView> {

    private String UUID;

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

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
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
    public OnInvoke<SolicitudServicioView> getAction(@NonNull Context context) {
        return null;
    }


    @Override
    public boolean compareTo(SolicitudServicioView value) {
        return getId().equals(value.id);
    }

    @NonNull
    public static List<SolicitudServicioView> factory(@NonNull List<SolicitudServicio> values) {
        List<SolicitudServicioView> results = new ArrayList<>();
        for (SolicitudServicio value : values) {
            SolicitudServicioView solicitudServicioView = new SolicitudServicioView();
            solicitudServicioView.setUUID(value.getUUID());
            solicitudServicioView.setId(value.getId());
            solicitudServicioView.setCodigo(value.getCodigo());
            solicitudServicioView.setFecha(value.getFecha());
            solicitudServicioView.setDescripcion(value.getDescripcion());
            results.add(solicitudServicioView);
        }
        return results;
    }
}