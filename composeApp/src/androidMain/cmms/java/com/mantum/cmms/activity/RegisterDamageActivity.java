package com.mantum.cmms.activity;

import static com.mantum.component.activity.DrawingView.DRAWING_PHOTO;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.signature.StringSignature;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Spinner;
import com.mantum.cmms.entity.Contenedor;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.DamageCode;
import com.mantum.cmms.entity.ElementCode;
import com.mantum.cmms.entity.Parte;
import com.mantum.cmms.entity.ReclasificacionGama;
import com.mantum.cmms.entity.SubtipoReparacionGama;
import com.mantum.cmms.entity.TipoFalla;
import com.mantum.cmms.entity.TipoReparacionGama;
import com.mantum.cmms.factory.SparseArrayTypeAdapterFactory;
import com.mantum.component.Mantum;
import com.mantum.component.activity.DrawingView;
import com.mantum.component.service.Photo;
import com.mantum.component.service.PhotoAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class RegisterDamageActivity extends Mantum.Activity {

    public static final int REQUEST_ACTION = 1227;
    public static final int RESULT_ACTION = 1226;
    public static final String MODE_EDIT = "edit";

    private Cuenta cuenta;
    private Database database;
    private String id;
    private String token;
    private Long idActividad;
    private String actividad;
    private boolean external = true;
    private Boolean isRequiereFotos = false;
    private ArrayAdapter<Spinner> tiposAdapter;
    private ArrayAdapter<Spinner> subtipoAdapter;
    private Spinner tipoFallaSelected;
    private String imageSelected;
    private Spinner parteSelected;
    private Spinner tipoReparacionSelected;
    private Spinner subtipoReparacionSelected;
    private Spinner reclasificacionSelected;
    private Spinner elementSelected;
    private Spinner damagesSelected;
    private final List<Photo> photos = new ArrayList<>();
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .registerTypeAdapterFactory(SparseArrayTypeAdapterFactory.INSTANCE)
            .create();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_register_damage);

            includeBackButtonAndTitle(R.string.register_damage);

            id = null;
            token = UUID.randomUUID().toString();

            database = new Database(this);
            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            List<TipoFalla> fallasQuery = database.where(TipoFalla.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .equalTo("xeir", true)
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

            List<Parte> partesQuery = database.where(Parte.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .sort("orden")
                    .findAll();

            ImageView background = findViewById(R.id.background);
            background.setOnClickListener(v -> {
                if (imageSelected == null || imageSelected.isEmpty()) {
                    Snackbar.make(v, R.string.seleccionar_imagen, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                if (external) {
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setMessage(R.string.descargando_imagen)
                            .setCancelable(true)
                            .create();

                    dialog.show();

                    Glide.with(this)
                            .load(Uri.parse(imageSelected))
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .fitCenter()
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    File file = compress(resource);
                                    if (file != null) {
                                        external = false;
                                        imageSelected = file.getAbsolutePath();
                                        goDrawingView(file);
                                    }
                                    dialog.dismiss();
                                }
                            });
                } else {
                    Glide.with(this)
                            .load(imageSelected)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .fitCenter()
                            .into(new SimpleTarget<GlideDrawable>() {
                                @Override
                                public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                                    goDrawingView(new File(imageSelected));
                                }
                            });
                }
            });

            List<DamageCode> damagesQuery = database.where(DamageCode.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .sort("name")
                    .findAll();

            List<Spinner> damages = new ArrayList<>();
            for (DamageCode item : damagesQuery) {
                damages.add(new Spinner(String.valueOf(item.getId()), item.getName()));
            }

            ArrayAdapter<Spinner> damagesAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_dropdown_item, damages);

            AutoCompleteTextView damageAutoCompleteTextView = findViewById(R.id.damage);
            damageAutoCompleteTextView.setAdapter(damagesAdapter);
            damageAutoCompleteTextView.setOnItemClickListener((parent, view1, position, id) -> {
                damagesSelected = damages.get(position);
            });

            List<ElementCode> elementsQuery = database.where(ElementCode.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .sort("name")
                    .findAll();

            List<Spinner> elements = new ArrayList<>();
            for (ElementCode item : elementsQuery) {
                elements.add(new Spinner(String.valueOf(item.getId()), item.getName()));
            }

            ArrayAdapter<Spinner> elementAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_dropdown_item, elements);

            AutoCompleteTextView elementAutoCompleteTextView = findViewById(R.id.element);
            elementAutoCompleteTextView.setAdapter(elementAdapter);
            elementAutoCompleteTextView.setOnItemClickListener((parent, view1, position, id) -> {
                elementSelected = elements.get(position);
            });

            TextInputEditText localizacionView = findViewById(R.id.localizacion);

            List<Spinner> partes = new ArrayList<>();
            for (Parte parte : partesQuery) {
                partes.add(new Spinner(String.valueOf(parte.getId()), parte.getName()));
            }

            ArrayAdapter<Spinner> partesAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_dropdown_item, partes);

            AutoCompleteTextView parteAutoCompleteTextView = findViewById(R.id.parte);
            parteAutoCompleteTextView.setAdapter(partesAdapter);
            parteAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
                parteSelected = partesAdapter.getItem(position);

                Parte parte = partesQuery.get(position);
                if (parte != null) {
                    localizacionView.setText(parte.getUbicacion());

                    external = true;
                    imageSelected = parte.getImage();

                    if (imageSelected != null && !imageSelected.isEmpty()) {
                        Glide.with(this)
                                .load(imageSelected)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .fitCenter()
                                .into(background);
                    }
                }
            });

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

            AutoCompleteTextView tiposAutoCompleteTextView = this.findViewById(R.id.tipo_reparacion);
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
                    .equalTo("tiposreparacion.subtiposreparacion.gamas.eir", true)
                    .sort("nombre")
                    .findAll();

            List<Spinner> reclasificaciones = new ArrayList<>();
            reclasificaciones.add(new Spinner("Limpiar selección", "Limpiar selección"));
            for (ReclasificacionGama reclasificacion : reclasificacionQuery) {
                reclasificaciones.add(new Spinner(
                        String.valueOf(reclasificacion.getId()), reclasificacion.getNombre()));
            }

            ArrayAdapter<Spinner> reclasificacionesAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_dropdown_item, reclasificaciones);

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

                intent.putExtra(BusquedaVariablesFallaActivity.TIPO_GAMA, "EIR");
                startActivityForResult(intent, 1);
            });

            AppCompatButton appCompatButton = findViewById(R.id.imagen);
            appCompatButton.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putSparseParcelableArray(GaleriaActivity.PATH_FILE_PARCELABLE, PhotoAdapter.factory(photos));

                Intent intent = new Intent(RegisterDamageActivity.this, GaleriaActivity.class);
                intent.putExtras(bundle);

                startActivityForResult(intent, GaleriaActivity.REQUEST_ACTION);
            });

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                String value = bundle.getString(MODE_EDIT);
                if (value != null) {
                    Contenedor.Damage damage = gson.fromJson(value, Contenedor.Damage.class);
                    if (damage != null) {
                        load(damage);
                    }
                }
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            backActivity(getString(R.string.error_app));
        }
    }

    private void goDrawingView(File file) {
        if (file != null) {
            Bundle bundle = new Bundle();
            bundle.putString(DrawingView.PATH_FILE_VIEW_PHOTO, file.getPath());

            Intent intent = new Intent(
                    RegisterDamageActivity.this, DrawingView.class);
            intent.putExtras(bundle);

            startActivityForResult(intent, DRAWING_PHOTO);
        }
    }

    @Nullable
    private File compress(Bitmap resource) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";

            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(imageFileName, ".png", storageDir);

            FileOutputStream fileOutputStream = new FileOutputStream(image);
            resource.compress(Bitmap.CompressFormat.PNG, 80, fileOutputStream);

            fileOutputStream.close();
            return image;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_formulario, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DRAWING_PHOTO) {
            if (imageSelected != null && !imageSelected.isEmpty()) {
                ImageView background = findViewById(R.id.background);
                Glide.with(this)
                        .load(imageSelected)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .fitCenter()
                        .signature(new StringSignature(UUID.randomUUID().toString()))
                        .into(background);
            }
        } else {
            if (data != null && data.getExtras() != null) {
                Bundle bundle = data.getExtras();
                if (resultCode == 1) {
                    idActividad = bundle.getLong("idVariable", -1L);

                    String tipoVariable = bundle.getString("tipoVariable");
                    String codigoVariable = bundle.getString("codigoVariable");
                    String descripcionVariable = bundle.getString("descripcionVariable");

                    isRequiereFotos = bundle.getBoolean("requierefotos");

                    if (tipoVariable != null && tipoVariable.equals("Gama")) {
                        actividad = String.format("%s - %s", codigoVariable, descripcionVariable);

                        TextInputEditText textInputEditText = findViewById(R.id.actividad);
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

    public void load(@NonNull Contenedor.Damage damage) {
        external = false;
        external = damage.getExternal();
        imageSelected = damage.getImagen();

        if (imageSelected != null && !imageSelected.isEmpty()) {
            ImageView background = findViewById(R.id.background);
            if (external) {
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setMessage(R.string.descargando_imagen)
                        .setCancelable(true)
                        .create();

                dialog.show();

                Glide.with(this)
                        .load(Uri.parse(imageSelected))
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .fitCenter()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                File file = compress(resource);
                                if (file != null) {
                                    external = false;
                                    imageSelected = file.getAbsolutePath();
                                    background.setImageBitmap(resource);
                                }
                                dialog.dismiss();
                            }
                        });
            } else {
                Glide.with(this)
                        .load(imageSelected)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .fitCenter()
                        .signature(new StringSignature(UUID.randomUUID().toString()))
                        .into(background);
            }
        }

        AutoCompleteTextView parteAutoCompleteTextView = findViewById(R.id.parte);
        parteAutoCompleteTextView.setText(damage.getParte(), false);

        if (damage.getIdparte() != null && damage.getParte() != null) {
            parteSelected = new Spinner(damage.getIdparte(), damage.getParte());
        }

        TextInputEditText localizacionView = findViewById(R.id.localizacion);
        localizacionView.setText(damage.getLocalizacion());

        AutoCompleteTextView elementAutoCompleteTextView = findViewById(R.id.element);
        elementAutoCompleteTextView.setText(damage.getElement(), false);

        if (damage.getIdelement() != null && damage.getElement() != null) {
            elementSelected = new Spinner(damage.getIdelement(), damage.getElement());
        }

        AutoCompleteTextView damageAutoCompleteTextView = findViewById(R.id.damage);
        damageAutoCompleteTextView.setText(damage.getDamage(), false);

        if (damage.getIddamage() != null && damage.getDamage() != null) {
            damagesSelected = new Spinner(damage.getIddamage(), damage.getDamage());
        }

        AutoCompleteTextView tipoFallaAutoCompleteTextView = findViewById(R.id.tipo_falla);
        tipoFallaAutoCompleteTextView.setText(damage.getTipo(), false);

        if (damage.getIdtipo() != null && damage.getTipo() != null) {
            tipoFallaSelected = new Spinner(damage.getIdtipo(), damage.getTipo());
        }

        AutoCompleteTextView reclasificacionAutoCompleteTextView = findViewById(R.id.reclasificacion);
        reclasificacionAutoCompleteTextView.setText(damage.getReclasificacion(), false);

        if (damage.getIdreclasificacion() != null && damage.getReclasificacion() != null) {
            reclasificacionSelected = new Spinner(
                    damage.getIdreclasificacion(), damage.getReclasificacion());
            prepareClasification(false);
        }

        AutoCompleteTextView tiposAutoCompleteTextView = findViewById(R.id.tipo_reparacion);
        tiposAutoCompleteTextView.setText(damage.getTiporeparacion(), false);

        if (damage.getIdtiporeparacion() != null && damage.getTiporeparacion() != null) {
            tipoReparacionSelected = new Spinner(
                    damage.getIdtiporeparacion(), damage.getTiporeparacion());
            prepareRepair(false);
        }

        AutoCompleteTextView subTipoAutoCompleteTextView = findViewById(R.id.subtipo_reparacion);
        subTipoAutoCompleteTextView.setText(damage.getSubtiporeparacion(), false);

        if (damage.getIdsubtiporeparacion() != null && damage.getSubtiporeparacion() != null) {
            subtipoReparacionSelected = new Spinner(
                    damage.getIdsubtiporeparacion(), damage.getSubtiporeparacion());
        }

        TextInputEditText actividadTextView = findViewById(R.id.actividad);
        actividadTextView.setText(damage.getActividad());

        id = damage.getId();
        idActividad = Long.valueOf(damage.getIdactividad());
        actividad = damage.getActividad();

        TextInputEditText descriptionView = findViewById(R.id.descripcion);
        descriptionView.setText(damage.getDescripcion());

        TextInputEditText lengthView = findViewById(R.id.length);
        lengthView.setText(damage.getLenght());

        TextInputEditText heightView = findViewById(R.id.height);
        heightView.setText(damage.getHeight());

        if (damage.getPhotos() != null) {
            photos.addAll(Photo.factory(this, damage.getPhotos()));
        }

        if (damage.getToken() != null) {
            token = damage.getToken();
        }

        isRequiereFotos = damage.getRequiereFotos();
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

    private boolean esRequeridoAnchoLargo() {
        if (damagesSelected == null || damagesSelected.getKey() == null) {
            return false;
        }

        DamageCode damagecode = database.where(DamageCode.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("id", Integer.valueOf(damagesSelected.getKey()))
                .findFirst();

        if (damagecode == null) {
            return false;
        }

        return damagecode.isRequire();
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

        TextInputLayout parteContenedorView = findViewById(R.id.parte_contenedor);
        parteContenedorView.setErrorEnabled(false);
        if (parteSelected == null) {
            isOk = false;
            parteContenedorView.setError(getString(R.string.numero_parte_requerido));
            parteContenedorView.setErrorEnabled(true);
        }

        TextInputLayout textInputLayout = findViewById(R.id.actividad_contenedor);
        textInputLayout.setErrorEnabled(false);
        if (idActividad == null || idActividad <= 0) {
            isOk = false;
            textInputLayout.setError(getString(R.string.actividad_requerida));
            textInputLayout.setErrorEnabled(true);
        }

        TextInputLayout elementInputLayout = findViewById(R.id.element_contenedor);
        elementInputLayout.setErrorEnabled(false);
        if (elementSelected == null) {
            isOk = false;
            elementInputLayout.setError(getString(R.string.element_requerido));
            elementInputLayout.setErrorEnabled(true);
        }

        TextInputLayout damageInputLayout = findViewById(R.id.damage_contenedor);
        damageInputLayout.setErrorEnabled(false);
        if (elementSelected == null) {
            isOk = false;
            damageInputLayout.setError(getString(R.string.damage_requerido));
            damageInputLayout.setErrorEnabled(true);
        }

        String localizacion = null;
        TextInputEditText localizacionView = findViewById(R.id.localizacion);
        if (localizacionView != null && localizacionView.getText() != null) {
            localizacion = localizacionView.getText().toString();
        }

        TextInputLayout localizacionContenedorView = findViewById(R.id.localizacion_contenedor);
        localizacionContenedorView.setErrorEnabled(false);

        if (localizacion == null || localizacion.isEmpty()) {
            isOk = false;
            localizacionContenedorView.setError(getString(R.string.localizacion_requerida));
            localizacionContenedorView.setErrorEnabled(true);
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

        if (isRequiereFotos) {
            if (photos.isEmpty()) {
                Snackbar.make(getView(), R.string.fotografia_requerida, Snackbar.LENGTH_LONG)
                        .show();
                return;
            }
        }

        String length = null;
        TextInputEditText lenghtView = findViewById(R.id.length);
        if (lenghtView != null && lenghtView.getText() != null) {
            length = lenghtView.getText().toString();
        }

        TextInputLayout lenghtContenedorView = findViewById(R.id.length_contenedor);
        lenghtContenedorView.setErrorEnabled(false);

        String height = null;
        TextInputEditText heightView = findViewById(R.id.height);
        if (heightView != null && heightView.getText() != null) {
            height = heightView.getText().toString();
        }

        TextInputLayout heightContenedorView = findViewById(R.id.height_contenedor);
        heightContenedorView.setErrorEnabled(false);

        if (esRequeridoAnchoLargo()) {
            if (length == null || length.isEmpty()) {
                isOk = false;
                lenghtContenedorView.setErrorEnabled(true);
                lenghtContenedorView.setError(getString(R.string.lenght_requerida));
            }

            if (height == null || height.isEmpty()) {
                isOk = false;
                heightContenedorView.setErrorEnabled(true);
                heightContenedorView.setError(getString(R.string.height_requerida));
            }
        }

        if (!isOk) {
            return;
        }

        Contenedor.Damage damage = new Contenedor.Damage();
        damage.setId(id);
        damage.setIdtipo(tipoFallaSelected.getKey());
        damage.setTipo(tipoFallaSelected.getValue());
        damage.setIdparte(parteSelected.getKey());
        damage.setParte(parteSelected.getValue());
        damage.setLocalizacion(localizacion);

        if (damagesSelected != null) {
            damage.setIddamage(damagesSelected.getKey());
            damage.setDamage(damagesSelected.getValue());
        }

        if (elementSelected != null) {
            damage.setIdelement(elementSelected.getKey());
            damage.setElement(elementSelected.getValue());
        }

        damage.setHeight(height);
        damage.setLenght(length);

        if (reclasificacionSelected != null) {
            damage.setIdreclasificacion(reclasificacionSelected.getKey());
            damage.setReclasificacion(reclasificacionSelected.getValue());
        }

        if (tipoReparacionSelected != null) {
            damage.setIdtiporeparacion(tipoReparacionSelected.getKey());
            damage.setTiporeparacion(tipoReparacionSelected.getValue());
        }

        if (subtipoReparacionSelected != null) {
            damage.setIdsubtiporeparacion(subtipoReparacionSelected.getKey());
            damage.setSubtiporeparacion(subtipoReparacionSelected.getValue());
        }

        damage.setImagen(imageSelected);
        damage.setIdactividad(String.valueOf(idActividad));
        damage.setActividad(actividad);
        damage.setDescripcion(description);
        damage.setPhotos(PhotoAdapter.factory(photos));
        damage.setRequiereFotos(isRequiereFotos);
        damage.setToken(token);

        Intent intent = new Intent();
        intent.putExtra(Mantum.KEY_ID, damage);
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
