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

import com.mantum.R;
import com.mantum.cmms.activity.DetalleRutaTrabajoActivity;
import com.mantum.cmms.activity.DiligenciarRutaTrabajoActivity;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.RutaTrabajo;
import com.mantum.cmms.service.RutaTrabajoService;
import com.mantum.cmms.view.EntidadView;
import com.mantum.cmms.view.RutaTrabajoView;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.InformationAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.RealmResults;
import io.realm.Sort;

public class RutaTrabajoFragment extends Mantum.Fragment {

    private final static String TAG = RutaTrabajoFragment.class.getSimpleName();

    public final static String KEY_TAB = "Ruta_Trabajo";

    private long idExtra;

    private Database database;

    private ProgressBar progressBar;

    private SwipeRefreshLayout swipeRefreshLayout;

    private boolean parcial;

    public boolean isListaChequeo() {
        return listaChequeo;
    }

    public void setListaChequeo(boolean listaChequeo) {
        this.listaChequeo = listaChequeo;
    }

    private boolean listaChequeo;

    private boolean modoVerDetalle;

    private boolean accionActualizar;

    private boolean realizarPeticionHttp;

    private RutaTrabajoService rutaTrabajoService;

    private OnCompleteListener onCompleteListener;

    private Mantum.OnScrollListener onScrollListener;

    public InformationAdapter<RutaTrabajoView, EntidadView> informationAdapter;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private View view;

    public RutaTrabajoFragment() {
        this.idExtra = -1;
        this.modoVerDetalle = false;
        this.accionActualizar = true;
        this.realizarPeticionHttp = true;
        this.parcial = false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(com.mantum.component.R.layout.information_layout_view,
                container, false);

        progressBar = view.findViewById(R.id.progressBar);
        database = new Database(view.getContext());

        informationAdapter = new InformationAdapter<>(view.getContext());
        informationAdapter.setDrawable(value -> R.drawable.ruta_trabajo_search);

        if (accionActualizar) {
            informationAdapter.setMenu(R.menu.menu_ruta_trabajo);
            informationAdapter.setOnCall((menu, value) -> {
                if (value == null) {
                    return true;
                }

                Intent intent;
                Bundle bundle;
                switch (menu.getItemId()) {
                    case R.id.refresh:
                        compositeDisposable.add(rutaTrabajoService.fetchById(value.getId(), value.getIdEjecucion())
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .flatMap(rutaTrabajos -> rutaTrabajos.isEmpty()
                                        ? rutaTrabajoService.remove(value.getId(), value.getIdEjecucion())
                                        : rutaTrabajoService.save(rutaTrabajos))
                                .subscribe(this::onNextUpdate, this::onError, this::onComplete)
                        );
                        break;

                    case R.id.diligenciar:
                        bundle = new Bundle();
                        bundle.putLong(Mantum.KEY_ID, value.getId());
                        bundle.putString(Mantum.KEY_UUID, value.getUUID());
                        bundle.putLong(DiligenciarRutaTrabajoActivity.ID_EXTRA, idExtra);
                        bundle.putString(DiligenciarRutaTrabajoActivity.TIPO, value.getTipo());

                        if (value.getIdEjecucion() != null) {
                            bundle.putLong(DetalleRutaTrabajoActivity.ID_EJECUCION, value.getIdEjecucion());
                        }

                        intent = new Intent(view.getContext(), DiligenciarRutaTrabajoActivity.class);
                        intent.putExtras(bundle);

                        startActivityForResult(intent, DiligenciarRutaTrabajoActivity.REQUEST_ACTION);
                        break;


                    case R.id.remove:
                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

                        builder.setTitle(R.string.titulo_eliminar_ruta_trabajo);
                        builder.setMessage(R.string.mensaje_eliminar_ruta_trabajo);

                        builder.setNegativeButton(R.string.aceptar, (dialog, which) -> {
                            if (rutaTrabajoService != null) {
                                compositeDisposable.add(rutaTrabajoService.remove(value.getId(), value.getIdEjecucion())
                                        .subscribe(ordenTrabajos -> this.onRemove(value), this::onError, this::onComplete));
                            }
                        });
                        builder.setPositiveButton(R.string.cancelar, (dialog, which) -> dialog.dismiss());

                        builder.setCancelable(true);
                        builder.create();
                        builder.show();
                        break;
                }
                return true;
            });
        }


        informationAdapter.setOnAction(new OnSelected<RutaTrabajoView>() {

            @Override
            public void onClick(RutaTrabajoView value, int position) {
                if (modoVerDetalle) {
                    Bundle bundle = new Bundle();
                    bundle.putLong(Mantum.KEY_ID, value.getId());
                    bundle.putString(Mantum.KEY_UUID, value.getUUID());
                    bundle.putLong(DetalleRutaTrabajoActivity.ID_EXTRA, idExtra);

                    if (value.getIdEjecucion() != null) {
                        bundle.putLong(DetalleRutaTrabajoActivity.ID_EJECUCION, value.getIdEjecucion());
                    }

                    Intent intent = new Intent(view.getContext(), DetalleRutaTrabajoActivity.class);
                    intent.putExtras(bundle);

                    startActivityForResult(intent, 0);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putLong(Mantum.KEY_ID, value.getId());
                    bundle.putString(Mantum.KEY_UUID, value.getUUID());
                    bundle.putLong(DiligenciarRutaTrabajoActivity.ID_EXTRA, idExtra);
                    bundle.putBoolean(DiligenciarRutaTrabajoActivity.ACCION_PARCIAL, isParcial());
                    bundle.putString(DiligenciarRutaTrabajoActivity.TIPO, value.getTipo());
                    if (value.getIdEjecucion() != null) {
                        bundle.putLong(DetalleRutaTrabajoActivity.ID_EJECUCION, value.getIdEjecucion());
                    }

                    Intent intent = new Intent(view.getContext(), DiligenciarRutaTrabajoActivity.class);
                    intent.putExtras(bundle);

                    startActivityForResult(intent, DiligenciarRutaTrabajoActivity.REQUEST_ACTION);
                }
            }

            @Override
            public boolean onLongClick(RutaTrabajoView value, int position) {
                return false;
            }

        });

        if (accionActualizar) {
            informationAdapter.setMenuVisibility(R.id.refresh, true);
            informationAdapter.setMenuVisibility(R.id.remove, true);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        onScrollListener = new Mantum.OnScrollListener(layoutManager, false) {

            @Override
            public void onRequest(int page) {
                RutaTrabajoFragment.this.onRequest(view, page, false);
            }
        };

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setEnabled(false);

        RecyclerView recyclerView = informationAdapter.startAdapter(view, layoutManager);
        if (realizarPeticionHttp) {
            swipeRefreshLayout.setEnabled(true);
            swipeRefreshLayout.setOnRefreshListener(() -> {
                if (!Mantum.isConnectedOrConnecting(view.getContext())) {
                    swipeRefreshLayout.setRefreshing(false);
                    Snackbar.make(view, R.string.offline, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                RutaTrabajoFragment.this.onRequest(view, 1, true);
            });

            recyclerView.addOnScrollListener(onScrollListener);
            onRequest(view, 1, true);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onCompleteListener.onComplete(KEY_TAB);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onCompleteListener = (OnCompleteListener) context;
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

        RealmResults<RutaTrabajo> results = database.where(RutaTrabajo.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .isNotNull("idejecucion")
                .sort(new String[]{"fecha", "codigo"}, new Sort[]{Sort.DESCENDING, Sort.ASCENDING})
                .findAll();

        List<RutaTrabajo> rutaTrabajos = database.pagination(results, page);
        if (Mantum.isConnectedOrConnecting(view.getContext())) {
            informationAdapter.addAll(RutaTrabajoView.factory(rutaTrabajos), notify);
            informationAdapter.showMessageEmpty(view, R.string.pendiente_ruta_trabajo, R.drawable.ruta);

            rutaTrabajoService = new RutaTrabajoService(view.getContext(), cuenta);
            progress();

            compositeDisposable.add(fetch(page)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap(this::onSave)
                    .subscribe(this::onNext, this::onError, this::onComplete));
        } else {
            if (rutaTrabajos.size() == 0) {
                onScrollListener.decrease();
            }

            informationAdapter.addAll(RutaTrabajoView.factory(rutaTrabajos), notify);
            onScrollListener.loading(false);
            Handler handler = new Handler();
            handler.post(() -> {
                informationAdapter.notifyDataSetChanged();
                informationAdapter.showMessageEmpty(view, R.string.pendiente_ruta_trabajo, R.drawable.ruta);
            });
        }
    }

    public Observable<RutaTrabajo.Request> fetch(int page) {
        onScrollListener.loading(true);
        return rutaTrabajoService.fetch(page);
    }

    public void onLoad(List<RutaTrabajo> rutaTrabajos) {
        if (informationAdapter != null && getView() != null) {
            informationAdapter.addAll(RutaTrabajoView.factory(rutaTrabajos), true);
            informationAdapter.showMessageEmpty(getView(), R.string.pendiente_ruta_trabajo, R.drawable.ruta);
        }
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

        if (rutaTrabajoService != null) {
            rutaTrabajoService.close();
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
        return context.getString(!isListaChequeo() ? R.string.tab_RT : R.string.tab_LC);
    }

    @NonNull
    private Observable<List<RutaTrabajo>> onSave(@NonNull RutaTrabajo.Request request) {
        if (request.getPendientes().size() == 0) {
            onScrollListener.decrease();
        }

        if (request.getTab() == null || request.getTab().getIds() == null) {
            return rutaTrabajoService.save(request.getPendientes());
        }

        Long[] value = request.getTab().getIds()
                .toArray(new Long[]{});

        return rutaTrabajoService.removeByIdEjecucion(value)
                .map(this::onRemove)
                .flatMap(rutas -> rutaTrabajoService.save(request.getPendientes()));
    }

    private void onRemove(RutaTrabajoView rutaTrabajoView) {
        if (rutaTrabajoView == null) {
            return;
        }

        if (informationAdapter != null) {
            informationAdapter.remove(rutaTrabajoView, false);
        }
    }

    private List<Long> onRemove(List<Long> remove) {
        for (Long id : remove) {
            informationAdapter.remove(value -> value.getIdEjecucion() != null && value.getIdEjecucion().equals(id));
        }
        return remove;
    }

    private void onNext(List<RutaTrabajo> rutaTrabajos) {
        Database database = new Database(view.getContext());
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return;
        }

        List<RutaTrabajoView> resultado = new ArrayList<>();
        for (RutaTrabajo rutaTrabajo : rutaTrabajos) {
            if (rutaTrabajo == null) {
                continue;
            }

            RutaTrabajo result = database.where(RutaTrabajo.class)
                    .equalTo("id", rutaTrabajo.getId())
                    .equalTo("idejecucion", rutaTrabajo.getIdejecucion())
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findFirst();

            RutaTrabajoView value = RutaTrabajoView.factory(rutaTrabajo);

            if (result == null) {
                informationAdapter.remove(value);
                continue;
            }
            resultado.add(value);
        }
        informationAdapter.addAll(resultado, false);

        database.close();
    }

    private void onNextUpdate(List<RutaTrabajo> rutaTrabajos) {
        if (informationAdapter != null) {
            informationAdapter.addAll(RutaTrabajoView.factory(rutaTrabajos), false);
        }
    }

    private void onError(Throwable throwable) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            });
        }

        if (onScrollListener != null) {
            onScrollListener.loading(false);
            onScrollListener.decrease();
        }

        if (informationAdapter != null) {
            informationAdapter.notifyDataSetChanged();
        }

        if (getView() != null) {
            Snackbar.make(getView(), R.string.error_pendientes, Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private void onComplete() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        if (onScrollListener != null) {
            onScrollListener.loading(false);
        }

        if (informationAdapter != null) {
            informationAdapter.notifyDataSetChanged();
        }

        if (getView() != null) {
            informationAdapter.showMessageEmpty(getView(), R.string.pendiente_ruta_trabajo, R.drawable.ruta);
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

    public void setIdExtra(long idExtra) {
        this.idExtra = idExtra;
    }

    public void setRealizarPeticionHttp(boolean realizarPeticionHttp) {
        this.realizarPeticionHttp = realizarPeticionHttp;
    }

    public void setAccionActualizar(boolean accionActualizar) {
        this.accionActualizar = accionActualizar;
    }

    public RutaTrabajoFragment setModoVerDetalle(boolean modoVerDetalle) {
        this.modoVerDetalle = modoVerDetalle;
        return this;
    }

    public boolean isParcial() {
        return parcial;
    }

    public void setParcial(boolean parcial) {
        this.parcial = parcial;
    }
}