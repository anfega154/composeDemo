package com.mantum.cmms.activity;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.cmms.entity.RutaTrabajo;
import com.mantum.cmms.fragment.RutaTrabajoFragment;
import com.mantum.cmms.service.RutaTrabajoService;
import com.mantum.cmms.view.RutaTrabajoView;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class DescargarRutaTrabajoActivity extends Mantum.Activity implements OnCompleteListener {

    private final String TAG = DescargarRutaTrabajoActivity.class.getSimpleName();

    public static final int REQUEST_ACTION = 1201;

    public static final String ID_EXTRA = "id_extra";

    public static final String ARRAY_ACTIVIDADES = "id_actividades";

    public static final String MODO_VER_DETALLE = "accion_actualizar";

    public static final String ACCION_REFRESCAR = "ACCION_REFRESCAR";

    public static final String ACCION_PARCIAL = "accion_parcial";

    private long idExtra;

    private Cuenta cuenta;

    private Database database;

    private ProgressDialog progressDialog;

    private RutaTrabajoService rutaTrabajoService;

    private RutaTrabajoFragment rutaTrabajoFragment;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private boolean refrescar = true;

    private boolean parcial = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            database = new Database(this);
            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            boolean modoVerDetalle = true;
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                idExtra = bundle.getLong(ID_EXTRA, -1);
                modoVerDetalle = bundle.getBoolean(MODO_VER_DETALLE, true);
                refrescar = bundle.getBoolean(ACCION_REFRESCAR, true);
                parcial = bundle.getBoolean(ACCION_PARCIAL, false);
            }

            includeBackButtonAndTitle(!parcial ? R.string.ruta_trabajo : R.string.lista_chequeo);

            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(getString(R.string.descargando));
            progressDialog.setMessage(getString(R.string.descargando_ruta_trabajo));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);

            rutaTrabajoFragment = new RutaTrabajoFragment();
            rutaTrabajoFragment.setIdExtra(idExtra);
            rutaTrabajoFragment.setAccionActualizar(false);
            rutaTrabajoFragment.setRealizarPeticionHttp(false);
            rutaTrabajoFragment.setModoVerDetalle(modoVerDetalle);
            rutaTrabajoFragment.setParcial(parcial);

            rutaTrabajoService = new RutaTrabajoService(this, cuenta);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, rutaTrabajoFragment)
                    .commit();

        } catch (Exception e) {
            Log.d(TAG, "onCreate: " + e);
            backActivity(getString(R.string.error_app));
        }
    }

    private void onNext(List<RutaTrabajo> values) {
        if (rutaTrabajoFragment != null) {
            rutaTrabajoFragment.informationAdapter.addAll(RutaTrabajoView.factory(values), false);
        }
    }

    private void onError(Throwable throwable) {
        Log.e(TAG, "onError: ", throwable);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                .show();
    }

    private void onComplete() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        if (rutaTrabajoFragment != null && rutaTrabajoFragment.informationAdapter != null) {
            rutaTrabajoFragment.informationAdapter.notifyDataSetChanged();
            rutaTrabajoFragment.informationAdapter.showMessageEmpty(getView());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actualizar, menu);
        menu.findItem(R.id.action_refresh).setVisible(refrescar);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;

            case R.id.action_refresh:
                request();
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

        if (rutaTrabajoService != null) {
            rutaTrabajoService.close();
            rutaTrabajoService.cancel();
        }

        compositeDisposable.clear();
    }

    @Override
    public void onComplete(@NonNull String name) {
        if (cuenta == null) {
            return;
        }

        OrdenTrabajo resultado = database.where(OrdenTrabajo.class)
                .equalTo("id", idExtra)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .findFirst();

        if (resultado == null) {
            return;
        }

        List<RutaTrabajo> values = database.copyFromRealm(resultado.getListachequeo());

        if (rutaTrabajoFragment != null && rutaTrabajoFragment.informationAdapter != null) {
            rutaTrabajoFragment.informationAdapter.addAll(RutaTrabajoView.factory(values));
            rutaTrabajoFragment.informationAdapter.showMessageEmpty(getView());
        }
    }

    private void request() {
        progressDialog.show();
        compositeDisposable.add(rutaTrabajoService.download()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::onShow)
                .flatMap(this::onSave)
                .subscribe(this::onNext, this::onError, this::onComplete));
    }

    @NonNull
    private RutaTrabajo.Request onShow(@NonNull RutaTrabajo.Request request) {
        for (RutaTrabajo rutaTrabajo : request.getPendientes()) {
            rutaTrabajo.setShow(true);
        }
        return request;
    }

    @NonNull
    private Observable<List<RutaTrabajo>> onSave(@NonNull RutaTrabajo.Request request) {
        if (request.getTab() == null || request.getTab().getIds() == null) {
            return rutaTrabajoService.save(request.getPendientes());
        }

        Long[] value = request.getTab().getIds()
                .toArray(new Long[]{});

        return rutaTrabajoService.removeById(value)
                .map(this::onRemove)
                .flatMap(rutas -> rutaTrabajoService.save(request.getPendientes()));
    }

    private List<Long> onRemove(@NonNull List<Long> remove) {
        for (Long id : remove) {
            rutaTrabajoFragment.informationAdapter.remove(value -> value.getId().equals(id));
        }
        return remove;
    }
}
