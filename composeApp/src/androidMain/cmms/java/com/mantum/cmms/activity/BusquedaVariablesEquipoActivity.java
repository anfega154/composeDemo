package com.mantum.cmms.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.SearchView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mantum.demo.R;
import com.mantum.cmms.convert.BusquedaConvert;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Busqueda;
import com.mantum.cmms.entity.CentroCostoEquipo;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Familia;
import com.mantum.cmms.entity.InstalacionLocativa;
import com.mantum.cmms.entity.InstalacionProceso;
import com.mantum.cmms.entity.MarcaEquipo;
import com.mantum.cmms.entity.Responsable;
import com.mantum.cmms.service.ActivoService;
import com.mantum.component.Mantum;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.AlphabetAdapter;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Case;

public class BusquedaVariablesEquipoActivity extends Mantum.Activity implements SearchView.OnQueryTextListener {

    private static final String TAG = BusquedaVariablesEquipoActivity.class.getSimpleName();

    private Database database;

    private ProgressBar progressBar;

    private ActivoService activoService;

    private AlphabetAdapter<Busqueda> alphabetAdapter;

    private AlphabetAdapter<CentroCostoEquipo> centroCostoAlphabetAdapter;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private String tipoEntidad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_busqueda_variables_equipo);

            progressBar = findViewById(R.id.progressBar);
            includeBackButtonAndTitle(R.string.accion_buscar_entidad);

            database = new Database(this);

            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                tipoEntidad = bundle.getString("tipoEntidad");
            }

            activoService = new ActivoService(this, cuenta);

            alphabetAdapter = new AlphabetAdapter<>(this);
            alphabetAdapter.setOnAction(new OnSelected<Busqueda>() {

                @Override
                public void onClick(Busqueda value, int position) {

                    database.executeTransaction(realm -> {
                        switch (tipoEntidad) {
                            case "Familia":
                                Familia familia = new Familia();
                                familia.setId(value.getId());
                                familia.setNombre(value.getName());
                                realm.insertOrUpdate(familia);
                                break;

                            case "InstalacionProceso":
                                InstalacionProceso instalacionProceso = new InstalacionProceso();
                                instalacionProceso.setId(value.getId());
                                instalacionProceso.setNombre(value.getName());
                                realm.insertOrUpdate(instalacionProceso);
                                break;

                            case "InstalacionLocativa":
                                InstalacionLocativa instalacionLocativa = new InstalacionLocativa();
                                instalacionLocativa.setId(value.getId());
                                instalacionLocativa.setNombre(value.getName());
                                realm.insertOrUpdate(instalacionLocativa);
                                break;

                            case "Fabricante":
                                MarcaEquipo marcaEquipo = new MarcaEquipo();
                                marcaEquipo.setId(value.getId());
                                marcaEquipo.setNombre(value.getName());
                                realm.insertOrUpdate(marcaEquipo);

                            case "Personal":
                                Responsable responsable = new Responsable();
                                responsable.setId(value.getId());
                                responsable.setNombre(value.getName());
                                realm.insertOrUpdate(responsable);
                                break;
                        }
                    });

                    Intent intent = new Intent();
                    intent.putExtra("idEntidad", value.getId());
                    intent.putExtra("tipoEntidad", value.getType());
                    setResult(1, intent);
                    finish();
                }

                @Override
                public boolean onLongClick(Busqueda value, int position) {
                    return false;
                }

            });
            alphabetAdapter.showMessageEmpty(getView());

            centroCostoAlphabetAdapter = new AlphabetAdapter<>(this);
            centroCostoAlphabetAdapter.setOnAction(new OnSelected<CentroCostoEquipo>() {
                @Override
                public void onClick(CentroCostoEquipo value, int position) {
                    Intent intent = new Intent();
                    intent.putExtra("idEntidad", value.getId());
                    intent.putExtra("tipoEntidad", "CentroCosto");
                    setResult(1, intent);
                    finish();
                }

                @Override
                public boolean onLongClick(CentroCostoEquipo value, int position) {
                    return false;
                }
            });
            centroCostoAlphabetAdapter.showMessageEmpty(getView());

            LinearLayoutManager layoutManager = new LinearLayoutManager(this);

            if (tipoEntidad.equals("CentroCosto")) {
                centroCostoAlphabetAdapter.startAdapter(getView(), layoutManager);
            } else {
                alphabetAdapter.startAdapter(getView(), layoutManager);
            }
        } catch (Exception e) {
            backActivity(getString(R.string.error_app));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }

        if (alphabetAdapter != null) {
            alphabetAdapter.clear();
        }

        if (centroCostoAlphabetAdapter != null) {
            centroCostoAlphabetAdapter.clear();
        }

        if (activoService != null) {
            activoService.onDestroy();
            activoService.cancel();
        }

        compositeDisposable.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_busqueda, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String criterio) {
        return search(criterio, tipoEntidad);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            backActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean search(@NonNull String criterio, @NonNull String tipoEntidad) {
//        TODO: revisar para modo offline
//        RealmQuery<Busqueda> query = database.where(Busqueda.class)
//                .equalTo("cuenta.UUID", cuenta.getUUID())
//                .beginGroup()
//                .contains("code", criterio, Case.INSENSITIVE).or()
//                .contains("name", criterio, Case.INSENSITIVE).or()
//                .contains("type", criterio, Case.INSENSITIVE).endGroup();
//
//        RealmResults<Busqueda> values = query.findAll();
//        List<Busqueda> busquedas = values.isManaged()
//                ? database.copyFromRealm(values)
//                : values;

        alphabetAdapter.clear();
        centroCostoAlphabetAdapter.clear();
        progressBar.setVisibility(View.VISIBLE);

        if (tipoEntidad.equals("CentroCosto")) {
            localSearch(criterio);
            return true;
        }

        compositeDisposable.add(activoService.searchByEntity(criterio, tipoEntidad)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNext, this::onError, this::onComplete));

        return true;
    }

    private void localSearch(String criterio) {
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta != null) {
            List<CentroCostoEquipo> centroCostoEquipos = database.where(CentroCostoEquipo.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .contains("codigo", criterio, Case.INSENSITIVE).or()
                    .contains("nombre", criterio, Case.INSENSITIVE)
                    .findAll();

            if (centroCostoEquipos != null) {
                centroCostoEquipos = database.copyFromRealm(centroCostoEquipos);
                centroCostoAlphabetAdapter.addAll(centroCostoEquipos);
            }

            progressBar.setVisibility(View.GONE);
            centroCostoAlphabetAdapter.sort();
            centroCostoAlphabetAdapter.refresh();
            centroCostoAlphabetAdapter.showMessageEmpty(getView(), R.string.message_search_empty, R.drawable.buscar_entidad);
        }
    }

    private void onNext(@NonNull Response response) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(new TypeToken<Busqueda.Request>() {
                }.getType(), new BusquedaConvert())
                .create();

        Busqueda.Request content = gson.fromJson(response.getBody(), Busqueda.Request.class);

        for (Busqueda busqueda : content.getEntities()) {
            if (busqueda.getType().equals(tipoEntidad)) {
                alphabetAdapter.add(busqueda);
            }
        }
    }

    private void onComplete() {
        progressBar.setVisibility(View.GONE);
        alphabetAdapter.sort();
        alphabetAdapter.refresh();
        alphabetAdapter.showMessageEmpty(getView(), R.string.message_search_empty, R.drawable.buscar_entidad);
    }

    private void onError(@NonNull Throwable throwable) {
        progressBar.setVisibility(View.GONE);
        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG).show();
        Log.e(TAG, "onError: ", throwable);
    }
}