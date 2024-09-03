package com.mantum.cmms.activity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.BusquedaAvanzada;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.domain.Resultado;
import com.mantum.cmms.entity.Familia;
import com.mantum.cmms.service.BusquedaServices;
import com.mantum.cmms.service.CaptureGeolocationService;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.service.Geolocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class BusquedaAvanzadaActivity extends Mantum.Activity {

    public static final String TIPO = "tipo";

    public static final String INSTALACION_PADRE = "instalacion_padre";

    private static final String TAG = BusquedaAvanzadaActivity.class.getSimpleName();

    private View progressBar;

    private Location location;

    private Database database;

    private BusquedaServices busquedaServices;

    private List<String> elements = new ArrayList<>();

    private List<Resultado> results = new ArrayList<>();

    private CaptureGeolocationService captureGeolocationService;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private TextInputEditText family;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_busqueda_avanzada);

            includeBackButtonAndTitle(R.string.opciones_busqueda);

            database = new Database(this);
            busquedaServices = new BusquedaServices(this);
            captureGeolocationService = new CaptureGeolocationService(this);

            elements = Arrays.asList(getString(R.string.seleccione_tipo_entidad_padre),
                    getString(R.string.equipo),
                    getString(R.string.instalacion_locativa)/*,
                    getString(R.string.instalacion_proceso)*/);

            ArrayAdapter<String> adapter
                    = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, elements);
            AppCompatSpinner type = findViewById(R.id.tipo);
            type.setAdapter(adapter);

            progressBar = findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.GONE);

            family = findViewById(R.id.familia);
            family.setOnClickListener(view -> loadBusquedaVariables("Familia"));

            // Limpiar campo familia
            ImageView btnRemoverFamilia = findViewById(R.id.remover_familia_busqueda);
            btnRemoverFamilia.setOnClickListener(view -> {
                family.setText("");
            });

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                TextInputEditText parent = findViewById(R.id.entidad_padre);
                parent.setText(bundle.getString(INSTALACION_PADRE, ""));

                String selected = bundle.getString(TIPO, "");
                if (!selected.isEmpty()) {
                    if ("InstalacionLocativa".equals(selected)) {
                        selected = getString(R.string.instalacion_locativa);
                    }

                    if ("InstalacionProceso".equals(selected)) {
                        selected = getString(R.string.instalacion_proceso);
                    }

                    int index = elements.indexOf(selected);
                    if (index > 0) {
                        type.setSelection(index);
                    }
                }
            }

            SwitchCompat switchCompat = findViewById(R.id.ubicacion);
            switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    Snackbar.make(getView(), "La ubicación solo filtra las instalaciones que tienen un sitio asociado y esta geolocalizado", Snackbar.LENGTH_LONG)
                            .show();
                }
            });

            if (!Geolocation.checkPermission(this)) {
                Geolocation.requestPermission(this);
            }
        } catch (Exception e) {
            backActivity(e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_formulario, menu);
        MenuItem itemDone = menu.findItem(R.id.action_done);
        itemDone.setIcon(R.drawable.buscar);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_done:
                SwitchCompat switchCompat = findViewById(R.id.ubicacion);
                if (switchCompat.isChecked()) {
                    compositeDisposable.add(captureGeolocationService.obtener(false)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::onNext, this::onError, this::search));
                    break;
                }

                search();
                break;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                TextInputEditText parent = findViewById(R.id.entidad_padre);
                parent.setText(bundle.getString(INSTALACION_PADRE, ""));

                String selected = bundle.getString(TIPO, "");
                if (!selected.isEmpty()) {

                    if ("InstalacionLocativa".equals(selected)) {
                        selected = getString(R.string.instalacion_locativa);
                    }

                    if ("InstalacionProceso".equals(selected)) {
                        selected = getString(R.string.instalacion_proceso);
                    }

                    int index = elements.indexOf(selected);
                    if (index > 0) {
                        AppCompatSpinner type = findViewById(R.id.tipo);
                        type.setSelection(index);
                    }
                }

                Long idEntidad = bundle.getLong("idEntidad");
                String tipoEntidad = bundle.getString("tipoEntidad");

                if (tipoEntidad != null) {
                    switch (tipoEntidad) {
                        case "Familia":
                            Familia familia = database.where(Familia.class)
                                    .equalTo("id", idEntidad)
                                    .findFirst();

                            if (familia != null) {
                                family.setText(familia.getNombre());
                            }
                            break;
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }

        results.clear();
        compositeDisposable.clear();

        if (captureGeolocationService != null) {
            captureGeolocationService.close();
        }
    }

    private void search() {
        if (progressBar.getVisibility() == View.VISIBLE) {
            return;
        }

        BusquedaAvanzada busquedaAvanzada = new BusquedaAvanzada();
        TextInputEditText parent = findViewById(R.id.entidad_padre);
        if (parent.getText() != null) {
            busquedaAvanzada.setParent(parent.getText().toString());
        }

        TextInputEditText code = findViewById(R.id.codigo);
        if (code.getText() != null) {
            busquedaAvanzada.setCode(code.getText().toString());
        }

        TextInputEditText external = findViewById(R.id.codigo_externo);
        if (external.getText() != null) {
            busquedaAvanzada.setExternal(external.getText().toString());
        }

        TextInputEditText name = findViewById(R.id.nombre);
        if (name.getText() != null) {
            busquedaAvanzada.setName(name.getText().toString());
        }

        TextInputEditText family = findViewById(R.id.familia);
        family.setOnClickListener(view -> loadBusquedaVariables("Familia"));
        if (family.getText() != null) {
            busquedaAvanzada.setFamily(family.getText().toString());
        }

        AppCompatSpinner type = findViewById(R.id.tipo);
        if (!type.getSelectedItem().equals(getString(R.string.seleccione_tipo_entidad_padre))) {
            if (type.getSelectedItem().equals(getString(R.string.instalacion_proceso))) {
                busquedaAvanzada.setType("InstalacionProceso");
            } else if (type.getSelectedItem().equals(getString(R.string.instalacion_locativa))) {
                busquedaAvanzada.setType("InstalacionLocativa");
            }else {
                busquedaAvanzada.setType("Equipo");
            }
        }

        SwitchCompat switchCompat = findViewById(R.id.ubicacion);
        if (location != null && switchCompat.isChecked()) {
            busquedaAvanzada.setAltitude(location.getAltitude());
            busquedaAvanzada.setLatitude(location.getLatitude());
            busquedaAvanzada.setLongitude(location.getLongitude());
        }

        if (switchCompat.isChecked() && !busquedaAvanzada.isLocationValid()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setTitle(R.string.ubicacion_titulo);
            alertDialogBuilder.setMessage("No fue posible obtener la ubicación actual, vuelve a intentarlo");
            alertDialogBuilder.setPositiveButton(R.string.aceptar, (dialog, which) -> dialog.dismiss());
            alertDialogBuilder.show();
            return;
        }

        if (!busquedaAvanzada.isValid() && !switchCompat.isChecked()) {
            Snackbar.make(getView(), R.string.error_formulario_busqueda_avanzada, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        results.clear();
        progressBar.setVisibility(View.VISIBLE);
        compositeDisposable.add(busquedaServices.avanzada(busquedaAvanzada)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNext, this::onError, this::onComplete));
    }

    private void onNext(Location location) {
        this.location = location;
    }

    private void onNext(@NonNull Response response) {
        Version.save(getApplicationContext(), response.getVersion());
        if (results == null) {
            results = new ArrayList<>();
        }

        BusquedaAvanzada.Request request = response.getBody(BusquedaAvanzada.Request.class);
        results.addAll(request.getEntities());
    }

    private void onError(@NonNull Throwable throwable) {
        progressBar.setVisibility(View.GONE);
        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                .show();
    }

    private void onComplete() {
        progressBar.setVisibility(View.GONE);
        if (results == null || results.isEmpty()) {
            Snackbar.make(getView(), R.string.message_search_empty, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putSparseParcelableArray(
                BusquedaAvanzadaResultadoActivity.SEARCH_RESULT, Resultado.factory(results));

        Intent intent = new Intent(this, BusquedaAvanzadaResultadoActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, BusquedaAvanzadaResultadoActivity.REQUEST_ACTION);
    }

    private void loadBusquedaVariables(String tipoEntidad) {
        Intent intent = new Intent(this, BusquedaVariablesEquipoActivity.class);
        intent.putExtra("tipoEntidad", tipoEntidad);
        startActivityForResult(intent, 1);
    }
}