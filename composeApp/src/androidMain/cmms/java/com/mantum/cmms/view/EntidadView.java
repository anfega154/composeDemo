package com.mantum.cmms.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.demo.R;
import com.mantum.cmms.entity.Actividad;
import com.mantum.cmms.entity.Entidad;
import com.mantum.component.adapter.handler.ViewInformationChildrenAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EntidadView implements ViewInformationChildrenAdapter<EntidadView> {

    private Long id;

    private String name;

    private String tipo;

    private List<Long> actividades;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public List<Long> getActividades() {
        return actividades;
    }

    public void setActividades(List<Long> actividades) {
        this.actividades = actividades;
    }

    @Override
    public boolean compareTo(EntidadView value) {
        return getId().equals(value.getId());
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public File getFile() {
        return null;
    }

    @Nullable
    @Override
    public Integer getDrawable() {
        switch (getTipo()) {
            case "Equipo" : return R.drawable.equipo;
            case "InstalacionLocativa" : return R.drawable.locativa;
            case "InstalacionProceso" : return R.drawable.proceso;
            case "Pieza" : return R.drawable.pieza;
            case "Componente" : return R.drawable.componente;
            default: return R.drawable.image;
        }
    }

    @Nullable
    @Override
    public Integer getImageColor() {
        return null;
    }

    @Nullable
    @Override
    public Integer getTextColor() {
        return null;
    }

    @NonNull
    public static List<EntidadView> factory(@NonNull List<Entidad> entidades) {
        List<EntidadView> results = new ArrayList<>();
        for (Entidad entidad : entidades) {
            List<Long> actividades = new ArrayList<>();
            for (Actividad actividad : entidad.getActividades()) {
                actividades.add(actividad.getId());
            }

            EntidadView entidadView = new EntidadView();
            entidadView.setId(entidad.getId());
            entidadView.setName(entidad.getCodigo() + " | " + entidad.getNombre());
            entidadView.setTipo(entidad.getTipo());
            entidadView.setActividades(actividades);
            results.add(entidadView);
        }
        return results;
    }
}