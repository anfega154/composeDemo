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

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.mantum.R;
import com.mantum.cmms.activity.BitacoraActivity;
import com.mantum.cmms.activity.SolicitudServicioComentarActivity;
import com.mantum.cmms.activity.DetalleSolicitudServicioActivity;
import com.mantum.cmms.activity.EstadoInicialActivity;
import com.mantum.cmms.activity.EvaluarActivity;
import com.mantum.cmms.activity.InformeTecnicoActivity;
import com.mantum.cmms.activity.RecibirSolicitudServicioActivity;
import com.mantum.cmms.activity.SolicitudServicioActivity;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Busqueda;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.SolicitudServicio;
import com.mantum.cmms.security.Security;
import com.mantum.cmms.service.SolicitudServicioService;
import com.mantum.cmms.view.EntidadView;
import com.mantum.cmms.view.SolicitudServicioView;
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

public class SolicitudServicioFragment extends Mantum.Fragment {

    private final static String TAG = SolicitudServicioFragment.class.getSimpleName();

    public final static String KEY_TAB = "Solicitud_Servicio";

    private Database database;

    private ProgressBar progressBar;

    private SolicitudServicioService solicitudServicioService;

    private Mantum.OnScrollListener onScrollListener;

    private InformationAdapter<SolicitudServicioView, EntidadView> informationAdapter;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_solicitud_servicio, container, false);

        progressBar = view.findViewById(R.id.progressBar);
        database = new Database(view.getContext());

        informationAdapter = new InformationAdapter<>(view.getContext());
        informationAdapter.setDrawable(value -> R.drawable.solicitud_servicio_search);
        informationAdapter.setMenu(R.menu.menu_solicitud_servicio);
        informationAdapter.setOnCall((menu, value) -> {
            Intent intent;
            Bundle bundle;
            switch (menu.getItemId()) {
                case R.id.refresh:
                    if (solicitudServicioService != null) {
                        compositeDisposable.add(solicitudServicioService.fetchById(value.getId())
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .flatMap(solicitudServicios -> solicitudServicios.isEmpty()
                                        ? solicitudServicioService.remove(value.getId())
                                        : solicitudServicioService.save(solicitudServicios))
                                .subscribe(this::onNext, this::onError, this::onComplete)
                        );
                    }
                    break;

                case R.id.solicitud_servicio_bitacora:
                    bundle = new Bundle();
                    bundle.putLong(BitacoraActivity.KEY_ID, value.getId());
                    bundle.putString(BitacoraActivity.KEY_CODIGO, value.getCodigo());
                    bundle.putInt(BitacoraActivity.KEY_TIPO_BITACORA, BitacoraActivity.SS);

                    intent = new Intent(view.getContext(), BitacoraActivity.class);
                    intent.putExtras(bundle);

                    startActivityForResult(intent, BitacoraActivity.REQUEST_ACTION);
                    break;

                case R.id.solicitud_servicio_evaluar:
                    bundle = new Bundle();
                    bundle.putLong(EvaluarActivity.KEY_ID, value.getId());
                    bundle.putString(EvaluarActivity.KEY_CODIGO, value.getCodigo());

                    intent = new Intent(view.getContext(), EvaluarActivity.class);
                    intent.putExtras(bundle);

                    startActivityForResult(intent, EvaluarActivity.REQUEST_ACTION);
                    break;

                case R.id.solicitud_servicio_comentario:
                    bundle = new Bundle();
                    bundle.putLong(Mantum.KEY_ID, value.getId());
                    bundle.putString(SolicitudServicioComentarActivity.KEY_CODE, value.getCodigo());

                    intent = new Intent(view.getContext(), SolicitudServicioComentarActivity.class);
                    intent.putExtras(bundle);

                    startActivityForResult(intent, SolicitudServicioComentarActivity.REQUEST_ACTION);
                    break;

                case R.id.solicitud_servicio_recibir:
                    bundle = new Bundle();
                    bundle.putLong(RecibirSolicitudServicioActivity.KEY_ID, value.getId());
                    bundle.putString(RecibirSolicitudServicioActivity.KEY_CODIGO, value.getCodigo());

                    intent = new Intent(view.getContext(), RecibirSolicitudServicioActivity.class);
                    intent.putExtras(bundle);

                    startActivityForResult(intent, RecibirSolicitudServicioActivity.REQUEST_ACTION);
                    break;

                case R.id.solicitud_servicio_estado_inicial:
                    bundle = new Bundle();
                    bundle.putLong(Mantum.KEY_ID, value.getId());
                    bundle.putString(EstadoInicialActivity.KEY_CODE, value.getCodigo());

                    intent = new Intent(view.getContext(), EstadoInicialActivity.class);
                    intent.putExtras(bundle);

                    startActivityForResult(intent, EstadoInicialActivity.REQUEST_ACTION);
                    break;

                case R.id.solicitud_servicio_informe_tecnico:
                    bundle = new Bundle();
                    bundle.putLong(Mantum.KEY_ID, value.getId());
                    bundle.putString(InformeTecnicoActivity.KEY_CODE, value.getCodigo());

                    intent = new Intent(view.getContext(), InformeTecnicoActivity.class);
                    intent.putExtras(bundle);

                    startActivityForResult(intent, InformeTecnicoActivity.REQUEST_ACTION);
                    break;

                case R.id.ubicar:
                    if (getView() != null && getView().getContext() != null) {
                        Cuenta cuenta = database.where(Cuenta.class)
                                .equalTo("active", true)
                                .findFirst();

                        if (cuenta != null) {
                            SolicitudServicio solicitudServicio = database.where(SolicitudServicio.class)
                                    .equalTo("id", value.getId())
                                    .equalTo("cuenta.UUID", cuenta.getUUID())
                                    .findFirst();

                            if (solicitudServicio != null && solicitudServicio.getGmap() != null && !solicitudServicio.getGmap().isEmpty()) {
                                Mantum.goGoogleMap(getView().getContext(), solicitudServicio.getGmap());
                            }
                        }

                        Snackbar.make(getView(), R.string.sin_coordenada_google_map, Snackbar.LENGTH_LONG)
                                .show();
                    }
                    break;

                case R.id.remove:
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

                    builder.setTitle(R.string.titulo_eliminar_solicitud_servicio);
                    builder.setMessage(R.string.mensaje_eliminar_solicitud_servicio);

                    builder.setNegativeButton(R.string.aceptar, (dialog, which) -> compositeDisposable.add(solicitudServicioService.remove(value.getId())
                            .subscribe(solicitudServicios -> this.onRemove(value), this::onError, this::onComplete)));
                    builder.setPositiveButton(R.string.cancelar, (dialog, which) -> dialog.dismiss());

                    builder.setCancelable(true);
                    builder.create();
                    builder.show();

                    break;
            }
            return true;
        });

        informationAdapter.setOnAction(new OnSelected<SolicitudServicioView>() {

            @Override
            public void onClick(SolicitudServicioView value, int position) {
                Bundle bundle = new Bundle();
                bundle.putString(Mantum.KEY_UUID, value.getUUID());
                bundle.putLong(Mantum.KEY_ID, value.getId());

                Intent intent = new Intent(view.getContext(), DetalleSolicitudServicioActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
            }

            @Override
            public boolean onLongClick(SolicitudServicioView value, int position) {
                return false;
            }

        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        onScrollListener = new Mantum.OnScrollListener(layoutManager) {

            @Override
            public void onRequest(int page) {
                SolicitudServicioFragment.this.onRequest(view, page, false);
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

            SolicitudServicioFragment.this.onRequest(view, 1, false);
        });

        RecyclerView recyclerView = informationAdapter.startAdapter(view, layoutManager);
        recyclerView.addOnScrollListener(onScrollListener);

        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta != null) {
            FloatingActionButton register = view.findViewById(R.id.register_ss);
            register.setOnClickListener(v -> {

                Busqueda busqueda = database.where(Busqueda.class)
                        .equalTo("selected", true)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();

                if (busqueda != null && !busqueda.isEmpty()) {
                    if (!Security.action(busqueda.getActions(), Security.ACTION_REGISTRAR_SS)) {
                        Snackbar.make(view, R.string.entidad_seleccionada, Snackbar.LENGTH_LONG)
                                .show();
                        return;
                    }

                    Bundle bundle = new Bundle();
                    bundle.putLong(SolicitudServicioActivity.KEY_ID, busqueda.getId());
                    bundle.putString(SolicitudServicioActivity.KEY_NAME, String.format("%s | %s", busqueda.getCode(), busqueda.getName()));
                    bundle.putString(SolicitudServicioActivity.KEY_TYPE, busqueda.getType());

                    Intent intent = new Intent(view.getContext(), SolicitudServicioActivity.class);
                    intent.putExtras(bundle);

                    startActivity(intent);
                    return;
                }

                Intent intent = new Intent(view.getContext(), SolicitudServicioActivity.class);
                startActivity(intent);
            });
        }

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

        RealmResults<SolicitudServicio> results = database.where(SolicitudServicio.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .findAll();

        List<SolicitudServicio> solicitudServicios = database.pagination(results, page);
        if (Mantum.isConnectedOrConnecting(view.getContext())) {
            informationAdapter.addAll(SolicitudServicioView.factory(solicitudServicios), notify);
            informationAdapter.showMessageEmpty(view, R.string.pendiente_solicitud_servicio, R.drawable.solicitud_servicio);

            solicitudServicioService = new SolicitudServicioService(view.getContext(), cuenta);
            if (page > 1) {
                progress();
            }

            compositeDisposable.add(fetch(page)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap(this::onSave)
                    .subscribe(this::onNext, this::onError, this::onComplete));
        } else {
            if (solicitudServicios.size() == 0) {
                onScrollListener.decrease();
            }

            informationAdapter.addAll(SolicitudServicioView.factory(solicitudServicios), notify);
            onScrollListener.loading(false);
            Handler handler = new Handler();
            handler.post(() -> {
                informationAdapter.notifyDataSetChanged();
                informationAdapter.showMessageEmpty(view, R.string.pendiente_solicitud_servicio, R.drawable.solicitud_servicio);
            });
        }
    }

    public Observable<SolicitudServicio.Request> fetch(int page) {
        onScrollListener.loading(true);
        return solicitudServicioService.fetch(page);
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

        if (solicitudServicioService != null) {
            solicitudServicioService.close();
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
        return context.getString(R.string.tab_SS);
    }

    @NonNull
    private Observable<List<SolicitudServicio>> onSave(@NonNull SolicitudServicio.Request request) {
        if (request.getPendientes().size() == 0) {
            onScrollListener.decrease();
        }

        if (request.getTab() == null || request.getTab().getIds() == null) {
            return solicitudServicioService.save(request.getPendientes());
        }

        Long[] value = request.getTab().getIds()
                .toArray(new Long[]{});

        return solicitudServicioService.remove(value)
                .map(this::onRemove)
                .flatMap(solicitures -> solicitudServicioService.save(request.getPendientes()));
    }

    private void onNext(List<SolicitudServicio> solicitudServicios) {
        informationAdapter.addAll(SolicitudServicioView.factory(solicitudServicios), false);
    }

    private void onRemove(SolicitudServicioView solicitudServicioView) {
        informationAdapter.remove(solicitudServicioView, false);
    }

    private List<Long> onRemove(List<Long> remove) {
        for (Long id : remove) {
            informationAdapter.remove(value -> value.getId().equals(id));
        }
        return remove;
    }

    private void onComplete() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }

        progressBar.setVisibility(View.GONE);
        onScrollListener.loading(false);
        informationAdapter.notifyDataSetChanged();
        if (getView() != null) {
            informationAdapter.showMessageEmpty(getView(), R.string.pendiente_solicitud_servicio, R.drawable.solicitud_servicio);
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