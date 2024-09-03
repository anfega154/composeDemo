package com.mantum.cmms.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;

import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.QRgenerico;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Almacen;
import com.mantum.cmms.entity.Bodega;
import com.mantum.cmms.entity.Busqueda;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.TipoMovimiento;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.cmms.service.AlmacenService;
import com.mantum.cmms.service.CaptureGeolocationService;
import com.mantum.cmms.service.MovimientoService;
import com.mantum.cmms.service.TipoMovimientoService;
import com.mantum.component.Mantum;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.AlmacenAdapter;
import com.mantum.component.adapter.SpinnerMultipleAdapter;
import com.mantum.core.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Case;

import static com.mantum.component.Mantum.isConnectedOrConnecting;

public class AlmacenActivity extends Mantum.Activity implements ActionMode.Callback {

    private static final String TAG = AlmacenActivity.class.getSimpleName();
    public static final String ITEMS_TRASLADO = "ITEMS_TRASLADO";
    public static final String BODEGA_ORIGEN = "BODEGA_ORIGEN";

    private Database database;
    private ProgressBar progressBar;
    private AlmacenService almacenService;
    private MovimientoService movimientoService;
    private TipoMovimientoService tipoMovimientoService;
    private AlmacenAdapter<Almacen> alphabetAdapter;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private boolean isMultiSelect = false;
    private List<Integer> selectedIds = new ArrayList();
    private List<String> listTipoEntrada = new ArrayList<>();
    private List<String> listTipoSalida = new ArrayList<>();
    private ArrayList<String> bodegasList;
    private ActionMode actionMode;
    private SpinnerMultipleAdapter spinnerMultipleAdapter;
    private AlertDialog.Builder changeUserQR;
    private AlertDialog.Builder showDialogQR;
    private Spinner spinnerView;
    private final Gson gson = new Gson();
    private Cuenta cuenta;
    private List<Bodega> bodegas;
    private Long idalmacenprimario;
    private Boolean selection = false;
    public String tipoElemento = "articulos";
    public boolean accionMovimientoAlmacen = false, mostrarFloating = true;
    public String movimiento = null;
    private String almacen = null;
    private Location location;
    private CaptureGeolocationService captureGeolocationService;
    private final int TIME_ALERT = 7000;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_almacen);

            database = new Database(this);
            progressBar = findViewById(R.id.progressBar);
            includeBackButtonAndTitle(R.string.almacen_title);

            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null)
                throw new Exception(getString(R.string.error_authentication));

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                selection = bundle.getBoolean(MovimientoActivity.ACTION2);
                tipoElemento = bundle.getString("tipoElemento");
                accionMovimientoAlmacen = bundle.getBoolean("accionMovimientoAlmacen");
                movimiento = bundle.getString("movimiento");
                almacen = bundle.getString("almacen");
            }

            almacenService = new AlmacenService(this, cuenta);
            movimientoService = new MovimientoService(this, cuenta);
            tipoMovimientoService = new TipoMovimientoService(this, cuenta);
            changeUserQR = new AlertDialog.Builder(this);
            showDialogQR = new AlertDialog.Builder(this);
            alphabetAdapter = new AlmacenAdapter<>(this);
            alphabetAdapter.setbMultiple(true);

            captureGeolocationService = new CaptureGeolocationService(this);
            spinnerMultipleAdapter = findViewById(R.id.mSpinner);
            spinnerMultipleAdapter.setMovimientoAlmacen(true);

            isEmptyList();

            alphabetAdapter.setOnAction(new OnSelected<Almacen>() {
                @Override
                public void onClick(Almacen value, int position) {
                    if (isMultiSelect)
                        multiSelect(position);
                }

                @Override
                public boolean onLongClick(Almacen value, int position) {

                    if (!isMultiSelect) {
                        selectedIds = new ArrayList();
                        isMultiSelect = true;

                        if (actionMode == null)
                            actionMode = startActionMode(AlmacenActivity.this);
                    }
                    multiSelect(position);
                    return true;
                }
            });

            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            RecyclerView recyclerView = findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemViewCacheSize(20);
            recyclerView.setDrawingCacheEnabled(true);
            recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(alphabetAdapter);

            requestTipoMovimiento();

            FloatingActionButton crearEntrada = findViewById(R.id.crearEntrada);
            FloatingActionButton crearSalida = findViewById(R.id.crearSalida);

            if (UserPermission.check(this, UserPermission.REALIZAR_MOVIMIENTO_GENERAL, false)) {
                crearEntrada.setVisibility(View.VISIBLE);
                crearSalida.setVisibility(View.VISIBLE);
            }

            crearEntrada.setOnClickListener(v -> {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listTipoEntrada);

                Spinner spinner = new Spinner(this);
                spinner.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                spinner.setAdapter(arrayAdapter);

                alertDialogBuilder.setCancelable(true);
                alertDialogBuilder.setNegativeButton("Cerrar", (dialog, id) -> dialog.cancel());
                alertDialogBuilder.setPositiveButton("Continuar", (dialog, id) -> {
                    Intent intent = new Intent(this, MovimientoAlmacenActivity.class);
                    intent.putExtra("tipoMovimiento", spinner.getSelectedItem().toString());
                    intent.putExtra("movimiento", "entrada");
                    startActivity(intent);
                });
                alertDialogBuilder.setView(spinner);
                alertDialogBuilder.setMessage("Seleccione el tipo de entrada");
                alertDialogBuilder.show();
            });

            crearSalida.setOnClickListener(v -> {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listTipoSalida);

                Spinner spinner = new Spinner(this);
                spinner.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                spinner.setAdapter(arrayAdapter);

                alertDialogBuilder.setCancelable(true);
                alertDialogBuilder.setNegativeButton("Cerrar", (dialog, id) -> dialog.cancel());
                alertDialogBuilder.setPositiveButton("Continuar", (dialog, id) -> {
                    Intent intent = new Intent(this, MovimientoAlmacenActivity.class);
                    intent.putExtra("tipoMovimiento", spinner.getSelectedItem().toString());
                    intent.putExtra("movimiento", "salida");
                    startActivity(intent);
                });
                alertDialogBuilder.setView(spinner);
                alertDialogBuilder.setMessage("Seleccione el tipo de salida");
                alertDialogBuilder.show();
            });

            if (UserPermission.check(this, UserPermission.TRANSFERIR_ITEMS, false)) {
                FloatingActionButton transferir = findViewById(R.id.transferir);
                transferir.setVisibility(View.VISIBLE);
                transferir.setOnClickListener(v -> {
                    if (!spinnerMultipleAdapter.getSelectedItemsAsString().isEmpty()) {
                        List<Bodega> bodegas = database.where(Bodega.class)
                                .findAll();

                        String spinnerBodega = spinnerMultipleAdapter.getSelectedItemsAsString();
                        String codigoBodega = null;

                        for (Bodega bodegaReplace : bodegas) {
                            if (spinnerBodega.equals(bodegaReplace.getCodigo() + " | " + bodegaReplace.getNombre())) {
                                codigoBodega = spinnerBodega.replace(" | " + bodegaReplace.getNombre(), "");
                                break;
                            }
                        }

                        Bodega bodega = database.where(Bodega.class)
                                .equalTo("codigo", codigoBodega)
                                .findFirst();

                        Intent intent = new Intent(this, TrasladoAlmacenActivity.class);
                        intent.putExtra(BODEGA_ORIGEN, bodega.getId());
                        startActivity(intent);
                    } else {
                        Snackbar.make(getView(), R.string.almacen_busqueda_vacio, Snackbar.LENGTH_SHORT).show();
                    }
                });
            }

            if (UserPermission.check(this, UserPermission.RECIBIR_ALMACEN, false)) {
                FloatingActionButton recibir = findViewById(R.id.receive_store);
                recibir.setVisibility(View.VISIBLE);
                recibir.setOnClickListener(v -> {
                    IntentIntegrator integrator = new IntentIntegrator(this);
                    integrator.setOrientationLocked(true);
                    integrator.setCameraId(0);
                    integrator.setPrompt(getString(R.string.mensaje_ayuda_barcode));
                    integrator.setCaptureActivity(CaptureActivityPortrait.class);
                    integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                    integrator.setBeepEnabled(false);
                    integrator.initiateScan();
                });
            }

            if (UserPermission.check(this, UserPermission.ENTREGAR_ALMACEN, false)) {
                FloatingActionButton entregar = findViewById(R.id.send_store);
                entregar.setVisibility(View.VISIBLE);
                entregar.setOnClickListener(v -> {

                    if (bodegasList.size() == 0) {
                        Snackbar.make(getView(), R.string.empty_almacenes, Snackbar.LENGTH_LONG)
                                .show();
                        return;
                    }

                    LayoutInflater factory = LayoutInflater.from(this);
                    final View view = factory.inflate(R.layout.dialog_select, null);

                    spinnerView = view.findViewById(R.id.spinnerStore);
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, bodegasList);
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerView.setAdapter(arrayAdapter);

                    changeUserQR.setPositiveButton(R.string.accept, (dialog, id) -> {
                        String nombre_bodega = (String) spinnerView.getSelectedItem();
                        if (!nombre_bodega.equals("")) {
                            List<Bodega> bodegas = database.where(Bodega.class)
                                    .findAll();

                            String codigoBodega = null;

                            for (Bodega bodegaReplace : bodegas) {
                                if (nombre_bodega.equals(bodegaReplace.getCodigo() + " | " + bodegaReplace.getNombre())) {
                                    codigoBodega = nombre_bodega.replace(" | " + bodegaReplace.getNombre(), "");
                                    break;
                                }
                            }

                            Bodega bodega = database.where(Bodega.class)
                                    .equalTo("codigo", codigoBodega)
                                    .findFirst();

                            requestQR(bodega.getId());
                        }
                    });
                    changeUserQR.setNegativeButton(R.string.close, null);
                    changeUserQR.setView(view);
                    changeUserQR.setTitle(R.string.entregar_almacen);
                    changeUserQR.show();
                });
            }

            if (selection || accionMovimientoAlmacen ||
                    (!UserPermission.check(this, UserPermission.TRANSFERIR_ITEMS, false) &&
                            !UserPermission.check(this, UserPermission.RECIBIR_ALMACEN, false) &&
                            !UserPermission.check(this, UserPermission.ENTREGAR_ALMACEN, false))) {
                getView().findViewById(R.id.floating).setVisibility(View.GONE);
                mostrarFloating = false;
            }

            request();
        } catch (Exception e) {
            backActivity(getString(R.string.error_app));
        }
    }

    @SuppressLint("RestrictedApi")
    private void multiSelect(int position) {
        Almacen data = alphabetAdapter.getItemPosition(position);

        if (data != null) {
            if (idalmacenprimario == null)
                idalmacenprimario = data.getIdbodega();

            if (alphabetAdapter != null) {
                if (movimiento == null || !movimiento.equals("entrada")) {
                    if (data.getCantidad() <= 0) {
                        Snackbar.make(getView(), R.string.cantidad_elementos_valida, Snackbar.LENGTH_LONG)
                                .show();
                        return;
                    }
                }

                showFilter(true);

                FloatingActionButton cameraQR = findViewById(R.id.cameraQR);
                cameraQR.setVisibility(View.GONE);

                if (selectedIds.contains(position))
                    selectedIds.remove(Integer.valueOf(position));
                else
                    selectedIds.add(position);

                if (selectedIds.size() > 0)
                    actionMode.setTitle(selectedIds.size() + " Seleccionados");
                else
                    actionMode.finish();

                alphabetAdapter.setSelectedIds(selectedIds);
            }
        }
    }

    private void showFilter(@Nullable boolean ocultar) {
        CardView container_filter = findViewById(R.id.container_filter);
        if (ocultar || container_filter.getVisibility() != View.GONE)
            container_filter.setVisibility(View.GONE);
        else
            container_filter.setVisibility(View.VISIBLE);
    }

    private void showQR(String qr_change) {
        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.dialog_image, null);

        Bitmap bitmap = Mantum.convertToBase64(qr_change);
        ImageView image = view.findViewById(R.id.dialog_imageview);
        image.setImageBitmap(bitmap);

        showDialogQR.setNegativeButton(R.string.close, null);
        showDialogQR.setView(view);
        showDialogQR.setCancelable(false);
        showDialogQR.setTitle(R.string.title_entregar_almacen);
        showDialogQR.setIcon(R.drawable.nfc);
        showDialogQR.show();
    }

    public void searchItems(View view) {
        closeKeyboard();
        search();
    }

    public void search() {
        try {
            EditText criterioEditText = findViewById(R.id.code_input);
            String criterio = criterioEditText.getText().toString();

            alphabetAdapter.clear();

            List<Bodega> bodegas = database.where(Bodega.class)
                    .findAll();

            String spinnerBodega = spinnerMultipleAdapter.getSelectedItemsAsString();
            String codigoBodega = null;

            for (Bodega bodegaReplace : bodegas) {
                if (spinnerBodega.equals(bodegaReplace.getCodigo() + " | " + bodegaReplace.getNombre())) {
                    codigoBodega = spinnerBodega.replace(" | " + bodegaReplace.getNombre(), "");
                    break;
                }
            }

            Bodega bodega = database.where(Bodega.class)
                    .equalTo("codigo", codigoBodega)
                    .findFirst();

            if (!spinnerMultipleAdapter.getSelectedItemsAsString().isEmpty()) {
                if (!criterio.equals("")) {
                    if (bodega != null)
                        requestElemento(criterio, bodega.getId());
                } else {
                    Snackbar.make(getView(), R.string.criterio_busqueda_vacio, Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(getView(), R.string.almacen_busqueda_vacio, Snackbar.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "search: ", e);
        }
    }

    private void searchQR(String criterio) {
        try {
            alphabetAdapter.clear();

            List<Bodega> bodegas = database.where(Bodega.class)
                    .findAll();

            String spinnerBodega = spinnerMultipleAdapter.getSelectedItemsAsString();
            String codigoBodega = null;

            for (Bodega bodegaReplace : bodegas) {
                if (spinnerBodega.equals(bodegaReplace.getCodigo() + " | " + bodegaReplace.getNombre())) {
                    codigoBodega = spinnerBodega.replace(" | " + bodegaReplace.getNombre(), "");
                    break;
                }
            }

            Bodega bodega = database.where(Bodega.class)
                    .equalTo("codigo", codigoBodega)
                    .findFirst();

            if (bodega != null)
                requestElementoQR(criterio, bodega.getId());
        } catch (Exception e) {
            Log.e(TAG, "searchQR: ", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_almacen, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_refresh:
                if (!isConnectedOrConnecting(getApplicationContext())) {
                    Snackbar.make(getView(), R.string.offline, Snackbar.LENGTH_LONG)
                            .show();
                    return true;
                }

                request();
                break;
            case R.id.action_filter:
                showFilter(false);
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

        listTipoEntrada.clear();
        listTipoSalida.clear();
        alphabetAdapter.clear();
        compositeDisposable.clear();
        almacenService.cancel();
        tipoMovimientoService.cancel();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    private void isEmptyList() {
        RelativeLayout container = findViewById(R.id.empty);
        container.setVisibility(alphabetAdapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void onNext(Response response) {
        alphabetAdapter.clear();

        if (!response.isValid())
            return;

        try {
            if (cuenta == null)
                return;

            bodegas = response.getBody(Bodega.Request.class).getBodegas();

            database.executeTransaction(self -> {

                database.where(Bodega.class)
                        .findAll()
                        .deleteAllFromRealm();

                for (Bodega bodega : bodegas)
                    bodega.setCuenta(cuenta);

                self.insertOrUpdate(bodegas);

                bodegasList = new ArrayList<>();

                for (Bodega bodega : bodegas) {
                    bodegasList.add(bodega.getCodigo() + " | " + bodega.getNombre());
                }

                if (bodegas.size() > 0) {
                    spinnerMultipleAdapter.setItems(bodegasList);
                    spinnerMultipleAdapter.setSelection(0);
                } else {
                    Snackbar.make(getView(), R.string.sin_almacenes_asociados, Snackbar.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "onNext: " + e);
        }
    }

    private void onNextElemento(Response response, Long bodega) {
        Log.e(TAG, "onNextElemento: " + response);
        if (!response.isValid())
            return;

        try {
            database.executeTransaction(self -> {
                database.where(Almacen.class)
                        .findAll()
                        .deleteAllFromRealm();

                List<Almacen> almacenes = response.getBody(Almacen.Request.class)
                        .getRecursos();

                if (tipoElemento.equals("activos")) {
                    almacenes = response.getBody(Almacen.Request.class).getElementos();
                }

                for (Almacen almacen : almacenes) {
                    almacen.setCuenta(cuenta);
                }

                self.insertOrUpdate(almacenes);
            });
        } catch (Exception e) {
            Log.d(TAG, "onNextElemento: " + e);
        }
    }

    private void onNextElementoQR(Response response, Long bodega, String qrcode) {
        if (!response.isValid())
            return;

        try {
            database.executeTransaction(self -> {
                database.where(Almacen.class)
                        .findAll()
                        .deleteAllFromRealm();

                List<Almacen> almacenes = response.getBody(Almacen.Request.class).getRecursos();

                for (Almacen almacen : almacenes) {
                    almacen.setIdbodega(bodega);
                    almacen.setQrcode(qrcode);
                }

                self.insertOrUpdate(almacenes);
            });
        } catch (Exception e) {
            Log.d(TAG, "onNextElemento: " + e);
        }
    }

    private void onNextQR(Response response) {
        if (!response.isValid())
            return;

        String qr_change = response.getBody(Almacen.Request.class).getImageqr();
        if (qr_change != null)
            showQR(qr_change);
    }

    private void onNextNewStorer(Response response) {
        if (response.isValid()) {
            Snackbar.make(getView(), R.string.ok_almacen_asociado, Snackbar.LENGTH_LONG)
                    .setDuration(TIME_ALERT)
                    .show();
            request();
        }
    }

    public void onNextTipoMovimiento(Response response) {
        try {
            database.executeTransaction(self -> {
                database.where(TipoMovimiento.class)
                        .findAll()
                        .deleteAllFromRealm();

                List<TipoMovimiento> tipoMovimientos = response.getBody(TipoMovimiento.Request.class).getTipoMovimientos();
                List<TipoMovimiento> tipoMovimientosAux = new ArrayList<>(tipoMovimientos);
                self.insertOrUpdate(tipoMovimientosAux);
            });
        } catch (Exception e) {
            Log.d(TAG, "onNext: " + e);
        }
    }

    private void onError(Throwable throwable) {
        if (!accionMovimientoAlmacen && mostrarFloating) {
            FloatingActionsMenu floatingActionButton = findViewById(R.id.floating);
            floatingActionButton.setVisibility(View.VISIBLE);
        }

        progressBar.setVisibility(View.GONE);
        if (throwable != null) {
            Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private void asignLocation(Location location) {
        this.location = location;
    }

    @SuppressLint("RestrictedApi")
    private void onComplete() {
        progressBar.setVisibility(View.GONE);
        alphabetAdapter.sort();
        alphabetAdapter.refresh();

        FloatingActionsMenu floatingActionButton = findViewById(R.id.floating);

        if (accionMovimientoAlmacen) {
            floatingActionButton.setVisibility(View.GONE);

            if (!tipoElemento.equals("activos")) {
                FloatingActionButton cameraQR = findViewById(R.id.cameraQR);
                cameraQR.setVisibility(View.VISIBLE);
                cameraQR.setOnClickListener(v -> {
                    if (!spinnerMultipleAdapter.getSelectedItemsAsString().isEmpty()) {
                        IntentIntegrator integrator = new IntentIntegrator(this);
                        integrator.setOrientationLocked(true);
                        integrator.setCameraId(0);
                        integrator.setPrompt(getString(R.string.mensaje_ayuda_barcode));
                        integrator.setCaptureActivity(CaptureActivityPortrait.class);
                        integrator.setBeepEnabled(false);
                        integrator.initiateScan();
                    } else {
                        Snackbar.make(getView(), R.string.almacen_busqueda_vacio, Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            if (mostrarFloating)
                floatingActionButton.setVisibility(View.VISIBLE);
        }
        isEmptyList();

        if (!alphabetAdapter.isEmpty())
            search();

        if (movimiento != null) {
            SwitchCompat switchCantidad = findViewById(R.id.check_cantidad);

            if (movimiento.equals("entrada"))
                switchCantidad.setChecked(false);
        }

        CardView container_filter = findViewById(R.id.container_filter);
        container_filter.setVisibility(View.VISIBLE);

        if (almacen != null) {
            almacen = almacen.replace("Almacén: ", "");
            spinnerMultipleAdapter.setSelection(Collections.singletonList(almacen));
            spinnerMultipleAdapter.setEnabled(false);
        }
    }

    private void request() {

        FloatingActionsMenu floatingActionButton = findViewById(R.id.floating);
        floatingActionButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        compositeDisposable.add(almacenService.getBodegas()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNext, this::onError, this::onComplete));
    }

    private void requestElemento(String criterio, Long bodega) {
        progressBar.setVisibility(View.VISIBLE);
        if (!tipoElemento.equals("activos")) {
            compositeDisposable.add(almacenService.getElemento(criterio, bodega)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> onNextElemento(response, bodega), this::onError, () -> {

                        SwitchCompat switchCantidad = findViewById(R.id.check_cantidad);
                        List<Almacen> almacenes;
                        almacenes = database.where(Almacen.class)
                                .beginGroup()
                                .contains("codigo", criterio, Case.INSENSITIVE).or()
                                .contains("nombre", criterio, Case.INSENSITIVE)
                                .endGroup()
                                .equalTo("idbodega", bodega)
                                .findAll();

                        if (tipoElemento.equals("recursos")) {
                            almacenes = database.where(Almacen.class)
                                    .beginGroup()
                                    .contains("codigo", criterio, Case.INSENSITIVE).or()
                                    .contains("nombre", criterio, Case.INSENSITIVE)
                                    .endGroup()
                                    .equalTo("activo", false)
                                    .equalTo("idbodega", bodega)
                                    .findAll();
                        }

                        if (!almacenes.isEmpty()) {
                            for (Almacen almacen : almacenes) {
                                if (switchCantidad.isChecked()) {
                                    if (almacen.getCantidad() > 0)
                                        alphabetAdapter.add(almacen);
                                } else {
                                    alphabetAdapter.add(almacen);
                                }
                            }
                        } else {
                            Snackbar.make(getView(), getString(R.string.message_search_empty), Snackbar.LENGTH_SHORT).show();
                        }

                        progressBar.setVisibility(View.GONE);
                        alphabetAdapter.sort();
                        alphabetAdapter.refresh();
                        isEmptyList();

                        if (switchCantidad.isChecked() && alphabetAdapter.isEmpty())
                            Snackbar.make(getView(), getString(R.string.cantidad_multiple_elementos_valida), Snackbar.LENGTH_SHORT).show();
                    })
            );
        } else {
            compositeDisposable.add(movimientoService.getEquipoPorCriterio(criterio, true)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> onNextElemento(response, bodega), this::onError, () -> {
                        SwitchCompat switchCantidad = findViewById(R.id.check_cantidad);
                        List<Almacen> almacenes;

                        almacenes = database.where(Almacen.class)
                                .beginGroup()
                                .contains("codigo", criterio, Case.INSENSITIVE).or()
                                .contains("nombre", criterio, Case.INSENSITIVE)
                                .endGroup()
                                .equalTo("idbodega", bodega)
                                .findAll();

                        if (!almacenes.isEmpty()) {
                            for (Almacen almacen : almacenes) {
                                if (switchCantidad.isChecked()) {
                                    if (almacen.getCantidad() > 0)
                                        alphabetAdapter.add(almacen);
                                } else {
                                    alphabetAdapter.add(almacen);
                                }
                            }
                        } else {
                            Snackbar.make(getView(), getString(R.string.message_search_empty), Snackbar.LENGTH_SHORT).show();
                        }

                        progressBar.setVisibility(View.GONE);
                        alphabetAdapter.sort();
                        alphabetAdapter.refresh();
                        isEmptyList();

                        if (switchCantidad.isChecked() && alphabetAdapter.isEmpty())
                            Snackbar.make(getView(), getString(R.string.cantidad_multiple_elementos_valida), Snackbar.LENGTH_SHORT).show();
                    })
            );
        }
    }

    private void requestElementoQR(String criterio, Long bodega) {
        progressBar.setVisibility(View.VISIBLE);
        compositeDisposable.add(almacenService.getElementoQR(criterio, bodega)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> onNextElementoQR(response, bodega, criterio), this::onError, () -> {

                    SwitchCompat switchCantidad = findViewById(R.id.check_cantidad);
                    Almacen almacen;

                    almacen = database.where(Almacen.class)
                            .equalTo("qrcode", criterio)
                            .equalTo("idbodega", bodega)
                            .findFirst();

                    if (tipoElemento.equals("recursos")) {
                        almacen = database.where(Almacen.class)
                                .equalTo("qrcode", criterio)
                                .equalTo("activo", false)
                                .equalTo("idbodega", bodega)
                                .findFirst();
                    }

                    if (almacen != null) {
                        if (switchCantidad.isChecked()) {
                            if (almacen.getCantidad() > 0)
                                alphabetAdapter.add(almacen);
                        } else {
                            alphabetAdapter.add(almacen);
                        }
                    } else {
                        Snackbar.make(getView(), getString(R.string.item_not_in_store_1) + " \"" + criterio + "\" " + getString(R.string.item_not_in_store_2), Snackbar.LENGTH_LONG).show();
                    }

                    progressBar.setVisibility(View.GONE);
                    alphabetAdapter.sort();
                    alphabetAdapter.refresh();
                    isEmptyList();
                })
        );
    }

    private void requestQR(Long idbodega) {
        progressBar.setVisibility(View.VISIBLE);
        compositeDisposable.add(almacenService.getQRChange(idbodega)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNextQR, this::onError, this::onComplete));
    }

    private void requestTipoMovimiento() {
        compositeDisposable.add(tipoMovimientoService.get()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNextTipoMovimiento, this::onError, () -> {
                    List<TipoMovimiento> tipoMovimientos = database.where(TipoMovimiento.class)
                            .findAll();

                    for (TipoMovimiento tipoMovimiento : tipoMovimientos) {
                        switch (tipoMovimiento.getMovimientovalido()) {
                            case "Entrada":
                                listTipoEntrada.add(tipoMovimiento.getNombre());
                                break;
                            case "Salida":
                                listTipoSalida.add(tipoMovimiento.getNombre());
                                break;
                            case "Ambos":
                                listTipoEntrada.add(tipoMovimiento.getNombre());
                                listTipoSalida.add(tipoMovimiento.getNombre());
                                break;
                            default:
                                break;
                        }
                    }
                })
        );
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(selection ? R.menu.menu_devolucion : R.menu.menu_repeat, menu);
        FloatingActionsMenu floatingActionButton = findViewById(R.id.floating);
        floatingActionButton.setVisibility(View.GONE);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        try {
            ArrayList<Long> items = new ArrayList();
            for (Integer data : selectedIds) {
                Almacen almacen = alphabetAdapter.getItemPosition(data);
                if (selectedIds.contains(data))
                    if (almacen != null)
                        items.add(almacen.getId());
            }

            long[] longArray = new long[items.size()];
            for (int i = 0; i < items.size(); i++)
                longArray[i] = items.get(i);

            Bundle bundle = new Bundle();
            bundle.putLongArray(ITEMS_TRASLADO, longArray);
            bundle.putLong(BODEGA_ORIGEN, idalmacenprimario);

            Intent intent;
            intent = new Intent(this, TrasladoAlmacenActivity.class);
            intent.putExtras(bundle);

            switch (item.getItemId()) {
                case R.id.action_transfer:
                    startActivityForResult(intent, TrasladoAlmacenActivity.RESULT_OK_BACK);
                    break;
                case R.id.agregar:
                    intent.putExtra("almacenMovimiento", spinnerMultipleAdapter.getSelectedItemsAsString());
                    setResult(1, intent);
                    finish();
                    break;
            }
        } catch (Exception e) {
            Log.d(TAG, "onActionItemClicked: " + e);
        }

        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        onDestroyActionMode();
    }

    @SuppressLint("RestrictedApi")
    public void onDestroyActionMode() {
        actionMode = null;
        isMultiSelect = false;
        selectedIds = new ArrayList();
        idalmacenprimario = null;
        alphabetAdapter.setSelectedIds(new ArrayList());
        FloatingActionsMenu floatingActionButton = findViewById(R.id.floating);
        FloatingActionButton cameraQR = findViewById(R.id.cameraQR);
        if (!selection) {
            if (accionMovimientoAlmacen) {
                cameraQR.setVisibility(View.VISIBLE);
            } else {
                if (mostrarFloating)
                    floatingActionButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void asignNewStorer(Integer idbodega, Integer idalmacenista, Long idnewstorer, String expiracion) {
        progressBar.setVisibility(View.VISIBLE);
        compositeDisposable.add(almacenService.setNewStorer(idbodega, idalmacenista, idnewstorer, expiracion, this.location)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNextNewStorer, this::onError, this::onComplete));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (!Assert.isNull(data)) {
            if (resultCode == TrasladoAlmacenActivity.RESULT_OK_BACK) {
                long[] longaArray = data.getExtras().getLongArray(ITEMS_TRASLADO);
                if (longaArray != null && longaArray.length > 0 && actionMode != null) {
                    int allItems = alphabetAdapter.getItemCount();
                    for (long l : longaArray) {
                        for (int e = 0; e < allItems; e++) {
                            Almacen itemList = alphabetAdapter.getOriginal().get(e);
                            if (l == itemList.getId()) {
                                selectedIds.add(e);
                                alphabetAdapter.setSelectedIds(selectedIds);
                            }
                        }
                    }
                    actionMode.setTitle(selectedIds.size() + " Seleccionados");
                }
            } else if (resultCode == TrasladoAlmacenActivity.RESULT_OK_SAVE) {
                if (actionMode != null)
                    actionMode.finish();

                Snackbar.make(getView(), R.string.traslado_ok, Snackbar.LENGTH_LONG).show();
                onDestroyActionMode();
            } else {
                String contents = result.getContents();
                if (contents != null) {
                    if (TrasladoAlmacenActivity.QR_CODE.equals(result.getFormatName())) {
                        try {
                            if (accionMovimientoAlmacen && !tipoElemento.equals("activos")) {
                                Log.d(TAG, "onActivityResult: " + contents);
                                if (result.getContents().startsWith("{")) {
                                    Busqueda.Read read = gson.fromJson(contents, Busqueda.Read.class);
                                    if (read.getEntityCode() == null) {
                                        Snackbar.make(getView(), getString(R.string.message_search_empty), Snackbar.LENGTH_LONG)
                                                .setDuration(TIME_ALERT)
                                                .show();
                                        return;
                                    }
                                    searchQR(read.getEntityCode());
                                } else {
                                    searchQR(contents);
                                }
                            } else {
                                QRgenerico qr = gson.fromJson(result.getContents(), QRgenerico.class);
                                compositeDisposable.add(captureGeolocationService.obtener(false)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(this::asignLocation, this::onError, () -> {
                                            asignNewStorer(qr.getIdbodega(), qr.getUsuario(), cuenta.getId(), qr.getExpiracion());
                                        })
                                );
                            }
                        } catch (Exception e) {
                            super.onActivityResult(requestCode, resultCode, data);
                        }
                    }
                }
            }
        }
    }
}
