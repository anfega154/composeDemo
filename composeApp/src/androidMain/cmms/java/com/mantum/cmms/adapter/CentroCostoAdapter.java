package com.mantum.cmms.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.mantum.demo.R;
import com.mantum.cmms.adapter.handler.CentroCostoHandler;
import com.mantum.component.Mantum;
import com.mantum.component.OnValueChange;
import java.util.Locale;

public class CentroCostoAdapter<T extends CentroCostoHandler<T>> extends Mantum.Adapter<T, CentroCostoAdapter.ViewHolder> {

    OnValueChange<T> onValueChange;

    public CentroCostoAdapter(@NonNull Context context) {
        super(context);
    }

    @Nullable
    @Override
    public T getItemPosition(int position) {
        return super.getItemPosition(position);
    }

    @NonNull
    @Override
    public CentroCostoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_centro_costo, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        T adapter = getItemPosition(position);

        if (adapter == null) {
            return;
        }

        holder.textViewCodigoCentroCosto.setText(adapter.getCodigo());
        holder.textViewNombreCentroCosto.setText(adapter.getNombre());
        holder.textViewPorcentajeCentroCosto.setText(String.format(Locale.getDefault(), "%.2f", adapter.getPorcentaje()));
        holder.btnRemoverCentroCosto.setOnClickListener(view -> onValueChange.onClick(adapter, holder.getAdapterPosition()));
    }

    public void refresh() {
        notifyDataSetChanged();
    }

    public void setOnAction(OnValueChange<T> onAction) {
        this.onValueChange = onAction;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCodigoCentroCosto;
        TextView textViewNombreCentroCosto;
        EditText textViewPorcentajeCentroCosto;
        ImageView btnRemoverCentroCosto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewCodigoCentroCosto = itemView.findViewById(R.id.codigo_centro_costo);
            textViewNombreCentroCosto = itemView.findViewById(R.id.nombre_centro_costo);
            textViewPorcentajeCentroCosto = itemView.findViewById(R.id.porcentaje_centro_costo);
            btnRemoverCentroCosto = itemView.findViewById(R.id.remover_centro_costo);
        }
    }
}
