package com.mantum.cmms.activity;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.helper.RecursoHelper;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Recurso;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.cmms.service.RecursoService;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.AlphabetAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;

import static com.mantum.cmms.entity.parameter.UserPermission.BLOCK_SEARCH_RESOURCES;

public class RecursosActivity extends Mantum.Activity implements SearchView.OnQueryTextListener {

    private static final String TAG = RecursosActivity.class.getSimpleName();

    public static final String RECURSO = "Recurso";

    public static final int REQUEST_ACTION = 1230;

    private Database database;

    private ProgressBar progressBar;

    private RecursoService recursoService;

    private AlphabetAdapter<Recurso> alphabetAdapter;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_recurso_bitacora);

            database = new Database(this);
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            progressBar = findViewById(R.id.progressBar);
            includeBackButtonAndTitle(R.string.seleccionar_recursos);

            Bundle bundle = getIntent().getExtras();
            alphabetAdapter = new AlphabetAdapter<>(this);
            if (bundle != null) {
                RecursoHelper recursoHelper = (RecursoHelper) bundle.getSerializable(RECURSO);
                if (recursoHelper != null && recursoHelper.getRecursos() != null && !recursoHelper.getRecursos().isEmpty()) {
                    alphabetAdapter.addAll(recursoHelper.getRecursos());
                }
            }

            isEmptyList();
            alphabetAdapter.setOnAction(new OnSelected<Recurso>() {

                @Override
                public void onClick(final Recurso value, int position) {
                    View form = View.inflate(RecursosActivity.this, R.layout.selector_recursos, null);

                    final TextInputEditText cantidad = form.findViewById(R.id.cantidad);
                    cantidad.setText(String.valueOf(value.getCantidad()));

                    TextView sigla = form.findViewById(R.id.sigla);
                    sigla.setText(value.getSigla());

                    TextInputEditText ubicacion = form.findViewById(R.id.ubicacion);
                    ubicacion.setText(value.getUbicacion());

                    TextInputEditText observacion = form.findViewById(R.id.observacion);
                    observacion.setText(value.getObservaciones());

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RecursosActivity.this);
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
                public boolean onLongClick(Recurso value, int position) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RecursosActivity.this);
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

            recursoService = new RecursoService(this, cuenta);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            RecyclerView recyclerView = findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemViewCacheSize(20);
            recyclerView.setDrawingCacheEnabled(true);
            recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(alphabetAdapter);

            FloatingActionButton camera = findViewById(R.id.camera);
            camera.setOnClickListener(v -> {
                progressBar.setVisibility(View.VISIBLE);

                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.setOrientationLocked(true);
                integrator.setCameraId(0);
                integrator.setPrompt("");
                integrator.setCaptureActivity(CaptureActivityPortrait.class);
                integrator.setBeepEnabled(false);
                integrator.initiateScan();
            });

        } catch (Exception e) {
            Log.e(TAG, "onCreate: ", e);
            backActivity(getString(R.string.error_app));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
        alphabetAdapter.clear();
        compositeDisposable.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recurso, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (Version.check(this, 7) && UserPermission.check(this, BLOCK_SEARCH_RESOURCES)) {
            searchItem.setVisible(false);
        }

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (searchManager != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setOnQueryTextListener(this);
            searchView.setIconifiedByDefault(false);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(
                    new ComponentName(this, RecursosActivity.class)));
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(RECURSO, RecursoHelper.recursoAdapter(alphabetAdapter.getOriginal()));
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        try {
            if (progressBar == null) {
                return false;
            }

            if (progressBar.getVisibility() == View.GONE && newText.length() >= 2) {
                progressBar.setVisibility(View.VISIBLE);
                return false;
            }

            if (newText.length() < 2 && progressBar.getVisibility() == View.VISIBLE) {
                progressBar.setVisibility(View.GONE);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "onQueryTextChange: ", e);
        }
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            compositeDisposable.add(recursoService.search(result.getContents())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onNext, this::onError, this::onComplete));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onNext(Response response) {
        Version.save(getApplicationContext(), response.getVersion());

        List<Recurso> recursos = response.getBody(Recurso.Request.class).getRecursos();
        if (recursos.size() == 0) {
            Snackbar.make(getView(), getString(R.string.message_search_empty), Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        Realm realm = database.instance();
        realm.executeTransaction(self -> {
            Cuenta cuenta = self.where(Cuenta.class).equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                return;
            }

            for (Recurso recurso : recursos) {
                Recurso temporal = self.where(Recurso.class)
                        .equalTo("id", recurso.getId())
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();

                if (temporal == null) {
                    recurso.setCuenta(cuenta);
                    self.insert(recurso);
                } else {
                    recurso.setCodigo(temporal.getCodigo());
                    recurso.setNombre(temporal.getNombre());
                    recurso.setCantidad(temporal.getCantidad());
                    recurso.setSigla(temporal.getSigla());
                }

                alphabetAdapter.add(recurso);
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

    private void isEmptyList() {
        RelativeLayout container = findViewById(R.id.container_search);
        container.setVisibility(alphabetAdapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void handleIntent(Intent intent) {
        try {
            progressBar.setVisibility(View.GONE);
            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                Gson gson = new GsonBuilder()
                        .disableHtmlEscaping()
                        .create();

                String text = intent.getDataString();
                Recurso recurso = gson.fromJson(text, Recurso.class);

                alphabetAdapter.add(recurso, true);
                isEmptyList();
            }
        } catch (Exception e) {
            Snackbar.make(getView(), getString(R.string.timeout_error), Snackbar.LENGTH_LONG)
                    .show();
        }
    }
}