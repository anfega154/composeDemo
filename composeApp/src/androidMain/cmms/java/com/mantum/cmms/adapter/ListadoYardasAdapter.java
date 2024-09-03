package com.mantum.cmms.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mantum.R;
import com.mantum.cmms.adapter.handler.ListadoYardasHandler;
import com.mantum.component.Mantum;
import com.mantum.component.OnValueChange;

public class ListadoYardasAdapter<T extends ListadoYardasHandler<T>> extends Mantum.Adapter<T, ListadoYardasAdapter.ViewHolder> {

    OnValueChange<T> onValueChange;

    public ListadoYardasAdapter(@NonNull Context context) {
        super(context);
    }

    @Nullable
    @Override
    protected T getItemPosition(int position) {
        return super.getItemPosition(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_listado_yardas, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        T adapter = getItemPosition(position);

        if (adapter == null) {
            return;
        }

        holder.nombreYarda.setText(adapter.getNombre());
        holder.btnRemoverYarda.setOnClickListener(view -> onValueChange.onClick(adapter, holder.getAdapterPosition()));
    }

    public void refresh() {
        notifyDataSetChanged();
    }

    public void setOnAction(OnValueChange<T> onAction) {
        this.onValueChange = onAction;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombreYarda;
        ImageView btnRemoverYarda;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nombreYarda = itemView.findViewById(R.id.nombre_yarda);
            btnRemoverYarda = itemView.findViewById(R.id.remover_yarda);
        }
    }
}
