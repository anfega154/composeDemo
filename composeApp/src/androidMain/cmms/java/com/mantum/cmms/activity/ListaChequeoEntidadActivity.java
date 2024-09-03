package com.mantum.cmms.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.ListaChequeo;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.cmms.service.ListaChequeoService;
import com.mantum.cmms.view.EntidadView;
import com.mantum.cmms.view.RutaTrabajoView;
import com.mantum.component.Mantum;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.InformationAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ListaChequeoEntidadActivity extends Mantum.Activity {

    private static final String TAG
            = ListaChequeoEntidadActivity.class.getSimpleName();

    private AlertDialog modal;
    private Database database;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListaChequeoService listaChequeoService;
    private Mantum.OnScrollListener onScrollListener;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public InformationAdapter<RutaTrabajoView, EntidadView> informationAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.mantum.component.R.layout.information_layout_view);

        includeBackButtonAndTitle(R.string.lista_chequeo_entidad);

        database = new Database(this);
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta != null) {
            listaChequeoService = new ListaChequeoService(this, cuenta);
        }

        progressBar = findViewById(R.id.progressBar);

        informationAdapter = new InformationAdapter<>(this);
        informationAdapter.setDrawable(value -> R.drawable.ruta_trabajo_search);
        informationAdapter.setOnAction(new OnSelected<RutaTrabajoView>() {

            @Override
            public void onClick(RutaTrabajoView value, int position) {
                Bundle bundle = new Bundle();
                bundle.putLong(Mantum.KEY_ID, value.getId());
                bundle.putString(DetalleListaChequeoActivity.KEY_CODE, value.getCodigo());

                Intent intent = new Intent(
                        ListaChequeoEntidadActivity.this, DetalleListaChequeoActivity.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, 0);
            }

            @Override
            public boolean onLongClick(RutaTrabajoView value, int position) {
                return false;
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        onScrollListener = new Mantum.OnScrollListener(layoutManager, false) {

            @Override
            public void onRequest(int page) {
                ListaChequeoEntidadActivity.this.onRequest(page);
            }
        };

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (!Mantum.isConnectedOrConnecting(this)) {
                swipeRefreshLayout.setRefreshing(false);
                Snackbar.make(getView(), R.string.offline, Snackbar.LENGTH_LONG)
                        .show();
                return;
            }

            ListaChequeoEntidadActivity.this.onRequest(1);
        });

        RecyclerView recyclerView = informationAdapter.startAdapter(getView(), layoutManager);
        recyclerView.addOnScrollListener(onScrollListener);

        onRequest(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lista_chequeo, menu);

        if (UserPermission.check(this, UserPermission.DESCARGAR_LC_ENTIDAD, true)) {
            menu.findItem(R.id.download).setVisible(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            backActivity();
        } else if (itemId == R.id.download) {
            download();
        }
        return super.onOptionsItemSelected(item);
    }

    private void download() {
        if (!Mantum.isConnectedOrConnecting(this)) {
            Toast.makeText(this, getString(R.string.offline), Toast.LENGTH_LONG)
                    .show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = View.inflate(
                this, R.layout.dialog_descargar_lista_chequeo, null);

        compositeDisposable.add(
                listaChequeoService.clear()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::onNext)
        );

        compositeDisposable.add(
                listaChequeoService.download()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap((ListaChequeo.Response response) -> {
                            TextView message = view.findViewById(R.id.percent);
                            message.setText(response.getPercent() != null
                                    ? response.getPercent() + " %"
                                    : ""
                            );
                            return listaChequeoService.save(response);
                        })
                        .repeatWhen(completed -> completed.delay(5, TimeUnit.SECONDS))
                        .subscribe(this::onNext, this::onError, () -> {
                            if (informationAdapter != null) {
                                informationAdapter.clear();
                                informationAdapter.addAll(pagination(1));
                                informationAdapter.showMessageEmpty(getView());
                            }
                        })
        );

        modal = builder.setView(view).setPositiveButton(R.string.cancelar, (dialog, which) -> {
            compositeDisposable.clear();
            dialog.dismiss();
        }).setCancelable(false).create();

        modal.show();
    }

    private void onRequest(int page) {
        if (listaChequeoService == null) {
            return;
        }

        if (Mantum.isConnectedOrConnecting(this)) {
            progress();
            onScrollListener.loading(true);

            compositeDisposable.add(listaChequeoService.findAll(page)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap(this::onSave)
                    .subscribe(this::onNext, this::onError, this::onComplete));
        } else {
            List<RutaTrabajoView> results = pagination(page);
            if (results.isEmpty()) {
                onScrollListener.decrease();
            }

            informationAdapter.addAll(results, false);
            onScrollListener.loading(false);

            Handler handler = new Handler();
            handler.post(() -> {
                if (informationAdapter != null) {
                    informationAdapter.notifyDataSetChanged();
                    informationAdapter.showMessageEmpty(getView());
                }
            });
        }
    }

    @NonNull
    private List<RutaTrabajoView> pagination(int page) {
        List<ListaChequeo> pagination = listaChequeoService.pagination(page);
        List<RutaTrabajoView> results = new ArrayList<>();
        for (ListaChequeo value : pagination) {
            results.add(RutaTrabajoView.factory(value));
        }
        return results;
    }

    @NonNull
    private Observable<List<ListaChequeo>> onSave(@NonNull ListaChequeo.Response response) {
        if (response.getData().size() == 0) {
            onScrollListener.decrease();
        }
        return listaChequeoService.saveSimple(response.getData());
    }

    private void onNext(@NonNull List<ListaChequeo> values) {
        List<RutaTrabajoView> results = new ArrayList<>();
        for (ListaChequeo value : values) {
            results.add(RutaTrabajoView.factory(value));
        }
        informationAdapter.addAll(results, false);
    }

    private void onNext(@NonNull ListaChequeo.Response response) {
        if (response.getNext() == null) {
            compositeDisposable.clear();
            modal.dismiss();

            Snackbar.make(
                    getView(),
                    R.string.descargar_lista_chequeo_finalizadas,
                    Snackbar.LENGTH_LONG
            ).show();
        }
    }

    private void onNext(boolean success) {
        Log.e(TAG,
                "Se han eliminado las listas de chequeo registradas? " + (success ? "Si" : "No")
        );
    }

    private void onError(Throwable throwable) {
        if (modal != null) {
            modal.dismiss();
        }

        compositeDisposable.clear();
        if (throwable != null && throwable.getMessage() != null) {
            Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private void onComplete() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }

        if (onScrollListener != null) {
            onScrollListener.loading(false);
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        if (informationAdapter != null) {
            informationAdapter.notifyDataSetChanged();
            if (getView() != null) {
                informationAdapter.showMessageEmpty(getView());
            }
        }
    }

    private void progress() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams layoutParams
                    = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            progressBar.setLayoutParams(layoutParams);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }
}
