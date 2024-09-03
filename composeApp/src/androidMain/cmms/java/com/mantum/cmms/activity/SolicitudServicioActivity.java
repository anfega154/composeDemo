package com.mantum.cmms.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatSpinner;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.mantum.R;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.entity.parameter.Area;
import com.mantum.cmms.entity.parameter.Priorities;
import com.mantum.cmms.entity.parameter.SS;
import com.mantum.cmms.entity.parameter.TypeArea;
import com.mantum.cmms.entity.parameter.Types;
import com.mantum.cmms.domain.SolicitudServicioRegistrar;
import com.mantum.cmms.service.temporal.SolicitudServicioService;
import com.mantum.cmms.util.BackEditTransaction;
import com.mantum.cmms.util.Message;
import com.mantum.component.service.Photo;
import com.mantum.core.Mantum;
import com.mantum.core.component.DatePicker;
import com.mantum.core.component.PickerAbstract;
import com.mantum.core.component.TimePicker;
import com.mantum.core.event.OnOffline;
import com.mantum.core.service.Permission;
import com.mantum.core.util.Assert;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.database.Where;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

@Deprecated
public class SolicitudServicioActivity extends Mantum.Activity
        implements DatePicker.Callback, TimePicker.Callback, OnOffline {

    private final static String TAG = SolicitudServicioActivity.class.getSimpleName();

    public static final String KEY_ID = "id";

    public static final String KEY_NAME = "name";

    public static final String KEY_TYPE = "type";

    public static final String UUID_TRANSACCION = "UUID";

    public static final String MODE_EDIT = "edit";

    //region Variables

    //region Entidad

    private String UUIDTransaccion; // Identificacion de la transaccion

    private Long idEntidad;

    private String tipoEntidad;

    private String nombreEntidad;

    //endregion

    private List<Photo> photos;

    private SolicitudServicioService register;

    private AppCompatSpinner type;

    private AppCompatSpinner area;

    private AppCompatSpinner priority;

    private EditText description;

    private DatePicker date;

    private TimePicker time;

    private Database database;

    private String account;

    private String url;

    private Cuenta cuenta;

    //endregion

    //region Estados

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            this.setContentView(R.layout.activity_register_ss);

            SharedPreferences sharedPreferences
                    = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            account = sharedPreferences.getString(getString(R.string.mantum_account), null);
            url = sharedPreferences.getString(getString(R.string.mantum_url), null);

            // Inicializa la conexion a la base de datos
            photos = new ArrayList<>();
            database = new Database(this);
            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            // Verifica el permiso
            Permission.storage(this);

            // Obtiene el id y el tipo de entidad seleccionada
            this.idEntidad = null;
            this.tipoEntidad = null;
            this.nombreEntidad = null;
            Bundle bundle = this.getIntent().getExtras();
            if (bundle != null) {
                this.idEntidad = bundle.getLong(KEY_ID);
                this.nombreEntidad = bundle.getString(KEY_NAME);
                this.tipoEntidad = bundle.getString(KEY_TYPE);
            }

            // Configura la vista actual
            this.type = findViewById(R.id.type);
            this.area = findViewById(R.id.areas);
            this.priority = findViewById(R.id.priority);
            this.description = findViewById(R.id.description);

            // Conecta las acciones de la aplicación
            this.includeBackButtonAndTitle(R.string.registrar_solicitud_servicio);

            // Prepara el componente de carga
            progressPrepare(getString(R.string.solicitud_servicio_registrar_carga_titulo), getString(R.string.solicitud_servicio_registrar_carga_mensaje));

            // Inicializa el componente de calendario
            Where where = new Where().equalTo("cuenta.UUID", account);
            SS ss = (SS) database.findOne(where, SS.class);

            // Inicializa el servicio de registro de S.S.
            this.register = new SolicitudServicioService.Builder(this)
                    .endPoint("restapp/app/createss")
                    .authenticate(true)
                    .version(cuenta.getServidor().getVersion())
                    .url(url)
                    .onOffline(this, true)
                    .callback(this)
                    .build();

            // Agrega el evento para tomar la foto
            final SolicitudServicioActivity self = this;
            FloatingActionButton floatingActionButton = (FloatingActionButton) this.findViewById(R.id.camera);
            if (!Assert.isNull(floatingActionButton)) {
                floatingActionButton.setOnClickListener(v -> {
                    Bundle bundle1 = new Bundle();
                    bundle1.putStringArrayList(GaleriaActivity.PATH_FILE, Photo.paths(photos));
                    self.startActivity(GaleriaActivity.class, bundle1);
                });
            }

            // Inicializa los tipos de la S.S.
            if (!Assert.isNull(this.type)) {
                ArrayAdapter<Types> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_dropdown_item, ss.getTypes());
                this.type.setAdapter(adapter);
            }

            // Inicializa la prioridad de la S.S.
            if (!Assert.isNull(this.priority)) {
                ArrayAdapter<Priorities> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_dropdown_item, ss.getPriorities());
                this.priority.setAdapter(adapter);
            }

            // Modo editar
            Long idarea = null;
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            if (!Assert.isNull(bundle)) {
                String JSON = bundle.getString(MODE_EDIT);
                this.UUIDTransaccion = bundle.getString(UUID_TRANSACCION);
                if (!Assert.isNull(JSON)) {
                    Gson gson = new Gson();
                    SolicitudServicioRegistrar register = gson.fromJson(JSON, SolicitudServicioRegistrar.class);

                    idarea = register.getIdArea();
                    this.type.setSelection(this.buscarTipo(ss, register));
                    this.priority.setSelection(this.buscarPrioridad(ss, register));

                    this.idEntidad = register.getIdEntidad();
                    this.nombreEntidad = register.getNombreEntidad();
                    this.tipoEntidad = register.getTipoEntidad();
                    this.description.setText(register.getDescripcion());
                    this.photos.addAll(register.getFiles());

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    calendar.setTime(formatter.parse(register.getFechaInicio() + " " + register.getHoraInicio()));
                }
            }

            if (idEntidad != null) {
                TextView information = findViewById(R.id.information);
                information.setVisibility(View.GONE);
            }

            // Inicializa las areas de la S.S.
            if (!Assert.isNull(this.area)) {
                // Se obtiene las areas de la base de datos

                List<Area> areas = this.database.instance().where(Area.class)
                        .equalTo("cuenta.UUID", account)
                        .findAll();

                if (!areas.isEmpty()) {
                    // Muestra el componente
                    this.area.setVisibility(View.VISIBLE);

                    // Se agrega la ayuda al selector
                    final Area help = new Area();
                    help.setValue(0L);
                    help.setLabel(this.getString(R.string.ss_register_spinner_area));

                    ArrayList<Area> temporal = new ArrayList<>();
                    temporal.add(0, help);
                    if (this.idEntidad == null) {
                        for (Area area : areas) {
                            boolean noRequiereEntidad = false;
                            for (TypeArea typeArea : area.getTypes()) {
                                if (!typeArea.isEntityrequired()) {
                                    noRequiereEntidad = true;
                                    break;
                                }
                            }
                            if (noRequiereEntidad) temporal.add(area);
                        }
                    } else {
                        temporal.addAll(areas);
                    }

                    if (temporal.size() == 1 && ss.isArearequired()) {
                        new AlertDialog.Builder(this)
                                .setMessage(getString(R.string.mensaje_registro_ss_denegado))
                                .setCancelable(false)
                                .setNegativeButton("Cerrar", ((dialog, which) -> backActivity()))
                                .show();
                    }

                    // Agrega los elementos del selector
                    ArrayAdapter<Area> adapter = new ArrayAdapter<>(
                            this, android.R.layout.simple_spinner_dropdown_item, temporal);
                    this.area.setAdapter(adapter);

                    if (!Assert.isNull(idarea)) {
                        this.area.setSelection(this.buscarArea(areas, idarea));
                    }

                    // Agrega el evento al selector
                    this.area.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (position == 0) {
                                Where where = new Where().equalTo("cuenta.UUID", account);
                                SS ss = (SS) self.database.findOne(where, SS.class);
                                List<Types> tipos = new ArrayList<>(ss.getTypes());
                                if (idEntidad == null) {
                                    tipos.clear();
                                    for (Types types : ss.getTypes()) {
                                        if (!types.isEntityrequired()) {
                                            tipos.add(types);
                                        }
                                    }
                                }
                                ArrayAdapter<Types> adapter = new ArrayAdapter<>(
                                        self, android.R.layout.simple_spinner_dropdown_item, tipos);
                                self.type.setAdapter(adapter);
                            } else {
                                Area selected = temporal.get(position);
                                List<TypeArea> tipos = new ArrayList<>(selected.getTypes());
                                if (idEntidad == null) {
                                    tipos.clear();
                                    for (TypeArea typeArea : selected.getTypes()) {
                                        if (!typeArea.isEntityrequired()) {
                                            tipos.add(typeArea);
                                        }
                                    }
                                }

                                if (tipos.isEmpty()) {
                                    List<TypeArea> temporal = new ArrayList<>();
                                    TypeArea type = new TypeArea();
                                    type.setLabel(self.getString(R.string.ss_register_spinner_type));
                                    type.setValue(null);
                                    temporal.add(type);
                                    tipos = temporal; // Se agrega para omitir la transaccion de realm
                                }

                                ArrayAdapter<TypeArea> adapter = new ArrayAdapter<>(
                                        self, android.R.layout.simple_spinner_dropdown_item, tipos);
                                self.type.setAdapter(adapter);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });

                    // Carga los tipos del area
                    Area results = areas.get(0);
                    if (results != null) {
                        List<TypeArea> types = results.getTypes();
                        if (!Assert.isNull(types) && !types.isEmpty()) {
                            this.type.setAdapter(new ArrayAdapter<>(
                                    this, android.R.layout.simple_spinner_dropdown_item, types));
                        }
                    }
                }
            }

            // Agrega el nombre de la entidad seleccionada
            EditText nombre = (EditText) this.findViewById(R.id.entity);
            if (!Assert.isNull(nombre) && !Assert.isNull(this.nombreEntidad)) {
                nombre.setVisibility(View.VISIBLE);
                nombre.setText(this.nombreEntidad);
            }

            boolean modify = ss.isModifycreatedate();
            this.date = new DatePicker.Builder(this)
                    .id(R.id.creation_date)
                    .calendar(calendar)
                    .callback(this)
                    .enabled(modify)
                    .build();

            this.time = new TimePicker.Builder(this)
                    .id(R.id.creation_time)
                    .calendar(calendar)
                    .callback(this)
                    .enabled(modify)
                    .build();

        } catch (Exception e) {
            this.backActivity(R.string.error_app);
        }
    }

    /**
     * Busca la posicion del area
     */
    private int buscarArea(List<Area> areas, Long idarea) {
        int index = 0;
        for (Area temporal : areas) {
            if (temporal.getValue().equals(idarea)) {
                break;
            }
            index = index + 1;
        }
        return index;
    }

    private int buscarTipo(SS ss, SolicitudServicioRegistrar register) {
        int index = 0;
        for (Types types : ss.getTypes()) {
            if (types.getValue().equals(register.getIdTipo())) {
                break;
            }
            index = index + 1;
        }
        return index;
    }

    /**
     * Busca la posicion de la prioridad en el array de prioridades
     */
    private int buscarPrioridad(SS ss, SolicitudServicioRegistrar register) {
        int index = 0;
        for (Priorities priorities : ss.getPriorities()) {
            if (priorities.getValue().equals(register.getPrioridad())) {
                break;
            }
            index = index + 1;
        }
        return index;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.showMessage(data);
        if (resultCode == RESULT_OK && !Assert.isNull(data)) {
            Bundle bundle = data.getExtras();
            if (!Assert.isNull(bundle)) {
                ArrayList<String> files = bundle.getStringArrayList(GaleriaActivity.PATH_FILE);
                if (!Assert.isNull(files)) {
                    photos.clear();
                    for (String file : files) {
                        photos.add(new Photo(this, new File(file)));
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_formulario, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
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

    //endregion

    //region Eventos

    @Override
    public void dateSet(View view, int year, int month, int day) {
        ((EditText) view).setText(new StringBuilder()
                .append(year).append("-")
                .append(PickerAbstract.normalize(month)).append("-")
                .append(PickerAbstract.normalize(day)));
    }

    @Override
    public void timeSet(View view, int hourOfDay, int minute) {
        ((EditText) view).setText(new StringBuilder()
                .append(PickerAbstract.normalize(hourOfDay))
                .append(":").append(PickerAbstract.normalize(minute)));
    }

    @Override
    public void success(final Mantum.Success success, boolean offline) {
        final SolicitudServicioActivity self = this;
        this.runOnUiThread(() -> {
            self.hide();
            self.backActivity(success.getMessage());
        });
    }

    //endregion

    //region API Privada

    /**
     * Registra una nueva solicitud de servicio
     */
    private void register() {
        try {
            // Cierra el teclado para facilitar la lectura de los mensajes
            this.closeKeyboard();

            Long type;
            String typeLabel;
            boolean entidad;
            if (this.type.getSelectedItem() instanceof TypeArea) {
                TypeArea temporal = (TypeArea) this.type.getSelectedItem();
                entidad = temporal.isEntityrequired();
                type = temporal.getValue();
                typeLabel = temporal.getLabel();
                if (this.getString(R.string.ss_register_spinner_type).equals(temporal.getLabel())) {
                    Message.snackbar(this, this.getString(R.string.tipo_requerida));
                    return;
                }
            } else {
                Types temporal = ((Types) this.type.getSelectedItem());
                entidad = temporal.isEntityrequired();
                type = temporal.getValue();
                typeLabel = temporal.getLabel();
            }

            // Valida si la entidad es necesaria, segun su tipo
            if (entidad && Assert.isNull(this.idEntidad)) {
                Message.snackbar(this, this.getString(R.string.entidad_requerida));
                return;
            }

            // Obtiene los valores del formulario
            Long area = null;
            String areaLabel = "";
            if (!Assert.isNull(this.area) && !Assert.isNull(this.area.getSelectedItem())) {
                area = ((Area) this.area.getSelectedItem()).getValue();
                areaLabel = ((Area) this.area.getSelectedItem()).getLabel();
            }

            // Valida la descripción
            String description = this.description.getText().toString();
            String priority = ((Priorities) this.priority.getSelectedItem()).getValue();
            if (description.isEmpty()) {
                this.description.setError(this.getString(R.string.ss_register_description_empty));
                this.description.requestFocus();
                return;
            }

            // Valida el área
            Where where = new Where().equalTo("cuenta.UUID", account);
            SS ss = (SS) this.database.findOne(where, SS.class);
            if (ss.isArearequired() && area == 0) {
                Message.snackbar(this, this.getString(R.string.ss_register_area_empty));
                return;
            }

            // Muestra el componente de carga
            this.show();

            // Registra una nueva solicitud de servicio
            SolicitudServicioRegistrar solicitudServicioRegistrar = new SolicitudServicioRegistrar();
            solicitudServicioRegistrar.setIdEntidad(idEntidad);
            solicitudServicioRegistrar.setTipoEntidad(tipoEntidad);
            solicitudServicioRegistrar.setNombreEntidad(nombreEntidad);
            solicitudServicioRegistrar.setPrioridad(priority);
            solicitudServicioRegistrar.setIdTipo(type);
            solicitudServicioRegistrar.setTipo(typeLabel);
            solicitudServicioRegistrar.setFechaInicio(this.date.value());
            solicitudServicioRegistrar.setHoraInicio(this.time.value());
            solicitudServicioRegistrar.setIdArea(area);
            solicitudServicioRegistrar.setArea(areaLabel);
            solicitudServicioRegistrar.setFiles(photos);
            solicitudServicioRegistrar.setDescripcion(description);

            this.register.register(solicitudServicioRegistrar);

        } catch (Exception e) {
            this.hide();
        }
    }

    @Override
    public Mantum.Response onRequest(String... params) {
        try {
            final String value = params[0];
            String url = params[1];

            // Obtiene la transaccion si esta en modo de editar el registro
            Transaccion transaccion = null;
            if (!Assert.isNull(this.UUIDTransaccion)) {
                Where where = new Where()
                        .equalTo("UUID", this.UUIDTransaccion)
                        .equalTo("cuenta.UUID", cuenta.getUUID());
                transaccion = (Transaccion) this.database.findOne(where, Transaccion.class);
            }

            // Agrega el registro de la solicitud de servicio
            if (Assert.isNull(transaccion)) {
                transaccion = new Transaccion();
                transaccion.setUUID(UUID.randomUUID().toString());
                transaccion.setCuenta(cuenta);
                transaccion.setCreation(Calendar.getInstance().getTime());
                transaccion.setUrl(url + "/restapp/app/createss");
                transaccion.setVersion(cuenta.getServidor().getVersion());
                transaccion.setValue(value);
                transaccion.setModulo(Transaccion.MODULO_SOLICITUD_SERVICIO);
                transaccion.setAccion(Transaccion.ACCION_CREAR_SOLICITUD_SERVICIO);
                transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);
                this.database.insert(transaccion);
            } else {
                final Transaccion finalTransaccion = transaccion;
                this.database.update(() -> {
                    finalTransaccion.setCreation(Calendar.getInstance().getTime());
                    finalTransaccion.setValue(value);
                    finalTransaccion.setUrl(url + "/restapp/app/createss");
                    finalTransaccion.setMessage("");
                    finalTransaccion.setEstado(Transaccion.ESTADO_PENDIENTE);
                });
            }

            Mantum.Success success = new Mantum.Success();
            success.ok(true);
            success.message(this.getString(R.string.registrar_solicitud_servicio_exitoso));
            return success;
        } catch (Exception e) {
            Mantum.Error error = new Mantum.Error();
            error.ok(false);
            error.message(this.getString(R.string.registrar_solicitud_servicio_error));
            return error;
        }
    }

    //endregion
}