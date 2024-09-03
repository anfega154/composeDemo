package com.mantum.cmms.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mantum.demo.R;
import com.mantum.cmms.adapter.handler.HistoricoParoHandler;
import com.mantum.component.Mantum;

public class HistoricoParoAdapter<T extends HistoricoParoHandler<T>> extends Mantum.Adapter<T, HistoricoParoAdapter.ViewHolder> {

    public HistoricoParoAdapter(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_historico_paro, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        T adapter = getItemPosition(position);

        if (adapter == null) {
            return;
        }

        holder.fechaInicio.setText(adapter.getFechainicio());
        holder.fechaFin.setText(adapter.getFechafin());
        holder.duracion.setText(adapter.getDuracion());
        holder.clasificacion.setText(adapter.getTipo());
        holder.tipo.setText(adapter.getTipoparo());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        EditText fechaInicio;
        EditText fechaFin;
        EditText duracion;
        EditText clasificacion;
        EditText tipo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            fechaInicio = itemView.findViewById(R.id.fecha_inicio);
            fechaFin = itemView.findViewById(R.id.fecha_fin);
            duracion = itemView.findViewById(R.id.duracion);
            clasificacion = itemView.findViewById(R.id.clasificacion);
            tipo = itemView.findViewById(R.id.tipo);
        }
    }
}
