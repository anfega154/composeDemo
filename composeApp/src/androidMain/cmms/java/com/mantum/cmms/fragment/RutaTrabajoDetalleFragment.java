package com.mantum.cmms.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mantum.demo.R;
import com.mantum.cmms.activity.DiligenciarRutaTrabajoActivity;
import com.mantum.cmms.activity.MovimientoAlmacenActivity;
import com.mantum.cmms.entity.ListaChequeo;
import com.mantum.cmms.entity.RutaTrabajo;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

import static com.mantum.cmms.security.Security.TAG;

public class RutaTrabajoDetalleFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Detalle_Ruta_Trabajo";

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_ruta_trabajo,
                container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onCompleteListener.onComplete(KEY_TAB);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        onCompleteListener = (OnCompleteListener) context;
    }

    @NonNull
    @Override
    public String getKey() {
        return KEY_TAB;
    }

    @NonNull
    @Override
    public String getTitle(@NonNull Context context) {
        return context.getString(R.string.tab_general);
    }

    public void onRefresh(@NonNull ListaChequeo value) {
        if (getView() == null) {
            return;
        }

        LinearLayout contenedor = getView().findViewById(R.id.fecha_contenedor);
        contenedor.setVisibility(View.GONE);

        TextView code = getView().findViewById(R.id.codigo);
        code.setText(value.getCodigo());

        TextView nombre = getView().findViewById(R.id.nombre);
        nombre.setText(value.getNombre());

        TextView especialidad = getView().findViewById(R.id.especialidad);
        especialidad.setText(value.getEspecialidad());

        TextView descripcion = getView().findViewById(R.id.descripcion);
        descripcion.setText(value.getDescripcion());
    }

    public void onRefresh(@NonNull RutaTrabajo value) {
        if (getView() == null) {
            return;
        }

        TextView code = getView().findViewById(R.id.codigo);
        code.setText(value.getCodigo());

        TextView nombre = getView().findViewById(R.id.nombre);
        nombre.setText(value.getNombre());

        TextView fecha = getView().findViewById(R.id.fecha);
        fecha.setText(value.getFecha());

        TextView especialidad = getView().findViewById(R.id.especialidad);
        especialidad.setText(value.getEspecialidad());

        TextView descripcion = getView().findViewById(R.id.descripcion);
        descripcion.setText(value.getDescripcion());

        try {
            DiligenciarRutaTrabajoActivity activity = new DiligenciarRutaTrabajoActivity();
            if (UserPermission.check(getContext(), UserPermission.REALIZAR_MOVIMIENTO_RT, false) && !activity.esRegistroFuturo(value.getFecha())) {
                getView().findViewById(R.id.actions).setVisibility(View.VISIBLE);

                getView().findViewById(R.id.actions).setOnClickListener(v -> {
                    Intent intent = new Intent(getContext(), MovimientoAlmacenActivity.class);
                    intent.putExtra("idrt", value.getIdejecucion());
                    intent.putExtra("idgrouprt", value.getId());
                    intent.putExtra("movimiento", "salida");
                    intent.putExtra("tipoElemento", "recursos");
                    startActivity(intent);
                });
            }
        } catch (Exception e) {
            Log.d(TAG, "onRefresh: " + e);
        }
    }
}