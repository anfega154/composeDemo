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
import com.mantum.cmms.adapter.SelectorPersonalListaChequeoAdapter;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.ListaChequeo;
import com.mantum.cmms.entity.PersonalListaChequeo;
import com.mantum.cmms.service.ListaChequeoService;
import com.mantum.component.Mantum;

import java.util.ArrayList;

public class PersonalListaChequeoActivity extends Mantum.Activity implements SearchView.OnQueryTextListener {

    public static String KEY_PERSONAL = "personal";

    private Bundle bundle;
    private ProgressBar progressBar;
    private ListaChequeo listaChequeo;
    private ListaChequeoService listaChequeoService;
    private SelectorPersonalListaChequeoAdapter alphabetAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.entidades_lista_chequeo);

            includeBackButtonAndTitle(R.string.buscar_personal);

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

            ArrayList<PersonalListaChequeo> personales = new ArrayList<>();
            SparseArray<Parcelable> selected = bundle.getSparseParcelableArray(KEY_PERSONAL);
            if (selected != null) {
                for (int i = 0; i < selected.size(); i++) {
                    PersonalListaChequeo value = (PersonalListaChequeo) selected.get(i);
                    personales.add(value);
                }
            }

            alphabetAdapter = new SelectorPersonalListaChequeoAdapter(this, false);
            alphabetAdapter.addAll(!personales.isEmpty()
                    ? personales : new ArrayList<>(listaChequeo.getPersonal()));

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
        SparseArray<PersonalListaChequeo> sparseArray = new SparseArray<>();
        for (int i = 0; i < alphabetAdapter.getOriginal().size(); i++) {
            sparseArray.append(i, alphabetAdapter.getOriginal().get(i));
        }

        Bundle bundle = new Bundle();
        bundle.putSparseParcelableArray(KEY_PERSONAL, sparseArray);

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

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        request(newText);
        return true;
    }

    private void request(String query) {
        progressBar.setVisibility(View.VISIBLE);
        if (listaChequeoService == null || bundle == null) {
            return;
        }

        if (query.isEmpty()) {
            alphabetAdapter.clear();
            alphabetAdapter.addAll(listaChequeo.getPersonal());

            progressBar.setVisibility(View.GONE);
            return;
        }

        ArrayList<PersonalListaChequeo> resultados = new ArrayList<>();
        for (PersonalListaChequeo value : listaChequeo.getPersonal()) {
            if (value.getNombre() != null) {
                if (value.getNombre().toLowerCase().contains(query.toLowerCase())) {
                    resultados.add(value);
                    continue;
                }
            }

            if (value.getApellido() != null) {
                if (value.getApellido().toLowerCase().contains(query.toLowerCase())) {
                    resultados.add(value);
                    continue;
                }
            }

            if (value.getCodigo() != null) {
                if (value.getCodigo().toLowerCase().contains(query.toLowerCase())) {
                    resultados.add(value);
                }
            }
        }

        alphabetAdapter.clear();
        alphabetAdapter.addAll(resultados);

        progressBar.setVisibility(View.GONE);
    }
}
