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
import com.mantum.cmms.entity.InformacionTecnica;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

public class InformacionTecnicaFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Informacion_Tecnica";

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_informacion_tecnica, container, false);
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
        return context.getString(R.string.tag_informacion_tecnica);
    }

    public void onLoad(@NonNull InformacionTecnica value) {
        if (getView() == null) {
            return;
        }

        TextView fabricante = getView().findViewById(R.id.fabricante);
        fabricante.setText(value.getFabricante());

        TextView pais = getView().findViewById(R.id.pais);
        pais.setText(value.getPais());

        TextView fechaFabricante = getView().findViewById(R.id.fecha_fabricante);
        fechaFabricante.setText(value.getFechafabricacion());

        TextView modelo = getView().findViewById(R.id.modelo);
        modelo.setText(value.getModelo());

        TextView nroserie = getView().findViewById(R.id.numero_serie);
        nroserie.setText(value.getNroserie());
    }
}
