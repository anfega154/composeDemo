package com.mantum.cmms.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;
import com.mantum.R;
import com.mantum.cmms.activity.ValidarInventarioActivoActivity;
import com.mantum.cmms.domain.RequestValidarEquipo;

public class ValidarEquipoCodeQrFragment extends Fragment {

    private View view;
    private EditText nombreEquipo;
    private String nomEquipo="";
    private TextInputLayout nombreEquipoContainer;
    public ValidarEquipoCodeQrFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_validar_equipo_code_qr, container, false);
        nombreEquipo = view.findViewById(R.id.nombre_equipo);
        nombreEquipoContainer = view.findViewById(R.id.nombre_equipo_container);
        AppCompatButton validarEquipo = view.findViewById(R.id.validar_equipo_qr);
        validarEquipo.setOnClickListener(v -> {
            ocultarTeclado(v);
            nombreEquipoContainer.setError(null);
            nomEquipo = nombreEquipo.getText().toString().trim();
            if(!this.isEmpty()){
                if (getActivity() instanceof ValidarInventarioActivoActivity) {
                    ValidarInventarioActivoActivity actividad = (ValidarInventarioActivoActivity) getActivity();
                    actividad.setNomEquipo(nomEquipo);
                    actividad.scanQrCode();
                }
            }
        });
        return view;
    }

    private boolean isEmpty(){
        boolean isEmpty = false;
        if (nomEquipo.isEmpty()) {
            nombreEquipoContainer.setError(getString(R.string.requiere_nombre));
            isEmpty = true;
        }
        return isEmpty;
    }
    private void ocultarTeclado(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}