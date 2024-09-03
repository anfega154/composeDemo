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
import com.mantum.cmms.entity.EstadoInicial;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

public class EstadoInicialDetalleFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Estado_Inicial_Detalle";

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_estado_inicial,
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
        return context.getString(R.string.tab_estado_inicial);
    }

    public void onRefresh(@NonNull EstadoInicial value) {
        if (getView() == null) {
            return;
        }

        TextView tipo = getView().findViewById(R.id.tipo);
        tipo.setText(value.getTipofalla());

        TextView denominacion = getView().findViewById(R.id.denominacion);
        denominacion.setText(value.getDenominacion());

        TextView marca = getView().findViewById(R.id.marca);
        marca.setText(value.getMarca());

        TextView numero = getView().findViewById(R.id.numero);
        numero.setText(value.getNumeroproducto());

        TextView serial = getView().findViewById(R.id.serial);
        serial.setText(value.getNumeroserial());

        TextView caracteristicas = getView().findViewById(R.id.caracteristicas);
        caracteristicas.setText(value.getCaracteristicas());

        TextView estado = getView().findViewById(R.id.estado);
        estado.setText(value.getEstado());
    }
}