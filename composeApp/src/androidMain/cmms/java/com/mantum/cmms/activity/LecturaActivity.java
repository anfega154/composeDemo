package com.mantum.cmms.activity;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mantum.R;
import com.mantum.cmms.adapter.LecturaAdapter;
import com.mantum.cmms.entity.Actividad;
import com.mantum.cmms.entity.Busqueda;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Entidad;
import com.mantum.cmms.entity.Equipo;
import com.mantum.cmms.entity.InstalacionLocativa;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.entity.Variable;
import com.mantum.cmms.entity.VariableCualitativa;
import com.mantum.cmms.domain.Lecturas;
import com.mantum.cmms.service.TransaccionService;
import com.mantum.component.Mantum;
import com.mantum.component.component.DatePicker;
import com.mantum.component.component.TimePicker;
import com.mantum.cmms.database.Database;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.internal.functions.Functions;

public class LecturaActivity extends Mantum.Activity {

    private static final String TAG = LecturaActivity.class.getSimpleName();

    public static final String KEY_TYPE = "type";

    public static final String HIDE_ACTION = "hide_action";

    public static final String JSON_RESPONSE = "lectura_json_response";

    public static final String ID_AM = "id_am";

    public static final String KEY_TYPE_ACTION = "key_type_action";

    public static final String KEY_VARIABLES = "key_variables";

    private Cuenta cuenta;

    private Database database;

    private RecyclerView recyclerView;

    private TransaccionService transaccionService;

    private List<Variable> variables = new ArrayList<>();

    private List<Variable> variablesProcesadas = new ArrayList<>();

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_lectura);

            variables = new ArrayList<>();
            database = new Database(this);
            transaccionService = new TransaccionService(this);
            includeBackButtonAndTitle(getString(R.string.registrar_lectura));

            Long id = null;
            Long idAm = null;
            String type = null;
            String typeAction = null;

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                id = bundle.getLong(Mantum.KEY_ID);
                type = bundle.getString(KEY_TYPE);
                idAm = bundle.getLong(ID_AM);
                typeAction = bundle.getString(KEY_TYPE_ACTION);
                String editado = bundle.getString(KEY_VARIABLES, null);
                if (editado != null) {
                    try {
                        Type variableType = new TypeToken<ArrayList<Variable>>() {
                        }.getType();
                        variablesProcesadas = new Gson().fromJson(editado, variableType);
                    } catch (Exception e) {
                        variablesProcesadas = new ArrayList<>();
                    }
                }
            }

            if (id == null || type == null) {
                backActivity(getString(R.string.search_entity));
                return;
            }

            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

            DatePicker datePicker = new DatePicker(this, R.id.expected_date);
            datePicker.load();
            datePicker.setEnabled(true);
            datePicker.setValue(calendar);

            TimePicker timePicker = new TimePicker(this, R.id.expected_time);
            timePicker.load();
            timePicker.setEnabled(true);
            timePicker.setValue(calendar);

            boolean mostrarDescripcion;
            EditText entity = findViewById(R.id.entity);
            if (OrdenTrabajo.SELF.equals(typeAction)) {
                mostrarDescripcion = false;
                OrdenTrabajo ordenTrabajo = database.where(OrdenTrabajo.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .equalTo("id", id)
                        .findFirst();

                if (ordenTrabajo == null || ordenTrabajo.getVariables().isEmpty()) {
                    backActivity(getString(R.string.variable_empty));
                    return;
                }

                List<Entidad> entidades = new ArrayList<>();
                for (Entidad entidad : ordenTrabajo.getEntidades()) {
                    for (Actividad actividad : entidad.getActividades()) {
                        if (actividad.getId().equals(idAm)) {
                            entidades.add(entidad);
                        }
                    }
                }

                List<Variable> temporal = ordenTrabajo.getVariables().isManaged()
                        ? database.copyFromRealm(ordenTrabajo.getVariables())
                        : ordenTrabajo.getVariables();

                for (Variable variable : temporal) {
                    for (Entidad entidad : entidades) {
                        if (entidad.getId().equals(variable.getIdentidad()) && entidad.getTipo().equals(variable.getTipoentidad())) {
                            variables.add(variable);
                        }
                    }
                }

                if (variables.isEmpty()) {
                    backActivity(getString(R.string.variable_empty));
                    return;
                }

                for (Variable variablesProcesada : variablesProcesadas) {
                    for (Variable variable : variables) {
                        if (variablesProcesada.getId().equals(variable.getId())) {
                            variable.setValor(variablesProcesada.getValor());
                            variable.setDescripcion(variablesProcesada.getDescripcion());
                        }
                    }
                }

                entity.setHint(getString(R.string.codigo));
                entity.setText(ordenTrabajo.getCodigo());
            } else if (Equipo.SELF.equals(typeAction)) {
                mostrarDescripcion = true;
                Equipo equipo = database.where(Equipo.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .equalTo("id", id)
                        .findFirst();

                if (equipo == null || equipo.getVariables().isEmpty()) {
                    backActivity(getString(R.string.variable_empty));
                    return;
                }

                variables = equipo.getVariables().isManaged()
                        ? database.copyFromRealm(equipo.getVariables())
                        : equipo.getVariables();
                entity.setText(equipo.getNombreMostrar());
            } else if (InstalacionLocativa.SELF.equals(typeAction)) {
                mostrarDescripcion = true;
                InstalacionLocativa instalacionLocativa = database.where(InstalacionLocativa.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .equalTo("id", id)
                        .findFirst();

                if (instalacionLocativa == null || instalacionLocativa.getVariables().isEmpty()) {
                    backActivity(getString(R.string.variable_empty));
                    return;
                }

                variables = instalacionLocativa.getVariables().isManaged()
                        ? database.copyFromRealm(instalacionLocativa.getVariables())
                        : instalacionLocativa.getVariables();
                entity.setText(instalacionLocativa.getNombreMostrar());
            } else if ("OT_Bitacora".equals(typeAction)) {
                mostrarDescripcion = false;
                Busqueda busqueda = database.where(Busqueda.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .equalTo("type", type)
                        .equalTo("id", id)
                        .findFirst();

                if (busqueda == null || busqueda.getActividades().isEmpty()) {

                    if (InstalacionLocativa.SELF.equals(type)) {
                        InstalacionLocativa instalacionLocativa = database.where(InstalacionLocativa.class)
                                .equalTo("cuenta.UUID", cuenta.getUUID())
                                .equalTo("id", id)
                                .findFirst();


                        if (instalacionLocativa != null && !instalacionLocativa.getActividades().isEmpty()) {

                            List<Actividad> actividades = instalacionLocativa.getActividades().isManaged()
                                    ? database.copyFromRealm(instalacionLocativa.getActividades())
                                    : instalacionLocativa.getActividades();

                            for (Actividad actividad : actividades) {
                                if (idAm.equals(actividad.getId())) {
                                    variables = actividad.getVariables().isManaged()
                                            ? database.copyFromRealm(actividad.getVariables())
                                            :actividad.getVariables();
                                    break;
                                }
                            }

                            if (variables.isEmpty()) {
                                backActivity(getString(R.string.variable_empty));
                                return;
                            }

                            entity.setText(instalacionLocativa.getNombreMostrar());
                        }

                    } else if (Equipo.SELF.equals(type)) {
                        Equipo equipo = database.where(Equipo.class)
                                .equalTo("cuenta.UUID", cuenta.getUUID())
                                .equalTo("id", id)
                                .findFirst();

                        if (equipo != null && !equipo.getActividades().isEmpty()) {

                            List<Actividad> actividades = equipo.getActividades().isManaged()
                                    ? database.copyFromRealm(equipo.getActividades())
                                    : equipo.getActividades();

                            for (Actividad actividad : actividades) {
                                if (idAm.equals(actividad.getId())) {
                                    variables = actividad.getVariables().isManaged()
                                            ? database.copyFromRealm(actividad.getVariables())
                                            :actividad.getVariables();
                                    break;
                                }
                            }

                            if (variables.isEmpty()) {
                                backActivity(getString(R.string.variable_empty));
                                return;
                            }

                            entity.setText(equipo.getNombreMostrar());
                        }

                    } else {
                        backActivity(getString(R.string.variable_empty));
                        return;
                    }


                } else {
                    for (Actividad actividad : busqueda.getActividades()) {
                        if (idAm.equals(actividad.getId())) {
                            variables = actividad.getVariables().isManaged()
                                    ? database.copyFromRealm(actividad.getVariables())
                                    : actividad.getVariables();
                            break;
                        }
                    }

                    if (variables.isEmpty()) {
                        backActivity(getString(R.string.variable_empty));
                        return;
                    }

                    entity.setText(busqueda.getNameView());
                }

                for (Variable variablesProcesada : variablesProcesadas) {
                    for (Variable variable : variables) {
                        if (variablesProcesada.getId().equals(variable.getId())) {
                            variable.setValor(variablesProcesada.getValor());
                            variable.setDescripcion(variablesProcesada.getDescripcion());
                        }
                    }
                }

            } else {
                mostrarDescripcion = true;
                Busqueda busqueda = database.where(Busqueda.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .equalTo("type", type)
                        .equalTo("id", id)
                        .findFirst();

                if (busqueda == null || busqueda.getVariables().isEmpty()) {
                    backActivity(getString(R.string.variable_empty));
                    return;
                }

                variables = busqueda.getVariables().isManaged()
                        ? database.copyFromRealm(busqueda.getVariables())
                        : busqueda.getVariables();
                entity.setText(busqueda.getNameView());
            }

            if (!type.equals("GrupoVariable")) {
                Collections.sort(variables, (o1, o2) -> o1.getNombre().compareTo(o2.getNombre()));
            }

            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            LecturaAdapter adapter = new LecturaAdapter(this, mostrarDescripcion);
            adapter.addAll(variables);
            recyclerView = adapter.startAdapter(getView(), layoutManager);
        } catch (Exception e) {
            Log.e(TAG, "onCreate: ", e);
            backActivity(getString(R.string.error_app));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
        compositeDisposable.clear();
    }

    @Override
    public void onBackPressed() {
        List<Lecturas> lecturas = this.procesar();

        Bundle bundle = new Bundle();
        bundle.putString(LecturaActivity.JSON_RESPONSE, new Gson().toJson(lecturas));

        Intent intent = new Intent();
        intent.putExtras(bundle);

        backActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_formulario, menu);
        MenuItem action = menu.findItem(R.id.action_done);
        if (action != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                boolean visible = bundle.getBoolean(HIDE_ACTION, false);
                action.setVisible(!visible);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (menuItem.getItemId() == R.id.action_done) {
            this.register();
            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    private List<Lecturas> procesar() {
        List<Lecturas> lecturas = new ArrayList<>();

        EditText date = this.findViewById(R.id.expected_date);
        EditText time = this.findViewById(R.id.expected_time);
        for (int i = 0; i < variables.size(); i++) {
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager != null) {
                View view = layoutManager.findViewByPosition(i);
                if (view != null) {
                    EditText id = view.findViewById(R.id.id);
                    EditText nombre = view.findViewById(R.id.nombre);
                    EditText value = view.findViewById(R.id.value);
                    EditText tipo = view.findViewById(R.id.tipo);
                    EditText description = view.findViewById(R.id.description);

                    String valor = value.getText().toString();
                    if (tipo.getText().toString().equals(VariableCualitativa.NAME)) {
                        AppCompatSpinner rango = view.findViewById(R.id.rango);
                        valor = ((VariableCualitativa) rango.getSelectedItem()).getValor();
                    }

                    if (valor != null && !valor.isEmpty() && !valor.equals(this.getString(R.string.seleccione_opcion))) {
                        Lecturas model = new Lecturas(Long.valueOf(id.getText().toString()),
                                date.getText() + " " + time.getText(),
                                nombre.getText().toString(), valor,
                                description.getText().toString());
                        lecturas.add(model);
                    }
                }
            }
        }

        return lecturas;
    }

    private void register() {
        try {
            closeKeyboard();

            List<Lecturas> lecturas = this.procesar();
            if (lecturas.isEmpty()) {
                Snackbar.make(getView(), R.string.lectura_empty, Snackbar.LENGTH_LONG).show();
                return;
            }

            String url = cuenta.getServidor().getUrl() + "/restapp/app/registerlec";

            Transaccion transaccion = new Transaccion();
            transaccion.setUUID(UUID.randomUUID().toString());
            transaccion.setCuenta(cuenta);
            transaccion.setCreation(Calendar.getInstance().getTime());
            transaccion.setUrl(url);
            transaccion.setVersion("000");
            transaccion.setValue(new Gson().toJson(lecturas));
            transaccion.setModulo(Transaccion.MODULO_VARIABLES);
            transaccion.setAccion(Transaccion.ACCION_REGISTRAR_LECTURAS_VARIABLES);
            transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);

            compositeDisposable.add(transaccionService.save(transaccion)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(Functions.emptyConsumer(), this::onError, this::onComplete));

        } catch (Exception e) {
            Log.e(TAG, "register: ", e);
        }
    }

    private void onComplete() {
        Snackbar.make(getView(), R.string.lectura_exitoso, Snackbar.LENGTH_LONG).show();
        clean();
    }

    private void onError(Throwable throwable) {
        Snackbar.make(getView(), R.string.lectura_error, Snackbar.LENGTH_LONG)
                .show();
    }

    private void clean() {
        for (int i = 0; i < variables.size(); i++) {
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager != null) {
                View view = layoutManager.findViewByPosition(i);
                if (view != null) {
                    // Rangos de la variable cualitativa
                    AppCompatSpinner rango = view.findViewById(R.id.rango);
                    rango.setSelection(0);

                    // Limpia los valores ingresados
                    EditText value = view.findViewById(R.id.value);
                    value.setText(null);

                    // Limpia las descripciones ingresadas
                    EditText description = view.findViewById(R.id.description);
                    description.setText(null);
                }
            }
        }
    }
}