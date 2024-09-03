package com.mantum.cmms.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;

import android.os.Parcelable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.mantum.demo.R;
import com.mantum.cmms.activity.EntidadesListaChequeoActivity;
import com.mantum.cmms.activity.FirmaxEntidadActivity;
import com.mantum.cmms.activity.GaleriaActivity;
import com.mantum.cmms.activity.ListaPersonalActivity;
import com.mantum.cmms.activity.PersonalListaChequeoActivity;
import com.mantum.cmms.activity.RecursosActivity;
import com.mantum.cmms.activity.SolicitudServicioActivity;
import com.mantum.cmms.adapter.DiligenciarAdapter;
import com.mantum.cmms.domain.Diligenciar;
import com.mantum.cmms.domain.Spinner;
import com.mantum.cmms.entity.ClienteListaChequeo;
import com.mantum.cmms.entity.EntidadesClienteListaChequeo;
import com.mantum.cmms.entity.ListaChequeo;
import com.mantum.cmms.entity.Personal;
import com.mantum.cmms.entity.PersonalListaChequeo;
import com.mantum.cmms.entity.parameter.UserParameter;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.cmms.helper.RecursoHelper;
import com.mantum.cmms.entity.Actividad;
import com.mantum.cmms.entity.Entidad;
import com.mantum.cmms.entity.RutaTrabajo;
import com.mantum.cmms.entity.Variable;
import com.mantum.cmms.entity.VariableCualitativa;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.adapter.SpinnerAdapter;
import com.mantum.component.component.DatePicker;
import com.mantum.component.component.TimePicker;
import com.mantum.component.service.Photo;
import com.mantum.component.service.PhotoAdapter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.mantum.cmms.entity.parameter.UserParameter.DESHABILITAR_OBSERVACIONES_LISTA_CHEQUEO;

public class RutaTrabajoDiligenciarFragment extends Mantum.Fragment {

    public static final int REQUEST_ACTION = 1250;
    public static final String KEY_TAB = "Diligenciar_Ruta_Trabajo";
    public static final String IS_LC = "Listas de Chequeo";

    public static  final String OT_LC = "OT-ListaChequeo";

    public static  final String LC = "ListaChequeo";
    public static final String MODULO = "modulo";

    public static final String LC_MODULE = "Lista de chequeo";

    public static final String OT_LC_MODULE = "OT-Lista de chequeo";

    private final List<Photo> photos = new ArrayList<>();
    private RecursoHelper resource = new RecursoHelper();
    private RecyclerView recyclerView;
    private OnCompleteListener onCompleteListener;
    private DiligenciarAdapter diligenciarAdapter;
    private boolean esModoVer;
    private boolean esTurnosManuales;
    private boolean bloquearTiempos;
    private boolean validarQR;
    private boolean esListaChequeo;
    private boolean esNuevaListaChequeo;
    private boolean parcial;
    private TimePicker horaFinal;
    private String horaInicioDesdeBitacora;
    private String fechaCreacionReal;
    private boolean esMostrarMenu;
    private Long entity;
    private Long cliente;
    private String nombreCliente;
    private String tipoLc;

    private String moduloTransacion;

    private String idFirma;
    private SparseArray<PersonalListaChequeo> personas;
    private SparseArray<EntidadesClienteListaChequeo> entidades;
    private boolean ocultarFechaHoraVigente = false;
    private final SimpleDateFormat simpleDateFormat
            = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private final ArrayList<Personal> grupos = new ArrayList<>();

    private final ActivityResultLauncher<Intent> personActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data == null) {
                        return;
                    }

                    Bundle bundle = data.getExtras();
                    if (bundle == null) {
                        return;
                    }

                    SparseArray<Parcelable> entities = bundle.getSparseParcelableArray(
                            ListaPersonalActivity.KEY_ENTITY);

                    grupos.clear();
                    if (entities != null) {
                        for (int i = 0; i < entities.size(); i++) {
                            Personal person = (Personal) entities.get(i);
                            grupos.add(person);
                        }
                    }
                }
            });

    public RutaTrabajoDiligenciarFragment() {
        this.esModoVer = false;
        this.esTurnosManuales = false;
        this.bloquearTiempos = false;
        this.validarQR = false;
        this.esListaChequeo = false;
        this.esMostrarMenu = true;
        this.esNuevaListaChequeo = false;
        this.parcial = false;
        this.personas = new SparseArray<>();
        this.entidades = new SparseArray<>();
    }

    @Nullable
    @Override
    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_ruta_trabajo_diligenciar, container, false);

        diligenciarAdapter = new DiligenciarAdapter(view.getContext(), validarQR);
        if (esMostrarMenu) {
            diligenciarAdapter.setMenu(R.menu.menu_diligenciar_ruta_trabajo);
            diligenciarAdapter.setOnCall((menu, value) -> {
                Bundle bundle = new Bundle();
                bundle.putLong(SolicitudServicioActivity.KEY_ID, value.getId());
                bundle.putString(SolicitudServicioActivity.KEY_NAME, value.getCodigo() + " | " + value.getNombre());
                bundle.putString(SolicitudServicioActivity.KEY_TYPE, value.getTipo());

                Intent intent = new Intent(getActivity(), SolicitudServicioActivity.class);
                intent.putExtras(bundle);

                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, 1);
                }
                return true;
            });
        }

        recyclerView = diligenciarAdapter.startAdapter(
                view, new LinearLayoutManager(view.getContext()));

        DatePicker fechaCreacion = new DatePicker(
                view.getContext(), view.findViewById(R.id.creation_date));
        fechaCreacion.setEnabled(esListaChequeo);
        if (esModoVer) {
            fechaCreacion.setEnabled(false);
        }
        fechaCreacion.load();

        TimePicker horaInicio = new TimePicker(
                view.getContext(), view.findViewById(R.id.creation_time_start));
        horaInicio.setEnabled(!bloquearTiempos);
        if (esModoVer) {
            horaInicio.setEnabled(false);
        }

        horaInicio.load(true);
        if (horaInicioDesdeBitacora != null) {
            horaInicio.setValue(horaInicioDesdeBitacora);
        }

        horaFinal = new TimePicker(view.getContext(), view.findViewById(R.id.creation_time_end));
        horaFinal.setEnabled(!bloquearTiempos);
        if (esModoVer) {
            horaFinal.setEnabled(false);
        }
        horaFinal.load();

        DatePicker fechaFinVigente = new DatePicker(
                view.getContext(), view.findViewById(R.id.fecha_fin_vigente));
        fechaFinVigente.setEnabled(true);
        if (esModoVer) {
            fechaFinVigente.setEnabled(false);
        }
        fechaFinVigente.load();

        if (isEsNuevaListaChequeo()) {
            LinearLayout horariosView = view.findViewById(R.id.horarios);
            horariosView.setVisibility(View.GONE);
        }

        FloatingActionButton cameraFloatingActionButton = view.findViewById(R.id.camera);
        cameraFloatingActionButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSparseParcelableArray(GaleriaActivity.PATH_FILE_PARCELABLE, PhotoAdapter.factory(photos));

            Intent intent = new Intent(getActivity(), GaleriaActivity.class);
            intent.putExtras(bundle);

            if (getActivity() != null) {
                getActivity().startActivityForResult(intent, GaleriaActivity.REQUEST_ACTION);
            }
        });

        FloatingActionButton recursosFloatingActionButton = view.findViewById(R.id.recursos);
        recursosFloatingActionButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable(RecursosActivity.RECURSO, resource);

            Intent intent = new Intent(getActivity(), RecursosActivity.class);
            intent.putExtras(bundle);

            if (getActivity() != null) {
                getActivity().startActivityForResult(intent, RecursosActivity.REQUEST_ACTION);
            }
        });

        FloatingActionButton firmaFloatingActionButton = view.findViewById(R.id.firma);
        firmaFloatingActionButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            if(this.idFirma != null){
                bundle.putLong(Mantum.KEY_ID, Long.parseLong(this.idFirma));
            }else{
                bundle.putLong(Mantum.KEY_ID, getEntity());

            }
            bundle.putString(Mantum.ENTITY_TYPE, this.tipoLc);
            bundle.putString(MODULO, this.moduloTransacion);
            Intent intent = new Intent(getActivity(), FirmaxEntidadActivity.class);
            intent.putExtras(bundle);
            if (getActivity() != null) {
                getActivity().startActivityForResult(intent, FirmaxEntidadActivity.REQUEST_ACTION);
            }
        });

        if (esTurnosManuales && !isEsNuevaListaChequeo()) {
            TextInputLayout horasHabilesContenedor
                    = view.findViewById(R.id.horas_habiles_contenedor);
            horasHabilesContenedor.setVisibility(View.VISIBLE);
        }

        if (!UserPermission.check(view.getContext(), UserPermission.REALIZAR_MOVIMIENTO_RT, false)) {
            view.findViewById(R.id.recursos).setVisibility(View.GONE);
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        fechaCreacionReal = simpleDateFormat.format(new Date());

        TextInputEditText entityTextInputEditText = view.findViewById(R.id.entidad);
        entityTextInputEditText.setOnClickListener(view1 -> {
            Bundle bundle = new Bundle();
            bundle.putLong(Mantum.KEY_ID, getEntity());
            bundle.putSparseParcelableArray(EntidadesListaChequeoActivity.KEY_ENTITY, entidades);
            if (cliente != null) {
                bundle.putLong(EntidadesListaChequeoActivity.KEY_CLIENT, cliente);
            }

            Intent intent = new Intent(getActivity(), EntidadesListaChequeoActivity.class);
            intent.putExtras(bundle);

            startActivityForResult(intent, 1);
        });

        FloatingActionButton personalFloatingActionButton = view.findViewById(R.id.personal);
        personalFloatingActionButton.setVisibility(View.GONE);
        personalFloatingActionButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putLong(Mantum.KEY_ID, getEntity());
            bundle.putSparseParcelableArray(PersonalListaChequeoActivity.KEY_PERSONAL, personas);

            Intent intent = new Intent(getActivity(), PersonalListaChequeoActivity.class);
            intent.putExtras(bundle);

            startActivityForResult(intent, 2);
        });

        if (esNuevaListaChequeo) {
            cameraFloatingActionButton.setVisibility(View.GONE);
            firmaFloatingActionButton.setVisibility(View.GONE);
        }

        if (!esListaChequeo && !esNuevaListaChequeo) {
            FloatingActionButton floatingActionPersona = view.findViewById(R.id.personas);
            if (UserPermission.check(view.getContext(), UserPermission.AGREGAR_PERSONAL_BITACTORA, false)) {
                floatingActionPersona.setVisibility(View.VISIBLE);
                floatingActionPersona.setOnClickListener(v -> {

                    SparseArray<Personal> results = new SparseArray<>();
                    for (int i = 0; i < grupos.size(); i++) {
                        results.append(i, grupos.get(i));
                    }

                    Bundle bundle69 = new Bundle();
                    bundle69.putSparseParcelableArray(
                            ListaPersonalActivity.KEY_ENTITY, results);

                    Intent intent = new Intent(getActivity(), ListaPersonalActivity.class);
                    intent.putExtras(bundle69);

                    personActivityResultLauncher.launch(intent);
                });
            }
        }

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {

            if (requestCode == 1) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    ocultarFechaHoraVigente = false;
                    entidades = bundle.getSparseParcelableArray(EntidadesListaChequeoActivity.KEY_ENTITY);

                    if (entidades != null && getView() != null) {
                        TextInputEditText inputEditText = getView().findViewById(R.id.entidad);
                        inputEditText.setText("");

                        for (int i = 0; i < entidades.size(); i++) {
                            EntidadesClienteListaChequeo value = entidades.get(i);
                            if (value.isSeleccionado()) {
                                if (inputEditText.getText() != null && inputEditText.getText().toString().isEmpty()) {
                                    inputEditText.setText(value.getNombre());
                                } else {
                                    inputEditText.setText(
                                            String.format("%s, %s", inputEditText.getText().toString(), value.getNombre())
                                    );
                                }

                                if (value.isPrerrequisito()) {
                                    ocultarFechaHoraVigente = true;
                                }
                            }
                        }
                    }

                    if (getView() != null) {
                        LinearLayout horariosView = getView().findViewById(R.id.horarios);
                        horariosView.setVisibility(ocultarFechaHoraVigente ? View.GONE : View.VISIBLE);

                        LinearLayout fechaHoraVigente = getView().findViewById(R.id.fechas_hora_vigente);
                        fechaHoraVigente.setVisibility(ocultarFechaHoraVigente ? View.GONE : View.VISIBLE);

                        if (ocultarFechaHoraVigente) {
                            TextInputEditText creationTimeStartView = getView().findViewById(R.id.creation_time_start);
                            if (creationTimeStartView != null) {
                                creationTimeStartView.setText("");
                            }

                            TextInputEditText creationTimeEndView = getView().findViewById(R.id.creation_time_end);
                            if (creationTimeEndView != null) {
                                creationTimeEndView.setText("");
                            }

                            TextInputEditText fechaFinVigenteTextView = getView().findViewById(R.id.fecha_fin_vigente);
                            if (fechaFinVigenteTextView != null) {
                                fechaFinVigenteTextView.setText("");
                            }
                        }
                    }
                }
            }

            if (requestCode == 2) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    personas = bundle.getSparseParcelableArray(PersonalListaChequeoActivity.KEY_PERSONAL);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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

    public void addResoures(RecursoHelper resource) {
        if (this.resource != null && resource != null) {
            this.resource = resource;
        }
    }

    public void setHoraInicioDesdeBitacora(String horaInicioDesdeBitacora) {
        this.horaInicioDesdeBitacora = horaInicioDesdeBitacora;
    }

    public void addPhoto(String file) {
        if (file != null && getView() != null) {
            photos.add(new Photo(getView().getContext(), new File(file)));
        }
    }

    public void clearPhoto() {
        photos.clear();
    }

    public void addPhoto(Photo photo) {
        photos.add(photo);
    }

    public void ejecutarActividad(int position) {
        int total = diligenciarAdapter.getItemCount();
        for (int i = 0; i < total; i++) {
            RecyclerView.LayoutManager layoutManager
                    = recyclerView.getLayoutManager();
            if (layoutManager == null) {
                break;
            }

            View view = layoutManager.findViewByPosition(i);
            if (view == null) {
                break;
            }

            RecyclerView actividades = view.findViewById(R.id.recycler_view);
            Entidad entidad = diligenciarAdapter.getOriginal().get(i);

            int totalActividades = entidad.getActividades().size();
            for (int k = 0; k < totalActividades; k++) {
                if (k == position) {
                    RecyclerView.LayoutManager actividadLayoutManager = actividades.getLayoutManager();
                    if (actividadLayoutManager == null) {
                        break;
                    }

                    View container = actividadLayoutManager.findViewByPosition(k);
                    if (container == null) {
                        break;
                    }

                    SwitchCompat switchCompat = container.findViewById(R.id.ejecutar);
                    switchCompat.setChecked(true);
                    FrameLayout frameLayout = container.findViewById(R.id.varContainer);
                    frameLayout.setVisibility(View.VISIBLE);
                    break;
                }
            }
        }
    }

    public void onStart(@NonNull ListaChequeo listaChequeo, @Nullable Diligenciar diligenciar) {
        if (getView() == null) {
            return;
        }
        this.tipoLc = LC;
        this.moduloTransacion = LC_MODULE;
        this.idFirma = listaChequeo.getIdFirma();
        TextView id = getView().findViewById(R.id.id);
        id.setText(String.valueOf(listaChequeo.getId()));

        TextView code = getView().findViewById(R.id.codigo);
        code.setText(listaChequeo.getCodigo());

        TextView creationDate = getView().findViewById(R.id.creation_date);
        try {
            creationDate.setText(simpleDateFormat.format(new Date()));
        } catch (Exception ignored) {
        }

        if (esModoVer) {
            diligenciarAdapter.activarModoVer();
        }

        FloatingActionButton firmaFloatingActionButton = getView().findViewById(R.id.firma);
        firmaFloatingActionButton.setVisibility(View.GONE);

        FloatingActionButton recursosFloatingActionButton = getView().findViewById(R.id.recursos);
        recursosFloatingActionButton.setVisibility(View.GONE);

        List<Spinner> values = new ArrayList<>();
        values.add(new Spinner(null, getString(R.string.seleccione_cliente)));
        for (ClienteListaChequeo value : listaChequeo.getClientes()) {
            values.add(new Spinner(value.getId().toString(), value.getNombre()));
        }

        SpinnerAdapter<Spinner> spinnerAdapter = new SpinnerAdapter<>(
                getView().getContext(), values);

        AppCompatSpinner clientAppCompatSpinner = getView().findViewById(R.id.cliente);
        clientAppCompatSpinner.setAdapter(spinnerAdapter.getAdapter());
        clientAppCompatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                Spinner current = values.get(position);
                if (current != null && current.getKey() != null) {
                    if (!current.getKey().isEmpty()) {
                        long before = Long.parseLong(current.getKey());
                        if (cliente == null || before != cliente) {
                            cliente = before;
                            nombreCliente = current.getValue();
                            entidades.clear();
                        }
                    }
                } else {
                    cliente = null;
                    nombreCliente = null;
                    entidades.clear();
                }

                if (getView() != null) {
                    TextInputEditText entityTextInputEditText = getView().findViewById(R.id.entidad);
                    entityTextInputEditText.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        clientAppCompatSpinner.setVisibility(View.GONE);
        if (UserParameter.getModuloClienteCMMS(getView().getContext())) {
            clientAppCompatSpinner.setVisibility(View.VISIBLE);
        }

        TextInputLayout entityTextInputLayout = getView().findViewById(R.id.entidad_contenedor);
        entityTextInputLayout.setVisibility(View.VISIBLE);

        FloatingActionButton personalFloatingActionButton = getView().findViewById(R.id.personal);
        personalFloatingActionButton.setVisibility(View.VISIBLE);

        LinearLayout horariosView = getView().findViewById(R.id.horarios);
        if (horariosView != null) {
            horariosView.setVisibility(ocultarFechaHoraVigente ? View.GONE : View.VISIBLE);
        }

        LinearLayout fechaHoraVigenteView = getView().findViewById(R.id.fechas_hora_vigente);
        if (fechaHoraVigenteView != null) {
            fechaHoraVigenteView.setVisibility(View.GONE);
        }

        diligenciarAdapter.setDiligenciar(diligenciar);
        if (diligenciar == null) {
            Collections.sort(listaChequeo.getEntidades(), EntidadesListaFragment::compare);
            diligenciarAdapter.addAll(listaChequeo.getEntidades(), true);
        }

        if (diligenciar != null) {
            onLoad(diligenciar);
        }

        if (esNuevaListaChequeo) {
            FloatingActionButton cameraFloatingActionButton = getView().findViewById(R.id.camera);
            cameraFloatingActionButton.setVisibility(View.GONE);
            firmaFloatingActionButton.setVisibility(View.VISIBLE);
        }
    }

    public void onStart(@NonNull RutaTrabajo rutaTrabajo, @Nullable Diligenciar diligenciar) {
        if (getView() == null) {
            return;
        }
        this.tipoLc = OT_LC;
        this.moduloTransacion = OT_LC_MODULE;
        this.idFirma = rutaTrabajo.getIdFirma();
        TextView id = getView().findViewById(R.id.id);
        id.setText(String.valueOf(rutaTrabajo.getId()));

        TextView idejecucion = getView().findViewById(R.id.idejecucion);
        idejecucion.setText(String.valueOf(rutaTrabajo.getIdejecucion()));

        TextView code = getView().findViewById(R.id.codigo);
        code.setText(rutaTrabajo.getCodigo());

        TextView creationDate = getView().findViewById(R.id.creation_date);
        try {
            Date date = simpleDateFormat.parse(rutaTrabajo.getFecha());
            if (date != null) {
                String fecha = simpleDateFormat.format(date);
                creationDate.setText(fecha);
            }
        } catch (Exception ignored) {
        }

        LinearLayout horariosView = getView().findViewById(R.id.horarios);
        if (horariosView != null) {
            horariosView.setVisibility(View.VISIBLE);
        }
        FloatingActionButton firmaFloatingActionButton = getView().findViewById(R.id.firma);
        firmaFloatingActionButton.setVisibility(View.VISIBLE);

        if (!parcial) {
            SwitchCompat parcial = getView().findViewById(R.id.parcial);
            parcial.setVisibility(View.GONE);
        }

        if (esModoVer) {
            diligenciarAdapter.activarModoVer();
        }

        if (esListaChequeo && Boolean.parseBoolean(
                UserParameter.getValue(getView().getContext(), DESHABILITAR_OBSERVACIONES_LISTA_CHEQUEO))) {
            diligenciarAdapter.ocultarObservacion();
        }

        diligenciarAdapter.setDiligenciar(diligenciar);
        if (diligenciar == null) {
            Collections.sort(rutaTrabajo.getEntidades(), EntidadesListaFragment::compare);
            diligenciarAdapter.addAll(rutaTrabajo.getEntidades(), true);
        }

        if (diligenciar != null) {
            onLoad(diligenciar);
        }

        if (esNuevaListaChequeo || IS_LC.equals(rutaTrabajo.getTipogrupo())){
            FloatingActionButton cameraFloatingActionButton = getView().findViewById(R.id.camera);
            cameraFloatingActionButton.setVisibility(View.VISIBLE);
        }
    }

    private void onLoad(@NonNull Diligenciar value) {
        if (getView() == null) {
            return;
        }

        diligenciarAdapter.addAll(value.getEntidades());

        FloatingActionsMenu menu = getView().findViewById(R.id.parent);
        menu.setVisibility(esModoVer ? View.GONE : View.VISIBLE);

        TextInputEditText token = getView().findViewById(R.id.token);
        token.setVisibility(esModoVer ? View.VISIBLE : View.GONE);
        token.setText(value.getToken());

        TextView code = getView().findViewById(R.id.codigo);
        code.setText(value.getCode());

        TextView creationDate = getView().findViewById(R.id.creation_date);
        creationDate.setText(value.getDate());

        TextView creationStartTime = getView().findViewById(R.id.creation_time_start);
        creationStartTime.setText(value.getTimestart());

        TextView creationEndTime = getView().findViewById(R.id.creation_time_end);
        creationEndTime.setText(value.getTimeend());

        TextView description = getView().findViewById(R.id.description);
        if (description != null) {
            if (esModoVer) {
                description.setFocusable(false);
                description.setCursorVisible(false);
            }
            description.setText(value.getDescription());
        }

        if (!esModoVer) {
            for (Photo photo : value.getFiles()) {
                addPhoto(photo);
            }
            resource = RecursoHelper.recursoAdapter(value.getRecursos());
        }

        if (value.isListachequeo() && esModoVer) {
            LinearLayout horariosView = getView().findViewById(R.id.horarios);
            if (horariosView != null) {
                horariosView.setVisibility(ocultarFechaHoraVigente ? View.GONE : View.VISIBLE);
            }

            LinearLayout fechaHoraVigenteView = getView().findViewById(R.id.fechas_hora_vigente);
            if (fechaHoraVigenteView != null) {
                fechaHoraVigenteView.setVisibility(value.isOcultarFechaHoraVigente() ? View.GONE : View.VISIBLE);
            }

            // ---

            TextInputEditText entidadInputEditText = getView().findViewById(R.id.entidad);
            entidadInputEditText.setVisibility(View.GONE);

            TextInputLayout entidadViewTextInputLayout = getView().findViewById(R.id.entidad_contenedor_view);
            entidadViewTextInputLayout.setVisibility(View.VISIBLE);

            TextInputEditText entidadTextInputEditText = getView().findViewById(R.id.entidad_view);
            entidadTextInputEditText.setText("");
            if (value.getEntidadesClienteListaChequeos() != null) {
                for (EntidadesClienteListaChequeo entidadesClienteListaChequeo : value.getEntidadesClienteListaChequeos()) {
                    if (entidadesClienteListaChequeo.isSeleccionado()) {
                        if (entidadTextInputEditText.getText() != null && entidadTextInputEditText.getText().toString().isEmpty()) {
                            entidadTextInputEditText.setText(entidadesClienteListaChequeo.getNombre());
                        } else {
                            entidadTextInputEditText.setText(
                                    String.format("%s, %s", entidadTextInputEditText.getText().toString(), entidadesClienteListaChequeo.getNombre())
                            );
                        }
                    }
                }
            }

            // ---

            AppCompatSpinner clientAppCompatSpinner = getView().findViewById(R.id.cliente);
            clientAppCompatSpinner.setVisibility(View.GONE);

            TextInputLayout clientTextInputLayout = getView().findViewById(R.id.cliente_contenedor_view);
            clientTextInputLayout.setVisibility(View.VISIBLE);

            TextInputEditText clientTextInputEditText = getView().findViewById(R.id.cliente_view);
            clientTextInputEditText.setText(value.getCliente());
        }
    }

    @Nullable
    public Diligenciar getValue() {
        if (getView() == null) {
            return null;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String fechaFinReal = simpleDateFormat.format(new Date());

        SimpleDateFormat simpleHourFormat = new SimpleDateFormat(
                "HH:mm", Locale.getDefault());
        String horaFinReal = simpleHourFormat.format(new Date());

        Diligenciar diligenciar = new Diligenciar();
        diligenciar.setFiles(photos);
        diligenciar.setIdFirma(this.idFirma);

        TextView codigo = getView().findViewById(R.id.codigo);
        diligenciar.setCode(codigo.getText().toString());

        TextView creationDate = getView().findViewById(R.id.creation_date);
        diligenciar.setDate(creationDate.getText().toString());

        diligenciar.setDateEndVig(null);
        TextInputEditText fechaFinVigenteView = getView().findViewById(R.id.fecha_fin_vigente);
        if (fechaFinVigenteView.getText() != null) {
            diligenciar.setDateEndVig(fechaFinVigenteView.getText().toString());
        }

        TextView creationTime = getView().findViewById(R.id.creation_time_start);
        diligenciar.setTimestart(creationTime.getText().toString());

        TextView creationTimeEnd = getView().findViewById(R.id.creation_time_end);
        if (!isEsNuevaListaChequeo()) {
            diligenciar.setTimeend(creationTimeEnd.getText().toString().isEmpty()
                    ? horaFinReal
                    : creationTimeEnd.getText().toString());
        } else {
            diligenciar.setTimeend(creationTimeEnd.getText().toString());
        }

        TextView description = getView().findViewById(R.id.description);
        diligenciar.setDescription(description.getText().toString());

        SwitchCompat parcial = getView().findViewById(R.id.parcial);
        diligenciar.setParcial(parcial.isChecked());

        diligenciar.setFechaCreacionReal(fechaCreacionReal);
        diligenciar.setFechaFinReal(fechaFinReal);

        TextInputEditText horasHabiles = getView().findViewById(R.id.horas_habiles);
        if (horasHabiles != null && horasHabiles.getText() != null) {
            String valueEvento = horasHabiles.getText().toString();
            diligenciar.setHorasHabilesDia(valueEvento.isEmpty()
                    ? null
                    : Float.parseFloat(valueEvento));
        }

        diligenciar.setRecursos(resource.getRecursos());
        diligenciar.setEntidades(diligenciarAdapter.getOriginal());

        int total = diligenciarAdapter.getItemCount();
        for (int i = 0; i < total; i++) {
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager == null) {
                break;
            }

            View view = layoutManager.findViewByPosition(i);
            if (view == null) {
                break;
            }

            RecyclerView actividades = view.findViewById(R.id.recycler_view);
            Entidad entidad = diligenciarAdapter.getOriginal().get(i);

            int totalActividades = entidad.getActividades().size();
            for (int k = 0; k < totalActividades; k++) {
                RecyclerView.LayoutManager actividadesLayoutManager = actividades.getLayoutManager();
                if (actividadesLayoutManager == null) {
                    break;
                }

                View container = actividadesLayoutManager.findViewByPosition(k);
                if (container == null) {
                    break;
                }

                SwitchCompat switchCompat = container.findViewById(R.id.ejecutar);
                if (switchCompat.isChecked()) {

                    TextView idActividad = container.findViewById(R.id.idactividad);
                    diligenciar.addAM(Long.valueOf(idActividad.getText().toString()));

                    TextInputEditText observacion = container.findViewById(R.id.observacion);
                    if (observacion != null && observacion.getText() != null) {
                        diligenciar.setObservaciones(observacion.getText().toString());
                    }

                    RecyclerView variables = container.findViewById(R.id.recycler_view);
                    Actividad actividad = entidad.getActividades().get(k);
                    int totalVariables = actividad != null ? actividad.getVariables().size() : 0;
                    for (int j = 0; j < totalVariables; j++) {
                        RecyclerView.LayoutManager variablesLayoutManager = variables.getLayoutManager();
                        if (variablesLayoutManager == null) {
                            break;
                        }

                        View recyclerView = variablesLayoutManager.findViewByPosition(j);
                        if (recyclerView == null) {
                            break;
                        }

                        Variable variable = new Variable();
                        variable.setIdentidad(entidad.getId());
                        variable.setEntidad(entidad.getNombre());
                        variable.setTipoentidad(entidad.getTipo());
                        variable.setIdActividad(actividad.getId());

                        TextView idVariable = recyclerView.findViewById(R.id.idvariable);
                        variable.setId(Long.valueOf(idVariable.getText().toString()));

                        TextView tipo = recyclerView.findViewById(R.id.tipo);
                        variable.setTipo(tipo.getText().toString());

                        TextInputEditText observacionVariable = recyclerView.findViewById(R.id.observacion);
                        if (observacionVariable != null && observacionVariable.getText() != null) {
                            variable.setObservacion(observacionVariable.getText().toString());
                        }

                        TextView value = recyclerView.findViewById(R.id.valor);
                        String valor = value.getText().toString();
                        if (VariableCualitativa.NAME.equals(variable.getTipo())) {
                            AppCompatSpinner rango = recyclerView.findViewById(R.id.rango);
                            valor = ((VariableCualitativa) rango.getSelectedItem()).getValor();
                        }

                        variable.setValor(valor);
                        if (variable.getValor() != null && !variable.getValor().isEmpty()
                                && !valor.equals(this.getString(R.string.seleccione_opcion))) {
                            diligenciar.addVariable(variable);
                        }
                    }

                    FrameLayout frameLayout = container.findViewById(R.id.varContainer);
                    frameLayout.setVisibility(View.VISIBLE);
                }
            }
        }

        List<PersonalListaChequeo> personalList = new ArrayList<>();
        if (personas != null) {
            for (int i = 0; i < personas.size(); i++) {
                PersonalListaChequeo value = personas.get(i);
                if (value.isSeleccionado()) {
                    personalList.add(value);
                }
            }
        }
        diligenciar.setPersonas(personalList);

        List<EntidadesClienteListaChequeo> entidadesList = new ArrayList<>();
        if (entidades != null) {
            for (int i = 0; i < entidades.size(); i++) {
                EntidadesClienteListaChequeo value = entidades.get(i);
                if (value.isSeleccionado()) {
                    entidadesList.add(value);
                }
            }
        }
        diligenciar.setEntidadesClienteListaChequeos(entidadesList);

        diligenciar.setIdlc(null);
        diligenciar.setListachequeo(false);
        if (isEsNuevaListaChequeo()) {
            diligenciar.setIdlc(getEntity());
            diligenciar.setListachequeo(true);
        }

        diligenciar.setIdCliente(cliente);
        diligenciar.setCliente(nombreCliente);
        diligenciar.setOcultarFechaHoraVigente(ocultarFechaHoraVigente);

        // Se incluye la hora actual cuando el usuario no tiene habilitado el selector de horario
        if (diligenciar.isOcultarFechaHoraVigente()) {
            diligenciar.setTimestart(horaFinReal);
            diligenciar.setTimeend(horaFinReal);
        }

        diligenciar.setGrupos(grupos);
        return diligenciar;
    }

    public RutaTrabajoDiligenciarFragment activarModoVer() {
        this.esModoVer = true;
        return this;
    }

    public void listaChequeo() {
        this.esListaChequeo = true;
    }

    public void setTurnosManuales(boolean esTurnosManuales) {
        this.esTurnosManuales = esTurnosManuales;
    }

    public void incluirHoraFinal() {
        if (horaFinal == null) {
            return;
        }
        horaFinal.setValue();
    }

    public void incluirValidacionQR() {
        this.validarQR = true;
    }

    public void bloquearTiempos() {
        this.bloquearTiempos = true;
    }

    public void setParcial() {
        this.parcial = true;
    }

    public RutaTrabajoDiligenciarFragment ocultarMenu() {
        this.esMostrarMenu = false;
        return this;
    }

    public Long getEntity() {
        return entity;
    }

    public void setEntity(Long entity) {
        this.entity = entity;
    }

    public boolean isEsNuevaListaChequeo() {
        return esNuevaListaChequeo;
    }

    public void setEsNuevaListaChequeo(boolean esNuevaListaChequeo) {
        this.esNuevaListaChequeo = esNuevaListaChequeo;
    }
}