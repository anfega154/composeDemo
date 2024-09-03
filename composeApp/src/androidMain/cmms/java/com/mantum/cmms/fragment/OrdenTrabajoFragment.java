package com.mantum.cmms.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mantum.R;
import com.mantum.cmms.activity.ActualizarContactoActivity;
import com.mantum.cmms.activity.BitacoraActivity;
import com.mantum.cmms.activity.DescargarRutaTrabajoActivity;
import com.mantum.cmms.activity.DetalleOrdenTrabajoActivity;
import com.mantum.cmms.activity.FirmaxEntidadActivity;
import com.mantum.cmms.activity.FormularioFallaOTActivity;
import com.mantum.cmms.activity.GaleriaActivity;
import com.mantum.cmms.activity.InspeccionElectricaActivity;
import com.mantum.cmms.activity.InstalacionPlantaExternaActivity;
import com.mantum.cmms.activity.RecibirOrdenTrabajoActivity;
import com.mantum.cmms.activity.RecorridoPlantaExternaActivity;
import com.mantum.cmms.activity.RegistrarTiemposActivity;
import com.mantum.cmms.activity.TerminarOrdenTrabajoActivity;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.cmms.entity.Yarda;
import com.mantum.cmms.entity.parameter.UserParameter;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.cmms.service.OrdenTrabajoService;
import com.mantum.cmms.util.Version;
import com.mantum.cmms.view.EntidadView;
import com.mantum.cmms.view.OrdenTrabajoView;
import com.mantum.component.Mantum;
import com.mantum.component.OnDrawable;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.InformationAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.RealmQuery;
import io.realm.Sort;

public class OrdenTrabajoFragment extends Mantum.Fragment {

    private final static String TAG = OrdenTrabajoFragment.class.getSimpleName();

    public final static String KEY_TAB = "Orden_Trabajo";

    public static final String MODULO = "modulo";

    private Database database;

    private ProgressBar progressBar;

    private OrdenTrabajoService ordenTrabajoService;

    private Mantum.OnScrollListener onScrollListener;

    private SwipeRefreshLayout swipeRefreshLayout;

    private RecyclerView recyclerView;

    private TextView busquedaOtYarda;

    private InformationAdapter<OrdenTrabajoView, EntidadView> informationAdapter;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private SwitchCompat switchOcultarCien;

    private SwitchCompat switchMostrarSoloAsignadas;

    private final List<OrdenTrabajoView> currentListado = new ArrayList<>();

    private boolean busquedaPorYardas;

    private Cuenta cuenta;

    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_orden_trabajo, container, false);

        progressBar = view.findViewById(R.id.progressBar);
        database = new Database(view.getContext());

        cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        informationAdapter = new InformationAdapter<>(view.getContext());
        informationAdapter.setDrawable(value -> R.drawable.orden_trabajo);
        informationAdapter.setMenu(R.menu.menu_orden_trabajo);
        informationAdapter.setOnCall((menu, value) -> {
            Intent intent;
            Bundle bundle;
            switch (menu.getItemId()) {
                case R.id.refresh:
                    if (ordenTrabajoService == null) {
                        break;
                    }

                    compositeDisposable.add(ordenTrabajoService.fetchById(value.getId())
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .flatMap(ordenTrabajos -> {
                                if (ordenTrabajos.isEmpty()) {
                                    informationAdapter.remove(value);
                                    return ordenTrabajoService.remove(value.getId());
                                } else {
                                    return ordenTrabajoService.save(ordenTrabajos);
                                }
                            })
                            .subscribe(this::onNext, this::onError, this::onComplete)
                    );
                    break;

                case R.id.bitacora:
                    bundle = new Bundle();
                    bundle.putLong(BitacoraActivity.KEY_ID, value.getId());
                    bundle.putString(BitacoraActivity.KEY_CODIGO, value.getCodigo());
                    bundle.putInt(BitacoraActivity.KEY_TIPO_BITACORA, BitacoraActivity.OT);

                    intent = new Intent(view.getContext(), BitacoraActivity.class);
                    intent.putExtras(bundle);

                    startActivityForResult(intent, BitacoraActivity.REQUEST_ACTION);
                    break;

                case R.id.recibir:
                    bundle = new Bundle();
                    bundle.putLong(Mantum.KEY_ID, value.getId());

                    intent = new Intent(view.getContext(), RecibirOrdenTrabajoActivity.class);
                    intent.putExtras(bundle);

                    startActivityForResult(intent, RecibirOrdenTrabajoActivity.REQUEST_ACTION);
                    break;

                case R.id.tiempos:
                    bundle = new Bundle();
                    bundle.putLong(Mantum.KEY_ID, value.getId());

                    intent = new Intent(view.getContext(), RegistrarTiemposActivity.class);
                    intent.putExtras(bundle);

                    startActivityForResult(intent, RegistrarTiemposActivity.REQUEST_ACTION);
                    break;

                case R.id.inspeccion_electrica:
                    bundle = new Bundle();
                    bundle.putLong(Mantum.KEY_ID, value.getId());

                    intent = new Intent(view.getContext(), InspeccionElectricaActivity.class);
                    intent.putExtras(bundle);

                    startActivityForResult(intent, InspeccionElectricaActivity.REQUEST_ACTION);
                    break;

                case R.id.contacto:
                    bundle = new Bundle();
                    bundle.putLong(Mantum.KEY_ID, value.getId());

                    intent = new Intent(view.getContext(), ActualizarContactoActivity.class);
                    intent.putExtras(bundle);

                    startActivityForResult(intent, ActualizarContactoActivity.REQUEST_ACTION);
                    break;

                case R.id.terminar:
                    bundle = new Bundle();
                    bundle.putLong(Mantum.KEY_ID, value.getId());
                    bundle.putString(TerminarOrdenTrabajoActivity.KEY_CODE, value.getCodigo());

                    intent = new Intent(view.getContext(), TerminarOrdenTrabajoActivity.class);
                    intent.putExtras(bundle);

                    startActivityForResult(intent, TerminarOrdenTrabajoActivity.REQUEST_ACTION);
                    break;

                case R.id.remove:
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

                    builder.setTitle(R.string.titulo_eliminar_orden_trabajo);
                    builder.setMessage(R.string.mensaje_eliminar_orden_trabajo);

                    builder.setNegativeButton(R.string.aceptar, (dialog, which) -> {
                        if (ordenTrabajoService == null) {
                            return;
                        }

                        compositeDisposable.add(ordenTrabajoService.remove(value.getId())
                                .subscribe(self -> this.onRemove(value), this::onError, this::onComplete));
                    });
                    builder.setPositiveButton(R.string.cancelar, (dialog, which) -> dialog.dismiss());

                    builder.setCancelable(true);
                    builder.create();
                    builder.show();
                    break;

                case R.id.check_list:
                    if (value.getListaChequeo().isEmpty()) {
                        Snackbar.make(view, R.string.lista_chequeo_error, Snackbar.LENGTH_LONG)
                                .show();
                        break;
                    }

                    bundle = new Bundle();
                    bundle.putLong(DescargarRutaTrabajoActivity.ID_EXTRA, value.getId());
                    bundle.putBoolean(DescargarRutaTrabajoActivity.MODO_VER_DETALLE, false);
                    bundle.putBoolean(DescargarRutaTrabajoActivity.ACCION_REFRESCAR, false);
                    bundle.putBoolean(DescargarRutaTrabajoActivity.ACCION_PARCIAL, true);

                    intent = new Intent(view.getContext(), DescargarRutaTrabajoActivity.class);
                    intent.putExtras(bundle);

                    startActivityForResult(intent, DescargarRutaTrabajoActivity.REQUEST_ACTION);
                    break;

                case R.id.firmaxentidad:
                    bundle = new Bundle();
                    bundle.putLong(Mantum.KEY_ID, value.getId());
                    bundle.putString(Mantum.ENTITY_TYPE, "OT");
                    bundle.putString(MODULO, "Órden de trabajo");

                    intent = new Intent(view.getContext(), FirmaxEntidadActivity.class);
                    intent.putExtras(bundle);

                    startActivity(intent);
                    break;

                case R.id.recorrido_planta_externa:
                    bundle = new Bundle();
                    bundle.putLong(Mantum.KEY_ID, value.getId());

                    intent = new Intent(view.getContext(), RecorridoPlantaExternaActivity.class);
                    intent.putExtras(bundle);

                    startActivityForResult(intent, RecorridoPlantaExternaActivity.REQUEST_ACTION);
                    break;

                case R.id.instalacion_planta_externa:
                    bundle = new Bundle();
                    bundle.putLong(Mantum.KEY_ID, value.getId());

                    intent = new Intent(view.getContext(), InstalacionPlantaExternaActivity.class);
                    intent.putExtras(bundle);

                    startActivityForResult(intent, InstalacionPlantaExternaActivity.REQUEST_ACTION);
                    break;

                case R.id.asociar_fotografia:
                    bundle = new Bundle();
                    bundle.putLong(Mantum.KEY_ID, value.getId());
                    bundle.putString(GaleriaActivity.KEY_TIPO_ENTIDAD, "OT");

                    intent = new Intent(view.getContext(), GaleriaActivity.class);
                    intent.putExtras(bundle);

                    startActivityForResult(intent, GaleriaActivity.REQUEST_ACTION);
                    break;

                case R.id.registrar_falla:
                    bundle = new Bundle();
                    bundle.putLong(Mantum.KEY_ID, value.getId());

                    intent = new Intent(view.getContext(), FormularioFallaOTActivity.class);
                    intent.putExtras(bundle);

                    startActivity(intent);
                    break;
            }
            return true;
        });

        informationAdapter.setOnAction(new OnSelected<OrdenTrabajoView>() {

            @Override
            public void onClick(OrdenTrabajoView value, int position) {
                Bundle bundle = new Bundle();
                bundle.putString(Mantum.KEY_UUID, value.getUUID());
                bundle.putLong(Mantum.KEY_ID, value.getId());

                Intent intent = new Intent(view.getContext(), DetalleOrdenTrabajoActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
            }

            @Override
            public boolean onLongClick(OrdenTrabajoView value, int position) {
                return false;
            }

        });

        busquedaOtYarda = view.findViewById(R.id.busqueda_ot_yarda);
        if (UserPermission.check(view.getContext(), UserPermission.BUSQUEDA_OTS_POR_YARDAS, false)) {
            final List<String> yardasNombres = new ArrayList<>();
            List<Yarda> yardaList = database.where(Yarda.class).findAll();

            for (Yarda yarda : yardaList) {
                yardasNombres.add(yarda.getNombre());
            }
            boolean[] checked = new boolean[yardasNombres.size()];
            List<String> yardasSeleccionadas = new ArrayList<>();

            busquedaOtYarda.setVisibility(View.VISIBLE);
            busquedaOtYarda.setOnClickListener(view1 -> {
                AlertDialog.Builder dialogSelectorYardas = new AlertDialog.Builder(view.getContext());
                dialogSelectorYardas.setTitle(R.string.buscar_ot_yarda_hint);
                dialogSelectorYardas.setMultiChoiceItems(yardasNombres.toArray(new String[0]), checked, (dialog, which, isChecked) -> {
                    if (isChecked) {
                        checked[which] = true;
                        yardasSeleccionadas.add(yardasNombres.get(which));
                    } else {
                        checked[which] = false;
                        yardasSeleccionadas.remove(yardasNombres.get(which));
                    }
                });
                dialogSelectorYardas.setNegativeButton(R.string.cancel, null);
                dialogSelectorYardas.setPositiveButton(R.string.buscar_ot_yarda, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    if (!yardasSeleccionadas.isEmpty()) {
                        activarAcciones(false);
                        progress();
                        if (ordenTrabajoService == null)
                            ordenTrabajoService = new OrdenTrabajoService(view.getContext(), cuenta);
                        compositeDisposable.add(ordenTrabajoService.getOtsByYardas(yardasSeleccionadas)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(this::onNextOtsByYardas, this::onErrorOtsByYardas, this::onCompleteOtsByYardas));
                    }
                });
                dialogSelectorYardas.show();
            });
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        onScrollListener = new Mantum.OnScrollListener(layoutManager, false) {

            @Override
            public void onRequest(int page) {
                OrdenTrabajoFragment.this.onRequest(view, page, false);
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
            OrdenTrabajoFragment.this.onRequest(view, 1, false);
        });

        recyclerView = informationAdapter.startAdapter(view, layoutManager);
        recyclerView.addOnScrollListener(onScrollListener);

        switchOcultarCien = view.findViewById(R.id.ocultar_100);
        switchOcultarCien.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                switchOcultarCienCheckedAction();
            } else {
                informationAdapter.clear();
                informationAdapter.addAll(currentListado);
            }

            informationAdapter.notifyDataSetChanged();
            informationAdapter.showMessageEmpty(view, R.string.pendiente_ordenes_trabajo, R.drawable.evento);
        });

        switchMostrarSoloAsignadas = view.findViewById(R.id.mostrar_solo_asignadas);
        switchMostrarSoloAsignadas.setOnCheckedChangeListener((buttonView, isChecked) -> {
            informationAdapter.clear();
            onRequest(getView(), 1, true);
        });

        setMenuVisibility();
        onRequest(view, 1, true);
        return view;
    }

    private void switchOcultarCienCheckedAction() {
        for (OrdenTrabajoView ordenTrabajo : currentListado) {
            if (ordenTrabajo.isCienPorciento()) {
                informationAdapter.remove(ordenTrabajo);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setMenuVisibility();
    }

    private void setMenuVisibility() {
        if (informationAdapter == null) {
            return;
        }

        if (getContext() != null) {
            if (Version.check(getContext(), 8)
                    && UserPermission.check(getContext(), UserPermission.MODULO_GESTION_SERVICIOS, false)) {
                informationAdapter.setMenuVisibility(R.id.inspeccion_electrica, true);
                informationAdapter.setMenuVisibility(R.id.contacto, true);
                informationAdapter.setMenuVisibility(R.id.recorrido_planta_externa, true);

                String registroTiempoOT = UserParameter.getValue(getContext(), UserParameter.REGISTRO_TIEMPOS_OT);
                if (registroTiempoOT == null || registroTiempoOT.equals("1")) {
                    informationAdapter.setMenuVisibility(R.id.tiempos, true);
                }
            }

            if (UserPermission.check(getContext(), UserPermission.LISTA_CHEQUEO, false)) {
                informationAdapter.setMenuVisibility(R.id.check_list, true);
            }

            if (Version.check(getContext(), 11) && UserPermission.check(getContext(), UserPermission.REGISTRAR_FIRMAS_OT)) {
                informationAdapter.setMenuVisibility(R.id.firmaxentidad, true);
            }

            String removerOT = UserParameter.getValue(getContext(), UserParameter.REMOVER_OT);
            if (removerOT == null || removerOT.equals("1")) {
                informationAdapter.setMenuVisibility(R.id.remove, true);
            }

            if (Version.check(getContext(), 17) && UserPermission.check(getContext(), UserPermission.REGISTRAR_FALLAS, false)) {
                informationAdapter.setMenuVisibility(R.id.registrar_falla, true);
            }

            if (UserPermission.check(getContext(), UserPermission.RECIBIR_OT, true)) {
                informationAdapter.setMenuVisibility(R.id.recibir, true);
            }

            if (UserPermission.check(getContext(), UserPermission.TERMINAR_OT, true)) {
                informationAdapter.setMenuVisibility(R.id.terminar, true);
            }
        }
    }

    public void onRefresh() {
        activarAcciones(true);
        onRequest(getView(), 1, true);
    }

    public void onRequest(View view, int page, boolean notify) {
        if (view == null || database.isClosed()) {
            return;
        }

        RealmQuery<OrdenTrabajo> query = database.where(OrdenTrabajo.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("asignada", switchMostrarSoloAsignadas.isChecked())
                .notEqualTo("estado", "Cerrada")
                .sort(new String[]{"prioridad", "codigo"}, new Sort[]{Sort.ASCENDING, Sort.DESCENDING});

        List<OrdenTrabajo> ordenTrabajos = database.pagination(query.findAll(), page);
        if (Mantum.isConnectedOrConnecting(view.getContext())) {
            informationAdapter.showMessageEmpty(view, R.string.pendiente_ordenes_trabajo, R.drawable.ordenes_trabajo);

            ordenTrabajoService = new OrdenTrabajoService(view.getContext(), cuenta);
            progress();

            compositeDisposable.add(fetch(page)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap(this::onSave)
                    .subscribe(this::onNext, this::onError, this::onComplete));
        } else {
            if (ordenTrabajos.size() == 0) {
                onScrollListener.decrease();
            }

            onScrollListener.loading(false);
            currentListado.addAll(OrdenTrabajoView.factory(ordenTrabajos));
            informationAdapter.addAll(OrdenTrabajoView.factory(ordenTrabajos), notify);

            Handler handler = new Handler();
            handler.post(() -> {
                informationAdapter.notifyDataSetChanged();
                informationAdapter.showMessageEmpty(view, R.string.pendiente_ordenes_trabajo, R.drawable.ordenes_trabajo);
            });
        }
    }

    public Observable<OrdenTrabajo.Request> fetch(int page) {
        onScrollListener.loading(true);
        return !switchMostrarSoloAsignadas.isChecked()
                ? ordenTrabajoService.fetch(page)
                : ordenTrabajoService.fetchPendingAT(page);
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

        currentListado.clear();

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
        return context.getString(R.string.tab_OT);
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

        return ordenTrabajoService.remove(value, false)
                .map(this::onRemove)
                .flatMap(ordenes -> ordenTrabajoService.save(request.getPendientes()));
    }

    private void onRemove(OrdenTrabajoView ordenTrabajoView) {
        informationAdapter.remove(ordenTrabajoView, false);
        currentListado.remove(ordenTrabajoView);
    }

    private List<Long> onRemove(List<Long> remove) {
        for (Long id : remove) {
            informationAdapter.remove(value -> value.getId().equals(id));
        }
        return remove;
    }

    private void onNext(@NonNull List<OrdenTrabajo> ordenTrabajos) {
        List<OrdenTrabajoView> resultado = new ArrayList<>();
        for (OrdenTrabajo ordenTrabajo : ordenTrabajos) {
            if (ordenTrabajo == null) {
                continue;
            }

            OrdenTrabajoView value = OrdenTrabajoView.factory(ordenTrabajo);
            if (ordenTrabajo.esCerrada()) {
                informationAdapter.remove(value);
                currentListado.remove(value);
                continue;
            }
            resultado.add(value);
        }

        informationAdapter.addAll(resultado, false);
        currentListado.addAll(resultado);
    }

    private void onError(Throwable throwable) {
        Log.e(TAG, "onError: ", throwable);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
        if (busquedaPorYardas) {
            activarAcciones(false);
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

    private void onComplete() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }

        if (busquedaPorYardas) {
            busquedaPorYardas = false;
            busquedaOtYarda.setBackground(
                    ContextCompat.getDrawable(view.getContext(), R.drawable.background_button_grey));
        }

        if (switchOcultarCien.isChecked()) {
            switchOcultarCienCheckedAction();
        }

        progressBar.setVisibility(View.GONE);
        onScrollListener.loading(false);
        informationAdapter.notifyDataSetChanged();

        if (getView() != null) {
            informationAdapter.showMessageEmpty(
                    getView(), R.string.pendiente_ordenes_trabajo, R.drawable.ordenes_trabajo
            );
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

    private void onNextOtsByYardas(List<OrdenTrabajo> ordenTrabajos) {
        ordenTrabajoService.newSave(ordenTrabajos);

        informationAdapter.clear();
        currentListado.clear();
        List<OrdenTrabajoView> resultado = new ArrayList<>();
        for (OrdenTrabajo ordenTrabajo : ordenTrabajos) {
            if (ordenTrabajo == null) {
                continue;
            }

            OrdenTrabajoView value = OrdenTrabajoView.factory(ordenTrabajo);
            if (ordenTrabajo.esCerrada()) {
                continue;
            }
            resultado.add(value);
        }

        informationAdapter.addAll(resultado, false);
        currentListado.addAll(resultado);
    }

    private void onErrorOtsByYardas(Throwable throwable) {
        activarAcciones(true);
        progressBar.setVisibility(View.GONE);
        if (getView() != null) {
            Snackbar.make(getView(), R.string.error_pendientes, Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private void onCompleteOtsByYardas() {
        progressBar.setVisibility(View.GONE);
        informationAdapter.notifyDataSetChanged();

        busquedaPorYardas = true;
        busquedaOtYarda.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.background_button));

        if (getView() != null) {
            informationAdapter.showMessageEmpty(getView(), R.string.pendiente_ordenes_trabajo, R.drawable.ordenes_trabajo);
        }
    }

    private void activarAcciones(boolean activar) {
        if (activar) {
            swipeRefreshLayout.setEnabled(true);
            recyclerView.addOnScrollListener(onScrollListener);
        } else {
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setEnabled(false);
            recyclerView.clearOnScrollListeners();
        }

    }
}
