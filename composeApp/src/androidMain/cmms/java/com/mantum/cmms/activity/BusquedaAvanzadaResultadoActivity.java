package com.mantum.cmms.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mantum.R;
import com.mantum.cmms.adapter.BusquedaAvanzadaResultadoAdapter;
import com.mantum.cmms.convert.BusquedaConvert;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.domain.Resultado;
import com.mantum.cmms.entity.Busqueda;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Equipo;
import com.mantum.cmms.entity.InstalacionLocativa;
import com.mantum.cmms.service.BusquedaServices;
import com.mantum.cmms.service.EquipoServices;
import com.mantum.cmms.service.InstalacionLocativaServices;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.OnSelected;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.internal.functions.Functions;

public class BusquedaAvanzadaResultadoActivity extends Mantum.Activity {

    public static final int REQUEST_ACTION = 1203;

    public static final String SEARCH_RESULT = "search_result";

    private static final String TAG = BusquedaAvanzadaResultadoActivity.class.getSimpleName();

    private BusquedaAvanzadaResultadoAdapter busquedaAvanzadaResultadoAdapter;

    private Database database;

    private BusquedaServices busquedaServices;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private ProgressDialog progressDialog;

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_busqueda_avanzada_resultado);

            includeBackButtonAndTitle(R.string.resultados);
            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                throw new Exception("bundle == null");
            }

            database = new Database(this);
            busquedaServices = new BusquedaServices(this);

            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            busquedaAvanzadaResultadoAdapter = new BusquedaAvanzadaResultadoAdapter(this);
            busquedaAvanzadaResultadoAdapter.setOnAction(new OnSelected<Resultado>() {

                @Override
                public void onClick(Resultado value, int position) {
                    Bundle bundle;
                    Intent intent;

                    Busqueda busqueda = database.where(Busqueda.class)
                            .equalTo("cuenta.UUID", cuenta.getUUID())
                            .equalTo("id", value.getId())
                            .equalTo("type", value.getTipo())
                            .findFirst();

                    if (busqueda == null) {
                        progressDialog = new ProgressDialog(BusquedaAvanzadaResultadoActivity.this);
                        progressDialog.setTitle(R.string.obtener_detalle_entidad);
                        progressDialog.setMessage(getString(R.string.obteniendo_detalle_entidad));
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        compositeDisposable.add(busquedaServices.buscar(value.getId(), value.getTipo())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(BusquedaAvanzadaResultadoActivity.this::onNext, BusquedaAvanzadaResultadoActivity.this::onError, () -> {

                                    Handler handler = new Handler();
                                    Runnable runnable = () -> {
                                        if (progressDialog != null && progressDialog.isShowing()) {
                                            progressDialog.dismiss();
                                        }

                                        if (recyclerView != null) {
                                            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                                            if (viewHolder != null) {
                                                LinearLayout container = viewHolder.itemView.findViewById(R.id.container);
                                                if (container != null) {
                                                    container.performClick();
                                                }
                                            }
                                        }
                                    };

                                    handler.postDelayed(runnable, 500);
                                }));

                        return;
                    }

                    switch (busqueda.getType()) {

                        case Equipo.SELF:

                            bundle = new Bundle();
                            bundle.putString(Mantum.KEY_UUID, busqueda.getReference());
                            bundle.putLong(Mantum.KEY_ID, busqueda.getId());

                            intent = new Intent(
                                    BusquedaAvanzadaResultadoActivity.this, DetalleEquipoActivity.class);
                            intent.putExtras(bundle);

                            startActivity(intent);
                            break;

                        case InstalacionLocativa.SELF:
                            bundle = new Bundle();
                            bundle.putString(Mantum.KEY_UUID, busqueda.getReference());
                            bundle.putLong(Mantum.KEY_ID, busqueda.getId());

                            intent = new Intent(
                                    BusquedaAvanzadaResultadoActivity.this, DetalleInstalacionLocativaActivity.class);
                            intent.putExtras(bundle);

                            startActivity(intent);
                            break;
                    }
                }

                @Override
                public boolean onLongClick(Resultado value, int position) {
                    Bundle bundle = new Bundle();
                    bundle.putString(BusquedaAvanzadaActivity.INSTALACION_PADRE, value.getNombre());
                    bundle.putString(BusquedaAvanzadaActivity.TIPO, value.getTipo());

                    Intent intent = new Intent();
                    intent.putExtras(bundle);
                    backActivity(intent);

                    return true;
                }
            });

            List<Resultado> items = Mantum.asList(
                    bundle.getSparseParcelableArray(SEARCH_RESULT));

            busquedaAvanzadaResultadoAdapter.sort(items);
            busquedaAvanzadaResultadoAdapter.addAll(items);

            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView = busquedaAvanzadaResultadoAdapter.startAdapter(getView(), layoutManager);
        } catch (Exception e) {
            backActivity(e.getMessage());
        }
    }

    private void onNext(@NonNull Response response) {
        Version.save(getApplicationContext(), response.getVersion());

        Database database = new Database(this);
        database.executeTransaction(self -> {
            try {
                Cuenta cuenta = self.where(Cuenta.class)
                        .equalTo("active", true)
                        .findFirst();

                if (cuenta == null) {
                    return;
                }

                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(new TypeToken<Busqueda.Request>() {
                        }.getType(), new BusquedaConvert())
                        .create();

                InstalacionLocativaServices instalacionLocativaServices
                        = new InstalacionLocativaServices(this, cuenta);
                EquipoServices equipoServices = new EquipoServices(this, cuenta);

                Busqueda.Request content = gson.fromJson(response.getBody(), Busqueda.Request.class);
                for (Busqueda busqueda : content.getEntities()) {
                    switch (busqueda.getType()) {

                        case Equipo.SELF:
                            Equipo equipo = busqueda.getDetalle(Equipo.class);
                            if (equipo != null) {
                                equipo.setId(busqueda.getId());
                                equipo.setVariables(busqueda.getVariables());
                                equipo.setGmap(busqueda.getGmap());
                                equipo.setOrdenTrabajos(busqueda.getHistoricoOT());

                                compositeDisposable.add(equipoServices.save(equipo)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(busquedaServices::guardarEquipos, Mantum::ignoreError, Functions.EMPTY_ACTION));
                            }
                            break;

                        case InstalacionLocativa.SELF:
                            InstalacionLocativa instalacionLocativa = busqueda.getDetalle(InstalacionLocativa.class);
                            if (instalacionLocativa != null) {
                                instalacionLocativa.setId(busqueda.getId());
                                instalacionLocativa.setVariables(busqueda.getVariables());
                                instalacionLocativa.setGmap(busqueda.getGmap());
                                instalacionLocativa.setOrdenTrabajos(busqueda.getHistoricoOT());

                                compositeDisposable.add(instalacionLocativaServices.save(instalacionLocativa)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(busquedaServices::guardarInstalacionesLocativas, Mantum::ignoreError, Functions.EMPTY_ACTION));
                            }
                            break;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "onNext: ", e);
            }
        });

        database.close();
    }

    private void onError(@NonNull Throwable throwable) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (busquedaAvanzadaResultadoAdapter != null) {
            busquedaAvanzadaResultadoAdapter.clear();
        }

        if (busquedaServices != null) {
            busquedaServices.onDestroy();
        }

        compositeDisposable.clear();
    }
}