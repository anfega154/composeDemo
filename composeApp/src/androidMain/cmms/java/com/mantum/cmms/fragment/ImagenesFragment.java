package com.mantum.cmms.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mantum.R;
import com.mantum.cmms.activity.GaleriaActivity;
import com.mantum.cmms.entity.Adjuntos;
import com.mantum.component.activity.FullScreenView;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.GalleryAdapter;

import java.util.List;

import static com.mantum.component.activity.FullScreenView.EDIT_MODE;
import static com.mantum.component.activity.FullScreenView.PATH_FILE_VIEW_PHOTO;
import static com.mantum.component.activity.FullScreenView.PATH_URI_VIEW_PHOTO;
import static com.mantum.component.activity.FullScreenView.REQUEST_VIEW_PHOTO;

public class ImagenesFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Imagenes";

    private GalleryAdapter<Adjuntos> galleryAdapter;

    private OnCompleteListener onCompleteListener;

    private boolean editar;

    private boolean acciones;

    private Long id;

    private String tipo;

    public ImagenesFragment() {
        this.editar = true;
        this.acciones = false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_imagen, container, false);

        galleryAdapter = new GalleryAdapter<>(view.getContext());
        galleryAdapter.setOnAction(new OnSelected<Adjuntos>() {

            @Override
            public void onClick(Adjuntos value, int position) {
                Intent intent = new Intent(getActivity(), FullScreenView.class);
                String key = value.isExternal() ? PATH_URI_VIEW_PHOTO : PATH_FILE_VIEW_PHOTO;
                intent.putExtra(key, value.getPath());
                intent.putExtra(EDIT_MODE, editar);
                startActivityForResult(intent, REQUEST_VIEW_PHOTO);
            }

            @Override
            public boolean onLongClick(Adjuntos value, int position) {
                return false;
            }

        });

        RecyclerView.LayoutManager layoutManager
                = new GridLayoutManager(getContext(), 2);
        galleryAdapter.startAdapter(view, layoutManager);
        galleryAdapter.showMessageEmpty(view, R.string.adjuntos_mensaje_vacio);

        FloatingActionButton camara = view.findViewById(R.id.camara);
        if (acciones) {
            camara.show();
        }

        camara.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putLong(Mantum.KEY_ID, id);
            bundle.putString(GaleriaActivity.KEY_TIPO_ENTIDAD, tipo);

            Intent intent = new Intent(view.getContext(), GaleriaActivity.class);
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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        onCompleteListener = (OnCompleteListener) context;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (galleryAdapter != null) {
            galleryAdapter.clear();
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
        return context.getString(R.string.tab_imagenes);
    }

    public void onLoad(@NonNull List<Adjuntos> adjuntos) {
        onLoad(null, null, adjuntos);
    }

    public void onLoad(Long id, String tipo, @NonNull List<Adjuntos> adjuntos) {
        this.id = id;
        this.tipo = tipo;
        if (galleryAdapter != null) {
            galleryAdapter.addAll(adjuntos);
        }

        if (getView() != null) {
            galleryAdapter.showMessageEmpty(getView(), R.string.datos_tecnicos_vacio);
        }
    }

    public ImagenesFragment clear() {
        if (galleryAdapter != null && !galleryAdapter.isEmpty()) {
            galleryAdapter.clear();
        }

        if (getView() != null) {
            galleryAdapter.showMessageEmpty(getView(), R.string.datos_tecnicos_vacio);
        }

        return this;
    }

    @NonNull
    public ImagenesFragment incluirAccion() {
        this.acciones = true;
        return this;
    }

    @NonNull
    public ImagenesFragment inactivarModoEditar() {
        this.editar = false;
        return this;
    }
}