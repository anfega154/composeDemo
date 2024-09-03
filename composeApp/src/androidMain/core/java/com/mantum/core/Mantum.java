package com.mantum.core;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.mantum.core.event.OnOffline;
import com.mantum.core.event.OnRequest;
import com.mantum.core.exception.ServiceException;
import com.mantum.core.util.Assert;
import com.mantum.core.util.Cache;
import com.mantum.core.util.Online;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;

/**
 * Contiene todas las clases necesarias para constuir la aplicación
 *
 * @author Jonattan Velásquez
 */
@Deprecated
public class Mantum {

    //region Respuestas HTTP

    /**
     * Contiene la estructura de datos para interactuar con los mensajes
     * de respuesta al realizar una peticion HTTP
     *
     * @author Jonattan Velásquez
     */
    public static abstract class Response implements Serializable {

        //region Variables

        private Integer version;

        private boolean ok;

        private boolean cancel;

        private String message;

        private Object body;

        private List<Detalle> error;

        //endregion

        //region Constructor

        public Response() {
            this.error = new ArrayList<>();
        }

        /**
         * Obtiene una nueva instancia del objeto
         * @param ok Verderaro peticion exitosa de lo contrario Falso
         */
        public Response(boolean ok) {
            this.ok = ok;
            this.cancel = false;
        }

        /**
         * Obtiene una nueva instancia del objeto
         * @param ok Verderaro peticion exitosa de lo contrario Falso
         * @param message {@link String}
         */
        public Response(boolean ok, String message) {
            this.ok = ok;
            this.message = message;
            this.cancel = false;
        }

        //endregion

        //region API Public

        public Mantum.Response ok(boolean ok) {
            this.ok = ok;
            return this;
        }

        /**
         * Obtiene el mensaje de la respuesta HTTP
         * @param message {@link String}
         * @return {@link Mantum.Response}
         */
        public Mantum.Response message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Agrega el cuerpo de la respuesta HTTP
         * @param body {@link Object}
         * @return {@link Mantum.Response}
         */
        public Mantum.Response body(Object body) {
            this.body = body;
            return this;
        }

        public void error(List<Detalle> error) {
            this.error = error;
        }

        /**
         * Obtiene si la peticion fue exitosa
         * @return Verderaro peticion exitosa de lo contrario Falso
         */
        public boolean isOk() {
            return this.ok;
        }

        /**
         * Obtiene el mensaje de la respuesta
         * @return {@link String}
         */
        public String getMessage() {
            return message;
        }

        /**
         * Obtiene el cuerpo de la respuesta
         * @param clazz {@link Class}
         * @return <T>
         */
        public <T> T getBody(Class<T> clazz) {
            return new Gson().fromJson(new Gson().toJson(this.body), clazz);
        }

        public String toBodyJson() {
            return new Gson().toJson(this.body);
        }

        public <T> T getBodyToObject(Class<T> clazz) {
            return clazz.cast(this.body);
        }

        public List<Detalle> getError() {
            return error;
        }

        public boolean isCancel() {
            return cancel;
        }

        public void setCancel(boolean cancel) {
            this.cancel = cancel;
        }

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }

        @Override
        public String toString() {
            return "Response{" +
                    "version=" + version +
                    ", ok=" + ok +
                    ", cancel=" + cancel +
                    ", message='" + message + '\'' +
                    ", body=" + body +
                    ", error=" + error +
                    '}';
        }
        //endregion
    }

    public static class Success extends Mantum.Response {

        //region Constructor

        /**
         * Obtiene una nueva instancia del objeto
         */
        public Success() {
            super(true);
        }

        //endregion

    }

    /**
     * Contiene la estructura de datos para una respuesta erronea
     * @author Jonattan Velásquez
     */
    public static class Error extends Mantum.Response {

        public Error() {
            super(false);
        }

        @Override
        public String toString() {
            return "Error{} " + super.toString();
        }
    }

    /**
     * Contiene los detalles de la respuesta HTTP
     * @author Jonattan Velásquez
     */
    public static class Detalle {

        private String title;

        private List<String> messages;

        /**
         * Obtiene el titulo del error
         * @return {@link String}
         */
        public String getTitulo() {
            return this.title;
        }

        /**
         * Obtiene el mensaje del error
         * @return {@link ArrayList}
         */
        public List<String> getMensaje() {
            return this.messages;
        }

        @Override
        public String toString() {
            return "Detalle{" +
                    "title='" + title + '\'' +
                    ", messages=" + messages +
                    '}';
        }
    }

    //endregion

    //region Eventos

    /**
     * Contiene las funciones utilizadas como callbacks en las peticiones HTTP
     * @author Jonattan Velásquez
     */
    public interface Callback {

        /**
         * Callback que es utilizado para una respuesta exitosa
         * @param success {@link Success}
         * @param offline Verdadero si el servicio se ejecuto
         *                sin conexión a internet de lo contrario
         *                falso
         */
        void success(Success success, boolean offline);

        /**
         * Callback que es utilizado para una respuesta erronea
         * @param error {@link Error}
         * @param offline Verdadero si el servicio se ejecuto
         *                sin conexión a internet de lo contrario
         *                falso
         */
        void error(Error error, boolean offline);
    }

    //endregion

    public interface Handler<T> {

        /**
         * Indica la ruta para realizar la petición HTTP
         * @param endPoint {@link String}
         * @return {@link Handler}
         */
        Handler<T> endPoint(String endPoint);

        /**
         * Obtiene la ruta para realizar la petición HTTP
         * @return {@link String}
         */
        String getEndPoint();

        /**
         * Indica si el servicio require autenticación
         * @param authenticate verdadero si el servicio requiere
         *                     autenticación de lo contario falso
         * @return {@link Handler}
         */
        Handler<T> authenticate(boolean authenticate);

        /**
         * Indica si el servicio require autenticación
         * @param authenticate verdadero si el servicio requiere
         *                     autenticación de lo contario falso
         * @param token {@link String}
         * @return {@link Handler}
         */
        Handler<T> authenticate(boolean authenticate, String token);

        /**
         * Obtiene si es necesario autenticarse para realizar la
         * petición HTTP
         * @return true si requiere autenticación de lo contrario false
         */
        boolean isAuthenticate();

        /**
         * Agrega la ruta del servidor para realizar la petición HTTP
         * @param url {@link String}
         * @return {@link Handler}
         */
        Handler<T> url(String url);

        Handler<T> url(String url, boolean force);

        /**
         * Obtiene la ruta del servidor para realizar la petición HTTP
         * @return {@link String}
         */
        String getUrl();

        /**
         * Agrega el callback de respuesta a las peticiones HTTP reaizadas
         * @param callback {@link Mantum.Callback}
         * @return {@link Handler}
         */
        Handler<T> callback(Mantum.Callback callback);

        /**
         * Obtiene el callback que se realizar despues de realizar la petición HTTP
         * @return {@link Mantum.Callback}
         */
        Mantum.Callback getCallback();

        /**
         * Construye el objeto antes de realizar una peticion en el servicio
         * @return T
         */
        T build() throws ServiceException;

        /**
         * Contiene los métodos necesarios que son llamandos al realizar una peticion HTTP
         * @param onRequest {@link OnRequest}
         * @return {@link T}
         */
        Handler<T> onRequest(OnRequest onRequest);

        /**
         * Contiene los métodos necesarios que son llamandos cuando
         * la aplicación no tiene conexión a internet y realiza una petición HTTP
         * @param onOffline {@link OnOffline}
         * @return {@link T}
         */
        Handler<T> onOffline(OnOffline onOffline);

        /**
         * Contiene los métodos necesarios para el funcionamiento offline
         * @param onOffline {@link OnOffline}
         * @param offline Verdadero si el servicio siempre debe de trabajar
         *                sin conexión a internet
         * @return {@link T}
         */
        Handler<T> onOffline(OnOffline onOffline, boolean offline);

        /**
         * Agrega la version al realizar la petición HTTP
         * @param  version {@link String}
         * @return {@link Handler}
         */
        Handler<T> version(String version);

        Handler<T> base(Integer base);

        Handler<T> timestamp(Integer timestamp);
    }

    /**
     * Clase que contiene los métodos para configurar un servicio
     * @author Jonattan Velásquez
     */
    public static abstract class Builder<T> implements Handler<T>, Serializable {

        private static final String TAG = Builder.class.getSimpleName();

        //region Variables

        protected String url;

        protected String endPoint;

        protected final Context context;

        protected boolean authenticate;

        protected String token;

        protected Mantum.Callback callback;

        protected OnRequest onRequest;

        protected OnOffline onOffline;

        protected String version;

        protected Integer base;

        protected boolean offline;

        protected Integer timestamp;

        protected boolean forceURL;

        //endregion

        //region Constructor

        public Builder(@NonNull Context context) {
            this.context = context;
            this.offline = false;
            this.token = null;
        }

        //endregion

        //region API

        @Override
        public Handler<T> url(String url) {
            return url(url, true);
        }

        @Override
        public Handler<T> url(String url, boolean force) {
            this.url = url;
            this.forceURL = force;
            return this;
        }

        @Override
        public String getUrl() {
            return this.url;
        }

        @Override
        public Handler<T> endPoint(String endPoint) {
            this.endPoint = endPoint;
            return this;
        }

        @Override
        public String getEndPoint() {
            return this.endPoint;
        }

        @Override
        public Handler<T> authenticate(boolean authenticate) {
            this.authenticate = authenticate;
            this.token = null;
            return this;
        }

        @Override
        public Handler<T> authenticate(boolean authenticate, String token) {
            this.authenticate = authenticate;
            this.token = token;
            return this;
        }

        @Override
        public Handler<T> version(String version) {
            this.version = version;
            return this;
        }

        @Override
        public Handler<T> base(Integer base) {
            this.base = base;
            return this;
        }

        @Override
        public Handler<T> timestamp(Integer timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Integer getTimestamp() {
            return this.timestamp;
        }

        public Integer getBase() {
            return this.base;
        }

        public String getToken() {
            return this.token;
        }

        @Override
        public boolean isAuthenticate() {
            return this.authenticate;
        }

        @Override
        public Handler<T> callback(Mantum.Callback callback) {
            this.callback = callback;
            return this;
        }

        @Override
        public Callback getCallback() {
            return this.callback;
        }

        @Override
        public abstract T build() throws ServiceException;

        @Override
        public Handler<T> onRequest(OnRequest onRequest) {
            this.onRequest = onRequest;
            return this;
        }

        @Override
        public Handler<T> onOffline(OnOffline onOffline) {
            this.onOffline = onOffline;
            this.offline = false;
            return this;
        }

        @Override
        public Handler<T> onOffline(OnOffline onOffline, boolean offline) {
            this.onOffline = onOffline;
            this.offline = offline;
            return this;
        }

        /**
         * Obtiene la cadena de texto que corresponda a la
         * clave enviada como argumento
         * @param key {@link String}
         * @return {@link String}
         */
        protected String getString(String key) {
            Bundle bundle = Builder.bundle(this.context);
            return !Assert.isNull(bundle) ? bundle.getString(key) : null;
        }

        /**
         * Verifica que los parámetros generales para ejecutar el servicio
         * sean validos
         */
        protected void ok() throws ServiceException {
            if (Assert.isNull(this.url)) {
                this.url = Cache.getInstance().get("url", String.class);
                if (Assert.isNull(this.url)) {
                    throw new ServiceException("El meta-data Mantum.Support.Url, no esta asignado");
                }
            }

            if (!Patterns.WEB_URL.matcher(this.url).matches()) {
                throw new ServiceException(String.format("%s no es una URL valida", this.url));
            }
        }

        /**
         * Obtiene la configuración que se encuentra en el manifest de la aplicación
         * @param context {@link Context}
         * @return {@link Bundle}
         */
        private static Bundle bundle(Context context) {
            try {
                ApplicationInfo application = context.getPackageManager().getApplicationInfo(
                        context.getPackageName(), PackageManager.GET_META_DATA);
                return application.metaData;
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "bundle: ", e);
                return null;
            }
        }

        //endregion
    }

    //region Tarea asincrona

    /**
     * Contiene los métodos necesarios para crear una accion asincrona
     * @param <I>
     */
    public static abstract class ServiceTask<I> extends AsyncTask<Void, Void, Void> {

        protected boolean execution;

        protected final I instance;

        public ServiceTask(@NonNull I instance) {
            this.instance = instance;
            this.execution = false;
        }
    }

    //endregion

    /**
     * Clase que debe de heredar todos los servicios de la aplicación
     */
    public static abstract class Service {

        private final Context context;

        private final String url;

        private final String endpoint;

        private final String version;

        private final boolean offline;

        //region Constructor

        public Service(@NonNull Context context, @NonNull String url, @NonNull String endpoint,
                       @NonNull String version, boolean offline) {
            this.context = context;
            this.url = url;
            this.endpoint = endpoint;
            this.version = version;
            this.offline = offline;
        }

        //endregion

        /**
         * Obtiene el contexto actual
         * @return {@link Context}
         */
        protected Context getContext() {
            return context;
        }

        /**
         * Obtiene la versión actual de la petición HTTP
         * @return {@link String}
         */
        protected String getVersion() {
            return version;
        }

        /**
         * Obtiene la url donde que identifica al servidor
         * @return {@link String}
         */
        public String getUrl() {
            return url;
        }

        /**
         * Obtiene la ruta donde se realizara la peticion HTTP
         * @return {@link String}
         */
        public String getEndpoint() {
            return endpoint;
        }

        /**
         * Indica si el servicio debe de ejecutarce sin conexión a internet
         * @return Verdadero sin conexión de lo contrario falso
         */
        public boolean isOffline() {
            return offline;
        }

        /**
         * Obtiene la ruta completa donde se realizara la petición HTTP
         * @return {@link String}
         */
        public String getPath() {
            return this.url + "/" + this.endpoint;
        }

    }

    //region Actividad

    /**
     * Clase que debe de heredar todas las clases activity de la aplicación
     * @author Jonattan Velásquez
     */
    public static abstract class Activity extends AppCompatActivity implements Mantum.Callback {


        //region Variables

        private ProgressDialog progressDialog;

        //endregion

        //region API Publica

        /**
         * Muestra el mensaje que es incluido en el intent
         * @param data {@link Intent}
         */
        protected boolean showMessage(Intent data) {
            if (Assert.isNull(data)) {
                return false;
            }

            Bundle bundle = data.getExtras();
            if (!Assert.isNull(bundle) && !Assert.isNull(this.getView())) {
                String message = bundle.getString("message");
                if (!Assert.isNull(message) && !message.isEmpty()) {
                    Snackbar.make(this.getView(), message, Snackbar.LENGTH_LONG)
                            .show();
                    return true;
                }
            }
            return false;
        }

        /**
         * Obtiene la versión actual de la aplicación
         * @return {@link String}
         */
        protected String version() {
            try {
                PackageManager manager = this.getPackageManager();
                PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
                return info.versionName;
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * Obtiene la vista actual
         * @return {@link View}
         */
        protected View getView() {
            return this.findViewById(android.R.id.content);
        }

        /**
         * Prepara el componente de cargando
         *
         * @param title {@link String}
         * @param message {@link String}
         */
        protected void progressPrepare(String title, String message) {
            this.progressDialog= new ProgressDialog(this);
            progressDialog.setTitle(title);
            progressDialog.setMessage(message);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
        }

        @Deprecated
        protected void show() {
            if (!Assert.isNull(this.progressDialog)) {
                this.progressDialog.show();
            }
        }

        /**
         * Oculta el componente de carga
         */
        @Deprecated
        protected void hide() {
            if (!Assert.isNull(this.progressDialog) && this.progressDialog.isShowing()) {
                this.progressDialog.dismiss();
            }
        }

        /**
         * Obtiene el identificador de un componente según su cadena de texto
         * @param identifier {@link String}
         * @return Retorna el entero que corresponde al identificador
         */
        protected int getIdentifier(String identifier) {
            return this.getResources().getIdentifier(identifier, "string", this.getPackageName());
        }

        /**
         * Cierra el teclado del dispositivo
         */
        protected void closeKeyboard() {
            View view = this.getCurrentFocus();
            if (!Assert.isNull(view)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }

        /**
         * Incluye las acciones del formulario
         * @param string {@link int}
         */
        protected void includeBackButtonAndTitle(@StringRes int string) {
            if (!Assert.isNull(this.getSupportActionBar())) {
                this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                this.getSupportActionBar().setDisplayShowTitleEnabled(true);
                this.setTitle(this.getString(string));
            }
        }

        /**
         * Incluye las acciones del formulario
         * @param string {@link String}
         */
        protected void includeBackButtonAndTitle(@NonNull String string) {
            if (!Assert.isNull(this.getSupportActionBar())) {
                this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                this.getSupportActionBar().setDisplayShowTitleEnabled(true);
                this.setTitle(string);
            }
        }

        protected void backActivity() {
            this.setResult(RESULT_OK);
            this.finish();
        }

        protected void backActivity(@StringRes int key) {
            this.backActivity(this.getString(key));
        }

        protected void backActivity(String message) {
            Intent intent = new Intent();
            intent.putExtra("message", message);
            this.setResult(RESULT_OK, intent);
            this.finish();
        }

        protected void backActivity(String message, Long id) {
            Intent intent = new Intent();
            intent.putExtra("id", id);
            intent.putExtra("message", message);
            this.setResult(RESULT_OK, intent);
            this.finish();
        }

        protected void backActivity(@NonNull Intent intent) {
            this.setResult(RESULT_OK, intent);
            this.finish();
        }

        protected void startActivity(@NonNull Class clazz) {
            this.startActivity(clazz, null);
        }

        protected void startActivity(@NonNull Class clazz, Bundle bundle) {
            Intent intent = new Intent(this, clazz);
            if (!Assert.isNull(bundle)) {
                intent.putExtras(bundle);
            }
            this.startActivityForResult(intent, 1);
        }

        //endregion

        //region Eventos

        @Deprecated
        protected void offline() {
            if (!Online.check(this)) {
                Snackbar.make(this.getView(), R.string.offline, Snackbar.LENGTH_SHORT).show();
            }
        }

        @Override
        public void success(Mantum.Success success, boolean offline) {}

        @Override
        public void error(Mantum.Error error, boolean offline) {
            View view = this.findViewById(android.R.id.content);
            if (!Assert.isNull(view) && !Assert.isNull(error) && !Assert.isNull(error.getError())) {
                for (Mantum.Detalle manager : error.getError()) {
                    String message = Arrays.toString(manager.getMensaje().toArray()).replace("[", "").replace("]", "");
                    if (message.isEmpty()) {
                        Snackbar.make(view, error.getMessage(), Snackbar.LENGTH_SHORT)
                                .setDuration(5000)
                                .show();
                        break;
                    }

                    Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
                            .setDuration(5000)
                            .show();
                }


                if (error.getError().isEmpty()) {
                    Snackbar.make(view, error.getMessage(), Snackbar.LENGTH_SHORT)
                            .setDuration(5000)
                            .show();
                }
            }

            this.runOnUiThread(new TimerTask() {
                @Override
                public void run() {
                    Activity.this.hide();
                }
            });
        }

        //endregion
    }

    //endregion


}