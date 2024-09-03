package com.mantum.cmms.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mantum.R;
import com.mantum.cmms.database.Model;
import com.mantum.component.adapter.handler.ViewAdapter;
import com.mantum.component.adapter.handler.ViewTrasladoAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;

//Clase utilizada para equipos
public class Almacen extends RealmObject implements Model, Serializable, ViewTrasladoAdapter<Almacen>, ViewAdapter<Almacen> {

    private Long id;

    private Cuenta cuenta;

    private String tipo;

    private String codigo;

    private String nombre;

    private Float cantidad;

    private String nfc;

    private String qrcode;

    private String barcode;

    private Long idbodega;

    @Nullable
    private String bodega;

    private boolean activo;

    @Nullable
    private Jerarquia jerarquia;

    @Nullable
    @Ignore
    private Long ippadre;

    @Nullable
    private String estado;

    @Nullable
    @Ignore
    private String descripcion;

    @Nullable
    @Ignore
    private Float cantidadentrar;

    private String unidadconsumo;

    private String tiposalida;

    @Nullable
    @Ignore
    private Long idequipo;

    @Nullable
    public Long getIdequipo() { return idequipo; }

    public void setIdequipo(Long idequipo) { this.idequipo = idequipo; }

    @Nullable
    public Float getCantidadentrar() { return cantidadentrar; }


    public void setCantidadentrar(@Nullable Float cantidadentrar) { this.cantidadentrar = cantidadentrar; }

    @Nullable
    public String getDescripcion() { return descripcion; }

    public void setDescripcion(@Nullable String descripcion) { this.descripcion = descripcion; }

    public String getEstado() { return estado; }

    public void setEstado(@Nullable String estado) { this.estado = estado; }

    public Long getIppadrestandby() { return ippadre; }

    public void setIppadrestandby(Long ippadre) { this.ippadre = ippadre; }

    public Jerarquia getJerarquia() { return jerarquia; }

    public void setJerarquia(Jerarquia jerarquia) { this.jerarquia = jerarquia; }

    public Long getIdbodega() { return idbodega; }

    public void setIdbodega(Long idbodega) { this.idbodega = idbodega; }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getNfc() {
        return nfc;
    }

    public void setNfc(String nfc) {
        this.nfc = nfc;
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
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

    public Float getCantidad() {
        return cantidad;
    }

    public void setCantidad(Float cantidaddisponible) { this.cantidad = cantidaddisponible; }

    @Nullable
    public String getBodega() {
        return bodega;
    }

    public void setBodega(@Nullable String bodega) {
        this.bodega = bodega;
    }

    public String getUnidadconsumo() { return unidadconsumo; }

    public void setUnidadconsumo(String unidadconsumo) { this.unidadconsumo = unidadconsumo; }

    public String getTiposalida() {
        return tiposalida;
    }

    public void setTiposalida(String tiposalida) {
        this.tiposalida = tiposalida;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getCodigo() != null ? getCodigo().trim() : "";
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return getNombre();
    }

    @Nullable
    @Override
    public String getSummary() {
        String disponible = "Disponible: " + getCantidad() + " " + getUnidadconsumo();
        String tipoSalida = getTiposalida() != null ? " | " + getTiposalida() : "";
        return disponible + tipoSalida;
    }

    @Nullable
    @Override
    public String getNombeBodega() {
        Realm realm = Realm.getDefaultInstance();
        Bodega bodega = realm.where(Bodega.class)
                .equalTo("id", getIdbodega())
                .findFirst();

        return "Almacén: " + bodega.getCodigo() + " | " + bodega.getNombre();
    }

    @Nullable
    @Override
    public String getPadreItem() { return getJerarquia() != null ? "Padre: "+getJerarquia().getNombre() : null; }

    @Nullable
    @Override
    public String getIcon() { return null; }

    @Nullable
    @Override
    public Integer getDrawable() { return R.drawable.recursos; }

    @NonNull
    @Override
    public Float getQuantity() { return getCantidadentrar(); }

    @NonNull
    @Override
    public String getEstadoItem() { return getEstado(); }

    @NonNull
    @Override
    public String getItemType() { return getTipo(); }

    @Override
    public boolean compareTo(Almacen value) {
        return getId().equals(value.id);
    }

    public class Request implements Serializable {

        private List<Almacen> elementos;
        private List<Almacen> recursos;
        private List<String> estados;
        private String imageqr;

        public Request() {
            this.elementos = new ArrayList<>();
            this.recursos = new ArrayList<>();
            this.estados = new ArrayList<>();
            this.imageqr = new String();
        }

        public List<Almacen> getElementos() {
            return elementos;
        }

        public List<Almacen> getRecursos() {
            return recursos;
        }

        public List<String> getEstados() { return estados; }
        public String getImageqr() {
            return imageqr;
        }
    }

    @NonNull
    public String getDataNewStorer(@NonNull Integer idbodega, @NonNull Integer idalmacenista, @NonNull Long idnewstorer, String expiracion, Double latitud, Double longitud) {
        JsonObject dataset = new JsonObject();
        dataset.addProperty("idalmacen", idbodega);
        dataset.addProperty("idalmacenista", idalmacenista);
        dataset.addProperty("idnuevoalmacenista", idnewstorer);
        dataset.addProperty("expiracion", expiracion);
        dataset.addProperty("latitud", latitud);
        dataset.addProperty("longitud", longitud);

        return new Gson().toJson(dataset);
    }
}
