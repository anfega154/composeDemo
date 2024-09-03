package com.mantum.cmms.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mantum.R;
import com.mantum.cmms.domain.Coordenada;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class UbicacionDetalleFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Ubicacion";

    private OnCompleteListener onCompleteListener;

    private boolean modePrincipal;

    public UbicacionDetalleFragment() {
        this.modePrincipal = true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ubicacion,
                container, false);
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
        return context.getString(R.string.tab_ubicacion);
    }

    public void onRefresh(@NonNull Coordenada value) {
        if (getView() == null) {
            return;
        }

        if (modePrincipal) {
            TextInputEditText token = getView().findViewById(R.id.token);
            token.setVisibility(View.VISIBLE);
            token.setText(value.getToken());

            if (value.getDatetime() != null) {
                SimpleDateFormat simpleDateFormat
                        = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                TextInputEditText fecha = getView().findViewById(R.id.fecha);
                fecha.setVisibility(View.VISIBLE);
                fecha.setText(simpleDateFormat.format(value.getDatetime()));
            }
        }

        TextInputEditText altitude = getView().findViewById(R.id.altitude);
        altitude.setText(String.valueOf(value.getAltitude()));

        TextInputEditText latitude = getView().findViewById(R.id.latitude);
        latitude.setText(String.valueOf(value.getLatitude()));

        TextInputEditText longitude = getView().findViewById(R.id.longitude);
        longitude.setText(String.valueOf(value.getLongitude()));

        TextInputEditText accuracy = getView().findViewById(R.id.accuracy);
        accuracy.setText(String.valueOf(value.getAccuracy()));
    }

    public UbicacionDetalleFragment setModePrincipal(boolean modePrincipal) {
        this.modePrincipal = modePrincipal;
        return this;
    }
}