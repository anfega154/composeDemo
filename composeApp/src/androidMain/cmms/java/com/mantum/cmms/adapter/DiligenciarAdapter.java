package com.mantum.cmms.adapter;

import android.content.Context;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mantum.demo.R;
import com.mantum.cmms.domain.Diligenciar;
import com.mantum.cmms.entity.Actividad;
import com.mantum.cmms.entity.Entidad;
import com.mantum.cmms.entity.Variable;
import com.mantum.component.Mantum;
import com.mantum.component.OnCall;

import java.util.Collections;
import java.util.List;

public class DiligenciarAdapter extends Mantum.Adapter<Entidad, DiligenciarAdapter.ViewHolder> {

    private Integer menu;

    private OnCall<Entidad> onCall;

    private Diligenciar diligenciar;

    private final boolean validarQR;

    private boolean isMostrarObservacion;

    private boolean modoVer;

    public DiligenciarAdapter(@NonNull Context context, boolean validarQR) {
        super(context);
        this.validarQR = validarQR;
        this.modoVer = false;
        this.isMostrarObservacion = true;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.adapter_diligenciar_ruta_trabajo, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Entidad value = getItemPosition(position);
        if (value == null) {
            return;
        }

        holder.codigo.setText(value.getCodigo());
        holder.titulo.setText(value.getNombre());
        Integer icon = value.getDrawable();
        if (icon != null) {
            holder.icono.setImageResource(icon);
        }

        if (menu != null) {
            holder.more.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(context, holder.more);
                popupMenu.getMenuInflater().inflate(menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(item -> onCall.onSelected(item, value));
                popupMenu.show();
            });
        } else {
            holder.more.setVisibility(View.GONE);
        }

        List<Actividad> actividades = value.getActividades();
        for (Actividad actividad : actividades) {
            for (Variable variable : actividad.getVariables()) {
                variable.setIdentidad(value.getId());
                variable.setTipoentidad(value.getTipo());
                variable.setEntidad(value.getNombre());
                variable.setIdActividad(actividad.getId());
            }
        }

        Collections.sort(actividades, DiligenciarAdapter::compare);
        ActividadAdapter actividadAdapter
                = new ActividadAdapter(context, value, validarQR);

        if (diligenciar != null) {
            actividadAdapter.agregarEjecutadas(diligenciar.getAms());
            actividadAdapter.agregarValoresVariable(diligenciar.getVariables());
        }

        if (modoVer) {
            actividadAdapter.activarModoVer();
        }

        if (!isMostrarObservacion) {
            actividadAdapter.ocultarObservacion();
        }

        actividadAdapter.addAll(actividades);
        actividadAdapter.startAdapter(holder.view, new LinearLayoutManager(context));
    }

    public void setDiligenciar(@Nullable Diligenciar diligenciar) {
        this.diligenciar = diligenciar;
    }

    private static int compare(@NonNull Actividad a, @NonNull Actividad b) {
        if (a.getOrden() == b.getOrden()) {
            return 0;
        }
        return a.getOrden() > b.getOrden() ? 1 : -1;
    }

    public void setOnCall(@NonNull OnCall<Entidad> onCall) {
        this.onCall = onCall;
    }

    public void setMenu(@MenuRes int menu) {
        this.menu = menu;
    }

    public void activarModoVer() {
        this.modoVer = true;
    }

    public void ocultarObservacion() {
        this.isMostrarObservacion = false;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final View view;

        final ImageView icono;

        final TextView codigo;

        final TextView titulo;

        final ImageButton more;

        ViewHolder(final View itemView) {
            super(itemView);
            this.view = itemView;
            this.icono = itemView.findViewById(R.id.icono);
            this.codigo = itemView.findViewById(R.id.codigo);
            this.titulo = itemView.findViewById(R.id.titulo);
            this.more = itemView.findViewById(R.id.more);
        }
    }
}
