package com.mantum.cmms.activity;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.RecursoAdicional;
import com.mantum.cmms.fragment.RecursoAdicionalFragment;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.service.RecursoAdicionalService;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

import java.util.List;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;

public class RecursosAdicionalesBuscarActivity extends Mantum.Activity
        implements OnCompleteListener, SearchView.OnQueryTextListener {

    private static final String TAG = RecursosAdicionalesBuscarActivity.class.getSimpleName();

    public static final int REQUEST_ACTION = 1296;

    private Realm realm;

    private Cuenta cuenta;

    private RealmResults<RecursoAdicional> recursos;

    private RecursoAdicionalService recursoAdicionalService;

    private RecursoAdicionalFragment recursoAdicionalFragment;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            realm = new Database(this).instance();
            includeBackButtonAndTitle(R.string.buscar);

            recursoAdicionalService = new RecursoAdicionalService(this);
            cuenta = realm.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                recursos = realm.where(RecursoAdicional.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findAll();
            }

            recursoAdicionalFragment = new RecursoAdicionalFragment();
            recursoAdicionalFragment.setAction(false);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, recursoAdicionalFragment)
                    .commit();

        } catch (Exception e) {
            backActivity(getString(R.string.error_app));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
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
        if (cuenta == null) {
            return false;
        }

        if (recursoAdicionalService != null) {
            compositeDisposable.add(recursoAdicionalService.search(newText)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onNext, this::onError));
        }

        List<RecursoAdicional> values = realm.where(RecursoAdicional.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .beginGroup()
                    .contains("nombre", newText, Case.INSENSITIVE).or()
                    .contains("referencia", newText, Case.INSENSITIVE)
                .endGroup()
                .findAll();

        recursoAdicionalFragment.addResources(realm.copyFromRealm(values));
        return true;
    }

    private void onNext(Response response) {
        Version.save(getApplicationContext(), response.getVersion());

        if (cuenta.getUUID() == null) {
            return;
        }

        realm.executeTransactionAsync(self -> {
            RecursoAdicional.Request recursos
                    = response.getBody(RecursoAdicional.Request.class);

            for (RecursoAdicional recurso : recursos.getRecursos()) {
                RecursoAdicional element = self.where(RecursoAdicional.class)
                        .equalTo("id", recurso.getId())
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();

                if (element == null) {
                    recurso.setUuid(UUID.randomUUID().toString());
                    recurso.setCuenta(cuenta);
                    self.insert(recurso);
                } else {
                    element.setUtilizado(recurso.isUtilizado());
                    element.setUnidad(recurso.getUnidad());
                    element.setReferencia(recurso.getReferencia());
                    element.setCantidad(recurso.getCantidad());
                    element.setNombre(recurso.getNombre());
                }

                recursoAdicionalFragment.addResources(recurso);
            }
        });
    }

    private void onError(Throwable throwable) {
        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void onComplete(@NonNull String name) {
        if (recursoAdicionalFragment != null && recursos != null) {
            List<RecursoAdicional> value = recursos.isManaged()
                    ? realm.copyFromRealm(recursos)
                    : recursos;
            recursoAdicionalFragment.addResources(value);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }

        if (recursoAdicionalService != null) {
            recursoAdicionalService.cancel();
        }

        compositeDisposable.clear();
    }
}