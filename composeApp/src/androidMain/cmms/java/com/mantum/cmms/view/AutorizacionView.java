package com.mantum.cmms.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.R;
import com.mantum.cmms.entity.Autorizaciones;
import com.mantum.component.adapter.handler.ViewAdapter;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AutorizacionView implements ViewAdapter<AutorizacionView>, Serializable {

    private static final String MODULO_AUTORIZACION = "Autorizacion";

    private Long id;

    private String codigo;

    private String fechainicio;

    private String fechafin;

    private String tipo;

    private String marca;

    private String modulo;

    private String descripcion;

    private String locacion;

    private String empresa;

    private String detalle;

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

    public String getFechainicio() {
        return fechainicio;
    }

    public void setFechainicio(String fechainicio) {
        this.fechainicio = fechainicio;
    }

    public String getFechafin() {
        return fechafin;
    }

    public void setFechafin(String fechafin) {
        this.fechafin = fechafin;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getLocacion() {
        return locacion;
    }

    public void setLocacion(String locacion) {
        this.locacion = locacion;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getDetalle() { return detalle; }

    public void setDetalle(String detalle) { this.detalle = detalle; }

    @Override
    public boolean compareTo(AutorizacionView value) {
        return getId().equals(value.getId());
    }

    @NonNull
    @Override
    public String getTitle() {
        return MODULO_AUTORIZACION.equals(getModulo()) ? getCodigo() : getMarca();
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return getTipo();
    }

    @Nullable
    @Override
    public String getSummary() {
        return getFechainicio() != null && !getFechainicio().isEmpty() && getFechafin() != null && !getFechafin().isEmpty()
                ? getFechainicio() + " - " + getFechafin()
                : null;
    }

    @Nullable
    @Override
    public String getIcon() {
        return null;
    }

    @Nullable
    public String getState() {
        return getDetalle();
    }

    @Nullable
    @Override
    public Integer getDrawable() {
        return MODULO_AUTORIZACION.equals(getModulo()) ? R.drawable.police : R.drawable.store;
    }

    @NonNull
    public static List<AutorizacionView> factory(@NonNull List<Autorizaciones> autorizaciones) {
        List<AutorizacionView> results = new ArrayList<>();

        SimpleDateFormat simpleDateFormat
                = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        for (Autorizaciones self : autorizaciones) {
            AutorizacionView autorizacionView = new AutorizacionView();
            autorizacionView.setId(self.getId());
            autorizacionView.setCodigo(self.getCodigo());
            if (self.getFechainicio() != null) {
                autorizacionView.setFechainicio(simpleDateFormat.format(self.getFechainicio()));
            }

            if (self.getFechafin() != null) {
                autorizacionView.setFechafin(simpleDateFormat.format(self.getFechafin()));
            }

            autorizacionView.setTipo(self.getTipo());
            autorizacionView.setMarca(self.getMarca());
            autorizacionView.setModulo(self.getModulo());
            autorizacionView.setDescripcion(self.getDescripcion());
            autorizacionView.setLocacion(self.getLocacion());
            autorizacionView.setEmpresa(self.getEmpresa());
            autorizacionView.setDetalle(self.getDetalle());
            results.add(autorizacionView);
        }
        return results;
    }
}
