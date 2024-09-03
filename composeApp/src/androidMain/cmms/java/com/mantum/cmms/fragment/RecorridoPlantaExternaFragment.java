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
import com.mantum.cmms.domain.RecorridoPlantaExterna;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.component.DatePicker;

public class RecorridoPlantaExternaFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Recorrido_Planta_Externa";

    private boolean action;

    private OnCompleteListener onCompleteListener;

    public RecorridoPlantaExternaFragment() {
        this.action = true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recorrido_planta_externa, container, false);

        if (action) {
            DatePicker fechaRecorrido = new DatePicker(view.getContext(), view.findViewById(R.id.fecha_recorrido));
            fechaRecorrido.setEnabled(true);
            fechaRecorrido.load();

            DatePicker fechaInicio = new DatePicker(view.getContext(), view.findViewById(R.id.fecha_inicio));
            fechaInicio.setEnabled(true);
            fechaInicio.load();

            DatePicker fechaFin = new DatePicker(view.getContext(), view.findViewById(R.id.fecha_fin));
            fechaFin.setEnabled(true);
            fechaFin.load();
        }

        return view;
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

    public RecorridoPlantaExterna getValue() {
        if (getView() == null) {
            return null;
        }

        RecorridoPlantaExterna recorridoPlantaExterna = new RecorridoPlantaExterna();

        TextView fechaRecorrido = getView().findViewById(R.id.fecha_recorrido);
        recorridoPlantaExterna.setFecharecorrido(fechaRecorrido.getText().toString());

        TextView direccion = getView().findViewById(R.id.direccion);
        recorridoPlantaExterna.setDireccion(direccion.getText().toString());

        TextView tramo = getView().findViewById(R.id.tramo);
        recorridoPlantaExterna.setTramo(tramo.getText().toString());

        TextView residente = getView().findViewById(R.id.residente);
        recorridoPlantaExterna.setResidente(residente.getText().toString());

        TextView empresa = getView().findViewById(R.id.empresa);
        recorridoPlantaExterna.setEmpresa(empresa.getText().toString());

        TextView telefono = getView().findViewById(R.id.telefono);
        recorridoPlantaExterna.setTelefono(telefono.getText().toString());

        TextView fechaInicio = getView().findViewById(R.id.fecha_inicio);
        recorridoPlantaExterna.setFechainicio(fechaInicio.getText().toString());

        TextView fechaFin = getView().findViewById(R.id.fecha_fin);
        recorridoPlantaExterna.setFechafin(fechaFin.getText().toString());

        TextView tiempoEstimado = getView().findViewById(R.id.tiempo_estimado);
        recorridoPlantaExterna.setTiempoestimado(tiempoEstimado.getText().toString());

        TextView porcentaje = getView().findViewById(R.id.porcentaje);
        recorridoPlantaExterna.setPorcentajeobra(porcentaje.getText().toString());

        TextView estado = getView().findViewById(R.id.estado);
        recorridoPlantaExterna.setEstado(estado.getText().toString());

        TextView observacion = getView().findViewById(R.id.observacion);
        recorridoPlantaExterna.setObservaciones(observacion.getText().toString());

        return recorridoPlantaExterna;
    }

    public RecorridoPlantaExternaFragment setAction(@SuppressWarnings("SameParameterValue") boolean action) {
        this.action = action;
        return this;
    }

    public void onView(RecorridoPlantaExterna recorridoPlantaExterna) {
        if (getView() == null) {
            return;
        }

        TextView fechaRecorrido = getView().findViewById(R.id.fecha_recorrido);
        fechaRecorrido.setEnabled(action);
        fechaRecorrido.setText(recorridoPlantaExterna.getFecharecorrido());

        TextView direccion = getView().findViewById(R.id.direccion);
        direccion.setEnabled(action);
        direccion.setText(recorridoPlantaExterna.getDireccion());

        TextView tramo = getView().findViewById(R.id.tramo);
        tramo.setEnabled(action);
        tramo.setText(recorridoPlantaExterna.getTramo());

        TextView residente = getView().findViewById(R.id.residente);
        residente.setEnabled(action);
        residente.setText(recorridoPlantaExterna.getResidente());

        TextView empresa = getView().findViewById(R.id.empresa);
        empresa.setEnabled(action);
        empresa.setText(recorridoPlantaExterna.getEmpresa());

        TextView telefono = getView().findViewById(R.id.telefono);
        telefono.setEnabled(action);
        telefono.setText(recorridoPlantaExterna.getTelefono());

        TextView fechaInicio = getView().findViewById(R.id.fecha_inicio);
        fechaInicio.setEnabled(action);
        fechaInicio.setText(recorridoPlantaExterna.getFechainicio());

        TextView fechaFin = getView().findViewById(R.id.fecha_fin);
        fechaFin.setEnabled(action);
        fechaFin.setText(recorridoPlantaExterna.getFechafin());

        TextView tiempoEstimado = getView().findViewById(R.id.tiempo_estimado);
        tiempoEstimado.setEnabled(action);
        tiempoEstimado.setText(recorridoPlantaExterna.getTiempoestimado());

        TextView porcentaje = getView().findViewById(R.id.porcentaje);
        porcentaje.setEnabled(action);
        porcentaje.setText(recorridoPlantaExterna.getPorcentajeobra());

        TextView estado = getView().findViewById(R.id.estado);
        estado.setEnabled(action);
        estado.setText(recorridoPlantaExterna.getEstado());

        TextView observacion = getView().findViewById(R.id.observacion);
        observacion.setEnabled(action);
        observacion.setText(recorridoPlantaExterna.getObservaciones());
    }
}