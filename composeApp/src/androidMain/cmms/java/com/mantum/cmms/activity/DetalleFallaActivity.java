package com.mantum.cmms.activity;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Adjuntos;
import com.mantum.cmms.entity.Consumible;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.ElementoFalla;
import com.mantum.cmms.entity.Falla;
import com.mantum.cmms.entity.RepuestoManual;
import com.mantum.cmms.fragment.AdjuntoFragment;
import com.mantum.cmms.fragment.ConsumiblesAsociadosListaFragment;
import com.mantum.cmms.fragment.DetalleFallaFragment;
import com.mantum.cmms.fragment.ElementosFallaListaFragment;
import com.mantum.cmms.fragment.ImagenesFragment;
import com.mantum.cmms.fragment.RepuestosAsociadosListaFragment;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.adapter.TabAdapter;

import java.util.Arrays;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

public class DetalleFallaActivity extends Mantum.Activity implements OnCompleteListener {

    private Database database;

    private Falla detalle;

    private TabAdapter tabAdapter;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_detalle);

            database = new Database(this);
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                throw new Exception(getString(R.string.detail_error_falla));
            }

            detalle = database.where(Falla.class)
                    .equalTo("UUID", bundle.getString(Mantum.KEY_UUID))
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findFirst();

            if (detalle == null) {
                throw new Exception(getString(R.string.error_detalle_falla));
            }

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            includeBackButtonAndTitle(detalle.getResumen());

            tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                    Arrays.asList(new DetalleFallaFragment(), new ElementosFallaListaFragment(), new RepuestosAsociadosListaFragment(), new ConsumiblesAsociadosListaFragment(),
                            new ImagenesFragment(), new AdjuntoFragment()));

            ViewPager viewPager = findViewById(R.id.viewPager);
            viewPager.setAdapter(tabAdapter);
            viewPager.setOffscreenPageLimit(tabAdapter.getCount() - 1);

            TabLayout tabLayout = findViewById(R.id.tabs);
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
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
            Log.e("TAG", "onCreate: ", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
        compositeDisposable.clear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onComplete(@NonNull String name) {
        onRefresh(tabAdapter.getFragment(name));
    }

    private void onRefresh(@Nullable Mantum.Fragment tabFragment) {
        if (tabFragment == null) {
            return;
        }

        switch (tabFragment.getKey()) {
            case DetalleFallaFragment.KEY_TAB:
                Falla falla = detalle.isLoaded()
                        ? database.copyFromRealm(detalle)
                        : detalle;

                DetalleFallaFragment detalleFallaFragment = (DetalleFallaFragment) tabFragment;
                detalleFallaFragment.onLoad(falla);

                break;

            case ElementosFallaListaFragment.KEY_TAB:
                List<ElementoFalla> elementoFallas = detalle.getElementos().isManaged()
                        ? database.copyFromRealm(detalle.getElementos())
                        : detalle.getElementos();

                ElementosFallaListaFragment elementosFallaListaFragment = (ElementosFallaListaFragment) tabFragment;
                elementosFallaListaFragment.onLoad(elementoFallas);

                break;

            case RepuestosAsociadosListaFragment.KEY_TAB:
                List<RepuestoManual> repuestos = detalle.getRepuestos().isManaged()
                        ? database.copyFromRealm(detalle.getRepuestos())
                        : detalle.getRepuestos();

                RepuestosAsociadosListaFragment repuestosManualesListaFragment = (RepuestosAsociadosListaFragment) tabFragment;
                repuestosManualesListaFragment.onLoad(repuestos);

                break;

            case ConsumiblesAsociadosListaFragment.KEY_TAB:
                List<Consumible> consumibles = detalle.getConsumibles().isManaged()
                        ? database.copyFromRealm(detalle.getConsumibles())
                        : detalle.getConsumibles();

                ConsumiblesAsociadosListaFragment consumiblesListaFragment = (ConsumiblesAsociadosListaFragment) tabFragment;
                consumiblesListaFragment.onLoad(consumibles);

                break;

            case ImagenesFragment.KEY_TAB:
                List<Adjuntos> imagenes = detalle.getImagenes().isManaged()
                        ? database.copyFromRealm(detalle.getImagenes())
                        : detalle.getImagenes();

                ImagenesFragment imagenesFragment = (ImagenesFragment) tabFragment;
                imagenesFragment.onLoad(imagenes);

                break;

            case AdjuntoFragment.KEY_TAB:
                List<Adjuntos> adjuntos = detalle.getAdjuntos().isManaged()
                        ? database.copyFromRealm(detalle.getAdjuntos())
                        : detalle.getAdjuntos();

                AdjuntoFragment adjuntoFragment = (AdjuntoFragment) tabFragment;
                adjuntoFragment.onLoad(adjuntos);

                break;
        }
    }
}
