package com.mantum.cmms.domain;

import android.util.SparseArray;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.mantum.cmms.Multipart;
import com.mantum.cmms.entity.Entidad;
import com.mantum.cmms.entity.EntidadesClienteListaChequeo;
import com.mantum.cmms.entity.Personal;
import com.mantum.cmms.entity.PersonalListaChequeo;
import com.mantum.cmms.entity.Recurso;
import com.mantum.cmms.entity.Variable;
import com.mantum.component.service.Photo;
import com.mantum.component.util.Tool;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.realm.RealmList;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class Diligenciar implements Multipart {

    private String code;
    private String token;
    private Long idrt;
    private Long idot;
    private Long idlc;

    private String idFirma;
    private Long idejecucion;
    private String date;
    private String timestart;
    private String timeend;
    private final List<Long> ams;
    private final List<String> observaciones;
    private String description;
    private List<Recurso> recursos;
    private List<Variable> variables;
    private List<Photo> files;
    private Float horasHabilesDia;
    private List<Entidad> entidades;
    private boolean parcial;
    private String fechaCreacionReal;
    private String fechaFinReal;
    private String tipo;
    private transient double latitud;
    private transient double longitud;
    private transient double altitud;
    private transient double exactitud;
    private String dateendvig;
    private List<PersonalListaChequeo> personas;
    private List<EntidadesClienteListaChequeo> entidadesClienteListaChequeos;
    private boolean listachequeo;
    private boolean ocultarFechaHoraVigente;
    private Long idCliente;
    private String cliente;
    private List<Personal> grupos;

    public Diligenciar() {
        this.idot = null;
        this.token = UUID.randomUUID().toString();
        this.ams = new ArrayList<>();
        this.recursos = new RealmList<>();
        this.variables = new ArrayList<>();
        this.files = new ArrayList<>();
        this.observaciones = new ArrayList<>();
        this.entidades = new ArrayList<>();
        this.entidades = new ArrayList<>();
        this.listachequeo = false;
        this.ocultarFechaHoraVigente = false;
        this.personas = new ArrayList<>();
        this.grupos = new ArrayList<>();
        this.entidadesClienteListaChequeos = new ArrayList<>();
    }

    public List<EntidadesClienteListaChequeo> getEntidadesClienteListaChequeos() {
        return entidadesClienteListaChequeos;
    }

    public void setEntidadesClienteListaChequeos(List<EntidadesClienteListaChequeo> entidadesClienteListaChequeos) {
        this.entidadesClienteListaChequeos = entidadesClienteListaChequeos;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public boolean isOcultarFechaHoraVigente() {
        return ocultarFechaHoraVigente;
    }

    public void setOcultarFechaHoraVigente(boolean ocultarFechaHoraVigente) {
        this.ocultarFechaHoraVigente = ocultarFechaHoraVigente;
    }

    public Long getIdlc() {
        return idlc;
    }

    public void setIdlc(Long idlc) {
        this.idlc = idlc;
    }

    public boolean isListachequeo() {
        return listachequeo;
    }

    public void setListachequeo(boolean listachequeo) {
        this.listachequeo = listachequeo;
    }

    public List<PersonalListaChequeo> getPersonas() {
        return personas;
    }

    public void setPersonas(List<PersonalListaChequeo> personas) {
        this.personas = personas;
    }

    public String getDateEndVig() {
        return dateendvig;
    }

    public void setDateEndVig(String dateendvig) {
        this.dateendvig = dateendvig;
    }

    public Long getIdot() {
        return idot;
    }

    public void setIdot(Long idot) {
        this.idot = idot;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getIdrt() {
        return idrt;
    }

    public void setIdrt(Long idrt) {
        this.idrt = idrt;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTimestart() {
        return timestart;
    }

    public void setTimestart(String timestart) {
        this.timestart = timestart;
    }

    public String getTimeend() {
        return timeend;
    }

    public void setTimeend(String timeend) {
        this.timeend = timeend;
    }

    public List<Long> getAms() {
        return ams;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Recurso> getRecursos() {
        return recursos;
    }

    public void setRecursos(List<Recurso> recursos) {
        this.recursos = recursos;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public void setVariables(ArrayList<Variable> variables) {
        this.variables = variables;
    }

    public List<Photo> getFiles() {
        return files;
    }

    public void setFiles(List<Photo> files) {
        this.files = files;
    }

    public Long getIdejecucion() {
        return idejecucion;
    }

    public void setIdejecucion(Long idejecucion) {
        this.idejecucion = idejecucion;
    }

    public void addAM(Long id) {
        this.ams.add(id);
    }

    public void addVariable(Variable variable) {
        this.variables.add(variable);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static Diligenciar fromJson(@NonNull String json) {
        return new Gson().fromJson(json, Diligenciar.class);
    }

    public List<String> getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observacion) {
        this.observaciones.add(observacion);
    }

    private Float getHorasHabilesDia() {
        return horasHabilesDia;
    }

    public void setHorasHabilesDia(Float horasHabilesDia) {
        this.horasHabilesDia = horasHabilesDia;
    }

    public List<Entidad> getEntidades() {
        return entidades;
    }

    public void setEntidades(List<Entidad> entidades) {
        this.entidades = entidades;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLonngitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public double getAltitud() {
        return altitud;
    }

    public void setAltitud(double altitud) {
        this.altitud = altitud;
    }

    public double getExactitud() {
        return exactitud;
    }

    public void setExactitud(double exactitud) {
        this.exactitud = exactitud;
    }

    public List<Personal> getGrupos() {
        return grupos;
    }

    public void setGrupos(List<Personal> grupos) {
        this.grupos = grupos;
    }

    public void setIdFirma(String idFirma) {
        this.idFirma = idFirma;
    }

    public String getIdFirma() {
        return idFirma;
    }

    @Override
    public MultipartBody.Builder builder() {
        MultipartBody.Builder multipart = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("token", Tool.formData(getToken()))
                .addFormDataPart("date", Tool.formData(getDate()))
                .addFormDataPart("datestart", Tool.formData(getDate()))
                .addFormDataPart("timestart", Tool.formData(getTimestart()))
                .addFormDataPart("timeend", Tool.formData(getTimeend()))
                .addFormDataPart("idrt", Tool.formData(getIdrt()))
                .addFormDataPart("idejecucion", Tool.formData(getIdejecucion()))
                .addFormDataPart("description", Tool.formData(getDescription()))
                .addFormDataPart("parcial", Tool.formData(isParcial()))
                .addFormDataPart("fechacreacionreal", Tool.formData(getFechaCreacionReal()))
                .addFormDataPart("fechafinreal", Tool.formData(getFechaFinReal()))
                .addFormDataPart("dateendvig", Tool.formData(getDateEndVig()))
                .addFormDataPart("timeendvig", Tool.formData(getTimeend()))
                .addFormDataPart("tipo", Tool.formData(getTipo()))
                .addFormDataPart("latitude", Tool.formData(getLatitud()))
                .addFormDataPart("longitude", Tool.formData(getLonngitud()))
                .addFormDataPart("altitude", Tool.formData(getLatitud()))
                .addFormDataPart("accuracy", Tool.formData(getExactitud()))
                .addFormDataPart("horashabilesdia", Tool.formData(getHorasHabilesDia()))
                .addFormDataPart("idfirma", Tool.formData(getIdFirma()));

        // Incluye id lista de chequeo
        if (isListachequeo()) {
            multipart.addFormDataPart("idlc", Tool.formData(getIdlc()));
        }

        // Incluye un id extra
        if (getIdot() != null && getIdot() > 0) {
            multipart.addFormDataPart("idot", Tool.formData(getIdot()));
        }

        // Incluye las actividades de mantenimiento
        for (Long id : getAms()) {
            multipart.addFormDataPart("ams[]", String.valueOf(id));
        }

        // Incluye las observaciones
        for (String observacion : getObservaciones()) {
            multipart.addFormDataPart("observacion[]", observacion);
        }

        // Incluye las variables
        int index = 0;
        for (Variable variable : getVariables()) {
            index = index + 1;
            multipart.addFormDataPart("measuring_" + index + "[]", String.valueOf(variable.getId()));
            multipart.addFormDataPart("measuring_" + index + "[]", String.valueOf(variable.getValor()));
            multipart.addFormDataPart("measuring_" + index + "[]", String.valueOf(variable.getObservacion()));
        }

        // Incluye los recursos
        index = 0;
        for (Recurso recurso : getRecursos()) {
            index = index + 1;
            multipart.addFormDataPart("resources_" + index + "[]", String.valueOf(recurso.getId()));
            multipart.addFormDataPart("resources_" + index + "[]", String.valueOf(recurso.getCantidad()));
        }

        // Incluye las entidades
        for (EntidadesClienteListaChequeo entidad : getEntidadesClienteListaChequeos()) {
            multipart.addFormDataPart("entidadescliente[]", String.valueOf(entidad.getId()));
        }

        // Incluye el personal
        for (PersonalListaChequeo persona : getPersonas()) {
            if (persona.isSeleccionado()) {
                multipart.addFormDataPart("personal[]", String.valueOf(persona.getId()));
            }
        }

        // Incluye las fotos
        for (Photo photo : getFiles()) {
            if (photo.exists() && photo.getNaturalName() != null) {
                if (photo.getMime() != null && photo.getFile() != null) {
                    multipart.addFormDataPart(photo.getNaturalName(), Tool.formData(photo.getDescription()));
                    multipart.addFormDataPart("files[]", photo.getName(),
                            RequestBody.create(MediaType.parse(photo.getMime()), photo.getFile()));
                }
            }
        }

        index = 1;
        if (!getGrupos().isEmpty()) {
            for (Personal personal : getGrupos()) {
                multipart.addFormDataPart("staff_" + index + "[]", Tool.formData(personal.getId()));
                multipart.addFormDataPart("staff_" + index + "[]", Tool.formData(personal.getNombre()));
                index = index + 1;
            }
        }

        return multipart;
    }


    public boolean isParcial() {
        return parcial;
    }

    public void setParcial(boolean parcial) {
        this.parcial = parcial;
    }

    public String getFechaCreacionReal() {
        return fechaCreacionReal;
    }

    public void setFechaCreacionReal(String fechaCreacionReal) {
        this.fechaCreacionReal = fechaCreacionReal;
    }

    public String getFechaFinReal() {
        return fechaFinReal;
    }

    public void setFechaFinReal(String fechaFinReal) {
        this.fechaFinReal = fechaFinReal;
    }

    @NonNull
    @Override
    public String toString() {
        return "Diligenciar{" +
                "code='" + code + '\'' +
                ", token='" + token + '\'' +
                ", idrt=" + idrt +
                ", idot=" + idot +
                ", idlc=" + idlc +
                ", idejecucion=" + idejecucion +
                ", date='" + date + '\'' +
                ", timestart='" + timestart + '\'' +
                ", timeend='" + timeend + '\'' +
                ", ams=" + ams +
                ", observaciones=" + observaciones +
                ", description='" + description + '\'' +
                ", recursos=" + recursos +
                ", variables=" + variables +
                ", files=" + files +
                ", horasHabilesDia=" + horasHabilesDia +
                ", entidades=" + entidades +
                ", parcial=" + parcial +
                ", fechaCreacionReal='" + fechaCreacionReal + '\'' +
                ", fechaFinReal='" + fechaFinReal + '\'' +
                ", tipo='" + tipo + '\'' +
                ", latitud=" + latitud +
                ", longitud=" + longitud +
                ", altitud=" + altitud +
                ", exactitud=" + exactitud +
                ", dateendvig='" + dateendvig + '\'' +
                ", personas=" + personas +
                ", listachequeo=" + listachequeo +
                ", ocultarFechaHoraVigente=" + ocultarFechaHoraVigente +
                ", idCliente=" + idCliente +
                ", cliente='" + cliente + '\'' +
                ", idfirma=" + idFirma +
                '}';
    }
}