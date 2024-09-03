package com.mantum.cmms.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mantum.demo.R;
import com.mantum.cmms.entity.Sitio;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

public class SitioFragment extends Mantum.Fragment {

    public static final int REQUEST_ACTION = 1251;

    public final static String KEY_TAB = "Sitio";

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sitio, container, false);
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
        return context.getString(R.string.tab_sitio);
    }

    public void onRefresh(Sitio sitio) {
        if (getView() == null) {
            return;
        }

        TextInputEditText codigo = getView().findViewById(R.id.codigo);
        codigo.setText(sitio.getCodigo());

        TextInputEditText nombre = getView().findViewById(R.id.nombre);
        nombre.setText(sitio.getNombre());

        TextInputEditText telefono = getView().findViewById(R.id.telefono);
        telefono.setText(sitio.getTelefono());

        TextInputEditText direccion = getView().findViewById(R.id.direccion);
        direccion.setText(sitio.getDireccion());

        TextInputEditText departamento = getView().findViewById(R.id.departamento);
        departamento.setText(sitio.getDepartamento());

        TextInputEditText pais = getView().findViewById(R.id.pais);
        pais.setText(sitio.getPais());

        TextInputEditText ciudad = getView().findViewById(R.id.ciudad);
        ciudad.setText(sitio.getCiudad());

        TextInputEditText tipoenlace = getView().findViewById(R.id.tipoenlace);
        tipoenlace.setText(sitio.getTipoenlace());

        TextInputEditText contacto = getView().findViewById(R.id.contacto);
        contacto.setText(sitio.getContacto());

        TextInputEditText cargo = getView().findViewById(R.id.cargo);
        cargo.setText(sitio.getCargo());

        TextInputEditText responsable = getView().findViewById(R.id.responsable);
        responsable.setText(sitio.getIngenieroresponsable());
    }
}