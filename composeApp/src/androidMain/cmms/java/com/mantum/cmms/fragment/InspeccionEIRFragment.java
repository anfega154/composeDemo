package com.mantum.cmms.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Spinner;
import com.mantum.cmms.entity.Contenedor;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.EquipmentGrade;
import com.mantum.cmms.entity.EstadosInspeccion;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.component.DatePicker;
import com.mantum.component.component.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class InspeccionEIRFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "InspeccionEIR";
    private final static String CON_NOVEDAD = "Con novedad";

    private String key;
    private Database database;
    private boolean readOnly;
    private OnCompleteListener onCompleteListener;
    private Spinner equipmentSelected;
    private Spinner stateSelected;
    private Spinner novedadSelected;

    public InspeccionEIRFragment setKey(String key) {
        this.key = key;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_inspeccion_eir, container, false);

        database = new Database(view.getContext());
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta != null) {
            List<EstadosInspeccion> stateQuery = database.where(EstadosInspeccion.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .sort("name")
                    .findAll();

            List<Spinner> states = new ArrayList<>();
            for (EstadosInspeccion brand : stateQuery) {
                states.add(new Spinner(String.valueOf(brand.getId()), brand.getName()));
            }

            ArrayAdapter<Spinner> stateAdapter = new ArrayAdapter<>(
                    view.getContext(), android.R.layout.simple_spinner_dropdown_item, states);

            AutoCompleteTextView stateCompleteTextView = view.findViewById(R.id.estado_actual);
            stateCompleteTextView.setAdapter(stateAdapter);
            stateCompleteTextView.setOnItemClickListener((parent, view15, position, id) -> stateSelected = states.get(position));

            for (Spinner state : states) {
                if (state.getValue().equals("Ejecutado")) {
                    stateCompleteTextView.setText(state.getValue(), false);
                    stateSelected = state;
                    break;
                }
            }

            Contenedor contenedor = database.where(Contenedor.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .equalTo("key", key)
                    .equalTo("eir", true)
                    .findFirst();

            if (contenedor != null) {
                List<Spinner> equipmentGrades = new ArrayList<>();
                for (EquipmentGrade equipmentGrade : contenedor.getEquipmentgradevalidos()) {
                    equipmentGrades.add(new Spinner(String.valueOf(equipmentGrade.getId()), equipmentGrade.getName()));
                }

                ArrayAdapter<Spinner> equipmentGradeAdapter = new ArrayAdapter<>(
                        view.getContext(), android.R.layout.simple_spinner_dropdown_item, equipmentGrades);

                AutoCompleteTextView equipmentGradeCompleteTextView = view.findViewById(R.id.equipment);
                equipmentGradeCompleteTextView.setAdapter(equipmentGradeAdapter);
                equipmentGradeCompleteTextView.setOnItemClickListener((parent, view15, position, id) -> equipmentSelected = equipmentGrades.get(position));

                for (int i = 0; i < equipmentGradeAdapter.getCount(); i++) {
                    Spinner item = equipmentGradeAdapter.getItem(i);
                    if (item != null && item.getKey() != null && !"".equals(item.getKey())) {
                        if (Long.valueOf(item.getKey()).equals(contenedor.getIdclasificacion())) {
                            equipmentGradeCompleteTextView.setText(item.getValue(), false);
                            equipmentSelected = item;
                            break;
                        }
                    }
                }
            }
        }

        DatePicker date = new DatePicker(view.getContext(), view.findViewById(R.id.fecha));
        date.setEnabled(true);
        date.load(true);

        TimePicker time = new TimePicker(view.getContext(), view.findViewById(R.id.hora));
        time.setEnabled(true);
        time.load(true);

        int weekOfYear = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
        TextInputEditText week = view.findViewById(R.id.semana);
        week.setText(String.valueOf(weekOfYear));

        List<Spinner> novedades = new ArrayList<>();
        novedades.add(new Spinner("Sin novedad", "Sin novedad"));
        novedades.add(new Spinner(CON_NOVEDAD, CON_NOVEDAD));

        ArrayAdapter<Spinner> novedadesAdapter = new ArrayAdapter<>(
                view.getContext(), android.R.layout.simple_spinner_dropdown_item, novedades);

        AutoCompleteTextView novedadAutoCompleteTextView = view.findViewById(R.id.novedad);
        novedadAutoCompleteTextView.setAdapter(novedadesAdapter);
        novedadAutoCompleteTextView.setOnItemClickListener((parent, view12, position, id) -> novedadSelected = novedades.get(position));

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onCompleteListener.onComplete(KEY_TAB);
    }

    @Override
    public void onAttach(@NonNull Context context) {
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

    @NonNull
    public InspeccionEIRFragment readOnly() {
        readOnly = true;
        return this;
    }

    public void init(String fecha, String tecnico, String linea, String tipo, String code) {
        if (getView() == null) {
            return;
        }

        TextInputEditText tecnicoView = getView().findViewById(R.id.tecnico);
        tecnicoView.setText(tecnico);

        TextInputEditText fechaView = getView().findViewById(R.id.fecha_ultimo_eir);
        fechaView.setText(fecha);

        TextInputEditText lineaView = getView().findViewById(R.id.linea);
        lineaView.setText(linea);

        TextInputEditText tipoView = getView().findViewById(R.id.tipo);
        tipoView.setText(tipo);

        TextInputEditText codigoContenedorView = getView().findViewById(R.id.numero_contenedor);
        codigoContenedorView.setText(code);
    }

    private boolean requireValidate() {
        if (database.isClosed() || stateSelected == null) {
            return true;
        }

        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta != null && stateSelected.getKey() != null) {
            EstadosInspeccion state = database.where(EstadosInspeccion.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .equalTo("id", Integer.valueOf(stateSelected.getKey()))
                    .findFirst();

            if (state != null) {
                return state.isValidate();
            }
        }

        return true;
    }

    private boolean requiereFalla() {
        if (database.isClosed() || stateSelected == null) {
            return false;
        }

        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta != null && stateSelected.getKey() != null) {
            EstadosInspeccion state = database.where(EstadosInspeccion.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .equalTo("id", Integer.valueOf(stateSelected.getKey()))
                    .findFirst();

            if (state != null) {
                return state.isRequierefalla();
            }
        }

        return false;
    }

    @Nullable
    public Contenedor.Request getValue() {
        if (getView() == null) {
            return null;
        }

        boolean isOk = true;
        boolean isRequireFalla = requiereFalla();
        boolean isRequireValidate = requireValidate();

        Contenedor.Request request = new Contenedor.Request();

        TextInputEditText tecnicoView = getView().findViewById(R.id.tecnico);
        if (tecnicoView.getText() != null) {
            request.setTecnico(tecnicoView.getText().toString());
        }

        TextInputEditText fechaUltimoPTIView = getView().findViewById(R.id.fecha_ultimo_eir);
        if (fechaUltimoPTIView.getText() != null) {
            request.setFechaultimopti(fechaUltimoPTIView.getText().toString());
        }

        TextInputEditText lineaView = getView().findViewById(R.id.linea);
        if (lineaView.getText() != null) {
            request.setLineaNaviera(lineaView.getText().toString());
        }

        TextInputEditText tipoView = getView().findViewById(R.id.tipo);
        if (tipoView.getText() != null) {
            request.setTipo(tipoView.getText().toString());
        }

        TextInputEditText conductorView = getView().findViewById(R.id.conductor);
        if (conductorView.getText() != null && !conductorView.getText().toString().isEmpty()) {
            request.setConductor(conductorView.getText().toString());

            TextInputLayout textInputLayout = getView().findViewById(R.id.conductor_contenedor);
            textInputLayout.setErrorEnabled(false);
        }

        TextInputEditText cedulaView = getView().findViewById(R.id.cedula);
        if (cedulaView.getText() != null && !cedulaView.getText().toString().isEmpty()) {
            request.setCedula(cedulaView.getText().toString());

            TextInputLayout textInputLayout = getView().findViewById(R.id.cedula_contenedor);
            textInputLayout.setErrorEnabled(false);
        }

        TextInputEditText placaView = getView().findViewById(R.id.placa);
        if (placaView.getText() != null && !placaView.getText().toString().isEmpty()) {
            request.setPlaca(placaView.getText().toString());

            TextInputLayout textInputLayout = getView().findViewById(R.id.placa_contenedor);
            textInputLayout.setErrorEnabled(false);
        }

        TextInputEditText propositoView = getView().findViewById(R.id.proposito);
        if (propositoView.getText() != null && !propositoView.getText().toString().isEmpty()) {
            request.setProposito(propositoView.getText().toString());

            TextInputLayout textInputLayout = getView().findViewById(R.id.proposito_contenedor);
            textInputLayout.setErrorEnabled(false);
        }

        TextInputEditText fechaView = getView().findViewById(R.id.fecha);
        if (fechaView.getText() != null && !fechaView.getText().toString().isEmpty()) {
            request.setFechainspeccion(fechaView.getText().toString());

            TextInputLayout textInputLayout = getView().findViewById(R.id.fecha_contenedor);
            textInputLayout.setErrorEnabled(false);
        } else {
            if (isRequireValidate) {
                isOk = false;
                TextInputLayout textInputLayout = getView().findViewById(R.id.fecha_contenedor);
                textInputLayout.setError(getView().getContext().getString(R.string.fecha_requerido));
                textInputLayout.setErrorEnabled(true);
            }
        }

        TextInputEditText horaView = getView().findViewById(R.id.hora);
        if (horaView.getText() != null && !horaView.getText().toString().isEmpty()) {
            request.setHoraInspeccion(horaView.getText().toString());

            TextInputLayout textInputLayout = getView().findViewById(R.id.hora_contenedor);
            textInputLayout.setErrorEnabled(false);
        } else {
            if (isRequireValidate) {
                isOk = false;
                TextInputLayout textInputLayout = getView().findViewById(R.id.hora_contenedor);
                textInputLayout.setError(getView().getContext().getString(R.string.hora_requerida));
                textInputLayout.setErrorEnabled(true);
            }
        }

        if (equipmentSelected != null && equipmentSelected.getKey() != null) {
            request.setIdEquipmentGrade(Long.valueOf(equipmentSelected.getKey()));
            request.setEquipmentgrade(equipmentSelected.getValue());

            TextInputLayout textInputLayout = getView().findViewById(R.id.equipment_contenedor);
            textInputLayout.setErrorEnabled(false);
        } else {
            if (isRequireValidate) {
                isOk = false;
                TextInputLayout textInputLayout = getView().findViewById(R.id.equipment_contenedor);
                textInputLayout.setError(getView().getContext().getString(R.string.equipment_requerida));
                textInputLayout.setErrorEnabled(true);
            }
        }

        TextInputEditText semanaView = getView().findViewById(R.id.semana);
        if (semanaView.getText() != null && !semanaView.getText().toString().isEmpty()) {
            request.setSemana(Integer.valueOf(semanaView.getText().toString()));

            TextInputLayout textInputLayout = getView().findViewById(R.id.semana_contenedor);
            textInputLayout.setErrorEnabled(false);
        } else {
            if (isRequireValidate) {
                isOk = false;
                TextInputLayout textInputLayout = getView().findViewById(R.id.semana_contenedor);
                textInputLayout.setError(getView().getContext().getString(R.string.semana_requerido));
                textInputLayout.setErrorEnabled(true);
            }
        }

        TextInputEditText observacionView = getView().findViewById(R.id.observacion);
        if (observacionView.getText() != null && !observacionView.getText().toString().isEmpty()) {
            request.setObservaciones(observacionView.getText().toString());

            TextInputLayout textInputLayout = getView().findViewById(R.id.observacion_contenedor);
            textInputLayout.setErrorEnabled(false);
        }

        if (stateSelected != null && stateSelected.getKey() != null) {
            request.setIdestadoregistro(Long.valueOf(stateSelected.getKey()));
            request.setEstadoregistro(stateSelected.getValue());

            TextInputLayout textInputLayout = getView().findViewById(R.id.estado_actual_contenedor);
            textInputLayout.setErrorEnabled(false);
        } else {
            if (isRequireValidate) {
                isOk = false;
                TextInputLayout textInputLayout = getView().findViewById(R.id.estado_actual_contenedor);
                textInputLayout.setError(getView().getContext().getString(R.string.estado_requerido));
                textInputLayout.setErrorEnabled(true);
            }
        }

        if (novedadSelected != null && novedadSelected.getKey() != null) {
            request.setNovedad(novedadSelected.getKey());

            TextInputLayout textInputLayout = getView().findViewById(R.id.novedad_contenedor);
            textInputLayout.setErrorEnabled(false);
        } else {
            if (isRequireValidate) {
                isOk = false;
                TextInputLayout textInputLayout = getView().findViewById(R.id.novedad_contenedor);
                textInputLayout.setError(getView().getContext().getString(R.string.novedad_requerido));
                textInputLayout.setErrorEnabled(true);
            }
        }

        SwitchCompat drainCleanAndFreeView = getView().findViewById(R.id.drain_clean_and_free);
        if (drainCleanAndFreeView != null) {
            request.setDraincleanandfree(drainCleanAndFreeView.isChecked());
        }

        if (!isOk) {
            return null;
        }

        if (!isRequireFalla && novedadSelected != null) {
            if (CON_NOVEDAD.equals(novedadSelected.getKey())) {
                isRequireFalla = true;
            }
        }

        request.setProceso("EIR");
        request.setRequiereFalla(isRequireFalla);
        return request;
    }

    public InspeccionEIRFragment onLoad(@NonNull Contenedor.Request request) {
        if (getView() == null) {
            return this;
        }

        TextInputEditText tecnicoView = getView().findViewById(R.id.tecnico);
        tecnicoView.setText(request.getTecnico());

        if (readOnly) {
            tecnicoView.setFocusable(false);
            tecnicoView.setCursorVisible(false);
        }

        TextInputEditText fechaUltimoPTIView = getView().findViewById(R.id.fecha_ultimo_eir);
        fechaUltimoPTIView.setText(request.getFechaultimopti());

        if (readOnly) {
            fechaUltimoPTIView.setFocusable(false);
            fechaUltimoPTIView.setCursorVisible(false);
        }

        TextInputEditText lineaView = getView().findViewById(R.id.linea);
        lineaView.setText(request.getLineaNaviera());

        if (readOnly) {
            lineaView.setFocusable(false);
            lineaView.setCursorVisible(false);
        }

        TextInputEditText tipoView = getView().findViewById(R.id.tipo);
        tipoView.setText(request.getTipo());

        if (readOnly) {
            tipoView.setFocusable(false);
            tipoView.setCursorVisible(false);
        }

        TextInputEditText fechaView = getView().findViewById(R.id.fecha);
        fechaView.setText(request.getFechainspeccion());

        if (readOnly) {
            TextInputLayout fechaContenedorView = getView().findViewById(R.id.fecha_contenedor);
            fechaContenedorView.setBoxBackgroundColor(getResources().getColor(R.color.gray_3));

            fechaView.setFocusable(false);
            fechaView.setCursorVisible(false);
            fechaView.setOnClickListener(null);
        }

        TextInputEditText horaView = getView().findViewById(R.id.hora);
        fechaView.setText(request.getFechainspeccion());

        if (readOnly) {
            TextInputLayout horaContenedorView = getView().findViewById(R.id.hora_contenedor);
            horaContenedorView.setBoxBackgroundColor(getResources().getColor(R.color.gray_3));

            horaView.setFocusable(false);
            horaView.setCursorVisible(false);
            horaView.setOnClickListener(null);
        }

        AutoCompleteTextView equipmentView = getView().findViewById(R.id.equipment);
        equipmentView.setText(request.getEquipmentgrade(), false);

        if (request.getIdequipmentgrade() != null && request.getEquipmentgrade() != null) {
            equipmentSelected = new Spinner(
                    String.valueOf(request.getIdequipmentgrade()),
                    request.getEquipmentgrade()
            );
        }

        if (readOnly) {
            equipmentView.setAdapter(null);

            TextInputLayout equipmentContenedorView = getView().findViewById(R.id.equipment_contenedor);
            equipmentContenedorView.setBoxBackgroundColor(getResources().getColor(R.color.gray_3));

            equipmentView.setFocusable(false);
            equipmentView.setCursorVisible(false);
            equipmentView.setOnClickListener(null);
        }

        TextInputEditText semanaView = getView().findViewById(R.id.semana);
        semanaView.setText(String.valueOf(request.getSemana()));

        if (readOnly) {
            semanaView.setFocusable(false);
            semanaView.setCursorVisible(false);
        }

        TextInputEditText observacionView = getView().findViewById(R.id.observacion);
        observacionView.setText(request.getObservaciones());

        if (readOnly) {
            TextInputLayout observacionContenedorView = getView().findViewById(R.id.observacion_contenedor);
            observacionContenedorView.setBoxBackgroundColor(getResources().getColor(R.color.gray_3));

            observacionView.setFocusable(false);
            observacionView.setCursorVisible(false);
        }

        AutoCompleteTextView stateView = getView().findViewById(R.id.estado_actual);
        stateView.setText(request.getEstadoregistro(), false);

        if (request.getIdestadoregistro() != null && request.getEstadoregistro() != null) {
            stateSelected = new Spinner(
                    String.valueOf(request.getIdestadoregistro()),
                    request.getEstadoregistro()
            );
        }

        if (readOnly) {
            stateView.setAdapter(null);

            TextInputLayout modeloContenedorView = getView().findViewById(R.id.estado_actual_contenedor);
            modeloContenedorView.setBoxBackgroundColor(getResources().getColor(R.color.gray_3));

            stateView.setFocusable(false);
            stateView.setCursorVisible(false);
            stateView.setOnClickListener(null);
        }

        AutoCompleteTextView novedadView = getView().findViewById(R.id.novedad);
        novedadView.setText(request.getNovedad(), false);

        if (request.getNovedad() != null) {
            novedadSelected = new Spinner(
                    request.getNovedad(),
                    request.getNovedad()
            );
        }

        if (readOnly) {
            novedadView.setAdapter(null);

            TextInputLayout novedadContenedorView = getView().findViewById(R.id.novedad_contenedor);
            novedadContenedorView.setBoxBackgroundColor(getResources().getColor(R.color.gray_3));

            novedadView.setFocusable(false);
            novedadView.setCursorVisible(false);
            novedadView.setOnClickListener(null);
        }

        TextInputEditText conductorView = getView().findViewById(R.id.conductor);
        conductorView.setText(request.getConductor());

        if (readOnly) {
            TextInputLayout conductorContenedorView = getView().findViewById(R.id.conductor_contenedor);
            conductorContenedorView.setBoxBackgroundColor(getResources().getColor(R.color.gray_3));

            conductorView.setFocusable(false);
            conductorView.setCursorVisible(false);
        }

        TextInputEditText cedulaView = getView().findViewById(R.id.cedula);
        cedulaView.setText(request.getCedula());

        if (readOnly) {
            TextInputLayout cedulaContenedorView = getView().findViewById(R.id.cedula_contenedor);
            cedulaContenedorView.setBoxBackgroundColor(getResources().getColor(R.color.gray_3));

            cedulaView.setFocusable(false);
            cedulaView.setCursorVisible(false);
        }

        TextInputEditText placaView = getView().findViewById(R.id.placa);
        placaView.setText(request.getPlaca());

        if (readOnly) {
            TextInputLayout placaContenedorView = getView().findViewById(R.id.placa_contenedor);
            placaContenedorView.setBoxBackgroundColor(getResources().getColor(R.color.gray_3));

            placaView.setFocusable(false);
            placaView.setCursorVisible(false);
        }

        TextInputEditText propositoView = getView().findViewById(R.id.proposito);
        propositoView.setText(request.getProposito());

        if (readOnly) {
            TextInputLayout propositoContenedorView = getView().findViewById(R.id.proposito_contenedor);
            propositoContenedorView.setBoxBackgroundColor(getResources().getColor(R.color.gray_3));

            propositoView.setFocusable(false);
            propositoView.setCursorVisible(false);
        }

        SwitchCompat drainCleanAndFreeView = getView().findViewById(R.id.drain_clean_and_free);
        drainCleanAndFreeView.setChecked(request.isDraincleanandfree());

        if (readOnly) {
            drainCleanAndFreeView.setFocusable(false);
            drainCleanAndFreeView.setCursorVisible(false);
        }

        return this;
    }
}
