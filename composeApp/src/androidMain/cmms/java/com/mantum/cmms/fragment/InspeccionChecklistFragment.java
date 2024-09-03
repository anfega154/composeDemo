package com.mantum.cmms.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mantum.R;
import com.mantum.cmms.adapter.PreguntaAdapter;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Checklist;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Seccion;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.OnSelected;

import java.util.ArrayList;
import java.util.List;

public class InspeccionChecklistFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "InspeccionChecklistFragment";

    private Database database;
    private OnCompleteListener onCompleteListener;
    private PreguntaAdapter<Seccion, Checklist> adapterView;

    private boolean readOnly = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_inspeccion_checklist, container, false);

        database = new Database(view.getContext());
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        adapterView = new PreguntaAdapter<>(view.getContext());
        adapterView.setReadOnly(readOnly);
        adapterView.setOnPositive(new OnSelected<Seccion>() {
            @Override
            public void onClick(Seccion value, int position) {
                value.setChecked(true);
                for (Checklist child : value.getChildren()) {
                    child.setChecked(true);
                }
                adapterView.notifyItemChanged(position);
            }

            @Override
            public boolean onLongClick(Seccion value, int position) {
                return false;
            }
        });
        adapterView.setOnNegative(new OnSelected<Seccion>() {
            @Override
            public void onClick(Seccion value, int position) {
                value.setChecked(false);
                for (Checklist child : value.getChildren()) {
                    child.setChecked(false);
                }
                adapterView.notifyItemChanged(position);
            }

            @Override
            public boolean onLongClick(Seccion value, int position) {
                return false;
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        adapterView.startAdapter(view, layoutManager);

        if (cuenta != null) {
            List<Seccion> secciones = database.where(Seccion.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findAll();
            adapterView.addAll(database.copyFromRealm(secciones));
        }

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
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
        return context.getString(R.string.tab_checklist);
    }

    public InspeccionChecklistFragment setReadOnly(boolean value) {
        this.readOnly = value;
        return this;
    }

    public InspeccionChecklistFragment onLoad(List<Seccion.Pregunta> preguntas) {
        if (getView() == null || preguntas == null) {
            return this;
        }

        Log.e("TAG", "onLoad: " + preguntas );
        adapterView.clear();
        adapterView.addAll(Seccion.convert(preguntas));
        return this;
    }

    public List<Seccion.Pregunta> getValue() {
        List<Seccion.Pregunta> preguntas = new ArrayList<>();
        for (Seccion seccion : adapterView.getOriginal()) {

            List<Checklist.Respuesta> respuestas = new ArrayList<>();
            for (Checklist checklist : seccion.getChecklist()) {
                Checklist.Respuesta respuesta = new Checklist.Respuesta();
                respuesta.setId(checklist.getId());
                respuesta.setEnglish(checklist.getEnglish());
                respuesta.setSpanish(checklist.getSpanish());
                respuesta.setChecked(checklist.getChecked());
                respuestas.add(respuesta);
            }

            Seccion.Pregunta pregunta = new Seccion.Pregunta();
            pregunta.setId(seccion.getId());
            pregunta.setCode(seccion.getCode());
            pregunta.setSpanish(seccion.getSpanish());
            pregunta.setEnglish(seccion.getEnglish());
            pregunta.setChecked(seccion.getChecked());
            pregunta.setRespuestas(respuestas);
            preguntas.add(pregunta);
        }

        return preguntas;
    }
}
