package com.mantum.cmms.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;

import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.mantum.demo.R;
import com.mantum.cmms.activity.BitacoraActivity;
import com.mantum.cmms.activity.CaptureActivityPortrait;
import com.mantum.cmms.activity.DescargarRutaTrabajoActivity;
import com.mantum.cmms.activity.GaleriaActivity;
import com.mantum.cmms.activity.MovimientoActivity;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.domain.Spinner;
import com.mantum.cmms.entity.ANS;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Entidad;
import com.mantum.cmms.entity.Estado;
import com.mantum.cmms.entity.EstadoCategoria;
import com.mantum.cmms.entity.Movimiento;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.cmms.entity.Recorrido;
import com.mantum.cmms.entity.RecorridoHistorico;
import com.mantum.cmms.entity.SSxOT;
import com.mantum.cmms.entity.Sitio;
import com.mantum.cmms.entity.parameter.Area;
import com.mantum.cmms.entity.parameter.TypeArea;
import com.mantum.cmms.entity.parameter.UserParameter;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.cmms.service.ATNotificationService;
import com.mantum.cmms.service.MovimientoService;
import com.mantum.cmms.service.RecorridoHistoricoService;
import com.mantum.cmms.service.RecorridoService;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.service.Photo;
import com.mantum.component.service.PhotoAdapter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;

import static android.Manifest.permission.CALL_PHONE;
import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.mantum.cmms.entity.parameter.UserParameter.MOSTRAR_SITIO_SS;
import static com.mantum.cmms.service.ATNotificationService.COMPLETAR;
import static com.mantum.cmms.service.ATNotificationService.DISPONIBLE;
import static com.mantum.cmms.service.ATNotificationService.EN_EJECUCION;
import static com.mantum.cmms.service.ATNotificationService.EN_SITIO;
import static com.mantum.cmms.service.ATNotificationService.EN_SITIO_AUTOMATICO;
import static com.mantum.cmms.service.ATNotificationService.FIN_EJECUCION;

public class RecorridoFragment extends Mantum.Fragment {

    private static final int CALL_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = RecorridoFragment.class.getSimpleName();

    public static final String KEY_TAB = "Tecnico";
    public static final String QR_CODE = "QR_CODE";

    private static ATNotificationService.Estado ESTADO_ACTUAL = DISPONIBLE;

    private OnCompleteListener onCompleteListener;
    private final List<Photo> photos = new ArrayList<>();
    private boolean isNovedad = false;

    @Nullable
    private Long identidad;

    @Nullable
    private String codigo;

    @Nullable
    private Sitio sitio;

    @Nullable
    private SSxOT ss;

    @Nullable
    private String estado;

    @Nullable
    private View.OnClickListener onClickDetalle;

    @Nullable
    private NfcAdapter activeNFC;

    private RecorridoService recorridoService;

    private ATNotificationService atNotificationService;

    private RecorridoHistoricoService recorridoHistoricoService;

    private Database database;

    private List<ANS> ans = new ArrayList<>();

    private String uuid;

    private Handler handlerReduce;

    private Handler handlerTimer;

    private AlertDialog.Builder dialogGeneral;
    private AlertDialog closeDialogGeneral;
    private List<Spinner> spinner;
    private List<Spinner> spinnerANS;
    private AppCompatSpinner tipo;
    private AppCompatSpinner prioridad;
    private Estado estadoActualPersonal;

    public boolean nfcActive = false;
    private final int TIME_ALERT = 7000;
    private MovimientoService movimientoService;
    private ProgressBar progressBar;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private OrdenTrabajo otRecorrido;

    public void setIdentidad(@Nullable Long identidad) {
        this.identidad = identidad;
    }

    public void setCodigo(@NonNull String codigo) {
        this.codigo = codigo;
    }

    public void setSitio(@Nullable Sitio sitio) {
        this.sitio = sitio;
    }

    public void setSs(@Nullable SSxOT ss) {
        this.ss = ss;
    }

    public void setOnClickDetalle(@Nullable View.OnClickListener onClickDetalle) {
        this.onClickDetalle = onClickDetalle;
    }

    public void setEstado(@Nullable String estado) {
        this.estado = estado;
    }

    public void setANS(@NonNull List<ANS> ans) {
        this.ans = ans;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_recorrido, container, false);

            isNovedad = false;
            ESTADO_ACTUAL = DISPONIBLE;

            database = new Database(view.getContext());
            atNotificationService = new ATNotificationService(view.getContext());
            recorridoHistoricoService = new RecorridoHistoricoService(view.getContext());
            recorridoService = new RecorridoService(view.getContext(), RecorridoService.Tipo.OT);
            dialogGeneral = new AlertDialog.Builder(view.getContext(), R.style.DialogTheme);

            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            movimientoService = new MovimientoService(view.getContext(), cuenta);
            progressBar = view.findViewById(R.id.progressBar);

            TextView codigoTextView = view.findViewById(R.id.codigo);
            codigoTextView.setText(codigo);

            boolean mostrarSitioSS = true;
            if (UserParameter.getValue(view.getContext(), MOSTRAR_SITIO_SS) != null) {
                mostrarSitioSS = Boolean.parseBoolean(
                        UserParameter.getValue(view.getContext(), MOSTRAR_SITIO_SS)
                );
            }

            if (mostrarSitioSS) {
                if (sitio != null) {
                    if (sitio.getDireccion() != null) {
                        TextView direccionTextView = view.findViewById(R.id.direccion);
                        direccionTextView.setText(sitio.getDireccion());
                    }

                    if (sitio.getReferenciadireccion() != null) {
                        TextView referenciaDireccionTextView = view.findViewById(R.id.referencia_direccion);
                        referenciaDireccionTextView.setText(sitio.getReferenciadireccion());
                    }

                    if (sitio.getCiudad() != null && sitio.getPais() != null) {
                        TextView regionTextView = view.findViewById(R.id.region);
                        regionTextView.setText(String.format("%s - %s", sitio.getCiudad(), sitio.getPais()));
                    }

                    TextView inputContacto = view.findViewById(R.id.nombre);
                    if (sitio.getTelefono() != null) {

                        if (sitio.getContacto() != null)
                            inputContacto.setText(String.format("%s: %s", getString(R.string.contacto), sitio.getContacto()));

                        TextView inputCargo = view.findViewById(R.id.cargo);
                        if (sitio.getCargo() != null)
                            inputCargo.setText(String.format("%s: %s", getString(R.string.cargo), sitio.getCargo()));

                        TextView inputSitio = view.findViewById(R.id.telefono);
                        inputSitio.setText(String.format("%s: %s", getString(R.string.telefono), sitio.getTelefono()));

                    } else {
                        view.findViewById(R.id.panelconacto).setVisibility(View.GONE);
                    }
                }
            } else {
                view.findViewById(R.id.panelInformacionSitio).setVisibility(View.GONE);
            }

            if (sitio != null) {
                if (sitio.getCodigoss() != null) {
                    TextView input = view.findViewById(R.id.codigo_ss);
                    input.setText(String.format("%s: %s", getString(R.string.codigo_ss), sitio.getCodigoss()));
                }

                if (sitio.getCodigoexterno() != null) {
                    TextView input = view.findViewById(R.id.codigo_externo);
                    input.setText(String.format("%s: %s", getString(R.string.codigo_externo), sitio.getCodigoexterno()));
                }

                if (sitio.getCodigoexterno2() != null) {
                    TextView input = view.findViewById(R.id.codigo_externo_2);
                    input.setText(String.format("%s: %s", getString(R.string.codigo_externo_dos), sitio.getCodigoexterno2()));
                }
            } else if (ss != null) {
                if (ss.getCodigoss() != null) {
                    TextView input = view.findViewById(R.id.codigo_ss);
                    input.setText(String.format("%s: %s", getString(R.string.codigo_ss), ss.getCodigoss()));
                }

                if (ss.getCodigoexterno() != null) {
                    TextView input = view.findViewById(R.id.codigo_externo);
                    input.setText(String.format("%s: %s", getString(R.string.codigo_externo), ss.getCodigoexterno()));
                }

                if (ss.getCodigoexterno2() != null) {
                    TextView input = view.findViewById(R.id.codigo_externo_2);
                    input.setText(String.format("%s: %s", getString(R.string.codigo_externo_dos), ss.getCodigoexterno2()));
                }
            }

            if (sitio != null && sitio.getCodigoss() == null && ss != null && ss.getCodigoss() == null && identidad != null) {
                view.findViewById(R.id.panelInformacionSitio).setVisibility(View.GONE);
                view.findViewById(R.id.panelInformacionSS).setVisibility(View.GONE);
                view.findViewById(R.id.panelrecalcular).setVisibility(View.GONE);
                view.findViewById(R.id.action).setVisibility(View.GONE);
                view.findViewById(R.id.bitacora).setVisibility(View.VISIBLE);
                view.findViewById(R.id.bitacora).setOnClickListener(v -> {
                    Bundle bundle = new Bundle();
                    bundle.putLong(BitacoraActivity.KEY_ID, identidad);
                    bundle.putInt(BitacoraActivity.KEY_TIPO_BITACORA, BitacoraActivity.OT);

                    Intent intent = new Intent(getActivity(), BitacoraActivity.class);
                    intent.putExtras(bundle);

                    startActivityForResult(intent, BitacoraActivity.REQUEST_ACTION);
                });
            }

            view.findViewById(R.id.llamar).setOnClickListener(v -> {
                if (sitio == null || sitio.getTelefono() == null || sitio.getTelefono().isEmpty()) {
                    Snackbar.make(view, R.string.telefono_requerido, Snackbar.LENGTH_LONG)
                            .setDuration(TIME_ALERT)
                            .show();
                    return;
                }

                if (!requestPermission(view.getContext())) {
                    return;
                }

                Mantum.call(view.getContext(), sitio.getTelefono());
            });

            view.findViewById(R.id.detalle).setOnClickListener(onClickDetalle);

            view.findViewById(R.id.ver_sitio).setOnClickListener(v -> {
                if (sitio == null || !sitio.isCoordenada()) {
                    Snackbar.make(view, R.string.at_ubicacion_sitio, Snackbar.LENGTH_LONG)
                            .setDuration(TIME_ALERT)
                            .show();
                    return;
                }

                Mantum.goGoogleMap(view.getContext(),
                        "https://www.google.com/maps/search/?api=1&query=" + sitio.getLatitud() + "," + sitio.getLongitud());
            });

            TextView estadoActual = view.findViewById(R.id.estado_actual);
            estadoActual.setText(String.format("%s: %s", getString(R.string.estado_actual), ESTADO_ACTUAL.getMostrar(view.getContext())));

            TextView novedadTextView = view.findViewById(R.id.novedad_actual);
            TextView observacionNovedadTextView = view.findViewById(R.id.observacion_novedad_actual);
            TextView fechaNovedadTextView = view.findViewById(R.id.novedad_fecha);
            TextView tiempoNovedadTextView = view.findViewById(R.id.novedad_tiempo);

            Button recalcularButton = view.findViewById(R.id.btn_recalcular);
            Button novedadButton = view.findViewById(R.id.novedad);
            Button actionButton = view.findViewById(R.id.action);
            if (identidad != null) {
                Recorrido recorrido = recorridoService.obtener(identidad);
                if (recorrido != null && recorrido.getEstado() != null) {
                    ATNotificationService.Estado estado = ATNotificationService.Estado.getEstado(
                            getContext(), recorrido.getEstado());
                    if (estado != null) {
                        prepare(view, estado);
                    }
                }
            }

            if (identidad != null && !ESTADO_ACTUAL.is(DISPONIBLE)) {
                RecorridoHistorico historico = recorridoHistoricoService.findRecorridoIdNotNull(identidad);
                if (historico != null) {
                    Date fechaInicio = historico.getFecha();

                    Calendar now = Calendar.getInstance();
                    long diff = now.getTime().getTime() - fechaInicio.getTime();
                    long minutes = (diff / 1000) / 60;

                    TextView estadoTiempoTextView = view.findViewById(R.id.estado_tiempo);
                    estadoTiempoTextView.setText(String.format("%s minutos", minutes));
                    estadoTiempoTextView.setVisibility(View.VISIBLE);

                    timer(view, fechaInicio, R.id.estado_tiempo);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

                    TextView estadoFechaTextView = view.findViewById(R.id.estado_fecha);
                    estadoFechaTextView.setText(simpleDateFormat.format(fechaInicio));
                    estadoFechaTextView.setVisibility(View.VISIBLE);
                }

                RecorridoHistorico historicoNovedad = recorridoHistoricoService.findNovedad(identidad);
                RecorridoHistorico ultimoHistorico = recorridoHistoricoService.find(identidad);

                if (historicoNovedad != null && ultimoHistorico != null && ultimoHistorico.getFecha().before(historicoNovedad.getFecha())) {
                    isNovedad = true;
                    Date fechaInicio = historicoNovedad.getFecha();

                    novedadTextView.setText(historicoNovedad.getTitle());
                    novedadTextView.setVisibility(View.VISIBLE);

                    observacionNovedadTextView.setText(historicoNovedad.getComentario());
                    observacionNovedadTextView.setVisibility(View.VISIBLE);

                    Calendar now = Calendar.getInstance();
                    long diff = now.getTime().getTime() - fechaInicio.getTime();
                    long minutes = (diff / 1000) / 60;

                    tiempoNovedadTextView.setText(String.format("%s minutos", minutes));
                    tiempoNovedadTextView.setVisibility(View.VISIBLE);

                    timer(view, fechaInicio, R.id.novedad_fecha);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm", Locale.getDefault()
                    );

                    fechaNovedadTextView.setText(simpleDateFormat.format(fechaInicio));
                    fechaNovedadTextView.setVisibility(View.VISIBLE);
                }
            }

            OrdenTrabajo ot = database.where(OrdenTrabajo.class)
                    .equalTo("id", identidad)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .equalTo("UUID", uuid)
                    .findFirst();

            actionButton.setOnClickListener(v -> {
                if (identidad == null) {
                    return;
                }

                if (!cuenta.isDisponible()) {
                    Snackbar.make(view, R.string.estado_disponible, Snackbar.LENGTH_LONG)
                            .setDuration(TIME_ALERT)
                            .show();
                    return;
                }

                novedadTextView.setVisibility(View.GONE);
                fechaNovedadTextView.setVisibility(View.GONE);
                tiempoNovedadTextView.setVisibility(View.GONE);
                observacionNovedadTextView.setVisibility(View.GONE);

                if (ESTADO_ACTUAL.is(COMPLETAR) || ESTADO_ACTUAL.is(EN_EJECUCION)) {
                    Bundle bundle = new Bundle();
                    bundle.putLong(BitacoraActivity.KEY_ID, identidad);
                    bundle.putInt(BitacoraActivity.KEY_TIPO_BITACORA, BitacoraActivity.OT);
                    bundle.putBoolean(BitacoraActivity.MODO_RECORRIDO, true);
                    bundle.putBoolean(BitacoraActivity.MODO_RECORRIDO, true);

                    RecorridoHistorico lastState = recorridoHistoricoService.find(identidad);

                    Recorrido recorrido = recorridoService.obtener(identidad);

                    if (recorrido != null && recorrido.isSincronizado()) {
                        Date date = Calendar.getInstance().getTime();
                        bundle.putLong(BitacoraActivity.HORA_FIN, date.getTime());
                    } else {
                        if (lastState != null && lastState.getFecha() != null) {
                            bundle.putLong(BitacoraActivity.HORA_FIN, lastState.getFecha().getTime());
                        }
                    }

                    Intent intent = new Intent(getActivity(), BitacoraActivity.class);
                    intent.putExtras(bundle);

                    startActivityForResult(intent, BitacoraActivity.REQUEST_ACTION);
                    return;
                }

                ATNotificationService.Estado estado = siguienteEstado(ESTADO_ACTUAL);
                if (estado == null) {
                    return;
                }

                if (estado.equals(EN_SITIO) && UserPermission.check(view.getContext(), UserPermission.VALIDAR_QR_SITIO, false)) {

                    if (ot != null && ot.getEntidadValida() == null) {
                        try {
                            LayoutInflater factory = LayoutInflater.from(getContext());
                            final View view2 = factory.inflate(R.layout.dialog_entidad_scan, null);

                            view2.findViewById(R.id.navigation_camera).setOnClickListener(v2 -> {
                                IntentIntegrator integrator = new IntentIntegrator(getActivity());
                                integrator.setOrientationLocked(true);
                                integrator.setCameraId(0);
                                integrator.setPrompt(getString(R.string.at_validar_entidad));
                                integrator.setCaptureActivity(CaptureActivityPortrait.class);
                                integrator.setBeepEnabled(false);
                                integrator.initiateScan();
                            });

                            if (activeNFC != null) {
                                view2.findViewById(R.id.navigation_nfc).setOnClickListener(v2 -> {
                                    view2.findViewById(R.id.section_scan).setVisibility(View.VISIBLE);
                                    v2.setVisibility(View.GONE);
                                    view2.findViewById(R.id.navigation_camera).setVisibility(View.GONE);
                                    nfcActive = true;
                                });
                            } else {
                                view2.findViewById(R.id.navigation_nfc).setVisibility(View.GONE);
                            }

                            dialogGeneral.setTitle(getString(R.string.at_validar_entidad));
                            dialogGeneral.setView(view2);
                            dialogGeneral.setCancelable(false);
                            dialogGeneral.setNegativeButton(R.string.close, (dialog, id) -> {
                                nfcActive = false;
                                closeDialogGeneral.dismiss();
                            });
                            closeDialogGeneral = dialogGeneral.show();
                            return;
                        } catch (Exception e) {
                            Log.d(TAG, "run: " + e);
                            return;
                        }
                    }
                }

                atNotificationService.cambiarEstado(identidad, codigo, ATNotificationService.OT, estado, null, null, null, null, (value, error) -> {
                    if (error) {
                        Snackbar.make(view, value, Snackbar.LENGTH_LONG)
                                .setDuration(TIME_ALERT)
                                .show();
                        return;
                    }

                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm", Locale.getDefault());

                    TextView estadoTiempoTextView = view.findViewById(R.id.estado_tiempo);
                    estadoTiempoTextView.setText(String.format("%s minutos", 0));
                    estadoTiempoTextView.setVisibility(View.VISIBLE);

                    timer(view, calendar.getTime(), R.id.estado_tiempo);
                    TextView estadoFechaTextView = view.findViewById(R.id.estado_fecha);
                    estadoFechaTextView.setText(simpleDateFormat.format(calendar.getTime()));
                    estadoFechaTextView.setVisibility(View.VISIBLE);

                    prepare(view, estado);
                    actualizarANS(cuenta);

                    showANS(view, cuenta);
                    procesarEstados(cuenta, recalcularButton, novedadButton);
                    Snackbar.make(view, value, Snackbar.LENGTH_LONG)
                            .show();
                }, isNovedad);

                if (isNovedad) {
                    isNovedad = false;
                }
            });

            procesarEstados(cuenta, recalcularButton, novedadButton);
            if (isNovedad) {
                novedadButton.setVisibility(View.VISIBLE);
                novedadButton.setText(R.string.detener_novedad);
                novedadButton.setTextColor(ResourcesCompat.getColor(
                        getResources(), R.color.red, null));
                novedadButton.setBackground(ResourcesCompat.getDrawable(
                        getResources(), R.drawable.red_bg_bordered_button, null));
            }

            novedadButton.setOnClickListener(v -> {

                if (!cuenta.isDisponible()) {
                    Snackbar.make(view, R.string.estado_disponible, Snackbar.LENGTH_LONG)
                            .setDuration(TIME_ALERT)
                            .show();
                    return;
                }

                if (isNovedad) {
                    RecorridoHistorico recorridoHistorico = recorridoHistoricoService.findNovedad(identidad);
                    if (recorridoHistorico == null) {
                        return;
                    }

                    atNotificationService.cambiarEstado(identidad, codigo, ATNotificationService.OT, ESTADO_ACTUAL, getString(R.string.mensaje_finalizacion_novedad), null, null, null, (value, error) -> {
                        if (error) {
                            Snackbar.make(view, value, Snackbar.LENGTH_LONG)
                                    .setDuration(TIME_ALERT)
                                    .show();
                            return;
                        }

                        isNovedad = false;
                        novedadTextView.setVisibility(View.GONE);
                        fechaNovedadTextView.setVisibility(View.GONE);
                        tiempoNovedadTextView.setVisibility(View.GONE);
                        observacionNovedadTextView.setVisibility(View.GONE);

                        novedadButton.setText(R.string.reportar_novedad);
                        novedadButton.setTextColor(ResourcesCompat.getColor(
                                getResources(), R.color.green, null));
                        novedadButton.setBackground(ResourcesCompat.getDrawable(
                                getResources(), R.drawable.green_bg_bordered_button, null));

                        Snackbar.make(view, value, Snackbar.LENGTH_LONG)
                                .show();
                    }, true);

                    return;
                }

                View form = View.inflate(view.getContext(), R.layout.novedad, null);
                ImageView camera = form.findViewById(R.id.camera);

                ArrayAdapter<Spinner> adapterTypeTime = new ArrayAdapter<>(
                        view.getContext(), android.R.layout.simple_spinner_dropdown_item, spinner);

                AppCompatSpinner novedades = form.findViewById(R.id.novedades);
                novedades.setAdapter(adapterTypeTime);
                novedades.setVisibility(adapterTypeTime.getCount() == 0 ? View.GONE : View.VISIBLE);

                camera.setOnClickListener(self -> {
                    Bundle bundle = new Bundle();
                    bundle.putSparseParcelableArray(GaleriaActivity.PATH_FILE_PARCELABLE, PhotoAdapter.factory(photos));

                    Intent intent = new Intent(getActivity(), GaleriaActivity.class);
                    intent.putExtras(bundle);

                    if (getActivity() != null) {
                        getActivity().startActivityForResult(intent, 1);
                    }
                });


                AlertDialog builder = new AlertDialog.Builder(view.getContext())
                        .setCancelable(false)
                        .setTitle(R.string.reportar_novedad)
                        .setNegativeButton(R.string.cancelar, ((dialog, which) -> dialog.dismiss()))
                        .setPositiveButton(R.string.aceptar, ((dialog, which) -> {
                            TextInputEditText observacionTextInput = form.findViewById(R.id.observacion);

                            Spinner selected = (Spinner) novedades.getSelectedItem();
                            Long idcategoria = selected.getKey() != null && !selected.getKey().isEmpty()
                                    ? Long.valueOf(selected.getKey())
                                    : null;
                            String observacion = observacionTextInput.getText() != null ? observacionTextInput.getText().toString() : "";

                            atNotificationService.cambiarEstado(identidad, codigo, ATNotificationService.OT, ESTADO_ACTUAL, observacion, idcategoria, selected.getValue(), photos, (value, error) -> {

                                if (error) {
                                    onError(new Exception(value));
                                    return;
                                }

                                novedadTextView.setText(selected.getValue());
                                novedadTextView.setVisibility(View.VISIBLE);

                                observacionNovedadTextView.setText(observacion);
                                observacionNovedadTextView.setVisibility(View.VISIBLE);

                                Calendar now = Calendar.getInstance();
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                                        "yyyy-MM-dd HH:mm", Locale.getDefault());
                                fechaNovedadTextView.setText(simpleDateFormat.format(now.getTime()));
                                fechaNovedadTextView.setVisibility(View.VISIBLE);

                                timer(view, now.getTime(), R.id.novedad_tiempo);
                                tiempoNovedadTextView.setText(String.format("%s minutos", 0));
                                tiempoNovedadTextView.setVisibility(View.VISIBLE);

                                onComplete();
                            }, false);
                        }))
                        .setView(form)
                        .create();

                builder.show();
            });

            recalcularButton.setOnClickListener(v -> {
                View form = View.inflate(view.getContext(), R.layout.novedad, null);
                ArrayAdapter<Spinner> adapterTypeTime = new ArrayAdapter<>(
                        view.getContext(), android.R.layout.simple_spinner_dropdown_item, spinnerANS);

                AppCompatSpinner novedades = form.findViewById(R.id.novedades);
                novedades.setAdapter(adapterTypeTime);
                form.findViewById(R.id.camera).setVisibility(View.GONE);
                form.findViewById(R.id.tiempos_ans).setVisibility(View.VISIBLE);

                List<Area> areas = database.where(Area.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findAll();

                List<Spinner> spinnerTipo = new ArrayList<>();
                for (Area area : areas) {
                    for (TypeArea typeArea : area.getTypes()) {
                        spinnerTipo.add(new Spinner(String.valueOf(typeArea.getValue()), typeArea.getLabel()));
                    }
                }

                ArrayAdapter<Spinner> adapterType = new ArrayAdapter<>(
                        view.getContext(), android.R.layout.simple_spinner_dropdown_item, spinnerTipo);

                tipo = form.findViewById(R.id.tipo);
                tipo.setAdapter(adapterType);

                List<Spinner> spinnerPrioridad = new ArrayList<>();
                spinnerPrioridad.add(new Spinner(String.valueOf(1), ATNotificationService.ALTA));
                spinnerPrioridad.add(new Spinner(String.valueOf(2), ATNotificationService.MEDIA));
                spinnerPrioridad.add(new Spinner(String.valueOf(3), ATNotificationService.BAJA));

                ArrayAdapter<Spinner> adapterTypePriority = new ArrayAdapter<>(
                        view.getContext(), android.R.layout.simple_spinner_dropdown_item, spinnerPrioridad);

                prioridad = form.findViewById(R.id.prioridad);
                prioridad.setAdapter(adapterTypePriority);

                Estado estado = database.where(Estado.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .equalTo("id", ESTADO_ACTUAL.getId())
                        .findFirst();

                if (estado != null) {
                    ANS ans = getANS(estado.getEjecucion());

                    for (int i = 0; i < spinnerTipo.size(); i++) {
                        Spinner spinner = spinnerTipo.get(i);
                        if (ans != null) {
                            if (spinner.getValue().equals(ans.getTipo())) {
                                tipo.setSelection(i);
                            }
                        }
                    }

                    for (int i = 0; i < spinnerPrioridad.size(); i++) {
                        Spinner spinner = spinnerPrioridad.get(i);
                        if (ans != null) {
                            if (spinner.getValue().equals(ans.getPrioridad())) {
                                prioridad.setSelection(i);
                            }
                        }
                    }
                }

                AlertDialog builderANS = new AlertDialog.Builder(view.getContext())
                        .setCancelable(false)
                        .setTitle(R.string.recalcular_ans)
                        .setNegativeButton(R.string.cancelar, ((dialog, which) -> dialog.dismiss()))
                        .setPositiveButton(R.string.aceptar, ((dialog, which) -> {
                            TextInputEditText observacionTextInput = form.findViewById(R.id.observacion);

                            Spinner selected = (Spinner) novedades.getSelectedItem();
                            Spinner tipoSS = (Spinner) tipo.getSelectedItem();
                            Spinner prioridadSS = (Spinner) prioridad.getSelectedItem();

                            Long idcategoria = selected != null && selected.getKey() != null && !selected.getKey().isEmpty()
                                    ? Long.valueOf(selected.getKey())
                                    : null;

                            String observacion = observacionTextInput != null && observacionTextInput.getText() != null
                                    ? observacionTextInput.getText().toString()
                                    : "";

                            atNotificationService.saveRecalculoANS(identidad, ATNotificationService.OT, ESTADO_ACTUAL, observacion, idcategoria, tipoSS.getKey(), prioridadSS.getValue(), (value, error) -> {
                                if (error) {
                                    onError(new Exception(value));
                                    return;
                                }

                                Snackbar.make(view, R.string.recalculo_ans_info, Snackbar.LENGTH_LONG)
                                        .show();
                            });
                        }))
                        .setView(form)
                        .create();

                builderANS.show();
            });


            if (ot != null && !ot.getListachequeo().isEmpty())
                view.findViewById(R.id.lc).setVisibility(View.VISIBLE);

            view.findViewById(R.id.lc).setOnClickListener(v -> {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putLong(DescargarRutaTrabajoActivity.ID_EXTRA, identidad);
                    bundle.putBoolean(DescargarRutaTrabajoActivity.MODO_VER_DETALLE, false);
                    bundle.putBoolean(DescargarRutaTrabajoActivity.ACCION_REFRESCAR, false);
                    bundle.putBoolean(DescargarRutaTrabajoActivity.ACCION_PARCIAL, true);

                    Intent intent = new Intent(view.getContext(), DescargarRutaTrabajoActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);

                } catch (Exception e) {
                    Log.e(TAG, "onCreateView: ", e);
                }
            });

            if (UserPermission.check(this.getContext(), UserPermission.REALIZAR_INSTALACION_RETIRO, false)) {
                view.findViewById(R.id.actions).setVisibility(View.VISIBLE);
            }

            view.findViewById(R.id.actions).setOnClickListener(v -> {
                try {
                    Bundle bundleM = new Bundle();
                    bundleM.putLong(com.mantum.component.Mantum.KEY_ID, identidad);
                    if (ESTADO_ACTUAL.is(EN_SITIO) || ESTADO_ACTUAL.is(EN_EJECUCION) || ESTADO_ACTUAL.is(COMPLETAR) || ESTADO_ACTUAL.is(FIN_EJECUCION) || ESTADO_ACTUAL.is(EN_SITIO_AUTOMATICO)) {
                        bundleM.putBoolean("noValidarEntidad", true);
                    }

                    Intent intentM = new Intent(view.getContext(), MovimientoActivity.class);
                    intentM.putExtras(bundleM);
                    startActivity(intentM);

                } catch (Exception e) {
                    Log.e(TAG, "onCreateView: ", e);
                }
            });

            if (estado != null) {
                actionButton.performClick();
                estado = null;
                return view;
            }

            showANS(view, cuenta);
            if (!cuenta.isDisponible()) {
                estadoActual.setText(String.format("%s: %s", getString(R.string.estado_actual),
                        getString(R.string.no_disponible)));
            }

            return view;
        } catch (Exception e) {
            Log.e(TAG, "onCreateView: ", e);
            return null;
        }
    }

    private void procesarEstados(Cuenta cuenta, Button recalcularButton, Button novedadButton) {

        estadoActualPersonal = database.where(Estado.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("id", ESTADO_ACTUAL.getId())
                .findFirst();

        spinner = new ArrayList<>();
        spinnerANS = new ArrayList<>();
        for (EstadoCategoria categoria : estadoActualPersonal.getCategorias()) {
            if (!categoria.isAfectatiempoans())
                spinner.add(new Spinner(String.valueOf(categoria.getId()), categoria.getNombre()));
            else
                spinnerANS.add(new Spinner(String.valueOf(categoria.getId()), categoria.getNombre()));
        }

        if (spinnerANS.size() > 0 && this.ans.size() > 0)
            recalcularButton.setVisibility(View.VISIBLE);

        if (spinner.size() > 0) {
            novedadButton.setVisibility(View.VISIBLE);
            novedadButton.setText(R.string.reportar_novedad);
            novedadButton.setTextColor(ResourcesCompat.getColor(
                    getResources(), R.color.green, null));
            novedadButton.setBackground(ResourcesCompat.getDrawable(
                    getResources(), R.drawable.green_bg_bordered_button, null));
        } else {
            novedadButton.setVisibility(View.GONE);
        }
    }

    @Nullable
    private ANS getANS(int idejecucion) {
        if (ans.isEmpty()) {
            return null;
        }

        for (ANS value : ans) {
            int ejecucionInicial = value.getEjecucioninicial() != null ? value.getEjecucioninicial() : 0;
            int ejecucionFinal = value.getEjecucionfinal() != null ? value.getEjecucionfinal() : 900;

            if (value.getFechafin() == null && idejecucion >= ejecucionInicial && idejecucion <= ejecucionFinal) {
                return value;
            }
        }

        return null;
    }

    private void actualizarANS(@NonNull Cuenta cuenta) {
        int idestado = ESTADO_ACTUAL.getId();

        database.executeTransaction(self -> {
            Estado estado = self.where(Estado.class)
                    .equalTo("id", idestado)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findFirst();

            Log.i(TAG, "Estado actual: " + estado);
            if (estado == null) {
                return;
            }

            OrdenTrabajo results = self.where(OrdenTrabajo.class)
                    .equalTo("id", identidad)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .equalTo("UUID", uuid)
                    .findFirst();

            if (results == null) {
                return;
            }

            for (ANS value : results.getAns()) {
                int ejecucionFinal = value.getEjecucionfinal() != null ? value.getEjecucionfinal() : 900;

                if (value.getFechafin() == null && estado.getEjecucion() >= ejecucionFinal) {
                    Calendar now = Calendar.getInstance();
                    value.setFechafin(now.getTime());

                    self.insertOrUpdate(value);
                }
            }

            ans = self.copyToRealm(results.getAns());
        });
    }

    private void showANS(@NonNull View view, @NonNull Cuenta cuenta) {
        Estado estado = database.where(Estado.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("id", ESTADO_ACTUAL.getId())
                .findFirst();

        TextView titulo = view.findViewById(R.id.ans);
        titulo.setVisibility(View.GONE);

        TextView fecha = view.findViewById(R.id.ans_fecha);
        fecha.setVisibility(View.GONE);

        TextView hora = view.findViewById(R.id.ans_tiempo);
        hora.setVisibility(View.GONE);

        if (estado != null) {
            ANS ans = getANS(estado.getEjecucion());
            if (ans != null) {
                titulo.setText(String.format("%s - %s", getString(R.string.ans), ans.getNombre()));

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                fecha.setText(simpleDateFormat.format(ans.getVencimiento()));

                Calendar now = Calendar.getInstance();
                long diff = ans.getVencimiento().getTime() - now.getTime().getTime();

                String texto = "0 minutos";
                long minutes = (diff / 1000) / 60;
                if (minutes > 0) {
                    texto = "";
                    long hours = minutes / 60;
                    if (hours > 0) {
                        String textoHora = " hora";
                        if (hours > 1) {
                            textoHora = " horas";
                        }
                        texto = hours + textoHora;
                    }

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(diff);
                    minutes = calendar.get(Calendar.MINUTE);
                    if (minutes > 0) {
                        if (texto.isEmpty()) {
                            texto = minutes + " minutos";
                        } else {
                            texto = texto + " y " + minutes + " minutos";
                        }
                    }
                }
                hora.setText(texto);

                titulo.setVisibility(View.VISIBLE);
                fecha.setVisibility(View.VISIBLE);
                hora.setVisibility(View.VISIBLE);

                reduce(view, ans.getVencimiento());
            }
        }
    }

    @Nullable
    private ATNotificationService.Estado siguienteEstado(@NonNull ATNotificationService.Estado estado) {
        if (estado.is(DISPONIBLE)) {
            return ATNotificationService.EN_MOVIMIENTO;
        }

        if (estado.is(ATNotificationService.EN_MOVIMIENTO)) {
            return EN_SITIO;
        }

        if (estado.is(EN_SITIO)) {
            return EN_EJECUCION;
        }

        if (estado.is(EN_EJECUCION)) {
            return COMPLETAR;
        }

        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == BitacoraActivity.REQUEST_ACTION) {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        }
    }


    public void requestEntidadValidar(String code, OrdenTrabajo ot, String tipocode) {
        otRecorrido = ot;
        progressBar.setVisibility(View.VISIBLE);
        compositeDisposable.add(movimientoService.getEntidadValidar(code, ot.getId(), tipocode)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNextEntidadvalidar, this::onErrorEntidadValidar, this::onCompleteGeneral));
        closeDialogGeneral.dismiss();
    }

    public void requestEntidadValidar(String identidad, OrdenTrabajo ot) {
        otRecorrido = ot;
        progressBar.setVisibility(View.VISIBLE);
        compositeDisposable.add(movimientoService.getEntidadValidar(identidad, ot.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNextEntidadvalidar, this::onErrorEntidadValidar, this::onCompleteGeneral));
        closeDialogGeneral.dismiss();
    }

    private void onNextEntidadvalidar(Response response) {
        if (!response.isValid())
            return;

        Long entidad = response.getBody(Movimiento.Request.class).getId();
        if (entidad != null)
            search(entidad, otRecorrido);
    }

    private void onErrorEntidadValidar(Throwable throwable) {
        onCompleteGeneral();
        if (getView() != null && throwable != null) {
            Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private void onCompleteGeneral() {
        progressBar.setVisibility(View.GONE);
    }


    public void search(Long identidad, @Nullable OrdenTrabajo ot) {
        try {
            Entidad eq = ot.getEntidades().where()
                    .equalTo("id", identidad)
                    .findFirst();

            if (eq != null) {
                Realm realm = database.instance();
                realm.executeTransaction(self -> {
                    ot.setEntidadValida(eq.getId());
                });

                Button actionButton = getView().findViewById(R.id.action);
                actionButton.performClick();
                Snackbar.make(getView(), R.string.validar_entidad_movimiento_ok, Snackbar.LENGTH_LONG)
                        .show();
            } else {
                Snackbar.make(getView(), R.string.validar_entidad_movimiento_error, Snackbar.LENGTH_LONG)
                        .setDuration(TIME_ALERT)
                        .show();
            }
        } catch (Exception e) {
            Log.d(TAG, "search: " + e);
        }

        nfcActive = false;
    }

    private void onError(Throwable throwable) {
        if (getView() == null) {
            return;
        }

        isNovedad = false;
        Button novedadButton = getView().findViewById(R.id.novedad);
        novedadButton.setText(R.string.reportar_novedad);
        novedadButton.setTextColor(ResourcesCompat.getColor(
                getResources(), R.color.green, null));
        novedadButton.setBackground(ResourcesCompat.getDrawable(
                getResources(), R.drawable.green_bg_bordered_button, null));

        Snackbar.make(getView(), R.string.reportar_novedad_error, Snackbar.LENGTH_LONG)
                .show();
    }

    private void onComplete() {
        if (getView() == null) {
            return;
        }

        isNovedad = true;
        Button novedadButton = getView().findViewById(R.id.novedad);
        novedadButton.setText(R.string.detener_novedad);
        novedadButton.setTextColor(ResourcesCompat.getColor(
                getResources(), R.color.red, null));
        novedadButton.setBackground(ResourcesCompat.getDrawable(
                getResources(), R.drawable.red_bg_bordered_button, null));

        Snackbar.make(getView(), R.string.reportar_novedad_exitosa, Snackbar.LENGTH_LONG)
                .show();
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

    private void prepare(@NonNull View view, @NonNull ATNotificationService.Estado estado) {
        ESTADO_ACTUAL = estado;
        Button actionButton = view.findViewById(R.id.action);
        TextView estadoTextView = view.findViewById(R.id.estado_actual);

        if (estado.is(DISPONIBLE)) {
            estadoTextView.setText(String.format("%s: %s", getString(R.string.estado_actual), getString(R.string.disponible)));
            actionButton.setText(R.string.en_camino);
        } else if (estado.is(ATNotificationService.EN_MOVIMIENTO)) {
            estadoTextView.setText(String.format("%s: %s", getString(R.string.estado_actual), getString(R.string.en_camino)));
            actionButton.setText(R.string.en_sitio);
        } else if (estado.is(EN_SITIO)) {
            estadoTextView.setText(String.format("%s: %s", getString(R.string.estado_actual), getString(R.string.en_sitio)));
            actionButton.setText(R.string.iniciar);

            actionButton.setTextColor(ResourcesCompat.getColor(
                    getResources(), R.color.green, null));
            actionButton.setBackground(ResourcesCompat.getDrawable(
                    getResources(), R.drawable.green_bg_bordered_button, null));
        } else if (estado.is(EN_EJECUCION)) {
            estadoTextView.setText(String.format("%s: %s", getString(R.string.estado_actual), getString(R.string.iniciar)));
            actionButton.setText(R.string.completar);

            actionButton.setTextColor(ResourcesCompat.getColor(
                    getResources(), R.color.green, null));
            actionButton.setBackground(ResourcesCompat.getDrawable(
                    getResources(), R.drawable.green_bg_bordered_button, null));
        } else if (estado.is(COMPLETAR)) {
            estadoTextView.setText(String.format("%s: %s", getString(R.string.estado_actual), getString(R.string.iniciar)));
            actionButton.setText(R.string.completar);

            actionButton.setTextColor(ResourcesCompat.getColor(
                    getResources(), R.color.green, null));
            actionButton.setBackground(ResourcesCompat.getDrawable(
                    getResources(), R.drawable.green_bg_bordered_button, null));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (atNotificationService != null) {
            atNotificationService.close();
        }

        if (recorridoService != null) {
            recorridoService.close();
        }

        if (database != null) {
            database.close();
        }

        if (recorridoHistoricoService != null) {
            recorridoHistoricoService.close();
        }
    }

    private void timer(@NonNull View view, @NonNull Date fechaInicio, @IdRes int id) {
        if (handlerTimer != null) {
            handlerTimer.removeCallbacksAndMessages(null);
        }

        handlerTimer = new Handler();
        handlerTimer.postDelayed(new Runnable() {

            @Override
            public void run() {
                Calendar now = Calendar.getInstance();
                long diff = now.getTime().getTime() - fechaInicio.getTime();
                long minutes = (diff / 1000) / 60;

                TextView estadoTiempoTextView = view.findViewById(id);
                estadoTiempoTextView.setText(String.format("%s minutos", minutes));
                estadoTiempoTextView.setVisibility(View.VISIBLE);

                handlerTimer.postDelayed(this, 60000);
            }
        }, 60000);
    }

    private void reduce(@NonNull View view, @NonNull Date fecha) {
        if (handlerReduce != null) {
            handlerReduce.removeCallbacksAndMessages(null);
        }

        handlerReduce = new Handler();
        handlerReduce.postDelayed(new Runnable() {

            @Override
            public void run() {
                Calendar now = Calendar.getInstance();
                long diff = fecha.getTime() - now.getTime().getTime();

                TextView estadoTiempoTextView = view.findViewById(R.id.ans_tiempo);

                String texto = "0 minutos";
                long minutes = (diff / 1000) / 60;
                if (minutes > 0) {
                    texto = "";
                    long hours = minutes / 60;
                    if (hours > 0) {
                        String textoHora = " hora";
                        if (hours > 1) {
                            textoHora = " horas";
                        }
                        texto = hours + textoHora;
                    }

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(diff);
                    minutes = calendar.get(Calendar.MINUTE);
                    if (minutes > 0) {
                        if (texto.isEmpty()) {
                            texto = minutes + " minutos";
                        } else {
                            texto = texto + " y " + minutes + " minutos";
                        }
                    }
                }

                estadoTiempoTextView.setText(texto);
                estadoTiempoTextView.setVisibility(View.VISIBLE);

                handlerReduce.postDelayed(this, 60000);
            }
        }, 60000);
    }

    public void onCameraResult(@NonNull Context context, @NonNull SparseArray<PhotoAdapter> sparseArray) {
        photos.clear();

        int total = sparseArray.size();
        if (total == 0) {
            return;
        }

        for (int i = 0; i < total; i++) {
            PhotoAdapter photoAdapter = sparseArray.get(i);
            photos.add(new Photo(context, new File(photoAdapter.getPath()),
                    photoAdapter.isDefaultImage(), photoAdapter.getIdCategory(),
                    photoAdapter.getDescription()));
        }
    }

    private boolean requestPermission(@NonNull Context context) {
        if (ActivityCompat.checkSelfPermission(context, CALL_PHONE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{CALL_PHONE}, CALL_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    public void setUUID(String value) {
        this.uuid = value;
    }

    public void setNFCActived(NfcAdapter getNfcAdapter) {
        this.activeNFC = getNfcAdapter;
    }
}