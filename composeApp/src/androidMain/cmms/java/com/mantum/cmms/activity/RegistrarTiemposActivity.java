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
import com.mantum.cmms.domain.RegistrarTiempoConsolidado;
import com.mantum.cmms.fragment.RegistrarTiemposFragment;
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
import io.realm.Realm;

public class RegistrarTiemposActivity extends TransaccionHelper.Dialog implements OnCompleteListener {

    private static final String TAG
            = RegistrarTiemposActivity.class.getSimpleName();

    public static final int REQUEST_ACTION = 1230;

    private Realm realm;

    private Long id;

    private TransaccionService transaccionService;

    private RegistrarTiemposFragment registrarTiemposFragment;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            includeBackButtonAndTitle(R.string.registrar_tiempos);

            realm = new Database(this).instance();
            transaccionService = new TransaccionService(this);

            Cuenta cuenta = realm.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                throw new Exception(getString(R.string.eliminar_orde_trabajo));
            }

            id = bundle.getLong(Mantum.KEY_ID);
            registrarTiemposFragment = new RegistrarTiemposFragment();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, registrarTiemposFragment)
                    .commit();

        } catch (Exception e) {
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
                onBackPressed();
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
        if (realm != null) {
            realm.close();
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
        if (registrarTiemposFragment == null) {
            Snackbar.make(getView(), R.string.error_app, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        Cuenta cuenta = realm.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            Snackbar.make(getView(), R.string.error_authentication, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        RegistrarTiempoConsolidado consolidado = registrarTiemposFragment.getValue(id);
        if (consolidado == null) {
            Snackbar.make(getView(), R.string.error_file, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        String url = Preferences.url(this, "restapp/app/saveconsolidados");

        Transaccion transaccion = new Transaccion();
        transaccion.setUUID(UUID.randomUUID().toString());
        transaccion.setCuenta(cuenta);
        transaccion.setCreation(Calendar.getInstance().getTime());
        transaccion.setUrl(url);
        transaccion.setVersion(cuenta.getServidor().getVersion());
        transaccion.setValue(consolidado.toJson());
        transaccion.setModulo(Transaccion.MODULO_ORDEN_TRABAJO);
        transaccion.setAccion(Transaccion.ACCION_REGISTRAR_TIEMPOS);
        transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);

        showProgressDialog();

        compositeDisposable.add(transaccionService.save(transaccion)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(self -> {
                }, this::onError, this::onComplete));
    }

    private void onComplete() {
        dismissProgressDialog();
        backActivity(getString(R.string.registrar_tiempos_exitoso));
    }

    private void onError(Throwable throwable) {
        Log.e(TAG, "onError: ", throwable);
        dismissProgressDialog();
        Snackbar.make(getView(), R.string.registrar_tiempos_error, Snackbar.LENGTH_LONG)
                .show();
    }
}