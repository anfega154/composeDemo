package com.mantum.cmms.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mantum.demo.R;
import com.mantum.cmms.entity.Actividad;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

public class ActividadMantenimientoFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Actividad_Mantenimiento";

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.fragment_actividad_mantenimiento, container, false);
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

    public void onLoad(@NonNull Actividad actividad) {
        if (getView() == null) {
            return;
        }

        TextView codigo = getView().findViewById(R.id.codigo);
        codigo.setText(actividad.getCodigo());

        TextView nombre = getView().findViewById(R.id.nombre);
        nombre.setText(actividad.getNombre());

        TextView requisitos = getView().findViewById(R.id.requisitos);
        requisitos.setText(actividad.getRequisitos());

        TextView porcentaje = getView().findViewById(R.id.porcentaje);
        porcentaje.setText(actividad.getPorcentaje() != null ? actividad.getPorcentaje().toString() : "");

        TextView descripcion = getView().findViewById(R.id.descripcion);
        descripcion.setText(actividad.getDescripcion());

        TextView tipo = getView().findViewById(R.id.tipo);
        tipo.setText(actividad.getTipo());

        TextView fechaUltimaEjecucion = getView().findViewById(R.id.fecha_ultima_ejecucion);
        fechaUltimaEjecucion.setText(actividad.getFechaultimaejecucion());

        TextView fechaProximaEjecucion = getView().findViewById(R.id.fecha_proxima_ejecucion);
        fechaProximaEjecucion.setText(actividad.getFechaproximaejecucion());

        TextView frecuencia = getView().findViewById(R.id.frecuencia);
        frecuencia.setText(actividad.getFrecuencia());
    }
}