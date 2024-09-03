package com.mantum.cmms.entity;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.mantum.cmms.database.Model;
import com.mantum.component.OnInvoke;
import com.mantum.component.adapter.handler.ViewInformationAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Transaccion extends RealmObject implements Model, ViewInformationAdapter<Transaccion, Archivos> {

    public static final String ESTADO_PENDIENTE = "pendiente";
    public static final String ESTADO_ERROR = "error";
    public static final String ESTADO_SINCRONIZANDO = "sincronizando";
    public static final String ESTADO_SINCRONIZADO = "sincronizado";
    public static final String MODULO_LISTA_CHEQUEO = "Lista de chequeo";
    public static final String MODULO_RUTA_TRABAJO = "Ruta de trabajo";
    public static final String MODULO_GEOLOCALIZACION = "Geolocalización";
    public static final String MODULO_VARIABLES = "Variables";
    public static final String MODULO_SOLICITUD_SERVICIO = "Solicitud de servicio";
    public static final String MODULO_INSPECCION = "Inspección";
    public static final String MODULO_ACTIVOS = "Activos";
    public static final String MODULO_EQUIPOS = "Equipos";
    public static final String MODULO_ORDEN_TRABAJO = "Órden de trabajo";
    public static final String MODULO_BITACORA = "Bitácora";
    public static final String MODULO_ARCHIVO = "Archivo";
    public static final String MODULO_AUTORIZACION_ACCESO = "Autorización de acceso";
    public static final String MODULO_MARCACION = "Marcación";
    public static final String ACCION_RECIBIR_ORDEN_TRABAJO = "Recibir O.T.";
    public static final String ACCION_TERMINAR_ORDEN_TRABAJO = "Terminar O.T.";
    public static final String ACCION_ACTUALIZAR_CONTACTO = "Actualizar contacto";
    public static final String ACCION_UBICACION = "Ubicación";
    public static final String ACCION_DILIGENCIAR_RUTA_TRABAJO = "Diligenciar";
    public static final String ACCION_REGISTRAR_LISTA_CHEQUEO = "Registrar lista de chequeo";
    public static final String ACCION_REGISTRAR_TIEMPOS = "Registrar tiempos";
    public static final String ACCION_INSPECCION_ELECTRICA = "Inspección eléctrica";
    public static final String ACCION_REGISTRAR_LECTURAS_VARIABLES = "Registrar lecturas";
    public static final String ACCION_COMENTAR_SOLICITUD_SERVICIO = "Comentar";
    public static final String ACCION_EVALUAR_SOLICITUD_SERVICIO = "Evaluar";
    public static final String ACCION_RECIBIR_SOLICITUD_SERVICIO = "Recibir S.S.";
    public static final String ACCION_CREAR_SOLICITUD_SERVICIO = "Crear S.S.";
    public static final String ACCION_ACEPTAR_TRANSFERENCIA = "Aceptar transferencia";
    public static final String ACCION_REGISTRAR_BITACORA_EVENTO = "Evento";
    public static final String ACCION_REGISTRAR_BITACORA_OT = "O.T.";
    public static final String ACCION_REGISTRAR_BITACORA_SS = "S.S.";
    public static final String ACCION_ARCHIVOS = "Fotografías";
    public static final String ACCION_REGISTRAR_OT_BITACORA = "O.T. Bitácora";
    public static final String ACCION_ESTADO_INICIAL = "Estado inicial";
    public static final String ACCION_INFORME_TECNICO = "Informe técnico";
    public static final String ACCION_ENTRADA = "Entrada";
    public static final String ACCION_RECORRIDO_PLANTA_EXTERNA = "Recorrido planta externa";
    public static final String ACCION_INSTALACION_PLANTA_EXTERNA = "Instalación planta externa";
    public static final String ACCION_FIRMA_X_ENTIDAD = "Firma por entidad";
    public static final String ACCION_CREAR_ACTIVO = "Crear activo";
    public static final String ACCION_EDITAR_ACTIVO = "Editar activo";
    public static final String ACCION_ASOCIAR_CODIGO_QR_BARRAS_EQUIPO = "Asociar código de barras o QR - Equipo";
    public static final String ACCION_ASOCIAR_CODIGO_QR_BARRAS_INSTALACION_LOCATIVA = "Asociar código de barras o QR - Instalación locativa";
    public static final String MODULO_ESTADO_USUARIO = "Estado";
    public static final String ACCION_ESTADO_USUARIO = "Estado usuario";
    public static final String ACCION_ANS_OT = "Recalcular tiempo ANS";
    public static final String MODULO_ALMACEN = "Almacén";
    public static final String ACCION_TRASLADO_ALMACEN = "Traslado entre almacenes";
    public static final String MODULO_MOVIMIENTO = "Movimiento";
    public static final String ACCION_MOVIMIENTO = "Movimiento de inventario";
    public static final String MODULO_FALLAS = "Fallas";
    public static final String ACCION_CREAR_FALLA_OT = "Crear falla OT";
    public static final String ACCION_CREAR_FALLA_EQUIPO = "Crear falla equipo";
    public static final String ACCION_CREAR_FALLA_INSTALACION_LOCATIVA = "Crear falla instalación locativa";
    public static final String MODULO_CORREO = "Correo";
    public static final String ACCION_ENVIAR_CORREO = "Enviar correo";
    public static final String ACCION_REGISTRAR_PTI = "PTI";
    public static final String ACCION_REGISTRAR_EIR = "EIR";
    public static final String ACCION_INVENTARIO_EQUIPO = "Validar equipo";
    public static final String MODULO_INVENTARIO_EQUIPO = "Validar inventario en campo";

    public static final String MODULO_OT_LISTA_CHEQUEO = "OT-Lista de chequeo";

    @PrimaryKey
    private String UUID;
    private Cuenta cuenta;
    private Date creation;
    private Date send;
    private Date fecharespuesta;
    private String url;
    private String version;
    private String value;
    private String modulo;
    private String accion;
    private String estado;
    private String message;
    private String respuesta;
    private Long identidad;
    private String information;
    private boolean show;
    private int prioridad;

    public Transaccion() {
        this.show = true;
        this.prioridad = 10;
        this.UUID = java.util.UUID.randomUUID().toString();
        this.creation = Calendar.getInstance().getTime();
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public Date getCreation() {
        return creation;
    }

    public void setCreation(Date creation) {
        this.creation = creation;
    }

    public Date getSend() {
        return send;
    }

    public Transaccion setSend(Date send) {
        this.send = send;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getValue() {
        return value;
    }

    public <T> T getValue(Class<T> clazz) {
        return new Gson().fromJson(value, clazz);
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getEstado() {
        return estado;
    }

    public Transaccion setEstado(String estado) {
        this.estado = estado;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public Date getFecharespuesta() {
        return fecharespuesta;
    }

    public void setFecharespuesta(Date fecharespuesta) {
        this.fecharespuesta = fecharespuesta;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    public Long getIdentidad() {
        return identidad;
    }

    public void setIdentidad(Long identidad) {
        this.identidad = identidad;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(int prioridad) {
        this.prioridad = prioridad;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getModulo();
    }

    @Nullable
    @Override
    public String getSummary() {
        try {
            SimpleDateFormat formatter
                    = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return formatter.format(getCreation());
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    @Override
    public Integer getColorSummary() {
        return null;
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return getAccion();
    }

    @Nullable
    @Override
    public String getDescription() {
        String message = getMessage() != null && !getMessage().isEmpty() ? getMessage() : "";
        return getInformation() != null && !getInformation().isEmpty()
                ? getInformation() + "\n" + message
                : message;
    }

    @Override
    public List<Archivos> getChildren() {
        return new ArrayList<>();
    }

    @Nullable
    @Override
    public String getState() {
        return estado;
    }

    @Override
    public boolean isShowAction() {
        return false;
    }

    @Nullable
    @Override
    public String getActionName() {
        return null;
    }

    @Nullable
    @Override
    public OnInvoke<Transaccion> getAction(@NonNull Context context) {
        return null;
    }

    @Override
    public boolean compareTo(@NonNull Transaccion value) {
        return getUUID().equals(value.getUUID());
    }

    @NonNull
    @Override
    public String toString() {
        return "Transaccion{" +
                "UUID='" + UUID + '\'' +
                ", creation=" + creation +
                ", send=" + send +
                ", fecharespuesta=" + fecharespuesta +
                ", url='" + url + '\'' +
                ", version='" + version + '\'' +
                ", value='" + value + '\'' +
                ", modulo='" + modulo + '\'' +
                ", accion='" + accion + '\'' +
                ", estado='" + estado + '\'' +
                ", message='" + message + '\'' +
                ", respuesta='" + respuesta + '\'' +
                ", identidad=" + identidad +
                ", information='" + information + '\'' +
                ", show=" + show +
                ", prioridad=" + prioridad +
                '}';
    }
}