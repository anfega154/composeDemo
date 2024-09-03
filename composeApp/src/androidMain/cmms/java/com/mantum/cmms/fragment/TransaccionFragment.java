package com.mantum.cmms.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mantum.demo.R;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TransaccionFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Transaccion";

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaccion, container, false);
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
        return context.getString(R.string.tab_transaccion);
    }

    public void onRefresh(@NonNull Transaccion value) {
        if (getView() == null) {
            return;
        }

        TextInputEditText account = getView().findViewById(R.id.account);
        account.setText(value.getCuenta().getUsername());

        SimpleDateFormat simpleDateFormat
                = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        if (value.getCreation() != null) {
            TextInputEditText creation = getView().findViewById(R.id.creation);
            creation.setText(simpleDateFormat.format(value.getCreation()));
        }

        if (value.getSend() != null) {
            TextInputEditText send = getView().findViewById(R.id.send);
            send.setText(simpleDateFormat.format(value.getSend()));
        }

        if (value.getFecharespuesta() != null) {
            TextInputEditText fechaRespuesta = getView().findViewById(R.id.fecha_respuesta);
            fechaRespuesta.setText(simpleDateFormat.format(value.getFecharespuesta()));
        }

        TextInputEditText url = getView().findViewById(R.id.url);
        url.setText(value.getUrl());

        TextInputEditText version = getView().findViewById(R.id.version);
        version.setText(value.getVersion());

        TextInputEditText versionApp = getView().findViewById(R.id.version_app);
        versionApp.setText(Mantum.versionName(getView().getContext()));

        TextInputEditText state = getView().findViewById(R.id.state);
        state.setText(value.getEstado());

        TextInputEditText description = getView().findViewById(R.id.description);
        description.setText(value.getDescription());

        TextInputEditText body = getView().findViewById(R.id.body);
        body.setText(Mantum.toPrettyFormat(value.getValue()));

        TextInputEditText response = getView().findViewById(R.id.response);
        response.setText(Mantum.toPrettyFormat(value.getRespuesta()));
    }
}