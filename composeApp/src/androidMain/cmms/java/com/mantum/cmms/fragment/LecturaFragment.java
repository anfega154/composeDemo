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
import com.mantum.cmms.adapter.LecturaAdapter;
import com.mantum.cmms.entity.Variable;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

import java.util.List;

public class LecturaFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Lecturas";

    private OnCompleteListener onCompleteListener;

    private LecturaAdapter adapter;

    private boolean readonly;

    public LecturaFragment() {
        this.readonly = false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(com.mantum.demo.R.layout.simple_layout_view,
                container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        adapter = new LecturaAdapter(view.getContext(), false, readonly);
        adapter.startAdapter(view, layoutManager);
        adapter.showMessageEmpty(view, R.string.variables_mensaje_vacio, R.drawable.variable);
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
        return context.getString(R.string.lectura);
    }

    public void onRefresh(List<Variable> variables) {
        adapter.addAll(variables);
        if (getView() != null) {
            adapter.showMessageEmpty(getView(), R.string.variables_mensaje_vacio, R.drawable.variable);
        }
    }

    public LecturaFragment setReadonly(boolean readonly) {
        this.readonly = readonly;
        return this;
    }
}