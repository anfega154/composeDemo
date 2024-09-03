package com.mantum.cmms.activity;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.EstadoInicial;
import com.mantum.cmms.fragment.EstadoInicialFragment;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.service.TransaccionService;
import com.mantum.cmms.util.Preferences;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

import java.util.Calendar;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;

public class EstadoInicialActivity extends Mantum.Activity
        implements OnCompleteListener {

    public static final String KEY_CODE = "codigo";

    public static final int REQUEST_ACTION = 1298;

    private static final String TAG = EstadoInicialActivity.class.getSimpleName();

    private Realm realm;

    private EstadoInicial estadoIncicial;

    private TransaccionService transaccionService;

    private EstadoInicialFragment estadoInicialFragment;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            realm = new Database(this).instance();
            includeBackButtonAndTitle(R.string.estado_inicial);

            transaccionService = new TransaccionService(this);
            Cuenta cuenta = realm.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                Long id = bundle.getLong(Mantum.KEY_ID);
                estadoIncicial = realm.where(EstadoInicial.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .equalTo("idss", id)
                        .findFirst();

                if (estadoIncicial == null) {
                    estadoIncicial = new EstadoInicial();
                    estadoIncicial.setIdss(id);
                    estadoIncicial.setCodigo(bundle.getString(KEY_CODE));
                }
            }

            estadoInicialFragment = new EstadoInicialFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, estadoInicialFragment)
                    .commit();

        } catch (Exception e) {
            backActivity(getString(R.string.error_app));
        }
    }

    @Override
    public void onComplete(@NonNull String name) {
        if (estadoInicialFragment != null && estadoIncicial != null) {
            EstadoInicial value = estadoIncicial.isManaged()
                    ? realm.copyFromRealm(estadoIncicial)
                    : estadoIncicial;
            estadoInicialFragment.onStart(value);
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
            case android.R.id.home :
                super.onBackPressed();
                break;

            case R.id.action_done :
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

    private void register() {
        if (estadoInicialFragment == null) {
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

        EstadoInicial.Request estadoInicial = estadoInicialFragment.getValue();
        if (estadoInicial == null) {
            Snackbar.make(getView(), R.string.error_estado_inicial, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        String url = Preferences.url(this, "restapp/app/saveestadoinicialss");

        Transaccion transaccion = new Transaccion();
        transaccion.setUUID(UUID.randomUUID().toString());
        transaccion.setCuenta(cuenta);
        transaccion.setCreation(Calendar.getInstance().getTime());
        transaccion.setUrl(url);
        transaccion.setVersion(cuenta.getServidor().getVersion());
        transaccion.setValue(estadoInicial.toJson());
        transaccion.setModulo(Transaccion.MODULO_SOLICITUD_SERVICIO);
        transaccion.setAccion(Transaccion.ACCION_ESTADO_INICIAL);
        transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);

        compositeDisposable.add(transaccionService.save(transaccion)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(self -> {}, this::onError, this::onComplete));
    }

    private void onComplete() {
        backActivity(getString(R.string.estado_inicial_exito));
    }

    private void onError(Throwable throwable) {
        Snackbar.make(getView(), R.string.estado_inicial_error, Snackbar.LENGTH_LONG)
                .show();
    }
}