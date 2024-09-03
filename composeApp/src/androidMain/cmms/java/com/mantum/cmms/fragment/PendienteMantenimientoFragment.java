package com.mantum.cmms.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.mantum.demo.R;
import com.mantum.cmms.activity.DetallePendienteActivity;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Pendiente;
import com.mantum.cmms.service.PendienteService;
import com.mantum.cmms.view.EntidadView;
import com.mantum.cmms.view.PendienteMantenimientoView;
import com.mantum.component.Mantum;
import com.mantum.component.OnDrawable;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.InformationAdapter;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.RealmResults;

public class PendienteMantenimientoFragment extends Mantum.Fragment {

    private final static String TAG = PendienteMantenimientoFragment.class.getSimpleName();

    public final static String KEY_TAB = "Pendiente_Mantenimiento";

    private Database database;

    private ProgressBar progressBar;

    private PendienteService pendienteService;

    private Mantum.OnScrollListener onScrollListener;

    private InformationAdapter<PendienteMantenimientoView, EntidadView> informationAdapter;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(com.mantum.demo.R.layout.information_layout_view, container, false);

        progressBar = view.findViewById(R.id.progressBar);
        database = new Database(view.getContext());

        informationAdapter = new InformationAdapter<>(view.getContext());
        informationAdapter.setDrawable(value -> R.drawable.pendiente_mantenimiento);
        informationAdapter.setMenu(R.menu.menu_pendiente);
        informationAdapter.setOnCall((menu, value) -> {
            switch (menu.getItemId()) {
                case R.id.refresh:
                    compositeDisposable.add(pendienteService.fetchById(value.getId())
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .flatMap(pendientes -> pendientes.isEmpty()
                                    ? pendienteService.remove(value.getId())
                                    : pendienteService.save(pendientes))
                            .subscribe(this::onNext, this::onError, this::onComplete)
                    );
                    break;

                case R.id.remove:
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

                    builder.setTitle(R.string.titulo_eliminar_pendiente);
                    builder.setMessage(R.string.mensaje_eliminar_pendiente);

                    builder.setNegativeButton(R.string.aceptar, (dialog, which) -> compositeDisposable.add(pendienteService.remove(value.getId())
                            .subscribe(ordenTrabajos -> this.onRemove(value), this::onError, this::onComplete)));
                    builder.setPositiveButton(R.string.cancelar, (dialog, which) -> dialog.dismiss());

                    builder.setCancelable(true);
                    builder.create();
                    builder.show();
                    break;
            }
            return true;
        });

        informationAdapter.setOnAction(new OnSelected<PendienteMantenimientoView>() {

            @Override
            public void onClick(PendienteMantenimientoView value, int position) {
                Bundle bundle = new Bundle();
                bundle.putLong(Mantum.KEY_ID, value.getId());

                Intent intent = new Intent(view.getContext(), DetallePendienteActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
            }

            @Override
            public boolean onLongClick(PendienteMantenimientoView value, int position) {
                return false;
            }

        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        onScrollListener = new Mantum.OnScrollListener(layoutManager) {

            @Override
            public void onRequest(int page) {
                PendienteMantenimientoFragment.this.onRequest(view, page, false);
            }
        };

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (!Mantum.isConnectedOrConnecting(view.getContext())) {
                swipeRefreshLayout.setRefreshing(false);
                Snackbar.make(view, R.string.offline, Snackbar.LENGTH_LONG)
                        .show();
                return;
            }

            PendienteMantenimientoFragment.this.onRequest(view, 1, false);
        });

        RecyclerView recyclerView = informationAdapter.startAdapter(view, layoutManager);
        recyclerView.addOnScrollListener(onScrollListener);

        onRequest(view, 1, true);
        return view;
    }

    public void onRefresh() {
        onRequest(getView(), 1, true);
    }

    public void onRequest(View view, int page, boolean notify) {
        if (view == null || database.isClosed()) {
            return;
        }

        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return;
        }

        RealmResults<Pendiente> results = database.where(Pendiente.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .findAll();

        List<Pendiente> pendientes = database.pagination(results, page);
        if (Mantum.isConnectedOrConnecting(view.getContext())) {
            informationAdapter.addAll(PendienteMantenimientoView.factory(pendientes), notify);
            informationAdapter.showMessageEmpty(view, R.string.pendiente_mantenimiento, R.drawable.evento);

            pendienteService = new PendienteService(view.getContext(), cuenta);
            if (page > 1) {
                progress();
            }

            compositeDisposable.add(fetch(page)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap(this::onSave)
                    .subscribe(this::onNext, this::onError, this::onComplete));
        } else {
            if (pendientes.size() == 0) {
                onScrollListener.decrease();
            }

            informationAdapter.addAll(PendienteMantenimientoView.factory(pendientes), notify);
            onScrollListener.loading(false);
            Handler handler = new Handler();
            handler.post(() -> {
                informationAdapter.notifyDataSetChanged();
                informationAdapter.showMessageEmpty(view, R.string.pendiente_mantenimiento, R.drawable.evento);
            });
        }
    }

    public Observable<Pendiente.Request> fetch(int page) {
        onScrollListener.loading(true);
        return pendienteService.fetch(page);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (database != null) {
            database.close();
        }

        if (informationAdapter != null) {
            informationAdapter.clear();
        }

        if (pendienteService != null) {
            pendienteService.close();
        }

        compositeDisposable.clear();
    }

    @NonNull
    @Override
    public String getKey() {
        return KEY_TAB;
    }

    @NonNull
    @Override
    public String getTitle(@NonNull Context context) {
        return context.getString(R.string.tab_pendientes);
    }

    @NonNull
    private Observable<List<Pendiente>> onSave(@NonNull Pendiente.Request request) {
        if (request.getPendientes().size() == 0) {
            onScrollListener.decrease();
        }

        if (request.getTab() == null || request.getTab().getIds() == null) {
            return pendienteService.save(request.getPendientes());
        }

        Long[] value = request.getTab().getIds()
                .toArray(new Long[]{});

        return pendienteService.remove(value)
                .map(this::onRemove)
                .flatMap(pendientes -> pendienteService.save(request.getPendientes()));
    }

    private void onRemove(PendienteMantenimientoView pendienteMantenimientoView) {
        informationAdapter.remove(pendienteMantenimientoView, false);
    }

    private List<Long> onRemove(List<Long> remove) {
        for (Long id : remove) {
            informationAdapter.remove(value -> value.getId().equals(id));
        }
        return remove;
    }

    private void onNext(List<Pendiente> pendientes) {
        informationAdapter.addAll(PendienteMantenimientoView.factory(pendientes), false);
    }

    private void onComplete() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }

        progressBar.setVisibility(View.GONE);
        onScrollListener.loading(false);
        informationAdapter.notifyDataSetChanged();
        if (getView() != null) {
            informationAdapter.showMessageEmpty(getView(), R.string.pendiente_mantenimiento, R.drawable.evento);
        }
    }

    private void onError(Throwable throwable) {
        Log.e(TAG, "onError: ", throwable);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }

        progressBar.setVisibility(View.GONE);
        onScrollListener.loading(false);
        onScrollListener.decrease();
        informationAdapter.notifyDataSetChanged();
        if (getView() != null) {
            Snackbar.make(getView(), R.string.error_pendientes, Snackbar.LENGTH_LONG)
                    .show();
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
}