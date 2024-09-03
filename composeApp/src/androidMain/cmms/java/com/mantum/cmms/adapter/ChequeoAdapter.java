package com.mantum.cmms.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputEditText;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mantum.demo.R;
import com.mantum.cmms.domain.Chequeo;
import com.mantum.component.Mantum;

public class ChequeoAdapter extends Mantum.Adapter<Chequeo, ChequeoAdapter.ViewHolder> {

    public ChequeoAdapter(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(
                parent.getContext()).inflate(R.layout.adapter_chequeo, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chequeo value = getItemPosition(position);
        if (value == null) {
            return;
        }

        holder.key.setText(value.getTitulo());
        holder.key.setVisibility(View.GONE);
        if (!value.getTitulo().isEmpty()) {
            holder.key.setVisibility(View.VISIBLE);
            holder.key.setOnCheckedChangeListener((buttonView, isChecked) -> {
                value.setAplica(isChecked);
                getOriginal().set(holder.getAdapterPosition(), value);
            });
        }

        holder.value.setHint(value.getDescripcion());
        holder.value.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                value.setCondiciones(holder.value.getText().toString());
                getOriginal().set(holder.getAdapterPosition(), value);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final SwitchCompat key;

        final TextInputEditText value;

        ViewHolder(View itemView) {
            super(itemView);
            this.key = itemView.findViewById(R.id.key);
            this.value = itemView.findViewById(R.id.value);
        }
    }
}