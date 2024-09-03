package com.mantum.cmms.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.mantum.demo.R;
import com.mantum.cmms.activity.BusquedaActivity;
import com.mantum.cmms.adapter.handler.BusquedaHandler;
import com.mantum.cmms.entity.parameter.Barcode;
import com.mantum.component.adapter.handler.ViewAdapter;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Busqueda extends RealmObject implements ViewAdapter<Busqueda>, BusquedaHandler<Busqueda> {

    @PrimaryKey
    private String UUID;

    private Long id;

    // variable para almacenar correctamente las R.T.
    private Long idejecucion;

    private Cuenta cuenta;

    private String code;

    private String name;

    private String type;

    private String entidadesrelacionadas;

    private String informacionvisualextra;

    private String referencia;

    private String detalle;

    private String nfc;

    private String qrcode;

    private RealmList<Barcode> barcode;

    private RealmList<Actividad> actividades;

    @Deprecated
    private RealmList<Accion> actions;

    @Deprecated
    private RealmList<Variable> variables;

    @Deprecated
    private RealmList<DetalleBusqueda> data;

    @Deprecated
    private boolean selected;

    @Deprecated
    private boolean mostrar;

    @Deprecated
    private String gmap;

    @Deprecated
    @SerializedName("historico_ot")
    private RealmList<OrdenTrabajo> historicoOT;

    private RealmList<Falla> fallas;

    public Busqueda() {
        this.actividades = new RealmList<>();
        this.data = new RealmList<>();
        this.variables = new RealmList<>();
        this.historicoOT = new RealmList<>();
        this.fallas = new RealmList<>();
        this.selected = false;
        this.mostrar = true;
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

    public RealmList<Barcode> getBarcode() {
        return barcode;
    }

    public void setBarcode(RealmList<Barcode> barcode) {
        this.barcode = barcode;
    }

    public String getNfc() {
        return nfc;
    }

    public void setNfc(String nfc) {
        this.nfc = nfc;
    }

    public boolean isEmpty() {
        return this.id == null;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public void generateUUID() {
        this.UUID = java.util.UUID.randomUUID().toString();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdejecucion() {
        return idejecucion;
    }

    public void setIdejecucion(Long idejecucion) {
        this.idejecucion = idejecucion;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEntidadesrelacionadas() {
        return entidadesrelacionadas;
    }

    public void setEntidadesrelacionadas(String entidadesrelacionadas) {
        this.entidadesrelacionadas = entidadesrelacionadas;
    }

    public String getInformacionvisualextra() {
        return informacionvisualextra;
    }

    public void setInformacionvisualextra(String informacionvisualextra) {
        this.informacionvisualextra = informacionvisualextra;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameView() {
        return (this.code != null ? code + " | " : "") + this.name;
    }

    public RealmList<Variable> getVariables() {
        return variables;
    }

    public void setVariables(RealmList<Variable> variables) {
        this.variables = variables;
    }

    public RealmList<DetalleBusqueda> getData() {
        return data;
    }

    public void setData(RealmList<DetalleBusqueda> data) {
        this.data = data;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public RealmList<Accion> getActions() {
        return actions;
    }

    public void setActions(RealmList<Accion> actions) {
        this.actions = actions;
    }

    public String getDetalle() {
        return detalle;
    }

    public <T> T getDetalle(Class<T> clazz) {
        return new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create()
                .fromJson(detalle, clazz);
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public String getGmap() {
        return gmap;
    }

    public void setGmap(String gmap) {
        this.gmap = gmap;
    }

    public RealmList<Actividad> getActividades() {
        return actividades;
    }

    public void setActividades(RealmList<Actividad> actividades) {
        this.actividades = actividades;
    }

    public boolean isMostrar() {
        return mostrar;
    }

    public void setMostrar(boolean mostrar) {
        this.mostrar = mostrar;
    }

    public RealmList<OrdenTrabajo> getHistoricoOT() {
        return historicoOT;
    }

    public void setHistoricoOT(RealmList<OrdenTrabajo> historicoOT) {
        this.historicoOT = historicoOT;
    }

    public RealmList<Falla> getFallas() {
        return fallas;
    }

    public void setFallas(RealmList<Falla> fallas) {
        this.fallas = fallas;
    }

    public String getReference() {
        return referencia;
    }

    public void setReference(String reference) {
        this.referencia = reference;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getCode() != null ? getCode().trim() : "";
    }

    @Nullable
    @Override
    public String getSubtitle() {
        String subtitle = "";
        if (getName() != null && !getName().isEmpty())
            subtitle = getName();

        switch (getType()) {
            case OrdenTrabajo.SELF:
                if (getEntidadesrelacionadas() != null && !getEntidadesrelacionadas().isEmpty()) {
                    subtitle = getEntidadesrelacionadas();
                }
                break;

            case SolicitudServicio.SELF:
                if (getEntidadesrelacionadas() != null && !getEntidadesrelacionadas().isEmpty()) {
                    subtitle = getEntidadesrelacionadas();
                } else {
                    subtitle = "Sin entidad";
                }
                break;

            case BusquedaActivity.GRUPO_VARIABLE:
                subtitle = "Sin entidad";
                for (DetalleBusqueda detalleBusqueda : getData()) {
                    if (detalleBusqueda.getTitle().equals("Entidad Asociada")) {
                        subtitle = detalleBusqueda.getValue();
                        break;
                    }
                }
                break;
        }

        return subtitle;
    }

    @Nullable
    @Override
    public String getSummary() {
        switch (getType()) {
            case Equipo.SELF:
            case InstalacionLocativa.SELF:
            case OrdenTrabajo.SELF:
            case RutaTrabajo.SELF:
            case SolicitudServicio.SELF:
                return getInformacionvisualextra();

            case InstalacionProceso.SELF:
                for (DetalleBusqueda detalleBusqueda : getData()) {
                    if (detalleBusqueda.getTitle().equals("Instalación Padre")) {
                        return detalleBusqueda.getValue();
                    }
                }
                return "";

            case BusquedaActivity.GRUPO_VARIABLE:
                for (DetalleBusqueda detalleBusqueda : getData()) {
                    if (detalleBusqueda.getTitle().equals("Frecuencia")) {
                        return detalleBusqueda.getValue();
                    }
                }
                return "";

            case BusquedaActivity.PERSONAL:
                for (DetalleBusqueda detalleBusqueda : getData()) {
                    if (detalleBusqueda.getTitle().equals("Cargo")) {
                        return detalleBusqueda.getValue();
                    }
                }
                return "";

            case BusquedaActivity.PIEZA:
                for (DetalleBusqueda detalleBusqueda : getData()) {
                    if (detalleBusqueda.getTitle().equals("Componente")) {
                        return detalleBusqueda.getValue();
                    }
                }
                return "";

            case BusquedaActivity.COMPONENTE:
                for (DetalleBusqueda detalleBusqueda : getData()) {
                    if (detalleBusqueda.getTitle().equals("Equipo")) {
                        return detalleBusqueda.getValue();
                    }
                }
                return "";

            case BusquedaActivity.RECURSO:
            case Familia.SELF:
                for (DetalleBusqueda detalleBusqueda : getData()) {
                    if (detalleBusqueda.getTitle().equals("Tipo")) {
                        return detalleBusqueda.getValue();
                    }
                }
                return "";

            case Proveedor.SELF:
                for (DetalleBusqueda detalleBusqueda : getData()) {
                    if (detalleBusqueda.getTitle().equals("Nit")) {
                        return detalleBusqueda.getValue();
                    }
                }
                return "";

            default:
                return "";
        }
    }

    @Override
    public String getCompleteType() {
        switch (getType()) {
            case Equipo.SELF:
                return "Equipo";
            case InstalacionLocativa.SELF:
                return "Instalación locativa";
            case InstalacionProceso.SELF:
                return "Instalación proceso";
            case OrdenTrabajo.SELF:
                return "Orden de trabajo";
            case RutaTrabajo.SELF:
                return "Ruta de trabajo";
            case SolicitudServicio.SELF:
                return "Solicitud de servicio";
            case BusquedaActivity.GRUPO_VARIABLE:
                return "Grupo variable";
            case BusquedaActivity.PERSONAL:
                return "Personal";
            case BusquedaActivity.PIEZA:
                return "Pieza";
            case BusquedaActivity.COMPONENTE:
                return "Componente";
            case BusquedaActivity.RECURSO:
                return "Recurso";
            case Familia.SELF:
                return "Familia";
            case Proveedor.SELF:
                return "Fabricante";
            default:
                return "Sin tipo";
        }
    }

    @Nullable
    @Override
    public String getIcon() {
        return null;
    }

    @Nullable
    @Override
    public Integer getDrawable() {
        switch (getType()) {
            case Equipo.SELF:
                return R.drawable.equipo;
            case InstalacionLocativa.SELF:
                return R.drawable.locativa;
            case InstalacionProceso.SELF:
                return R.drawable.proceso;
            case OrdenTrabajo.SELF:
                return R.drawable.orden_trabajo;
            case RutaTrabajo.SELF:
                return R.drawable.ruta_trabajo_search;
            case SolicitudServicio.SELF:
                return R.drawable.solicitud_servicio_search;
            case BusquedaActivity.GRUPO_VARIABLE:
                return R.drawable.variable;
            case BusquedaActivity.PERSONAL:
                return R.drawable.persona;
            case BusquedaActivity.PIEZA:
                return R.drawable.pieza;
            case BusquedaActivity.COMPONENTE:
                return R.drawable.componente;
            case BusquedaActivity.RECURSO:
                return R.drawable.recursos;
            case Familia.SELF:
                return R.drawable.ic_truck;
            case Proveedor.SELF:
                return R.drawable.ic_supplier;
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return "Busqueda{" +
                "UUID='" + UUID + '\'' +
                ", id=" + id +
                ", idejecucion=" + idejecucion +
                ", cuenta=" + cuenta +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", entidadesrelacionadas='" + entidadesrelacionadas + '\'' +
                ", informacionvisualextra='" + informacionvisualextra + '\'' +
                ", referencia='" + referencia + '\'' +
                ", detalle='" + detalle + '\'' +
                ", nfc='" + nfc + '\'' +
                ", qrcode='" + qrcode + '\'' +
                ", barcode=" + barcode +
                ", actividades=" + actividades +
                ", actions=" + actions +
                ", variables=" + variables +
                ", data=" + data +
                ", selected=" + selected +
                ", mostrar=" + mostrar +
                ", gmap='" + gmap + '\'' +
                ", historicoOT=" + historicoOT +
                ", fallas=" + fallas +
                '}';
    }

    @Override
    public boolean compareTo(Busqueda value) {
        if (getIdejecucion() != null) {
            return getId().equals(value.getId()) && getIdejecucion().equals(value.getIdejecucion()) && getType().equals(value.getType());
        }
        return getId().equals(value.getId()) && getType().equals(value.getType());
    }

    public static class Request {

        private List<Busqueda> entities;

        public void setEntities(List<Busqueda> entities) {
            this.entities = entities;
        }

        public List<Busqueda> getEntities() {
            return this.entities;
        }
    }

    public static class Read {

        private final String entitytype;

        private final String entityid;

        private final String entitycode;

        private final String entityname;

        private final Integer version;

        public Read(String entitytype, String entityid, String entitycode, String entityname) {
            this.entitytype = entitytype;
            this.entityid = entityid;
            this.entitycode = entitycode;
            this.entityname = entityname;
            this.version = 1;
        }

        public String getEntityType() {
            return entitytype;
        }

        public String getEntityId() {
            return entityid;
        }

        public String getEntityCode() {
            return entitycode;
        }

        public String getEntityName() {
            return entityname;
        }

        @NonNull
        public Integer getVersion() {
            //noinspection ConstantConditions
            if (version == null) {
                return 1;
            }
            return version;
        }
    }
}