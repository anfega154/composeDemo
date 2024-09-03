package com.mantum.cmms.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.mantum.R;
import com.mantum.cmms.adapter.ContenedorAdapter;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Contenedor;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.service.InspeccionService;
import com.mantum.component.Mantum;
import com.mantum.component.OnSelected;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;

public class BusquedaInspeccionManagerActivity extends Mantum.Activity {
    private Database database;
    private InspeccionService inspeccionService;
    private ContenedorAdapter<Contenedor> contenedorAdapter;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busqueda_inspeccion_manager);

        includeBackButtonAndTitle(R.string.busqueda_inspeccion);

        database = new Database(this);
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta != null) {
            inspeccionService = new InspeccionService(this, cuenta);
        }

        TextInputEditText codigoView = findViewById(R.id.codigo);
        TextInputEditText ubicacionView = findViewById(R.id.ubicacion);

        SharedPreferences sharedPreferences
                = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        String codigo = sharedPreferences.getString(getString(R.string.mantum_code), "");
        codigoView.setText(codigo);

        String ubicacion = sharedPreferences.getString(getString(R.string.mantum_ubicacion), "");
        ubicacionView.setText(ubicacion);

        search();

        compositeDisposable.add(inspeccionService.count()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Long count) -> {
                    TextView informationView = BusquedaInspeccionManagerActivity.this.findViewById(R.id.information);
                    informationView.setText(String.format("%s Inspecciones programadas", count));
                }, this::onError, Functions.EMPTY_ACTION));

        findViewById(R.id.buscar).setOnClickListener(v -> search());

        contenedorAdapter = new ContenedorAdapter<>(this);
        contenedorAdapter.setOnAction(new OnSelected<Contenedor>() {
            @Override
            public void onClick(Contenedor value, int position) {
                if (value.getPti()) {
                    Intent intent = new Intent(
                            BusquedaInspeccionManagerActivity.this, InspeccionRegistroPTIActivity.class);
                    intent.putExtra(Mantum.KEY_UUID, value.getKey());
                    intent.putExtra(Mantum.KEY_ID, value.getId());

                    intent.putExtra(InspeccionRegistroPTIActivity.CODE, value.getCodigo());
                    intent.putExtra(InspeccionRegistroPTIActivity.ESTADO, value.getEstado());
                    intent.putExtra(InspeccionRegistroPTIActivity.FECHA, value.getFechaultimaejecucionpti());
                    intent.putExtra(InspeccionRegistroPTIActivity.TECNICO, value.getPersonalultimaejecucionpti());
                    intent.putExtra(InspeccionRegistroPTIActivity.LINEA_NAVIERA, value.getLineanaviera());
                    intent.putExtra(InspeccionRegistroPTIActivity.LOCATION, value.getUbicacion());
                    intent.putExtra(InspeccionRegistroPTIActivity.MODE_EDIT, value.getDetalleinspeccion());
                    intent.putExtra(InspeccionRegistroPTIActivity.SOFTWARE, value.getSoftware());
                    intent.putExtra(InspeccionRegistroPTIActivity.SERIAL, value.getSerial());
                    intent.putExtra(InspeccionRegistroPTIActivity.FECHA_FABRICACION, value.getFechafabricacion());
                    intent.putExtra(InspeccionRegistroPTIActivity.ID_MARCA, value.getIdmarca());
                    intent.putExtra(InspeccionRegistroPTIActivity.MARCA, value.getMarca());
                    intent.putExtra(InspeccionRegistroPTIActivity.ID_MODELO, value.getIdmodelo());
                    intent.putExtra(InspeccionRegistroPTIActivity.MODELO, value.getModelo());
                    intent.putExtra(InspeccionRegistroPTIActivity.EQUIPMENT_GRADE, value.getEquipmentgrade());
                    intent.putExtra(InspeccionRegistroPTIActivity.ID_CLASIFICACION, value.getIdclasificacion());
                    startActivity(intent);
                    return;
                }

                Intent intent = new Intent(
                        BusquedaInspeccionManagerActivity.this, InspeccionRegistroEIRActivity.class);
                intent.putExtra(Mantum.KEY_UUID, value.getKey());
                intent.putExtra(Mantum.KEY_ID, value.getId());

                intent.putExtra(InspeccionRegistroEIRActivity.CODE, value.getCodigo());
                intent.putExtra(InspeccionRegistroEIRActivity.ESTADO, value.getEstado());
                intent.putExtra(InspeccionRegistroEIRActivity.FECHA, value.getFechaultimaejecucioneir());
                intent.putExtra(InspeccionRegistroEIRActivity.TECNICO, value.getPersonalultimaejecucioneir());
                intent.putExtra(InspeccionRegistroEIRActivity.LINEA_NAVIERA, value.getLineanaviera());
                intent.putExtra(InspeccionRegistroEIRActivity.TIPO, value.getTipo());
                intent.putExtra(InspeccionRegistroEIRActivity.LOCATION, value.getUbicacion());
                intent.putExtra(InspeccionRegistroEIRActivity.MODE_EDIT, value.getDetalleinspeccion());
                startActivity(intent);
            }

            @Override
            public boolean onLongClick(Contenedor value, int position) {
                return false;
            }
        });

        RecyclerView recyclerView = findViewById(R.id.contenedores);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(contenedorAdapter);
    }

    private void search() {
        TextInputEditText codigoView = findViewById(R.id.codigo);
        Editable codigoText = codigoView.getText();

        TextInputEditText ubicacionView = findViewById(R.id.ubicacion);
        Editable ubicacionText = ubicacionView.getText();

        if (codigoText == null || ubicacionText == null) {
            return;
        }

        if (codigoText.length() == 0 && ubicacionText.length() == 0) {
            return;
        }

        compositeDisposable.add(
                inspeccionService.search(codigoText.toString(), ubicacionText.toString())
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::onNext, this::onError, Functions.EMPTY_ACTION)
        );
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            backActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void backActivity() {
        TextInputEditText codigoView = findViewById(R.id.codigo);
        TextInputEditText ubicacionView = findViewById(R.id.ubicacion);

        if (codigoView.getText() != null && ubicacionView.getText() != null) {
            SharedPreferences.Editor editor
                    = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit();
            editor.putString(getString(R.string.mantum_code), codigoView.getText().toString());
            editor.putString(getString(R.string.mantum_ubicacion), ubicacionView.getText().toString());
            editor.apply();
        }

        super.backActivity();
    }

    private void onNext(List<Contenedor> contenedores) {
        if (contenedorAdapter != null) {
            contenedorAdapter.clear();
            contenedorAdapter.addAll(contenedores, true);
        }
    }

    private void onError(Throwable throwable) {
        if (contenedorAdapter != null) {
            contenedorAdapter.clear();
            contenedorAdapter.notifyDataSetChanged();
        }

        Snackbar.make(getView(), R.string.contenedores_encontrados, Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();

        if (database != null) {
            database.close();
        }

        if (inspeccionService != null) {
            inspeccionService.close();
        }

        if (contenedorAdapter != null) {
            contenedorAdapter.clear();
        }
    }
}