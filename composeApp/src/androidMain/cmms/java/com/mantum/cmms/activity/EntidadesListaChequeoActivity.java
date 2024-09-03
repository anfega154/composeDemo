package com.mantum.cmms.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.mantum.demo.R;
import com.mantum.cmms.adapter.SelectorEntidadListaChequeoAdapter;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.EntidadesClienteListaChequeo;
import com.mantum.cmms.entity.ListaChequeo;
import com.mantum.cmms.service.ListaChequeoService;
import com.mantum.component.Mantum;

import java.util.ArrayList;
import java.util.List;

public class EntidadesListaChequeoActivity extends Mantum.Activity implements SearchView.OnQueryTextListener {

    public static String KEY_CLIENT = "client";
    public static String KEY_ENTITY = "entity";

    private Bundle bundle;
    private ProgressBar progressBar;
    private ListaChequeo listaChequeo;
    private ListaChequeoService listaChequeoService;
    private SelectorEntidadListaChequeoAdapter alphabetAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.entidades_lista_chequeo);

            includeBackButtonAndTitle(R.string.buscar_entidad);

            Database database = new Database(this);
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            bundle = getIntent().getExtras();
            if (bundle == null) {
                throw new Exception(getString(R.string.detail_error_ruta));
            }

            listaChequeoService = new ListaChequeoService(this, cuenta);
            listaChequeo = listaChequeoService.getById(bundle.getLong(Mantum.KEY_ID));
            if (listaChequeo == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            progressBar = findViewById(R.id.progressBar);

            ArrayList<EntidadesClienteListaChequeo> entities = new ArrayList<>();
            SparseArray<Parcelable> selected = bundle.getSparseParcelableArray(EntidadesListaChequeoActivity.KEY_ENTITY);
            if (selected != null) {
                for (int i = 0; i < selected.size(); i++) {
                    EntidadesClienteListaChequeo value = (EntidadesClienteListaChequeo) selected.get(i);
                    entities.add(value);
                }
            }

            alphabetAdapter = new SelectorEntidadListaChequeoAdapter(this);
            alphabetAdapter.addAll(!entities.isEmpty()
                    ? entities
                    : getEntityByClient(bundle.getLong(KEY_CLIENT, -1))
            );

            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            RecyclerView recyclerView = findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemViewCacheSize(20);
            recyclerView.setDrawingCacheEnabled(true);
            recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(alphabetAdapter);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            backActivity(getString(R.string.error_app));
        }
    }

    @NonNull
    private List<EntidadesClienteListaChequeo> getEntityByClient(Long client) {
        if (listaChequeo == null) {
            return new ArrayList<>();
        }

        if (client == null || client <= 0) {
            return new ArrayList<>(listaChequeo.getEntidadesCliente());
        }

        List<EntidadesClienteListaChequeo> results = new ArrayList<>();
        for (EntidadesClienteListaChequeo entidadesClienteListaChequeo : listaChequeo.getEntidadesCliente()) {
            if (client.equals(entidadesClienteListaChequeo.getIdcliente())) {
                results.add(entidadesClienteListaChequeo);
            }
        }
        return results;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alphabetAdapter != null) {
            alphabetAdapter.clear();
        }

        if (listaChequeoService != null) {
            listaChequeoService.close();
        }
    }

    @Override
    public void onBackPressed() {
        SparseArray<EntidadesClienteListaChequeo> sparseArray = new SparseArray<>();
        for (int i = 0; i < alphabetAdapter.getOriginal().size(); i++) {
            sparseArray.append(i, alphabetAdapter.getOriginal().get(i));
        }

        Bundle bundle = new Bundle();
        bundle.putSparseParcelableArray(KEY_ENTITY, sparseArray);

        Intent intent = new Intent();
        intent.putExtras(bundle);
        backActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_busqueda_black, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(false);
        return super.onCreateOptionsMenu(menu);
    }

    private void request(String query) {
        progressBar.setVisibility(View.VISIBLE);
        if (listaChequeoService == null || bundle == null) {
            return;
        }

        if (query.isEmpty()) {
            alphabetAdapter.clear();
            alphabetAdapter.addAll(getEntityByClient(bundle.getLong(KEY_CLIENT, -1)));

            progressBar.setVisibility(View.GONE);
            return;
        }

        ArrayList<EntidadesClienteListaChequeo> resultados = new ArrayList<>();
        for (EntidadesClienteListaChequeo entidadesClienteListaChequeo : getEntityByClient(bundle.getLong(KEY_CLIENT, -1))) {
            if (entidadesClienteListaChequeo.getNombre() != null) {
                if (entidadesClienteListaChequeo.getNombre().toLowerCase().contains(query.toLowerCase())) {
                    resultados.add(entidadesClienteListaChequeo);
                }
            }
        }

        alphabetAdapter.clear();
        alphabetAdapter.addAll(resultados);

        progressBar.setVisibility(View.GONE);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        request(newText);
        return true;
    }
}
