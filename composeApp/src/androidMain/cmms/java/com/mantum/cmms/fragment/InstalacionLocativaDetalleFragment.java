package com.mantum.cmms.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.mantum.demo.R;
import com.mantum.cmms.activity.BitacoraActivity;
import com.mantum.cmms.activity.SolicitudServicioActivity;
import com.mantum.cmms.entity.InstalacionLocativa;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

public class InstalacionLocativaDetalleFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Detalle_Instalacion_Locativa";

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_detalle_instalacion_locativa, container, false);

        FloatingActionButton register = view.findViewById(R.id.registrar_solicitud_servicio);
        register.setOnClickListener(v -> {
            TextView id = view.findViewById(R.id.id);
            TextView codigo = view.findViewById(R.id.codigo);
            TextView nombre = view.findViewById(R.id.nombre);

            String nombreMostrar = String.format(
                    "%s | %s", codigo.getText().toString(), nombre.getText().toString());

            if (id.getText() == null || id.getText().toString().isEmpty()) {
                Snackbar.make(v, R.string.error_identificador_entidad, Snackbar.LENGTH_LONG)
                        .show();
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putLong(SolicitudServicioActivity.KEY_ID, Long.parseLong(id.getText().toString()));
            bundle.putString(SolicitudServicioActivity.KEY_NAME, nombreMostrar);
            bundle.putString(SolicitudServicioActivity.KEY_TYPE, InstalacionLocativa.SELF);

            Intent intent = new Intent(view.getContext(), SolicitudServicioActivity.class);
            intent.putExtras(bundle);

            startActivity(intent);
        });

        if (Version.check(view.getContext(), 7)) {
            if (!UserPermission.check(view.getContext(), UserPermission.SOLICITUD_SERVICIO_CREAR)) {
                register.setVisibility(View.INVISIBLE);
            }
        }

        FloatingActionButton bitacora = view.findViewById(R.id.registrar_ot_bitacora);
        bitacora.setOnClickListener(v -> {
            TextView id = view.findViewById(R.id.id);
            TextView codigo = view.findViewById(R.id.codigo);
            TextView nombre = view.findViewById(R.id.nombre);

            String nombreMostrar = String.format(
                    "%s | %s", codigo.getText().toString(), nombre.getText().toString());

            if (id.getText() != null && id.getText().toString().isEmpty()) {
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putLong(BitacoraActivity.KEY_ID, Long.parseLong(id.getText().toString()));
            bundle.putString(BitacoraActivity.KEY_CODIGO, nombreMostrar);
            bundle.putString(BitacoraActivity.KEY_TIPO_ENTIDAD, InstalacionLocativa.SELF);
            bundle.putInt(BitacoraActivity.KEY_TIPO_BITACORA, BitacoraActivity.OT_BITACORA);

            Intent intent = new Intent(view.getContext(), BitacoraActivity.class);
            intent.putExtras(bundle);

            startActivity(intent);
        });

        FloatingActionButton varUbicacion = view.findViewById(R.id.ver_ubicacion);
        varUbicacion.setOnClickListener(v -> {
            TextView gmapa = view.findViewById(R.id.gmap);
            String key = gmapa.getText().toString();
            if (key.isEmpty()) {
                Snackbar.make(view, R.string.ruta_sin_definir, Snackbar.LENGTH_LONG)
                        .show();
                return;
            }
            Mantum.goGoogleMap(view.getContext(), key);
        });

        if (Version.check(view.getContext(), 12)) {
            LinearLayout qrcode = view.findViewById(R.id.qrcode_contenedor);
            qrcode.setVisibility(View.VISIBLE);

            LinearLayout barcode = view.findViewById(R.id.barcode_contenedor);
            barcode.setVisibility(View.VISIBLE);

            LinearLayout nfc = view.findViewById(R.id.nfc_contenedor);
            nfc.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onCompleteListener.onComplete(KEY_TAB);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        onCompleteListener = (OnCompleteListener) context;
    }

    @NonNull
    @Override
    public String getKey() {
        return KEY_TAB;
    }

    @NonNull
    @Override
    public String getTitle(@NonNull Context context) {
        return context.getString(R.string.tab_general);
    }

    public void onLoad(@NonNull InstalacionLocativa value) {
        if (getView() == null) {
            return;
        }

        TextView id = getView().findViewById(R.id.id);
        id.setText(String.valueOf(value.getId()));

        TextView gmap = getView().findViewById(R.id.gmap);
        gmap.setText(value.getGmap());

        TextView codigo = getView().findViewById(R.id.codigo);
        codigo.setText(value.getCodigo());

        TextView nombre = getView().findViewById(R.id.nombre);
        nombre.setText(value.getNombre());

        TextView instalacionProceso = getView().findViewById(R.id.instalacion_padre);
        instalacionProceso.setText(value.getInstalacionpadre());

        TextView instalacionLocativa = getView().findViewById(R.id.tipo_instalacion);
        instalacionLocativa.setText(value.getTipodeinstalacion());

        TextView familia1 = getView().findViewById(R.id.familia_one);
        familia1.setText(value.getFamilia1());

        TextView familia2 = getView().findViewById(R.id.familia_two);
        familia2.setText(value.getFamilia2());

        TextView familia3 = getView().findViewById(R.id.familia_three);
        familia3.setText(value.getFamilia3());

        TextView estado = getView().findViewById(R.id.estado);
        estado.setText(value.getEstado());

        TextView criticidad = getView().findViewById(R.id.criticidad);
        criticidad.setText(value.getCriticidad());

        TextView direccion = getView().findViewById(R.id.direccion);
        direccion.setText(value.getDireccion());

        TextView nfc = getView().findViewById(R.id.nfc);
        nfc.setText(value.getNfctoken());

        TextView barcode = getView().findViewById(R.id.barcode);
        barcode.setText(value.getBarcode());

        TextView qrcode = getView().findViewById(R.id.qrcode);
        qrcode.setText(value.getQrcode());
    }

    public void setViewBarcode(@NonNull String value) {
        if (getView() == null) {
            return;
        }

        TextView barcode = getView().findViewById(R.id.barcode);
        barcode.setText(value);
    }

    public void setViewQrCode(@NonNull String value) {
        if (getView() == null) {
            return;
        }

        TextView qrcode = getView().findViewById(R.id.qrcode);
        qrcode.setText(value);
    }
}
