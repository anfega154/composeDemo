package com.mantum.cmms.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Spinner;
import com.mantum.cmms.entity.Contenedor;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.ReclasificacionGama;
import com.mantum.cmms.entity.SubtipoReparacionGama;
import com.mantum.cmms.entity.TipoFalla;
import com.mantum.cmms.entity.TipoReparacionGama;
import com.mantum.cmms.factory.SparseArrayTypeAdapterFactory;
import com.mantum.component.Mantum;
import com.mantum.component.service.Photo;
import com.mantum.component.service.PhotoAdapter;
import com.mantum.component.util.Tool;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class RegistrarFallaActivity extends Mantum.Activity {

    public static final int REQUEST_ACTION = 1227;
    public static final int RESULT_ACTION = 1226;

    public static final String MODE_EDIT = "edit";

    private Cuenta cuenta;
    private Database database;

    private String id;
    private String token;
    private Long idActividad;
    private String actividad;

    private Spinner tipoFallaSelected;
    private Spinner tipoReparacionSelected;
    private Spinner subtipoReparacionSelected;
    private Spinner reclasificacionSelected;

    private Boolean isRequiereFotos = false;
    private Boolean isRequireRepuestos = false;

    private ArrayAdapter<Spinner> tiposAdapter;
    private ArrayAdapter<Spinner> subtipoAdapter;

    private final List<Photo> photos = new ArrayList<>();
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .registerTypeAdapterFactory(SparseArrayTypeAdapterFactory.INSTANCE)
            .create();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_registrar_falla);

            includeBackButtonAndTitle(R.string.registrar_falla);

            id = null;
            token = UUID.randomUUID().toString();

            database = new Database(this);
            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            List<TipoFalla> fallasQuery = database.where(TipoFalla.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .equalTo("xpti", true)
                    .sort("tipo")
                    .findAll();

            List<Spinner> fallas = new ArrayList<>();
            for (TipoFalla falla : fallasQuery) {
                String name = falla.getDescripcion() != null
                        ? falla.getTipo() + " - " + falla.getDescripcion()
                        : falla.getTipo();
                fallas.add(new Spinner(falla.getTipo(), name));
            }

            ArrayAdapter<Spinner> fallasAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_dropdown_item, fallas);

            AutoCompleteTextView fallaAutoCompleteTextView = findViewById(R.id.tipo_falla);
            fallaAutoCompleteTextView.setAdapter(fallasAdapter);
            fallaAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> tipoFallaSelected = fallasAdapter.getItem(position));

            List<Spinner> subtiporepareciones = new ArrayList<>();
            subtipoAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_dropdown_item, subtiporepareciones);

            AutoCompleteTextView subtipoAutoCompleteTextView = findViewById(R.id.subtipo_reparacion);
            subtipoAutoCompleteTextView.setAdapter(subtipoAdapter);
            subtipoAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
                subtipoReparacionSelected = subtipoAdapter.getItem(position);
                if ("Limpiar selección".equals(subtipoReparacionSelected.getKey())) {
                    subtipoReparacionSelected = null;
                    subtipoAutoCompleteTextView.setText("", false);
                }
            });

            List<Spinner> tiporepareciones = new ArrayList<>();
            tiposAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_dropdown_item, tiporepareciones);

            AutoCompleteTextView tiposAutoCompleteTextView = findViewById(R.id.tipo_reparacion);
            tiposAutoCompleteTextView.setAdapter(tiposAdapter);
            tiposAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
                tipoReparacionSelected = tiposAdapter.getItem(position);
                if ("Limpiar selección".equals(tipoReparacionSelected.getKey())) {
                    tipoReparacionSelected = null;
                    subtipoReparacionSelected = null;

                    subtipoAdapter.clear();

                    tiposAutoCompleteTextView.setText("");
                    subtipoAutoCompleteTextView.setText("");
                } else {
                    prepareRepair(true);
                }
            });

            List<ReclasificacionGama> reclasificacionQuery = database.where(ReclasificacionGama.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .equalTo("tiposreparacion.subtiposreparacion.gamas.pti", true)
                    .sort("nombre")
                    .findAll();

            List<Spinner> reclasificaciones = new ArrayList<>();
            reclasificaciones.add(new Spinner("Limpiar selección", "Limpiar selección"));
            for (ReclasificacionGama reclasificacion : reclasificacionQuery) {
                reclasificaciones.add(new Spinner(String.valueOf(reclasificacion.getId()), reclasificacion.getNombre()));
            }

            ArrayAdapter<Spinner> reclasificacionesAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_dropdown_item, reclasificaciones
            );

            AutoCompleteTextView reclasificacionAutoCompleteTextView = findViewById(R.id.reclasificacion);
            reclasificacionAutoCompleteTextView.setAdapter(reclasificacionesAdapter);
            reclasificacionAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
                reclasificacionSelected = reclasificaciones.get(position);

                if ("Limpiar selección".equals(reclasificacionSelected.getKey())) {
                    reclasificacionSelected = null;
                    tipoReparacionSelected = null;
                    subtipoReparacionSelected = null;

                    tiposAdapter.clear();
                    subtipoAdapter.clear();

                    tiposAutoCompleteTextView.setText("");
                    subtipoAutoCompleteTextView.setText("");
                    reclasificacionAutoCompleteTextView.setText("");
                } else {
                    prepareClasification(true);
                }
            });

            TextInputEditText actividad = findViewById(R.id.actividad);
            actividad.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), BusquedaVariablesFallaActivity.class);
                intent.putExtra("tipoVariable", "Gama");

                String tipo = tipoReparacionSelected != null ? tipoReparacionSelected.getKey() : null;
                intent.putExtra("idTipoReparacionGama", tipo);

                String subtipo = subtipoReparacionSelected != null ? subtipoReparacionSelected.getKey() : null;
                intent.putExtra("idSubtipoReparacionGama", subtipo);

                String reclasificacion = reclasificacionSelected != null ? reclasificacionSelected.getKey() : null;
                intent.putExtra("idReclasificacionGama", reclasificacion);

                startActivityForResult(intent, 1);
            });

            AppCompatButton appCompatButton = findViewById(R.id.imagen);
            appCompatButton.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putSparseParcelableArray(GaleriaActivity.PATH_FILE_PARCELABLE, PhotoAdapter.factory(photos));

                Intent intent = new Intent(RegistrarFallaActivity.this, GaleriaActivity.class);
                intent.putExtras(bundle);

                intent.putExtra(BusquedaVariablesFallaActivity.TIPO_GAMA, "PTI");
                startActivityForResult(intent, GaleriaActivity.REQUEST_ACTION);
            });

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                String value = bundle.getString(MODE_EDIT);
                if (value != null) {
                    Contenedor.Falla falla = gson.fromJson(value, Contenedor.Falla.class);
                    if (falla != null) {
                        load(falla);
                    }
                }
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            backActivity(getString(R.string.error_app));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_formulario, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        View view = getView();
        if (view == null) {
            return;
        }

        if (data != null && data.getExtras() != null) {
            Bundle bundle = data.getExtras();
            if (resultCode == 1) {
                idActividad = bundle.getLong("idVariable", -1L);

                String tipoVariable = bundle.getString("tipoVariable");
                String codigoVariable = bundle.getString("codigoVariable");
                String descripcionVariable = bundle.getString("descripcionVariable");

                isRequiereFotos = bundle.getBoolean("requierefotos");
                isRequireRepuestos = bundle.getBoolean("requirerepuestos");

                if (tipoVariable != null && tipoVariable.equals("Gama")) {
                    actividad = String.format("%s - %s", codigoVariable, descripcionVariable);

                    TextInputEditText textInputEditText = view.findViewById(R.id.actividad);
                    textInputEditText.setText(actividad);
                }
            } else if (requestCode == GaleriaActivity.REQUEST_ACTION) {
                SparseArray<PhotoAdapter> parcelable = bundle.getSparseParcelableArray(GaleriaActivity.PATH_FILE_PARCELABLE);
                if (parcelable != null) {
                    photos.clear();
                    int total = parcelable.size();
                    if (total > 0) {
                        for (int i = 0; i < total; i++) {
                            PhotoAdapter photoAdapter = parcelable.get(i);
                            Photo photo;
                            if (!photoAdapter.isExternal()) {
                                photo = new Photo(this, new File(photoAdapter.getPath()),
                                        photoAdapter.isDefaultImage(), photoAdapter.getIdCategory(),
                                        photoAdapter.getDescription());
                                photo.setExternal(false);
                            } else {
                                Uri uri = Uri.parse(photoAdapter.getPath());
                                photo = new Photo(this, uri,
                                        photoAdapter.isDefaultImage(), photoAdapter.getIdCategory(),
                                        photoAdapter.getDescription());
                                photo.setExternal(true);
                            }
                            photos.add(photo);
                        }
                    }

                    cantidadImagenes();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void cantidadImagenes() {
        if (photos != null) {
            AppCompatButton appCompatButton = findViewById(R.id.imagen);
            appCompatButton.setText(String.format(
                    "%s - (%s)",
                    getView().getContext().getString(R.string.registrar_imagen),
                    photos.size()
            ));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == android.R.id.home) {
            super.onBackPressed();
        } else if (itemId == R.id.action_done) {
            register();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void load(@NonNull Contenedor.Falla falla) {
        AutoCompleteTextView tipoFallaAutoCompleteTextView = findViewById(R.id.tipo_falla);
        tipoFallaAutoCompleteTextView.setText(falla.getTipo(), false);

        if (falla.getIdtipo() != null && falla.getTipo() != null) {
            tipoFallaSelected = new Spinner(falla.getIdtipo(), falla.getTipo());
        }

        AutoCompleteTextView reclasificacionAutoCompleteTextView = findViewById(R.id.reclasificacion);
        reclasificacionAutoCompleteTextView.setText(falla.getReclasificacion(), false);

        if (falla.getIdreclasificacion() != null && falla.getReclasificacion() != null) {
            reclasificacionSelected = new Spinner(
                    falla.getIdreclasificacion(), falla.getReclasificacion());
            prepareClasification(false);
        }

        AutoCompleteTextView tiposAutoCompleteTextView = findViewById(R.id.tipo_reparacion);
        tiposAutoCompleteTextView.setText(falla.getTiporeparacion(), false);

        if (falla.getIdtiporeparacion() != null && falla.getTiporeparacion() != null) {
            tipoReparacionSelected = new Spinner(
                    falla.getIdtiporeparacion(), falla.getTiporeparacion());
            prepareRepair(false);
        }

        AutoCompleteTextView subTipoAutoCompleteTextView = findViewById(R.id.subtipo_reparacion);
        subTipoAutoCompleteTextView.setText(falla.getSubtiporeparacion(), false);

        if (falla.getIdsubtiporeparacion() != null && falla.getSubtiporeparacion() != null) {
            subtipoReparacionSelected = new Spinner(
                    falla.getIdsubtiporeparacion(), falla.getSubtiporeparacion());
        }

        TextInputEditText actividadTextView = findViewById(R.id.actividad);
        actividadTextView.setText(falla.getActividad());

        id = falla.getId();
        idActividad = falla.getIdActividad();
        actividad = falla.getActividad();

        TextInputEditText serialView = findViewById(R.id.serial);
        serialView.setText(falla.getSerial());

        TextInputEditText parteView = findViewById(R.id.parte);
        parteView.setText(falla.getParte());

        TextInputEditText descriptionView = findViewById(R.id.descripcion);
        descriptionView.setText(falla.getDescription());

        if (falla.getPhotos() != null) {
            photos.addAll(Photo.factory(this, falla.getPhotos()));
        }

        if (falla.getToken() != null) {
            token = falla.getToken();
        }

        isRequireRepuestos = falla.getRequireRepuestos();
        isRequiereFotos = falla.getRequiereFotos();

        cantidadImagenes();
    }

    private void prepareClasification(boolean clear) {
        if (reclasificacionSelected == null || reclasificacionSelected.getKey() == null) {
            return;
        }

        if (clear) {
            tipoReparacionSelected = null;
            subtipoReparacionSelected = null;

            subtipoAdapter.clear();
            tiposAdapter.clear();
        }

        ReclasificacionGama reclasificacion = database.where(ReclasificacionGama.class)
                .equalTo("id", reclasificacionSelected.getKey())
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .sort("nombre")
                .findFirst();

        if (reclasificacion == null) {
            if (clear) {
                AutoCompleteTextView tiposAutoCompleteTextView = findViewById(R.id.tipo_reparacion);
                tiposAutoCompleteTextView.setText("");

                AutoCompleteTextView subtipoAutoCompleteTextView = findViewById(R.id.subtipo_reparacion);
                subtipoAutoCompleteTextView.setText("");
            }
            return;
        }

        List<TipoReparacionGama> tipos = reclasificacion
                .getTiposreparacion()
                .sort("nombre");

        if (tiposAdapter.isEmpty() || !("Limpiar selección".equals(tiposAdapter.getItem(0).getKey()))) {
            tiposAdapter.add(new Spinner("Limpiar selección", "Limpiar selección"));
        }

        List<SubtipoReparacionGama> subtipos = new ArrayList<>();
        for (TipoReparacionGama tipo : tipos) {
            tiposAdapter.add(new Spinner(String.valueOf(tipo.getId()), tipo.getNombre()));
            subtipos.addAll(tipo.getSubtiposreparacion());
        }

        if (!subtipos.isEmpty()) {
            Collections.sort(subtipos, (o1, o2) -> o1.getNombre().compareTo(o2.getNombre()));

            if (subtipoAdapter.isEmpty() || !("Limpiar selección".equals(subtipoAdapter.getItem(0).getKey()))) {
                subtipoAdapter.add(new Spinner("Limpiar selección", "Limpiar selección"));
            }

            for (SubtipoReparacionGama subtipo : subtipos) {
                subtipoAdapter.add(new Spinner(String.valueOf(subtipo.getId()), subtipo.getNombre()));
            }
        }

        if (clear) {
            AutoCompleteTextView tiposAutoCompleteTextView = findViewById(R.id.tipo_reparacion);
            tiposAutoCompleteTextView.setText("");

            AutoCompleteTextView subtipoAutoCompleteTextView = findViewById(R.id.subtipo_reparacion);
            subtipoAutoCompleteTextView.setText("");
        }
    }

    private void prepareRepair(boolean clear) {
        if (tipoReparacionSelected == null || tipoReparacionSelected.getKey() == null) {
            return;
        }

        if (clear) {
            subtipoReparacionSelected = null;
            subtipoAdapter.clear();
        }

        TipoReparacionGama tipo = database.where(TipoReparacionGama.class)
                .equalTo("id", tipoReparacionSelected.getKey())
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .sort("nombre")
                .findFirst();

        if (tipo == null) {
            if (clear) {
                AutoCompleteTextView subtipoAutoCompleteTextView = findViewById(R.id.subtipo_reparacion);
                subtipoAutoCompleteTextView.setText("");
            }
            return;
        }

        if (subtipoAdapter.isEmpty() || !("Limpiar selección".equals(subtipoAdapter.getItem(0).getKey()))) {
            subtipoAdapter.add(new Spinner("Limpiar selección", "Limpiar selección"));
        }

        List<SubtipoReparacionGama> subtipos = tipo.getSubtiposreparacion()
                .sort("nombre");

        for (SubtipoReparacionGama subtipo : subtipos) {
            subtipoAdapter.add(new Spinner(String.valueOf(subtipo.getId()), subtipo.getNombre()));
        }

        if (clear) {
            AutoCompleteTextView subtipoAutoCompleteTextView = findViewById(R.id.subtipo_reparacion);
            subtipoAutoCompleteTextView.setText("");
        }
    }

    private void register() {
        boolean isOk = true;

        TextInputLayout tipoFallaContenedorView = findViewById(R.id.tipo_falla_contenedor);
        tipoFallaContenedorView.setErrorEnabled(false);
        if (tipoFallaSelected == null) {
            isOk = false;
            tipoFallaContenedorView.setError(getString(R.string.tipo_falla_requerido));
            tipoFallaContenedorView.setErrorEnabled(true);
        }

        TextInputLayout textInputLayout = findViewById(R.id.actividad_contenedor);
        textInputLayout.setErrorEnabled(false);
        if (idActividad == null || idActividad <= 0) {
            isOk = false;
            textInputLayout.setError(getString(R.string.actividad_requerida));
            textInputLayout.setErrorEnabled(true);
        }

        String serial = null;
        TextInputEditText serialView = findViewById(R.id.serial);
        if (serialView != null && serialView.getText() != null) {
            serial = serialView.getText().toString();
        }

        String parte = null;
        TextInputEditText parteView = findViewById(R.id.parte);
        if (parteView != null && parteView.getText() != null) {
            parte = parteView.getText().toString();
        }

        if (isRequireRepuestos) {
            TextInputLayout serialContenedorView = findViewById(R.id.serial_contenedor);
            serialContenedorView.setErrorEnabled(false);
            if (serial == null || serial.isEmpty()) {
                isOk = false;
                serialContenedorView.setError(getString(R.string.serial_requerida));
                serialContenedorView.setErrorEnabled(true);
            }

            TextInputLayout parteContenedorView = findViewById(R.id.parte_contenedor);
            parteContenedorView.setErrorEnabled(false);
            if (parte == null || parte.isEmpty()) {
                isOk = false;
                parteContenedorView.setError(getString(R.string.numero_parte_requerido));
                parteContenedorView.setErrorEnabled(true);
            }
        }

        if (isRequiereFotos) {
            if (photos.isEmpty()) {
                Snackbar.make(getView(), R.string.fotografia_requerida, Snackbar.LENGTH_LONG)
                        .show();
                return;
            }
        }

        String description = null;
        TextInputEditText descriptionView = findViewById(R.id.descripcion);
        if (descriptionView != null && descriptionView.getText() != null) {
            description = descriptionView.getText().toString();
        }

        TextInputLayout descripcionContenedorView = findViewById(R.id.descripcion_contenedor);
        descripcionContenedorView.setErrorEnabled(false);
        if (description == null || description.isEmpty()) {
            isOk = false;
            descripcionContenedorView.setError(getString(R.string.descripcion_requerida));
            descripcionContenedorView.setErrorEnabled(true);
        }

        if (!isOk) {
            return;
        }

        Contenedor.Falla falla = new Contenedor.Falla();
        falla.setId(id);
        falla.setIdtipo(tipoFallaSelected.getKey());
        falla.setTipo(tipoFallaSelected.getValue());

        if (reclasificacionSelected != null) {
            falla.setIdreclasificacion(reclasificacionSelected.getKey());
            falla.setReclasificacion(reclasificacionSelected.getValue());
        }

        if (tipoReparacionSelected != null) {
            falla.setIdtiporeparacion(tipoReparacionSelected.getKey());
            falla.setTiporeparacion(tipoReparacionSelected.getValue());
        }

        if (subtipoReparacionSelected != null) {
            falla.setIdsubtiporeparacion(subtipoReparacionSelected.getKey());
            falla.setSubtiporeparacion(subtipoReparacionSelected.getValue());
        }

        falla.setFecha(Tool.datetime(new Date()));
        falla.setIdActividad(idActividad);
        falla.setActividad(actividad);
        falla.setSerial(serial);
        falla.setParte(parte);
        falla.setDescription(description);
        falla.setPhotos(PhotoAdapter.factory(photos));
        falla.setRequiereFotos(isRequiereFotos);
        falla.setRequireRepuestos(isRequireRepuestos);
        falla.setToken(token);

        Intent intent = new Intent();
        intent.putExtra(Mantum.KEY_ID, falla);
        setResult(RESULT_ACTION, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setTitle(R.string.cancelar_inspeccion);
        alertDialogBuilder.setMessage(R.string.cancelar_inspeccion_mensaje);
        alertDialogBuilder.setNegativeButton(R.string.salir, (dialog, id) -> super.backActivity());
        alertDialogBuilder.setPositiveButton(R.string.continuar, (dialog, id) -> dialog.dismiss());
        alertDialogBuilder.show();
    }

}