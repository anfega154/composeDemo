package com.mantum.cmms.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mantum.R;
import com.mantum.cmms.entity.DetalleBusqueda;

import java.util.List;

@Deprecated
public class DetalleAdapter extends RecyclerView.Adapter<DetalleAdapter.ViewHolder> {

    private List<DetalleBusqueda> detalles;

    public DetalleAdapter(List<DetalleBusqueda> detalles) {
        this.detalles = detalles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.detalle, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DetalleBusqueda detalle = detalles.get(position);
        if (detalle == null) {
            return;
        }

        holder.title.setText(detalle.getTitle());
        holder.value.setText(detalle.getValue());
    }

    @Override
    public int getItemCount() {
        return detalles.size();
    }

    public void add(List<DetalleBusqueda> detalles) {
        this.detalles = detalles;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;

        private final TextView value;

        ViewHolder(View itemView) {
            super(itemView);
            this.title = itemView.findViewById(R.id.title);
            this.value = itemView.findViewById(R.id.value);
        }
    }
}