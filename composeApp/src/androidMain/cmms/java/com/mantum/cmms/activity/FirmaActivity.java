package com.mantum.cmms.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.component.Mantum;
import com.mantum.component.view.Drawing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class FirmaActivity extends Mantum.Activity {

    private static final String TAG = FirmaActivity.class.getSimpleName();

    public static final String PATH_FILE = "file";
    public static final int REQUEST_ACTION = 1229;

    private Drawing drawing;
    private Cuenta cuenta;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_firma);

            includeBackButtonAndTitle(R.string.firma_digital);

            Database database = new Database(this);
            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            drawing = findViewById(R.id.drawing);
        } catch (Exception e) {
            backActivity(getString(R.string.error_app));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.firma, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case android.R.id.home:
                this.backActivity();
                return true;

            case R.id.action_accept:
                if (!save()) {
                    Snackbar.make(getView(), R.string.firma_error, Snackbar.LENGTH_LONG)
                            .show();
                }
                return true;

            case R.id.action_erase:
                drawing.clean();
                return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @NonNull
    private File createImage() throws IOException {
        String key = cuenta.getUUID();
        long timeStamp = new Date().getTime();

        String imageFileName = String.format("Firma_Digital_%s_%s", key, timeStamp);
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private boolean save() {
        try {
            drawing.setDrawingCacheEnabled(true);
            drawing.invalidate();

            File file = createImage();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            drawing.getDrawingCache()
                    .compress(Bitmap.CompressFormat.JPEG, 70, fileOutputStream);

            fileOutputStream.flush();
            fileOutputStream.close();

            Intent intent = new Intent();
            intent.putExtra("file", file.getAbsolutePath());
            backActivity(intent);

            return true;
        } catch (Exception e) {
            Log.e(TAG, "save: ", e);
            return false;
        }
    }
}
