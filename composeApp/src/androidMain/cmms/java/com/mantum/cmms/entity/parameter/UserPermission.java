package com.mantum.cmms.entity.parameter;

import android.content.Context;

import androidx.annotation.NonNull;

import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class UserPermission extends RealmObject {

    public static final String SOLICITUD_SERVICIO_CREAR = "solicitudservicios_crear";

    public static final String BITACORA_GPS = "bitacoras_gps"; // REQUERIR USO DE GPS

    public static final String BITACORA_GPS_SOLICITE = "bitacoras_sgps"; // SOLICITAR GPS

    public static final String IMAGE_GPS_ENABLE = "adjuntos_gpsencendido";

    public static final String LAST_KNOWN_LOCATION = "bitacoras_ultimaposicionconocidagps";

    public static final String BLOCK_SEARCH_RESOURCES = "bitacoras_bloquearbuscarrecursosapp";

    public static final String IMAGE_GPS_SOLICITE = "adjuntos_solicitargps";

    public static final String INCLUDE_IMAGE_GALLERY = "adjuntos_agregarimagenesgaleria";

    public static final String MODULO_GESTION_SERVICIOS = "modulo_gestionservicios";

    public static final String MODULO_PANEL_GESTION_SERVICIO = "modulo_panelgestionservicios";

    public static final String VALIDAR_REGISTRO_RUTA_TRABAJO = "bitacoras_requerirusoqrregistrort";

    public static final String VALIDAR_REGISTRO_BITACORA_OT = "bitacoras_requerirusoqrregistrobitacoraot";

    public static final String REGISTRAR_PAROS = "bitacoras_registroparosbitacoraapp";

    public static final String PERMITE_MODIFICAR_NOMBRE_IMAGEN = "rutas_permitirmodificarnombrefotosapp";

    public static final String USUARIO_ALMACENISTA = "usuario_almacenista";

    public static final String LISTA_CHEQUEO = "diligenciar_lc";

    public static final String ENTREGAR_ALMACEN = "entregar_almacen";
    public static final String RECIBIR_ALMACEN = "recibir_almacen";
    public static final String TRANSFERIR_ITEMS = "transferir_items";

    public static final String VALIDAR_QR_SITIO = "adjuntos_validarentidad";

    public static final String AGREGAR_PERSONAL_BITACTORA = "bitacoras_registrarBitacoraGeneral";

    public static final String REALIZAR_MOVIMIENTO_GENERAL = "adjuntos_realizarmovimientos";
    public static final String REALIZAR_MOVIMIENTO_RT = "rutas_realizarmovimientos";
    public static final String REALIZAR_MOVIMIENTO_OT = "ordenestrabajo_realizarmovimientos";
    public static final String REALIZAR_INSTALACION_RETIRO = "ordenestrabajo_realizarinstalaciones";

    public static final String REALIZAR_TRANSACCIONES_DIRECTAS = "adjuntos_transaccionesdirectas";
    public static final String TRANSACCIONES_DIRECTAS_MOVIMIENTOS = "adjuntos_movimientosdirectos";

    public static final String VER_CANTIDAD_ASIGNADA_RECURSO_OT = "ordenestrabajo_cantasignadarec";

    public static final String CREAR_EQUIPO = "equipos_crearequipo";
    public static final String EDITAR_EQUIPO = "equipos_editarequipo";
    public static final String GENERAR_ROTULO = "equipos_generarrotulo";
    public static final String MARCACION_EQUIPOS = "adjuntos_asociarnfcqrbarcode";

    public static final String REGISTRAR_LECTURA = "adjuntos_registrarlectura";

    public static final String BUSQUEDA_OTS_POR_YARDAS = "adjuntos_busquedaotsporyardas";

    public static final String ENVIAR_IMAGENES_POR_CORREO = "adjuntos_enviarimagenescorreo";

    public static final String REGISTRAR_FALLAS = "adjuntos_registrarfallas";
    public static final String GESTIONAR_FALLAS_BITACORA = "adjuntos_gestionarfallasbitacora";

    public static final String EXTENDER_TIEMPO_SINCRONIZACION_AUTOMATICA = "adjuntos_extendertiemposincronizacionautomatica";

    //Por defecto, si estos permisos son nulos, se muestran las acciones o listados
    public static final String VER_LISTADO_ORDENES_DE_TRABAJO = "adjuntos_vertabordenesdetrabajoapp";
    public static final String VER_LISTADO_RUTAS_DE_TRABAJO = "adjuntos_vertabrutasdetrabajoapp";
    public static final String VER_LISTADO_SOLICITUDES_DE_SERVICIO = "adjuntos_vertabsolicitudservicioapp";
    public static final String VER_LISTADO_PENDIENTES = "adjuntos_vertabspendientesapp";

    public static final String MENU_LATERAL_VER_OPCION_CALENDARIO = "adjuntos_menulateralveropcioncalendario";
    public static final String MENU_LATERAL_VER_OPCION_MI_MOCHILA = "adjuntos_menulateralveropcionmochila";
    public static final String MENU_LATERAL_VER_OPCION_EVENTOS = "adjuntos_menulateralveropcioneventos";
    public static final String MENU_LATERAL_VER_OPCION_VALIDAR_INGRESO_QR = "adjuntos_menulateralveropcionvalidaringreso";
    public static final String ACEPTAR_TRANSFERENCIA = "adjuntos_aceptartransferencia";
    public static final String AUTORIZACION_ACCESO = "adjuntos_accesoautorizacion";
    public static final String DESCARGAR_AUTORIZACIONES = "adjuntos_descargaracutorizaciones";

    public static final String DILIGENCIAR_LC_ENTIDAD = "adjuntos_diligenciarlcentidad";
    public static final String DESCARGAR_LC_ENTIDAD = "adjuntos_descargarlcentidad";

    public static final String RECIBIR_OT = "recibirot";
    public static final String TERMINAR_OT = "terminarot";
    public static final String REGISTRAR_FIRMAS_OT = "ordenestrabajo_registrarfirmaotapp";
    public static final String FINALIZACION_DIRECTA_OT_APP = "adjuntos_finalizaciondirectaotapp";

    public static final String VISUALIZAR_BOTON_FIRMAR_DIGITAL = "adjuntos_visualizarbotonfirmadigitalapp";
    public static final String VISUALIZAR_BOTON_PENDIENTES = "adjuntos_visualizarbotonpendientesapp";
    public static final String VISUALIZAR_BOTON_REGISTRAR_LECTURAS_BITACORA = "adjuntos_visualizarbotonregistrarlecturasbitacoraapp";
    public static final String VISUALIZAR_BOTON_ADJUNTAR_ARCHIVOS = "adjuntos_visualizarbotonadjuntosfotografiasapp";
    public static final String VISUALIZAR_BOTON_EJECUTAR_TAREAS = "tareasam_registrotareasapp";

    public static final String BITACORA_REGISTRO_OT_BITACORA = "bitacoras_registrootbitacora";

    public static final String GESTION_PTI_EIR = "cem_accesoaprogramacion";

    public static final String VALIDARINVENTARIO_ACCESO = "validarinventario_accesoinventarios";

    @PrimaryKey
    private String UUID;

    private Cuenta cuenta;

    private String name;

    private Boolean value;

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

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getValue() {
        return this.value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }

    public static boolean check(@NonNull Context context, @NonNull String permission) {
        Database database = new Database(context);
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            database.close();
            return false;
        }

        UserPermission userPermission = database.where(UserPermission.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("name", permission)
                .findFirst();

        if (userPermission == null) {
            database.close();
            return false;
        }

        boolean respuesta = userPermission.getValue();
        database.close();

        return respuesta;
    }

    public static boolean check(@NonNull Context context, @NonNull String permission, boolean defaultValue) {
        Database database = new Database(context);
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            database.close();
            return defaultValue;
        }

        UserPermission userPermission = database.where(UserPermission.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("name", permission)
                .findFirst();

        if (userPermission == null) {
            database.close();
            return defaultValue;
        }

        boolean respuesta = userPermission.getValue();
        database.close();

        return respuesta;
    }
}