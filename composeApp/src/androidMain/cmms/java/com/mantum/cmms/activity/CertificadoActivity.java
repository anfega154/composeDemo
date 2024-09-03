package com.mantum.cmms.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import androidx.core.app.ActivityCompat;
import androidx.appcompat.widget.AppCompatButton;

import android.util.Log;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mantum.demo.R;
import com.mantum.cmms.domain.QR;
import com.mantum.cmms.entity.Certificado;
import com.mantum.cmms.net.CertificateManager;
import com.mantum.cmms.service.CertificadoServices;
import com.mantum.component.Mantum;

import java.io.File;
import java.io.InputStream;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.mantum.cmms.net.CertificateManager.Builder.check;
import static com.mantum.cmms.net.CertificateManager.Builder.getExtension;
import static com.mantum.cmms.net.CertificateManager.Builder.read;
import static com.mantum.cmms.net.CertificateManager.Builder.copy;

public class CertificadoActivity extends Mantum.Activity {

    private static final String TAG = CertificadoActivity.class.getSimpleName();

    public static final int REQUEST_CODE = 100;
    public static final String MODO_ACTUALIZAR = "modo_actualizar";

    private static final int SERVER_REQUEST_CODE = 101;
    private static final int CLIENT_REQUEST_CODE = 102;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private boolean isActualizar = false;
    private final Gson gson = new Gson();
    private CertificadoServices certificadoServices;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificado);

        requestPermission(this);
        certificadoServices = new CertificadoServices(this);

        findViewById(R.id.server).setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent = Intent.createChooser(
                    intent, getString(R.string.seleccionar_certificado_servidor));

            startActivityForResult(intent, SERVER_REQUEST_CODE);
        });

        findViewById(R.id.client).setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent = Intent.createChooser(
                    intent, getString(R.string.seleccionar_certificado_cliente));

            startActivityForResult(intent, CLIENT_REQUEST_CODE);
        });

        AppCompatButton access = findViewById(R.id.access);
        access.setOnClickListener(view -> {
            TextInputEditText server = findViewById(R.id.server);
            TextInputEditText client = findViewById(R.id.client);

            if ("".equals(server.getText().toString()) || "".equals(client.getText().toString())) {
                Snackbar.make(view, R.string.certificado_requerido, Snackbar.LENGTH_LONG)
                        .show();
                return;
            }

            TextInputEditText password = findViewById(R.id.password);
            TextInputEditText confirm = findViewById(R.id.confirm_password);

            if ("".equals(password.getText().toString()) || "".equals(confirm.getText().toString())) {
                Snackbar.make(view, R.string.contrasena_requerida, Snackbar.LENGTH_LONG)
                        .show();
                return;
            }

            if (!password.getText().toString().equals(confirm.getText().toString())) {
                Snackbar.make(view, R.string.contrasena_invalida, Snackbar.LENGTH_LONG)
                        .show();
                return;
            }

            String path = getFilesDir().getPath() + "/";
            InputStream clientInputStream = read(path + client.getText().toString());

            if (clientInputStream == null || !check(clientInputStream, password.getText().toString())) {
                Snackbar.make(view, R.string.error_abrir_certificado, Snackbar.LENGTH_LONG)
                        .show();
                return;
            }

            if (isActualizar) {
                certificadoServices.update(new Certificado()
                        .setServer(path + server.getText().toString())
                        .setClient(path + client.getText().toString())
                        .setPassword(password.getText().toString())
                );

                backActivity();
                return;
            }

            IntentIntegrator integrator = new IntentIntegrator(CertificadoActivity.this);
            integrator.setOrientationLocked(true);
            integrator.setCameraId(0);
            integrator.setPrompt(getString(R.string.mensaje_ayuda_barcode));
            integrator.setCaptureActivity(CaptureActivityPortrait.class);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
            integrator.setBeepEnabled(false);
            integrator.initiateScan();
        });

        findViewById(R.id.test).setOnClickListener(view -> {
            TextInputEditText server = findViewById(R.id.server);
            TextInputEditText client = findViewById(R.id.client);

            if ("".equals(server.getText().toString()) || "".equals(client.getText().toString())) {
                Snackbar.make(view, R.string.certificado_requerido, Snackbar.LENGTH_LONG)
                        .show();
                return;
            }

            TextInputEditText password = findViewById(R.id.password);
            TextInputEditText confirm = findViewById(R.id.confirm_password);

            if ("".equals(password.getText().toString()) || "".equals(confirm.getText().toString())) {
                Snackbar.make(view, R.string.contrasena_requerida, Snackbar.LENGTH_LONG)
                        .show();
                return;
            }

            if (!password.getText().toString().equals(confirm.getText().toString())) {
                Snackbar.make(view, R.string.contrasena_invalida, Snackbar.LENGTH_LONG)
                        .show();
                return;
            }

            String path = getFilesDir().getPath() + "/";

            InputStream clientInputStream = read(path + client.getText().toString());
            if (clientInputStream == null || !check(clientInputStream, password.getText().toString())) {
                Snackbar.make(view, R.string.error_abrir_certificado, Snackbar.LENGTH_LONG)
                        .show();
                return;
            }

            try {
                new CertificateManager.Builder()
                        .server(read(path + server.getText().toString()))
                        .client(read(path + client.getText().toString()), password.getText().toString())
                        .build();

                Snackbar.make(getView(), getString(R.string.prueba_conexion_exitosa_certificado), Snackbar.LENGTH_LONG)
                        .show();

            } catch (Exception e) {
                Log.e(TAG, "onCreate: ", e);
                Snackbar.make(getView(), getString(R.string.prueba_conexion_error_certificado), Snackbar.LENGTH_LONG)
                        .show();
            }
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            isActualizar = bundle.getBoolean(MODO_ACTUALIZAR, false);
            if (isActualizar) {
                access.setText(R.string.actualizar);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {

            case SERVER_REQUEST_CODE: {
                if (!"crt".equals(getExtension(this, data.getData()))) {
                    Snackbar.make(getView(), getString(R.string.server_certificate_error), Snackbar.LENGTH_LONG)
                            .show();
                    break;
                }

                File target = copy(this, data.getData(), ".crt");
                if (target == null) {
                    Snackbar.make(getView(), getString(R.string.error_copiar_certificado), Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                TextInputEditText server = findViewById(R.id.server);
                server.setText(target.getName());
                break;
            }

            case CLIENT_REQUEST_CODE: {
                if (!"p12".equals(getExtension(this, data.getData()))) {
                    Snackbar.make(getView(), getString(R.string.client_certificate_error), Snackbar.LENGTH_LONG)
                            .show();
                    break;
                }

                File target = copy(this, data.getData(), ".p12");
                if (target == null) {
                    Snackbar.make(getView(), getString(R.string.error_copiar_certificado), Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                TextInputEditText client = findViewById(R.id.client);
                client.setText(target.getName());
                break;
            }

            case IntentIntegrator.REQUEST_CODE:
                IntentResult result = IntentIntegrator.parseActivityResult(
                        requestCode, resultCode, data);

                if (result != null) {
                    QR access = gson.fromJson(result.getContents(), QR.class);

                    TextInputEditText server = findViewById(R.id.server);
                    TextInputEditText client = findViewById(R.id.client);
                    TextInputEditText password = findViewById(R.id.password);

                    String path = getFilesDir().getPath() + "/";

                    certificadoServices.save(new Certificado()
                            .setServer(path + server.getText().toString())
                            .setClient(path + client.getText().toString())
                            .setPassword(password.getText().toString())
                            .setUrl(access.getUrl())
                            .setUsername(access.getLogin())
                            .setDatabase(access.getBasename())
                    );

                    setResult(resultCode, data);
                    finish();
                    break;
                }

                Snackbar.make(getView(), getString(R.string.token_error_qr), Snackbar.LENGTH_LONG)
                        .show();
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (certificadoServices != null) {
            certificadoServices.close();
        }
    }

    public static boolean requestPermission(@NonNull Context context) {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            return false;
        }
        return true;
    }
}