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
import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Contenedor;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.EstadosInspeccion;
import com.mantum.cmms.entity.Seccion;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.factory.SparseArrayTypeAdapterFactory;
import com.mantum.cmms.fragment.FallaFragment;
import com.mantum.cmms.fragment.InspeccionChecklistFragment;
import com.mantum.cmms.fragment.InspeccionPTIFragment;
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

public class InspeccionRegistroPTIActivity extends Mantum.Activity implements OnCompleteListener {

    public static final String ESTADO = "Estado";
    public static final String FECHA = "Fecha";
    public static final String TECNICO = "Tecnico";
    public static final String LINEA_NAVIERA = "Linea naviera";
    public static final String LOCATION = "Location";
    public static final String CODE = "Code";
    public static final String SOFTWARE = "Software";
    public static final String SERIAL = "Serial";
    public static final String FECHA_FABRICACION = "Fecha fabricacion";
    public static final String MARCA = "Marca";
    public static final String ID_MARCA = "ID marca";
    public static final String MODELO = "Modelo";
    public static final String ID_MODELO = "ID modelo";
    public static final String UUID_TRANSACCION = "UUID";
    public static final String MODE_EDIT = "edit";
    public static final String EQUIPMENT_GRADE = "Equipment grade";
    public static final String ID_CLASIFICACION = "ID clasificacion";

    private Cuenta cuenta;
    private Database database;
    private TransaccionService transaccionService;

    private FallaFragment fallaFragment;
    private InspeccionPTIFragment inspeccionPTIFragment;
    private InspeccionChecklistFragment inspeccionChecklistFragment;

    private String key;
    private Long id;
    private String fecha;
    private String estado;
    private String tecnico;
    private String linea;
    private String location;
    private String code;
    private String software;
    private String serial;
    private String fechaFabricacion;
    private Long idmodelo;
    private String modelo;
    private Long idmarca;
    private String marca;
    private String equipmentGrade;
    private String uuid;
    private Contenedor.Request currentValue;
    private Long idclasificacion;

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

            database = new Database(this);
            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                key = bundle.getString(Mantum.KEY_UUID, "");
                id = bundle.getLong(Mantum.KEY_ID, 0);
                estado = bundle.getString(ESTADO, "");
                fecha = bundle.getString(FECHA, "");
                tecnico = bundle.getString(TECNICO, "");
                linea = bundle.getString(LINEA_NAVIERA, "");
                location = bundle.getString(LOCATION, "");
                code = bundle.getString(CODE, "");
                software = bundle.getString(SOFTWARE, "");
                serial = bundle.getString(SERIAL, "");
                fechaFabricacion = bundle.getString(FECHA_FABRICACION, "");
                idmodelo = bundle.getLong(ID_MODELO);
                modelo = bundle.getString(MODELO, "");
                idmarca = bundle.getLong(ID_MARCA);
                marca = bundle.getString(MARCA, "");
                equipmentGrade = bundle.getString(EQUIPMENT_GRADE, "");
                idclasificacion = bundle.getLong(ID_CLASIFICACION, 0);

                String value = bundle.getString(MODE_EDIT, null);
                uuid = bundle.getString(UUID_TRANSACCION, null);
                if (value != null) {
                    currentValue = gson.fromJson(value, Contenedor.Request.class);
                    id = currentValue.getIdinspeccion();
                }
            }

            fallaFragment = new FallaFragment();
            inspeccionPTIFragment = new InspeccionPTIFragment().setKey(key);
            inspeccionChecklistFragment = new InspeccionChecklistFragment();

            transaccionService = new TransaccionService(this);

            TabAdapter tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                    Arrays.asList(inspeccionPTIFragment, inspeccionChecklistFragment, fallaFragment));

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            includeBackButtonAndTitle(R.string.generar_inspeccion);

            ViewPager viewPager = findViewById(R.id.viewPager);
            viewPager.setAdapter(tabAdapter);
            viewPager.setOffscreenPageLimit(tabAdapter.getCount() - 1);

            TabLayout tabLayout = findViewById(R.id.tabs);
            tabLayout.setTabMode(TabLayout.MODE_FIXED);
            tabLayout.setupWithViewPager(viewPager);
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    InspeccionRegistroPTIActivity.this.closeKeyboard();
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }

            });

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
            case InspeccionPTIFragment.KEY_TAB: {
                if (inspeccionPTIFragment != null) {
                    Contenedor.Request request = new Contenedor.Request();
                    request.setFechaultimopti(fecha);
                    request.setTecnico(tecnico);
                    request.setEstado(estado);
                    request.setLineaNaviera(linea);
                    request.setYardainspeccion(location);
                    request.setSoftware(software);
                    request.setSerial(serial);
                    request.setFechafabricacion(fechaFabricacion);
                    request.setMarca(marca);
                    request.setIdmarca(idmarca);
                    request.setIdmodelo(idmodelo);
                    request.setModelo(modelo);
                    request.setCodigo(code);
                    request.setEquipmentgrade(equipmentGrade);
                    request.setIdclasificacion(idclasificacion);

                    inspeccionPTIFragment.setKey(key)
                            .init(request);

                    if (currentValue != null) {
                        inspeccionPTIFragment.onLoad(currentValue);
                    }
                }
                break;
            }

            case InspeccionChecklistFragment.KEY_TAB: {
                if (inspeccionChecklistFragment != null && currentValue != null) {
                    if (currentValue.getPreguntas() != null) {
                        inspeccionChecklistFragment.onLoad(currentValue.getPreguntas());
                    }
                }
                break;
            }

            case FallaFragment.KEY_TAB: {
                if (fallaFragment != null && currentValue != null) {
                    if (currentValue.getFallas() != null) {
                        fallaFragment.onLoad(false, currentValue.getFallas());
                    }
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
        if (inspeccionPTIFragment == null || inspeccionChecklistFragment == null) {
            return;
        }

        Contenedor.Request request = inspeccionPTIFragment.getValue();
        if (request == null) {
            Snackbar.make(getView(), R.string.verificar_formulario, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        if (request.isRequiereFalla() && fallaFragment.getValue().isEmpty()) {
            Snackbar.make(getView(), R.string.requiere_falla, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        request.setKey(key);
        request.setIdinspeccion(id);
        request.setFallas(fallaFragment.getValue());
        request.setAbrirOT(fallaFragment.isOpenOT());
        request.setFecharegistro(Tool.datetime(new Date()));
        request.setPreguntas(inspeccionChecklistFragment.getValue());

        if (request.isRequiereValidacion()) {
            if ("Con novedad".equals(request.getNovedad()) && fallaFragment.getValue().isEmpty()) {
                Snackbar.make(getView(), R.string.requiere_falla, Snackbar.LENGTH_LONG)
                        .show();
                return;
            }

            EstadosInspeccion estadosInspeccion = database.where(EstadosInspeccion.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .equalTo("id", request.getIdestadoregistro())
                    .findFirst();

            if (request.getPreguntas().isEmpty()) {
                if (estadosInspeccion == null || estadosInspeccion.isValidate()) {
                    Snackbar.make(getView(), R.string.checklist_requerido, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }
            }

            boolean isValid = true;
            for (Seccion.Pregunta pregunta : request.getPreguntas()) {
                if (!pregunta.isValid()) {
                    isValid = false;
                    break;
                }
            }

            if (!isValid) {
                Snackbar.make(getView(), R.string.checklist_requerido, Snackbar.LENGTH_LONG)
                        .show();
                return;
            }
        }

        Transaccion transaccion = new Transaccion();
        transaccion.setUUID(uuid != null ? uuid : UUID.randomUUID().toString());
        transaccion.setCuenta(cuenta);
        transaccion.setCreation(Calendar.getInstance().getTime());
        transaccion.setUrl(cuenta.getServidor().getUrl() + "/restapp/app/ejecutarinspeccion");
        transaccion.setVersion(cuenta.getServidor().getVersion());
        transaccion.setValue(request.toJson());
        transaccion.setModulo(Transaccion.MODULO_INSPECCION);
        transaccion.setAccion(Transaccion.ACCION_REGISTRAR_PTI);
        transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);
        transaccion.setInformation(code + " - " + location);

        compositeDisposable.add(transaccionService.save(transaccion)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNext, this::onError, this::onComplete));
    }

    private void onNext(List<Transaccion> transaccions) {
    }

    private void onComplete() {
        backActivity(getString(R.string.registrar_pti_exitoso));
    }

    private void onError(Throwable throwable) {
        Snackbar.make(getView(), R.string.registrar_pti_error, Snackbar.LENGTH_LONG)
                .show();
    }
}
