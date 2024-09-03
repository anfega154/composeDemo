package com.mantum.cmms.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.mantum.R;
import com.mantum.cmms.Multipart;
import com.mantum.cmms.factory.SparseArrayTypeAdapterFactory;
import com.mantum.component.adapter.handler.ViewAdapter;
import com.mantum.component.service.Photo;
import com.mantum.component.service.PhotoAdapter;
import com.mantum.component.util.Tool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class Contenedor extends RealmObject implements ViewAdapter<Contenedor> {

    @PrimaryKey
    private String key;
    private Long id;
    private Cuenta cuenta;
    private String codigo;
    private String nombre;
    private String ubicacion;
    private String lineanaviera;
    private String equipmentgrade;
    private Boolean pti;
    private Boolean eir;
    private String estado;
    private String personalprogramacioneir;
    private String personalprogramacionpti;
    private String fechaultimaprogramacioneir;
    private String fechaultimaprogramacionpti;
    private String fechaultimaejecucionpti;
    private String fechaultimaejecucioneir;
    private String personalultimaejecucioneir;
    private String personalultimaejecucionpti;
    private Long idtipo;
    private String tipo;
    private RealmList<EquipmentGrade> equipmentgradevalidos;
    private String detalleinspeccion;
    private Long idmodelo;
    private String modelo;
    private Long idmarca;
    private String marca;
    private String serial;
    private String software;
    private String fechafabricacion;
    private Long idclasificacion;

    public Long getIdclasificacion() {
        return idclasificacion;
    }

    public void setIdclasificacion(Long idclasificacion) {
        this.idclasificacion = idclasificacion;
    }

    public String getFechafabricacion() {
        return fechafabricacion;
    }

    public void setFechafabricacion(String fechafabricacion) {
        this.fechafabricacion = fechafabricacion;
    }

    public Long getIdmodelo() {
        return idmodelo;
    }

    public void setIdmodelo(Long idmodelo) {
        this.idmodelo = idmodelo;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Long getIdmarca() {
        return idmarca;
    }

    public void setIdmarca(Long idmarca) {
        this.idmarca = idmarca;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getSoftware() {
        return software;
    }

    public void setSoftware(String software) {
        this.software = software;
    }

    public String getLineanaviera() {
        return lineanaviera;
    }

    public void setLineanaviera(String lineanaviera) {
        this.lineanaviera = lineanaviera;
    }

    public String getEquipmentgrade() {
        return equipmentgrade;
    }

    public void setEquipmentgrade(String equipmentgrade) {
        this.equipmentgrade = equipmentgrade;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
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

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Boolean getPti() {
        return pti;
    }

    public void setPti(Boolean pti) {
        this.pti = pti;
    }

    public Boolean getEir() {
        return eir;
    }

    public void setEir(Boolean eir) {
        this.eir = eir;
    }

    public String getPersonalprogramacioneir() {
        return personalprogramacioneir;
    }

    public void setPersonalprogramacioneir(String personalprogramacioneir) {
        this.personalprogramacioneir = personalprogramacioneir;
    }

    public String getPersonalprogramacionpti() {
        return personalprogramacionpti;
    }

    public void setPersonalprogramacionpti(String personalprogramacionpti) {
        this.personalprogramacionpti = personalprogramacionpti;
    }

    public String getFechaultimaprogramacioneir() {
        return fechaultimaprogramacioneir;
    }

    public void setFechaultimaprogramacioneir(String fechaultimaprogramacioneir) {
        this.fechaultimaprogramacioneir = fechaultimaprogramacioneir;
    }

    public String getFechaultimaprogramacionpti() {
        return fechaultimaprogramacionpti;
    }

    public void setFechaultimaprogramacionpti(String fechaultimaprogramacionpti) {
        this.fechaultimaprogramacionpti = fechaultimaprogramacionpti;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPersonalultimaejecucioneir() {
        return personalultimaejecucioneir;
    }

    public void setPersonalultimaejecucioneir(String personalultimaejecucioneir) {
        this.personalultimaejecucioneir = personalultimaejecucioneir;
    }

    public String getPersonalultimaejecucionpti() {
        return personalultimaejecucionpti;
    }

    public void setPersonalultimaejecucionpti(String personalultimaejecucionpti) {
        this.personalultimaejecucionpti = personalultimaejecucionpti;
    }

    public String getFechaultimaejecucionpti() {
        return fechaultimaejecucionpti;
    }

    public void setFechaultimaejecucionpti(String fechaultimaejecucionpti) {
        this.fechaultimaejecucionpti = fechaultimaejecucionpti;
    }

    public String getFechaultimaejecucioneir() {
        return fechaultimaejecucioneir;
    }

    public void setFechaultimaejecucioneir(String fechaultimaejecucioneir) {
        this.fechaultimaejecucioneir = fechaultimaejecucioneir;
    }

    public Long getIdtipo() {
        return idtipo;
    }

    public void setIdtipo(Long idtipo) {
        this.idtipo = idtipo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public RealmList<EquipmentGrade> getEquipmentgradevalidos() {
        return equipmentgradevalidos;
    }

    public void setEquipmentgradevalidos(RealmList<EquipmentGrade> equipmentgradevalidos) {
        this.equipmentgradevalidos = equipmentgradevalidos;
    }

    public String getDetalleinspeccion() {
        return detalleinspeccion;
    }

    public void setDetalleinspeccion(String detalleinspeccion) {
        this.detalleinspeccion = detalleinspeccion;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getCodigo();
    }

    @Nullable
    @Override
    public String getSubtitle() {
        String personal = pti ? getPersonalprogramacionpti() : getPersonalprogramacioneir();
        String programacion = pti ? getFechaultimaprogramacionpti() : getFechaultimaprogramacioneir();
        return "Programación: " + personal + " - " + programacion;
    }

    @Nullable
    @Override
    public String getSummary() {
        return String.format("Ubicacion: %s", getUbicacion() != null ? getUbicacion() : "");
    }

    @Nullable
    @Override
    public String getIcon() {
        return pti ? "PTI" : "EIR";
    }

    @Nullable
    @Override
    public Integer getDrawable() {
        if ("Por evaluar".equals(getEstado())) {
            return R.drawable.shape_red;
        }

        if ("Parcialmente Ejecutado".equals(getEstado())) {
            return R.drawable.circle_yellow;
        }

        if ("Ejecutado".equals(getEstado())) {
            return R.drawable.shape_green;
        }

        return null;
    }

    @Override
    public boolean compareTo(@NonNull Contenedor value) {
        return value.getKey().equals(getKey());
    }

    @NonNull
    @Override
    public String toString() {
        return "Contenedor{" +
                "key='" + key + '\'' +
                ", id=" + id +
                ", cuenta=" + cuenta +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", ubicacion='" + ubicacion + '\'' +
                ", lineanaviera='" + lineanaviera + '\'' +
                ", equipmentgrade='" + equipmentgrade + '\'' +
                ", pti=" + pti +
                ", eir=" + eir +
                ", estado='" + estado + '\'' +
                ", personalprogramacioneir='" + personalprogramacioneir + '\'' +
                ", personalprogramacionpti='" + personalprogramacionpti + '\'' +
                ", fechaultimaprogramacioneir='" + fechaultimaprogramacioneir + '\'' +
                ", fechaultimaprogramacionpti='" + fechaultimaprogramacionpti + '\'' +
                ", fechaultimaejecucionpti='" + fechaultimaejecucionpti + '\'' +
                ", fechaultimaejecucioneir='" + fechaultimaejecucioneir + '\'' +
                ", personalultimaejecucioneir='" + personalultimaejecucioneir + '\'' +
                ", personalultimaejecucionpti='" + personalultimaejecucionpti + '\'' +
                ", idtipo=" + idtipo +
                ", tipo='" + tipo + '\'' +
                ", equipmentgradevalidos=" + equipmentgradevalidos +
                ", detalleinspeccion='" + detalleinspeccion + '\'' +
                ", idmodelo=" + idmodelo +
                ", modelo='" + modelo + '\'' +
                ", idmarca=" + idmarca +
                ", marca='" + marca + '\'' +
                ", serial='" + serial + '\'' +
                ", software='" + software + '\'' +
                '}';
    }

    public static class Response {
        @SerializedName("sListaEq")
        private List<Contenedor> body;
        @SerializedName("iNextPage")
        private Integer next;
        @SerializedName("iPercent")
        private Integer percent;

        public Response() {
            body = new ArrayList<>();
        }

        public List<Contenedor> getBody() {
            return body;
        }

        public void setBody(List<Contenedor> body) {
            this.body = body;
        }

        public Integer getNext() {
            return next;
        }

        public void setNext(Integer next) {
            this.next = next;
        }

        public Integer getPercent() {
            return percent;
        }

        public void setPercent(Integer percent) {
            this.percent = percent;
        }

        @Override
        public String toString() {
            return "Response{" +
                    "body=" + body +
                    ", next=" + next +
                    ", percent=" + percent +
                    '}';
        }
    }

    public static class Request implements Multipart {
        private String key;
        private String token;
        private String fechainspeccion;
        private String horaInspeccion;
        private Long idinspeccion;
        private String proceso;
        private String serial;
        private String fechafabricacion;
        private String software;
        private Long idmodelo;
        private String modelo;
        private Long idmarca;
        private String marca;
        private Long idproposito;
        private String proposito;
        private String temperatura;
        private String grados;
        private String gradosText;
        private Long idgroupcodecontenedor;
        private String groupcodecontenedor;
        private String observaciones;
        private Long idyardainspeccion;
        private String yardainspeccion;
        private String nivelrefrigerante;
        private String nivelrefrigeranteText;
        private Long idequipmentgrade;
        private String equipmentgrade;
        private Integer semana;
        private String lineanaviera;
        private List<Seccion.Pregunta> preguntas;
        private List<Contenedor.Falla> fallas;
        private List<Contenedor.Damage> damages;
        private boolean abrirOT;
        private String estado;
        private String tecnico;
        private String fechaultimopti;
        private String tipo;
        private Long idestadoregistro;
        private String estadoregistro;
        private String fecharegistro;
        private String novedad;
        private boolean requiereValidacion;
        private boolean requiereFalla;
        private String codigo;
        private String conductor;
        private String cedula;
        private String placa;
        private boolean draincleanandfree;
        private Long idclasificacion;

        public Request() {
            this.token = UUID.randomUUID().toString();
            this.idmarca = null;
        }

        public Long getIdclasificacion() {
            return idclasificacion;
        }

        public void setIdclasificacion(Long idclasificacion) {
            this.idclasificacion = idclasificacion;
        }

        public String getConductor() {
            return conductor;
        }

        public void setConductor(String conductor) {
            this.conductor = conductor;
        }

        public String getCedula() {
            return cedula;
        }

        public void setCedula(String cedula) {
            this.cedula = cedula;
        }

        public String getPlaca() {
            return placa;
        }

        public void setPlaca(String placa) {
            this.placa = placa;
        }

        public String getCodigo() {
            return codigo;
        }

        public void setCodigo(String codigo) {
            this.codigo = codigo;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getFecharegistro() {
            return fecharegistro;
        }

        public void setFecharegistro(String fecharegistro) {
            this.fecharegistro = fecharegistro;
        }

        public Long getIdestadoregistro() {
            return idestadoregistro;
        }

        public void setIdestadoregistro(Long idestadoregistro) {
            this.idestadoregistro = idestadoregistro;
        }

        public String getEstadoregistro() {
            return estadoregistro;
        }

        public void setEstadoregistro(String estadoregistro) {
            this.estadoregistro = estadoregistro;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getHoraInspeccion() {
            return horaInspeccion;
        }

        public void setHoraInspeccion(String horaInspeccion) {
            this.horaInspeccion = horaInspeccion;
        }

        public String getFechainspeccion() {
            return fechainspeccion;
        }

        public void setFechainspeccion(String fechainspeccion) {
            this.fechainspeccion = fechainspeccion;
        }

        public Long getIdproposito() {
            return idproposito;
        }

        public void setIdproposito(Long idproposito) {
            this.idproposito = idproposito;
        }

        public String getProposito() {
            return proposito;
        }

        public void setProposito(String proposito) {
            this.proposito = proposito;
        }

        public Long getIdinspeccion() {
            return idinspeccion;
        }

        public void setIdinspeccion(Long idinspeccion) {
            this.idinspeccion = idinspeccion;
        }

        public String getProceso() {
            return proceso;
        }

        public void setProceso(String proceso) {
            this.proceso = proceso;
        }

        public String getSerial() {
            return serial;
        }

        public void setSerial(String serial) {
            this.serial = serial;
        }

        public String getFechafabricacion() {
            return fechafabricacion;
        }

        public void setFechafabricacion(String fechafabricacion) {
            this.fechafabricacion = fechafabricacion;
        }

        public String getSoftware() {
            return software;
        }

        public void setSoftware(String software) {
            this.software = software;
        }

        public Long getIdmodelo() {
            return idmodelo;
        }

        public void setIdmodelo(Long idmodelo) {
            this.idmodelo = idmodelo;
        }

        public Long getIdmarca() {
            return idmarca;
        }

        public void setIdmarca(Long idmarca) {
            this.idmarca = idmarca;
        }

        public String getTemperatura() {
            return temperatura;
        }

        public void setTemperatura(String temperatura) {
            this.temperatura = temperatura;
        }

        public String getGrados() {
            return grados;
        }

        public void setGrados(String grados) {
            this.grados = grados;
        }

        public Long getIdgroupcodecontenedor() {
            return idgroupcodecontenedor;
        }

        public void setIdgroupcodecontenedor(Long idgroupcodecontenedor) {
            this.idgroupcodecontenedor = idgroupcodecontenedor;
        }

        public String getObservaciones() {
            return observaciones;
        }

        public void setObservaciones(String observaciones) {
            this.observaciones = observaciones;
        }

        public Long getIdyardainspeccion() {
            return idyardainspeccion;
        }

        public void setIdyardainspeccion(Long idyardainspeccion) {
            this.idyardainspeccion = idyardainspeccion;
        }

        public String getNivelrefrigerante() {
            return nivelrefrigerante;
        }

        public void setNivelrefrigerante(String nivelrefrigerante) {
            this.nivelrefrigerante = nivelrefrigerante;
        }

        public String getEquipmentgrade() {
            return equipmentgrade;
        }

        public void setEquipmentgrade(String equipmentgrade) {
            this.equipmentgrade = equipmentgrade;
        }

        public Integer getSemana() {
            return semana;
        }

        public void setSemana(Integer semana) {
            this.semana = semana;
        }

        public String getLineaNaviera() {
            return lineanaviera;
        }

        public void setLineaNaviera(String lineaNaviera) {
            this.lineanaviera = lineaNaviera;
        }

        public List<Seccion.Pregunta> getPreguntas() {
            return preguntas;
        }

        public void setPreguntas(List<Seccion.Pregunta> preguntas) {
            this.preguntas = preguntas;
        }

        public List<Falla> getFallas() {
            return fallas;
        }

        public void setFallas(List<Falla> fallas) {
            this.fallas = fallas;
        }

        public boolean isAbrirOT() {
            return abrirOT;
        }

        public void setAbrirOT(boolean abrirOT) {
            this.abrirOT = abrirOT;
        }

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }

        public String getTecnico() {
            return tecnico;
        }

        public void setTecnico(String tecnico) {
            this.tecnico = tecnico;
        }

        public String getFechaultimopti() {
            return fechaultimopti;
        }

        public void setFechaultimopti(String fechaultimopti) {
            this.fechaultimopti = fechaultimopti;
        }

        public String getGradosText() {
            return gradosText;
        }

        public void setGradosText(String gradosText) {
            this.gradosText = gradosText;
        }

        public String getNivelrefrigeranteText() {
            return nivelrefrigeranteText;
        }

        public void setNivelrefrigeranteText(String nivelrefrigeranteText) {
            this.nivelrefrigeranteText = nivelrefrigeranteText;
        }

        public String getModelo() {
            return modelo;
        }

        public void setModelo(String modelo) {
            this.modelo = modelo;
        }

        public String getMarca() {
            return marca;
        }

        public void setMarca(String marca) {
            this.marca = marca;
        }

        public String getGroupcodecontenedor() {
            return groupcodecontenedor;
        }

        public void setGroupcodecontenedor(String groupcodecontenedor) {
            this.groupcodecontenedor = groupcodecontenedor;
        }

        public String getYardainspeccion() {
            return yardainspeccion;
        }

        public void setYardainspeccion(String yardainspeccion) {
            this.yardainspeccion = yardainspeccion;
        }

        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public Long getIdequipmentgrade() {
            return idequipmentgrade;
        }

        public void setIdEquipmentGrade(Long idequipmentgrade) {
            this.idequipmentgrade = idequipmentgrade;
        }

        public List<Damage> getDamages() {
            return damages;
        }

        public void setDamages(List<Damage> damages) {
            this.damages = damages;
        }

        public boolean isRequiereValidacion() {
            return requiereValidacion;
        }

        public void setRequiereValidacion(boolean requiereValidacion) {
            this.requiereValidacion = requiereValidacion;
        }

        public boolean isRequiereFalla() {
            return requiereFalla;
        }

        public void setRequiereFalla(boolean requiereFalla) {
            this.requiereFalla = requiereFalla;
        }

        public String getNovedad() {
            return novedad;
        }

        public void setNovedad(String novedad) {
            this.novedad = novedad;
        }

        public boolean isDraincleanandfree() {
            return draincleanandfree;
        }

        public void setDraincleanandfree(boolean draincleanandfree) {
            this.draincleanandfree = draincleanandfree;
        }

        public String toJson() {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapterFactory(SparseArrayTypeAdapterFactory.INSTANCE)
                    .create();
            return gson.toJson(this);
        }

        @NonNull
        @Override
        public String toString() {
            return "Request{" +
                    "token='" + token + '\'' +
                    ", fechainspeccion='" + fechainspeccion + '\'' +
                    ", horaInspeccion='" + horaInspeccion + '\'' +
                    ", idinspeccion=" + idinspeccion +
                    ", proceso='" + proceso + '\'' +
                    ", serial='" + serial + '\'' +
                    ", fechafabricacion='" + fechafabricacion + '\'' +
                    ", software='" + software + '\'' +
                    ", idmodelo=" + idmodelo +
                    ", modelo='" + modelo + '\'' +
                    ", idmarca=" + idmarca +
                    ", marca='" + marca + '\'' +
                    ", idproposito=" + idproposito +
                    ", proposito='" + proposito + '\'' +
                    ", temperatura='" + temperatura + '\'' +
                    ", grados='" + grados + '\'' +
                    ", gradosText='" + gradosText + '\'' +
                    ", idgroupcodecontenedor=" + idgroupcodecontenedor +
                    ", groupcodecontenedor='" + groupcodecontenedor + '\'' +
                    ", observaciones='" + observaciones + '\'' +
                    ", idyardainspeccion=" + idyardainspeccion +
                    ", yardainspeccion='" + yardainspeccion + '\'' +
                    ", nivelrefrigerante='" + nivelrefrigerante + '\'' +
                    ", nivelrefrigeranteText='" + nivelrefrigeranteText + '\'' +
                    ", idequipmentgrade=" + idequipmentgrade +
                    ", equipmentgrade='" + equipmentgrade + '\'' +
                    ", semana=" + semana +
                    ", lineaNaviera='" + lineanaviera + '\'' +
                    ", preguntas=" + preguntas +
                    ", fallas=" + fallas +
                    ", damages=" + damages +
                    ", abrirOT=" + abrirOT +
                    ", novedad=" + novedad +
                    ", estado='" + estado + '\'' +
                    ", tecnico='" + tecnico + '\'' +
                    ", fechaultimopti='" + fechaultimopti + '\'' +
                    ", tipo='" + tipo + '\'' +
                    '}';
        }

        @Override
        public MultipartBody.Builder builder() {
            MultipartBody.Builder multipart = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("token", getToken())
                    .addFormDataPart("fechainspeccion", Tool.formData(getFechainspeccion() + " " + getHoraInspeccion()))
                    .addFormDataPart("idinspeccion", Tool.formData(getIdinspeccion()))
                    .addFormDataPart("proceso", Tool.formData(getProceso()))
                    .addFormDataPart("serial", Tool.formData(getSerial()))
                    .addFormDataPart("fechafabricacion", Tool.formData(getFechafabricacion()))
                    .addFormDataPart("software", Tool.formData(getSoftware()))
                    .addFormDataPart("idmodelo", getIdmodelo() != null && getIdmodelo() > 0 ? Tool.formData(getIdmodelo()) : "")
                    .addFormDataPart("idmarca", getIdmarca() != null && getIdmarca() > 0 ? Tool.formData(getIdmarca()) : "")
                    .addFormDataPart("temperatura", Tool.formData(getTemperatura()))
                    .addFormDataPart("grados", Tool.formData(getGrados()))
                    .addFormDataPart("idgroupcodecontenedor", Tool.formData(getIdgroupcodecontenedor()))
                    .addFormDataPart("observaciones", Tool.formData(getObservaciones()))
                    .addFormDataPart("nivelrefrigerante", Tool.formData(getNivelrefrigerante()))
                    .addFormDataPart("lineanaviera", Tool.formData(getLineaNaviera()))
                    .addFormDataPart("idequipmentgrade", Tool.formData(getEquipmentgrade()))
                    .addFormDataPart("equipmentgrade", Tool.formData(getIdequipmentgrade()))
                    .addFormDataPart("semana", Tool.formData(getSemana()))
                    .addFormDataPart("abrirot", Tool.formData(isAbrirOT()))
                    .addFormDataPart("estado", Tool.formData(getEstado()))
                    .addFormDataPart("idproposito", Tool.formData(getIdproposito()))
                    .addFormDataPart("proposito", Tool.formData(getProposito()))
                    .addFormDataPart("tecnico", Tool.formData(getTecnico()))
                    .addFormDataPart("tipo", Tool.formData(getTipo()))
                    .addFormDataPart("novedad", Tool.formData(getNovedad()))
                    .addFormDataPart("conductor", Tool.formData(getConductor()))
                    .addFormDataPart("cedula", Tool.formData(getCedula()))
                    .addFormDataPart("placa", Tool.formData(getPlaca()))
                    .addFormDataPart("draincleanandfree", Tool.formData(isDraincleanandfree()))
                    .addFormDataPart("idestadoregistro", Tool.formData(getIdestadoregistro()))
                    .addFormDataPart("estadoregistro", Tool.formData(getEstadoregistro()))
                    .addFormDataPart("fechaultimopti", Tool.formData(getFechaultimopti()))
                    .addFormDataPart("fecharegistro", Tool.formData(getFecharegistro()))
                    .addFormDataPart("idyardainspeccion", Tool.formData(getIdyardainspeccion()));

            int key = 0;
            if (getPreguntas() != null) {
                for (Seccion.Pregunta pregunta : getPreguntas()) {
                    for (Checklist.Respuesta respuesta : pregunta.getRespuestas()) {
                        multipart.addFormDataPart("checklist_" + key + "_idseccion", Tool.formData(pregunta.getId()));
                        multipart.addFormDataPart("checklist_" + key + "_iditem", Tool.formData(respuesta.getId()));
                        multipart.addFormDataPart("checklist_" + key + "_cumple", Tool.formData(respuesta.getChecked()));
                        key = key + 1;
                    }
                }
            }

            if (getFallas() != null) {
                key = 0;
                for (Falla falla : getFallas()) {
                    multipart.addFormDataPart("falla_" + key + "_id", Tool.formData(falla.getId()));
                    multipart.addFormDataPart("falla_" + key + "_idtipofalla", Tool.formData(falla.getIdtipo()));
                    multipart.addFormDataPart("falla_" + key + "_tipofalla", Tool.formData(falla.getTipo()));
                    multipart.addFormDataPart("falla_" + key + "_parte", Tool.formData(falla.getParte()));
                    multipart.addFormDataPart("falla_" + key + "_idreclasificacion", Tool.formData(falla.getIdreclasificacion()));
                    multipart.addFormDataPart("falla_" + key + "_reclasificacion", Tool.formData(falla.getReclasificacion()));
                    multipart.addFormDataPart("falla_" + key + "_idtiporeparacion", Tool.formData(falla.getIdtiporeparacion()));
                    multipart.addFormDataPart("falla_" + key + "_tiporeparacion", Tool.formData(falla.getTiporeparacion()));
                    multipart.addFormDataPart("falla_" + key + "_idsubtiporeparacion", Tool.formData(falla.getIdsubtiporeparacion()));
                    multipart.addFormDataPart("falla_" + key + "_subtiporeparacion", Tool.formData(falla.getSubtiporeparacion()));
                    multipart.addFormDataPart("falla_" + key + "_idgama", Tool.formData(falla.getIdActividad()));
                    multipart.addFormDataPart("falla_" + key + "_gama", Tool.formData(falla.getActividad()));
                    multipart.addFormDataPart("falla_" + key + "_serial", Tool.formData(falla.getSerial()));
                    multipart.addFormDataPart("falla_" + key + "_partenumero", Tool.formData(falla.getParte()));
                    multipart.addFormDataPart("falla_" + key + "_descripcion", Tool.formData(falla.getDescription()));
                    multipart.addFormDataPart("falla_" + key + "_fecharegistro", Tool.formData(falla.getFecha()));
                    multipart.addFormDataPart("falla_" + key + "_token", Tool.formData(falla.getToken()));

                    if (falla.getPhotos() != null) {
                        int total = falla.getPhotos().size();
                        for (int i = 0; i < total; i++) {
                            PhotoAdapter photoAdapter = falla.getPhotos().get(i);
                            if (photoAdapter.getPath() != null && !photoAdapter.isExternal()) {
                                File file = new File(photoAdapter.getPath());
                                if (file.exists()) {
                                    multipart.addFormDataPart(
                                            "falla_" + key + "_fotografias[]", file.getName(),
                                            RequestBody.create(MediaType.parse(Photo.mime(file.getName())), file)
                                    );
                                }
                            }
                        }
                    }
                    key = key + 1;
                }
            }

            if (getDamages() != null) {
                key = 0;
                for (Damage damage : getDamages()) {
                    multipart.addFormDataPart("falla_" + key + "_id", Tool.formData(damage.getId()));
                    multipart.addFormDataPart("falla_" + key + "_idtipofalla", Tool.formData(damage.getIdtipo()));
                    multipart.addFormDataPart("falla_" + key + "_tipofalla", Tool.formData(damage.getTipo()));
                    multipart.addFormDataPart("falla_" + key + "_idparte", Tool.formData(damage.getIdparte()));
                    multipart.addFormDataPart("falla_" + key + "_parte", Tool.formData(damage.getParte()));
                    multipart.addFormDataPart("falla_" + key + "_localizacion", Tool.formData(damage.getLocalizacion()));
                    multipart.addFormDataPart("falla_" + key + "_idreclasificacion", Tool.formData(damage.getIdreclasificacion()));
                    multipart.addFormDataPart("falla_" + key + "_reclasificacion", Tool.formData(damage.getReclasificacion()));
                    multipart.addFormDataPart("falla_" + key + "_idtiporeparacion", Tool.formData(damage.getIdtiporeparacion()));
                    multipart.addFormDataPart("falla_" + key + "_tiporeparacion", Tool.formData(damage.getTiporeparacion()));
                    multipart.addFormDataPart("falla_" + key + "_idsubtiporeparacion", Tool.formData(damage.getIdsubtiporeparacion()));
                    multipart.addFormDataPart("falla_" + key + "_subtiporeparacion", Tool.formData(damage.getSubtiporeparacion()));
                    multipart.addFormDataPart("falla_" + key + "_idgama", Tool.formData(damage.getIdactividad()));
                    multipart.addFormDataPart("falla_" + key + "_gama", Tool.formData(damage.getActividad()));
                    multipart.addFormDataPart("falla_" + key + "_descripcion", Tool.formData(damage.getDescripcion()));
                    multipart.addFormDataPart("falla_" + key + "_token", Tool.formData(damage.getToken()));
                    multipart.addFormDataPart("falla_" + key + "_fecharegistro", Tool.formData(damage.getFecha()));
                    multipart.addFormDataPart("falla_" + key + "_idelementcode", Tool.formData(damage.getIdelement()));
                    multipart.addFormDataPart("falla_" + key + "_elementcode", Tool.formData(damage.getElement()));
                    multipart.addFormDataPart("falla_" + key + "_iddamagecode", Tool.formData(damage.getIddamage()));
                    multipart.addFormDataPart("falla_" + key + "_damagecode", Tool.formData(damage.getDamage()));
                    multipart.addFormDataPart("falla_" + key + "_lenght", Tool.formData(damage.getLenght()));
                    multipart.addFormDataPart("falla_" + key + "_height", Tool.formData(damage.getHeight()));

                    if (damage.getImagen() != null && !damage.getImagen().isEmpty()) {
                        File file = new File(damage.getImagen());
                        if (file.exists()) {
                            multipart.addFormDataPart(
                                    "falla_" + key + "_bocetoseir", file.getName(),
                                    RequestBody.create(MediaType.parse(Photo.mime(file.getName())), file)
                            );
                        }
                    }

                    if (damage.getPhotos() != null) {
                        int total = damage.getPhotos().size();
                        for (int i = 0; i < total; i++) {
                            PhotoAdapter photoAdapter = damage.getPhotos().get(i);
                            if (photoAdapter.getPath() != null && !photoAdapter.isExternal()) {
                                File file = new File(photoAdapter.getPath());
                                if (file.exists()) {
                                    multipart.addFormDataPart(
                                            "falla_" + key + "_fotografias[]", file.getName(),
                                            RequestBody.create(MediaType.parse(Photo.mime(file.getName())), file)
                                    );
                                }
                            }
                        }
                    }

                    key = key + 1;
                }
            }

            return multipart;
        }
    }

    public static class Damage implements Parcelable, ViewAdapter<Damage> {
        private String id;
        private String fecha;
        private String tipo;
        private String idtipo;
        private String idparte;
        private String parte;
        private String localizacion;
        private String idreclasificacion;
        private String reclasificacion;
        private String idtiporeparacion;
        private String tiporeparacion;
        private String idsubtiporeparacion;
        private String subtiporeparacion;
        private String idactividad;
        private String actividad;
        private String descripcion;
        private SparseArray<PhotoAdapter> photos;
        private String imagen;
        private Boolean requiereFotos = false;
        private Boolean external = false;
        private String token;
        private String idelement;
        private String element;
        private String iddamage;
        private String damage;
        private String lenght;
        private String height;

        public Damage() {
        }

        protected Damage(@NonNull Parcel in) {
            id = in.readString();
            fecha = in.readString();
            tipo = in.readString();
            idtipo = in.readString();
            idparte = in.readString();
            parte = in.readString();
            localizacion = in.readString();
            idreclasificacion = in.readString();
            reclasificacion = in.readString();
            idtiporeparacion = in.readString();
            tiporeparacion = in.readString();
            idsubtiporeparacion = in.readString();
            subtiporeparacion = in.readString();
            idactividad = in.readString();
            actividad = in.readString();
            descripcion = in.readString();
            photos = in.readSparseArray(getClass().getClassLoader());
            imagen = in.readString();
            byte tmpRequiereFotos = in.readByte();
            requiereFotos = tmpRequiereFotos == 0 ? null : tmpRequiereFotos == 1;
            byte tmpexternal = in.readByte();
            external = tmpexternal == 0 ? null : tmpexternal == 1;
            token = in.readString();
            idelement = in.readString();
            element = in.readString();
            iddamage = in.readString();
            damage = in.readString();
            lenght = in.readString();
            height = in.readString();
        }

        public static final Creator<Damage> CREATOR = new Creator<Damage>() {
            @Override
            public Damage createFromParcel(Parcel in) {
                return new Damage(in);
            }

            @Override
            public Damage[] newArray(int size) {
                return new Damage[size];
            }
        };

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public String getIdtipo() {
            return idtipo;
        }

        public void setIdtipo(String idtipo) {
            this.idtipo = idtipo;
        }

        public String getFecha() {
            return fecha;
        }

        public void setFecha(String fecha) {
            this.fecha = fecha;
        }

        public String getIdparte() {
            return idparte;
        }

        public void setIdparte(String idparte) {
            this.idparte = idparte;
        }

        public String getParte() {
            return parte;
        }

        public void setParte(String parte) {
            this.parte = parte;
        }

        public String getLocalizacion() {
            return localizacion;
        }

        public void setLocalizacion(String localizacion) {
            this.localizacion = localizacion;
        }

        public String getIdreclasificacion() {
            return idreclasificacion;
        }

        public void setIdreclasificacion(String idreclasificacion) {
            this.idreclasificacion = idreclasificacion;
        }

        public String getReclasificacion() {
            return reclasificacion;
        }

        public void setReclasificacion(String reclasificacion) {
            this.reclasificacion = reclasificacion;
        }

        public String getIdtiporeparacion() {
            return idtiporeparacion;
        }

        public void setIdtiporeparacion(String idtiporeparacion) {
            this.idtiporeparacion = idtiporeparacion;
        }

        public String getTiporeparacion() {
            return tiporeparacion;
        }

        public void setTiporeparacion(String tiporeparacion) {
            this.tiporeparacion = tiporeparacion;
        }

        public String getIdsubtiporeparacion() {
            return idsubtiporeparacion;
        }

        public void setIdsubtiporeparacion(String idsubtiporeparacion) {
            this.idsubtiporeparacion = idsubtiporeparacion;
        }

        public String getSubtiporeparacion() {
            return subtiporeparacion;
        }

        public void setSubtiporeparacion(String subtiporeparacion) {
            this.subtiporeparacion = subtiporeparacion;
        }

        public String getIdactividad() {
            return idactividad;
        }

        public void setIdactividad(String idactividad) {
            this.idactividad = idactividad;
        }

        public String getActividad() {
            return actividad;
        }

        public String getImagen() {
            return imagen;
        }

        public void setImagen(String imagen) {
            this.imagen = imagen;
        }

        public void setActividad(String actividad) {
            this.actividad = actividad;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public SparseArray<PhotoAdapter> getPhotos() {
            return photos;
        }

        public void setPhotos(SparseArray<PhotoAdapter> photos) {
            this.photos = photos;
        }

        public Boolean getRequiereFotos() {
            return requiereFotos;
        }

        public void setRequiereFotos(Boolean requiereFotos) {
            this.requiereFotos = requiereFotos;
        }

        public Boolean getExternal() {
            return external;
        }

        public void setExternal(Boolean external) {
            this.external = external;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        @NonNull
        @Override
        public String getTitle() {
            return getParte();
        }

        @Nullable
        @Override
        public String getSubtitle() {
            return getLocalizacion();
        }

        @Nullable
        @Override
        public String getSummary() {
            return getDescripcion();
        }

        @Nullable
        @Override
        public String getIcon() {
            return null;
        }


        public String getIdelement() {
            return idelement;
        }

        public void setIdelement(String idelement) {
            this.idelement = idelement;
        }

        public String getElement() {
            return element;
        }

        public void setElement(String element) {
            this.element = element;
        }

        public String getIddamage() {
            return iddamage;
        }

        public void setIddamage(String iddamage) {
            this.iddamage = iddamage;
        }

        public String getDamage() {
            return damage;
        }

        public void setDamage(String damage) {
            this.damage = damage;
        }

        public String getLenght() {
            return lenght;
        }

        public void setLenght(String lenght) {
            this.lenght = lenght;
        }

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        @Nullable
        @Override
        public Integer getDrawable() {
            return null;
        }

        @Override
        public boolean compareTo(@NonNull Damage value) {
            if (getId() == null) {
                return getToken().equals(value.getToken());
            } else {
                return getId().equals(value.getId());
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(fecha);
            dest.writeString(tipo);
            dest.writeString(idtipo);
            dest.writeString(idparte);
            dest.writeString(parte);
            dest.writeString(localizacion);
            dest.writeString(idreclasificacion);
            dest.writeString(reclasificacion);
            dest.writeString(idtiporeparacion);
            dest.writeString(tiporeparacion);
            dest.writeString(idsubtiporeparacion);
            dest.writeString(subtiporeparacion);
            dest.writeString(idactividad);
            dest.writeString(actividad);
            dest.writeString(descripcion);
            dest.writeSparseArray(photos);
            dest.writeString(imagen);
            dest.writeByte((byte) (requiereFotos == null ? 0 : requiereFotos ? 1 : 2));
            dest.writeByte((byte) (external == null ? 0 : external ? 1 : 2));
            dest.writeString(token);
            dest.writeString(idelement);
            dest.writeString(element);
            dest.writeString(iddamage);
            dest.writeString(damage);
            dest.writeString(lenght);
            dest.writeString(height);
        }

        public String toJson() {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapterFactory(SparseArrayTypeAdapterFactory.INSTANCE)
                    .create();
            return gson.toJson(this);
        }

        @Override
        public String toString() {
            return "Damage{" +
                    "id='" + id + '\'' +
                    ", fecha='" + fecha + '\'' +
                    ", tipo='" + tipo + '\'' +
                    ", idtipo='" + idtipo + '\'' +
                    ", idparte='" + idparte + '\'' +
                    ", parte='" + parte + '\'' +
                    ", localizacion='" + localizacion + '\'' +
                    ", idreclasificacion='" + idreclasificacion + '\'' +
                    ", reclasificacion='" + reclasificacion + '\'' +
                    ", idtiporeparacion='" + idtiporeparacion + '\'' +
                    ", tiporeparacion='" + tiporeparacion + '\'' +
                    ", idsubtiporeparacion='" + idsubtiporeparacion + '\'' +
                    ", subtiporeparacion='" + subtiporeparacion + '\'' +
                    ", idactividad='" + idactividad + '\'' +
                    ", actividad='" + actividad + '\'' +
                    ", descripcion='" + descripcion + '\'' +
                    ", photos=" + photos +
                    ", imagen='" + imagen + '\'' +
                    ", requiereFotos=" + requiereFotos +
                    ", external=" + external +
                    ", token='" + token + '\'' +
                    '}';
        }
    }

    public static class Falla implements Parcelable, ViewAdapter<Falla> {
        private String id;
        private String fecha;
        private String tipo;
        private String idtipo;
        private String idreclasificacion;
        private String reclasificacion;
        private String idtiporeparacion;
        private String tiporeparacion;
        private String idsubtiporeparacion;
        private String subtiporeparacion;
        private Long idActividad;
        private String actividad;
        private String serial;
        private String parte;
        private String description;
        private SparseArray<PhotoAdapter> photos;
        private Boolean requiereFotos = false;
        private Boolean requireRepuestos = false;
        private String token;

        public Falla() {

        }

        protected Falla(@NonNull Parcel in) {
            id = in.readString();
            fecha = in.readString();
            tipo = in.readString();
            idtipo = in.readString();
            idreclasificacion = in.readString();
            reclasificacion = in.readString();
            idtiporeparacion = in.readString();
            tiporeparacion = in.readString();
            idsubtiporeparacion = in.readString();
            subtiporeparacion = in.readString();
            if (in.readByte() == 0) {
                idActividad = null;
            } else {
                idActividad = in.readLong();
            }
            actividad = in.readString();
            serial = in.readString();
            parte = in.readString();
            description = in.readString();
            photos = in.readSparseArray(getClass().getClassLoader());
            byte tmpRequiereFotos = in.readByte();
            requiereFotos = tmpRequiereFotos == 0 ? null : tmpRequiereFotos == 1;
            byte tmpRequireRepuestos = in.readByte();
            requireRepuestos = tmpRequireRepuestos == 0 ? null : tmpRequireRepuestos == 1;
            token = in.readString();
        }

        public static final Creator<Falla> CREATOR = new Creator<Falla>() {
            @Override
            public Falla createFromParcel(Parcel in) {
                return new Falla(in);
            }

            @Override
            public Falla[] newArray(int size) {
                return new Falla[size];
            }
        };

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getIdreclasificacion() {
            return idreclasificacion;
        }

        public void setIdreclasificacion(String idreclasificacion) {
            this.idreclasificacion = idreclasificacion;
        }

        public String getReclasificacion() {
            return reclasificacion;
        }

        public void setReclasificacion(String reclasificacion) {
            this.reclasificacion = reclasificacion;
        }

        public String getIdtiporeparacion() {
            return idtiporeparacion;
        }

        public void setIdtiporeparacion(String idtiporeparacion) {
            this.idtiporeparacion = idtiporeparacion;
        }

        public String getTiporeparacion() {
            return tiporeparacion;
        }

        public void setTiporeparacion(String tiporeparacion) {
            this.tiporeparacion = tiporeparacion;
        }

        public String getIdsubtiporeparacion() {
            return idsubtiporeparacion;
        }

        public void setIdsubtiporeparacion(String idsubtiporeparacion) {
            this.idsubtiporeparacion = idsubtiporeparacion;
        }

        public String getSubtiporeparacion() {
            return subtiporeparacion;
        }

        public void setSubtiporeparacion(String subtiporeparacion) {
            this.subtiporeparacion = subtiporeparacion;
        }

        public String getIdtipo() {
            return idtipo;
        }

        public void setIdtipo(String idtipo) {
            this.idtipo = idtipo;
        }

        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public Long getIdActividad() {
            return idActividad;
        }

        public void setIdActividad(Long idActividad) {
            this.idActividad = idActividad;
        }

        public String getActividad() {
            return actividad;
        }

        public void setActividad(String actividad) {
            this.actividad = actividad;
        }

        public String getSerial() {
            return serial;
        }

        public void setSerial(String serial) {
            this.serial = serial;
        }

        public String getParte() {
            return parte;
        }

        public void setParte(String parte) {
            this.parte = parte;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public SparseArray<PhotoAdapter> getPhotos() {
            return photos;
        }

        public void setPhotos(SparseArray<PhotoAdapter> photos) {
            this.photos = photos;
        }

        public String getFecha() {
            return fecha;
        }

        public void setFecha(String fecha) {
            this.fecha = fecha;
        }

        public Boolean getRequiereFotos() {
            return requiereFotos;
        }

        public void setRequiereFotos(Boolean requiereFotos) {
            this.requiereFotos = requiereFotos;
        }

        public Boolean getRequireRepuestos() {
            return requireRepuestos;
        }

        public void setRequireRepuestos(Boolean requireRepuestos) {
            this.requireRepuestos = requireRepuestos;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String toJson() {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapterFactory(SparseArrayTypeAdapterFactory.INSTANCE)
                    .create();
            return gson.toJson(this);
        }

        @Override
        public String toString() {
            return "Falla{" +
                    "id='" + id + '\'' +
                    ", fecha='" + fecha + '\'' +
                    ", tipo='" + tipo + '\'' +
                    ", idtipo='" + idtipo + '\'' +
                    ", idreclasificacion='" + idreclasificacion + '\'' +
                    ", reclasificacion='" + reclasificacion + '\'' +
                    ", idtiporeparacion='" + idtiporeparacion + '\'' +
                    ", tiporeparacion='" + tiporeparacion + '\'' +
                    ", idsubtiporeparacion='" + idsubtiporeparacion + '\'' +
                    ", subtiporeparacion='" + subtiporeparacion + '\'' +
                    ", idActividad=" + idActividad +
                    ", actividad='" + actividad + '\'' +
                    ", serial='" + serial + '\'' +
                    ", parte='" + parte + '\'' +
                    ", description='" + description + '\'' +
                    ", photos=" + photos +
                    ", requiereFotos=" + requiereFotos +
                    ", requireRepuestos=" + requireRepuestos +
                    ", token='" + token + '\'' +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(fecha);
            dest.writeString(tipo);
            dest.writeString(idtipo);
            dest.writeString(idreclasificacion);
            dest.writeString(reclasificacion);
            dest.writeString(idtiporeparacion);
            dest.writeString(tiporeparacion);
            dest.writeString(idsubtiporeparacion);
            dest.writeString(subtiporeparacion);
            if (idActividad == null) {
                dest.writeByte((byte) 0);
            } else {
                dest.writeByte((byte) 1);
                dest.writeLong(idActividad);
            }
            dest.writeString(actividad);
            dest.writeString(serial);
            dest.writeString(parte);
            dest.writeString(description);
            dest.writeSparseArray(photos);
            dest.writeByte((byte) (requiereFotos == null ? 0 : requiereFotos ? 1 : 2));
            dest.writeByte((byte) (requireRepuestos == null ? 0 : requireRepuestos ? 1 : 2));
            dest.writeString(token);
        }

        @NonNull
        @Override
        public String getTitle() {
            return getActividad();
        }

        @Nullable
        @Override
        public String getSubtitle() {
            return getSerial();
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
        public boolean compareTo(@NonNull Falla value) {
            if (getId() == null) {
                return getToken().equals(value.getToken());
            } else {
                return getId().equals(value.getId());
            }
        }
    }
}
