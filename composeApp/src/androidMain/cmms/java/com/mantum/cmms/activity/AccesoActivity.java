package com.mantum.cmms.activity;

import static com.mantum.component.Mantum.isConnectedOrConnecting;
import static com.mantum.component.Mantum.versionName;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Check;
import com.mantum.cmms.domain.QR;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Bodega;
import com.mantum.cmms.entity.Categoria;
import com.mantum.cmms.entity.Certificado;
import com.mantum.cmms.entity.Checklist;
import com.mantum.cmms.entity.ClasificacionCEM;
import com.mantum.cmms.entity.ClasificacionParo;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.DamageCode;
import com.mantum.cmms.entity.ElementCode;
import com.mantum.cmms.entity.Estado;
import com.mantum.cmms.entity.EstadoEquipo;
import com.mantum.cmms.entity.EstadosInspeccion;
import com.mantum.cmms.entity.Formato;
import com.mantum.cmms.entity.GamaCEM;
import com.mantum.cmms.entity.GroupCode;
import com.mantum.cmms.entity.InstalacionProcesoStandBy;
import com.mantum.cmms.entity.MarcaCEM;
import com.mantum.cmms.entity.ModeloCEM;
import com.mantum.cmms.entity.Modulos;
import com.mantum.cmms.entity.Parametro;
import com.mantum.cmms.entity.Parte;
import com.mantum.cmms.entity.Proposito;
import com.mantum.cmms.entity.Recorrido;
import com.mantum.cmms.entity.Seccion;
import com.mantum.cmms.entity.Servidor;
import com.mantum.cmms.entity.TipoContenedorCEM;
import com.mantum.cmms.entity.TipoFallaCEM;
import com.mantum.cmms.entity.TipoParo;
import com.mantum.cmms.entity.TipoTiempo;
import com.mantum.cmms.entity.parameter.Area;
import com.mantum.cmms.entity.parameter.LogBook;
import com.mantum.cmms.entity.parameter.OT;
import com.mantum.cmms.entity.parameter.SS;
import com.mantum.cmms.entity.parameter.UserParameter;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.cmms.net.ClientManager;
import com.mantum.cmms.service.ATNotificationService;
import com.mantum.cmms.service.AutenticarServices;
import com.mantum.cmms.service.CertificadoServices;
import com.mantum.cmms.task.AnsCounterTask;
import com.mantum.cmms.task.TransaccionTask;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.service.Photo;
import com.mantum.component.service.Resource;
import com.mantum.component.util.Timeout;
import com.mantum.core.service.Authentication;
import com.mantum.core.util.Cache;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class AccesoActivity extends Mantum.Activity {

    private static final String VERSION = "000";

    private static final String TAG = AccesoActivity.class.getSimpleName();

    private static final int REQUEST_GALLERY_ACTION = 1400;

    private EditText username;

    private EditText password;

    private Database database;

    private ProgressBar progress;

    private final Gson gson = new Gson();

    private AutenticarServices autenticarServices;

    private CertificadoServices certificadoServices;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final Authentication.Model authentication = Authentication.Model.getInstace();

    private ATNotificationService atNotificationService;

    private boolean access = false;
    private static final boolean MODE_DEVELOPMENT = false;
    //#ef6636
    public void setAccess(boolean access) {
        this.access = access;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_acceso);

            database = new Database(this);
            certificadoServices = new CertificadoServices(this);

            TextView information = findViewById(R.id.information);
            information.setText(String.format("%s - %s", getString(R.string.powerby), versionName(AccesoActivity.this)));

            progress = findViewById(R.id.progress_bar);
            progress.setVisibility(View.VISIBLE);

            username = findViewById(R.id.username);
            password = findViewById(R.id.password);
            password.setOnEditorActionListener((v, actionId, event) -> actionId == EditorInfo.IME_ACTION_UNSPECIFIED);

            AppCompatButton button = findViewById(R.id.access);
            button.setOnClickListener(v -> login());

            autenticarServices = new AutenticarServices(this);
            startService(new Intent(this, AnsCounterTask.class));

            atNotificationService = new ATNotificationService(this);

            // Escanear código QR
            TextView connect = findViewById(R.id.nfc);
            connect.setOnClickListener(v -> {
                if (progress.getVisibility() == View.VISIBLE) {
                    return;
                }

                if (!isConnectedOrConnecting(this)) {
                    Snackbar.make(getView(), getString(R.string.modo_offline), Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                AlertDialog qr = new AlertDialog.Builder(this)
                        .setTitle("Código QR")
                        .setMessage("Seleccione desde donde va a leer el código QR")
                        .setPositiveButton("Leer código QR desde la cámara", (current, which) -> {
                            IntentIntegrator integrator = new IntentIntegrator(AccesoActivity.this);
                            integrator.setOrientationLocked(true);
                            integrator.setCameraId(0);
                            integrator.setPrompt(getString(R.string.mensaje_ayuda_barcode));
                            integrator.setCaptureActivity(CaptureActivityPortrait.class);
                            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                            integrator.setBeepEnabled(false);
                            integrator.initiateScan();
                        })
                        .setNegativeButton("Leer código QR desde una imagen", (current, which) -> {
                            if (requestPermissions()) {
                                Intent intent = new Intent();
                                intent.setType("image/*");
                                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                                intent.setAction(Intent.ACTION_GET_CONTENT);

                                startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST_GALLERY_ACTION);
                            }
                        })
                        .setCancelable(true)
                        .create();

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 3);
                } else {
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle("Seleccionar medio de enlace")
                            .setMessage("Esto es necesario para determinar a que servidor se va a conectar la aplicación")
                            .setNegativeButton("Código QR", (current, which) -> {
                                progress.setVisibility(View.VISIBLE);
                                qr.show();
                            })
                            .setPositiveButton(R.string.certificado_waf, (current, which) -> {
                                Intent intent = new Intent(this, CertificadoActivity.class);
                                startActivityForResult(intent, CertificadoActivity.REQUEST_CODE);
                            })
                            .setCancelable(true)
                            .create();

                    dialog.show();

                }
            });

            // para el autologin
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta != null) { //se logea con usuario y contraseña pero sin pedir al servidors
                onSharedPreferences(cuenta);
                if (!isConnectedOrConnecting(this)) {
                    onComplete();
                    return;
                }

                Parametro parametro = database.where(Parametro.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();

                boolean ejecutar = parametro == null || !parametro.isEjecutado();
                compositeDisposable.add(check(true)
                        .flatMap(response -> setting(ejecutar || response.getBody(Check.class).isModified()))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::onNext, this::onError, this::onComplete));
                return;
            }

            if (MODE_DEVELOPMENT) {
                AppCompatButton logindev = findViewById(R.id.access_dev);
                logindev.setVisibility(View.VISIBLE);
                logindev.setOnClickListener(view -> {

                    if (progress.getVisibility() == View.VISIBLE) {
                        return;
                    }

                    progress.setVisibility(View.VISIBLE);
                    String json = readFile("login.json");
                    if (json == null) {
                        Snackbar.make(getView(), R.string.request_error, Snackbar.LENGTH_LONG)
                                .show();
                        return;
                    }

                    getBundleApp(json);
                });
            }

            progress.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.e(TAG, "onCreate: ", e);
            FirebaseCrashlytics.getInstance().recordException(e);

            new AlertDialog.Builder(this)
                    .setTitle(R.string.titulo_eliminar_datos)
                    .setMessage(R.string.notificar_problemas)
                    .setPositiveButton(R.string.boton_verificar_actualizacion, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Mantum.goPlayStore(AccesoActivity.this);
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    private boolean requestPermissions() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (resultCode == RESULT_OK) {
                if (requestCode == REQUEST_GALLERY_ACTION) {
                    progress.setVisibility(View.GONE);
                    if (data == null) {
                        return;
                    }

                    if (data.getData() != null) {
                        Uri selected = data.getData();
                        if (selected != null) {
                            Resource resource = new Resource(this, selected);
                            Bitmap image = BitmapFactory.decodeFile(resource.getPath());
                            if (image != null) {
                                String decoded = AccesoActivity.decodeQRCode(image);
                                getBundleApp(decoded);
                            }
                        }
                    }

                    return;
                }
            }

            if (requestCode == CertificadoActivity.REQUEST_CODE) {
                progress.setVisibility(View.VISIBLE);
                requestCode = IntentIntegrator.REQUEST_CODE;
            }

            IntentResult result = IntentIntegrator.parseActivityResult(
                    requestCode, resultCode, data);

            if (result != null) {
                if (result.getContents() == null) {
                    progress.setVisibility(View.GONE);
                    return;
                }

                getBundleApp(result.getContents());
            } else {
                super.onActivityResult(requestCode, resultCode, data);
                progress.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private static String decodeQRCode(@NonNull Bitmap image) throws Exception {
        IntBuffer intBuffer = IntBuffer.allocate(image.getWidth() * image.getHeight());
        image.copyPixelsToBuffer(intBuffer);

        LuminanceSource source = new RGBLuminanceSource(image.getWidth(), image.getHeight(), intBuffer.array());
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        MultiFormatReader reader = new MultiFormatReader();
        Result result = reader.decode(binaryBitmap);

        if (result != null) {
            return result.getText();
        } else {
            throw new Exception("QR code not found in image");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }

        if (autenticarServices != null) {
            autenticarServices.close();
        }

        if (certificadoServices != null) {
            certificadoServices.close();
        }

        if (atNotificationService != null) {
            atNotificationService.close();
        }

        compositeDisposable.clear();
    }

    private void getBundleApp(String json) {
        Bundle bundle = Mantum.bundle(this);
        if (bundle == null) {
            progress.setVisibility(View.GONE);
            Snackbar.make(getView(), getString(R.string.token_error), Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        String token = bundle.getString("Mantum.Authentication.Token");
        if (token == null) {
            progress.setVisibility(View.GONE);
            Snackbar.make(getView(), getString(R.string.token_error), Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        authenticate(json, token);
    }

    private void authenticate(String json, String token) {
        final String[] uuid = {null};
        QR qr = gson.fromJson(json, QR.class);
        String password = Mantum.md5(qr.getLogin() + token + qr.getPass());
        if (qr.getUrl() == null || qr.getUrl().isEmpty()) {
            progress.setVisibility(View.GONE);
            Snackbar.make(getView(), getString(R.string.token_error_qr), Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        compositeDisposable.add(autenticarServices.autenticar(qr.getUrl(), qr.getLogin(), password, qr.getBasename())
                .map(response -> {

                    // Servidor
                    Servidor servidor = new Servidor();
                    servidor.setUrl(qr.getUrl());
                    servidor.setNombre(qr.getBasename());
                    servidor.setNumero(qr.getBase());
                    servidor.setVersion(response.getVersion().toString());

                    // Cuenta auxiliar para traer cierta información
                    Cuenta.Request cuentaAux = response.getBody(Cuenta.Request.class);

                    // Objeto cuenta
                    Cuenta cuenta = new Cuenta();
                    cuenta.setId(cuentaAux.getId())
                            .setName(cuentaAux.getName())
                            .setLastname(cuentaAux.getLastname())
                            .setImage(cuentaAux.getImage());
                    cuenta.setUsername(qr.getLogin());
                    cuenta.setPassword(qr.getPass());
                    cuenta.setActive(true);
                    cuenta.setServidor(servidor);
                    cuenta.setActiveDirectory(cuentaAux.getActiveDirectory());

                    // Guardar
                    Cuenta newCuenta = onSave(cuenta, response.getVersion());
                    onSharedPreferences(newCuenta);

                    uuid[0] = newCuenta.getUUID();

                    Database database = new Database(this);
                    Parametro parametroaux = database.where(Parametro.class)
                            .equalTo("cuenta.UUID", uuid[0])
                            .findFirst();

                    if (parametroaux != null) {
                        database.executeTransaction(self -> parametroaux.setEjecutado(false));
                    }

                    if (cuentaAux.getParams() != null) {
                        processParam(cuentaAux.getParams());
                        processPermission(cuentaAux.getPermissions());
                    }

                    database.close();
                    return response;
                })
                .flatMap(response -> {
                    Database database = new Database(this);
                    Parametro parametro = database.where(Parametro.class)
                            .equalTo("cuenta.UUID", uuid[0])
                            .findFirst();

                    Observable<Response> check = check(parametro == null || !parametro.isEjecutado());
                    database.close();
                    return check;
                })
                .flatMap(response -> {
                    Database database = new Database(this);
                    Parametro parametro = database.where(Parametro.class)
                            .equalTo("cuenta.UUID", uuid[0])
                            .findFirst();

                    Check check = response.getBody(Check.class);
                    Observable<Response> setting = setting(parametro == null || !parametro.isEjecutado() || check.isModified());
                    database.close();
                    return setting;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNext, this::onError, this::onComplete));
    }

    private void login() {
        try {
            if (progress.getVisibility() == View.VISIBLE) {
                return;
            }

            closeKeyboard();
            TextInputLayout usernameContainer = findViewById(R.id.username_container);
            usernameContainer.setError(null);

            TextInputLayout passwordContainer = findViewById(R.id.password_container);
            passwordContainer.setError(null);

            String vUsername = username.getText().toString();
            if (vUsername.isEmpty()) {
                usernameContainer.setError(getString(R.string.login_username_empty));
                username.requestFocus();
                return;
            }

            String vPassword = password.getText().toString();
            if (vPassword.isEmpty()) {
                passwordContainer.setError(getString(R.string.login_password_empty));
                password.requestFocus();
                return;
            }

            Bundle bundle = Mantum.bundle(this);
            if (bundle == null) {
                Snackbar.make(getView(), getString(R.string.token_error), Snackbar.LENGTH_LONG)
                        .show();
                return;
            }

            String token = bundle.getString("Mantum.Authentication.Token");
            if (token == null) {
                Snackbar.make(getView(), getString(R.string.token_error), Snackbar.LENGTH_LONG)
                        .show();
                return;
            }

            if (getString(R.string.google_username).equals(vUsername) && getString(R.string.google_password).equals(vPassword)) {
                String json = readFile("google.json");
                if (json == null) {
                    Snackbar.make(getView(), R.string.request_error, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                getBundleApp(json);
                return;
            }

            progress.setVisibility(View.VISIBLE);
            database.executeTransaction(self -> {
                if (isConnectedOrConnecting(this)) {
                    //no pasa por bd, pero debe actualizar el usuario luego
                    Cuenta cuenta = database.where(Cuenta.class)
                            .equalTo("username", vUsername)
                            .findFirst();

                    if (cuenta == null) {
                        UserParameter ultimoServidor = database.where(UserParameter.class)
                                .equalTo("name", UserParameter.ULTIMO_SERVIDOR)
                                .findFirst();

                        UserParameter ultimoBaseName = database.where(UserParameter.class)
                                .equalTo("name", UserParameter.ULTIMO_BASE_NAME)
                                .findFirst();

                        if (ultimoServidor != null && ultimoBaseName != null) {
                            String url = ultimoServidor.getValue();
                            String baseName = ultimoBaseName.getValue();
                            final String[] uuid = {null};
                            final String md5 = Mantum.md5(vPassword);
                            String value = Mantum.md5(vUsername + token + md5);
                            compositeDisposable.add(autenticarServices.autenticar(url, vUsername, value, baseName)
                                    .map(response -> {
                                        // Servidor
                                        Database database = new Database(this);
                                        Servidor servidor = database.where(Servidor.class)
                                                .equalTo("url", url)
                                                .findFirst();

                                        // Cuenta auxiliar para traer cierta información
                                        Cuenta.Request cuentaAux = response.getBody(Cuenta.Request.class);

                                        // Objeto cuenta
                                        Cuenta newCuenta = new Cuenta();
                                        newCuenta.setId(cuentaAux.getId());
                                        newCuenta.setName(cuentaAux.getName());
                                        newCuenta.setLastname(cuentaAux.getLastname());
                                        newCuenta.setImage(cuentaAux.getImage());
                                        newCuenta.setUsername(vUsername);
                                        newCuenta.setPassword(md5);
                                        newCuenta.setActive(true);
                                        newCuenta.setServidor(servidor);
                                        newCuenta.setActiveDirectory(cuentaAux.getActiveDirectory());

                                        // Guardar
                                        Cuenta account = onSave(newCuenta, response.getVersion());
                                        onSharedPreferences(account);

                                        uuid[0] = account.getUUID();

                                        Parametro parametroaux = database.where(Parametro.class)
                                                .equalTo("cuenta.UUID", uuid[0])
                                                .findFirst();

                                        if (parametroaux != null) {
                                            database.executeTransaction(realm -> parametroaux.setEjecutado(false));
                                        }

                                        if (cuentaAux.getParams() != null) {
                                            processParam(cuentaAux.getParams());
                                            processPermission(cuentaAux.getPermissions());
                                        }

                                        database.close();
                                        return response;
                                    })
                                    .flatMap(response -> {
                                        Database database = new Database(this);
                                        Parametro parametro = database.where(Parametro.class)
                                                .equalTo("cuenta.UUID", uuid[0])
                                                .findFirst();

                                        Observable<Response> check = check(parametro == null || !parametro.isEjecutado());
                                        database.close();
                                        return check;
                                    })
                                    .flatMap(response -> {
                                        Database database = new Database(this);
                                        Parametro parametro = database.where(Parametro.class)
                                                .equalTo("cuenta.UUID", uuid[0])
                                                .findFirst();

                                        Check check = response.getBody(Check.class);
                                        Observable<Response> setting = setting(parametro == null || !parametro.isEjecutado() || check.isModified());
                                        database.close();
                                        return setting;
                                    })
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(this::onNext, this::onError, this::onComplete));
                        } else {
                            progress.setVisibility(View.GONE);
                            Snackbar.make(getView(), getString(R.string.authentication_error_url), Snackbar.LENGTH_LONG)
                                    .show();
                        }
                    } else {
                        cuenta.setActive(true);
                        onSharedPreferences(cuenta);

                        Parametro parametro = database.where(Parametro.class)
                                .equalTo("cuenta.UUID", cuenta.getUUID())
                                .findFirst();
                        final boolean verificar = parametro == null || !parametro.isEjecutado();
                        final String md5 = Mantum.md5(vPassword);
                        String value = Mantum.md5(cuenta.getUsername() + token + md5);

                        compositeDisposable.add(autenticarServices.autenticar(cuenta, value)
                                .flatMap(response -> check(verificar))
                                .flatMap(response -> setting(verificar || response.getBody(Check.class).isModified()))
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(this::onNext, this::onError, () -> {
                                    database.executeTransaction(realm -> cuenta.setPassword(md5));
                                    onComplete();
                                }));
                    }
                } else {
                    //pasa por bd al no tener internet
                    Cuenta cuenta = database.where(Cuenta.class)
                            .equalTo("username", vUsername)
                            .equalTo("password", Mantum.md5(vPassword))
                            .findFirst();

                    if (cuenta == null) {
                        progress.setVisibility(View.GONE);
                        Snackbar.make(getView(), getString(R.string.authentication_error_url), Snackbar.LENGTH_LONG)
                                .show();
                        return;
                    }

                    cuenta.setActive(true);
                    onSharedPreferences(cuenta);
                    onComplete();
                }
            });
        } catch (Exception e) {
            Snackbar.make(getView(), getString(R.string.authentication_error_resquest), Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private Observable<Response> check(boolean check) {
        return Observable.create(subscriber -> {
            if (!isConnectedOrConnecting(this)) {
                subscriber.onError(new Exception(getString(R.string.offline)));
                return;
            }
            if (!check) {
                subscriber.onComplete();
                return;
            }

            SharedPreferences sharedPreferences
                    = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            String url = sharedPreferences.getString(getString(R.string.mantum_url), "");

            sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            String token = sharedPreferences.getString(getString(R.string.mantum_token), "");

            sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            String username = sharedPreferences.getString(getString(R.string.mantum_username), "");

            sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            String basename = sharedPreferences.getString(getString(R.string.mantum_database_name), "");

            Certificado certificado = certificadoServices.find(url, username, basename);
            OkHttpClient client = ClientManager.prepare(
                    new OkHttpClient.Builder()
                            .connectTimeout(Timeout.CONNECT, TimeUnit.SECONDS)
                            .writeTimeout(Timeout.WRITE, TimeUnit.SECONDS)
                            .readTimeout(Timeout.READ, TimeUnit.SECONDS), certificado
            ).build();

            String endpoint = String.format("%s/restapp/app/checkmodifyparam", url);
            Request request = new Request.Builder().get().url(endpoint)
                    .addHeader("token", token)
                    .addHeader("cache-control", "no-cache")
                    .addHeader("accept-language", "application/json")
                    .addHeader("accept", Version.build(VERSION))
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!call.isCanceled() && !subscriber.isDisposed()) {
                        subscriber.onError(new Exception(getString(R.string.request_error)));
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                    if (call.isCanceled()) {
                        return;
                    }

                    ResponseBody body = response.body();
                    if (body == null) {
                        if (!call.isCanceled() && !subscriber.isDisposed()) {
                            subscriber.onError(new Exception(getString(R.string.setting_error_resquest)));
                        }
                        return;
                    }

                    String json = body.string();
                    try {
                        if (response.isSuccessful()) {
                            Response content = gson.fromJson(json, Response.class);
                            content.setVersion(response.header("Max-Version"));

                            subscriber.onNext(content);
                            subscriber.onComplete();
                        } else {
                            if (!subscriber.isDisposed()) {
                                subscriber.onError(new Exception(getString(R.string.setting_error)));
                            }
                        }
                    } catch (Exception e) {
                        if (!subscriber.isDisposed()) {
                            subscriber.onError(new Exception(getString(R.string.request_reading_error)));
                        }
                    }
                    response.close();
                }
            });
        });
    }

    private Observable<Response> setting(boolean check) {
        return Observable.create(subscriber -> {
            if (!isConnectedOrConnecting(this)) {
                subscriber.onError(new Exception(getString(R.string.offline)));
                return;
            }

            if (!check) {
                subscriber.onComplete();
                return;
            }

            SharedPreferences sharedPreferences
                    = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            String url = sharedPreferences.getString(getString(R.string.mantum_url), "");

            sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            String token = sharedPreferences.getString(getString(R.string.mantum_token), "");

            sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            String username = sharedPreferences.getString(getString(R.string.mantum_username), "");

            sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            String basename = sharedPreferences.getString(getString(R.string.mantum_database_name), "");

            Certificado certificado = certificadoServices.find(url, username, basename);
            OkHttpClient client = ClientManager.prepare(
                    new OkHttpClient.Builder()
                            .connectTimeout(Timeout.CONNECT, TimeUnit.SECONDS)
                            .writeTimeout(Timeout.WRITE, TimeUnit.SECONDS)
                            .readTimeout(Timeout.READ, TimeUnit.SECONDS), certificado
            ).build();

            String endpoint = String.format("%s/restapp/app/getparam", url);
            Request request = new Request.Builder().get().url(endpoint)
                    .addHeader("token", token)
                    .addHeader("cache-control", "no-cache")
                    .addHeader("accept-language", "application/json")
                    .addHeader("accept", Version.build(VERSION))
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!compositeDisposable.isDisposed()) {
                        subscriber.onError(new Exception(getString(R.string.request_error)));
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                    ResponseBody body = response.body();
                    if (body == null) {
                        subscriber.onError(new Exception(getString(R.string.authentication_error_resquest)));
                        return;
                    }

                    String json = body.string();
                    try {
                        if (response.isSuccessful()) {
                            Response content = gson.fromJson(json, Response.class);
                            content.setVersion(response.header("Max-Version"));

                            subscriber.onNext(content);
                            subscriber.onComplete();
                        } else {
                            subscriber.onError(new Exception(getString(R.string.setting_error)));
                        }
                    } catch (Exception e) {
                        subscriber.onError(new Exception(getString(R.string.request_reading_error)));
                    }
                    response.close();
                }
            });
        });
    }

    @NonNull
    private Cuenta onSave(@NonNull Cuenta cuenta, @Nullable Integer version) {
        Realm realm = new Database(this).instance();

        realm.beginTransaction();
        RealmResults<Cuenta> cuentas = realm.where(Cuenta.class).findAll();
        for (Cuenta temp : cuentas) {
            temp.setActive(false);
        }

        // Obtener el servidor ya registrado
        Servidor servidor = realm.where(Servidor.class)
                .equalTo("url", cuenta.getServidor().getUrl())
                .equalTo("numero", cuenta.getServidor().getNumero())
                .findFirst();

        // No existe
        if (servidor == null) {
            cuenta.getServidor().setUUID(UUID.randomUUID().toString());
        } else {
            // Nombre y versión del servidor
            servidor.setNombre(cuenta.getServidor().getNombre());
            servidor.setVersion(cuenta.getServidor().getVersion());
            cuenta.setServidor(servidor);
        }

        Cuenta register = realm.where(Cuenta.class)
                .equalTo("username", cuenta.getUsername())
                .equalTo("password", cuenta.getPassword())
                .equalTo("servidor.UUID", cuenta.getServidor().getUUID())
                .findFirst();

        if (register == null) {
            cuenta.setTimestamp(new Date());
            cuenta.setUUID(UUID.randomUUID().toString());
            cuenta.setActive(true);
            realm.insert(cuenta);
        } else {
            register.setImage(cuenta.getImage());
            register.setName(cuenta.getName());
            register.setLastname(cuenta.getLastname());
            register.setServidor(cuenta.getServidor());
            register.setActive(true);
            register.setActiveDirectory(cuenta.isActiveDirectory());

            cuenta = register;
        }

        realm.commitTransaction();

        Version.save(this, version);
        return cuenta;
    }

    @Deprecated
    private void onSharedPreferences(@NonNull Cuenta cuenta) {
        try {
            Bundle bundle = Mantum.bundle(this);
            if (bundle == null) {
                progress.setVisibility(View.GONE);
                Snackbar.make(getView(), getString(R.string.token_error), Snackbar.LENGTH_LONG)
                        .show();
                return;
            }

            String token = bundle.getString("Mantum.Authentication.Token");
            if (token == null) {
                progress.setVisibility(View.GONE);
                Snackbar.make(getView(), getString(R.string.token_error), Snackbar.LENGTH_LONG)
                        .show();
                return;
            }

            // TODO: PENDIENTE REMOVER --->
            String password = Mantum.md5(cuenta.getUsername() + token + cuenta.getPassword());
            SharedPreferences.Editor editor
                    = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit();
            editor.putString(getString(R.string.mantum_token), password);
            editor.putString(getString(R.string.mantum_username), cuenta.getUsername());
            editor.putString(getString(R.string.mantum_account), cuenta.getUUID());
            editor.putString(getString(R.string.mantum_url), cuenta.getServidor().getUrl());
            editor.putString(getString(R.string.mantum_database_name), cuenta.getServidor().getNombre());
            editor.apply();

            Cache.getInstance()
                    .add("url", cuenta.getServidor().getUrl())
                    .add("database", cuenta.getServidor().getNombre());

            authentication.setUUID(cuenta.getUUID());
            authentication.setId(cuenta.getId());
            authentication.setToken(password);
            authentication.setNumero(cuenta.getServidor().getNumero());
            authentication.setUrl(cuenta.getServidor().getUrl());
            authentication.setDatabase(cuenta.getServidor().getNombre());
            // TODO: PENDIENTE REMOVER --->
        } catch (Exception e) {
            Log.e(TAG, "onSharedPreferences: ", e);
        }
    }

    private void onNext(@NonNull Response response) {
        // Guardar la versión
        Version.save(this, response.getVersion());

        // Cargar información del usuario
        Parametro.Request request = response.getBody(Parametro.Request.class);
        processParam(request);
    }

    private void processParam(Parametro.Request request) {
        Database database = new Database(this);
        database.executeTransaction(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                return;
            }

            if (request.getCategories() != null) {
                RealmResults<Categoria> current = self.where(Categoria.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findAll();

                current.deleteAllFromRealm();
                for (Categoria categoria : request.getCategories()) {
                    categoria.setUuid(UUID.randomUUID().toString());
                    categoria.setCuenta(cuenta);
                    self.insert(categoria);
                }
            }

            if (request.getFormatos() != null) {
                // clean
                self.where(Formato.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findAll()
                        .deleteAllFromRealm();

                // save
                for (Formato formato : request.getFormatos()) {
                    formato.setUuid(UUID.randomUUID().toString());
                    formato.setCuenta(cuenta);
                    self.insert(formato);
                }
            }

            if (request.getEstadoModulos() != null) {
                self.where(Modulos.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findAll()
                        .deleteAllFromRealm();

                Modulos modulos = request.getEstadoModulos();
                if (modulos != null) {
                    modulos.setCuenta(cuenta);
                    modulos.setUUID(UUID.randomUUID().toString());
                    self.insert(modulos);
                }
            }

            if (request.getAreas() != null) {
                RealmResults<Area> current = self.where(Area.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findAll();

                for (Area area : current) {
                    area.getTypes().deleteAllFromRealm();
                }

                current.deleteAllFromRealm();
                for (Area area : request.getAreas()) {
                    area.setUUID(UUID.randomUUID().toString());
                    area.setCuenta(cuenta);
                    self.insert(area);
                }
            }

            if (request.getParametros() != null) {
                for (UserParameter parameters : request.getParametros()) {
                    UserParameter element = self.where(UserParameter.class)
                            .equalTo("name", parameters.getName())
                            .equalTo("cuenta.UUID", cuenta.getUUID())
                            .findFirst();
                    if (element == null) {
                        parameters.setCuenta(cuenta);
                        self.insert(parameters);
                    } else {
                        element.setName(parameters.getName());
                        element.setValue(parameters.getValue());
                    }
                }
            }

            RealmResults<LogBook> current = self.where(LogBook.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findAll();

            for (LogBook logBook : current) {
                logBook.getEventtype().deleteAllFromRealm();
                logBook.getTiempos().deleteAllFromRealm();
            }

            current.deleteAllFromRealm();
            LogBook logBook = request.getLogBook();
            if (logBook != null) {
                for (TipoTiempo tipoTiempo : logBook.getTiempos()) {
                    tipoTiempo.setCuenta(cuenta);
                    tipoTiempo.setUuid(UUID.randomUUID().toString());
                }

                logBook.setUUID(UUID.randomUUID().toString());
                logBook.setCuenta(cuenta);
                self.insert(logBook);
            }

            self.where(OT.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findAll()
                    .deleteAllFromRealm();

            OT ot = request.getOt();
            if (ot != null) {
                ot.setUUID(UUID.randomUUID().toString());
                ot.setCuenta(cuenta);
                self.insert(ot);
            }

            if (request.getEstados() != null) {
                self.where(Estado.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findAll()
                        .deleteAllFromRealm();

                for (Estado estado : request.getEstados()) {
                    estado.setUuid(UUID.randomUUID().toString());
                    estado.setCuenta(cuenta);
                    self.insert(estado);
                }
            }

            if (request.getEstadoEquipos() != null) {
                self.where(EstadoEquipo.class)
                        .findAll()
                        .deleteAllFromRealm();

                for (EstadoEquipo estadoEquipo : request.getEstadoEquipos()) {
                    self.insert(estadoEquipo);
                }
            }

            self.where(Bodega.class)
                    .findAll().deleteAllFromRealm();

            if (request.getBodegas() != null) {

                for (Bodega bodega : request.getBodegas()) {
                    bodega.setCuenta(cuenta);
                    self.insert(bodega);
                }
            }

            self.where(InstalacionProcesoStandBy.class)
                    .findAll().deleteAllFromRealm();

            if (request.getStandbyprocesos() != null) {

                for (InstalacionProcesoStandBy proceso : request.getStandbyprocesos()) {
                    proceso.setCuenta(cuenta);
                    self.insert(proceso);
                }
            }

            self.where(SS.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findAll().deleteAllFromRealm();

            SS ss = request.getSs();
            if (ss != null) {
                ss.setUUID(UUID.randomUUID().toString());
                ss.setCuenta(cuenta);
                self.insert(ss);
            }

            Parametro parametro = self.where(Parametro.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findFirst();

            if (parametro == null) {
                parametro = new Parametro();
                parametro.setUUID(UUID.randomUUID().toString());
                parametro.setCuenta(cuenta);
                parametro.setEjecutado(true);
                self.insert(parametro);
            } else if (!parametro.isEjecutado()) {
                parametro.setEjecutado(true);
            }

            self.where(Checklist.class)
                    .findAll()
                    .deleteAllFromRealm();

            self.where(Seccion.class)
                    .findAll()
                    .deleteAllFromRealm();

            if (request.getSecciones() != null) {
                for (Seccion seccion : request.getSecciones()) {
                    seccion.setCuenta(cuenta);
                    self.insert(seccion);
                }
            }

            self.where(GroupCode.class)
                    .findAll()
                    .deleteAllFromRealm();

            if (request.getGroupcode() != null) {
                for (GroupCode group : request.getGroupcode()) {
                    group.setCuenta(cuenta);
                    self.insert(group);
                }
            }

            self.where(ClasificacionParo.class)
                    .findAll()
                    .deleteAllFromRealm();

            if (request.getClasificacionparos() != null) {
                for (ClasificacionParo clasificacionParo : request.getClasificacionparos()) {
                    clasificacionParo.setCuenta(cuenta);
                    self.insert(clasificacionParo);
                }
            }

            self.where(TipoParo.class)
                    .findAll()
                    .deleteAllFromRealm();

            if (request.getTipoparos() != null) {
                for (TipoParo tipoParo : request.getTipoparos()) {
                    tipoParo.setCuenta(cuenta);
                    self.insert(tipoParo);
                }
            }

            self.where(Proposito.class)
                    .findAll()
                    .deleteAllFromRealm();

            if (request.getPropositos() != null) {
                for (Proposito proposito : request.getPropositos()) {
                    proposito.setCuenta(cuenta);
                    self.insert(proposito);
                }
            }

            self.where(EstadosInspeccion.class)
                    .findAll()
                    .deleteAllFromRealm();

            if (request.getEstadosinspeccion() != null) {
                for (EstadosInspeccion estadosInspeccion : request.getEstadosinspeccion()) {
                    estadosInspeccion.setCuenta(cuenta);
                    self.insert(estadosInspeccion);
                }
            }

            self.where(Parte.class)
                    .findAll()
                    .deleteAllFromRealm();

            if (request.getPartes() != null) {
                for (Parte parte : request.getPartes()) {
                    parte.setCuenta(cuenta);
                    self.insert(parte);
                }
            }

            self.where(MarcaCEM.class)
                    .findAll()
                    .deleteAllFromRealm();

            if (request.getMarcaCEM() != null) {
                for (MarcaCEM marca : request.getMarcaCEM()) {
                    marca.setCuenta(cuenta);
                    self.insert(marca);
                }
            }

            self.where(ModeloCEM.class)
                    .findAll()
                    .deleteAllFromRealm();

            if (request.getModeloCEM() != null) {
                for (ModeloCEM modelo : request.getModeloCEM()) {
                    modelo.setCuenta(cuenta);
                    self.insert(modelo);
                }
            }

            self.where(GamaCEM.class)
                    .findAll()
                    .deleteAllFromRealm();

            if (request.getGamaCEM() != null) {
                for (GamaCEM modelo : request.getGamaCEM()) {
                    modelo.setCuenta(cuenta);
                    self.insert(modelo);
                }
            }

            self.where(DamageCode.class)
                    .findAll()
                    .deleteAllFromRealm();

            if (request.getDamagecodes() != null) {
                for (DamageCode modelo : request.getDamagecodes()) {
                    modelo.setCuenta(cuenta);
                    self.insert(modelo);
                }
            }

            self.where(ElementCode.class)
                    .findAll()
                    .deleteAllFromRealm();

            if (request.getElementcodes() != null) {
                for (ElementCode modelo : request.getElementcodes()) {
                    modelo.setCuenta(cuenta);
                    self.insert(modelo);
                }
            }

            self.where(TipoFallaCEM.class)
                    .findAll()
                    .deleteAllFromRealm();

            if (request.getTipoFallaCEM() != null) {
                for (TipoFallaCEM modelo : request.getTipoFallaCEM()) {
                    modelo.setCuenta(cuenta);
                    self.insert(modelo);
                }
            }

            self.where(TipoContenedorCEM.class)
                    .findAll()
                    .deleteAllFromRealm();

            if (request.getTipoContenedorCEM() != null) {
                for (TipoContenedorCEM modelo : request.getTipoContenedorCEM()) {
                    modelo.setCuenta(cuenta);
                    self.insert(modelo);
                }
            }

            self.where(ClasificacionCEM.class)
                    .findAll()
                    .deleteAllFromRealm();

            if (request.getClasificacionCEM() != null) {
                for (ClasificacionCEM modelo : request.getClasificacionCEM()) {
                    modelo.setCuenta(cuenta);
                    self.insert(modelo);
                }
            }

            self.where(Recorrido.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findAll().deleteAllFromRealm();

            Recorrido recorrido = request.getEstadoActualTecnico();
            if (recorrido.getFechainicio() != null && recorrido.getEstado() != null && !ATNotificationService.DISPONIBLE.getMostrar(getApplicationContext()).equals(recorrido.getEstado())) {
                Estado estado = database.where(Estado.class).equalTo("estado", recorrido.getEstado()).findFirst();
                if (estado == null)
                    return;

                if (ATNotificationService.NO_DISPONIBLE.getMostrar(getApplicationContext()).equals(recorrido.getEstado()) ||
                        ATNotificationService.FIN_EJECUCION.getMostrar(getApplicationContext()).equals(recorrido.getEstado())) {
                    setAccess(true);
                    return;
                }

                ATNotificationService.Estado estado_ot = ATNotificationService.Estado.getEstado(estado.getId());
                if (estado_ot != null) {
                    recorrido.setEstado(estado_ot.getNombre());
                    recorrido.setUuid(UUID.randomUUID().toString());
                    recorrido.setCuenta(cuenta);
                    self.insert(recorrido);
                }
            } else {
                cuenta.setDisponible(true);
            }
        });

        database.close();
    }

    private void processPermission(RealmList<UserPermission> permissions) {
        Database database = new Database(this);
        database.executeTransaction(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                return;
            }

            for (UserPermission perm : permissions) {
                UserPermission element = self.where(UserPermission.class)
                        .equalTo("name", perm.getName())
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();

                if (element == null) {
                    perm.setUUID(UUID.randomUUID().toString());
                    perm.setCuenta(cuenta);
                    self.insert(perm);
                } else {
                    element.setName(perm.getName());
                    element.setValue(perm.getValue());
                }
            }
        });
        database.close();
    }

    private void onError(Throwable throwable) {
        progress.setVisibility(View.GONE);
        if (throwable.getMessage() != null) {
            Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private void onComplete() {
        progress.setVisibility(View.GONE);
        Bundle bundle = new Bundle();
        bundle.putBoolean("access", access);

        startService(new Intent(this, TransaccionTask.class));

        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    @Nullable
    private String readFile(@NonNull String filename) {
        try {
            InputStream inputStream = getAssets().open(filename);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            int read = inputStream.read(buffer);
            inputStream.close();
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    public void invokeGetBundleApp(String json) {
        getBundleApp(json);
    }
}