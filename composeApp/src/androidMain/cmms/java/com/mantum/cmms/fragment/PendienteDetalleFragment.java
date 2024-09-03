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
import com.mantum.cmms.entity.Pendiente;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

public class PendienteDetalleFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Detalle_Pendiente";

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_pendiente,
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

    public void onRefresh(@NonNull Pendiente value) {
        if (getView() == null) {
            return;
        }

        TextView code = getView().findViewById(R.id.codigo);
        code.setText(value.getCodigo());

        TextView date = getView().findViewById(R.id.fecha);
        date.setText(value.getFecha());

        TextView state = getView().findViewById(R.id.estado);
        state.setText(value.getEstado());

        TextView tiempoEstimado = getView().findViewById(R.id.tiempo_estimado);
        tiempoEstimado.setText(value.getTiempoestimadopmtto());

        TextView actividad = getView().findViewById(R.id.actividad);
        actividad.setText(value.getActividadpmtto());

        TextView description = getView().findViewById(R.id.descripcion);
        description.setText(value.getDescripcion());

        TextView criticidad = getView().findViewById(R.id.criticidad);
        criticidad.setText(value.getCriticidad());

        TextView personal = getView().findViewById(R.id.personal);
        personal.setText(value.getPersonal());
    }
}