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
import com.mantum.cmms.activity.DetalleOrdenTrabajoActivity;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.AlphabetAdapter;

import java.util.List;

public class OrdenTrabajoListaFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Orden_Trabajo_Lista";

    private AlphabetAdapter<OrdenTrabajo> alphabetAdapter;

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(com.mantum.component.R.layout.alphabet_layout_view,
                container, false);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(view.getContext());

        alphabetAdapter = new AlphabetAdapter<>(view.getContext());
        alphabetAdapter.setOnAction(new OnSelected<OrdenTrabajo>() {

            @Override
            public void onClick(OrdenTrabajo value, int position) {
                Bundle bundle = new Bundle();
                bundle.putString(Mantum.KEY_UUID, value.getUUID());
                bundle.putLong(Mantum.KEY_ID, value.getId());
                bundle.putBoolean(DetalleOrdenTrabajoActivity.INCLUIR_ACCIONES, false);

                Intent intent = new Intent(
                        view.getContext(), DetalleOrdenTrabajoActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
            }

            @Override
            public boolean onLongClick(OrdenTrabajo value, int position) {
                return false;
            }
        });
        alphabetAdapter.startAdapter(view, layoutManager);
        alphabetAdapter.showMessageEmpty(view, R.string.orden_trabajo_vacio);
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
        return context.getString(R.string.tab_historico_OT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alphabetAdapter != null) {
            alphabetAdapter.clear();
        }
    }

    public void onLoad(@NonNull List<OrdenTrabajo> values) {
        if (alphabetAdapter != null) {
            alphabetAdapter.addAll(values);
        }

        if (getView() != null) {
            alphabetAdapter.showMessageEmpty(getView(), R.string.orden_trabajo_vacio);
        }
    }
}
