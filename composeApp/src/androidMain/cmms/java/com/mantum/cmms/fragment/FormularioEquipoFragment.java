package com.mantum.cmms.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mantum.R;
import com.mantum.cmms.activity.BusquedaVariablesEquipoActivity;
import com.mantum.cmms.activity.FormularioEquipoActivity;
import com.mantum.cmms.adapter.CentroCostoAdapter;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Activo;
import com.mantum.cmms.entity.CategoriaEquipo;
import com.mantum.cmms.entity.CentroCostoEquipo;
import com.mantum.cmms.entity.Ciudad;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Departamento;
import com.mantum.cmms.entity.Equipo;
import com.mantum.cmms.entity.EstadoActualEquipo;
import com.mantum.cmms.entity.EstadoTransferenciaEquipo;
import com.mantum.cmms.entity.Familia;
import com.mantum.cmms.entity.InstalacionLocativa;
import com.mantum.cmms.entity.InstalacionProceso;
import com.mantum.cmms.entity.MarcaEquipo;
import com.mantum.cmms.entity.MedidasEquipo;
import com.mantum.cmms.entity.Pais;
import com.mantum.cmms.entity.Responsable;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.entity.UbicacionPredeterminada;
import com.mantum.cmms.entity.parameter.UserParameter;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.cmms.helper.TransaccionHelper;
import com.mantum.cmms.service.ActivoService;
import com.mantum.cmms.service.TransaccionService;
import com.mantum.component.Mantum;
import com.mantum.component.OnValueChange;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

import static com.mantum.cmms.activity.FormularioEquipoActivity.UUID_TRANSACCION;
import static com.mantum.cmms.security.Security.TAG;

public class FormularioEquipoFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Equipo";

    //General
    private EditText editTextCodigo;

    private AutoCompleteTextView editTextNombre;

    private EditText editTextFamilia;

    private EditText editTextInstalacionProceso;

    private EditText editTextInstalacionLocativa;

    private Spinner spinnerProvocaParo;

    private EditText editTextUbicacion;

    private EditText editTextPais;

    private EditText editTextDepartamento;

    private EditText editTextCiudad;

    private EditText editTextObservaciones;

    //Información técnica
    private EditText editTextSerie;

    private EditText editTextModelo;

    private EditText editTextColor;

    private EditText editTextMarca;

    private EditText editTextLargo;

    private Spinner spinnerMedidaLargo;

    private EditText editTextAncho;

    private Spinner spinnerMedidaAncho;

    private EditText editTextAlto;

    private Spinner spinnerMedidaAlto;

    private EditText editTextPeso;

    private Spinner spinnerMedidaPeso;

    //Personal
    private EditText editTextResponsable;

    private Spinner spinnerEstadoEquipo;

    //Información histórica
    private Spinner spinnerEstadoActualEquipo;

    //Información financiera
    private EditText editTextCodigoContable;

    private Spinner spinnerCategoriaEquipo;

    private RecyclerView recyclerViewCentroCosto;

    //Listados
    private final String[] provocaParo = {"Provoca paro", "Sí", "No"};
    private final List<String> paises = new ArrayList<>();
    private final List<String> departamentos = new ArrayList<>();
    private final List<String> ciudades = new ArrayList<>();
    private final List<String> medidasDimension = new ArrayList<>();
    private final List<String> medidasPeso = new ArrayList<>();
    private final List<String> categorias = new ArrayList<>();
    private final List<String> estados = new ArrayList<>();
    private final List<String> estadosActual = new ArrayList<>();

    //Adaptadores
    private CentroCostoAdapter<CentroCostoEquipo> centroCostoAdapter;
    private ArrayAdapter<String> adapterEstados;
    private ArrayAdapter<String> adapterEstadosActual;
    private ArrayAdapter<String> adapterDimensiones;
    private ArrayAdapter<String> adapterPesos;
    private ArrayAdapter<String> adapterCategorias;
    private ArrayAdapter<String> adapterNombres;


    //Ids
    private Long id, idFamilia, idInstalacionProceso, idInstalacionLocativa, idMarca, idResponsable;

    private boolean success;

    private ProgressDialog progressDialog;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private TransaccionHelper.Dialog dialogHelper;

    private ActivoService activoService;

    private TransaccionService transaccionService;

    private boolean crearEquipo = true;

    private String uuidTransaccion = null;

    String MODE_EDIT = null;

    private Database database;

    private Cuenta cuenta;

    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            super.onCreateView(inflater, container, savedInstanceState);
            view = inflater.inflate(R.layout.fragment_formulario_equipo, container, false);
            dialogHelper = new TransaccionHelper.Dialog(view.getContext());

            database = new Database(view.getContext());
            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            startProgressDialog();

            activoService = new ActivoService(view.getContext(), cuenta);
            transaccionService = new TransaccionService(view.getContext());

            compositeDisposable.add(activoService.getAllVariables()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(responseBodyGet -> activoService.saveAllVariables(responseBodyGet), this::onError, this::onComplete));

            if(UserParameter.getValue(view.getContext(), UserParameter.UBICACION_PARA_EQUIPO_REQUERIDA) != null){
                TextInputLayout ubicacionEquipoContainer = view.findViewById(R.id.ubicacion_equipo_container);
                ubicacionEquipoContainer.setVisibility(View.VISIBLE);
            }

            if (UserParameter.getValue(view.getContext(), UserParameter.CAMPOS_ADICIONALES_CREAR_EQUIPO) != null) {
                TextInputLayout ubicacionEquipoContainer = view.findViewById(R.id.ubicacion_equipo_container);
                ubicacionEquipoContainer.setVisibility(View.VISIBLE);

                TextInputLayout paisEquipoContainer = view.findViewById(R.id.pais_equipo_container);
                paisEquipoContainer.setVisibility(View.VISIBLE);

                TextInputLayout departamentoEquipoContainer = view.findViewById(R.id.departamento_equipo_container);
                departamentoEquipoContainer.setVisibility(View.VISIBLE);

                TextInputLayout ciudadEquipoContainer = view.findViewById(R.id.ciudad_equipo_container);
                ciudadEquipoContainer.setVisibility(View.VISIBLE);

                LinearLayout contenedorResponsable = view.findViewById(R.id.contenedor_responsable);
                contenedorResponsable.setVisibility(View.VISIBLE);

                LinearLayout contenedorContable = view.findViewById(R.id.contenedor_contable);
                contenedorContable.setVisibility(View.VISIBLE);
            }

            //Campos de texto editables
            editTextCodigo = view.findViewById(R.id.codigo_equipo);
            editTextUbicacion = view.findViewById(R.id.ubicacion_equipo);
            editTextObservaciones = view.findViewById(R.id.observaciones_equipo);
            editTextSerie = view.findViewById(R.id.serie_equipo);
            editTextModelo = view.findViewById(R.id.modelo_equipo);
            editTextColor = view.findViewById(R.id.color_equipo);
            editTextLargo = view.findViewById(R.id.largo_equipo);
            editTextAncho = view.findViewById(R.id.ancho_equipo);
            editTextAlto = view.findViewById(R.id.alto_equipo);
            editTextPeso = view.findViewById(R.id.peso_equipo);
            editTextCodigoContable = view.findViewById(R.id.codigo_contable_equipo);

            // Autocompletables
            editTextNombre = view.findViewById(R.id.nombre_equipo);
            editTextNombre.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (editTextNombre.getText().toString().length() > 2) {
                        compositeDisposable.add(activoService.getNombresEquipo(editTextNombre.getText().toString(), idFamilia)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(strings -> {
                                    adapterNombres = new ArrayAdapter<>(view.getContext(), R.layout.custom_simple_spinner, R.id.item);
                                    adapterNombres.addAll(strings);
                                    editTextNombre.setAdapter(adapterNombres);
                                    editTextNombre.showDropDown();
                                }, throwable -> Log.e(TAG, "searchEquipoNombre: ", throwable), () -> {
                                }));
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            // Spinners y adaptadores
            ArrayAdapter<String> adapterProvocaParo = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, provocaParo) {
                @Override
                public boolean isEnabled(int position) {
                    return position > 0;
                }

                @Override
                public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    ((TextView) view).setTextColor(position == 0 ? Color.GRAY : Color.BLACK);
                    return view;
                }
            };

            spinnerProvocaParo = view.findViewById(R.id.provoca_paro);
            spinnerProvocaParo.setAdapter(adapterProvocaParo);
            spinnerProvocaParo.setSelection(2);

            // Diálogos de selección
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

            editTextPais = view.findViewById(R.id.pais_equipo);
            editTextPais.setOnClickListener(view -> {
                builder.setCancelable(true);
                builder.setNegativeButton("Cancelar", (dialogInterface, i) -> dialogInterface.cancel());
                builder.setItems(paises.toArray(new String[0]), (dialogInterface, i) -> {
                    editTextPais.setText(paises.get(i));

                    Pais pais = database.where(Pais.class)
                            .equalTo("nombre", paises.get(i))
                            .findFirst();

                    departamentos.clear();
                    editTextDepartamento.setText("");
                    editTextCiudad.setText("");
                    if (pais != null) {
                        List<Departamento> listDepartamento = database.where(Departamento.class)
                                .equalTo("idpais", pais.getId())
                                .findAll()
                                .sort("nombre");

                        for (Departamento departamento : listDepartamento) {
                            departamentos.add(departamento.getNombre());
                        }
                    }

                    dialogInterface.dismiss();
                });
                builder.show();
            });

            editTextDepartamento = view.findViewById(R.id.departamento_equipo);
            editTextDepartamento.setOnClickListener(view -> {
                builder.setCancelable(true);
                builder.setNegativeButton("Cancelar", (dialogInterface, i) -> dialogInterface.cancel());
                builder.setItems(departamentos.toArray(new String[0]), (dialogInterface, i) -> {
                    editTextDepartamento.setText(departamentos.get(i));

                    Departamento departamento = database.where(Departamento.class)
                            .equalTo("nombre", departamentos.get(i))
                            .findFirst();

                    ciudades.clear();
                    editTextCiudad.setText("");
                    if (departamento != null) {
                        List<Ciudad> listCiudad = database.where(Ciudad.class)
                                .equalTo("iddepartamento", departamento.getId())
                                .findAll()
                                .sort("nombre");

                        for (Ciudad ciudad : listCiudad) {
                            ciudades.add(ciudad.getNombre());
                        }
                    }

                    dialogInterface.dismiss();
                });
                builder.show();
            });

            editTextCiudad = view.findViewById(R.id.ciudad_equipo);
            editTextCiudad.setOnClickListener(view -> {
                builder.setCancelable(true);
                builder.setNegativeButton("Cancelar", (dialogInterface, i) -> dialogInterface.cancel());
                builder.setItems(ciudades.toArray(new String[0]), (dialogInterface, i) -> {
                    editTextCiudad.setText(ciudades.get(i));
                    dialogInterface.dismiss();
                });
                builder.show();
            });

            // Búsquedas
            editTextFamilia = view.findViewById(R.id.familia_equipo);
            editTextFamilia.setOnClickListener(view -> loadBusquedaVariables("Familia"));

            editTextInstalacionProceso = view.findViewById(R.id.instalacion_proceso_equipo);
            editTextInstalacionProceso.setOnClickListener(view -> loadBusquedaVariables("InstalacionProceso"));

            editTextInstalacionLocativa = view.findViewById(R.id.instalacion_locativa_equipo);
            editTextInstalacionLocativa.setOnClickListener(view -> loadBusquedaVariables("InstalacionLocativa"));

            editTextMarca = view.findViewById(R.id.marca_equipo);
            editTextMarca.setOnClickListener(view -> loadBusquedaVariables("Fabricante"));

            editTextResponsable = view.findViewById(R.id.responsable_equipo);
            editTextResponsable.setOnClickListener(view -> loadBusquedaVariables("Personal"));

            LinearLayout btnBuscarCentroCosto = view.findViewById(R.id.buscar_centro_costo_equipo);
            btnBuscarCentroCosto.setOnClickListener(view -> loadBusquedaVariables("CentroCosto"));

            // Limpiar campos
            ImageView btnRemoverFamilia = view.findViewById(R.id.remover_familia_equipo);
            btnRemoverFamilia.setOnClickListener(view -> {
                idFamilia = null;
                editTextFamilia.setText("");
            });

            ImageView btnRemoverInstalacionLocativa = view.findViewById(R.id.remover_instalacion_locativa_equipo);
            btnRemoverInstalacionLocativa.setOnClickListener(view -> {
                idInstalacionLocativa = null;
                editTextInstalacionLocativa.setText("");
            });

            ImageView btnRemoverMarca = view.findViewById(R.id.remover_marca_equipo);
            btnRemoverMarca.setOnClickListener(view -> {
                idMarca = null;
                editTextMarca.setText("");
            });

            ImageView btnRemoverResponsable = view.findViewById(R.id.remover_responsable_equipo);
            btnRemoverResponsable.setOnClickListener(view -> {
                idResponsable = null;
                editTextResponsable.setText("");
            });

            // Lógica listado centros de costo
            centroCostoAdapter = new CentroCostoAdapter<>(view.getContext());
            recyclerViewCentroCosto = view.findViewById(R.id.recycler_view_centro_costo);
            recyclerViewCentroCosto.setLayoutManager(new LinearLayoutManager(view.getContext()));
            recyclerViewCentroCosto.setAdapter(centroCostoAdapter);

            centroCostoAdapter.setOnAction(new OnValueChange<CentroCostoEquipo>() {
                @Override
                public void onClick(CentroCostoEquipo value, int position) {
                    centroCostoAdapter.remove(position);
                    centroCostoAdapter.refresh();
                }

                @Override
                public boolean onChange(CentroCostoEquipo value, TextView position) {
                    return false;
                }

                @Override
                public void onTextChange(Float value, int position) {

                }
            });

            return view;
        } catch (Exception e) {
            Log.e(TAG, "onCreateView: ", e);
            return null;
        }
    }

    private void loadBusquedaVariables(String tipoEntidad) {
        Intent intent = new Intent(getActivity(), BusquedaVariablesEquipoActivity.class);
        intent.putExtra("tipoEntidad", tipoEntidad);
        startActivityForResult(intent, 1);
    }

    public boolean enviarEquipo() {
        try {
            TextInputLayout nombreEquipoContainer = view.findViewById(R.id.nombre_equipo_container);
            TextInputLayout familiaEquipoContainer = view.findViewById(R.id.familia_equipo_container);
            TextInputLayout instalacionProcesoEquipoContainer = view.findViewById(R.id.instalacion_proceso_equipo_container);
            TextInputLayout ubicacionEquipoContainer = view.findViewById(R.id.ubicacion_equipo_container);
            TextInputLayout paisEquipoContainer = view.findViewById(R.id.pais_equipo_container);
            TextInputLayout departamentoEquipoContainer = view.findViewById(R.id.departamento_equipo_container);
            TextInputLayout ciudadEquipoContainer = view.findViewById(R.id.ciudad_equipo_container);
            TextInputLayout serieEquipoContainer = view.findViewById(R.id.serie_equipo_container);
            TextInputLayout modeloEquipoContainer = view.findViewById(R.id.modelo_equipo_container);
            TextInputLayout marcaEquipoContainer = view.findViewById(R.id.marca_equipo_container);


            nombreEquipoContainer.setError(null);
            familiaEquipoContainer.setError(null);
            instalacionProcesoEquipoContainer.setError(null);
            ubicacionEquipoContainer.setError(null);
            paisEquipoContainer.setError(null);
            departamentoEquipoContainer.setError(null);
            ciudadEquipoContainer.setError(null);
            serieEquipoContainer.setError(null);
            modeloEquipoContainer.setError(null);
            marcaEquipoContainer.setError(null);

            boolean familiaParaEquipoRequerida = false;
            if (UserParameter.getValue(view.getContext(), UserParameter.FAMILIA_PARA_EQUIPO_REQUERIDA) != null) {
                familiaParaEquipoRequerida = Boolean.parseBoolean(UserParameter.getValue(view.getContext(), UserParameter.FAMILIA_PARA_EQUIPO_REQUERIDA));
            }

            if (editTextFamilia.getText().toString().isEmpty() && familiaParaEquipoRequerida) {
                familiaEquipoContainer.setError(getString(R.string.crear_equipo_campo_familia_vacio));
                familiaEquipoContainer.requestFocus();
                return false;
            }
            if (editTextNombre.getText().toString().isEmpty()) {
                nombreEquipoContainer.setError(getString(R.string.crear_equipo_campo_nombre_vacio));
                return false;
            }
            if (editTextInstalacionProceso.getText().toString().isEmpty()) {
                instalacionProcesoEquipoContainer.setError(getString(R.string.crear_equipo_campo_instalacion_proceso_vacio));
                return false;
            }
            if (spinnerProvocaParo.getSelectedItemPosition() == 0) {
                Snackbar.make(view, getString(R.string.crear_equipo_campo_provoca_paro_vacio), Snackbar.LENGTH_LONG).show();
                return false;
            }
            if (spinnerEstadoActualEquipo.getSelectedItemPosition() == 0) {
                Snackbar.make(view, getString(R.string.crear_equipo_campo_estado_actual_vacio), Snackbar.LENGTH_LONG).show();
                return false;
            }

            boolean ubicacionParaEquipoRequerida = false;
            if (UserParameter.getValue(view.getContext(), UserParameter.UBICACION_PARA_EQUIPO_REQUERIDA) != null) {
                ubicacionParaEquipoRequerida = Boolean.parseBoolean(UserParameter.getValue(view.getContext(), UserParameter.UBICACION_PARA_EQUIPO_REQUERIDA));
            }
            if (editTextUbicacion.getText().toString().isEmpty() && ubicacionParaEquipoRequerida) {
                ubicacionEquipoContainer.setError(getString(R.string.crear_equipo_campo_ubicacion_vacio));
                ubicacionEquipoContainer.requestFocus();
                return false;
            }

            boolean serieParaEquipoRequerida=false;
            if (UserParameter.getValue(view.getContext(), UserParameter.SERIE_PARA_EQUIPO_REQUERIDA) != null) {
                serieParaEquipoRequerida = Boolean.parseBoolean(UserParameter.getValue(view.getContext(), UserParameter.SERIE_PARA_EQUIPO_REQUERIDA));

            }
            if (editTextSerie.getText().toString().isEmpty() && serieParaEquipoRequerida) {
                serieEquipoContainer.setError(getString(R.string.crear_equipo_campo_serie_vacio));
                serieEquipoContainer.requestFocus();
                return false;
            }

            boolean modeloParaEquipoRequerida=false;
            if (UserParameter.getValue(view.getContext(), UserParameter.MODELO_PARA_EQUIPO_REQUERIDO) != null) {
                modeloParaEquipoRequerida = Boolean.parseBoolean(UserParameter.getValue(view.getContext(), UserParameter.MODELO_PARA_EQUIPO_REQUERIDO));
            }
            if (editTextModelo.getText().toString().isEmpty() && modeloParaEquipoRequerida) {
                modeloEquipoContainer.setError(getString(R.string.crear_equipo_campo_modelo_vacio));
                modeloEquipoContainer.requestFocus();
                return false;
            }

            boolean marcaParaEquipoRequerida=false;
            if (UserParameter.getValue(view.getContext(), UserParameter.MARCA_PARA_EQUIPO_REQUERIDO) != null) {
                marcaParaEquipoRequerida = Boolean.parseBoolean(UserParameter.getValue(view.getContext(), UserParameter.MARCA_PARA_EQUIPO_REQUERIDO));
            }
            if (editTextMarca.getText().toString().isEmpty()&& marcaParaEquipoRequerida) {
                marcaEquipoContainer.setError(getString(R.string.crear_equipo_campo_marca_vacio));
                marcaEquipoContainer.requestFocus();
                return false;
            }

            // --->
            ArrayList<Activo.CentroCosto> centroCostoEquipos = new ArrayList<>();
            if (UserParameter.getValue(view.getContext(), UserParameter.CAMPOS_ADICIONALES_CREAR_EQUIPO) != null) {
                // Validaciones para algunos campos ocultos
                if (editTextUbicacion.getText().toString().isEmpty()) {
                    ubicacionEquipoContainer.setError(getString(R.string.crear_equipo_campo_ubicacion_vacio));
                    return false;
                }
                if (editTextPais.getText().toString().isEmpty()) {
                    paisEquipoContainer.setError(getString(R.string.crear_equipo_campo_pais_vacio));
                    return false;
                }
                if (editTextDepartamento.getText().toString().isEmpty()) {
                    departamentoEquipoContainer.setError(getString(R.string.crear_equipo_campo_departamento_vacio));
                    return false;
                }
                if (editTextCiudad.getText().toString().isEmpty()) {
                    ciudadEquipoContainer.setError(getString(R.string.crear_equipo_campo_ciudad_vacio));
                    return false;
                }

                if (spinnerEstadoEquipo.getSelectedItemPosition() == 0) {
                    Snackbar.make(view, getString(R.string.crear_equipo_campo_estado_vacio), Snackbar.LENGTH_LONG).show();
                    return false;
                }
                if (spinnerCategoriaEquipo.getSelectedItemPosition() == 0) {
                    Snackbar.make(view, getString(R.string.crear_equipo_campo_categoria_vacio), Snackbar.LENGTH_LONG).show();
                    return false;
                }
                if (centroCostoAdapter.isEmpty()) {
                    Snackbar.make(view, getString(R.string.crear_equipo_campo_centro_costo_vacio), Snackbar.LENGTH_LONG).show();
                    return false;
                }

                if (centroCostoAdapter.getItemCount() > 0) {
                    float porcentajeAcumulado = 0;
                    for (int i = 0; i < centroCostoAdapter.getItemCount(); i++) {
                        RecyclerView.ViewHolder holder = recyclerViewCentroCosto.getChildViewHolder(recyclerViewCentroCosto.getChildAt(i));
                        EditText editTextPorcentaje = holder.itemView.findViewById(R.id.porcentaje_centro_costo);
                        String stringPorcentaje = editTextPorcentaje.getText().toString();

                        if (stringPorcentaje.equals("") || stringPorcentaje.endsWith(".")) {
                            stringPorcentaje = stringPorcentaje + "0";
                        }

                        float porcentaje = Float.parseFloat(stringPorcentaje);
                        porcentajeAcumulado = porcentajeAcumulado + porcentaje;

                        CentroCostoEquipo centroCostoEquipo = centroCostoAdapter.getItemPosition(i);
                        if (centroCostoEquipo != null) {
                            Activo.CentroCosto centroCosto = new Activo.CentroCosto();
                            centroCosto.setId(centroCostoEquipo.getId());
                            centroCosto.setCodigo(centroCostoEquipo.getCodigo());
                            centroCosto.setNombre(centroCostoEquipo.getNombre());
                            centroCosto.setPorcentaje(porcentaje);
                            centroCostoEquipos.add(i, centroCosto);
                        }
                    }

                    if (porcentajeAcumulado != 100) {
                        Snackbar.make(view, getString(R.string.crear_equipo_centro_costo_no_100), Snackbar.LENGTH_LONG).show();
                        centroCostoEquipos.clear();
                        return false;
                    }
                }
            }
            // --->

            Activo activo = new Activo();
            activo.setId(id);
            activo.setToken(UUID.randomUUID().toString());
            activo.setCodigo(editTextCodigo.getText().toString());
            activo.setNombre(editTextNombre.getText().toString());
            activo.setIdfamilia(idFamilia);
            activo.setIdinstalacionproceso(idInstalacionProceso);
            activo.setIdinstalacionlocativa(idInstalacionLocativa);
            activo.setProvocaparo(spinnerProvocaParo.getSelectedItemPosition() == 1);
            activo.setObservaciones(editTextObservaciones.getText().toString());
            activo.setSerie(editTextSerie.getText().toString());
            activo.setModelo(editTextModelo.getText().toString());
            activo.setColor(editTextColor.getText().toString());
            activo.setIdfabricante(idMarca);
            activo.setLargo(medida(editTextLargo));
            activo.setAncho(medida(editTextAncho));
            activo.setAlto(medida(editTextAlto));
            activo.setPeso(medida(editTextPeso));

            activo.setUbicacion(editTextUbicacion.getText().toString());

            TextInputEditText annosInventario = view.findViewById(R.id.annos_inventario);
            if (annosInventario != null && annosInventario.getText() != null) {
                activo.setAnnosInventario(annosInventario.getText().toString());
            }

            // -->
            if (UserParameter.getValue(view.getContext(), UserParameter.CAMPOS_ADICIONALES_CREAR_EQUIPO) != null) {
                // Se obtiene información de campos ocultos
                activo.setUbicacion(editTextUbicacion.getText().toString());
                activo.setIdResponsable(idResponsable);
                activo.setCodigocontable(editTextCodigoContable.getText().toString());
                activo.setCentrocostos(centroCostoEquipos);

                Pais pais = database.where(Pais.class)
                        .equalTo("nombre", editTextPais.getText().toString())
                        .findFirst();

                if (pais != null) {
                    activo.setIdpais(pais.getId());

                    Departamento departamento = database.where(Departamento.class)
                            .equalTo("nombre", editTextDepartamento.getText().toString())
                            .equalTo("idpais", pais.getId())
                            .findFirst();

                    if (departamento != null) {
                        activo.setIddepartamento(departamento.getId());

                        Ciudad ciudad = database.where(Ciudad.class)
                                .equalTo("nombre", editTextCiudad.getText().toString())
                                .equalTo("iddepartamento", departamento.getId())
                                .findFirst();

                        if (ciudad != null) {
                            activo.setIdciudad(ciudad.getId());
                        }
                    }
                }

                EstadoTransferenciaEquipo estado = database.where(EstadoTransferenciaEquipo.class)
                        .equalTo("nombre", spinnerEstadoEquipo.getSelectedItem().toString())
                        .findFirst();

                if (estado != null) {
                    activo.setIdestado(estado.getId());
                }

                CategoriaEquipo categoria = database.where(CategoriaEquipo.class)
                        .equalTo("nombre", spinnerCategoriaEquipo.getSelectedItem().toString())
                        .findFirst();

                if (categoria != null) {
                    activo.setIdcategoria(categoria.getId());
                }
            }
            // -->

            List<MedidasEquipo> medidasEquipos = database.where(MedidasEquipo.class)
                    .findAll();

            for (MedidasEquipo medidasEquipo : medidasEquipos) {
                if (!editTextLargo.getText().toString().isEmpty() && spinnerMedidaLargo.getSelectedItem().toString().equals(medidasEquipo.getDescripcion())) {
                    activo.setIdmedidalargo(medidasEquipo.getId());
                }

                if (!editTextAncho.getText().toString().isEmpty() && spinnerMedidaAncho.getSelectedItem().toString().equals(medidasEquipo.getDescripcion())) {
                    activo.setIdmedidaancho(medidasEquipo.getId());
                }

                if (!editTextAlto.getText().toString().isEmpty() && spinnerMedidaAlto.getSelectedItem().toString().equals(medidasEquipo.getDescripcion())) {
                    activo.setIdmedidaalto(medidasEquipo.getId());
                }

                if (!editTextPeso.getText().toString().isEmpty() && spinnerMedidaPeso.getSelectedItem().toString().equals(medidasEquipo.getDescripcion())) {
                    activo.setIdmedidapeso(medidasEquipo.getId());
                }
            }

            //Información extra
            activo.setFamilia(editTextFamilia.getText().toString());
            activo.setInstalacionproceso(editTextInstalacionProceso.getText().toString());
            activo.setInstalacionlocativa(editTextInstalacionLocativa.getText().toString());
            activo.setFabricante(editTextMarca.getText().toString());
            activo.setMedidalargo(spinnerMedidaLargo.getSelectedItem().toString());
            activo.setMedidaancho(spinnerMedidaAncho.getSelectedItem().toString());
            activo.setMedidaalto(spinnerMedidaAlto.getSelectedItem().toString());
            activo.setMedidapeso(spinnerMedidaPeso.getSelectedItem().toString());
            activo.setEstadoactual(spinnerEstadoActualEquipo.getSelectedItem().toString());

            // -->
            if (UserParameter.getValue(view.getContext(), UserParameter.CAMPOS_ADICIONALES_CREAR_EQUIPO) != null) {
                // Se obtiene información extra de campos ocultos
                activo.setPais(editTextPais.getText().toString());
                activo.setDepartamento(editTextDepartamento.getText().toString());
                activo.setCiudad(editTextCiudad.getText().toString());
                activo.setResponsable(editTextResponsable.getText().toString());
                activo.setEstado(spinnerEstadoEquipo.getSelectedItem().toString());
                activo.setCategoria(spinnerCategoriaEquipo.getSelectedItem().toString());
            }
            // -->

            String value = activo.toJson();
            String url = cuenta.getServidor().getUrl() + "/restapp/app/createequipo";
            String accion = Transaccion.ACCION_CREAR_ACTIVO;
            if (!crearEquipo) {
                url = cuenta.getServidor().getUrl() + "/restapp/app/editequipo";
                accion = Transaccion.ACCION_EDITAR_ACTIVO;
            }

            Transaccion transaccion = null;
            if (uuidTransaccion != null) {
                transaccion = database.where(Transaccion.class)
                        .equalTo("UUID", uuidTransaccion)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();
            }

            if (transaccion == null) {
                transaccion = new Transaccion();
                transaccion.setUUID(UUID.randomUUID().toString());
                transaccion.setCuenta(cuenta);
                transaccion.setCreation(Calendar.getInstance().getTime());
                transaccion.setUrl(url);
                transaccion.setVersion(cuenta.getServidor().getVersion());
                transaccion.setValue(value);
                transaccion.setModulo(Transaccion.MODULO_EQUIPOS);
                transaccion.setAccion(accion);
                transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);

                dialogHelper.showProgressDialog();

                compositeDisposable.add(transaccionService.save(transaccion)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(transaccions -> {
                        }, throwable -> {
                            success = false;
                            dialogHelper.dismissProgressDialog();

                            if (throwable != null && throwable.getMessage() != null) {
                                Snackbar.make(view, throwable.getMessage(), Snackbar.LENGTH_LONG).show();
                            }

                        }, () -> {
                            success = true;
                            dialogHelper.dismissProgressDialog();
                            if (getActivity() != null) {
                                getActivity().finish();
                            }
                        }));
                if (UserPermission.check(view.getContext(), UserPermission.REALIZAR_TRANSACCIONES_DIRECTAS, false)) {
                    return success;
                }
            } else {
                Transaccion finalTransaccion = transaccion;
                String finalUrl = url;
                database.update(() -> {
                    finalTransaccion.setCreation(Calendar.getInstance().getTime());
                    finalTransaccion.setValue(value);
                    finalTransaccion.setMessage("");
                    finalTransaccion.setUrl(finalUrl);
                    finalTransaccion.setEstado(Transaccion.ESTADO_PENDIENTE);
                });

                if (getActivity() != null) getActivity().finish();
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "crearEquipo: ", e);
            return false;
        }
    }

    private Double medida(EditText editText) {
        String stringMedida = editText.getText().toString();
        if (stringMedida.equals("")) {
            return null;
        }
        if (stringMedida.endsWith(".")) {
            stringMedida = stringMedida + "0";
        }
        return Double.parseDouble(stringMedida);
    }

    private void onError(Throwable throwable) {
        dismiss();
        new AlertDialog.Builder(view.getContext())
                .setTitle("Por favor intente nuevamente")
                .setMessage(getString(R.string.mensaje_error_obtener_informacion))
                .setCancelable(false)
                .setPositiveButton("Cerrar", (dialogInterface, i) -> {
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                })
                .show();
    }

    private void onComplete() {
        Bundle bundle = getArguments();

        if (bundle == null) {
            return;
        }

        crearEquipo = bundle.getBoolean("crearEquipo");
        uuidTransaccion = bundle.getString(UUID_TRANSACCION);
        MODE_EDIT = bundle.getString(FormularioEquipoActivity.MODE_EDIT);

        List<Pais> listPais = database.where(Pais.class)
                .findAll()
                .sort("nombre");

        List<MedidasEquipo> listMedida = database.where(MedidasEquipo.class)
                .findAll();

        List<CategoriaEquipo> categoriaEquipos = database.where(CategoriaEquipo.class)
                .findAll();

        List<EstadoTransferenciaEquipo> estadoTransferenciaEquipos = database.where(EstadoTransferenciaEquipo.class)
                .findAll();

        List<EstadoActualEquipo> estadoActualEquipos;
        if (crearEquipo) {
            estadoActualEquipos = database.where(EstadoActualEquipo.class)
                    .equalTo("crear", true)
                    .sort("orden")
                    .findAll();
        } else {
            estadoActualEquipos = database.where(EstadoActualEquipo.class)
                    .equalTo("editar", true)
                    .sort("orden")
                    .findAll();
        }


        for (Pais pais : listPais) {
            paises.add(pais.getNombre());
        }

        for (MedidasEquipo medidasEquipo : listMedida) {
            switch (medidasEquipo.getTipo()) {
                case "Longitud":
                    medidasDimension.add(medidasEquipo.getDescripcion());
                    break;

                case "Masa":
                    medidasPeso.add(medidasEquipo.getDescripcion());
                    break;

                default:
                    break;
            }
        }

        categorias.add("Categoría activo");
        for (CategoriaEquipo categoriaEquipo : categoriaEquipos) {
            categorias.add(categoriaEquipo.getNombre());
        }

        estados.add("Estado activo");
        for (EstadoTransferenciaEquipo estadoTransferenciaEquipo : estadoTransferenciaEquipos) {
            estados.add(estadoTransferenciaEquipo.getNombre());
        }

        estadosActual.add("Estado actual equipo");
        for (EstadoActualEquipo estadoActualEquipo : estadoActualEquipos) {
            estadosActual.add(estadoActualEquipo.getNombre());
        }


        adapterDimensiones = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, medidasDimension);
        adapterPesos = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, medidasPeso);

        adapterEstados = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, estados) {
            @Override
            public boolean isEnabled(int position) {
                return position > 0;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setTextColor(position == 0 ? Color.GRAY : Color.BLACK);
                return view;
            }
        };

        adapterEstadosActual = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, estadosActual) {
            @Override
            public boolean isEnabled(int position) {
                return position > 0;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setTextColor(position == 0 ? Color.GRAY : Color.BLACK);
                return view;
            }
        };

        adapterCategorias = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, categorias) {
            @Override
            public boolean isEnabled(int position) {
                return position > 0;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setTextColor(position == 0 ? Color.GRAY : Color.BLACK);
                return view;
            }
        };


        spinnerMedidaLargo = view.findViewById(R.id.medida_largo_equipo);
        spinnerMedidaLargo.setAdapter(adapterDimensiones);

        spinnerMedidaAncho = view.findViewById(R.id.medida_ancho_equipo);
        spinnerMedidaAncho.setAdapter(adapterDimensiones);

        spinnerMedidaAlto = view.findViewById(R.id.medida_alto_equipo);
        spinnerMedidaAlto.setAdapter(adapterDimensiones);

        spinnerMedidaPeso = view.findViewById(R.id.medida_peso_equipo);
        spinnerMedidaPeso.setAdapter(adapterPesos);

        spinnerEstadoEquipo = view.findViewById(R.id.estado_equipo);
        spinnerEstadoEquipo.setAdapter(adapterEstados);

        spinnerEstadoActualEquipo = view.findViewById(R.id.estado_actual_equipo);
        spinnerEstadoActualEquipo.setAdapter(adapterEstadosActual);
        spinnerEstadoActualEquipo.setSelection(1);

        spinnerCategoriaEquipo = view.findViewById(R.id.categoria_equipo);
        spinnerCategoriaEquipo.setAdapter(adapterCategorias);


        UbicacionPredeterminada ubicacionPredeterminada = database.where(UbicacionPredeterminada.class)
                .findFirst();

        if (ubicacionPredeterminada != null) {
            Pais pais = database.where(Pais.class)
                    .equalTo("id", ubicacionPredeterminada.getIdPais())
                    .findFirst();

            Departamento departamento = database.where(Departamento.class)
                    .equalTo("id", ubicacionPredeterminada.getIdDepartamento())
                    .findFirst();

            Ciudad ciudad = database.where(Ciudad.class)
                    .equalTo("id", ubicacionPredeterminada.getIdCiudad())
                    .findFirst();

            if (pais != null) {
                editTextPais.setText(pais.getNombre());

                List<Departamento> listDepartamentos = database.where(Departamento.class)
                        .equalTo("idpais", pais.getId())
                        .findAll()
                        .sort("nombre");

                for (Departamento departamentoAux : listDepartamentos) {
                    departamentos.add(departamentoAux.getNombre());
                }
            }
            if (departamento != null) {
                editTextDepartamento.setText(departamento.getNombre());

                List<Ciudad> listCiudades = database.where(Ciudad.class)
                        .equalTo("iddepartamento", departamento.getId())
                        .findAll()
                        .sort("nombre");

                for (Ciudad ciudadAux : listCiudades) {
                    ciudades.add(ciudadAux.getNombre());
                }
            }
            if (ciudad != null) {
                editTextCiudad.setText(ciudad.getNombre());
            }

            // si campo ubicación requerido campo debe estar vacio
            if (UserParameter.getValue(view.getContext(), UserParameter.UBICACION_PARA_EQUIPO_REQUERIDA) != null) {
                editTextUbicacion.setText("");
            } else if (ubicacionPredeterminada.getUbicacion() != null) {
                editTextUbicacion.setText(ubicacionPredeterminada.getUbicacion());
            }

        }


        if (uuidTransaccion == null) {
            if (!crearEquipo) {
                editarEquipo(bundle);
            }
        } else {
            editarTransaccionEquipo(MODE_EDIT);
        }

        dismiss();
    }

    private void startProgressDialog() {
        progressDialog = new ProgressDialog(view.getContext());
        progressDialog.setTitle("Actualizando información");
        progressDialog.setMessage(getString(R.string.mensaje_progress_espera));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismiss() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void editarEquipo(Bundle bundle) {
        Equipo equipo = database.where(Equipo.class)
                .equalTo("uuid", bundle.getString("uuidEquipo"))
                .equalTo("id", bundle.getLong("idEquipo"))
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .findFirst();

        if (equipo != null) {
            id = equipo.getId();
            editTextCodigo.setText(equipo.getCodigo());
            editTextNombre.setText(equipo.getNombre());
            editTextFamilia.setText(equipo.getFamilia1());
            spinnerProvocaParo.setSelection(equipo.getProvocaparo().equals("No") ? 2 : 1);
            editTextUbicacion.setText(equipo.getUbicacion());
            editTextObservaciones.setText(equipo.getObservaciones());

            String split = " \\| ";
            String instalacionProceso = equipo.getInstalacionproceso();
            String instalacionLocativa = equipo.getInstalacionlocativa();

            if (instalacionProceso != null) {
                String[] instalacionProcesoSplit = instalacionProceso.split(split);
                editTextInstalacionProceso.setText(instalacionProcesoSplit[1]);
            }
            if (instalacionLocativa != null) {
                String[] instalacionLocativaSplit = instalacionLocativa.split(split);
                editTextInstalacionLocativa.setText(instalacionLocativaSplit[1]);
            }

            if (equipo.getIdfamilia1() != null) {
                idFamilia = equipo.getIdfamilia1();
            }
            if (equipo.getIdinstalacionproceso() != null) {
                idInstalacionProceso = equipo.getIdinstalacionproceso();
            }
            if (equipo.getIdinstalacionlocativa() != null) {
                idInstalacionLocativa = equipo.getIdinstalacionlocativa();
            }
            if (equipo.getPais() != null) {
                editTextPais.setText(equipo.getPais());
            }
            if (equipo.getDepartamento() != null) {
                editTextDepartamento.setText(equipo.getDepartamento());
            }
            if (equipo.getCiudad() != null) {
                editTextCiudad.setText(equipo.getCiudad());
            }
            if (equipo.getUbicacion() != null) {
                editTextUbicacion.setText(equipo.getUbicacion());
            }

            String estadoActual = equipo.getEstado();
            if (estadoActual != null) {
                spinnerEstadoActualEquipo.setSelection(adapterEstadosActual.getPosition(estadoActual));
            }


            if (equipo.getInformacionTecnica() != null) {
                idMarca = equipo.getInformacionTecnica().getIdfabricante();

                editTextSerie.setText(equipo.getInformacionTecnica().getNroserie());
                editTextModelo.setText(equipo.getInformacionTecnica().getModelo());
                editTextColor.setText(equipo.getInformacionTecnica().getColor());

                String marca = equipo.getInformacionTecnica().getFabricante();
                if (marca != null) {
                    String[] marcaSplit = marca.split(split);
                    editTextMarca.setText(marcaSplit[1]);
                }

                if (equipo.getInformacionTecnica().getLargo() != null) {
                    editTextLargo.setText(String.valueOf(equipo.getInformacionTecnica().getLargo()));
                }
                if (equipo.getInformacionTecnica().getAncho() != null) {
                    editTextAncho.setText(String.valueOf(equipo.getInformacionTecnica().getAncho()));
                }
                if (equipo.getInformacionTecnica().getAlto() != null) {
                    editTextAlto.setText(String.valueOf(equipo.getInformacionTecnica().getAlto()));
                }
                if (equipo.getInformacionTecnica().getPeso() != null) {
                    editTextPeso.setText(String.valueOf(equipo.getInformacionTecnica().getPeso()));
                }

                String medidaLargo = equipo.getInformacionTecnica().getMedidalargo();
                if (medidaLargo != null) {
                    spinnerMedidaLargo.setSelection(adapterDimensiones.getPosition(medidaLargo));
                }

                String medidaAncho = equipo.getInformacionTecnica().getMedidaancho();
                if (medidaAncho != null) {
                    spinnerMedidaAncho.setSelection(adapterDimensiones.getPosition(medidaAncho));
                }

                String medidaAlto = equipo.getInformacionTecnica().getMedidaalto();
                if (medidaAlto != null) {
                    spinnerMedidaAlto.setSelection(adapterDimensiones.getPosition(medidaAlto));
                }

                String medidaPeso = equipo.getInformacionTecnica().getMedidapeso();
                if (medidaPeso != null) {
                    spinnerMedidaPeso.setSelection(adapterPesos.getPosition(medidaPeso));
                }
            }

            if (equipo.getPersonal() != null) {
                idResponsable = equipo.getPersonal().getId();
                editTextResponsable.setText(equipo.getPersonal().getNombre());

                String estado = equipo.getPersonal().getEstado();
                if (estado != null) {
                    spinnerEstadoEquipo.setSelection(adapterEstados.getPosition(estado));
                }
            }

            if (equipo.getInformacionfinanciera() != null) {
                if (view != null) {
                    TextInputEditText annosInventario = view.findViewById(R.id.annos_inventario);
                    if (annosInventario != null) {
                        annosInventario.setText(equipo.getInformacionfinanciera().getAnnosInventario());
                    }
                }

                editTextCodigoContable.setText(equipo.getInformacionfinanciera().getCodigocontable());

                String categoria = equipo.getInformacionfinanciera().getCategoria();
                if (categoria != null) {
                    spinnerCategoriaEquipo.setSelection(adapterCategorias.getPosition(categoria));
                }

                List<CentroCostoEquipo> centroCostoEquipos = equipo.getInformacionfinanciera().getCentrocostos().isManaged()
                        ? database.copyFromRealm(equipo.getInformacionfinanciera().getCentrocostos())
                        : equipo.getInformacionfinanciera().getCentrocostos();

                centroCostoAdapter.addAll(centroCostoEquipos);
                centroCostoAdapter.refresh();
            }

            if (equipo.getIdpais() != null) {
                List<Departamento> listDepartamento = database.where(Departamento.class)
                        .equalTo("idpais", equipo.getIdpais())
                        .findAll()
                        .sort("nombre");

                for (Departamento departamento : listDepartamento) {
                    departamentos.add(departamento.getNombre());
                }
            }
            if (equipo.getIddepartamento() != null) {
                List<Ciudad> listCiudad = database.where(Ciudad.class)
                        .equalTo("iddepartamento", equipo.getIddepartamento())
                        .findAll()
                        .sort("nombre");

                for (Ciudad ciudad : listCiudad) {
                    ciudades.add(ciudad.getNombre());
                }
            }
        }
    }

    public void editarTransaccionEquipo(String value) {
        if (value != null) {
            Gson gson = new Gson();
            Activo equipo = gson.fromJson(value, Activo.class);

            if (equipo != null) {
                id = equipo.getId();
                editTextCodigo.setText(equipo.getCodigo());
                editTextNombre.setText(equipo.getNombre());
                editTextFamilia.setText(equipo.getFamilia());
                editTextInstalacionProceso.setText(equipo.getInstalacionproceso());
                editTextInstalacionLocativa.setText(equipo.getInstalacionlocativa());
                spinnerProvocaParo.setSelection(!equipo.isProvocaparo() ? 2 : 1);
                editTextObservaciones.setText(equipo.getObservaciones());
                editTextSerie.setText(equipo.getSerie());
                editTextModelo.setText(equipo.getModelo());
                editTextColor.setText(equipo.getColor());
                editTextMarca.setText(equipo.getFabricante());
                editTextResponsable.setText(equipo.getResponsable());
                editTextCodigoContable.setText(equipo.getCodigocontable());

                if (view != null) {
                    TextInputEditText annosInventario = view.findViewById(R.id.annos_inventario);
                    if (annosInventario != null) {
                        annosInventario.setText(equipo.getAnnosInventario());
                    }

                    // -->
                    if (UserParameter.getValue(view.getContext(), UserParameter.CAMPOS_ADICIONALES_CREAR_EQUIPO) != null) {
                        // Campos ocultos
                        editTextUbicacion.setText(equipo.getUbicacion());
                        editTextPais.setText(equipo.getPais());
                        editTextDepartamento.setText(equipo.getDepartamento());
                        editTextCiudad.setText(equipo.getCiudad());

                        idResponsable = equipo.getIdResponsable();
                    }
                    // -->
                }

                idFamilia = equipo.getIdfamilia();
                idInstalacionProceso = equipo.getIdinstalacionproceso();
                idInstalacionLocativa = equipo.getIdinstalacionlocativa();
                idMarca = equipo.getIdfabricante();

                if (equipo.getLargo() != null) {
                    editTextLargo.setText(String.valueOf(equipo.getLargo()));
                }
                if (equipo.getAncho() != null) {
                    editTextAncho.setText(String.valueOf(equipo.getAncho()));
                }
                if (equipo.getAlto() != null) {
                    editTextAlto.setText(String.valueOf(equipo.getAlto()));
                }
                if (equipo.getPeso() != null) {
                    editTextPeso.setText(String.valueOf(equipo.getPeso()));
                }

                String medidaLargo = equipo.getMedidalargo();
                if (medidaLargo != null) {
                    spinnerMedidaLargo.setSelection(adapterDimensiones.getPosition(medidaLargo));
                }
                String medidaAncho = equipo.getMedidaancho();
                if (medidaAncho != null) {
                    spinnerMedidaAncho.setSelection(adapterDimensiones.getPosition(medidaAncho));
                }
                String medidaAlto = equipo.getMedidaalto();
                if (medidaAlto != null) {
                    spinnerMedidaAlto.setSelection(adapterDimensiones.getPosition(medidaAlto));
                }
                String medidaPeso = equipo.getMedidapeso();
                if (medidaPeso != null) {
                    spinnerMedidaPeso.setSelection(adapterPesos.getPosition(medidaPeso));
                }
                String estado = equipo.getEstado();
                if (estado != null) {
                    spinnerEstadoEquipo.setSelection(adapterEstados.getPosition(estado));
                }
                String estadoActual = equipo.getEstadoactual();
                if (estadoActual != null) {
                    spinnerEstadoActualEquipo.setSelection(adapterEstadosActual.getPosition(estadoActual));
                }
                String categoria = equipo.getCategoria();
                if (categoria != null) {
                    spinnerCategoriaEquipo.setSelection(adapterCategorias.getPosition(categoria));
                }

                // -->
                if (UserParameter.getValue(view.getContext(), UserParameter.CAMPOS_ADICIONALES_CREAR_EQUIPO) != null) {
                    List<Activo.CentroCosto> centroCostos = new ArrayList<>(equipo.getCentrocostos());
                    for (Activo.CentroCosto centroCosto : centroCostos) {
                        centroCostoAdapter.add(new CentroCostoEquipo(centroCosto.getId(), centroCosto.getCodigo(), centroCosto.getNombre(), centroCosto.getPorcentaje()));
                    }
                    centroCostoAdapter.refresh();

                    if (equipo.getIdpais() != null) {
                        List<Departamento> listDepartamento = database.where(Departamento.class)
                                .equalTo("idpais", equipo.getIdpais())
                                .findAll()
                                .sort("nombre");

                        for (Departamento departamento : listDepartamento) {
                            departamentos.add(departamento.getNombre());
                        }
                    }

                    if (equipo.getIddepartamento() != null) {
                        List<Ciudad> listCiudad = database.where(Ciudad.class)
                                .equalTo("iddepartamento", equipo.getIddepartamento())
                                .findAll()
                                .sort("nombre");

                        for (Ciudad ciudad : listCiudad) {
                            ciudades.add(ciudad.getNombre());
                        }
                    }
                    // -->
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        compositeDisposable.clear();
        paises.clear();
        departamentos.clear();
        ciudades.clear();
        medidasDimension.clear();
        medidasPeso.clear();
        categorias.clear();
        estados.clear();

        if (centroCostoAdapter != null) {
            centroCostoAdapter.clear();
        }
        if (adapterEstados != null) {
            adapterEstados.clear();
        }
        if (adapterDimensiones != null) {
            adapterDimensiones.clear();
        }
        if (adapterPesos != null) {
            adapterPesos.clear();
        }
        if (adapterCategorias != null) {
            adapterCategorias.clear();
        }
        if (database != null) {
            database.close();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 1) {
            if (data != null && data.getExtras() != null) {
                Bundle bundle = data.getExtras();
                Long idEntidad = bundle.getLong("idEntidad");
                String tipoEntidad = bundle.getString("tipoEntidad");

                if (tipoEntidad != null) {
                    switch (tipoEntidad) {
                        case "Familia":
                            Familia familia = database.where(Familia.class)
                                    .equalTo("id", idEntidad)
                                    .findFirst();

                            if (familia != null) {
                                idFamilia = idEntidad;
                                editTextFamilia.setText(familia.getNombre());
                            }
                            break;

                        case "InstalacionProceso":
                            InstalacionProceso instalacionProceso = database.where(InstalacionProceso.class)
                                    .equalTo("id", idEntidad)
                                    .findFirst();

                            if (instalacionProceso != null) {
                                idInstalacionProceso = idEntidad;
                                editTextInstalacionProceso.setText(instalacionProceso.getNombre());
                            }
                            break;

                        case "InstalacionLocativa":
                            InstalacionLocativa instalacionLocativa = database.where(InstalacionLocativa.class)
                                    .equalTo("id", idEntidad)
                                    .findFirst();

                            if (instalacionLocativa != null) {
                                idInstalacionLocativa = idEntidad;
                                editTextInstalacionLocativa.setText(instalacionLocativa.getNombre());
                            }
                            break;

                        case "Fabricante":
                            MarcaEquipo marcaEquipo = database.where(MarcaEquipo.class)
                                    .equalTo("id", idEntidad)
                                    .findFirst();

                            if (marcaEquipo != null) {
                                idMarca = idEntidad;
                                editTextMarca.setText(marcaEquipo.getNombre());
                            }
                            break;

                        case "Personal":
                            Responsable responsable = database.where(Responsable.class)
                                    .equalTo("id", idEntidad)
                                    .findFirst();

                            if (responsable != null) {
                                idResponsable = idEntidad;
                                editTextResponsable.setText(responsable.getNombre());
                            }
                            break;

                        case "CentroCosto":
                            CentroCostoEquipo centroCostoEquipo = database.where(CentroCostoEquipo.class)
                                    .equalTo("cuenta.UUID", cuenta.getUUID())
                                    .equalTo("id", idEntidad)
                                    .findFirst();

                            if (centroCostoEquipo != null) {
                                centroCostoAdapter.add(centroCostoEquipo);
                                centroCostoAdapter.refresh();
                            }
                            break;
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
}