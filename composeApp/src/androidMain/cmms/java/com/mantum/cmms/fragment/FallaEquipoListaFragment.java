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
import com.mantum.cmms.activity.DetalleFallaActivity;
import com.mantum.cmms.entity.Falla;
import com.mantum.cmms.view.FallaEquipo;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.AlphabetAdapter;

import java.util.ArrayList;
import java.util.List;

public class FallaEquipoListaFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Fallas Equipo";

    private AlphabetAdapter<FallaEquipo> alphabetAdapter;

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(com.mantum.component.R.layout.alphabet_layout_view, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        alphabetAdapter = new AlphabetAdapter<>(view.getContext());
        alphabetAdapter.startAdapter(view, layoutManager);
        alphabetAdapter.showMessageEmpty(view, R.string.fallas_mensaje_vacio);

        alphabetAdapter.setOnAction(new OnSelected<FallaEquipo>() {
            @Override
            public void onClick(FallaEquipo value, int position) {
                Intent intent = new Intent(view.getContext(), DetalleFallaActivity.class);
                intent.putExtra(Mantum.KEY_UUID, value.getUUID());
                intent.putExtra("fallaOT", false);
                startActivity(intent);
            }

            @Override
            public boolean onLongClick(FallaEquipo value, int position) {
                return false;
            }
        });

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        alphabetAdapter.clear();
    }

    @NonNull
    @Override
    public String getKey() {
        return KEY_TAB;
    }

    @NonNull
    @Override
    public String getTitle(@NonNull Context context) {
        return context.getString(R.string.tab_fallas_activas);
    }

    public void onRefresh(@NonNull List<Falla> values) {
        if (alphabetAdapter != null) {
            List<FallaEquipo> fallaEquipos = new ArrayList<>();
            for (Falla falla : values) {
                fallaEquipos.add(new FallaEquipo(falla.getUUID(), falla.getId(), falla.getResumen(), falla.getAm(), falla.getDescripcion()));
            }

            alphabetAdapter.addAll(fallaEquipos);
        }

        if (getView() != null) {
            alphabetAdapter.showMessageEmpty(getView(), R.string.fallas_mensaje_vacio);
        }
    }
}
