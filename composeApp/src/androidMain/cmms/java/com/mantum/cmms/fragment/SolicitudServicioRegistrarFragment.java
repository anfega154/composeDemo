package com.mantum.cmms.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mantum.demo.R;
import com.mantum.cmms.domain.SolicitudServicioRegistrar;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

public class SolicitudServicioRegistrarFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Crear_Solicitud_Servicio";

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.fragment_solicitud_servicio_crear, container, false);
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

    public void onView(@NonNull SolicitudServicioRegistrar solicitudServicioRegistrar) {
        if (getView() == null) {
            return;
        }

        TextInputEditText entidad = getView().findViewById(R.id.entidad);
        entidad.setText(solicitudServicioRegistrar.getNombreEntidad());

        TextInputEditText fechaInicio = getView().findViewById(R.id.fecha_inicio);
        fechaInicio.setText(solicitudServicioRegistrar.getFechaInicio());

        TextInputEditText horaInicio = getView().findViewById(R.id.hora_inicio);
        horaInicio.setText(solicitudServicioRegistrar.getHoraInicio());

        TextInputEditText tipo = getView().findViewById(R.id.tipo);
        tipo.setText(solicitudServicioRegistrar.getTipo());

        TextInputEditText prioridad = getView().findViewById(R.id.prioridad);
        prioridad.setText(solicitudServicioRegistrar.getPrioridad());

        TextInputEditText descripcion = getView().findViewById(R.id.descripcion);
        descripcion.setText(solicitudServicioRegistrar.getDescripcion());
    }
}
