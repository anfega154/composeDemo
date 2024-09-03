package com.mantum.cmms.activity;

import android.content.Intent;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.mantum.R;
import com.mantum.cmms.entity.Busqueda;

public class BridgeActivity extends CaptureActivity {

    public static final int REQUEST_CODE = 0x0000c0de; // Only use bottom 16 bits

    public static final String ID_ENTIDAD_SELECCIONADA = "ID_ENTIDAD_SELECCIONADA";

    public static final String CODIGO_ENTIDAD_SELECCIONADA = "CODIGO_ENTIDAD_SELECCIONADA";

    public static final String NOMBRE_ENTIDAD_SELECCIONADA = "NOMBRE_ENTIDAD_SELECCIONADA";

    public static final String TIPO_ENTIDAD_SELECCIONADA = "TIPO_ENTIDAD_SELECCIONADA";

    public static final String POSICION_SELECCIONADA = "POSICION_SELECCIONADA";

    public static final String LECTURA_TIPO_ENTIDAD = "LECTURA_TIPO_ENTIDAD";

    public static final String LECTURA_ID_ENTIDAD = "LECTURA_ID_ENTIDAD";

    private Long idEntidad;

    private String tipoEntidad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.idEntidad = null;
        this.tipoEntidad = null;

        String codigo = "";
        String nombre = "";
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            codigo = bundle.getString(CODIGO_ENTIDAD_SELECCIONADA, "");
            nombre = bundle.getString(NOMBRE_ENTIDAD_SELECCIONADA, "");
        }

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setOrientationLocked(true);
        integrator.setCameraId(0);
        integrator.setPrompt(codigo.isEmpty() && nombre.isEmpty()
                ? getString(R.string.leer_entidad_qr)
                : String.format(getString(R.string.confirmar_entidad), codigo, nombre));
        integrator.setCaptureActivity(CaptureActivityPortrait.class);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        Bundle bundle = getIntent().getExtras();
        intent.putExtra(LECTURA_ID_ENTIDAD, idEntidad);
        intent.putExtra(LECTURA_TIPO_ENTIDAD, tipoEntidad);
        if (bundle != null) {
            intent.putExtra(POSICION_SELECCIONADA,
                    bundle.getInt(POSICION_SELECCIONADA, -1));

            intent.putExtra(ID_ENTIDAD_SELECCIONADA,
                    bundle.getLong(ID_ENTIDAD_SELECCIONADA, -1));

            intent.putExtra(TIPO_ENTIDAD_SELECCIONADA,
                    bundle.getString(TIPO_ENTIDAD_SELECCIONADA, ""));
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            Busqueda.Read read = new Gson()
                    .fromJson(result.getContents(), Busqueda.Read.class);

            if (read != null) {
                this.tipoEntidad = read.getEntityType();
                this.idEntidad = read.getEntityId() != null
                        ? Long.valueOf(read.getEntityId())
                        : null;
            }
        }
        onBackPressed();
    }
}