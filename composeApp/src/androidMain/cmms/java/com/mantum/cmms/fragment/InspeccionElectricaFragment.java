package com.mantum.cmms.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mantum.demo.R;
import com.mantum.cmms.adapter.InspeccionCalidadAdapter;
import com.mantum.cmms.domain.Chequeo;
import com.mantum.cmms.domain.InspeccionElectrica;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InspeccionElectricaFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Inspeccion_Electrica";

    private OnCompleteListener onCompleteListener;

    private InspeccionCalidadAdapter inspeccionCalidadAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inspeccion_electrica, container, false);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());

        inspeccionCalidadAdapter = new InspeccionCalidadAdapter(view.getContext());
        inspeccionCalidadAdapter.startAdapter(view, layoutManager);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onCompleteListener.onComplete(KEY_TAB);
    }

    @Override
    public void onAttach(Context context) {
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

    public void iniciar() {
        if (getView() == null) {
            return;
        }

        View view = getView();
        String condiciones = view.getContext().getString(R.string.condiciones_brm);
        List<Chequeo> indoor = Arrays.asList(
                new Chequeo(1L, view.getContext().getString(R.string.rack_equipos), condiciones),
                new Chequeo(2L, view.getContext().getString(R.string.aterrizaje_rack), condiciones),
                new Chequeo(3L, view.getContext().getString(R.string.aterrizaje_equipos), condiciones),
                new Chequeo(4L, view.getContext().getString(R.string.alimentacion_ac_dc), condiciones),
                new Chequeo(5L, view.getContext().getString(R.string.aire_acondicionado), condiciones),
                new Chequeo(6L, view.getContext().getString(R.string.marcacion), condiciones),
                new Chequeo(7L, view.getContext().getString(R.string.aseo), condiciones),
                new Chequeo(8L, view.getContext().getString(R.string.montaje), condiciones),
                new Chequeo(9L, view.getContext().getString(R.string.cable_if), condiciones),
                new Chequeo(10L, view.getContext().getString(R.string.conectores), condiciones),
                new Chequeo(11L, view.getContext().getString(R.string.cableado), condiciones)
        );

        List<Chequeo> condicionesElectricas = Arrays.asList(
                new Chequeo(12L, "", view.getContext().getString(R.string.fase_neutro)),
                new Chequeo(13L, "", view.getContext().getString(R.string.fase_tierra)),
                new Chequeo(14L, "", view.getContext().getString(R.string.neutro_tierra)),
                new Chequeo(15L, "", view.getContext().getString(R.string.frecuencia_voltaje)),
                new Chequeo(16L, "", view.getContext().getString(R.string.fase)),
                new Chequeo(17L, "", view.getContext().getString(R.string.neutro)),
                new Chequeo(18L, "", view.getContext().getString(R.string.tierra)),
                new Chequeo(19L, "", view.getContext().getString(R.string.fase_calibre_cable)),
                new Chequeo(20L, "", view.getContext().getString(R.string.neutro_calibre_cable)),
                new Chequeo(21L, "", view.getContext().getString(R.string.tierra_calibre_cable))
        );

        List<Chequeo> outdoor = Arrays.asList(
                new Chequeo(22L, view.getContext().getString(R.string.fijacion_antena), condiciones),
                new Chequeo(23L, view.getContext().getString(R.string.tipo_montaje), condiciones),
                new Chequeo(24L, view.getContext().getString(R.string.oxidacion), condiciones),
                new Chequeo(25L, view.getContext().getString(R.string.tornilleria), condiciones),
                new Chequeo(26L, view.getContext().getString(R.string.encintado), condiciones),
                new Chequeo(27L, view.getContext().getString(R.string.humedad), condiciones),
                new Chequeo(28L, view.getContext().getString(R.string.semirigidos), condiciones),
                new Chequeo(29L, view.getContext().getString(R.string.montaje), condiciones),
                new Chequeo(30L, view.getContext().getString(R.string.conectores), condiciones),
                new Chequeo(31L, view.getContext().getString(R.string.cableado), condiciones),
                new Chequeo(32L, view.getContext().getString(R.string.aterrizaje_antena), condiciones),
                new Chequeo(33L, view.getContext().getString(R.string.aterrizaje_soporte), condiciones),
                new Chequeo(34L, view.getContext().getString(R.string.torre), condiciones),
                new Chequeo(35L, view.getContext().getString(R.string.pararrayos), condiciones),
                new Chequeo(36L, view.getContext().getString(R.string.linea_vista), condiciones),
                new Chequeo(37L, view.getContext().getString(R.string.marcacion), condiciones)
        );

        String valor = view.getContext().getString(R.string.valor);
        List<Chequeo> operacion = Arrays.asList(
                new Chequeo(38L, view.getContext().getString(R.string.voltaje_agc), valor, true),
                new Chequeo(39L, view.getContext().getString(R.string.nivel_recepcion), valor, true),
                new Chequeo(52L, view.getContext().getString(R.string.nivel_apuntamiento), valor, true),
                new Chequeo(40L, view.getContext().getString(R.string.performance), valor, true),
                new Chequeo(41L, view.getContext().getString(R.string.trafico_wan), valor, true),
                new Chequeo(42L, view.getContext().getString(R.string.troughput_out), valor, true),
                new Chequeo(43L, view.getContext().getString(R.string.troughput_in), valor, true),
                new Chequeo(44L, view.getContext().getString(R.string.alarmas_presentes), valor, true),
                new Chequeo(45L, view.getContext().getString(R.string.alarmas_almacenadas), valor, true),
                new Chequeo(46L, view.getContext().getString(R.string.interfaces_lan), valor, true),
                new Chequeo(47L, view.getContext().getString(R.string.interfaces_wan), valor, true),
                new Chequeo(48L, view.getContext().getString(R.string.tiempo_respuesta_ping), valor, true),
                new Chequeo(49L, view.getContext().getString(R.string.porcentaje_uso_memoria), valor, true),
                new Chequeo(50L, view.getContext().getString(R.string.porcentaje_uso_cpu), valor, true),
                new Chequeo(51L, view.getContext().getString(R.string.errores_input_output), valor, true),
                new Chequeo(53L, "", view.getContext().getString(R.string.compromisos_por_parte_del_cliente))
        );

        inspeccionCalidadAdapter.addAll(Arrays.asList(
                new Chequeo.ListaChequeo(view.getContext().getString(R.string.equipos_indoor), indoor),
                new Chequeo.ListaChequeo(view.getContext().getString(R.string.condiciones_electricas), condicionesElectricas),
                new Chequeo.ListaChequeo(view.getContext().getString(R.string.equipos_outdoor), outdoor),
                new Chequeo.ListaChequeo(view.getContext().getString(R.string.parametros_operacion), operacion)
        ));
    }

    @Nullable
    public InspeccionElectrica getValue(Long idot) {
        if (getView() == null) {
            return null;
        }

        InspeccionElectrica inspeccionElectrica = new InspeccionElectrica();
        inspeccionElectrica.setIdot(idot);

        List<InspeccionElectrica.Aspectos> aspectos = new ArrayList<>();
        for (Chequeo.ListaChequeo listaChequeo : inspeccionCalidadAdapter.getOriginal()) {
            for (Chequeo chequeo : listaChequeo.getChequeos()) {
                InspeccionElectrica.Aspectos aspecto = new InspeccionElectrica.Aspectos();
                aspecto.setId(chequeo.getId());
                aspecto.setAplica(chequeo.getAplica());
                if (!chequeo.isOperacion()) {
                    aspecto.setCondiciones(chequeo.getCondiciones());
                } else {
                    aspecto.setValor(chequeo.getCondiciones());
                }
                aspectos.add(aspecto);
            }
        }

        inspeccionElectrica.setAspectos(aspectos);
        return inspeccionElectrica;
    }
}