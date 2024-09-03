package com.mantum.cmms.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mantum.R;
import com.mantum.cmms.domain.Chequeo;
import com.mantum.component.Mantum;

public class InspeccionCalidadAdapter extends Mantum.Adapter<Chequeo.ListaChequeo, InspeccionCalidadAdapter.ViewHolder> {

    public InspeccionCalidadAdapter(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new InspeccionCalidadAdapter.ViewHolder(LayoutInflater.from(
                parent.getContext()).inflate(R.layout.adapter_inspeccion_calidad, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chequeo.ListaChequeo value = getItemPosition(position);
        if (value == null) {
            return;
        }

        holder.title.setText(value.getTitulo());

        ChequeoAdapter chequeoAdapter = new ChequeoAdapter(context);
        chequeoAdapter.addAll(value.getChequeos());
        chequeoAdapter.startAdapter(holder.view, new LinearLayoutManager(context));
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final View view;

        final TextView title;

        ViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.title = itemView.findViewById(R.id.titulo);
        }
    }
}
