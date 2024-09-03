package com.mantum.cmms.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mantum.component.adapter.handler.ViewAdapter;
import com.mantum.component.mapped.IgnoreField;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class OrdenTrabajo extends RealmObject implements ViewAdapter<OrdenTrabajo> {

    public static final String SELF = "OT";

    @PrimaryKey
    private String UUID;
    private Long id;
    private Cuenta cuenta;
    private String codigo;
    private String prioridad;
    private String fechainicio;
    private String fechafin;
    private String estado;
    private String descripcion;
    private String realimentacion;
    private String color;
    private String porcentaje;
    private String duracion;
    private int orden;
    private String fechainicioreal;
    private String fechafinreal;
    private RealmList<Entidad> entidades;
    private RealmList<Recurso> recursos;
    private RealmList<Falla> fallas;
    private RealmList<RepuestoManual> repuestos;
    private RealmList<Consumible> consumibles;
    private RealmList<Ejecutores> ejecutores;
    private RealmList<Variable> variables;
    private String cliente;
    @SerializedName("imagenes")
    private RealmList<Adjuntos> imagenes;
    @SerializedName("adjuntos")
    private RealmList<Adjuntos> adjuntos;
    private Sitio sitio;
    private SSxOT ss;
    private boolean movimiento;
    private boolean asignada;
    @IgnoreField
    private Long entidadValida;
    @SerializedName("estadospersonal")
    private RealmList<RecorridoHistorico> recorridos;
    @SerializedName("ans")
    private RealmList<ANS> ans;
    @SerializedName("marcadaterminada")
    private Boolean terminada;
    private RealmList<RutaTrabajo> listachequeo;

    @Override
    public String toString() {
        return "OrdenTrabajo{" +
                "UUID='" + UUID + '\'' +
                ", id=" + id +
                ", cuenta=" + cuenta +
                ", codigo='" + codigo + '\'' +
                ", prioridad='" + prioridad + '\'' +
                ", fechainicio='" + fechainicio + '\'' +
                ", fechafin='" + fechafin + '\'' +
                ", estado='" + estado + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", realimentacion='" + realimentacion + '\'' +
                ", color='" + color + '\'' +
                ", porcentaje='" + porcentaje + '\'' +
                ", duracion='" + duracion + '\'' +
                ", orden=" + orden +
                ", fechainicioreal='" + fechainicioreal + '\'' +
                ", fechafinreal='" + fechafinreal + '\'' +
                ", entidades=" + entidades +
                ", recursos=" + recursos +
                ", fallas=" + fallas +
                ", repuestos=" + repuestos +
                ", consumibles=" + consumibles +
                ", ejecutores=" + ejecutores +
                ", variables=" + variables +
                ", cliente='" + cliente + '\'' +
                ", imagenes=" + imagenes +
                ", adjuntos=" + adjuntos +
                ", sitio=" + sitio +
                ", ss=" + ss +
                ", movimiento=" + movimiento +
                ", asignada=" + asignada +
                ", entidadValida=" + entidadValida +
                ", recorridos=" + recorridos +
                ", ans=" + ans +
                ", terminada=" + terminada +
                ", listachequeo=" + listachequeo +
                '}';
    }

    public OrdenTrabajo() {
        this.entidades = new RealmList<>();
        this.recursos = new RealmList<>();
        this.fallas = new RealmList<>();
        this.repuestos = new RealmList<>();
        this.consumibles = new RealmList<>();
        this.ejecutores = new RealmList<>();
        this.variables = new RealmList<>();
        this.adjuntos = new RealmList<>();
        this.recorridos = new RealmList<>();
        this.imagenes = new RealmList<>();
        this.movimiento = false;
        this.asignada = false;
        this.ans = new RealmList<>();
        this.terminada = null;
        this.listachequeo = new RealmList<>();
    }

    public String getFechafinreal() {
        return fechafinreal;
    }

    public void setFechafinreal(String fechafinreal) {
        this.fechafinreal = fechafinreal;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

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

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public RealmList<Entidad> getEntidades() {
        return entidades;
    }

    public void setEntidades(RealmList<Entidad> entidades) {
        this.entidades = entidades;
    }

    public RealmList<Recurso> getRecursos() {
        return recursos;
    }

    public void setRecursos(RealmList<Recurso> recursos) {
        this.recursos = recursos;
    }

    public RealmList<Falla> getFallas() {
        return fallas;
    }

    public void setFallas(RealmList<Falla> fallas) {
        this.fallas = fallas;
    }

    public RealmList<RepuestoManual> getRepuestos() {
        return repuestos;
    }

    public void setRepuestos(RealmList<RepuestoManual> repuestos) {
        this.repuestos = repuestos;
    }

    public RealmList<Consumible> getConsumibles() {
        return consumibles;
    }

    public void setConsumibles(RealmList<Consumible> consumibles) {
        this.consumibles = consumibles;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public String getRealimentacion() {
        return realimentacion;
    }

    public void setRealimentacion(String realimentacion) {
        this.realimentacion = realimentacion;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(String porcentaje) {
        this.porcentaje = porcentaje;
    }

    public RealmList<Ejecutores> getEjecutores() {
        return ejecutores;
    }

    public void setEjecutores(RealmList<Ejecutores> ejecutores) {
        this.ejecutores = ejecutores;
    }

    public RealmList<Adjuntos> getAdjuntos() {
        return adjuntos;
    }

    public void setAdjuntos(RealmList<Adjuntos> adjuntos) {
        this.adjuntos = adjuntos;
    }

    public RealmList<Variable> getVariables() {
        return variables;
    }

    public void setVariables(RealmList<Variable> variables) {
        this.variables = variables;
    }

    public Sitio getSitio() {
        return sitio;
    }

    public void setSitio(Sitio sitio) {
        this.sitio = sitio;
    }

    public SSxOT getSs() {
        return ss;
    }

    public void setSs(SSxOT ss) {
        this.ss = ss;
    }

    public RealmList<Adjuntos> getImagenes() {
        return imagenes;
    }

    public void setImagenes(RealmList<Adjuntos> imagenes) {
        this.imagenes = imagenes;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public boolean esCerrada() {
        return "Cerrada".equals(estado);
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public RealmList<RecorridoHistorico> getRecorridos() {
        return recorridos;
    }

    public void setRecorridos(RealmList<RecorridoHistorico> recorridos) {
        this.recorridos = recorridos;
    }

    public String getFechainicioreal() {
        return fechainicioreal;
    }

    public void setFechainicioreal(String fechainicioreal) {
        this.fechainicioreal = fechainicioreal;
    }

    public RealmList<ANS> getAns() {
        return ans;
    }

    public void setAns(RealmList<ANS> ans) {
        this.ans = ans;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getCodigo();
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return String.format("%s / %s", getFechainicio(), getFechafin());
    }

    @Nullable
    @Override
    public String getSummary() {
        return null;
    }

    @Nullable
    @Override
    public String getIcon() {
        return null;
    }

    @Nullable
    @Override
    public Integer getDrawable() {
        return null;
    }

    @Override
    public boolean compareTo(OrdenTrabajo value) {
        return getId().equals(value.id);
    }

    public boolean isMovimiento() {
        return movimiento;
    }

    public void setMovimiento(boolean movimiento) {
        this.movimiento = movimiento;
    }

    public boolean isAsignada() {
        return asignada;
    }

    public void setAsignada(boolean asignada) {
        this.asignada = asignada;
    }

    public boolean isCienPorciento() {
        return "100.0".equals(getPorcentaje()) || "100".equals(getPorcentaje());
    }

    public long[] getIdActividades() {
        List<Long> actividades = new ArrayList<>();
        for (Entidad entidad : getEntidades()) {
            for (Actividad activadad : entidad.getActividades()) {
                actividades.add(activadad.getId());
            }
        }

        int total = actividades.size();
        long[] response = new long[total];
        for (int i = 0; i < total; i++) {
            response[i] = actividades.get(i);
        }

        return response;
    }

    @Nullable
    public Boolean getTerminada() {
        return terminada;
    }

    public void setTerminada(Boolean terminada) {
        this.terminada = terminada;
    }

    public RealmList<RutaTrabajo> getListachequeo() {
        return listachequeo;
    }

    public void setListachequeo(RealmList<RutaTrabajo> listachequeo) {
        this.listachequeo = listachequeo;
    }

    public Long getEntidadValida() {
        return entidadValida;
    }

    public void setEntidadValida(Long entidadValida) {
        this.entidadValida = entidadValida;
    }

    public static class Response {

        private Long id;

        private Long idot;

        private List<Adjuntos> imagenes;

        private List<Adjuntos> adjuntos;

        private String realimentacion;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getIdot() {
            return idot;
        }

        public void setIdot(Long idot) {
            this.idot = idot;
        }

        public List<Adjuntos> getImagenes() {
            return imagenes;
        }

        public void setImagenes(List<Adjuntos> imagenes) {
            this.imagenes = imagenes;
        }

        public List<Adjuntos> getAdjuntos() {
            return adjuntos;
        }

        public void setAdjuntos(List<Adjuntos> adjuntos) {
            this.adjuntos = adjuntos;
        }

        public String getRealimentacion() {
            return realimentacion;
        }

        public void setRealimentacion(String realimentacion) {
            this.realimentacion = realimentacion;
        }

        @Override
        public String toString() {
            return "Response{" +
                    "id=" + id +
                    ", idot=" + idot +
                    ", imagenes=" + imagenes +
                    ", adjuntos=" + adjuntos +
                    ", realimentacion='" + realimentacion + '\'' +
                    '}';
        }
    }

    public static class Request implements Serializable {

        private Integer version;

        @SerializedName("tabOT")
        private Tab tab;
        @SerializedName("ejecucionesRT")
        private List<LCxOT> lcxot;

        private List<OrdenTrabajo> listaOT;

        public Request() {
            this.listaOT = new ArrayList<>();
        }

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }

        public List<OrdenTrabajo> getPendientes() {
            return listaOT;
        }

        public void setPendientes(List<OrdenTrabajo> pendientes) {
            this.listaOT = pendientes;
        }

        public Tab getTab() {
            return tab;
        }

        public void setTab(Tab tab) {
            this.tab = tab;
        }

        public List<LCxOT> getLcxot() {
            return lcxot;
        }

        public void setLcxot(List<LCxOT> lcxot) {
            this.lcxot = lcxot;
        }
    }

    public static class Tab implements Serializable {

        private final String title;

        private final Integer value;

        private final String ordenamiento;

        private final List<Long> ids;

        public Tab(String title, Integer value, String ordenamiento, List<Long> ids) {
            this.title = title;
            this.value = value;
            this.ordenamiento = ordenamiento;
            this.ids = ids;
        }

        public String getTitle() {
            return title;
        }

        public Integer getValue() {
            return value;
        }

        public String getOrdenamiento() {
            return ordenamiento;
        }

        public List<Long> getIds() {
            return ids;
        }
    }

    public static class OrdenTrabajoByFalla {

        private Response body;

        public Response getBody() {
            return body;
        }

        public void setBody(Response body) {
            this.body = body;
        }
    }
}