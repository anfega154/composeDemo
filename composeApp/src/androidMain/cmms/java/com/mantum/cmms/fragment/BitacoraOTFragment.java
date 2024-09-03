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

import com.mantum.R;
import com.mantum.cmms.domain.BitacoraOT;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

/**
 * O.T. Bitacora
 */
public class BitacoraOTFragment extends Mantum.Fragment {

    public static final int REQUEST_ACTION = 1212;

    public final static String KEY_TAB = "OT_Bitacora";

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ot_bitacora, container, false);
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

    public void onRefresh(@NonNull BitacoraOT value) {
        if (getView() == null) {
            return;
        }

        TextInputEditText token = getView().findViewById(R.id.token);
        token.setText(value.getToken());

        TextInputEditText entity = getView().findViewById(R.id.entity);
        entity.setText(value.getEntity());

        TextInputEditText paro = getView().findViewById(R.id.paro);
        paro.setText(value.getTipoparo());

        TextInputEditText date = getView().findViewById(R.id.creation_date);
        date.setText(value.getDate());

        TextInputEditText creationTime = getView().findViewById(R.id.creation_time);
        creationTime.setText(value.getTimestart());

        TextInputEditText creationEnd = getView().findViewById(R.id.creation_end);
        creationEnd.setText(value.getTimeend());

        TextView state = getView().findViewById(R.id.state);
        state.setText(value.getStateot());

        TextView execution = getView().findViewById(R.id.execution);
        execution.setText(value.getExecutionrate());

        TextView descripcion = getView().findViewById(R.id.description);
        descripcion.setText(value.getDescription());

        if (value.getHorashabilesdia() != null) {
            TextInputLayout horasHabilesContenedor = getView().findViewById(R.id.horas_habiles_contenedor);
            horasHabilesContenedor.setVisibility(View.VISIBLE);

            TextView horasHabiles = getView().findViewById(R.id.horas_habiles);
            horasHabiles.setText(String.valueOf(value.getHorashabilesdia()));
        }
    }
}