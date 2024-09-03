package com.mantum.cmms.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mantum.demo.R;
import com.mantum.cmms.entity.DatosTecnico;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.adapter.AlphabetAdapter;

import java.util.List;

public class DatosTecnicosListaFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Datos_Tecnico";

    private AlphabetAdapter<DatosTecnico> alphabetAdapter;

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(com.mantum.demo.R.layout.alphabet_layout_view,
                container, false);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(view.getContext());

        alphabetAdapter = new AlphabetAdapter<>(view.getContext());
        alphabetAdapter.startAdapter(view, layoutManager);
        alphabetAdapter.showMessageEmpty(view, R.string.datos_tecnicos_vacio);
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
        return context.getString(R.string.tab_datos_tecnicos);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alphabetAdapter != null) {
            alphabetAdapter.clear();
        }
    }

    public void onLoad(@NonNull List<DatosTecnico> values) {
        if (alphabetAdapter != null) {
            if (alphabetAdapter.isEmpty()) {
                alphabetAdapter.addAll(values);
            }

            if (getView() != null) {
                alphabetAdapter.showMessageEmpty(getView(), R.string.datos_tecnicos_vacio);
            }
        }
    }
}