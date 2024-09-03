package com.mantum.cmms.util;

import android.app.Activity;

public class BackEditTransaction {

    public static void backDialog(Activity activity) {
        new android.app.AlertDialog.Builder(activity)
                .setMessage("Los cambios no serán guardados.")
                .setPositiveButton("Salir", (dialogInterface, i) -> activity.onBackPressed())
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
