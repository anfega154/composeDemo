package com.mantum.cmms.adapter;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mantum.R;
import com.mantum.cmms.entity.Variable;
import com.mantum.cmms.entity.VariableCualitativa;
import com.mantum.component.Mantum;

import java.util.ArrayList;
import java.util.List;

public class LecturaAdapter extends Mantum.Adapter<Variable, LecturaAdapter.ViewHolder> {

    private final boolean isDescripcion;

    private boolean readonly;

    public LecturaAdapter(@NonNull Context context, boolean descripcion) {
        super(context);
        this.isDescripcion = descripcion;
        this.readonly = false;
    }

    public LecturaAdapter(@NonNull Context context, boolean descripcion, boolean readonly) {
        super(context);
        this.isDescripcion = descripcion;
        this.readonly = readonly;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.adapter_lectura, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void addAll(@NonNull List<Variable> values) {
        super.addAll(values);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Variable variable = getItemPosition(position);
        if (variable == null) {
            return;
        }

        holder.id.setText(String.valueOf(variable.getId()));
        holder.nombre.setText(variable.getNombre());
        holder.tipo.setText(variable.getTipo());
        holder.valor.setText(variable.getValor());

        holder.description.setText(variable.getDescripcion());
        applyTouchListener(holder.description,holder.scrollView);
        if (!isDescripcion) {
            holder.description.setVisibility(View.GONE);
        }

        holder.limites.setVisibility(View.GONE);
        holder.ultimaLectura.setVisibility(View.GONE);

        if (!readonly) {
            holder.valor.setFocusable(true);
            holder.valor.setCursorVisible(true);

            String limites = String.format("%s: %s - %s", holder.context.getString(R.string.limites),
                    String.valueOf(variable.getRangoinferior()),
                    String.valueOf(variable.getRangosuperior()));
            holder.limites.setVisibility(View.VISIBLE);
            holder.limites.setText(limites);

            if (variable.getUltimalectura() != null) {
                String horario = String.format("%s - %s: %s %s: %s",
                        holder.context.getString(R.string.ultima_lectura),
                        holder.context.getString(R.string.fecha),
                        variable.getUltimalectura().getFecha(),
                        holder.context.getString(R.string.valor),
                        variable.getUltimalectura().getValor());
                holder.ultimaLectura.setVisibility(View.VISIBLE);
                holder.ultimaLectura.setText(horario);
            }
        } else {
            holder.valor.setFocusable(false);
            holder.valor.setCursorVisible(false);
        }

        if (Variable.DESCRIPTIVA.equals(variable.getTipo())) {
            holder.limites.setVisibility(View.GONE);
            holder.valor.setInputType(InputType.TYPE_CLASS_TEXT);
            holder.valor.setSingleLine(false);
            holder.valor.setMaxLines(6);
            applyTouchListener(holder.valor, holder.scrollView);
        }

        if (Variable.CUALITATIVA.equals(variable.getTipo())) {
            holder.limites.setVisibility(View.GONE);
            holder.contenedor.setVisibility(View.GONE);

            VariableCualitativa valores = new VariableCualitativa();
            valores.setId(null);
            valores.setValor(holder.context.getString(R.string.seleccione_opcion));
            ArrayList<VariableCualitativa> data = new ArrayList<>();
            data.add(0, valores);
            data.addAll(variable.getValores());

            ArrayAdapter<VariableCualitativa> adapter = new ArrayAdapter<>(holder.context, R.layout.custom_simple_spinner, R.id.item, data);

            holder.rango.setAdapter(adapter);
            holder.rango.setVisibility(View.VISIBLE);
            holder.rango.setSelection(0);

            holder.rango.setFocusable(true);
            holder.rango.setFocusableInTouchMode(true);
            holder.rango.setOnTouchListener((v, event) -> {
                holder.rango.requestFocus();
                return false;
            });

            int index = 0;
            for (VariableCualitativa datum : data) {
                if (datum.getValor().equals(variable.getValor())) {
                    holder.rango.setSelection(index);
                    break;
                }
                index = index + 1;
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final Context context;

        final TextInputLayout contenedor;

        final EditText id;

        final EditText tipo;

        final EditText nombre;

        final TextView limites;

        final TextView description;

        final TextView valor;

        final AppCompatSpinner rango;

        final TextView ultimaLectura;

        final ScrollView scrollView;

        ViewHolder(View itemView) {
            super(itemView);
            this.context = itemView.getContext();
            this.contenedor = itemView.findViewById(R.id.contenedor_valor);
            this.id = itemView.findViewById(R.id.id);
            this.tipo = itemView.findViewById(R.id.tipo);
            this.nombre = itemView.findViewById(R.id.nombre);
            this.rango = itemView.findViewById(R.id.rango);
            this.limites = itemView.findViewById(R.id.limites);
            this.ultimaLectura = itemView.findViewById(R.id.utlima_lectura);
            this.description = itemView.findViewById(R.id.description);
            this.valor = itemView.findViewById(R.id.value);
            this.scrollView = itemView.findViewById(R.id.scroll_lectura);
        }
    }
    private void applyTouchListener(final View v, final ScrollView scrollView) {
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });
    }
}