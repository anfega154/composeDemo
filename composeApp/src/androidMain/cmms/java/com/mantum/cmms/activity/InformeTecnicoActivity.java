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
import com.mantum.cmms.entity.InformeTecnico;
import com.mantum.cmms.entity.RecursoAdicional;
import com.mantum.cmms.fragment.InformeTecnicoFragment;
import com.mantum.cmms.helper.RecursoAdicionalHelper;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.SolicitudServicio;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.service.TransaccionService;
import com.mantum.cmms.util.Preferences;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;
import io.realm.RealmList;

public class InformeTecnicoActivity extends Mantum.Activity
        implements OnCompleteListener {

    public static final String KEY_CODE = "codigo";

    public static final int REQUEST_ACTION = 1297;

    private Realm realm;

    private InformeTecnico informeTecnico;

    private RecursoAdicionalHelper recursoHelper;

    private TransaccionService transaccionService;

    private RealmList<RecursoAdicional> recursosAdicionales;

    private InformeTecnicoFragment informeTecnicoFragment;

    private static final String TAG = InformeTecnicoActivity.class.getSimpleName();

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            realm = new Database(this).instance();
            includeBackButtonAndTitle(R.string.informe_tecnico);

            recursoHelper = new RecursoAdicionalHelper();
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
                SolicitudServicio solicitudServicio = realm.where(SolicitudServicio.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .equalTo("id", id)
                        .findFirst();

                if (solicitudServicio == null) {
                    throw new Exception("La solicitud de servicio con id " + id + " no existe");
                }

                recursosAdicionales = solicitudServicio.getRecursosadicionales();
                informeTecnico = solicitudServicio.getInformeTecnico();
                if (informeTecnico == null) {
                    informeTecnico = new InformeTecnico();
                    informeTecnico.setIdss(id);
                    informeTecnico.setCodigo(bundle.getString(KEY_CODE));
                }
            }

            informeTecnicoFragment = new InformeTecnicoFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, informeTecnicoFragment)
                    .commit();

        } catch (Exception e) {
            backActivity(getString(R.string.error_app));
        }
    }

    @Override
    public void onComplete(@NonNull String name) {
        if (informeTecnicoFragment != null && informeTecnico != null) {
            InformeTecnico value = informeTecnico.isManaged()
                    ? realm.copyFromRealm(informeTecnico)
                    : informeTecnico;

            informeTecnicoFragment.onStart(value);
            if (recursosAdicionales != null) {
                List<RecursoAdicional> values = recursosAdicionales.isManaged()
                        ? realm.copyFromRealm(recursosAdicionales)
                        : recursosAdicionales;

                informeTecnicoFragment.setRecursoAdicional(RecursoAdicionalHelper.adapter(values));
            }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RecursosAdicionalesActivity.REQUEST_ACTION:
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        this.recursoHelper = (RecursoAdicionalHelper) bundle.getSerializable(
                                RecursosAdicionalesActivity.KEY_RESOURCES);
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void register() {
        closeKeyboard();
        if (informeTecnicoFragment == null) {
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

        InformeTecnico.Request estadoInicial = informeTecnicoFragment.getValue();
        if (estadoInicial == null) {
            Snackbar.make(getView(), R.string.informe_tecnico_error_formulario, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        estadoInicial.setRecursosAdicionales(recursoHelper.getRecursos());

        String url = Preferences.url(this, "restapp/app/savefichatecnica");
        Transaccion transaccion = new Transaccion();
        transaccion.setUUID(UUID.randomUUID().toString());
        transaccion.setCuenta(cuenta);
        transaccion.setCreation(Calendar.getInstance().getTime());
        transaccion.setUrl(url);
        transaccion.setVersion(cuenta.getServidor().getVersion());
        transaccion.setValue(estadoInicial.toJson());
        transaccion.setModulo(Transaccion.MODULO_SOLICITUD_SERVICIO);
        transaccion.setAccion(Transaccion.ACCION_INFORME_TECNICO);
        transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);

        compositeDisposable.add(transaccionService.save(transaccion)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(self -> {}, this::onError, this::onComplete));
    }

    private void onComplete() {
        backActivity(getString(R.string.informe_tecnico_exito));
    }

    private void onError(Throwable throwable) {
        Snackbar.make(getView(), R.string.informe_tecnico_error, Snackbar.LENGTH_LONG)
                .show();
    }
}