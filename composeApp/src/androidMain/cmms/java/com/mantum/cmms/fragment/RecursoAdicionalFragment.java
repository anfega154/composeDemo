package com.mantum.cmms.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mantum.R;
import com.mantum.cmms.entity.RecursoAdicional;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.adapter.AlphabetAdapter;

import java.util.List;

public class RecursoAdicionalFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Recurso_Adicional";

    private boolean action;

    private OnCompleteListener onCompleteListener;

    private AlphabetAdapter<RecursoAdicional> alphabetAdapter;

    public RecursoAdicionalFragment() {
        this.action = true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_recursos_adicional,
                container, false);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(view.getContext());

        alphabetAdapter = new AlphabetAdapter<>(view.getContext());
        alphabetAdapter.startAdapter(view, layoutManager);
        alphabetAdapter.showMessageEmpty(view, R.string.recursos_adicionales_vacio);

        FloatingActionButton floatingActionButton = view.findViewById(R.id.add);
        floatingActionButton.setOnClickListener(v -> {
            View form = View.inflate(view.getContext(), R.layout.agregar_recurso_adicional, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
            alertDialogBuilder.setTitle(R.string.agregar_recursos);
            alertDialogBuilder.setNegativeButton(R.string.agregar, (dialog, which) -> {
                RecursoAdicional recursoAdicional = new RecursoAdicional();
                TextInputEditText descripcion = form.findViewById(R.id.descripcion);
                if (descripcion.getText() == null || descripcion.getText().toString().isEmpty()) {
                    Snackbar.make(view, R.string.descripcion_requerida, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                recursoAdicional.setNombre(descripcion.getText().toString());

                TextInputEditText cantidad = form.findViewById(R.id.cantidad);
                if (cantidad.getText() == null || cantidad.getText().toString().isEmpty()) {
                    Snackbar.make(view, R.string.cantidad_requerida, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                recursoAdicional.setCantidad(cantidad.getText().toString());

                SwitchCompat switchCompat = form.findViewById(R.id.utilizado);
                recursoAdicional.setUtilizado(switchCompat.isChecked());

                TextInputEditText unidad = form.findViewById(R.id.unidad);
                if (unidad.getText() != null && !unidad.getText().toString().isEmpty()) {
                    recursoAdicional.setUnidad(unidad.getText().toString());
                }

                TextInputEditText referencia = form.findViewById(R.id.referencia);
                if (referencia.getText() != null && !referencia.getText().toString().isEmpty()) {
                    recursoAdicional.setReferencia(referencia.getText().toString());
                }

                alphabetAdapter.add(recursoAdicional);
                alphabetAdapter.refresh();
                alphabetAdapter.showMessageEmpty(view);
            });

            alertDialogBuilder.setPositiveButton(R.string.cancelar, (dialog, which) -> dialog.cancel());
            alertDialogBuilder.setView(form);
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.show();
        });

        if (!action) {
            floatingActionButton.setVisibility(View.GONE);
        }

        return view;
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
        return context.getString(R.string.tab_recursos_adicionales);
    }

    public void addResources(@NonNull RecursoAdicional values) {
        if (getView() == null) {
            return;
        }

        alphabetAdapter.add(values, true);
        alphabetAdapter.showMessageEmpty(getView());
    }

    public void addResources(@NonNull List<RecursoAdicional> values) {
        if (getView() == null) {
            return;
        }

        alphabetAdapter.addAll(values);
        alphabetAdapter.showMessageEmpty(getView());
    }

    public List<RecursoAdicional> getValue() {
        return alphabetAdapter.getOriginal();
    }

    public RecursoAdicionalFragment setAction(@SuppressWarnings("SameParameterValue") boolean action) {
        this.action = action;
        return this;
    }
}