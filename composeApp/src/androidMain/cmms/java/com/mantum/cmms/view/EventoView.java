package com.mantum.cmms.view;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.cmms.entity.Evento;
import com.mantum.component.OnInvoke;
import com.mantum.component.adapter.handler.ViewInformationAdapter;

import java.util.ArrayList;
import java.util.List;

public class EventoView implements ViewInformationAdapter<EventoView, EntidadView> {

    private Long id;

    private String nombre;

    private String fecha;

    private String descripcion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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
        return getNombre();
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
    public OnInvoke<EventoView> getAction(@NonNull Context context) {
        return null;
    }

    @Override
    public boolean compareTo(EventoView value) {
        return getId().equals(value.id);
    }

    @NonNull
    public static List<EventoView> factory(@NonNull List<Evento> values) {
        List<EventoView> results = new ArrayList<>();
        for (Evento value : values) {
            EventoView eventoView = new EventoView();
            eventoView.setId(value.getId());
            eventoView.setNombre(value.getEvento());
            eventoView.setFecha(value.getFecha());
            String descripcion = value.getLugar() == null || value.getLugar().isEmpty() ? value.getPersonal() : value.getLugar();
            eventoView.setDescripcion(descripcion + " - " + value.getDescripcion());
            results.add(eventoView);
        }
        return results;
    }
}
