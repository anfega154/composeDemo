package com.mantum.cmms.activity;

import static com.mantum.cmms.entity.parameter.UserPermission.BITACORA_GPS;
import static com.mantum.cmms.entity.parameter.UserPermission.BITACORA_GPS_SOLICITE;
import static com.mantum.cmms.entity.parameter.UserPermission.FINALIZACION_DIRECTA_OT_APP;
import static com.mantum.cmms.entity.parameter.UserPermission.LAST_KNOWN_LOCATION;
import static com.mantum.cmms.entity.parameter.UserPermission.LISTA_CHEQUEO;
import static com.mantum.cmms.entity.parameter.UserPermission.MODULO_GESTION_SERVICIOS;
import static com.mantum.cmms.entity.parameter.UserPermission.MODULO_PANEL_GESTION_SERVICIO;
import static com.mantum.cmms.entity.parameter.UserPermission.REGISTRAR_PAROS;
import static com.mantum.cmms.entity.parameter.UserPermission.VALIDAR_REGISTRO_BITACORA_OT;
import static com.mantum.cmms.entity.parameter.UserPermission.VISUALIZAR_BOTON_EJECUTAR_TAREAS;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.database.Where;
import com.mantum.cmms.domain.BitacoraEvento;
import com.mantum.cmms.domain.BitacoraOT;
import com.mantum.cmms.domain.BitacoraOrdenTrabajo;
import com.mantum.cmms.domain.BitacoraSolicitudServicio;
import com.mantum.cmms.domain.Coordenada;
import com.mantum.cmms.domain.Spinner;
import com.mantum.cmms.domain.Terminar;
import com.mantum.cmms.entity.Actividad;
import com.mantum.cmms.entity.BitacoraContinua;
import com.mantum.cmms.entity.Busqueda;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Devolucion;
import com.mantum.cmms.entity.Entidad;
import com.mantum.cmms.entity.Equipo;
import com.mantum.cmms.entity.EstadoEquipo;
import com.mantum.cmms.entity.Falla;
import com.mantum.cmms.entity.InstalacionLocativa;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.cmms.entity.Paro;
import com.mantum.cmms.entity.PendienteMantenimiento;
import com.mantum.cmms.entity.Personal;
import com.mantum.cmms.entity.Recorrido;
import com.mantum.cmms.entity.SolicitudServicio;
import com.mantum.cmms.entity.Tarea;
import com.mantum.cmms.entity.TipoTiempo;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.entity.UltimoRegistroBitacora;
import com.mantum.cmms.entity.parameter.EventType;
import com.mantum.cmms.entity.parameter.LogBook;
import com.mantum.cmms.entity.parameter.UserParameter;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.cmms.helper.RecursoHelper;
import com.mantum.cmms.helper.TransaccionHelper;
import com.mantum.cmms.service.ATNotificationService;
import com.mantum.cmms.service.BitacoraContinuaService;
import com.mantum.cmms.service.RecorridoService;
import com.mantum.cmms.service.TransaccionService;
import com.mantum.cmms.util.BackEditTransaction;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.adapter.SpinnerAdapter;
import com.mantum.component.component.DatePicker;
import com.mantum.component.component.Progress;
import com.mantum.component.component.TimePicker;
import com.mantum.component.service.Geolocation;
import com.mantum.component.service.Notification;
import com.mantum.component.service.Photo;
import com.mantum.component.service.PhotoAdapter;
import com.mantum.component.service.handler.OnLocationListener;
import com.mantum.core.util.Assert;

import java.io.File;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.internal.functions.Functions;
import io.realm.RealmResults;
import io.realm.Sort;

@Deprecated
public class BitacoraActivity extends TransaccionHelper.Dialog {

    public static final int REQUEST_ACTION = 1203;

    private final static String TAG = BitacoraActivity.class.getSimpleName();

    private final static int COUNT_DOWN_INTERVAL = 1000;

    public static final String UUID_TRANSACCION = "UUID";

    public static final String MODE_EDIT = "edit";

    public static final String KEY_ID = "id";

    public static final String KEY_TIPO_ENTIDAD = "tipoentidad";

    public static final String KEY_CODIGO = "codigo";

    public static final String KEY_TIPO_BITACORA = "tipo";

    public static final String MODO_RECORRIDO = "modo_recorrido";

    public static final String HORA_FIN = "HORA_FIN";

    public static final int SS = 0;

    public static final int EVENT = 1;

    public static final int OT = 2;

    public static final int OT_BITACORA = 3;

    private String UUIDTransaccion;

    private AppCompatSpinner activity;

    private Long id;

    private int type;

    private List<Photo> photos;

    private DatePicker date;

    private TimePicker time;

    private TimePicker end;

    private String tipoentidad;

    private String codigoentidad;

    private RecursoHelper recursos;

    private RecursoHelper mochilas;

    private String account;

    private Database database;

    private Geolocation geolocation;

    private Progress progress;

    private Location location;

    private CountDownTimer countDownTimer;

    private PendienteMantenimiento.Request pendienteMantenimiento;

    private Falla.Request falla;

    private List<Paro.ParoHelper> paros;

    private String tareas;

    private long idEquipoParo;

    private String idAmParo;

    private String idAmTarea;

    private AlertDialog dialog1;

    private String variables;

    private String tipoparo;

    private String estadoEquipo;

    private RecorridoService recorridoService;

    private BitacoraContinuaService bitacoraContinuaService;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private ATNotificationService atNotificationService;

    private boolean verificada;

    private boolean modoRecorrido;

    private boolean disponibleAux = false;

    private SparseArray<Devolucion> devoluciones;

    private Long horafinal;

    private final String SIN_ENTIDAD = "SIN_ENTIDAD";

    private FloatingActionButton floatingActionAsignarParos;

    private FloatingActionButton floatingActionEjecutarTareas;

    private List<Personal> grupos = new ArrayList<>();

    private final ActivityResultLauncher<Intent> personActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data == null) {
                        return;
                    }

                    Bundle bundle = data.getExtras();
                    if (bundle == null) {
                        return;
                    }

                    SparseArray<Parcelable> entities = bundle.getSparseParcelableArray(
                            ListaPersonalActivity.KEY_ENTITY);

                    grupos.clear();
                    if (entities != null) {
                        for (int i = 0; i < entities.size(); i++) {
                            Personal person = (Personal) entities.get(i);
                            grupos.add(person);
                        }
                    }
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_bitacora);

            TextInputEditText eDescription = findViewById(R.id.description);
            TextInputEditText eNotas = findViewById(R.id.nota);
            TextInputEditText eObservacionActivos = findViewById(R.id.observacion_activos);

            ScrollView scrollView = findViewById(R.id.scrollview_bitacora);
            Mantum.applyTouchListener(eDescription, scrollView);
            Mantum.applyTouchListener(eNotas, scrollView);
            Mantum.applyTouchListener(eObservacionActivos, scrollView);

            database = new Database(this);
            includeBackButtonAndTitle(R.string.accion_registrar_bitacora);

            atNotificationService = new ATNotificationService(this);

            photos = new ArrayList<>();
            paros = new ArrayList<>();
            progress = new Progress(this);

            boolean validateGPS = (Version.check(this, 7) && (UserPermission.check(this, BITACORA_GPS)
                    || UserPermission.check(this, BITACORA_GPS_SOLICITE)));

            if (validateGPS && (!Geolocation.checkPermission(this) || !Geolocation.isEnabled(this))) {
                AlertDialog.Builder alertDialogBuilder
                        = new AlertDialog.Builder(BitacoraActivity.this);
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setTitle(R.string.ubicacion_titulo);
                alertDialogBuilder.setMessage(R.string.ubicacion_error_acceso);
                alertDialogBuilder.setPositiveButton(R.string.accion_configuration, (dialog, which) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)));
                alertDialogBuilder.setNegativeButton(getString(R.string.cancelar), (dialog, id) -> dialog.cancel());
                alertDialogBuilder.show();
            }

            id = null;
            tipoentidad = null;
            type = BitacoraActivity.EVENT;
            SharedPreferences sharedPreferences
                    = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            account = sharedPreferences.getString(getString(R.string.mantum_account), null);

            FloatingActionButton firma = findViewById(R.id.firma);
            if (UserPermission.check(this, UserPermission.VISUALIZAR_BOTON_FIRMAR_DIGITAL, true)) {
                firma.setVisibility(View.VISIBLE);
                firma.setOnClickListener(v -> startActivity(FirmaActivity.class));
            }

            Calendar horaInicial = null;
            UltimoRegistroBitacora ultimoRegistroBitacora = database.where(UltimoRegistroBitacora.class)
                    .equalTo("cuenta.UUID", account)
                    .equalTo("fecha", com.mantum.cmms.util.Date.date())
                    .findFirst();

            horaInicial = Calendar.getInstance(TimeZone.getDefault());
            if (ultimoRegistroBitacora != null) {
                horaInicial.setTime(new Date(ultimoRegistroBitacora.getHorafinal()));
            }

            Long idam = null;
            String rate = null;
            String state = null;
            Long idLogBook = null;
            Calendar horaFinal = null;
            Float horasHabiles = null;
            Calendar fecha = Calendar.getInstance(TimeZone.getDefault());
            Bundle bundle = getIntent().getExtras();
            if (KEY_ID != null) {
                modoRecorrido = bundle.getBoolean(MODO_RECORRIDO, false);

                if (modoRecorrido) {
                    horafinal = bundle.getLong(HORA_FIN);
                    horaInicial.setTime(new Date(horafinal));
                }

                String JSON = bundle.getString(MODE_EDIT);
                UUIDTransaccion = bundle.getString(UUID_TRANSACCION);
                type = bundle.getInt(KEY_TIPO_BITACORA);
                if (JSON != null) {
                    Gson gson = new Gson();
                    String description;
                    SimpleDateFormat simpleDateFormat;
                    switch (type) {

                        case OT:
                            BitacoraOrdenTrabajo bitacoraOrdenTrabajo
                                    = gson.fromJson(JSON, BitacoraOrdenTrabajo.class);

                            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            fecha.setTime(simpleDateFormat.parse(bitacoraOrdenTrabajo.getDate()));

                            horaInicial = Calendar.getInstance(TimeZone.getDefault());
                            simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            horaInicial.setTime(simpleDateFormat.parse(bitacoraOrdenTrabajo.getTimestart()));

                            horaFinal = Calendar.getInstance(TimeZone.getDefault());
                            simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            horaFinal.setTime(simpleDateFormat.parse(bitacoraOrdenTrabajo.getTimeend()));

                            description = bitacoraOrdenTrabajo.getDescription();
                            horasHabiles = bitacoraOrdenTrabajo.getHorashabilesdia();

                            id = bitacoraOrdenTrabajo.getIdot();
                            idam = bitacoraOrdenTrabajo.getIdam();
                            photos.addAll(bitacoraOrdenTrabajo.getFiles());
                            recursos = RecursoHelper.recursoAdapter(bitacoraOrdenTrabajo.getRecursos());

                            rate = bitacoraOrdenTrabajo.getExecutionrate();
                            mochilas = RecursoHelper.mochilaAdapter(bitacoraOrdenTrabajo.getMochila());

                            variables = bitacoraOrdenTrabajo.getVariables();

                            tipoparo = bitacoraOrdenTrabajo.getTipoparo();
                            estadoEquipo = bitacoraOrdenTrabajo.getEstadoEquipo();

                            grupos = bitacoraOrdenTrabajo.getGroups();

                            if (!Assert.isNull(UUIDTransaccion) && Version.check(this, 7)) {
                                Coordenada coor = bitacoraOrdenTrabajo.getLocation();
                                if (coor != null) {
                                    location = new Location("");
                                    location.setAltitude(coor.getAltitude());
                                    location.setLongitude(coor.getLongitude());
                                    location.setLatitude(coor.getLatitude());
                                    location.setAccuracy(coor.getAccuracy());
                                }
                            }

                            pendienteMantenimiento = bitacoraOrdenTrabajo.getPendientepmtto();

                            if (bitacoraOrdenTrabajo.getFallas() != null && !bitacoraOrdenTrabajo.getFallas().isEmpty()) {
                                falla = bitacoraOrdenTrabajo.getFallas().get(0);
                            }

                            if (bitacoraOrdenTrabajo.getParos() != null && !bitacoraOrdenTrabajo.getParos().isEmpty()) {
                                paros = bitacoraOrdenTrabajo.getParos();
                                if (floatingActionAsignarParos != null)
                                    floatingActionAsignarParos.setTitle(String.format("(%s) %s", paros.size(), getString(R.string.asignar_paros_titulo)));
                            }

                            if (bitacoraOrdenTrabajo.getTareas() != null && !bitacoraOrdenTrabajo.getTareas().isEmpty()) {
                                tareas = bitacoraOrdenTrabajo.getTareas();
                                if (floatingActionEjecutarTareas != null && !tareas.isEmpty()) {
                                    int tareasCount = 0;
                                    Type type = new TypeToken<ArrayList<Tarea.TareaHelper>>() {
                                    }.getType();
                                    List<Tarea.TareaHelper> tareasAux = new Gson().fromJson(tareas, type);

                                    for (Tarea.TareaHelper tareaHelper : tareasAux) {
                                        if (tareaHelper.isEjecutada()) {
                                            tareasCount++;
                                        }
                                    }

                                    floatingActionEjecutarTareas.setTitle(String.format("(%s de %s) %s", tareasCount, tareasAux.size(), getString(R.string.ejecutar_tareas_titulo)));
                                }
                            }

                            break;

                        case SS:
                            BitacoraSolicitudServicio bitacoraSolicitudServicio
                                    = gson.fromJson(JSON, BitacoraSolicitudServicio.class);

                            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            fecha.setTime(simpleDateFormat.parse(bitacoraSolicitudServicio.getDate()));

                            horaInicial = Calendar.getInstance(TimeZone.getDefault());
                            simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            horaInicial.setTime(simpleDateFormat.parse(bitacoraSolicitudServicio.getTimestart()));

                            horaFinal = Calendar.getInstance(TimeZone.getDefault());
                            simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            horaFinal.setTime(simpleDateFormat.parse(bitacoraSolicitudServicio.getTimeend()));

                            description = bitacoraSolicitudServicio.getDescription();
                            horasHabiles = bitacoraSolicitudServicio.getHorashabilesdia();

                            id = bitacoraSolicitudServicio.getIdss();
                            rate = bitacoraSolicitudServicio.getExecutionrate();
                            photos.addAll(bitacoraSolicitudServicio.getFiles());
                            recursos = RecursoHelper.recursoAdapter(bitacoraSolicitudServicio.getRecursos());

                            if (UUIDTransaccion != null && Version.check(this, 7)) {
                                Coordenada coor = bitacoraSolicitudServicio.getLocation();
                                if (coor != null) {
                                    location = new Location("");
                                    location.setAltitude(coor.getAltitude());
                                    location.setLongitude(coor.getLongitude());
                                    location.setLatitude(coor.getLatitude());
                                    location.setAccuracy(coor.getAccuracy());
                                }
                            }

                            grupos = bitacoraSolicitudServicio.getGroups();
                            pendienteMantenimiento = bitacoraSolicitudServicio.getPendientepmtto();
                            break;

                        case OT_BITACORA:
                            BitacoraOT bitacoraOT = gson.fromJson(JSON, BitacoraOT.class);

                            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            fecha.setTime(simpleDateFormat.parse(bitacoraOT.getDate()));

                            horaInicial = Calendar.getInstance(TimeZone.getDefault());
                            simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            horaInicial.setTime(simpleDateFormat.parse(bitacoraOT.getTimestart()));

                            horaFinal = Calendar.getInstance(TimeZone.getDefault());
                            simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            horaFinal.setTime(simpleDateFormat.parse(bitacoraOT.getTimeend()));

                            description = bitacoraOT.getDescription();
                            horasHabiles = bitacoraOT.getHorashabilesdia();

                            id = bitacoraOT.getIdentity();
                            tipoentidad = bitacoraOT.getTypeentity();

                            state = bitacoraOT.getStateot();
                            rate = bitacoraOT.getExecutionrate();
                            photos.addAll(bitacoraOT.getFiles());
                            recursos = RecursoHelper.recursoAdapter(bitacoraOT.getRecursos());

                            variables = bitacoraOT.getVariables();
                            tipoparo = bitacoraOT.getTipoparo();
                            estadoEquipo = bitacoraOT.getEstadoEquipo();

                            if (!Assert.isNull(UUIDTransaccion) && Version.check(this, 7)) {
                                Coordenada coor = bitacoraOT.getLocation();
                                if (coor != null) {
                                    location = new Location("");
                                    location.setAltitude(coor.getAltitude());
                                    location.setLongitude(coor.getLongitude());
                                    location.setLatitude(coor.getLatitude());
                                    location.setAccuracy(coor.getAccuracy());
                                }
                            }

                            grupos = bitacoraOT.getGroups();
                            pendienteMantenimiento = bitacoraOT.getPendientepmtto();

                            if (bitacoraOT.getParos() != null && !bitacoraOT.getParos().isEmpty()) {
                                paros = bitacoraOT.getParos();
                                if (floatingActionAsignarParos != null)
                                    floatingActionAsignarParos.setTitle(String.format("(%s) %s", paros.size(), getString(R.string.asignar_paros_titulo)));
                            }

                            if (bitacoraOT.getTareas() != null && !bitacoraOT.getTareas().isEmpty()) {
                                tareas = bitacoraOT.getTareas();
                                if (floatingActionEjecutarTareas != null && !tareas.isEmpty()) {
                                    int tareasCount = 0;
                                    Type type = new TypeToken<ArrayList<Tarea.TareaHelper>>() {
                                    }.getType();
                                    List<Tarea.TareaHelper> tareasAux = new Gson().fromJson(tareas, type);

                                    for (Tarea.TareaHelper tareaHelper : tareasAux) {
                                        if (tareaHelper.isEjecutada()) {
                                            tareasCount++;
                                        }
                                    }

                                    floatingActionEjecutarTareas.setTitle(String.format("(%s de %s) %s", tareasCount, tareasAux.size(), getString(R.string.ejecutar_tareas_titulo)));
                                }
                            }

                            break;

                        default:
                            BitacoraEvento bitacoraEvento = gson.fromJson(JSON, BitacoraEvento.class);

                            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            fecha.setTime(simpleDateFormat.parse(bitacoraEvento.getDate()));

                            horaInicial = Calendar.getInstance(TimeZone.getDefault());
                            simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            horaInicial.setTime(simpleDateFormat.parse(bitacoraEvento.getTimestart()));

                            horaFinal = Calendar.getInstance(TimeZone.getDefault());
                            simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            horaFinal.setTime(simpleDateFormat.parse(bitacoraEvento.getTimeend()));

                            description = bitacoraEvento.getDescription();
                            horasHabiles = bitacoraEvento.getHorashabilesdia();

                            id = bitacoraEvento.getIdentity();
                            tipoentidad = bitacoraEvento.getTypeentity();
                            codigoentidad = bitacoraEvento.getEntity();

                            idLogBook = bitacoraEvento.getTypeevent();
                            photos.addAll(bitacoraEvento.getFiles());

                            grupos = bitacoraEvento.getGroups();
                            recursos = RecursoHelper.recursoAdapter(bitacoraEvento.getRecursos());

                            if (!Assert.isNull(UUIDTransaccion) && Version.check(this, 7)) {
                                Coordenada coordenada = bitacoraEvento.getLocation();
                                if (coordenada != null) {
                                    location = new Location("");
                                    location.setAltitude(coordenada.getAltitude());
                                    location.setLongitude(coordenada.getLongitude());
                                    location.setLatitude(coordenada.getLatitude());
                                    location.setAccuracy(coordenada.getAccuracy());
                                }
                            }
                            pendienteMantenimiento = bitacoraEvento.getPendientepmtto();
                    }

                    ((EditText) findViewById(R.id.description)).setText(description);
                }
            }

            List<Spinner> typesTime = new ArrayList<>();
            List<TipoTiempo> tipoTiempos = database.where(TipoTiempo.class)
                    .equalTo("cuenta.UUID", account)
                    .findAll();

            for (TipoTiempo tipoTiempo : tipoTiempos) {
                typesTime.add(new Spinner(String.valueOf(tipoTiempo.getId()), tipoTiempo.getNombre()));
            }

            AppCompatSpinner typeTime = findViewById(R.id.type_time);
            ArrayAdapter<Spinner> adapterTypeTime
                    = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, typesTime);
            typeTime.setAdapter(adapterTypeTime);
            typeTime.setVisibility(adapterTypeTime.getCount() == 0 ? View.GONE : View.VISIBLE);

            this.date = new DatePicker(this, R.id.creation_date);
            this.date.setEnabled(true);
            this.date.load(UUIDTransaccion == null);
            if (fecha != null) {
                this.date.setValue(fecha);
            }

            this.time = new TimePicker(this, R.id.creation_time);
            this.time.setEnabled(true);
            this.time.load(UUIDTransaccion == null);
            if (horaInicial != null) {
                this.time.setValue(horaInicial);
            }

            this.end = new TimePicker(this, R.id.creation_end);
            this.end.setEnabled(true);
            this.end.load();
            if (horaFinal != null) {
                this.end.setValue(horaFinal);
            }

            if ((type == OT && UUIDTransaccion == null) && (UserPermission.check(this, VALIDAR_REGISTRO_BITACORA_OT, false) || UserPermission.check(this, UserPermission.VALIDAR_QR_SITIO, false))) {
                this.date.setEnabled(false);
                this.time.setEnabled(false);
                this.end.setEnabled(false);
            }

            FloatingActionButton pendiente = findViewById(R.id.pendiente);
            if (UserPermission.check(this, UserPermission.VISUALIZAR_BOTON_PENDIENTES, true)) {
                pendiente.setVisibility(View.VISIBLE);
                pendiente.setOnClickListener(v -> {
                    Bundle bundle_2 = new Bundle();
                    bundle_2.putSerializable(PendienteMantenimientoActivity.KEY_FORM, pendienteMantenimiento);

                    startActivity(PendienteMantenimientoActivity.class, bundle_2);
                });
            }

            FloatingActionButton floatingActionButton = findViewById(R.id.camera);
            if (UserPermission.check(this, UserPermission.VISUALIZAR_BOTON_ADJUNTAR_ARCHIVOS, true)) {
                floatingActionButton.setVisibility(View.VISIBLE);
                floatingActionButton.setOnClickListener(v -> {
                    Bundle self = new Bundle();
                    self.putSparseParcelableArray(GaleriaActivity.PATH_FILE_PARCELABLE, PhotoAdapter.factory(photos));

                    startActivity(GaleriaActivity.class, self);
                });
            }

            FloatingActionButton floatingActionButton1 = findViewById(R.id.devolucion);
            floatingActionButton1.setOnClickListener(v -> {
                Bundle self = new Bundle();
                self.putSparseParcelableArray(
                        DevolucionActivity.DEVOLUCIONES_PARCELABLE, devoluciones);

                startActivity(DevolucionActivity.class, self);
            });


            final AppCompatSpinner typeSpinner = findViewById(R.id.type);
            if (bundle != null) {
                TextInputLayout register_container = findViewById(R.id.register_container);
                TextInputEditText register = findViewById(R.id.register);
                register_container.setVisibility(View.VISIBLE);
                register.setText(bundle.getString(KEY_CODIGO));

                id = id == null ? bundle.getLong(KEY_ID) : id;
                tipoentidad = tipoentidad == null ? bundle.getString(KEY_TIPO_ENTIDAD) : tipoentidad;
                type = bundle.getInt(KEY_TIPO_BITACORA);
                if (type == OT) {
                    int indexAM = 0, index = 0;
                    List<Spinner> elements = new ArrayList<>();
                    OrdenTrabajo ordentrabajo = database.where(OrdenTrabajo.class)
                            .equalTo("id", id)
                            .equalTo("cuenta.UUID", account)
                            .findFirst();


                    int repetidas = 0;
                    int indexValida = 0;
                    if (ordentrabajo != null) {
                        register.setText(ordentrabajo.getCodigo());
                        for (Entidad entidad : ordentrabajo.getEntidades()) {
                            for (Actividad actividad : entidad.getActividades()) {
                                Spinner spinner = new Spinner(
                                        String.valueOf(actividad.getId()),
                                        String.format("%s | %s - %s", actividad.getNombre(), entidad.getCodigo(), entidad.getNombre()));

                                spinner.setIdentidad(entidad.getId());
                                spinner.setTipoentidad(entidad.getTipo());
                                elements.add(spinner);

                                if (ordentrabajo.getEntidadValida() != null
                                        && entidad.getId().equals(ordentrabajo.getEntidadValida())) {
                                    repetidas++;
                                    indexValida = index + 1;
                                    verificada = true;
                                }

                                if (idam != null && idam.equals(actividad.getId())) {
                                    indexAM = index + 1;
                                }
                                index = index + 1;
                            }
                        }

                        FloatingActionButton floatingActionGestionarFallas = findViewById(R.id.gestionar_fallas);
                        if (Version.check(this, 17) && UserPermission.check(this, UserPermission.GESTIONAR_FALLAS_BITACORA, false)
                                && ordentrabajo.getFallas() != null && !ordentrabajo.getFallas().isEmpty()) {
                            floatingActionGestionarFallas.setVisibility(View.VISIBLE);
                            floatingActionGestionarFallas.setOnClickListener(view -> {
                                Bundle bundle1 = new Bundle();
                                bundle1.putLong(com.mantum.component.Mantum.KEY_ID, id);

                                if (falla != null) {
                                    bundle1.putSerializable(GestionFallaActivity.KEY_FORM, falla);

                                    if (ordentrabajo.getFallas().size() == 1) {
                                        startActivity(GestionFallaActivity.class, bundle1);
                                    } else {
                                        View dialogGestionFalla = View.inflate(this, R.layout.dialog_gestion_falla, null);
                                        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                                        dialog.setView(dialogGestionFalla);
                                        TextView fallaAccion1 = dialogGestionFalla.findViewById(R.id.falla_accion_1);
                                        TextView fallaAccion2 = dialogGestionFalla.findViewById(R.id.falla_accion_2);
                                        TextView fallaAccion3 = dialogGestionFalla.findViewById(R.id.falla_accion_3);

                                        fallaAccion1.setOnClickListener(view1 -> {
                                            bundle1.putInt("actionClicked", 1);
                                            startActivity(GestionFallaActivity.class, bundle1);
                                            dialog1.dismiss();
                                        });

                                        fallaAccion2.setOnClickListener(view1 -> {
                                            bundle1.putInt("actionClicked", 2);
                                            startActivity(GestionFallaActivity.class, bundle1);
                                            dialog1.dismiss();
                                        });

                                        fallaAccion3.setOnClickListener(view1 -> {
                                            bundle1.putInt("actionClicked", 3);
                                            startActivity(GestionFallaActivity.class, bundle1);
                                            dialog1.dismiss();
                                        });
                                        dialog1 = dialog.show();
                                    }
                                } else {
                                    startActivity(GestionFallaActivity.class, bundle1);
                                }
                            });
                        }
                    }

                    if (!elements.isEmpty()) {
                        elements.add(0, new Spinner(null, getString(R.string.registro_general)));

                        if (repetidas == 1) {
                            indexAM = indexValida;
                        } else if (repetidas > 1) {
                            Spinner spinner = new Spinner(SIN_ENTIDAD, "SELECCIONE LA ENTIDAD");
                            spinner.setIdentidad(null);
                            spinner.setTipoentidad(null);
                            elements.add(0, spinner);
                        }

                        ArrayAdapter<Spinner> adapter = new ArrayAdapter<>(this, R.layout.custom_simple_spinner, R.id.item, elements);

                        LinearLayout activityContainer = findViewById(R.id.activity_container);
                        activityContainer.setVisibility(View.VISIBLE);

                        activity = findViewById(R.id.activity);
                        activity.setAdapter(adapter);
                        activity.setVisibility(View.VISIBLE);
                        activity.setSelection(indexAM);
                    }
                }

                if (type == SS) {
                    SolicitudServicio solicitudServicio = database.where(SolicitudServicio.class)
                            .equalTo("id", id)
                            .equalTo("cuenta.UUID", account)
                            .findFirst();

                    if (solicitudServicio != null) {
                        register.setText(solicitudServicio.getCodigo());
                    }

                    LinearLayout activityContainer = findViewById(R.id.activity_container);
                    activityContainer.setVisibility(View.GONE);
                }

                if (type == OT_BITACORA) {
                    includeBackButtonAndTitle(R.string.registrar_ot_bitacora);
                    boolean pordefecto = false;
                    if ("Equipo".equals(tipoentidad)) {
                        Equipo equipo = database.where(Equipo.class)
                                .equalTo("id", id)
                                .equalTo("cuenta.UUID", account)
                                .findFirst();

                        if (equipo != null) {
                            pordefecto = true;
                            register.setText(equipo.getCodigo());

                            List<Spinner> elements = new ArrayList<>();
                            for (Actividad actividad : equipo.getActividades()) {
                                if (actividad.getTipo().equals("Rutinaria") || actividad.getTipo().equals("Autónoma") || (Version.check(this, 13) && !actividad.isProgramable())) {
                                    continue;
                                }

                                elements.add(new Spinner(actividad.getId().toString(), actividad.getNombre()));
                            }

                            if (elements.isEmpty()) {
                                elements.add(0, new Spinner(null, getString(R.string.reparacion_general)));
                            }

                            LinearLayout activityContainer = findViewById(R.id.activity_container);
                            activityContainer.setVisibility(View.VISIBLE);

                            SpinnerAdapter<Spinner> spinnerSpinnerAdapter
                                    = new SpinnerAdapter<>(this, elements);

                            activity = findViewById(R.id.activity);
                            activity.setAdapter(spinnerSpinnerAdapter.getAdapter());
                            activity.setVisibility(View.VISIBLE);
                        }

                    } else if ("InstalacionLocativa".equals(tipoentidad)) {
                        InstalacionLocativa instalacionLocativa = database.where(InstalacionLocativa.class)
                                .equalTo("id", id)
                                .equalTo("cuenta.UUID", account)
                                .findFirst();

                        if (instalacionLocativa != null) {
                            pordefecto = true;
                            register.setText(instalacionLocativa.getCodigo());

                            List<Spinner> elements = new ArrayList<>();
                            for (Actividad actividad : instalacionLocativa.getActividades()) {
                                if (actividad.isProgramable()) {
                                    elements.add(new Spinner(actividad.getId().toString(), actividad.getNombre()));
                                }
                            }

                            if (elements.isEmpty()) {
                                elements.add(0, new Spinner(null, getString(R.string.reparacion_general)));
                            }

                            LinearLayout activityContainer = findViewById(R.id.activity_container);
                            activityContainer.setVisibility(View.VISIBLE);

                            SpinnerAdapter<Spinner> spinnerSpinnerAdapter
                                    = new SpinnerAdapter<>(this, elements);

                            activity = findViewById(R.id.activity);
                            activity.setAdapter(spinnerSpinnerAdapter.getAdapter());
                            activity.setVisibility(View.VISIBLE);
                        }
                    }

                    if (!pordefecto) {
                        Busqueda busqueda = database.where(Busqueda.class)
                                .equalTo("id", id)
                                .equalTo("type", tipoentidad)
                                .equalTo("cuenta.UUID", account)
                                .findFirst();

                        if (busqueda != null) {
                            register.setText(busqueda.getCode());

                            List<Spinner> elements = new ArrayList<>();
                            for (Actividad actividad : busqueda.getActividades()) {
                                elements.add(new Spinner(actividad.getId().toString(), actividad.getNombre()));
                            }

                            if (elements.isEmpty()) {
                                elements.add(0, new Spinner(null, getString(R.string.reparacion_general)));
                            }

                            LinearLayout activityContainer = findViewById(R.id.activity_container);
                            activityContainer.setVisibility(View.VISIBLE);

                            SpinnerAdapter<Spinner> spinnerSpinnerAdapter
                                    = new SpinnerAdapter<>(this, elements);

                            activity = findViewById(R.id.activity);
                            activity.setAdapter(spinnerSpinnerAdapter.getAdapter());
                            activity.setVisibility(View.VISIBLE);
                        }
                    }
                }

                // Muestra el estado
                AppCompatSpinner tipos = null;
                if (type == OT_BITACORA) {
                    List<String> elements = Arrays.asList(getString(R.string.tipos_abierta), getString(R.string.tipos_cerrada));
                    ArrayAdapter<String> adapter
                            = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, elements);

                    tipos = findViewById(R.id.tipos);
                    tipos.setVisibility(View.VISIBLE);
                    tipos.setAdapter(adapter);
                    register_container.setHint(getString(R.string.bitacora_entidad));
                    if (state != null) {
                        tipos.setSelection(elements.indexOf(state));
                    }
                }

                if (type == EVENT) {
                    if (id != null && id != 0) {
                        register_container.setHint(getString(R.string.bitacora_entidad));
                        if (codigoentidad != null) {
                            register.setText(codigoentidad);
                        }
                    } else {
                        register_container.setVisibility(View.GONE);

                        TextView information = findViewById(R.id.information);
                        information.setVisibility(View.VISIBLE);
                    }

                    typeSpinner.setVisibility(View.VISIBLE);
                    LogBook logBook = database.where(LogBook.class)
                            .equalTo("cuenta.UUID", account)
                            .findFirst();

                    if (logBook != null) {
                        List<EventType> eventTypes = new ArrayList<>();
                        if (id != null && id != 0) {
                            eventTypes.addAll(logBook.getEventtype());
                        } else {
                            for (EventType eventType : logBook.getEventtype()) {
                                if (!eventType.isRequiereentidad()) {
                                    eventTypes.add(eventType);
                                }
                            }

                            if (eventTypes.isEmpty()) {
                                new android.app.AlertDialog.Builder(this)
                                        .setMessage(getString(R.string.mensaje_registro_bitacora_evento_denegado))
                                        .setCancelable(false)
                                        .setNegativeButton("Cerrar", ((dialog, which) -> backActivity()))
                                        .show();
                            }
                        }

                        typeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, eventTypes));
                        if (idLogBook != null) {
                            int total = eventTypes.size();
                            for (int i = 0; i < total; i++) {
                                EventType eventType = eventTypes.get(i);
                                if (eventType != null && eventType.getId().equals(idLogBook)) {
                                    typeSpinner.setSelection(i);
                                    i = total;
                                }
                            }
                        }
                    }
                }

                // Muestra el campo de ejecución
                final EditText execution = findViewById(R.id.execution);
                if (execution != null && type != EVENT) {
                    execution.setText(rate);
                    TextInputLayout contentExecution = findViewById(R.id.content_execution);
                    contentExecution.setVisibility(View.VISIBLE);
                    AppCompatSpinner fTipos = tipos;
                    execution.setOnFocusChangeListener((v, hasFocus) -> {
                        if (!hasFocus) {
                            try {
                                int value = Integer.parseInt(execution.getText().toString());
                                if (fTipos != null) {
                                    String tipoValor = (String) fTipos.getSelectedItem();
                                    if (tipoValor.equals("Cerrada")) {
                                        value = 100;
                                    }
                                }

                                execution.setText(value >= 100 ? "100" : execution.getText().toString());
                            } catch (Exception e) {
                                execution.setText(null);
                            }
                        }
                    });
                }
            }

            // Inicializa la vista mi mochila
            if (type == OT && Version.check(this, 5) && UserPermission.check(this, UserPermission.MENU_LATERAL_VER_OPCION_MI_MOCHILA, true)) {
                FloatingActionButton bMochila = findViewById(R.id.mochila);
                bMochila.setVisibility(View.VISIBLE);
                bMochila.setOnClickListener(v -> {
                    Bundle bundle_2 = new Bundle();
                    bundle_2.putSerializable(MochilaActivity.RECURSO, mochilas);
                    startActivity(MochilaActivity.class, bundle_2);
                });
            }

            if (type == OT && Version.check(this, 7) && UserPermission.check(this, MODULO_GESTION_SERVICIOS)) {
                TextInputLayout contentNota = findViewById(R.id.content_nota);
                contentNota.setVisibility(View.VISIBLE);

                TextInputLayout contentObservacionAtivos = findViewById(R.id.content_observacionactivos);
                contentObservacionAtivos.setVisibility(View.VISIBLE);
            }

            FloatingActionButton floatingActionRecurso = findViewById(R.id.recursos);
            if (UserPermission.check(this, UserPermission.REALIZAR_MOVIMIENTO_GENERAL, true)) {
                floatingActionRecurso.setVisibility(View.VISIBLE);
                floatingActionRecurso.setOnClickListener(view -> {
                    try {
                        Bundle bundle_1 = new Bundle();
                        if (type == OT && recursos == null) {
                            OrdenTrabajo detalle = database.where(OrdenTrabajo.class)
                                    .equalTo("id", id)
                                    .equalTo("cuenta.UUID", account)
                                    .findFirst();

                            if (detalle != null) {
                                recursos = RecursoHelper.recursoAdapter(detalle.getRecursos());
                            }
                        }

                        bundle_1.putSerializable(RecursosActivity.RECURSO, recursos);
                        startActivity(RecursosActivity.class, bundle_1);
                    } catch (Exception e) {
                        Log.e(TAG, "onCreate: ", e);
                    }
                });
            }

            FloatingActionButton floatingActionPersona = findViewById(R.id.personas);
            if (UserPermission.check(this, UserPermission.AGREGAR_PERSONAL_BITACTORA, false)) {
                floatingActionPersona.setVisibility(View.VISIBLE);
                floatingActionPersona.setOnClickListener(view -> {
                    SparseArray<Personal> results = new SparseArray<>();
                    for (int i = 0; i < grupos.size(); i++) {
                        results.append(i, grupos.get(i));
                    }

                    Bundle bundle69 = new Bundle();
                    bundle69.putSparseParcelableArray(
                            ListaPersonalActivity.KEY_ENTITY, results);

                    Intent intent = new Intent(this, ListaPersonalActivity.class);
                    intent.putExtras(bundle69);

                    personActivityResultLauncher.launch(intent);

                });
            }

            AppCompatSpinner generaParo = findViewById(R.id.paro);
            AppCompatSpinner stateSpinner = findViewById(R.id.state);
            if (type == OT || type == OT_BITACORA) {
                if (UserPermission.check(this, REGISTRAR_PAROS, false)) {
                    if (Version.check(this, 18)) {
                        floatingActionAsignarParos = findViewById(R.id.asignar_paros);

                        if (type == OT || tipoentidad.equals(Equipo.SELF)) {
                            floatingActionAsignarParos.setVisibility(View.VISIBLE);
                        } else {
                            floatingActionAsignarParos.setVisibility(View.GONE);
                        }

                        floatingActionAsignarParos.setOnClickListener(view -> {

                            Long idAm = null;
                            boolean entidadEquipo = false;
                            if (activity != null) {
                                Spinner spinner = (Spinner) activity.getSelectedItem();
                                idAm = spinner.getKey() != null ? Long.valueOf(spinner.getKey()) : null;
                                entidadEquipo = spinner.getTipoentidad() != null && spinner.getTipoentidad().equals(Equipo.SELF);
                            }

                            if (type == OT) {
                                if (idAm == null) {
                                    Snackbar.make(getView(), R.string.seleccionar_actividad, Snackbar.LENGTH_LONG)
                                            .show();
                                    return;
                                }

                                if (!entidadEquipo) {
                                    Snackbar.make(getView(), R.string.seleccionar_entidad_tipo_equipo, Snackbar.LENGTH_LONG)
                                            .show();
                                    return;
                                }
                            }

                            Bundle bundle1 = new Bundle();
                            SparseArray<Paro.ParoHelper> paroSparseArray = new SparseArray<>();
                            for (int i = 0; i < paros.size(); i++) {
                                paroSparseArray.append(i, paros.get(i));
                            }
                            bundle1.putSparseParcelableArray(AsignarParoActivity.KEY_FORM, paroSparseArray);

                            bundle1.putString(AsignarParoActivity.FECHA, date.getValue());
                            bundle1.putString(AsignarParoActivity.HORA_INICIO, time.getValue());
                            bundle1.putString(AsignarParoActivity.HORA_FIN, end.getValue());

                            if (type == OT) {
                                Spinner spinner = (Spinner) activity.getSelectedItem();
                                if (spinner.getIdentidad() != null) {
                                    bundle1.putLong(Mantum.KEY_ID, spinner.getIdentidad());
                                    bundle1.putString(AsignarParoActivity.ID_AM_PARO, spinner.getKey());
                                }
                            } else {
                                bundle1.putLong(Mantum.KEY_ID, id);
                            }
                            startActivity(AsignarParoActivity.class, bundle1);
                        });
                    } else {
                        List<Spinner> paros = new ArrayList<>();
                        paros.add(0, new Spinner(null, getString(R.string.seleccione_tipo_paro)));
                        paros.add(1, new Spinner(getString(R.string.correctivo), getString(R.string.correctivo_hint)));
                        paros.add(2, new Spinner(getString(R.string.preventivo), getString(R.string.preventivo_hint)));

                        SpinnerAdapter<Spinner> spinnerSpinnerAdapter
                                = new SpinnerAdapter<>(this, paros);
                        generaParo.setAdapter(spinnerSpinnerAdapter.getAdapter());

                        if (tipoparo != null && !tipoparo.isEmpty()) {
                            int index = 0;
                            for (Spinner paro : paros) {
                                if (tipoparo.equals(paro.getKey())) {
                                    generaParo.setSelection(index);
                                    break;
                                }
                                index = index + 1;
                            }
                        }

                        RealmResults<EstadoEquipo> estadoequipos = database.where(EstadoEquipo.class).findAll();
                        if (estadoequipos != null) {
                            List<Spinner> estadoEquipos = new ArrayList<>();
                            estadoEquipos.add(0, new Spinner(getString(R.string.estado_equipo), getString(R.string.estado_equipo)));

                            int index = 1;
                            int indexDefault = 0;
                            for (EstadoEquipo estado : estadoequipos) {
                                estadoEquipos.add(index, new Spinner(estado.getEstado(), estado.getEstado()));
                                if (estadoEquipo != null && !estadoEquipo.isEmpty()) {
                                    if (estadoEquipo.equals(estado.getEstado())) {
                                        indexDefault = index;
                                    }
                                }
                                index = index + 1;
                            }

                            SpinnerAdapter<Spinner> stateSpinnerAdapter
                                    = new SpinnerAdapter<>(this, estadoEquipos);
                            stateSpinner.setAdapter(stateSpinnerAdapter.getAdapter());
                            stateSpinner.setSelection(indexDefault);
                        }

                        generaParo.setVisibility(View.VISIBLE);
                        stateSpinner.setVisibility(View.VISIBLE);
                    }
                }

                if (UserPermission.check(BitacoraActivity.this, VISUALIZAR_BOTON_EJECUTAR_TAREAS) && Version.check(this, 19)) {
                    floatingActionEjecutarTareas = findViewById(R.id.ejecutar_tareas);
                    floatingActionEjecutarTareas.setVisibility(View.VISIBLE);
                    floatingActionEjecutarTareas.setOnClickListener(view -> {
                        Long idAm = null;
                        if (activity != null) {
                            Spinner spinner = (Spinner) activity.getSelectedItem();
                            idAm = spinner.getKey() != null ? Long.valueOf(spinner.getKey()) : null;
                        }

                        if (type == OT) {
                            if (idAm == null) {
                                Snackbar.make(getView(), R.string.seleccionar_actividad, Snackbar.LENGTH_LONG)
                                        .show();
                                return;
                            }
                        }

                        if (tareas == null || tareas.isEmpty()) {
                            ArrayList<Tarea.TareaHelper> tareasAux = new ArrayList<>();

                            List<Actividad> actividades = new ArrayList<>();
                            if (type == OT) {
                                OrdenTrabajo ordenTrabajo = database.where(OrdenTrabajo.class)
                                        .equalTo("cuenta.UUID", account)
                                        .equalTo("id", id)
                                        .findFirst();

                                if (ordenTrabajo != null) {
                                    for (Entidad entidad : ordenTrabajo.getEntidades()) {
                                        actividades.addAll(database.copyFromRealm(entidad.getActividades()));
                                    }
                                }
                            } else {
                                if (tipoentidad.equals(Equipo.SELF)) {
                                    Equipo entidad = database.where(Equipo.class)
                                            .equalTo("cuenta.UUID", account)
                                            .equalTo("id", id)
                                            .findFirst();

                                    if (entidad != null) {
                                        actividades.addAll(database.copyFromRealm(entidad.getActividades()));
                                    }
                                } else {
                                    InstalacionLocativa entidad = database.where(InstalacionLocativa.class)
                                            .equalTo("cuenta.UUID", account)
                                            .equalTo("id", id)
                                            .findFirst();

                                    if (entidad != null) {
                                        actividades.addAll(database.copyFromRealm(entidad.getActividades()));
                                    }
                                }
                            }

                            for (Actividad actividad : actividades) {
                                if (actividad.getId().equals(idAm)) {
                                    tareasAux.addAll(Tarea.TareaHelper.factory(actividad.getTareas()));
                                }
                            }

                            if (!tareasAux.isEmpty()) {
                                tareas = new Gson().toJson(tareasAux);
                            }
                        }

                        Bundle bundle1 = new Bundle();
                        if (tareas != null && !tareas.equals("")) {
                            bundle1.putString(EjecutarTareaActivity.KEY_FORM, tareas);
                        }

                        Spinner spinner = (Spinner) activity.getSelectedItem();
                        bundle1.putString(EjecutarTareaActivity.ID_AM_TAREA, spinner.getKey());
                        startActivity(EjecutarTareaActivity.class, bundle1);
                    });
                }

                activity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                        if (UserPermission.check(BitacoraActivity.this, REGISTRAR_PAROS)
                                && (Version.check(BitacoraActivity.this, 18))) {
                            if (paros != null && !paros.isEmpty() && idEquipoParo != 0) {
                                AlertDialog.Builder dialog = new AlertDialog.Builder(BitacoraActivity.this)
                                        .setCancelable(false)
                                        .setMessage(R.string.mensaje_paros_cambio_entidad)
                                        .setNegativeButton(R.string.cancel, ((dialogInterface, pos) -> {
                                            for (int i = 0; i < activity.getCount(); i++) {
                                                Spinner spinner = (Spinner) activity.getAdapter().getItem(i);
                                                if (spinner.getIdentidad() != null && spinner.getIdentidad().equals(idEquipoParo)
                                                        && spinner.getKey() != null && spinner.getKey().equals(idAmParo)) {
                                                    activity.setSelection(i);
                                                }
                                            }
                                            dialogInterface.dismiss();
                                        }))
                                        .setPositiveButton(R.string.continuar, ((dialogInterface, pos) -> {
                                            floatingActionAsignarParos.setTitle(getString(R.string.asignar_paros_titulo));
                                            paros.clear();
                                            dialogInterface.dismiss();
                                        }));

                                if (position == 0) {
                                    dialog.show();
                                    return;
                                }

                                Spinner spinner = (Spinner) activity.getSelectedItem();
                                if (spinner.getIdentidad() != null && !spinner.getIdentidad().equals(idEquipoParo)) {
                                    dialog.show();
                                } else if (spinner.getKey() != null && !spinner.getKey().equals(idAmParo)) {
                                    idAmParo = spinner.getKey();
                                }
                            }
                        }

                        if (UserPermission.check(BitacoraActivity.this, VISUALIZAR_BOTON_EJECUTAR_TAREAS)
                                && (Version.check(BitacoraActivity.this, 19))) {

                            List<Actividad> actividades = new ArrayList<>();
                            if (type == OT) {
                                OrdenTrabajo ordenTrabajo = database.where(OrdenTrabajo.class)
                                        .equalTo("cuenta.UUID", account)
                                        .equalTo("id", id)
                                        .findFirst();

                                if (ordenTrabajo != null) {
                                    for (Entidad entidad : ordenTrabajo.getEntidades()) {
                                        actividades.addAll(database.copyFromRealm(entidad.getActividades()));
                                    }
                                }
                            } else {
                                if (tipoentidad.equals(Equipo.SELF)) {
                                    Equipo entidad = database.where(Equipo.class)
                                            .equalTo("cuenta.UUID", account)
                                            .equalTo("id", id)
                                            .findFirst();

                                    if (entidad != null) {
                                        actividades.addAll(database.copyFromRealm(entidad.getActividades()));
                                    }
                                } else {
                                    InstalacionLocativa entidad = database.where(InstalacionLocativa.class)
                                            .equalTo("cuenta.UUID", account)
                                            .equalTo("id", id)
                                            .findFirst();

                                    if (entidad != null) {
                                        actividades.addAll(database.copyFromRealm(entidad.getActividades()));
                                    }
                                }
                            }

                            int oldTareasCount = 0;
                            if (tareas != null && !tareas.equals("")) {
                                for (Actividad actividad : actividades) {
                                    if (idAmTarea != null && actividad.getId().equals(Long.valueOf(idAmTarea))) {
                                        for (Tarea tarea : actividad.getTareas()) {
                                            if (tarea.isEjecutada()) {
                                                oldTareasCount++;
                                            }
                                        }
                                    }
                                }

                                int newTareasCount = 0;
                                Type typeTarea = new TypeToken<ArrayList<Tarea.TareaHelper>>() {
                                }.getType();
                                List<Tarea.TareaHelper> tareasAux = new Gson().fromJson(tareas, typeTarea);

                                for (Tarea.TareaHelper tareaHelper : tareasAux) {
                                    if (tareaHelper.isEjecutada()) {
                                        newTareasCount++;
                                    }
                                }

                                if (oldTareasCount > 0 && newTareasCount != oldTareasCount) {
                                    int tareasCount = newTareasCount;
                                    AlertDialog.Builder dialog2 = new AlertDialog.Builder(BitacoraActivity.this)
                                            .setCancelable(false)
                                            .setMessage("Tienes tareas ejecutadas, si cambias de actividad perderás los cambios.")
                                            .setNegativeButton(R.string.cancel, ((dialogInterface, pos) -> {
                                                for (int i = 0; i < activity.getCount(); i++) {
                                                    Spinner spinner = (Spinner) activity.getAdapter().getItem(i);
                                                    if (spinner.getKey() != null && spinner.getKey().equals(idAmTarea)) {
                                                        activity.setSelection(i);
                                                    }
                                                }
                                                dialogInterface.dismiss();

                                                floatingActionEjecutarTareas.setTitle(String.format("(%s de %s) %s", tareasCount, tareasAux.size(), getString(R.string.ejecutar_tareas_titulo)));
                                            }))
                                            .setPositiveButton(R.string.continuar, ((dialogInterface, pos) -> {
                                                floatingActionEjecutarTareas.setTitle(getString(R.string.ejecutar_tareas_titulo));
                                                tareas = null;
                                                dialogInterface.dismiss();
                                            }));

                                    Spinner spinner = (Spinner) activity.getSelectedItem();
                                    if (spinner.getKey() == null || (spinner.getKey() != null && !spinner.getKey().equals(idAmTarea))) {
                                        dialog2.show();
                                    }
                                } else {
                                    floatingActionEjecutarTareas.setTitle(getString(R.string.ejecutar_tareas_titulo));
                                    tareas = null;
                                }
                            } else {
                                Spinner spinner = (Spinner) activity.getSelectedItem();
                                if (spinner.getKey() != null) {
                                    int tareasCount = 0;
                                    for (Actividad actividad : actividades) {
                                        if (spinner.getKey() != "SIN_ENTIDAD" && actividad.getId().equals(Long.valueOf(spinner.getKey()))) {
                                            for (Tarea tarea : actividad.getTareas()) {
                                                if (tarea.isEjecutada()) {
                                                    tareasCount++;
                                                }
                                            }

                                            if (actividad.getTareas().size() > 0) {
                                                floatingActionEjecutarTareas.setTitle(String.format("(%s de %s) %s", tareasCount, actividad.getTareas().size(), getString(R.string.ejecutar_tareas_titulo)));
                                            } else {
                                                floatingActionEjecutarTareas.setTitle(getString(R.string.ejecutar_tareas_titulo));
                                                tareas = null;
                                            }
                                            return;
                                        }
                                    }
                                } else {
                                    floatingActionEjecutarTareas.setTitle(getString(R.string.ejecutar_tareas_titulo));
                                    tareas = null;
                                }
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                id = id == null ? bundle.getLong(KEY_ID) : id;
                FloatingActionButton floatingActionLecturas = findViewById(R.id.lecturas);
                if (UserPermission.check(this, UserPermission.VISUALIZAR_BOTON_REGISTRAR_LECTURAS_BITACORA, true)) {
                    floatingActionLecturas.setVisibility(View.VISIBLE);
                    floatingActionLecturas.setOnClickListener(v -> {
                        if (type == OT) {
                            Long idAm = null;
                            if (activity != null) {
                                Spinner spinner = (Spinner) activity.getSelectedItem();
                                idAm = spinner.getKey() != null ? Long.valueOf(spinner.getKey()) : null;
                            }

                            if (idAm == null) {
                                Snackbar.make(getView(), R.string.seleccionar_actividad, Snackbar.LENGTH_LONG)
                                        .show();
                                return;
                            }

                            Bundle bundle_2 = new Bundle();
                            bundle_2.putLong(com.mantum.component.Mantum.KEY_ID, id);
                            bundle_2.putString(LecturaActivity.KEY_TYPE, "OT");
                            bundle_2.putString(LecturaActivity.KEY_TYPE_ACTION, "OT");
                            bundle_2.putBoolean(LecturaActivity.HIDE_ACTION, true);
                            bundle_2.putLong(LecturaActivity.ID_AM, idAm);
                            bundle_2.putString(LecturaActivity.KEY_VARIABLES, variables);
                            startActivity(LecturaActivity.class, bundle_2);
                        } else {
                            Long idAm = null;
                            if (activity != null) {
                                Spinner spinner = (Spinner) activity.getSelectedItem();
                                idAm = spinner.getKey() != null ? Long.valueOf(spinner.getKey()) : null;
                            }

                            Bundle bundle_2 = new Bundle();
                            bundle_2.putLong(com.mantum.component.Mantum.KEY_ID, id);
                            bundle_2.putString(LecturaActivity.KEY_TYPE_ACTION, "OT_Bitacora");
                            bundle_2.putString(LecturaActivity.KEY_TYPE, tipoentidad);
                            bundle_2.putString(LecturaActivity.KEY_VARIABLES, variables);
                            bundle_2.putBoolean(LecturaActivity.HIDE_ACTION, true);
                            if (idAm != null) {
                                bundle_2.putLong(LecturaActivity.ID_AM, idAm);
                            }
                            startActivity(LecturaActivity.class, bundle_2);
                        }
                    });
                }
            }

            LogBook logBook = database.where(LogBook.class)
                    .equalTo("cuenta.UUID", account)
                    .findFirst();

            if (logBook != null && logBook.isTurnosmanualesbitacora()) {
                TextInputLayout horasHabilesContenedor = findViewById(R.id.horas_habiles_contenedor);
                horasHabilesContenedor.setVisibility(View.VISIBLE);
                if (horasHabiles != null) {
                    TextInputEditText horasHabilesInput = findViewById(R.id.horas_habiles);
                    horasHabilesInput.setText(String.valueOf(horasHabiles));
                }
            }

            recorridoService = new RecorridoService(this, RecorridoService.Tipo.OT);
            Recorrido recorrido = recorridoService.obtenerPendiente(id);
            if (recorrido != null && recorrido.getEstado() != null && recorrido.getEstado().equals(ATNotificationService.EN_EJECUCION.getNombre())) {
                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage(String.format(getString(R.string.mensaje_recorrido_en_ejecucion), recorrido.getCodigo()))
                        .setNegativeButton(getString(R.string.aceptar), (dialog, id) -> {
                            dialog.dismiss();
                            backActivity();
                        })
                        .show();
            }
            TextView buscar = findViewById(R.id.buscar);
            buscar.setOnClickListener(v -> {
                if (UUIDTransaccion != null && UserPermission.check(this, VALIDAR_REGISTRO_BITACORA_OT, false)) {
                    Snackbar.make(getView(), R.string.orden_trabajo_buscar_error, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                Bundle bundle1 = new Bundle();
                bundle1.putLong(BridgeActivity.ID_ENTIDAD_SELECCIONADA, id);
                bundle1.putString(BridgeActivity.TIPO_ENTIDAD_SELECCIONADA, tipoentidad);

                Intent intent = new Intent(this, BridgeActivity.class);
                intent.putExtras(bundle1);
                startActivityForResult(intent, BridgeActivity.REQUEST_CODE);
            });

            bitacoraContinuaService = new BitacoraContinuaService(this, BitacoraContinuaService.Tipo.OT);
            if (type == OT || type == OT_BITACORA) {
                if (UUIDTransaccion == null) {
                    boolean validarRegistroOT = UserPermission.check(this, VALIDAR_REGISTRO_BITACORA_OT, false);
                    boolean validarGestionServicio = UserPermission.check(this, MODULO_GESTION_SERVICIOS, false);
                    if (validarRegistroOT || validarGestionServicio) {
                        if (bitacoraContinuaService.pendientes(id)) {
                            BitacoraContinua bitacoraContinua = bitacoraContinuaService.obtenerPendiente(id);

                            String codigo = "";
                            if (bitacoraContinua != null) {
                                codigo = bitacoraContinua.getCodigo();
                            }

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                            alertDialogBuilder.setCancelable(false);

                            if (validarRegistroOT) {
                                alertDialogBuilder.setMessage(String.format(getString(R.string.orden_trabajo_pendiente), codigo));
                                alertDialogBuilder.setNegativeButton(R.string.no, ((dialog, which) -> super.backActivity()));

                                alertDialogBuilder.setPositiveButton(R.string.si, (dialog, id) -> {
                                    if (bitacoraContinuaService.eliminar()) {
                                        Snackbar.make(getView(), R.string.orden_trabajo_error_eliminar, Snackbar.LENGTH_LONG)
                                                .show();
                                    }
                                });

                            } else {
                                alertDialogBuilder.setMessage(String.format(getString(R.string.orden_trabajo_pendiente_tecnico), codigo));
                                alertDialogBuilder.setPositiveButton(R.string.aceptar, (dialog, which) -> {
                                    dialog.dismiss();
                                });
                            }

                            alertDialogBuilder.show();
                            return;
                        }

                        if (validarRegistroOT && bitacoraContinuaService.pendientes(BitacoraContinuaService.Tipo.RUTA_TRABAJO)) {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                            alertDialogBuilder.setCancelable(false);
                            alertDialogBuilder.setMessage(R.string.orden_trabajo_rt_pendiente);
                            alertDialogBuilder.setNegativeButton(R.string.aceptar, (dialog, id) -> super.backActivity());
                            alertDialogBuilder.show();
                            return;
                        }

                        if (validarRegistroOT && !bitacoraContinuaService.existe(id)) {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                            alertDialogBuilder.setCancelable(true);
                            alertDialogBuilder.setMessage(R.string.orden_trabajo_inicio_registro);
                            alertDialogBuilder.setNegativeButton(getString(R.string.aceptar), (dialog, id) -> dialog.cancel());
                            alertDialogBuilder.show();

                            String codigo = "";
                            TextInputEditText register = findViewById(R.id.register);
                            if (register != null && register.getText() != null) {
                                codigo = register.getText().toString();
                            }

                            BitacoraContinua.Data data = new BitacoraContinua.Data();
                            data.setCodigo(codigo);
                            compositeDisposable.add(bitacoraContinuaService.iniciar(id, data)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(Functions.emptyConsumer(), com.mantum.component.Mantum::ignoreError, Functions.EMPTY_ACTION));

                            // Se inactivan los campos de fecha, hora inicial y hora final
                            this.time.setEnabled(false);
                            this.end.setEnabled(false);
                            this.date.setEnabled(false);
                        }

                        BitacoraContinua bitacoraContinua = bitacoraContinuaService.obtener(id);
                        if (bitacoraContinua != null) {
                            BitacoraOrdenTrabajo bitacoraOrdenTrabajo
                                    = new Gson().fromJson(bitacoraContinua.getValue(), BitacoraOrdenTrabajo.class);

                            if (bitacoraOrdenTrabajo != null) {
                                if (bitacoraOrdenTrabajo.getDate() != null && !bitacoraOrdenTrabajo.getDate().isEmpty()) {
                                    SimpleDateFormat simpleDateFormat
                                            = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                    fecha.setTime(simpleDateFormat.parse(bitacoraOrdenTrabajo.getDate()));
                                    this.date.setValue(fecha);
                                    this.date.setEnabled(false);
                                }

                                if (bitacoraOrdenTrabajo.getTimestart() != null && !bitacoraOrdenTrabajo.getTimestart().isEmpty()) {
                                    horaInicial = Calendar.getInstance(TimeZone.getDefault());
                                    SimpleDateFormat simpleDateFormat
                                            = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                    horaInicial.setTime(simpleDateFormat.parse(bitacoraOrdenTrabajo.getTimestart()));
                                    this.time.setValue(horaInicial);
                                    this.time.setEnabled(false);
                                }

                                if (bitacoraOrdenTrabajo.getTimeend() != null && !bitacoraOrdenTrabajo.getTimeend().isEmpty()) {
                                    horaFinal = Calendar.getInstance(TimeZone.getDefault());
                                    SimpleDateFormat simpleDateFormat
                                            = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                    horaFinal.setTime(simpleDateFormat.parse(bitacoraOrdenTrabajo.getTimeend()));
                                    this.end.setValue(horaFinal);
                                    this.end.setEnabled(false);
                                } else if (validarGestionServicio) {
                                    this.end.setEnabled(false);
                                }

                                if (UserPermission.check(this, VALIDAR_REGISTRO_BITACORA_OT, false)) {
                                    verificada = bitacoraOrdenTrabajo.isVerficiada();
                                }

                                if (UserPermission.check(this, VALIDAR_REGISTRO_BITACORA_OT, false)) {
                                    EditText nota = findViewById(R.id.nota);
                                    nota.setText(bitacoraOrdenTrabajo.getNota());

                                    EditText observacionActivos = findViewById(R.id.observacion_activos);
                                    observacionActivos.setText(bitacoraOrdenTrabajo.getObservacionActivos());

                                    EditText execution = findViewById(R.id.execution);
                                    execution.setText(bitacoraOrdenTrabajo.getExecutionrate());

                                    EditText description = findViewById(R.id.description);
                                    description.setText(bitacoraOrdenTrabajo.getDescription());

                                    photos.addAll(bitacoraOrdenTrabajo.getFiles());
                                    variables = bitacoraOrdenTrabajo.getVariables();

                                    recursos = RecursoHelper.recursoAdapter(bitacoraOrdenTrabajo.getRecursos());
                                    mochilas = RecursoHelper.mochilaAdapter(bitacoraOrdenTrabajo.getMochila());
                                }

                                if (bitacoraOrdenTrabajo.getIdam() != null) {
                                    android.widget.SpinnerAdapter adapter = activity.getAdapter();
                                    if (adapter != null) {
                                        int total = adapter.getCount();
                                        for (int i = 0; i < total; i++) {
                                            Spinner spinner = (Spinner) adapter.getItem(i);
                                            if (spinner.getKey() != null && spinner.getKey().equals(String.valueOf(bitacoraOrdenTrabajo.getIdam()))) {
                                                activity.setSelection(i);
                                                i = total;
                                            }
                                        }

                                        Spinner selected = (Spinner) activity.getSelectedItem();
                                        if (selected != null) {
                                            List<Spinner> elements = new ArrayList<>();
                                            adapter = activity.getAdapter();
                                            total = adapter.getCount();
                                            for (int i = 0; i < total; i++) {
                                                Spinner spinner = (Spinner) adapter.getItem(i);
                                                if (selected.getIdentidad() != null
                                                        && selected.getIdentidad().equals(spinner.getIdentidad())
                                                        && selected.getTipoentidad() != null
                                                        && selected.getTipoentidad().equals(spinner.getTipoentidad())) {
                                                    elements.add(spinner);
                                                }
                                            }

                                            if (!elements.isEmpty()) {
                                                activity.setAdapter(new SpinnerAdapter<>(this, elements).getAdapter());
                                            }
                                        }
                                    }
                                }

                                generaParo = findViewById(R.id.paro);
                                android.widget.SpinnerAdapter adapter = generaParo.getAdapter();
                                int total = adapter.getCount();
                                for (int i = 0; i < total; i++) {
                                    Spinner spinner = (Spinner) adapter.getItem(i);
                                    if (spinner.getKey() != null && spinner.getKey().equals(bitacoraOrdenTrabajo.getTipoparo())) {
                                        generaParo.setSelection(i);
                                        i = total;
                                    }
                                }
                            }
                        } else if (modoRecorrido) {
                            this.date.setEnabled(false);
                            this.time.setEnabled(false);
                            this.end.setEnabled(false);
                        }
                    }
                }

                if (UUIDTransaccion != null && UserPermission.check(this, VALIDAR_REGISTRO_BITACORA_OT, false)) {
                    Spinner selected = (Spinner) activity.getSelectedItem();
                    if (selected != null) {
                        List<Spinner> elements = new ArrayList<>();
                        android.widget.SpinnerAdapter adapter = activity.getAdapter();
                        int total = adapter.getCount();
                        for (int i = 0; i < total; i++) {
                            Spinner spinner = (Spinner) adapter.getItem(i);
                            if (selected.getIdentidad().equals(spinner.getIdentidad())
                                    && selected.getTipoentidad().equals(spinner.getTipoentidad())) {
                                elements.add(spinner);
                            }
                        }

                        if (!elements.isEmpty()) {
                            verificada = true;
                            activity.setAdapter(new SpinnerAdapter<>(this, elements).getAdapter());
                        }
                    }
                }
            }


            if (type == EVENT) {
                LinearLayout activityContainer = findViewById(R.id.activity_container);
                activityContainer.setVisibility(View.GONE);
            }

            if (type == OT && UserPermission.check(this, LISTA_CHEQUEO, false)) {
                OrdenTrabajo ordentrabajo = database.where(OrdenTrabajo.class)
                        .equalTo("id", id)
                        .equalTo("cuenta.UUID", account)
                        .findFirst();

                if (ordentrabajo != null && !ordentrabajo.getListachequeo().isEmpty()) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                    alertDialogBuilder.setCancelable(true);
                    alertDialogBuilder.setMessage(R.string.lista_chequeo_pendiente);

                    alertDialogBuilder.setPositiveButton(R.string.si, (dialog, which) -> dialog.cancel());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "onCreate: ", e);
            //backActivity(R.string.error_app);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }

        showMessage(data);
        Bundle bundle = data.getExtras();
        if (resultCode == RESULT_OK && bundle != null) {
            switch (requestCode) {
                case BridgeActivity.REQUEST_CODE:
                    Long idEntidadLectura = bundle.getLong(
                            BridgeActivity.LECTURA_ID_ENTIDAD, -1);

                    String tipoEntidadLectura = bundle.getString(
                            BridgeActivity.LECTURA_TIPO_ENTIDAD, "NO_LECTURA");

                    List<Spinner> elements = new ArrayList<>();
                    if (activity != null) {
                        android.widget.SpinnerAdapter adapter = activity.getAdapter();
                        int total = adapter.getCount();
                        for (int i = 0; i < total; i++) {
                            Spinner spinner = (Spinner) adapter.getItem(i);
                            if (spinner != null && idEntidadLectura.equals(spinner.getIdentidad())
                                    && tipoEntidadLectura.equals(spinner.getTipoentidad())) {
                                elements.add(spinner);
                            }
                        }
                    }

                    if (!elements.isEmpty() && activity != null) {
                        verificada = true;
                        activity.setAdapter(new SpinnerAdapter<>(this, elements).getAdapter());
                        break;
                    }

                    Snackbar.make(getView(), R.string.validacion_lectura_qr_ot, Snackbar.LENGTH_LONG)
                            .show();
                    break;
            }

            String lecturaJsonResponse = bundle.getString(LecturaActivity.JSON_RESPONSE, null);
            if (lecturaJsonResponse != null) {
                variables = lecturaJsonResponse;
            }

            RecursoHelper recursoHelper = (RecursoHelper) bundle.getSerializable(RecursosActivity.RECURSO);
            if (recursoHelper != null) {
                recursos = recursoHelper;
            }

            String firma = bundle.getString("file");
            if (firma != null) {
                photos.add(new Photo(this, new File(firma)));
            }

            RecursoHelper mochilaHerlper = (RecursoHelper) bundle.getSerializable(MochilaActivity.RECURSO);
            if (mochilaHerlper != null) {
                mochilas = mochilaHerlper;
            }

            SparseArray<PhotoAdapter> parcelable = bundle.getSparseParcelableArray(
                    GaleriaActivity.PATH_FILE_PARCELABLE);

            if (parcelable != null) {
                photos.clear();
                int total = parcelable.size();
                if (total > 0) {
                    for (int i = 0; i < total; i++) {
                        PhotoAdapter photoAdapter = parcelable.get(i);
                        photos.add(new Photo(this, new File(photoAdapter.getPath()),
                                photoAdapter.isDefaultImage(), photoAdapter.getIdCategory(),
                                photoAdapter.getDescription()));
                    }
                }
            }

            PendienteMantenimiento.Request request
                    = (PendienteMantenimiento.Request) bundle.getSerializable(PendienteMantenimientoActivity.KEY_FORM);
            if (request != null) {
                pendienteMantenimiento = request;
            }

            Falla.Request falla = (Falla.Request) bundle.getSerializable(GestionFallaActivity.KEY_FORM);
            if (falla != null) {
                this.falla = falla;
            }

            SparseArray<Devolucion> devolucionesParcelable
                    = bundle.getSparseParcelableArray(DevolucionActivity.DEVOLUCIONES_PARCELABLE);

            if (devolucionesParcelable != null) {
                devoluciones = devolucionesParcelable;
            }

            idEquipoParo = bundle.getLong(AsignarParoActivity.ID_EQUIPO_PARO);
            idAmParo = bundle.getString(AsignarParoActivity.ID_AM_PARO);

            SparseArray<Paro.ParoHelper> paroSparseArray = bundle.getSparseParcelableArray(AsignarParoActivity.KEY_FORM);
            if (paroSparseArray != null) {
                paros.clear();
                if (paroSparseArray.size() > 0) {
                    for (int i = 0; i < paroSparseArray.size(); i++) {
                        paros.add(paroSparseArray.get(i));
                    }
                    floatingActionAsignarParos.setTitle(String.format("(%s) %s", paros.size(), getString(R.string.asignar_paros_titulo)));
                } else if (floatingActionAsignarParos != null) {
                    floatingActionAsignarParos.setTitle(getString(R.string.asignar_paros_titulo));
                }
            }

            String idAmTareaAux = bundle.getString(EjecutarTareaActivity.ID_AM_TAREA);
            if (idAmTareaAux != null) idAmTarea = idAmTareaAux;

            String tareasJson = bundle.getString(EjecutarTareaActivity.KEY_FORM);
            if (tareasJson != null) {
                tareas = tareasJson;

                int tareasCount = 0;
                Type type = new TypeToken<ArrayList<Tarea.TareaHelper>>() {
                }.getType();
                List<Tarea.TareaHelper> tareasAux = new Gson().fromJson(tareas, type);

                for (Tarea.TareaHelper tareaHelper : tareasAux) {
                    if (tareaHelper.isEjecutada()) {
                        tareasCount++;
                    }
                }

                floatingActionEjecutarTareas.setTitle(String.format("(%s de %s) %s", tareasCount, tareasAux.size(), getString(R.string.ejecutar_tareas_titulo)));
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (type == OT || type == OT_BITACORA) {
            if (UUIDTransaccion == null && UserPermission.check(this, VALIDAR_REGISTRO_BITACORA_OT, false)) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setMessage(R.string.orden_trabajo_cancelar);
                alertDialogBuilder.setNegativeButton(R.string.si, (dialog, id) -> {
                    BitacoraOrdenTrabajo value = obtenerFormularioOT();
                    bitacoraContinuaService.procesando(this.id, value.toJson());
                    super.backActivity();
                });

                alertDialogBuilder.setPositiveButton(R.string.no, (dialog, id) -> {
                    if (this.id != null) {
                        bitacoraContinuaService.eliminar(this.id);
                    }
                    super.backActivity();
                });

                alertDialogBuilder.show();
                return;
            }
        }
        super.backActivity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_formulario, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null && bundle.getString(MODE_EDIT) != null) {
                BackEditTransaction.backDialog(this);
            } else {
                onBackPressed();
            }
            return true;
        }

        if (menuItem.getItemId() == R.id.action_done) {
            register();
            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (geolocation != null) {
            geolocation.stop();
        }

        if (recorridoService != null) {
            recorridoService.close();
        }

        if (bitacoraContinuaService != null) {
            bitacoraContinuaService.close();
        }

        if (atNotificationService != null) {
            atNotificationService.close();
        }

        compositeDisposable.clear();

        getSharedPreferences(getString(R.string.preference_file_bitacora), Context.MODE_PRIVATE)
                .edit().clear().apply();
    }

    @NonNull
    @Deprecated
    private BitacoraOrdenTrabajo obtenerFormularioOT() {
        String idTipoTiempo = null;
        String textoTipoTIempo = null;
        AppCompatSpinner tipoTiempo = findViewById(R.id.type_time);
        Spinner spinnerTipoTiempo = (Spinner) tipoTiempo.getSelectedItem();
        if (spinnerTipoTiempo != null) {
            idTipoTiempo = spinnerTipoTiempo.getKey();
            textoTipoTIempo = spinnerTipoTiempo.getValue();
        }

        String generaParo = "";
        AppCompatSpinner generaParoAppCompatSpinner = findViewById(R.id.paro);
        Spinner spinnerGeneraParo = (Spinner) generaParoAppCompatSpinner.getSelectedItem();
        if (spinnerGeneraParo != null) {
            generaParo = spinnerGeneraParo.getKey();
        }

        String estadoEquipo = "";
        AppCompatSpinner estadoEquipoAppCompatSpinner = findViewById(R.id.state);
        Spinner spinnerEstadoEquipo = (Spinner) estadoEquipoAppCompatSpinner.getSelectedItem();
        if (spinnerEstadoEquipo != null) {
            estadoEquipo = spinnerEstadoEquipo.getKey();
        }

        EditText description = findViewById(R.id.description);
        EditText rate_ot = findViewById(R.id.execution);

        Long idam = null;
        String activityName = null;
        EditText nota = findViewById(R.id.nota);
        EditText observacionActivos = findViewById(R.id.observacion_activos);
        if (activity != null) {
            Spinner spinner = (Spinner) activity.getSelectedItem();
            idam = spinner.getKey() != null ? Long.valueOf(spinner.getKey()) : null;
            activityName = spinner.getValue();
        }

        BitacoraOrdenTrabajo datosOrdenTrabajo = new BitacoraOrdenTrabajo();
        datosOrdenTrabajo.setCode(((TextInputEditText) findViewById(R.id.register))
                .getText().toString());
        datosOrdenTrabajo.setType(activityName);
        datosOrdenTrabajo.setIdot(id);
        datosOrdenTrabajo.setIdam(idam);
        datosOrdenTrabajo.setDate(date.getValue());
        datosOrdenTrabajo.setTimestart(time.getValue());
        datosOrdenTrabajo.setTimeend(end.getValue());
        datosOrdenTrabajo.setExecutionrate(rate_ot.getText().toString());
        datosOrdenTrabajo.setDescription(description.getText().toString());
        datosOrdenTrabajo.setFiles(photos);
        datosOrdenTrabajo.setMochila(mochilas == null ? new ArrayList<>() : mochilas.getMochilas());
        datosOrdenTrabajo.setRecursos(recursos == null ? new ArrayList<>() : recursos.getRecursos());
        datosOrdenTrabajo.setPendientepmtto(pendienteMantenimiento);
        datosOrdenTrabajo.setTipotiempo(idTipoTiempo);
        datosOrdenTrabajo.setTipoTiempoTexto(textoTipoTIempo);
        datosOrdenTrabajo.setNota(nota.getText().toString());
        datosOrdenTrabajo.setTipoparo(generaParo);
        datosOrdenTrabajo.setVariables(variables);
        datosOrdenTrabajo.setObservacionActivos(observacionActivos.getText().toString());
        datosOrdenTrabajo.setVerficiada(verificada);
        datosOrdenTrabajo.setEstadoEquipo(estadoEquipo);

        TextInputEditText horasHabilesOrdenTrabajo = findViewById(R.id.horas_habiles);
        if (horasHabilesOrdenTrabajo != null) {
            String valueOT = horasHabilesOrdenTrabajo.getText().toString();
            datosOrdenTrabajo.setHorashabilesdia(valueOT.isEmpty() ? null : Float.parseFloat(valueOT));
        }

        if (Version.check(this, 7)) {
            if (location != null) {
                Coordenada coordenada = new Coordenada();
                coordenada.setLatitude(location.getLatitude());
                coordenada.setAltitude(location.getAltitude());
                coordenada.setAccuracy(location.getAccuracy());
                coordenada.setLongitude(location.getLongitude());
                datosOrdenTrabajo.setLocation(coordenada);
            }
        }

        return datosOrdenTrabajo;
    }

    private void register() {
        boolean validateGPS = (Version.check(this, 7) && (UserPermission.check(this, BITACORA_GPS)
                || UserPermission.check(this, BITACORA_GPS_SOLICITE)));
        register(validateGPS, true, true, false, true, false);
    }

    private void register(boolean validarGPS, boolean confirm, boolean obtenerFinalziacion, boolean finalizar, boolean disponible, boolean estadoSincronizado) {
        try {
            Where where = new Where()
                    .equalTo("UUID", account);
            Cuenta cuenta = (Cuenta) database.findOne(where, Cuenta.class);

            if (validarGPS && UUIDTransaccion == null) {
                if (!Geolocation.checkPermission(this) || !Geolocation.isEnabled(this)) {
                    AlertDialog.Builder alertDialogBuilder
                            = new AlertDialog.Builder(BitacoraActivity.this);
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setTitle(R.string.ubicacion_titulo);
                    alertDialogBuilder.setMessage(R.string.ubicacion_error_acceso);
                    alertDialogBuilder.setPositiveButton(R.string.accion_configuration, (dialog, which) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)));
                    alertDialogBuilder.setNegativeButton(getString(R.string.cancelar), (dialog, id) -> dialog.cancel());
                    alertDialogBuilder.show();
                    return;
                }

                geolocation = new Geolocation(this, new OnLocationListener() {

                    @Override
                    public void onLocationChanged(@NonNull Geolocation geolocation, @NonNull Location location) {
                        geolocation.stop();
                        if (!isFinishing()) {
                            if (progress != null && progress.isShowing()) {
                                progress.hidden();
                            }

                            if (countDownTimer != null) {
                                countDownTimer.cancel();
                            }

                            BitacoraActivity.this.location = location;
                            register(false, true, true, false, disponible, false);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                                .show();
                    }

                });

                int millisInFuture = UserParameter.NUMBER_SECONDS_TIMER_GPS;
                String milisecondsGPSString = UserParameter.getValue(this, UserParameter.SECONDS_TIMER_GPS);
                if (milisecondsGPSString != null) {
                    millisInFuture = Integer.parseInt(milisecondsGPSString);
                }

                countDownTimer = new CountDownTimer(millisInFuture, COUNT_DOWN_INTERVAL) {

                    @Override
                    public void onTick(long millisUntilFinished) {
                    }

                    @Override
                    public void onFinish() {
                        if (progress != null && !progress.isShowing()) {
                            return;
                        }

                        geolocation.stop();
                        Location location = geolocation.getLastKnownLocation();
                        if (UserPermission.check(BitacoraActivity.this, LAST_KNOWN_LOCATION)) {
                            location = null; // Requiere una nueva ubicación
                        }

                        if (location == null) {
                            if (!isFinishing()) {
                                if (progress != null && progress.isShowing()) {
                                    progress.hidden();
                                }
                            }

                            AlertDialog.Builder alertDialogBuilder
                                    = new AlertDialog.Builder(BitacoraActivity.this);
                            alertDialogBuilder.setCancelable(false);
                            alertDialogBuilder.setTitle(R.string.ubicacion_titulo);
                            alertDialogBuilder.setNegativeButton(getString(R.string.ubicacion_reintentar), (dialogInterface, i) -> {
                                geolocation.start(0, 0);

                                start();
                                dialogInterface.cancel();
                                if (progress != null) {
                                    progress.show();
                                }
                            });

                            if (UserPermission.check(BitacoraActivity.this, BITACORA_GPS_SOLICITE)) {
                                alertDialogBuilder.setMessage(R.string.ubicacion_error_continuar);
                                alertDialogBuilder.setPositiveButton(getString(R.string.ubicacion_continuar), (dialogInterface, i) -> {
                                    dialogInterface.cancel();
                                    if (!isFinishing()) {
                                        if (progress != null && progress.isShowing()) {
                                            progress.hidden();
                                        }
                                    }
                                    register(false, true, true, false, disponible, false);
                                });
                            } else {
                                alertDialogBuilder.setMessage(R.string.ubicacion_error_requiere);
                                alertDialogBuilder.setPositiveButton(getString(R.string.cancelar), (dialogInterface, i) -> {
                                    dialogInterface.cancel();
                                    if (!isFinishing()) {
                                        if (progress != null && progress.isShowing()) {
                                            progress.hidden();
                                        }
                                    }
                                    //backActivity();
                                });
                            }

                            alertDialogBuilder.show();
                            return;
                        }

                        if (!isFinishing()) {
                            if (progress != null && progress.isShowing()) {
                                progress.hidden();
                            }
                        }

                        Log.i(TAG, "Ubicación obtenida desde la cache -> " + location.toString());
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }

                        register(true, true, true, false, disponible, false);
                    }
                };

                long diffMinutes = 0;
                location = geolocation.getLastKnownLocation();
                if (location != null) {
                    Log.i(TAG, "Ultima ubicación conocida en cache -> " + location.toString());
                    long now = Calendar.getInstance().getTime().getTime();
                    diffMinutes = ((now - location.getTime()) / 1000) / 60;
                }

                Log.i(TAG, "Hace " + diffMinutes + " minutos se obtuvo una ubicación");
                if (location == null || diffMinutes >= 10) {
                    Log.i(TAG, "Buscando ultima ubicación conocida en la base de datos");
                    List<Transaccion> transaccions = database.where(Transaccion.class)
                            .equalTo("modulo", Transaccion.MODULO_GEOLOCALIZACION)
                            .equalTo("accion", Transaccion.ACCION_UBICACION)
                            .sort("creation", Sort.DESCENDING)
                            .findAll();

                    if (transaccions != null && !transaccions.isEmpty()) {
                        Transaccion transaccion = transaccions.get(0);
                        if (transaccion != null) {
                            Coordenada coordenada = new GsonBuilder()
                                    .setDateFormat("yyyy-MM-dd HH:mm:ss")
                                    .create().fromJson(transaccion.getValue(), Coordenada.class);

                            location = new Location("temporal");
                            location.setAccuracy(coordenada.getAccuracy());
                            location.setLatitude(coordenada.getLatitude());
                            location.setLongitude(coordenada.getLongitude());
                            location.setAltitude(coordenada.getAltitude());
                            location.setTime(coordenada.getDatetime().getTime());

                            Log.i(TAG, "Ultima ubicación conocida en base de datos -> " + location.toString());
                            long now = Calendar.getInstance().getTime().getTime();
                            diffMinutes = ((now - location.getTime()) / 1000) / 60;
                        }
                    }
                }

                if (location == null || diffMinutes >= 10) {
                    Log.i(TAG, "Vamos a cargar la ubicación");
                    geolocation.start(0, 0);
                    progress.show(R.string.titulo_ubicacion_progreso,
                            R.string.mensaje_ubicacion_progreso);
                    countDownTimer.start();
                    return;
                }
            }

            if (progress != null && progress.isShowing()) {
                return;
            }

            closeKeyboard();

            TextInputLayout contentExecution = findViewById(R.id.content_execution);
            TextInputLayout contentCreationEnd = findViewById(R.id.content_creation_end);
            TextInputLayout contentCreationTime = findViewById(R.id.content_creation_time);
            TextInputLayout contentCreationDate = findViewById(R.id.content_creation_date);

            contentExecution.setError(null);
            contentCreationEnd.setError(null);
            contentCreationTime.setError(null);
            contentCreationDate.setError(null);

            String idTipoTiempo = null;
            String textoTipoTIempo = null;
            AppCompatSpinner tipoTiempo = findViewById(R.id.type_time);
            Spinner spinnerTipoTiempo = (Spinner) tipoTiempo.getSelectedItem();
            if (spinnerTipoTiempo != null) {
                idTipoTiempo = spinnerTipoTiempo.getKey();
                textoTipoTIempo = spinnerTipoTiempo.getValue();
            }

            String generaParo = "";
            AppCompatSpinner generaParoAppCompatSpinner = findViewById(R.id.paro);
            Spinner spinnerGeneraParo = (Spinner) generaParoAppCompatSpinner.getSelectedItem();
            if (spinnerGeneraParo != null) {
                generaParo = spinnerGeneraParo.getKey();
            }

            String estadoEquipo = "";
            AppCompatSpinner estadoEquipoAppCompatSpinner = findViewById(R.id.state);
            Spinner spinnerEstadoEquipo = (Spinner) estadoEquipoAppCompatSpinner.getSelectedItem();
            if (spinnerEstadoEquipo != null) {
                estadoEquipo = spinnerEstadoEquipo.getKey();
            }

            if (confirm && (type == OT || type == OT_BITACORA)) {
                if (UUIDTransaccion == null && UserPermission.check(this, VALIDAR_REGISTRO_BITACORA_OT, false)) {
                    if (!verificada) {
                        hide();
                        Snackbar.make(getView(), R.string.falta_verificacion, Snackbar.LENGTH_LONG)
                                .show();
                        return;
                    }

                    hide();
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setMessage(R.string.orden_trabajo_finalizar_registro);
                    alertDialogBuilder.setPositiveButton(R.string.si, (dialog, id) -> {
                        this.end.setValue();
                        register(false, false, true, false, disponible, false);
                    });
                    alertDialogBuilder.setNegativeButton(R.string.no, (dialog, id) -> dialog.cancel());
                    alertDialogBuilder.show();
                    return;
                }
            }

            if (!this.end.isEnabled()) {
                Calendar horaFinal = Calendar.getInstance(TimeZone.getDefault());
                SimpleDateFormat simpleDateFormat
                        = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String results = simpleDateFormat.format(horaFinal.getTime());
                horaFinal.setTime(simpleDateFormat.parse(results));
                this.end.setValue(horaFinal);
            }

            boolean isEmpty;
            String accion, url, value, dateend = null;
            EditText description = findViewById(R.id.description);

            // setear la fecha por cambio de dia (un dia superior)
            if (horafinal != null) {
                Calendar current = Calendar.getInstance();
                Calendar horaInicial = Calendar.getInstance(TimeZone.getDefault());
                Date date = new Date(horafinal);
                horaInicial.setTime(date);

                if (current.get(Calendar.DAY_OF_YEAR) != horaInicial.get(Calendar.DAY_OF_YEAR)) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    dateend = simpleDateFormat.format(date);
                }
            }

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date current = Calendar.getInstance().getTime();
            Date currentDate = dateFormat.parse(dateFormat.format(current));
            Date currentTime = timeFormat.parse(timeFormat.format(current));

            switch (type) {
                case SS:
                    if (!time.isValid()) {
                        contentCreationTime.setError(getString(R.string.ss_register_creationTime_empty));
                        return;
                    }

                    if (!end.isValid()) {
                        contentCreationEnd.setError(getString(R.string.ss_register_creationEnd_empty));
                        return;
                    }

                    Date fechaInicio = dateFormat.parse(date.getValue());
                    Date horaInicio = timeFormat.parse(time.getValue());
                    Date horaFin = timeFormat.parse(end.getValue());

                    if (fechaInicio.after(currentDate)) {
                        contentCreationDate.setError(getString(R.string.fecha_indicada_mayor_hora_actual));
                        return;
                    }

                    if (fechaInicio.equals(currentDate)) {
                        if (horaInicio.after(currentTime)) {
                            contentCreationTime.setError(getString(R.string.hora_indicada_mayor_hora_actual));
                            return;
                        }
                        if (horaFin.after(currentTime)) {
                            contentCreationEnd.setError(getString(R.string.hora_indicada_mayor_hora_actual));
                            return;
                        }
                    }

                    if (horaInicio.equals(horaFin)) {
                        contentCreationTime.setError(getString(R.string.hora_indicada_igual_hora_final));
                        return;
                    }
                    if (horaInicio.after(horaFin)) {
                        contentCreationTime.setError(getString(R.string.hora_indicada_mayor_hora_final));
                        return;
                    }

                    EditText rate_ss = findViewById(R.id.execution);
                    if (rate_ss.getText().toString().isEmpty()) {
                        contentExecution.setError(getString(R.string.ss_register_execution_empty));
                        contentExecution.requestFocus();
                        return;
                    }

                    try {
                        int ejecucion = Integer.parseInt(rate_ss.getText().toString());
                        if (ejecucion > 100) {
                            rate_ss.setText("100");
                        }
                    } catch (Exception ignored) {
                    }

                    // Bitacora SS o por grupo
                    url = grupos.isEmpty()
                            ? "restapp/app/savelogbookss"
                            : "restapp/app/savelogbookssgroup";

                    accion = Transaccion.ACCION_REGISTRAR_BITACORA_SS;
                    String code = ((TextInputEditText) findViewById(R.id.register))
                            .getText().toString();

                    BitacoraSolicitudServicio register = new BitacoraSolicitudServicio();
                    register.setToken(UUID.randomUUID().toString());
                    register.setCode(code);
                    register.setIdss(id);
                    register.setDate(date.getValue());
                    register.setTimestart(time.getValue());
                    register.setTimeend(end.getValue());
                    register.setExecutionrate(rate_ss.getText().toString());
                    register.setDescription(description.getText().toString());
                    register.setFiles(photos);
                    register.setGroup(grupos);
                    register.setRecursos(recursos == null ? new ArrayList<>() : recursos.getRecursos());
                    register.setPendientepmtto(pendienteMantenimiento);
                    register.setTipotiempo(idTipoTiempo);
                    register.setTipoTiempoTexto(textoTipoTIempo);
                    register.setFechaAnterior(dateend);

                    TextInputEditText horasHabilesSS = findViewById(R.id.horas_habiles);
                    if (horasHabilesSS != null) {
                        String valueSS = horasHabilesSS.getText().toString();
                        register.setHorashabilesdia(valueSS.isEmpty() ? null : Float.parseFloat(valueSS));
                    }

                    isEmpty = true;
                    if (Version.check(this, 7)) {

                        if (location != null) {
                            isEmpty = false;
                            Coordenada coordenada = new Coordenada();
                            coordenada.setLatitude(location.getLatitude());
                            coordenada.setAltitude(location.getAltitude());
                            coordenada.setAccuracy(location.getAccuracy());
                            coordenada.setLongitude(location.getLongitude());
                            register.setLocation(coordenada);
                        }
                    }

                    if (isEmpty && UUIDTransaccion == null && (Version.check(this, 7)
                            && UserPermission.check(this, BITACORA_GPS))) {
                        hide();
                        Snackbar.make(getView(), R.string.requiere_ubicacion, Snackbar.LENGTH_LONG)
                                .show();
                        return;
                    }

                    value = register.toJson();
                    break;

                case OT:
                    if (!time.isValid()) {
                        contentCreationTime.setError(getString(R.string.ss_register_creationTime_empty));
                        return;
                    }

                    if (!end.isValid()) {
                        contentCreationEnd.setError(getString(R.string.ss_register_creationEnd_empty));
                        return;
                    }

                    fechaInicio = dateFormat.parse(date.getValue());
                    horaInicio = timeFormat.parse(time.getValue());
                    horaFin = timeFormat.parse(end.getValue());

                    if (fechaInicio.after(currentDate)) {
                        contentCreationDate.setError(getString(R.string.fecha_indicada_mayor_hora_actual));
                        return;
                    }

                    if (fechaInicio.equals(currentDate)) {
                        if (horaInicio.after(currentTime)) {
                            contentCreationTime.setError(getString(R.string.hora_indicada_mayor_hora_actual));
                            return;
                        }
                        if (horaFin.after(currentTime)) {
                            contentCreationEnd.setError(getString(R.string.hora_indicada_mayor_hora_actual));
                            return;
                        }
                    }

                    if (!modoRecorrido) {
                        if (horaInicio.equals(horaFin)) {
                            contentCreationTime.setError(getString(R.string.hora_indicada_igual_hora_final));
                            return;
                        }
                        if (horaInicio.after(horaFin)) {
                            contentCreationTime.setError(getString(R.string.hora_indicada_mayor_hora_final));
                            return;
                        }
                    }

                    EditText rate_ot = findViewById(R.id.execution);
                    if (rate_ot != null && rate_ot.getText().toString().isEmpty()) {
                        contentExecution.setError(getString(R.string.ss_register_execution_empty));
                        contentExecution.requestFocus();
                        return;
                    }

                    try {
                        int ejecucion = Integer.parseInt(rate_ot.getText().toString());
                        if (ejecucion > 100) {
                            rate_ot.setText("100");
                        }
                    } catch (Exception ignored) {
                    }

                    show();

                    accion = Transaccion.ACCION_REGISTRAR_BITACORA_OT;

                    Long idam = null;
                    String activityName = null;
                    EditText nota = findViewById(R.id.nota);
                    EditText observacionActivos = findViewById(R.id.observacion_activos);
                    if (activity != null) {
                        Spinner spinner = (Spinner) activity.getSelectedItem();
                        if (spinner.getKey() != null && spinner.getKey().equals(SIN_ENTIDAD)) {
                            Snackbar.make(getView(), R.string.seleccionar_actividad, Snackbar.LENGTH_LONG)
                                    .show();
                            return;
                        }
                        idam = spinner.getKey() != null ? Long.valueOf(spinner.getKey()) : null;
                        activityName = spinner.getValue();
                    }

                    BitacoraOrdenTrabajo datosOrdenTrabajo = new BitacoraOrdenTrabajo();
                    datosOrdenTrabajo.setCode(((TextInputEditText) findViewById(R.id.register))
                            .getText().toString());
                    datosOrdenTrabajo.setType(activityName);
                    datosOrdenTrabajo.setIdot(id);
                    datosOrdenTrabajo.setIdam(idam);
                    datosOrdenTrabajo.setDate(date.getValue());
                    datosOrdenTrabajo.setTimestart(time.getValue());
                    datosOrdenTrabajo.setTimeend(end.getValue());
                    datosOrdenTrabajo.setExecutionrate(rate_ot.getText().toString());
                    datosOrdenTrabajo.setDescription(description.getText().toString());
                    datosOrdenTrabajo.setFiles(photos);
                    datosOrdenTrabajo.setGroup(grupos);
                    datosOrdenTrabajo.setMochila(mochilas == null ? new ArrayList<>() : mochilas.getMochilas());
                    datosOrdenTrabajo.setRecursos(recursos == null ? new ArrayList<>() : recursos.getRecursos());
                    datosOrdenTrabajo.setPendientepmtto(pendienteMantenimiento);
                    datosOrdenTrabajo.setTipotiempo(idTipoTiempo);
                    datosOrdenTrabajo.setTipoTiempoTexto(textoTipoTIempo);
                    datosOrdenTrabajo.setNota(nota.getText().toString());
                    datosOrdenTrabajo.setVariables(variables);
                    datosOrdenTrabajo.setObservacionActivos(observacionActivos.getText().toString());
                    datosOrdenTrabajo.setFechaAnterior(dateend);

                    if (Version.check(this, 18)) {
                        datosOrdenTrabajo.setParos(paros);
                    } else {
                        datosOrdenTrabajo.setTipoparo(generaParo);
                        datosOrdenTrabajo.setEstadoEquipo(estadoEquipo);
                    }

                    if (Version.check(this, 19) && tareas != null && !tareas.isEmpty()) {
                        datosOrdenTrabajo.setTareas(tareas);
                    }

                    TextInputEditText horasHabilesOrdenTrabajo = findViewById(R.id.horas_habiles);
                    if (horasHabilesOrdenTrabajo != null) {
                        String valueOT = horasHabilesOrdenTrabajo.getText().toString();
                        datosOrdenTrabajo.setHorashabilesdia(valueOT.isEmpty() ? null : Float.parseFloat(valueOT));
                    }

                    isEmpty = true;
                    if (Version.check(this, 7)) {

                        if (location != null) {
                            isEmpty = false;
                            Coordenada coordenada = new Coordenada();
                            coordenada.setLatitude(location.getLatitude());
                            coordenada.setAltitude(location.getAltitude());
                            coordenada.setAccuracy(location.getAccuracy());
                            coordenada.setLongitude(location.getLongitude());
                            datosOrdenTrabajo.setLocation(coordenada);
                        }
                    }

                    if (dateend != null) {
                        url = "restapp/app/dividelogbookot";

                        Date date = Calendar.getInstance().getTime();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        datosOrdenTrabajo.setDate(simpleDateFormat.format(date));
                    } else {
                        // Bitacora o por grupo
                        url = grupos.isEmpty()
                                ? "restapp/app/savelogbookot"
                                : "restapp/app/savelogbookotgroup";
                    }

                    if (isEmpty && UUIDTransaccion == null && (Version.check(this, 7)
                            && UserPermission.check(this, BITACORA_GPS))) {
                        hide();
                        Snackbar.make(getView(), R.string.requiere_ubicacion, Snackbar.LENGTH_LONG)
                                .show();
                        return;
                    }

                    if (falla != null) {
                        ArrayList<Falla.Request> fallas = new ArrayList<>();
                        fallas.add(falla);
                        datosOrdenTrabajo.setFallas(fallas);
                    }

                    value = datosOrdenTrabajo.toJson();
                    break;

                case OT_BITACORA:
                    if (!time.isValid()) {
                        contentCreationTime.setError(getString(R.string.ss_register_creationTime_empty));
                        return;
                    }

                    if (!end.isValid()) {
                        contentCreationEnd.setError(getString(R.string.ss_register_creationEnd_empty));
                        return;
                    }

                    fechaInicio = dateFormat.parse(date.getValue());
                    horaInicio = timeFormat.parse(time.getValue());
                    horaFin = timeFormat.parse(end.getValue());

                    if (fechaInicio.after(currentDate)) {
                        contentCreationDate.setError(getString(R.string.fecha_indicada_mayor_hora_actual));
                        return;
                    }

                    if (fechaInicio.equals(currentDate)) {
                        if (horaInicio.after(currentTime)) {
                            contentCreationTime.setError(getString(R.string.hora_indicada_mayor_hora_actual));
                            return;
                        }
                        if (horaFin.after(currentTime)) {
                            contentCreationEnd.setError(getString(R.string.hora_indicada_mayor_hora_actual));
                            return;
                        }
                    }

                    if (horaInicio.equals(horaFin)) {
                        contentCreationTime.setError(getString(R.string.hora_indicada_igual_hora_final));
                        return;
                    }
                    if (horaInicio.after(horaFin)) {
                        contentCreationTime.setError(getString(R.string.hora_indicada_mayor_hora_final));
                        return;
                    }

                    EditText rate_ot_bitacora = findViewById(R.id.execution);
                    if (rate_ot_bitacora.getText().toString().isEmpty()) {
                        contentExecution.setError(getString(R.string.ss_register_execution_empty));
                        contentExecution.requestFocus();
                        return;
                    }

                    try {
                        int ejecucion = Integer.parseInt(rate_ot_bitacora.getText().toString());
                        if (ejecucion > 100) {
                            rate_ot_bitacora.setText("100");
                        }
                    } catch (Exception ignored) {
                    }

                    show();

                    // OT Bitacora o por grupo
                    url = grupos.isEmpty()
                            ? "restapp/app/savelogbookotfast"
                            : "restapp/app/savelogbookotfastgroup";

                    accion = Transaccion.ACCION_REGISTRAR_OT_BITACORA;
                    AppCompatSpinner tipos = findViewById(R.id.tipos);

                    Long idAm = null;
                    if (activity != null) {
                        Spinner spinner = (Spinner) activity.getSelectedItem();
                        idAm = spinner.getKey() != null ? Long.valueOf(spinner.getKey()) : null;
                    }

                    BitacoraOT otbitacora = new BitacoraOT();
                    otbitacora.setToken(UUID.randomUUID().toString());
                    otbitacora.setEntity(((TextInputEditText) findViewById(R.id.register))
                            .getText().toString());
                    otbitacora.setDate(date.getValue());
                    otbitacora.setIdentity(id);
                    otbitacora.setTypeentity(tipoentidad);
                    otbitacora.setStateot((String) tipos.getSelectedItem());
                    otbitacora.setTimestart(time.getValue());
                    otbitacora.setTimeend(end.getValue());
                    otbitacora.setExecutionrate(rate_ot_bitacora.getText().toString());
                    otbitacora.setDescription(description.getText().toString());
                    otbitacora.setFiles(photos);
                    otbitacora.setGroup(grupos);
                    otbitacora.setRecursos(recursos == null ? new ArrayList<>() : recursos.getRecursos());
                    otbitacora.setPendientepmtto(pendienteMantenimiento);
                    otbitacora.setTipotiempo(idTipoTiempo);
                    otbitacora.setVariables(variables);
                    otbitacora.setIdam(idAm);
                    otbitacora.setTipoTiempoTexto(textoTipoTIempo);
                    otbitacora.setFechaAnterior(dateend);

                    if (Version.check(this, 18)) {
                        otbitacora.setParos(paros);
                    } else {
                        otbitacora.setTipoparo(generaParo);
                    }

                    if (Version.check(this, 19) && tareas != null && !tareas.isEmpty()) {
                        otbitacora.setTareas(tareas);
                    }

                    TextInputEditText horasHabilesOTBitacora = findViewById(R.id.horas_habiles);
                    if (horasHabilesOTBitacora != null) {
                        String valueOTBitacora = horasHabilesOTBitacora.getText().toString();
                        otbitacora.setHorashabilesdia(valueOTBitacora.isEmpty() ? null : Float.parseFloat(valueOTBitacora));
                    }

                    isEmpty = true;
                    if (Version.check(this, 7)) {

                        if (location != null) {
                            isEmpty = false;
                            Coordenada coordenada = new Coordenada();
                            coordenada.setLatitude(location.getLatitude());
                            coordenada.setAltitude(location.getAltitude());
                            coordenada.setAccuracy(location.getAccuracy());
                            coordenada.setLongitude(location.getLongitude());
                            otbitacora.setLocation(coordenada);
                        }
                    }

                    if (isEmpty && UUIDTransaccion == null && (Version.check(this, 7)
                            && UserPermission.check(this, BITACORA_GPS))) {
                        hide();
                        Snackbar.make(getView(), R.string.requiere_ubicacion, Snackbar.LENGTH_LONG)
                                .show();
                        return;
                    }

                    value = otbitacora.toJson();
                    break;

                default:
                    if (!time.isValid()) {
                        contentCreationTime.setError(getString(R.string.ss_register_creationTime_empty));
                        return;
                    }

                    // Valida la hora final
                    if (!end.isValid()) {
                        contentCreationEnd.setError(getString(R.string.ss_register_creationEnd_empty));
                        return;
                    }

                    fechaInicio = dateFormat.parse(date.getValue());
                    horaInicio = timeFormat.parse(time.getValue());
                    horaFin = timeFormat.parse(end.getValue());

                    if (fechaInicio.after(currentDate)) {
                        contentCreationDate.setError(getString(R.string.fecha_indicada_mayor_hora_actual));
                        return;
                    }

                    if (fechaInicio.equals(currentDate)) {
                        if (horaInicio.after(currentTime)) {
                            contentCreationTime.setError(getString(R.string.hora_indicada_mayor_hora_actual));
                            return;
                        }
                        if (horaFin.after(currentTime)) {
                            contentCreationEnd.setError(getString(R.string.hora_indicada_mayor_hora_actual));
                            return;
                        }
                    }

                    if (horaInicio.equals(horaFin)) {
                        contentCreationTime.setError(getString(R.string.hora_indicada_igual_hora_final));
                        return;
                    }
                    if (horaInicio.after(horaFin)) {
                        contentCreationTime.setError(getString(R.string.hora_indicada_mayor_hora_final));
                        return;
                    }

                    // Muestra el cargando
                    show();

                    // Bitacora evento o por grupo
                    url = grupos.isEmpty()
                            ? "restapp/app/savelogbookevent"
                            : "restapp/app/savelogbookeventgroup";

                    accion = Transaccion.ACCION_REGISTRAR_BITACORA_EVENTO;

                    // Obtiene los valores
                    AppCompatSpinner type = findViewById(R.id.type);
                    Long id = ((EventType) type.getSelectedItem()).getId();
                    String name = ((EventType) type.getSelectedItem()).getNombre();

                    // Registra la bitacora
                    BitacoraEvento datosEvento = new BitacoraEvento();
                    datosEvento.setDate(date.getValue());
                    datosEvento.setTypeevent(id);
                    datosEvento.setType(name);
                    datosEvento.setTimestart(time.getValue());
                    datosEvento.setTimeend(end.getValue());
                    datosEvento.setDescription(description.getText().toString());
                    datosEvento.setFiles(photos);
                    datosEvento.setGroup(grupos);
                    datosEvento.setRecursos(recursos == null ? new ArrayList<>() : recursos.getRecursos());
                    datosEvento.setPendientepmtto(pendienteMantenimiento);
                    datosEvento.setTipotiempo(idTipoTiempo);
                    datosEvento.setTipoTiempoTexto(textoTipoTIempo);
                    datosEvento.setFechaAnterior(dateend);

                    TextInputEditText horasHabilesEvento = findViewById(R.id.horas_habiles);
                    if (horasHabilesEvento != null) {
                        String valueEvento = horasHabilesEvento.getText().toString();
                        datosEvento.setHorashabilesdia(valueEvento.isEmpty() ? null : Float.parseFloat(valueEvento));
                    }

                    isEmpty = true;
                    if (Version.check(this, 7)) {
                        if (location != null) {
                            isEmpty = false;
                            Coordenada coordenada = new Coordenada();
                            coordenada.setLatitude(location.getLatitude());
                            coordenada.setAltitude(location.getAltitude());
                            coordenada.setAccuracy(location.getAccuracy());
                            coordenada.setLongitude(location.getLongitude());
                            datosEvento.setLocation(coordenada);
                        }
                    }

                    if (isEmpty && UUIDTransaccion == null && (Version.check(this, 7)
                            && UserPermission.check(this, BITACORA_GPS))) {
                        hide();
                        Snackbar.make(getView(), R.string.requiere_ubicacion, Snackbar.LENGTH_LONG)
                                .show();
                        return;
                    }

                    if (this.id != null && this.id != 0) {
                        datosEvento.setIdentity(this.id);
                        datosEvento.setEntity(((TextInputEditText) findViewById(R.id.register)).getText().toString());
                        datosEvento.setTypeentity(tipoentidad);
                    }

                    value = datosEvento.toJson();
                    break;
            }

            if (modoRecorrido) {
                if (obtenerFinalziacion && type == OT && UUIDTransaccion == null && UserPermission.check(this, MODULO_PANEL_GESTION_SERVICIO, false)) {
                    hide();
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                    alertDialogBuilder.setCancelable(true);
                    alertDialogBuilder.setNeutralButton("Cancelar", (dialog, id) -> dialog.dismiss());
                    alertDialogBuilder.setMessage(R.string.terminar_bitacora);

                    alertDialogBuilder.setPositiveButton(R.string.si, (dialog, which) -> {
                        register(validarGPS, confirm, false, true, disponible, false);
                        dialog.cancel();
                    });

                    alertDialogBuilder.setNegativeButton(R.string.no, (dialog, id) -> {
                        AlertDialog.Builder newAlertDialogBuilder = new AlertDialog.Builder(this);
                        newAlertDialogBuilder.setCancelable(true);
                        newAlertDialogBuilder.setNeutralButton("Cancelar", (temp, which) -> temp.dismiss());

                        newAlertDialogBuilder.setMessage("Desea quedar disponible?");
                        newAlertDialogBuilder.setPositiveButton(R.string.si, (temp, which) -> {
                            temp.cancel();
                            dialog.cancel();

                            register(validarGPS, confirm, false, false, true, false);
                        });

                        newAlertDialogBuilder.setNegativeButton(R.string.no, (temp, which) -> {
                            temp.cancel();
                            dialog.cancel();

                            register(validarGPS, confirm, false, false, false, true);
                        });

                        newAlertDialogBuilder.show();
                    });

                    alertDialogBuilder.show();
                    return;
                }
            }

            // Obtiene la transaccion si esta en modo de editar el registro
            Transaccion transaccion = null;
            if (UUIDTransaccion != null) {
                transaccion = database.where(Transaccion.class)
                        .equalTo("UUID", UUIDTransaccion)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();
            }

            if (transaccion == null) {
                transaccion = new Transaccion();
                transaccion.setUUID(UUID.randomUUID().toString());
                transaccion.setCuenta(cuenta);
                transaccion.setCreation(Calendar.getInstance().getTime());
                transaccion.setUrl(cuenta.getServidor().getUrl() + "/" + url);
                transaccion.setVersion(cuenta.getServidor().getVersion());
                transaccion.setValue(value);
                transaccion.setModulo(Transaccion.MODULO_BITACORA);
                transaccion.setAccion(accion);
                transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);
                transaccion.setIdentidad(id);

                showProgressDialog();

                TransaccionService transaccionService = new TransaccionService(this);
                compositeDisposable.add(transaccionService.save(transaccion)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(Functions.emptyConsumer(), error -> {
                            disponibleAux = false;
                            dismissProgressDialog();
                        }, () -> {
                            boolean actualizar = true;
                            if (recorridoService != null && disponibleAux && disponible) {
                                recorridoService.terminar(id, value);
                                actualizar = false;
                            }

                            if (disponibleAux && modoRecorrido) {
                                if (finalizar) {
                                    atNotificationService.finEjecucion(id);
                                }

                                if (disponible) {
                                    atNotificationService.disponible();
                                }

                                if (actualizar) {
                                    BitacoraContinua bitacoraContinua = bitacoraContinuaService.obtener(id);
                                    if (bitacoraContinua != null) {
                                        Gson gson = new Gson();
                                        BitacoraOrdenTrabajo bitacoraOrdenTrabajo
                                                = gson.fromJson(bitacoraContinua.getValue(), BitacoraOrdenTrabajo.class);

                                        if (bitacoraOrdenTrabajo != null) {
                                            Calendar now = Calendar.getInstance(TimeZone.getDefault());
                                            SimpleDateFormat simpleDateFormat
                                                    = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                            bitacoraOrdenTrabajo.setDate(simpleDateFormat.format(now.getTime()));

                                            SimpleDateFormat simpleTimeFormat
                                                    = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                            bitacoraOrdenTrabajo.setTimestart(simpleTimeFormat.format(now.getTime()));

                                            BitacoraContinua.Data data = new BitacoraContinua.Data();
                                            data.setCodigo(bitacoraContinua.getCodigo());
                                            data.setEstado(bitacoraContinua.getEstado());
                                            data.setValue(gson.toJson(bitacoraOrdenTrabajo));
                                            bitacoraContinuaService.actualizar(id, data);
                                        }
                                    }
                                }
                            }

                            success();
                            super.backActivity();

                            Notification.cancel(this, ATNotificationService.ID_NOTIFICATION);

                            if (type == OT && UUIDTransaccion == null && (UserPermission.check(this, VALIDAR_REGISTRO_BITACORA_OT, false) || UserPermission.check(this, MODULO_GESTION_SERVICIOS, false) || UserPermission.check(this, UserPermission.VALIDAR_QR_SITIO, false)) && finalizar) {
                                String code = ((TextInputEditText) findViewById(R.id.register))
                                        .getText().toString();

                                if (UserPermission.check(this, FINALIZACION_DIRECTA_OT_APP)) {
                                    finalizacionDirectaOT(id, code, cuenta);
                                } else {
                                    Bundle bundle = new Bundle();
                                    bundle.putLong(com.mantum.component.Mantum.KEY_ID, id);
                                    bundle.putString(TerminarOrdenTrabajoActivity.KEY_CODE, code);

                                    Intent intent = new Intent(this, TerminarOrdenTrabajoActivity.class);
                                    intent.putExtras(bundle);

                                    startActivityForResult(intent, TerminarOrdenTrabajoActivity.REQUEST_ACTION);
                                }
                            }

                            if (estadoSincronizado) {
                                recorridoService.actualizarEstadoSincronizado(id);
                            }

                            dismissProgressDialog();
                        }));

                if (type == OT && UUIDTransaccion == null && (UserPermission.check(this, VALIDAR_REGISTRO_BITACORA_OT, false) || UserPermission.check(this, MODULO_GESTION_SERVICIOS, false) || UserPermission.check(this, UserPermission.VALIDAR_QR_SITIO, false))) {
                    disponibleAux = true;
                }

            } else {
                final Transaccion finalTransaccion = transaccion;
                database.update(() -> {
                    finalTransaccion.setCreation(Calendar.getInstance().getTime());
                    finalTransaccion.setValue(value);
                    finalTransaccion.setMessage("");
                    finalTransaccion.setUrl(cuenta.getServidor().getUrl() + "/" + url);
                    finalTransaccion.setEstado(Transaccion.ESTADO_PENDIENTE);
                });

                dismissProgressDialog();
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "register: ", e);
            hide();
            dismissProgressDialog();
        }
    }

    public void success() {
        UltimoRegistroBitacora ultimoRegistroBitacora
                = (UltimoRegistroBitacora) database.findOne(new Where()
                .equalTo("cuenta.UUID", account), UltimoRegistroBitacora.class);

        Calendar start = Calendar.getInstance();
        String[] normalize = time.getValue().split(":");
        start.set(0, 0, 0, Integer.parseInt(normalize[0]), Integer.parseInt(normalize[1]));

        Calendar calendar = Calendar.getInstance();
        normalize = end.getValue().split(":");
        calendar.set(0, 0, 0, Integer.parseInt(normalize[0]), Integer.parseInt(normalize[1]));

        if (ultimoRegistroBitacora == null) {

            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("UUID", account)
                    .findFirst();

            UltimoRegistroBitacora temp = new UltimoRegistroBitacora();
            temp.setUUID(UUID.randomUUID().toString());
            temp.setCuenta(cuenta);
            temp.setFecha(date.getValue());
            temp.setHorainicial(start.getTime().getTime());
            temp.setHorafinal(calendar.getTime().getTime());
            database.insert(temp);
        } else {
            database.update(() -> {
                ultimoRegistroBitacora.setFecha(date.getValue());
                ultimoRegistroBitacora.setHorainicial(start.getTime().getTime());
                ultimoRegistroBitacora.setHorafinal(calendar.getTime().getTime());
            });
        }
    }

    private void finalizacionDirectaOT(Long id, String code, Cuenta cuenta) {
        Terminar terminar = new Terminar();
        terminar.setIdot(id);
        terminar.setCode(code);
        terminar.setFiles(new ArrayList<>());

        Transaccion transaccionFinishOT = new Transaccion();
        transaccionFinishOT.setUUID(UUID.randomUUID().toString());
        transaccionFinishOT.setCuenta(cuenta);
        transaccionFinishOT.setCreation(Calendar.getInstance().getTime());
        transaccionFinishOT.setUrl(cuenta.getServidor().getUrl() + "/restapp/app/finishot");
        transaccionFinishOT.setVersion(cuenta.getServidor().getVersion());
        transaccionFinishOT.setValue(terminar.toJson());
        transaccionFinishOT.setModulo(Transaccion.MODULO_ORDEN_TRABAJO);
        transaccionFinishOT.setAccion(Transaccion.ACCION_TERMINAR_ORDEN_TRABAJO);
        transaccionFinishOT.setEstado(Transaccion.ESTADO_PENDIENTE);
        transaccionFinishOT.setIdentidad(terminar.getIdot());

        TransaccionService transaccionService = new TransaccionService(this);
        compositeDisposable.add(transaccionService.save(transaccionFinishOT)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(self -> {
                        },
                        throwable -> Snackbar.make(getView(), R.string.terminar_error, Snackbar.LENGTH_LONG).show(),
                        () -> backActivity(getString(R.string.terminar_exitos))));
    }
}