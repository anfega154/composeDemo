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
import com.mantum.cmms.entity.Ejecutores;
import com.mantum.component.Mantum;
import com.mantum.component.adapter.AlphabetAdapter;
import com.mantum.component.OnCompleteListener;

import java.util.List;

public class EjecutoresListaFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Ejecutores";

    public boolean tiempos;

    private AlphabetAdapter<Ejecutores> alphabetAdapter;

    private OnCompleteListener onCompleteListener;

    public EjecutoresListaFragment() {
        this.tiempos = false;
    }

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
        if (tiempos) {
            alphabetAdapter.hiddenSummary();
        }

        alphabetAdapter.startAdapter(view, layoutManager);
        alphabetAdapter.showMessageEmpty(view, R.string.ejecutores_mensaje_vacio);
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
        return context.getString(R.string.tab_ejecutores);
    }

    public void mostrarTiempos() {
        this.tiempos = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alphabetAdapter != null) {
            alphabetAdapter.clear();
        }
    }

    public void onRefresh(@NonNull List<Ejecutores> values) {
        if (alphabetAdapter != null) {
            alphabetAdapter.addAll(values);
        }

        if (getView() != null) {
            alphabetAdapter.showMessageEmpty(getView(), R.string.ejecutores_mensaje_vacio);
        }
    }
}