package com.mantum.cmms.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import com.mantum.cmms.entity.parameter.Barcode;
import com.mantum.cmms.service.DescargarEntidadesServices;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.adapter.AlphabetAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

import static com.mantum.component.Mantum.isConnectedOrConnecting;

public class DescargarEntidadesActivity extends Mantum.Activity {

    private static final String TAG = DescargarEntidadesActivity.class.getSimpleName();

    private final Gson gson = new Gson();

    private Database database;

    private ProgressBar progressBar;

    private AlphabetAdapter<Busqueda> alphabetAdapter;

    private DescargarEntidadesServices descargarEntidadesServices;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Cuenta cuenta;

    private BusquedasSingleton busquedasSingleton;

    private final static int PAGINATE = 100;

    Mantum.OnScrollListener onScrollListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descargar_entidad);

        progressBar = findViewById(R.id.progressBar);
        includeBackButtonAndTitle(R.string.descargar_entidades);

        database = new Database(this);
        cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            backActivity();
            return;
        }

        descargarEntidadesServices = new DescargarEntidadesServices(this, cuenta);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        alphabetAdapter = new AlphabetAdapter<>(this);
        alphabetAdapter.startAdapter(getView(), layoutManager);
        alphabetAdapter.showMessageEmpty(getView());

        onScrollListener = new Mantum.OnScrollListener(layoutManager, false) {
            @Override
            public void onRequest(int page) {
                DescargarEntidadesActivity.this.onRequest(page);
            }
        };

        RecyclerView recyclerView = alphabetAdapter.startAdapter(getView(), layoutManager);
        recyclerView.addOnScrollListener(onScrollListener);

        onRequest(0);
    }


    private void onRequest(int page) {
        onScrollListener.loading(false);
        busquedasSingleton = BusquedasSingleton.getInstance();
        List<Busqueda> busquedasAdd = busquedasSingleton.getBusquedas(page);

        if (page == 0 && busquedasAdd.isEmpty()) {
            request();
            return;
        }

        if (busquedasAdd.isEmpty()) {
            return;
        }

        alphabetAdapter.addAll(busquedasAdd, false);

        Handler handler = new Handler();
        handler.post(() -> {
            alphabetAdapter.notifyDataSetChanged();
            alphabetAdapter.showMessageEmpty(getView());
        });
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
        compositeDisposable.add(descargarEntidadesServices.get()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNext, this::onError, this::onComplete));
    }

    private void onNext(Response response) {
        List<Busqueda> busquedasTemp = new ArrayList<>();

        if (Version.check(this, 25)) {
            Descargas descargas = gson.fromJson(response.getBody(), Descargas.class);

            List<Entidades> entidades = descargas.getEntidades();
            if (entidades == null || entidades.size() == 0) {
                return;
            }

            database.executeTransaction(self -> {
                cuenta = self.where(Cuenta.class)
                        .equalTo("active", true)
                        .findFirst();

                if (cuenta == null) {
                    return;
                }

                /* Default actions */
                String[] acciones = new String[] { "SS", "Bit" };

                int i = 0;
                for (Entidades entity : entidades) {
                    Busqueda busqueda = self.where(Busqueda.class)
                            .equalTo("id", entity.getId())
                            .equalTo("type", entity.getType())
                            .equalTo("cuenta.UUID", cuenta.getUUID())
                            .findFirst();

                    RealmList<DetalleBusqueda> detalleBusquedas = new RealmList<>();

                    DetalleBusqueda detalleBusquedaCodigo = new DetalleBusqueda();
                    detalleBusquedaCodigo.setTitle("Código");
                    detalleBusquedaCodigo.setValue(entity.getCodigo());

                    DetalleBusqueda detalleBusquedaNombre = new DetalleBusqueda();
                    detalleBusquedaNombre.setTitle("Nombre");
                    detalleBusquedaNombre.setValue(entity.getNombre());

                    RealmList<Accion> actions = new RealmList<>();
                    for (String accion: acciones) {
                        Accion current = self.where(Accion.class)
                                .equalTo("name", accion)
                                .findFirst();

                        if (current == null) {
                            Accion nuevaaccion = new Accion();
                            nuevaaccion.setName(accion);
                            actions.add(self.copyToRealm(nuevaaccion));

                        } else {
                            actions.add(current);
                        }
                    }

                    if (busqueda == null) {
                        detalleBusquedas.add(detalleBusquedaCodigo);
                        detalleBusquedas.add(detalleBusquedaNombre);

                        busqueda = new Busqueda();
                        busqueda.setUUID(UUID.randomUUID().toString());
                        busqueda.setCuenta(cuenta);
                        busqueda.setId(entity.getId());
                        busqueda.setType(entity.getType());
                        busqueda.setCode(entity.getCodigo());
                        busqueda.setName(entity.getNombre());
                        busqueda.setQrcode(entity.getQrcode());
                        busqueda.setNfc(entity.getNfctoken());
                        busqueda.setMostrar(false);
                        busqueda.setData(detalleBusquedas);
                        busqueda.setActions(actions);

                        RealmList<Barcode> barcodes = new RealmList<>();
                        for (Barcode barcode : entity.getBarcode()) {
                            barcodes.add(self.copyToRealm(barcode));
                        }
                        busqueda.setBarcode(barcodes);

                        busquedasTemp.add(busqueda);

                    } else {
                        detalleBusquedas.add(self.copyToRealm(detalleBusquedaCodigo));
                        detalleBusquedas.add(self.copyToRealm(detalleBusquedaNombre));

                        busqueda.setCode(entity.getCodigo());
                        busqueda.setName(entity.getNombre());
                        busqueda.setType(entity.getType());
                        busqueda.setQrcode(entity.getQrcode());
                        busqueda.setNfc(entity.getNfctoken());
                        busqueda.setMostrar(false);
                        busqueda.setData(detalleBusquedas);
                        busqueda.setActions(actions);

                        RealmList<Barcode> barcodes = new RealmList<>();
                        for (Barcode barcode : entity.getBarcode()) {
                            barcodes.add(self.copyToRealm(barcode));
                        }
                        busqueda.setBarcode(barcodes);
                    }

                    if (i < PAGINATE) {
                        alphabetAdapter.add(busqueda);
                    }
                    i += 1;
                }

            });
        } else {
            DescargasOld descargas = gson.fromJson(response.getBody(), DescargasOld.class);

            List<EntidadesOld> entidades = descargas.getEntidades();
            if (entidades == null || entidades.size() == 0) {
                return;
            }

            database.executeTransaction(self -> {
                cuenta = self.where(Cuenta.class)
                        .equalTo("active", true)
                        .findFirst();

                if (cuenta == null) {
                    return;
                }

                /* Default actions */
                String[] acciones = new String[] { "SS", "Bit" };

                int i = 0;
                for (EntidadesOld entity : entidades) {
                    Busqueda busqueda = self.where(Busqueda.class)
                            .equalTo("id", entity.getId())
                            .equalTo("type", entity.getType())
                            .equalTo("cuenta.UUID", cuenta.getUUID())
                            .findFirst();

                    RealmList<DetalleBusqueda> detalleBusquedas = new RealmList<>();

                    DetalleBusqueda detalleBusquedaCodigo = new DetalleBusqueda();
                    detalleBusquedaCodigo.setTitle("Código");
                    detalleBusquedaCodigo.setValue(entity.getCodigo());

                    DetalleBusqueda detalleBusquedaNombre = new DetalleBusqueda();
                    detalleBusquedaNombre.setTitle("Nombre");
                    detalleBusquedaNombre.setValue(entity.getNombre());

                    RealmList<Accion> actions = new RealmList<>();
                    for (String accion: acciones) {
                        Accion current = self.where(Accion.class)
                                .equalTo("name", accion)
                                .findFirst();

                        if (current == null) {
                            Accion nuevaaccion = new Accion();
                            nuevaaccion.setName(accion);
                            actions.add(self.copyToRealm(nuevaaccion));

                        } else {
                            actions.add(current);
                        }
                    }

                    if (busqueda == null) {
                        detalleBusquedas.add(detalleBusquedaCodigo);
                        detalleBusquedas.add(detalleBusquedaNombre);

                        busqueda = new Busqueda();
                        busqueda.setUUID(UUID.randomUUID().toString());
                        busqueda.setCuenta(cuenta);
                        busqueda.setId(entity.getId());
                        busqueda.setType(entity.getType());
                        busqueda.setCode(entity.getCodigo());
                        busqueda.setName(entity.getNombre());
                        busqueda.setQrcode(entity.getQrcode());
                        busqueda.setNfc(entity.getNfctoken());
                        busqueda.setMostrar(false);
                        busqueda.setData(detalleBusquedas);
                        busqueda.setActions(actions);

                        Barcode barcode = new Barcode();
                        barcode.setCodigo(entity.getBarcode());
                        RealmList<Barcode> barcodes = new RealmList<>();
                        barcodes.add(self.copyToRealm(barcode));
                        busqueda.setBarcode(barcodes);

                        busquedasTemp.add(busqueda);

                    } else {
                        detalleBusquedas.add(self.copyToRealm(detalleBusquedaCodigo));
                        detalleBusquedas.add(self.copyToRealm(detalleBusquedaNombre));

                        busqueda.setCode(entity.getCodigo());
                        busqueda.setName(entity.getNombre());
                        busqueda.setType(entity.getType());
                        busqueda.setQrcode(entity.getQrcode());
                        busqueda.setNfc(entity.getNfctoken());
                        busqueda.setMostrar(false);
                        busqueda.setData(detalleBusquedas);
                        busqueda.setActions(actions);

                        Barcode barcode = new Barcode();
                        barcode.setCodigo(entity.getBarcode());
                        RealmList<Barcode> barcodes = new RealmList<>();
                        barcodes.add(self.copyToRealm(barcode));
                        busqueda.setBarcode(barcodes);
                    }

                    if (i < PAGINATE) {
                        alphabetAdapter.add(busqueda);
                    }
                    i += 1;
                }

            });
        }

        new saveDatabase().execute(busquedasTemp);
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

        private final List<Entidades> entidades;

        private Descargas(List<Entidades> entidades) {
            this.entidades = entidades;
        }

        public List<Entidades> getEntidades() {
            return entidades;
        }
    }

    private static class Entidades {

        private final Long id;

        private final String type;

        private final String codigo;

        private final String nombre;

        private final String qrcode;

        private final List<Barcode> barcode;

        private final String nfctoken;

        public Entidades(Long id, String type, String codigo, String nombre, String qrcode, List<Barcode> barcode, String nfctoken) {
            this.id = id;
            this.type = type;
            this.codigo = codigo;
            this.nombre = nombre;
            this.qrcode = qrcode;
            this.barcode = barcode;
            this.nfctoken = nfctoken;
        }

        public Long getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public String getCodigo() {
            return codigo;
        }

        public String getNombre() {
            return nombre;
        }

        public String getQrcode() {
            return qrcode;
        }

        public List<Barcode> getBarcode() {
            return barcode;
        }

        public String getNfctoken() {
            return nfctoken;
        }
    }

    private static class DescargasOld {

        private final List<EntidadesOld> entidades;

        private DescargasOld(List<EntidadesOld> entidades) {
            this.entidades = entidades;
        }

        public List<EntidadesOld> getEntidades() {
            return entidades;
        }
    }

    private static class EntidadesOld {

        private final Long id;

        private final String type;

        private final String codigo;

        private final String nombre;

        private final String qrcode;

        private final String barcode;

        private final String nfctoken;

        public EntidadesOld(Long id, String type, String codigo, String nombre, String qrcode, String barcode, String nfctoken) {
            this.id = id;
            this.type = type;
            this.codigo = codigo;
            this.nombre = nombre;
            this.qrcode = qrcode;
            this.barcode = barcode;
            this.nfctoken = nfctoken;
        }

        public Long getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public String getCodigo() {
            return codigo;
        }

        public String getNombre() {
            return nombre;
        }

        public String getQrcode() {
            return qrcode;
        }

        public String getBarcode() {
            return barcode;
        }

        public String getNfctoken() {
            return nfctoken;
        }
    }

    private class saveDatabase extends AsyncTask<List<Busqueda>, Void, Void> {
        @Override
        protected Void doInBackground(List<Busqueda>... lists) {
            List<Busqueda> busquedas = lists[0];
            if (busquedas.isEmpty()) {
                return null;
            }

            Realm realm = Realm.getDefaultInstance();

            try {
                realm.executeTransaction(self -> {
                    self.insertOrUpdate(busquedas);
                });

                busquedasSingleton.loadBusquedas();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                realm.close();
            }

            return null;
        }
    }


    private static class BusquedasSingleton {

        private static BusquedasSingleton instance;

        private List<Busqueda> rows = new ArrayList<>();

        private boolean loaded = false;

        public static BusquedasSingleton getInstance() {
            if (instance == null) {
                instance = new BusquedasSingleton();
            }

            return instance;
        }

        public List<Busqueda> getBusquedas(int page) {

            if (loaded) {
                loadBusquedas();
                loaded = true;
            }

            if (rows.isEmpty()) {
                return rows;
            }

            int start = page  * PAGINATE;
            int end   = start + PAGINATE;

            if ((end-1) > rows.size() ) {
                end = rows.size();
            }

            if (start >= end) {
                return rows;
            }

            return rows.subList(start, end);
        }


        public void loadBusquedas() {
            Realm realm = Realm.getDefaultInstance();

            try {
                /* ******* actualizar el singleton   ************  */
                Cuenta cuenta = realm.where(Cuenta.class)
                        .equalTo("active", true)
                        .findFirst();

                /* hago la consulta porque no se si hay otros tipos */
                RealmResults<Busqueda> results = realm.where(Busqueda.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .beginGroup()
                        .equalTo("type", "Equipo").or()
                        .equalTo("type", "InstalacionLocativa").or()
                        .equalTo("type", "InstalacionProceso")
                        .endGroup()
                        .findAll();

                rows = realm.copyFromRealm(results);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                realm.close();
            }
        }
    }


}