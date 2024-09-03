package com.mantum.cmms.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Spinner;
import com.mantum.cmms.entity.Contenedor;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.EquipmentGrade;
import com.mantum.cmms.entity.EstadosInspeccion;
import com.mantum.cmms.entity.MarcaCEM;
import com.mantum.cmms.entity.ModeloCEM;
import com.mantum.cmms.entity.Yarda;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.component.DatePicker;
import com.mantum.component.component.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class InspeccionPTIFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "InspeccionPTI";
    private final static String CON_NOVEDAD = "Con novedad";

    private String key;
    private boolean readOnly;
    private Database database;
    private OnCompleteListener onCompleteListener;

    private Spinner degreeSelected;
    private Spinner refrigerantSelected;
    private Spinner brandsSelected;
    private Spinner modelSelected;
    private Spinner yardaSelected;
    private Spinner equipmentGradeSelected;
    private Spinner stateSelected;
    private Spinner novedadSelected;
    private DatePicker creationDate;

    public InspeccionPTIFragment setKey(String key) {
        this.key = key;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_inspeccion_pti, container, false);

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
            for (EstadosInspeccion state : stateQuery) {
                states.add(new Spinner(String.valueOf(state.getId()), state.getName()));
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
                    .equalTo("pti", true)
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
                equipmentGradeCompleteTextView.setOnItemClickListener((parent, view15, position, id) -> equipmentGradeSelected = equipmentGrades.get(position));

                String equipment = contenedor.getEquipmentgrade();
                if (equipment != null && !equipment.isEmpty()) {
                    for (int i = 0; i < equipmentGradeAdapter.getCount(); i++) {
                        Spinner item = equipmentGradeAdapter.getItem(i);
                        if (item != null && equipment.equals(item.getValue())) {
                            equipmentGradeCompleteTextView.setText(item.getValue(), false);
                            equipmentGradeSelected = item;
                            break;
                        }
                    }
                }
            }

            List<MarcaCEM> brandsQuery = database.where(MarcaCEM.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .sort("name")
                    .findAll();

            List<Spinner> brands = new ArrayList<>();
            for (MarcaCEM brand : brandsQuery) {
                brands.add(new Spinner(String.valueOf(brand.getId()), brand.getName()));
            }

            ArrayAdapter<Spinner> brandAdapter = new ArrayAdapter<>(
                    view.getContext(), android.R.layout.simple_spinner_dropdown_item, brands);

            AutoCompleteTextView brandAutoCompleteTextView = view.findViewById(R.id.marca);
            brandAutoCompleteTextView.setAdapter(brandAdapter);
            brandAutoCompleteTextView.setOnItemClickListener((parent, view1, position, id) -> {
                brandsSelected = brands.get(position);
                prepareMarca();
            });

            if (brandAdapter.getCount() == 1) {
                Spinner current = brandAdapter.getItem(0);
                if (current != null) {
                    brandAutoCompleteTextView.setText(current.getValue(), false);

                    brandsSelected = current;
                    prepareMarca();
                }
            }

            List<Yarda> yardaQuery = database.where(Yarda.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .equalTo("xpti", true)
                    .sort("nombre")
                    .findAll();

            List<Spinner> yardas = new ArrayList<>();
            for (Yarda yarda : yardaQuery) {
                yardas.add(new Spinner(String.valueOf(yarda.getId()), yarda.getNombre()));
            }

            ArrayAdapter<Spinner> yardaAdapter = new ArrayAdapter<>(
                    view.getContext(), android.R.layout.simple_spinner_dropdown_item, yardas);

            AutoCompleteTextView yardaAutoCompleteTextView = view.findViewById(R.id.yarda);
            yardaAutoCompleteTextView.setAdapter(yardaAdapter);
            yardaAutoCompleteTextView.setOnItemClickListener((parent, view15, position, id) -> yardaSelected = yardas.get(position));
        }

        List<Spinner> novedades = new ArrayList<>();
        novedades.add(new Spinner("Sin novedad", "Sin novedad"));
        novedades.add(new Spinner(CON_NOVEDAD, CON_NOVEDAD));

        ArrayAdapter<Spinner> novedadesAdapter = new ArrayAdapter<>(
                view.getContext(), android.R.layout.simple_spinner_dropdown_item, novedades);

        AutoCompleteTextView novedadAutoCompleteTextView = view.findViewById(R.id.novedad);
        novedadAutoCompleteTextView.setAdapter(novedadesAdapter);
        novedadAutoCompleteTextView.setOnItemClickListener((parent, view12, position, id) -> novedadSelected = novedades.get(position));

        List<Spinner> degree = new ArrayList<>();
        degree.add(new Spinner("C", "ºC"));
        degree.add(new Spinner("F", "ºF"));

        ArrayAdapter<Spinner> degreeAdapter = new ArrayAdapter<>(
                view.getContext(), android.R.layout.simple_spinner_dropdown_item, degree);

        AutoCompleteTextView degreeAutoCompleteTextView = view.findViewById(R.id.grados);
        degreeAutoCompleteTextView.setAdapter(degreeAdapter);
        degreeAutoCompleteTextView.setOnItemClickListener((parent, view12, position, id) -> degreeSelected = degree.get(position));

        degreeSelected = degree.get(0);
        degreeAutoCompleteTextView.setText(degreeSelected.getValue(), false);

        DatePicker date = new DatePicker(view.getContext(), view.findViewById(R.id.fecha));
        date.setEnabled(true);
        date.load(true);

        creationDate = new DatePicker(
                view.getContext(), view.findViewById(R.id.fecha_fabricacion));
        creationDate.setEnabled(true);
        creationDate.load(true);

        TimePicker time = new TimePicker(view.getContext(), view.findViewById(R.id.hora));
        time.setEnabled(true);
        time.load(true);

        int weekOfYear = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
        TextInputEditText week = view.findViewById(R.id.semana);
        week.setText(String.valueOf(weekOfYear));

        List<Spinner> refrigerant = new ArrayList<>();
        refrigerant.add(new Spinner("bajo", "Bajo"));
        refrigerant.add(new Spinner("normal", "Normal"));

        ArrayAdapter<Spinner> refrigerantAdapter = new ArrayAdapter<>(
                view.getContext(), android.R.layout.simple_spinner_dropdown_item, refrigerant);

        AutoCompleteTextView refrigerantAutoCompleteTextView = view.findViewById(R.id.refrigerante);
        refrigerantAutoCompleteTextView.setAdapter(refrigerantAdapter);
        refrigerantAutoCompleteTextView.setOnItemClickListener((parent, view13, position, id) -> refrigerantSelected = refrigerant.get(position));

        return view;
    }

    private void prepareMarca() {
        if (getView() == null || brandsSelected == null || brandsSelected.getKey() == null) {
            return;
        }

        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return;
        }

        List<ModeloCEM> modelQuery = database.where(ModeloCEM.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("idMarca", Long.valueOf(brandsSelected.getKey()))
                .sort("name")
                .findAll();

        List<Spinner> models = new ArrayList<>();
        for (ModeloCEM model : modelQuery) {
            models.add(new Spinner(String.valueOf(model.getId()), model.getName()));
        }

        ArrayAdapter<Spinner> modelAdapter = new ArrayAdapter<>(
                getView().getContext(), android.R.layout.simple_spinner_dropdown_item, models);

        AutoCompleteTextView modelAutoCompleteTextView = getView().findViewById(R.id.modelo);
        modelAutoCompleteTextView.setAdapter(modelAdapter);
        modelAutoCompleteTextView.setOnItemClickListener((parent1, view2, position1, id1) -> modelSelected = models.get(position1));
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

        TextInputEditText fechaUltimoPTIView = getView().findViewById(R.id.fecha_ultimo_pti);
        if (fechaUltimoPTIView.getText() != null) {
            request.setFechaultimopti(fechaUltimoPTIView.getText().toString());
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

        TextInputEditText serialView = getView().findViewById(R.id.serial);
        if (serialView.getText() != null && !serialView.getText().toString().isEmpty()) {
            request.setSerial(serialView.getText().toString());

            TextInputLayout textInputLayout = getView().findViewById(R.id.serial_contenedor);
            textInputLayout.setErrorEnabled(false);
        } else {
            if (isRequireValidate) {
                isOk = false;
                TextInputLayout textInputLayout = getView().findViewById(R.id.serial_contenedor);
                textInputLayout.setError(getView().getContext().getString(R.string.serial_requerida));
                textInputLayout.setErrorEnabled(true);
            }
        }

        TextInputEditText softwareView = getView().findViewById(R.id.software);
        if (softwareView.getText() != null && !softwareView.getText().toString().isEmpty()) {
            request.setSoftware(softwareView.getText().toString());

            TextInputLayout textInputLayout = getView().findViewById(R.id.software_contenedor);
            textInputLayout.setErrorEnabled(false);
        } else {
            if (isRequireValidate) {
                isOk = false;
                TextInputLayout textInputLayout = getView().findViewById(R.id.software_contenedor);
                textInputLayout.setError(getView().getContext().getString(R.string.software_requerida));
                textInputLayout.setErrorEnabled(true);
            }
        }

        TextInputEditText fechaFabricacionView = getView().findViewById(R.id.fecha_fabricacion);
        if (fechaFabricacionView.getText() != null && !fechaFabricacionView.getText().toString().isEmpty()) {
            request.setFechafabricacion(fechaFabricacionView.getText().toString());

            TextInputLayout textInputLayout = getView().findViewById(R.id.fecha_fabricacion_contenedor);
            textInputLayout.setErrorEnabled(false);
        } else {
            if (isRequireValidate) {
                isOk = false;
                TextInputLayout textInputLayout = getView().findViewById(R.id.fecha_fabricacion_contenedor);
                textInputLayout.setError(getView().getContext().getString(R.string.fecha_fabricacion_requerida));
                textInputLayout.setErrorEnabled(true);
            }
        }

        TextInputEditText temperaturaView = getView().findViewById(R.id.temperatura);
        if (temperaturaView.getText() != null && !temperaturaView.getText().toString().isEmpty()) {
            request.setTemperatura(temperaturaView.getText().toString());

            TextInputLayout textInputLayout = getView().findViewById(R.id.temperatura_contenedor);
            textInputLayout.setErrorEnabled(false);
        } else {
            if (isRequireValidate) {
                isOk = false;
                TextInputLayout textInputLayout = getView().findViewById(R.id.temperatura_contenedor);
                textInputLayout.setError(getView().getContext().getString(R.string.temperatura_requerida));
                textInputLayout.setErrorEnabled(true);
            }
        }

        if (stateSelected != null && stateSelected.getKey() != null) {
            request.setIdestadoregistro(Long.valueOf(stateSelected.getKey()));
            request.setEstadoregistro(stateSelected.getValue());

            TextInputLayout textInputLayout = getView().findViewById(R.id.estado_actual_contenedor);
            textInputLayout.setErrorEnabled(false);
        } else {
            isOk = false;
            TextInputLayout textInputLayout = getView().findViewById(R.id.estado_actual_contenedor);
            textInputLayout.setError(getView().getContext().getString(R.string.estado_requerido));
            textInputLayout.setErrorEnabled(true);
        }

        if (degreeSelected != null) {
            request.setGrados(degreeSelected.getKey());
            request.setGradosText(degreeSelected.getValue());

            TextInputLayout textInputLayout = getView().findViewById(R.id.grados_contenedor);
            textInputLayout.setErrorEnabled(false);
        } else {
            if (isRequireValidate) {
                isOk = false;
                TextInputLayout textInputLayout = getView().findViewById(R.id.grados_contenedor);
                textInputLayout.setError(getView().getContext().getString(R.string.grados_requeridos));
                textInputLayout.setErrorEnabled(true);
            }
        }

        if (refrigerantSelected != null && refrigerantSelected.getKey() != null) {
            request.setNivelrefrigerante(refrigerantSelected.getKey());
            request.setNivelrefrigeranteText(refrigerantSelected.getValue());

            TextInputLayout textInputLayout = getView().findViewById(R.id.refrigerante_contenedor);
            textInputLayout.setErrorEnabled(false);
        } else {
            if (isRequireValidate) {
                isOk = false;
                TextInputLayout textInputLayout = getView().findViewById(R.id.refrigerante_contenedor);
                textInputLayout.setError(getView().getContext().getString(R.string.refrigerante_requerido));
                textInputLayout.setErrorEnabled(true);
            }
        }

        TextInputEditText lineaNavieraView = getView().findViewById(R.id.linea);
        if (lineaNavieraView.getText() != null && !lineaNavieraView.getText().toString().isEmpty()) {
            request.setLineaNaviera(lineaNavieraView.getText().toString());

            TextInputLayout textInputLayout = getView().findViewById(R.id.linea_contenedor);
            textInputLayout.setErrorEnabled(false);
        }

        if (brandsSelected != null && brandsSelected.getKey() != null) {
            request.setIdmarca(null);
            if (!brandsSelected.getKey().equals("")) {
                request.setIdmarca(Long.valueOf(brandsSelected.getKey()));
            }
            request.setMarca(brandsSelected.getValue());

            TextInputLayout textInputLayout = getView().findViewById(R.id.marca_contenedor);
            textInputLayout.setErrorEnabled(false);
        } else {
            if (isRequireValidate) {
                isOk = false;
                TextInputLayout textInputLayout = getView().findViewById(R.id.marca_contenedor);
                textInputLayout.setError(getView().getContext().getString(R.string.marca_requrido));
                textInputLayout.setErrorEnabled(true);
            }
        }

        if (modelSelected != null && modelSelected.getKey() != null) {
            request.setIdmodelo(null);
            if (!modelSelected.getKey().equals("")) {
                request.setIdmodelo(Long.valueOf(modelSelected.getKey()));
            }
            request.setModelo(modelSelected.getValue());

            TextInputLayout textInputLayout = getView().findViewById(R.id.modelo_contenedor);
            textInputLayout.setErrorEnabled(false);
        } else {
            if (isRequireValidate) {
                isOk = false;
                TextInputLayout textInputLayout = getView().findViewById(R.id.modelo_contenedor);
                textInputLayout.setError(getView().getContext().getString(R.string.modelo_requerido));
                textInputLayout.setErrorEnabled(true);
            }
        }

        if (equipmentGradeSelected != null && equipmentGradeSelected.getKey() != null) {
            request.setIdEquipmentGrade(Long.valueOf(equipmentGradeSelected.getKey()));
            request.setEquipmentgrade(equipmentGradeSelected.getValue());

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

        if (yardaSelected != null && yardaSelected.getKey() != null) {
            request.setIdyardainspeccion(Long.valueOf(yardaSelected.getKey()));
            request.setYardainspeccion(yardaSelected.getValue());

            TextInputLayout textInputLayout = getView().findViewById(R.id.yarda_contenedor);
            textInputLayout.setErrorEnabled(false);
        } else {
            if (isRequireValidate) {
                isOk = false;
                TextInputLayout textInputLayout = getView().findViewById(R.id.yarda_contenedor);
                textInputLayout.setError(getView().getContext().getString(R.string.yarda_requerido));
                textInputLayout.setErrorEnabled(true);
            }
        }

        TextInputEditText observacionView = getView().findViewById(R.id.observacion);
        if (observacionView.getText() != null && !observacionView.getText().toString().isEmpty()) {
            request.setObservaciones(observacionView.getText().toString());

            TextInputLayout textInputLayout = getView().findViewById(R.id.observacion_contenedor);
            textInputLayout.setErrorEnabled(false);
        }

        if (!isOk) {
            return null;
        }

        if (!isRequireFalla && novedadSelected != null) {
            if (CON_NOVEDAD.equals(novedadSelected.getKey())) {
                isRequireFalla = true;
            }
        }

        request.setProceso("PTI");
        request.setRequiereFalla(isRequireFalla);
        request.setRequiereValidacion(isRequireValidate);
        return request;
    }

    public void init(Contenedor.Request request) {
        if (getView() == null) {
            return;
        }

        TextInputEditText tecnicoView = getView().findViewById(R.id.tecnico);
        tecnicoView.setText(request.getTecnico());

        TextInputEditText fechaView = getView().findViewById(R.id.fecha_ultimo_pti);
        fechaView.setText(request.getFechaultimopti());

        TextInputEditText lineaView = getView().findViewById(R.id.linea);
        lineaView.setText(request.getLineaNaviera());

        TextInputEditText serialView = getView().findViewById(R.id.serial);
        serialView.setText(request.getSerial());

        TextInputEditText softwareView = getView().findViewById(R.id.software);
        softwareView.setText(request.getSoftware());

        TextInputEditText codigoContenedorView = getView().findViewById(R.id.numero_contenedor);
        codigoContenedorView.setText(request.getCodigo());

        if (request.getFechafabricacion() != null && !request.getFechafabricacion().isEmpty()) {
            creationDate.setValue(request.getFechafabricacion());
        }

        AutoCompleteTextView marcaAutoCompleteTextView = getView().findViewById(R.id.marca);
        marcaAutoCompleteTextView.setText(request.getMarca(), false);

        if (request.getIdmarca() != null) {
            brandsSelected = new Spinner(String.valueOf(request.getIdmarca()), request.getMarca());
            prepareMarca();
        }

        AutoCompleteTextView modeloAutoCompleteTextView = getView().findViewById(R.id.modelo);
        modeloAutoCompleteTextView.setText(request.getModelo(), false);

        if (request.getIdmodelo() != null) {
            modelSelected = new Spinner(String.valueOf(request.getIdmodelo()), request.getModelo());
        }

        if (request.getYardainspeccion() != null && !request.getYardainspeccion().isEmpty()) {
            AutoCompleteTextView yardaAutoCompleteTextView = getView().findViewById(R.id.yarda);
            if (yardaAutoCompleteTextView != null) {
                ListAdapter adapter = yardaAutoCompleteTextView.getAdapter();
                if (adapter != null) {
                    for (int i = 0; i < adapter.getCount(); i++) {
                        Spinner item = (Spinner) adapter.getItem(i);
                        if (item.getValue().equals(request.getYardainspeccion())) {
                            yardaAutoCompleteTextView.setText(item.getValue(), false);
                            yardaSelected = item;
                            break;
                        }
                    }
                }
            }
        }

        if (request.getEquipmentgrade() != null) {
            AutoCompleteTextView equipmentGradeAutoCompleteTextView = getView().findViewById(R.id.equipment);
            if (equipmentGradeAutoCompleteTextView != null) {
                ListAdapter adapter = equipmentGradeAutoCompleteTextView.getAdapter();
                if (adapter != null) {
                    for (int i = 0; i < adapter.getCount(); i++) {
                        Spinner item = (Spinner) adapter.getItem(i);
                        if (item != null && item.getKey() != null && !"".equals(item.getKey())) {
                            if (Long.valueOf(item.getKey()).equals(request.getIdclasificacion())) {
                                equipmentGradeAutoCompleteTextView.setText(item.getValue(), false);
                                equipmentGradeSelected = item;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    @NonNull
    public InspeccionPTIFragment readOnly() {
        readOnly = true;
        return this;
    }

    public InspeccionPTIFragment onLoad(@NonNull Contenedor.Request request) {
        if (getView() == null) {
            return this;
        }

        TextInputEditText tecnicoView = getView().findViewById(R.id.tecnico);
        tecnicoView.setText(request.getTecnico());

        if (readOnly) {
            tecnicoView.setFocusable(false);
            tecnicoView.setCursorVisible(false);
        }

        TextInputEditText fechaUltimoPTIView = getView().findViewById(R.id.fecha_ultimo_pti);
        fechaUltimoPTIView.setText(request.getFechaultimopti());

        if (readOnly) {
            fechaUltimoPTIView.setFocusable(false);
            fechaUltimoPTIView.setCursorVisible(false);
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

        TextInputEditText fechaFabricacionView = getView().findViewById(R.id.fecha_fabricacion);
        fechaFabricacionView.setText(request.getFechafabricacion());

        if (readOnly) {
            TextInputLayout fechaFabricacionContenedorView = getView().findViewById(R.id.fecha_fabricacion_contenedor);
            fechaFabricacionContenedorView.setBoxBackgroundColor(getResources().getColor(R.color.gray_3));

            fechaFabricacionView.setFocusable(false);
            fechaFabricacionView.setCursorVisible(false);
            fechaFabricacionView.setOnClickListener(null);
        }

        TextInputEditText serialView = getView().findViewById(R.id.serial);
        serialView.setText(request.getSerial());

        if (readOnly) {
            TextInputLayout serialContenedorView = getView().findViewById(R.id.serial_contenedor);
            serialContenedorView.setBoxBackgroundColor(getResources().getColor(R.color.gray_3));

            serialView.setFocusable(false);
            serialView.setCursorVisible(false);
        }

        TextInputEditText softwareView = getView().findViewById(R.id.software);
        softwareView.setText(request.getSoftware());

        if (readOnly) {
            TextInputLayout softwareContenedorView = getView().findViewById(R.id.software_contenedor);
            softwareContenedorView.setBoxBackgroundColor(getResources().getColor(R.color.gray_3));

            softwareView.setFocusable(false);
            softwareView.setCursorVisible(false);
        }

        TextInputEditText temperaturaView = getView().findViewById(R.id.temperatura);
        temperaturaView.setText(request.getTemperatura());

        if (readOnly) {
            TextInputLayout temperaturaContenedorView = getView().findViewById(R.id.temperatura_contenedor);
            temperaturaContenedorView.setBoxBackgroundColor(getResources().getColor(R.color.gray_3));

            temperaturaView.setFocusable(false);
            temperaturaView.setCursorVisible(false);
        }

        AutoCompleteTextView equipmentGradeView = getView().findViewById(R.id.equipment);
        equipmentGradeView.setText(request.getEquipmentgrade(), false);

        if (request.getIdequipmentgrade() != null && request.getEquipmentgrade() != null) {
            equipmentGradeSelected = new Spinner(
                    String.valueOf(request.getIdequipmentgrade()),
                    request.getEquipmentgrade()
            );
        }

        if (readOnly) {
            equipmentGradeView.setAdapter(null);

            TextInputLayout equipmentGradeContenedorView = getView().findViewById(R.id.equipment_contenedor);
            equipmentGradeContenedorView.setBoxBackgroundColor(getResources().getColor(R.color.gray_3));

            equipmentGradeView.setFocusable(false);
            equipmentGradeView.setCursorVisible(false);
            equipmentGradeView.setOnClickListener(null);
        }

        TextInputEditText lineaView = getView().findViewById(R.id.linea);
        lineaView.setText(request.getLineaNaviera());

        if (readOnly) {
            lineaView.setFocusable(false);
            lineaView.setCursorVisible(false);
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

        AutoCompleteTextView gradosView = getView().findViewById(R.id.grados);
        gradosView.setText(request.getGradosText(), false);

        if (request.getGrados() != null && request.getGradosText() != null) {
            degreeSelected = new Spinner(
                    request.getGrados(),
                    request.getGradosText()
            );
        }

        if (readOnly) {
            gradosView.setAdapter(null);

            TextInputLayout gradosContenedorView = getView().findViewById(R.id.grados_contenedor);
            gradosContenedorView.setBoxBackgroundColor(getResources().getColor(R.color.gray_3));

            gradosView.setFocusable(false);
            gradosView.setCursorVisible(false);
            gradosView.setOnClickListener(null);
        }

        AutoCompleteTextView refrigeranteView = getView().findViewById(R.id.refrigerante);
        refrigeranteView.setText(request.getNivelrefrigeranteText(), false);

        if (request.getNivelrefrigerante() != null && request.getNivelrefrigeranteText() != null) {
            refrigerantSelected = new Spinner(
                    request.getNivelrefrigerante(),
                    request.getNivelrefrigeranteText()
            );
        }

        if (readOnly) {
            refrigeranteView.setAdapter(null);

            TextInputLayout refrigeranteContenedorView = getView().findViewById(R.id.refrigerante_contenedor);
            refrigeranteContenedorView.setBoxBackgroundColor(getResources().getColor(R.color.gray_3));

            refrigeranteView.setFocusable(false);
            refrigeranteView.setCursorVisible(false);
            refrigeranteView.setOnClickListener(null);
        }

        AutoCompleteTextView marcaView = getView().findViewById(R.id.marca);
        marcaView.setText(request.getMarca(), false);

        if (request.getIdmarca() != null && request.getMarca() != null) {
            brandsSelected = new Spinner(
                    String.valueOf(request.getIdmarca()),
                    request.getMarca()
            );
        }

        if (readOnly) {
            marcaView.setAdapter(null);

            TextInputLayout marcaContenedorView = getView().findViewById(R.id.marca_contenedor);
            marcaContenedorView.setBoxBackgroundColor(getResources().getColor(R.color.gray_3));

            marcaView.setFocusable(false);
            marcaView.setCursorVisible(false);
            marcaView.setOnClickListener(null);
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

        AutoCompleteTextView modeloView = getView().findViewById(R.id.modelo);
        modeloView.setText(request.getModelo(), false);

        if (request.getIdmodelo() != null && request.getModelo() != null) {
            modelSelected = new Spinner(
                    String.valueOf(request.getIdmodelo()),
                    request.getModelo()
            );
        }

        if (readOnly) {
            modeloView.setAdapter(null);

            TextInputLayout modeloContenedorView = getView().findViewById(R.id.modelo_contenedor);
            modeloContenedorView.setBoxBackgroundColor(getResources().getColor(R.color.gray_3));

            modeloView.setFocusable(false);
            modeloView.setCursorVisible(false);
            modeloView.setOnClickListener(null);
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

        AutoCompleteTextView yardaView = getView().findViewById(R.id.yarda);
        yardaView.setText(request.getYardainspeccion(), false);

        if (request.getIdyardainspeccion() != null && request.getYardainspeccion() != null) {
            yardaSelected = new Spinner(
                    String.valueOf(request.getIdyardainspeccion()),
                    request.getYardainspeccion()
            );
        }

        if (readOnly) {
            yardaView.setAdapter(null);

            TextInputLayout yardaContenedorView = getView().findViewById(R.id.yarda_contenedor);
            yardaContenedorView.setBoxBackgroundColor(getResources().getColor(R.color.gray_3));

            yardaView.setFocusable(false);
            yardaView.setCursorVisible(false);
            yardaView.setOnClickListener(null);
        }

        return this;
    }
}