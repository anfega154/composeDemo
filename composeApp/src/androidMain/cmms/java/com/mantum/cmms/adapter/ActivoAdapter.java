package com.mantum.cmms.adapter;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mantum.demo.R;
import com.mantum.cmms.entity.Activos;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class ActivoAdapter extends RecyclerView.Adapter<ActivoAdapter.ViewHolder> {

    private List<Activos> activos;

    public ActivoAdapter() {
        this.activos = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(
                parent.getContext()).inflate(R.layout.adapter_activos, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Activos activo = this.activos.get(position);
        holder.equipo.setText(activo.getEquipo());
        holder.responsable.setText(activo.getResponsable());
    }

    @Override
    public int getItemCount() {
        if (activos != null) {
            return activos.size();
        }
        return 0;
    }

    public void add(List<Activos> activos) {
        this.activos = activos;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView equipo;

        TextView responsable;

        public ViewHolder(View itemView) {
            super(itemView);
            equipo = (TextView) itemView.findViewById(R.id.equipo);
            responsable = (TextView) itemView.findViewById(R.id.responsable);
        }
    }
}
