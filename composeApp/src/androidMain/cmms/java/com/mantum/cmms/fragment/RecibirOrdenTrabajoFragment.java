package com.mantum.cmms.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.mantum.demo.R;
import com.mantum.cmms.activity.FirmaActivity;
import com.mantum.cmms.domain.RecibirOT;
import com.mantum.cmms.entity.parameter.StateReceive;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.service.Photo;

import java.io.File;
import java.util.List;

public class RecibirOrdenTrabajoFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "RecibirOrdenTrabajo";

    private Photo photo;
    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recibir_orden_trabajo, container, false);

        FloatingActionButton firma = view.findViewById(R.id.firma);
        if (firma != null) {
            firma.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), FirmaActivity.class);
                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, FirmaActivity.REQUEST_ACTION);
                }
            });
        }

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
        return context.getString(R.string.tab_general);
    }

    public void onStart(@NonNull List<StateReceive> value) {
        if (getView() == null) {
            return;
        }

        ArrayAdapter<StateReceive> adapter = new ArrayAdapter<>(
                getView().getContext(), android.R.layout.simple_spinner_dropdown_item, value);

        AppCompatSpinner appCompatSpinner = getView().findViewById(R.id.estados);
        appCompatSpinner.setAdapter(adapter);
    }

    @Nullable
    public RecibirOT getValue() {
        if (getView() == null) {
            return null;
        }

        RecibirOT result = new RecibirOT();
        result.setId(null);
        result.setFiles(photo);

        AppCompatSpinner appCompatSpinner = (AppCompatSpinner) getView().findViewById(R.id.estados);
        if (appCompatSpinner != null && appCompatSpinner.getSelectedItem() != null) {
            result.setStatereceive(appCompatSpinner.getSelectedItem().toString());
        }

        TextInputEditText description = getView().findViewById(R.id.description);
        if (description != null && description.getText() != null) {
            result.setReason(description.getText().toString());
        }

        return result;
    }

    public void addPhoto(String file) {
        if (getView() == null) {
            return;
        }
        photo = new Photo(getView().getContext(), new File(file));
    }
}
