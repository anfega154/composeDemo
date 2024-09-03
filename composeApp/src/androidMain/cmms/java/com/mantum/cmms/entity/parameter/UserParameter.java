package com.mantum.cmms.entity.parameter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;

import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Modulos;
import com.mantum.cmms.util.Version;

import io.realm.RealmObject;

public class UserParameter extends RealmObject {

    public static final int NUMBER_SECONDS_TIMER_GPS = 1000 * 30; // 30 Segundos

    public static final String SECONDS_TIMER_GPS = "milisegundosGpsCMMSApp";
    public static final String REGISTRO_TIEMPOS_OT = "bMostrarAccionRegistroTiemposAPP";
    public static final String REMOVER_OT = "bMostrarAccionRemoverOTAPP";
    public static final String DESCARGAR_ENTIDADES = "bPuedeDescargarEntidadesDesdeApp";
    public static final String FIRMA_OBLIGATORIA = "bObligatorioFirmaTerminarOT";
    public static final String DESHABILITAR_OBSERVACIONES_LISTA_CHEQUEO = "bDeshabilitarObservacionesLC";
    public static final String RANGO_TIEMPO_ANS = "aRangosConteoAlertaTiemposANS";
    public static final String MOSTRAR_SITIO_SS = "iMostrarSitioSS"; // Mostrar panel de sitios en OT e informacion de sitio en OT asignada
    public static final String SALIDA_RUTA = "SalidaRutaAPP"; //indica el id para el movimiento de salida RT
    public static final String ENTRADA_OT = "EntradaOTAPP"; //indica el id para el movimiento de entrada OT
    public static final String SALIDA_OT = "SalidaOTAPP"; //indica el id para el movimiento de salida OT
    public static final String MOSTRAR_MENSAJE_ACEPTAR_TRANSFERENCIA = "bMostrarMensajeAceptarTransferencia";
    public static final String MENSAJE_ACEPTAR_TRANSFERENCIA = "mensajeAceptarTransferencia";
    public static final String TIEMPO_PERSONALIZADO_ENVIO_TRANSACCIONES = "iTiempoPersonalizadoEnMinutosParaEnvioTransaccionesDesdeAPP";
    public static final String ASOCIAR_ENTIDAD_BITACORA_EVENTO = "bMostrarSeleccionEntidadEventoBitacora";
    public static final String FAMILIA_PARA_EQUIPO_REQUERIDA = "bFamiliaEquipoObligatorio";
    public static final String SERIE_PARA_EQUIPO_REQUERIDA = "bSerieEquipoObligatorio";
    public static final String MODELO_PARA_EQUIPO_REQUERIDO = "bModeloEquipoObligatorio";
    public static final String MARCA_PARA_EQUIPO_REQUERIDO = "bMarcaEquipoObligatorio";
    public static final String UBICACION_PARA_EQUIPO_REQUERIDA = "bUbicacionEquipoObligatorio";
    public static final String CAMPOS_ADICIONALES_CREAR_EQUIPO = "bCamposAdicionalesCrearEquipoAPP";

    // Parámetros de la App
    public static final String ULTIMO_SERVIDOR = "ultimoServidor";
    public static final String ULTIMO_BASE_NAME = "ultimoBaseName";
    public static final String ULTIMA_ACTUALIZACION_INSPECCION_PROGRAMADA = "ultima_actualizacion_inspeccion";

    public static final String URL_MANTUM_FUTURE = "URL_FUTURE";

    public static final String VER_ADIONALES_FIRMA = "bMostrarSeccionesAdicionalesFirma_APP";

    private Cuenta cuenta;
    private String name;
    private String value;

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public String getName() {
        return name;
    }

    public void setName(String label) {
        this.name = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @NonNull
    public static Boolean isTrue(@NonNull Context context, @NonNull String parameter) {
        String value = getValue(context, parameter);
        return ("true".equals(value) || "1".equals(value) || "t".equals(value));
    }

    @Nullable
    public static String getValue(@NonNull Context context, @NonNull String parameter) {
        Database database = new Database(context);
        try {
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception("La cuenta de usuario no esta definida");
            }

            UserParameter userParameter = database.where(UserParameter.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .equalTo("name", parameter)
                    .findFirst();

            String response = userParameter != null
                    ? userParameter.getValue()
                    : null;
            database.close();
            return response;
        } catch (Exception e) {
            database.close();
            return null;
        }
    }

    public static void saveParameter(Context context, String parameter, String value) {
        Database database = new Database(context);
        database.executeTransaction(self -> {
            UserParameter userParameter = self.where(UserParameter.class)
                    .equalTo("name", parameter)
                    .findFirst();

            if (userParameter == null) {
                userParameter = new UserParameter();
                userParameter.setName(parameter);
                userParameter.setValue(value);
                self.insert(userParameter);
            } else {
                userParameter.setValue(value);
            }
        });
    }

    public static boolean getModuloClienteCMMS(Context context) {
        Database database = new Database(context);
        try {
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception("La cuenta de usuario no esta definida");
            }

            Modulos modulo = database.where(Modulos.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findFirst();

            if (modulo == null) {
                throw new Exception("El módulo no existe");
            }

            database.close();
            return modulo.isClientecmms();
        } catch (Exception e) {
            database.close();
            return false;
        }
    }
}