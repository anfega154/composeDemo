package com.mantum.cmms.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.mantum.demo.R;
import com.mantum.cmms.domain.RegistrarTiempoConsolidado;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.component.DatePicker;
import com.mantum.component.component.TimePicker;

import java.util.UUID;

public class RegistrarTiemposFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Registrar_Tiempos";

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_registrar_tiempos,
                container, false);

        DatePicker fechaInicio
                = new DatePicker(view.getContext(), view.findViewById(R.id.fecha_inicio));
        fechaInicio.setEnabled(true);
        fechaInicio.load();

        DatePicker fechaFin
                = new DatePicker(view.getContext(), view.findViewById(R.id.fecha_fin));
        fechaFin.setEnabled(true);
        fechaFin.load();

        TimePicker horaLlegada
                = new TimePicker(view.getContext(), view.findViewById(R.id.hora_llegada));
        horaLlegada.setEnabled(true);
        horaLlegada.load();

        TimePicker horaSalida
                = new TimePicker(view.getContext(), view.findViewById(R.id.hora_salida));
        horaSalida.setEnabled(true);
        horaSalida.load();

        TimePicker horaInicio
                = new TimePicker(view.getContext(), view.findViewById(R.id.hora_inicio));
        horaInicio.setEnabled(true);
        horaInicio.load();

        TimePicker horaFin
                = new TimePicker(view.getContext(), view.findViewById(R.id.hora_fin));
        horaFin.setEnabled(true);
        horaFin.load();

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

    public void onStart(@NonNull OrdenTrabajo value) { }

    @Nullable
    public RegistrarTiempoConsolidado getValue(Long idot) {
        if (getView() == null) {
            return null;
        }

        RegistrarTiempoConsolidado consolidado = new RegistrarTiempoConsolidado();
        consolidado.setIdot(idot);
        consolidado.setToken(UUID.randomUUID().toString());

        EditText fechaInicio = getView().findViewById(R.id.fecha_inicio);
        consolidado.setFechaInicio(fechaInicio.getText().toString());

        EditText fechaFin = getView().findViewById(R.id.fecha_fin);
        consolidado.setFechaFin(fechaFin.getText().toString());

        EditText horaLlegada = getView().findViewById(R.id.hora_llegada);
        consolidado.setHoraLlegada(horaLlegada.getText().toString());

        EditText horaSalida = getView().findViewById(R.id.hora_salida);
        consolidado.setHoraSalida(horaSalida.getText().toString());

        EditText horaInicio = getView().findViewById(R.id.hora_inicio);
        consolidado.setHoraInicio(horaInicio.getText().toString());

        EditText horaFin = getView().findViewById(R.id.hora_fin);
        consolidado.setHoraFin(horaFin.getText().toString());

        EditText tiempoTotal = getView().findViewById(R.id.tiempo_total);

        EditText tiempoCliente = getView().findViewById(R.id.tiempo_cliente);

        EditText tiempoPorL3 = getView().findViewById(R.id.tiempo_por_l3);

        EditText tiempoFactoresExternos = getView().findViewById(R.id.tiempo_factores_externos);

        consolidado.setTipos(tiempoTotal.getText().toString(), tiempoCliente.getText().toString(),
                tiempoPorL3.getText().toString(), tiempoFactoresExternos.getText().toString());

        return consolidado;
    }
}
