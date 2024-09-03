package com.mantum.cmms.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Diligenciar;
import com.mantum.cmms.entity.Actividad;
import com.mantum.cmms.entity.BitacoraContinua;
import com.mantum.cmms.entity.Entidad;
import com.mantum.cmms.entity.LCxOT;
import com.mantum.cmms.entity.ListaChequeo;
import com.mantum.cmms.entity.Personal;
import com.mantum.cmms.entity.UltimoRegistroBitacora;
import com.mantum.cmms.entity.parameter.LogBook;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.cmms.fragment.EntidadesListaFragment;
import com.mantum.cmms.fragment.RutaTrabajoDiligenciarFragment;
import com.mantum.cmms.helper.RecursoHelper;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.RutaTrabajo;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.helper.TransaccionHelper;
import com.mantum.cmms.service.BitacoraContinuaService;
import com.mantum.cmms.service.CaptureGeolocationService;
import com.mantum.cmms.service.ListaChequeoService;
import com.mantum.cmms.service.RutaTrabajoService;
import com.mantum.cmms.service.TransaccionService;
import com.mantum.cmms.util.BackEditTransaction;
import com.mantum.cmms.view.EntidadView;
import com.mantum.cmms.view.RutaTrabajoView;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.InformationAdapter;
import com.mantum.component.adapter.TabAdapter;
import com.mantum.component.service.Photo;
import com.mantum.component.service.PhotoAdapter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.internal.functions.Functions;
import io.realm.RealmList;

import static com.mantum.cmms.entity.parameter.UserPermission.BITACORA_GPS_SOLICITE;
import static com.mantum.cmms.entity.parameter.UserPermission.VALIDAR_REGISTRO_RUTA_TRABAJO;
import static com.mantum.cmms.service.BitacoraContinuaService.Tipo.RUTA_TRABAJO;

public class DiligenciarRutaTrabajoActivity extends TransaccionHelper.Dialog implements OnCompleteListener {

    private static final String TAG = DiligenciarRutaTrabajoActivity.class.getSimpleName();

    public static final int REQUEST_ACTION = 1206;

    public static final String ID_EXTRA = "id_extra";
    public static final String ID_EJECUCION = "id_ejecucion";
    public static final String UUID_TRANSACCION = "UUID";
    public static final String MODE_EDIT = "edit";
    public static final String ACCION_PARCIAL = "accion_parcial";
    public static final String ACCION_LINEA = "accion_linea";
    public static final String TIPO = "tipo";
    public static final String LC = "Listas de Chequeo";

    // Esta constante se usa para cargar el nuevo módulo de lista de chequeo
    // es que se carga desde el menú
    public static final String ES_MODO_NEW_LISTA_CHEQUEO = "Nuevo - Lista de chequeo";

    private long idot;
    private Database database;
    private TabAdapter tabAdapter;
    private RutaTrabajo rutaTrabajo;
    private ListaChequeo listaChequeo;
    private BitacoraContinuaService bitacoraContinuaService;
    private TransaccionService transaccionService;
    private RutaTrabajoDiligenciarFragment rutaTrabajoDiligenciarFragment;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    public InformationAdapter<RutaTrabajoView, EntidadView> lcAdapter;
    private AlertDialog.Builder popupLC;
    private AlertDialog closeDialog;
    private RutaTrabajoService rutaTrabajoService;
    private String tipoRuta;
    private Bundle bundle;
    private boolean sinEjecucion = false;
    private CaptureGeolocationService captureGeolocationService;
    private Location location;
    private ListaChequeoService listaChequeoService;

    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_tab_adapter);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            includeBackButtonAndTitle(R.string.diligenciar_ruta);

            database = new Database(this);
            transaccionService = new TransaccionService(this);
            bitacoraContinuaService = new BitacoraContinuaService(this, RUTA_TRABAJO);
            captureGeolocationService = new CaptureGeolocationService(this);

            lcAdapter = new InformationAdapter<>(this);
            lcAdapter.setDrawable(value -> R.drawable.ruta_trabajo_search);
            popupLC = new AlertDialog.Builder(this);

            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            bundle = getIntent().getExtras();
            if (bundle == null) {
                throw new Exception(getString(R.string.detail_error_ruta));
            }

            tipoRuta = bundle.getString(TIPO, ""); // TODO: Cuidado al mover el tipo de la ruta

            rutaTrabajoService = new RutaTrabajoService(this, cuenta);
            listaChequeoService = new ListaChequeoService(this, cuenta);

            boolean isTurnosManuales = false;
            boolean newModeCheckList = bundle.getBoolean(ES_MODO_NEW_LISTA_CHEQUEO, false);

            if (!newModeCheckList) {
                String uuid = bundle.getString(UUID_TRANSACCION, null);
                String diligenciarJson = bundle.getString(MODE_EDIT, null);
                if (uuid != null) {
                    // Es necesario para validar rutas de trabajo que tienen infomarción incompleta
                    Diligenciar diligenciar = gson.fromJson(diligenciarJson, Diligenciar.class);
                    if (diligenciar.getEntidades().isEmpty()) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                        alertDialogBuilder.setCancelable(true);
                        alertDialogBuilder.setMessage(R.string.editar_ruta_trabajo);
                        alertDialogBuilder.setNegativeButton(getString(R.string.aceptar), (dialog, id) -> super.backActivity());
                        alertDialogBuilder.setCancelable(false);
                        alertDialogBuilder.show();
                        return;
                    }
                }

                idot = bundle.getLong(ID_EXTRA, -1);
                Long idEjecucion = bundle.getLong(ID_EJECUCION) != 0 ? bundle.getLong(ID_EJECUCION) : null;

                rutaTrabajo = database.where(RutaTrabajo.class)
                        .equalTo("UUID", bundle.getString(Mantum.KEY_UUID))
                        .equalTo("id", bundle.getLong(Mantum.KEY_ID))
                        .equalTo("idejecucion", idEjecucion)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();

                boolean esLinea = bundle.getBoolean(ACCION_LINEA, false);

                if (rutaTrabajo != null && rutaTrabajo.isMultiple() && !esLinea) {
                    List<LCxOT> lineas = database.where(LCxOT.class)
                            .equalTo("idot", idot)
                            .equalTo("idgrupoam", rutaTrabajo.getId())
                            .sort("idrt")
                            .findAll();

                    if (!lineas.isEmpty()) {
                        List<RutaTrabajo> lineasRT = new ArrayList<>();
                        int cont = 1;
                        for (int i = 1; i <= lineas.size(); i++) {
                            RutaTrabajo linea = database.copyFromRealm(rutaTrabajo);
                            long generatedLong = 2 + (long) (Math.random() * (1000 - 10));
                            linea.setId(generatedLong);
                            linea.setIdejecucion(generatedLong);
                            linea.setCodigo(linea.getCodigo() + " - Línea " + cont);
                            lineasRT.add(linea);
                            cont++;
                        }
                        lcAdapter.addAll(RutaTrabajoView.factory(lineasRT), true);
                        showPopupLC();
                    }

                }

                bundle.putBoolean(ACCION_LINEA, false);

                if (rutaTrabajo == null && diligenciarJson != null && !diligenciarJson.isEmpty()) {
                    Diligenciar diligenciar = gson.fromJson(diligenciarJson, Diligenciar.class);
                    idot = diligenciar.getIdot();

                    rutaTrabajo = new RutaTrabajo();
                    rutaTrabajo.setId(diligenciar.getIdrt());
                    rutaTrabajo.setIdejecucion(diligenciar.getIdejecucion());
                    rutaTrabajo.setCodigo(diligenciar.getCode());
                    rutaTrabajo.setFecha(diligenciar.getDate());

                    RealmList<Entidad> entidades = new RealmList<>();
                    entidades.addAll(diligenciar.getEntidades());
                    rutaTrabajo.setEntidades(entidades);

                    tipoRuta = diligenciar.getTipo(); // TODO: Indica el tipo de la ruta al editar
                }

                if (rutaTrabajo == null) {
                    throw new Exception(getString(R.string.error_detail_ruta));
                }

                LogBook logBook = database.where(LogBook.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();

                if (logBook != null) {
                    isTurnosManuales = logBook.isTurnosmanualesbitacora();
                }
            } else {
                listaChequeo = listaChequeoService.getById(bundle.getLong(Mantum.KEY_ID));
            }

            rutaTrabajoDiligenciarFragment = new RutaTrabajoDiligenciarFragment();
            rutaTrabajoDiligenciarFragment.setTurnosManuales(isTurnosManuales);
            rutaTrabajoDiligenciarFragment.setEntity(bundle.getLong(Mantum.KEY_ID));
            rutaTrabajoDiligenciarFragment.setEsNuevaListaChequeo(false);

            if (rutaTrabajo != null) {
                if (rutaTrabajo.getTipogrupo() != null && rutaTrabajo.getTipogrupo().equals(LC)) {
                    rutaTrabajoDiligenciarFragment.listaChequeo();
                    rutaTrabajoDiligenciarFragment.setEsNuevaListaChequeo(false);
                }
            } else if (listaChequeo != null) {
                rutaTrabajoDiligenciarFragment.ocultarMenu();
                rutaTrabajoDiligenciarFragment.setEsNuevaListaChequeo(true);
            }

            boolean parcial = bundle.getBoolean(ACCION_PARCIAL, false);
            if (parcial) {
                rutaTrabajoDiligenciarFragment.setParcial();
            }

            if (!newModeCheckList) {
                UltimoRegistroBitacora ultimoRegistroBitacora = database.where(UltimoRegistroBitacora.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .equalTo("fecha", com.mantum.cmms.util.Date.date())
                        .findFirst();

                if (ultimoRegistroBitacora != null) {
                    String hf = new SimpleDateFormat("HH:mm", Locale.getDefault())
                            .format(new Date(ultimoRegistroBitacora.getHorafinal()));
                    rutaTrabajoDiligenciarFragment.setHoraInicioDesdeBitacora(hf);
                }
            }

            tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                    Arrays.asList(rutaTrabajoDiligenciarFragment, new EntidadesListaFragment()));

            ViewPager viewPager = findViewById(R.id.viewPager);
            viewPager.setAdapter(tabAdapter);
            viewPager.setOffscreenPageLimit(tabAdapter.getCount() - 1);

            TabLayout tabLayout = findViewById(R.id.tabs);
            tabLayout.setTabMode(TabLayout.MODE_FIXED);
            tabLayout.setupWithViewPager(viewPager);

            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

            if (rutaTrabajo != null) {
                if (esRegistroFuturo(rutaTrabajo.getFecha())) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setMessage(R.string.ruta_trabajo_registro_futuro);
                    alertDialogBuilder.setNegativeButton(getString(R.string.aceptar), (dialog, id) -> super.backActivity());
                    alertDialogBuilder.show();
                    return;
                }

                if (UserPermission.check(this, VALIDAR_REGISTRO_RUTA_TRABAJO, false)) {
                    rutaTrabajoDiligenciarFragment.bloquearTiempos();
                    rutaTrabajoDiligenciarFragment.incluirValidacionQR();
                    if (esRegistroPasado(rutaTrabajo.getFecha())) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                        alertDialogBuilder.setCancelable(false);
                        alertDialogBuilder.setMessage(R.string.ruta_trabajo_registro_pasado_diligenciable);
                        alertDialogBuilder.setNegativeButton(getString(R.string.aceptar), (dialog, id) -> dialog.dismiss());
                        alertDialogBuilder.setPositiveButton(getString(R.string.cancelar), (dialog, id) -> super.backActivity());
                        alertDialogBuilder.show();
                        return;
                    }

                    if (rutaTrabajo.getIdejecucion() != null && bitacoraContinuaService.pendientes(rutaTrabajo.getIdejecucion())) {
                        BitacoraContinua bitacoraContinua = bitacoraContinuaService.obtenerPendiente(rutaTrabajo.getIdejecucion());

                        String codigo = "";
                        String fecha = "";
                        if (bitacoraContinua != null) {
                            codigo = bitacoraContinua.getCodigo();
                            fecha = bitacoraContinua.getFecharegistro();
                        }

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                        alertDialogBuilder.setCancelable(false);
                        alertDialogBuilder.setMessage(String.format(getString(R.string.ruta_trabajo_pendiente), codigo, fecha));
                        alertDialogBuilder.setNegativeButton(R.string.si, (dialog, id) -> {
                            if (bitacoraContinuaService.eliminar()) {
                                Snackbar.make(getView(), R.string.ruta_trabajo_error_eliminar, Snackbar.LENGTH_LONG)
                                        .show();
                                return;
                            }
                            super.backActivity();
                        });
                        alertDialogBuilder.setPositiveButton(R.string.no, (dialog, id) -> super.backActivity());
                        alertDialogBuilder.show();
                        return;
                    }

                    if (bitacoraContinuaService.pendientes(BitacoraContinuaService.Tipo.OT) && !rutaTrabajo.getTipogrupo().equals(LC)) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                        alertDialogBuilder.setCancelable(false);
                        alertDialogBuilder.setMessage(R.string.ruta_trabajo_ot_pendiente);
                        alertDialogBuilder.setNegativeButton(R.string.aceptar, (dialog, id) -> super.backActivity());
                        alertDialogBuilder.show();
                        return;
                    }

                    if (rutaTrabajo.getIdejecucion() != null && !bitacoraContinuaService.existe(rutaTrabajo.getIdejecucion())) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                        alertDialogBuilder.setCancelable(true);
                        alertDialogBuilder.setMessage(R.string.ruta_trabajo_inicio_registro);
                        alertDialogBuilder.setNegativeButton(getString(R.string.aceptar), (dialog, id) -> dialog.cancel());
                        alertDialogBuilder.show();

                        BitacoraContinua.Data data = new BitacoraContinua.Data();
                        data.setCodigo(rutaTrabajo.getCodigo());
                        data.setFecharegistro(rutaTrabajo.getFecha());

                        compositeDisposable.add(bitacoraContinuaService.iniciar(rutaTrabajo.getIdejecucion(), data)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(Functions.emptyConsumer(), com.mantum.component.Mantum::ignoreError, Functions.EMPTY_ACTION));
                    }
                }
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            backActivity(e.getMessage());
        }
    }

    public boolean esRegistroFuturo(@NonNull String fecha) {
        try {
            Calendar now = Calendar.getInstance(TimeZone.getDefault());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            return simpleDateFormat.parse(fecha).after(now.getTime());
        } catch (Exception e) {
            return true;
        }
    }

    private boolean esRegistroPasado(@NonNull String fecha) {
        try {
            fecha = fecha + " 23:59:59";
            Calendar now = Calendar.getInstance(TimeZone.getDefault());

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return simpleDateFormat.parse(fecha).before(now.getTime());
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        if (rutaTrabajo != null) {
            if (UserPermission.check(this, VALIDAR_REGISTRO_RUTA_TRABAJO, false)) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setMessage(R.string.ruta_trabajo_cancelar);
                alertDialogBuilder.setNegativeButton(R.string.si, (dialog, id) -> {
                    if (rutaTrabajoDiligenciarFragment != null) {
                        Diligenciar diligenciar = rutaTrabajoDiligenciarFragment.getValue();
                        if (diligenciar != null && rutaTrabajo.getIdejecucion() != null) {
                            bitacoraContinuaService.procesando(rutaTrabajo.getIdejecucion(), diligenciar.toJson());
                        }
                    }
                    super.backActivity();
                });

                alertDialogBuilder.setPositiveButton(R.string.no, (dialog, id) -> {
                    if (rutaTrabajo.getIdejecucion() != null)
                        bitacoraContinuaService.eliminar(rutaTrabajo.getIdejecucion());
                    super.backActivity();
                });
                alertDialogBuilder.show();
                return;
            }
        }
        super.backActivity();
    }

    private void onRefresh(@Nullable Mantum.Fragment fragment) {
        if (fragment == null) {
            return;
        }

        switch (fragment.getKey()) {
            case RutaTrabajoDiligenciarFragment.KEY_TAB:

                Diligenciar diligenciar = null;
                Bundle bundle = getIntent().getExtras();
                if (bundle != null) {
                    String diligenciarJson = bundle.getString(MODE_EDIT, null);
                    if (diligenciarJson != null && !diligenciarJson.isEmpty()) {
                        diligenciar = gson.fromJson(diligenciarJson, Diligenciar.class);
                    }
                }

                if (rutaTrabajo != null) {
                    RutaTrabajo value = rutaTrabajo.isManaged()
                            ? database.copyFromRealm(rutaTrabajo)
                            : rutaTrabajo;

                    String currentTime = String.valueOf(System.currentTimeMillis());
                    String lastFourDigits = currentTime.length() > 4 ? currentTime.substring(currentTime.length() - 4) : currentTime;
                    value.setIdFirma(String.valueOf(value.getId()) + lastFourDigits);

                    if (bitacoraContinuaService != null && value.getIdejecucion() != null) {
                        BitacoraContinua bitacoraContinua = bitacoraContinuaService.obtener(value.getIdejecucion());
                        if (bitacoraContinua != null && bitacoraContinua.getValue() != null) {
                            diligenciar = Diligenciar.fromJson(bitacoraContinua.getValue());
                        }
                    }

                    if (rutaTrabajoDiligenciarFragment != null) {
                        rutaTrabajoDiligenciarFragment.onStart(value, diligenciar);
                    }
                } else if (listaChequeo != null) {
                    ListaChequeo value = listaChequeo.isManaged()
                            ? database.copyFromRealm(listaChequeo)
                            : listaChequeo;

                    if (rutaTrabajoDiligenciarFragment != null) {
                        rutaTrabajoDiligenciarFragment.onStart(value, diligenciar);
                    }
                }
                break;

            case EntidadesListaFragment.KEY_TAB:
                List<Entidad> entidades = new ArrayList<>();
                if (rutaTrabajo != null && !rutaTrabajo.getEntidades().isEmpty()) {
                    entidades = rutaTrabajo.getEntidades().isManaged()
                            ? database.copyFromRealm(rutaTrabajo.getEntidades())
                            : rutaTrabajo.getEntidades();
                } else if (listaChequeo != null && !listaChequeo.getEntidades().isEmpty()) {
                    entidades = listaChequeo.getEntidades().isManaged()
                            ? database.copyFromRealm(listaChequeo.getEntidades())
                            : listaChequeo.getEntidades();
                }

                EntidadesListaFragment entidadesListaFragment = (EntidadesListaFragment) fragment;
                entidadesListaFragment.onLoad(entidades);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_formulario, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                Bundle bundle = getIntent().getExtras();
                if (bundle != null && bundle.getString(MODE_EDIT) != null) {
                    BackEditTransaction.backDialog(this);
                } else {
                    onBackPressed();
                }
                break;

            case R.id.action_done:
                register();
                break;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }

        Bundle bundle = data.getExtras();
        if (resultCode == RESULT_OK && bundle != null) {
            switch (requestCode) {
                case BridgeActivity.REQUEST_CODE:
                    Long idEntidadSeleccionada = bundle.getLong(
                            BridgeActivity.ID_ENTIDAD_SELECCIONADA, -1);

                    String tipoEntidadSeleccionada = bundle.getString(
                            BridgeActivity.TIPO_ENTIDAD_SELECCIONADA, "NO_SELECCIONADO");

                    Long idEntidadLectura = bundle.getLong(
                            BridgeActivity.LECTURA_ID_ENTIDAD, -2);

                    String tipoEntidadLectura = bundle.getString(
                            BridgeActivity.LECTURA_TIPO_ENTIDAD, "NO_LECTURA");

                    if (!idEntidadSeleccionada.equals(idEntidadLectura) || !tipoEntidadSeleccionada.equals(tipoEntidadLectura)) {
                        Snackbar.make(getView(), R.string.validacion_lectura_qr, Snackbar.LENGTH_LONG)
                                .show();
                        break;
                    }

                    int position = bundle.getInt(
                            BridgeActivity.POSICION_SELECCIONADA, -1);

                    rutaTrabajoDiligenciarFragment.ejecutarActividad(position);

                    break;

                case RecursosActivity.REQUEST_ACTION:
                    RecursoHelper resources
                            = (RecursoHelper) bundle.getSerializable(RecursosActivity.RECURSO);
                    rutaTrabajoDiligenciarFragment.addResoures(resources);
                    break;

                case FirmaActivity.REQUEST_ACTION:
                    String path = bundle.getString(FirmaActivity.PATH_FILE);
                    rutaTrabajoDiligenciarFragment.addPhoto(path);
                    break;

                case GaleriaActivity.REQUEST_ACTION:
                    SparseArray<PhotoAdapter> parcelable = bundle.getSparseParcelableArray(
                            GaleriaActivity.PATH_FILE_PARCELABLE);

                    if (parcelable != null) {
                        rutaTrabajoDiligenciarFragment.clearPhoto();
                        int total = parcelable.size();
                        for (int i = 0; i < total; i++) {
                            PhotoAdapter photoAdapter = parcelable.get(i);
                            rutaTrabajoDiligenciarFragment.addPhoto(new Photo(this,
                                    new File(photoAdapter.getPath()),
                                    photoAdapter.isDefaultImage(), photoAdapter.getIdCategory(),
                                    photoAdapter.getDescription()));
                        }
                    }
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }

        if (transaccionService != null) {
            transaccionService.close();
        }

        if (bitacoraContinuaService != null) {
            bitacoraContinuaService.close();
        }

        if (listaChequeoService != null) {
            listaChequeoService.close();
        }

        compositeDisposable.clear();
    }

    @Override
    public void onComplete(@NonNull String name) {
        onRefresh(tabAdapter.getFragment(name));
    }

    private void register() {
        if (listaChequeo != null) {
            register(false);
            return;
        }

        if (UserPermission.check(this, BITACORA_GPS_SOLICITE)) {
            compositeDisposable.add(captureGeolocationService.obtener(false)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onNext, this::onErrorLocation, () -> register(true)));
        } else {
            register(true);
        }
    }

    private void register(boolean confirm) {
        try {
            if (rutaTrabajoDiligenciarFragment == null) {
                Snackbar.make(getView(), R.string.error_app, Snackbar.LENGTH_LONG)
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

            if (confirm) {
                if (UserPermission.check(this, VALIDAR_REGISTRO_RUTA_TRABAJO, false)) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setMessage(R.string.ruta_trabajo_finalizar_registro);
                    alertDialogBuilder.setNegativeButton(R.string.si, (dialog, id) -> {
                        rutaTrabajoDiligenciarFragment.incluirHoraFinal();
                        register(false);
                    });
                    alertDialogBuilder.setPositiveButton(R.string.no, (dialog, id) -> dialog.cancel());
                    alertDialogBuilder.show();
                    return;
                }
            }

            Diligenciar diligenciar = rutaTrabajoDiligenciarFragment.getValue();
            if (diligenciar == null) {
                Snackbar.make(getView(), R.string.error_obtener_formulario, Snackbar.LENGTH_LONG)
                        .show();
                return;
            }

            if (diligenciar.getAms().isEmpty()) {
                Snackbar.make(getView(), R.string.diligenciar_vacio, Snackbar.LENGTH_LONG)
                        .show();
                return;
            }

            if (!diligenciar.isListachequeo()) {
                if (diligenciar.getTimestart() == null || diligenciar.getTimestart().isEmpty()) {
                    Snackbar.make(getView(), R.string.diligenciar_hora_inicio_vacio, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                if (diligenciar.getTimeend() == null || diligenciar.getTimeend().isEmpty()) {
                    Snackbar.make(getView(), R.string.diligenciar_hora_fin_vacio, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }
            }

            // Diligenciar nueva lista de chequeo
            if (diligenciar.isListachequeo()) {
                if (diligenciar.getEntidadesClienteListaChequeos().isEmpty()) {
                    Snackbar.make(getView(), R.string.diligenciar_entidades_vacio, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                if (!diligenciar.isOcultarFechaHoraVigente()) {
                    if (diligenciar.getTimestart() == null || diligenciar.getTimestart().isEmpty()) {
                        Snackbar.make(getView(), R.string.diligenciar_hora_inicio_vacio, Snackbar.LENGTH_LONG)
                                .show();
                        return;
                    }

                    if (diligenciar.getTimeend() == null || diligenciar.getTimeend().isEmpty()) {
                        Snackbar.make(getView(), R.string.diligenciar_hora_fin_vacio, Snackbar.LENGTH_LONG)
                                .show();
                        return;
                    }

                    if (diligenciar.getDateEndVig() == null || diligenciar.getDateEndVig().isEmpty()) {
                        Snackbar.make(getView(), R.string.fecha_fin_vigente_requerido, Snackbar.LENGTH_LONG)
                                .show();
                        return;
                    }
                }
            }

            if (rutaTrabajo != null) {
                rutaTrabajo = rutaTrabajo.isManaged()
                        ? database.copyFromRealm(rutaTrabajo)
                        : rutaTrabajo;

                for (Entidad value : rutaTrabajo.getEntidades()) {
                    value.setCuenta(null);
                    for (Actividad actividad : value.getActividades()) {
                        actividad.setCuenta(null);
                    }
                }

                diligenciar.setIdot(idot);
                diligenciar.setIdrt(rutaTrabajo.getId());
                diligenciar.setEntidades(rutaTrabajo.getEntidades());

                if (!sinEjecucion) {
                    diligenciar.setIdejecucion(rutaTrabajo.getIdejecucion());
                }

                diligenciar.setTipo(tipoRuta);
                if (location != null) {
                    diligenciar.setAltitud(location.getAltitude());
                    diligenciar.setExactitud(location.getAccuracy());
                    diligenciar.setLatitud(location.getLatitude());
                    diligenciar.setLongitud(location.getLongitude());
                }
            }

            if (listaChequeo != null) {
                listaChequeo = listaChequeo.isManaged()
                        ? database.copyFromRealm(listaChequeo)
                        : listaChequeo;

                for (Entidad value : listaChequeo.getEntidades()) {
                    value.setCuenta(null);
                    for (Actividad actividad : value.getActividades()) {
                        actividad.setCuenta(null);
                    }
                }

                diligenciar.setEntidades(listaChequeo.getEntidades());
            }

            String uuid = bundle != null
                    ? bundle.getString(UUID_TRANSACCION, UUID.randomUUID().toString())
                    : UUID.randomUUID().toString();

            Transaccion transaccion = new Transaccion();
            transaccion.setUUID(uuid);
            transaccion.setCuenta(cuenta);
            transaccion.setCreation(Calendar.getInstance().getTime());
            transaccion.setVersion(cuenta.getServidor().getVersion());
            transaccion.setValue(diligenciar.toJson());
            transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);

            if (listaChequeo != null) {
                transaccion.setUrl(cuenta.getServidor().getUrl() + "/restapp/app/savelogbooklc");
                transaccion.setModulo(Transaccion.MODULO_LISTA_CHEQUEO);
                transaccion.setAccion(Transaccion.ACCION_REGISTRAR_LISTA_CHEQUEO);

                compositeDisposable.add(transaccionService.save(transaccion)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(Functions.emptyConsumer(), this::onError, this::onComplete));
            } else {

                String endpoint = diligenciar.getGrupos().isEmpty()
                        ? "/restapp/app/savelogbookrt"
                        : "/restapp/app/savelogbookrtgroup";

                transaccion.setUrl(cuenta.getServidor().getUrl() + endpoint);
                transaccion.setModulo(Transaccion.MODULO_RUTA_TRABAJO);
                transaccion.setAccion(Transaccion.ACCION_DILIGENCIAR_RUTA_TRABAJO);

                compositeDisposable.add(transaccionService.save(transaccion)
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap(this::onFinish)
                        .subscribe(Functions.emptyConsumer(), this::onError, this::onComplete));
            }

            showProgressDialog();
        } catch (Exception e) {
            Log.e(TAG, "register: ", e);
            onError(e);
        }
    }

    private void onNext(Location location) {
        this.location = location;
    }

    private void onError(Throwable throwable) {
        dismissProgressDialog();

        int message = listaChequeo != null
                ? R.string.diligenciar_lista_chequeo_error
                : R.string.diligenciar_error;

        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                .show();
    }

    private void onErrorLocation(Throwable throwable) {
        Snackbar.make(getView(), R.string.diligenciar_error_obtener_ubicacion, Snackbar.LENGTH_LONG)
                .show();
    }

    private void onComplete() {
        if (rutaTrabajo != null && rutaTrabajo.isMultiple()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setMessage(R.string.ruta_trabajo_nueva_linea);
            alertDialogBuilder.setNegativeButton(R.string.si, (dialog, id) -> {
                bundle.putBoolean(ACCION_LINEA, true);
                Intent intent = new Intent(getView().getContext(), DiligenciarRutaTrabajoActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
                dialog.dismiss();
                finish();
            });
            alertDialogBuilder.setPositiveButton(R.string.no, (dialog, id) -> {
                dialog.dismiss();
                backActivity(getString(R.string.diligenciar_exitoso));
            });
            alertDialogBuilder.show();
            return;
        }

        int message = listaChequeo != null
                ? R.string.diligenciar_lista_chequeo_exitoso
                : R.string.diligenciar_exitoso;

        dismissProgressDialog();
        backActivity(getString(message));
    }

    private Observable<List<RutaTrabajo>> onFinish(List<Transaccion> transaccions) {
        if (rutaTrabajo != null) {
            if (rutaTrabajo.getIdejecucion() == null || (rutaTrabajo.getTipogrupo() != null && rutaTrabajo.getTipogrupo().equals(LC))) {
                return Observable.empty();
            }

            if (UserPermission.check(this, VALIDAR_REGISTRO_RUTA_TRABAJO, false)) {
                for (Transaccion transaccion : transaccions) {
                    bitacoraContinuaService.terminar(rutaTrabajo.getIdejecucion(), transaccion.getValue());
                }
            }

            return rutaTrabajoService.remove(rutaTrabajo.getId(), rutaTrabajo.getIdejecucion());
        }

        return Observable.empty();
    }

    private void showPopupLC() {
        try {
            lcAdapter.showMessageEmpty(getView(), R.string.pendiente_ruta_trabajo, R.drawable.ruta);
            lcAdapter.setOnAction(new OnSelected<RutaTrabajoView>() {
                @Override
                public void onClick(RutaTrabajoView value, int position) {
                }

                @Override
                public boolean onLongClick(RutaTrabajoView value, int position) {
                    return false;
                }
            });

            LayoutInflater factory = LayoutInflater.from(this);
            View vistaLC = factory.inflate(R.layout.dialog_entidades, null);
            vistaLC.findViewById(R.id.container_filter).setVisibility(View.GONE);

            RecyclerView recyclerView = vistaLC.findViewById(R.id.listaEntidades);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemViewCacheSize(10);
            recyclerView.setDrawingCacheEnabled(true);
            recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(lcAdapter);

            if (lcAdapter.getItemCount() > 0)
                vistaLC.findViewById(R.id.container_actions).setVisibility(View.VISIBLE);

            vistaLC.findViewById(R.id.btn_new_lc).setOnClickListener(v -> {
                closeDialog.dismiss();
                sinEjecucion = true;
            });
            vistaLC.findViewById(R.id.btn_close_lc).setOnClickListener(v -> {
                closeDialog.dismiss();
                finish();
            });

            popupLC.setView(vistaLC);
            popupLC.setTitle(R.string.lista_chequeo_lineas);
            popupLC.setCancelable(false);
            closeDialog = popupLC.show();
        } catch (Exception e) {
            Log.d(TAG, "showPopupLC: " + e);
        }
    }
}