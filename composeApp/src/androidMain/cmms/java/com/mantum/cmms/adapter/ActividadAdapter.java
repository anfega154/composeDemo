package com.mantum.cmms.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.mantum.demo.R;
import com.mantum.cmms.activity.BridgeActivity;
import com.mantum.cmms.entity.Actividad;
import com.mantum.cmms.entity.Entidad;
import com.mantum.cmms.entity.Variable;
import com.mantum.component.Mantum;

import java.util.ArrayList;
import java.util.List;

class ActividadAdapter extends Mantum.Adapter<Actividad, ActividadAdapter.ViewHolder> {

    private final Entidad entidad;

    private final boolean validarQR;

    private boolean modoVer;

    private boolean isMostrarObservacion;

    private List<Long> ejecutadas = new ArrayList<>();

    private List<Variable> variables = new ArrayList<>();

    ActividadAdapter(@NonNull Context context, @NonNull Entidad entidad, boolean validarQR) {
        super(context);
        this.entidad = entidad;
        this.validarQR = validarQR;
        this.modoVer = false;
        this.isMostrarObservacion = true;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.adapter_actividades, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Actividad value = getItemPosition(position);
        if (value == null) {
            return;
        }

        holder.id.setText(String.valueOf(value.getId()));
        holder.ejecutar.setText(value.getNombre());
        holder.ejecutar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (holder.ejecutar.isChecked() && validarQR) {
                Bundle bundle = new Bundle();
                bundle.putLong(BridgeActivity.ID_ENTIDAD_SELECCIONADA, entidad.getId());
                bundle.putString(BridgeActivity.CODIGO_ENTIDAD_SELECCIONADA, entidad.getCodigo());
                bundle.putString(BridgeActivity.NOMBRE_ENTIDAD_SELECCIONADA, entidad.getNombre());
                bundle.putString(BridgeActivity.TIPO_ENTIDAD_SELECCIONADA, entidad.getTipo());
                bundle.putInt(BridgeActivity.POSICION_SELECCIONADA, position);

                Intent intent = new Intent(context, BridgeActivity.class);
                intent.putExtras(bundle);
                ((Activity) context).startActivityForResult(intent, BridgeActivity.REQUEST_CODE);

                holder.ejecutar.setChecked(false);
                holder.varContainer.setVisibility(View.GONE);
            }

            if (holder.ejecutar.isChecked()) {
                holder.varContainer.setVisibility(View.VISIBLE);
            } else {
                holder.varContainer.setVisibility(View.GONE);
            }

        });

        for (Long ejecutada : ejecutadas) {
            if (value.getId().equals(ejecutada)) {
                holder.ejecutar.setChecked(true);
                holder.varContainer.setVisibility(View.VISIBLE);
            }
        }

        if (modoVer) {
            holder.ejecutar.setEnabled(false);
            holder.varContainer.setVisibility(View.VISIBLE);
        }

        holder.variables.setVisibility(View.GONE);
        if (value.getVariables() != null && !value.getVariables().isEmpty()) {
            holder.variables.setVisibility(View.VISIBLE);

            VariableAdapter variableAdapter = new VariableAdapter(context);
            if (modoVer) {
                variableAdapter.activarModoDetalle();
            }

            if (!isMostrarObservacion) {
                variableAdapter.ocultarObservacion();
            }

            variableAdapter.agregarValoresVariable(this.variables);
            variableAdapter.addAll(value.getVariables());
            variableAdapter.startAdapter(holder.view, new LinearLayoutManager(context));
        }
    }

    void agregarEjecutadas(@NonNull List<Long> ejecutadas) {
        this.ejecutadas = ejecutadas;
    }

    void agregarValoresVariable(@NonNull List<Variable> variables) {
        this.variables = variables;
    }

    void activarModoVer() {
        this.modoVer = true;
    }

    void ocultarObservacion() {
        this.isMostrarObservacion = false;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final View view;

        final TextView id;

        final RecyclerView variables;

        final SwitchCompat ejecutar;

        final FrameLayout varContainer;

        final TextInputEditText observacion;

        ViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.ejecutar = itemView.findViewById(R.id.ejecutar);
            this.varContainer = itemView.findViewById(R.id.varContainer);
            this.id = itemView.findViewById(R.id.idactividad);
            this.variables = itemView.findViewById(R.id.recycler_view);
            this.observacion = itemView.findViewById(R.id.observacion);
        }
    }
}