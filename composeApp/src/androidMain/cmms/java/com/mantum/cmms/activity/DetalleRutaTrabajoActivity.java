package com.mantum.cmms.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.mantum.R;
import com.mantum.cmms.entity.Adjuntos;
import com.mantum.cmms.fragment.AdjuntoFragment;
import com.mantum.cmms.fragment.EntidadesListaFragment;
import com.mantum.cmms.fragment.ImagenesFragment;
import com.mantum.cmms.fragment.RutaTrabajoDetalleFragment;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Entidad;
import com.mantum.cmms.entity.RutaTrabajo;
import com.mantum.cmms.service.RutaTrabajoService;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.cmms.database.Database;
import com.mantum.component.adapter.TabAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class DetalleRutaTrabajoActivity extends Mantum.Activity implements OnCompleteListener {

    public static final String ID_EJECUCION = "id_ejecucion";

    public static final String ID_EXTRA = "id_extra";

    private Cuenta cuenta;

    private Database database;

    private ViewPager viewPager;

    private TabAdapter tabAdapter;

    private RutaTrabajo detalle;

    private ProgressBar progressBar;

    private RutaTrabajoService rutaTrabajoService;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Long idRutaTrabajo;

    private Long idEjecucion;

    private Long idOt;

    private String uuid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
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

            rutaTrabajoService = new RutaTrabajoService(this, cuenta);
            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                throw new Exception(getString(R.string.detail_error_ruta));
            }

            detalle = database.where(RutaTrabajo.class)
                    .equalTo("UUID", bundle.getString(Mantum.KEY_UUID))
                    .equalTo("id", bundle.getLong(Mantum.KEY_ID))
                    .equalTo("idejecucion", bundle.getLong(ID_EJECUCION) != 0 ? bundle.getLong(ID_EJECUCION) : null)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findFirst();

            if (detalle == null) {
                throw new Exception(getString(R.string.error_detail_ruta));
            }

            uuid = detalle.getUUID();
            idRutaTrabajo = detalle.getId();
            idEjecucion = detalle.getIdejecucion();
            idOt = bundle.getLong(ID_EXTRA, -1);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            includeBackButtonAndTitle(detalle.getCodigo());

            tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                    Arrays.asList(new RutaTrabajoDetalleFragment(), new EntidadesListaFragment(),
                            new ImagenesFragment(), new AdjuntoFragment()));

            viewPager = findViewById(R.id.viewPager);
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
        if (database != null) {
            database.close();
        }

        if (rutaTrabajoService != null) {
            rutaTrabajoService.close();
        }

        compositeDisposable.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ruta_trabajo, menu);
        menu.findItem(R.id.refresh).setVisible(true);
        menu.findItem(R.id.remove).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        Bundle bundle;
        Intent intent;

        switch (menuItem.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;

            case R.id.remove:
                AlertDialog.Builder builder
                        = new AlertDialog.Builder(this);

                builder.setTitle(R.string.titulo_eliminar_ruta_trabajo);
                builder.setMessage(R.string.mensaje_eliminar_ruta_trabajo);

                builder.setNegativeButton(R.string.aceptar, (dialog, which) -> compositeDisposable.add(rutaTrabajoService.remove(idRutaTrabajo, idEjecucion)
                        .subscribe(this::onNext, this::onError, () -> {
                            if (detalle == null) {
                                Intent newIntent = new Intent();
                                newIntent.putExtra(Mantum.KEY_ID, idRutaTrabajo);
                                newIntent.putExtra(ID_EJECUCION, idEjecucion);
                                newIntent.putExtra(Mantum.KEY_REFRESH, true);
                                newIntent.putExtra(Mantum.KEY_MESSAGE, getString(R.string.mensaje_ruta_trabajo_eliminada));
                                backActivity(newIntent);
                            }
                        })));
                builder.setPositiveButton(R.string.cancelar, (dialog, which) -> dialog.dismiss());

                builder.setCancelable(true);
                builder.create();
                builder.show();

                break;

            case R.id.refresh:
                progressBar.setVisibility(View.VISIBLE);
                compositeDisposable.add(rutaTrabajoService.fetchById(idRutaTrabajo, idEjecucion)
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap(rutaTrabajos -> rutaTrabajos.isEmpty()
                                ? rutaTrabajoService.remove(idRutaTrabajo, idEjecucion)
                                : rutaTrabajoService.save(rutaTrabajos))
                        .subscribe(this::onNext, this::onError, () -> {
                            if (detalle == null) {
                                Intent newIntent = new Intent();
                                newIntent.putExtra(Mantum.KEY_ID, idRutaTrabajo);
                                newIntent.putExtra(ID_EJECUCION, idEjecucion);
                                newIntent.putExtra(Mantum.KEY_REFRESH, true);
                                newIntent.putExtra(Mantum.KEY_MESSAGE, getString(R.string.error_detail_ruta_trabajo));
                                backActivity(newIntent);
                                return;
                            }

                            onRefresh(tabAdapter.getFragment(viewPager.getCurrentItem()));
                            progressBar.setVisibility(View.GONE);
                        }));

                break;

            case R.id.diligenciar:
                bundle = new Bundle();
                bundle.putLong(Mantum.KEY_ID, idRutaTrabajo);
                bundle.putString(Mantum.KEY_UUID, uuid);
                bundle.putString(DiligenciarRutaTrabajoActivity.TIPO, detalle.getTipogrupo());
                bundle.putLong(DiligenciarRutaTrabajoActivity.ID_EXTRA, idOt);

                if (detalle.getIdejecucion() != null) {
                    bundle.putLong(DiligenciarRutaTrabajoActivity.ID_EJECUCION, idEjecucion);
                }

                intent = new Intent(this, DiligenciarRutaTrabajoActivity.class);
                intent.putExtras(bundle);

                startActivityForResult(intent, DiligenciarRutaTrabajoActivity.REQUEST_ACTION);
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void onRefresh(@Nullable Mantum.Fragment tabFragment) {
        if (tabFragment != null) {
            if (detalle != null && !detalle.isManaged()) {
                detalle = database.where(RutaTrabajo.class)
                        .equalTo("id", detalle.getId())
                        .equalTo("idejecucion", detalle.getIdejecucion())
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();
            }

            switch (tabFragment.getKey()) {
                case RutaTrabajoDetalleFragment.KEY_TAB:
                    RutaTrabajo rutaTrabajo = detalle.isManaged()
                            ? database.copyFromRealm(detalle)
                            : detalle;
                    RutaTrabajoDetalleFragment.class.cast(tabFragment).onRefresh(rutaTrabajo);
                    break;

                case EntidadesListaFragment.KEY_TAB:
                    List<Entidad> entidades = detalle.getEntidades().isManaged()
                            ? database.copyFromRealm(detalle.getEntidades())
                            : detalle.getEntidades();

                    EntidadesListaFragment entidadesListaFragment
                            = EntidadesListaFragment.class.cast(tabFragment);

                    if (entidadesListaFragment != null) {
                        entidadesListaFragment.onLoad(entidades);
                    }

                    break;

                case ImagenesFragment.KEY_TAB:
                    List<Adjuntos> imagenes = new ArrayList<>();
                    if (detalle.getImagenes() != null) {
                        imagenes = detalle.getImagenes().isManaged()
                                ? database.copyFromRealm(detalle.getImagenes())
                                : detalle.getImagenes();
                    }

                    ImagenesFragment imagenesFragment = (ImagenesFragment) tabFragment;
                    imagenesFragment.inactivarModoEditar()
                            .onLoad(imagenes);
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

    @Override
    public void onComplete(@NonNull String name) {
        onRefresh(tabAdapter.getFragment(name));
    }

    private void onNext(List<RutaTrabajo> rutaTrabajos) {
        if (rutaTrabajos.isEmpty()) {
            detalle = null;
            return;
        }
        detalle = rutaTrabajos.get(0);
    }

    private void onError(Throwable throwable) {
        progressBar.setVisibility(View.GONE);
        if (throwable != null) {
            Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                    .show();
        }
    }
}