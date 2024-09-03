package com.mantum.cmms.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.R;
import com.mantum.cmms.entity.Tarea;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

public class DetalleTareaFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Tareas";

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_tarea, container, false);
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

    public void onLoad(@NonNull Tarea tarea, boolean tareaEntidadOt) {
        if (getView() == null) {
            return;
        }

        TextView codigo = getView().findViewById(R.id.codigo);
        codigo.setText(tarea.getCodigo());

        TextView nombre = getView().findViewById(R.id.nombre);
        nombre.setText(tarea.getTarea());

        TextView descripcion = getView().findViewById(R.id.descripcion);
        descripcion.setText(tarea.getDescripcion());

        TextView tiempoBaseTexto = getView().findViewById(R.id.tiempo_base_texto);
        tiempoBaseTexto.setText(tarea.getTiempobasetexto());

        TextView tiempoBaseMinutos = getView().findViewById(R.id.tiempo_base_minutos);
        tiempoBaseMinutos.setText(String.valueOf(tarea.getTiempobase()));

        TextView critica = getView().findViewById(R.id.critica);
        critica.setText(tarea.isCritica() ? "Sí" : "No");

        LinearLayout layoutEjecutada = getView().findViewById(R.id.layout_ejecutada);
        layoutEjecutada.setVisibility(tareaEntidadOt ? View.VISIBLE : View.GONE);

        TextView ejecutada = getView().findViewById(R.id.ejecutada);
        ejecutada.setText(tarea.isEjecutada() ? "Sí" : "No");

        TextView ultimoEjecutor = getView().findViewById(R.id.ultimo_ejecutor);
        ultimoEjecutor.setText(tarea.getEjecutor());

        TextView ultimaFechaEjecucion = getView().findViewById(R.id.ultima_fecha_ejecucion);
        ultimaFechaEjecucion.setText(tarea.getFechaejecucion());
    }
}
