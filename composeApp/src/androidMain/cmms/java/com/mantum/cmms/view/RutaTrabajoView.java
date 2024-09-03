package com.mantum.cmms.view;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.cmms.entity.ListaChequeo;
import com.mantum.cmms.entity.RutaTrabajo;
import com.mantum.component.OnInvoke;
import com.mantum.component.adapter.handler.ViewInformationAdapter;
import com.mantum.component.mapped.IgnoreField;

import java.util.ArrayList;
import java.util.List;

public class RutaTrabajoView implements ViewInformationAdapter<RutaTrabajoView, EntidadView> {

    private String UUID;

    private Long id;

    private Long idEjecucion;

    private String codigo;

    private String nombre;

    private String fecha;

    private String descripcion;

    private String tipo;

    @IgnoreField
    private boolean diligenciada;

    @IgnoreField
    private boolean multiple;

    @IgnoreField
    private Long idot;

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

    public Long getIdEjecucion() {
        return idEjecucion;
    }

    public void setIdEjecucion(Long idEjecucion) {
        this.idEjecucion = idEjecucion;
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isDiligenciada() {
        return diligenciada;
    }

    public void setDiligenciada(boolean diligenciada) {
        this.diligenciada = diligenciada;
    }

    public Long getIdot() {
        return idot;
    }

    public void setIdot(Long idot) {
        this.idot = idot;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getCodigo();
    }

    @Nullable
    @Override
    public String getSummary() {
        return getNombre();
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
    public OnInvoke<RutaTrabajoView> getAction(@NonNull Context context) {
        return null;
    }

    @Override
    public boolean compareTo(RutaTrabajoView value) {
        if (value.getIdot() != null)
            return value.getIdot().equals(getIdot()) && value.getId().equals(getId());
        else
            return value.getIdEjecucion() != null ? value.getIdEjecucion().equals(getIdEjecucion()) : value.getId().equals(getId());
    }

    public static RutaTrabajoView factory(ListaChequeo value) {
        RutaTrabajoView rutaTrabajoView = new RutaTrabajoView();
        rutaTrabajoView.setUUID(String.valueOf(value.getId()));
        rutaTrabajoView.setId(value.getId());
        rutaTrabajoView.setCodigo(value.getCodigo());
        rutaTrabajoView.setNombre(value.getNombre());
        rutaTrabajoView.setFecha(value.getEspecialidad());
        rutaTrabajoView.setDescripcion(value.getDescripcion());
        return rutaTrabajoView;
    }

    public static RutaTrabajoView factory(RutaTrabajo value) {
        RutaTrabajoView rutaTrabajoView = new RutaTrabajoView();
        rutaTrabajoView.setUUID(value.getUUID());
        rutaTrabajoView.setId(value.getId());
        rutaTrabajoView.setIdEjecucion(value.getIdejecucion());
        rutaTrabajoView.setNombre(value.getNombre());
        rutaTrabajoView.setCodigo(value.getCodigo());
        rutaTrabajoView.setFecha(value.getFecha());
        rutaTrabajoView.setDescripcion(value.getDescripcion());
        rutaTrabajoView.setTipo(value.getTipogrupo());
        rutaTrabajoView.setDiligenciada(value.isDiligenciada());
        rutaTrabajoView.setIdot(value.getIdot());
        rutaTrabajoView.setMultiple(value.isMultiple());

        return rutaTrabajoView;
    }

    @NonNull
    public static List<RutaTrabajoView> factory(@NonNull List<RutaTrabajo> values) {
        List<RutaTrabajoView> results = new ArrayList<>();
        for (RutaTrabajo value : values) {
            RutaTrabajoView rutaTrabajoView = new RutaTrabajoView();
            rutaTrabajoView.setUUID(value.getUUID());
            rutaTrabajoView.setId(value.getId());
            rutaTrabajoView.setIdEjecucion(value.getIdejecucion());
            rutaTrabajoView.setNombre(value.getNombre());
            rutaTrabajoView.setCodigo(value.getCodigo());
            rutaTrabajoView.setFecha(value.getFecha());
            rutaTrabajoView.setDescripcion(value.getDescripcion());
            rutaTrabajoView.setTipo(value.getTipogrupo());
            rutaTrabajoView.setDiligenciada(value.isDiligenciada());
            rutaTrabajoView.setIdot(value.getIdot());
            rutaTrabajoView.setMultiple(value.isMultiple());
            results.add(rutaTrabajoView);
        }
        return results;
    }
}
