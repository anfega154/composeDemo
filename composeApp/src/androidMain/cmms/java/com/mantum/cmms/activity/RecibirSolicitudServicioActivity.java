package com.mantum.cmms.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.widget.AppCompatSpinner;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.mantum.demo.R;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.entity.parameter.SS;
import com.mantum.cmms.entity.parameter.StateReceive;
import com.mantum.cmms.domain.Recibir;
import com.mantum.cmms.service.temporal.SolicitudServicioService;
import com.mantum.component.service.Photo;
import com.mantum.core.Mantum;
import com.mantum.core.event.OnOffline;
import com.mantum.core.util.Assert;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.database.Where;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

public class RecibirSolicitudServicioActivity extends Mantum.Activity implements OnOffline {

    public static final int REQUEST_ACTION = 1207;

    private final static String TAG = RecibirSolicitudServicioActivity.class.getSimpleName();

    public static final String KEY_ID = "id";

    public static final String KEY_CODIGO = "codigo";

    private Photo image;

    private Long idSS;

    private Database database;

    private AppCompatSpinner estados;

    private AppCompatSpinner evaluation;

    private SolicitudServicioService solicitudServicioService;

    private String account;

    private Cuenta cuenta;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            this.setContentView(R.layout.activity_recibir_solicitud_servicio);

            this.database = new Database(this);
            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            Bundle bundle = this.getIntent().getExtras();
            if (Assert.isNull(bundle)) {
                throw new Exception();
            }

            this.idSS = bundle.getLong(KEY_ID);
            String code = bundle.getString(KEY_CODIGO);
            this.includeBackButtonAndTitle(this.getString(R.string.recibir_solicitud_servicio) + " " + code);

            this.progressPrepare(this.getString(R.string.solicitud_servicio_recibir_carga_titulo), this.getString(R.string.solicitud_servicio_recibir_carga_mensaje));

            final RecibirSolicitudServicioActivity self = this;
            FloatingActionButton firma = (FloatingActionButton) this.findViewById(R.id.firma);
            if (!Assert.isNull(firma)) {
                firma.setOnClickListener(v -> self.startActivity(FirmaActivity.class));
            }

            SharedPreferences sharedPreferences
                    = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            account = sharedPreferences.getString(getString(R.string.mantum_account), null);

            this.estados = (AppCompatSpinner) this.findViewById(R.id.estados);
            if (!Assert.isNull(this.estados)) {
                Where where = new Where().equalTo("cuenta.UUID", account);
                SS ss = (SS) this.database.findOne(where, SS.class);
                ArrayAdapter<StateReceive> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_dropdown_item, ss.getStatereceive());
                this.estados.setAdapter(adapter);
            }

            ArrayList<Integer> start = new ArrayList<>();
            for (int i = 5; i > 0; i--) {
                start.add(i);
            }
            ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, start);
            this.evaluation = (AppCompatSpinner) this.findViewById(R.id.evaluation);
            this.evaluation.setAdapter(adapter);

            this.solicitudServicioService = new SolicitudServicioService.Builder(this)
                    .authenticate(true)
                    .callback(this)
                    .onOffline(this, true)
                    .version(cuenta.getServidor().getVersion())
                    .endPoint("restapp/app/receivingss")
                    .build();

        } catch (Exception e) {
            this.backActivity(R.string.error_app);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!Assert.isNull(data)) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                String path = bundle.getString("file");
                if (!Assert.isNull(path)) {
                    this.image = new Photo(this, new File(path));
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.database.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_formulario, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }

        if (menuItem.getItemId() == R.id.action_done) {
            this.register();
            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void success(final Mantum.Success success, boolean offline) {
        final RecibirSolicitudServicioActivity self = this;
        this.runOnUiThread(() -> {
            self.hide();
            self.backActivity(success.getMessage());
        });
    }

    private void register() {
        try {
            TextInputEditText description = (TextInputEditText) this.findViewById(R.id.description);

            Recibir recibir = new Recibir();
            recibir.setId(null);
            recibir.setIdot(this.idSS);
            recibir.setStatereceive(this.estados.getSelectedItem().toString());
            recibir.setEvaluation(this.evaluation.getSelectedItem().toString());
            recibir.setReason(description.getText().toString());
            recibir.setImage(this.image);

            this.show();
            this.solicitudServicioService.toReceive(recibir);
        } catch (Exception e) {
            this.hide();
        }
    }

    @Override
    public Mantum.Response onRequest(String... params) {
        try {
            String value = params[0];
            String url = params[1];

            // Agrega las lecturas
            Transaccion transaccion = new Transaccion();
            transaccion.setUUID(UUID.randomUUID().toString());
            transaccion.setCuenta(cuenta);
            transaccion.setCreation(Calendar.getInstance().getTime());
            transaccion.setUrl(url + "/restapp/app/receivingss");
            transaccion.setVersion(cuenta.getServidor().getVersion());
            transaccion.setValue(value);
            transaccion.setModulo(Transaccion.MODULO_SOLICITUD_SERVICIO);
            transaccion.setAccion(Transaccion.ACCION_RECIBIR_SOLICITUD_SERVICIO);
            transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);
            this.database.insert(transaccion);

            Mantum.Success success = new Mantum.Success();
            success.ok(true);
            success.message(this.getString(R.string.recibir_solicitud_servicio_exitoso));
            return success;
        } catch (Exception e) {
            Mantum.Error error = new Mantum.Error();
            error.ok(false);
            error.message(this.getString(R.string.recibir_solicitud_servicio_error));
            return error;
        }
    }
}