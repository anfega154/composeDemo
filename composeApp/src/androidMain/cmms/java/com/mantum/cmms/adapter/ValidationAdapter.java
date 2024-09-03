package com.mantum.cmms.adapter;
import com.mantum.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mantum.cmms.entity.Validation;

import java.util.List;

public class ValidationAdapter extends RecyclerView.Adapter<ValidationAdapter.ViewHolder>{
    private List<Validation> validations;
    private ValidationAdapterListener listener;
    public ValidationAdapter(List<Validation> validations, ValidationAdapterListener listener) {
        this.validations = validations;
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView lblCodigo;
        TextView lblNombre;
        TextView lblFechas;
        public ViewHolder(View view){
            super(view);
            lblCodigo= view.findViewById(R.id.lblCodigo);
            lblNombre=view.findViewById(R.id.lblNombre);
            lblFechas=view.findViewById(R.id.lblFechas);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.OnItemClicked(
                    validations.get(getAdapterPosition()).getId(),
                    validations.get(getAdapterPosition()).getCode(),
                    validations.get(getAdapterPosition()).getName()
            );
        }
    }

    public interface ValidationAdapterListener{
        void OnItemClicked(int id, String code, String name);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_validation,parent,false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Validation validation = validations.get(position);
        holder.lblCodigo.setText(validation.getCode());
        holder.lblNombre.setText(validation.getName());
        holder.lblFechas.setText(validation.getFechas());
    }

    @Override
    public int getItemCount() {
        int i=0;
        if (this.validations.size()>0) i = this.validations.size();
        return i;
    }
}
