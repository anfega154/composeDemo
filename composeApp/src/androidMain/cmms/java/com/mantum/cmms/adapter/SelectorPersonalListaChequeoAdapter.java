package com.mantum.cmms.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mantum.demo.R;
import com.mantum.cmms.entity.PersonalListaChequeo;
import com.mantum.component.Mantum;

public class SelectorPersonalListaChequeoAdapter extends Mantum.Adapter<PersonalListaChequeo, SelectorPersonalListaChequeoAdapter.ViewHolder> {

    private final boolean readonly;

    public SelectorPersonalListaChequeoAdapter(@NonNull Context context, boolean readonly) {
        super(context);
        this.readonly = readonly;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_selector_lista_chequeo, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PersonalListaChequeo value = getItemPosition(position);
        if (value == null) {
            return;
        }

        holder.title.setText(value.getTitle());
        holder.subtitle.setText(value.getSubtitle());
        holder.summary.setText(value.getCargo());
        holder.switchCompat.setChecked(value.isSeleccionado());
        if (readonly) {
            holder.switchCompat.setEnabled(false);
        }

        holder.switchCompat.setOnCheckedChangeListener((compoundButton, b) -> {
            value.setSeleccionado(b);
            getOriginal().set(holder.getBindingAdapterPosition(), value);
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView subtitle;
        final TextView summary;
        final SwitchCompat switchCompat;

        ViewHolder(View itemView) {
            super(itemView);
            this.title = itemView.findViewById(R.id.title);
            this.subtitle = itemView.findViewById(R.id.subtitle);
            this.summary = itemView.findViewById(R.id.summary);
            this.switchCompat = itemView.findViewById(R.id.switch_compat);
        }
    }
}
