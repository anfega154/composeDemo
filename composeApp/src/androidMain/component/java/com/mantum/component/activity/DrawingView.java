package com.mantum.component.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;
import com.mantum.component.Mantum;
import com.mantum.component.R;
import com.mantum.component.view.Drawing;

import java.io.File;
import java.io.FileOutputStream;

public class DrawingView extends Mantum.Activity {

    public static final int DRAWING_PHOTO = 1201;

    public static final String PATH_FILE_VIEW_PHOTO = "path_file_view_photo";

    private AlertDialog alertDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.drawing_layout);

            includeBackButtonAndTitle(R.string.editar);
            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                throw new Exception("bundle == null");
            }

            String path = bundle.getString(PATH_FILE_VIEW_PHOTO, null);
            File file = new File(path);
            if (!file.exists()) {
                throw new Exception("La imagen ya no existe " + path);
            }

            Drawing drawing = findViewById(R.id.background);
            drawing.setEnabled(true);
            drawing.setBackground(BitmapFactory.decodeFile(file.getPath()));

            View form = View.inflate(this, R.layout.color, null);
            ColorPicker picker = form.findViewById(R.id.picker);
            SVBar svBar = form.findViewById(R.id.svbar);
            OpacityBar opacityBar = form.findViewById(R.id.opacitybar);

            picker.addSVBar(svBar);
            picker.addOpacityBar(opacityBar);
            picker.setOldCenterColor(drawing.getColor());

            alertDialog = new AlertDialog.Builder(this, R.style.dialogTheme)
                    .setCancelable(false)
                    .setView(form)
                    .setNegativeButton(R.string.cancelar, ((dialog, which) -> dialog.dismiss()))
                    .setPositiveButton(R.string.aceptar, ((dialog, which) -> {
                        picker.setOldCenterColor(picker.getColor());
                        drawing.setColor(picker.getColor());
                    })).create();

            findViewById(R.id.cancelar).setOnClickListener(v -> drawing.clean());

            findViewById(R.id.aceptar).setOnClickListener(v -> {
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    drawing.setDrawingCacheEnabled(true);
                    drawing.invalidate();
                    drawing.getDrawingCache().compress(Bitmap.CompressFormat.JPEG, 70, fileOutputStream);

                    fileOutputStream.flush();
                    fileOutputStream.close();

                    onBackPressed();
                } catch (Exception e) {
                    Snackbar.make(getView(), R.string.error_editar_foto, Snackbar.LENGTH_LONG)
                            .show();
                }
            });
        } catch (Exception e) {
            onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawing_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (android.R.id.home == id) {
            onBackPressed();
            return true;
        }

        if (R.id.color == id && alertDialog != null && !alertDialog.isShowing()) {
            alertDialog.show();
        }

        return false;
    }
}
