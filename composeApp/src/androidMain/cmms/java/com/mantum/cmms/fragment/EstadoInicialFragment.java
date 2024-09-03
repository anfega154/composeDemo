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
import com.mantum.cmms.entity.EstadoInicial;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

public class EstadoInicialFragment extends Mantum.Fragment {

    public static final int REQUEST_ACTION = 1299;

    public final static String KEY_TAB = "Estado_Inicial";

    private OnCompleteListener onCompleteListener;

    private boolean viewMode;

    public EstadoInicialFragment() {
        this.viewMode = false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_estado_inicial, container, false);
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

    public void onStart(@NonNull EstadoInicial value) {
        if (getView() == null) {
            return;
        }

        TextInputEditText id = getView().findViewById(R.id.id);
        id.setText(String.valueOf(value.getIdss()));

        TextInputEditText code = getView().findViewById(R.id.code);
        code.setText(value.getCodigo());

        TextInputEditText marca = getView().findViewById(R.id.marca);
        marca.setText(value.getMarca());

        TextInputEditText type = getView().findViewById(R.id.type);
        type.setText(value.getTipofalla());

        TextInputEditText client = getView().findViewById(R.id.client);
        client.setText(value.getDenominacion());

        TextInputEditText product = getView().findViewById(R.id.product);
        product.setText(value.getNumeroproducto());

        TextInputEditText serial = getView().findViewById(R.id.serial);
        serial.setText(value.getNumeroserial());

        TextInputEditText characteristics = getView().findViewById(R.id.characteristics);
        characteristics.setText(value.getCaracteristicas());

        TextInputEditText state = getView().findViewById(R.id.state);
        state.setText(value.getEstado());
    }

    public void onRefresh(EstadoInicial.Request value) {
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

        TextInputEditText marca = getView().findViewById(R.id.marca);
        marca.setText(value.getMarca());
        if (viewMode) {
            marca.setFocusable(false);
            marca.setCursorVisible(false);
        }

        TextInputEditText type = getView().findViewById(R.id.type);
        type.setText(value.getTipofalla());
        if (viewMode) {
            type.setFocusable(false);
            type.setCursorVisible(false);
        }

        TextInputEditText client = getView().findViewById(R.id.client);
        client.setText(value.getDenominacion());
        if (viewMode) {
            client.setFocusable(false);
            client.setCursorVisible(false);
        }

        TextInputEditText product = getView().findViewById(R.id.product);
        product.setText(value.getNumeroproducto());
        if (viewMode) {
            product.setFocusable(false);
            product.setCursorVisible(false);
        }

        TextInputEditText serial = getView().findViewById(R.id.serial);
        serial.setText(value.getNumeroserial());
        if (viewMode) {
            serial.setFocusable(false);
            serial.setCursorVisible(false);
        }

        TextInputEditText characteristics = getView().findViewById(R.id.characteristics);
        characteristics.setText(value.getCaracteristicas());
        if (viewMode) {
            characteristics.setFocusable(false);
            characteristics.setCursorVisible(false);
        }

        TextInputEditText state = getView().findViewById(R.id.state);
        state.setText(value.getEstado());
        if (viewMode) {
            state.setFocusable(false);
            state.setCursorVisible(false);
        }
    }

    @Nullable
    public EstadoInicial.Request getValue() {
        if (getView() == null) {
            return null;
        }

        EstadoInicial.Request estadoInicial = new EstadoInicial.Request();
        TextInputEditText id = getView().findViewById(R.id.id);
        estadoInicial.setIdss(Long.valueOf(id.getText().toString()));

        TextInputEditText code = getView().findViewById(R.id.code);
        estadoInicial.setCodigo(code.getText().toString());

        TextInputEditText type = getView().findViewById(R.id.type);
        estadoInicial.setTipofalla(type.getText().toString());

        TextInputEditText marca = getView().findViewById(R.id.marca);
        estadoInicial.setMarca(marca.getText().toString());

        TextInputEditText client = getView().findViewById(R.id.client);
        estadoInicial.setDenominacion(client.getText().toString());

        TextInputEditText product = getView().findViewById(R.id.product);
        estadoInicial.setNumeroproducto(product.getText().toString());

        TextInputEditText serial = getView().findViewById(R.id.serial);
        estadoInicial.setNumeroserial(serial.getText().toString());

        TextInputEditText characteristics = getView().findViewById(R.id.characteristics);
        estadoInicial.setCaracteristicas(characteristics.getText().toString());

        TextInputEditText state = getView().findViewById(R.id.state);
        estadoInicial.setEstado(state.getText().toString());

        return estadoInicial;
    }

    public EstadoInicialFragment setViewMode(@SuppressWarnings("SameParameterValue") boolean viewMode) {
        this.viewMode = viewMode;
        return this;
    }
}