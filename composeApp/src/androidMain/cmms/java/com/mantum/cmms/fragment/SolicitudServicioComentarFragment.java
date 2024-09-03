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
import com.mantum.cmms.activity.GaleriaActivity;
import com.mantum.cmms.domain.Comentar;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.service.Photo;
import com.mantum.component.service.PhotoAdapter;

import java.util.ArrayList;
import java.util.List;

public class SolicitudServicioComentarFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Comentar_Solicitud_Servicio";

    private OnCompleteListener onCompleteListener;

    private boolean acciones;

    private List<Photo> photos = new ArrayList<>();

    public SolicitudServicioComentarFragment() {
        this.acciones = true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_solicitud_servicio_comentar, container, false);

        FloatingActionButton cameraFloatingActionButton = view.findViewById(R.id.camara);
        cameraFloatingActionButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSparseParcelableArray(GaleriaActivity.PATH_FILE_PARCELABLE, PhotoAdapter.factory(photos));

            Intent intent = new Intent(getActivity(), GaleriaActivity.class);
            intent.putExtras(bundle);

            if (getActivity() != null) {
                getActivity().startActivityForResult(intent, GaleriaActivity.REQUEST_ACTION);
            }
        });

        Integer versionsoportada = Version.get(view.getContext());
        if ((versionsoportada != null && versionsoportada <= 10) || !acciones) {
            FloatingActionsMenu floatingActionsMenu = view.findViewById(R.id.parent);
            floatingActionsMenu.setVisibility(View.GONE);
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

    public void clearPhoto() {
        photos.clear();
    }

    public void addPhoto(Photo photo) {
        photos.add(photo);
    }

    public void init(@Nullable String codigo) {
        if (getView() == null) {
            return;
        }

        if (codigo != null) {
            TextInputEditText codigoText = getView().findViewById(R.id.codigo);
            codigoText.setText(codigo);
        }
    }

    public void onView(@NonNull Comentar comentar) {
        if (getView() == null) {
            return;
        }

        TextInputEditText codigoText = getView().findViewById(R.id.codigo);
        codigoText.setText(comentar.getCodigo());

        TextInputEditText descripcion = getView().findViewById(R.id.descripcion);
        descripcion.setText(comentar.getDescripcion());
        descripcion.setFocusable(false);
        descripcion.setCursorVisible(false);

        if (photos != null) {
            photos.addAll(comentar.getFiles());
        }
    }

    @Nullable
    public Comentar getValue() {
        if (getView() == null) {
            return null;
        }

        Comentar comentar = new Comentar();
        comentar.setFiles(photos);

        TextInputEditText descripcion = getView().findViewById(R.id.descripcion);
        if (descripcion.getText() != null) {
            comentar.setDescripcion(descripcion.getText().toString());
        }

        return comentar;
    }

    public void ocultarAcciones() {
        acciones = false;
    }
}
