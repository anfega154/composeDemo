package com.mantum.cmms.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Autorizaciones;
import com.mantum.cmms.entity.Personal;
import com.mantum.cmms.fragment.AutorizacionFragment;
import com.mantum.cmms.fragment.MarcasFragment;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.service.AutorizacionAccesoService;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.adapter.TabAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.RealmResults;

import static com.mantum.component.Mantum.*;
import static com.mantum.component.Mantum.isConnectedOrConnecting;

public class MiAutorizacionAccesoActivity extends Mantum.Activity
        implements OnCompleteListener {

    private static final String TAG
            = MiAutorizacionAccesoActivity.class.getSimpleName();

    private Database database;

    private ViewPager viewPager;

    private TabAdapter tabAdapter;

    private ProgressBar progressBar;

    private AutorizacionAccesoService autorizacionAccesoService;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_detalle_transaccion);

            database = new Database(this);
            autorizacionAccesoService = new AutorizacionAccesoService(this);

            progressBar = findViewById(R.id.progressBar);
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            includeBackButtonAndTitle(R.string.titulo_mis_autorizaciones);
            tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                    Arrays.asList(new AutorizacionFragment(), new MarcasFragment()));

            viewPager = findViewById(R.id.viewPager);
            viewPager.setAdapter(tabAdapter);
            viewPager.setOffscreenPageLimit(tabAdapter.getCount() - 1);

            TabLayout tabLayout = findViewById(R.id.tabs);
            tabLayout.setTabMode(TabLayout.MODE_FIXED);
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
            if (isConnectedOrConnecting(this)) {
                request();
            }

        } catch (Exception e) {
            backActivity(getString(R.string.error_app));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actualizar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();

        if (database != null) {
            database.close();
        }

        if (autorizacionAccesoService != null) {
            autorizacionAccesoService.cancel();
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
                request();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onComplete(@NonNull String name) {
        onRefresh(tabAdapter.getFragment(name));
    }

    private void onRefresh(@Nullable Fragment fragment) {
        if (fragment == null) {
            return;
        }

        switch (fragment.getKey()) {
            case AutorizacionFragment.KEY_TAB:
                AutorizacionFragment autorizacionFragment
                        = AutorizacionFragment.class.cast(fragment);

                if (autorizacionFragment != null) {
                    autorizacionFragment.onRefresh(getAutorizaciones(Autorizaciones.MODULO_AUTORIZACION));
                }

                break;

            case MarcasFragment.KEY_TAB:
                MarcasFragment marcasFragment
                        = MarcasFragment.class.cast(fragment);

                if (marcasFragment != null) {
                    marcasFragment.onRefresh(getAutorizaciones(Autorizaciones.MODULO_MARCAS));
                }

                break;
        }
    }

    private void onNext(Response response) {
        try {
            Version.save(this, response.getVersion());
            final MiAutorizacionAccesoActivity.Request request
                    = response.getBody(MiAutorizacionAccesoActivity.Request.class, getGson());

            database.executeTransaction(self -> {
                Cuenta cuenta = self.where(Cuenta.class)
                        .equalTo("active", true)
                        .findFirst();

                if (cuenta == null) {
                    return;
                }

                self.where(Personal.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findAll().deleteAllFromRealm();

                self.where(Autorizaciones.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findAll().deleteAllFromRealm();

                for (Autorizaciones autorizaciones : request.getAutorizaciones()) {
                    for (Personal personal : autorizaciones.getPersonal()) {
                        personal.setUuid(UUID.randomUUID().toString());
                        personal.setCuenta(cuenta);
                    }

                    autorizaciones.setUuid(UUID.randomUUID().toString());
                    autorizaciones.setCuenta(cuenta);
                    self.insert(autorizaciones);
                }

                for (Autorizaciones autorizaciones : request.getMarcas()) {
                    for (Personal personal : autorizaciones.getPersonal()) {
                        personal.setUuid(UUID.randomUUID().toString());
                        personal.setCuenta(cuenta);
                    }

                    autorizaciones.setUuid(UUID.randomUUID().toString());
                    autorizaciones.setCuenta(cuenta);
                    self.insert(autorizaciones);
                }
            });
            database.close();
        } catch (Exception e) {
            Log.e(TAG, "onNext: ", e);
        }
    }

    private void onError(Throwable throwable) {
        Log.e(TAG, "onError: ", throwable);
        progressBar.setVisibility(View.GONE);
        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                .show();
    }

    private void onComplete() {
        progressBar.setVisibility(View.GONE);
        onRefresh(tabAdapter.getFragment(viewPager.getCurrentItem()));
    }

    private void request() {
        progressBar.setVisibility(View.VISIBLE);
        compositeDisposable.add(autorizacionAccesoService.get()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNext, this::onError, this::onComplete));
    }

    private List<Autorizaciones> getAutorizaciones(String modulo) {
        if (database == null || database.isClosed()) {
            database = new Database(this);
        }

        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return new ArrayList<>();
        }

        RealmResults<Autorizaciones> results = database.where(Autorizaciones.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("modulo", modulo)
                .findAll();

        return results.isManaged()
                ? database.copyFromRealm(results)
                : results;
    }

    public static class Request {

        private List<Autorizaciones> autorizaciones;

        private List<Autorizaciones> marcas;

        public Request() {
            autorizaciones = new ArrayList<>();
            marcas = new ArrayList<>();
        }

        public List<Autorizaciones> getAutorizaciones() {
            return autorizaciones;
        }

        public void setAutorizaciones(List<Autorizaciones> autorizaciones) {
            this.autorizaciones = autorizaciones;
        }

        public List<Autorizaciones> getMarcas() {
            return marcas;
        }

        public void setMarcas(List<Autorizaciones> marcas) {
            this.marcas = marcas;
        }
    }
}