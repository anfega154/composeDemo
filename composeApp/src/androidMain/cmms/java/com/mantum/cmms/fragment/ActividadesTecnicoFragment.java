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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.mantum.R;
import com.mantum.cmms.activity.DetalleOrdenTrabajoActivity;
import com.mantum.cmms.activity.DetalleRutaTrabajoActivity;
import com.mantum.cmms.activity.DiligenciarRutaTrabajoActivity;
import com.mantum.cmms.activity.RecorridoActivity;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Asignada;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.LCxOT;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.cmms.entity.Recorrido;
import com.mantum.cmms.entity.RecorridoHistorico;
import com.mantum.cmms.service.ATNotificationService;
import com.mantum.cmms.service.OrdenTrabajoService;
import com.mantum.cmms.service.RecorridoHistoricoService;
import com.mantum.cmms.service.RecorridoService;
import com.mantum.cmms.view.EntidadView;
import com.mantum.cmms.view.OrdenTrabajoView;
import com.mantum.cmms.view.RutaTrabajoView;
import com.mantum.component.Mantum;
import com.mantum.component.OnDrawable;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.InformationAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

public class ActividadesTecnicoFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Actividades_Tecnico";

    private final static String TAG = ActividadesTecnicoFragment.class.getSimpleName();

    private Database database;

    private ProgressBar progressBar;

    private OrdenTrabajoService ordenTrabajoService;

    private Mantum.OnScrollListener onScrollListener;

    private InformationAdapter<OrdenTrabajoView, EntidadView> informationAdapter;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private ATNotificationService atNotificationService;

    private RecorridoService recorridoService;

    private RecorridoHistoricoService recorridoHistoricoService;

    private SwipeRefreshLayout swipeRefreshLayout;

    private List<OrdenTrabajoView> ordenesPendientesLC = new ArrayList<>();
    private List<OrdenTrabajoView> currentListado = new ArrayList<>();

    public InformationAdapter<RutaTrabajoView, EntidadView> lcAdapter;

    private AlertDialog.Builder dialogGeneral;

    private Long ID_OT_SELECTED;

    private int allLC = 0, currentPagination = 1;

    private final String SELECCIONAR = "Seleccionar";

    private Cuenta cuenta;

    private final List<OrdenTrabajo> ordenTrabajo = new ArrayList<>();

    private SwitchCompat switchCompat;
    private TextView contador;
    private boolean cleanList = false;

    public ActividadesTecnicoFragment() {
        if (getContext() != null) {
            this.recorridoHistoricoService = new RecorridoHistoricoService(getContext());
        }
    }

    public LinearLayoutManager getLayout(@NonNull Context context) {
        return Mantum.isTablet(context)
                ? new GridLayoutManager(context, 2)
                : new LinearLayoutManager(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            super.onCreateView(inflater, container, savedInstanceState);
            View view = inflater.inflate(R.layout.at_information_layout_view, container, false);

            recorridoHistoricoService = new RecorridoHistoricoService(view.getContext());

            progressBar = view.findViewById(R.id.progressBar);
            database = new Database(view.getContext());
            atNotificationService = new ATNotificationService(view.getContext());
            recorridoService = new RecorridoService(view.getContext(), RecorridoService.Tipo.OT);

            informationAdapter = new InformationAdapter<>(view.getContext());
            informationAdapter.setDrawable(value -> R.drawable.orden_trabajo);
            informationAdapter.showMessageEmpty(view, R.string.pendiente_ordenes_trabajo, R.drawable.evento);

            lcAdapter = new InformationAdapter<>(view.getContext());
            lcAdapter.setDrawable(value -> R.drawable.ruta_trabajo_search);
            dialogGeneral = new AlertDialog.Builder(view.getContext());
            contador = view.findViewById(R.id.contador);

            swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
            swipeRefreshLayout.setOnRefreshListener(() -> {
                if (currentPagination > 1) {
                    cleanList = true;
                    for (int i = 1; i <= currentPagination; i++) {
                        ActividadesTecnicoFragment.this.onRequest(view, i, true);
                    }
                } else {
                    ActividadesTecnicoFragment.this.onRequest(view, 1, true);
                }
            });

            informationAdapter.setOnAction(new OnSelected<OrdenTrabajoView>() {

                @Override
                public void onClick(OrdenTrabajoView value, int position) {
                    Bundle bundle = new Bundle();
                    bundle.putString(Mantum.KEY_UUID, value.getUUID());
                    bundle.putLong(Mantum.KEY_ID, value.getId());

                    if (value.isCienPorciento() || recorridoService.pendientes(value.getId())) {
                        Intent intent = new Intent(view.getContext(), DetalleOrdenTrabajoActivity.class);
                        bundle.putBoolean(DetalleOrdenTrabajoActivity.OCULTAR_REMOVER, true);
                        intent.putExtras(bundle);

                        startActivity(intent);
                        return;
                    }

                    Intent intent = new Intent(view.getContext(), RecorridoActivity.class);
                    intent.putExtras(bundle);

                    startActivity(intent);
                }

                @Override
                public boolean onLongClick(OrdenTrabajoView value, int position) {
                    return false;
                }

            });

            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            ATNotificationService atNotificationService = new ATNotificationService(view.getContext());

            view.findViewById(R.id.disponible).setOnClickListener(v -> {
                ATNotificationService.Estado ultimoEstado = atNotificationService.getEstadoActual();
                if (ultimoEstado.isDiponible()) {
                    Snackbar.make(view, view.getContext().getString(R.string.mensaje_estado_actual), Snackbar.LENGTH_LONG).show();
                    return;
                }

                boolean mostrarDialog = false;
                String text = null;

                RecorridoHistorico novedad = recorridoHistoricoService.findNovedad();
                if (novedad != null && novedad.getCategoria() != null && novedad.getCategoria().equals(novedad.getEstado())) {
                    Long ot = novedad.getIdentidad();
                    OrdenTrabajo ot_recorrido = database.where(OrdenTrabajo.class)
                            .equalTo("id", ot)
                            .findFirst();

                    if (ot_recorrido != null && ot_recorrido.isMovimiento()) {
                        text = "Tienes una novedad activa en la: " + ot_recorrido.getCodigo() + ", ¿Deseas cancelarla?";
                    } else {
                        text = "Tienes una novedad activa, ¿Deseas cancelarla?";
                    }
                    mostrarDialog = true;

                } else {
                    Recorrido actual = recorridoService.obtenerActual();
                    if (actual != null) {
                        Long ot = actual.getIdmodulo();
                        OrdenTrabajo ot_recorrido = database.where(OrdenTrabajo.class)
                                .equalTo("id", ot)
                                .findFirst();

                        if (ot_recorrido != null) {
                            text = "Tienes un recorrido activo en la: " + ot_recorrido.getCodigo() + ", ¿Deseas cancelarlo?";
                        } else {
                            text = "Tienes un recorrido activo, ¿Deseas cancelarlo?";
                        }
                        mostrarDialog = true;
                    }
                }

                if (mostrarDialog) {
                    dialogGeneral.setNegativeButton(R.string.aceptar, (dialog, which) -> {
                        atNotificationService.cancelar((value, error) -> {
                            Snackbar.make(view, value, Snackbar.LENGTH_LONG)
                                    .show();
                            setEstadoUsuario(ATNotificationService.DISPONIBLE.getMostrar(getContext()), view);
                        }, ATNotificationService.DISPONIBLE, false);
                    });
                    dialogGeneral.setPositiveButton(R.string.close, null);
                    dialogGeneral.setMessage(text);
                    dialogGeneral.setCancelable(false);
                    dialogGeneral.setTitle(R.string.devolver_estado_disponible);
                    dialogGeneral.show();
                    return;
                }

                atNotificationService.disponible((value, error) -> {
                    Snackbar.make(view, value, Snackbar.LENGTH_LONG)
                            .show();
                    setEstadoUsuario(ATNotificationService.DISPONIBLE.getMostrar(getContext()), view);
                }, false);
            });

            view.findViewById(R.id.no_disponible).setOnClickListener(v -> {
                ATNotificationService.Estado ultimoEstado = atNotificationService.getEstadoActual();
                if (ultimoEstado.isNoDisponible()) {
                    Snackbar.make(view, view.getContext().getString(R.string.mensaje_estado_actual), Snackbar.LENGTH_LONG).show();
                    return;
                }

                atNotificationService.noDisponible((value, error) -> {
                    Snackbar.make(view, value, Snackbar.LENGTH_LONG)
                            .show();
                    setEstadoUsuario(ATNotificationService.NO_DISPONIBLE.getMostrar(getContext()), view);
                });
            });

            LinearLayoutManager layoutManager = getLayout(view.getContext());
            onScrollListener = new Mantum.OnScrollListener(layoutManager, false) {

                @Override
                public void onRequest(int page) {
                    currentPagination = page;
                    ActividadesTecnicoFragment.this.onRequest(view, page, false);
                }
            };

            RecyclerView recyclerView = informationAdapter.startAdapter(view, layoutManager);
            recyclerView.addOnScrollListener(onScrollListener);

            onRequest(view, 1, true);
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(true);
            }

            switchCompat = view.findViewById(R.id.ocultar_100);
            switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (cuenta == null) {
                    return;
                }

                informationAdapter.clear();

                if (isChecked) {
                    for (OrdenTrabajoView asignada : currentListado) {
                        if (!asignada.isCienPorciento()) {
                            informationAdapter.add(asignada);
                        }
                    }
                } else {
                    informationAdapter.addAll(currentListado);
                }

                informationAdapter.notifyDataSetChanged();
                informationAdapter.showMessageEmpty(view, R.string.pendiente_ordenes_trabajo, R.drawable.evento);
                contador.setText(String.valueOf(informationAdapter.getItemCount()));
            });

            view.findViewById(R.id.pendinglc).setOnClickListener(v -> showPopupLC());

            return view;
        } catch (Exception e) {
            Log.e(TAG, "onCreateView: ", e);
            return null;
        }
    }

    public void validarLCPendientes(View view, List<OrdenTrabajoView> asignadas) {
        ordenesPendientesLC.clear();
        allLC = 0;
        List<RutaTrabajoView> sdMarcar = new ArrayList<>();

        try {
            for (OrdenTrabajoView ot : asignadas) {
                if (ot.getListaChequeo() != null && ot.getListaChequeo().size() > 0) {
                    List<RutaTrabajoView> listaLC = ot.getListaChequeo();
                    Long idot = ot.getId();
                    for (RutaTrabajoView lc : listaLC) {
                        lc.setIdot(idot);
                        if (!lc.isDiligenciada()) {
                            allLC++;
                            sdMarcar.add(lc);
                        }
                    }
                    ot.setListaChequeo(listaLC);
                    ordenesPendientesLC.add(ot);
                }
            }

            lcAdapter.setSelectedMultiple(sdMarcar);

        } catch (Exception e) {
            Log.d(TAG, "validarLCPendientes: " + e);
        }

        TextView count = view.findViewById(R.id.badge_btn_3);
        count.setText(String.valueOf(allLC));

    }

    public void onRefresh() {
        if (swipeRefreshLayout.isRefreshing()) {
            return;
        }

        if (swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }

        if (currentPagination > 1) {
            cleanList = true;
            for (int i = 1; i <= currentPagination; i++) {
                onRequest(getView(), i, true);
            }
        } else {
            onRequest(getView(), 1, true);
        }
    }

    public void setEstadoUsuario(@Nullable String newState, View view) {
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return;
        }

        String actual_state = null;
        String actual_date_state = null;
        if (newState == null) {
            Recorrido recorrido = recorridoService.obtenerActual();
            if (recorrido == null) {
                actual_state = cuenta.isDisponible() ? ATNotificationService.DISPONIBLE.getMostrar(getContext()) : ATNotificationService.NO_DISPONIBLE.getMostrar(getContext());
            } else {
                RecorridoHistorico historico = recorridoHistoricoService.findNovedad();
                if (historico != null) {
                    if (historico.getCategoria() != null)
                        actual_state = "Reporta: " + historico.getCategoria();
                    else
                        actual_state = historico.getEstado();

                    if (historico.getDate() != null)
                        actual_date_state = "Fecha estado: " + historico.getDate();
                }
            }
        } else {
            actual_state = newState;
        }

        TextView state = view.findViewById(R.id.current_state);
        state.setText(actual_state);
        TextView date_state = view.findViewById(R.id.date_state);
        if (actual_date_state != null) {
            date_state.setText(actual_date_state);
            date_state.setVisibility(View.VISIBLE);
        } else {
            date_state.setVisibility(View.GONE);
        }

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void onRequest(View view, int page, boolean notify) {
        try {
            if (view == null || database.isClosed()) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                return;
            }

            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                return;
            }

            ordenTrabajoService = new OrdenTrabajoService(view.getContext(), cuenta);

            if (Mantum.isConnectedOrConnecting(view.getContext())) {

                if (page > 1) {
                    progress();
                }

                compositeDisposable.add(fetch(page)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap(this::onSave)
                        .subscribe(this::onNext, this::onError, this::onComplete));
            } else {
                Handler handler = new Handler();
                handler.post(() -> {
                    if (ordenTrabajo.isEmpty()) {
                        addSimpleOffline();
                    }

                    final List<OrdenTrabajo> ordenTrabajoAdd = database.pagination(ordenTrabajo, page);
                    informationAdapter.addAll(OrdenTrabajoView.factory(ordenTrabajoAdd, true), notify);
                    onComplete();
                });

            }

        } catch (Exception e) {
            Log.e(TAG, "onRequest: ", e);
        }
    }

    private void markCurrentActivity() {
        if (getView() != null && informationAdapter != null) {

            OrdenTrabajoView actual = null;
            Recorrido recorrido = recorridoService.obtener();

            for (OrdenTrabajoView ot : informationAdapter.getOriginal()) {
                if (recorrido != null && ot.getId().equals(recorrido.getIdmodulo())) {
                    actual = ot;
                    break;
                }
            }
            informationAdapter.setSelected(actual);
        }
    }


    private void addSimpleOffline() {
        if (getView() != null && informationAdapter != null) {

            List<Asignada> asignadas = database.where(Asignada.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .sort("orden")
                    .findAll();

            for (Asignada asignada : asignadas) {
                if (asignada == null) {
                    continue;
                }

                asignada = asignada.isManaged()
                        ? database.copyFromRealm(asignada)
                        : asignada;

                OrdenTrabajo value = asignada.getOrdenTrabajo();
                if (value != null) {
                    if (switchCompat.isChecked() && value.isCienPorciento()) {
                        continue;
                    }

                    value.setTerminada(asignada.isTerminada());
                    ordenTrabajo.add(value);
                }
            }
        }
    }

    public Observable<OrdenTrabajo.Request> fetch(int page) {
        if (onScrollListener != null && !onScrollListener.isLoading()) {
            onScrollListener.loading(true);
        }
        return ordenTrabajoService.fetchPendingAT(page);
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

        if (ordenTrabajoService != null) {
            ordenTrabajoService.close();
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
        return context.getString(R.string.tab_actividades_tecnico);
    }

    @NonNull
    private Observable<List<OrdenTrabajo>> onSave(@NonNull OrdenTrabajo.Request request) {

        if (request.getPendientes().size() == 0) {
            onScrollListener.decrease();
        }

        if (request.getTab() == null || request.getTab().getIds() == null) {
            return ordenTrabajoService.save(request.getPendientes());
        }

        Long[] value = request.getTab().getIds()
                .toArray(new Long[]{});

        List<LCxOT> lcxot = request.getLcxot();
        if (lcxot != null && lcxot.size() > 0) {
            database.executeTransaction(self -> {

                self.where(LCxOT.class).findAll().deleteAllFromRealm();

                for (LCxOT row : lcxot) {
                    LCxOT newRow = new LCxOT();
                    newRow.setIdgrupoam(row.getIdgrupoam());
                    newRow.setIdrt(row.getIdrt());
                    newRow.setIdot(row.getIdot());
                    self.insert(newRow);
                }
            });
        }

        return ordenTrabajoService.remove(value, true)
                .map(this::onRemove)
                .flatMap(actividades -> ordenTrabajoService.save(request.getPendientes()));
    }

    private List<Long> onRemove(List<Long> remove) {
        for (Long id : remove) {
            informationAdapter.remove(value -> value.getId().equals(id));
        }
        return remove;
    }

    private int compare(@NonNull OrdenTrabajo a, @NonNull OrdenTrabajo b) {
        if (a.getOrden() == b.getOrden()) {
            return 0;
        }
        return a.getOrden() > b.getOrden() ? 1 : -1;
    }

    private int compare(@NonNull OrdenTrabajoView a, @NonNull OrdenTrabajoView b) {
        return a.getId() < b.getId() ? 1 : -1;
    }

    private void onNext(List<OrdenTrabajo> ordenes) {

        if (cleanList) {
            currentListado.clear();
            informationAdapter.clear();
            cleanList = false;
        }

        Collections.sort(ordenes, this::compare);
        currentListado.addAll(OrdenTrabajoView.factory(ordenes, true));

        if (getView() == null) {
            informationAdapter.addAll(OrdenTrabajoView.factory(ordenes, true), false);
            return;
        }

        SwitchCompat switchCompat = getView().findViewById(R.id.ocultar_100);
        for (OrdenTrabajo value : ordenes) {
            if (switchCompat.isChecked() && value.isCienPorciento()) {
                continue;
            }
            informationAdapter.add(OrdenTrabajoView.factory(value, true), false);
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
            informationAdapter.showMessageEmpty(getView(), R.string.pendiente_ordenes_trabajo, R.drawable.evento);
            validarLCPendientes(getView(), informationAdapter.getOriginal());
            informationAdapter.sort(this::compare);
            contador.setText(String.valueOf(informationAdapter.getItemCount()));

            if (allLC > 0) {
                getView().findViewById(R.id.badge_lc).setVisibility(View.VISIBLE);
                Snackbar.make(getView(), R.string.lista_chequeo_pendiente, Snackbar.LENGTH_LONG)
                        .show();
            } else {
                if (ordenesPendientesLC.size() == 0)
                    getView().findViewById(R.id.badge_lc).setVisibility(View.GONE);
            }

            setEstadoUsuario(null, getView());
            markCurrentActivity();
        }
    }

    private void onError(Throwable throwable) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }


        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        if (onScrollListener != null) {
            onScrollListener.loading(false);
            onScrollListener.decrease();
        }

        if (informationAdapter != null) {
            informationAdapter.notifyDataSetChanged();
        }

        if (getView() != null) {
            String message = throwable.getMessage() != null ? throwable.getMessage() : "";
            if (message.equals(getString(R.string.error_pendientes_ot_asignadas))) {
                new AlertDialog.Builder(getView().getContext())
                        .setTitle(R.string.titulo_problemas_conexion)
                        .setMessage(R.string.mensaje_problemas_conexion)
                        .setNegativeButton(R.string.close, (dialogInterface1, id) -> dialogInterface1.dismiss())
                        .setCancelable(false)
                        .show();
            }

            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
        }
    }

    private void progress() {
        if (progressBar == null) {
            return;
        }

        RelativeLayout.LayoutParams layoutParams
                = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setLayoutParams(layoutParams);
    }

    private void showPopupLC() {

        try {

            List<String> tipos = new ArrayList<>();
            for (OrdenTrabajoView ots : ordenesPendientesLC) {
                tipos.add(ots.getCodigo());
                lcAdapter.addAll(ots.getListaChequeo(), true);
            }

            lcAdapter.showMessageEmpty(getView(), R.string.pendiente_ruta_trabajo, R.drawable.ruta);
            lcAdapter.setOnAction(new OnSelected<RutaTrabajoView>() {
                @Override
                public void onClick(RutaTrabajoView value, int position) {

                    Bundle bundle = new Bundle();
                    bundle.putLong(Mantum.KEY_ID, value.getId());
                    bundle.putString(Mantum.KEY_UUID, value.getUUID());
                    bundle.putLong(DiligenciarRutaTrabajoActivity.ID_EXTRA, ID_OT_SELECTED);
                    bundle.putBoolean(DiligenciarRutaTrabajoActivity.ACCION_PARCIAL, true);
                    bundle.putString(DiligenciarRutaTrabajoActivity.TIPO, value.getTipo());

                    if (value.getIdEjecucion() != null) {
                        bundle.putLong(DetalleRutaTrabajoActivity.ID_EJECUCION, value.getIdEjecucion());
                    }

                    Intent intent = new Intent(getView().getContext(), DiligenciarRutaTrabajoActivity.class);
                    intent.putExtras(bundle);

                    startActivityForResult(intent, DiligenciarRutaTrabajoActivity.REQUEST_ACTION);
                }

                @Override
                public boolean onLongClick(RutaTrabajoView value, int position) {
                    return false;
                }
            });

            LayoutInflater factory = LayoutInflater.from(getContext());
            View vistaLC = factory.inflate(R.layout.dialog_entidades, null);
            vistaLC.findViewById(R.id.container_filter).setVisibility(View.VISIBLE);

            Spinner filter = vistaLC.findViewById(R.id.mSpinner);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, tipos);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            filter.setAdapter(arrayAdapter);

            filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String codeOT = filter.getSelectedItem().toString();
                    for (OrdenTrabajoView ots : ordenesPendientesLC) {
                        if (!codeOT.equals(SELECCIONAR) && codeOT.equals(ots.getCodigo())) {
                            lcAdapter.clear();
                            lcAdapter.addAll(ots.getListaChequeo(), true);
                            lcAdapter.showMessageEmpty(getView(), R.string.pendiente_ruta_trabajo, R.drawable.ruta);
                            ID_OT_SELECTED = ots.getId();
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            RecyclerView recyclerView = vistaLC.findViewById(R.id.listaEntidades);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemViewCacheSize(10);
            recyclerView.setDrawingCacheEnabled(true);
            recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(lcAdapter);

            dialogGeneral.setNegativeButton(R.string.close, null);
            dialogGeneral.setPositiveButton(null, null);
            dialogGeneral.setView(vistaLC);
            dialogGeneral.setTitle(R.string.lista_chequeo);
            dialogGeneral.setMessage(null);
            dialogGeneral.show();

        } catch (Exception e) {
            Log.d(TAG, "showPopupLC: " + e);
        }

    }

}