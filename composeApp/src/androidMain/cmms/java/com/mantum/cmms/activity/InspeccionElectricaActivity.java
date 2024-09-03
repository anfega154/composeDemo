package com.mantum.cmms.activity;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import android.view.Menu;
import android.view.MenuItem;

import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.InspeccionElectrica;
import com.mantum.cmms.fragment.InspeccionElectricaFragment;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.helper.TransaccionHelper;
import com.mantum.cmms.service.TransaccionService;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

import java.util.Calendar;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class InspeccionElectricaActivity extends TransaccionHelper.Dialog implements OnCompleteListener {

    public static final int REQUEST_ACTION = 1230;

    private Long id;

    private Database database;

    private TransaccionService transaccionService;

    private InspeccionElectricaFragment inspeccionElectricaFragment;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            includeBackButtonAndTitle(R.string.calidad);

            database = new Database(this);
            transaccionService = new TransaccionService(this);

            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                throw new Exception(getString(R.string.error_detail_ot));
            }

            id = bundle.getLong(Mantum.KEY_ID);
            inspeccionElectricaFragment = new InspeccionElectricaFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, inspeccionElectricaFragment)
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
        if (database != null) {
            database.close();
        }

        if (transaccionService != null) {
            transaccionService.close();
        }

        compositeDisposable.clear();
    }

    private void register() {
        if (inspeccionElectricaFragment == null) {
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

        InspeccionElectrica inspeccionElectrica = inspeccionElectricaFragment.getValue(id);
        if (inspeccionElectrica == null) {
            Snackbar.make(getView(), R.string.error_app, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        Transaccion transaccion = new Transaccion();
        transaccion.setUUID(UUID.randomUUID().toString());
        transaccion.setCuenta(cuenta);
        transaccion.setCreation(Calendar.getInstance().getTime());
        transaccion.setUrl(cuenta.getServidor().getUrl() + "/restapp/app/saveinspeccioncalidad");
        transaccion.setVersion(cuenta.getServidor().getVersion());
        transaccion.setValue(inspeccionElectrica.toJson());
        transaccion.setModulo(Transaccion.MODULO_ORDEN_TRABAJO);
        transaccion.setAccion(Transaccion.ACCION_INSPECCION_ELECTRICA);
        transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);

        showProgressDialog();

        compositeDisposable.add(transaccionService.save(transaccion)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(self -> { }, this::onError, this::onComplete));
    }

    private void onComplete() {
        dismissProgressDialog();
        backActivity(getString(R.string.inspeccion_electrica_exitoso));
    }

    private void onError(@SuppressWarnings("unused") Throwable throwable) {
        dismissProgressDialog();
        Snackbar.make(getView(), R.string.inspeccion_electrica_error, Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void onComplete(@NonNull String name) {
        inspeccionElectricaFragment.iniciar();
    }
}