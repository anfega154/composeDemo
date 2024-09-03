package com.mantum.cmms.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mantum.R;
import com.mantum.cmms.entity.Devolucion;
import com.mantum.cmms.fragment.DevolucionFragment;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

public class DevolucionActivity extends Mantum.Activity implements OnCompleteListener {

    private DevolucionFragment devolucionFragment;

    private SparseArray<Devolucion> devoluciones;

    public static final String DEVOLUCIONES_PARCELABLE = "devoluciones_parcelable";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            includeBackButtonAndTitle(R.string.tab_devolucion);

            devolucionFragment = new DevolucionFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, devolucionFragment)
                    .commit();

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                devoluciones = bundle.getSparseParcelableArray(DEVOLUCIONES_PARCELABLE);
            }
        } catch (Exception e) {
            backActivity(getString(R.string.error_app));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_devolucion, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (menuItem.getItemId() == R.id.agregar) {
            View form = View.inflate(this, R.layout.form_devolucion, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(form);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.aceptar, (dialog, which) -> {
                TextInputEditText codigo = form.findViewById(R.id.codigo);
                TextInputEditText cantidad = form.findViewById(R.id.cantidad);
                TextInputEditText descripcion = form.findViewById(R.id.descripcion);

                if (codigo.getText() == null || codigo.getText().toString().isEmpty()) {
                    Snackbar.make(getView(), R.string.requiere_codigo, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                if (cantidad.getText() == null || cantidad.getText().toString().isEmpty()) {
                    Snackbar.make(getView(), R.string.requiere_cantidad, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                Devolucion devolucion = new Devolucion();
                devolucion.setCodigo(codigo.getText().toString());
                devolucion.setCantidad(cantidad.getText().toString());
                if (descripcion.getText() != null) {
                    devolucion.setDescripcion(descripcion.getText().toString());
                }

                if (devolucionFragment != null) {
                    devolucionFragment.add(devolucion);
                }
            });

            builder.setNegativeButton(R.string.cancelar, (dialog, which) -> dialog.dismiss());
            builder.create();
            builder.show();
            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onComplete(@NonNull String name) {
        if (devolucionFragment != null && devoluciones != null) {
            devolucionFragment.add(devoluciones);
        }
    }

    @Override
    public void onBackPressed() {
        Bundle bundle = new Bundle();
        bundle.putSparseParcelableArray(DEVOLUCIONES_PARCELABLE, devolucionFragment.getOriginal());

        Intent intent = new Intent();
        intent.putExtras(bundle);
        backActivity(intent);
    }
}