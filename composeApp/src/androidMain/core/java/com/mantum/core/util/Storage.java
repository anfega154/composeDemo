package com.mantum.core.util;

import android.content.Context;
import android.os.Environment;
import androidx.annotation.NonNull;
import android.util.Log;

import com.mantum.core.service.Permission;

import java.io.File;

public abstract class Storage {

    private static final String TAG = Storage.class.getSimpleName();

    public static String path(@NonNull Context context) {
        Permission.storage(context);
        String path = context.getFilesDir().getAbsolutePath();
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED) && !state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            String storage = Environment.getExternalStorageDirectory().getAbsolutePath();
            String temp = storage + File.separator + "mantum" + File.separator + "files";
            File file = new File(temp);
            if (!file.exists() && !file.mkdirs()) {
                Log.e(TAG, "path: Ocurrio un error creando la ruta " + file.getAbsolutePath());
            } else { path = file.getAbsolutePath(); }
        }
        return path;
    }
}