package com.mantum.cmms.adapter;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mantum.R;
import com.mantum.cmms.domain.Resultado;
import com.mantum.component.Mantum;
import com.mantum.component.OnSelected;

import java.text.Normalizer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BusquedaAvanzadaResultadoAdapter extends Mantum.Adapter<Resultado, BusquedaAvanzadaResultadoAdapter.ViewHolder> {

    private OnSelected<Resultado> onSelected;

    public BusquedaAvanzadaResultadoAdapter(@NonNull Context context) {
        super(context);
    }

    public void setOnAction(@Nullable OnSelected<Resultado> onSelected) {
        this.onSelected = onSelected;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.busqueda_avanzada_resultado, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Resultado adapter = getItemPosition(position);
        if (adapter == null) {
            return;
        }

        viewHolder.caracter.setText("");
        viewHolder.caracter.setVisibility(View.VISIBLE);
        viewHolder.separador.setVisibility(View.GONE);

        int total = 0;
        String letter = getCharacter(cleanedName(adapter.getTitle()));
        viewHolder.caracter.setVisibility(View.VISIBLE);

        if (viewHolder.getAdapterPosition() < total) {
            if (viewHolder.getAdapterPosition() == 0) {
                viewHolder.caracter.setVisibility(View.GONE);
            }
        } else {
            com.mantum.component.adapter.handler.ViewAdapter before = getItemPosition(viewHolder.getAdapterPosition() - 1);
            if (viewHolder.getAdapterPosition() == 0 || viewHolder.getAdapterPosition() == total ||
                    (before != null && !letter.equals(getCharacter(cleanedName(before.getTitle()))))) {
                if (viewHolder.getAdapterPosition() > 0) {
                    viewHolder.separador.setVisibility(View.VISIBLE);
                }

                viewHolder.caracter.setText(letter);
                viewHolder.caracter.setTextColor(Color.parseColor(getColor()));
            }
        }

        viewHolder.codigo.setText(adapter.getTitle());

        viewHolder.nombre.setText("");
        if (adapter.getSubtitle() != null) {
            viewHolder.nombre.setText(adapter.getSubtitle());
        }

        viewHolder.instalacionPadre.setText("");
        if (adapter.getSummary() != null) {
            viewHolder.instalacionPadre.setText(adapter.getSummary());
        }

        viewHolder.equipoPadre.setText("");
        viewHolder.equipoPadre.setVisibility(View.GONE);
        if (adapter.getEquipopadre() != null) {
            viewHolder.equipoPadre.setText(adapter.getEquipopadre());
            viewHolder.equipoPadre.setVisibility(View.VISIBLE);
        }

        viewHolder.tipo.setVisibility(View.GONE);
        if (adapter.getDrawable() != null) {
            viewHolder.tipo.setVisibility(View.VISIBLE);
            viewHolder.tipo.setBackgroundResource(adapter.getDrawable());
        }

        viewHolder.familiaUno.setText("");
        viewHolder.familiaUno.setVisibility(View.GONE);
        if (adapter.getFamilia1() != null) {
            viewHolder.familiaUno.setText(adapter.getFamilia1());
            viewHolder.familiaUno.setVisibility(View.VISIBLE);
        }

        viewHolder.familiaDos.setText("");
        viewHolder.familiaDos.setVisibility(View.GONE);
        if (adapter.getFamilia2() != null) {
            viewHolder.familiaDos.setText(adapter.getFamilia2());
            viewHolder.familiaDos.setVisibility(View.VISIBLE);
        }

        viewHolder.familiaTres.setText("");
        viewHolder.familiaTres.setVisibility(View.GONE);
        if (adapter.getFamilia3() != null) {
            viewHolder.familiaTres.setText(adapter.getFamilia3());
            viewHolder.familiaTres.setVisibility(View.VISIBLE);
        }

        if (onSelected != null) {
            viewHolder.container.setOnClickListener(
                    view -> onSelected.onClick(adapter, viewHolder.getAdapterPosition()));

            viewHolder.container.setOnLongClickListener(
                    view -> onSelected.onLongClick(adapter, viewHolder.getAdapterPosition()));
        }
    }

    @NonNull
    private Comparator<Resultado> comparator() {
        return (v1, v2) -> cleanedName(v1.getTitle()).compareTo(cleanedName(v2.getTitle()));
    }

    public void sort(@NonNull List<Resultado> values) {
        if (values.isEmpty()) {
            return;
        }
        Collections.sort(values, comparator());
    }

    @NonNull
    private String cleanedName(@NonNull String value) {
        value = Normalizer.normalize(value, Normalizer.Form.NFD);
        value = value.replaceAll("[^A-Za-z0-9]", "");
        value = value.trim();
        return value;
    }

    @NonNull
    private String getCharacter(@NonNull String value) {
        value = value.toUpperCase();

        try {
            value = value.substring(0, 1);
        } catch (Exception ignored) {
        }

        return value;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout container;
        final View separador;
        final TextView caracter;
        final TextView codigo;
        final TextView nombre;
        final TextView instalacionPadre;
        final TextView familiaUno;
        final TextView familiaDos;
        final TextView familiaTres;
        final ImageView tipo;
        final TextView equipoPadre;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            separador = itemView.findViewById(R.id.separador);
            caracter = itemView.findViewById(R.id.caracter);
            codigo = itemView.findViewById(R.id.codigo);
            nombre = itemView.findViewById(R.id.nombre);
            instalacionPadre = itemView.findViewById(R.id.instalacion_padre);
            familiaUno = itemView.findViewById(R.id.familia_uno);
            familiaDos = itemView.findViewById(R.id.familia_dos);
            familiaTres = itemView.findViewById(R.id.familia_tres);
            tipo = itemView.findViewById(R.id.tipo);
            equipoPadre = itemView.findViewById(R.id.equipo_padre);
        }
    }

    public interface ViewAdapter<T extends com.mantum.component.adapter.handler.ViewAdapter> extends com.mantum.component.adapter.handler.ViewAdapter<T> {

        @Nullable
        String getFamilia1();

        @Nullable
        String getFamilia2();

        @Nullable
        String getFamilia3();
    }
}