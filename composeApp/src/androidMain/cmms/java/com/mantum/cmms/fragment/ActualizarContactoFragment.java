package com.mantum.cmms.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mantum.R;
import com.mantum.cmms.domain.Contacto;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

public class ActualizarContactoFragment extends Mantum.Fragment {

    public static final int REQUEST_ACTION = 1251;

    public final static String KEY_TAB = "Actualizar_Contacto";

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_actualizar_contacto,
                container, false);
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

    @Nullable
    public Contacto getValue(Long idot) {
        if (getView() == null) {
            return null;
        }

        Contacto contacto = new Contacto();
        contacto.setIdot(idot);

        TextInputEditText identificacion = getView().findViewById(R.id.identificacion);
        contacto.setIdentificacion(identificacion.getText().toString());

        TextInputEditText nombre = getView().findViewById(R.id.nombre);
        contacto.setNombre(nombre.getText().toString());

        TextInputEditText apellido = getView().findViewById(R.id.apellido);
        contacto.setApellido(apellido.getText().toString());

        TextInputEditText cargo = getView().findViewById(R.id.cargo);
        contacto.setCargo(cargo.getText().toString());

        TextInputEditText ingeniero = getView().findViewById(R.id.ingeniero);
        contacto.setIngeniero(ingeniero.getText().toString());

        TextInputEditText telefono = getView().findViewById(R.id.telefono);
        contacto.setTelefono(telefono.getText().toString());

        TextInputEditText celular = getView().findViewById(R.id.celular);
        contacto.setCelular(celular.getText().toString());

        TextInputEditText direccion = getView().findViewById(R.id.direccion);
        contacto.setDireccion(direccion.getText().toString());

        return contacto;
    }
}
