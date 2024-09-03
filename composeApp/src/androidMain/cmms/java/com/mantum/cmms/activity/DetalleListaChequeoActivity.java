package com.mantum.cmms.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Entidad;
import com.mantum.cmms.entity.ListaChequeo;
import com.mantum.cmms.fragment.EntidadesListaFragment;
import com.mantum.cmms.fragment.RutaTrabajoDetalleFragment;
import com.mantum.cmms.service.ListaChequeoService;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.adapter.TabAdapter;

import java.util.Arrays;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class DetalleListaChequeoActivity extends Mantum.Activity implements OnCompleteListener {

    public static final String KEY_CODE = "code";

    private Database database;
    private ViewPager viewPager;
    private ListaChequeo detalle;
    private TabAdapter tabAdapter;
    private ProgressBar progressBar;
    private ListaChequeoService listaChequeoService;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_detalle);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                throw new Exception(getString(R.string.detail_error_ruta));
            }

            String code = bundle.getString(KEY_CODE, "");
            includeBackButtonAndTitle(code);

            database = new Database(this);
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            listaChequeoService = new ListaChequeoService(this, cuenta);

            tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                    Arrays.asList(new RutaTrabajoDetalleFragment(), new EntidadesListaFragment()));

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

            progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);

            compositeDisposable.add(
                    listaChequeoService.fetchById(bundle.getLong(Mantum.KEY_ID))
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .flatMap(listaChequeoService::save)
                            .subscribe(this::onNext, this::onError, this::onComplete)
            );
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            backActivity();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detalle_lista_chequeo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;

            case R.id.refresh:
                if (!Mantum.isConnectedOrConnecting(this)) {
                    Toast.makeText(this, getString(R.string.offline), Toast.LENGTH_LONG)
                            .show();
                    break;
                }

                if (detalle != null) {
                    progressBar.setVisibility(View.VISIBLE);
                    compositeDisposable.add(
                            listaChequeoService.fetchById(detalle.getId())
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .flatMap(listaChequeoService::save)
                                    .subscribe(this::onNext, this::onError, this::onComplete)
                    );
                }
                break;

            case R.id.register:
                if (detalle != null) {
                    Bundle bundle = new Bundle();
                    bundle.putLong(Mantum.KEY_ID, detalle.getId());
                    bundle.putBoolean(DiligenciarRutaTrabajoActivity.ES_MODO_NEW_LISTA_CHEQUEO, true);

                    Intent intent = new Intent(this, DiligenciarRutaTrabajoActivity.class);
                    intent.putExtras(bundle);

                    startActivityForResult(intent, DiligenciarRutaTrabajoActivity.REQUEST_ACTION);
                }
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }

        if (listaChequeoService != null) {
            listaChequeoService.close();
        }

        compositeDisposable.clear();
    }

    private void onNext(@NonNull List<ListaChequeo> values) {
        this.detalle = values.get(0);
    }

    private void onError(Throwable throwable) {
        Log.e("TAG", "onError: ", throwable);
        progressBar.setVisibility(View.GONE);
        if (throwable != null && throwable.getMessage() != null) {
            Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private void onComplete() {
        progressBar.setVisibility(View.GONE);
        onRefresh(tabAdapter.getFragment(viewPager.getCurrentItem()));
    }

    @Override
    public void onComplete(@NonNull String name) {
        onRefresh(tabAdapter.getFragment(name));
    }

    private void onRefresh(@Nullable Mantum.Fragment tabFragment) {
        if (detalle == null) {
            return;
        }

        if (tabFragment != null) {
            switch (tabFragment.getKey()) {
                case RutaTrabajoDetalleFragment.KEY_TAB:
                    ListaChequeo value = detalle.isManaged()
                            ? database.copyFromRealm(detalle)
                            : detalle;

                    ((RutaTrabajoDetalleFragment) tabFragment).onRefresh(value);
                    break;

                case EntidadesListaFragment.KEY_TAB:
                    List<Entidad> entidades = detalle.getEntidades().isManaged()
                            ? database.copyFromRealm(detalle.getEntidades())
                            : detalle.getEntidades();

                    EntidadesListaFragment entidadesListaFragment
                            = (EntidadesListaFragment) tabFragment;

                    entidadesListaFragment.onLoad(entidades);
                    break;
            }
        }
    }
}