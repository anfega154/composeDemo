package com.mantum.cmms.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;

import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Comentar;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.fragment.SolicitudServicioComentarFragment;
import com.mantum.cmms.service.TransaccionService;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.service.Photo;
import com.mantum.component.service.PhotoAdapter;

import java.io.File;
import java.util.Calendar;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class SolicitudServicioComentarActivity extends Mantum.Activity implements OnCompleteListener {

    public final static int REQUEST_ACTION = 1251;

    public final static String KEY_CODE = "KEY_CODE";

    private final static String TAG = SolicitudServicioComentarActivity.class.getSimpleName();

    private Cuenta cuenta;

    private Database database;

    private long idSolicitudServicio;

    private String codigoSolicitudServicio;

    private TransaccionService transaccionService;

    private SolicitudServicioComentarFragment solicitudServicioComentarFragment;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            database = new Database(this);
            includeBackButtonAndTitle(R.string.registrar_comentario);

            transaccionService = new TransaccionService(this);
            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                idSolicitudServicio = bundle.getLong(Mantum.KEY_ID, -1);
                codigoSolicitudServicio = bundle.getString(KEY_CODE, "");
            }

            solicitudServicioComentarFragment = new SolicitudServicioComentarFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, solicitudServicioComentarFragment)
                    .commit();

        } catch (Exception e) {
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
        if (resultCode == RESULT_OK && bundle != null) {
            switch (requestCode) {
                case GaleriaActivity.REQUEST_ACTION:
                    SparseArray<PhotoAdapter> parcelable = bundle.getSparseParcelableArray(
                            GaleriaActivity.PATH_FILE_PARCELABLE);

                    if (parcelable != null) {
                        solicitudServicioComentarFragment.clearPhoto();
                        int total = parcelable.size();
                        for (int i = 0; i < total; i++) {
                            PhotoAdapter photoAdapter = parcelable.get(i);
                            solicitudServicioComentarFragment.addPhoto(new Photo(this,
                                    new File(photoAdapter.getPath()),
                                    photoAdapter.isDefaultImage(), photoAdapter.getIdCategory(),
                                    photoAdapter.getDescription()));
                        }
                    }
                    break;
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
    int itemId = menuItem.getItemId();
    if (itemId == android.R.id.home) {
        onBackPressed();
    } else if (itemId == R.id.action_done) {
        register();
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
        if (solicitudServicioComentarFragment != null) {
            solicitudServicioComentarFragment.init(codigoSolicitudServicio);
        }
    }

    private void register() {
        this.closeKeyboard();
        if (solicitudServicioComentarFragment == null) {
            Snackbar.make(getView(), R.string.error_app, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        if (idSolicitudServicio < 0) {
            Snackbar.make(getView(), R.string.error_app, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        Comentar comentar = solicitudServicioComentarFragment.getValue();
        if (comentar == null) {
            Snackbar.make(getView(), R.string.error_terminar, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        comentar.setId(idSolicitudServicio);
        comentar.setCodigo(codigoSolicitudServicio);

        Transaccion transaccion = new Transaccion();
        transaccion.setUUID(UUID.randomUUID().toString());
        transaccion.setCuenta(cuenta);
        transaccion.setCreation(Calendar.getInstance().getTime());
        transaccion.setUrl(cuenta.getServidor().getUrl() + "/restapp/app/commentss");
        transaccion.setVersion(cuenta.getServidor().getVersion());
        transaccion.setValue(comentar.toJson());
        transaccion.setModulo(Transaccion.MODULO_SOLICITUD_SERVICIO);
        transaccion.setAccion(Transaccion.ACCION_COMENTAR_SOLICITUD_SERVICIO);
        transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);

        compositeDisposable.add(transaccionService.save(transaccion)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(self -> {
                }, this::onError, this::onComplete));
    }

    private void onComplete() {
        backActivity(getString(R.string.comentar_exitos));
    }

    private void onError(Throwable throwable) {
        Log.e(TAG, "onError: ", throwable);
        Snackbar.make(getView(), R.string.comentar_error, Snackbar.LENGTH_LONG)
                .show();
    }
}