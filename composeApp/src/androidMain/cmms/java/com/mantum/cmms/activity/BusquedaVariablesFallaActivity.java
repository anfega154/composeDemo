package com.mantum.cmms.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.SearchView;

import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.mantum.demo.R;
import com.mantum.cmms.adapter.CustomAlphabetAdapter;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.GamaMantenimiento;
import com.mantum.component.Mantum;
import com.mantum.component.OnSelected;

import java.util.List;

import io.realm.Case;
import io.realm.RealmQuery;

public class BusquedaVariablesFallaActivity extends Mantum.Activity implements SearchView.OnQueryTextListener {

    public static final String TIPO_GAMA = "tipo_gama";

    private Cuenta cuenta;
    private Database database;

    private CustomAlphabetAdapter<GamaMantenimiento> gamaAlphabetAdapter;

    private String idReclasificacionGama;
    private String idTipoReparacionGama;
    private String idSubtipoReparacionGama;
    private String tipoGama;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.alphabet_layout_view);
            includeBackButtonAndTitle(R.string.accion_buscar_entidad);

            database = new Database(this);

            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                tipoGama = bundle.getString(TIPO_GAMA, null);
                idReclasificacionGama = bundle.getString("idReclasificacionGama");
                idTipoReparacionGama = bundle.getString("idTipoReparacionGama");
                idSubtipoReparacionGama = bundle.getString("idSubtipoReparacionGama");
            }

            gamaAlphabetAdapter = new CustomAlphabetAdapter<>(this);
            gamaAlphabetAdapter.setOnAction(new OnSelected<GamaMantenimiento>() {
                @Override
                public void onClick(GamaMantenimiento value, int position) {
                    Intent intent = new Intent();
                    intent.putExtra("idVariable", value.getId());
                    intent.putExtra("codigoVariable", value.getCodigo());
                    intent.putExtra("descripcionVariable", value.getDescripcion());
                    intent.putExtra("requierefotos", value.getRequierefotos());
                    intent.putExtra("requirerepuestos", value.getRequiererepuestos());
                    intent.putExtra("tipoVariable", "Gama");
                    setResult(1, intent);
                    finish();
                }

                @Override
                public boolean onLongClick(GamaMantenimiento value, int position) {
                    return false;
                }
            });
            gamaAlphabetAdapter.showMessageEmpty(getView());

            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            gamaAlphabetAdapter.startAdapter(getView(), layoutManager);

            search("");
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            backActivity(getString(R.string.error_app));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }

        if (gamaAlphabetAdapter != null) {
            gamaAlphabetAdapter.clear();
        }
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            backActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String criterio) {
        return search(criterio);
    }

    @Override
    public boolean onQueryTextChange(String criterio) {
        return search(criterio);
    }

    private boolean search(String criterio) {
        gamaAlphabetAdapter.clear();

        List<GamaMantenimiento> gamasMantenimiento;
        if (idSubtipoReparacionGama != null) {
            RealmQuery<GamaMantenimiento> query = database.where(GamaMantenimiento.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .equalTo("idsubtiporeparacion", idSubtipoReparacionGama)
                    .beginGroup()
                    .contains("codigo", criterio, Case.INSENSITIVE).or()
                    .contains("actividad", criterio, Case.INSENSITIVE)
                    .endGroup();

            if (tipoGama != null) {
                query = query.equalTo(tipoGama.equals("PTI") ? "pti" : "eir", true);
            }

            gamasMantenimiento = query.findAll();
        } else if (idTipoReparacionGama != null) {
            RealmQuery<GamaMantenimiento> query = database.where(GamaMantenimiento.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .equalTo("idtiporeparacion", idTipoReparacionGama)
                    .beginGroup()
                    .contains("codigo", criterio, Case.INSENSITIVE).or()
                    .contains("actividad", criterio, Case.INSENSITIVE)
                    .endGroup();

            if (tipoGama != null) {
                query = query.equalTo(tipoGama.equals("PTI") ? "pti" : "eir", true);
            }

            gamasMantenimiento = query.findAll();
        } else if (idReclasificacionGama != null) {
            RealmQuery<GamaMantenimiento> query = database.where(GamaMantenimiento.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .equalTo("idreclasificacion", idReclasificacionGama)
                    .beginGroup()
                    .contains("codigo", criterio, Case.INSENSITIVE).or()
                    .contains("actividad", criterio, Case.INSENSITIVE)
                    .endGroup();

            if (tipoGama != null) {
                query = query.equalTo(tipoGama.equals("PTI") ? "pti" : "eir", true);
            }

            gamasMantenimiento = query.findAll();
        } else {
            RealmQuery<GamaMantenimiento> query = database.where(GamaMantenimiento.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .beginGroup()
                    .contains("codigo", criterio, Case.INSENSITIVE).or()
                    .contains("actividad", criterio, Case.INSENSITIVE)
                    .endGroup();

            if (tipoGama != null) {
                query = query.equalTo(tipoGama.equals("PTI") ? "pti" : "eir", true);
            }

            gamasMantenimiento = query.findAll();
        }

        gamasMantenimiento = database.copyFromRealm(gamasMantenimiento);
        gamaAlphabetAdapter.addAll(gamasMantenimiento);

        gamaAlphabetAdapter.sort();
        gamaAlphabetAdapter.refresh();
        gamaAlphabetAdapter.showMessageEmpty(getView(), R.string.message_search_empty, R.drawable.buscar_entidad);
        return true;
    }
}