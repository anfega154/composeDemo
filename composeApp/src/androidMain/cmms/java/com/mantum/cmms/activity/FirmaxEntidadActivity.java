package com.mantum.cmms.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.widget.AppCompatSpinner;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;

import com.mantum.demo.R;
import com.mantum.cmms.domain.FirmaxEntidad;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.helper.TransaccionHelper;
import com.mantum.cmms.util.Message;
import com.mantum.component.component.TimePicker;
import com.mantum.component.service.Photo;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.service.TransaccionService;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.view.Drawing;
import com.mantum.component.view.ScrollControl;
import com.mantum.core.service.Permission;
import com.mantum.core.util.Assert;
import com.mantum.core.util.Storage;
import com.mantum.cmms.entity.Category;
import com.mantum.cmms.entity.Formato;
import com.mantum.cmms.service.FormatoService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import com.mantum.cmms.entity.parameter.UserParameter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import android.os.Environment;


public class FirmaxEntidadActivity extends TransaccionHelper.Dialog implements OnCompleteListener {

    private static final String TAG = FirmaxEntidadActivity.class.getSimpleName();

    private static final int PERSONAL = 1220;
    private static final int PROVEEDOR = 1221;
    private static final int CLIENTE = 1222;
    public static final int REQUEST_ACTION = 1223;
    public static final String MODULO = "modulo";

    private long identidad;
    private String tipoentidad;
    private long idpersonal;
    private long idcliente;
    private long idproveedor;
    private Photo firma;

    private MenuItem menuItem;

    private Database database;

    private TransaccionService transaccionService;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private FormatoService formatoService;

    private AppCompatSpinner spinnerCategory;

    private Drawing drawing;

    private String module;

    private Boolean mostrarCamposAdicionales;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_firmaxentidad);
            String mostrarCamposAdicionalesParameter = UserParameter.getValue(this, UserParameter.VER_ADIONALES_FIRMA);
            this.mostrarCamposAdicionales = "1".equals(mostrarCamposAdicionalesParameter);
            Permission.storage(this);

            database = new Database(this);
            transaccionService = new TransaccionService(this);
            Bundle bundle = getIntent().getExtras();

            spinnerCategory = findViewById(R.id.category);

            AppCompatSpinner formatoSpinner = findViewById(R.id.formato);

            formatoService = new FormatoService(this);
            List<Formato> formatosList = formatoService.getFormatos();
            List<Category> allCategories = formatoService.getAllCategories();

            Category categoryDefault = new Category();
            categoryDefault.setId((long) -1);
            categoryDefault.setNombre(getString(com.mantum.demo.R.string.category_select_firma));

            Formato formatoDefault = new Formato();
            formatoDefault.setId((long) -1);
            formatoDefault.setFormato(getString(com.mantum.demo.R.string.formato_select_category));
            formatosList.add(0, formatoDefault);

            ArrayAdapter<Formato> spinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, formatosList);
            formatoSpinner.setAdapter(spinnerAdapter);

            formatoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Formato formatoSelected = (Formato) adapterView.getSelectedItem();
                    List<Category> filteredCategories = new ArrayList<>();

                    if (formatoSelected.getId() == -1) {
                        filteredCategories.addAll(allCategories);
                    } else {
                        filteredCategories.addAll(formatoService.getCategoriesByFormato(formatoSelected, true));
                    }
                    filteredCategories.add(0, categoryDefault);

                    ArrayAdapter<Category> categoryAdapter = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_dropdown_item, filteredCategories);
                    spinnerCategory.setAdapter(categoryAdapter);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });

            if (bundle != null) {
                this.identidad = bundle.getLong(Mantum.KEY_ID);
                this.tipoentidad = bundle.getString(Mantum.ENTITY_TYPE);
                this.module = bundle.getString(MODULO);
            }

            includeBackButtonAndTitle(R.string.firmaxEntidad);

            this.drawing = findViewById(R.id.firmaxentidad);

            EditText personal = findViewById(R.id.selectidpersonal);
            personal.setOnClickListener((event) -> {
                Intent intent;
                intent = new Intent(this, PersonalAutocompleteActivity.class);
                startActivityForResult(intent, PersonalAutocompleteActivity.REQUEST_ACTION);
            });

            EditText proveedor = findViewById(R.id.selectidproveedor);
            proveedor.setOnClickListener((event) -> {
                Intent intent;
                intent = new Intent(this, ProveedorAutocompleteActivity.class);
                startActivityForResult(intent, ProveedorAutocompleteActivity.REQUEST_ACTION);
            });

            EditText cliente = findViewById(R.id.selectidcliente);
            cliente.setOnClickListener((event) -> {
                Intent intent;
                intent = new Intent(this, ClienteAutocompleteActivity.class);
                startActivityForResult(intent, ClienteAutocompleteActivity.REQUEST_ACTION);
            });

            TimePicker fecha = new TimePicker(this, findViewById(R.id.hora));
            fecha.setEnabled(true);
            fecha.load();

            RelativeLayout muestraLayout = findViewById(R.id.muestra_layout);
            muestraLayout.setVisibility(this.mostrarCamposAdicionales ? View.VISIBLE : View.GONE);

            LinearLayout itemLayout = findViewById(R.id.layout_item);
            itemLayout.setVisibility(this.mostrarCamposAdicionales ? View.VISIBLE : View.GONE);

        } catch (Exception e) {
            Log.e(TAG, "onCreate: ", e);
            backActivity(getString(R.string.error_app));
        }
    }

    public void clearFirma(View view) {
        this.drawing.clean();
    }

    public void modeDraw(View view) {
        closeKeyboard();
        findViewById(R.id.layou_header).setVisibility(View.GONE);
        findViewById(R.id.layout_draw).setVisibility(View.VISIBLE);
        findViewById(R.id.layout_buttons).setVisibility(View.VISIBLE);
        findViewById(R.id.text_scroll).setVisibility(View.VISIBLE);

        ScrollControl scroll = findViewById(R.id.scroll);
        scroll.setScrolling(false);
    }

    public void EnableScroll(View view) {
        renderFirma();
        closeKeyboard();

        this.menuItem.setVisible(true);

        findViewById(R.id.layou_header).setVisibility(View.VISIBLE);
        findViewById(R.id.layout_buttons).setVisibility(View.GONE);
        findViewById(R.id.text_scroll).setVisibility(View.GONE);
        findViewById(R.id.button_add).setVisibility(View.GONE);

        ScrollControl scroll = findViewById(R.id.scroll);
        scroll.setScrolling(true);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (!Assert.isNull(data)) {
            String nombre = data.getExtras().getString("Nombre");
            EditText personal = findViewById(R.id.selectidpersonal);
            EditText proveedor = findViewById(R.id.selectidproveedor);
            EditText cliente = findViewById(R.id.selectidcliente);
            if (resultCode == RESULT_OK) {
                long id = data.getExtras().getLong("Id");
                validateText(requestCode, id, nombre, personal, proveedor, cliente);
            }
        }
    }

    private void validateText(int requestCode, long id, String nombre, EditText personal, EditText proveedor, EditText cliente) {
        switch (requestCode) {
            case PERSONAL:
                this.idpersonal = id;
                personal.setText(nombre);
                proveedor.setText("");
                cliente.setText("");
                break;

            case PROVEEDOR:
                this.idproveedor = id;
                proveedor.setText(nombre);
                personal.setText("");
                cliente.setText("");
                break;

            case CLIENTE:
                this.idcliente = id;
                cliente.setText(nombre);
                personal.setText("");
                proveedor.setText("");
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_formulario, menu);
        final MenuItem sendItem = menu.findItem(R.id.action_done);
        this.menuItem = sendItem;
        this.menuItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
            case R.id.action_done:
                saveFirma();
                break;
        }

        return super.onOptionsItemSelected(menuItem);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }

        if (transaccionService != null) {
            transaccionService.close();
        }

        compositeDisposable.clear();
    }

    @Override
    public void onComplete(@NonNull String name) {
    }

    private void saveFirma() {
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            Snackbar.make(getView(), R.string.error_authentication, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        long idCategory = this.mostrarCamposAdicionales ? ((Category) spinnerCategory.getSelectedItem()).getId(): -1;

        EditText nombre = findViewById(R.id.nombre);
        EditText cedula = findViewById(R.id.cedula);
        EditText personal = findViewById(R.id.selectidpersonal);
        EditText proveedor = findViewById(R.id.selectidproveedor);
        EditText cliente = findViewById(R.id.selectidcliente);
        EditText muestra = this.mostrarCamposAdicionales ? findViewById(R.id.muestra): null;
        EditText resultado = this.mostrarCamposAdicionales ? findViewById(R.id.resultado): null;
        EditText hora = this.mostrarCamposAdicionales ? findViewById(R.id.hora): null;
        EditText cargo = findViewById(R.id.cargo);
        EditText empresa = findViewById(R.id.empresa);

        FirmaxEntidad firmaxEntidad = new FirmaxEntidad();
        firmaxEntidad.setIdentidad(this.identidad);
        firmaxEntidad.setIdpersonal(this.idpersonal);
        firmaxEntidad.setIdproveedor(this.idproveedor);
        firmaxEntidad.setIdcliente(this.idcliente);
        firmaxEntidad.setTipoentidad(this.tipoentidad);
        firmaxEntidad.setNombre(nombre.getText().toString());
        firmaxEntidad.setCedula(cedula.getText().toString());
        firmaxEntidad.setEmpresa(empresa.getText().toString());
        firmaxEntidad.setCargo(cargo.getText().toString());

        firmaxEntidad.setNombrepersonal(personal.getText().toString());
        firmaxEntidad.setNombreproveedor(proveedor.getText().toString());
        firmaxEntidad.setNombrecliente(cliente.getText().toString());
        firmaxEntidad.setIdcategoria(idCategory);
        firmaxEntidad.setToken(UUID.randomUUID().toString());
        firmaxEntidad.setFirma(this.firma);
        if (this.firma != null) {
            firmaxEntidad.setLocatefirma(this.firma.getPath());
        }

        firmaxEntidad.setMuestra(this.mostrarCamposAdicionales ? muestra.getText().toString(): null);
        firmaxEntidad.setResultado(this.mostrarCamposAdicionales ? resultado.getText().toString(): null);
        firmaxEntidad.setHora(this.mostrarCamposAdicionales ? hora.getText().toString(): null);

        Transaccion transaccion = new Transaccion();
        transaccion.setUUID(UUID.randomUUID().toString());
        transaccion.setCuenta(cuenta);
        transaccion.setUrl(cuenta.getServidor().getUrl() + "/restapp/app/saveentitysignatures");
        transaccion.setVersion(cuenta.getServidor().getVersion());
        transaccion.setValue(firmaxEntidad.toJson());
        transaccion.setModulo(this.module);
        transaccion.setAccion(Transaccion.ACCION_FIRMA_X_ENTIDAD);
        transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);

        showProgressDialog();

        compositeDisposable.add(transaccionService.save(transaccion)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(self -> {
                }, this::onError, this::onComplete));
    }

    public void addFirma(String file) {
        if (file != null && getView() != null) {
            this.firma = (new Photo(getView().getContext(), new File(file)));
        }
    }

    private void renderFirma() {
        try {
            this.drawing.setDrawingCacheEnabled(true);
            this.drawing.invalidate();
            File file = createImage();

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            this.drawing.getDrawingCache().compress(Bitmap.CompressFormat.JPEG, 70, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            ImageView firma = findViewById(R.id.firmaView);
            firma.setImageURI(Uri.parse(file.getAbsolutePath()));
            firma.setVisibility(View.VISIBLE);
            findViewById(R.id.firmaText).setVisibility(View.VISIBLE);
            findViewById(R.id.firmaxentidad).setVisibility(View.GONE);
            findViewById(R.id.textInfo).setVisibility(View.GONE);
            this.addFirma(file.getAbsolutePath());
            return;
        } catch (Exception e) {
            Log.e(TAG, "save: ", e);
            Message.snackbar(this, getString(R.string.firma_error));
        }
    }

    private File createImage() throws IOException {
        SharedPreferences sharedPreferences
                = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String key = sharedPreferences.getString(getString(R.string.mantum_account), null);

        long timeStamp = new Date().getTime();

        String imageFileName = String.format("Firma_Digital_%s_%s", key, timeStamp);
        File storageDir = getExternalFilesDir(Environment.getExternalStorageState());
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }


    private void onComplete() {
        dismissProgressDialog();
        backActivity(getString(R.string.firmaxEntidad));
    }

    private void onError(Throwable throwable) {
        Log.e(TAG, "onError: ", throwable);
        dismissProgressDialog();
        Snackbar.make(getView(), R.string.firmaxentidad_error, Snackbar.LENGTH_LONG)
                .show();
    }
}