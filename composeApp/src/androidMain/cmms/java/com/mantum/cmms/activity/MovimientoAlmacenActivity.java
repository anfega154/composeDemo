package com.mantum.cmms.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Almacen;
import com.mantum.cmms.entity.Bodega;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.MovimientoAlmacen;
import com.mantum.cmms.entity.TipoMovimiento;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.entity.parameter.UserParameter;
import com.mantum.cmms.helper.TransaccionHelper;
import com.mantum.cmms.service.TransaccionService;
import com.mantum.component.OnValueChange;
import com.mantum.component.adapter.ItemsAlmacenAdapter;
import com.mantum.core.util.Assert;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.RealmResults;

public class MovimientoAlmacenActivity extends TransaccionHelper.Dialog {

    private static final String TAG = MovimientoAlmacenActivity.class.getSimpleName();
    private Database database;
    private ItemsAlmacenAdapter<Almacen> movimientoAdapter;
    public static final String QR_CODE = "QR_CODE";
    public static final String ACTION2 = "AGREGAR_ITEM";
    private Cuenta cuenta;
    private TransaccionService transaccionService;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private RecyclerView recyclerViewMovimiento;
    public static final int RESULT_OK_BACK = 1;
    public static final int RESULT_OK_SAVE = 2;
    private String tipoMovimiento = null;
    private String movimiento = null;
    private String almacenMovimiento = null;
    private Long idRT, idgrouprt, idOT;
    private ProgressBar progressBar;
    private Long[] selectedIds;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_movimiento_almacen);
            includeBackButtonAndTitle(R.string.movimiento_bitacora);

            database = new Database(this);
            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null)
                throw new Exception(getString(R.string.error_authentication));

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                tipoMovimiento = bundle.getString("tipoMovimiento");
                movimiento = bundle.getString("movimiento");
                idRT = bundle.getLong("idrt");
                idgrouprt = bundle.getLong("idgrouprt");
                idOT = bundle.getLong("idot");
            }

            transaccionService = new TransaccionService(this);
            progressBar = findViewById(R.id.progressBar);

            movimientoAdapter = new ItemsAlmacenAdapter<>(this);

            RecyclerView.LayoutManager retirarManager = new LinearLayoutManager(this);
            recyclerViewMovimiento = findViewById(R.id.recycler_view_movimiento);
            recyclerViewMovimiento.setLayoutManager(retirarManager);
            recyclerViewMovimiento.setItemViewCacheSize(10);
            recyclerViewMovimiento.setDrawingCacheEnabled(true);
            recyclerViewMovimiento.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            recyclerViewMovimiento.setHasFixedSize(true);
            recyclerViewMovimiento.setAdapter(movimientoAdapter);

            FloatingActionButton accciones = findViewById(R.id.actions);
            accciones.setOnClickListener(v -> {
                Bundle bundleM = new Bundle();
                bundleM.putBoolean(ACTION2, true);
                bundleM.putString("tipoElemento", "articulos");
                bundleM.putBoolean("accionMovimientoAlmacen", true);
                bundleM.putString("movimiento", movimiento);

                View holder = null;
                if (recyclerViewMovimiento != null) {
                    holder = recyclerViewMovimiento.getChildAt(0);
                }

                if (holder != null) {
                    TextView almacenText = holder.findViewById(R.id.summary_2);
                    String almacen = almacenText.getText().toString();
                    bundleM.putString("almacen", almacen);
                }

                Intent intentM = new Intent(MovimientoAlmacenActivity.this, AlmacenActivity.class);
                intentM.putExtras(bundleM);
                startActivityForResult(intentM, 1);
            });

            movimientoAdapter.setOnAction(new OnValueChange<Almacen>() {

                @Override
                public void onClick(Almacen value, int position) {
                    movimientoAdapter.remove(position);
                    this.reload();
                }

                @Override
                public boolean onChange(Almacen value, TextView position) {
                    float cantidad = Float.valueOf(position.getText().toString());
                    if(cantidad > value.getCantidad()){
                        Toast alert = Toast.makeText(getApplicationContext(),
                                R.string.traslado_cantidad_error, Toast.LENGTH_LONG);

                        alert.setGravity(Gravity.CENTER|Gravity.TOP,0,50);
                        alert.show();
                        position.setText("1");
                    }
                    return false;
                }

                @Override
                public void onTextChange(Float value, int position) {

                }

                private void reload() {
                    TextView container_movimiento = getView().findViewById(R.id.empty_movimiento);
                    container_movimiento.setVisibility(movimientoAdapter.isEmpty() ? View.VISIBLE : View.GONE);
                    movimientoAdapter.refresh();
                }
            });

            isEmptyList();

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
                saveMovimiento();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void saveMovimiento() {
        if(movimientoAdapter.getItemCount() == 0) {
            Snackbar.make(getView(), R.string.movimiento_void, Snackbar.LENGTH_LONG).show();
            return;
        }

        ArrayList elementosMovimiento = new ArrayList<>();

        try {
            for (int i = 0, size = recyclerViewMovimiento.getChildCount(); i < size; i++) {
                RecyclerView.ViewHolder holder = recyclerViewMovimiento.getChildViewHolder(recyclerViewMovimiento.getChildAt(i));
                if (holder != null) {
                    EditText cantidad = holder.itemView.findViewById(R.id.cantidad);
                    Almacen item = movimientoAdapter.getItemPosition(i);

                    Float cantidadIngresada = Float.valueOf(cantidad.getText().toString());

                    if (item != null) {
                        if (movimiento.equals("salida"))
                            if (cantidadIngresada > item.getCantidad()) {
                                closeKeyboard();
                                Snackbar.make(getView(), getString(R.string.movimiento_cantidad_elemento_error) + " " + item.getCodigo(), Snackbar.LENGTH_LONG).show();
                                return;
                            }

                        if (cantidadIngresada <= 0) {
                            closeKeyboard();
                            Snackbar.make(getView(), getString(R.string.movimiento_cantidad_cero), Snackbar.LENGTH_LONG).show();
                            return;
                        }
                    }

                    MovimientoAlmacen.Resources resources = new MovimientoAlmacen.Resources();

                    resources.setIdarticle(item.getId());
                    resources.setQuantity(cantidadIngresada);

                    elementosMovimiento.add(i, resources);
                }
            }

            Date date = Calendar.getInstance().getTime();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String fechaActual = simpleDateFormat.format(date);

            List<Bodega> bodegas = database.where(Bodega.class)
                    .findAll();

            String codigoBodega = null;

            for (Bodega bodegaReplace : bodegas) {
                if (almacenMovimiento.equals(bodegaReplace.getCodigo() + " | " + bodegaReplace.getNombre())) {
                    codigoBodega = almacenMovimiento.replace(" | " + bodegaReplace.getNombre(), "");
                    break;
                }
            }

            Bodega bodega = database.where(Bodega.class)
                    .equalTo("codigo", codigoBodega)
                    .findFirst();

            String salidaRutaApp = null;
            if (UserParameter.getValue(getView().getContext(), UserParameter.SALIDA_RUTA) != null)
                salidaRutaApp = UserParameter.getValue(getView().getContext(), UserParameter.SALIDA_RUTA);

            String entradaOTApp = null;
            if (UserParameter.getValue(getView().getContext(), UserParameter.ENTRADA_OT) != null)
                entradaOTApp = UserParameter.getValue(getView().getContext(), UserParameter.ENTRADA_OT);

            String salidaOTApp = null;
            if (UserParameter.getValue(getView().getContext(), UserParameter.SALIDA_OT) != null)
                salidaOTApp = UserParameter.getValue(getView().getContext(), UserParameter.SALIDA_OT);

            MovimientoAlmacen movimientoAlmacen = new MovimientoAlmacen();
            movimientoAlmacen.setToken(UUID.randomUUID().toString());
            movimientoAlmacen.setDate(fechaActual);
            movimientoAlmacen.setMovement(movimiento);
            movimientoAlmacen.setIdstore(bodega.getId());
            movimientoAlmacen.setResources(elementosMovimiento);

            if (idRT != 0 && idgrouprt != 0 && salidaRutaApp != null) {
                movimientoAlmacen.setIdmovementtype(Integer.parseInt(salidaRutaApp));
                movimientoAlmacen.setIdrt(idRT);
                movimientoAlmacen.setIdgrouprt(idgrouprt);
            }
            else if (idOT != 0) {
                movimientoAlmacen.setIdot(idOT);
                if (movimiento.equals("entrada") && entradaOTApp != null)
                    movimientoAlmacen.setIdmovementtype(Integer.parseInt(entradaOTApp));

                if (movimiento.equals("salida") && salidaOTApp != null)
                    movimientoAlmacen.setIdmovementtype(Integer.parseInt(salidaOTApp));
            }
            else {
                TipoMovimiento tipoMovimientos = database.where(TipoMovimiento.class)
                        .equalTo("nombre", tipoMovimiento)
                        .findFirst();

                movimientoAlmacen.setIdmovementtype(Math.round(Float.parseFloat(tipoMovimientos.getId())));
            }

            Transaccion transaccion = new Transaccion();
            transaccion.setUUID(UUID.randomUUID().toString());
            transaccion.setCuenta(cuenta);
            transaccion.setUrl(cuenta.getServidor().getUrl() + "/restapp/app/movements");
            transaccion.setVersion(cuenta.getServidor().getVersion());
            transaccion.setValue(movimientoAlmacen.toJson());
            transaccion.setModulo(Transaccion.MODULO_MOVIMIENTO);
            transaccion.setAccion(Transaccion.ACCION_MOVIMIENTO);
            transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);

            showProgressDialogMovement();

            compositeDisposable.add(transaccionService.save(transaccion)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(self -> {}, this::onError, this::onComplete));
        } catch (Exception e) {
            Log.d(TAG, "saveMovimiento: " + e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }

        movimientoAdapter.clear();
        compositeDisposable.clear();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_OK_BACK, intent);
        finish();
    }

    public void isEmptyList() {
        TextView emptyMovimiento = findViewById(R.id.empty_movimiento);
        emptyMovimiento.setVisibility(movimientoAdapter.isEmpty() ? View.VISIBLE : View.GONE);

        movimientoAdapter.refresh();
    }

    private List<Almacen> getItemsAlmacen() {
        RealmResults<Almacen> itemsAlmacen = database.where(Almacen.class)
                .in("id", selectedIds)
                .findAll();

        List<Almacen> listItems = itemsAlmacen.isManaged()
                ? database.copyFromRealm(itemsAlmacen)
                : itemsAlmacen;

        return new ArrayList<>(listItems);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (!Assert.isNull(data)) {
            if(resultCode == 1) {
                try {
                    long[] longArray = data.getExtras().getLongArray(AlmacenActivity.ITEMS_TRASLADO);
                    Long[] selectedIdsTmp = new Long[longArray.length];

                    for (int i = 0; i < longArray.length; i++)
                        selectedIdsTmp[i] = longArray[i];

                    selectedIds = selectedIdsTmp;
                    ItemsAlmacenAdapter recorrer = movimientoAdapter;

                    List<Almacen> items = getItemsAlmacen();
                    recorrer.addAll(items);

                    isEmptyList();

                    almacenMovimiento = data.getExtras().getString("almacenMovimiento");
                } catch (Exception e) {
                    Log.e(TAG, "onResult: ", e);
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onCompleteGeneral() {
        progressBar.setVisibility(View.GONE);
        Intent intent = new Intent();
        setResult(RESULT_OK_SAVE, intent);
        isEmptyList();
    }

    private void onComplete() {
        dismissProgressDialog();
        finish();
    }

    private void onError(Throwable throwable) {
        Log.e(TAG, "onError: ", throwable);
        dismissProgressDialog();
        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG).show();
        onCompleteGeneral();
    }
}
