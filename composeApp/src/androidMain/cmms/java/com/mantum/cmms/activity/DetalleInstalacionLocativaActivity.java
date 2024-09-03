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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mantum.R;
import com.mantum.cmms.convert.BusquedaConvert;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Actividad;
import com.mantum.cmms.entity.Adjuntos;
import com.mantum.cmms.entity.Busqueda;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.DatosTecnico;
import com.mantum.cmms.entity.Falla;
import com.mantum.cmms.entity.InstalacionLocativa;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.entity.Variable;
import com.mantum.cmms.entity.parameter.UserParameter;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.cmms.fragment.ActividadMantenimientoListaFragment;
import com.mantum.cmms.fragment.AdjuntoFragment;
import com.mantum.cmms.fragment.DatosTecnicosListaFragment;
import com.mantum.cmms.fragment.FallaEquipoListaFragment;
import com.mantum.cmms.fragment.ImagenesFragment;
import com.mantum.cmms.fragment.InstalacionLocativaDetalleFragment;
import com.mantum.cmms.fragment.OrdenTrabajoListaFragment;
import com.mantum.cmms.fragment.VariableFragment;
import com.mantum.cmms.service.BusquedaServices;
import com.mantum.cmms.service.InstalacionLocativaServices;
import com.mantum.cmms.service.TransaccionService;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.adapter.TabAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.internal.functions.Functions;

public class DetalleInstalacionLocativaActivity extends Mantum.NfcActivity implements OnCompleteListener {

    private static final String TAG = DetalleInstalacionLocativaActivity.class.getSimpleName();

    private Database database;

    private TabAdapter tabAdapter;

    private ViewPager viewPager;

    private InstalacionLocativa detalle;

    private ProgressBar progressBar;

    private InstalacionLocativaServices instalacionLocativaServices;

    private BusquedaServices busquedaServices;

    private TransaccionService transaccionService;

    private InstalacionLocativaDetalleFragment instalacionLocativaDetalleFragment;

    private final Gson gson = new Gson();

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Cuenta cuenta;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_detalle);

            transaccionService = new TransaccionService(this);
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
                throw new Exception(getString(R.string.error_detalle));
            }

            detalle = database.where(InstalacionLocativa.class)
                    .equalTo("uuid", bundle.getString(Mantum.KEY_UUID))
                    .equalTo("id", bundle.getLong(Mantum.KEY_ID))
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findFirst();

            if (detalle == null) {
                throw new Exception(getString(R.string.error_detail_instalacion_locativa));
            }

            instalacionLocativaServices = new InstalacionLocativaServices(this, cuenta);
            busquedaServices = new BusquedaServices(this);
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            includeBackButtonAndTitle(detalle.getCodigo());

            instalacionLocativaDetalleFragment = new InstalacionLocativaDetalleFragment();

            List<Mantum.Fragment> tabs = new ArrayList<>();
            tabs.add(instalacionLocativaDetalleFragment);
            tabs.add(new DatosTecnicosListaFragment());
            tabs.add(new FallaEquipoListaFragment());
            tabs.add(new ActividadMantenimientoListaFragment());
            tabs.add(new VariableFragment().incluirAccion());
            tabs.add(new OrdenTrabajoListaFragment());
            tabs.add(new ImagenesFragment().incluirAccion());
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
                    onLoad(tabAdapter.getFragment(tab.getPosition()));
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }

            });
            viewPager.addOnPageChangeListener(
                    new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        } catch (Exception e) {
            Log.e(TAG, "onCreate: ", e);
            backActivity(e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }

        if (instalacionLocativaServices != null) {
            instalacionLocativaServices.onDestroy();
        }
        if (busquedaServices != null) {
            busquedaServices.onDestroy();
        }

        compositeDisposable.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_instalacion_locativa, menu);
        if (Version.check(this, 12) && UserPermission.check(this, UserPermission.MARCACION_EQUIPOS, true)) {
            MenuItem qr = menu.findItem(R.id.qr);
            qr.setVisible(true);

            MenuItem nfc = menu.findItem(R.id.nfc);
            nfc.setVisible(true);
        }
        if (UserPermission.check(this, UserPermission.REGISTRAR_LECTURA, true)) {
            MenuItem registrarLectura = menu.findItem(R.id.registrar_lectura);
            registrarLectura.setVisible(true);
        }
        if (Version.check(this, 17) && UserPermission.check(this, UserPermission.REGISTRAR_FALLAS, false)) {
            MenuItem registrarFalla = menu.findItem(R.id.registrar_falla);
            registrarFalla.setVisible(true);
        }

        String asociarEntidadBitacoraEvento = UserParameter.getValue(this, UserParameter.ASOCIAR_ENTIDAD_BITACORA_EVENTO);
        if (Version.check(this, 18) && asociarEntidadBitacoraEvento != null && asociarEntidadBitacoraEvento.equals("1")) {
            MenuItem registrarBitacoraEvento = menu.findItem(R.id.registrar_bitacora_evento);
            registrarBitacoraEvento.setVisible(true);
        }

        if (!UserPermission.check(this, UserPermission.BITACORA_REGISTRO_OT_BITACORA, true)) {
            MenuItem item = menu.findItem(R.id.registrar_ot_bitacora);
            item.setVisible(false);
        }

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

            case R.id.registrar_solicitud_servicio:
                bundle = new Bundle();
                bundle.putLong(SolicitudServicioActivity.KEY_ID, detalle.getId());
                bundle.putString(SolicitudServicioActivity.KEY_NAME, detalle.getNombreMostrar());
                bundle.putString(SolicitudServicioActivity.KEY_TYPE, InstalacionLocativa.SELF);

                intent = new Intent(this, SolicitudServicioActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
                break;

            case R.id.registrar_ot_bitacora:
                bundle = new Bundle();
                bundle.putLong(BitacoraActivity.KEY_ID, detalle.getId());
                bundle.putString(BitacoraActivity.KEY_CODIGO, detalle.getNombreMostrar());
                bundle.putString(BitacoraActivity.KEY_TIPO_ENTIDAD, InstalacionLocativa.SELF);
                bundle.putInt(BitacoraActivity.KEY_TIPO_BITACORA, BitacoraActivity.OT_BITACORA);

                intent = new Intent(this, BitacoraActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
                break;
                
            case R.id.registrar_bitacora_evento:
                bundle = new Bundle();
                bundle.putLong(BitacoraActivity.KEY_ID, detalle.getId());
                bundle.putString(BitacoraActivity.KEY_CODIGO, detalle.getNombreMostrar());
                bundle.putString(BitacoraActivity.KEY_TIPO_ENTIDAD, InstalacionLocativa.SELF);
                bundle.putInt(BitacoraActivity.KEY_TIPO_BITACORA, BitacoraActivity.EVENT);

                intent = new Intent(this, BitacoraActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
                break;


            case R.id.ver_ubicacion:
                if (detalle.getGmap() == null || detalle.getGmap().isEmpty()) {
                    Snackbar.make(getView(), R.string.ruta_sin_definir, Snackbar.LENGTH_LONG)
                            .show();
                    break;
                }

                Mantum.goGoogleMap(this, detalle.getGmap());
                break;


            case R.id.registrar_lectura:
                bundle = new Bundle();
                bundle.putLong(Mantum.KEY_ID, detalle.getId());
                bundle.putString(LecturaActivity.KEY_TYPE, InstalacionLocativa.SELF);

                intent = new Intent(this, LecturaActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
                break;


            case R.id.camara:
                bundle = new Bundle();
                bundle.putLong(Mantum.KEY_ID, detalle.getId());
                bundle.putString(GaleriaActivity.KEY_TIPO_ENTIDAD, InstalacionLocativa.SELF);

                intent = new Intent(this, GaleriaActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
                break;

            case R.id.nfc:
                if (detalle.getNfctoken() == null || detalle.getNfctoken().isEmpty()) {
                    Snackbar.make(getView(), R.string.token_no_asociado, Snackbar.LENGTH_LONG)
                            .show();
                    break;
                }

                prepareNFCWrite(Mantum.NfcActivity.write(detalle.getNfctoken(), false));
                break;

            case R.id.qr:
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.setOrientationLocked(true);
                integrator.setCameraId(0);
                integrator.setPrompt("");
                integrator.setCaptureActivity(CaptureActivityPortrait.class);
                integrator.setBeepEnabled(false);
                integrator.initiateScan();
                break;

            case R.id.actualizar:
                progressBar.setVisibility(View.VISIBLE);
                compositeDisposable.add(busquedaServices.buscar(detalle.getId(), "InstalacionLocativa")
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::onNextUpdate, this::onError, this::onCompleteUpdate));
                break;

            case R.id.registrar_falla:
                bundle = new Bundle();
                bundle.putString(Mantum.KEY_UUID, detalle.getUuid());
                bundle.putLong(Mantum.KEY_ID, detalle.getId());

                intent = new Intent(this, FormularioFallaInstalacionLocativaActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            String tipo = "barcode";
            String contents = result.getContents();
            if (BusquedaActivity.QR_CODE.equals(result.getFormatName())) {
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

                tipo = "qrcode";
                contents = read.getEntityCode();
            }

            Transaccion transaccion = new Transaccion();
            transaccion.setUUID(UUID.randomUUID().toString());
            transaccion.setCuenta(cuenta);
            transaccion.setCreation(Calendar.getInstance().getTime());
            transaccion.setUrl(cuenta.getServidor().getUrl() + "/restapp/app/actualizarmarcacionentidad");
            transaccion.setVersion(cuenta.getServidor().getVersion());
            transaccion.setValue(detalle.getInformacionParaAsociar(contents, tipo));
            transaccion.setModulo(Transaccion.MODULO_MARCACION);
            transaccion.setAccion(Transaccion.ACCION_ASOCIAR_CODIGO_QR_BARRAS_INSTALACION_LOCATIVA);
            transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);

            compositeDisposable.add(transaccionService.save(transaccion)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(Functions.emptyConsumer(), com.mantum.component.Mantum::ignoreError, () -> {
                        Snackbar.make(getView(), R.string.marcacion_en_proceso, Snackbar.LENGTH_LONG)
                                .show();
                    }));
        }
    }

    private void onError(@NonNull Throwable throwable) {
        Log.e(TAG, "onError: ", throwable);
        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void onComplete(@NonNull String name) {
        onLoad(tabAdapter.getFragment(name));
    }

    private void onNextUpdate(Response response) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(new TypeToken<Busqueda.Request>() {}.getType(), new BusquedaConvert())
                .create();

        Busqueda.Request content = gson.fromJson(response.getBody(), Busqueda.Request.class);
        List<Busqueda> busquedas = content.getEntities();
        if (busquedas.isEmpty()) {
            detalle = null;
            return;
        }

        InstalacionLocativa instalacionLocativa = busquedas.get(0).getDetalle(InstalacionLocativa.class);
        instalacionLocativa.setId(busquedas.get(0).getId());
        instalacionLocativa.setVariables(busquedas.get(0).getVariables());
        instalacionLocativa.setGmap(busquedas.get(0).getGmap());
        instalacionLocativa.setOrdenTrabajos(busquedas.get(0).getHistoricoOT());
        instalacionLocativa.setFallas(busquedas.get(0).getFallas());
        instalacionLocativaServices.update(instalacionLocativa);
    }

    public void onCompleteUpdate() {
        if (detalle == null) {
            Intent newIntent = new Intent();
            newIntent.putExtra(Mantum.KEY_REFRESH, true);
            newIntent.putExtra(Mantum.KEY_MESSAGE, getString(R.string.error_detail_equipo));
            backActivity(newIntent);
            return;
        }

        onLoad(tabAdapter.getFragment(viewPager.getCurrentItem()));
        progressBar.setVisibility(View.GONE);
    }

    public void onLoad(@Nullable Mantum.Fragment tabFragment) {
        if (tabFragment == null) {
            return;
        }

        switch (tabFragment.getKey()) {
            case InstalacionLocativaDetalleFragment.KEY_TAB:
                InstalacionLocativa instalacionLocativa = detalle.isManaged()
                        ? database.copyFromRealm(detalle)
                        : detalle;

                InstalacionLocativaDetalleFragment instalacionLocativaDetalleFragment
                        = InstalacionLocativaDetalleFragment.class.cast(tabFragment);

                if (instalacionLocativaDetalleFragment != null) {
                    instalacionLocativaDetalleFragment.onLoad(instalacionLocativa);
                }
                break;

            case DatosTecnicosListaFragment.KEY_TAB:
                List<DatosTecnico> datosTecnicos = detalle.getDatostecnicos().isManaged()
                        ? database.copyFromRealm(detalle.getDatostecnicos())
                        : detalle.getDatostecnicos();

                DatosTecnicosListaFragment datosTecnicosListaFragment
                        = DatosTecnicosListaFragment.class.cast(tabFragment);

                if (datosTecnicosListaFragment != null) {
                    datosTecnicosListaFragment.onLoad(datosTecnicos);
                }

                break;

            case FallaEquipoListaFragment.KEY_TAB:
                List<Falla> fallas = detalle.getFallas().isManaged()
                        ? database.copyFromRealm(detalle.getFallas())
                        : detalle.getFallas();

                FallaEquipoListaFragment fallaEquipoListaFragment = (FallaEquipoListaFragment) tabFragment;
                fallaEquipoListaFragment.onRefresh(fallas);

                break;

            case ActividadMantenimientoListaFragment.KEY_TAB:
                List<Actividad> actividades = detalle.getActividades().isManaged()
                        ? database.copyFromRealm(detalle.getActividades())
                        : detalle.getActividades();

                ActividadMantenimientoListaFragment actividadMantenimientoListaFragment
                        = ActividadMantenimientoListaFragment.class.cast(tabFragment);

                if (actividadMantenimientoListaFragment != null) {
                    actividadMantenimientoListaFragment.onLoad(actividades);
                }
                break;

            case VariableFragment.KEY_TAB:
                List<Variable> variables = detalle.getVariables().isManaged()
                        ? database.copyFromRealm(detalle.getVariables())
                        : detalle.getVariables();

                VariableFragment variableFragment
                        = VariableFragment.class.cast(tabFragment);

                if (variableFragment != null) {
                    variableFragment.onLoad(
                            detalle.getId(), InstalacionLocativa.SELF, Variable.incluirValor(variables));
                }

                break;

            case OrdenTrabajoListaFragment.KEY_TAB:
                List<OrdenTrabajo> ordentrabajo = detalle.getOrdenTrabajos().isManaged()
                        ? database.copyFromRealm(detalle.getOrdenTrabajos())
                        : detalle.getOrdenTrabajos();

                OrdenTrabajoListaFragment ordenTrabajoListaFragment
                        = OrdenTrabajoListaFragment.class.cast(tabFragment);

                if (ordenTrabajoListaFragment != null) {
                    ordenTrabajoListaFragment.onLoad(ordentrabajo);
                }
                break;

            case ImagenesFragment.KEY_TAB:
                List<Adjuntos> imagenes = detalle.getImagenes().isManaged()
                        ? database.copyFromRealm(detalle.getImagenes())
                        : detalle.getImagenes();

                ImagenesFragment imagenesFragment
                        = ImagenesFragment.class.cast(tabFragment);

                if (imagenesFragment != null) {
                    imagenesFragment.inactivarModoEditar()
                            .onLoad(detalle.getId(), InstalacionLocativa.SELF, imagenes);
                }

                break;

            case AdjuntoFragment.KEY_TAB:
                List<Adjuntos> adjuntos = detalle.getAdjuntos().isManaged()
                        ? database.copyFromRealm(detalle.getAdjuntos())
                        : detalle.getAdjuntos();

                AdjuntoFragment adjuntoFragment
                        = AdjuntoFragment.class.cast(tabFragment);

                if (adjuntoFragment != null) {
                    adjuntoFragment.onLoad(adjuntos);
                }

                break;
        }
    }
}
