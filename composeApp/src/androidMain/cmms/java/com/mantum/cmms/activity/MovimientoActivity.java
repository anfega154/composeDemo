package com.mantum.cmms.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

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
import com.mantum.cmms.entity.InstalacionProcesoStandBy;
import com.mantum.cmms.entity.Jerarquia;
import com.mantum.cmms.entity.Movimiento;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.entity.TrasladoItems;
import com.mantum.cmms.helper.TransaccionHelper;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.cmms.service.MovimientoService;
import com.mantum.cmms.service.TransaccionService;
import com.mantum.component.Mantum;
import com.mantum.component.OnSelected;
import com.mantum.component.OnValueChange;
import com.mantum.component.adapter.AlphabetAdapter;
import com.mantum.component.adapter.ItemsAlmacenAdapter;
import com.mantum.component.adapter.MovableFloatingActionButton;
import com.mantum.core.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;
import io.realm.RealmResults;

// Movimientos por OT
public class MovimientoActivity  extends Mantum.NfcActivity {

    private static final String TAG = MovimientoActivity.class.getSimpleName();
    private Database database;
    private ItemsAlmacenAdapter<Almacen> instalarAdapter, retirarAdapter, recursosAdapter;
    public static final String QR_CODE = "QR_CODE";
    private Cuenta cuenta;
    private final Gson gson = new Gson();
    private TransaccionService transaccionService;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final TransaccionHelper.Dialog dialogHelper = new TransaccionHelper.Dialog(this);
    private RecyclerView recyclerViewI, recyclerViewR, recyclerViewRecurso;
    public static final int RESULT_OK_BACK = 1;
    public static final int RESULT_OK_SAVE = 2;
    public static final int REQUEST_ACTION = 2;

    public static final String TIPO1 = "INSTALACION";
    public static final String TIPO2 = "RETIRO";
    public static final String TIPO3 = "INSTALACION - RETIRO";
    public static final String TIPO4 = "REEMPLAZO";

    public static final String ACTION2 = "AGREGAR_ITEM";
    public static final String ACTION3 = "ASIGNAR_PADRE";

    public static final String ESTADO1 = "Stand By";
    public static final String ESTADO2 = "Inactivo";

    private Animation slide_in_left, slide_out_right, slide_in_right, slide_out_left;
    private ViewAnimator viewAnimator;
    private Button focusInsta,focusRet, focusRec, buttonPrev,buttonNext,buttonRecursos;
    private String actionCamera;
    private Long OT, ENTIDAD;
    private ProgressBar progressBar;
    private MovimientoService movimientoService;
    private AlertDialog.Builder dialogGeneral;
    private AlertDialog closeJerarquia, closeBtnactions, closeDialogNfcScan;
    private AlphabetAdapter<Jerarquia> alphabetAdapter;
    private Long[] selectedIds;
    private Spinner spinnerEstados, spinnerProcesos, spinnerView;
    public List<InstalacionProcesoStandBy> ipstandby = new ArrayList<>();
    private int activeBtn = 0;
    private final int TIME_ALERT = 7000;
    private float oldTouchValue;

    private Almacen currentItemInstalar;
    public void setCurrentItemInstalar(Almacen currentItemInstalar) { this.currentItemInstalar = currentItemInstalar; }

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_movimiento);
            includeBackButtonAndTitle(R.string.movimiento_bitacora);

            database = new Database(this);
            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null)
                throw new Exception(getString(R.string.error_authentication));


            alphabetAdapter = new AlphabetAdapter<>(this,false);
            dialogGeneral = new AlertDialog.Builder(this, R.style.DialogTheme);
            movimientoService = new MovimientoService(this, cuenta);
            transaccionService = new TransaccionService(this);
            progressBar = findViewById(R.id.progressBar);
            actionCamera = ACTION2;

            Bundle bundle = getIntent().getExtras();
            boolean noValidarEntidad;
            if (bundle != null) {
                this.OT = bundle.getLong(Mantum.KEY_ID);
                noValidarEntidad = bundle.getBoolean("noValidarEntidad");
            }
            else
                throw new Exception(getString(R.string.detail_error_OT));

            instalarAdapter = new ItemsAlmacenAdapter<>(this);
            instalarAdapter.setMultipleActions(true);
            instalarAdapter.setMenu(R.menu.menu_elementos);
            instalarAdapter.setAllOptions(true);

            retirarAdapter = new ItemsAlmacenAdapter<>(this);
            retirarAdapter.setMultipleActions(true);
            retirarAdapter.setMenu(R.menu.menu_elementos);

            recursosAdapter = new ItemsAlmacenAdapter<>(this);
            recursosAdapter.setMenu(R.menu.menu_elementos);
            recursosAdapter.setShowOutputType(true);

            spinnerView = findViewById(R.id.mSpinner);
            List<String> tipos = new ArrayList<>();
            tipos.add(TIPO1);
            tipos.add(TIPO2);
            tipos.add(TIPO3);
            tipos.add(TIPO4);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tipos);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerView.setAdapter(arrayAdapter);

            RecyclerView.LayoutManager instalarManager = new LinearLayoutManager(this);
            recyclerViewI = findViewById(R.id.recycler_view_instalar);
            recyclerViewI.setLayoutManager(instalarManager);
            recyclerViewI.setItemViewCacheSize(10);
            recyclerViewI.setDrawingCacheEnabled(true);
            recyclerViewI.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            recyclerViewI.setHasFixedSize(true);
            recyclerViewI.setAdapter(instalarAdapter);

            RecyclerView.LayoutManager retirarManager = new LinearLayoutManager(this);
            recyclerViewR = findViewById(R.id.recycler_view_retirar);
            recyclerViewR.setLayoutManager(retirarManager);
            recyclerViewR.setItemViewCacheSize(10);
            recyclerViewR.setDrawingCacheEnabled(true);
            recyclerViewR.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            recyclerViewR.setHasFixedSize(true);
            recyclerViewR.setAdapter(retirarAdapter);

            RecyclerView.LayoutManager recursoManager = new LinearLayoutManager(this);
            recyclerViewRecurso = findViewById(R.id.recycler_view_recursos);
            recyclerViewRecurso.setLayoutManager(recursoManager);
            recyclerViewRecurso.setItemViewCacheSize(10);
            recyclerViewRecurso.setDrawingCacheEnabled(true);
            recyclerViewRecurso.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            recyclerViewRecurso.setHasFixedSize(true);
            recyclerViewRecurso.setAdapter(recursosAdapter);

            instalarAdapter.setOnCall((menu, value) -> {
                setCurrentItemInstalar(value);
                switch (menu.getItemId()) {
                    case R.id.drop:
                        removeItem(instalarAdapter, value);
                        break;
                    case R.id.nfc:
                        actionCamera = ACTION3;
                        try {
                            LayoutInflater factory = LayoutInflater.from(this);
                            final View view = factory.inflate(R.layout.dialog_entidad_scan, null);
                            view.findViewById(R.id.section_scan).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.navigation_camera).setVisibility(View.GONE);
                            view.findViewById(R.id.navigation_nfc).setVisibility(View.GONE);
                            dialogGeneral.setView(view);
                            dialogGeneral.setCancelable(false);
                            dialogGeneral.setNegativeButton(R.string.close, (dialog, id) -> {
                                actionCamera = ACTION2;
                                closeDialogNfcScan.dismiss();
                            });
                            closeDialogNfcScan = dialogGeneral.show();
                        } catch (Exception e) {
                            Log.d(TAG, "nfcScan: "+e);
                        }
                        break;
                    case R.id.qr:
                        actionCamera = ACTION3;
                        openQRCamera();
                        break;
                    case R.id.jerarquia:
                        dialogAsignarPadreJerarquia(value);
                        break;

                    case R.id.tipo_salida:
                        dialogTipoSalida(value);
                        break;
                }

                return super.onOptionsItemSelected(menu);
            });

            retirarAdapter.setOnCall((menu, value) -> {
                if (menu.getItemId() == R.id.drop) {
                    removeItem(retirarAdapter, value);
                }

                return super.onOptionsItemSelected(menu);
            });

            recursosAdapter.setOnCall((menu, value) -> {
                switch (menu.getItemId()) {
                    case R.id.drop:
                        removeItem(recursosAdapter, value);
                        break;

                    case R.id.tipo_salida:
                        dialogTipoSalida(value);
                        break;
                }

                return super.onOptionsItemSelected(menu);
            });

            MovableFloatingActionButton accciones = findViewById(R.id.actions);
            accciones.setOnClickListener(v -> {
                LayoutInflater factory = LayoutInflater.from(this);
                View vistaAcciones = factory.inflate(R.layout.dialog_acciones, null);

                TextView act1 = vistaAcciones.findViewById(R.id.navigation_camera);
                act1.setOnClickListener(a -> {
                    actionCamera = ACTION2;
                    openQRCamera();
                });

                TextView act2 = vistaAcciones.findViewById(R.id.navigation_equipos);

                if (activeBtn == 1)
                    act2.setVisibility(View.GONE);

                act2.setOnClickListener(b -> {
                    if (activeBtn == 2) {
                        Intent intent = new Intent(this, AlmacenActivity.class);
                        intent.putExtra(ACTION2, true);
                        intent.putExtra("tipoElemento", "recursos");
                        intent.putExtra("accionMovimientoAlmacen", true);
                        startActivityForResult(intent, 1);
                    } else {
                        Bundle bundleM = new Bundle();
                        bundleM.putBoolean(ACTION2, true);

                        bundleM.putString("tipoElemento", "activos");
                        bundleM.putBoolean("accionMovimientoAlmacen", false);

                        Intent intentM = new Intent(MovimientoActivity.this, AlmacenActivity.class);
                        intentM.putExtras(bundleM);
                        startActivityForResult(intentM, 1);
                    }
                });

                TextView act3 = vistaAcciones.findViewById(R.id.navigation_marcar);
                act3.setOnClickListener(c -> startActivity(new Intent(MovimientoActivity.this, BusquedaActivity.class)));

                if (UserPermission.check(this, UserPermission.MARCACION_EQUIPOS, true)) {
                    act3.setVisibility(View.VISIBLE);
                }

                TextView act4 = vistaAcciones.findViewById(R.id.navigation_crear);
                act4.setOnClickListener(d -> {

                    if (activeBtn == 0 || activeBtn == 2) {
                        Snackbar.make(getView(), R.string.movimiento_item_no_permitido, Snackbar.LENGTH_LONG)
                                .setDuration(TIME_ALERT)
                                .show();
                        return;
                    }

                    LayoutInflater factory2 = LayoutInflater.from(this);
                    final View view = factory2.inflate(R.layout.dialog_crear_item, null);

                    spinnerEstados = view.findViewById(R.id.estado);
                    List<String> estados = new ArrayList<>();
                    estados.add(ESTADO1);
                    estados.add(ESTADO2);
                    ArrayAdapter<String> arrayAdapterEstados = new ArrayAdapter<String>(MovimientoActivity.this, android.R.layout.simple_spinner_item, estados);
                    arrayAdapterEstados.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerEstados.setAdapter(arrayAdapterEstados);

                    spinnerProcesos = view.findViewById(R.id.ip);
                    List<String> ipstandby = new ArrayList<>();
                    for (InstalacionProcesoStandBy procesos : getIntalacioneStandBy(cuenta))
                        ipstandby.add(procesos.getNombre());

                    ArrayAdapter<String> arrayAdapterAlmacen = new ArrayAdapter<String>(MovimientoActivity.this, android.R.layout.simple_spinner_item, ipstandby);
                    arrayAdapterAlmacen.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerProcesos.setAdapter(arrayAdapterAlmacen);

                    dialogGeneral.setPositiveButton(R.string.aceptar, (dialog, whichButton) -> {
                        try {
                            TextInputEditText nombre = view.findViewById(R.id.nombre);
                            TextInputEditText codigo = view.findViewById(R.id.codigo);
                            TextInputEditText descripcion = view.findViewById(R.id.observacion);
                            TextInputEditText cantidad = view.findViewById(R.id.cantidad);
                            Spinner spinnerEstado = view.findViewById(R.id.estado);
                            String estado = spinnerEstado.getSelectedItem().toString();
                            Spinner spinnerIP = view.findViewById(R.id.ip);
                            String instalacionProceso = spinnerIP.getSelectedItem().toString();

                            Almacen itemCreate = new Almacen();
                            itemCreate.setId(new Random().nextLong());
                            itemCreate.setNombre(nombre.getText().toString());
                            itemCreate.setCodigo(codigo.getText().toString());
                            itemCreate.setDescripcion(descripcion.getText().toString());
                            itemCreate.setCantidad(Float.valueOf(cantidad.getText().toString()));
                            itemCreate.setEstado(estado);
                            itemCreate.setCantidadentrar(Float.valueOf(cantidad.getText().toString()));

                            for (InstalacionProcesoStandBy procesos : getIntalacioneStandBy(cuenta)) {
                                if (procesos.getNombre().equals(instalacionProceso)) {
                                    itemCreate.setIppadrestandby(procesos.getId());
                                    break;
                                }
                            }

                            if (activeBtn == 1) {
                                instalarAdapter.add(itemCreate);
                                instalarAdapter.showMessageEmpty(getView());
                            }

                            isEmptyList();
                            dialog.dismiss();

                        } catch (Exception e) {
                            Log.d(TAG, "onClick: " + e);
                            Snackbar.make(getView(), R.string.movimiento_error_add_item, Snackbar.LENGTH_LONG)
                                    .show();
                        }

                    });

                    dialogGeneral.setNegativeButton(R.string.cancel, null);
                    dialogGeneral.setView(view);
                    dialogGeneral.setTitle(R.string.movimiento_crear_item);
                    dialogGeneral.show();
                });

                TextView act5 = vistaAcciones.findViewById(R.id.navigation_nfc);
                act5.setOnClickListener(view -> {
                    act1.setVisibility(View.GONE);
                    act2.setVisibility(View.GONE);
                    act3.setVisibility(View.GONE);
                    act4.setVisibility(View.GONE);
                    act5.setVisibility(View.GONE);
                    vistaAcciones.findViewById(R.id.section_scan).setVisibility(View.VISIBLE);
                });

                if(activeBtn == 2) {
                    act3.setVisibility(View.GONE);
                    act5.setVisibility(View.GONE);
                }

                dialogGeneral.setNegativeButton(R.string.close, null);
                dialogGeneral.setView(vistaAcciones);
                dialogGeneral.setTitle(null);
                closeBtnactions = dialogGeneral.show();
            });

            instalarAdapter.setOnAction(new OnValueChange<Almacen>() {

                @Override
                public void onClick(Almacen value, int position) {
                    instalarAdapter.remove(position);
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
                    instalarAdapter.getItemPosition(position).setCantidadentrar(value);
                }

                private void reload() {
                    TextView container_i = getView().findViewById(R.id.empty_instalar);
                    container_i.setVisibility(instalarAdapter.isEmpty() ? View.VISIBLE : View.GONE);
                    TextView countI = getView().findViewById(R.id.badge_btn_1);
                    countI.setText(String.valueOf(instalarAdapter.getItemCount()));
                    instalarAdapter.refresh();
                }
            });

            retirarAdapter.setOnAction(new OnValueChange<Almacen>() {

                @Override
                public void onClick(Almacen value, int position) {
                    retirarAdapter.remove(position);
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
                    retirarAdapter.getItemPosition(position).setCantidadentrar(value);
                }

                private void reload() {
                    TextView container_r = getView().findViewById(R.id.empty_retirar);
                    container_r.setVisibility(retirarAdapter.isEmpty() ? View.VISIBLE : View.GONE);
                    TextView countR = getView().findViewById(R.id.badge_btn_2);
                    countR.setText(String.valueOf(retirarAdapter.getItemCount()));
                    retirarAdapter.refresh();
                }
            });

            recursosAdapter.setOnAction(new OnValueChange<Almacen>() {

                @Override
                public void onClick(Almacen value, int position) {
                    recursosAdapter.remove(position);
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
                    recursosAdapter.getItemPosition(position).setCantidadentrar(value);
                }

                private void reload() {
                    TextView container_i = getView().findViewById(R.id.empty_recurso);
                    container_i.setVisibility(recursosAdapter.isEmpty() ? View.VISIBLE : View.GONE);
                    TextView countI = getView().findViewById(R.id.badge_btn_3);
                    countI.setText(String.valueOf(recursosAdapter.getItemCount()));
                    recursosAdapter.refresh();
                }
            });


            buttonPrev = findViewById(R.id.instalar);
            buttonNext = findViewById(R.id.retirar);
            buttonRecursos = findViewById(R.id.recursos);
            viewAnimator = findViewById(R.id.viewanimator);

            slide_in_left = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
            slide_in_right = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
            slide_out_right = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
            slide_out_left = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);

            focusInsta = findViewById(R.id.instalar);
            focusRet = findViewById(R.id.retirar);
            focusRec = findViewById(R.id.recursos);

            recyclerViewI.setOnTouchListener((v, event) -> onTouchEvent(event));
            recyclerViewR.setOnTouchListener((v, event) -> onTouchEvent(event));
            recyclerViewRecurso.setOnTouchListener((v, event) -> onTouchEvent(event));

            buttonPrev.setOnClickListener(view -> {
                if(spinnerView.getSelectedItem().toString().equals(TIPO2)){
                    Toast alert = Toast.makeText(getApplicationContext(),
                            R.string.movimiento_accion_na, Toast.LENGTH_LONG);
                    alert.setGravity(Gravity.CENTER|Gravity.TOP,0,50);
                    alert.show();
                    return;
                }
                viewAnimator.setInAnimation(slide_in_left);
                viewAnimator.setOutAnimation(slide_out_right);
                focusInsta.getBackground().setTint(getView().getResources().getColor(R.color.colorPrimary_2));
                focusRet.getBackground().setTint(getView().getResources().getColor(R.color.gray));
                focusRec.getBackground().setTint(getView().getResources().getColor(R.color.gray));
                viewAnimator.setDisplayedChild(0);
                activeBtn = 0;
            });

            buttonNext.setOnClickListener(view -> {
                if (spinnerView.getSelectedItem().toString().equals(TIPO1)) {
                    Toast alert = Toast.makeText(getApplicationContext(),
                            R.string.movimiento_accion_na, Toast.LENGTH_LONG);
                    alert.setGravity(Gravity.CENTER | Gravity.TOP, 0, 50);
                    alert.show();
                    return;
                }

                if (activeBtn == 2) {
                    viewAnimator.setInAnimation(slide_in_left);
                    viewAnimator.setOutAnimation(slide_out_right);
                } else {
                    viewAnimator.setInAnimation(slide_in_right);
                    viewAnimator.setOutAnimation(slide_out_left);
                }

                focusRet.getBackground().setTint(getView().getResources().getColor(R.color.colorPrimary_2));
                focusInsta.getBackground().setTint(getView().getResources().getColor(R.color.gray));
                focusRec.getBackground().setTint(getView().getResources().getColor(R.color.gray));
                viewAnimator.setDisplayedChild(1);
                activeBtn = 1;
            });

            buttonRecursos.setOnClickListener(view -> {
                if (spinnerView.getSelectedItem().toString().equals(TIPO4)) {
                    Toast alert = Toast.makeText(getApplicationContext(),
                            R.string.movimiento_accion_na, Toast.LENGTH_LONG);
                    alert.setGravity(Gravity.CENTER|Gravity.TOP,0,50);
                    alert.show();
                    return;
                }

                viewAnimator.setInAnimation(slide_in_right);
                viewAnimator.setOutAnimation(slide_out_left);
                focusRec.getBackground().setTint(getView().getResources().getColor(R.color.colorPrimary_2));
                focusInsta.getBackground().setTint(getView().getResources().getColor(R.color.gray));
                focusRet.getBackground().setTint(getView().getResources().getColor(R.color.gray));
                viewAnimator.setDisplayedChild(2);
                activeBtn = 2;
            });

            spinnerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(position==0) {
                        retirarAdapter.clear();
                        buttonPrev.performClick();
                        instalarAdapter.setAllOptions(true);
                    }
                    if(position==1) {
                        buttonNext.performClick();
                        instalarAdapter.clear();
                    }
                    if (position == 3) {
                        recursosAdapter.clear();
                        instalarAdapter.setAllOptions(false);
                        instalarAdapter.setShowOutputType(true);
                        buttonPrev.performClick();
                    }

                    isEmptyList();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });

            OrdenTrabajo ot = database.where(OrdenTrabajo.class)
                    .equalTo("id", this.OT)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findFirst();

            if (ot != null) {
                if (!noValidarEntidad || ot.getEntidadValida() != null) {
                    ENTIDAD = ot.getEntidadValida();
                    requestJerarquia(ot.getEntidadValida());
                } else if (noValidarEntidad && !ot.getEntidades().isEmpty()) {
                    ENTIDAD = ot.getEntidades().get(0).getId();
                    requestJerarquia(ot.getEntidades().get(0).getId());
                }
            }

            isEmptyList();

        } catch (Exception e) {
            backActivity(getString(R.string.error_app));
        }
    }

    private void dialogAsignarPadreJerarquia(Almacen adapterSelect) {

        if(this.ENTIDAD == null) {
            Snackbar.make(getView(), R.string.entidad_vacia, Snackbar.LENGTH_LONG)
                    .setDuration(TIME_ALERT)
                    .show();
            return;
        }

        Realm realm = database.instance();
        RealmResults<Jerarquia> jerarquias = realm.where(Jerarquia.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("entidadfiltro", this.ENTIDAD)
                .findAll();

        List<Jerarquia> all = jerarquias.isManaged()
                ? database.copyFromRealm(jerarquias)
                : jerarquias;

        Collections.sort(all, MovimientoActivity::compare);
        alphabetAdapter.addAll(all);
        alphabetAdapter.setOnAction(new OnSelected<Jerarquia>() {
            @Override
            public void onClick(Jerarquia value, int position) {
                try {
                    for (int i = 0; i < instalarAdapter.getItemCount(); i++) {
                        Almacen itemList = instalarAdapter.getItemPosition(i);
                        if (adapterSelect.getId().equals(itemList.getId())) {
                            value.setCuenta(null);
                            Log.d(TAG, "onClick: "+value);
                            itemList.setJerarquia(value);
                            isEmptyList();
                            closeJerarquia.dismiss();
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, "onClick: "+e);
                }
            }

            @Override
            public boolean onLongClick(Jerarquia value, int position) {
                return false;
            }
        });

        LayoutInflater factory = LayoutInflater.from(this);
        View vistaJerarquia = factory.inflate(R.layout.dialog_entidades, null);

        RecyclerView recyclerView = vistaJerarquia.findViewById(R.id.listaEntidades);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(alphabetAdapter);

        dialogGeneral.setNegativeButton(R.string.close, null);
        dialogGeneral.setView(vistaJerarquia);
        dialogGeneral.setTitle(R.string.title_jerarquia);
        closeJerarquia = dialogGeneral.show();

    }

    private void dialogTipoSalida(Almacen adapterSelect) {
        new AlertDialog.Builder(this)
                .setMessage("Seleccione el tipo de salida")
                .setNegativeButton("Salida OT", (dialogInterface, x) -> {
                    if (activeBtn == 0) {
                        for (int i = 0; i < instalarAdapter.getItemCount(); i++) {
                            Almacen itemList = instalarAdapter.getItemPosition(i);
                            if (adapterSelect.getId().equals(itemList.getId())) {
                                itemList.setTiposalida("Salida OT");
                                isEmptyList();
                                dialogInterface.dismiss();
                                break;
                            }
                        }
                    } else {
                        for (int i = 0; i < recursosAdapter.getItemCount(); i++) {
                            Almacen itemList = recursosAdapter.getItemPosition(i);
                            if (adapterSelect.getId().equals(itemList.getId())) {
                                itemList.setTiposalida("Salida OT");
                                isEmptyList();
                                dialogInterface.dismiss();
                                break;
                            }
                        }
                    }
                })
                .setPositiveButton("Salida Manual", ((dialogInterface, x) -> {
                    if (activeBtn == 0) {
                        for (int i = 0; i < instalarAdapter.getItemCount(); i++) {
                            Almacen itemList = instalarAdapter.getItemPosition(i);
                            if (adapterSelect.getId().equals(itemList.getId())) {
                                itemList.setTiposalida("Salida Manual");
                                isEmptyList();
                                dialogInterface.dismiss();
                                break;
                            }
                        }
                    } else {
                        for (int i = 0; i < recursosAdapter.getItemCount(); i++) {
                            Almacen itemList = recursosAdapter.getItemPosition(i);
                            if (adapterSelect.getId().equals(itemList.getId())) {
                                itemList.setTiposalida("Salida Manual");
                                isEmptyList();
                                dialogInterface.dismiss();
                                break;
                            }
                        }
                    }
                }))
                .show();
    }

    private void removeItem(ItemsAlmacenAdapter<Almacen> adapter, Almacen value) {
        try {
            for (int i = 0; i < adapter.getItemCount(); i++) {
                Almacen itemList = adapter.getItemPosition(i);
                if (value.getId().equals(itemList.getId())) {
                    adapter.remove(i);
                    isEmptyList();
                    break;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "removeItem: "+e);
        }
    }

    private void openQRCamera(){
        IntentIntegrator integrator = new IntentIntegrator(MovimientoActivity.this);
        integrator.setOrientationLocked(true);
        integrator.setCameraId(0);
        integrator.setPrompt("");
        integrator.setCaptureActivity(CaptureActivityPortrait.class);
        integrator.setBeepEnabled(false);
        integrator.initiateScan();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_guardar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction())  {
            case MotionEvent.ACTION_DOWN: {
                oldTouchValue = event.getX();
                break;
            }
            case MotionEvent.ACTION_UP: {
                float currentX = event.getX();
                if (oldTouchValue < currentX) {
                    switch (viewAnimator.getDisplayedChild()){
                        case 2:
                            buttonNext.performClick();
                            break;
                        case 1:
                            buttonPrev.performClick();
                            break;
                    }
                }

                if (oldTouchValue > currentX ) {
                    switch (viewAnimator.getDisplayedChild()){
                        case 0:
                            buttonNext.performClick();
                            break;
                        case 1:
                            buttonRecursos.performClick();
                            break;
                    }
                }
                break;
            }
        }
        return super.onTouchEvent(event);
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
        if (spinnerView.getSelectedItem().toString().equals(TIPO4) && instalarAdapter.getItemCount() != 1) {
            Snackbar.make(getView(), R.string.movimiento_reemplazo_instalar_vacio, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        if (spinnerView.getSelectedItem().toString().equals(TIPO4) && retirarAdapter.getItemCount() != 1) {
            Snackbar.make(getView(), R.string.movimiento_reemplazo_retirar_vacio, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        if( (viewAnimator.getDisplayedChild() == 0 && instalarAdapter.getItemCount() == 0) || (viewAnimator.getDisplayedChild() == 1 && retirarAdapter.getItemCount() == 0) ) {
            Snackbar.make(getView(), R.string.movimiento_void, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        ArrayList<TrasladoItems> elementosRetirar = new ArrayList<>();
        ArrayList<TrasladoItems> elementosNuevos = new ArrayList<>();
        ArrayList<TrasladoItems> elementosInstalar = new ArrayList<>();

        try {

            for (int i = 0; i < instalarAdapter.getItemCount(); i++) {
                Almacen item = instalarAdapter.getItemPosition(i);

                if (item != null) {
                    if(item.getCantidadentrar() == null || item.getCantidadentrar() <= 0) {
                        item.setCantidadentrar(1F);
                    }

                    TrasladoItems ti = new TrasladoItems();
                    ti.setCantidad(item.getCantidadentrar());
                    ti.setIdelemento(item.getId());
                    ti.setIdalmacen(item.getIdbodega());
                    ti.setTiposalida(item.getTiposalida() != null ? item.getTiposalida() : "Salida OT");

                    if(item.getJerarquia() == null && !spinnerView.getSelectedItem().toString().equals(TIPO4)) {
                        elementosInstalar.clear();
                        String msg = getString(R.string.movimiento_jerarquia_valid) +" "+ item.getCodigo();
                        Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG)
                                .setDuration(TIME_ALERT)
                                .show();
                        return;
                    }

                    if (!spinnerView.getSelectedItem().toString().equals(TIPO4)) {
                        Jerarquia.JerarquiaHelper jerarquia = new Jerarquia.JerarquiaHelper();
                        jerarquia.setId(item.getJerarquia().getId());
                        jerarquia.setNombre(item.getJerarquia().getNombre());
                        jerarquia.setEntidadfiltro(item.getJerarquia().getEntidadfiltro());
                        jerarquia.setOrden(item.getJerarquia().getOrden());
                        jerarquia.setTipo(item.getJerarquia().getTipo());
                        ti.setJerarquia(jerarquia);
                    }

                    ti.setNombre(item.getNombre());
                    ti.setCodigo(item.getCodigo());
                    ti.setCantidaddisponible(item.getCantidad() - item.getCantidadentrar());

                    elementosInstalar.add(i, ti);
                }
            }

            for (int i = 0; i < retirarAdapter.getItemCount(); i++) {
                Almacen item = retirarAdapter.getItemPosition(i);

                if (item != null) {
                    if(item.getCantidadentrar() == null || item.getCantidadentrar() <= 0) {
                        item.setCantidadentrar(1F);
                    }

                    TrasladoItems ti = new TrasladoItems();
                    ti.setCantidad(item.getCantidadentrar());
                    ti.setIdelemento(item.getId());
                    ti.setIdalmacen(item.getIdbodega());

                    ti.setNombre(item.getNombre());
                    ti.setCodigo(item.getCodigo());
                    ti.setCantidaddisponible(item.getCantidad() - item.getCantidadentrar());

                    if (item.getIppadrestandby() != null) {
                        elementosNuevos.add(i, ti);
                    } else {
                        elementosRetirar.add(i, ti);
                    }
                }
            }

            for (int i = 0; i < recursosAdapter.getItemCount(); i++) {
                Almacen item = recursosAdapter.getItemPosition(i);

                if (item != null) {
                    if(item.getCantidadentrar() == null || item.getCantidadentrar() <= 0) {
                        item.setCantidadentrar(1F);
                    }

                    TrasladoItems ti = new TrasladoItems();
                    ti.setCantidad(item.getCantidadentrar());
                    ti.setIdelemento(item.getId());
                    ti.setIdalmacen(item.getIdbodega());
                    ti.setTiposalida(item.getTiposalida() != null ? item.getTiposalida() : "Salida OT");

                    ti.setNombre(item.getNombre());
                    ti.setCodigo(item.getCodigo());
                    ti.setCantidaddisponible(item.getCantidad() - item.getCantidadentrar());

                    elementosInstalar.add(elementosInstalar.size() , ti);
                }
            }

            Movimiento movimiento = new Movimiento();
            movimiento.setIdot(this.OT);
            movimiento.setEntrantes(elementosRetirar);
            movimiento.setSalientes(elementosInstalar);
            movimiento.setNuevos(elementosNuevos);

            Spinner spinner = findViewById(R.id.mSpinner);
            movimiento.setTipomovimiento(spinner.getSelectedItem().toString());

            Transaccion transaccion = new Transaccion();
            transaccion.setUUID(UUID.randomUUID().toString());
            transaccion.setCuenta(cuenta);
            transaccion.setUrl(cuenta.getServidor().getUrl() + "/restapp/app/fieldmovements");
            transaccion.setVersion(cuenta.getServidor().getVersion());
            transaccion.setValue(movimiento.toJson());
            transaccion.setModulo(Transaccion.MODULO_MOVIMIENTO);
            transaccion.setAccion(Transaccion.ACCION_MOVIMIENTO);
            transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);

            dialogHelper.showProgressDialogMovement();

            compositeDisposable.add(transaccionService.save(transaccion)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(self -> {
                    }, this::onError, this::onComplete));

        } catch (Exception e) {
            Log.d(TAG, "saveMovimiento: "+ e);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }

        instalarAdapter.clear();
        retirarAdapter.clear();
        compositeDisposable.clear();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_OK_BACK, intent);
        finish();
    }

    public void isEmptyList() {
        TextView container_ins = findViewById(R.id.empty_instalar);
        TextView container_ret = findViewById(R.id.empty_retirar);
        TextView container_rec = findViewById(R.id.empty_recurso);

        container_ins.setVisibility(instalarAdapter.isEmpty() ? View.VISIBLE : View.GONE);
        container_ret.setVisibility(retirarAdapter.isEmpty() ? View.VISIBLE : View.GONE);
        container_rec.setVisibility(recursosAdapter.isEmpty() ? View.VISIBLE : View.GONE);

        TextView countIns = findViewById(R.id.badge_btn_1);
        countIns.setText(String.valueOf(instalarAdapter.getItemCount()));

        TextView countRet = findViewById(R.id.badge_btn_2);
        countRet.setText(String.valueOf(retirarAdapter.getItemCount()));

        TextView countRec = findViewById(R.id.badge_btn_3);
        countRec.setText(String.valueOf(recursosAdapter.getItemCount()));

        retirarAdapter.refresh();
        instalarAdapter.refresh();
        recursosAdapter.refresh();
    }

    private List<Almacen> getItemsAlmacen(Cuenta cuenta) {
        RealmResults<Almacen> itemsAlmacen = database.where(Almacen.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .in("id", selectedIds)
                .findAll();

        List<Almacen> listItems = itemsAlmacen.isManaged()
                ? database.copyFromRealm(itemsAlmacen)
                : itemsAlmacen;

        List<Almacen> agregar = new ArrayList<>();

        if(activeBtn == 0) {
            int omitted = 0;
            for (Almacen almacen : listItems) {
                if(almacen.getCantidad() <= 0)
                    omitted++;
                else
                    agregar.add(almacen);
            }

            if(omitted > 0) {
                Snackbar.make(getView(), getString(R.string.message_search_no_disponibles), Snackbar.LENGTH_LONG)
                        .setDuration(TIME_ALERT)
                        .show();
            }
        } else {
            agregar.addAll(listItems);
        }

        return agregar;
    }

    private List<InstalacionProcesoStandBy> getIntalacioneStandBy(Cuenta cuenta) {
        Realm realm = database.instance();
        RealmResults<InstalacionProcesoStandBy> procesos = realm.where(InstalacionProcesoStandBy.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .findAll();

        ipstandby = database.copyFromRealm(procesos);
        return ipstandby;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (!Assert.isNull(data)) {
            if(resultCode == 1) {
                try {
                    long[] longaArray = data.getExtras().getLongArray(AlmacenActivity.ITEMS_TRASLADO);
                    Long[] selectedIdsTmp = new Long[longaArray.length];

                    for (int i = 0; i < longaArray.length; i++)
                        selectedIdsTmp[i] = longaArray[i];

                    selectedIds = selectedIdsTmp;
                    ItemsAlmacenAdapter<Almacen> recorrer = activeBtn == 0 ? instalarAdapter : activeBtn == 1 ? retirarAdapter : recursosAdapter;

                    List<Almacen> items = getItemsAlmacen(cuenta);
                    recorrer.addAll(items);

                    isEmptyList();

                } catch (Exception e) {
                    Log.e(TAG, "onResult: ", e);
                }

            } else {
                if (result != null && result.getContents() != null) {
                    String contents = result.getContents();
                    Busqueda.Read read = gson.fromJson(contents, Busqueda.Read.class);

                    Long id = read.getEntityId() != null ? Long.valueOf(read.getEntityId()) : null;
                    String msg = " para el código: " + contents;

                    if (QR_CODE.equals(result.getFormatName())) {
                        if (contents.startsWith("{")) {
                            try {
                                if (read.getEntityCode() == null && activeBtn != 1) {
                                    Snackbar.make(getView(), getString(R.string.message_search_empty) + msg, Snackbar.LENGTH_LONG)
                                            .setDuration(TIME_ALERT)
                                            .show();
                                    return;
                                }

                                if (activeBtn == 1) {
                                    if (read.getVersion() == 1) {
                                        if (read.getEntityCode() != null && read.getEntityName() != null) {
                                            searchEquipoActivo(id, read.getEntityType());
                                            return;
                                        }
                                    }

                                    if (read.getVersion() == 2) {
                                        String codigo = read.getEntityCode();
                                        searchEquipoActivo(codigo, "qrcode");
                                    }
                                }

                                searchEquipoScan(id);
                            } catch (Exception e) {
                                Log.e(TAG, "onActivityResult: ", e);
                                Snackbar.make(getView(), getString(R.string.message_search_empty) + msg, Snackbar.LENGTH_LONG)
                                        .setDuration(TIME_ALERT)
                                        .show();
                            }
                        } else {
                            if (activeBtn == 1) {
                                if (read.getEntityCode() != null && read.getEntityName() != null) {
                                    searchEquipoActivo(id, read.getEntityType());
                                    return;
                                }
                            }

                            searchEquipoScan(id);
                        }
                    } else {
                        if (activeBtn == 1)
                            searchEquipoActivo(result.getContents(), BusquedaActivity.DATA_MATRIX.equals(result.getFormatName()) ? "qrcode" : "barcode");
                        else
                            searchEquipoScan(result.getContents(), BusquedaActivity.DATA_MATRIX.equals(result.getFormatName()) ? "qrcode" : "barcode");
                    }
                } else {
                    Snackbar.make(getView(), getString(R.string.message_search_empty), Snackbar.LENGTH_LONG)
                            .setDuration(TIME_ALERT)
                            .show();
                }
            }
        }

        if(closeBtnactions != null)
            closeBtnactions.dismiss();

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void searchEquipoActivo(Long criterio, String tipo) {
        if(dialogGeneral != null)
            closeBtnactions.dismiss();

        requestEquipoActivo(criterio, tipo);
    }

    private void searchEquipoActivo(String criterio, String tipo) {
        if(closeBtnactions != null)
            closeBtnactions.dismiss();

        requestEquipoActivo(criterio, tipo);
    }

    private void searchEquipoScan(Long code) {
        if(dialogGeneral != null)
            closeBtnactions.dismiss();

        requestEquipoScan(code);
    }

    private void searchEquipoScan(String criterio, String tipo) {
        if(closeBtnactions != null)
            closeBtnactions.dismiss();

        requestEquipoScan(criterio, tipo);
    }

    private void requestJerarquia(Long identidad) {
        progressBar.setVisibility(View.VISIBLE);
        compositeDisposable.add(movimientoService.get(identidad)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNext, this::onErrorJerarquia, this::onCompleteGeneral));
    }

    private void requestEquipoActivo(Long code, String tipo) {
        progressBar.setVisibility(View.VISIBLE);
        compositeDisposable.add(movimientoService.getEquipoActivo(code, tipo)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNextEquipoActivo, this::onErrorEquipoActivo, this::onCompleteGeneral));
    }

    private void requestEquipoActivo(String code, String tipo) {
        progressBar.setVisibility(View.VISIBLE);
        compositeDisposable.add(movimientoService.getEquipoActivo(code, tipo)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNextEquipoActivo, this::onErrorEquipoActivo, this::onCompleteGeneral));
    }

    private void requestEquipoScan(Long code) {
        progressBar.setVisibility(View.VISIBLE);
        compositeDisposable.add(movimientoService.getEquipoScan(code, "qrcode", true)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNextEquipoScan, this::onErrorEquipoActivo, this::onCompleteGeneral));
    }

    private void requestEquipoScan(String code, String tipo) {
        progressBar.setVisibility(View.VISIBLE);
        compositeDisposable.add(movimientoService.getEquipoScan(code, tipo, true)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNextEquipoScan, this::onErrorEquipoActivo, this::onCompleteGeneral));
    }

    private void onNext(Response response) {
        if (!response.isValid())
            return;

        List<Jerarquia> jerarquias = response.getBody(Jerarquia.Request.class).getJerarquia();
        Realm realm = database.instance();
        try {
            realm.executeTransaction(self -> {
                self.where(Jerarquia.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .equalTo("entidadfiltro", ENTIDAD)
                        .findAll()
                        .deleteAllFromRealm();

                for (Jerarquia jerarquia : jerarquias) {
                    jerarquia.setCuenta(cuenta);
                    jerarquia.setEntidadfiltro(ENTIDAD);
                    self.insert(jerarquia);
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "onNext: "+e);
        }
    }

    private void onNextEquipoActivo(Response response) {
        if (!response.isValid())
            return;

        try {

            List<Almacen> item = response.getBody(Almacen.Request.class).getElementos();
            RealmResults<Bodega> bodegasUsuario  = database.where(Bodega.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findAll();

            if(bodegasUsuario.size() == 0){
                Snackbar.make(getView(), R.string.empty_almacenes, Snackbar.LENGTH_LONG)
                        .show();
                return;
            }

            Almacen tmp = item.get(0);

            if (bodegasUsuario.size() == 1) {
                tmp.setBodega(bodegasUsuario.get(0).getNombre());
                tmp.setIdbodega(bodegasUsuario.get(0).getId());
                retirarAdapter.add(tmp);
                isEmptyList();
            } else {

                LayoutInflater factory = LayoutInflater.from(this);
                final View view = factory.inflate(R.layout.dialog_select, null);

                ArrayList bodegasList = new ArrayList();
                for (Bodega bodega : bodegasUsuario)
                    bodegasList.add(bodega.getNombre());

                spinnerView = view.findViewById(R.id.spinnerStore);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, bodegasList);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerView.setAdapter(arrayAdapter);

                dialogGeneral.setPositiveButton(R.string.accept, (dialog, id) -> {
                    String nombre_bodega = (String) spinnerView.getSelectedItem();
                    Bodega bodega = bodegasUsuario.where()
                            .equalTo("nombre", nombre_bodega)
                            .findFirst();

                    tmp.setBodega(bodega.getNombre());
                    tmp.setIdbodega(bodega.getId());
                    retirarAdapter.add(tmp);
                    isEmptyList();
                });
                dialogGeneral.setCancelable(false);
                dialogGeneral.setView(view);
                dialogGeneral.setTitle(R.string.almacen_destino);
                dialogGeneral.show();
            }
        } catch (Exception e) {
            Log.d(TAG, "onNextEquipoActivo: " + e);
            Snackbar.make(getView(), R.string.message_search_empty, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void onNextEquipoScan(Response response) {
        if (!response.isValid())
            return;

        try {
            List<Almacen> item = response.getBody(Almacen.Request.class).getElementos();

            database.executeTransaction(self -> {
                database.where(Almacen.class)
                        .findAll()
                        .deleteAllFromRealm();

                self.insertOrUpdate(item);
            });

            RealmResults<Bodega> bodegasUsuario  = database.where(Bodega.class)
                    .findAll();

            if(bodegasUsuario.size() == 0){
                Snackbar.make(getView(), R.string.empty_almacenes, Snackbar.LENGTH_LONG).show();
                return;
            }

            int position = 0;
            for (int i = 0; i < item.toArray().length; i++) {
                if (item.get(i).getCantidad() > 0){
                    position = i;
                    break;
                }
            }

            Almacen tmp = item.get(position);

            if (bodegasUsuario.size() == 1) {
                if(actionCamera.equals(ACTION3)) {
                    String auxNombre = tmp.getCodigo().concat(" | ").concat(tmp.getNombre());
                    Jerarquia padre = new Jerarquia();
                    padre.setId(tmp.getIdequipo());
                    padre.setEntidadfiltro(tmp.getIdequipo());
                    padre.setNombre(auxNombre);
                    padre.setOrden(0);
                    padre.setTipo("equipo");
                    padre.setCuenta(null);

                    for (int i = 0; i < instalarAdapter.getItemCount(); i++) {
                        Almacen itemList = instalarAdapter.getItemPosition(i);
                        if (this.currentItemInstalar.getId().equals(itemList.getId())) {
                            itemList.setIdbodega(bodegasUsuario.get(0).getId());
                            itemList.setJerarquia(padre);
                        }
                    }
                }
                else {
                    tmp.setIdbodega(bodegasUsuario.get(0).getId());
                    instalarAdapter.add(tmp);
                }
                isEmptyList();
            } else {
                LayoutInflater factory = LayoutInflater.from(this);
                final View view = factory.inflate(R.layout.dialog_select, null);

                ArrayList bodegasList = new ArrayList();
                for (Bodega bodega : bodegasUsuario)
                    bodegasList.add(bodega.getNombre());

                spinnerView = view.findViewById(R.id.spinnerStore);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, bodegasList);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerView.setAdapter(arrayAdapter);

                dialogGeneral.setPositiveButton(R.string.accept, (dialog, id) -> {
                    String nombre_bodega = (String) spinnerView.getSelectedItem();
                    Bodega bodega = bodegasUsuario.where()
                            .equalTo("nombre", nombre_bodega)
                            .findFirst();

                    if(actionCamera.equals(ACTION3)) {
                        String auxNombre = tmp.getCodigo().concat(" | ").concat(tmp.getNombre());
                        Jerarquia padre = new Jerarquia();
                        padre.setId(tmp.getIdequipo());
                        padre.setEntidadfiltro(tmp.getIdequipo());
                        padre.setNombre(auxNombre);
                        padre.setOrden(0);
                        padre.setTipo("equipo");
                        padre.setCuenta(null);

                        for (int i = 0; i < instalarAdapter.getItemCount(); i++) {
                            Almacen itemList = instalarAdapter.getItemPosition(i);
                            if (this.currentItemInstalar.getId().equals(itemList.getId())) {
                                itemList.setIdbodega(bodega.getId());
                                itemList.setJerarquia(padre);
                            }
                        }
                    }
                    else{
                        tmp.setIdbodega(bodega.getId());
                        instalarAdapter.add(tmp);
                    }

                    isEmptyList();
                });
                dialogGeneral.setCancelable(false);
                dialogGeneral.setView(view);
                dialogGeneral.setTitle(R.string.almacen_title);
                dialogGeneral.show();
            }
        } catch (Exception e) {
            Log.d(TAG, "onNextEquipoActivo: " + e);
            Snackbar.make(getView(), R.string.message_search_empty, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void onCompleteGeneral() {
        progressBar.setVisibility(View.GONE);
        Intent intent = new Intent();
        setResult(RESULT_OK_SAVE, intent);
        isEmptyList();
    }

    private void onComplete() {
        dialogHelper.dismissProgressDialog();
        finish();
    }

    private void onErrorJerarquia(Throwable throwable) {
        Log.e(TAG, "onErrorJerarquia: ", throwable);
        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                .setDuration(TIME_ALERT)
                .show();
        onCompleteGeneral();
    }

    private void onErrorEquipoActivo(Throwable throwable) {
        Log.e(TAG, "onErrorEquipoActivo: ", throwable);
        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                .setDuration(TIME_ALERT)
                .show();
        onCompleteGeneral();
    }

    private void onError(Throwable throwable) {
        Log.e(TAG, "onError: ", throwable);
        dialogHelper.dismissProgressDialog();
        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                .setDuration(TIME_ALERT)
                .show();
        onCompleteGeneral();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent != null) {
            String value = prepareNFCRead(intent, false);
            if (activeBtn == 1)
                searchEquipoActivo(value, "nfc");
            else
                searchEquipoScan(value, "nfc");
        }
    }

    public static int compare(@NonNull Jerarquia a, @NonNull Jerarquia b) {
        if (a.getOrden().equals(b.getOrden())) {
            return 0;
        }
        return a.getOrden() > b.getOrden() ? 1 : -1;
    }
}
