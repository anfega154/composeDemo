package com.mantum.cmms.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mantum.R;
import com.mantum.cmms.domain.InstalacionPlantaExterna;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.component.DatePicker;

public class InstalacionPlantaExternaFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Instalacion_Planta_Externa";

    private boolean action;

    private OnCompleteListener onCompleteListener;

    public InstalacionPlantaExternaFragment() {
        this.action = true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_instalacion_planta_externa, container, false);

        if (action) {
            DatePicker fecha = new DatePicker(view.getContext(), view.findViewById(R.id.fecha));
            fecha.setEnabled(true);
            fecha.load();
        }

        return view;
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

    public InstalacionPlantaExternaFragment setAction(@SuppressWarnings("SameParameterValue") boolean action) {
        this.action = action;
        return this;
    }

    public InstalacionPlantaExterna getValue() {
        if (getView() == null) {
            return null;
        }

        InstalacionPlantaExterna instalacionPlantaExterna = new InstalacionPlantaExterna();

        TextInputEditText requisitos = getView().findViewById(R.id.requisitos);
        instalacionPlantaExterna.setRequisitos(requisitos.getText().toString());

        SwitchCompat ejecutar = getView().findViewById(R.id.ejecutar);
        instalacionPlantaExterna.setCurso(ejecutar.isChecked());

        TextInputEditText empresa = getView().findViewById(R.id.empresa);
        instalacionPlantaExterna.setEmpresa(empresa.getText().toString());

        TextInputEditText fecha = getView().findViewById(R.id.fecha);
        instalacionPlantaExterna.setFecha(fecha.getText().toString());

        TextInputEditText duracion = getView().findViewById(R.id.duracion);
        instalacionPlantaExterna.setDuracion(duracion.getText().toString());

        TextInputEditText vigencia = getView().findViewById(R.id.vigencia);
        instalacionPlantaExterna.setVigencia(vigencia.getText().toString());

        TextInputEditText nombre = getView().findViewById(R.id.nombre);
        instalacionPlantaExterna.setPersonal(nombre.getText().toString());

        TextInputEditText telefono = getView().findViewById(R.id.telefono);
        instalacionPlantaExterna.setTelefono(telefono.getText().toString());

        TextInputEditText tiempo = getView().findViewById(R.id.tiempo);
        instalacionPlantaExterna.setTiempo(tiempo.getText().toString());

        InstalacionPlantaExterna.Infraestructura temporal =
                new InstalacionPlantaExterna.Infraestructura();
        temporal.setNombre("TECNOLOGIA DE ULTIMA MILLA EN EL CLIENTE");

        SwitchCompat convenio = getView().findViewById(R.id.convenio);
        temporal.setConvenio(convenio.isChecked());

        SwitchCompat radio = getView().findViewById(R.id.radio);
        temporal.setRadio(radio.isChecked());

        SwitchCompat satelite = getView().findViewById(R.id.satelite);
        temporal.setSatelite(satelite.isChecked());

        SwitchCompat fibra = getView().findViewById(R.id.fibra);
        temporal.setFibra(fibra.isChecked());

        SwitchCompat gsmgprs = getView().findViewById(R.id.gsm_gprs);
        temporal.setGsmgprs(gsmgprs.isChecked());

        instalacionPlantaExterna.addInfraestructura(temporal);

        InstalacionPlantaExterna.IngenieriaRed ingenieriaRed
                = new InstalacionPlantaExterna.IngenieriaRed();
        ingenieriaRed.setId(1);

        TextInputEditText cliente = getView().findViewById(R.id.modem_radio_cliente);
        ingenieriaRed.setCliente(cliente.getText().toString());

        TextInputEditText equipo = getView().findViewById(R.id.modem_radio_equipo);
        ingenieriaRed.setMarca(equipo.getText().toString());

        TextInputEditText cantidad = getView().findViewById(R.id.modem_radio_cantidad);
        ingenieriaRed.setCantidad(cantidad.getText().toString());

        instalacionPlantaExterna.addIngenieriaRed(ingenieriaRed);

        ingenieriaRed = new InstalacionPlantaExterna.IngenieriaRed();
        ingenieriaRed.setId(2);

        cliente = getView().findViewById(R.id.modem_satelital_cliente);
        ingenieriaRed.setCliente(cliente.getText().toString());

        equipo = getView().findViewById(R.id.modem_satelital_equipo);
        ingenieriaRed.setMarca(equipo.getText().toString());

        cantidad = getView().findViewById(R.id.modem_satelital_cantidad);
        ingenieriaRed.setCantidad(cantidad.getText().toString());

        instalacionPlantaExterna.addIngenieriaRed(ingenieriaRed);

        ingenieriaRed = new InstalacionPlantaExterna.IngenieriaRed();
        ingenieriaRed.setId(3);

        cliente = getView().findViewById(R.id.router_cliente);
        ingenieriaRed.setCliente(cliente.getText().toString());

        equipo = getView().findViewById(R.id.router_equipo);
        ingenieriaRed.setMarca(equipo.getText().toString());

        cantidad = getView().findViewById(R.id.router_cantidad);
        ingenieriaRed.setCantidad(cantidad.getText().toString());

        instalacionPlantaExterna.addIngenieriaRed(ingenieriaRed);

        ingenieriaRed = new InstalacionPlantaExterna.IngenieriaRed();
        ingenieriaRed.setId(4);

        cliente = getView().findViewById(R.id.switch_cliente);
        ingenieriaRed.setCliente(cliente.getText().toString());

        equipo = getView().findViewById(R.id.switch_equipo);
        ingenieriaRed.setMarca(equipo.getText().toString());

        cantidad = getView().findViewById(R.id.switch_cantidad);
        ingenieriaRed.setCantidad(cantidad.getText().toString());

        instalacionPlantaExterna.addIngenieriaRed(ingenieriaRed);

        ingenieriaRed = new InstalacionPlantaExterna.IngenieriaRed();
        ingenieriaRed.setId(5);

        cliente = getView().findViewById(R.id.hub_cliente);
        ingenieriaRed.setCliente(cliente.getText().toString());

        equipo = getView().findViewById(R.id.hub_equipo);
        ingenieriaRed.setMarca(equipo.getText().toString());

        cantidad = getView().findViewById(R.id.hub_cantidad);
        ingenieriaRed.setCantidad(cantidad.getText().toString());

        instalacionPlantaExterna.addIngenieriaRed(ingenieriaRed);

        ingenieriaRed = new InstalacionPlantaExterna.IngenieriaRed();
        ingenieriaRed.setId(6);

        cliente = getView().findViewById(R.id.transceiver_cliente);
        ingenieriaRed.setCliente(cliente.getText().toString());

        equipo = getView().findViewById(R.id.transceiver_equipo);
        ingenieriaRed.setMarca(equipo.getText().toString());

        cantidad = getView().findViewById(R.id.transceiver_cantidad);
        ingenieriaRed.setCantidad(cantidad.getText().toString());

        instalacionPlantaExterna.addIngenieriaRed(ingenieriaRed);

        return instalacionPlantaExterna;
    }

    public void onView(InstalacionPlantaExterna instalacionPlantaExterna) {
        if (getView() == null) {
            return;
        }

        TextInputEditText requisitos = getView().findViewById(R.id.requisitos);
        requisitos.setFocusable(action);
        requisitos.setCursorVisible(action);
        requisitos.setText(instalacionPlantaExterna.getRequisitos());

        SwitchCompat ejecutar = getView().findViewById(R.id.ejecutar);
        ejecutar.setEnabled(action);
        ejecutar.setChecked(instalacionPlantaExterna.isCurso());

        TextInputEditText empresa = getView().findViewById(R.id.empresa);
        empresa.setFocusable(action);
        empresa.setCursorVisible(action);
        empresa.setText(instalacionPlantaExterna.getEmpresa());

        TextInputEditText fecha = getView().findViewById(R.id.fecha);
        fecha.setFocusable(action);
        fecha.setCursorVisible(action);
        fecha.setText(instalacionPlantaExterna.getFecha());

        TextInputEditText duracion = getView().findViewById(R.id.duracion);
        duracion.setFocusable(action);
        duracion.setCursorVisible(action);
        duracion.setText(instalacionPlantaExterna.getDuracion());

        TextInputEditText vigencia = getView().findViewById(R.id.vigencia);
        vigencia.setFocusable(action);
        vigencia.setCursorVisible(action);
        vigencia.setText(instalacionPlantaExterna.getVigencia());

        TextInputEditText nombre = getView().findViewById(R.id.nombre);
        nombre.setFocusable(action);
        nombre.setCursorVisible(action);
        nombre.setText(instalacionPlantaExterna.getPersonal());

        TextInputEditText telefono = getView().findViewById(R.id.telefono);
        telefono.setFocusable(action);
        telefono.setCursorVisible(action);
        telefono.setText(instalacionPlantaExterna.getTelefono());

        TextInputEditText tiempo = getView().findViewById(R.id.tiempo);
        tiempo.setFocusable(action);
        tiempo.setCursorVisible(action);
        tiempo.setText(instalacionPlantaExterna.getTiempo());

        for (InstalacionPlantaExterna.Infraestructura infraestructura : instalacionPlantaExterna.getInfraestructura()) {
            SwitchCompat convenio = getView().findViewById(R.id.convenio);
            convenio.setEnabled(action);
            convenio.setChecked(infraestructura.getConvenio());

            SwitchCompat radio = getView().findViewById(R.id.radio);
            radio.setEnabled(action);
            radio.setChecked(infraestructura.getRadio());

            SwitchCompat satelite = getView().findViewById(R.id.satelite);
            satelite.setEnabled(action);
            satelite.setChecked(infraestructura.getSatelite());

            SwitchCompat fibra = getView().findViewById(R.id.fibra);
            fibra.setEnabled(action);
            fibra.setChecked(infraestructura.getFibra());

            SwitchCompat gsmgprs = getView().findViewById(R.id.gsm_gprs);
            gsmgprs.setEnabled(action);
            gsmgprs.setChecked(infraestructura.getGsmgprs());
        }

        for (InstalacionPlantaExterna.IngenieriaRed ingenieriaRed : instalacionPlantaExterna.getIngenieriaRed()) {
            TextInputEditText cliente = null;
            TextInputEditText equipo = null;
            TextInputEditText cantidad = null;

            switch (ingenieriaRed.getId()) {
                case 1:
                    cliente = getView().findViewById(R.id.modem_radio_cliente);
                    equipo = getView().findViewById(R.id.modem_radio_equipo);
                    cantidad = getView().findViewById(R.id.modem_radio_cantidad);
                    break;

                case 2:
                    cliente = getView().findViewById(R.id.modem_satelital_cliente);
                    equipo = getView().findViewById(R.id.modem_satelital_equipo);
                    cantidad = getView().findViewById(R.id.modem_satelital_cantidad);
                    break;

                case 3:
                    cliente = getView().findViewById(R.id.router_cliente);
                    equipo = getView().findViewById(R.id.router_equipo);
                    cantidad = getView().findViewById(R.id.router_cantidad);
                    break;

                case 4:
                    cliente = getView().findViewById(R.id.switch_cliente);
                    equipo = getView().findViewById(R.id.switch_equipo);
                    cantidad = getView().findViewById(R.id.switch_cantidad);
                    break;

                case 5:
                    cliente = getView().findViewById(R.id.hub_cliente);
                    equipo = getView().findViewById(R.id.hub_equipo);
                    cantidad = getView().findViewById(R.id.hub_cantidad);
                    break;

                case 6:
                    cliente = getView().findViewById(R.id.transceiver_cliente);
                    equipo = getView().findViewById(R.id.transceiver_equipo);
                    cantidad = getView().findViewById(R.id.transceiver_cantidad);
                    break;
            }

            if (cliente != null) {
                cliente.setFocusable(action);
                cliente.setCursorVisible(action);
                cliente.setText(ingenieriaRed.getCliente());
            }

            if (equipo != null) {
                equipo.setFocusable(action);
                equipo.setCursorVisible(action);
                equipo.setText(ingenieriaRed.getMarca());
            }

            if (cantidad != null) {
                cantidad.setFocusable(action);
                cantidad.setCursorVisible(action);
                cantidad.setText(ingenieriaRed.getCantidad());
            }
        }
    }
}