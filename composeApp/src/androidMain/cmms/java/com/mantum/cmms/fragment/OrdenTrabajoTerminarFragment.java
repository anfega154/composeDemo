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
import com.mantum.demo.R;
import com.mantum.cmms.activity.FirmaActivity;
import com.mantum.cmms.activity.GaleriaActivity;
import com.mantum.cmms.domain.Terminar;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.service.Photo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OrdenTrabajoTerminarFragment extends Mantum.Fragment {

    public static final int REQUEST_ACTION = 1251;

    public final static String KEY_TAB = "Terminar_Orden_Trabajo";

    private OnCompleteListener onCompleteListener;

    private List<Photo> photos = new ArrayList<>();

    private boolean viewMode;

    public OrdenTrabajoTerminarFragment() {
        this.viewMode = false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orden_trabajo_terminar,
                container, false);

        FloatingActionButton firmarActionButton = view.findViewById(R.id.firma);
        firmarActionButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), FirmaActivity.class);
                getActivity().startActivityForResult(intent, FirmaActivity.REQUEST_ACTION);
            }
        });

        FloatingActionButton cameraActionButton = view.findViewById(R.id.camera);
        cameraActionButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(GaleriaActivity.PATH_FILE, Photo.paths(photos));

            Intent intent = new Intent(getActivity(), GaleriaActivity.class);
            intent.putExtras(bundle);

            if (getActivity() != null) {
                getActivity().startActivityForResult(intent, GaleriaActivity.REQUEST_ACTION);
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

    public void onStart(@NonNull Terminar value) {
        onRefresh(value);
    }

    public void onRefresh(@NonNull Terminar value) {
        if (getView() == null) {
            return;
        }

        if (value.getIdot() != null) {
            TextInputEditText id = getView().findViewById(R.id.id);
            id.setText(String.valueOf(value.getIdot()));
        }

        FloatingActionsMenu menu = getView().findViewById(R.id.menu);
        menu.setVisibility(viewMode ? View.GONE : View.VISIBLE);

        TextInputEditText token = getView().findViewById(R.id.token);
        token.setVisibility(viewMode ? View.VISIBLE : View.GONE);
        token.setText(value.getToken());

        TextInputEditText code = getView().findViewById(R.id.code);
        code.setVisibility(viewMode ? View.VISIBLE : View.GONE);
        code.setText(value.getCode());

        TextInputEditText description = getView().findViewById(R.id.description);
        description.setText(value.getReason());
        if (viewMode) {
            description.setFocusable(false);
            description.setCursorVisible(false);
        }
    }

    @Nullable
    public Terminar getValue() {
        if (getView() == null) {
            return null;
        }

        Terminar terminar = new Terminar();
        TextInputEditText id = getView().findViewById(R.id.id);
        if (id.getText() != null) {
            terminar.setIdot(Long.parseLong(id.getText().toString()));
        }

        TextInputEditText code = getView().findViewById(R.id.code);
        if (code.getText() != null) {
            terminar.setCode(code.getText().toString());
        }

        TextInputEditText description = getView().findViewById(R.id.description);
        if (description.getText() != null) {
            terminar.setReason(description.getText().toString());
        }

        terminar.setFiles(photos);
        return terminar;
    }

    public void addPhoto(String file) {
        if (photos != null && file != null && getView() != null) {
            photos.add(new Photo(getView().getContext(), new File(file)));
        }
    }

    public void addPhoto(List<String> files) {
        if (photos != null && files != null && getView() != null) {
            photos.clear();
            for (String file : files) {
                photos.add(new Photo(getView().getContext(), new File(file)));
            }
        }
    }

    public OrdenTrabajoTerminarFragment setViewMode(boolean viewMode) {
        this.viewMode = viewMode;
        return this;
    }
}