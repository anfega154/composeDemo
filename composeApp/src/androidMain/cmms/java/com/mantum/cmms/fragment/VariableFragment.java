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

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.mantum.demo.R;
import com.mantum.cmms.activity.LecturaActivity;
import com.mantum.cmms.entity.Variable;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.adapter.AlphabetAdapter;

import java.util.List;

public class VariableFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Variables";

    private AlphabetAdapter<Variable> alphabetAdapter;

    private OnCompleteListener onCompleteListener;

    private boolean acciones;

    private Long id;

    private String tipo;

    public VariableFragment() {
        this.acciones = false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_variable_listar, container, false);

        alphabetAdapter = new AlphabetAdapter<>(view.getContext());
        alphabetAdapter.startAdapter(view, new LinearLayoutManager(view.getContext()));
        alphabetAdapter.showMessageEmpty(view, R.string.variables_mensaje_vacio);

        FloatingActionsMenu actions = view.findViewById(R.id.acciones);
        actions.setVisibility(acciones ? View.VISIBLE : View.GONE);

        FloatingActionButton lectura = view.findViewById(R.id.registrar_lectura);
        lectura.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putLong(Mantum.KEY_ID, id);
            bundle.putString(LecturaActivity.KEY_TYPE, tipo);
            bundle.putString(LecturaActivity.KEY_TYPE_ACTION, tipo);

            Intent intent = new Intent(view.getContext(), LecturaActivity.class);
            intent.putExtras(bundle);

            startActivity(intent);
        });

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
        return context.getString(R.string.tab_variables);
    }

    public void onLoad(@NonNull List<Variable> variables) {
        onLoad(null, null, variables);
    }

    public void onLoad(Long id, String tipo, @NonNull List<Variable> variables) {
        this.id = id;
        this.tipo = tipo;
        if (alphabetAdapter != null) {
            alphabetAdapter.addAll(variables);
            alphabetAdapter.sort();
        }

        if (getView() != null) {
            alphabetAdapter.showMessageEmpty(getView(), R.string.variables_mensaje_vacio);
        }
    }

    @NonNull
    public VariableFragment incluirAccion() {
        this.acciones = true;
        return this;
    }
}