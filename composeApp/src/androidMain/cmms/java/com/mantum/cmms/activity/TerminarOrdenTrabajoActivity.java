package com.mantum.cmms.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Terminar;
import com.mantum.cmms.entity.parameter.UserParameter;
import com.mantum.cmms.fragment.OrdenTrabajoTerminarFragment;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.helper.TransaccionHelper;
import com.mantum.cmms.service.TransaccionService;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.service.Photo;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

import static com.mantum.cmms.entity.parameter.UserParameter.FIRMA_OBLIGATORIA;

public class TerminarOrdenTrabajoActivity extends TransaccionHelper.Dialog implements OnCompleteListener {

    private static final String TAG = TerminarOrdenTrabajoActivity.class.getSimpleName();

    public static final int REQUEST_ACTION = 1201;

    public static final String KEY_CODE = "key_code";

    private Database database;

    private Terminar terminar;

    private TransaccionService transaccionService;

    private OrdenTrabajoTerminarFragment ordenTrabajoTerminarFragment;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            includeBackButtonAndTitle(R.string.terminar_orden_trabajo);

            database = new Database(this);
            transaccionService = new TransaccionService(this);
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                Long id = bundle.getLong(Mantum.KEY_ID);
                String code = bundle.getString(KEY_CODE);
                terminar = new Terminar();
                terminar.setIdot(id);
                terminar.setCode(code);
            }

            ordenTrabajoTerminarFragment = new OrdenTrabajoTerminarFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, ordenTrabajoTerminarFragment)
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }

        Bundle bundle = data.getExtras();
        if (resultCode == RESULT_OK && bundle != null) {
            switch (requestCode) {

                case FirmaActivity.REQUEST_ACTION:
                    String path = bundle.getString(FirmaActivity.PATH_FILE);
                    ordenTrabajoTerminarFragment.addPhoto(path);
                    break;

                case GaleriaActivity.REQUEST_ACTION:
                    List<String> files = bundle.getStringArrayList(GaleriaActivity.PATH_FILE);
                    ordenTrabajoTerminarFragment.addPhoto(files);
                    break;
            }
        }
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
        if (ordenTrabajoTerminarFragment != null && terminar != null) {
            ordenTrabajoTerminarFragment.onStart(terminar);
        }
    }

    private void register() {
        if (ordenTrabajoTerminarFragment == null) {
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

        Terminar terminar = ordenTrabajoTerminarFragment.getValue();
        if (terminar == null) {
            Snackbar.make(getView(), R.string.error_terminar, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        String firmaObligatoria = UserParameter.getValue(this, FIRMA_OBLIGATORIA);
        if (firmaObligatoria != null && (firmaObligatoria.equals("1") || firmaObligatoria.equals("1.0"))) {
            boolean firmaDigital = false;
            for (Photo file : terminar.getFiles()) {
                if (file.getName().contains("Firma_Digital")) {
                    firmaDigital = true;
                }
            }

            if (!firmaDigital) {
                Snackbar.make(getView(), R.string.requiere_firma, Snackbar.LENGTH_LONG)
                        .show();
                return;
            }
        }

        Transaccion transaccion = new Transaccion();
        transaccion.setUUID(UUID.randomUUID().toString());
        transaccion.setCuenta(cuenta);
        transaccion.setCreation(Calendar.getInstance().getTime());
        transaccion.setUrl(cuenta.getServidor().getUrl() + "/restapp/app/finishot");
        transaccion.setVersion(cuenta.getServidor().getVersion());
        transaccion.setValue(terminar.toJson());
        transaccion.setModulo(Transaccion.MODULO_ORDEN_TRABAJO);
        transaccion.setAccion(Transaccion.ACCION_TERMINAR_ORDEN_TRABAJO);
        transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);
        transaccion.setIdentidad(terminar.getIdot());

        showProgressDialog();

        compositeDisposable.add(transaccionService.save(transaccion)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(self -> {}, this::onError, this::onComplete));
    }

    private void onComplete() {
        dismissProgressDialog();
        backActivity(getString(R.string.terminar_exitos));
    }

    private void onError(Throwable throwable) {
        Log.e(TAG, "onError: ", throwable);
        dismissProgressDialog();
        Snackbar.make(getView(), R.string.terminar_error, Snackbar.LENGTH_LONG)
                .show();
    }
}