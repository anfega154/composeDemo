package com.mantum.cmms.activity;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
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

import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Cliente;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.service.ClienteServices;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.AlphabetAdapter;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class ClienteAutocompleteActivity extends Mantum.Activity {

    private ProgressBar progressBar;

    public static final int REQUEST_ACTION = 1222;

    private static final String TAG = ClienteAutocompleteActivity.class.getSimpleName();

    private ClienteServices clienteService;

    private AlphabetAdapter<Cliente> alphabetAdapter;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            includeBackButtonAndTitle(R.string.filtro_cliente_autocomplete);
            setContentView(R.layout.autocomplete_cliente);

            Database database = new Database(this);
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            progressBar = findViewById(R.id.progressBar);
            clienteService =  new ClienteServices(this, cuenta);
            alphabetAdapter = new AlphabetAdapter<>(this);

            isEmptyList();
            alphabetAdapter.setOnAction(new OnSelected<Cliente>() {

                @Override
                public void onClick(final Cliente value, int position) {
                    Intent intent = new Intent();
                    intent.putExtra("Nombre", value.getNombre());
                    intent.putExtra("Id", value.getId());
                    setResult(RESULT_OK, intent);
                    finish();
                }
                @Override
                public boolean onLongClick(Cliente value, int position) {
                    return true;
                }
            });

            alphabetAdapter.hiddenSummary();

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        alphabetAdapter.clear();
        compositeDisposable.clear();
        clienteService.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_autocomplete, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.expandActionView();

        final SearchView searchView= (androidx.appcompat.widget.SearchView) menu.findItem(R.id.action_search).getActionView();
        SearchManager searchManager =  (SearchManager) getSystemService(SEARCH_SERVICE);
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

    private void onError(Throwable throwable) {
        progressBar.setVisibility(View.GONE);
        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }


    private void onNext(Response response) {

        Version.save(getApplicationContext(), response.getVersion());
        List<Cliente> listaCliente = response.getBody(Cliente.Request.class)
                .getCliente();

        if (listaCliente.isEmpty()){
            Snackbar.make(getView(), R.string.cliente_autocomplete_empty, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        for (Cliente cliente : listaCliente){
            if (cliente.getCedula() != null) alphabetAdapter.add(cliente);
        }

    }

    private void request(String query) {
        progressBar.setVisibility(View.VISIBLE);
        compositeDisposable.add(clienteService.get(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNext, this::onError, this::onComplete));
    }

}
