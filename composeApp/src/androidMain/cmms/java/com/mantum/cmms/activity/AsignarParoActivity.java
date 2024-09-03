package com.mantum.cmms.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mantum.demo.R;
import com.mantum.cmms.adapter.ParoAdapter;
import com.mantum.cmms.adapter.onValueChange.ParoOnValueChange;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Paro;
import com.mantum.cmms.service.ParoService;
import com.mantum.component.Mantum;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class AsignarParoActivity extends Mantum.Activity {

    private static final String TAG = AsignarParoActivity.class.getSimpleName();

    public static final String KEY_FORM = "key_paros_form";

    public static final String FECHA = "fecha_inicio_bitacora";

    public static final String HORA_INICIO = "hora_inicio_bitacora";

    public static final String HORA_FIN = "hora_fin_bitacora";

    public static final String ID_EQUIPO_PARO = "id_equipo_paro";

    public static final String ID_AM_PARO = "id_am_paro";

    private Database database;

    private ParoAdapter<Paro.ParoHelper> listadoParoAdapter;

    private String horaInicioBitacora;

    private String horaFinBitacora;

    private EditText fechaEditText;

    private final SparseArray<Paro.ParoHelper> sparseArrayParos = new SparseArray<>();

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private ParoService paroService;

    private long idEquipo;

    private String idAm;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_asignar_paro);
            includeBackButtonAndTitle(R.string.asignar_paros_titulo);

            database = new Database(this);
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            paroService = new ParoService(this, cuenta);

            TextView btnAgregarParo = findViewById(R.id.agregar_paro);
            btnAgregarParo.setOnClickListener(view -> {
                listadoParoAdapter.add(new Paro.ParoHelper(horaInicioBitacora, horaFinBitacora));
                listadoParoAdapter.notifyItemInserted(listadoParoAdapter.getItemCount());
            });

            listadoParoAdapter = new ParoAdapter<>(this);
            RecyclerView recyclerViewParos = findViewById(R.id.recycler_view_paros);
            recyclerViewParos.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewParos.setAdapter(listadoParoAdapter);

            listadoParoAdapter.setOnAction(new ParoOnValueChange() {
                @Override
                public void onClick(int position) {
                    if (position >= 0) {
                        listadoParoAdapter.remove(position);
                        listadoParoAdapter.notifyItemRemoved(position);
                    }
                }

                @Override
                public void onTimeStartChange(String value, int position) {
                    listadoParoAdapter.getItemPosition(position).setHoraInicio(value);
                }

                @Override
                public void onTimeEndChange(String value, int position) {
                    listadoParoAdapter.getItemPosition(position).setHoraFin(value);
                }

                @Override
                public void onClasificationChange(String value, int position) {
                    listadoParoAdapter.getItemPosition(position).setClasificacion(value);
                }

                @Override
                public void onTypeChange(Long value, int position) {
                    listadoParoAdapter.getItemPosition(position).setTipo(value);
                }
            });

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                horaInicioBitacora = bundle.getString(HORA_INICIO);
                horaFinBitacora = bundle.getString(HORA_FIN);

                fechaEditText = findViewById(R.id.fecha);
                fechaEditText.setText(bundle.getString(FECHA));

                SparseArray<Paro.ParoHelper> paroSparseArray = bundle.getSparseParcelableArray(KEY_FORM);
                if (paroSparseArray != null) {
                    for (int i = 0; i < paroSparseArray.size(); i++) {
                        listadoParoAdapter.add(paroSparseArray.get(i));
                    }
                }

                idEquipo = bundle.getLong(Mantum.KEY_ID);
                idAm = bundle.getString(ID_AM_PARO);

                ExtendedFloatingActionButton verHistorico = findViewById(R.id.ver_historico);
                verHistorico.setOnClickListener(view -> {
                    startProgressDialog();
                    compositeDisposable.add(paroService.getHistoricoParos(idEquipo)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe((body) -> paroService.saveHistorico(body, idEquipo),
                                    throwable -> {
                                        String mensaje = throwable.getMessage() != null ? throwable.getMessage() : getString(R.string.mensaje_error_obtener_informacion);
                                        procesarHistorico(mensaje);
                                    },
                                    () -> procesarHistorico(getString(R.string.historico_paros_vacio))));
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "onCreate: ", e);
        }
    }

    private void startProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.mensaje_progress_obteniendo_paros));
        progressDialog.setMessage(getString(R.string.mensaje_progress_espera));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private boolean historicoNoVacio() {
        List<Paro> paros = database.where(Paro.class)
                .equalTo("idequipo", idEquipo)
                .findAll();

        return paros != null && !paros.isEmpty();
    }

    private void procesarHistorico(String mensaje) {
        Bundle bundle = new Bundle();
        bundle.putLong(Mantum.KEY_ID, idEquipo);

        if (historicoNoVacio()) {
            Intent intent = new Intent(this, HistoricoParoActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        } else {
            new AlertDialog.Builder(this)
                    .setMessage(mensaje)
                    .setCancelable(false)
                    .setPositiveButton("Cerrar", (dialogInterface, i) -> dialogInterface.dismiss())
                    .show();
        }

        dismissProgressDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (listadoParoAdapter != null) {
            listadoParoAdapter.clear();
        }

        if (database != null) {
            database.close();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        try {
            if (!listadoParoAdapter.isEmpty()) {
                for (int i = 0; i < listadoParoAdapter.getItemCount(); i++) {
                    Paro.ParoHelper paro = listadoParoAdapter.getOriginal().get(i);
                    String horaInicio = paro.getHoraInicio() != null ? paro.getHoraInicio() : "";
                    String horaFin = paro.getHoraFin() != null ? paro.getHoraFin() : "";
                    String clasificacion = paro.getClasificacion() != null ? paro.getClasificacion() : "";
                    String listPosition = String.valueOf(i + 1);

                    if (horaInicio.equals("")) {
                        Snackbar.make(getView(), String.format(getString(R.string.hora_inicial_paro_requerida), listPosition), Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    if (horaFin.equals("") && !clasificacion.equalsIgnoreCase("correctivo")) {
                        Snackbar.make(getView(), String.format(getString(R.string.hora_final_paro_requerida), listPosition), Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    Date current = Calendar.getInstance().getTime();
                    Date currentDate = dateFormat.parse(dateFormat.format(current));
                    Date currentTime = timeFormat.parse(timeFormat.format(current));

                    Date fechaInicio = dateFormat.parse(fechaEditText.getText().toString());
                    Date horaInicioDate = timeFormat.parse(horaInicio);
                    Date horaFinDate = horaFin.equals("") ? null : timeFormat.parse(horaFin);

                    Date horaInicioBitacoraDate = horaInicioBitacora.equals("") ? null : timeFormat.parse(horaInicioBitacora);
                    Date horaFinBitacoraDate =  horaFinBitacora.equals("") ? null : timeFormat.parse(horaFinBitacora);

                    if (horaInicioDate != null) {
                        if (horaInicioDate.equals(horaFinDate)) {
                            Snackbar.make(getView(), String.format(getString(R.string.hora_inicial_paro_igual_hora_final), listPosition), Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        if (horaFinDate != null && horaInicioDate.after(horaFinDate)) {
                            Snackbar.make(getView(), String.format(getString(R.string.hora_inicial_paro_mayor_hora_final), listPosition), Snackbar.LENGTH_LONG).show();
                            return;
                        }

                        if (horaInicioDate.before(horaInicioBitacoraDate) && !clasificacion.equalsIgnoreCase("correctivo")) {
                            Snackbar.make(getView(), String.format(getString(R.string.hora_inicial_paro_menor_hora_inicial_bitacora), listPosition), Snackbar.LENGTH_LONG).show();
                            return;
                        }
                    }

                    if (fechaInicio != null && fechaInicio.equals(currentDate)) {
                        if (horaInicioDate != null && horaInicioDate.after(currentTime)) {
                            Snackbar.make(getView(), String.format(getString(R.string.hora_inicial_paro_mayor_hora_actual), listPosition), Snackbar.LENGTH_LONG).show();
                            return;
                        }

                        if (horaFinDate != null && horaFinDate.after(currentTime)) {
                            Snackbar.make(getView(), String.format(getString(R.string.hora_final_paro_mayor_hora_actual), listPosition), Snackbar.LENGTH_LONG).show();
                            return;
                        }
                    }

                    if (horaFinDate != null && horaFinBitacoraDate != null && horaFinDate.after(horaFinBitacoraDate)) {
                        Snackbar.make(getView(), String.format(getString(R.string.hora_final_paro_mayor_hora_final_bitacora), listPosition), Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    if (clasificacion.equals("")) {
                        Snackbar.make(getView(), String.format(getString(R.string.clasificacion_paro_vacia), listPosition), Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    sparseArrayParos.append(i, paro);
                }
            }

            Bundle bundle = new Bundle();
            bundle.putLong(ID_EQUIPO_PARO, idEquipo);
            bundle.putString(ID_AM_PARO, idAm);
            bundle.putSparseParcelableArray(KEY_FORM, sparseArrayParos);

            Intent intent = new Intent();
            intent.putExtras(bundle);
            backActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "onBackPressed: ", e);
        }
    }
}

