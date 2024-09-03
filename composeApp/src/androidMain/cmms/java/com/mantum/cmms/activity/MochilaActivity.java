package com.mantum.cmms.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.helper.RecursoHelper;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Mochila;
import com.mantum.cmms.service.MochilaService;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.AlphabetAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;

import static com.mantum.component.Mantum.isConnectedOrConnecting;

public class MochilaActivity extends Mantum.Activity {

    private static final String TAG = MochilaActivity.class.getSimpleName();

    public static final String RECURSO = "Mochila";

    public static final String ACTION = "Action";

    private Database database;

    private ProgressBar progressBar;

    private MochilaService mochilaService;

    private AlphabetAdapter<Mochila> alphabetAdapter;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_mochila_bitacora);

            database = new Database(this);
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            progressBar = findViewById(R.id.progressBar);
            includeBackButtonAndTitle(R.string.mochila_title);

            final boolean[] actions = { false };
            Bundle bundle = getIntent().getExtras();
            mochilaService = new MochilaService(this, cuenta);
            alphabetAdapter = new AlphabetAdapter<>(this);

            if (bundle != null) {
                RecursoHelper recursoHelper = (RecursoHelper) bundle.getSerializable(RECURSO);
                alphabetAdapter.addAll(recursoHelper != null
                        ? recursoHelper.getMochilas()
                        : database.instance().copyFromRealm(getMochilas()));
                actions[0] = bundle.getBoolean(ACTION);
            }

            isEmptyList();
            alphabetAdapter.setOnAction(new OnSelected<Mochila>() {

                @Override
                public void onClick(Mochila value, int position) {
                    if (actions[0]) {
                        return;
                    }

                    View form = View.inflate(MochilaActivity.this,
                            R.layout.selector_recursos, null);

                    final TextInputEditText cantidad = form.findViewById(R.id.cantidad);
                    cantidad.setText(String.valueOf(value.getCantidad()));

                    TextView sigla = form.findViewById(R.id.sigla);
                    sigla.setText(value.getSigla());

                    TextInputEditText ubicacion = form.findViewById(R.id.ubicacion);
                    ubicacion.setText(value.getUbicacion());

                    TextInputEditText observacion = form.findViewById(R.id.observacion);
                    observacion.setText(value.getObservaciones());

                    AlertDialog.Builder alertDialogBuilder
                            = new AlertDialog.Builder(MochilaActivity.this);
                    alertDialogBuilder.setView(form);
                    alertDialogBuilder.setCancelable(false);

                    alertDialogBuilder.setPositiveButton(getString(R.string.add_resource), (dialogInterface, i) -> {
                        String number = cantidad.getText().toString();
                        number = number.isEmpty() ? "0" : number;
                        value.setCantidad(number);

                        value.setUbicacion(ubicacion.getText().toString());
                        value.setObservaciones(observacion.getText().toString());

                        alphabetAdapter.add(value, true);
                        dialogInterface.cancel();
                    });

                    alertDialogBuilder.setNegativeButton(getString(R.string.cancel_resource), (dialogInterface, i) -> dialogInterface.cancel());
                    alertDialogBuilder.show();
                }

                @Override
                public boolean onLongClick(Mochila value, int position) {
                    if (actions[0]) {
                        return true;
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(MochilaActivity.this);
                    builder.setTitle(R.string.remove_resource_title);
                    builder.setMessage(R.string.remove_resource_message);
                    builder.setPositiveButton(R.string.acept_resource, (dialog, which) -> {
                        alphabetAdapter.remove(value, true);

                        isEmptyList();
                        dialog.cancel();
                    });

                    builder.setNegativeButton(getString(R.string.cancel_resource), (dialog, i) -> dialog.cancel());
                    builder.setCancelable(false);
                    builder.show();
                    return true;
                }

            });

            if (actions[0]) {
                alphabetAdapter.hiddenSummary();
            }

            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            RecyclerView recyclerView = findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemViewCacheSize(20);
            recyclerView.setDrawingCacheEnabled(true);
            recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(alphabetAdapter);

            if (actions[0] || alphabetAdapter.isEmpty()) {
                request();
            }
        } catch (Exception e) {
            Log.e(TAG, "onCreate: ", e);
            backActivity(getString(R.string.error_app));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mochila, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_refresh:
                if (!isConnectedOrConnecting(getApplicationContext())) {
                    Snackbar.make(getView(), R.string.offline, Snackbar.LENGTH_LONG)
                            .show();
                    return true;
                }

                request();
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

        alphabetAdapter.clear();
        compositeDisposable.clear();
        mochilaService.cancel();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(RECURSO, RecursoHelper.mochilaAdapter(alphabetAdapter.getOriginal()));
        setResult(RESULT_OK, intent);
        finish();
    }

    private void isEmptyList() {
        RelativeLayout container = findViewById(R.id.empty);
        container.setVisibility(alphabetAdapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private List<Mochila> getMochilas() {
        Realm realm = database.instance();
        Cuenta cuenta = realm.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return new ArrayList<>();
        }

        return realm.where(Mochila.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .findAll();
    }

    private void onNext(Response response) {
        if (!response.isValid()) {
            for (Mochila mochila : getMochilas()) {
                alphabetAdapter.add(mochila);
            }
            return;
        }

        Version.save(getApplicationContext(), response.getVersion());
        List<Mochila> mochilas = response.getBody(Mochila.Request.class)
                .getRecursos();

        Realm realm = database.instance();
        realm.executeTransaction(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                return;
            }

            alphabetAdapter.clear();
            self.where(Mochila.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findAll()
                    .deleteAllFromRealm();

            for (Mochila mochila : mochilas) {
                Mochila register = self.where(Mochila.class)
                        .equalTo("idphrec", mochila.getIdphrec())
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();

                if (register == null) {
                    mochila.setCuenta(cuenta);
                    self.insert(mochila);
                } else {
                    register.setIdphrec(mochila.getIdphrec());
                    register.setCodigoph(mochila.getCodigoph());
                    register.setTipo(mochila.getTipo());
                    register.setCodigo(mochila.getCodigo());
                    register.setNombre(mochila.getNombre());
                    register.setSigla(mochila.getSigla());
                    register.setCantidad(mochila.getCantidad());
                    register.setCantidaddisponible(mochila.getCantidaddisponible());
                }

                alphabetAdapter.add(mochila);
            }
        });
    }

    private void onError(Throwable throwable) {
        progressBar.setVisibility(View.GONE);
        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                .show();
    }

    private void onComplete() {
        progressBar.setVisibility(View.GONE);
        alphabetAdapter.refresh();
        isEmptyList();
    }

    private void request() {
        progressBar.setVisibility(View.VISIBLE);
        compositeDisposable.add(mochilaService.get()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNext, this::onError, this::onComplete));
    }
}