package com.mantum.cmms.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mantum.demo.R;
import com.mantum.cmms.domain.BitacoraOrdenTrabajo;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

import static com.mantum.cmms.entity.parameter.UserPermission.REGISTRAR_PAROS;

/**
 * Bitacora O.T.
 */
public class BitacoraOrdenTrabajoFragment extends Mantum.Fragment {

    public static final int REQUEST_ACTION = 1211;

    public final static String KEY_TAB = "Bitacora_orden_trabajo";

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.fragment_bitacora_orden_trabajo, container, false);
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

    public void onRefresh(@NonNull BitacoraOrdenTrabajo value) {
        if (getView() == null) {
            return;
        }

        TextInputEditText token = getView().findViewById(R.id.token);
        token.setText(value.getToken());

        TextInputEditText code = getView().findViewById(R.id.code);
        code.setText(value.getCode());

        TextInputEditText type = getView().findViewById(R.id.activity);
        type.setText(value.getType());

        TextInputEditText date = getView().findViewById(R.id.creation_date);
        date.setText(value.getDate());

        TextInputEditText paro = getView().findViewById(R.id.paro);
        paro.setText(value.getTipoparo());

        if (UserPermission.check(getView().getContext(), REGISTRAR_PAROS, false)) {
            TextInputLayout paroContenedor = getView().findViewById(R.id.paro_contenedor);
            paroContenedor.setVisibility(View.VISIBLE);

            TextInputEditText state = getView().findViewById(R.id.state);
            if(value.getEstadoEquipo() != null && value.getEstadoEquipo() != getString(R.string.estado_equipo)) {
                state.setText(value.getEstadoEquipo());
                state.setVisibility(View.VISIBLE);
            }

        }

        TextInputEditText creationTime = getView().findViewById(R.id.creation_time);
        creationTime.setText(value.getTimestart());

        TextInputEditText creationEnd = getView().findViewById(R.id.creation_end);
        creationEnd.setText(value.getTimeend());

        TextView execution = getView().findViewById(R.id.execution);
        execution.setText(value.getExecutionrate());

        TextView descripcion = getView().findViewById(R.id.description);
        descripcion.setText(value.getDescription());

        TextView nota = getView().findViewById(R.id.nota);
        nota.setText(value.getNota());

        TextView observacionactivos = getView().findViewById(R.id.observacion_activos);
        observacionactivos.setText(value.getObservacionActivos());

        if (value.getHorashabilesdia() != null) {
            TextInputLayout horasHabilesContenedor = getView().findViewById(R.id.horas_habiles_contenedor);
            horasHabilesContenedor.setVisibility(View.VISIBLE);

            TextView horasHabiles = getView().findViewById(R.id.horas_habiles);
            horasHabiles.setText(String.valueOf(value.getHorashabilesdia()));
        }
    }
}