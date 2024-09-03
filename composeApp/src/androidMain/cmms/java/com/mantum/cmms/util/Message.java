package com.mantum.cmms.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import android.text.TextUtils;
import android.view.View;

import com.mantum.core.Mantum;
import com.mantum.core.util.Assert;

import static com.mantum.cmms.activity.ConfiguracionActivity.PREFERENCIA_MENSAJE;

@Deprecated
public abstract class Message {

    public static void snackbar(@NonNull Context context,  @NonNull String message) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean automatic = sharedPreferences.getBoolean(PREFERENCIA_MENSAJE, true);
        View view = ((Mantum.Activity) context).findViewById(android.R.id.content);
        if (automatic) {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                    .show();
        } else {
            Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE)
                    .setAction("Ocultar", v -> {})
                    .show();
        }
    }

    public static String get(Mantum.Error error) {
        if (error == null) {
            return "Ocurrio un error obteniendo el mensaje";
        }

        try {
            if (Assert.isNull(error.getError()) || error.getError().isEmpty() || Assert.isNull(error.getError().get(0)) || Assert.isNull(error.getError().get(0).getMensaje()) || error.getError().get(0).getMensaje().isEmpty()) {
                return error.getMessage();
            }
            return TextUtils.join(", ", error.getError().get(0).getMensaje());
        } catch (Exception e) {
            e.printStackTrace();
            return error.getMessage();
        }
    }
}