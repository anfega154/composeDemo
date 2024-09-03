package com.mantum.cmms.activity;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Personal;
import com.mantum.cmms.service.PersonalServices;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.AlphabetAdapter;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class PersonalAutocompleteActivity extends Mantum.Activity {

    public static final int REQUEST_ACTION = 1220;

    public static final String LAST_SEARCH = "Last Search";

    private static final String TAG = PersonalAutocompleteActivity.class.getSimpleName();

    private SearchView searchView;

    private ProgressBar progressBar;

    private PersonalServices personalService;

    private AlphabetAdapter<Personal> alphabetAdapter;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            includeBackButtonAndTitle(R.string.filtro_personal_autocomplete);
            setContentView(R.layout.autocomplete_personal);

            Database database = new Database(this);
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            progressBar = findViewById(R.id.progressBar);
            alphabetAdapter = new AlphabetAdapter<>(this);
            personalService = new PersonalServices(this, cuenta);

            isEmptyList();

            alphabetAdapter.setOnAction(new OnSelected<Personal>() {

                @Override
                public void onClick(final Personal value, int position) {
                    Intent intent = new Intent();
                    intent.putExtra("Id", value.getId());
                    intent.putExtra("Nombre", value.getNombre());
                    intent.putExtra("Cedula", value.getCedula());
                    intent.putExtra("Grupo", value.getGrupo());

                    if (searchView != null && searchView.getQuery() != null) {
                        intent.putExtra("Busqueda", searchView.getQuery().toString());
                    }

                    setResult(RESULT_OK, intent);
                    finish();
                }

                @Override
                public boolean onLongClick(Personal value, int position) {
                    return true;
                }
            });

            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            RecyclerView recyclerView = findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemViewCacheSize(20);
            recyclerView.setDrawingCacheEnabled(true);
            recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(alphabetAdapter);

        } catch (Exception e) {
            Log.e(TAG, "onCreate: ", e);
            backActivity(getString(R.string.error_app));
        }
    }

    @Nullable
    private String getLastSearch() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            return null;
        }
        return bundle.getString(LAST_SEARCH);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        alphabetAdapter.clear();
        compositeDisposable.clear();
        personalService.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_autocomplete, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.expandActionView();

        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView = (androidx.appcompat.widget.SearchView) menu.findItem(R.id.action_search)
                .getActionView();

        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setSubmitButtonEnabled(true);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    request(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });

            String lastSearch = getLastSearch();
            if (lastSearch != null && !lastSearch.isEmpty()) {
                searchView.setQuery(lastSearch, true);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void onComplete() {
        progressBar.setVisibility(View.GONE);
        alphabetAdapter.refresh();
        isEmptyList();
    }

    private void isEmptyList() {
        RelativeLayout container = findViewById(R.id.empty);
        container.setVisibility(alphabetAdapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void onError(@NonNull Throwable throwable) {
        progressBar.setVisibility(View.GONE);
        if (throwable.getMessage() != null) {
            Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void onNext(@NonNull Response response) {
        Version.save(getApplicationContext(), response.getVersion());
        List<Personal> results = response.getBody(Personal.Request.class)
                .getPersonal();

        if (results.isEmpty()) {
            Snackbar.make(getView(), R.string.personal_autocomplete_empty, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        for (Personal personal : results) {
            if (personal.getCedula() != null) {
                alphabetAdapter.add(personal);
            }
        }
    }

    private void request(String query) {
        progressBar.setVisibility(View.VISIBLE);
        compositeDisposable.add(personalService.get(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNext, this::onError, this::onComplete));
    }
}
