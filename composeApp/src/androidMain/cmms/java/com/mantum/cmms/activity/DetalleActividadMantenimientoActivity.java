package com.mantum.cmms.activity;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Actividad;
import com.mantum.cmms.entity.Adjuntos;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Tarea;
import com.mantum.cmms.entity.Variable;
import com.mantum.cmms.fragment.ActividadMantenimientoFragment;
import com.mantum.cmms.fragment.AdjuntoFragment;
import com.mantum.cmms.fragment.ImagenesFragment;
import com.mantum.cmms.fragment.TareaListaFragment;
import com.mantum.cmms.fragment.VariableFragment;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.adapter.TabAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

public class DetalleActividadMantenimientoActivity extends Mantum.Activity implements OnCompleteListener {

    private Database database;

    private Actividad detalle;

    private TabAdapter tabAdapter;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private boolean tareaEntidadOt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_detalle_actividad_mantenimiento);

            database = new Database(this);
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                throw new Exception(getString(R.string.detail_error_OT));
            }

            detalle = database.where(Actividad.class)
                    .equalTo("uuid", bundle.getString(Mantum.KEY_ID))
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findFirst();

            if (detalle == null) {
                throw new Exception(getString(R.string.error_detalle_actividad_mantenimiento));
            }

            tareaEntidadOt = bundle.getBoolean(DetalleTareaActivity.TAREA_ENTIDAD_OT, false);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            includeBackButtonAndTitle(
                    !detalle.getCodigo().isEmpty() ? detalle.getCodigo() : detalle.getNombre());

            List<Mantum.Fragment> tabs = new ArrayList<>();
            tabs.add(new ActividadMantenimientoFragment());
            tabs.add(new VariableFragment());

            if (Version.check(this, 19)) {
                tabs.add(new TareaListaFragment());
            }

            tabs.add(new ImagenesFragment());
            tabs.add(new AdjuntoFragment());

            tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(), tabs);

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
            backActivity(e.getMessage());
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
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
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
            case ActividadMantenimientoFragment.KEY_TAB:
                Actividad actividad = detalle.isManaged()
                        ? database.copyFromRealm(detalle)
                        : detalle;

                ActividadMantenimientoFragment actividadMantenimientoFragment
                        = ActividadMantenimientoFragment.class.cast(tabFragment);

                if (actividadMantenimientoFragment != null) {
                    actividadMantenimientoFragment.onLoad(actividad);
                }

                break;

            case VariableFragment.KEY_TAB:
                List<Variable> variables = detalle.getVariables().isManaged()
                        ? database.copyFromRealm(detalle.getVariables())
                        : detalle.getVariables();
                VariableFragment.class.cast(tabFragment).onLoad(variables);
                break;

            case TareaListaFragment.KEY_TAB:
                List<Tarea> tareas = detalle.getTareas().isManaged()
                        ? database.copyFromRealm(detalle.getTareas())
                        : detalle.getTareas();

                ((TareaListaFragment) tabFragment).onRefresh(tareas, tareaEntidadOt);
                break;

            case ImagenesFragment.KEY_TAB:
                List<Adjuntos> imagenes = detalle.getImagenes().isManaged()
                        ? database.copyFromRealm(detalle.getImagenes())
                        : detalle.getImagenes();

                ImagenesFragment.class.cast(tabFragment)
                        .inactivarModoEditar()
                        .onLoad(imagenes);
                break;

            case AdjuntoFragment.KEY_TAB:
                List<Adjuntos> adjuntos = detalle.getAdjuntos().isManaged()
                        ? database.copyFromRealm(detalle.getAdjuntos())
                        : detalle.getAdjuntos();

                AdjuntoFragment.class.cast(tabFragment)
                        .onLoad(adjuntos);
                break;
        }
    }
}
