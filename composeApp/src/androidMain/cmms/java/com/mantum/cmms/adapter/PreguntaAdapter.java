package com.mantum.cmms.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mantum.R;
import com.mantum.component.Mantum;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.handler.ViewGroupSelectedAdapter;
import com.mantum.component.adapter.handler.ViewSelectedAdapter;

public class PreguntaAdapter<T extends ViewGroupSelectedAdapter<T, K>, K extends ViewSelectedAdapter<K>> extends Mantum.Adapter<T, PreguntaAdapter.ViewHolder> {

    private RespuestaAdapter<K> checklistAdapter;

    private OnSelected<T> onPositive;
    private OnSelected<T> onNegative;

    private boolean readOnly;

    public PreguntaAdapter(@NonNull Context context) {
        super(context);
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
    public PreguntaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PreguntaAdapter.ViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.adapter_seccion_pregunta, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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

        checklistAdapter = new RespuestaAdapter<>(context);
        checklistAdapter.setReadOnly(readOnly);
        if (adapter.getChildren() != null) {
            checklistAdapter.addAll(adapter.getChildren());
        }

        checklistAdapter.setOnPositive(new OnSelected<K>() {
            @Override
            public void onClick(K value, int position) {
                value.setSelected(true);
                checklistAdapter.notifyItemChanged(position);
            }

            @Override
            public boolean onLongClick(K value, int position) {
                return false;
            }
        });
        checklistAdapter.setOnNegative(new OnSelected<K>() {
            @Override
            public void onClick(K value, int position) {
                value.setSelected(false);
                checklistAdapter.notifyItemChanged(position);
            }

            @Override
            public boolean onLongClick(K value, int position) {
                return false;
            }
        });

        if (readOnly) {
            holder.noAppCompatCheckBox.setEnabled(false);
            holder.yesAppCompatCheckBox.setEnabled(false);
        }

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        checklistAdapter.startAdapter(holder.view, layoutManager);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        private final TextView englishView;
        private final TextView spanishView;
        private final AppCompatCheckBox yesAppCompatCheckBox;
        private final AppCompatCheckBox noAppCompatCheckBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            englishView = itemView.findViewById(R.id.english);
            spanishView = itemView.findViewById(R.id.spanish);
            yesAppCompatCheckBox = itemView.findViewById(R.id.si);
            noAppCompatCheckBox = itemView.findViewById(R.id.no);
        }
    }
}
