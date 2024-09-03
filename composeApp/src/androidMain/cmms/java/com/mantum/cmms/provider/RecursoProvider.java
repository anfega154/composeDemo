package com.mantum.cmms.provider;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Recurso;
import com.mantum.cmms.net.ClientManager;
import com.mantum.cmms.util.Preferences;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.util.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.realm.Case;
import io.realm.Realm;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class RecursoProvider extends ContentProvider {

    private static final String TAG = RecursoProvider.class.getSimpleName();

    private static final String SPACE = "%20";

    private final Gson gson = new GsonBuilder().create();

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        try {
            MatrixCursor cursor = new MatrixCursor(new String[]{
                    BaseColumns._ID,
                    SearchManager.SUGGEST_COLUMN_TEXT_1,
                    SearchManager.SUGGEST_COLUMN_INTENT_DATA});

            if (getContext() == null) {
                return cursor;
            }

            Database database = new Database(getContext());
            Cuenta account = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (account == null) {
                throw new Exception(getContext().getString(R.string.error_authentication));
            }

            String query = uri.getLastPathSegment();
            if ("search_suggest_query".equals(query)) {
                return cursor;
            }

            Realm realm = new Database(getContext()).instance();
            final List<Recurso> recursos = new ArrayList<>();
            if (Mantum.isConnectedOrConnecting(getContext())) {
                String url = Preferences.url(getContext(), "/restapp/app/searchrec/") + query.replace(" ", SPACE);
                Request request = new Request.Builder().get().url(url)
                        .addHeader("token", Preferences.token(getContext()))
                        .addHeader("cache-control", "no-cache")
                        .addHeader("accept-language", "application/json")
                        .addHeader("accept", Version.build(account.getServidor().getVersion()))
                        .build();

                OkHttpClient client = ClientManager.prepare(

                        new OkHttpClient.Builder()
                                .connectTimeout(Timeout.CONNECT, TimeUnit.SECONDS)
                                .writeTimeout(Timeout.WRITE, TimeUnit.SECONDS)
                                .readTimeout(Timeout.READ, TimeUnit.SECONDS), getContext()
                ).build();

                okhttp3.Response response = client.newCall(request).execute();
                ResponseBody body = response.body();
                if (body == null) {
                    throw new Exception("Intentalo de nuevo, ya que la conexión no es estable");
                }

                String json = body.string();
                if (response.isSuccessful()) {
                    Response success = gson.fromJson(json, Response.class);
                    recursos.addAll(success.getBody(Recurso.Request.class).getRecursos());
                    realm.executeTransaction(self -> {
                        Cuenta cuenta = self.where(Cuenta.class).equalTo("active", true)
                                .findFirst();

                        if (cuenta == null) {
                            return;
                        }

                        for (Recurso recurso : recursos) {
                            Recurso temporal = self.where(Recurso.class)
                                    .equalTo("id", recurso.getId())
                                    .equalTo("cuenta.UUID", cuenta.getUUID())
                                    .findFirst();

                            if (temporal == null) {
                                recurso.setCuenta(cuenta);
                                self.insert(recurso);
                            } else {
                                temporal.setCodigo(recurso.getCodigo());
                                temporal.setNombre(recurso.getNombre());
                                temporal.setCantidad(recurso.getCantidad());
                                temporal.setSigla(recurso.getSigla());
                            }
                        }
                    });
                }
                response.close();
            } else {
                Cuenta cuenta = realm.where(Cuenta.class).equalTo("active", true)
                        .findFirst();

                if (cuenta == null) {
                    throw new Exception("No te encuentras autenticado en la aplicación, por favor cierra la aplicación y autenticate de nuevo");
                }

                recursos.addAll(realm.where(Recurso.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .beginGroup()
                        .contains("codigo", query, Case.INSENSITIVE).or()
                        .contains("nombre", query, Case.INSENSITIVE).endGroup()
                        .findAll());
            }

            for (Recurso recurso : recursos) {
                cursor.addRow(new Object[]{recurso.getId(), recurso.getNombre(), recurso.toJSON()});
            }

            realm.close();
            return cursor;
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int delete(@NonNull Uri uri, String s, String[] strings) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String s, String[] strings) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}