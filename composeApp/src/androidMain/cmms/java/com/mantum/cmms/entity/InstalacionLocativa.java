package com.mantum.cmms.entity;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class InstalacionLocativa extends RealmObject {

    public static final String SELF = "InstalacionLocativa";

    @PrimaryKey
    private String uuid;

    private Long id;

    private Cuenta cuenta;

    private String codigo;

    private String nombre;

    private String instalacionpadre;

    private String tipodeinstalacion;

    private String estado;

    private String criticidad;

    private String familia1;

    private String familia2;

    private String familia3;

    private String direccion;

    private RealmList<Variable> variables;

    private RealmList<Adjuntos> adjuntos;

    private RealmList<Adjuntos> imagenes;

    @SerializedName("dataam")
    private RealmList<Actividad> actividades;

    @SerializedName("datostecnicos")
    private RealmList<DatosTecnico> datostecnicos;

    private String gmap;

    private RealmList<OrdenTrabajo> ordenTrabajos;

    private String nfctoken;

    private String qrcode;

    private String barcode;

    private RealmList<Falla> fallas;

    public InstalacionLocativa() {
        this.variables = new RealmList<>();
        this.adjuntos = new RealmList<>();
        this.imagenes = new RealmList<>();
        this.actividades = new RealmList<>();
        this.datostecnicos = new RealmList<>();
        this.ordenTrabajos = new RealmList<>();
        this.fallas = new RealmList<>();
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
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

    public String getInstalacionpadre() {
        return instalacionpadre;
    }

    public void setInstalacionpadre(String instalacionpadre) {
        this.instalacionpadre = instalacionpadre;
    }

    public String getTipodeinstalacion() {
        return tipodeinstalacion;
    }

    public void setTipodeinstalacion(String tipodeinstalacion) {
        this.tipodeinstalacion = tipodeinstalacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCriticidad() {
        return criticidad;
    }

    public void setCriticidad(String criticidad) {
        this.criticidad = criticidad;
    }

    public String getFamilia1() {
        return familia1;
    }

    public void setFamilia1(String familia1) {
        this.familia1 = familia1;
    }

    public String getFamilia2() {
        return familia2;
    }

    public void setFamilia2(String familia2) {
        this.familia2 = familia2;
    }

    public String getFamilia3() {
        return familia3;
    }

    public void setFamilia3(String familia3) {
        this.familia3 = familia3;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public RealmList<Variable> getVariables() {
        return variables;
    }

    public void setVariables(RealmList<Variable> variables) {
        this.variables = variables;
    }

    public RealmList<Adjuntos> getAdjuntos() {
        return adjuntos;
    }

    public void setAdjuntos(RealmList<Adjuntos> adjuntos) {
        this.adjuntos = adjuntos;
    }

    public RealmList<Adjuntos> getImagenes() {
        return imagenes;
    }

    public void setImagenes(RealmList<Adjuntos> imagenes) {
        this.imagenes = imagenes;
    }

    public RealmList<Actividad> getActividades() {
        return actividades;
    }

    public void setActividades(RealmList<Actividad> actividades) {
        this.actividades = actividades;
    }

    public RealmList<DatosTecnico> getDatostecnicos() {
        return datostecnicos;
    }

    public void setDatostecnicos(RealmList<DatosTecnico> datostecnicos) {
        this.datostecnicos = datostecnicos;
    }

    public String getGmap() {
        return gmap;
    }

    public void setGmap(String gmap) {
        this.gmap = gmap;
    }

    public String getNombreMostrar() {
        return String.format("%s | %s", getCodigo(), getNombre());
    }

    public RealmList<OrdenTrabajo> getOrdenTrabajos() {
        return ordenTrabajos;
    }

    public void setOrdenTrabajos(RealmList<OrdenTrabajo> ordenTrabajos) {
        this.ordenTrabajos = ordenTrabajos;
    }

    public String getNfctoken() {
        return nfctoken;
    }

    public void setNfctoken(String nfctoken) {
        this.nfctoken = nfctoken;
    }

    public RealmList<Falla> getFallas() {
        return fallas;
    }

    public void setFallas(RealmList<Falla> fallas) {
        this.fallas = fallas;
    }

    public String getInformacionParaAsociar(@NonNull String content, @NonNull String tipo) {
        JsonObject dataset = new JsonObject();
        dataset.addProperty("identidad", getId());
        dataset.addProperty("codigo", content);
        dataset.addProperty("tipoentidad", SELF);
        dataset.addProperty("tipo", tipo);

        return new Gson().toJson(dataset);
    }
}
