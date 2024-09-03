package com.mantum.cmms.activity;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Contacto;
import com.mantum.cmms.fragment.ActualizarContactoFragment;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.helper.TransaccionHelper;
import com.mantum.cmms.service.TransaccionService;
import com.mantum.cmms.util.Preferences;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

import java.util.Calendar;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class ActualizarContactoActivity extends TransaccionHelper.Dialog implements OnCompleteListener {

    public static final int REQUEST_ACTION = 1231;

    private static final String TAG = ActualizarContactoActivity.class.getSimpleName();

    private long idot;

    private TransaccionService transaccionService;

    private ActualizarContactoFragment actualizarContactoFragment;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Database database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            database = new Database(this);
            includeBackButtonAndTitle(R.string.actualizar_contacto);

            transaccionService = new TransaccionService(this);
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                this.idot = bundle.getLong(Mantum.KEY_ID);
            }

            actualizarContactoFragment = new ActualizarContactoFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, actualizarContactoFragment)
                    .commit();
        } catch (Exception e) {
            Log.e(TAG, "onCreate: ", e);
            backActivity(getString(R.string.error_app));
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

    @Override
    public void onComplete(@NonNull String name) {
    }

    private void register() {
        if (actualizarContactoFragment == null) {
            Snackbar.make(getView(), R.string.error_app, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            Snackbar.make(getView(), R.string.error_authentication, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        Contacto contacto = actualizarContactoFragment.getValue(this.idot);
        if (contacto == null) {
            Snackbar.make(getView(), R.string.error_terminar, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        String url = Preferences.url(this, "restapp/app/registercontact");

        Transaccion transaccion = new Transaccion();
        transaccion.setUUID(UUID.randomUUID().toString());
        transaccion.setCuenta(cuenta);
        transaccion.setCreation(Calendar.getInstance().getTime());
        transaccion.setUrl(url);
        transaccion.setVersion(cuenta.getServidor().getVersion());
        transaccion.setValue(contacto.toJson());
        transaccion.setModulo(Transaccion.MODULO_ORDEN_TRABAJO);
        transaccion.setAccion(Transaccion.ACCION_ACTUALIZAR_CONTACTO);
        transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);

        showProgressDialog();

        compositeDisposable.add(transaccionService.save(transaccion)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(self -> {
                }, this::onError, this::onComplete));
    }

    private void onComplete() {
        dismissProgressDialog();
        backActivity(getString(R.string.actualizar_contacto_exito));
    }

    private void onError(Throwable throwable) {
        Log.e(TAG, "onError: ", throwable);
        dismissProgressDialog();
        Snackbar.make(getView(), R.string.actualizar_contacto_error, Snackbar.LENGTH_LONG)
                .show();
    }
}