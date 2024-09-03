package com.mantum.cmms.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.fragment.FormularioFallaInstalacionLocativaFragment;
import com.mantum.cmms.util.BackEditTransaction;
import com.mantum.component.Mantum;

public class FormularioFallaInstalacionLocativaActivity extends Mantum.Activity {

    public static final String UUID_TRANSACCION = "UUID";

    public static final String MODE_EDIT = "edit";

    private Database database;

    private FormularioFallaInstalacionLocativaFragment formularioFallaInstalacionLocativaFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_formulario_falla);
            includeBackButtonAndTitle(R.string.registrar_falla);

            database = new Database(this);
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            Bundle bundle = getIntent().getExtras();
            formularioFallaInstalacionLocativaFragment = new FormularioFallaInstalacionLocativaFragment();
            formularioFallaInstalacionLocativaFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, formularioFallaInstalacionLocativaFragment)
                    .commit();
        } catch (Exception e) {
            Log.e("TAG", "onCreate: ", e);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
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
                Bundle bundle = getIntent().getExtras();
                if (bundle != null && bundle.getString(MODE_EDIT) != null) {
                    BackEditTransaction.backDialog(this);
                } else {
                    onBackPressed();
                }
                break;

            case R.id.action_done:
                if (formularioFallaInstalacionLocativaFragment != null) {
                    formularioFallaInstalacionLocativaFragment.create();
                }
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (formularioFallaInstalacionLocativaFragment != null) {
            formularioFallaInstalacionLocativaFragment.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}