package com.mantum.cmms.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Autorizacion;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.service.AutorizacionAccesoService;
import com.mantum.cmms.service.TransaccionService;
import com.mantum.cmms.view.AutorizacionView;
import com.mantum.component.Mantum;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.AlphabetAdapter;
import com.mantum.component.component.Progress;
import com.mantum.component.swipe.SwipeController;

import java.util.Calendar;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class AutorizacionAccesoActivity extends Mantum.Activity {

    private Progress progress;

    private TextInputEditText id;

    private TextInputEditText nombre;

    private TextInputEditText estado;

    private Database database;

    private TransaccionService transaccionService;

    private AlphabetAdapter<AutorizacionView> alphabetAdapter;

    private AutorizacionAccesoService autorizacionAccesoService;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autorizacion_acceso_activity);

        database = new Database(this);
        includeBackButtonAndTitle(R.string.autorizacion_acceso);
        transaccionService = new TransaccionService(this);
        autorizacionAccesoService = new AutorizacionAccesoService(this);

        id = findViewById(R.id.id);
        nombre = findViewById(R.id.name);
        estado = findViewById(R.id.state);

        findViewById(R.id.clear).setOnClickListener(v -> {
            if (id != null) {
                id.setText("");
            }
        });

        alphabetAdapter = new AlphabetAdapter<>(this, false);
        alphabetAdapter.setOnAction(new OnSelected<AutorizacionView>() {

            @Override
            public void onClick(AutorizacionView value, int position) {
                Intent intent = new Intent(
                        AutorizacionAccesoActivity.this, DetalleAutorizacionActivity.class);

                intent.putExtra(DetalleAutorizacionActivity.FORM_VALUES, value);
                startActivityForResult(intent, DetalleAutorizacionActivity.REQUEST_VALUE);
            }

            @Override
            public boolean onLongClick(AutorizacionView value, int position) {
                enter(new Autorizacion.Request(
                        value.getId(), id.getText().toString(), value.getModulo(), value.getFechafin()));
                return true;
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        RecyclerView recyclerView = alphabetAdapter.startAdapter(getView(), layoutManager);

        SwipeController swipeController = new SwipeController(this);
        swipeController.setOnSwipeLeft(viewHolder -> {
            AutorizacionView value
                    = alphabetAdapter.getOriginal().get(viewHolder.getAdapterPosition());

            enter(new Autorizacion.Request(
                    value.getId(), id.getText().toString(), value.getModulo(), value.getFechafin()));
        }, getResources().getColor(R.color.positive_event), R.drawable.directions_run);

        swipeController.setOnSwipeRight(viewHolder -> {
            AutorizacionView value
                    = alphabetAdapter.getOriginal().get(viewHolder.getAdapterPosition());

            enter(new Autorizacion.Request(
                    value.getId(), id.getText().toString(), value.getModulo(), value.getFechafin()));
        }, getResources().getColor(R.color.positive_event), R.drawable.directions_run);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeController);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        id.setOnEditorActionListener((v, keyCode, event) -> {
            if (event == null) {
                if (keyCode == KeyEvent.KEYCODE_ENTER
                        || keyCode == KeyEvent.KEYCODE_ENDCALL
                        || keyCode == KeyEvent.KEYCODE_NUMPAD_DOT) {
                    request();
                    return true;
                }
            } else {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_ENDCALL
                        || event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_DOT) {
                    request();
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;

            case R.id.action_done:
                request();
                break;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_formulario, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        autorizacionAccesoService.cancel();
        compositeDisposable.clear();

        if (transaccionService != null) {
            transaccionService.close();
        }

        if (progress != null && !isFinishing()) {
            progress.hidden();
        }

        if (database != null) {
            database.close();
        }
    }

    private void request() {
        if (progress != null && progress.isShowing()) {
            return;
        }

        closeKeyboard();

        TextInputLayout textInputLayout = findViewById(R.id.id_container);
        textInputLayout.setError("");

        String value = id.getText().toString();
        if (value.isEmpty()) {
            textInputLayout.setError(getString(R.string.cedula_empty));
            textInputLayout.requestFocus();
            return;
        }

        value = value.replaceFirst("^0+(?!$)", "");
        id.setText(value);

        if (!isFinishing()) {
            progress = new Progress(this);
            progress.show(R.string.titulo_autorizacion,
                    R.string.mensaje_autorizacion);
        }

        compositeDisposable.add(autorizacionAccesoService.fetch(value)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNext, this::onError, this::onComplete));
    }

    private void onNext(Response response) {
        Autorizacion autorizacion = response.getBody(Autorizacion.class, Mantum.getGson());

        alphabetAdapter.clear();
        alphabetAdapter.addAll(AutorizacionView.factory(autorizacion.getAutorizaciones()));
        if (nombre != null) {
            nombre.setText(autorizacion.getNombre());
        }

        if (estado != null) {
            estado.setText(autorizacion.isAcceso() ? getString(R.string.accede) : getString(R.string.no_accede));
        }
    }

    private void onError(Throwable throwable) {
        if (progress != null && !isFinishing()) {
            progress.hidden();
        }

        clean();
        message(throwable);
    }

    private void message(Throwable throwable) {
        if (progress != null && !isFinishing()) {
            progress.hidden();
        }

        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                .show();
    }

    private void clean() {
        if (progress != null && !isFinishing()) {
            progress.hidden();
        }

        if (alphabetAdapter != null) {
            alphabetAdapter.clear();
            alphabetAdapter.refresh();
        }

        if (id != null) {
            nombre.setText("");
        }

        if (nombre != null) {
            nombre.setText("");
        }

        if (estado != null) {
            estado.setText("");
        }
    }

    private void onComplete() {
        if (progress != null && !isFinishing()) {
            progress.hidden();
        }

        alphabetAdapter.refresh();
        if (alphabetAdapter.getItemCount() == 1) {
            AutorizacionView autorizaciones
                    = alphabetAdapter.getOriginal().get(0);

            enter(new Autorizacion.Request(
                    autorizaciones.getId(), id.getText().toString(),
                    autorizaciones.getModulo(), autorizaciones.getFechafin()));
        }
    }

    private void enter(final Autorizacion.Request request) {
        if (getString(R.string.no_accede).equals(estado.getText().toString())) {
            Snackbar.make(getView(), R.string.no_autorizacion_acceso, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            Snackbar.make(getView(), R.string.error_authentication, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        View form = View.inflate(this, R.layout.ingresar_autorizacion, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setNegativeButton(R.string.ingresar, ((dialog, which) -> {
            progress = new Progress(this);
            progress.show(R.string.titulo_autorizacion_ingreso,
                    R.string.mensaje_autorizacion_ingreso);

            Transaccion transaccion = new Transaccion();
            transaccion.setUUID(UUID.randomUUID().toString());
            transaccion.setCuenta(cuenta);
            transaccion.setCreation(Calendar.getInstance().getTime());
            transaccion.setUrl(cuenta.getServidor().getUrl() + "/restapp/app/saveingresospersonal");
            transaccion.setValue(request.toJson());
            transaccion.setVersion(cuenta.getServidor().getVersion());
            transaccion.setModulo(Transaccion.MODULO_AUTORIZACION_ACCESO);
            transaccion.setAccion(Transaccion.ACCION_ENTRADA);
            transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);

            compositeDisposable.add(transaccionService.save(transaccion)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> Snackbar.make(getView(), getString(R.string.autorizacion_file), Snackbar.LENGTH_LONG).show(), this::message, this::clean));

            dialog.dismiss();
        }));
        alertDialogBuilder.setPositiveButton(R.string.cancelar, (dialog, which) -> dialog.cancel());
        alertDialogBuilder.setView(form);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.show();
    }
}