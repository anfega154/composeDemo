package com.mantum.cmms.activity;

import static com.mantum.cmms.entity.Transaccion.ACCION_REGISTRAR_EIR;
import static com.mantum.cmms.entity.Transaccion.ACCION_REGISTRAR_PTI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Archivos;
import com.mantum.cmms.entity.Contenedor;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.entity.parameter.UserParameter;
import com.mantum.cmms.service.InspeccionService;
import com.mantum.cmms.service.SendEmailService;
import com.mantum.cmms.task.TransaccionTask;
import com.mantum.component.Mantum;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.InformationAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.RealmResults;
import io.realm.Sort;

public class InspeccionManagerActivity extends Mantum.Activity {

    private static final String TAG = InspeccionManagerActivity.class.getSimpleName();

    private AlertDialog modal;
    private Database database;
    private InspeccionService inspeccionService;
    private InformationAdapter<Transaccion, Archivos> informationAdapter;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspeccion_manager);

        includeBackButtonAndTitle(R.string.inspecciones_pendientes);

        database = new Database(this);
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        findViewById(R.id.enviar).setOnClickListener(v -> {
            Toast.makeText(InspeccionManagerActivity.this, R.string.sincronizando, Toast.LENGTH_SHORT)
                    .show();

            TransaccionTask.Task task = new TransaccionTask.Task(InspeccionManagerActivity.this);
            task.process(new Database(InspeccionManagerActivity.this));

            if (cuenta != null) {
                informationAdapter.clear();
                informationAdapter.addAll(getPendientes(cuenta), true);
            }
        });

        if (cuenta != null) {
            inspeccionService = new InspeccionService(this, cuenta);

            informationAdapter = new InformationAdapter<>(this);
            informationAdapter.setDrawable(value -> {
                if (value.getState() != null) {
                    if (value.getState().equals(Transaccion.ESTADO_PENDIENTE)) {
                        return R.drawable.ic_outline_access_time_24;
                    }

                    if (value.getState().equals(Transaccion.ESTADO_ERROR)) {
                        return R.drawable.ic_outline_error_outline_24;
                    }

                    if (value.getState().equals(Transaccion.ESTADO_SINCRONIZANDO)) {
                        return R.drawable.ic_outline_cloud_upload_24;
                    }

                    if (value.getState().equals(Transaccion.ESTADO_SINCRONIZADO)) {
                        return R.drawable.ic_outline_check_circle_24;
                    }
                }
                return null;
            });
            informationAdapter.setMenu(R.menu.menu_transaccion);
            informationAdapter.setOnCall((menu, value) -> {
                final String uuid = value.getUUID();
                int itemId = menu.getItemId();
                if (itemId == R.id.detail) {
                    detail(value);
                } else if (itemId == R.id.refresh) {
                    if (!Transaccion.ESTADO_ERROR.equals(value.getEstado())) {
                        Snackbar.make(getView(), R.string.reintentar_estado_sincronizar, Snackbar.LENGTH_LONG)
                                .show();
                        return super.onOptionsItemSelected(menu);
                    }

                    database.executeTransactionAsync(self -> {
                        Transaccion transaccion = self.where(Transaccion.class)
                                .equalTo("UUID", uuid)
                                .findFirst();

                        if (transaccion != null) {
                            transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);
                            transaccion.setMessage(" ");
                        }

                    }, () -> informationAdapter.remove(value, true));
                } else if (itemId == R.id.edit) {
                    if (ACCION_REGISTRAR_PTI.equals(value.getAccion())) {
                        Intent intent = new Intent(this, InspeccionRegistroPTIActivity.class);
                        intent.putExtra(InspeccionRegistroPTIActivity.UUID_TRANSACCION, value.getUUID());
                        intent.putExtra(InspeccionRegistroPTIActivity.MODE_EDIT, value.getValue());

                        startActivityForResult(intent, 1);
                    } else if (ACCION_REGISTRAR_EIR.equals(value.getAccion())) {
                        Intent intent = new Intent(this, InspeccionRegistroEIRActivity.class);
                        intent.putExtra(InspeccionRegistroPTIActivity.UUID_TRANSACCION, value.getUUID());
                        intent.putExtra(InspeccionRegistroPTIActivity.MODE_EDIT, value.getValue());

                        startActivityForResult(intent, 1);
                    }
                } else if (itemId == R.id.remove) {
                    remove(value);
                } else if (itemId == R.id.sincronizar) {
                    Transaccion transaccion = database.where(Transaccion.class)
                            .equalTo("UUID", uuid)
                            .findFirst();

                    if (transaccion == null) {
                        Snackbar.make(getView(), R.string.transaccion_empty, Snackbar.LENGTH_LONG)
                                .show();
                        return super.onOptionsItemSelected(menu);
                    }

                    if (!Transaccion.ESTADO_PENDIENTE.equals(transaccion.getEstado())) {
                        Snackbar.make(getView(), R.string.transaccion_no_pendiente, Snackbar.LENGTH_LONG)
                                .show();
                        return super.onOptionsItemSelected(menu);
                    }

                    Transaccion element = transaccion.isManaged()
                            ? database.copyFromRealm(transaccion)
                            : transaccion;

                    TransaccionTask.Task task = new TransaccionTask.Task(InspeccionManagerActivity.this);
                    database.executeTransactionAsync((self) -> task.prepare(self, element));
                } else if (itemId == R.id.sendemail) {
                    SendEmailService.shareTransactionDetail(InspeccionManagerActivity.this, cuenta, value);
                }
                return super.onOptionsItemSelected(menu);
            });
            informationAdapter.startAdapter(getView(), new LinearLayoutManager(this));
            informationAdapter.setOnAction(new OnSelected<Transaccion>() {

                @Override
                public void onClick(Transaccion value, int position) {
                    detail(value);
                }

                @Override
                public boolean onLongClick(Transaccion value, int position) {
                    return true;
                }

            });

            SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
            swipeRefreshLayout.setOnRefreshListener(() -> {
                swipeRefreshLayout.setRefreshing(false);

                informationAdapter.clear();
                informationAdapter.addAll(getPendientes(cuenta));
                getCantidadTransaccionesPendientes();
            });

            informationAdapter.addAll(getPendientes(cuenta));
            getCantidadTransaccionesPendientes();
        }

        getFechaUltimaActualizacion();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (informationAdapter != null) {
            informationAdapter.clear();
            informationAdapter.addAll(getPendientes());
            getCantidadTransaccionesPendientes();
        }
    }

    private void getCantidadTransaccionesPendientes() {
        TextView subtitle = findViewById(R.id.subtitle);
        runOnUiThread(() -> subtitle.setText(String.format("Transacciones: %s", informationAdapter.getItemCount())));
    }

    private void detail(@NonNull Transaccion value) {
        switch (value.getAccion()) {
            case ACCION_REGISTRAR_PTI: {
                Intent intent = new Intent(this, TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, ACCION_REGISTRAR_PTI);

                startActivityForResult(intent, 1);
                break;
            }

            case Transaccion.ACCION_REGISTRAR_EIR: {
                Intent intent = new Intent(this, TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_REGISTRAR_EIR);

                startActivityForResult(intent, 1);
                break;
            }
        }
    }

    private void getFechaUltimaActualizacion() {
        String fecha = UserParameter.getValue(
                this, UserParameter.ULTIMA_ACTUALIZACION_INSPECCION_PROGRAMADA);
        getFechaUltimaActualizacion(fecha);
    }

    private void getFechaUltimaActualizacion(String fecha) {
        TextView title = findViewById(R.id.title);
        runOnUiThread(() -> title.setText(String.format("Última actualización: %s", fecha != null ? fecha : "")));
    }

    private List<Transaccion> getPendientes(@NonNull Cuenta cuenta) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -3);

        List<Transaccion> results = database.where(Transaccion.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("modulo", Transaccion.MODULO_INSPECCION)
                .beginGroup()
                .equalTo("accion", ACCION_REGISTRAR_PTI)
                .or()
                .equalTo("accion", Transaccion.ACCION_REGISTRAR_EIR)
                .endGroup()
                .greaterThanOrEqualTo("creation", calendar.getTime())
                .sort("creation", Sort.DESCENDING)
                .findAll();

        return database.copyFromRealm(results);
    }

    private List<Transaccion> getPendientes() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -3);

        Database database = new Database(this);
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return new ArrayList<>();
        }

        List<Transaccion> results = database.where(Transaccion.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("modulo", Transaccion.MODULO_INSPECCION)
                .beginGroup()
                .equalTo("accion", ACCION_REGISTRAR_PTI)
                .or()
                .equalTo("accion", Transaccion.ACCION_REGISTRAR_EIR)
                .endGroup()
                .greaterThanOrEqualTo("creation", calendar.getTime())
                .sort("creation", Sort.DESCENDING)
                .findAll();

        return database.copyFromRealm(results);
    }

    private void removerTransaccionesSincronizada() {
        Database database = new Database(this);
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return;
        }

        database.where(Transaccion.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("modulo", Transaccion.MODULO_INSPECCION)
                .beginGroup()
                .equalTo("accion", ACCION_REGISTRAR_PTI)
                .or()
                .equalTo("accion", Transaccion.ACCION_REGISTRAR_EIR)
                .endGroup()
                .equalTo("estado", Transaccion.ESTADO_SINCRONIZADO)
                .findAll()
                .deleteAllFromRealm();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_inspeccion_manager, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            backActivity();
        } else if (itemId == R.id.search) {
            Intent intent = new Intent(
                    this, BusquedaInspeccionManagerActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.download) {
            downloadView();
        }

        return super.onOptionsItemSelected(item);
    }

    private void remove(Transaccion value) {
        if (getView() == null) {
            return;
        }

        AlertDialog.Builder alertDialogBuilder
                = new AlertDialog.Builder(getView().getContext());
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setTitle(R.string.eliminar_transaccion_titulo);
        alertDialogBuilder.setMessage(R.string.eliminar_transaccion);
        alertDialogBuilder.setNegativeButton(getString(R.string.aceptar), (dialogInterface, i) -> {
            database.executeTransactionAsync(self -> {
                RealmResults<Transaccion> results = self.where(Transaccion.class)
                        .equalTo("UUID", value.getUUID())
                        .findAll();

                if (results != null) {
                    results.deleteFirstFromRealm();
                }
            }, () -> informationAdapter.remove(value, true));

            dialogInterface.cancel();

        });

        alertDialogBuilder.setPositiveButton(getString(R.string.cancelar), (dialogInterface, i) -> dialogInterface.cancel());
        alertDialogBuilder.show();
    }

    private void downloadView() {
        if (inspeccionService == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = View.inflate(
                this, R.layout.dialog_descargar_inspecciones, null);

        compositeDisposable.add(
                inspeccionService.clear()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::onNext)
        );

        compositeDisposable.add(
                inspeccionService.download()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap((Contenedor.Response response) -> {
                            TextView message = view.findViewById(R.id.percent);
                            message.setText(response.getPercent() != null
                                    ? response.getPercent() + " %"
                                    : ""
                            );

                            return inspeccionService.save(response);
                        })
                        .repeatWhen(completed -> completed.delay(5, TimeUnit.SECONDS))
                        .subscribe(this::onNext, this::onError)
        );

        modal = builder.setView(view).setPositiveButton(R.string.cancelar, (dialog, which) -> {
            compositeDisposable.clear();
            dialog.dismiss();
        }).setCancelable(false).create();

        modal.show();
    }

    private void onNext(Boolean success) {
        Log.e(TAG, "Se han eliminado los contenedores: " + success);
    }

    private void onNext(@NonNull Contenedor.Response response) {
        if (response.getNext() == null) {
            getFechaUltimaActualizacion();

            compositeDisposable.clear();
            modal.dismiss();

            Snackbar.make(
                    getView(),
                    R.string.descargar_inspecciones_programadas_finalizadas,
                    Snackbar.LENGTH_LONG
            ).show();

            removerTransaccionesSincronizada();

            if (informationAdapter != null) {
                runOnUiThread(() -> {
                    informationAdapter.clear();
                    informationAdapter.addAll(getPendientes());
                    getCantidadTransaccionesPendientes();
                });
            }
        }
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
}