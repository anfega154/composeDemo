package com.mantum.cmms.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Activos;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Transferencia;
import com.mantum.cmms.service.TransferenciaService;
import com.mantum.component.Mantum;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.AlphabetAdapter;
import com.mantum.demo.R;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.RealmList;

import static com.mantum.component.Mantum.isConnectedOrConnecting;

public class TransferenciaActivity extends Mantum.Activity {

    private static final String TAG = TransferenciaActivity.class.getSimpleName();

    private Database database;

    private ProgressBar progressBar;

    private AlphabetAdapter<Transferencia> alphabetAdapter;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private TransferenciaService transferenciaService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_transferencia);

            progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);

            database = new Database(this);
            includeBackButtonAndTitle(R.string.accion_transferencia);

            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            transferenciaService = new TransferenciaService(this);
            alphabetAdapter = new AlphabetAdapter<>(this);
            alphabetAdapter.hiddenSummary();
            alphabetAdapter.setOnAction(new OnSelected<Transferencia>() {

                @Override
                public void onClick(Transferencia value, int position) {
                    Bundle bundle = new Bundle();
                    bundle.putLong(AceptarTransferenciaActivity.KEY_ID, value.getId());

                    Intent intent = new Intent(getApplicationContext(), AceptarTransferenciaActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }

                @Override
                public boolean onLongClick(Transferencia value, int position) {
                    return false;
                }

            });

            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            RecyclerView recyclerView = findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemViewCacheSize(20);
            recyclerView.setDrawingCacheEnabled(true);
            recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(alphabetAdapter);

            request();
        } catch (Exception e) {
            backActivity(getString(R.string.error_app));
        }
    }

    private void request() {
        progressBar.setVisibility(View.VISIBLE);
        compositeDisposable.add(transferenciaService.get()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNext, this::onError, this::onComplete));
    }

    private void onNext(List<Transferencia> transferencias) {
        boolean write = true;
        if (transferencias.isEmpty() && !isConnectedOrConnecting(getApplicationContext())) {
            write = false;
            transferencias = read();
        }

        for (Transferencia transferencia : transferencias) {
            alphabetAdapter.add(transferencia);
        }

        if (write) {
            write(transferencias);
        }
    }

    private void write(@NonNull List<Transferencia> transferencias) {
        if (transferencias.isEmpty()) {
            return;
        }

        Database database = new Database(this);
        database.executeTransaction(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                return;
            }

            self.where(Transferencia.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findAll()
                    .deleteAllFromRealm();

            for (Transferencia transferencia : transferencias) {
                Transferencia temporal = self.where(Transferencia.class)
                        .equalTo("id", transferencia.getId())
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();

                if (temporal == null) {
                    transferencia.setUUID(UUID.randomUUID().toString());
                    transferencia.setCuenta(cuenta);
                    self.insert(transferencia);
                } else {
                    temporal.setCodigo(transferencia.getCodigo());
                    temporal.setFecha(transferencia.getFecha());
                    temporal.setPersonal(transferencia.getPersonal());

                    RealmList<Activos> activos = new RealmList<>();
                    for (Activos activo : transferencia.getActivos()) {
                        activos.add(self.copyToRealm(activo));
                    }
                    temporal.setActivos(activos);
                }
            }
        });
        database.close();
    }

    private List<Transferencia> read() {
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return new ArrayList<>();
        }

        return database.where(Transferencia.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .findAll();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actualizar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;

            case R.id.action_refresh:
                request();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
        alphabetAdapter.clear();
        compositeDisposable.clear();
        transferenciaService.cancel();
    }

    private void onComplete() {
        progressBar.setVisibility(View.GONE);

        alphabetAdapter.refresh();
        isEmptyList();
    }

    private void onError(@NonNull Throwable throwable) {
        progressBar.setVisibility(View.GONE);
        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                .show();
    }

    private void isEmptyList() {
        RelativeLayout container = findViewById(R.id.empty);
        container.setVisibility(alphabetAdapter.isEmpty() ? View.VISIBLE : View.GONE);
    }
}