package com.mantum.cmms.activity;

import android.content.Intent;
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
import com.mantum.cmms.fragment.ImagenesFragment;
import com.mantum.cmms.fragment.PendienteDetalleFragment;
import com.mantum.cmms.entity.Adjuntos;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Pendiente;
import com.mantum.cmms.service.PendienteService;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.adapter.TabAdapter;

import java.util.Arrays;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class DetallePendienteActivity extends Mantum.Activity
        implements OnCompleteListener {

    private static final String TAG
            = DetallePendienteActivity.class.getSimpleName();

    private Cuenta cuenta;

    private Database database;

    private ViewPager viewPager;

    private TabAdapter tabAdapter;

    private Pendiente detalle;

    private ProgressBar progressBar;

    private PendienteService pendienteService;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_detalle);

            progressBar = findViewById(R.id.progressBar);
            database = new Database(this);

            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                throw new Exception(getString(R.string.detail_error_OT));
            }

            pendienteService = new PendienteService(this, cuenta);
            detalle = database.where(Pendiente.class)
                    .equalTo("id", bundle.getLong(Mantum.KEY_ID))
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findFirst();

            if (detalle == null) {
                throw new Exception(getString(R.string.error_detail_ot));
            }

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            includeBackButtonAndTitle(detalle.getCodigo());

            tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                    Arrays.asList(new PendienteDetalleFragment(), new ImagenesFragment()));

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
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) {}

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

        if (pendienteService != null) {
            pendienteService.close();
        }

        compositeDisposable.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pendiente, menu);
        return true;
    }

    @Override
    public void onComplete(@NonNull String name) {
        onRefresh(tabAdapter.getFragment(name));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home :
                super.onBackPressed();
                break;

            case R.id.refresh:
                final Long id = detalle.getId();
                progressBar.setVisibility(View.VISIBLE);
                compositeDisposable.add(pendienteService.fetchById(detalle.getId())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap(ordenTrabajos -> ordenTrabajos.isEmpty()
                                ? pendienteService.remove(id)
                                : pendienteService.save(ordenTrabajos))
                        .subscribe(this::onNext, this::onError, () -> {
                            if (detalle == null) {
                                Intent newIntent = new Intent();
                                newIntent.putExtra(Mantum.KEY_ID, id);
                                newIntent.putExtra(Mantum.KEY_REFRESH, true);
                                newIntent.putExtra(Mantum.KEY_MESSAGE, getString(R.string.error_detail_pendiente));
                                backActivity(newIntent);
                                return;
                            }

                            onRefresh(tabAdapter.getFragment(viewPager.getCurrentItem()));
                            progressBar.setVisibility(View.GONE);
                        }));
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void onNext(List<Pendiente> pendientes) {
        if (pendientes.isEmpty()) {
            detalle = null;
            return;
        }
        detalle = pendientes.get(0);
    }

    private void onError(Throwable throwable) {
        progressBar.setVisibility(View.GONE);
        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                .show();
    }

    private void onRefresh(@Nullable Mantum.Fragment tabFragment) {
        if (tabFragment != null) {
            if (detalle != null && !detalle.isManaged()) {
                detalle = database.where(Pendiente.class)
                        .equalTo("id", detalle.getId())
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();
            }

            switch (tabFragment.getKey()) {
                case PendienteDetalleFragment.KEY_TAB:
                    Pendiente pendiente = detalle.isManaged()
                            ? database.copyFromRealm(detalle)
                            : detalle;

                    PendienteDetalleFragment.class.cast(tabFragment)
                            .onRefresh(pendiente);
                    break;

                case ImagenesFragment.KEY_TAB:
                    List<Adjuntos> adjuntos = detalle.getAdjuntos().isManaged()
                            ? database.copyFromRealm(detalle.getAdjuntos())
                            : detalle.getAdjuntos();

                    ImagenesFragment.class.cast(tabFragment)
                            .inactivarModoEditar()
                            .onLoad(adjuntos);
                    break;
            }
        }
    }
}