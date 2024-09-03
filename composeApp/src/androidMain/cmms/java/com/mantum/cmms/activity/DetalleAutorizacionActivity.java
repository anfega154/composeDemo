package com.mantum.cmms.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import android.util.Log;
import android.view.MenuItem;

import com.mantum.demo.R;
import com.mantum.cmms.view.AutorizacionView;
import com.mantum.component.Mantum;

public class DetalleAutorizacionActivity extends Mantum.Activity {

    private static final String TAG = DetalleAutorizacionActivity.class.getSimpleName();

    public static final String FORM_VALUES = "form_value";

    public static final int REQUEST_VALUE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_detalle_autorizacion_acceso_activity);

            includeBackButtonAndTitle(R.string.detalle_autorizacion_acceso);
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                throw new Exception("Debe de incluir el formulario que desea mostrar");
            }

            AutorizacionView autorizaciones
                    = (AutorizacionView) extras.get(FORM_VALUES);
            if (autorizaciones == null) {
                throw new Exception("No fue posible obtener la información de la autorización");
            }

            TextInputEditText code = findViewById(R.id.code);
            code.setText(autorizaciones.getCodigo());

            TextInputEditText dateStart = findViewById(R.id.date_start);
            dateStart.setText(autorizaciones.getFechainicio());

            TextInputEditText dateEnd = findViewById(R.id.date_end);
            dateEnd.setText(autorizaciones.getFechafin());

            TextInputEditText type = findViewById(R.id.type);
            type.setText(autorizaciones.getTipo());

            TextInputEditText description = findViewById(R.id.description);
            description.setText(autorizaciones.getDescripcion());

            TextInputEditText location = findViewById(R.id.location);
            location.setText(autorizaciones.getLocacion());

            TextInputEditText company = findViewById(R.id.company);
            company.setText(autorizaciones.getEmpresa());

            TextInputEditText store = findViewById(R.id.store);
            store.setText(autorizaciones.getMarca());

        } catch (Exception e) {
            backActivity(e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home :
                super.onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }
}