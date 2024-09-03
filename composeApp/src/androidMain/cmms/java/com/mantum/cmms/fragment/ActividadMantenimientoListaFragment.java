package com.mantum.cmms.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mantum.R;
import com.mantum.cmms.activity.DetalleActividadMantenimientoActivity;
import com.mantum.cmms.activity.DetalleTareaActivity;
import com.mantum.cmms.entity.Actividad;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.AlphabetAdapter;

import java.util.List;

public class ActividadMantenimientoListaFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Lista_Actividad_Mantenimiento";

    private OnCompleteListener onCompleteListener;

    private AlphabetAdapter<Actividad> alphabetAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(com.mantum.component.R.layout.alphabet_layout_view,
                container, false);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(view.getContext());

        alphabetAdapter = new AlphabetAdapter<>(view.getContext());
        alphabetAdapter.setOnAction(new OnSelected<Actividad>() {

            @Override
            public void onClick(Actividad value, int position) {
                Bundle bundle = new Bundle();
                bundle.putString(Mantum.KEY_ID, value.getUuid());
                bundle.putBoolean(DetalleTareaActivity.TAREA_ENTIDAD_OT, false);

                Intent intent = new Intent(view.getContext(), DetalleActividadMantenimientoActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
            }

            @Override
            public boolean onLongClick(Actividad value, int position) {
                return false;
            }

        });

        alphabetAdapter.startAdapter(view, layoutManager);
        alphabetAdapter.showMessageEmpty(view, R.string.equipo_actividades_vacio);
        return view;
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
        return context.getString(R.string.tab_actividades);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alphabetAdapter != null) {
            alphabetAdapter.clear();
        }
    }

    public void onLoad(@NonNull List<Actividad> values) {
        if (alphabetAdapter != null) {
            alphabetAdapter.addAll(values);
        }

        if (getView() != null) {
            alphabetAdapter.showMessageEmpty(getView(), R.string.equipo_actividades_vacio);
        }
    }
}