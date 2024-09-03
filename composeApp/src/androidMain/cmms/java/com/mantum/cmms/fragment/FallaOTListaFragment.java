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

import com.mantum.demo.R;
import com.mantum.cmms.activity.DetalleFallaActivity;
import com.mantum.cmms.entity.Falla;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.AlphabetAdapter;

import java.util.List;

public class FallaOTListaFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Fallas OT";

    private AlphabetAdapter<Falla> alphabetAdapter;

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(com.mantum.demo.R.layout.alphabet_layout_view, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        alphabetAdapter = new AlphabetAdapter<>(view.getContext());
        alphabetAdapter.startAdapter(view, layoutManager);
        alphabetAdapter.showMessageEmpty(view, R.string.fallas_mensaje_vacio);

        alphabetAdapter.setOnAction(new OnSelected<Falla>() {
            @Override
            public void onClick(Falla value, int position) {
                Intent intent = new Intent(view.getContext(), DetalleFallaActivity.class);
                intent.putExtra(Mantum.KEY_UUID, value.getUUID());
                intent.putExtra("fallaOT", true);
                startActivity(intent);
            }

            @Override
            public boolean onLongClick(Falla value, int position) {
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
        if (alphabetAdapter != null) {
            alphabetAdapter.clear();
        }
    }

    @NonNull
    @Override
    public String getKey() {
        return KEY_TAB;
    }

    @NonNull
    @Override
    public String getTitle(@NonNull Context context) {
        return context.getString(R.string.tab_fallas);
    }

    public void onRefresh(@NonNull List<Falla> values) {
        if (alphabetAdapter != null) {
            alphabetAdapter.addAll(values);
        }

        if (getView() != null) {
            alphabetAdapter.showMessageEmpty(getView(), R.string.fallas_mensaje_vacio);
        }
    }
}
