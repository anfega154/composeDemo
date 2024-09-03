package com.mantum.cmms.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.mantum.R;
import com.mantum.cmms.Multipart;
import com.mantum.component.adapter.handler.ViewAdapter;
import com.mantum.component.service.Photo;
import com.mantum.component.util.Tool;

import java.io.Serializable;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class Falla extends RealmObject implements ViewAdapter<Falla> {

    @PrimaryKey
    private String UUID;

    private Long id;

    private Cuenta cuenta;

    private String nombre;

    private String entidad;

    private String resumen;

    private String am;

    private Long idot;

    private String ot;

    private String descripcion;

    private String idtipofalla;

    private String fechainicio;

    private String fechafin;

    private String horafin;

    @SerializedName("requiererepuestos")
    private boolean requiererepuesto;

    @SerializedName("requierefotos")
    private boolean requierefoto;

    private RealmList<Consumible> consumibles;

    private RealmList<RepuestoManual> repuestos;

    private RealmList<ElementoFalla> elementos;

    private RealmList<Adjuntos> imagenes;

    private RealmList<Adjuntos> adjuntos;

    public Falla() {
        this.consumibles = new RealmList<>();
        this.repuestos = new RealmList<>();
        this.elementos = new RealmList<>();
        this.imagenes = new RealmList<>();
        this.adjuntos = new RealmList<>();
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

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public String getAm() {
        return am;
    }

    public void setAm(String am) {
        this.am = am;
    }

    public Long getIdot() {
        return idot;
    }

    public void setIdot(Long idot) {
        this.idot = idot;
    }

    public String getOt() {
        return ot;
    }

    public void setOt(String ot) {
        this.ot = ot;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getIdtipofalla() {
        return idtipofalla;
    }

    public void setIdtipofalla(String idtipofalla) {
        this.idtipofalla = idtipofalla;
    }

    public String getResumen() {
        return resumen;
    }

    public void setResumen(String resumen) {
        this.resumen = resumen;
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

    public String getHorafin() {
        return horafin;
    }

    public void setHorafin(String horafin) {
        this.horafin = horafin;
    }

    public boolean isRequiererepuesto() {
        return requiererepuesto;
    }

    public void setRequiererepuesto(boolean requiererepuesto) {
        this.requiererepuesto = requiererepuesto;
    }

    public boolean isRequierefoto() {
        return requierefoto;
    }

    public void setRequierefoto(boolean requierefoto) {
        this.requierefoto = requierefoto;
    }

    public RealmList<Consumible> getConsumibles() {
        return consumibles;
    }

    public void setConsumibles(RealmList<Consumible> consumibles) {
        this.consumibles = consumibles;
    }

    public RealmList<RepuestoManual> getRepuestos() {
        return repuestos;
    }

    public void setRepuestos(RealmList<RepuestoManual> repuestos) {
        this.repuestos = repuestos;
    }

    public RealmList<ElementoFalla> getElementos() {
        return elementos;
    }

    public void setElementos(RealmList<ElementoFalla> elementos) {
        this.elementos = elementos;
    }

    public RealmList<Adjuntos> getImagenes() {
        return imagenes;
    }

    public void setImagenes(RealmList<Adjuntos> imagenes) {
        this.imagenes = imagenes;
    }

    public RealmList<Adjuntos> getAdjuntos() {
        return adjuntos;
    }

    public void setAdjuntos(RealmList<Adjuntos> adjuntos) {
        this.adjuntos = adjuntos;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getResumen() != null ? getResumen() : "";
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return getEntidad() != null ? getEntidad() : "";
    }

    @Nullable
    @Override
    public String getSummary() {
        return getDescripcion() != null ? getDescripcion() : "";
    }

    @Nullable
    @Override
    public String getIcon() {
        return null;
    }

    @Nullable
    @Override
    public Integer getDrawable() {
        return fechafin != null ? R.drawable.ic_check_orange : null;
    }

    @Override
    public boolean compareTo(Falla value) {
        return getId().equals(value.id);
    }

    public static class Request implements Serializable {

        private String UUID;

        private Long id;

        private String fechainicio;

        private boolean corregida;

        private String fechafin;

        private String horafin;

        private boolean requiererepuesto;

        private boolean requierefoto;

        private List<RepuestoManual.Repuesto> repuestos;

        private List<Consumible.ConsumibleHelper> consumibles;

        private List<Photo> imagenesPrevias;

        private List<Photo> imagenesPosteriores;

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

        public String getFechainicio() {
            return fechainicio;
        }

        public void setFechainicio(String fechainicio) {
            this.fechainicio = fechainicio;
        }

        public boolean isCorregida() {
            return corregida;
        }

        public void setCorregida(boolean corregida) {
            this.corregida = corregida;
        }

        public String getFechafin() {
            return fechafin;
        }

        public void setFechafin(String fechafin) {
            this.fechafin = fechafin;
        }

        public String getHorafin() {
            return horafin;
        }

        public void setHorafin(String horafin) {
            this.horafin = horafin;
        }

        public boolean isRequiererepuesto() {
            return requiererepuesto;
        }

        public void setRequiererepuesto(boolean requiererepuesto) {
            this.requiererepuesto = requiererepuesto;
        }

        public boolean isRequierefoto() {
            return requierefoto;
        }

        public void setRequierefoto(boolean requierefoto) {
            this.requierefoto = requierefoto;
        }

        public List<RepuestoManual.Repuesto> getRepuestos() {
            return repuestos;
        }

        public void setRepuestos(List<RepuestoManual.Repuesto> repuestos) {
            this.repuestos = repuestos;
        }

        public List<Consumible.ConsumibleHelper> getConsumibles() {
            return consumibles;
        }

        public void setConsumibles(List<Consumible.ConsumibleHelper> consumibles) {
            this.consumibles = consumibles;
        }

        public List<Photo> getImagenesPrevias() {
            return imagenesPrevias;
        }

        public void setImagenesPrevias(List<Photo> imagenesPrevias) {
            this.imagenesPrevias = imagenesPrevias;
        }

        public List<Photo> getImagenesPosteriores() {
            return imagenesPosteriores;
        }

        public void setImagenesPosteriores(List<Photo> imagenesPosteriores) {
            this.imagenesPosteriores = imagenesPosteriores;
        }
    }

    public static class CreateFalla implements Multipart {

        private String token;

        private String descripcion;

        private Long idequipo;

        private Long idlocacion;

        private String groupcode;

        private String tipofalla;

        private String date;

        private int abrirot;

        private String codigogama;

        private String descripciongama;

        private Long idot;

        private String codigoequipo;

        private List<RepuestoManual.Repuesto> repuestos;

        private List<Photo> imagenes;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public Long getIdequipo() {
            return idequipo;
        }

        public void setIdequipo(Long idequipo) {
            this.idequipo = idequipo;
        }

        public Long getIdlocacion() {
            return idlocacion;
        }

        public void setIdlocacion(Long idlocacion) {
            this.idlocacion = idlocacion;
        }

        public String getGroupcode() {
            return groupcode;
        }

        public void setGroupcode(String groupcode) {
            this.groupcode = groupcode;
        }

        public String getTipofalla() {
            return tipofalla;
        }

        public void setTipofalla(String tipofalla) {
            this.tipofalla = tipofalla;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public int getAbrirot() {
            return abrirot;
        }

        public void setAbrirot(int abrirot) {
            this.abrirot = abrirot;
        }

        public String getCodigogama() {
            return codigogama;
        }

        public void setCodigogama(String codigogama) {
            this.codigogama = codigogama;
        }

        public String getDescripciongama() {
            return descripciongama;
        }

        public void setDescripciongama(String descripciongama) {
            this.descripciongama = descripciongama;
        }

        public Long getIdot() {
            return idot;
        }

        public void setIdot(Long idot) {
            this.idot = idot;
        }

        public String getCodigoequipo() {
            return codigoequipo;
        }

        public void setCodigoequipo(String codigoequipo) {
            this.codigoequipo = codigoequipo;
        }

        public List<RepuestoManual.Repuesto> getRepuestos() {
            return repuestos;
        }

        public void setRepuestos(List<RepuestoManual.Repuesto> repuestos) {
            this.repuestos = repuestos;
        }

        public List<Photo> getImagenes() {
            return imagenes;
        }

        public void setImagenes(List<Photo> imagenes) {
            this.imagenes = imagenes;
        }

        public String toJson() {
            return new Gson().toJson(this);
        }

        @Override
        public MultipartBody.Builder builder() {
            MultipartBody.Builder multipart = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("token", getToken())
                    .addFormDataPart("descripcion", Tool.formData(getDescripcion()))
                    .addFormDataPart("idequipo", Tool.formData(getIdequipo()))
                    .addFormDataPart("idlocacion", Tool.formData(getIdlocacion()))
                    .addFormDataPart("groupcode", Tool.formData(getGroupcode()))
                    .addFormDataPart("tipofalla", Tool.formData(getTipofalla()))
                    .addFormDataPart("date", Tool.formData(getDate()))
                    .addFormDataPart("abrirot", Tool.formData(getAbrirot()))
                    .addFormDataPart("codigogama", Tool.formData(getCodigogama()))
                    .addFormDataPart("idot", Tool.formData(getIdot()))
                    .addFormDataPart("codigoequipo", Tool.formData(getCodigoequipo()));

            int key = 1;
            if (getRepuestos() != null && !getRepuestos().isEmpty()) {
                for (RepuestoManual.Repuesto repuesto : getRepuestos()) {
                    multipart.addFormDataPart("replacement_" + key + "[]", Tool.formData(repuesto.getNombre()));
                    multipart.addFormDataPart("replacement_" + key + "[]", Tool.formData(repuesto.getSerialRetiro()));

                    key = key + 1;
                }
            }

            key = 1;
            if (getImagenes() != null && !getImagenes().isEmpty()) {
                for (Photo photo : getImagenes()) {
                    if (photo.exists()) {
                        multipart.addFormDataPart(photo.getNaturalName(), Tool.formData(photo.getDescription()));
                        multipart.addFormDataPart("filesfail_" + key + "[]", photo.getName(),
                                RequestBody.create(MediaType.parse(photo.getMime()), photo.getFile()));

                        key = key + 1;
                    }
                }
            }

            return multipart;
        }
    }
}
