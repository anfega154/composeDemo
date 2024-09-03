package com.mantum.cmms.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.fragment.FormularioEquipoFragment;
import com.mantum.cmms.helper.TransaccionHelper;

import static com.mantum.cmms.security.Security.TAG;

public class FormularioEquipoActivity extends TransaccionHelper.Dialog {

    public static final String UUID_TRANSACCION = "UUID";

    public static final String MODE_EDIT = "edit";

    private FormularioEquipoFragment formularioEquipoFragment;

    boolean crearEquipo;

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_formulario_equipo);

            formularioEquipoFragment = new FormularioEquipoFragment();

            Database database = new Database(this);
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                crearEquipo = bundle.getBoolean("crearEquipo");
                formularioEquipoFragment.setArguments(bundle);

                if (crearEquipo) {
                    includeBackButtonAndTitle(R.string.crear_equipo_titulo_crear);
                } else {
                    includeBackButtonAndTitle(R.string.crear_equipo_titulo_editar);
                }

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, formularioEquipoFragment)
                        .addToBackStack(null)
                        .commit();
            }
        } catch (Exception e) {
            Log.e(TAG, "onCreate: ", e);
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("¿Estás seguro?")
                .setMessage("Se perderán los datos ingresados.")
                .setPositiveButton("Cancelar", (dialogInterface, i) -> dialogInterface.cancel())
                .setNegativeButton("Aceptar", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    finish();
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_formulario, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_done:
                boolean disableButton = formularioEquipoFragment.enviarEquipo();
                menu.findItem(R.id.action_done).setEnabled(!disableButton);
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }
}