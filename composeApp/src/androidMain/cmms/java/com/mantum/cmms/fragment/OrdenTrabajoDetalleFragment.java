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

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.mantum.demo.R;
import com.mantum.cmms.activity.MovimientoActivity;
import com.mantum.cmms.activity.MovimientoAlmacenActivity;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

public class OrdenTrabajoDetalleFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Detalle_Orden_Trabajo";

    private static final String TAG = OrdenTrabajoDetalleFragment.class.getSimpleName();

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_orden_trabajo,
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

    public void onRefresh(@NonNull OrdenTrabajo value) {
        if (getView() == null) {
            return;
        }

        TextView code = getView().findViewById(R.id.codigo);
        code.setText(value.getCodigo());

        TextView start = getView().findViewById(R.id.fecha_inicio);
        start.setText(value.getFechainicio());

        TextView end = getView().findViewById(R.id.fecha_final);
        end.setText(value.getFechafin());

        TextView prioridad = getView().findViewById(R.id.prioridad);
        prioridad.setText(value.getPrioridad());

        TextView estado = getView().findViewById(R.id.estado);
        estado.setText(value.getEstado());

        TextView cliente = getView().findViewById(R.id.cliente);
        cliente.setText(value.getCliente());

        TextView porcentaje = getView().findViewById(R.id.porcentaje);
        porcentaje.setText(value.getPorcentaje());

        boolean realizarMovimiento = UserPermission.check(getView().getContext(), UserPermission.REALIZAR_MOVIMIENTO_OT, false);
        boolean realizarInstalacionRetiro = UserPermission.check(getView().getContext(), UserPermission.REALIZAR_INSTALACION_RETIRO, false);

        if (realizarMovimiento || realizarInstalacionRetiro) {
            getView().findViewById(R.id.floating).setVisibility(View.VISIBLE);

            if (realizarMovimiento) {
                FloatingActionButton entradaRecursos = getView().findViewById(R.id.entradaRecursos);
                FloatingActionButton salidaRecursos = getView().findViewById(R.id.salidaRecursos);

                entradaRecursos.setVisibility(View.VISIBLE);
                salidaRecursos.setVisibility(View.VISIBLE);

                entradaRecursos.setOnClickListener(view -> {
                    try {
                        Intent intent = new Intent(this.getContext(), MovimientoAlmacenActivity.class);
                        intent.putExtra("idot", value.getId());
                        intent.putExtra("movimiento", "entrada");
                        intent.putExtra("tipoElemento", "recursos");
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.d(TAG, "onRefresh: " + e);
                    }
                });

                salidaRecursos.setOnClickListener(view -> {
                    try {
                        Intent intent = new Intent(this.getContext(), MovimientoAlmacenActivity.class);
                        intent.putExtra("idot", value.getId());
                        intent.putExtra("movimiento", "salida");
                        intent.putExtra("tipoElemento", "recursos");
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.d(TAG, "onRefresh: " + e);
                    }
                });
            }

            if (realizarInstalacionRetiro) {
                FloatingActionButton movimientoOT = getView().findViewById(R.id.actions);
                movimientoOT.setVisibility(View.VISIBLE);

                movimientoOT.setOnClickListener(v -> {
                    try {
                        Bundle bundleM = new Bundle();
                        bundleM.putLong(com.mantum.component.Mantum.KEY_ID, value.getId());

                        Intent intentM = new Intent(getView().getContext(), MovimientoActivity.class);
                        intentM.putExtras(bundleM);
                        startActivity(intentM);
                    } catch (Exception e) {
                        Log.d(TAG, "onCreateView: " + e);
                    }
                });
            }
        }

        TextView duracion = getView().findViewById(R.id.duracion);
        duracion.setText(value.getDuracion());
        if (value.esCerrada()) {
            LinearLayout contenedor = getView().findViewById(R.id.duracion_contenedor);
            contenedor.setVisibility(View.VISIBLE);
        }

        if (value.getDescripcion() != null && !value.getDescripcion().isEmpty()) {
            TextView descripcion = getView().findViewById(R.id.descripcion);
            descripcion.setText(value.getDescripcion().trim());
        }

        if (value.getRealimentacion() != null && !value.getRealimentacion().isEmpty()) {
            TextView realimentacion = getView().findViewById(R.id.realimentacion);
            realimentacion.setText(value.getRealimentacion().replace("\n", "\n \n"));
        }
    }
}