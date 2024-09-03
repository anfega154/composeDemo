package com.mantum.cmms.entity;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.mantum.cmms.entity.parameter.Barcode;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Equipo extends RealmObject {

    public static final String SELF = "Equipo";

    @PrimaryKey
    private String uuid;
    private Long id;
    private Cuenta cuenta;
    private String codigo;
    private String nombre;
    private String instalacionproceso;
    private String instalacionlocativa;
    private String familia1;
    private String familia2;
    private String familia3;
    private String provocaparo;
    private String ubicacion;
    private String observaciones;
    @SerializedName("informaciontecnica")
    private InformacionTecnica informacionTecnica;
    private RealmList<Variable> variables;
    private RealmList<Adjuntos> adjuntos;
    private RealmList<Adjuntos> imagenes;
    @SerializedName("dataam")
    private RealmList<Actividad> actividades;
    @SerializedName("datostecnicos")
    private RealmList<DatosTecnico> datostecnicos;
    private String gmap;
    private RealmList<OrdenTrabajo> ordenTrabajos;
    private RealmList<Falla> fallas;
    private String nfctoken;
    private String qrcode;
    private RealmList<Barcode> barcode;
    private String estado;
    private Long idfamilia1;
    private Long idinstalacionproceso;
    private Long idinstalacionlocativa;
    private String pais;
    private String departamento;
    private String ciudad;
    private Responsable personal;
    private InformacionFinanciera informacionfinanciera;
    private Long idpais;
    private Long iddepartamento;
    private Long idciudad;
    private String cliente;

    public Equipo() {
        this.variables = new RealmList<>();
        this.adjuntos = new RealmList<>();
        this.imagenes = new RealmList<>();
        this.actividades = new RealmList<>();
        this.datostecnicos = new RealmList<>();
        this.ordenTrabajos = new RealmList<>();
        this.fallas = new RealmList<>();
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
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

    public String getUUID() {
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getInstalacionproceso() {
        return instalacionproceso;
    }

    public void setInstalacionproceso(String instalacionproceso) {
        this.instalacionproceso = instalacionproceso;
    }

    public String getInstalacionlocativa() {
        return instalacionlocativa;
    }

    public void setInstalacionlocativa(String instalacionlocativa) {
        this.instalacionlocativa = instalacionlocativa;
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

    public String getProvocaparo() {
        return provocaparo;
    }

    public void setProvocaparo(String provocaparo) {
        this.provocaparo = provocaparo;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public InformacionTecnica getInformacionTecnica() {
        return informacionTecnica;
    }

    public void setInformacionTecnica(InformacionTecnica informacionTecnica) {
        this.informacionTecnica = informacionTecnica;
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

    public String getGmap() {
        return gmap;
    }

    public void setGmap(String gmap) {
        this.gmap = gmap;
    }

    public RealmList<DatosTecnico> getDatostecnicos() {
        return datostecnicos;
    }

    public void setDatostecnicos(RealmList<DatosTecnico> datostecnicos) {
        this.datostecnicos = datostecnicos;
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

    @NonNull
    public String getInformacionParaAsociar(@NonNull String content, @NonNull String tipo) {
        JsonObject dataset = new JsonObject();
        dataset.addProperty("identidad", getId());
        dataset.addProperty("codigo", content);
        dataset.addProperty("tipoentidad", SELF);
        dataset.addProperty("tipo", tipo);

        return new Gson().toJson(dataset);
    }

    public String getNfctoken() {
        return nfctoken;
    }

    public void setNfctoken(String nfctoken) {
        this.nfctoken = nfctoken;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public RealmList<Falla> getFallas() {
        return fallas;
    }

    public void setFallas(RealmList<Falla> fallas) {
        this.fallas = fallas;
    }

    public Long getIdfamilia1() {
        return idfamilia1;
    }

    public void setIdfamilia1(Long idfamilia1) {
        this.idfamilia1 = idfamilia1;
    }

    public Long getIdinstalacionproceso() {
        return idinstalacionproceso;
    }

    public void setIdinstalacionproceso(Long idinstalacionproceso) {
        this.idinstalacionproceso = idinstalacionproceso;
    }

    public Long getIdinstalacionlocativa() {
        return idinstalacionlocativa;
    }

    public void setIdinstalacionlocativa(Long idinstalacionlocativa) {
        this.idinstalacionlocativa = idinstalacionlocativa;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public Responsable getPersonal() {
        return personal;
    }

    public void setPersonal(Responsable personal) {
        this.personal = personal;
    }

    public InformacionFinanciera getInformacionfinanciera() {
        return informacionfinanciera;
    }

    public void setInformacionfinanciera(InformacionFinanciera informacionfinanciera) {
        this.informacionfinanciera = informacionfinanciera;
    }

    public Long getIdpais() {
        return idpais;
    }

    public void setIdpais(Long idpais) {
        this.idpais = idpais;
    }

    public Long getIddepartamento() {
        return iddepartamento;
    }

    public void setIddepartamento(Long iddepartamento) {
        this.iddepartamento = iddepartamento;
    }

    public Long getIdciudad() {
        return idciudad;
    }

    public void setIdciudad(Long idciudad) {
        this.idciudad = idciudad;
    }

    public static class EquipoAux {
        private String codigo;
        private String nombre;
        private String instalacionproceso;
        private String instalacionlocativa;
        private String familia1;
        private String familia2;
        private String familia3;
        private String provocaparo;
        private String ubicacion;
        private String observaciones;
        @SerializedName("informaciontecnica")
        private InformacionTecnica informacionTecnica;
        private RealmList<Variable> variables;
        private RealmList<Adjuntos> adjuntos;
        private RealmList<Adjuntos> imagenes;
        @SerializedName("dataam")
        private RealmList<Actividad> actividades;
        @SerializedName("datostecnicos")
        private RealmList<DatosTecnico> datostecnicos;
        private String gmap;
        private RealmList<OrdenTrabajo> ordenTrabajos;
        private RealmList<Falla> fallas;
        private String nfctoken;
        private String qrcode;
        private String barcode;
        private String estado;
        private String cliente;

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

        public String getInstalacionproceso() {
            return instalacionproceso;
        }

        public void setInstalacionproceso(String instalacionproceso) {
            this.instalacionproceso = instalacionproceso;
        }

        public String getInstalacionlocativa() {
            return instalacionlocativa;
        }

        public void setInstalacionlocativa(String instalacionlocativa) {
            this.instalacionlocativa = instalacionlocativa;
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

        public String getProvocaparo() {
            return provocaparo;
        }

        public void setProvocaparo(String provocaparo) {
            this.provocaparo = provocaparo;
        }

        public String getUbicacion() {
            return ubicacion;
        }

        public void setUbicacion(String ubicacion) {
            this.ubicacion = ubicacion;
        }

        public String getObservaciones() {
            return observaciones;
        }

        public void setObservaciones(String observaciones) {
            this.observaciones = observaciones;
        }

        public InformacionTecnica getInformacionTecnica() {
            return informacionTecnica;
        }

        public void setInformacionTecnica(InformacionTecnica informacionTecnica) {
            this.informacionTecnica = informacionTecnica;
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

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }

        public RealmList<Falla> getFallas() {
            return fallas;
        }

        public void setFallas(RealmList<Falla> fallas) {
            this.fallas = fallas;
        }

        public String getCliente() {
            return cliente;
        }

        public void setCliente(String cliente) {
            this.cliente = cliente;
        }
    }
}