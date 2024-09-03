package com.mantum.cmms.activity;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Busqueda;
import com.mantum.cmms.entity.Certificado;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Equipo;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.cmms.entity.Recorrido;
import com.mantum.cmms.entity.RecorridoHistorico;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.entity.parameter.UserParameter;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.cmms.fragment.ActividadesTecnicoFragment;
import com.mantum.cmms.fragment.InicioFragment;
import com.mantum.cmms.fragment.OrdenTrabajoFragment;
import com.mantum.cmms.fragment.PendienteMantenimientoFragment;
import com.mantum.cmms.fragment.RutaTrabajoFragment;
import com.mantum.cmms.fragment.SolicitudServicioFragment;
import com.mantum.cmms.service.ATNotificationService;
import com.mantum.cmms.service.CalendarioService;
import com.mantum.cmms.service.CertificadoServices;
import com.mantum.cmms.service.DeviceServices;
import com.mantum.cmms.service.FallaService;
import com.mantum.cmms.service.GamaMantenimientoService;
import com.mantum.cmms.service.PingService;
import com.mantum.cmms.service.RecorridoHistoricoService;
import com.mantum.cmms.service.RecorridoService;
import com.mantum.cmms.service.TransaccionService;
import com.mantum.cmms.service.YardaService;
import com.mantum.cmms.task.GeolocalizacionTask;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.adapter.TabAdapter;
import com.mantum.component.service.Calendar;
import com.mantum.component.service.Geolocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;
import io.realm.RealmResults;
import com.mantum.cmms.activity.ValidarInventarioActivoActivity;
import com.mantum.demo.R;

import static com.mantum.cmms.entity.Transaccion.MODULO_ESTADO_USUARIO;
import static com.mantum.cmms.service.ATNotificationService.DISPONIBLE;
import static com.mantum.cmms.service.ATNotificationService.NO_DISPONIBLE;
import static com.mantum.component.Mantum.isDiskSpace;
import static com.mantum.component.Mantum.whitelist;

public class HomeActivity extends Mantum.Activity
        implements NavigationView.OnNavigationItemSelectedListener, OnCompleteListener {

    private static final String TAG = HomeActivity.class.getSimpleName();

    private Database database;

    private ViewPager viewPager;

    private TabAdapter tabAdapter;

    private NavigationView navigationView;

    private Cuenta cuenta;

    private TransaccionService transaccionService;

    private final Gson gson = new Gson();

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_home);

            FirebaseApp.initializeApp(this);
            database = new Database(this);
            transaccionService = new TransaccionService(this);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            if (!Geolocation.checkPermission(this)) {
                Geolocation.requestPermission(this);
            } else {
                whitelist(this);

                iniciarServicioUbicacion();
                obtenerEstadoActual();
            }

            List<Mantum.Fragment> tabs = new ArrayList<>();
            tabs.add(new InicioFragment());

            if (UserPermission.check(this, UserPermission.MODULO_PANEL_GESTION_SERVICIO)) {
                tabs.add(1, new ActividadesTecnicoFragment());
            }

            if (UserPermission.check(this, UserPermission.VER_LISTADO_ORDENES_DE_TRABAJO, true)) {
                tabs.add(new OrdenTrabajoFragment());
            }

            if (UserPermission.check(this, UserPermission.VER_LISTADO_RUTAS_DE_TRABAJO, true)) {
                tabs.add(new RutaTrabajoFragment().setModoVerDetalle(true));
            }

            if (UserPermission.check(this, UserPermission.VER_LISTADO_SOLICITUDES_DE_SERVICIO, true)) {
                tabs.add(new SolicitudServicioFragment());
            }

            if (UserPermission.check(this, UserPermission.VER_LISTADO_PENDIENTES, true)) {
                tabs.add(new PendienteMantenimientoFragment());
            }
            tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(), tabs);

            viewPager = findViewById(R.id.viewPager);
            viewPager.setAdapter(tabAdapter);
            viewPager.setOffscreenPageLimit(tabAdapter.getCount() - 1);

            TabLayout tabLayout = findViewById(R.id.tabs);
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            tabLayout.setupWithViewPager(viewPager);

            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta != null) {
                List<Transaccion> estadosErroneos = database.where(Transaccion.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .equalTo("estado", Transaccion.ESTADO_ERROR)
                        .equalTo("modulo", MODULO_ESTADO_USUARIO)
                        .findAll();

                AlertDialog estadosDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.titulo_transaccion_estado_error)
                        .setMessage(R.string.mensaje_transaccion_estado_error)
                        .setCancelable(false)
                        .setNegativeButton(R.string.close, (dialogInterface, i) -> dialogInterface.dismiss())
                        .setPositiveButton(R.string.ir_a_transacciones, (dialogInterface, i) -> startActivity(new Intent(this, TransaccionActivity.class)))
                        .create();

                viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float v, int i1) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        if (position == 1 && !estadosErroneos.isEmpty() && !estadosDialog.isShowing()) {
                            estadosDialog.show();
                        }
                    }

                    @Override
                    public void onPageScrollStateChanged(int i) {

                    }
                });

                View view = navigationView.getHeaderView(0);
                if (view != null) {
                    TextView name = view.findViewById(R.id.name);
                    name.setText(String.format("%s %s", cuenta.getName(), cuenta.getLastname()));

                    Bitmap bitmap = Mantum.convertToBase64(cuenta.getImage());
                    if (bitmap != null) {
                        ImageView photo = view.findViewById(R.id.photo);
                        photo.setImageBitmap(bitmap);
                    }
                }

                if (cuenta.getIdCalendario() == null && Calendar.requestPermission(this)) {
                    Calendar calendar = new Calendar(this);
                    Cursor cursor = calendar.find(cuenta.getUsername());

                    Long idCalendario;
                    if (cursor == null) {
                        idCalendario = calendar.createNew(cuenta.getUsername() + " - Mántum CMMS", cuenta.getUsername());
                    } else {
                        if (cursor.moveToFirst()) {
                            idCalendario = cursor.getLong(0);
                        } else {
                            idCalendario = calendar.createNew(cuenta.getUsername() + " - Mántum CMMS", cuenta.getUsername());
                        }

                        cursor.close();
                    }

                    if (idCalendario != null) {
                        database.beginTransaction();
                        cuenta.setIdCalendario(idCalendario);
                        database.commitTransaction();
                    }
                }

                DeviceServices deviceServices = new DeviceServices(this, cuenta);
                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    compositeDisposable.add(deviceServices.registrar(token)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(Functions.emptyConsumer(), Mantum::ignoreError, Functions.EMPTY_ACTION));
                });

                if (cuenta.getIdCalendario() != null && UserPermission.check(this, UserPermission.MENU_LATERAL_VER_OPCION_CALENDARIO, true)) {
                    CalendarioService calendarioService = new CalendarioService(this, cuenta);
                    compositeDisposable.add(calendarioService.fetch(cuenta.getId())
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .flatMap(value -> calendarioService.save(value.getCalendario()))
                            .subscribe(Functions.emptyConsumer(), Mantum::ignoreError, Functions.EMPTY_ACTION));
                }

                FallaService fallaService = new FallaService(this, cuenta);
                compositeDisposable.add(fallaService.getAllTiposFalla()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(fallaService::saveAllTiposFalla, Mantum::ignoreError, Functions.EMPTY_ACTION));

                GamaMantenimientoService gamaMantenimientoService = new GamaMantenimientoService(this, cuenta);
                compositeDisposable.add(gamaMantenimientoService.getClasificacionesGama()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(gamaMantenimientoService::saveClasificacionesGama, Mantum::ignoreError, Functions.EMPTY_ACTION));

                YardaService yardaService = new YardaService(this, cuenta);
                compositeDisposable.add(yardaService.getYardas()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(yardaService::saveYardas, Mantum::ignoreError, Functions.EMPTY_ACTION));
            }

            if (!isDiskSpace()) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setTitle(R.string.alerta);
                alertDialogBuilder.setMessage(R.string.alerta_espacio);
                alertDialogBuilder.setPositiveButton(R.string.aceptar, (dialog, which) -> dialog.cancel());
                alertDialogBuilder.show();
            }
        } catch (Exception e) {
            Log.e(TAG, "onCreate: ", e);
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    private void iniciarServicioUbicacion() {
        try {
            startService(new Intent(this, GeolocalizacionTask.class));
        } catch (Exception e) {
            Log.e(TAG, "iniciarServicioUbicacion: ", e);
        }
    }

    private void obtenerEstadoActual() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            boolean access = bundle.getBoolean("access", false);
            if (access) {
                ATNotificationService atNotificationService = new ATNotificationService(this);
                atNotificationService.disponible((value, error) -> {
                    if (error) {
                        Snackbar.make(getView(), value, Snackbar.LENGTH_LONG)
                                .show();
                    }
                }, true);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Geolocation.LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    iniciarServicioUbicacion();
                    obtenerEstadoActual();
                }
                break;

            case Geolocation.MY_LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    obtenerMiUbicacionActual();
                }
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        onComplete(InicioFragment.KEY_TAB);

        if (data != null) {
            if (data.getBooleanExtra(BusquedaActivity.ACTION, false)) {
                onComplete(InicioFragment.KEY_TAB);
            }

            Bundle bundle = data.getExtras();

            if (resultCode == DiligenciarRutaTrabajoActivity.REQUEST_ACTION) {
                bundle.putBoolean(DiligenciarRutaTrabajoActivity.ACCION_LINEA, true);
                Intent intent = new Intent(getView().getContext(), DiligenciarRutaTrabajoActivity.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, DiligenciarRutaTrabajoActivity.REQUEST_ACTION);
                return;
            }

            if (bundle != null && getView() != null) {
                String message = bundle.getString("message");
                if (message != null && !message.isEmpty()) {
                    Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        }

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            asociarCodigoEquipo(result);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (navigationView != null) {
            if (Version.check(this, 5) && UserPermission.check(this, UserPermission.MENU_LATERAL_VER_OPCION_MI_MOCHILA)) {
                MenuItem mochila = navigationView.getMenu().findItem(R.id.mochila);
                mochila.setVisible(true);
            }

            if (Version.check(this, 6) && UserPermission.check(this, UserPermission.ACEPTAR_TRANSFERENCIA, true)) {
                MenuItem transferencia = navigationView.getMenu().findItem(R.id.transferencia);
                transferencia.setVisible(true);
            }

            if (Version.check(this, 7) && !UserPermission.check(this, UserPermission.SOLICITUD_SERVICIO_CREAR)) {
                MenuItem solicitud = navigationView.getMenu().findItem(R.id.solicitud_servicio);
                solicitud.setVisible(false);
            }

            if (Version.check(this, 8)) {
                if (UserPermission.check(this, UserPermission.AUTORIZACION_ACCESO, true)) {
                    MenuItem security = navigationView.getMenu().findItem(R.id.security);
                    security.setVisible(true);
                }

                if (UserPermission.check(this, UserPermission.DESCARGAR_AUTORIZACIONES, true)) {
                    MenuItem mySecurity = navigationView.getMenu().findItem(R.id.my_security);
                    mySecurity.setVisible(true);
                }
            }

            if (Version.check(this, 15) && UserPermission.check(this, UserPermission.USUARIO_ALMACENISTA)) {
                MenuItem almacen = navigationView.getMenu().findItem(R.id.almacen);
                almacen.setVisible(true);
            }

            if (Version.check(this, 16) && UserPermission.check(this, UserPermission.CREAR_EQUIPO)) {
                MenuItem crearEquipo = navigationView.getMenu().findItem(R.id.crear_equipo);
                crearEquipo.setVisible(true);
            }

            if (Version.check(this, 17) && UserPermission.check(this, UserPermission.ENVIAR_IMAGENES_POR_CORREO)) {
                MenuItem enviarImagenesCorreo = navigationView.getMenu().findItem(R.id.imagenes_correo);
                enviarImagenesCorreo.setVisible(true);
            }

            if (UserPermission.check(this, UserPermission.GESTION_PTI_EIR, false)) {
                MenuItem item = navigationView.getMenu()
                        .findItem(R.id.manager_pti_eir);
                item.setVisible(true);
            }

            if (UserPermission.check(this, UserPermission.MENU_LATERAL_VER_OPCION_CALENDARIO, true)) {
                MenuItem calendario = navigationView.getMenu().findItem(R.id.calendar);
                calendario.setVisible(true);
            }

            if (UserPermission.check(this, UserPermission.MENU_LATERAL_VER_OPCION_VALIDAR_INGRESO_QR, false)) {
                MenuItem validacion = navigationView.getMenu().findItem(R.id.validacion);
                validacion.setVisible(true);
            }

            if (UserPermission.check(this, UserPermission.MENU_LATERAL_VER_OPCION_EVENTOS, true)) {
                MenuItem eventos = navigationView.getMenu().findItem(R.id.eventos);
                eventos.setVisible(true);
            }

            CertificadoServices certificadoServices = new CertificadoServices(this);
            Certificado certificado = certificadoServices.find(cuenta);
            if (certificado != null) {
                MenuItem actualizarCertificado = navigationView.getMenu().findItem(R.id.certificado);
                actualizarCertificado.setVisible(true);
            }

            if (Version.check(this, 17)) {
                MenuItem connectionTest = navigationView.getMenu().findItem(R.id.connection_test);
                connectionTest.setVisible(true);
            }

            String descargarEntidades = UserParameter.getValue(this, UserParameter.DESCARGAR_ENTIDADES);
            if (descargarEntidades != null && (descargarEntidades.equals("0") || descargarEntidades.equals("false"))) {
                MenuItem item = navigationView.getMenu().findItem(R.id.descargar_entidad);
                item.setVisible(false);
            }
            if (UserPermission.check(this, UserPermission.VALIDARINVENTARIO_ACCESO, false)) {
                MenuItem item = navigationView.getMenu().findItem(R.id.descargar_inventarios);
                item.setVisible(true);
            }
            if (UserPermission.check(this, UserPermission.DILIGENCIAR_LC_ENTIDAD, true)) {
                MenuItem item = navigationView.getMenu().findItem(R.id.lista_chequeo);
                item.setVisible(true);
            }

            if (UserPermission.check(this, UserPermission.VALIDARINVENTARIO_ACCESO, false)) {
                MenuItem item = navigationView.getMenu().findItem(R.id.inventario_activos);
                item.setVisible(true);
            }


            setTextItemSincronizar();
        }
    }

    @Override
    public void onComplete(@NonNull String name) {
        if (tabAdapter != null) {
            Mantum.Fragment fragment = tabAdapter.getFragment(name);
            if (fragment != null) {
                if (InicioFragment.KEY_TAB.equals(fragment.getKey())) {
                    InicioFragment inicioFragment = ((InicioFragment) tabAdapter.getFragment(name));
                    if (inicioFragment != null) {
                        inicioFragment.onRefresh();
                    }
                }
            }
        }
    }

    private void onSearch(@Nullable Mantum.Fragment tabFragment) {
        Intent intent = new Intent(this, BusquedaActivity.class);
        boolean[] checksBusqueda = new boolean[3];

        if (tabFragment == null) {
            startActivityForResult(intent, 1);
            return;
        }

        switch (tabFragment.getKey()) {
            case OrdenTrabajoFragment.KEY_TAB:
                checksBusqueda[0] = true;
                break;

            case RutaTrabajoFragment.KEY_TAB:
                checksBusqueda[1] = true;
                break;

            case SolicitudServicioFragment.KEY_TAB:
                checksBusqueda[2] = true;
        }

        intent.putExtra(BusquedaActivity.CHECKS_BUSQUEDA, checksBusqueda);
        startActivityForResult(intent, 1);
    }

    private void onRefresh(@Nullable Mantum.Fragment tabFragment) {
        if (tabFragment == null) {
            return;
        }

        switch (tabFragment.getKey()) {

            case ActividadesTecnicoFragment.KEY_TAB:
                ActividadesTecnicoFragment actividadesTecnicoFragment = (ActividadesTecnicoFragment) tabFragment;
                actividadesTecnicoFragment.onRefresh();
                break;

            case InicioFragment.KEY_TAB:
                InicioFragment.class.cast(tabFragment).onRefresh();
                break;

            case OrdenTrabajoFragment.KEY_TAB:
                OrdenTrabajoFragment.class.cast(tabFragment).onRefresh();
                break;

            case RutaTrabajoFragment.KEY_TAB:
                RutaTrabajoFragment.class.cast(tabFragment).onRefresh();
                break;

            case SolicitudServicioFragment.KEY_TAB:
                SolicitudServicioFragment.class.cast(tabFragment).onRefresh();
                break;

            case PendienteMantenimientoFragment.KEY_TAB:
                PendienteMantenimientoFragment.class.cast(tabFragment).onRefresh();
                break;

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_barra_acciones, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.bitacora) {
            Bundle bundle = new Bundle();
            bundle.putInt(BitacoraActivity.KEY_TIPO_BITACORA, BitacoraActivity.EVENT);

            Intent intent = new Intent(this, BitacoraActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, 1);
        } else if (itemId == R.id.mochila) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(MochilaActivity.ACTION, true);

            Intent intent = new Intent(this, MochilaActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, 1);
        } else if (itemId == R.id.certificado) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(CertificadoActivity.MODO_ACTUALIZAR, true);

            Intent intent = new Intent(this, CertificadoActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        } else if (itemId == R.id.almacen) {
            startActivityForResult(new Intent(this, AlmacenActivity.class), 1);
        } else if (itemId == R.id.calendar) {
            Calendar.open(this);
        } else if (itemId == R.id.solicitud_servicio) {
            irSolicitudServicio();
        } else if (itemId == R.id.cloud) {
            startActivityForResult(new Intent(this, TransaccionActivity.class), 1);
        } else if (itemId == R.id.security) {
            startActivityForResult(new Intent(this, AutorizacionAccesoActivity.class), 1);
        } else if (itemId == R.id.my_security) {
            startActivityForResult(new Intent(this, MiAutorizacionAccesoActivity.class), 1);
        } else if (itemId == R.id.descargar_entidad) {
            startActivityForResult(new Intent(this, DescargarEntidadesActivity.class), 1);
        }else if (itemId == R.id.descargar_inventarios) {
            startActivityForResult(new Intent(this, DescargarInventariosActivity.class), 1);
        }else if (itemId == R.id.descargar_recurso) {
            startActivityForResult(new Intent(this, DescargarRecursosActivity.class), 1);
        } else if (itemId == R.id.descargar_rutas_trabajo) {
            startActivityForResult(new Intent(this, DescargarRutaTrabajoActivity.class), 1);
        } else if (itemId == R.id.close) {
            salir();
        } else if (itemId == R.id.location) {
            ubicar();
        } else if (itemId == R.id.about) {
            informacion();
        } else if (itemId == R.id.connection_test) {
            connectionTest();
        } else if (itemId == R.id.setting) {
            startActivityForResult(new Intent(
                    this, ConfiguracionActivity.class), 1);
        } else if (itemId == R.id.transferencia) {
            startActivityForResult(new Intent(
                    this, TransferenciaActivity.class), 1);
        } else if (itemId == R.id.eventos) {
            startActivityForResult(new Intent(
                    this, EventoActivity.class), 1);
        } else if (itemId == R.id.validacion) {
            startActivity(new Intent(this, ValidarIngresoActivity.class));
        } else if (itemId == R.id.crear_equipo) {
            Intent intentFormularioEquipo = new Intent(this, FormularioEquipoActivity.class);
            intentFormularioEquipo.putExtra("crearEquipo", true);
            startActivity(intentFormularioEquipo);
        } else if (itemId == R.id.imagenes_correo) {
            startActivity(new Intent(this, EnviarImagenesCorreoActivity.class));
        } else if (itemId == R.id.manager_pti_eir) {
            startActivity(new Intent(this, InspeccionManagerActivity.class));
        } else if (itemId == R.id.lista_chequeo) {
            startActivity(new Intent(this, ListaChequeoEntidadActivity.class));
        } else if (itemId == R.id.inventario_activos) {
            startActivity(new Intent(this, InventarioActivosActivity.class));
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.search && viewPager != null) {
            onSearch(tabAdapter.getFragment(viewPager.getCurrentItem()));
            return true;
        }

        if (id == R.id.refresh) {
            onRefresh(tabAdapter.getFragment(viewPager.getCurrentItem()));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
        compositeDisposable.clear();
    }

    private void salir() {
        RecorridoHistoricoService recorridoHistoricoService = new RecorridoHistoricoService(this);
        RecorridoService recorridoService = new RecorridoService(this, RecorridoService.Tipo.OT);

        if (UserPermission.check(this, UserPermission.MODULO_PANEL_GESTION_SERVICIO, false)) {
            ATNotificationService atNotificationService = new ATNotificationService(this);
            atNotificationService.setDirectTransaction(true);
            ATNotificationService.Estado ultimoEstado = atNotificationService.getEstadoActual();

            AlertDialog.Builder dialogGeneral = new AlertDialog.Builder(this);
            RecorridoHistorico novedad = recorridoHistoricoService.findNovedad();

            boolean mostrarDialog = false;
            String text = null;

            if (!ultimoEstado.equals(DISPONIBLE) && !ultimoEstado.equals(NO_DISPONIBLE)) {
                if (novedad != null && novedad.getCategoria() != null && novedad.getCategoria().equals(novedad.getEstado())) {
                    Long ot = novedad.getIdentidad();
                    OrdenTrabajo ot_recorrido = database.where(OrdenTrabajo.class)
                            .equalTo("id", ot)
                            .findFirst();

                    if (ot_recorrido != null && ot_recorrido.isMovimiento()) {
                        text = "Tienes una novedad activa en la: " + ot_recorrido.getCodigo() + ", ¿Deseas cancelarla y cerrar sesión?";
                    } else {
                        text = "Tienes una novedad activa, ¿Deseas cancelarla y cerrar sesión?";
                    }
                    mostrarDialog = true;
                } else {
                    Recorrido actual = recorridoService.obtenerActual();
                    if (actual != null) {
                        Long ot = actual.getIdmodulo();
                        OrdenTrabajo ot_recorrido = database.where(OrdenTrabajo.class)
                                .equalTo("id", ot)
                                .findFirst();

                        if (ot_recorrido != null) {
                            text = "Tienes un recorrido activo en la: " + ot_recorrido.getCodigo() + ", ¿Deseas cancelarlo y cerrar sesión?";
                        } else {
                            text = "Tienes un recorrido activo, ¿Deseas cancelarlo y cerrar sesión?";
                        }
                        mostrarDialog = true;
                    }
                }
            }

            if (mostrarDialog) {
                dialogGeneral.setNegativeButton(R.string.aceptar, (dialog, which) -> atNotificationService.cancelar((value, error) -> {
                    if (error) {
                        Snackbar.make(getView(), value, Snackbar.LENGTH_LONG)
                                .show();
                        return;
                    }
                    closeApp();
                }, NO_DISPONIBLE, true));

                dialogGeneral.setPositiveButton(R.string.close, null);
                dialogGeneral.setMessage(text);
                dialogGeneral.setCancelable(false);
                dialogGeneral.setTitle(R.string.devolver_estado_disponible);
                dialogGeneral.show();
            } else {
                atNotificationService.homeNoDisponible((value, error) -> {
                    if (error) {
                        Snackbar.make(getView(), value, Snackbar.LENGTH_LONG)
                                .show();
                        return;
                    }
                    closeApp();
                });
            }
        } else {
            closeApp();
        }
    }

    public void closeApp() {
        UserParameter.saveParameter(this, UserParameter.ULTIMO_SERVIDOR, cuenta.getServidor().getUrl());
        UserParameter.saveParameter(this, UserParameter.ULTIMO_BASE_NAME, cuenta.getServidor().getNombre());
        database.executeTransaction(self -> {
            RealmResults<Cuenta> cuentas = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findAll();

            for (Cuenta cuenta : cuentas) {
                cuenta.setActive(false);
            }
        });

        Intent intent = new Intent(this, AccesoActivity.class);
        startActivity(intent);
        finish();
    }

    private void informacion() {
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null || cuenta.getServidor() == null) {
            return;
        }

        View about = View.inflate(this, R.layout.about, null);

        TextView app = about.findViewById(R.id.version_app);
        String versionApp = String.format("%s %s", getString(R.string.about_version_app), Mantum.versionName(this));
        app.setText(versionApp);

        String nombre = cuenta.getServidor().getNombre();
        if (nombre != null) {
            TextView viewDatabase = about.findViewById(R.id.database);
            viewDatabase.setVisibility(View.VISIBLE);
            viewDatabase.setText(String.format("%s %s", getString(R.string.about_database), nombre));
        }

        TextView connect = about.findViewById(R.id.connect);
        String url = String.format("%s %s", getString(R.string.about_connect), cuenta.getServidor().getUrl());
        connect.setText(url);

        Integer number = Version.get(getView().getContext());
        if (number == null) {
            number = 0;
        }

        TextView server = about.findViewById(R.id.version_server);
        String version = String.format("%s %s", getString(R.string.about_version_server), number);
        server.setText(version);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(about);
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.show();
    }

    private void obtenerMiUbicacionActual() {
        GeolocalizacionTask.Task task
                = new GeolocalizacionTask.Task(this, true, true);
        task.process();

        Snackbar.make(getView(), R.string.solicitud_posicion_actual_exito, Snackbar.LENGTH_LONG)
                .show();
    }

    private void ubicar() {
        if (!Geolocation.isEnabled(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.gps_disabled))
                    .setCancelable(false)
                    .setPositiveButton(R.string.si, (dialog, id) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setNegativeButton(R.string.no, (dialog, id) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();
            return;
        }

        if (!Geolocation.checkPermission(this)) {
            Geolocation.requestPermission(this, Geolocation.MY_LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        obtenerMiUbicacionActual();
    }

    private void irSolicitudServicio() {
        Intent intent = new Intent(this, SolicitudServicioActivity.class);
        startActivityForResult(intent, 1);
    }

    private void setTextItemSincronizar() {
        List<Transaccion> transaccionesPendientes = database.where(Transaccion.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("show", true)
                .equalTo("estado", Transaccion.ESTADO_PENDIENTE)
                .findAll();

        List<Transaccion> transaccionesErroneas = database.where(Transaccion.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("show", true)
                .equalTo("estado", Transaccion.ESTADO_ERROR)
                .findAll();

        MenuItem sincronizar = navigationView.getMenu().findItem(R.id.cloud);
        sincronizar.setTitle(String.format("%s (%s/%s)", "Sincronizar", transaccionesPendientes.size(), transaccionesErroneas.size()));
    }

    private void connectionTest() {
        String[] options = {"Enviar archivo"};
        boolean[] checked = new boolean[1];

        AlertDialog.Builder dialogConnectionTest = new AlertDialog.Builder(this);
        dialogConnectionTest.setTitle(R.string.menu_connection_test);
        dialogConnectionTest.setMultiChoiceItems(options, null, (dialog, which, isChecked) -> checked[0] = isChecked);
        dialogConnectionTest.setCancelable(false);
        dialogConnectionTest.setNegativeButton(R.string.cancel, null);
        dialogConnectionTest.setPositiveButton(R.string.iniciar_prueba_conexion, (dialogInterface, i) -> {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(getString(R.string.titulo_prueba_conexion_cargando));
            progressDialog.setMessage(getString(R.string.mensaje_prueba_conexion_cargando));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();

            long tiempoInicial = System.currentTimeMillis();
            PingService pingService = new PingService(this, cuenta);
            compositeDisposable.add(pingService.getPingTest(checked[0])
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(body -> {
                        long tiempoFinal = System.currentTimeMillis() - tiempoInicial;
                        progressDialog.dismiss();

                        new AlertDialog.Builder(this)
                                .setTitle(getString(R.string.titulo_prueba_conexion_exitosa))
                                .setMessage(String.format(getString(R.string.mensaje_prueba_conexion_exitosa), String.valueOf(tiempoFinal)))
                                .setNegativeButton(R.string.close, (dialogInterface1, id) -> dialogInterface1.dismiss())
                                .setCancelable(false)
                                .show();
                    }, throwable -> {
                        long tiempoFinal = System.currentTimeMillis() - tiempoInicial;
                        progressDialog.dismiss();

                        new AlertDialog.Builder(this)
                                .setTitle(getString(R.string.titulo_prueba_conexion_fallida))
                                .setMessage(String.format(getString(R.string.mensaje_prueba_conexion_fallida),
                                        cuenta.getServidor().getUrl(), String.valueOf(tiempoFinal), throwable.getMessage()))
                                .setNegativeButton(R.string.close, (dialogInterface1, id) -> dialogInterface1.dismiss())
                                .setCancelable(false)
                                .show();
                    }, Functions.EMPTY_ACTION));
        });
        dialogConnectionTest.show();
    }

    private void asociarCodigoEquipo(@NonNull IntentResult result) {
        String tipo = "barcode";
        String contents = result.getContents();

        if (BusquedaActivity.QR_CODE.equals(result.getFormatName())) {
            tipo = "qrcode";
            if (contents.startsWith("{")) {
                Busqueda.Read read;
                try {
                    read = gson.fromJson(result.getContents(), Busqueda.Read.class);
                } catch (Exception e) {
                    read = null;
                }

                if (read == null || read.getVersion() == 1) {
                    Snackbar.make(getView(), R.string.error_asociar_nfc_qr, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                contents = read.getEntityCode();
            }
        }

        if (BusquedaActivity.DATA_MATRIX.equals(result.getFormatName())) {
            tipo = "qrcode";
        }

        Busqueda busqueda = database.where(Busqueda.class)
                .equalTo("selected", true)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .findFirst();

        if (busqueda != null) {
            Equipo equipo = new Equipo();
            equipo.setId(busqueda.getId());

            Transaccion transaccion = new Transaccion();
            transaccion.setUUID(UUID.randomUUID().toString());
            transaccion.setCuenta(cuenta);
            transaccion.setCreation(java.util.Calendar.getInstance().getTime());
            transaccion.setUrl(cuenta.getServidor().getUrl() + "/restapp/app/actualizarmarcacionentidad");
            transaccion.setVersion(cuenta.getServidor().getVersion());
            transaccion.setValue(equipo.getInformacionParaAsociar(contents, tipo));
            transaccion.setModulo(Transaccion.MODULO_MARCACION);
            transaccion.setAccion(Transaccion.ACCION_ASOCIAR_CODIGO_QR_BARRAS_EQUIPO);
            transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);

            compositeDisposable.add(transaccionService.save(transaccion)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(Functions.emptyConsumer(), com.mantum.component.Mantum::ignoreError, () -> {
                        Snackbar.make(getView(), R.string.marcacion_en_proceso, Snackbar.LENGTH_LONG)
                                .show();
                    }));
        }
    }
}
