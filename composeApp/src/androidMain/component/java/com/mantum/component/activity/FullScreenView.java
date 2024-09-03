package com.mantum.component.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.widget.AppCompatSpinner;

import android.os.Environment;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.mantum.component.Mantum;
import com.mantum.demo.R;
import com.mantum.component.adapter.CategoryAdapter;
import com.mantum.component.adapter.FormatoAdapter;
import com.mantum.component.view.TouchImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.mantum.component.activity.DrawingView.DRAWING_PHOTO;

public class FullScreenView extends Mantum.Activity {

    public static final int REQUEST_VIEW_PHOTO = 1200;

    public static final String PATH_FILE = "path_file";
    public static final String PATH_FILE_VIEW_PHOTO = "path_file_view_photo";
    public static final String PATH_URI_VIEW_PHOTO = "path_uri_view_photo";
    public static final String BEFORE_NAME_FILE_VIEW_PHOTO = "before_name_file_view_photo";
    public static final String AFTER_NAME_FILE_VIEW_PHOTO = "after_name_file_view_photo";
    public static final String DEFAULT_FILE_VIEW_PHOTO = "default_file_view_photo";
    public static final String NAME_ENABLED = "name_enabled";
    public static final String DESCRIPTION_PHOTO = "description_photo";
    public static final String ID_CATEGORY_PHOTO = "id_category_photo";
    public static final String EDIT_MODE = "edit_mode";
    public static final String FORMATOS = "formatos";
    public static final String ALL_CATEGORIES = "allCategories";

    private File file;
    private Long idCategory;
    private String description;
    private String beforeNameFile;
    private String afterNameFile;
    private boolean defaultFile;
    private boolean isNameEnabled;

    private List<FormatoAdapter> formatos;
    private List<CategoryAdapter> allCategories;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.fullscreen_layout);

            includeBackButtonAndTitle(R.string.photo);

            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                throw new Exception("bundle == null");
            }

            description = bundle.getString(DESCRIPTION_PHOTO);
            idCategory = bundle.getLong(ID_CATEGORY_PHOTO, -1);
            isNameEnabled = bundle.getBoolean(NAME_ENABLED, true);

            formatos = (List<FormatoAdapter>) bundle.getSerializable(FORMATOS);
            allCategories = (List<CategoryAdapter>) bundle.getSerializable(ALL_CATEGORIES);

            if (formatos != null && !formatos.isEmpty()) {
                FormatoAdapter formatoDefault = new FormatoAdapter();
                formatoDefault.setId((long) -1);
                formatoDefault.setFormato(getString(com.mantum.demo.R.string.formato_select_category));
                formatos.add(0, formatoDefault);
            }

            TouchImageView touchImageView = findViewById(R.id.display);

            // Imagen interna
            String path = bundle.getString(PATH_FILE_VIEW_PHOTO, null);
            if (path != null) {
                file = new File(path);
                if (!file.exists()) {
                    throw new Exception("La imagen no existe " + path);
                }

                beforeNameFile = afterNameFile = file.getName();
                defaultFile = bundle.getBoolean(DEFAULT_FILE_VIEW_PHOTO, false);

                Glide.with(this)
                        .load(file)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                        .fitCenter()
                        .into(touchImageView);
            }

            // Imagen externa
            path = bundle.getString(PATH_URI_VIEW_PHOTO, null);
            if (path != null) {
                Glide.with(this)
                        .load(Uri.parse(path))
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .fitCenter()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                touchImageView.setImageBitmap(resource);
                            }
                        });
            }
        } catch (Exception e) {
            onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        TouchImageView touchImageView = findViewById(R.id.display);
        Glide.with(this)
                .load(file)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                .fitCenter()
                .into(touchImageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.full_screen_menu, menu);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            menu.findItem(R.id.detail)
                    .setVisible(bundle.getBoolean(EDIT_MODE, true));

            menu.findItem(R.id.edit)
                    .setVisible(bundle.getBoolean(EDIT_MODE, true));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (android.R.id.home == id) {
            onBackPressed();
            return true;
        }

        if (R.id.detail == id) {
            View form = View.inflate(this, R.layout.rename_form, null);

            TextInputEditText name = form.findViewById(R.id.rename);
            name.setText(afterNameFile);
            name.setEnabled(isNameEnabled);

            CheckBox checkBox = form.findViewById(R.id.background);
            checkBox.setChecked(defaultFile);

            AppCompatSpinner category = form.findViewById(R.id.category);
            category.setVisibility(View.GONE);

            AppCompatSpinner formatoSpinner = form.findViewById(R.id.formato);
            formatoSpinner.setVisibility(View.GONE);

            TextInputEditText description = form.findViewById(R.id.description);
            description.setText(this.description);

            if (formatos.size() > 0) {
                formatoSpinner.setVisibility(View.VISIBLE);
                category.setVisibility(View.VISIBLE);

                CategoryAdapter categoryDefault = new CategoryAdapter();
                categoryDefault.setId((long) -1);
                categoryDefault.setNombre(getString(com.mantum.demo.R.string.category_select));

                ArrayAdapter<FormatoAdapter> spinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, formatos);
                formatoSpinner.setAdapter(spinnerAdapter);

                formatoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        FormatoAdapter formatoSelected = (FormatoAdapter) adapterView.getSelectedItem();
                        List<CategoryAdapter> filteredCategories = new ArrayList<>();

                        if (formatoSelected.getId() == -1) {
                            filteredCategories.addAll(allCategories);
                        } else {
                            filteredCategories.addAll(formatoSelected.getCategorias());
                        }
                        filteredCategories.add(0, categoryDefault);

                        ArrayAdapter<CategoryAdapter> categoryAdapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, filteredCategories);
                        category.setAdapter(categoryAdapter);

                        if (idCategory != -1) {
                            for (int j = 0; j < filteredCategories.size(); j++) {
                                if (filteredCategories.get(j).getId().equals(idCategory)) {
                                    category.setSelection(j);
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
            } else if (allCategories.size() > 0) {
                category.setVisibility(View.VISIBLE);

                CategoryAdapter categoryDefault = new CategoryAdapter();
                categoryDefault.setId((long) -1);
                categoryDefault.setNombre(getString(com.mantum.demo.R.string.category_select));

                List<CategoryAdapter> filteredCategories = new ArrayList<>(allCategories);
                filteredCategories.add(0, categoryDefault);

                ArrayAdapter<CategoryAdapter> categoryAdapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, filteredCategories);
                category.setAdapter(categoryAdapter);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(form);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.accept, (dialog, which) -> {
                Editable temporal = name.getText();
                if (temporal != null && !temporal.toString().isEmpty()) {
                    afterNameFile = temporal.toString();
                    defaultFile = checkBox.isChecked();
                    if (category.getSelectedItem() != null) {
                        idCategory = ((CategoryAdapter) category.getSelectedItem()).getId();
                    }

                    if (description.getText() != null) {
                        this.description = description.getText().toString();
                    }

                    dialog.dismiss();
                }
            });

            builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
            builder.create();
            builder.show();
        }

        if (R.id.edit == id) {
            if (file == null) {
                Snackbar.make(getView(), R.string.imagen_no_disponible, Snackbar.LENGTH_LONG)
                        .show();
                return false;
            }

            Bundle bundle = new Bundle();
            bundle.putString(DrawingView.PATH_FILE_VIEW_PHOTO, file.getPath());

            Intent intent = new Intent(this, DrawingView.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, DRAWING_PHOTO);
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(BEFORE_NAME_FILE_VIEW_PHOTO, beforeNameFile);
        intent.putExtra(AFTER_NAME_FILE_VIEW_PHOTO, afterNameFile);
        intent.putExtra(DEFAULT_FILE_VIEW_PHOTO, defaultFile);
        intent.putExtra(ID_CATEGORY_PHOTO, idCategory);
        intent.putExtra(DESCRIPTION_PHOTO, description);
        intent.putExtra(PATH_FILE, file != null ? file.getAbsolutePath() : null);
        backActivity(intent);
    }
}