package com.mantum.cmms.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mantum.demo.R;
import com.mantum.cmms.entity.Variable;
import com.mantum.cmms.entity.VariableCualitativa;
import com.mantum.component.Mantum;

import java.util.ArrayList;
import java.util.List;

public class VariableAdapter extends Mantum.Adapter<Variable, VariableAdapter.ViewHolder> {

    private boolean modoVer;

    private boolean modoDetalle;

    private boolean isMostrarObservacion;

    private List<Variable> variables = new ArrayList<>();

    public VariableAdapter(@NonNull Context context) {
        super(context);
        this.modoVer = false;
        this.modoDetalle = false;
        this.isMostrarObservacion = true;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(
                parent.getContext()).inflate(R.layout.adapter_variables, parent, false));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Variable value = getItemPosition(position);
        if (value == null) {
            return;
        }

        holder.id.setText(String.valueOf(value.getId()));
        holder.nombre.setText(value.getNombre());
        holder.tipo.setText(value.getTipo());

        holder.valor.setVisibility(View.GONE);
        holder.observacion.setVisibility(View.GONE);
        if (value.isRango()) {
            holder.limites.setVisibility(View.VISIBLE);
            holder.limites.setText(String.format("%s - %s - %s",
                    context.getString(R.string.limites),
                    String.valueOf(value.getRangoinferior()),
                    String.valueOf(value.getRangosuperior())));
        } else {
            holder.limites.setText("");
            holder.limites.setVisibility(View.GONE);
        }

        if (value.getUnidadmedida() != null && !value.getUnidadmedida().equals("")) {
            holder.unidadmedida.setVisibility(View.VISIBLE);
            holder.unidadmedida.setText(String.format("%s %s", "Unidad:", value.getUnidadmedida()));
        } else {
            holder.unidadmedida.setVisibility(View.GONE);
        }

        if (!modoVer) {
            holder.tipo.setVisibility(View.GONE);
            holder.valor.setVisibility(View.VISIBLE);

            if (isMostrarObservacion) {
                holder.observacion.setVisibility(View.VISIBLE);
                holder.observacion.setMaxLines(6);
                Mantum.applyTouchListener(holder.observacion,holder.scrollView);
            }

            if (Variable.DESCRIPTIVA.equals(value.getTipo())) {
                holder.valor.setInputType(InputType.TYPE_CLASS_TEXT);
                holder.valor.setSingleLine(false);
                holder.valor.setMaxLines(6);
                Mantum.applyTouchListener(holder.valor, holder.scrollView);
            }

            if (Variable.CUALITATIVA.equals(value.getTipo())) {
                holder.limites.setText("");
                holder.limites.setVisibility(View.GONE);
                holder.valor.setVisibility(View.GONE);

                VariableCualitativa valores = new VariableCualitativa();
                valores.setId(null);
                valores.setValor(context.getString(R.string.seleccione_opcion));

                List<VariableCualitativa> data = new ArrayList<>();
                data.add(0, valores);
                data.addAll(value.getValores());

                ArrayAdapter<VariableCualitativa> adapter = new ArrayAdapter<>(
                        context, R.layout.custom_simple_spinner, R.id.item, data);

                holder.rango.setAdapter(adapter);
                holder.rango.setVisibility(View.VISIBLE);
                holder.rango.setSelection(0);

                holder.rango.setFocusable(true);
                holder.rango.setOnTouchListener((v, event) -> {
                    holder.rango.requestFocus();
                    return false;
                });
            }

            if (modoDetalle) {
                holder.limites.setVisibility(View.GONE);
                holder.valor.setEnabled(false);
                holder.rango.setEnabled(false);
                holder.observacion.setEnabled(false);
            }
        }

        for (Variable variable : variables) {
            if (value.getId().equals(variable.getId())
                    && value.getIdentidad().equals(variable.getIdentidad())
                    && value.getTipoentidad().equals(variable.getTipoentidad())
                    && value.getIdActividad().equals(variable.getIdActividad())) {
                holder.valor.setText(variable.getValor());
                holder.observacion.setText(variable.getObservacion());
                if (holder.rango.getAdapter() != null) {
                    int i = 0;
                    int total = holder.rango.getAdapter().getCount();
                    for (; i < total; i++) {
                        VariableCualitativa variableCualitativa
                                = (VariableCualitativa) holder.rango.getAdapter().getItem(i);
                        if (variableCualitativa.getValor().equals(variable.getValor())) {
                            break;
                        }
                    }
                    holder.rango.setSelection(i);
                }
            }
        }
    }

    @Override
    public void addAll(@NonNull List<Variable> values) {
//        Collections.sort(values, VariableAdapter::ordenar);
        super.addAll(values);
    }

    public static int ordenar(Variable a, Variable b) {
        if (a.getOrden() == b.getOrden()) {
            int compare = a.getTipo().compareTo(b.getTipo());
            if (compare != 0) {
                return compare;
            }
            return a.getCodigo().compareTo(b.getCodigo());
        }
        return a.getOrden() > b.getOrden() ? 1 : -1;
    }

    private static int ordenarSegmento(Variable a, Variable b) {
        if(a.getSegmento() != null ) {
            if (a.getSegmento().getId() == b.getSegmento().getId()) {
                int compare = a.getTipo().compareTo(b.getTipo());
                if (compare != 0) {
                    return compare;
                }
                return a.getCodigo().compareTo(b.getCodigo());
            }
            return a.getSegmento().getId() > b.getSegmento().getId() ? 1 : -1;
        }
        return a.getOrden() > b.getOrden() ? 1 : -1;
    }

    void agregarValoresVariable(@NonNull List<Variable> variables) {
        this.variables = variables;
    }

    public void activarModoVer() {
        this.modoVer = true;
    }

    void activarModoDetalle() {
        this.modoDetalle = true;
    }

    void ocultarObservacion() {
        this.isMostrarObservacion = false;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView id;

        final TextView limites;

        final TextView nombre;

        final TextView unidadmedida;

        final TextView tipo;

        final TextInputEditText valor;

        final AppCompatSpinner rango;

        final TextInputEditText observacion;

        final ScrollView scrollView;

        ViewHolder(View itemView) {
            super(itemView);
            this.id = itemView.findViewById(R.id.idvariable);
            this.nombre = itemView.findViewById(R.id.nombre);
            this.unidadmedida = itemView.findViewById(R.id.unidadmedida);
            this.limites = itemView.findViewById(R.id.limites);
            this.valor = itemView.findViewById(R.id.valor);
            this.rango = itemView.findViewById(R.id.rango);
            this.tipo = itemView.findViewById(R.id.tipo);
            this.observacion = itemView.findViewById(R.id.observacion);
            this.scrollView = itemView.findViewById(R.id.scrollView);
        }
    }
}