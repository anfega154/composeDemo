package com.mantum.cmms.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mantum.R;
import com.mantum.cmms.entity.Consumible;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.adapter.AlphabetAdapter;

import java.util.List;

public class ConsumiblesManualesListaFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Consumibles_Manuales";

    private AlphabetAdapter<Consumible> alphabetAdapter;

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(com.mantum.component.R.layout.alphabet_layout_view, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        alphabetAdapter = new AlphabetAdapter<>(view.getContext());
        alphabetAdapter.startAdapter(view, layoutManager);
        alphabetAdapter.showMessageEmpty(view, R.string.consumibles_manuales_mensaje_vacio);

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
        return context.getString(R.string.tab_consumibles_manuales);
    }

    public void onLoad(@NonNull List<Consumible> values) {
        if (alphabetAdapter != null) {
            alphabetAdapter.addAll(values);
        }

        if (getView() != null) {
            alphabetAdapter.showMessageEmpty(getView(), R.string.consumibles_manuales_mensaje_vacio);
        }
    }
}
