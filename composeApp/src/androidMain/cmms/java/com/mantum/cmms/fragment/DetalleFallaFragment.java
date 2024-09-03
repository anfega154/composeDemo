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
import com.mantum.cmms.entity.Falla;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

public class DetalleFallaFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Falla_Equipo";

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_falla_equipo, container, false);
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

    public void onLoad(@NonNull Falla falla) {
        if (getView() == null) {
            return;
        }

        TextView nombre = getView().findViewById(R.id.nombre);
        nombre.setText(falla.getResumen());

        TextView descripcion = getView().findViewById(R.id.descripcion);
        descripcion.setText(falla.getDescripcion());

        TextView tipoFalla = getView().findViewById(R.id.tipo_falla);
        tipoFalla.setText(falla.getIdtipofalla());

        TextView entidad = getView().findViewById(R.id.entidad);
        entidad.setText(falla.getEntidad());

        TextView am = getView().findViewById(R.id.am);
        am.setText(falla.getAm());

        TextView ot = getView().findViewById(R.id.ot);
        ot.setText(falla.getOt());

        TextView fechaInicio = getView().findViewById(R.id.fecha_inicio);
        fechaInicio.setText(falla.getFechainicio());

        TextView fechaFin = getView().findViewById(R.id.fecha_fin);
        if (falla.getFechafin() != null && falla.getHorafin() != null) {
            fechaFin.setText(String.format("%s %s", falla.getFechafin(), falla.getHorafin()));
        }
    }
}
