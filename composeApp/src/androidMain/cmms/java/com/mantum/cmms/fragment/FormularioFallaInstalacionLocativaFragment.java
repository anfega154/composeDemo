package com.mantum.cmms.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mantum.R;
import com.mantum.cmms.activity.BusquedaVariablesFallaActivity;
import com.mantum.cmms.activity.CaptureActivityPortrait;
import com.mantum.cmms.activity.FormularioFallaEquipoActivity;
import com.mantum.cmms.activity.GaleriaActivity;
import com.mantum.cmms.adapter.ListadoRepuestosAdapter;
import com.mantum.cmms.adapter.onValueChange.CustomOnValueChange;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Falla;
import com.mantum.cmms.entity.InstalacionLocativa;
import com.mantum.cmms.entity.ReclasificacionGama;
import com.mantum.cmms.entity.RepuestoManual;
import com.mantum.cmms.entity.SubtipoReparacionGama;
import com.mantum.cmms.entity.TipoFalla;
import com.mantum.cmms.entity.TipoReparacionGama;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.helper.TransaccionHelper;
import com.mantum.cmms.service.TransaccionService;
import com.mantum.component.Mantum;
import com.mantum.component.component.DatePicker;
import com.mantum.component.component.TimePicker;
import com.mantum.component.service.Photo;
import com.mantum.component.service.PhotoAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

import static android.app.Activity.RESULT_OK;
import static com.mantum.cmms.security.Security.TAG;

public class FormularioFallaInstalacionLocativaFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Formulario_Falla_Instalacion_Locativa";

    private String uuidTransaccion = null;

    private String MODE_EDIT = null;

    private Database database;

    private EditText descripcionFalla;

    private EditText groupcodeFalla;

    private DatePicker date;

    private TimePicker time;

    private ListadoRepuestosAdapter<RepuestoManual.Repuesto> listadoRepuestosAdapter;

    private RecyclerView recyclerViewRepuestos;

    private Cuenta cuenta;

    private Long idInstalacionLocativa;

    private android.widget.Spinner tiposFallaSpinner;

    private android.widget.Spinner reclasificacionGamaSpinner;

    private android.widget.Spinner tipoReparacionGamaSpinner;

    private android.widget.Spinner subtipoReparacionGamaSpinner;

    private EditText codigoGamaFalla;

    private EditText descripcionGamaFalla;

    private TransaccionService transaccionService;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private TransaccionHelper.Dialog dialogHelper;

    private List<Photo> photosList;

    private View view;

    private final List<String> tiposFalla = new ArrayList<>();

    private final List<String> tiposFallaAux = new ArrayList<>();

    private ArrayAdapter<String> spinnerTiposFallaAdapter;

    private String idReclasificacionGama, idTipoReparacionGama, idSubtipoReparacionGama;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            super.onCreateView(inflater, container, savedInstanceState);
            view = inflater.inflate(R.layout.fragment_formulario_falla, container, false);
            dialogHelper = new TransaccionHelper.Dialog(view.getContext());

            transaccionService = new TransaccionService(view.getContext());

            photosList = new ArrayList<>();

            descripcionFalla = view.findViewById(R.id.descripcion_falla);
            groupcodeFalla = view.findViewById(R.id.groupcode_falla);
            descripcionGamaFalla = view.findViewById(R.id.descripcion_gama_falla);
            reclasificacionGamaSpinner = view.findViewById(R.id.reclasificacion_gama_falla);
            tipoReparacionGamaSpinner = view.findViewById(R.id.tipo_reparacion_gama_falla);
            subtipoReparacionGamaSpinner = view.findViewById(R.id.subtipo_reparacion_gama_falla);

            android.widget.Spinner equiposSpinner = view.findViewById(R.id.equipo_falla);
            equiposSpinner.setVisibility(View.GONE);

            database = new Database(view.getContext());
            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            TextView btnAgregarElemento = view.findViewById(R.id.agregar_repuesto);
            btnAgregarElemento.setOnClickListener(view1 -> {
                listadoRepuestosAdapter.add(new RepuestoManual.Repuesto());
                listadoRepuestosAdapter.notifyItemInserted(listadoRepuestosAdapter.getItemCount());
            });

            Bundle bundle = getArguments();
            if (bundle == null) {
                throw new Exception(getString(R.string.error_detail_equipo));
            }

            List<com.mantum.cmms.domain.Spinner> reclasificaciones = new ArrayList<>();
            reclasificaciones.add(new com.mantum.cmms.domain.Spinner("0", "Reclasificación"));
            List<ReclasificacionGama> reclasificacionGamas = database.where(ReclasificacionGama.class)
                    .sort("nombre")
                    .findAll();

            for (ReclasificacionGama reclasificacionGama : reclasificacionGamas) {
                reclasificaciones.add(new com.mantum.cmms.domain.Spinner(String.valueOf(reclasificacionGama.getId()), reclasificacionGama.getNombre()));
            }

            List<com.mantum.cmms.domain.Spinner> tipoReparaciones = new ArrayList<>();
            tipoReparaciones.add(new com.mantum.cmms.domain.Spinner("0", "T. Reparación"));
            List<TipoReparacionGama> tipoReparacionGamas = database.where(TipoReparacionGama.class)
                    .sort("nombre")
                    .findAll();

            for (TipoReparacionGama tipoReparacionGama : tipoReparacionGamas) {
                tipoReparaciones.add(new com.mantum.cmms.domain.Spinner(String.valueOf(tipoReparacionGama.getId()), tipoReparacionGama.getNombre()));
            }

            List<com.mantum.cmms.domain.Spinner> subtipoReparaciones = new ArrayList<>();
            subtipoReparaciones.add(new com.mantum.cmms.domain.Spinner("0", "Subtipo reparación"));
            List<SubtipoReparacionGama> subtipoReparacionGamas = database.where(SubtipoReparacionGama.class)
                    .sort("nombre")
                    .findAll();

            for (SubtipoReparacionGama subtipoReparacionGama : subtipoReparacionGamas) {
                subtipoReparaciones.add(new com.mantum.cmms.domain.Spinner(String.valueOf(subtipoReparacionGama.getId()), subtipoReparacionGama.getNombre()));
            }

            idReclasificacionGama = null;
            idTipoReparacionGama = null;
            idSubtipoReparacionGama = null;

            reclasificacionGamaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    tipoReparaciones.clear();
                    subtipoReparaciones.clear();

                    tipoReparaciones.add(new com.mantum.cmms.domain.Spinner("0", "T. Reparación"));
                    subtipoReparaciones.add(new com.mantum.cmms.domain.Spinner("0", "Subtipo reparación"));

                    if (position > 0) {
                        com.mantum.cmms.domain.Spinner reclasificacionSpinner = (com.mantum.cmms.domain.Spinner) reclasificacionGamaSpinner.getSelectedItem();
                        String idReclasificacion = reclasificacionSpinner.getKey();

                        idReclasificacionGama = idReclasificacion;
                        idTipoReparacionGama = null;
                        idSubtipoReparacionGama = null;

                        ReclasificacionGama reclasificacionGama = database.where(ReclasificacionGama.class)
                                .equalTo("id", idReclasificacion)
                                .findFirst();

                        if (reclasificacionGama != null) {
                            List<SubtipoReparacionGama> subtipoReparacionGamasList = new ArrayList<>();
                            for (TipoReparacionGama tipoReparacionGama : reclasificacionGama.getTiposreparacion().sort("nombre")) {
                                tipoReparaciones.add(new com.mantum.cmms.domain.Spinner(String.valueOf(tipoReparacionGama.getId()), tipoReparacionGama.getNombre()));
                                subtipoReparacionGamasList.addAll(tipoReparacionGama.getSubtiposreparacion());
                            }

                            if (!subtipoReparacionGamasList.isEmpty()) {
                                Collections.sort(subtipoReparacionGamasList, (o1, o2) -> o1.getNombre().compareTo(o2.getNombre()));
                                for (SubtipoReparacionGama subtipoReparacionGama : subtipoReparacionGamasList) {
                                    subtipoReparaciones.add(new com.mantum.cmms.domain.Spinner(String.valueOf(subtipoReparacionGama.getId()), subtipoReparacionGama.getNombre()));
                                }
                            }
                        }
                    } else {
                        for (TipoReparacionGama tipoReparacionGama : tipoReparacionGamas) {
                            tipoReparaciones.add(new com.mantum.cmms.domain.Spinner(String.valueOf(tipoReparacionGama.getId()), tipoReparacionGama.getNombre()));
                        }

                        for (SubtipoReparacionGama subtipoReparacionGama : subtipoReparacionGamas) {
                            subtipoReparaciones.add(new com.mantum.cmms.domain.Spinner(String.valueOf(subtipoReparacionGama.getId()), subtipoReparacionGama.getNombre()));
                        }

                        idReclasificacionGama = null;
                        idTipoReparacionGama = null;
                        idSubtipoReparacionGama = null;
                    }

                    tipoReparacionGamaSpinner.setSelection(0);
                    subtipoReparacionGamaSpinner.setSelection(0);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            tipoReparacionGamaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    subtipoReparaciones.clear();

                    subtipoReparaciones.add(new com.mantum.cmms.domain.Spinner("0", "Subtipo reparación"));

                    if (position > 0) {
                        com.mantum.cmms.domain.Spinner tipoReparacionSpinner = (com.mantum.cmms.domain.Spinner) tipoReparacionGamaSpinner.getSelectedItem();
                        String idTipoReparacion = tipoReparacionSpinner.getKey();

                        idReclasificacionGama = null;
                        idTipoReparacionGama = idTipoReparacion;
                        idSubtipoReparacionGama = null;

                        TipoReparacionGama tipoReparacionGama = database.where(TipoReparacionGama.class)
                                .equalTo("id", idTipoReparacion)
                                .findFirst();

                        if (tipoReparacionGama != null) {
                            for (SubtipoReparacionGama subtipoReparacionGama : tipoReparacionGama.getSubtiposreparacion().sort("nombre")) {
                                subtipoReparaciones.add(new com.mantum.cmms.domain.Spinner(String.valueOf(subtipoReparacionGama.getId()), subtipoReparacionGama.getNombre()));
                            }
                        }
                    } else {
                        if (reclasificacionGamaSpinner.getSelectedItemPosition() > 0) {
                            com.mantum.cmms.domain.Spinner reclasificacionSpinner = (com.mantum.cmms.domain.Spinner) reclasificacionGamaSpinner.getSelectedItem();
                            String idReclasificacion = reclasificacionSpinner.getKey();

                            idReclasificacionGama = idReclasificacion;
                            idTipoReparacionGama = null;
                            idSubtipoReparacionGama = null;

                            ReclasificacionGama reclasificacionGama = database.where(ReclasificacionGama.class)
                                    .equalTo("id", idReclasificacion)
                                    .findFirst();

                            if (reclasificacionGama != null) {
                                List<SubtipoReparacionGama> subtipoReparacionGamasList = new ArrayList<>();
                                for (TipoReparacionGama tipoReparacionGama : reclasificacionGama.getTiposreparacion()) {
                                    subtipoReparacionGamasList.addAll(tipoReparacionGama.getSubtiposreparacion());
                                }

                                if (!subtipoReparacionGamasList.isEmpty()) {
                                    Collections.sort(subtipoReparacionGamasList, (o1, o2) -> o1.getNombre().compareTo(o2.getNombre()));
                                    for (SubtipoReparacionGama subtipoReparacionGama : subtipoReparacionGamasList) {
                                        subtipoReparaciones.add(new com.mantum.cmms.domain.Spinner(String.valueOf(subtipoReparacionGama.getId()), subtipoReparacionGama.getNombre()));
                                    }
                                }
                            }
                        } else {
                            idReclasificacionGama = null;
                            idTipoReparacionGama = null;
                            idSubtipoReparacionGama = null;

                            for (SubtipoReparacionGama subtipoReparacionGama : subtipoReparacionGamas) {
                                subtipoReparaciones.add(new com.mantum.cmms.domain.Spinner(String.valueOf(subtipoReparacionGama.getId()), subtipoReparacionGama.getNombre()));
                            }
                        }
                    }
                    subtipoReparacionGamaSpinner.setSelection(0);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            ArrayAdapter<com.mantum.cmms.domain.Spinner> spinnerReclasificacionGamaAdapter = new ArrayAdapter<>(view.getContext(), R.layout.custom_simple_spinner, R.id.item, reclasificaciones);
            reclasificacionGamaSpinner.setAdapter(spinnerReclasificacionGamaAdapter);

            ArrayAdapter<com.mantum.cmms.domain.Spinner> spinnerTipoReparacionGamaAdapter = new ArrayAdapter<>(view.getContext(), R.layout.custom_simple_spinner, R.id.item, tipoReparaciones);
            tipoReparacionGamaSpinner.setAdapter(spinnerTipoReparacionGamaAdapter);

            ArrayAdapter<com.mantum.cmms.domain.Spinner> spinnerSubtipoReparacionGamaAdapter = new ArrayAdapter<>(view.getContext(), R.layout.custom_simple_spinner, R.id.item, subtipoReparaciones);
            subtipoReparacionGamaSpinner.setAdapter(spinnerSubtipoReparacionGamaAdapter);

            idInstalacionLocativa = bundle.getLong(Mantum.KEY_ID);

            List<TipoFalla> listTiposFalla = database.where(TipoFalla.class)
                    .sort("tipo")
                    .findAll();

            tiposFalla.add("Tipo falla");
            tiposFallaAux.add("Tipo falla");
            for (TipoFalla tipoFalla : listTiposFalla) {
                tiposFalla.add(tipoFalla.getDescripcion() != null
                        ? tipoFalla.getTipo() + " - " + tipoFalla.getDescripcion()
                        : tipoFalla.getTipo());

                tiposFallaAux.add(tipoFalla.getTipo());
            }

            spinnerTiposFallaAdapter = new ArrayAdapter<String>(view.getContext(), R.layout.custom_simple_spinner, R.id.item, tiposFalla) {
                @Override
                public boolean isEnabled(int position) {
                    return position > 0;
                }

                @Override
                public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    ((TextView) view.findViewById(R.id.item)).setTextColor(position == 0 ? Color.GRAY : Color.BLACK);
                    return view;
                }
            };

            tiposFallaSpinner = view.findViewById(R.id.tipo_falla);
            tiposFallaSpinner.setAdapter(spinnerTiposFallaAdapter);
            tiposFallaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position > 0) {
                        String tipoFalla = tiposFallaAux.get(tiposFallaSpinner.getSelectedItemPosition());
                        Calendar calendar = Calendar.getInstance(Locale.GERMAN);
                        int semanaActual = calendar.get(Calendar.WEEK_OF_YEAR);

                        groupcodeFalla.setText(String.format("%s%s", tipoFalla, semanaActual));
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            codigoGamaFalla = view.findViewById(R.id.codigo_gama_falla);
            codigoGamaFalla.setOnClickListener(view1 -> {
                if (subtipoReparacionGamaSpinner.getSelectedItemPosition() > 0) {
                    com.mantum.cmms.domain.Spinner subtipoReparacionSpinner = (com.mantum.cmms.domain.Spinner) subtipoReparacionGamaSpinner.getSelectedItem();
                    String idSubtipoReparacion = subtipoReparacionSpinner.getKey();

                    idReclasificacionGama = null;
                    idTipoReparacionGama = null;
                    idSubtipoReparacionGama = idSubtipoReparacion;
                } else {
                    idSubtipoReparacionGama = null;
                }

                Intent intent = new Intent(getActivity(), BusquedaVariablesFallaActivity.class);
                intent.putExtra("tipoVariable", "Gama");
                intent.putExtra("idReclasificacionGama", idReclasificacionGama);
                intent.putExtra("idTipoReparacionGama", idTipoReparacionGama);
                intent.putExtra("idSubtipoReparacionGama", idSubtipoReparacionGama);
                startActivityForResult(intent, 1);
            });

            listadoRepuestosAdapter = new ListadoRepuestosAdapter<>(view.getContext());
            recyclerViewRepuestos = view.findViewById(R.id.recycler_view_repuestos);
            recyclerViewRepuestos.setLayoutManager(new LinearLayoutManager(view.getContext()));
            recyclerViewRepuestos.setAdapter(listadoRepuestosAdapter);

            listadoRepuestosAdapter.setSerialVisibility(false);
            listadoRepuestosAdapter.setOnAction(new CustomOnValueChange<RepuestoManual.Repuesto>() {
                @Override
                public void onClick(RepuestoManual.Repuesto value, int position) {
                    listadoRepuestosAdapter.remove(position);
                    listadoRepuestosAdapter.notifyItemRemoved(position);
                }

                @Override
                public void onFirstTextChange(String value, int position) {

                }

                @Override
                public void onSecondTextChange(String value, int position) {

                }

                @Override
                public void onThirdTextChange(String value, int position) {

                }
            });

            FloatingActionButton cameraQr = view.findViewById(R.id.camera_qr);
            cameraQr.setOnClickListener(view1 -> {
                IntentIntegrator integrator = new IntentIntegrator(getActivity());
                integrator.setOrientationLocked(true);
                integrator.setCameraId(0);
                integrator.setPrompt("");
                integrator.setCaptureActivity(CaptureActivityPortrait.class);
                integrator.setBeepEnabled(false);
                integrator.initiateScan();
            });

            FloatingActionButton previousPhotos = view.findViewById(R.id.fail_photos);
            previousPhotos.setOnClickListener(view1 -> {
                Bundle bundle1 = new Bundle();
                bundle1.putSparseParcelableArray(GaleriaActivity.PATH_FILE_PARCELABLE, PhotoAdapter.factory(photosList));

                Intent intent = new Intent(view.getContext(), GaleriaActivity.class);
                intent.putExtras(bundle1);
                startActivityForResult(intent, GaleriaActivity.REQUEST_ACTION);
            });

            uuidTransaccion = bundle.getString(FormularioFallaEquipoActivity.UUID_TRANSACCION);
            MODE_EDIT = bundle.getString(FormularioFallaEquipoActivity.MODE_EDIT);

            return view;
        } catch (Exception e) {
            Log.e(TAG, "onCreateView: ", e);
            return null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        date = new DatePicker(view.getContext(), R.id.date);
        date.setEnabled(true);
        date.load(true);

        time = new TimePicker(view.getContext(), R.id.time);
        time.setEnabled(true);
        time.load(true);

        if (uuidTransaccion != null) {
            editarTransaccionFalla(MODE_EDIT);
        }
    }

    public void create() {
        TextInputLayout contentCodigoGamaFalla = view.findViewById(R.id.content_codigo_gama_falla);
        contentCodigoGamaFalla.setError(null);

        if (tiposFallaSpinner.getSelectedItemPosition() == 0) {
            Snackbar.make(view, getString(R.string.registrar_falla_campo_tipo_falla_vacio), Snackbar.LENGTH_LONG).show();
            return;
        }
        if (codigoGamaFalla.getText().toString().isEmpty()) {
            contentCodigoGamaFalla.setError(getString(R.string.registrar_falla_campo_gama_vacio));
            return;
        }

        String descripcion = descripcionFalla.getText().toString();
        String groupcode = groupcodeFalla.getText().toString();
        String fecha = date.getValue();
        String hora = time.getValue();

        InstalacionLocativa instalacionLocativa = database.where(InstalacionLocativa.class)
                .equalTo("id", idInstalacionLocativa)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .findFirst();

        if (instalacionLocativa == null) {
            return;
        }

        String tipoFalla = tiposFallaAux.get(tiposFallaSpinner.getSelectedItemPosition());
        String codigoGama = codigoGamaFalla.getText().toString();
        String descripcionGama = descripcionGamaFalla.getText().toString();

        ArrayList<RepuestoManual.Repuesto> repuestos = new ArrayList<>();
        if (!listadoRepuestosAdapter.isEmpty()) {
            for (int i = 0; i < listadoRepuestosAdapter.getItemCount(); i++) {
                RecyclerView.ViewHolder holder = recyclerViewRepuestos.getChildViewHolder(recyclerViewRepuestos.getChildAt(i));
                EditText serialRetiro = holder.itemView.findViewById(R.id.serial_retiro);
                EditText nombreRepuesto = holder.itemView.findViewById(R.id.nombre_repuesto);
                TextInputLayout contentNombreRepuesto = holder.itemView.findViewById(R.id.content_nombre_repuesto);
                TextInputLayout contentSerialRetiro = holder.itemView.findViewById(R.id.content_serial_retiro);

                contentNombreRepuesto.setError(null);
                contentSerialRetiro.setError(null);

                if (nombreRepuesto.getText().toString().equals("") && serialRetiro.getText().toString().equals("")) {
                    continue;
                }
                if (nombreRepuesto.getText().toString().equals("")) {
                    contentNombreRepuesto.setError(getString(R.string.repuesto_campo_nombre_vacio));
                    return;
                }
                if (serialRetiro.getText().toString().equals("")) {
                    contentSerialRetiro.setError(getString(R.string.repuesto_campo_serial_retiro_vacio));
                    return;
                }

                RepuestoManual.Repuesto repuesto = new RepuestoManual.Repuesto();
                repuesto.setSerialRetiro(serialRetiro.getText().toString());
                repuesto.setNombre(nombreRepuesto.getText().toString());
                repuestos.add(repuesto);
            }
        }

        Falla.CreateFalla falla = new Falla.CreateFalla();
        falla.setToken(UUID.randomUUID().toString());
        falla.setIdot(null);
        falla.setIdlocacion(idInstalacionLocativa);
        falla.setTipofalla(tipoFalla);
        falla.setCodigogama(codigoGama);
        falla.setDescripciongama(descripcionGama);
        falla.setCodigoequipo(instalacionLocativa.getCodigo());
        falla.setDescripcion(descripcion);
        falla.setGroupcode(groupcode);
        falla.setDate(fecha + " " + hora);

        if (!repuestos.isEmpty()) {
            falla.setRepuestos(repuestos);
        }
        if (!photosList.isEmpty()) {
            falla.setImagenes(photosList);
        }

        String value = falla.toJson();
        String url = cuenta.getServidor().getUrl() + "/restapp/app/savefalla";

        Transaccion transaccion = null;
        if (uuidTransaccion != null) {
            transaccion = database.where(Transaccion.class)
                    .equalTo("UUID", uuidTransaccion)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findFirst();
        }

        if (transaccion == null) {
            transaccion = new Transaccion();
            transaccion.setUUID(UUID.randomUUID().toString());
            transaccion.setCuenta(cuenta);
            transaccion.setCreation(Calendar.getInstance().getTime());
            transaccion.setUrl(url);
            transaccion.setVersion(cuenta.getServidor().getVersion());
            transaccion.setValue(value);
            transaccion.setModulo(Transaccion.MODULO_FALLAS);
            transaccion.setAccion(Transaccion.ACCION_CREAR_FALLA_INSTALACION_LOCATIVA);
            transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);

            dialogHelper.showProgressDialog();

            compositeDisposable.add(transaccionService.save(transaccion)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(transaccions -> {}, throwable -> {
                        dialogHelper.dismissProgressDialog();
                        Log.e(TAG, "create: ", throwable);
                        Snackbar.make(view, throwable.getMessage(), Snackbar.LENGTH_LONG).show();
                    }, () -> {
                        dialogHelper.dismissProgressDialog();
                        if (getActivity() != null) getActivity().finish();
                    }));
        } else {
            Transaccion finalTransaccion = transaccion;
            database.update(() -> {
                finalTransaccion.setCreation(Calendar.getInstance().getTime());
                finalTransaccion.setValue(value);
                finalTransaccion.setMessage("");
                finalTransaccion.setUrl(url);
                finalTransaccion.setEstado(Transaccion.ESTADO_PENDIENTE);
            });

            if (getActivity() != null) getActivity().finish();
        }
    }

    private void editarTransaccionFalla(String value) {
        if (value != null) {
            Gson gson = new Gson();
            Falla.CreateFalla falla = gson.fromJson(value, Falla.CreateFalla.class);

            if (falla != null) {
                idInstalacionLocativa = falla.getIdlocacion();

                TipoFalla tipoFalla = database.where(TipoFalla.class)
                        .equalTo("tipo", falla.getTipofalla())
                        .findFirst();

                if (tipoFalla != null) {
                    tiposFallaSpinner.setSelection(spinnerTiposFallaAdapter.getPosition(tipoFalla.getDescripcion() != null
                            ? tipoFalla.getTipo() + " - " + tipoFalla.getDescripcion()
                            : tipoFalla.getTipo()));
                }

                codigoGamaFalla.setText(falla.getCodigogama());
                descripcionGamaFalla.setText(falla.getDescripciongama());
                groupcodeFalla.setText(falla.getGroupcode());
                descripcionFalla.setText(falla.getDescripcion());

                String[] fechaHora = falla.getDate().split(" ");
                date.setValue(fechaHora[0]);
                time.setValue(fechaHora[1]);

                if (falla.getRepuestos() != null && !falla.getRepuestos().isEmpty()) {
                    listadoRepuestosAdapter.addAll(falla.getRepuestos());
                }

                if (falla.getImagenes() != null && !falla.getImagenes().isEmpty()) {
                    photosList.addAll(falla.getImagenes());
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && data.getExtras() != null) {
            Bundle bundle = data.getExtras();
            if (resultCode == RESULT_OK) {
                SparseArray<PhotoAdapter> parcelable = bundle.getSparseParcelableArray(GaleriaActivity.PATH_FILE_PARCELABLE);
                if (parcelable != null) {
                    photosList.clear();
                    int total = parcelable.size();
                    if (total > 0) {
                        for (int i = 0; i < total; i++) {
                            PhotoAdapter photoAdapter = parcelable.get(i);
                            photosList.add(new Photo(view.getContext(), new File(photoAdapter.getPath()),
                                    photoAdapter.isDefaultImage(), photoAdapter.getIdCategory(),
                                    photoAdapter.getDescription()));
                        }
                    }
                } else {
                    IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                    String contents = result.getContents();
                    if (contents != null) {
                        listadoRepuestosAdapter.add(new RepuestoManual.Repuesto("", "", contents));
                    }
                }
            } else if (resultCode == 1) {
                String tipoVariable = bundle.getString("tipoVariable");
                String codigoVariable = bundle.getString("codigoVariable");
                String descripcionVariable = bundle.getString("descripcionVariable");

                if (tipoVariable != null && tipoVariable.equals("Gama")) {
                    codigoGamaFalla.setText(codigoVariable);
                    descripcionGamaFalla.setText(descripcionVariable);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (database != null) {
            database.close();
        }
    }

    @NonNull
    @Override
    public String getKey() {
        return KEY_TAB;
    }

    @NonNull
    @Override
    public String getTitle(@NonNull Context context) {
        return context.getString(R.string.tab_general);
    }
}
