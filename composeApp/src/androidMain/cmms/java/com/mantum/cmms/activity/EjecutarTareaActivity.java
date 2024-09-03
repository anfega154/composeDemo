package com.mantum.cmms.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mantum.demo.R;
import com.mantum.cmms.adapter.TareaAdapter;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Tarea;
import com.mantum.component.Mantum;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class EjecutarTareaActivity extends Mantum.Activity {

    private static final String TAG = EjecutarTareaActivity.class.getSimpleName();

    public static final String KEY_FORM = "key_tareas_form";

    public static final String ID_AM_TAREA = "id_am_tarea";

    private Database database;

    private String idAm;

    private TareaAdapter<Tarea.TareaHelper> listadoTareaAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_ejecutar_tarea);
            includeBackButtonAndTitle(R.string.ejecutar_tareas_titulo);

            database = new Database(this);
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            listadoTareaAdapter = new TareaAdapter<>(this);
            RecyclerView recyclerViewTareas = findViewById(R.id.recycler_view_tareas);
            recyclerViewTareas.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewTareas.setAdapter(listadoTareaAdapter);

            listadoTareaAdapter.setOnAction((position, checked) -> listadoTareaAdapter.getItemPosition(position).setEjecutada(checked));

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                String tareaJson = bundle.getString(KEY_FORM);
                idAm = bundle.getString(ID_AM_TAREA);

                if (tareaJson == null || tareaJson.equals("")) {
                    backActivity(getString(R.string.listado_tareas_vacio));
                    return;
                }

                Type type = new TypeToken<ArrayList<Tarea.TareaHelper>>() {}.getType();
                List<Tarea.TareaHelper> tareas = new Gson().fromJson(tareaJson, type);
                listadoTareaAdapter.addAll(tareas);
            }
        } catch (Exception e) {
            Log.e(TAG, "onCreate: ", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (database != null) {
            database.close();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_FORM, new Gson().toJson(listadoTareaAdapter.getOriginal()));
        bundle.putString(ID_AM_TAREA, idAm);

        Intent intent = new Intent();
        intent.putExtras(bundle);
        backActivity(intent);
    }
}