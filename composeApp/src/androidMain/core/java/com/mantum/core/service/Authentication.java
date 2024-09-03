package com.mantum.core.service;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mantum.core.event.OnRequest;
import com.mantum.core.event.Callback;
import com.mantum.core.exception.ServiceException;
import com.mantum.core.Mantum;
import com.mantum.core.util.Assert;
import com.mantum.core.util.Cache;
import com.mantum.core.util.MaxVersion;
import com.mantum.core.util.Online;
import com.mantum.core.util.Timeout;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Contiene los métodos necesarios para gestionar el acceso
 * a la aplicación
 *
 * @author Jonattan Velásquez
 */
@Deprecated
public final class Authentication {

    private static final String TAG = Authentication.class.getSimpleName();

    //region Variables

    private final Context context;

    private final Mantum.Callback callback;

    private final Type type;

    private final String username;

    private final String password;

    private final String endPoint;

    private final String url;

    private final OnRequest onRequest;

    private final String version;

    //endregion

    //region Constructor

    private Authentication(Context context, Mantum.Callback callback, Type type, String username,
                           String password, String endPoint, String url, OnRequest onRequest,
                           String version) {
        this.context = context;
        this.callback = callback;
        this.type = type;
        this.username = username;
        this.password = password;
        this.endPoint = endPoint;
        this.url = url;
        this.onRequest = onRequest;
        this.version = version;
    }

    //endregion

    //region API Publica

    /**
     * Verifica la información del usuario ya sea de modo online
     * realizando una petición HTTP o offline verificando en
     * la base de datos local
     */
    public void login() {
        Send send = new Send(this);
        send.execute();
    }

    /**
     * Obtiene el usuario que tenga la session abierta
     * para autenticarlo automaticamente
     * @param callback {@link Callback}
     */
    public void trust(Callback callback) {
        boolean success = callback.onExecution(null);
        if (success && !Assert.isNull(this.callback)) {
            this.callback.success(null, true);
        }
    }

    /**
     * Desloguea al usuario actualmente autenticado
     * @param callback {@link Callback}
     */
    public static void logout(Callback callback) {
        Authentication.Model model = Authentication.Model.getInstace();
        String uuid = model.getUUID();
        model.clean();
        callback.onExecution(uuid);
    }

    @Override
    public String toString() {
        return "Authentication{" +
                "context=" + context +
                ", callback=" + callback +
                ", type=" + type +
                ", password='****'" +
                ", username='" + username + '\'' +
                '}';
    }

    //endregion

    //region API Privada

    /**
     * Realiza una peticion HTTP para autenticar al usuario
     *
     * @param url {@link String}
     * @return {@link Response}
     */
    private Response request(String url) {
        try {
            String type;
            RequestBody body;
            if (this.type == Type.MANTUM || this.type == Type.QR) {
                type = "application/json";
                MediaType mediaType = MediaType.parse("application/json");
                body = RequestBody.create(mediaType, "{" +
                        "\"token\" : \"" + this.password + "\"" +
                        "}");
            } else {
                type = "application/x-www-form-urlencoded";
                MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                body = RequestBody.create(
                        mediaType, "username=" + this.username + "&password=" + this.password);
            }

            Request request = new Request.Builder()
                    .url(url + "/" + this.endPoint)
                    .addHeader("accept", "application/vnd.mantum.app-v" + this.version + "+json")
                    .addHeader("cache-control", "no-cache")
                    .addHeader("content-type", type)
                    .post(body).build();

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(Timeout.CONNECT, TimeUnit.SECONDS)
                    .writeTimeout(Timeout.WRITE, TimeUnit.SECONDS)
                    .readTimeout(Timeout.READ, TimeUnit.SECONDS)
                    .build();

            return client.newCall(request).execute();
        } catch (Exception e) {
            Log.e(TAG, "request: ", e);
            return null;
        }
    }

    //endregion

    //region Petición HTTP

    /**
     * Contiene los métodos para realizar la petición HTTP
     * en modo asincrono
     *
     * @author Jonattan Velásquez
     * @see Mantum.ServiceTask
     */
    private final static class Send extends Mantum.ServiceTask<Authentication> {

        final Authentication.Model model = Authentication.Model.getInstace();

        /**
         * Obtiene una nueva instancia del objeto
         * @param instance {@link Authentication}
         */
        private Send(Authentication instance) {
            super(instance);
        }

        @Override
        protected void onPreExecute() {
            try {
                // Inicializa la configuración antes de realizar la peticion HTTP
                boolean execute = this.instance.onRequest.onBeforeRequest(
                        this.instance.username, this.instance.password, this.instance.url);

                if (!execute) {
                    throw new Exception("Ocurrio un error al verificar los datos del acceso a la aplicación");
                }
            } catch (Exception e) {
                Log.e(TAG, "doInBackground: " + e.getMessage(), e);
                model.clean();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Verifica que contenga conexion a internet
                if (!Online.check(this.instance.context) && model.isValid()) {
                    this.instance.callback.success(null, true);
                    return null;
                }

                // Realia una peticion HTTP para autenticar al usuario frente al servidor
                final String url = Cache.getInstance().get("url", String.class);
                if (Assert.isNull(url)) {
                    throw new Exception("Recuerda que debes de leer el código QR antes de autenticarse");
                }

                // Verifica que contenga una url antes de realizar la peticion HTTP
                final Response response = this.instance.request(url);
                if (Assert.isNull(this.instance.callback)) {
                    throw new Exception("Recuerda que debes de leer el código QR antes de autenticarse");
                }

                // Error al enviar la peticion HTTP
                if (Assert.isNull(response)) {
                    throw new Exception("Ocurrio un error al enviar la petición HTTP");
                }

                // Obtiene la respuesta del servidor
                final String body = response.body().string();
                final Gson gson = new GsonBuilder().create();
                if (response.isSuccessful()) {
                    final Authentication self = this.instance;
                    ((Mantum.Activity) this.instance.context).runOnUiThread(() -> {
                        try {
                            Mantum.Success success = gson.fromJson(body, Mantum.Success.class);
                            if (!self.onRequest.onAfterRequest(success, self.username, self.password, url)) {
                                throw new Exception("Ocurrio un error guardando los datos de autenticación en la base de datos");
                            }

                            success.setVersion(MaxVersion.get(response));
                            response.close();
                            self.callback.success(success, true);
                        } catch (Exception e) {
                            model.setToken(null);
                            response.close();
                            self.callback.error(null, true);
                        }
                    });
                } else {
                    Mantum.Error error = gson.fromJson(body, Mantum.Error.class);
                    error.message(response.message());
                    response.close();
                    this.instance.callback.error(error, true);
                }
            } catch (Exception e) {
                Log.e(TAG, "doInBackground: " + e.getMessage(), e);
                model.setToken(null);
                this.instance.callback.error(null, true);
            }
            return null;
        }
    }

    //endregion

    //region Builder Services

    /**
     * Contiene los metodos necesarios para configurar
     * el servicio de autenticación
     *
     * @author Jonattan Velásquez
     * @see Mantum.Builder
     */
    public final static class Builder extends Mantum.Builder<Authentication> {

        private String username;

        private String password;

        private Authentication.Type type;

        /**
         * Obtiene una nueva instancia del objeto
         * @param context {@link Context}
         */
        public Builder(@NonNull Context context) {
            super(context);
            this.username = "";
            this.password = "";
        }

        /**
         * Agrega el nombre de la cuenta de usuario
         * @param username {@link String}
         * @return {@link Builder}
         */
        public Builder username(@NonNull String username) {
            this.username = username;
            return this;
        }

        /**
         * Agrega la contraseña y el tipo de autenticación
         * @param password {@link String}
         * @return {@link Builder}
         */
        public Builder password(@NonNull String password, Authentication.Type type) {
            this.password = password;
            this.type = type;
            return this;
        }

        @Override
        public Authentication build() throws ServiceException {
            if (this.type == Type.MANTUM || this.type == Type.QR) {
                String token = this.getString("Mantum.Authentication.Token");
                if (Assert.isNull(token) || token.isEmpty()) {
                    throw new ServiceException("El meta-data Mantum.Account.Token no esta asignado");
                }

                // Construye el token de acceso
                this.username = !Assert.isNull(this.username) ? this.username : "";
                String password = this.type == Type.MANTUM ? this.md5(this.password).trim() : this.password;
                this.password = this.username.trim() + token.trim() + password;
            }

            this.password = this.md5(this.password);
            return new Authentication(this.context, this.callback, this.type,
                    this.username, this.password, this.endPoint,
                    this.url, this.onRequest, this.version);
        }

        /**
         * Codifica una cadena de texto en formato MD5
         * @param string {@link String}
         * @return {@link String}
         */
        private String md5(@NonNull String string) {
            try {
                MessageDigest digest = java.security.MessageDigest
                        .getInstance("MD5");
                digest.update(string.getBytes());
                byte messageDigest[] = digest.digest();

                StringBuilder hexString = new StringBuilder();
                for (byte aMessageDigest : messageDigest) {
                    String h = Integer.toHexString(0xFF & aMessageDigest);
                    while (h.length() < 2) { h = "0" + h; }
                    hexString.append(h);
                }
                return hexString.toString();
            } catch (NoSuchAlgorithmException ignored) {}
            return "";
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "Builder{" +
                    "username='" + username + '\'' +
                    ", password='****'" +
                    ", type=" + type +
                    '}';
        }
    }

    //endregion

    //region Tipos de autenticación

    /**
     * Contiene los tipos usados de contraseñas
     *
     * @author Jonattan Velásquez
     */
    public enum Type {
        MANTUM, QR
    }

    //endregion

    //region Modelo

    /**
     * Contiene el token de autenticación de los servicios cuando
     * realizan una petición HTTP
     *
     * @author Jonattan Velásquez
     */
    @Deprecated
    public static class Model {

        private static final Model ourInstance = new Model();

        private String UUID;

        private Long id;

        private String token;

        private Integer numero;

        private String url;

        private String database;

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        /**
         * Obtiene una nueva instancia del objeto
         */
        private Model() {}

        /**
         * Obtiene la instancia actual del objeto
         * @return {@link Model}
         */
        public static Model getInstace() {
            return ourInstance;
        }

        /**
         * Obtiene el identificador unico del objeto
         * @return {@link String}
         */
        public String getUUID() {
            return UUID;
        }

        /**
         * Setea el identificador unico del objeto
         * @param UUID {@link String}
         */
        public void setUUID(String UUID) {
            this.UUID = UUID;
        }

        /**
         * Obtiene el id de la cuenta autenticada
         * @return {@link Long}
         */
        public Long getId() {
            return id;
        }

        /**
         * Setea el id de la cuenta autenticada
         * @param id {@link Long}
         */
        public void setId(Long id) {
            this.id = id;
        }

        /**
         * Obtiene el token de autenticación
         * @return {@link String}
         */
        public String getToken() {
            return this.token;
        }

        /**
         * Setea el token de autenticación
         * @param token {@link String}
         */
        public void setToken(String token) {
            this.token = token;
        }

        /**
         * Obtiene el numero de la base de datos
         * @return {@link Integer}
         */
        public Integer getNumero() {
            return numero;
        }

        /**
         * Setea el numero de la base de datos
         * @param numero {@link Integer}
         */
        public void setNumero(Integer numero) {
            this.numero = numero;
        }

        /**
         * Verifica si los datos estan correctos para ser autenticado
         * @return Verdero si debe ser autenticado de lo contrario NO!
         */
        boolean isValid() {
            return !Assert.isNull(this.UUID) && !Assert.isNull(this.id) && !Assert.isNull(this.token);
        }

        /**
         * Limpia todos los datos
         */
        void clean() {
            this.UUID = null;
            this.id = null;
            this.token = null;
            this.numero = null;
            this.url = null;
        }

        @Override
        public String toString() {
            return "Model{" +
                    "UUID='" + UUID + '\'' +
                    ", id=" + id +
                    ", token='" + "*****" + '\'' +
                    ", numero=" + numero +
                    '}';
        }
    }

    //endregion
}