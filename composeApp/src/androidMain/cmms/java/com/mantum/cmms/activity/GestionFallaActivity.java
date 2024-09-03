package com.mantum.cmms.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.MenuItem;

import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Falla;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.cmms.fragment.BitacoraFallaFragment;
import com.mantum.cmms.fragment.GestionFallaListaFragment;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

import java.util.Objects;

public class GestionFallaActivity extends Mantum.Activity implements OnCompleteListener {

    public static final String KEY_FORM = "key_falla_form";

    private Database database;

    private GestionFallaListaFragment gestionFallaListaFragment;

    private BitacoraFallaFragment bitacoraFallaFragment;

    private Falla.Request falla;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_gestion_falla);
            includeBackButtonAndTitle(R.string.gestionar_falla_titulo);

            database = new Database(this);
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                falla = (Falla.Request) bundle.getSerializable(KEY_FORM);
                int actionClicked = bundle.getInt("actionClicked");

                OrdenTrabajo ordentrabajo = database.where(OrdenTrabajo.class)
                        .equalTo("id", bundle.getLong(Mantum.KEY_ID))
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();

                if (ordentrabajo != null) {
                    if (ordentrabajo.getFallas().size() == 1) {
                        String uuidFalla = Objects.requireNonNull(ordentrabajo.getFallas().get(0)).getUUID();
                        bundle.putString(Mantum.KEY_UUID, uuidFalla);
                        bitacoraFallaFragment = new BitacoraFallaFragment();
                        bitacoraFallaFragment.setArguments(bundle);
                        setFragment(bitacoraFallaFragment);
                    } else if (ordentrabajo.getFallas().size() > 1) {
                        if (falla != null) {
                            switch (actionClicked) {
                                case 1:
                                    bundle.putString(Mantum.KEY_UUID, falla.getUUID());
                                    bitacoraFallaFragment = new BitacoraFallaFragment();
                                    bitacoraFallaFragment.setArguments(bundle);
                                    setFragment(bitacoraFallaFragment);
                                    break;

                                case 2:
                                    gestionFallaListaFragment = new GestionFallaListaFragment();
                                    bundle.putSerializable(KEY_FORM, null);
                                    gestionFallaListaFragment.setArguments(bundle);
                                    setFragment(gestionFallaListaFragment);
                                    break;

                                case 3:
                                    gestionFallaListaFragment = new GestionFallaListaFragment();
                                    gestionFallaListaFragment.setArguments(bundle);
                                    setFragment(gestionFallaListaFragment);
                                    break;
                            }
                        } else {
                            gestionFallaListaFragment = new GestionFallaListaFragment();
                            gestionFallaListaFragment.setArguments(bundle);
                            setFragment(gestionFallaListaFragment);
                        }
                    }
                }
            }

        } catch (Exception e) {
            Log.e("TAG", "onCreate: ", e);
        }
    }

    private void setFragment(Mantum.Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        Falla.Request fallaRequest = new Falla.Request();

        if (bitacoraFallaFragment != null) {
            fallaRequest = bitacoraFallaFragment.getValue();
            if (fallaRequest == null) {
                return;
            }
        } else if (gestionFallaListaFragment != null) {
            fallaRequest = gestionFallaListaFragment.getValue();
            if (fallaRequest == null && gestionFallaListaFragment.getBitacoraFallaFragment() != null) {
                return;
            }
        }
        backActions(fallaRequest);
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
        if (bitacoraFallaFragment != null) {
            bitacoraFallaFragment.onActivityResult(requestCode, resultCode, data);
        } else if (gestionFallaListaFragment != null) {
            gestionFallaListaFragment.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onComplete(@NonNull String name) {
        if (bitacoraFallaFragment != null && falla != null) {
            bitacoraFallaFragment.onStart(falla);
        }
    }

    private void backActions(@Nullable Falla.Request fallaRequest) {
        Intent intent = new Intent();

        if (fallaRequest != null) {
            boolean listadoRepuestosVacio = fallaRequest.isRequiererepuesto() && fallaRequest.getRepuestos() == null;
            boolean listadoFotosVacio = fallaRequest.isRequierefoto() && fallaRequest.getImagenesPrevias() == null && fallaRequest.getImagenesPosteriores() == null;

            if (listadoRepuestosVacio && listadoFotosVacio) {
                new AlertDialog.Builder(this)
                        .setTitle("Repuesto y foto requeridos")
                        .setMessage("La gama asociada a esta falla requiere al menos un repuesto y una foto.")
                        .setPositiveButton("Cerrar", (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(KEY_FORM, fallaRequest);
                            intent.putExtras(bundle);
                            backActivity(intent);
                        })
                        .setNegativeButton("Agregar", null)
                        .show();

                return;
            }

            if (listadoRepuestosVacio) {
                new AlertDialog.Builder(this)
                        .setTitle("Repuesto requerido")
                        .setMessage("La gama asociada a esta falla requiere al menos un repuesto.")
                        .setPositiveButton("Cerrar", (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(KEY_FORM, fallaRequest);
                            intent.putExtras(bundle);
                            backActivity(intent);
                        })
                        .setNegativeButton("Agregar repuesto", null)
                        .show();

                return;
            }

            if (listadoFotosVacio) {
                new AlertDialog.Builder(this)
                        .setTitle("Foto requerida")
                        .setMessage("La gama asociada a esta falla requiere al menos una foto.")
                        .setPositiveButton("Cerrar", (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(KEY_FORM, fallaRequest);
                            intent.putExtras(bundle);
                            backActivity(intent);
                        })
                        .setNegativeButton("Agregar foto", null)
                        .show();

                return;
            }
        }

        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_FORM, fallaRequest);
        intent.putExtras(bundle);
        backActivity(intent);
    }
}