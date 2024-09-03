package com.mantum.cmms.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Accion;
import com.mantum.cmms.entity.Busqueda;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.DetalleBusqueda;
import com.mantum.cmms.service.DescargarRecursosServices;
import com.mantum.component.Mantum;
import com.mantum.component.adapter.AlphabetAdapter;

import java.util.List;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.RealmList;
import io.realm.RealmResults;

import static com.mantum.component.Mantum.isConnectedOrConnecting;

public class DescargarRecursosActivity extends Mantum.Activity {

    private static final String TAG = DescargarRecursosActivity.class.getSimpleName();

    private final Gson gson = new Gson();

    private Database database;

    private ProgressBar progressBar;

    private AlphabetAdapter<Busqueda> alphabetAdapter;

    private DescargarRecursosServices descargarRecursosServices;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descargar_recursos);

        progressBar = findViewById(R.id.progressBar);
        includeBackButtonAndTitle(R.string.descargar_recursos);

        database = new Database(this);
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            backActivity();
            return;
        }

        descargarRecursosServices = new DescargarRecursosServices(this, cuenta);
        RealmResults<Busqueda> busquedas = database.where(Busqueda.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("type", "Recurso")
                .findAll();

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this);

        alphabetAdapter = new AlphabetAdapter<>(this);
        alphabetAdapter.addAll(database.copyFromRealm(busquedas));
        alphabetAdapter.startAdapter(getView(), layoutManager);
        alphabetAdapter.showMessageEmpty(getView());

        if (alphabetAdapter.isEmpty()) {
            request();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }

        if (alphabetAdapter != null) {
            alphabetAdapter.clear();
        }

        compositeDisposable.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actualizar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_refresh:
                if (!isConnectedOrConnecting(this)) {
                    Snackbar.make(getView(), R.string.offline, Snackbar.LENGTH_LONG)
                            .show();
                    return true;
                }
                request();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void request() {
        progressBar.setVisibility(View.VISIBLE);
        compositeDisposable.add(descargarRecursosServices.get()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNext, this::onError, this::onComplete));
    }

    private void onNext(Response response) {
        Descargas descargas = gson.fromJson(response.getBody(), Descargas.class);
        database.executeTransaction(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                return;
            }

            for (Recursos recursos : descargas.getRecursos()) {
                Busqueda element = self.where(Busqueda.class)
                        .equalTo("id", recursos.getId())
                        .equalTo("type", recursos.getTipo())
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();

                if (element == null) {
                    RealmList<DetalleBusqueda> detalleBusquedas = new RealmList<>();

                    DetalleBusqueda detalleBusqueda = new DetalleBusqueda();
                    detalleBusqueda.setTitle("Código");
                    detalleBusqueda.setValue(recursos.getCodigo());
                    detalleBusquedas.add(detalleBusqueda);

                    detalleBusqueda = new DetalleBusqueda();
                    detalleBusqueda.setTitle("Nombre");
                    detalleBusqueda.setValue(recursos.getNombre());
                    detalleBusquedas.add(detalleBusqueda);

                    RealmList<Accion> actions = new RealmList<>();
                    String[] acciones = new String[] { "SS", "Bit" };
                    for (String accion: acciones) {
                        Accion current = self.where(Accion.class)
                                .equalTo("name", accion)
                                .findFirst();

                        if (current == null) {
                            Accion nuevaaccion = new Accion();
                            nuevaaccion.setName(accion);
                            actions.add(nuevaaccion);
                        } else {
                            actions.add(current);
                        }
                    }

                    Busqueda busqueda = new Busqueda();
                    busqueda.setUUID(UUID.randomUUID().toString());
                    busqueda.setCuenta(cuenta);
                    busqueda.setId(recursos.getId());
                    busqueda.setType(recursos.getTipo());
                    busqueda.setCode(recursos.getCodigo());
                    busqueda.setName(recursos.getNombre());
                    busqueda.setMostrar(false);
                    busqueda.setData(detalleBusquedas);
                    busqueda.setActions(actions);

                    self.insert(busqueda);
                    alphabetAdapter.add(busqueda);
                } else {
                    RealmList<DetalleBusqueda> detalle = new RealmList<>();

                    DetalleBusqueda detalleBusqueda = new DetalleBusqueda();
                    detalleBusqueda.setTitle("Código");
                    detalleBusqueda.setValue(recursos.getCodigo());
                    detalle.add(self.copyToRealm(detalleBusqueda));

                    detalleBusqueda = new DetalleBusqueda();
                    detalleBusqueda.setTitle("Nombre");
                    detalleBusqueda.setValue(recursos.getNombre());
                    detalle.add(self.copyToRealm(detalleBusqueda));

                    RealmList<Accion> actions = new RealmList<>();
                    String[] acciones = new String[] { "SS", "Bit" };
                    for (String accion: acciones) {
                        Accion current = self.where(Accion.class)
                                .equalTo("name", accion)
                                .findFirst();

                        if (current == null) {
                            Accion nuevaaccion = new Accion();
                            nuevaaccion.setName(accion);
                            actions.add(nuevaaccion);
                        } else {
                            actions.add(current);
                        }
                    }

                    element.setCode(recursos.getCodigo());
                    element.setName(recursos.getNombre());
                    element.setType(recursos.getTipo());
                    element.setMostrar(true);
                    element.setData(detalle);
                    element.setActions(actions);

                    alphabetAdapter.add(element);
                }
            }
        });
    }

    private void onError(Throwable throwable) {
        Log.e(TAG, "onError: ", throwable);
        progressBar.setVisibility(View.GONE);
        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                .show();
    }

    private void onComplete() {
        progressBar.setVisibility(View.GONE);
        alphabetAdapter.refresh();
        alphabetAdapter.showMessageEmpty(getView());
    }

    private static class Descargas {

        private final List<Recursos> recursos;

        private Descargas(List<Recursos> recursos) {
            this.recursos = recursos;
        }

        public List<Recursos> getRecursos() {
            return recursos;
        }
    }

    private static class Recursos {

        private final Long id;

        private final String tipo;

        private final String codigo;

        private final String nombre;

        private Recursos(Long id, String tipo, String codigo, String nombre) {
            this.id = id;
            this.tipo = tipo;
            this.codigo = codigo;
            this.nombre = nombre;
        }

        public Long getId() {
            return id;
        }

        public String getTipo() {
            return tipo;
        }

        public String getCodigo() {
            return codigo;
        }

        public String getNombre() {
            return nombre;
        }
    }
}