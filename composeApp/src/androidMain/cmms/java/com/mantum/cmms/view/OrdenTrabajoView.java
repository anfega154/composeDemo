package com.mantum.cmms.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.cmms.activity.TerminarOrdenTrabajoActivity;
import com.mantum.cmms.entity.ANS;
import com.mantum.cmms.entity.Asignada;
import com.mantum.cmms.entity.Ejecutores;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.cmms.entity.Sitio;
import com.mantum.component.Mantum;
import com.mantum.component.OnInvoke;
import com.mantum.component.adapter.handler.ViewInformationAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class OrdenTrabajoView implements ViewInformationAdapter<OrdenTrabajoView, EntidadView> {

    private String UUID;

    private Long id;

    private String codigo;

    private String fecha;

    private String descripcion;

    private String state;

    private List<EntidadView> entidades;

    private List<Ejecutores> ejecutores;

    private Sitio sitio;

    public int orden;

    private boolean mostrarAccion;

    private String summary;

    private Integer colorSummary;

    private List<RutaTrabajoView> listaChequeo;

    private OrdenTrabajoView() {
        this.entidades = new ArrayList<>();
        this.ejecutores = new ArrayList<>();
        this.mostrarAccion = false;
        this.summary = null;
        this.colorSummary = null;
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

    public List<EntidadView> getEntidades() {
        return entidades;
    }

    public void setEntidades(List<EntidadView> entidades) {
        this.entidades = entidades;
    }

    public List<Ejecutores> getEjecutores() {
        return ejecutores;
    }

    public void setEjecutores(List<Ejecutores> ejecutores) {
        this.ejecutores = ejecutores;
    }

    public Sitio getSitio() {
        return sitio;
    }

    public void setSitio(Sitio sitio) {
        this.sitio = sitio;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public long[] getActividades() {
        List<Long> actividades = new ArrayList<>();
        for (EntidadView entidad : entidades) {
            actividades.addAll(entidad.getActividades());
        }

        int total = actividades.size();
        long[] response = new long[total];
        for (int i = 0; i < total; i++) {
            response[i] = actividades.get(i);
        }

        return response;
    }


    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public boolean isMostrarAccion() {
        return mostrarAccion;
    }

    public void setMostrarAccion(boolean mostrarAccion) {
        this.mostrarAccion = mostrarAccion;
    }

    public List<RutaTrabajoView> getListaChequeo() {
        return listaChequeo;
    }

    public void setListaChequeo(List<RutaTrabajoView> listaChequeo) {
        this.listaChequeo = listaChequeo;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getCodigo();
    }

    @Nullable
    @Override
    public String getSummary() {
        return summary;
    }

    @Nullable
    @Override
    public Integer getColorSummary() {
        return colorSummary;
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
        return getEntidades();
    }

    @Nullable
    @Override
    public String getState() {
        return String.format("%s%%", this.state);
    }

    @Override
    public boolean isShowAction() {
        return isMostrarAccion();
    }

    @Nullable
    @Override
    public String getActionName() {
        return "Terminar";
    }

    @Nullable
    @Override
    public OnInvoke<OrdenTrabajoView> getAction(@NonNull Context context) {
        return value -> {
            Bundle bundle = new Bundle();
            bundle.putLong(Mantum.KEY_ID, value.getId());
            bundle.putString(TerminarOrdenTrabajoActivity.KEY_CODE, value.getCodigo());

            Intent intent = new Intent(context, TerminarOrdenTrabajoActivity.class);
            intent.putExtras(bundle);

            context.startActivity(intent);
            return true;
        };
    }

    public boolean isCienPorciento() {
        return "100.0%".equals(getState()) || "100%".equals(getState());
    }

    public boolean isRecorrido() {
        // los preventivos no provienen de ss
        return getSitio() != null && getSitio().getCodigo() != null ? true : false;
    }

    @Override
    public boolean compareTo(OrdenTrabajoView value) {
        return getId().equals(value.id);
    }

    @NonNull
    public static List<OrdenTrabajoView> factory(@NonNull List<OrdenTrabajo> values) {
        List<OrdenTrabajoView> results = new ArrayList<>();
        if (values.isEmpty()) {
            return results;
        }

        for (OrdenTrabajo value : values) {
            if (value == null) {
                continue;
            }
            results.add(OrdenTrabajoView.factory(value));
        }

        return results;
    }

    @NonNull
    public static List<OrdenTrabajoView> factory(@NonNull List<OrdenTrabajo> values, boolean mostrarAccion) {
        List<OrdenTrabajoView> results = new ArrayList<>();
        if (values.isEmpty()) {
            return results;
        }

        for (OrdenTrabajo value : values) {
            if (value == null) {
                continue;
            }
            results.add(OrdenTrabajoView.factory(value, mostrarAccion));
        }

        return results;
    }

    @Nullable
    public static OrdenTrabajoView factory(@NonNull Asignada asignada) {
        OrdenTrabajo value = asignada.getOrdenTrabajo();
        if (value == null) {
            return null;
        }

        OrdenTrabajoView ordenTrabajoView = factory(value);
        ordenTrabajoView.setMostrarAccion(false);
        if (!asignada.isTerminada() && ordenTrabajoView.isCienPorciento()) {
            ordenTrabajoView.setMostrarAccion(true);
        }

        return ordenTrabajoView;
    }

    @NonNull
    public static OrdenTrabajoView factory(@NonNull OrdenTrabajo value) {
        return factory(value, false);
    }

    @NonNull
    public static OrdenTrabajoView factory(@NonNull OrdenTrabajo value, boolean mostrarAccion) {
        OrdenTrabajoView ordenTrabajoView = new OrdenTrabajoView();
        ordenTrabajoView.setUUID(value.getUUID());
        ordenTrabajoView.setId(value.getId());
        ordenTrabajoView.setEntidades(EntidadView.factory(value.getEntidades()));
        ordenTrabajoView.setEjecutores(value.getEjecutores());
        ordenTrabajoView.setSitio(value.getSitio());
        ordenTrabajoView.setCodigo(value.getCodigo());
        ordenTrabajoView.setFecha(getFechas(value));
        ordenTrabajoView.setDescripcion(value.getDescripcion());
        ordenTrabajoView.setState(value.getPorcentaje());
        ordenTrabajoView.setOrden(value.getOrden());
        ordenTrabajoView.setMostrarAccion(false);
        ordenTrabajoView.setListaChequeo(RutaTrabajoView.factory(value.getListachequeo()));

        if (mostrarAccion) {
            if (value.getTerminada() != null && !value.getTerminada() && ordenTrabajoView.isCienPorciento()) {
                ordenTrabajoView.setMostrarAccion(true);
            }
        }

        if (!value.getAns().isEmpty() && value.isAsignada()) {
            ordenTrabajoView.setFecha(null); // Se oculta la fecha
            List<ANS> resultados = new ArrayList<>();
            for (ANS ans : value.getAns()) {
                if (ans.getFechafin() == null) {
                    resultados.add(ans);
                }
            }

            if (!resultados.isEmpty()) {
                Collections.sort(resultados, (o1, o2) -> o1.getVencimiento().compareTo(o2.getVencimiento()));
                ANS ans = resultados.get(0);
                if (ans != null) {
                    Calendar now = Calendar.getInstance();
                    long diff = ans.getVencimiento().getTime() - now.getTime().getTime();

                    ordenTrabajoView.colorSummary = Color.GREEN;
                    if (diff < 0) {
                        ordenTrabajoView.colorSummary = Color.RED;
                        diff = now.getTime().getTime() - ans.getVencimiento().getTime();
                    }

                    String texto = "0 minutos";
                    long minutes = (diff / 1000) / 60;
                    if (minutes > 0) {
                        texto = "";
                        long hours = minutes / 60;
                        if (hours > 0) {
                            String textoHora = " hora";
                            if (hours > 1) {
                                textoHora = " horas";
                            }
                            texto = hours + textoHora;
                        }

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(diff);
                        minutes = calendar.get(Calendar.MINUTE);
                        if (minutes > 0) {
                            if (texto.isEmpty()) {
                                texto = minutes + " minutos";
                            } else {
                                texto = texto + " y " + minutes + " minutos";
                            }
                        }
                    }

                    ordenTrabajoView.summary = ans.getNombre() + " " + texto;
                }
            }
        }

        return ordenTrabajoView;
    }

    private static String getFechas(OrdenTrabajo value) {
        String dates = "Programación: " + value.getFechainicio() + " / " + value.getFechafin() ;
        if(value.getFechainicioreal() != null)
            dates +=  " \n Real: " + value.getFechainicioreal() + " / " + value.getFechafinreal();

        return dates;
    }

}