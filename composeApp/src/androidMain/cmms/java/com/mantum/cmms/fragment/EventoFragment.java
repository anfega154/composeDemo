package com.mantum.cmms.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.mantum.R;
import com.mantum.cmms.activity.BitacoraActivity;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Evento;
import com.mantum.cmms.service.EventoService;
import com.mantum.cmms.view.EntidadView;
import com.mantum.cmms.view.EventoView;
import com.mantum.component.Mantum;
import com.mantum.component.OnDrawable;
import com.mantum.component.adapter.InformationAdapter;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.RealmResults;

public class EventoFragment extends Mantum.Fragment {

    private final static String TAG = OrdenTrabajoFragment.class.getSimpleName();

    public final static String KEY_TAB = "Evento";

    private Database database;

    private ProgressBar progressBar;

    private EventoService eventoService;

    private Mantum.OnScrollListener onScrollListener;

    private InformationAdapter<EventoView, EntidadView> informationAdapter;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_evento, container, false);

        progressBar = view.findViewById(R.id.progressBar);
        database = new Database(view.getContext());

        informationAdapter = new InformationAdapter<>(view.getContext());
        informationAdapter.setDrawable(value -> R.drawable.orden_trabajo);

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        onScrollListener = new Mantum.OnScrollListener(layoutManager) {

            @Override
            public void onRequest(int page) {
                EventoFragment.this.onRequest(view, page, false);
            }
        };

        RecyclerView recyclerView = informationAdapter.startAdapter(view, layoutManager);
        recyclerView.addOnScrollListener(onScrollListener);

        view.findViewById(R.id.bitacora_evento).setOnClickListener(v -> {
            Intent intent = new Intent(view.getContext(), BitacoraActivity.class);
            startActivity(intent);
        });

        onRequest(view, 1, true);
        return view;
    }

    public void onRefresh() {
        onRequest(getView(), 1, true);
    }

    public void onRequest(View view, int page, boolean notify) {
        if (view == null) {
            return;
        }

        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta != null) {
            RealmResults<Evento> results = database.where(Evento.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findAll();

            List<Evento> eventos = database.pagination(results, page);
            informationAdapter.addAll(EventoView.factory(eventos), notify);
            informationAdapter.showMessageEmpty(view, R.string.pendiente_evento, R.drawable.evento);

            eventoService = new EventoService(view.getContext(), cuenta);
            progress(page == 1 ? RelativeLayout.ALIGN_PARENT_TOP : RelativeLayout.ALIGN_PARENT_BOTTOM);
            compositeDisposable.add(fetch(page)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap(this::onSave)
                    .subscribe(this::onNext, this::onError, this::onComplete));
        }
    }

    public Observable<Evento.Request> fetch(int page) {
        onScrollListener.loading(true);
        return eventoService.fetch(page);
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

        if (eventoService != null) {
            eventoService.close();
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
        return context.getString(R.string.tab_evento);
    }

    @NonNull
    private Observable<List<Evento>> onSave(@NonNull Evento.Request request) {
        if (request.getPendientes().size() == 0) {
            onScrollListener.decrease();
        }

        if (request.getTab() == null || request.getTab().getIds() == null) {
            return eventoService.save(request.getPendientes());
        }

        Long[] value = request.getTab().getIds()
                .toArray(new Long[]{});

        return eventoService.remove(value)
                .flatMap(ordenes -> eventoService.save(request.getPendientes()));
    }

    private void onNext(List<Evento> eventos) {
        informationAdapter.addAll(EventoView.factory(eventos), false);
    }

    private void onComplete() {
        progressBar.setVisibility(View.GONE);
        onScrollListener.loading(false);
        informationAdapter.notifyDataSetChanged();
        if (getView() != null) {
            informationAdapter.showMessageEmpty(getView(), R.string.pendiente_evento, R.drawable.evento);
        }
    }

    private void onError(Throwable throwable) {
        Log.e(TAG, "onError: ", throwable);
        progressBar.setVisibility(View.GONE);
        onScrollListener.loading(false);
        onScrollListener.decrease();
        if (getView() != null) {
            Snackbar.make(getView(), R.string.error_pendientes, Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private void progress(int align) {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams layoutParams
                    = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            layoutParams.addRule(align);
            progressBar.setLayoutParams(layoutParams);
        }
    }
}
