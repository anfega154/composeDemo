package com.mantum.cmms.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.mantum.R;
import com.mantum.cmms.activity.RecursosAdicionalesActivity;
import com.mantum.cmms.entity.InformeTecnico;
import com.mantum.cmms.helper.RecursoAdicionalHelper;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

public class InformeTecnicoFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Informe_Tecnico";

    private boolean viewMode;

    private boolean action;

    private OnCompleteListener onCompleteListener;

    private RecursoAdicionalHelper recursoAdicionalHelper = new RecursoAdicionalHelper();

    public InformeTecnicoFragment() {
        this.viewMode = false;
        this.action = true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_informe_tecnico, container, false);

        FloatingActionButton floatingActionButton = view.findViewById(R.id.add_resources);
        floatingActionButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable(RecursosAdicionalesActivity.KEY_RESOURCES, recursoAdicionalHelper);

            Intent intent = new Intent(getActivity(), RecursosAdicionalesActivity.class);
            intent.putExtras(bundle);

            if (getActivity() != null) {
                getActivity().startActivityForResult(intent, RecursosAdicionalesActivity.REQUEST_ACTION);
            }
        });

        if (!action) {
            FloatingActionsMenu floatingActionMenu = view.findViewById(R.id.menu);
            floatingActionMenu.setVisibility(View.GONE);
        }

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
        return context.getString(R.string.tab_general);
    }

    public void setRecursoAdicional(@NonNull RecursoAdicionalHelper recursoAdicionalHelper) {
        this.recursoAdicionalHelper = recursoAdicionalHelper;
    }

    public void onStart(@NonNull InformeTecnico value) {
        if (getView() == null) {
            return;
        }

        if (value.getIdss() != null) {
            TextInputEditText id = getView().findViewById(R.id.id);
            id.setText(String.valueOf(value.getIdss()));
        }

        TextInputEditText code = getView().findViewById(R.id.code);
        code.setText(value.getCodigo());

        TextInputEditText activity = getView().findViewById(R.id.activity);
        activity.setText(value.getActividades());

        TextInputEditText recommendations = getView().findViewById(R.id.recommendations);
        recommendations.setText(value.getRecomendaciones());
    }

    public void onRefresh(InformeTecnico.Request value) {
        if (getView() == null) {
            return;
        }

        TextInputEditText token = getView().findViewById(R.id.token);
        token.setVisibility(viewMode ? View.VISIBLE : View.GONE);
        token.setText(value.getToken());

        TextInputEditText code = getView().findViewById(R.id.code);
        code.setText(value.getCodigo());
        if (viewMode) {
            code.setFocusable(false);
            code.setCursorVisible(false);
        }

        TextInputEditText activity = getView().findViewById(R.id.activity);
        activity.setText(value.getActividades());
        if (viewMode) {
            activity.setFocusable(false);
            activity.setCursorVisible(false);
        }

        TextInputEditText recommendations = getView().findViewById(R.id.recommendations);
        recommendations.setText(value.getRecomendaciones());
        if (viewMode) {
            recommendations.setFocusable(false);
            recommendations.setCursorVisible(false);
        }
    }

    @Nullable
    public InformeTecnico.Request getValue() {
        if (getView() == null) {
            return null;
        }

        InformeTecnico.Request request = new InformeTecnico.Request();
        TextInputEditText id = getView().findViewById(R.id.id);

        request.setIdss(Long.valueOf(id.getText().toString()));

        TextInputEditText code = getView().findViewById(R.id.code);
        request.setCodigo(code.getText().toString());

        TextInputEditText activity = getView().findViewById(R.id.activity);
        request.setActividades(activity.getText().toString());

        TextInputEditText recommendations = getView().findViewById(R.id.recommendations);
        request.setRecomendaciones(recommendations.getText().toString());

        return request;
    }

    public InformeTecnicoFragment setViewMode(@SuppressWarnings("SameParameterValue") boolean viewMode) {
        this.viewMode = viewMode;
        return this;
    }

    public InformeTecnicoFragment setAction(@SuppressWarnings("SameParameterValue") boolean action) {
        this.action = action;
        return this;
    }
}