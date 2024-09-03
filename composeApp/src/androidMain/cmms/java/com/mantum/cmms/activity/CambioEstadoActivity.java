package com.mantum.cmms.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.mantum.demo.R;
import com.mantum.component.Mantum;
import com.mantum.component.component.DatePicker;
import com.mantum.component.component.TimePicker;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Novedad;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Transaccion;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CambioEstadoActivity extends Mantum.Activity {

    private static final String TAG = CambioEstadoActivity.class.getSimpleName();

    public static final String UUID_TRANSACCION = "UUID";

    public static final String MODE_EDIT = "edit";

    private Database database;

    private Cuenta cuenta;

    private String uuidTransacion;

    private Novedad novedad;

    private DatePicker date;

    private TimePicker time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_cambio_estado);
            includeBackButtonAndTitle("Cambio de estado");

            database = new Database(this);
            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                throw new Exception(getString(R.string.error_detalle));
            }

            uuidTransacion = bundle.getString(UUID_TRANSACCION);

            Gson gson = new Gson();
            novedad = gson.fromJson(bundle.getString(MODE_EDIT), Novedad.class);

            date = new DatePicker(this, R.id.date);
            date.setEnabled(true);
            date.load();

            time = new TimePicker(this, R.id.time);
            time.setEnabled(true);
            time.load();

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            Date dateAndTime = simpleDateFormat.parse(novedad.getFechaInicial());
            if (dateAndTime != null) {
                date.setValue(dateFormat.format(dateAndTime));
                time.setValue(timeFormat.format(dateAndTime));
            }
        } catch (Exception e) {
            Log.e(TAG, "onCreate: ", e);
        }
    }

    private void enviarTransaccion() {
        novedad.setFechaInicial(String.format("%s %s:00", date.getValue(), time.getValue()));

        if (uuidTransacion != null) {
            Transaccion transaccion = database.where(Transaccion.class)
                    .equalTo("UUID", uuidTransacion)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findFirst();

            if (transaccion != null) {
                database.update(() -> {
                    transaccion.setCreation(Calendar.getInstance().getTime());
                    transaccion.setValue(novedad.toJson());
                    transaccion.setMessage("");
                    transaccion.setUrl(cuenta.getServidor().getUrl() + "/restapp/app/actualizarestadopersonal");
                    transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);
                });

                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (database != null) {
            database.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_formulario, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_done:
                enviarTransaccion();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }
}