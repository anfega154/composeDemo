package com.mantum.cmms.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
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
import com.mantum.cmms.adapter.handler.ListadoRepuestosHandler;
import com.mantum.component.Mantum;

public class ListadoRepuestosAdapter<T extends ListadoRepuestosHandler<T>> extends Mantum.Adapter<T, ListadoRepuestosAdapter.ViewHolder> {

    CustomOnValueChange<T> onValueChange;

    boolean serialVisibility;

    public ListadoRepuestosAdapter(@NonNull Context context) {
        super(context);
    }

    @Nullable
    @Override
    public T getItemPosition(int position) {
        return super.getItemPosition(position);
    }

    @NonNull
    @Override
    public ListadoRepuestosAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_listado_repuestos, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ListadoRepuestosAdapter.ViewHolder holder, int position) {
        T adapter = getItemPosition(position);

        if (adapter == null) {
            return;
        }

        holder.serialRepuesto.setText(adapter.getSerial());
        holder.nombreRepuesto.setText(adapter.getNombre());
        holder.serialRetiro.setText(adapter.getSerialRetiro());
        holder.btnRemoverRepuesto.setOnClickListener(view -> onValueChange.onClick(adapter, holder.getAdapterPosition()));
        holder.contentSerialRepuesto.setVisibility(serialVisibility ? View.VISIBLE : View.GONE);

        holder.serialRepuesto.addTextChangedListener(customTextWatcher(holder.serialRepuesto, holder));
        holder.nombreRepuesto.addTextChangedListener(customTextWatcher(holder.nombreRepuesto, holder));
        holder.serialRetiro.addTextChangedListener(customTextWatcher(holder.serialRetiro, holder));
    }

    public void setOnAction(CustomOnValueChange<T> onAction) {
        this.onValueChange = onAction;
    }

    public void setSerialVisibility(boolean serialVisibility) {
        this.serialVisibility = serialVisibility;
    }

    private TextWatcher customTextWatcher(EditText editText, ViewHolder holder) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editText == holder.serialRepuesto) {
                    onValueChange.onFirstTextChange(s.length() == 0 ? "" : s.toString(), holder.getAdapterPosition());
                } if (editText == holder.nombreRepuesto) {
                    onValueChange.onSecondTextChange(s.length() == 0 ? "" : s.toString(), holder.getAdapterPosition());
                } if (editText == holder.serialRetiro) {
                    onValueChange.onThirdTextChange(s.length() == 0 ? "" : s.toString(), holder.getAdapterPosition());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        EditText serialRepuesto;
        EditText nombreRepuesto;
        EditText serialRetiro;
        ImageView btnRemoverRepuesto;
        TextInputLayout contentSerialRepuesto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            serialRepuesto = itemView.findViewById(R.id.serial_repuesto);
            nombreRepuesto = itemView.findViewById(R.id.nombre_repuesto);
            serialRetiro = itemView.findViewById(R.id.serial_retiro);
            btnRemoverRepuesto = itemView.findViewById(R.id.remover_repuesto);
            contentSerialRepuesto = itemView.findViewById(R.id.content_serial_repuesto);
        }
    }
}
