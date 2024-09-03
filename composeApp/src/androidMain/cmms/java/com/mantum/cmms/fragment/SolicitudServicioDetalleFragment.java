package com.mantum.cmms.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mantum.R;
import com.mantum.cmms.entity.Sitio;
import com.mantum.cmms.entity.SolicitudServicio;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

public class SolicitudServicioDetalleFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Detalle_Solicitud_Servicio";

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_solicitud_servicio,
                container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onCompleteListener.onComplete(KEY_TAB);
    }

    @Override
    public void onAttach(Context context) {
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

    public void onRefresh(@NonNull SolicitudServicio value) {
        if (getView() == null) {
            return;
        }

        TextView codigo = getView().findViewById(R.id.codigo);
        codigo.setText(value.getCodigo());

        TextView fecha = getView().findViewById(R.id.fecha);
        fecha.setText(value.getFecha());

        TextView solicitante = getView().findViewById(R.id.solicitante);
        solicitante.setText(value.getSolicitante());

        TextView tipo = getView().findViewById(R.id.tipo);
        tipo.setText(value.getTipo());

        TextView prioridad = getView().findViewById(R.id.prioridad);
        prioridad.setText(value.getPrioridad());

        TextView area = getView().findViewById(R.id.area);
        area.setText(value.getArea());

        TextView fechaesperada = getView().findViewById(R.id.fecha_esperada);
        fechaesperada.setText(value.getFechaesperada());

        TextView fechavencimiento = getView().findViewById(R.id.fecha_vencimiento);
        fechavencimiento.setText(value.getFechavencimiento());

        TextView estado = getView().findViewById(R.id.estado);
        estado.setText(value.getEstado());

        TextView descripcion = getView().findViewById(R.id.descripcion);
        descripcion.setText(value.getDescripcion());

        Sitio sitio = value.getSitio();
        if (sitio != null) {
            TextView cliente = getView().findViewById(R.id.cliente);
            cliente.setText(sitio.getCliente());

            TextView direccion = getView().findViewById(R.id.direccion);
            direccion.setText(sitio.getDireccion());

            TextView planta = getView().findViewById(R.id.planta);
            planta.setText(sitio.getPlanta());

            TextView telefono = getView().findViewById(R.id.telefono);
            telefono.setText(sitio.getTelefono());
        }
    }
}