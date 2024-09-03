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

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.GsonBuilder;
import com.mantum.R;
import com.mantum.cmms.activity.ValidarInventarioActivoActivity;
import com.mantum.cmms.domain.RequestValidarEquipo;

import com.google.gson.Gson;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.service.TransaccionService;

import java.util.Calendar;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.internal.functions.Functions;

public class ValidarEquipoCodeFragment extends Fragment {

    private View view;
    private EditText codigoEquipo;
    private EditText nombreEquipo;
    protected final Gson gson = new GsonBuilder().create();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    String codEquipo = "";
    String nomEquipo= "";
    private TextInputLayout nombreEquipoContainer;
    private TextInputLayout codigoEquipoContainer;

    public ValidarEquipoCodeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_validar_equipo_code, container, false);
        codigoEquipo = view.findViewById(R.id.codigo_equipo);
        nombreEquipo = view.findViewById(R.id.nombre_equipo);
        nombreEquipoContainer = view.findViewById(R.id.nombre_equipo_container);
        codigoEquipoContainer = view.findViewById(R.id.codigo_equipo_container);

        AppCompatButton validarEquipo = view.findViewById(R.id.validar_equipo);
        validarEquipo.setOnClickListener(v -> {
            ocultarTeclado(v);
            nombreEquipoContainer.setError(null);
            codigoEquipoContainer.setError(null);
            codEquipo = codigoEquipo.getText().toString().trim();
            nomEquipo = nombreEquipo.getText().toString().trim();
            if (!this.isEmpty()) {
                if (getActivity() instanceof ValidarInventarioActivoActivity) {
                    ValidarInventarioActivoActivity actividad = (ValidarInventarioActivoActivity) getActivity();
                    actividad.setCodEquipo(codEquipo);
                    actividad.setNomEquipo(nomEquipo);
                    actividad.validateCode();
                    codigoEquipo.setText("");
                    nombreEquipo.setText("");
                }
            }
        });

        return view;
    }

    private void ocultarTeclado(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private boolean isEmpty(){
        boolean isEmpty = false;
        if (codEquipo.isEmpty()) {
            codigoEquipoContainer.setError(getString(R.string.requiere_codigo));
            isEmpty = true;
        }
        if (nomEquipo.isEmpty()) {
            nombreEquipoContainer.setError(getString(R.string.requiere_nombre));
            isEmpty = true;
        }
        return isEmpty;
    }
}