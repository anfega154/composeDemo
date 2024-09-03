package com.mantum.cmms.activity;


import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.ResultValidationQr;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.component.Mantum;
import com.mantum.cmms.util.AESUtils;
import com.google.android.material.snackbar.Snackbar;

public class ValidarIngresoActivity extends Mantum.Activity {
    private Cuenta cuenta;
    private final String TITLE_BACK = "Validar ingreso autorización";
    private Database database;
    protected final Gson gson = new GsonBuilder().create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validar_ingreso);

        try {
            database = new Database(ValidarIngresoActivity.this);
            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();
            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }
            includeBackButtonAndTitle(TITLE_BACK);

            AppCompatButton validar = findViewById(R.id.validar_ingreso_qr);
            validar.setOnClickListener(v ->{
                scanQrCode();
            });

        } catch (Exception e) {
            Log.e("ValidarIngresoActivity", "Error: " + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                String encryptedData = result.getContents();

                String decryptedData = AESUtils.decrypt(encryptedData, getString(R.string.secret_qr_key));

                ResultValidationQr validacion = gson.fromJson(decryptedData, ResultValidationQr.class);
                displayQrData(validacion);
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        } catch (Exception e) {
            Log.e("ValidarIngresoActivity", "Error: " + e.getMessage());
            Snackbar.make(getView(), R.string.error_qr_code, Snackbar.LENGTH_LONG)
                    .show();
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void scanQrCode() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 3);
        } else {
            IntentIntegrator integrator = new IntentIntegrator(ValidarIngresoActivity.this);
            integrator.setOrientationLocked(true);
            integrator.setCameraId(0);
            integrator.setPrompt(getString(R.string.mensaje_ayuda_barcode));
            integrator.setCaptureActivity(CaptureActivityPortrait.class);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
            integrator.setBeepEnabled(false);
            integrator.initiateScan();
        }
    }

    private void displayQrData(ResultValidationQr validacion) {
        Intent intent = new Intent(ValidarIngresoActivity.this, MostrarIngresoActivity.class);
        intent.putExtra("identificacion", validacion.getIdentificacion());
        intent.putExtra("nombre", validacion.getNombre());
        intent.putExtra("empresa", !validacion.getEmpresa().isEmpty() ? validacion.getEmpresa() : "No aplica");
        intent.putExtra("marca", !validacion.getMarca().isEmpty() ? validacion.getMarca() : "No aplica");
        intent.putExtra("instalacionLocativa", !validacion.getInstalacionLocativa().isEmpty() ? validacion.getInstalacionLocativa() : "No aplica");
        intent.putExtra("fechaIngreso", validacion.getFechaIngreso());
        intent.putExtra("codigo", validacion.getCodigo());
        intent.putExtra("tipo", validacion.getTipo());
        intent.putExtra("fechaInicio", validacion.getFechaInicio());
        intent.putExtra("fechaFin", validacion.getFechaFin());
        startActivity(intent);
    }


    public void includeBackButtonAndTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            setTitle(title);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }

}
