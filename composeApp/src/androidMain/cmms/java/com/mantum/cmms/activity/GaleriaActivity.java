package com.mantum.cmms.activity;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.Formatter;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.GsonBuilder;
import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Archivo;
import com.mantum.cmms.domain.Coordenada;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.entity.parameter.UserParameter;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.cmms.service.TransaccionService;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.OnSelected;
import com.mantum.component.activity.FullScreenView;
import com.mantum.component.adapter.CategoryAdapter;
import com.mantum.component.adapter.FormatoAdapter;
import com.mantum.component.adapter.GalleryAdapter;
import com.mantum.component.component.Progress;
import com.mantum.component.service.Camera;
import com.mantum.component.service.Geolocation;
import com.mantum.component.service.Photo;
import com.mantum.component.service.PhotoAdapter;
import com.mantum.component.service.handler.OnLocationListener;
import com.mantum.core.service.Permission;
import com.mantum.cmms.service.FormatoService;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Sort;

import static com.mantum.cmms.entity.parameter.UserPermission.IMAGE_GPS_ENABLE;
import static com.mantum.cmms.entity.parameter.UserPermission.IMAGE_GPS_SOLICITE;
import static com.mantum.cmms.entity.parameter.UserPermission.INCLUDE_IMAGE_GALLERY;
import static com.mantum.cmms.entity.parameter.UserPermission.LAST_KNOWN_LOCATION;
import static com.mantum.cmms.entity.parameter.UserPermission.PERMITE_MODIFICAR_NOMBRE_IMAGEN;
import static com.mantum.component.Mantum.KEY_ID;
import static com.mantum.component.activity.FullScreenView.AFTER_NAME_FILE_VIEW_PHOTO;
import static com.mantum.component.activity.FullScreenView.BEFORE_NAME_FILE_VIEW_PHOTO;
import static com.mantum.component.activity.FullScreenView.DEFAULT_FILE_VIEW_PHOTO;
import static com.mantum.component.activity.FullScreenView.DESCRIPTION_PHOTO;
import static com.mantum.component.activity.FullScreenView.PATH_FILE_VIEW_PHOTO;
import static com.mantum.component.activity.FullScreenView.REQUEST_VIEW_PHOTO;
import static com.mantum.component.service.Camera.REQUEST_TAKE_PHOTO;

public class GaleriaActivity extends Mantum.Activity {

    private final static String TAG = GaleriaActivity.class.getSimpleName();

    public static final int REQUEST_ACTION = 1228;

    private static final int REQUEST_GALLERY_ACTION = 1400;

    private static final int REQUEST_FILES_ACTION = 1500;

    private final static int COUNT_DOWN_INTERVAL = 1000;

    @Deprecated
    public static final String PATH_FILE = "path_file";

    public static final String PATH_FILE_PARCELABLE = "path_file_parcelable";

    public static final String KEY_TIPO_ENTIDAD = "tipo_entidad";

    public static final String KEY_CATEGORIAS = "categorias";

    public static final String KEY_MOSTRAR_ACCION_GALERIA = "mostrar_accion_galeria";

    public static final String KEY_LIMITE_PESO_ARCHIVOS = "limite_peso_archivos";

    private Camera camera;

    private Long idEntidad;

    private boolean isPreviousPhotosList;

    private Long limitePesoArchivos = null;

    private Database database;

    private String tipoEntidad;

    private Progress progress;

    private Location location;

    protected Geolocation geolocation;

    private CountDownTimer countDownTimer;

    private GalleryAdapter<Photo> galleryAdapter;

    private TransaccionService transaccionService;

    private final List<Photo> photos = new ArrayList<>();

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_view);

            database = new Database(this);
            includeBackButtonAndTitle(R.string.accion_galeria);

            camera = new Camera(this);
            Camera.requestPermission(this);

            progress = new Progress(this);
            galleryAdapter = new GalleryAdapter<>(this);
            transaccionService = new TransaccionService(this);

            if (Version.check(this, 7)
                    && (UserPermission.check(this, IMAGE_GPS_SOLICITE)
                    || UserPermission.check(this, IMAGE_GPS_ENABLE))) {

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

                            camera.capture();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (throwable != null) {
                            Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                                    .show();
                        }
                    }

                });

                int millisInFuture = UserParameter.NUMBER_SECONDS_TIMER_GPS;
                String milisecondsGPSString = UserParameter.getValue(
                        this, UserParameter.SECONDS_TIMER_GPS);
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
                        location = geolocation.getLastKnownLocation();
                        if (UserPermission.check(GaleriaActivity.this, LAST_KNOWN_LOCATION)) {
                            location = null; // Requiere una nueva ubicación
                        }

                        if (location == null) {
                            if (progress != null && progress.isShowing()) {
                                progress.hidden();
                            }

                            AlertDialog.Builder alertDialogBuilder
                                    = new AlertDialog.Builder(GaleriaActivity.this);
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

                            if (!UserPermission.check(GaleriaActivity.this, IMAGE_GPS_ENABLE)) {
                                alertDialogBuilder.setMessage(R.string.ubicacion_error_continuar);
                                alertDialogBuilder.setPositiveButton(getString(R.string.ubicacion_continuar), (dialogInterface, i) -> {
                                    capture();
                                    dialogInterface.cancel();
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
                                    backActivity();
                                });
                            }

                            if (!isFinishing()) {
                                alertDialogBuilder.show();
                            }

                            return;
                        }

                        capture();
                    }
                };

                long diffMinutes = 0;
                location = geolocation.getLastKnownLocation();
                if (location != null) {
                    long now = Calendar.getInstance().getTime().getTime();
                    diffMinutes = ((now - location.getTime()) / 1000) / 60;
                }

                if (location == null || diffMinutes >= 10) {
                    Cuenta cuenta = database.where(Cuenta.class)
                            .equalTo("active", true)
                            .findFirst();

                    if (cuenta == null) {
                        Snackbar.make(getView(), R.string.error_authentication, Snackbar.LENGTH_LONG)
                                .show();
                        return;
                    }

                    List<Transaccion> transaccions = database.where(Transaccion.class)
                            .equalTo("modulo", Transaccion.MODULO_GEOLOCALIZACION)
                            .equalTo("accion", Transaccion.ACCION_UBICACION)
                            .sort("creation", Sort.DESCENDING)
                            .findAll();

                    if (transaccions != null && transaccions.size() > 0) {
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

                            long now = Calendar.getInstance().getTime().getTime();
                            diffMinutes = ((now - location.getTime()) / 1000) / 60;
                        }
                    }
                }

                if (location == null || diffMinutes >= 10) {
                    geolocation.start(0, 0);
                    progress.show(R.string.titulo_ubicacion_progreso,
                            R.string.mensaje_ubicacion_progreso);
                    countDownTimer.start();
                }
            }

            FloatingActionButton floatingActionGalleryButton = findViewById(R.id.image);
            floatingActionGalleryButton.setOnClickListener(v -> {
                if (requestPermissions()) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);

                    startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST_GALLERY_ACTION);
                }
            });

            if (UserPermission.check(this, INCLUDE_IMAGE_GALLERY)) {
                floatingActionGalleryButton.setVisibility(View.GONE);
            }

            FloatingActionButton floatingActionCameraButton = findViewById(R.id.camera);
            floatingActionCameraButton.setOnClickListener(v -> camera.capture());

            FloatingActionButton floatingActionFilesButton = findViewById(R.id.file);
            floatingActionFilesButton.setOnClickListener(v -> {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("application/pdf");

                    startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_FILES_ACTION);
                } else {
                    if (requestPermissions()) {
                        Intent intent = new Intent();
                        intent.setType("*/*");
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                        intent.setAction(Intent.ACTION_GET_CONTENT);

                        startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_FILES_ACTION);
                    }
                }
            });

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                idEntidad = bundle.getLong(KEY_ID);
                idEntidad = idEntidad != 0 ? idEntidad : null;

                tipoEntidad = bundle.getString(KEY_TIPO_ENTIDAD);
                List<String> paths = bundle.getStringArrayList(PATH_FILE);
                if (paths != null) {
                    for (String file : paths) {
                        Photo photo = new Photo(this, new File(file));
                        photos.add(photo);
                        galleryAdapter.add(photo);
                    }
                }

                SparseArray<PhotoAdapter> photoAdapterSparseArray = bundle.getSparseParcelableArray(PATH_FILE_PARCELABLE);
                if (photoAdapterSparseArray != null) {
                    int total = photoAdapterSparseArray.size();
                    for (int i = 0; i < total; i++) {

                        Photo photo;
                        PhotoAdapter photoAdapter = photoAdapterSparseArray.get(i);
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
                        galleryAdapter.add(photo);
                    }
                }

                isPreviousPhotosList = bundle.getBoolean("isPreviousPhotosList");

                boolean mostrarAccionGaleria = bundle.getBoolean(KEY_MOSTRAR_ACCION_GALERIA, true);
                if (!mostrarAccionGaleria) {
                    floatingActionGalleryButton.setVisibility(View.GONE);
                    floatingActionFilesButton.setVisibility(View.GONE);
                }

                limitePesoArchivos = bundle.getLong(KEY_LIMITE_PESO_ARCHIVOS);
            }

            boolean isNameEnabled = UserPermission.check(
                    this, PERMITE_MODIFICAR_NOMBRE_IMAGEN, true);

            FormatoService formatoService = new FormatoService(this);
            List<FormatoAdapter> formatosPlanos = formatoService.getFormatosImagenes();
            List<CategoryAdapter> allCategories = formatoService.getAllCategoriesImagenes();

            galleryAdapter.setOnAction(new OnSelected<Photo>() {

                @Override
                public void onClick(Photo value, int position) {
                    if (value.isExternal() && value.getUri() != null) {
                        Bundle self = new Bundle();
                        self.putString(FullScreenView.PATH_URI_VIEW_PHOTO, value.getUri().toString());
                        self.putBoolean(FullScreenView.EDIT_MODE, false);

                        Intent intent = new Intent(GaleriaActivity.this, FullScreenView.class);
                        intent.putExtras(self);
                        startActivityForResult(intent, REQUEST_VIEW_PHOTO);
                        return;
                    }

                    if (value.getMime() != null && value.getMime().startsWith("image/")) {
                        Bundle self = new Bundle();
                        self.putString(PATH_FILE_VIEW_PHOTO, value.getPath());
                        self.putBoolean(DEFAULT_FILE_VIEW_PHOTO, value.isDefaultImage());
                        self.putLong(FullScreenView.ID_CATEGORY_PHOTO, value.getIdCategory() != null ? value.getIdCategory() : -1);
                        self.putString(DESCRIPTION_PHOTO, value.getDescription());
                        self.putBoolean(FullScreenView.NAME_ENABLED, isNameEnabled);

                        self.putSerializable(FullScreenView.FORMATOS, (Serializable) formatosPlanos);
                        self.putSerializable(FullScreenView.ALL_CATEGORIES, (Serializable) allCategories);

                        Intent intent = new Intent(GaleriaActivity.this, FullScreenView.class);
                        intent.putExtras(self);
                        startActivityForResult(intent, REQUEST_VIEW_PHOTO);
                    } else {
                        Snackbar.make(getView(), R.string.no_image, Snackbar.LENGTH_SHORT).show();
                    }
                }

                @Override
                public boolean onLongClick(Photo value, int position) {
                    if (value.isExternal() && value.getUri() != null) {
                        Snackbar.make(getView(), "Este archivo no se puede borrar ya que no se encuentra en su dispositivo", Snackbar.LENGTH_SHORT).show();
                        return true;
                    }

                    AlertDialog.Builder builder
                            = new AlertDialog.Builder(GaleriaActivity.this);
                    builder.setTitle(R.string.titulo_remover_archivo);
                    builder.setMessage(R.string.mensaje_remover_archivo);
                    builder.setCancelable(true);
                    builder.setNegativeButton(R.string.aceptar, (dialog, which) -> {
                        photos.remove(position);
                        galleryAdapter.remove(position, true);
                        galleryAdapter.showMessageEmpty(getView());

                        setTextViewInformation(false);
                    });
                    builder.setPositiveButton(R.string.cancelar, (dialog, which) -> dialog.dismiss());

                    if (!isFinishing()) {
                        builder.create();
                        builder.show();
                    }
                    return true;
                }

            });

            RecyclerView.LayoutManager layoutManager
                    = new GridLayoutManager(this, 2);
            galleryAdapter.startAdapter(getView(), layoutManager);
            galleryAdapter.showMessageEmpty(getView());

            Permission.storage(this);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            backActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            switch (requestCode) {
                case REQUEST_GALLERY_ACTION:
                case REQUEST_FILES_ACTION:
                    if (data == null) {
                        break;
                    }

                    if (data.getClipData() != null) {
                        ClipData clipData = data.getClipData();

                        int total = clipData.getItemCount();
                        for (int i = 0; i < total; i++) {
                            ClipData.Item item = clipData.getItemAt(i);

                            if (item.getUri().getPath() != null) {
                                Photo newPhoto = new Photo(this, item.getUri());
                                photos.add(newPhoto);
                                galleryAdapter.add(newPhoto, false);
                            }
                        }

                        galleryAdapter.notifyDataSetChanged();
                        galleryAdapter.showMessageEmpty(getView());
                    } else {
                        if (data.getData() != null) {
                            Uri selected = data.getData();
                            if (selected != null) {
                                Photo newPhoto = new Photo(this, selected);
                                photos.add(newPhoto);

                                galleryAdapter.add(newPhoto, true);
                                galleryAdapter.showMessageEmpty(getView());
                            }
                        }
                    }

                    break;

                case REQUEST_TAKE_PHOTO:
                    Photo newPhoto = camera.includeGallery(location);
                    if (newPhoto != null) {
                        photos.add(newPhoto);

                        galleryAdapter.add(newPhoto, true);
                        galleryAdapter.showMessageEmpty(getView());

                        camera.revokePermission();
                        if (geolocation != null) {
                            geolocation.stop();
                        }
                    }
                    break;

                case REQUEST_VIEW_PHOTO:
                    if (data == null) {
                        break;
                    }

                    String beforeName = data.getStringExtra(BEFORE_NAME_FILE_VIEW_PHOTO);
                    String afterName = data.getStringExtra(AFTER_NAME_FILE_VIEW_PHOTO);

                    boolean defaultImage
                            = data.getBooleanExtra(DEFAULT_FILE_VIEW_PHOTO, false);

                    Long idCategoria = data.getLongExtra(FullScreenView.ID_CATEGORY_PHOTO, -1);
                    idCategoria = idCategoria >= 0 ? idCategoria : null;

                    String description = data.getStringExtra(DESCRIPTION_PHOTO);

                    if (beforeName != null) {
                        int total = photos.size();
                        for (int i = 0; i < total; i++) {
                            Photo photo = photos.get(i);
                            if (photo.isExternal()) {
                                continue;
                            }

                            // Cambia el nombre
                            if (afterName != null && !beforeName.equals(afterName)
                                    && photo.getName() != null && photo.getName().equals(beforeName)) {
                                boolean change = photo.rename(afterName);
                                beforeName = afterName;
                                if (change) {
                                    photos.set(i, photo);
                                }
                            }

                            // Incluye la descripción
                            if (photo.getName() != null && photo.getName().equals(beforeName)) {
                                photo.setDescription(description);
                                photos.set(i, photo);
                            }

                            // Indica si la imagen es predeterminada
                            if (defaultImage) {
                                photo.setDefaultImage(false);
                                if (photo.getName() != null && photo.getName().equals(beforeName)) {
                                    photo.setDefaultImage(true);
                                    photos.set(i, photo);
                                }
                            } else {
                                if (photo.getName() != null && photo.getName().equals(beforeName) && photo.isDefaultImage()) {
                                    photo.setDefaultImage(false);
                                    photos.set(i, photo);
                                }
                            }

                            // Incluye la categoria, esta funcionalidad debe de ser la ultima
                            // ya que es necesario renombrar el archivo
                            if (photo.getName() != null && photo.getName().equals(beforeName)) {
                                photo.setIdCategory(idCategoria);
                                boolean change = photo.rename();
                                if (change) {
                                    photos.set(i, photo);
                                }
                            }
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
        } else if (itemId == R.id.action_done) {
            send();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        photos.clear();
        compositeDisposable.clear();

        if (transaccionService != null) {
            transaccionService.close();
        }

        if (database != null) {
            database.close();
        }

        if (geolocation != null) {
            geolocation.stop();
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_formulario, menu);

        MenuItem done = menu.findItem(R.id.action_done);
        done.setVisible(idEntidad != null);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (limitePesoArchivos != null && limitePesoArchivos > 0 && !photos.isEmpty()) {
            long listSize = 0;
            for (Photo file : photos) {
                if (file.getFile() != null) {
                    listSize += file.getFile().length();
                }
            }

            if (listSize > limitePesoArchivos) {
                dialogLimitePesoArchivos();
                return;
            }
        }

        Bundle bundle = new Bundle();
        bundle.putSparseParcelableArray(PATH_FILE_PARCELABLE,
                PhotoAdapter.factory(galleryAdapter.getOriginal()));

        bundle.putBoolean("isPreviousPhotosList", isPreviousPhotosList);

        Intent intent = new Intent();
        intent.putExtra(PATH_FILE, Photo.paths(galleryAdapter.getOriginal()));
        intent.putExtras(bundle);
        backActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        setTextViewInformation(true);
    }

    private void setTextViewInformation(boolean agregarArchivo) {
        long listSize = 0;
        if (!photos.isEmpty()) {
            for (Photo file : photos) {
                if (file.getFile() != null) {
                    listSize += file.getFile().length();
                }
            }

            if (agregarArchivo && limitePesoArchivos != null && limitePesoArchivos > 0 && listSize > limitePesoArchivos) {
                dialogLimitePesoArchivos();
            }
        }

        TextView information = findViewById(R.id.information);
        information.setText(String.format("%s archivos agregados - %s", photos.size(), Formatter.formatShortFileSize(this, listSize)));
    }

    private void dialogLimitePesoArchivos() {
        new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.titulo_limite_peso_archivos)
                .setMessage(String.format(getString(R.string.mensaje_limite_peso_archivos), Formatter.formatShortFileSize(this, limitePesoArchivos)))
                .setNegativeButton(R.string.close, null)
                .show();
    }

    private void send() {
        if (photos.isEmpty()) {
            Snackbar.make(getView(), getString(R.string.listado_imagenes_vacio), Snackbar.LENGTH_LONG).show();
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

        List<Transaccion> transactions = new ArrayList<>();
        for (Photo photo : photos) {
            Archivo archivo = new Archivo();
            archivo.setIdEntidad(idEntidad);
            archivo.setEntidad(tipoEntidad);
            archivo.setPhotos(Collections.singletonList(photo));

            Transaccion transaccion = new Transaccion();
            transaccion.setUUID(UUID.randomUUID().toString());
            transaccion.setCuenta(cuenta);
            transaccion.setCreation(Calendar.getInstance().getTime());
            transaccion.setUrl(cuenta.getServidor().getUrl() + "/gestiondocumental/cargaarchivos/subir");
            transaccion.setValue(archivo.toJson());
            transaccion.setModulo(Transaccion.MODULO_ARCHIVO);
            transaccion.setAccion(Transaccion.ACCION_ARCHIVOS);
            transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);

            transactions.add(transaccion);
        }

        compositeDisposable.add(transaccionService.save(transactions)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(self -> {
                }, this::onError, this::onComplete));
    }

    private void onComplete() {
        backActivity(getString(R.string.success_file));
    }

    private void onError(Throwable throwable) {
        Snackbar.make(getView(), R.string.error_file, Snackbar.LENGTH_LONG)
                .show();
    }

    private void capture() {
        if (!isFinishing()) {
            if (progress != null && progress.isShowing()) {
                progress.hidden();
            }
            camera.capture();
        }
    }

    public boolean requestPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return readMediaImagesPermission();
        } else {
            return readExternalStoragePermission();
        }
    }

    private boolean readMediaImagesPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 3);
            return false;
        }
        return true;
    }

    private boolean readExternalStoragePermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
            return false;
        }
        return true;
    }
}