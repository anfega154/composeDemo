package com.mantum.cmms.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;

import com.mantum.R;
import com.mantum.component.Mantum;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.handler.ViewSelectedAdapter;

public class RespuestaAdapter<T extends ViewSelectedAdapter<T>> extends Mantum.Adapter<T, RespuestaAdapter.ViewHolder> {

    private OnSelected<T> onPositive;
    private OnSelected<T> onNegative;

    private boolean readOnly;

    public RespuestaAdapter(@NonNull Context context) {
        super(context);
        this.readOnly = false;
    }

    public void setOnPositive(OnSelected<T> onPositive) {
        this.onPositive = onPositive;
    }

    public void setOnNegative(OnSelected<T> onNegative) {
        this.onNegative = onNegative;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @NonNull
    @Override
    public RespuestaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RespuestaAdapter.ViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.adapter_seccion_respuesta, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RespuestaAdapter.ViewHolder holder, int position) {
        T adapter = getItemPosition(position);
        if (adapter == null) {
            return;
        }

        holder.englishView.setText(adapter.getTitle());
        holder.spanishView.setText(adapter.getSubtitle());

        holder.noAppCompatCheckBox.setChecked(false);
        holder.yesAppCompatCheckBox.setChecked(false);

        if (adapter.isSelected() != null) {
            if (adapter.isSelected()) {
                holder.yesAppCompatCheckBox.setChecked(true);
                holder.noAppCompatCheckBox.setChecked(false);
            } else {
                holder.yesAppCompatCheckBox.setChecked(false);
                holder.noAppCompatCheckBox.setChecked(true);
            }
        }

        holder.yesAppCompatCheckBox.setOnClickListener(v -> {
            if (holder.yesAppCompatCheckBox.isChecked()) {
                holder.noAppCompatCheckBox.setChecked(false);
            }

            if (onPositive != null) {
                onPositive.onClick(adapter, position);
            }
        });

        holder.noAppCompatCheckBox.setOnClickListener(v -> {
            if (holder.noAppCompatCheckBox.isChecked()) {
                holder.yesAppCompatCheckBox.setChecked(false);
            }

            if (onNegative != null) {
                onNegative.onClick(adapter, position);
            }
        });

        if (readOnly) {
            holder.noAppCompatCheckBox.setEnabled(false);
            holder.yesAppCompatCheckBox.setEnabled(false);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView englishView;
        private final TextView spanishView;
        private final AppCompatCheckBox yesAppCompatCheckBox;
        private final AppCompatCheckBox noAppCompatCheckBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            englishView = itemView.findViewById(R.id.english);
            spanishView = itemView.findViewById(R.id.spanish);
            yesAppCompatCheckBox = itemView.findViewById(R.id.si);
            noAppCompatCheckBox = itemView.findViewById(R.id.no);
        }
    }
}
