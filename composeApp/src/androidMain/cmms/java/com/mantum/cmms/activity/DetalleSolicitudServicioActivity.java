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
import com.mantum.cmms.entity.EstadoInicial;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.cmms.entity.InformeTecnico;
import com.mantum.cmms.entity.RecursoAdicional;
import com.mantum.cmms.fragment.AdjuntoFragment;
import com.mantum.cmms.fragment.EstadoInicialDetalleFragment;
import com.mantum.cmms.fragment.ImagenesFragment;
import com.mantum.cmms.fragment.EntidadesListaFragment;
import com.mantum.cmms.fragment.InformeTecnicoDetalleFragment;
import com.mantum.cmms.fragment.ProcesoFragment;
import com.mantum.cmms.fragment.RecursoAdicionalFragment;
import com.mantum.cmms.fragment.SolicitudServicioDetalleFragment;
import com.mantum.cmms.entity.Adjuntos;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Entidad;
import com.mantum.cmms.entity.Proceso;
import com.mantum.cmms.entity.SolicitudServicio;
import com.mantum.cmms.service.SolicitudServicioService;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.adapter.TabAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class DetalleSolicitudServicioActivity extends Mantum.Activity
        implements OnCompleteListener {

    private static final String TAG = DetalleSolicitudServicioActivity.class.getSimpleName();

    private Cuenta cuenta;

    private Database database;

    private ViewPager viewPager;

    private TabAdapter tabAdapter;

    private ProgressBar progressBar;

    private SolicitudServicio detalle;

    private SolicitudServicioService solicitudServicioService;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                throw new Exception(getString(R.string.detalle_error_solicitud_servicio));
            }

            solicitudServicioService = new SolicitudServicioService(this, cuenta);
            detalle = database.where(SolicitudServicio.class)
                    .equalTo("UUID", bundle.getString(Mantum.KEY_UUID))
                    .equalTo("id", bundle.getLong(Mantum.KEY_ID))
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findFirst();

            if (detalle == null) {
                throw new Exception(getString(R.string.detalle_vacio_solicitud_servicio));
            }

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            includeBackButtonAndTitle(detalle.getCodigo());

            List<Mantum.Fragment> fragments = new ArrayList<>();
            fragments.add(new SolicitudServicioDetalleFragment());
            fragments.add(new EntidadesListaFragment());

            if (Version.check(this, 8) && UserPermission.check(this, UserPermission.MODULO_GESTION_SERVICIOS)) {
                fragments.add(new EstadoInicialDetalleFragment());
                fragments.add(new InformeTecnicoDetalleFragment());
                fragments.add(new RecursoAdicionalFragment().setAction(false));
            }

            fragments.add(new ProcesoFragment());
            fragments.add(new ImagenesFragment());
            fragments.add(new AdjuntoFragment());

            tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(), fragments);

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

        if (solicitudServicioService != null) {
            solicitudServicioService.close();
        }

        compositeDisposable.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_solicitud_servicio, menu);

        if (Version.check(this, 8)) {
            menu.findItem(R.id.solicitud_servicio_estado_inicial).setVisible(true);
            menu.findItem(R.id.solicitud_servicio_informe_tecnico).setVisible(true);
        }

        return true;
    }

    @Override
    public void onComplete(@NonNull String name) {
        if (!isFinishing() && progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        onRefresh(tabAdapter.getFragment(name));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        Bundle bundle;
        Intent intent;
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;

            case R.id.refresh:
                final Long id = detalle.getId();
                progressBar.setVisibility(View.VISIBLE);
                compositeDisposable.add(solicitudServicioService.fetchById(detalle.getId())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap(ordenTrabajos -> ordenTrabajos.isEmpty()
                                ? solicitudServicioService.remove(id)
                                : solicitudServicioService.save(ordenTrabajos))
                        .subscribe(this::onNext, this::onError, () -> {
                            if (detalle == null) {
                                Intent newIntent = new Intent();
                                newIntent.putExtra(Mantum.KEY_ID, id);
                                newIntent.putExtra(Mantum.KEY_REFRESH, true);
                                newIntent.putExtra(Mantum.KEY_MESSAGE, getString(R.string.error_detalle_solicitud_servicio));
                                backActivity(newIntent);
                                return;
                            }

                            onRefresh(tabAdapter.getFragment(viewPager.getCurrentItem()));
                            progressBar.setVisibility(View.GONE);
                        }));

                break;

            case R.id.solicitud_servicio_estado_inicial:
                bundle = new Bundle();
                bundle.putLong(Mantum.KEY_ID, detalle.getId());
                bundle.putString(EstadoInicialActivity.KEY_CODE, detalle.getCodigo());

                intent = new Intent(this, EstadoInicialActivity.class);
                intent.putExtras(bundle);

                startActivityForResult(intent, EstadoInicialActivity.REQUEST_ACTION);
                break;

            case R.id.solicitud_servicio_informe_tecnico:
                bundle = new Bundle();
                bundle.putLong(Mantum.KEY_ID, detalle.getId());
                bundle.putString(InformeTecnicoActivity.KEY_CODE, detalle.getCodigo());

                intent = new Intent(this, InformeTecnicoActivity.class);
                intent.putExtras(bundle);

                startActivityForResult(intent, InformeTecnicoActivity.REQUEST_ACTION);
                break;

            case R.id.solicitud_servicio_bitacora:
                bundle = new Bundle();
                bundle.putLong(BitacoraActivity.KEY_ID, detalle.getId());
                bundle.putString(BitacoraActivity.KEY_CODIGO, detalle.getCodigo());
                bundle.putInt(BitacoraActivity.KEY_TIPO_BITACORA, BitacoraActivity.SS);

                intent = new Intent(this, BitacoraActivity.class);
                intent.putExtras(bundle);

                startActivityForResult(intent, BitacoraActivity.REQUEST_ACTION);
                break;

            case R.id.solicitud_servicio_evaluar:
                bundle = new Bundle();
                bundle.putLong(EvaluarActivity.KEY_ID, detalle.getId());
                bundle.putString(EvaluarActivity.KEY_CODIGO, detalle.getCodigo());

                intent = new Intent(this, EvaluarActivity.class);
                intent.putExtras(bundle);

                startActivityForResult(intent, EvaluarActivity.REQUEST_ACTION);
                break;

            case R.id.solicitud_servicio_comentario:
                bundle = new Bundle();
                bundle.putLong(Mantum.KEY_ID, detalle.getId());
                bundle.putString(SolicitudServicioComentarActivity.KEY_CODE, detalle.getCodigo());

                intent = new Intent(this, SolicitudServicioComentarActivity.class);
                intent.putExtras(bundle);

                startActivityForResult(intent, SolicitudServicioComentarActivity.REQUEST_ACTION);
                break;

            case R.id.solicitud_servicio_recibir:
                bundle = new Bundle();
                bundle.putLong(RecibirSolicitudServicioActivity.KEY_ID, detalle.getId());
                bundle.putString(RecibirSolicitudServicioActivity.KEY_CODIGO, detalle.getCodigo());

                intent = new Intent(this, RecibirSolicitudServicioActivity.class);
                intent.putExtras(bundle);

                startActivityForResult(intent, RecibirSolicitudServicioActivity.REQUEST_ACTION);
                break;

            case R.id.ubicar:
                Mantum.goGoogleMap(this, detalle.getGmap());
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void onNext(List<SolicitudServicio> solicitudServicios) {
        if (solicitudServicios.isEmpty()) {
            detalle = null;
            return;
        }
        detalle = solicitudServicios.get(0);
    }

    private void onError(Throwable throwable) {
        if (!isFinishing() && progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                .show();
    }

    private void onRefresh(@Nullable Mantum.Fragment tabFragment) {
        if (tabFragment != null) {
            if (detalle != null && !detalle.isManaged()) {
                detalle = database.where(SolicitudServicio.class)
                        .equalTo("id", detalle.getId())
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();
            }

            switch (tabFragment.getKey()) {
                case SolicitudServicioDetalleFragment.KEY_TAB:
                    SolicitudServicio solicitudServicio = detalle.isManaged()
                            ? database.copyFromRealm(detalle)
                            : detalle;

                    SolicitudServicioDetalleFragment.class.cast(tabFragment)
                            .onRefresh(solicitudServicio);
                    break;

                case EntidadesListaFragment.KEY_TAB:
                    SolicitudServicio temporal = detalle.isManaged()
                            ? database.copyFromRealm(detalle)
                            : detalle;

                    Entidad entidad = null;
                    if (temporal.getEntidad() != null) {
                        String[] information = temporal.getEntidad().split("\\|");

                        entidad = new Entidad();
                        entidad.setId(temporal.getIdentidad());
                        entidad.setCodigo(information[0]);
                        entidad.setNombre(information[1]);
                        entidad.setTipo(temporal.getTipoentidad());
                        entidad.setOrden(0);
                    }

                    EntidadesListaFragment entidadesListaFragment
                            = EntidadesListaFragment.class.cast(tabFragment);

                    if (entidadesListaFragment != null && entidad != null) {
                        entidad = entidad.isManaged()
                                ? database.copyFromRealm(entidad)
                                : entidad;

                        entidadesListaFragment.onLoad(entidad);
                    }

                    break;

                case EstadoInicialDetalleFragment.KEY_TAB:
                    if (detalle.getEstadoInicial() == null) {
                        break;
                    }

                    EstadoInicial estadoInicial = detalle.getEstadoInicial().isManaged()
                            ? database.copyFromRealm(detalle.getEstadoInicial())
                            : detalle.getEstadoInicial();

                    EstadoInicialDetalleFragment.class.cast(tabFragment)
                            .onRefresh(estadoInicial);
                    break;

                case InformeTecnicoDetalleFragment.KEY_TAB:
                    if (detalle.getInformeTecnico() == null) {
                        break;
                    }

                    InformeTecnico informeTecnico = detalle.getInformeTecnico().isManaged()
                            ? database.copyFromRealm(detalle.getInformeTecnico())
                            : detalle.getInformeTecnico();

                    InformeTecnicoDetalleFragment.class.cast(tabFragment)
                            .onRefresh(informeTecnico);
                    break;

                case RecursoAdicionalFragment.KEY_TAB:
                    List<RecursoAdicional> recursosadicionales = detalle.getRecursosadicionales().isManaged()
                            ? database.copyFromRealm(detalle.getRecursosadicionales())
                            : detalle.getRecursosadicionales();

                    RecursoAdicionalFragment.class.cast(tabFragment)
                            .addResources(recursosadicionales);
                    break;

                case ProcesoFragment.KEY_TAB:
                    List<Proceso> procesos = detalle.getProcesos().isManaged()
                            ? database.copyFromRealm(detalle.getProcesos())
                            : detalle.getProcesos();
                    ProcesoFragment.class.cast(tabFragment).onRefresh(procesos);
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
}