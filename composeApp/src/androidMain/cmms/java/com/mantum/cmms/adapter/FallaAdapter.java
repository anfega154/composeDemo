package com.mantum.cmms.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mantum.demo.R;
import com.mantum.component.Mantum;
import com.mantum.component.OnCall;
import com.mantum.component.OnInvoke;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.handler.ViewAdapter;

public class FallaAdapter<T extends ViewAdapter<T>> extends Mantum.Adapter<T, FallaAdapter.ViewHolder> {

    private boolean readOnly;
    private OnInvoke<Integer> onRemove;
    private OnSelected<T> OnSelected;

    public FallaAdapter(@NonNull Context context) {
        super(context);
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @NonNull
    @Override
    public FallaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FallaAdapter.ViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.adapter_falla, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        T adapter = getItemPosition(position);
        if (adapter == null) {
            return;
        }

        holder.rowView.setOnClickListener(v -> {
            if (!readOnly && OnSelected != null) {
                OnSelected.onClick(adapter, position);
            }
        });


        holder.actividadView.setText(adapter.getTitle());
        holder.serialView.setText(adapter.getSubtitle());
        holder.eliminarView.setOnClickListener(v -> {
            if (!readOnly && onRemove != null) {
                onRemove.invoke(position);
            }
        });

        if (readOnly) {
            holder.eliminarView.setVisibility(View.GONE);
        }
    }

    public void setOnRemove(OnInvoke<Integer> onRemove) {
        this.onRemove = onRemove;
    }

    public void setOnSelected(OnSelected<T> OnSelected) {
        this.OnSelected = OnSelected;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout rowView;
        private final TextView actividadView;
        private final TextView serialView;
        private final ImageView eliminarView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rowView = itemView.findViewById(R.id.row);
            actividadView = itemView.findViewById(R.id.actividad);
            serialView = itemView.findViewById(R.id.serial);
            eliminarView = itemView.findViewById(R.id.eliminar);
        }
    }
}
