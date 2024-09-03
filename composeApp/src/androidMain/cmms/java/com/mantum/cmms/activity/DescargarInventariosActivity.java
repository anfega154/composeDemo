package com.mantum.cmms.activity;

import static com.mantum.component.Mantum.isConnectedOrConnecting;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import com.google.gson.Gson;
import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.ResponseInventarioActivos;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.service.InventarioActivosService;
import com.mantum.component.Mantum;
import com.mantum.cmms.adapter.ValidationAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;
import io.realm.RealmResults;
import com.mantum.cmms.entity.Validation;
import android.widget.TextView;



public class DescargarInventariosActivity extends  Mantum.Activity implements ValidationAdapter.ValidationAdapterListener{

    private static final String TAG = DescargarInventariosActivity.class.getSimpleName();

    private Database database;

    private ProgressBar progressBar;

    private ValidationAdapter validationAdapter;

    private RecyclerView.Adapter adapter;

    private InventarioActivosService inventarioActivosService;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Cuenta cuenta;

    private Long idCuenta;

    private DescargarInventariosActivity.BusquedasSingleton busquedasSingleton;

    private final static int PAGINATE = 100;
    private RecyclerView validationRecycler;
    private SwipeRefreshLayout swipeRefreshLayout;

    private TextView message;

    private RecyclerView.LayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            database = new Database(DescargarInventariosActivity.this);
            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();
            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }
            inventarioActivosService = new InventarioActivosService(this, cuenta);
            idCuenta = (long) cuenta.getId();
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_descargar_inventarios);

            includeBackButtonAndTitle(R.string.descargar_inventarios);
            swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
            validationRecycler = findViewById(R.id.validationRecycler);
            message = findViewById(R.id.message);
            progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
            onRequest(0);

            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    onRequest(0);
                    swipeRefreshLayout.setRefreshing(false);
                }
            });

        }catch (Exception e){

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_refresh:
                if (!isConnectedOrConnecting(this)) {
                    Snackbar.make(getView(), R.string.offline, Snackbar.LENGTH_LONG)
                            .show();
                    return true;
                }
                progressBar.setVisibility(View.VISIBLE);
                request();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void onRequest(int page) {
        if (!Mantum.isConnectedOrConnecting(this)) {
            busquedasSingleton = DescargarInventariosActivity.BusquedasSingleton.getInstance();
            List<Validation> busquedasAdd = busquedasSingleton.getBusquedas(page);

            setListInventarios(busquedasAdd);
            return;
        }

        request();
    }

    private void request(){
        compositeDisposable.add(inventarioActivosService.getValidations(idCuenta)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNext, this::onError, () -> {
                    message.setVisibility(View.GONE);
                }));
    }

    @Override
    public void OnItemClicked(int id, String code, String name) {

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (database != null) {
            database.close();
        }
        if (inventarioActivosService != null) {
            inventarioActivosService.close();
        }
        compositeDisposable.clear();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actualizar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void onNext(@NonNull ResponseInventarioActivos responseInventario) {
        List<Validation> validationsFinds = new ArrayList<>();

        List<Validation> validations = responseInventario.getValidations();

        if (validations == null || validations.isEmpty()) {
            return;
        }

        database.executeTransaction(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                return;
            }

            validationsFinds.addAll(validations);
        });

        new saveDatabase().execute(validationsFinds);
        setListInventarios(validations);
    }


    private void setListInventarios(List<Validation> validations){
        adapter = new ValidationAdapter(validations, DescargarInventariosActivity.this);
        validationRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        validationRecycler.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(this,1);
        validationRecycler.setLayoutManager(layoutManager);
        progressBar.setVisibility(View.GONE);
    }

    private void onError(@NonNull Throwable throwable) {
        if (throwable.getMessage() != null) {
            Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                    .show();
        }
        progressBar.setVisibility(View.GONE);
        message.setText(R.string.empty_inventory);
    }

    private static class BusquedasSingleton {

        private static DescargarInventariosActivity.BusquedasSingleton instance;

        private List<Validation> rows = new ArrayList<>();

        private boolean loaded = false;

        public static DescargarInventariosActivity.BusquedasSingleton getInstance() {
            if (instance == null) {
                instance = new DescargarInventariosActivity.BusquedasSingleton();
            }

            return instance;
        }

        public List<Validation> getBusquedas(int page) {

            if (loaded) {
                loadBusquedas();
                loaded = true;
            }

            if (rows.isEmpty()) {
                return rows;
            }

            int start = page  * PAGINATE;
            int end   = start + PAGINATE;

            if ((end-1) > rows.size() ) {
                end = rows.size();
            }

            if (start >= end) {
                return rows;
            }

            return rows.subList(start, end);
        }


        public void loadBusquedas() {
            Realm realm = Realm.getDefaultInstance();

            try {
                Cuenta cuenta = realm.where(Cuenta.class)
                        .equalTo("active", true)
                        .findFirst();
                RealmResults<Validation> results = realm.where(Validation.class)
                        .findAll();

                rows = realm.copyFromRealm(results);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                realm.close();
            }
        }
    }
    private class saveDatabase extends AsyncTask<List<Validation>, Void, Void> {
        @Override
        protected Void doInBackground(List<Validation>... lists) {
            List<Validation> validations = lists[0];
            if (validations.isEmpty()) {
                return null;
            }

            Realm realm = Realm.getDefaultInstance();

            try {
                realm.executeTransaction(self -> {
                    self.insertOrUpdate(validations);
                });

                busquedasSingleton.loadBusquedas();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                realm.close();
            }

            return null;
        }
    }
}