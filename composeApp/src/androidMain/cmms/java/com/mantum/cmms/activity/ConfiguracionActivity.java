package com.mantum.cmms.activity;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import androidx.annotation.Nullable;
import android.view.MenuItem;

import com.mantum.R;
import com.mantum.cmms.entity.parameter.UserParameter;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.cmms.service.SocketService;
import com.mantum.component.Mantum;

import static com.mantum.cmms.entity.parameter.UserPermission.BITACORA_GPS;
import static com.mantum.cmms.entity.parameter.UserPermission.BITACORA_GPS_SOLICITE;
import static com.mantum.cmms.entity.parameter.UserPermission.BLOCK_SEARCH_RESOURCES;
import static com.mantum.cmms.entity.parameter.UserPermission.IMAGE_GPS_SOLICITE;
import static com.mantum.cmms.entity.parameter.UserPermission.INCLUDE_IMAGE_GALLERY;
import static com.mantum.cmms.entity.parameter.UserPermission.MODULO_GESTION_SERVICIOS;
import static com.mantum.cmms.entity.parameter.UserPermission.MODULO_PANEL_GESTION_SERVICIO;
import static com.mantum.cmms.entity.parameter.UserPermission.REALIZAR_TRANSACCIONES_DIRECTAS;
import static com.mantum.cmms.entity.parameter.UserPermission.REGISTRAR_PAROS;
import static com.mantum.cmms.entity.parameter.UserPermission.VALIDAR_REGISTRO_BITACORA_OT;
import static com.mantum.cmms.entity.parameter.UserPermission.VALIDAR_REGISTRO_RUTA_TRABAJO;

public class ConfiguracionActivity extends Mantum.Activity {

    public static final String PREFERENCIA_SINCRONIZAR_REMOVE = "preference.sincronizar.remove";

    public static final String PREFERENCIA_UBICACION = "preference.ubicacion";

    public static final String PREFERENCIA_MENSAJE = "preference.mensajes";

    public static final String PREFERENCIA_TRANSACCION_DIRECTA = "preference_transaccion_directa";

    public static final String PREFERENCIA_NOTIFICACION_SATISFACTORIA = "preference_notificacion_satisfactoria";

    public static final String PREFERENCIA_NOTIFICACION_ERRONEA = "preference_notificacion_erronea";

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        includeBackButtonAndTitle(R.string.accion_configuration);

        ConfiguracionFragment configuracionFragment = new ConfiguracionFragment();
        configuracionFragment.setContext(this);
        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, configuracionFragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public static class ConfiguracionFragment extends PreferenceFragment {

        private Context context;

        @Override
        public Context getContext() {
            return context;
        }

        public void setContext(Context context) {
            this.context = context;
        }

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.configuracion);

            SocketService socket = SocketService.getInstance();
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference) findPreference("preference.socket.estado");
            checkBoxPreference.setChecked(socket.isLive());

            if (context != null) {

                checkBoxPreference = (CheckBoxPreference) findPreference("preference.requiere.ubicacion");
                checkBoxPreference.setChecked(UserPermission.check(context, BITACORA_GPS));

                checkBoxPreference = (CheckBoxPreference) findPreference("preference.solicitar.ubicacion");
                checkBoxPreference.setChecked(UserPermission.check(context, BITACORA_GPS_SOLICITE));

                checkBoxPreference = (CheckBoxPreference) findPreference("preference.solicitar.ubicacion.image");
                checkBoxPreference.setChecked(UserPermission.check(context, IMAGE_GPS_SOLICITE));

                checkBoxPreference = (CheckBoxPreference) findPreference("preference.galery");
                checkBoxPreference.setChecked(UserPermission.check(context, INCLUDE_IMAGE_GALLERY));

                checkBoxPreference = (CheckBoxPreference) findPreference("preference.validar.registro.rt");
                checkBoxPreference.setChecked(UserPermission.check(context, VALIDAR_REGISTRO_RUTA_TRABAJO));

                checkBoxPreference = (CheckBoxPreference) findPreference("preference.validar.registro.ot");
                checkBoxPreference.setChecked(UserPermission.check(context, VALIDAR_REGISTRO_BITACORA_OT));

                checkBoxPreference = (CheckBoxPreference) findPreference("preference.registrar.paros");
                checkBoxPreference.setChecked(UserPermission.check(context, REGISTRAR_PAROS));

                checkBoxPreference = (CheckBoxPreference) findPreference("preference.whitelist");
                checkBoxPreference.setChecked(Mantum.isWhitelist(context));

                checkBoxPreference = (CheckBoxPreference) findPreference("preference.gestion.servicios");
                checkBoxPreference.setChecked(UserPermission.check(context, MODULO_GESTION_SERVICIOS));

                checkBoxPreference = (CheckBoxPreference) findPreference("preference_panel_gestion_servicios");
                checkBoxPreference.setChecked(UserPermission.check(context, MODULO_PANEL_GESTION_SERVICIO));

                checkBoxPreference = (CheckBoxPreference) findPreference("preference_bloquear_busqueda_recursos");
                checkBoxPreference.setChecked(UserPermission.check(context, BLOCK_SEARCH_RESOURCES));

                checkBoxPreference = (CheckBoxPreference) findPreference("preference_transaccion_directa");
                checkBoxPreference.setChecked(UserPermission.check(context, REALIZAR_TRANSACCIONES_DIRECTAS));

                checkBoxPreference = (CheckBoxPreference) findPreference("campos_adicionales_crear_equipo");
                checkBoxPreference.setChecked(UserParameter.isTrue(context, UserParameter.CAMPOS_ADICIONALES_CREAR_EQUIPO));
            }
        }
    }
}