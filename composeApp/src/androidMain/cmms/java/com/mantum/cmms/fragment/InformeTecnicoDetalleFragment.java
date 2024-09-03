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
import com.mantum.cmms.entity.InformeTecnico;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

public class InformeTecnicoDetalleFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Informe_Tecnico_Detalle";

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_informe_tecnico,
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
        return context.getString(R.string.tab_informe_tecnico);
    }

    public void onRefresh(@NonNull InformeTecnico value) {
        if (getView() == null) {
            return;
        }

        TextView activity = getView().findViewById(R.id.activity);
        activity.setText(value.getActividades());

        TextView recommendations = getView().findViewById(R.id.recommendations);
        recommendations.setText(value.getRecomendaciones());
    }
}