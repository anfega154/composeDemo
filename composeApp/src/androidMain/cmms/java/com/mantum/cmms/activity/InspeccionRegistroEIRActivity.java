package com.mantum.cmms.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Contenedor;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.factory.SparseArrayTypeAdapterFactory;
import com.mantum.cmms.fragment.DamageFragment;
import com.mantum.cmms.fragment.InspeccionEIRFragment;
import com.mantum.cmms.service.TransaccionService;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.adapter.TabAdapter;
import com.mantum.component.util.Tool;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class InspeccionRegistroEIRActivity extends Mantum.Activity implements OnCompleteListener {

    public static final String ESTADO = "Estado";
    public static final String FECHA = "Fecha";
    public static final String TECNICO = "Tecnico";
    public static final String LINEA_NAVIERA = "Linea naviera";
    public static final String TIPO = "Tipo";
    public static final String LOCATION = "Location";
    public static final String CODE = "Code";

    public static final String UUID_TRANSACCION = "UUID";
    public static final String MODE_EDIT = "edit";

    private Cuenta cuenta;
    private Database database;
    private TransaccionService transaccionService;

    private DamageFragment damageFragment;
    private InspeccionEIRFragment inspeccionEIRFragment;

    private String key;
    private Long id;
    private String fecha;
    private String tecnico;
    private String linea;
    private String code;
    private String location;
    private String tipo;
    private String uuid;
    private Contenedor.Request currentValue;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .registerTypeAdapterFactory(SparseArrayTypeAdapterFactory.INSTANCE)
            .create();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_inspeccion_registro);

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                key = bundle.getString(Mantum.KEY_UUID, "");
                id = bundle.getLong(Mantum.KEY_ID, 0);
                fecha = bundle.getString(FECHA, "");
                tecnico = bundle.getString(TECNICO, "");
                linea = bundle.getString(LINEA_NAVIERA, "");
                tipo = bundle.getString(TIPO, "");
                code = bundle.getString(CODE, "");
                location = bundle.getString(LOCATION, "");

                String value = bundle.getString(MODE_EDIT, null);
                uuid = bundle.getString(UUID_TRANSACCION, null);
                if (value != null) {
                    currentValue = gson.fromJson(value, Contenedor.Request.class);
                    id = currentValue.getIdinspeccion();
                }
            }

            transaccionService = new TransaccionService(this);

            database = new Database(this);
            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            damageFragment = new DamageFragment();
            inspeccionEIRFragment = new InspeccionEIRFragment().setKey(key);
            TabAdapter tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                    Arrays.asList(inspeccionEIRFragment, damageFragment));

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            includeBackButtonAndTitle(R.string.generar_inspeccion);

            ViewPager viewPager = findViewById(R.id.viewPager);
            viewPager.setAdapter(tabAdapter);
            viewPager.setOffscreenPageLimit(tabAdapter.getCount() - 1);

            TabLayout tabLayout = findViewById(R.id.tabs);
            tabLayout.setTabMode(TabLayout.MODE_FIXED);
            tabLayout.setupWithViewPager(viewPager);
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            backActivity(getString(R.string.error_app));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }

        compositeDisposable.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_registrar_inspeccion, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onComplete(@NonNull String name) {
        switch (name) {
            case InspeccionEIRFragment.KEY_TAB: {
                if (inspeccionEIRFragment != null) {
                    inspeccionEIRFragment.setKey(key)
                            .init(fecha, tecnico, linea, tipo, code);

                    if (currentValue != null) {
                        inspeccionEIRFragment.onLoad(currentValue);
                    }
                }
                break;
            }

            case DamageFragment.KEY_TAB: {
                if (damageFragment != null && currentValue != null) {
                    damageFragment.onLoad(currentValue.getDamages());
                }
                break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
        } else if (itemId == R.id.send) {
            send();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        if (currentValue != null) {
            super.backActivity();
            return;
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setTitle(R.string.cancelar_inspeccion);
        alertDialogBuilder.setMessage(R.string.cancelar_inspeccion_mensaje);
        alertDialogBuilder.setNegativeButton(R.string.salir, (dialog, id) -> super.backActivity());
        alertDialogBuilder.setPositiveButton(R.string.continuar, (dialog, id) -> dialog.dismiss());
        alertDialogBuilder.show();
    }

    private void send() {
        if (inspeccionEIRFragment == null) {
            return;
        }

        Contenedor.Request request = inspeccionEIRFragment.getValue();
        if (request == null) {
            Snackbar.make(getView(), R.string.verificar_formulario, Snackbar.LENGTH_LONG).show();
            return;
        }

        if (request.isRequiereFalla() && damageFragment.getValue().isEmpty()) {
            Snackbar.make(getView(), R.string.requiere_damage, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        request.setKey(key);
        request.setIdinspeccion(id);
        request.setFecharegistro(Tool.datetime(new Date()));
        request.setDamages(damageFragment.getValue());

        Transaccion transaccion = new Transaccion();
        transaccion.setUUID(uuid != null ? uuid : UUID.randomUUID().toString());
        transaccion.setCuenta(cuenta);
        transaccion.setCreation(Calendar.getInstance().getTime());
        transaccion.setUrl(cuenta.getServidor().getUrl() + "/restapp/app/ejecutarinspeccion");
        transaccion.setVersion(cuenta.getServidor().getVersion());
        transaccion.setValue(request.toJson());
        transaccion.setModulo(Transaccion.MODULO_INSPECCION);
        transaccion.setAccion(Transaccion.ACCION_REGISTRAR_EIR);
        transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);
        transaccion.setInformation(code + " - " + location);

        compositeDisposable.add(transaccionService.save(transaccion)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNext, this::onError, this::onComplete));
    }

    private void onNext(List<Transaccion> transaccions) {
    }

    private void onComplete() {
        backActivity(getString(R.string.registrar_eir_exitoso));
    }

    private void onError(Throwable throwable) {
        Snackbar.make(getView(), R.string.registrar_eir_error, Snackbar.LENGTH_LONG)
                .show();
    }
}