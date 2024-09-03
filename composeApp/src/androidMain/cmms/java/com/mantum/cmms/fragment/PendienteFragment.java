package com.mantum.cmms.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mantum.demo.R;
import com.mantum.cmms.entity.PendienteMantenimiento;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

public class PendienteFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Pendiente_Mantenimiento";

    private OnCompleteListener onCompleteListener;

    private boolean modoVer;

    public PendienteFragment() {
        this.modoVer = false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_pendiente_formulario,
                container, false);

        TextInputLayout time = view.findViewById(R.id.time_container);
        if (!Version.check(view.getContext(), 8)) {
            time.setVisibility(View.GONE);
        }

        TextInputLayout activity = view.findViewById(R.id.activity_container);
        if (!Version.check(view.getContext(), 8)) {
            activity.setVisibility(View.GONE);
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
        return context.getString(R.string.tab_pendientes);
    }

    public void onStart(@NonNull PendienteMantenimiento.Request value) {
        onRefresh(value);
    }

    public void onRefresh(@NonNull PendienteMantenimiento.Request value) {
        if (getView() == null) {
            return;
        }

        TextInputEditText time = getView().findViewById(R.id.time);
        if (value.getTiempoestimado() != null) {
            time.setText(String.valueOf(value.getTiempoestimado()));
        }

        if (modoVer) {
            time.setFocusable(false);
            time.setCursorVisible(false);
        }

        TextView descripcion = getView().findViewById(R.id.description);
        descripcion.setText(value.getDescripcion());
        if (modoVer) {
            descripcion.setFocusable(false);
            descripcion.setCursorVisible(false);
        }

        TextInputEditText activity = getView().findViewById(R.id.activity);
        activity.setText(value.getActividad());
        if (modoVer) {
            activity.setFocusable(false);
            activity.setCursorVisible(false);
        }
    }

    @Nullable
    public PendienteMantenimiento.Request getValue() {
        if (getView() == null) {
            return null;
        }

        PendienteMantenimiento.Request request
                = new PendienteMantenimiento.Request();

        TextView descripcion = getView().findViewById(R.id.description);
        request.setDescripcion(descripcion.getText().toString());

        TextView activity = getView().findViewById(R.id.activity);
        request.setActividad(activity.getText().toString());

        TextView time = getView().findViewById(R.id.time);
        if (time.getText() != null && !time.getText().toString().isEmpty()) {
            request.setTiempoestimado(Float.valueOf(time.getText().toString()));
        }

        return request;
    }

    public PendienteFragment activarModoVer() {
        this.modoVer = true;
        return this;
    }
}