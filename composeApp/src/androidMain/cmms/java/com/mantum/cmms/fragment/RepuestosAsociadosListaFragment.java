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
import com.mantum.cmms.entity.RepuestoManual;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.adapter.AlphabetAdapter;

import java.util.List;

public class RepuestosAsociadosListaFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Respuestos_Asociados";

    private AlphabetAdapter<RepuestoManual> alphabetAdapter;

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(com.mantum.demo.R.layout.alphabet_layout_view, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        alphabetAdapter = new AlphabetAdapter<>(view.getContext());
        alphabetAdapter.startAdapter(view, layoutManager);
        alphabetAdapter.showMessageEmpty(view, R.string.repuestos_mensaje_vacio);

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
        return context.getString(R.string.tab_repuestos_asociados_ot);
    }

    public void onLoad(@NonNull List<RepuestoManual> values) {
        if (alphabetAdapter != null) {
            alphabetAdapter.addAll(values);
        }

        if (getView() != null) {
            alphabetAdapter.showMessageEmpty(getView(), R.string.repuestos_mensaje_vacio);
        }
    }
}
