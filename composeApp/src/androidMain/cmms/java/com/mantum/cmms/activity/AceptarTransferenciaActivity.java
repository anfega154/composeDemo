package com.mantum.cmms.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mantum.demo.R;
import com.mantum.cmms.adapter.ActivoAdapter;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Transferencia;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.entity.parameter.UserParameter;
import com.mantum.cmms.service.CaptureGeolocationService;
import com.mantum.component.service.Photo;
import com.mantum.core.Mantum;
import com.mantum.core.component.DatePicker;
import com.mantum.core.component.PickerAbstract;
import com.mantum.core.component.TimePicker;
import com.mantum.core.util.Assert;
import com.mantum.core.util.Url;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;

import static com.mantum.cmms.entity.parameter.UserParameter.MENSAJE_ACEPTAR_TRANSFERENCIA;
import static com.mantum.cmms.entity.parameter.UserParameter.MOSTRAR_MENSAJE_ACEPTAR_TRANSFERENCIA;

public class AceptarTransferenciaActivity extends Mantum.Activity
        implements DatePicker.Callback, TimePicker.Callback {

    public static final String KEY_ID = "id";

    private static final String TAG = AceptarTransferenciaActivity.class.getSimpleName();

    private Long id;

    private List<Photo> camera;

    private DatePicker date;

    private TimePicker time;

    private Database database;

    private Cuenta cuenta;

    private final ActivoAdapter adapter = new ActivoAdapter();

    private CaptureGeolocationService captureGeolocationService;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Location location;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_aceptar_transferencia);

            camera = new ArrayList<>();
            includeBackButtonAndTitle(R.string.accion_aceptar_transferencia);
            progressPrepare(getString(R.string.transferencia_titulo), getString(R.string.transferencia_mensaje));
            captureGeolocationService = new CaptureGeolocationService(this);

            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            date = new DatePicker.Builder(this)
                    .id(R.id.creation_date)
                    .calendar(calendar)
                    .callback(this)
                    .enabled(true)
                    .build();

            time = new TimePicker.Builder(this)
                    .id(R.id.creation_time)
                    .calendar(calendar)
                    .callback(this)
                    .enabled(true)
                    .build();

            FloatingActionButton firma = findViewById(R.id.firma);
            firma.setOnClickListener(v -> startActivity(FirmaActivity.class));

            database = new Database(this);
            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();
            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                id = bundle.getLong(KEY_ID);
                com.mantum.cmms.entity.Transferencia transferencia = database.instance()
                        .where(com.mantum.cmms.entity.Transferencia.class)
                        .equalTo("id", id)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();
                if (transferencia == null) {
                    throw new Exception(getString(R.string.error_transferencia));
                }

                TextView code = (TextView) findViewById(R.id.code);
                code.setText(transferencia.getCodigo());

                TextView date = (TextView) findViewById(R.id.date);
                date.setText(transferencia.getFecha());

                adapter.add(transferencia.getActivos());
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setHasFixedSize(true);
                recyclerView.setAdapter(adapter);
                recyclerView.setItemViewCacheSize(20);
                recyclerView.setDrawingCacheEnabled(true);
                recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            }
        } catch (Exception e) {
            backActivity(R.string.error_app);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && !Assert.isNull(data)) {
            Bundle bundle = data.getExtras();
            if (!Assert.isNull(bundle)) {
                String file = bundle.getString("file");
                if (!Assert.isNull(file)) {
                    camera.add(new Photo(this, new File(file)));
                }
            }
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
            super.onBackPressed();
            return true;
        }

        boolean mostrarMensajeAceptarTransferencia = false;
        if (UserParameter.getValue(getView().getContext(), MOSTRAR_MENSAJE_ACEPTAR_TRANSFERENCIA) != null)
            mostrarMensajeAceptarTransferencia = Boolean.parseBoolean(UserParameter.getValue(getView().getContext(), MOSTRAR_MENSAJE_ACEPTAR_TRANSFERENCIA));

        String mensajeAceptarTransferencia = UserParameter.getValue(this, MENSAJE_ACEPTAR_TRANSFERENCIA);

        if (menuItem.getItemId() == R.id.action_done) {
            if (mostrarMensajeAceptarTransferencia && mensajeAceptarTransferencia != null) {
                new AlertDialog.Builder(this)
                        .setMessage(mensajeAceptarTransferencia)
                        .setPositiveButton("Cancelar", (dialogInterface, i) -> dialogInterface.cancel())
                        .setNegativeButton("Aceptar", (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            done();
                        })
                        .show();
            } else {
                done();
            }
        }

        return super.onOptionsItemSelected(menuItem);
    }

    private void done() {
        compositeDisposable.add(captureGeolocationService.obtener(false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNext, this::onError, this::register));
    }

    @Override
    public void timeSet(View view, int hourOfDay, int minute) {
        ((EditText) view).setText(new StringBuilder()
                .append(PickerAbstract.normalize(hourOfDay))
                .append(":").append(PickerAbstract.normalize(minute)));
    }

    @Override
    public void dateSet(View view, int year, int month, int day) {
        ((EditText) view).setText(new StringBuilder()
                .append(year).append("-")
                .append(PickerAbstract.normalize(month)).append("-")
                .append(PickerAbstract.normalize(day)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
        camera.clear();
        compositeDisposable.clear();
    }

    private void onNext(Location location) {
        this.location = location;
    }

    private void onError(@NonNull Throwable throwable) {
        if (throwable.getMessage() != null) {
            Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private void register() {
        try {
            closeKeyboard();

            show();
            Gson gson = new GsonBuilder().create();
            String value = gson.toJson(new Transferencia(camera, id, date.value() + " " + time.value(), this.location.getLatitude(), this.location.getLongitude()));

            Realm realm = database.instance();
            realm.beginTransaction();

            Transaccion transaccion = new Transaccion();
            transaccion.setUUID(UUID.randomUUID().toString());
            transaccion.setCuenta(cuenta);
            transaccion.setCreation(Calendar.getInstance().getTime());
            transaccion.setUrl(Url.build(this, "/restapp/app/aceptartransferencia"));
            transaccion.setVersion(cuenta.getServidor().getVersion());
            transaccion.setValue(value);
            transaccion.setModulo(Transaccion.MODULO_ACTIVOS);
            transaccion.setAccion(Transaccion.ACCION_ACEPTAR_TRANSFERENCIA);
            transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);

            realm.insert(transaccion);
            realm.commitTransaction();

            hide();
            backActivity(getString(R.string.transferencia_transaccion));
        } catch (Exception e) {
            hide();
            Log.e(TAG, "register: " + e.getMessage(), e);
        }
    }
}