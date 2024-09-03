package com.mantum.cmms.activity;

import static com.mantum.cmms.entity.Transaccion.MODULO_ESTADO_USUARIO;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.fragment.SincronizarErrorFragment;
import com.mantum.cmms.fragment.SincronizarExitoFragment;
import com.mantum.cmms.fragment.SincronizarGeolocalizacionFragment;
import com.mantum.cmms.fragment.SincronizarPendienteFragment;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.task.TransaccionTask;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.adapter.TabAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.RealmResults;
import io.realm.Sort;

public class TransaccionActivity extends Mantum.Activity implements OnCompleteListener {

    private Database database;

    private Cuenta cuenta;

    private ViewPager viewPager;

    private TabAdapter tabAdapter;

    private List<Transaccion> transaccions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_transaccion);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            includeBackButtonAndTitle(getString(R.string.transacciones));

            database = new Database(this);
            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            transaccions = database.where(Transaccion.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .equalTo("show", true)
                    .sort("creation", Sort.DESCENDING)
                    .findAll();

            tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                    Arrays.asList(
                            new SincronizarPendienteFragment(),
                            new SincronizarErrorFragment(),
                            new SincronizarExitoFragment(),
                            new SincronizarGeolocalizacionFragment()
                    )
            );

            viewPager = findViewById(R.id.viewPager);
            viewPager.setAdapter(tabAdapter);
            viewPager.setOffscreenPageLimit(tabAdapter.getCount() - 1);

            TabLayout tabLayout = findViewById(R.id.tabs);
            tabLayout.setTabMode(TabLayout.MODE_AUTO);
            tabLayout.setupWithViewPager(viewPager);
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    onRefresh(tabAdapter.getFragment(tab.getPosition()));
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }

            });

            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        } catch (Exception e) {
            backActivity(e.getMessage());
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
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_transacciones, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;

            case R.id.refresh:
                if (database.isClosed()) {
                    break;
                }

                transaccions = database.where(Transaccion.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .equalTo("show", true)
                        .sort("creation", Sort.DESCENDING)
                        .findAll();

                onRefresh(tabAdapter.getFragment(viewPager.getCurrentItem()));
                break;

            case R.id.retry: {
                if (database.isClosed()) {
                    break;
                }

                database.executeTransactionAsync(self -> {
                    Cuenta cuenta = self.where(Cuenta.class)
                            .equalTo("active", true)
                            .findFirst();

                    if (cuenta == null) {
                        return;
                    }

                    RealmResults<Transaccion> transacciones = self.where(Transaccion.class)
                            .equalTo("cuenta.UUID", cuenta.getUUID())
                            .equalTo("estado", Transaccion.ESTADO_ERROR)
                            .findAll();

                    for (Transaccion transaccion : transacciones) {
                        transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);
                        transaccion.setMessage(" ");
                    }
                }, () -> onRefresh(tabAdapter.getFragment(viewPager.getCurrentItem())));

                break;
            }

            case R.id.send:
                if (!Mantum.isConnectedOrConnecting(this)) {
                    Snackbar.make(getView(), R.string.offline, Snackbar.LENGTH_LONG)
                            .show();
                    break;
                }

                List<Transaccion> sincronizandoEstados = database.where(Transaccion.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .equalTo("estado", Transaccion.ESTADO_SINCRONIZANDO)
                        .equalTo("modulo", MODULO_ESTADO_USUARIO)
                        .findAll();

                if (sincronizandoEstados.isEmpty()) {
                    Toast.makeText(this, R.string.sincronizando, Toast.LENGTH_SHORT)
                            .show();

                    TransaccionTask.Task task = new TransaccionTask.Task(this);
                    task.process(new Database(this));
                } else {
                    Toast.makeText(this, "Se están sincronizando estados de personal", Toast.LENGTH_SHORT)
                            .show();
                }

                onRefresh(tabAdapter.getFragment(viewPager.getCurrentItem()));
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onComplete(@NonNull String name) {
        onRefresh(tabAdapter.getFragment(name));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        onRefresh(tabAdapter.getFragment(viewPager.getCurrentItem()));
    }

    private List<Transaccion> filter(@NonNull String state) {
        try {
            if (transaccions == null) {
                return new ArrayList<>();
            }

            List<Transaccion> results = new ArrayList<>();
            for (Transaccion transaccion : transaccions) {
                if (state.equals(transaccion.getEstado())) {
                    results.add(transaccion);
                }
            }

            return database.copyFromRealm(results);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<Transaccion> filterByPending() {
        try {
            if (transaccions == null) {
                return new ArrayList<>();
            }

            List<Transaccion> results = new ArrayList<>();
            for (Transaccion transaccion : transaccions) {
                if (Transaccion.ESTADO_PENDIENTE.equals(transaccion.getEstado()) && !Transaccion.MODULO_GEOLOCALIZACION.equals(transaccion.getModulo())) {
                    results.add(transaccion);
                }
            }

            return database.copyFromRealm(results);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<Transaccion> filterBygGeolocation() {
        try {
            if (transaccions == null) {
                return new ArrayList<>();
            }

            List<Transaccion> results = new ArrayList<>();
            for (Transaccion transaccion : transaccions) {
                if (Transaccion.ESTADO_PENDIENTE.equals(transaccion.getEstado()) && Transaccion.MODULO_GEOLOCALIZACION.equals(transaccion.getModulo())) {
                    results.add(transaccion);
                }
            }

            return database.copyFromRealm(results);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void onRefresh(@Nullable Mantum.Fragment fragment) {
        if (fragment == null) {
            return;
        }

        switch (fragment.getKey()) {
            case SincronizarPendienteFragment.KEY_TAB:
                SincronizarPendienteFragment sincronizarPendienteFragment
                        = (SincronizarPendienteFragment) fragment;

                sincronizarPendienteFragment.clear();
                sincronizarPendienteFragment.onRefresh(filterByPending());
                break;

            case SincronizarGeolocalizacionFragment.KEY_TAB:
                SincronizarGeolocalizacionFragment sincronizarGeolocalizacionFragment
                        = (SincronizarGeolocalizacionFragment) fragment;

                sincronizarGeolocalizacionFragment.clear();
                sincronizarGeolocalizacionFragment.onRefresh(filterBygGeolocation());
                break;

            case SincronizarExitoFragment.KEY_TAB:
                SincronizarExitoFragment sincronizarExitoFragment
                        = (SincronizarExitoFragment) fragment;

                sincronizarExitoFragment.clear();
                sincronizarExitoFragment.onRefresh(filter(Transaccion.ESTADO_SINCRONIZADO));
                break;

            case SincronizarErrorFragment.KEY_TAB:
                SincronizarErrorFragment sincronizarErrorFragment
                        = (SincronizarErrorFragment) fragment;

                sincronizarErrorFragment.clear();
                sincronizarErrorFragment.onRefresh(filter(Transaccion.ESTADO_ERROR));
                break;
        }
    }
}