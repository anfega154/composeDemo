package com.mantum.cmms.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;

import com.mantum.R;
import com.mantum.cmms.domain.FirmaxEntidad;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.adapter.CategoryAdapter;
import com.mantum.cmms.entity.Category;
import com.mantum.cmms.service.FormatoService;

import java.util.List;

public class FirmaxEntidadFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "FirmaxEntidad";

    private boolean action;

    private OnCompleteListener onCompleteListener;

    private Long idCategory;

    public FirmaxEntidadFragment() {
        this.action = true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_firmaxentidad, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onCompleteListener.onComplete(KEY_TAB);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onCompleteListener = (OnCompleteListener) context;
    }

    @NonNull
    @Override
    public String getKey() {
        return KEY_TAB;
    }

    @NonNull
    @Override
    public String getTitle(@NonNull Context context) {
        return context.getString(R.string.tab_general);
    }

    public FirmaxEntidadFragment setAction(@SuppressWarnings("SameParameterValue") boolean action) {
        this.action = action;
        return this;
    }

    public FirmaxEntidad getValue() {
        if (getView() == null) {
            return null;
        }

        FirmaxEntidad firmaxEntidad = new FirmaxEntidad();

        EditText personal = getView().findViewById(R.id.selectidpersonal);
        firmaxEntidad.setNombrepersonal(personal.getText().toString());

        EditText proveedor = getView().findViewById(R.id.selectidproveedor);
        firmaxEntidad.setNombreproveedor(proveedor.getText().toString());

        EditText cliente = getView().findViewById(R.id.selectidcliente);
        firmaxEntidad.setNombrecliente(cliente.getText().toString());

        EditText nombre= getView().findViewById(R.id.nombre);
        firmaxEntidad.setNombre(nombre.getText().toString());

        EditText cedula = getView().findViewById(R.id.cedula);
        firmaxEntidad.setCedula(cedula.getText().toString());

        EditText muestra = getView().findViewById(R.id.muestra);
        firmaxEntidad.setMuestra(muestra.getText().toString());

        EditText resultado = getView().findViewById(R.id.resultado);
        firmaxEntidad.setResultado(resultado.getText().toString());

        EditText hora = getView().findViewById(R.id.hora);
        firmaxEntidad.setHora(hora.getText().toString());

        AppCompatSpinner category = getView().findViewById(R.id.category);
        firmaxEntidad.setIdcategoria(((CategoryAdapter) category.getSelectedItem()).getId());

        ImageView firma = getView().findViewById(R.id.firmaView);
        firmaxEntidad.setLocatefirma(firma.getResources().toString());

        return firmaxEntidad;
    }

    public void onView(FirmaxEntidad firmaxEntidad) {
        if (getView() == null) {
            return;
        }

        EditText personal = getView().findViewById(R.id.selectidpersonal);
        personal.setFocusable(false);
        personal.setCursorVisible(action);
        personal.setText(firmaxEntidad.getNombrepersonal());

        EditText proveedor = getView().findViewById(R.id.selectidproveedor);
        proveedor.setFocusable(false);
        proveedor.setCursorVisible(action);
        proveedor.setText(firmaxEntidad.getNombreproveedor());

        EditText cliente = getView().findViewById(R.id.selectidcliente);
        cliente.setFocusable(false);
        cliente.setCursorVisible(action);
        cliente.setText(firmaxEntidad.getNombrecliente());

        EditText nombre = getView().findViewById(R.id.nombre);
        nombre.setFocusable(false);
        nombre.setCursorVisible(action);
        nombre.setText(firmaxEntidad.getNombre());

        EditText cedula = getView().findViewById(R.id.cedula);
        cedula.setFocusable(false);
        cedula.setCursorVisible(action);
        cedula.setText(firmaxEntidad.getCedula());

        EditText muestra = getView().findViewById(R.id.muestra);
        muestra.setFocusable(false);
        muestra.setCursorVisible(action);
        muestra.setText(firmaxEntidad.getMuestra());

        EditText resultado = getView().findViewById(R.id.resultado);
        resultado.setFocusable(false);
        resultado.setCursorVisible(action);
        resultado.setText(firmaxEntidad.getResultado());

        EditText hora = getView().findViewById(R.id.hora);
        hora.setFocusable(false);
        hora.setCursorVisible(action);
        hora.setText((CharSequence) firmaxEntidad.getHora());

        AppCompatSpinner category = getView().findViewById(R.id.category);
        category.setVisibility(View.GONE);

        idCategory = firmaxEntidad.getIdcategoria();
        if (idCategory != null) {
            category.setVisibility(View.VISIBLE);
            category.setEnabled(false);

            List<Category> items = new FormatoService(getContext()).getAllCategories();
            Category general = new Category();
            general.setId((long) -1);
            general.setNombre(getString(com.mantum.component.R.string.category_select));
            items.add(0, general);

            category.setAdapter(new ArrayAdapter<>(
                    this.getContext(), android.R.layout.simple_spinner_dropdown_item, items));

            // seleccionar por defecto
            if (!items.isEmpty()) {
                int index = 0;
                boolean isCurrent = false;
                for (Category myCategory : items) {
                    if (idCategory.equals(myCategory.getId())) {
                        isCurrent = true;
                        break;
                    }
                    index = index + 1;
                }

                if (!isCurrent) {
                    index = 0;
                }

                category.setSelection(index);
            }
        }

        getView().findViewById(R.id.layout_buttons).setVisibility(View.GONE);
        getView().findViewById(R.id.text_scroll).setVisibility(View.GONE);
        getView().findViewById(R.id.button_add).setVisibility(View.GONE);

        getView().findViewById(R.id.layout_draw).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.firmaText).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.firmaxentidad).setVisibility(View.GONE);
        getView().findViewById(R.id.textInfo).setVisibility(View.GONE);
        getView().findViewById(R.id.formato).setVisibility(View.GONE);

        ImageView firma = getView().findViewById(R.id.firmaView);
        firma.setImageURI(Uri.parse(firmaxEntidad.getLocatefirma()));
        firma.setVisibility(View.VISIBLE);


    }
}