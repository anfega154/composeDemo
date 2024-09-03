package com.mantum.cmms.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.cardview.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mantum.demo.R;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.entity.parameter.AspectEval;
import com.mantum.cmms.entity.parameter.SS;
import com.mantum.cmms.domain.Evaluar;
import com.mantum.cmms.service.temporal.SolicitudServicioService;
import com.mantum.cmms.util.BackEditTransaction;
import com.mantum.core.Mantum;
import com.mantum.core.event.OnOffline;
import com.mantum.core.util.Assert;
import com.mantum.core.util.DPI;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.database.Where;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class EvaluarActivity extends Mantum.Activity implements OnOffline {

    private static final String TAG = EvaluarActivity.class.getSimpleName();

    public static final String KEY_ID = "id";

    public static final String KEY_CODIGO = "codigo";

    private static final String VERSION = "000";

    public static final String UUID_TRANSACCION = "UUID";

    public static final String MODE_EDIT = "edit";

    public static final int REQUEST_ACTION = 1209;

    //region Variables

    private String UUIDTransaccion; // Identificacion de la transaccion

    private Long id;

    private String codigo;

    private Database database;

    private SolicitudServicioService solicitudServicioService;

    private String account;

    private Cuenta cuenta;

    //endregion

    //region Estados

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            // Inicia la conexion a la base de datos
            this.database = new Database(this);
            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            // Muestra el mensaje de trabajo sin conexion
            this.offline();

            // Incluye la vista
            super.onCreate(savedInstanceState);
            this.setContentView(R.layout.activity_evaluar_solicitud_servicio);

            // Incluye las acciones
            this.includeBackButtonAndTitle(R.string.accion_registrar_evaluar);

            // Inicializa el componente de carga
            this.progressPrepare(this.getString(R.string.solicitud_servicio_evaluar_carga_titulo), this.getString(R.string.solicitud_servicio_evaluar_carga_mensaje));

            // Agrega la entidad seleccionada
            Bundle bundle = this.getIntent().getExtras();
            if (!Assert.isNull(bundle)) {
                this.id = bundle.getLong(KEY_ID);
                this.codigo = bundle.getString(KEY_CODIGO);
            }

            SharedPreferences sharedPreferences
                    = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            account = sharedPreferences.getString(getString(R.string.mantum_account), null);

            // Inicializa el servicio
            this.solicitudServicioService = new SolicitudServicioService.Builder(this)
                    .endPoint("restapp/app/evalss")
                    .callback(this)
                    .onOffline(this, true)
                    .version(VERSION)
                    .authenticate(true)
                    .build();

            // Modo editar
            List<Evaluar.Detalle> detalle = null;
            if (!Assert.isNull(bundle)) {
                String JSON = bundle.getString(MODE_EDIT);
                this.UUIDTransaccion = bundle.getString(UUID_TRANSACCION);
                if (!Assert.isNull(JSON)) {
                    Gson gson = new Gson();
                    Evaluar evaluar = gson.fromJson(JSON, Evaluar.class);

                    this.id = evaluar.getId();
                    this.codigo = evaluar.getCodigo();
                    detalle = evaluar.getAspecteval();
                    TextInputEditText descripcion = (TextInputEditText) this.findViewById(R.id.description);
                    if (!Assert.isNull(descripcion)) {
                        descripcion.setText(evaluar.getDescription());
                    }
                }
            }

            // Agrega el codigo de la solicitud de servicio
            EditText entity = (EditText) this.findViewById(R.id.entity);
            if (!Assert.isNull(entity)) {
                entity.setText(this.codigo);
            }

            // Contiene los valores para calificar la S:S.
            List<Integer> start = new ArrayList<>();
            for (int i = 1; i <= 5; i++) { start.add(i); }
            ArrayAdapter<Integer> adapter
                    = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, start);

            // Obtiene los aspectos para evaluar de la base de datos
            LinearLayout body = (LinearLayout) this.findViewById(R.id.body);
            Where where = new Where().equalTo("cuenta.UUID", account);
            SS ss = (SS) this.database.findOne(where, SS.class);
            for (AspectEval aspectEval : ss.getAspecteval()) {
                CardView cardView = new CardView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

                params.setMargins(
                        DPI.convert(8, getResources().getDisplayMetrics()),
                        DPI.convert(8, getResources().getDisplayMetrics()),
                        DPI.convert(8, getResources().getDisplayMetrics()),
                        DPI.convert(4, getResources().getDisplayMetrics()));
                cardView.setLayoutParams(params);

                LinearLayout linearLayout= new LinearLayout(this);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));

                TextView textView = new TextView(this);
                textView.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL), Typeface.BOLD);
                textView.setText(aspectEval.getNombre());
                textView.setMaxLines(2);
                textView.setTextSize(20);
                textView.setPadding(
                        DPI.convert(16, getResources().getDisplayMetrics()),
                        DPI.convert(16, getResources().getDisplayMetrics()),
                        DPI.convert(16, getResources().getDisplayMetrics()),
                        DPI.convert(4, getResources().getDisplayMetrics()));

                textView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                linearLayout.addView(textView);

                textView = new TextView(this);
                textView.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
                textView.setText(aspectEval.getDescripcion());
                textView.setMaxLines(3);
                textView.setTextSize(14);
                textView.setPadding(
                        DPI.convert(16, getResources().getDisplayMetrics()),
                        DPI.convert(4, getResources().getDisplayMetrics()),
                        DPI.convert(16, getResources().getDisplayMetrics()),
                        DPI.convert(4, getResources().getDisplayMetrics()));

                textView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                linearLayout.addView(textView);

                params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

                params.setMargins(
                    DPI.convert(16, getResources().getDisplayMetrics()),
                        DPI.convert(0, getResources().getDisplayMetrics()),
                        DPI.convert(16, getResources().getDisplayMetrics()),
                        DPI.convert(0, getResources().getDisplayMetrics()));

                int seleccion = 2;
                if (!Assert.isNull(detalle)) {
                    for (Evaluar.Detalle editar : detalle) {
                        if (editar.getId().equals(aspectEval.getId())) {
                            seleccion = editar.getScore() - 1;
                            break;
                        }
                    }
                }

                AppCompatSpinner spinner = new AppCompatSpinner(this);
                spinner.setId(this.getIdentifier(String.valueOf(aspectEval.getId())));
                spinner.setAdapter(adapter);
                spinner.setSelection(seleccion);
                spinner.setLayoutParams(params);
                linearLayout.addView(spinner);
                cardView.addView(linearLayout);
                body.addView(cardView);
            }
        } catch (Exception e) {
            this.backActivity(R.string.error_app);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_formulario, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null && bundle.getString(MODE_EDIT) != null) {
                BackEditTransaction.backDialog(this);
            } else {
                onBackPressed();
            }
            return true;
        }

        if (menuItem.getItemId() == R.id.action_done) {
            this.register();
            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.database.close();
    }

    //endregion

    //region Eventos

    @Override
    public void success(final Mantum.Success success, boolean offline) {
        final EvaluarActivity self = this;
        this.runOnUiThread(() -> {
            self.hide();
            self.backActivity(success.getMessage());
        });
    }

    //endregion

    //region Acciones

    /**
     * Registra la evaluación de la S.S.
     */
    private void register() {
        try {
            // Cierra el teclado
            this.closeKeyboard();

            TextInputLayout contenedor = (TextInputLayout) this.findViewById(R.id.description_content);
            contenedor.setError(null);

            EditText description = (EditText) this.findViewById(R.id.description);
            if (!Assert.isNull(description) && description.getText().toString().isEmpty()) {
                contenedor.setError(this.getString(R.string.ss_register_description_empty));
                contenedor.requestFocus();
                return;
            }

            // Muestra el componente de carga
            this.show();

            // Obtiene los elementos para evaluar
            ArrayList<Evaluar.Detalle> detalles = new ArrayList<>();
            Where where = new Where().equalTo("cuenta.UUID", account);
            SS ss = (SS) this.database.findOne(where, SS.class);
            for (AspectEval eval : ss.getAspecteval()) {
                AppCompatSpinner spinner = (AppCompatSpinner) this.findViewById(this.getIdentifier(String.valueOf(eval.getId())));
                Integer score = (Integer) spinner.getSelectedItem();

                Evaluar.Detalle detalle = new Evaluar.Detalle();
                detalle.setId(eval.getId());
                detalle.setScore(score);
                detalles.add(detalle);
            }

            // Evalua la S.S.
            Evaluar evaluar = new Evaluar();
            evaluar.setId(this.id);
            evaluar.setCodigo(this.codigo);
            evaluar.setDescription(description.getText().toString());
            evaluar.setAspecteval(detalles);
            this.solicitudServicioService.evaluate(evaluar);
        } catch (Exception e) {
            this.hide();
        }
    }

    @Override
    public Mantum.Response onRequest(String... params) {
        try {
            final String value = params[0];
            String url = params[1];

            // Obtiene la transaccion si esta en modo de editar el registro
            Transaccion transaccion = null;
            if (!Assert.isNull(this.UUIDTransaccion)) {
                Where where = new Where()
                        .equalTo("UUID", this.UUIDTransaccion)
                        .equalTo("cuenta.UUID", cuenta.getUUID());
                transaccion = (Transaccion) this.database.findOne(where, Transaccion.class);
            }

            // Agrega la evaluación de la solicitud de servicio
            if (Assert.isNull(transaccion)) {
                transaccion = new Transaccion();
                transaccion.setUUID(UUID.randomUUID().toString());
                transaccion.setCuenta(cuenta);
                transaccion.setCreation(Calendar.getInstance().getTime());
                transaccion.setUrl(url + "/restapp/app/evalss");
                transaccion.setVersion(VERSION);
                transaccion.setValue(value);
                transaccion.setModulo(Transaccion.MODULO_SOLICITUD_SERVICIO);
                transaccion.setAccion(Transaccion.ACCION_EVALUAR_SOLICITUD_SERVICIO);
                transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);
                this.database.insert(transaccion);
            } else {
                final Transaccion finalTransaccion = transaccion;
                this.database.update(() -> {
                    finalTransaccion.setCreation(Calendar.getInstance().getTime());
                    finalTransaccion.setValue(value);
                    finalTransaccion.setMessage("");
                    finalTransaccion.setUrl(url + "/restapp/app/evalss");
                    finalTransaccion.setEstado(Transaccion.ESTADO_PENDIENTE);
                });
            }

            Mantum.Success success = new Mantum.Success();
            success.ok(true);
            success.message(this.getString(R.string.evaluar_exitos));
            return success;
        } catch (Exception e) {
            Mantum.Error error = new Mantum.Error();
            error.ok(false);
            error.message(this.getString(R.string.evaluar_error));
            return error;
        }
    }

    //endregion
}