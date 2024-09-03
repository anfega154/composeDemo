package com.mantum.cmms.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mantum.demo.R;
import com.mantum.cmms.adapter.BusquedaAlphabetAdapter;
import com.mantum.cmms.convert.BusquedaConvert;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Accion;
import com.mantum.cmms.entity.Actividad;
import com.mantum.cmms.entity.Busqueda;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.DetalleBusqueda;
import com.mantum.cmms.entity.Entidad;
import com.mantum.cmms.entity.Equipo;
import com.mantum.cmms.entity.Familia;
import com.mantum.cmms.entity.InstalacionLocativa;
import com.mantum.cmms.entity.InstalacionProceso;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.cmms.entity.ParsedJsonBusqueda;
import com.mantum.cmms.entity.Proveedor;
import com.mantum.cmms.entity.RutaTrabajo;
import com.mantum.cmms.entity.SolicitudServicio;
import com.mantum.cmms.entity.Variable;
import com.mantum.cmms.entity.busquedahelper.BusquedaQrConPropiedadEntityId;
import com.mantum.cmms.entity.busquedahelper.BusquedaQrConPropiedadId;
import com.mantum.cmms.entity.busquedahelper.BusquedaQrConPropiedadVersion;
import com.mantum.cmms.entity.busquedahelper.BusquedaQrSimple;
import com.mantum.cmms.entity.parameter.Barcode;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.cmms.handler.IProcesarBusquedaQrStrategy;
import com.mantum.cmms.service.BusquedaServices;
import com.mantum.cmms.service.EquipoServices;
import com.mantum.cmms.service.InstalacionLocativaServices;
import com.mantum.cmms.service.OrdenTrabajoService;
import com.mantum.cmms.service.RutaTrabajoService;
import com.mantum.cmms.service.SolicitudServicioService;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.OnSelected;
import com.mantum.component.swipe.SwipeController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.internal.functions.Functions;
import io.realm.Case;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class BusquedaActivity extends Mantum.NfcActivity implements SearchView.OnQueryTextListener {

    private static final String TAG = BusquedaActivity.class.getSimpleName();

    public static final String ACTION = "busqueda";

    public static final String QR_CODE = "QR_CODE";
    public static final String DATA_MATRIX = "DATA_MATRIX";

    public static final String CHECKS_BUSQUEDA = "CHECKS_BUSQUEDA";

    public static final String GRUPO_VARIABLE = "GrupoVariable";
    public static final String PERSONAL = "Personal";
    public static final String PIEZA = "Pieza";
    public static final String COMPONENTE = "Componente";
    public static final String RECURSO = "Recurso";
    private static final int OBJECT_SIZE = 2048;

    private Cuenta cuenta;

    private Database database;

    private ProgressBar progressBar;

    private BusquedaServices busquedaServices;

    private BusquedaAlphabetAdapter<Busqueda> alphabetAdapter;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private String contentScan = null;

    private final List<Busqueda> busquedaSinFiltros = new ArrayList<>();

    private CheckBox checkBusquedaOrdenTrabajo;
    private CheckBox checkBusquedaRutaTrabajo;
    private CheckBox checkBusquedaSolicitudServicio;
    private CheckBox checkBusquedaEquipo;
    private CheckBox checkBusquedaInstalacionLocativa;
    private CheckBox checkBusquedaInstalacionProceso;

    private CheckBox checkBusquedaGrupoVariable;
    private CheckBox checkBusquedaPesonal;
    private CheckBox checkBusquedaPieza;
    private CheckBox checkBusquedaComponente;
    private CheckBox checkBusquedaRecurso;
    private CheckBox checkBusquedaFamilia;
    private CheckBox checkBusquedaFabricante;
    private int limit = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_search);

            progressBar = findViewById(R.id.progressBar);
            includeBackButtonAndTitle(R.string.accion_buscar_entidad);

            database = new Database(this);
            busquedaServices = new BusquedaServices(this);

            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            checkBusquedaOrdenTrabajo = findViewById(R.id.check_busqueda_orden_trabajo);
            checkBusquedaRutaTrabajo = findViewById(R.id.check_busqueda_ruta_trabajo);
            checkBusquedaSolicitudServicio = findViewById(R.id.check_busqueda_solicitud_servicio);
            checkBusquedaEquipo = findViewById(R.id.check_busqueda_equipo);
            checkBusquedaInstalacionLocativa = findViewById(R.id.check_busqueda_instalacion_locativa);
            checkBusquedaInstalacionProceso = findViewById(R.id.check_busqueda_instalacion_proceso);

            checkBusquedaGrupoVariable = findViewById(R.id.check_busqueda_grupo_variable);
            checkBusquedaPesonal = findViewById(R.id.check_busqueda_personal);
            checkBusquedaPieza = findViewById(R.id.check_busqueda_pieza);
            checkBusquedaComponente = findViewById(R.id.check_busqueda_componente);
            checkBusquedaRecurso = findViewById(R.id.check_busqueda_recurso);
            checkBusquedaFamilia = findViewById(R.id.check_busqueda_familia);
            checkBusquedaFabricante = findViewById(R.id.check_busqueda_fabricante);

            RealmResults<Busqueda> query = database.where(Busqueda.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .equalTo("mostrar", true)
                    .findAll();

            List<Busqueda> busquedas = query.isManaged() ?
                    database.copyFromRealm(query)
                    : query;

            final String currentAccount = cuenta.getUUID();
            alphabetAdapter = new BusquedaAlphabetAdapter<>(this);
            alphabetAdapter.addAll(busquedas);
            alphabetAdapter.setOnAction(new OnSelected<Busqueda>() {

                @Override
                public void onClick(Busqueda value, int position) {
                    database.executeTransactionAsync(self -> {
                        List<Busqueda> cache = self.where(Busqueda.class)
                                .equalTo("cuenta.UUID", currentAccount)
                                .findAll();

                        for (Busqueda busqueda : cache) {
                            boolean selectedBusqueda = busqueda.getId().equals(value.getId()) && busqueda.getType().equals(value.getType());
                            if (busqueda.getIdejecucion() != null) {
                                selectedBusqueda = busqueda.getId().equals(value.getId()) && busqueda.getIdejecucion().equals(value.getIdejecucion()) && busqueda.getType().equals(value.getType());
                            }

                            if (selectedBusqueda) {
                                busqueda.setSelected(true);
                                busqueda.setMostrar(true);
                                continue;
                            }
                            busqueda.setSelected(false);
                        }
                    }, () -> {
                        Busqueda busqueda = database.where(Busqueda.class)
                                .equalTo("selected", true)
                                .equalTo("cuenta.UUID", currentAccount)
                                .findFirst();

                        if (busqueda != null) {
                            Bundle bundle;
                            Intent intent;

                            if (busqueda.getData() != null && !busqueda.getData().isEmpty()) {
                                backActivity();
                                return;
                            }

                            switch (busqueda.getType()) {
                                case Equipo.SELF:
                                    bundle = new Bundle();
                                    bundle.putString(Mantum.KEY_UUID, busqueda.getReference());
                                    bundle.putLong(Mantum.KEY_ID, busqueda.getId());

                                    intent = new Intent(
                                            BusquedaActivity.this, DetalleEquipoActivity.class);
                                    intent.putExtras(bundle);

                                    startActivity(intent);
                                    return;

                                case InstalacionLocativa.SELF:
                                    bundle = new Bundle();
                                    bundle.putString(Mantum.KEY_UUID, busqueda.getReference());
                                    bundle.putLong(Mantum.KEY_ID, busqueda.getId());

                                    intent = new Intent(
                                            BusquedaActivity.this, DetalleInstalacionLocativaActivity.class);
                                    intent.putExtras(bundle);

                                    startActivity(intent);
                                    break;

                                case OrdenTrabajo.SELF:
                                    bundle = new Bundle();
                                    bundle.putString(Mantum.KEY_UUID, busqueda.getReference());
                                    bundle.putLong(Mantum.KEY_ID, busqueda.getId());

                                    intent = new Intent(
                                            BusquedaActivity.this, DetalleOrdenTrabajoActivity.class);
                                    intent.putExtras(bundle);

                                    startActivity(intent);
                                    return;

                                case RutaTrabajo.SELF:
                                    bundle = new Bundle();
                                    bundle.putString(Mantum.KEY_UUID, busqueda.getReference());
                                    bundle.putLong(Mantum.KEY_ID, busqueda.getId());

                                    if (busqueda.getIdejecucion() != null) {
                                        bundle.putLong(DetalleRutaTrabajoActivity.ID_EJECUCION, busqueda.getIdejecucion());
                                    }

                                    intent = new Intent(
                                            BusquedaActivity.this, DetalleRutaTrabajoActivity.class);
                                    intent.putExtras(bundle);

                                    startActivity(intent);
                                    return;

                                case SolicitudServicio.SELF:
                                    bundle = new Bundle();
                                    bundle.putString(Mantum.KEY_UUID, busqueda.getReference());
                                    bundle.putLong(Mantum.KEY_ID, busqueda.getId());

                                    intent = new Intent(
                                            getApplicationContext(), DetalleSolicitudServicioActivity.class);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                    return;
                            }
                        }

                        onBackPressed();
                    }, throwable -> Snackbar.make(getView(), getString(R.string.select_search), Snackbar.LENGTH_LONG)
                            .show());
                }

                @Override
                public boolean onLongClick(Busqueda value, int position) {
                    return false;
                }

            });

            busquedaSinFiltros.addAll(busquedas);
            filtrarBusqueda();

            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            RecyclerView recyclerView = alphabetAdapter.startAdapter(getView(), layoutManager);

            SwipeController swipeController = new SwipeController(this);
            swipeController.setOnSwipeLeft(viewHolder -> {

                AlertDialog.Builder builder
                        = new AlertDialog.Builder(this);

                builder.setTitle(R.string.titulo_eliminar_busqueda);
                builder.setMessage(R.string.mensaje_eliminar_busqueda);

                builder.setNegativeButton(R.string.aceptar, (dialog, which) -> {
                    Busqueda current = alphabetAdapter.getOriginal().get(viewHolder.getAdapterPosition());
                    if (current != null) {
                        database.executeTransactionAsync(self -> self.where(Busqueda.class)
                                .equalTo("cuenta.UUID", currentAccount)
                                .equalTo("UUID", current.getUUID())
                                .findAll().deleteFirstFromRealm(), () -> {
                            alphabetAdapter.remove(viewHolder.getAdapterPosition(), true);
                            alphabetAdapter.showMessageEmpty(getView());
                        });
                    }
                });
                builder.setPositiveButton(R.string.cancelar, (dialog, which) -> dialog.dismiss());

                builder.setCancelable(true);
                builder.create();
                builder.show();

            }, getResources().getColor(R.color.negative_event), R.drawable.delete);
            swipeController.setOnSwipeRight(viewHolder -> {
                AlertDialog.Builder builder
                        = new AlertDialog.Builder(this);

                builder.setTitle(R.string.titulo_eliminar_busqueda);
                builder.setMessage(R.string.mensaje_eliminar_busqueda);

                builder.setNegativeButton(R.string.aceptar, (dialog, which) -> {
                    Busqueda current = alphabetAdapter.getOriginal().get(viewHolder.getAdapterPosition());
                    if (current != null) {
                        database.executeTransactionAsync(self -> self.where(Busqueda.class)
                                .equalTo("cuenta.UUID", currentAccount)
                                .equalTo("UUID", current.getUUID())
                                .findAll().deleteFirstFromRealm(), () -> {
                            alphabetAdapter.remove(viewHolder.getAdapterPosition(), true);
                            alphabetAdapter.showMessageEmpty(getView());
                        });
                    }
                });
                builder.setPositiveButton(R.string.cancelar, (dialog, which) -> dialog.dismiss());

                builder.setCancelable(true);
                builder.create();
                builder.show();
            }, getResources().getColor(R.color.negative_event), R.drawable.delete);

            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeController);
            itemTouchHelper.attachToRecyclerView(recyclerView);

            FloatingActionButton camera = findViewById(R.id.camera);
            camera.setOnClickListener(v -> {
                progressBar.setVisibility(View.VISIBLE);

                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.setOrientationLocked(true);
                integrator.setCameraId(0);
                integrator.setPrompt("");
                integrator.setCaptureActivity(CaptureActivityPortrait.class);
                integrator.setBeepEnabled(false);
                integrator.initiateScan();
            });

            checkBusquedaOrdenTrabajo.setOnCheckedChangeListener(customOnCheckedChangeListener(OrdenTrabajo.SELF));
            checkBusquedaRutaTrabajo.setOnCheckedChangeListener(customOnCheckedChangeListener(RutaTrabajo.SELF));
            checkBusquedaSolicitudServicio.setOnCheckedChangeListener(customOnCheckedChangeListener(SolicitudServicio.SELF));
            checkBusquedaEquipo.setOnCheckedChangeListener(customOnCheckedChangeListener(Equipo.SELF));
            checkBusquedaInstalacionLocativa.setOnCheckedChangeListener(customOnCheckedChangeListener(InstalacionLocativa.SELF));
            checkBusquedaInstalacionProceso.setOnCheckedChangeListener(customOnCheckedChangeListener(InstalacionProceso.SELF));

            checkBusquedaGrupoVariable.setOnCheckedChangeListener(customOnCheckedChangeListener(GRUPO_VARIABLE));
            checkBusquedaPesonal.setOnCheckedChangeListener(customOnCheckedChangeListener(PERSONAL));
            checkBusquedaPieza.setOnCheckedChangeListener(customOnCheckedChangeListener(PIEZA));
            checkBusquedaComponente.setOnCheckedChangeListener(customOnCheckedChangeListener(COMPONENTE));
            checkBusquedaRecurso.setOnCheckedChangeListener(customOnCheckedChangeListener(RECURSO));
            checkBusquedaFamilia.setOnCheckedChangeListener(customOnCheckedChangeListener(Familia.SELF));
            checkBusquedaFabricante.setOnCheckedChangeListener(customOnCheckedChangeListener(Proveedor.SELF));

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                boolean[] checksBusqueda = bundle.getBooleanArray(CHECKS_BUSQUEDA);
                checkBusquedaOrdenTrabajo.setChecked(checksBusqueda[0]);
                checkBusquedaRutaTrabajo.setChecked(checksBusqueda[1]);
                checkBusquedaSolicitudServicio.setChecked(checksBusqueda[2]);
            }

            if (UserPermission.check(this, UserPermission.VER_LISTADO_ORDENES_DE_TRABAJO, true)) {
                checkBusquedaOrdenTrabajo.setVisibility(View.VISIBLE);
            }

            if (UserPermission.check(this, UserPermission.VER_LISTADO_SOLICITUDES_DE_SERVICIO, true)) {
                checkBusquedaSolicitudServicio.setVisibility(View.VISIBLE);
            }

            if (Version.check(this, 13)) {
                if (UserPermission.check(this, UserPermission.VER_LISTADO_RUTAS_DE_TRABAJO, true)) {
                    checkBusquedaRutaTrabajo.setVisibility(View.VISIBLE);
                }

                checkBusquedaRecurso.setVisibility(View.VISIBLE);
                checkBusquedaFamilia.setVisibility(View.VISIBLE);
                checkBusquedaFabricante.setVisibility(View.VISIBLE);
            }

            ImageView imageBusquedaRelevante = findViewById(R.id.image_busqueda_relevante);
            LinearLayout layoutTituloBusquedaRelevante = findViewById(R.id.layout_titulo_busqueda_relevante);
            LinearLayout layoutCheckBusquedaRelevante = findViewById(R.id.layout_check_busqueda_relevante);

            layoutTituloBusquedaRelevante.setOnClickListener(view -> {
                boolean layoutBusquedaRelevanteIsVisible = layoutCheckBusquedaRelevante.getVisibility() == View.VISIBLE;
                layoutCheckBusquedaRelevante.setVisibility(layoutBusquedaRelevanteIsVisible ? View.GONE : View.VISIBLE);
                imageBusquedaRelevante.setImageResource(layoutBusquedaRelevanteIsVisible ? R.drawable.expand_more : R.drawable.expand_less);
            });

            ImageView imageFiltrosMostrarMas = findViewById(R.id.image_filtros_mostrar_mas);
            LinearLayout layoutTituloFiltrosMostrarMas = findViewById(R.id.layout_titulo_filtros_mostrar_mas);
            LinearLayout layoutCheckBusquedaMas = findViewById(R.id.layout_check_busqueda_mas);

            layoutTituloFiltrosMostrarMas.setOnClickListener(view -> {
                boolean layoutBusquedaMasIsVisible = layoutCheckBusquedaMas.getVisibility() == View.VISIBLE;
                layoutCheckBusquedaMas.setVisibility(layoutBusquedaMasIsVisible ? View.GONE : View.VISIBLE);
                imageFiltrosMostrarMas.setImageResource(layoutBusquedaMasIsVisible ? R.drawable.expand_more : R.drawable.expand_less);
            });
        } catch (Exception e) {
            backActivity(getString(R.string.error_app));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String value = prepareNFCRead(intent, false);
        if (value == null || value.isEmpty()) {
            return;
        }

        search(value, "nfc");
        super.onNewIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        progressBar.setVisibility(View.GONE);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        String contents = result.getContents();
        contentScan = contents;
        if (contents != null) {
            if (QR_CODE.equals(result.getFormatName())) {
                procesarQr(contents);
            } else {
                if (DATA_MATRIX.equals(result.getFormatName()))
                    search(contents, "qrcode");
                else
                    search(contents, "barcode");
            }
        } else {
            progressBar.setVisibility(View.GONE);
            Snackbar.make(getView(), getString(R.string.message_search_empty_scan), Snackbar.LENGTH_LONG)
                    .show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }

        if (alphabetAdapter != null) {
            alphabetAdapter.clear();
        }

        if (busquedaServices != null) {
            busquedaServices.onDestroy();
            busquedaServices.cancel();
        }

        compositeDisposable.clear();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(ACTION, true);
        backActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_busqueda, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(false);

        if (Version.check(this, 12)) {
            MenuItem filtro = menu.findItem(R.id.filtro);
            filtro.setVisible(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    private void procesarQr(String contents) {
        try {
            IProcesarBusquedaQrStrategy iProcesarBusquedaQrStrategy;
            if (contents.startsWith("{")) {
                if (contents.contains("\"entityid\"")) {
                    iProcesarBusquedaQrStrategy = new BusquedaQrConPropiedadEntityId();
                } else if (contents.contains("\"id\"")) {
                    iProcesarBusquedaQrStrategy = new BusquedaQrConPropiedadId();
                } else if (contents.contains("\"version\"")) {
                    iProcesarBusquedaQrStrategy = new BusquedaQrConPropiedadVersion();
                } else {
                    throw new Exception();
                }
            } else {
                iProcesarBusquedaQrStrategy = new BusquedaQrSimple();
            }

            ParsedJsonBusqueda parsedJsonBusqueda = iProcesarBusquedaQrStrategy.parser(contents);
            iProcesarBusquedaQrStrategy.saveAndSearch(parsedJsonBusqueda, this, busquedaServices);
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Snackbar.make(getView(), getString(R.string.message_search_empty_scan) + contents, Snackbar.LENGTH_LONG)
                    .show();
        }
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return search(query);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            backActivity();
        } else if (itemId == R.id.filtro) {
            Intent intent = new Intent(this, BusquedaAvanzadaActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean search(@Nullable String criterio, @NonNull String tipo) {
        try {
            RealmResults<Busqueda> query = database.where(Busqueda.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .beginGroup()
                    .equalTo("nfc", criterio).or()
                    .equalTo("qrcode", criterio).or()
                    .equalTo("barcode.codigo", criterio).endGroup()
                    .findAll();

            List<Busqueda> busquedas = query.isManaged()
                    ? database.copyFromRealm(query)
                    : query;

            buscarTrabajosAsociados(busquedas);

            busquedaServices.cancel();
            progressBar.setVisibility(View.VISIBLE);

            compositeDisposable.add(busquedaServices.buscar(criterio, tipo)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onNext, this::onError, this::onComplete));

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean search(String newText) {
        return search((Long) null, newText);
    }

    public boolean search(@Nullable Long id, @NonNull String newText) {
        try {
            RealmQuery<Busqueda> query = database.where(Busqueda.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID());

            if (id != null) {
                query.equalTo("id", id)
                        .equalTo("type", newText);
            } else {
                query.beginGroup()
                        .contains("code", newText, Case.INSENSITIVE).or()
                        .contains("name", newText, Case.INSENSITIVE)
                        .endGroup();
            }
            long maxHeapSize = Runtime.getRuntime().maxMemory();
            long maxObjects = maxHeapSize / OBJECT_SIZE;

            RealmResults<Busqueda> values = query.limit((int) maxObjects).findAll();
            List<Busqueda> busquedas = values.isManaged()
                    ? database.copyFromRealm(values)
                    : values;

            buscarTrabajosAsociados(busquedas);

            busquedaServices.cancel();
            progressBar.setVisibility(View.VISIBLE);
            String entitytypes = getCheckedsEntitytypes();
            compositeDisposable.add(busquedaServices.buscar(id, newText, entitytypes)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onNext, this::onError, this::onComplete));

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void buscarTrabajosAsociados(List<Busqueda> busquedas) {
        alphabetAdapter.clear();
        busquedaSinFiltros.clear();

        for (Busqueda busqueda : busquedas) {
            if (busqueda.getType().equals(Equipo.SELF) || busqueda.getType().equals(InstalacionLocativa.SELF) || busqueda.getType().equals(InstalacionProceso.SELF)) {
                RealmResults<Busqueda> trabajosAsociados = database.where(Busqueda.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .contains("entidadesrelacionadas", busqueda.getCode())
                        .beginGroup()
                        .equalTo("type", OrdenTrabajo.SELF).or()
                        .equalTo("type", RutaTrabajo.SELF).or()
                        .equalTo("type", SolicitudServicio.SELF)
                        .endGroup()
                        .findAll();

                alphabetAdapter.addAll(trabajosAsociados);
                busquedaSinFiltros.addAll(trabajosAsociados);
            }
        }

        alphabetAdapter.addAll(busquedas);
        busquedaSinFiltros.addAll(busquedas);
        filtrarBusqueda();
    }

    private void guardarBusquedaOrdenTrabajo(@NonNull final List<OrdenTrabajo> ordenTrabajos) {
        Database database = new Database(this);
        database.executeTransaction(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                return;
            }

            for (OrdenTrabajo ordenTrabajo : ordenTrabajos) {
                StringBuilder entidadesRelacionadas = new StringBuilder();
                for (Entidad entidad : ordenTrabajo.getEntidades()) {
                    entidadesRelacionadas.append(entidad.getCodigo()).append(" | ").append(entidad.getNombre()).append(", ");
                }

                entidadesRelacionadas = new StringBuilder(borrarUltimosDosCaracteres(entidadesRelacionadas.toString()));

                Busqueda busqueda = self.where(Busqueda.class)
                        .equalTo("id", ordenTrabajo.getId())
                        .equalTo("type", OrdenTrabajo.SELF)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();

                if (busqueda == null) {
                    busqueda = new Busqueda();
                    busqueda.generateUUID();
                    busqueda.setCuenta(cuenta);
                    busqueda.setId(ordenTrabajo.getId());
                    busqueda.setCode(ordenTrabajo.getCodigo());
                    busqueda.setName("");
                    busqueda.setReference(ordenTrabajo.getUUID());
                    busqueda.setMostrar(true);
                    busqueda.setType(OrdenTrabajo.SELF);
                    busqueda.setEntidadesrelacionadas(entidadesRelacionadas.toString());
                    busqueda.setInformacionvisualextra(ordenTrabajo.getFechainicio() + " - " + ordenTrabajo.getFechafin());

                    self.insert(busqueda);
                } else {
                    busqueda.setId(ordenTrabajo.getId());
                    busqueda.setCode(ordenTrabajo.getCodigo());
                    busqueda.setName("");
                    busqueda.setReference(ordenTrabajo.getUUID());
                    busqueda.setType(OrdenTrabajo.SELF);
                    busqueda.setEntidadesrelacionadas(entidadesRelacionadas.toString());
                    busqueda.setInformacionvisualextra(ordenTrabajo.getFechainicio() + " - " + ordenTrabajo.getFechafin());
                    busqueda.setMostrar(true);
                }
            }
        });
    }

    private void guardarBusquedaRutaTrabajo(@NonNull final List<RutaTrabajo> rutaTrabajos) {
        Database database = new Database(this);
        database.executeTransaction(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                return;
            }

            for (RutaTrabajo rutaTrabajo : rutaTrabajos) {
                StringBuilder entidadesRelacionadas = new StringBuilder();
                for (Entidad entidad : rutaTrabajo.getEntidades()) {
                    entidadesRelacionadas.append(entidad.getCodigo()).append(" | ").append(entidad.getNombre()).append(", ");
                }

                entidadesRelacionadas = new StringBuilder(borrarUltimosDosCaracteres(entidadesRelacionadas.toString()));

                Busqueda busqueda = self.where(Busqueda.class)
                        .equalTo("id", rutaTrabajo.getId())
                        .equalTo("idejecucion", rutaTrabajo.getIdejecucion())
                        .equalTo("type", RutaTrabajo.SELF)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();

                if (busqueda == null) {
                    busqueda = new Busqueda();
                    busqueda.generateUUID();
                    busqueda.setCuenta(cuenta);
                    busqueda.setId(rutaTrabajo.getId());
                    busqueda.setIdejecucion(rutaTrabajo.getIdejecucion());
                    busqueda.setCode(rutaTrabajo.getCodigo());
                    busqueda.setName(rutaTrabajo.getNombre());
                    busqueda.setReference(rutaTrabajo.getUUID());
                    busqueda.setMostrar(true);
                    busqueda.setType(RutaTrabajo.SELF);
                    busqueda.setEntidadesrelacionadas(entidadesRelacionadas.toString());
                    busqueda.setInformacionvisualextra(rutaTrabajo.getFecha());

                    self.insert(busqueda);
                } else {
                    busqueda.setId(rutaTrabajo.getId());
                    busqueda.setIdejecucion(rutaTrabajo.getIdejecucion());
                    busqueda.setCode(rutaTrabajo.getCodigo());
                    busqueda.setName(rutaTrabajo.getNombre());
                    busqueda.setReference(rutaTrabajo.getUUID());
                    busqueda.setType(RutaTrabajo.SELF);
                    busqueda.setEntidadesrelacionadas(entidadesRelacionadas.toString());
                    busqueda.setInformacionvisualextra(rutaTrabajo.getFecha());
                    busqueda.setMostrar(true);
                }
            }
        });
    }

    private void guardarBusquedaInstalacionLocativa(@NonNull final List<InstalacionLocativa> instalacionlocativa) {
        Database database = new Database(this);
        database.executeTransaction(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                return;
            }

            for (InstalacionLocativa instalacionLocativa : instalacionlocativa) {
                Busqueda busqueda = self.where(Busqueda.class)
                        .equalTo("id", instalacionLocativa.getId())
                        .equalTo("type", InstalacionLocativa.SELF)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();

                Barcode barcode = new Barcode();
                barcode.setCodigo(instalacionLocativa.getBarcode());
                RealmList<Barcode> barcodes = new RealmList<>();
                barcodes.add(self.copyToRealm(barcode));

                if (busqueda == null) {
                    busqueda = new Busqueda();
                    busqueda.generateUUID();
                    busqueda.setCuenta(cuenta);
                    busqueda.setId(instalacionLocativa.getId());
                    busqueda.setCode(instalacionLocativa.getCodigo());
                    busqueda.setName(instalacionLocativa.getNombre());
                    busqueda.setReference(instalacionLocativa.getUuid());
                    busqueda.setType(InstalacionLocativa.SELF);
                    busqueda.setMostrar(true);
                    busqueda.setNfc(instalacionLocativa.getNfctoken());
                    busqueda.setBarcode(barcodes);
                    busqueda.setQrcode(instalacionLocativa.getQrcode());
                    busqueda.setData(null);
                    busqueda.setInformacionvisualextra(instalacionLocativa.getInstalacionpadre());
                    self.insert(busqueda);
                } else {
                    busqueda.setId(instalacionLocativa.getId());
                    busqueda.setCode(instalacionLocativa.getCodigo());
                    busqueda.setName(instalacionLocativa.getNombre());
                    busqueda.setReference(instalacionLocativa.getUuid());
                    busqueda.setType(InstalacionLocativa.SELF);
                    busqueda.setNfc(instalacionLocativa.getNfctoken());
                    busqueda.setBarcode(barcodes);
                    busqueda.setQrcode(instalacionLocativa.getQrcode());
                    busqueda.setData(null);
                    busqueda.setInformacionvisualextra(instalacionLocativa.getInstalacionpadre());
                    busqueda.setMostrar(true);
                }
            }
        });
    }

    private void guardarSolicitudServicio(@NonNull List<SolicitudServicio> solicitudServicios) {
        Database database = new Database(this);
        database.executeTransaction(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                return;
            }

            for (SolicitudServicio solicitudServicio : solicitudServicios) {
                String entidadesRelacionadas = "";
                if (solicitudServicio.getEntidad() != null && !solicitudServicio.getEntidad().isEmpty()) {
                    entidadesRelacionadas = solicitudServicio.getEntidad();
                }

                Busqueda busqueda = self.where(Busqueda.class)
                        .equalTo("id", solicitudServicio.getId())
                        .equalTo("type", SolicitudServicio.SELF)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();

                if (busqueda == null) {
                    busqueda = new Busqueda();
                    busqueda.generateUUID();
                    busqueda.setCuenta(cuenta);
                    busqueda.setId(solicitudServicio.getId());
                    busqueda.setCode(solicitudServicio.getCodigo());
                    busqueda.setName("");
                    busqueda.setType(SolicitudServicio.SELF);
                    busqueda.setEntidadesrelacionadas(entidadesRelacionadas);
                    busqueda.setInformacionvisualextra(solicitudServicio.getDescripcion());
                    busqueda.setReference(solicitudServicio.getUUID());
                    busqueda.setMostrar(true);
                    self.insert(busqueda);
                } else {
                    busqueda.setId(solicitudServicio.getId());
                    busqueda.setCode(solicitudServicio.getCodigo());
                    busqueda.setName("");
                    busqueda.setType(SolicitudServicio.SELF);
                    busqueda.setEntidadesrelacionadas(entidadesRelacionadas);
                    busqueda.setInformacionvisualextra(solicitudServicio.getDescripcion());
                    busqueda.setReference(solicitudServicio.getUUID());
                    busqueda.setMostrar(true);
                }
            }
        });
    }

    private void guardarBusquedaEquipo(@NonNull final List<Equipo> equipos) {
        Database database = new Database(this);
        database.executeTransaction(self -> {
            try {
                Cuenta cuenta = self.where(Cuenta.class)
                        .equalTo("active", true)
                        .findFirst();

                if (cuenta == null) {
                    return;
                }

                for (Equipo equipo : equipos) {
                    Busqueda busqueda = self.where(Busqueda.class)
                            .equalTo("id", equipo.getId())
                            .equalTo("type", Equipo.SELF)
                            .equalTo("cuenta.UUID", cuenta.getUUID())
                            .findFirst();

                    if (busqueda == null) {
                        busqueda = new Busqueda();
                        busqueda.generateUUID();
                        busqueda.setCuenta(cuenta);
                        busqueda.setId(equipo.getId());
                        busqueda.setCode(equipo.getCodigo());
                        busqueda.setName(equipo.getNombre());
                        busqueda.setType(Equipo.SELF);
                        busqueda.setReference(equipo.getUUID());
                        busqueda.setMostrar(true);
                        busqueda.setNfc(equipo.getNfctoken());
                        busqueda.setBarcode(equipo.getBarcode());
                        busqueda.setQrcode(equipo.getQrcode());
                        busqueda.setData(null);
                        busqueda.setInformacionvisualextra(equipo.getInstalacionproceso());
                        self.insert(busqueda);
                    } else {
                        busqueda.setId(equipo.getId());
                        busqueda.setCode(equipo.getCodigo());
                        busqueda.setName(equipo.getNombre());
                        busqueda.setReference(equipo.getUUID());
                        busqueda.setType(Equipo.SELF);
                        busqueda.setNfc(equipo.getNfctoken());

                        RealmList<Barcode> barcodes = new RealmList<>();
                        for (Barcode barcode : equipo.getBarcode()) {
                            barcodes.add(self.copyToRealm(barcode));
                        }

                        busqueda.setBarcode(barcodes);
                        busqueda.setQrcode(equipo.getQrcode());
                        busqueda.setMostrar(true);
                        busqueda.setData(null);
                        busqueda.setInformacionvisualextra(equipo.getInstalacionproceso());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "guardarBusquedaEquipo: ", e);
            }
        });
    }

    private void onNext(@NonNull Response response) {
        Version.save(getApplicationContext(), response.getVersion());

        Database database = new Database(this);
        database.executeTransaction(self -> {
            try {
                Cuenta cuenta = self.where(Cuenta.class)
                        .equalTo("active", true)
                        .findFirst();

                if (cuenta == null) {
                    return;
                }

                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(new TypeToken<Busqueda.Request>() {
                        }.getType(), new BusquedaConvert())
                        .create();

                InstalacionLocativaServices instalacionLocativaServices = new InstalacionLocativaServices(this, cuenta);
                EquipoServices equipoServices = new EquipoServices(this, cuenta);
                OrdenTrabajoService ordenTrabajoService = new OrdenTrabajoService(this, cuenta);
                RutaTrabajoService rutaTrabajoService = new RutaTrabajoService(this, cuenta);
                SolicitudServicioService solicitudServicioService = new SolicitudServicioService(this, cuenta);

                Busqueda.Request content = gson.fromJson(response.getBody(), Busqueda.Request.class);

                for (Busqueda busqueda : content.getEntities()) {
                    String informacionVisualExtra = null;
                    boolean viejo = false;
                    switch (busqueda.getType()) {

                        case Equipo.SELF:
                            Equipo equipo = null;
                            if (Version.check(this, 16)) {
                                equipo = busqueda.getDetalle(Equipo.class);
                            } else {
                                Equipo.EquipoAux equipoAux = busqueda.getDetalle(Equipo.EquipoAux.class);
                                if (equipoAux != null) {
                                    equipo = new Equipo();
                                    equipo.setCodigo(equipoAux.getCodigo());
                                    equipo.setNombre(equipoAux.getNombre());
                                    equipo.setInstalacionproceso(equipoAux.getInstalacionproceso());
                                    equipo.setInstalacionlocativa(equipoAux.getInstalacionlocativa());
                                    equipo.setFamilia1(equipoAux.getFamilia1());
                                    equipo.setFamilia2(equipoAux.getFamilia2());
                                    equipo.setFamilia3(equipoAux.getFamilia3());
                                    equipo.setProvocaparo(equipoAux.getProvocaparo());
                                    equipo.setUbicacion(equipoAux.getUbicacion());
                                    equipo.setObservaciones(equipoAux.getObservaciones());
                                    equipo.setInformacionTecnica(equipoAux.getInformacionTecnica());
                                    equipo.setAdjuntos(equipoAux.getAdjuntos());
                                    equipo.setImagenes(equipoAux.getImagenes());
                                    equipo.setActividades(equipoAux.getActividades());
                                    equipo.setDatostecnicos(equipoAux.getDatostecnicos());
                                    equipo.setNfctoken(equipoAux.getNfctoken());
                                    equipo.setQrcode(equipoAux.getQrcode());
                                    equipo.setEstado(equipoAux.getEstado());
                                    equipo.setCliente(equipoAux.getCliente());

                                    Barcode barcode = new Barcode();
                                    barcode.setCodigo(equipoAux.getBarcode());
                                    RealmList<Barcode> barcodes = new RealmList<>();
                                    barcodes.add(barcode);
                                    equipo.setBarcode(barcodes);
                                }
                            }

                            if (equipo != null) {
                                equipo.setId(busqueda.getId());
                                equipo.setVariables(busqueda.getVariables());
                                equipo.setGmap(busqueda.getGmap());
                                equipo.setOrdenTrabajos(busqueda.getHistoricoOT());
                                equipo.setFallas(busqueda.getFallas());

                                viejo = true;
                                compositeDisposable.add(equipoServices.save(equipo)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(this::guardarBusquedaEquipo, Mantum::ignoreError, Functions.EMPTY_ACTION));
                            }

                            if (equipo != null && equipo.getInstalacionproceso() != null) {
                                informacionVisualExtra = equipo.getInstalacionproceso();
                            }

                            break;

                        case InstalacionLocativa.SELF:
                            InstalacionLocativa instalacionLocativa = busqueda.getDetalle(InstalacionLocativa.class);
                            if (instalacionLocativa != null) {
                                instalacionLocativa.setId(busqueda.getId());
                                instalacionLocativa.setVariables(busqueda.getVariables());
                                instalacionLocativa.setGmap(busqueda.getGmap());
                                instalacionLocativa.setOrdenTrabajos(busqueda.getHistoricoOT());
                                instalacionLocativa.setFallas(busqueda.getFallas());

                                viejo = true;
                                compositeDisposable.add(instalacionLocativaServices.save(instalacionLocativa)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(this::guardarBusquedaInstalacionLocativa, Mantum::ignoreError, Functions.EMPTY_ACTION));
                            }
                            break;

                        case OrdenTrabajo.SELF:
                            OrdenTrabajo detalle = busqueda.getDetalle(OrdenTrabajo.class);
                            if (detalle != null) {
                                StringBuilder entidadesRelacionadas = new StringBuilder();
                                for (Entidad entidad : detalle.getEntidades()) {
                                    entidadesRelacionadas.append(entidad.getCodigo()).append(" | ").append(entidad.getNombre()).append(", ");
                                }

                                entidadesRelacionadas = new StringBuilder(borrarUltimosDosCaracteres(entidadesRelacionadas.toString()));
                                busqueda.setEntidadesrelacionadas(entidadesRelacionadas.toString());

                                informacionVisualExtra = detalle.getFechainicio() + " - " + detalle.getFechafin();

                                viejo = true;
                                compositeDisposable.add(ordenTrabajoService.save(detalle)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(this::guardarBusquedaOrdenTrabajo, Mantum::ignoreError, Functions.EMPTY_ACTION));
                            }
                            break;

                        case RutaTrabajo.SELF:
                            viejo = true;
                            compositeDisposable.add(rutaTrabajoService.save(busqueda.getDetalle(RutaTrabajo.class))
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(this::guardarBusquedaRutaTrabajo, Mantum::ignoreError, Functions.EMPTY_ACTION));
                            break;

                        case SolicitudServicio.SELF:
                            viejo = true;
                            compositeDisposable.add(solicitudServicioService.save(busqueda.getDetalle(SolicitudServicio.class))
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(this::guardarSolicitudServicio, Mantum::ignoreError, Functions.EMPTY_ACTION));
                            break;
                    }

                    if (viejo) {
                        busqueda.setInformacionvisualextra(informacionVisualExtra);
                        alphabetAdapter.add(busqueda);
                        busquedaSinFiltros.add(busqueda);
                        continue;
                    }

                    Busqueda element = self.where(Busqueda.class)
                            .equalTo("id", busqueda.getId())
                            .equalTo("idejecucion", busqueda.getIdejecucion())
                            .equalTo("type", busqueda.getType())
                            .equalTo("cuenta.UUID", cuenta.getUUID())
                            .findFirst();

                    if (busqueda.getCode() != null && !busqueda.getCode().isEmpty()) {
                        busqueda.setCode(busqueda.getCode().trim());
                    }

                    if (busqueda.getName() != null && !busqueda.getName().isEmpty()) {
                        busqueda.setName(busqueda.getName().trim());
                    }

                    if (element == null) {
                        busqueda.generateUUID();
                        busqueda.setCuenta(cuenta);

                        RealmList<Accion> actions = new RealmList<>();
                        for (Accion accion : busqueda.getActions()) {
                            Accion current = self.where(Accion.class)
                                    .equalTo("name", accion.getName())
                                    .findFirst();
                            actions.add(current == null ? self.copyToRealm(accion) : current);
                        }

                        busqueda.setHistoricoOT(null); // NO SE REQUIERE GUARDAR EL HISTORICO
                        busqueda.setFallas(null); // NO SE REQUIERE GUARDAR EL LISTADO DE FALLAS

                        busqueda.setActions(actions);
                        self.insert(busqueda);
                    } else {
                        element.setCode(busqueda.getCode());
                        element.setName(busqueda.getName());
                        element.setDetalle(busqueda.getDetalle());
                        element.setGmap(busqueda.getGmap());
                        element.setMostrar(true);

                        if (element.getVariables() != null && !element.getVariables().isEmpty()) {
                            element.getVariables().deleteAllFromRealm();
                        }

                        RealmList<Variable> variables = new RealmList<>();
                        for (Variable variable : busqueda.getVariables()) {
                            variables.add(self.copyToRealm(variable));
                        }
                        element.setVariables(variables);

                        if (element.getActividades() != null && !element.getActividades().isEmpty()) {
                            element.getActividades().deleteAllFromRealm();
                        }

                        RealmList<Actividad> actividades = new RealmList<>();
                        for (Actividad actividad : busqueda.getActividades()) {
                            actividades.add(self.copyToRealm(actividad));
                        }
                        element.setActividades(actividades);

                        if (element.getData() != null && !element.getData().isEmpty()) {
                            element.getData().deleteAllFromRealm();
                        }

                        RealmList<DetalleBusqueda> detalle = new RealmList<>();
                        for (DetalleBusqueda detalleBusqueda : busqueda.getData()) {
                            detalle.add(self.copyToRealm(detalleBusqueda));
                        }
                        element.setData(detalle);

                        RealmList<Accion> actions = new RealmList<>();
                        for (Accion accion : busqueda.getActions()) {
                            Accion current = self.where(Accion.class)
                                    .equalTo("name", accion.getName())
                                    .findFirst();
                            actions.add(current == null ? self.copyToRealm(accion) : current);
                        }
                        element.setActions(actions);
                    }

                    alphabetAdapter.add(busqueda);
                    busquedaSinFiltros.add(busqueda);
                }
            } catch (Exception e) {
                Log.e(TAG, "onNext: ", e);
            }
        });

        database.close();
    }

    private void onComplete() {
        progressBar.setVisibility(View.GONE);
        filtrarBusqueda();
    }

    private void onError(@NonNull Throwable throwable) {
        String message = throwable.getMessage() != null ? throwable.getMessage() : getString(R.string.error_app);
        if (contentScan != null) {
            message += " código: " + contentScan;
        }

        progressBar.setVisibility(View.GONE);
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                .setDuration(7000)
                .show();
    }

    private CompoundButton.OnCheckedChangeListener customOnCheckedChangeListener(String type) {
        return (button, isChecked) -> {
            if (checksFiltroDesactivados()) {
                alphabetAdapter.addAll(busquedaSinFiltros);
                ordenarBusquedas();
                return;
            }

            if (isChecked) {
                for (Busqueda busqueda : busquedaSinFiltros) {
                    if (busqueda.getType().equals(type))
                        alphabetAdapter.add(busqueda);
                }
            }
            filtrarBusqueda();
        };
    }

    private void filtrarBusqueda() {
        if (!checksFiltroDesactivados()) {
            List<Busqueda> busquedas = new ArrayList<>(alphabetAdapter.getOriginal());
            for (Busqueda busqueda : busquedas) {
                switch (busqueda.getType()) {
                    case OrdenTrabajo.SELF:
                        if (!checkBusquedaOrdenTrabajo.isChecked()) {
                            alphabetAdapter.remove(busqueda);
                        }
                        break;

                    case RutaTrabajo.SELF:
                        if (!checkBusquedaRutaTrabajo.isChecked()) {
                            alphabetAdapter.remove(busqueda);
                        }
                        break;

                    case SolicitudServicio.SELF:
                        if (!checkBusquedaSolicitudServicio.isChecked()) {
                            alphabetAdapter.remove(busqueda);
                        }
                        break;

                    case Equipo.SELF:
                        if (!checkBusquedaEquipo.isChecked()) {
                            alphabetAdapter.remove(busqueda);
                        }
                        break;

                    case InstalacionLocativa.SELF:
                        if (!checkBusquedaInstalacionLocativa.isChecked()) {
                            alphabetAdapter.remove(busqueda);
                        }
                        break;

                    case InstalacionProceso.SELF:
                        if (!checkBusquedaInstalacionProceso.isChecked()) {
                            alphabetAdapter.remove(busqueda);
                        }
                        break;

                    case GRUPO_VARIABLE:
                        if (!checkBusquedaGrupoVariable.isChecked()) {
                            alphabetAdapter.remove(busqueda);
                        }
                        break;

                    case PERSONAL:
                        if (!checkBusquedaPesonal.isChecked()) {
                            alphabetAdapter.remove(busqueda);
                        }
                        break;

                    case PIEZA:
                        if (!checkBusquedaPieza.isChecked()) {
                            alphabetAdapter.remove(busqueda);
                        }
                        break;

                    case COMPONENTE:
                        if (!checkBusquedaComponente.isChecked()) {
                            alphabetAdapter.remove(busqueda);
                        }
                        break;

                    case RECURSO:
                        if (!checkBusquedaRecurso.isChecked()) {
                            alphabetAdapter.remove(busqueda);
                        }
                        break;

                    case Familia.SELF:
                        if (!checkBusquedaFamilia.isChecked()) {
                            alphabetAdapter.remove(busqueda);
                        }
                        break;

                    case Proveedor.SELF:
                        if (!checkBusquedaFabricante.isChecked()) {
                            alphabetAdapter.remove(busqueda);
                        }
                        break;

                    default:
                        alphabetAdapter.remove(busqueda);
                }
            }
        }
        ordenarBusquedas();
    }

    private boolean checksFiltroDesactivados() {
        return !checkBusquedaOrdenTrabajo.isChecked() && !checkBusquedaRutaTrabajo.isChecked() && !checkBusquedaSolicitudServicio.isChecked()
                && !checkBusquedaEquipo.isChecked() && !checkBusquedaInstalacionLocativa.isChecked() && !checkBusquedaInstalacionProceso.isChecked()
                && !checkBusquedaGrupoVariable.isChecked() && !checkBusquedaPesonal.isChecked() && !checkBusquedaPieza.isChecked()
                && !checkBusquedaComponente.isChecked() && !checkBusquedaRecurso.isChecked() && !checkBusquedaFamilia.isChecked()
                && !checkBusquedaFabricante.isChecked();
    }

    private void ordenarBusquedas() {
        Collections.sort(alphabetAdapter.getOriginal(), (o1, o2) -> {
            String code1 = o1.getCode() != null ? o1.getCode() : "";
            String code2 = o2.getCode() != null ? o2.getCode() : "";
            return code1.compareTo(code2);
        });

        Collections.sort(alphabetAdapter.getOriginal(), (o1, o2) -> {
            String type1 = o1.getType() != null ? o1.getType() : "";
            String type2 = o2.getType() != null ? o2.getType() : "";
            return type1.compareTo(type2);
        });

        alphabetAdapter.refresh();
        alphabetAdapter.showMessageEmpty(getView());
    }

    private String borrarUltimosDosCaracteres(String texto) {
        if (texto.length() > 0) {
            return texto.substring(0, texto.length() - 2);
        }

        return "";
    }

    private String getCheckedsEntitytypes() {
        StringBuilder resultado = new StringBuilder();
        if (checkBusquedaOrdenTrabajo.isChecked()) {
            resultado.append("OT,");
        }
        if (checkBusquedaSolicitudServicio.isChecked()) {
            resultado.append("SS,");
        }
        if (checkBusquedaRutaTrabajo.isChecked()) {
            resultado.append("RT,");
        }
        if (checkBusquedaEquipo.isChecked()) {
            resultado.append("Equipo,");
        }
        if (checkBusquedaInstalacionLocativa.isChecked()) {
            resultado.append("InstalacionLocativa,");
        }
        if (checkBusquedaInstalacionProceso.isChecked()) {
            resultado.append("InstalacionProceso,");
        }
        if (checkBusquedaGrupoVariable.isChecked()) {
            resultado.append("GrupoVariable,");
        }
        if (checkBusquedaPesonal.isChecked()) {
            resultado.append("Personal,");
        }
        if (checkBusquedaPieza.isChecked()) {
            resultado.append("Pieza,");
        }
        if (checkBusquedaComponente.isChecked()) {
            resultado.append("Componente,");
        }
        if (checkBusquedaRecurso.isChecked()) {
            resultado.append("Recurso,");
        }
        if (checkBusquedaFamilia.isChecked()) {
            resultado.append("Familia,");
        }
        if (checkBusquedaFabricante.isChecked()) {
            resultado.append("Fabricante,");
        }
        if (resultado.length() > 0) {
            resultado.deleteCharAt(resultado.length() - 1);
        }
        String entitytypes = resultado.toString();

        return entitytypes;
    }
}