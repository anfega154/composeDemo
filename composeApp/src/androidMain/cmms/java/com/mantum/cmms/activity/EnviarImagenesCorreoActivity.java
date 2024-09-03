package com.mantum.cmms.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.helper.ImagenesCorreoHelper;
import com.mantum.cmms.helper.TransaccionHelper;
import com.mantum.cmms.service.TransaccionService;
import com.mantum.component.Mantum;
import com.mantum.component.service.Photo;
import com.mantum.component.service.PhotoAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

import static com.mantum.cmms.security.Security.TAG;

public class EnviarImagenesCorreoActivity extends TransaccionHelper.Dialog {

    private EditText codigoContenedor;

    private List<Photo> photosList;

    private Cuenta cuenta;

    private TransaccionService transaccionService;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enviar_imagenes_correo);
        includeBackButtonAndTitle(R.string.enviar_correo);

        photosList = new ArrayList<>();
        Database database = new Database(this);
        transaccionService = new TransaccionService(this);
        cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        codigoContenedor = findViewById(R.id.codigo_contenedor);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            codigoContenedor.setText(bundle.getString(Mantum.KEY_ID));
        }

        FloatingActionButton imagenes = findViewById(R.id.imagenes);
        imagenes.setOnClickListener(view -> {
            Bundle bundle1 = new Bundle();
            bundle1.putSparseParcelableArray(GaleriaActivity.PATH_FILE_PARCELABLE, PhotoAdapter.factory(photosList));
            bundle1.putLong(GaleriaActivity.KEY_LIMITE_PESO_ARCHIVOS, 8000000);

            Intent intent = new Intent(view.getContext(), GaleriaActivity.class);
            intent.putExtras(bundle1);
            startActivityForResult(intent, GaleriaActivity.REQUEST_ACTION);
        });
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
                enviarCorreo();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void enviarCorreo() {
        TextInputLayout contentCodigoContenedor = findViewById(R.id.content_codigo_contenedor);
        contentCodigoContenedor.setError(null);

        if (codigoContenedor.getText().toString().isEmpty()) {
            contentCodigoContenedor.setError(getString(R.string.codigo_contenedor_vacio));
            return;
        }

        if (photosList.isEmpty()) {
            Snackbar.make(getView(), getString(R.string.listado_imagenes_vacio), Snackbar.LENGTH_LONG).show();
            return;
        }

        ImagenesCorreoHelper imagenesCorreoHelper = new ImagenesCorreoHelper();
        imagenesCorreoHelper.setCodigoContenedor(codigoContenedor.getText().toString());

        if (!photosList.isEmpty()) {
            imagenesCorreoHelper.setImagenes(photosList);
        }

        Transaccion transaccion = new Transaccion();
        transaccion.setUUID(UUID.randomUUID().toString());
        transaccion.setCuenta(cuenta);
        transaccion.setCreation(Calendar.getInstance().getTime());
        transaccion.setUrl(cuenta.getServidor().getUrl() + "/restapp/app/sendimagesbyemail");
        transaccion.setVersion(cuenta.getServidor().getVersion());
        transaccion.setValue(imagenesCorreoHelper.toJson());
        transaccion.setModulo(Transaccion.MODULO_CORREO);
        transaccion.setAccion(Transaccion.ACCION_ENVIAR_CORREO);
        transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);

        showProgressDialog();

        compositeDisposable.add(transaccionService.save(transaccion)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(transaccions -> {}, throwable -> {
                    dismissProgressDialog();
                    Log.e(TAG, "create: ", throwable);
                    Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG).show();
                }, () -> {
                    dismissProgressDialog();
                    finish();
                }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data != null && data.getExtras() != null) {
            Bundle bundle = data.getExtras();
            if (resultCode == RESULT_OK) {
                SparseArray<PhotoAdapter> parcelable = bundle.getSparseParcelableArray(GaleriaActivity.PATH_FILE_PARCELABLE);
                if (parcelable != null) {
                    photosList.clear();
                    int total = parcelable.size();
                    if (total > 0) {
                        for (int i = 0; i < total; i++) {
                            PhotoAdapter photoAdapter = parcelable.get(i);
                            photosList.add(new Photo(this, new File(photoAdapter.getPath()),
                                    photoAdapter.isDefaultImage(), photoAdapter.getIdCategory(),
                                    photoAdapter.getDescription()));
                        }
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}