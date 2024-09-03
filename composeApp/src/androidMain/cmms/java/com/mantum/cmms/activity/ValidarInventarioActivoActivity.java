package com.mantum.cmms.activity;

import static com.mantum.component.Mantum.isConnectedOrConnecting;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.ResultEquipoQr;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.domain.RequestValidarEquipo;
import com.mantum.cmms.entity.busquedahelper.BusquedaQrConPropiedadEntityId;
import com.mantum.cmms.entity.busquedahelper.BusquedaQrConPropiedadId;
import com.mantum.cmms.entity.busquedahelper.BusquedaQrConPropiedadVersion;
import com.mantum.cmms.entity.parameter.UserParameter;
import com.mantum.cmms.fragment.ValidarEquipoCodeFragment;
import com.mantum.cmms.fragment.ValidarEquipoCodeQrFragment;
import com.mantum.cmms.service.InventarioActivosService;
import com.mantum.cmms.service.TransaccionService;
import com.mantum.component.Mantum;

import java.util.Calendar;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.internal.functions.Functions;

public class ValidarInventarioActivoActivity extends Mantum.Activity {
    private TextView codigoInventario;
    private TextView nombreInventario;
    private ProgressBar progress;
    private Cuenta cuenta;
    private Database database;
    protected final Gson gson = new GsonBuilder().create();
    private InventarioActivosService inventarioActivosService;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Long idValidation;
    private Long idCuenta;
    private FloatingActionsMenu validarEquipoMenu;
    private String  jsonRequest="";
    private final String MANUAL_METHOD = "Manual";
    private final String QR_CODE_METHOD = "Código QR";
    private final String URL_VALIDAR_EQUIPO = "api/v1/validation/assets/validation/equipments";
    private String nomEquipo= "";
    private String codEquipo="";

    private String urlMantumLaravel = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_validar_inventario_activo);

            database = new Database(ValidarInventarioActivoActivity.this);
            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();
            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }
            inventarioActivosService = new InventarioActivosService(this, cuenta);

            codigoInventario = findViewById(R.id.codigo_inventario);
            nombreInventario = findViewById(R.id.nombre_inventario);
            progress = findViewById(R.id.progressBarValidation);

            codigoInventario.setText(getIntent().getStringExtra("code"));
            nombreInventario.setText(getIntent().getStringExtra("name"));
            idValidation = (long) getIntent().getIntExtra("id", 0);
            idCuenta = (long) cuenta.getId();

            Toolbar toolbar = findViewById(R.id.toolb_val_inventario);
            setSupportActionBar(toolbar);
            includeBackButton();

            final Drawable upArrow = getResources().getDrawable(R.drawable.arrow_back);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setHomeAsUpIndicator(upArrow);
            }

            validarEquipoMenu = findViewById(R.id.validar_equipo_menu);

            loadFragment(new ValidarEquipoCodeQrFragment());

            FloatingActionButton validarByCode = findViewById(R.id.validar_by_code);
            validarByCode.setOnClickListener(V -> {
                loadFragment(new ValidarEquipoCodeFragment());
                validarEquipoMenu.toggle();

            });

            FloatingActionButton validarByQr = findViewById(R.id.validar_by_qr);
            validarByQr.setOnClickListener(v -> {
                loadFragment(new ValidarEquipoCodeQrFragment());
                validarEquipoMenu.toggle();
            });
        }catch (Exception e){

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == CertificadoActivity.REQUEST_CODE) {
                progress.setVisibility(View.VISIBLE);
                requestCode = IntentIntegrator.REQUEST_CODE;
            }

            IntentResult result = IntentIntegrator.parseActivityResult(
                    requestCode, resultCode, data);

            if (result != null) {
                if (result.getContents() == null) {
                    progress.setVisibility(View.GONE);
                    return;
                }else{
                    String resultJsonData = result.getContents();
                    if (resultJsonData.startsWith("{")) {
                        ResultEquipoQr equipo = gson.fromJson(resultJsonData, ResultEquipoQr.class);
                        this.setJsonRequest(equipo.getEntitycode(), QR_CODE_METHOD, false);
                    } else {
                        this.setJsonRequest(resultJsonData, QR_CODE_METHOD, true);
                    }
                    this.transacciones(jsonRequest);
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
                progress.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void scanQrCode(){
        if (progress.getVisibility() == View.VISIBLE) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 3);
        } else {
            progress.setVisibility(View.VISIBLE);

            IntentIntegrator integrator = new IntentIntegrator(ValidarInventarioActivoActivity.this);
            integrator.setOrientationLocked(true);
            integrator.setCameraId(0);
            integrator.setPrompt(getString(R.string.mensaje_ayuda_barcode));
            integrator.setCaptureActivity(CaptureActivityPortrait.class);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
            integrator.setBeepEnabled(false);
            integrator.initiateScan();
        }
    }

    public void validateCode(){
        this.setJsonRequest(this.codEquipo, MANUAL_METHOD, false);
        this.transacciones(jsonRequest);
    }

    public void transacciones(String value){
        String urlMantumLaravelParam = UserParameter.getValue(this, UserParameter.URL_MANTUM_FUTURE);
        this.urlMantumLaravel = (urlMantumLaravelParam == null || urlMantumLaravelParam.isEmpty()) ? inventarioActivosService.getUrlApi() + URL_VALIDAR_EQUIPO : urlMantumLaravelParam + URL_VALIDAR_EQUIPO;
        Transaccion transaccion = new Transaccion();
        transaccion.setUUID(UUID.randomUUID().toString());
        transaccion.setCuenta(cuenta);
        transaccion.setCreation(Calendar.getInstance().getTime());
        transaccion.setUrl(this.urlMantumLaravel);
        transaccion.setVersion(cuenta.getServidor().getVersion());
        transaccion.setValue(value);
        transaccion.setModulo(Transaccion.MODULO_INVENTARIO_EQUIPO);
        transaccion.setAccion(Transaccion.ACCION_INVENTARIO_EQUIPO);
        transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);
        transaccion.setIdentidad(null);

        TransaccionService transaccionService = new TransaccionService(this);
        compositeDisposable.add(transaccionService.save(transaccion)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Functions.emptyConsumer(), error -> {
                    progress.setVisibility(View.GONE);
                    Snackbar.make(getView(), R.string.error_encolar_proceso_contar_equipo, Snackbar.LENGTH_LONG)
                            .show();
                }, () -> {
                    progress.setVisibility(View.GONE);
                    Snackbar.make(getView(), R.string.success_encolar_proceso_contar_equipo, Snackbar.LENGTH_LONG)
                            .show();
                }));
    }

    private void setJsonRequest(String code, String countingMethod, boolean qrCodeAssociated){
        RequestValidarEquipo requestValidarEquipo = new RequestValidarEquipo(
                code,
                this.nomEquipo,
                this.idValidation,
                this.idCuenta,
                countingMethod,
                qrCodeAssociated
        );
        jsonRequest = gson.toJson(requestValidarEquipo);
    }

    private void loadFragment(Fragment fragment){
       getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragmentContainer, fragment)
        .commit();
    }

    public void setNomEquipo(String nombre){
        this.nomEquipo=nombre;
    }
    public void setCodEquipo(String codigo){
        this.codEquipo=codigo;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (database != null) {
            database.close();
        }
        if (inventarioActivosService != null) {
            inventarioActivosService.close();
        }
        compositeDisposable.clear();
    }
}