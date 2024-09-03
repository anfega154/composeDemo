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

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Consumible;
import com.mantum.cmms.entity.Falla;
import com.mantum.cmms.entity.RepuestoManual;
import com.mantum.cmms.entity.RutaTrabajo;
import com.mantum.cmms.entity.Sitio;
import com.mantum.cmms.entity.parameter.UserParameter;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.cmms.fragment.AdjuntoFragment;
import com.mantum.cmms.fragment.ConsumiblesManualesListaFragment;
import com.mantum.cmms.fragment.FallaOTListaFragment;
import com.mantum.cmms.fragment.ImagenesFragment;
import com.mantum.cmms.fragment.EjecutoresListaFragment;
import com.mantum.cmms.fragment.EntidadesListaFragment;
import com.mantum.cmms.fragment.RecursoListaFragment;
import com.mantum.cmms.fragment.OrdenTrabajoDetalleFragment;
import com.mantum.cmms.fragment.RepuestosManualesListaFragment;
import com.mantum.cmms.fragment.RutaTrabajoFragment;
import com.mantum.cmms.fragment.SitioFragment;
import com.mantum.cmms.fragment.VariableFragment;
import com.mantum.cmms.entity.Adjuntos;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Ejecutores;
import com.mantum.cmms.entity.Entidad;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.cmms.entity.Recurso;
import com.mantum.cmms.entity.Variable;
import com.mantum.cmms.service.OrdenTrabajoService;
import com.mantum.cmms.util.Version;
import com.mantum.cmms.view.RecursoView;
import com.mantum.component.Mantum;
import com.mantum.component.adapter.TabAdapter;
import com.mantum.component.OnCompleteListener;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

import static com.mantum.cmms.entity.parameter.UserParameter.MOSTRAR_SITIO_SS;

public class DetalleOrdenTrabajoActivity extends Mantum.Activity implements OnCompleteListener {

    private static final String TAG = DetalleOrdenTrabajoActivity.class.getSimpleName();

    public static final String INCLUIR_ACCIONES = "INCLUIR_ACCIONES";
    public static final String OCULTAR_REGISTRO_BITACORA = "OCULTAR_REGISTRO_BITACORA";
    public static final String OCULTAR_REMOVER = "OCULTAR_REMOVER";
    public static final String OCULTAR_ACTUALIZAR = "OCULTAR_ACTUALIZAR";

    public static final String MODULO = "modulo";

    private Cuenta cuenta;

    private Database database;

    private ViewPager viewPager;

    private TabAdapter tabAdapter;

    private OrdenTrabajo detalle;

    private ProgressBar progressBar;

    private boolean incluirAcciones = true;

    private boolean ocultarRegistroBitacora = false;

    private boolean ocultarRemover = false;

    private boolean ocultarActualizar = false;

    private OrdenTrabajoService ordenTrabajoService;

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

            ordenTrabajoService = new OrdenTrabajoService(this, cuenta);
            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                throw new Exception(getString(R.string.detail_error_OT));
            }

            incluirAcciones = bundle.getBoolean(INCLUIR_ACCIONES, true);
            ocultarRegistroBitacora = bundle.getBoolean(OCULTAR_REGISTRO_BITACORA, false);
            ocultarRemover = bundle.getBoolean(OCULTAR_REMOVER, false);
            ocultarActualizar = bundle.getBoolean(OCULTAR_ACTUALIZAR, false);

            detalle = database.where(OrdenTrabajo.class)
                    .equalTo("UUID", bundle.getString(Mantum.KEY_UUID))
                    .equalTo("id", bundle.getLong(Mantum.KEY_ID))
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findFirst();

            if (detalle == null) {
                throw new Exception(getString(R.string.error_detail_ot));
            }

            detalle = detalle.isManaged()
                    ? database.copyFromRealm(detalle)
                    : detalle;

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            includeBackButtonAndTitle(detalle.getCodigo());

            List<Mantum.Fragment> tabs = new ArrayList<>();
            tabs.add(new OrdenTrabajoDetalleFragment());
            tabs.add(new EntidadesListaFragment());
            tabs.add(new FallaOTListaFragment());

            boolean mostrarSitioSS = true;
            if (UserParameter.getValue(getView().getContext(), MOSTRAR_SITIO_SS) != null)
                mostrarSitioSS = Boolean.parseBoolean(UserParameter.getValue(getView().getContext(), MOSTRAR_SITIO_SS));

            if (UserPermission.check(this, UserPermission.MODULO_GESTION_SERVICIOS, false) && mostrarSitioSS) {
                tabs.add(new SitioFragment());
            }

            if (UserPermission.check(this, UserPermission.LISTA_CHEQUEO, false)) {
                RutaTrabajoFragment rutaTrabajoFragment = new RutaTrabajoFragment();
                rutaTrabajoFragment.setIdExtra(detalle.getId());
                rutaTrabajoFragment.setAccionActualizar(false);
                rutaTrabajoFragment.setRealizarPeticionHttp(false);
                rutaTrabajoFragment.setModoVerDetalle(true);
                rutaTrabajoFragment.setParcial(true);
                rutaTrabajoFragment.setListaChequeo(true);
                tabs.add(rutaTrabajoFragment);
            }

            tabs.add(new RecursoListaFragment());
            tabs.add(new RepuestosManualesListaFragment());
            tabs.add(new ConsumiblesManualesListaFragment());

            EjecutoresListaFragment ejecutoresListaFragment = new EjecutoresListaFragment();
            if (!detalle.esCerrada()) {
                ejecutoresListaFragment.mostrarTiempos();
            }

            tabs.add(ejecutoresListaFragment);
            tabs.add(new VariableFragment());
            tabs.add(new ImagenesFragment());
            tabs.add(new AdjuntoFragment());

            tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(), tabs);

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
            FirebaseCrashlytics.getInstance().recordException(e);
            backActivity(e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }

        if (ordenTrabajoService != null) {
            ordenTrabajoService.close();
        }

        compositeDisposable.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (incluirAcciones) {
            getMenuInflater().inflate(R.menu.menu_orden_trabajo, menu);

            if (ocultarRegistroBitacora) {
                menu.findItem(R.id.bitacora).setVisible(false);
            }

            if (Version.check(this, 8)
                    && UserPermission.check(this, UserPermission.MODULO_GESTION_SERVICIOS, false)) {
                menu.findItem(R.id.inspeccion_electrica).setVisible(true);
                menu.findItem(R.id.contacto).setVisible(true);
                menu.findItem(R.id.recorrido_planta_externa).setVisible(true);
                menu.findItem(R.id.instalacion_planta_externa).setVisible(true);

                String registroTiempoOT = UserParameter.getValue(this, UserParameter.REGISTRO_TIEMPOS_OT);
                if (registroTiempoOT == null || registroTiempoOT.equals("1")) {
                    menu.findItem(R.id.tiempos).setVisible(true);
                }
            }

            if (UserPermission.check(this, UserPermission.LISTA_CHEQUEO, false)) {
                menu.findItem(R.id.check_list).setVisible(true);
            }

            if (Version.check(this, 11) && UserPermission.check(this, UserPermission.REGISTRAR_FIRMAS_OT)) {
                menu.findItem(R.id.firmaxentidad).setVisible(true);
            }

            if (Version.check(this, 17) && UserPermission.check(this, UserPermission.REGISTRAR_FALLAS, false)) {
                MenuItem registrarFalla = menu.findItem(R.id.registrar_falla);
                registrarFalla.setVisible(true);
            }

            String removerOT = UserParameter.getValue(this, UserParameter.REMOVER_OT);
            if (removerOT == null || removerOT.equals("1")) {
                menu.findItem(R.id.remove).setVisible(true);
            }

            if (ocultarRemover) {
                menu.findItem(R.id.remove).setVisible(false);
            }

            if (ocultarActualizar) {
                menu.findItem(R.id.refresh).setVisible(false);
            }

            if (UserPermission.check(this, UserPermission.RECIBIR_OT, true)) {
                MenuItem recibirOT = menu.findItem(R.id.recibir);
                recibirOT.setVisible(true);
            }

            if (UserPermission.check(this, UserPermission.TERMINAR_OT, true)) {
                MenuItem terminarOT = menu.findItem(R.id.terminar);
                terminarOT.setVisible(true);
            }
        }

        return true;
    }

    @Override
    public void onComplete(@NonNull String name) {
        onRefresh(tabAdapter.getFragment(name));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        Bundle bundle;
        Intent intent;

        if (detalle == null || detalle.getId() == null) {
            return false;
        }

        Long idOrdenTrabajo = detalle.getId();
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;

            case R.id.remove:
                AlertDialog.Builder builder
                        = new AlertDialog.Builder(this);

                builder.setTitle(R.string.titulo_eliminar_orden_trabajo);
                builder.setMessage(R.string.mensaje_eliminar_orden_trabajo);

                builder.setNegativeButton(R.string.aceptar, (dialog, which) -> compositeDisposable.add(ordenTrabajoService.remove(idOrdenTrabajo)
                        .subscribe(this::onNext, this::onError, () -> {
                            if (detalle == null) {
                                Intent newIntent = new Intent();
                                newIntent.putExtra(Mantum.KEY_ID, idOrdenTrabajo);
                                newIntent.putExtra(Mantum.KEY_REFRESH, true);
                                newIntent.putExtra(Mantum.KEY_MESSAGE, getString(R.string.eliminar_orde_trabajo));
                                backActivity(newIntent);
                            }
                        })));
                builder.setPositiveButton(R.string.cancelar, (dialog, which) -> dialog.dismiss());

                builder.setCancelable(true);
                builder.create();
                builder.show();

                break;

            case R.id.refresh:
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }

                compositeDisposable.add(ordenTrabajoService.fetchById(detalle.getId())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap(ordenTrabajos -> ordenTrabajos.isEmpty()
                                ? ordenTrabajoService.remove(idOrdenTrabajo)
                                : ordenTrabajoService.save(ordenTrabajos))
                        .subscribe(this::onNext, this::onError, () -> {

                            if (detalle == null) {
                                Intent newIntent = new Intent();
                                newIntent.putExtra(Mantum.KEY_ID, idOrdenTrabajo);
                                newIntent.putExtra(Mantum.KEY_REFRESH, true);
                                newIntent.putExtra(Mantum.KEY_MESSAGE, getString(R.string.error_detail_ot));
                                backActivity(newIntent);
                                return;
                            }

                            onRefresh(tabAdapter.getFragment(viewPager.getCurrentItem()));
                            progressBar.setVisibility(View.GONE);
                        }));

                break;

            case R.id.bitacora:
                bundle = new Bundle();
                bundle.putString(Mantum.KEY_UUID, detalle.getUUID());
                bundle.putLong(BitacoraActivity.KEY_ID, detalle.getId());
                bundle.putString(BitacoraActivity.KEY_CODIGO, detalle.getCodigo());
                bundle.putInt(BitacoraActivity.KEY_TIPO_BITACORA, BitacoraActivity.OT);

                intent = new Intent(this, BitacoraActivity.class);
                intent.putExtras(bundle);

                startActivityForResult(intent, BitacoraActivity.REQUEST_ACTION);
                break;

            case R.id.recibir:
                bundle = new Bundle();
                bundle.putLong(Mantum.KEY_ID, detalle.getId());

                intent = new Intent(this, RecibirOrdenTrabajoActivity.class);
                intent.putExtras(bundle);

                startActivityForResult(intent, RecibirOrdenTrabajoActivity.REQUEST_ACTION);
                break;

            case R.id.tiempos:
                bundle = new Bundle();
                bundle.putLong(Mantum.KEY_ID, detalle.getId());

                intent = new Intent(this, RegistrarTiemposActivity.class);
                intent.putExtras(bundle);

                startActivityForResult(intent, RegistrarTiemposActivity.REQUEST_ACTION);
                break;

            case R.id.inspeccion_electrica:
                bundle = new Bundle();
                bundle.putLong(Mantum.KEY_ID, detalle.getId());

                intent = new Intent(this, InspeccionElectricaActivity.class);
                intent.putExtras(bundle);

                startActivityForResult(intent, InspeccionElectricaActivity.REQUEST_ACTION);
                break;

            case R.id.contacto:
                bundle = new Bundle();
                bundle.putLong(Mantum.KEY_ID, detalle.getId());

                intent = new Intent(this, ActualizarContactoActivity.class);
                intent.putExtras(bundle);

                startActivityForResult(intent, ActualizarContactoActivity.REQUEST_ACTION);
                break;

            case R.id.terminar:
                menuItem.setVisible(Version.check(this, 5));

                bundle = new Bundle();
                bundle.putLong(Mantum.KEY_ID, detalle.getId());
                bundle.putString(TerminarOrdenTrabajoActivity.KEY_CODE, detalle.getCodigo());

                intent = new Intent(this, TerminarOrdenTrabajoActivity.class);
                intent.putExtras(bundle);

                startActivityForResult(intent, TerminarOrdenTrabajoActivity.REQUEST_ACTION);
                break;

            case R.id.check_list:
                if (detalle.getListachequeo().isEmpty()) {
                    Snackbar.make(getView(), R.string.lista_chequeo_error, Snackbar.LENGTH_LONG)
                            .show();
                    break;
                }

                bundle = new Bundle();
                bundle.putLong(DescargarRutaTrabajoActivity.ID_EXTRA, detalle.getId());
                bundle.putLongArray(DescargarRutaTrabajoActivity.ARRAY_ACTIVIDADES, detalle.getIdActividades());
                bundle.putBoolean(DescargarRutaTrabajoActivity.MODO_VER_DETALLE, false);
                bundle.putBoolean(DescargarRutaTrabajoActivity.ACCION_REFRESCAR, false);
                bundle.putBoolean(DescargarRutaTrabajoActivity.ACCION_PARCIAL, true);

                intent = new Intent(this, DescargarRutaTrabajoActivity.class);
                intent.putExtras(bundle);

                startActivityForResult(intent, DescargarRutaTrabajoActivity.REQUEST_ACTION);
                break;

            case R.id.firmaxentidad:
                bundle = new Bundle();
                bundle.putLong(Mantum.KEY_ID, detalle.getId());
                bundle.putString(Mantum.ENTITY_TYPE, "OT");
                bundle.putString(MODULO, "Órden de trabajo");

                intent = new Intent(this, FirmaxEntidadActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
                break;

            case R.id.recorrido_planta_externa:
                bundle = new Bundle();
                bundle.putLong(Mantum.KEY_ID, detalle.getId());

                intent = new Intent(this, RecorridoPlantaExternaActivity.class);
                intent.putExtras(bundle);

                startActivityForResult(intent, RecorridoPlantaExternaActivity.REQUEST_ACTION);
                break;

            case R.id.instalacion_planta_externa:
                bundle = new Bundle();
                bundle.putLong(Mantum.KEY_ID, detalle.getId());

                intent = new Intent(this, InstalacionPlantaExternaActivity.class);
                intent.putExtras(bundle);

                startActivityForResult(intent, InstalacionPlantaExternaActivity.REQUEST_ACTION);
                break;

            case R.id.asociar_fotografia:
                bundle = new Bundle();
                bundle.putLong(Mantum.KEY_ID, detalle.getId());
                bundle.putString(GaleriaActivity.KEY_TIPO_ENTIDAD, "OT");

                intent = new Intent(this, GaleriaActivity.class);
                intent.putExtras(bundle);

                startActivityForResult(intent, GaleriaActivity.REQUEST_ACTION);
                break;

            case R.id.registrar_falla:
                bundle = new Bundle();
                bundle.putLong(Mantum.KEY_ID, detalle.getId());

                intent = new Intent(this, FormularioFallaOTActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void onNext(@NonNull List<OrdenTrabajo> ordenTrabajos) {
        if (ordenTrabajos.isEmpty()) {
            detalle = null;
            return;
        }

        OrdenTrabajo row = ordenTrabajos.get(0);

        // Obtiene el detalle de la OT que se encuentra en la BD ya que anteriormente
        // se guardo en la BD
        Database database = new Database(this);
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return;
        }

        OrdenTrabajo current = database.where(OrdenTrabajo.class)
                .equalTo("id", row.getId())
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .findFirst();

        detalle = current != null && current.isManaged()
                ? database.copyFromRealm(current)
                : null;
    }

    private void onError(@NonNull Throwable throwable) {
        progressBar.setVisibility(View.GONE);
        if (throwable.getMessage() != null) {
            Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private void onRefresh(@Nullable Mantum.Fragment tabFragment) {
        if (detalle == null) {
            return;
        }

        if (tabFragment != null) {
            OrdenTrabajo current = database.where(OrdenTrabajo.class)
                    .equalTo("id", detalle.getId())
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findFirst();

            if (current == null) {
                return;
            }


            /*
            if (detalle != null && !detalle.isManaged()) {
                detalle = database.where(OrdenTrabajo.class)
                        .equalTo("id", detalle.getId())
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();
            }
            */

            switch (tabFragment.getKey()) {
                case OrdenTrabajoDetalleFragment.KEY_TAB:
                    OrdenTrabajo ordenTrabajo = current.isManaged()
                            ? database.copyFromRealm(current)
                            : current;
                    OrdenTrabajoDetalleFragment.class.cast(tabFragment).onRefresh(ordenTrabajo);
                    break;

                case EntidadesListaFragment.KEY_TAB:
                    if (current.getEntidades() == null) {
                        break;
                    }

                    List<Entidad> entidades = current.getEntidades().isManaged()
                            ? database.copyFromRealm(current.getEntidades())
                            : current.getEntidades();

                    EntidadesListaFragment entidadesListaFragment
                            = EntidadesListaFragment.class.cast(tabFragment);

                    if (entidadesListaFragment != null) {
                        entidadesListaFragment.onLoad(entidades);
                    }

                    break;

                case SitioFragment.KEY_TAB:
                    if (current.getSitio() != null) {
                        Sitio sitio = current.getSitio().isManaged()
                                ? database.copyFromRealm(current.getSitio())
                                : current.getSitio();

                        SitioFragment.class.cast(tabFragment)
                                .onRefresh(sitio);
                    }

                    break;

                case RecursoListaFragment.KEY_TAB:
                    List<Recurso> recursos = current.getRecursos().isManaged()
                            ? database.copyFromRealm(current.getRecursos())
                            : current.getRecursos();

                    RecursoListaFragment recursoListaFragment
                            = RecursoListaFragment.class.cast(tabFragment);

                    if (recursoListaFragment != null) {
                        recursoListaFragment.onRefresh(RecursoView.View.factory(recursos));
                    }

                    break;

                case FallaOTListaFragment.KEY_TAB:
                    List<Falla> fallas = current.getFallas().isManaged()
                            ? database.copyFromRealm(current.getFallas())
                            : current.getFallas();

                    FallaOTListaFragment fallaOTListaFragment = (FallaOTListaFragment) tabFragment;
                    fallaOTListaFragment.onRefresh(fallas);

                    break;

                case RepuestosManualesListaFragment.KEY_TAB:
                    List<RepuestoManual> repuestos = current.getRepuestos().isManaged()
                            ? database.copyFromRealm(current.getRepuestos())
                            : current.getRepuestos();

                    RepuestosManualesListaFragment repuestosManualesListaFragment = (RepuestosManualesListaFragment) tabFragment;
                    repuestosManualesListaFragment.onLoad(repuestos);

                    break;

                case ConsumiblesManualesListaFragment.KEY_TAB:
                    List<Consumible> consumibles = current.getConsumibles().isManaged()
                            ? database.copyFromRealm(current.getConsumibles())
                            : current.getConsumibles();

                    ConsumiblesManualesListaFragment consumiblesManualesListaFragment = (ConsumiblesManualesListaFragment) tabFragment;
                    consumiblesManualesListaFragment.onLoad(consumibles);

                    break;

                case EjecutoresListaFragment.KEY_TAB:
                    List<Ejecutores> ejecutores = new ArrayList<>();
                    if (current.getEjecutores() != null && !current.getEjecutores().isEmpty()) {
                        ejecutores = current.getEjecutores().isManaged()
                                ? database.copyFromRealm(current.getEjecutores())
                                : current.getEjecutores();
                    }

                    EjecutoresListaFragment ejecutoresListaFragment = (EjecutoresListaFragment) tabFragment;
                    ejecutoresListaFragment.onRefresh(ejecutores);

                    break;

                case RutaTrabajoFragment.KEY_TAB:
                    List<RutaTrabajo> rutatrabajo = current.getListachequeo().isManaged()
                            ? database.copyFromRealm(current.getListachequeo())
                            : current.getListachequeo();
                    ((RutaTrabajoFragment) tabFragment).onLoad(rutatrabajo);
                    break;

                case VariableFragment.KEY_TAB:
                    List<Variable> variables = current.getVariables().isManaged()
                            ? database.copyFromRealm(current.getVariables())
                            : current.getVariables();
                    ((VariableFragment) tabFragment).onLoad(variables);
                    break;

                case ImagenesFragment.KEY_TAB:
                    List<Adjuntos> imagenes = current.getImagenes().isManaged()
                            ? database.copyFromRealm(current.getImagenes())
                            : current.getImagenes();

                    ImagenesFragment.class.cast(tabFragment)
                            .inactivarModoEditar()
                            .onLoad(imagenes);
                    break;

                case AdjuntoFragment.KEY_TAB:
                    List<Adjuntos> adjuntos = current.getAdjuntos().isManaged()
                            ? database.copyFromRealm(current.getAdjuntos())
                            : current.getAdjuntos();

                    AdjuntoFragment.class.cast(tabFragment)
                            .onLoad(adjuntos);
                    break;
            }
        }
    }
}
