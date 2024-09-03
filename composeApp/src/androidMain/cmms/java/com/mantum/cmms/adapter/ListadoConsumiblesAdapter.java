package com.mantum.cmms.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.mantum.R;
import com.mantum.cmms.adapter.onValueChange.CustomOnValueChange;
import com.mantum.cmms.adapter.handler.ListadoConsumiblesHandler;
import com.mantum.component.Mantum;

public class ListadoConsumiblesAdapter<T extends ListadoConsumiblesHandler<T>> extends Mantum.Adapter<T, ListadoConsumiblesAdapter.ViewHolder> {

    CustomOnValueChange<T> onValueChange;

    public ListadoConsumiblesAdapter(@NonNull Context context) {
        super(context);
    }

    @Nullable
    @Override
    public T getItemPosition(int position) {
        return super.getItemPosition(position);
    }

    @NonNull
    @Override
    public ListadoConsumiblesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_listado_consumibles, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ListadoConsumiblesAdapter.ViewHolder holder, int position) {
        T adapter = getItemPosition(position);

        if (adapter == null) {
            return;
        }

        holder.nombreConsumible.setText(adapter.getNombre());
        holder.cantidadConsumible.setText(adapter.getCantidadreal() != null ? String.valueOf(adapter.getCantidadreal()) : "");
        holder.btnRemoverConsumible.setOnClickListener(view -> onValueChange.onClick(adapter, holder.getAdapterPosition()));

        holder.nombreConsumible.addTextChangedListener(customTextWatcher(holder.nombreConsumible, holder));
        holder.cantidadConsumible.addTextChangedListener(customTextWatcher(holder.cantidadConsumible, holder));
    }

    public void setOnAction(CustomOnValueChange<T> onAction) {
        this.onValueChange = onAction;
    }

    private TextWatcher customTextWatcher(EditText editText, ViewHolder holder) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editText == holder.nombreConsumible) {
                    onValueChange.onFirstTextChange(s.length() == 0 ? "" : s.toString(), holder.getAdapterPosition());
                } if (editText == holder.cantidadConsumible) {
                    onValueChange.onSecondTextChange(s.length() == 0 ? "" : s.toString(), holder.getAdapterPosition());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        EditText nombreConsumible;
        EditText cantidadConsumible;
        ImageView btnRemoverConsumible;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nombreConsumible = itemView.findViewById(R.id.nombre_consumible);
            cantidadConsumible = itemView.findViewById(R.id.cantidad_consumible);
            btnRemoverConsumible = itemView.findViewById(R.id.remover_consumible);
        }
    }
}
