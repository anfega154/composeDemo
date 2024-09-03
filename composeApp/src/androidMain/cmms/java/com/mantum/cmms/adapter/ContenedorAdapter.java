package com.mantum.cmms.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.mantum.demo.R;
import com.mantum.component.Mantum;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.handler.ViewAdapter;

public class ContenedorAdapter<T extends ViewAdapter<T>> extends Mantum.Adapter<T, ContenedorAdapter.ViewHolder> {

    private OnSelected<T> onSelected;

    public ContenedorAdapter(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ContenedorAdapter.ViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.adapter_contenedor, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        T adapter = getItemPosition(position);
        if (adapter == null) {
            return;
        }

        holder.tituloView.setText(adapter.getTitle());
        holder.ubicacionView.setText(adapter.getSummary());
        holder.fechaView.setText(adapter.getSubtitle());
        holder.tipoView.setText(adapter.getIcon());
        if (adapter.getDrawable() != null) {
            holder.imageView.setBackgroundResource(adapter.getDrawable());
        }

        if (onSelected != null) {
            holder.container.setOnClickListener(
                    view -> onSelected.onClick(adapter, holder.getBindingAdapterPosition()));
        }
    }

    public void setOnAction(@Nullable OnSelected<T> onSelected) {
        this.onSelected = onSelected;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout container;
        private final TextView tituloView;
        private final TextView ubicacionView;
        private final TextView fechaView;
        private final TextView tipoView;
        private final ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.contenedor);
            tituloView = itemView.findViewById(R.id.titulo);
            ubicacionView = itemView.findViewById(R.id.ubicacion);
            fechaView = itemView.findViewById(R.id.fecha);
            tipoView = itemView.findViewById(R.id.tipo);
            imageView = itemView.findViewById(R.id.estado);
        }
    }
}
