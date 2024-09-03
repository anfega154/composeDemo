package com.mantum.cmms.activity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.mantum.demo.R;
import com.mantum.cmms.domain.RecibirOT;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.entity.parameter.OT;
import com.mantum.cmms.entity.parameter.StateReceive;
import com.mantum.cmms.fragment.RecibirOrdenTrabajoFragment;
import com.mantum.cmms.service.TransaccionService;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.cmms.database.Database;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class RecibirOrdenTrabajoActivity extends Mantum.Activity implements OnCompleteListener {

    private static final String TAG = RecibirOrdenTrabajoActivity.class.getSimpleName();

    public static final int REQUEST_ACTION = 1202;

    private Long id;
    private Cuenta cuenta;
    private Database database;
    private List<StateReceive> estados;
    private TransaccionService transaccionService;
    private RecibirOrdenTrabajoFragment recibirOrdenTrabajoFragment;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            includeBackButtonAndTitle(R.string.recibir_orden_trabajo);

            database = new Database(this);
            transaccionService = new TransaccionService(this);

            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                id = bundle.getLong(Mantum.KEY_ID);
                OT ot = database.where(OT.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();

                if (ot == null) {
                    throw new Exception("La orden de trabajo con id " + id + " no existe");
                }

                estados = ot.getStatereceive().isManaged()
                        ? database.copyFromRealm(ot.getStatereceive())
                        : ot.getStatereceive();
            }

            recibirOrdenTrabajoFragment = new RecibirOrdenTrabajoFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, recibirOrdenTrabajoFragment)
                    .commit();

        } catch (Exception e) {
            Log.e(TAG, "onCreate: ", e);
            backActivity(getString(R.string.error_app));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }

        Bundle bundle = data.getExtras();
        if (bundle != null) {
            String path = bundle.getString("file");
            if (path != null && recibirOrdenTrabajoFragment != null) {
                recibirOrdenTrabajoFragment.addPhoto(path);
            }
        }
    }

    @Override
    public void onComplete(@NonNull String name) {
        if (recibirOrdenTrabajoFragment != null && estados != null) {
            recibirOrdenTrabajoFragment.onStart(estados);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_formulario, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case android.R.id.home:
                super.onBackPressed();
                break;

            case R.id.action_done:
                register();
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

        if (transaccionService != null) {
            transaccionService.close();
        }

        compositeDisposable.clear();
    }

    private void register() {
        closeKeyboard();
        if (recibirOrdenTrabajoFragment == null) {
            Snackbar.make(getView(), R.string.error_app, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        if (cuenta == null || cuenta.getServidor() == null) {
            Snackbar.make(getView(), R.string.error_authentication, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        RecibirOT form = recibirOrdenTrabajoFragment.getValue();
        if (form == null) {
            Snackbar.make(getView(), R.string.recibir_orden_trabajo_error_formulario, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        form.setIdot(id);
        String url = cuenta.getServidor().getUrl() + "/restapp/app/receivingot";

        Transaccion transaccion = new Transaccion();
        transaccion.setUUID(UUID.randomUUID().toString());
        transaccion.setCuenta(cuenta);
        transaccion.setCreation(Calendar.getInstance().getTime());
        transaccion.setUrl(url);
        transaccion.setVersion(cuenta.getServidor().getVersion());
        transaccion.setValue(form.toJson());
        transaccion.setModulo(Transaccion.MODULO_ORDEN_TRABAJO);
        transaccion.setAccion(Transaccion.ACCION_RECIBIR_ORDEN_TRABAJO);
        transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);

        compositeDisposable.add(transaccionService.save(transaccion)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(self -> {
                }, this::onError, this::onComplete));
    }

    private void onComplete() {
        backActivity(getString(R.string.recibir_orden_trabajo_exitoso));
    }

    private void onError(Throwable throwable) {
        Snackbar.make(getView(), R.string.recibir_orden_trabajo_error, Snackbar.LENGTH_LONG)
                .show();
    }
}