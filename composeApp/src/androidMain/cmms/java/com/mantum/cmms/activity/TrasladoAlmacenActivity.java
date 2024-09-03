package com.mantum.cmms.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Almacen;
import com.mantum.cmms.entity.Bodega;
import com.mantum.cmms.entity.Busqueda;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.entity.TrasladoAlmacen;
import com.mantum.cmms.entity.TrasladoItems;
import com.mantum.cmms.helper.TransaccionHelper;
import com.mantum.cmms.service.AlmacenService;
import com.mantum.cmms.service.TransaccionService;
import com.mantum.component.Mantum;
import com.mantum.component.OnValueChange;
import com.mantum.component.adapter.ItemsAlmacenAdapter;
import com.mantum.component.adapter.MovableFloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;
import io.realm.RealmResults;

public class TrasladoAlmacenActivity  extends Mantum.NfcActivity {

    private static final String TAG = TrasladoAlmacenActivity.class.getSimpleName();
    private Database database;
    private ItemsAlmacenAdapter<Almacen> alphabetAdapter;
    private  Long[] selectedIds;
    public static final String QR_CODE = "QR_CODE";
    private Cuenta cuenta;
    private final Gson gson = new Gson();
    private Spinner spinnerView;
    private ArrayList<Long> newItems = new ArrayList();
    private TransaccionService transaccionService;
    private AlmacenService almacenService;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final TransaccionHelper.Dialog dialogHelper = new TransaccionHelper.Dialog(this);
    private LinearLayoutManager layoutManager;
    private RecyclerView recyclerView;

    public void setAlmacenOrigen(Long almacenOrigen) { this.almacenOrigen = almacenOrigen; }

    private Long almacenOrigen;
    public static final int RESULT_OK_BACK = 1;
    public static final int RESULT_OK_SAVE = 2;
    public static final int REQUEST_ACTION = 2;
    private final String ACTIVO = "Activo";
    private final int TIME_ALERT = 7000;
    private long bodega = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_traslado_almacen);

            database = new Database(this);
            transaccionService = new TransaccionService(this);
            includeBackButtonAndTitle(R.string.traslado_almacen_title);

            alphabetAdapter = new ItemsAlmacenAdapter<>(this);
            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            almacenService = new AlmacenService(this, cuenta);

            if (cuenta == null)
                throw new Exception(getString(R.string.error_authentication));

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                bodega = bundle.getLong(AlmacenActivity.BODEGA_ORIGEN);
                this.almacenOrigen = bundle.getLong(AlmacenActivity.BODEGA_ORIGEN);
                long[] longaArray = bundle.getLongArray(AlmacenActivity.ITEMS_TRASLADO);

                Long[] selectedIdsTmp;
                if (longaArray != null) {
                    selectedIdsTmp = new Long[longaArray.length];

                    for (int i = 0; i < longaArray.length; i++)
                        selectedIdsTmp[i] = longaArray[i];

                    selectedIds = selectedIdsTmp;
                }
            }

            RealmResults<Bodega> query = database.where(Bodega.class)
                    .findAll();

            List<Bodega> bodegas = query.isManaged() ? database.copyFromRealm(query) : query;
            spinnerView= findViewById(R.id.mSpinner);
            ArrayList<String> bodegasList = new ArrayList<>();

            for (Bodega bodega : bodegas)
                bodegasList.add(bodega.getNombre());

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, bodegasList);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerView.setAdapter(arrayAdapter);

            layoutManager = new LinearLayoutManager(this);
            recyclerView = findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemViewCacheSize(20);
            recyclerView.setDrawingCacheEnabled(true);
            recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(alphabetAdapter);

            MovableFloatingActionButton camera = findViewById(R.id.camera);
            camera.setOnClickListener(v -> {
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.setOrientationLocked(true);
                integrator.setCameraId(0);
                integrator.setPrompt("");
                integrator.setCaptureActivity(CaptureActivityPortrait.class);
                integrator.setBeepEnabled(false);
                integrator.initiateScan();
            });

            alphabetAdapter.setOnAction(new OnValueChange<Almacen>() {

                @Override
                public void onClick(Almacen value, int position) {
                    alphabetAdapter.remove(position);
                    alphabetAdapter.notifyItemRemoved(position);
                }

                @Override
                public boolean onChange(Almacen value, TextView position) {
                    float cantidad = Float.valueOf(position.getText().toString());
                    if(cantidad > value.getCantidad()){
                        Toast alert = Toast.makeText(getApplicationContext(),
                                R.string.traslado_cantidad_error, Toast.LENGTH_SHORT);

                        alert.setGravity(Gravity.CENTER|Gravity.TOP,0,50);
                        alert.show();
                        position.setText("1");
                    }
                    return false;
                }

                @Override
                public void onTextChange(Float value, int position) {
                    alphabetAdapter.getItemPosition(position).setCantidadentrar(value);
                }
            });

            setItems();
            closeKeyboard();

        } catch (Exception e) {
            backActivity(getString(R.string.error_app));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_guardar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_save:
                saveTransferencia();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void saveTransferencia() {

        if(recyclerView.getChildCount() == 0) {
            Snackbar.make(getView(), R.string.traslado_void, Snackbar.LENGTH_LONG)
                    .setDuration(TIME_ALERT)
                    .show();
            return;
        }

        ArrayList<TrasladoItems> elementos = new ArrayList<>();
        int activos = 0;
        int recursos = 0;

        for (int i = 0; i < alphabetAdapter.getItemCount(); i++) {
            Almacen item = alphabetAdapter.getItemPosition(i);

            if(item.isActivo())
                activos++;
            else
                recursos++;

            if(item.getCantidad() <= 0) {
                elementos.clear();
                Snackbar.make(getView(), R.string.almacen_transferir_solo_disponible, Snackbar.LENGTH_LONG)
                        .setDuration(TIME_ALERT)
                        .show();
                return;
            }

            TrasladoItems ti = new TrasladoItems();
            Float cant = alphabetAdapter.getItemPosition(i).getCantidadentrar();
            ti.setCantidad(cant);
            ti.setIdelemento(item.getId());

            ti.setNombre(item.getNombre());
            ti.setCodigo(item.getCodigo());
            ti.setCantidaddisponible(item.getCantidad() - cant);

            elementos.add(i,ti);
        }

        if(activos > 0 && recursos > 0){
            elementos.clear();
            Snackbar.make(getView(), R.string.permitir_solo_tipo_articulo, Snackbar.LENGTH_LONG)
                    .setDuration(TIME_ALERT)
                    .show();
            return;
        }

        Spinner spinner = findViewById(R.id.mSpinner);
        String destino =  spinner.getSelectedItem().toString();
        SwitchCompat aip = findViewById(R.id.ejecutar);
        EditText obs = findViewById(R.id.observacion);

        if(activos > 0 && !aip.isChecked()) {
            Snackbar.make(getView(), R.string.trasladar_ip, Snackbar.LENGTH_LONG)
                    .setDuration(TIME_ALERT)
                    .show();
            return;
        }

        Realm realm = database.instance();
        Bodega bodegaDestino = realm.where(Bodega.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("nombre", destino)
                .findFirst();

        TrasladoAlmacen trasladoAlmacen = new TrasladoAlmacen();
        trasladoAlmacen.setIdalmacenista(cuenta.getId());
        trasladoAlmacen.setIdbodegaorigen(almacenOrigen);
        trasladoAlmacen.setElementos(elementos);
        trasladoAlmacen.setIdbodegadestino(bodegaDestino.getId());
        trasladoAlmacen.setActivosIP(aip.isChecked());
        trasladoAlmacen.setObservacion(obs.getText().toString());

        Transaccion transaccion = new Transaccion();
        transaccion.setUUID(UUID.randomUUID().toString());
        transaccion.setCuenta(cuenta);
        transaccion.setUrl(cuenta.getServidor().getUrl() + "/restapp/app/savestoretransfer");
        transaccion.setVersion(cuenta.getServidor().getVersion());
        transaccion.setValue(trasladoAlmacen.toJson());
        transaccion.setModulo(Transaccion.MODULO_ALMACEN);
        transaccion.setAccion(Transaccion.ACCION_TRASLADO_ALMACEN);
        transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);

        dialogHelper.showProgressDialog();

        compositeDisposable.add(transaccionService.save(transaccion)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(self -> { }, this::onError, this::onComplete));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (database != null)
            database.close();

        if (transaccionService != null)
            transaccionService.close();

        alphabetAdapter.clear();
        compositeDisposable.clear();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        long[] longArray = new long[newItems.size()];
        for (int i = 0; i < newItems.size(); i++)
            longArray[i] = newItems.get(i);

        Bundle bundle = new Bundle();
        bundle.putLongArray(AlmacenActivity.ITEMS_TRASLADO, longArray);
        intent.putExtras(bundle);

        setResult(RESULT_OK_BACK, intent);
        finish();
    }

    private void isEmptyList() {
        RelativeLayout container = findViewById(R.id.empty);
        container.setVisibility(alphabetAdapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private List<Almacen> getItemsAlmacen(Cuenta cuenta) {
        Realm realm = database.instance();
        RealmResults<Almacen> itemsAlmacen = realm.where(Almacen.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .in("id", selectedIds)
                .findAll();

        if(bodega > 0)
            itemsAlmacen = itemsAlmacen.where().equalTo("idbodega", bodega).findAll();

        return itemsAlmacen;
    }

    private void setItems() {

        Realm realm = database.instance();
        Cuenta cuenta = realm.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null)
            return;

        List<Almacen> items = getItemsAlmacen(cuenta);
        for (Almacen almacen : items) {
            if (almacen.getCantidadentrar() == null) {
                almacen.setCantidadentrar(1F);
            }
            alphabetAdapter.add(almacen);
        }

        isEmptyList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        String contents = result.getContents();
        if (contents != null) {
            if (QR_CODE.equals(result.getFormatName())) {
                if(result.getContents().startsWith("{")) {
                    Busqueda.Read read = gson.fromJson(contents, Busqueda.Read.class);
                    if (read.getEntityCode() == null) {
                        Snackbar.make(getView(), getString(R.string.message_search_empty), Snackbar.LENGTH_LONG)
                                .setDuration(TIME_ALERT)
                                .show();
                        return;
                    }
                    search(read.getEntityCode());
                } else {
                    search(contents);
                }
            } else {
                search(contents);
            }
        } else {
            Snackbar.make(getView(), getString(R.string.message_search_empty_scan) + null, Snackbar.LENGTH_LONG)
                    .setDuration(TIME_ALERT)
                    .show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void search(String criterio) {
        try {
            Bodega bodega = database.where(Bodega.class)
                    .equalTo("id", almacenOrigen)
                    .findFirst();

            if (bodega != null)
                requestElementoQR(criterio, bodega.getId());
        } catch (Exception e) {
            Log.e(TAG, "searchQR: ", e);
        }
    }

    private void requestElementoQR(String criterio, Long bodega) {
        compositeDisposable.add(almacenService.getElementoQR(criterio, bodega)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> onNextElementoQR(response, bodega, criterio), this::onError, () -> {

                    Almacen almacen = database.where(Almacen.class)
                            .equalTo("qrcode", criterio)
                            .equalTo("idbodega", bodega)
                            .findFirst();

                    if (almacen == null) {
                        Snackbar.make(getView(), getString(R.string.item_not_in_store_1) + " \"" + criterio + "\" " + getString(R.string.item_not_in_store_2), Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    Almacen busqueda = database.copyFromRealm(almacen);

                    alphabetAdapter.add(busqueda);
                    alphabetAdapter.showMessageEmpty(getView());
                    alphabetAdapter.refresh();
                    if(selectedIds != null)
                        newItems.add(busqueda.getId());
                })
        );
    }

    private void onNextElementoQR(Response response, Long bodega, String qrcode) {
        if (!response.isValid())
            return;

        try {
            database.executeTransaction(self -> {
                List<Almacen> almacenes = response.getBody(Almacen.Request.class).getRecursos();

                for (Almacen almacen : almacenes) {
                    almacen.setIdbodega(bodega);
                    almacen.setQrcode(qrcode);
                }

                self.insertOrUpdate(almacenes);
            });
        } catch (Exception e) {
            Log.d(TAG, "onNextElemento: " + e);
        }
    }

    private void onComplete() {
        Intent intent = new Intent();
        setResult(RESULT_OK_SAVE, intent);
        dialogHelper.dismissProgressDialog();
        finish();
    }

    private void onError(Throwable throwable) {
        Log.e(TAG, "onError: ", throwable);
        dialogHelper.dismissProgressDialog();
        Snackbar.make(getView(), R.string.traslado_save_error, Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent != null) {
            String value = prepareNFCRead(intent, false);
            search(value);
        }
    }

}
