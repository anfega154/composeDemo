package com.mantum.cmms.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.mantum.demo.R;
import com.mantum.cmms.adapter.handler.TareaHandler;
import com.mantum.cmms.adapter.onValueChange.CustomCheckChange;
import com.mantum.component.Mantum;

public class TareaAdapter<T extends TareaHandler<T>> extends Mantum.Adapter<T, TareaAdapter.ViewHolder> {

    private CustomCheckChange customCheckChange;

    public TareaAdapter(@NonNull Context context) {
        super(context);
    }

    @Nullable
    @Override
    public T getItemPosition(int position) {
        return super.getItemPosition(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_tarea, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        T adapter = getItemPosition(position);

        if (adapter == null) {
            return;
        }

        holder.codigo.setText(adapter.getCodigo());
        holder.critica.setText(String.format("Crítica: %s", adapter.isCritica() ? "Sí" : "No"));
        holder.tarea.setText(adapter.getTarea());
        holder.ejecutada.setChecked(adapter.isEjecutada());
        holder.descripcion.setText(adapter.getDescripcion());

        if (adapter.isEjecutada()) {
            holder.ejecutor.setText(adapter.getEjecutor());
            holder.fechaEjecucion.setText(adapter.getFechaejecucion());
        } else {
            holder.layoutEjecutor.setVisibility(View.GONE);
            holder.layoutFechaEjecucion.setVisibility(View.GONE);
        }

        holder.ejecutada.setOnCheckedChangeListener((compoundButton, b) -> customCheckChange.onClick(holder.getAdapterPosition(), holder.ejecutada.isChecked()));
        holder.mostrarMas.setOnClickListener(view -> {
            boolean layoutMasIsVisible = holder.layoutMas.getVisibility() == View.VISIBLE;
            holder.layoutMas.setVisibility(layoutMasIsVisible ? View.GONE : View.VISIBLE);
            holder.mostrarMas.setImageResource(layoutMasIsVisible ? R.drawable.expand_more : R.drawable.expand_less);
        });
    }

    public void setOnAction(CustomCheckChange onAction) {
        this.customCheckChange = onAction;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox ejecutada;
        TextView codigo;
        TextView critica;
        TextView tarea;
        ImageView mostrarMas;
        LinearLayout layoutMas;
        LinearLayout layoutEjecutor;
        LinearLayout layoutFechaEjecucion;
        TextView ejecutor;
        TextView fechaEjecucion;
        TextView descripcion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ejecutada = itemView.findViewById(R.id.ejecutada);
            codigo = itemView.findViewById(R.id.codigo);
            critica = itemView.findViewById(R.id.critica);
            tarea = itemView.findViewById(R.id.tarea);
            mostrarMas = itemView.findViewById(R.id.mostrar_mas);
            layoutMas = itemView.findViewById(R.id.layout_mas);
            layoutEjecutor = itemView.findViewById(R.id.layout_ejecutor);
            layoutFechaEjecucion = itemView.findViewById(R.id.layout_fecha_ejecucion);
            ejecutor = itemView.findViewById(R.id.ejecutor);
            fechaEjecucion = itemView.findViewById(R.id.fecha_ejecucion);
            descripcion = itemView.findViewById(R.id.descripcion);
        }
    }
}
