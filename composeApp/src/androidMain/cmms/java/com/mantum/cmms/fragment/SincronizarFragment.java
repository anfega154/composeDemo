package com.mantum.cmms.fragment;

import static com.mantum.cmms.entity.Transaccion.ACCION_ACEPTAR_TRANSFERENCIA;
import static com.mantum.cmms.entity.Transaccion.MODULO_ESTADO_USUARIO;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.mantum.demo.R;
import com.mantum.cmms.activity.BitacoraActivity;
import com.mantum.cmms.activity.CambioEstadoActivity;
import com.mantum.cmms.activity.DiligenciarRutaTrabajoActivity;
import com.mantum.cmms.activity.EvaluarActivity;
import com.mantum.cmms.activity.FirmaxEntidadActivity;
import com.mantum.cmms.activity.FormularioEquipoActivity;
import com.mantum.cmms.activity.FormularioFallaEquipoActivity;
import com.mantum.cmms.activity.FormularioFallaInstalacionLocativaActivity;
import com.mantum.cmms.activity.FormularioFallaOTActivity;
import com.mantum.cmms.activity.InformeTecnicoActivity;
import com.mantum.cmms.activity.InspeccionRegistroEIRActivity;
import com.mantum.cmms.activity.InspeccionRegistroPTIActivity;
import com.mantum.cmms.activity.InstalacionPlantaExternaActivity;
import com.mantum.cmms.activity.RecorridoPlantaExternaActivity;
import com.mantum.cmms.activity.SolicitudServicioActivity;
import com.mantum.cmms.activity.TransaccionDetalleActivity;
import com.mantum.cmms.activity.TrasladoAlmacenActivity;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Diligenciar;
import com.mantum.cmms.entity.Archivos;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.RutaTrabajo;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.service.SendEmailService;
import com.mantum.cmms.task.TransaccionTask;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.OnDrawable;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.InformationAdapter;

import java.util.List;

import io.realm.RealmResults;

public abstract class SincronizarFragment extends Mantum.Fragment {

    private final String key;

    private Database database;

    protected OnCompleteListener onCompleteListener;

    protected InformationAdapter<Transaccion, Archivos> informationAdapter;

    public SincronizarFragment(String key) {
        this.key = key;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(com.mantum.demo.R.layout.information_layout_view,
                container, false);

        database = new Database(view.getContext());

        informationAdapter = new InformationAdapter<>(view.getContext());
        informationAdapter.setDrawable(value -> R.drawable.synchronization);
        informationAdapter.setMenu(R.menu.menu_transaccion);
        informationAdapter.setOnCall((menu, value) -> {
            final String uuid = value.getUUID();

            switch (menu.getItemId()) {

                case R.id.detail:
                    detail(view, value);
                    break;

                case R.id.refresh:
                    if (!Transaccion.ESTADO_ERROR.equals(value.getEstado())) {
                        Snackbar.make(view, R.string.reintentar_estado_sincronizar, Snackbar.LENGTH_LONG)
                                .show();
                        break;
                    }

                    database.executeTransactionAsync(self -> {
                        Transaccion transaccion = self.where(Transaccion.class)
                                .equalTo("UUID", uuid)
                                .findFirst();

                        if (transaccion != null) {
                            transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);
                            transaccion.setMessage(" ");
                        }

                    }, () -> {
                        informationAdapter.remove(value, true);
                        showMessageEmpty();
                    });

                    break;

                case R.id.edit:
                    if (!Transaccion.ESTADO_ERROR.equals(value.getEstado())) {
                        Snackbar.make(view, R.string.editar_estado_sincronizar, Snackbar.LENGTH_LONG)
                                .show();
                        return false;
                    }

                    Intent intent;
                    switch (value.getAccion()) {
                        case Transaccion.ACCION_REGISTRAR_PTI: {
                            intent = new Intent(getActivity(), InspeccionRegistroPTIActivity.class);
                            intent.putExtra(InspeccionRegistroPTIActivity.UUID_TRANSACCION, value.getUUID());
                            intent.putExtra(InspeccionRegistroPTIActivity.MODE_EDIT, value.getValue());

                            if (getActivity() != null) {
                                getActivity().startActivityForResult(intent, 1);
                            }
                            break;
                        }

                        case Transaccion.ACCION_REGISTRAR_EIR: {
                            intent = new Intent(getActivity(), InspeccionRegistroEIRActivity.class);
                            intent.putExtra(InspeccionRegistroPTIActivity.UUID_TRANSACCION, value.getUUID());
                            intent.putExtra(InspeccionRegistroPTIActivity.MODE_EDIT, value.getValue());

                            if (getActivity() != null) {
                                getActivity().startActivityForResult(intent, 1);
                            }
                            break;
                        }

                        case Transaccion.ACCION_CREAR_SOLICITUD_SERVICIO:
                            intent = new Intent(getActivity(), SolicitudServicioActivity.class);

                            intent.putExtra(SolicitudServicioActivity.UUID_TRANSACCION, value.getUUID());
                            intent.putExtra(SolicitudServicioActivity.MODE_EDIT, value.getValue());

                            if (getActivity() != null) {
                                getActivity().startActivityForResult(intent, 1);
                            }
                            break;

                        case Transaccion.ACCION_EVALUAR_SOLICITUD_SERVICIO:
                            intent = new Intent(getActivity(), EvaluarActivity.class);

                            intent.putExtra(EvaluarActivity.UUID_TRANSACCION, value.getUUID());
                            intent.putExtra(EvaluarActivity.MODE_EDIT, value.getValue());

                            if (getActivity() != null) {
                                getActivity().startActivityForResult(intent, 1);
                            }
                            break;

                        case Transaccion.ACCION_REGISTRAR_BITACORA_EVENTO:
                            intent = new Intent(getActivity(), BitacoraActivity.class);

                            intent.putExtra(BitacoraActivity.UUID_TRANSACCION, value.getUUID());
                            intent.putExtra(BitacoraActivity.MODE_EDIT, value.getValue());
                            intent.putExtra(BitacoraActivity.KEY_TIPO_BITACORA, BitacoraActivity.EVENT);

                            if (getActivity() != null) {
                                getActivity().startActivityForResult(intent, 1);
                            }
                            break;

                        case Transaccion.ACCION_REGISTRAR_BITACORA_OT:
                            intent = new Intent(getActivity(), BitacoraActivity.class);

                            intent.putExtra(BitacoraActivity.UUID_TRANSACCION, value.getUUID());
                            intent.putExtra(BitacoraActivity.MODE_EDIT, value.getValue());
                            intent.putExtra(BitacoraActivity.KEY_TIPO_BITACORA, BitacoraActivity.OT);

                            if (getActivity() != null) {
                                getActivity().startActivityForResult(intent, 1);
                            }
                            break;

                        case Transaccion.ACCION_REGISTRAR_BITACORA_SS:
                            intent = new Intent(getActivity(), BitacoraActivity.class);

                            intent.putExtra(BitacoraActivity.UUID_TRANSACCION, value.getUUID());
                            intent.putExtra(BitacoraActivity.MODE_EDIT, value.getValue());
                            intent.putExtra(BitacoraActivity.KEY_TIPO_BITACORA, BitacoraActivity.SS);

                            if (getActivity() != null) {
                                getActivity().startActivityForResult(intent, 1);
                            }
                            break;

                        case Transaccion.ACCION_REGISTRAR_OT_BITACORA:
                            intent = new Intent(getActivity(), BitacoraActivity.class);

                            intent.putExtra(BitacoraActivity.UUID_TRANSACCION, value.getUUID());
                            intent.putExtra(BitacoraActivity.MODE_EDIT, value.getValue());
                            intent.putExtra(BitacoraActivity.KEY_TIPO_BITACORA, BitacoraActivity.OT_BITACORA);

                            if (getActivity() != null) {
                                getActivity().startActivityForResult(intent, 1);
                            }
                            break;

                        case Transaccion.ACCION_COMENTAR_SOLICITUD_SERVICIO:
                            Snackbar.make(view, R.string.editar_comentario_sincronizar, Snackbar.LENGTH_LONG)
                                    .show();
                            break;

                        case Transaccion.ACCION_UBICACION:
                            Snackbar.make(view, R.string.editar_coordenadas_sincronizar, Snackbar.LENGTH_LONG)
                                    .show();
                            break;


                        case Transaccion.ACCION_DILIGENCIAR_RUTA_TRABAJO:
                            intent = new Intent(getActivity(), DiligenciarRutaTrabajoActivity.class);
                            intent.putExtra(DiligenciarRutaTrabajoActivity.UUID_TRANSACCION, value.getUUID());
                            intent.putExtra(DiligenciarRutaTrabajoActivity.MODE_EDIT, value.getValue());

                            if (getActivity() != null) {
                                getActivity().startActivityForResult(intent, 1);
                            }

                            break;

                        case Transaccion.ACCION_CREAR_ACTIVO:
                            intent = new Intent(getActivity(), FormularioEquipoActivity.class);
                            intent.putExtra(FormularioEquipoActivity.UUID_TRANSACCION, value.getUUID());
                            intent.putExtra(FormularioEquipoActivity.MODE_EDIT, value.getValue());
                            intent.putExtra("crearEquipo", true);

                            if (getActivity() != null) {
                                getActivity().startActivityForResult(intent, 1);
                            }

                            break;

                        case Transaccion.ACCION_EDITAR_ACTIVO:
                            intent = new Intent(getActivity(), FormularioEquipoActivity.class);
                            intent.putExtra(FormularioEquipoActivity.UUID_TRANSACCION, value.getUUID());
                            intent.putExtra(FormularioEquipoActivity.MODE_EDIT, value.getValue());
                            intent.putExtra("crearEquipo", false);

                            if (getActivity() != null) {
                                getActivity().startActivityForResult(intent, 1);
                            }
                            break;

                        case Transaccion.ACCION_CREAR_FALLA_OT:
                            intent = new Intent(getActivity(), FormularioFallaOTActivity.class);
                            intent.putExtra(FormularioFallaOTActivity.UUID_TRANSACCION, value.getUUID());
                            intent.putExtra(FormularioFallaOTActivity.MODE_EDIT, value.getValue());

                            if (getActivity() != null) {
                                getActivity().startActivityForResult(intent, 1);
                            }
                            break;

                        case Transaccion.ACCION_CREAR_FALLA_EQUIPO:
                            intent = new Intent(getActivity(), FormularioFallaEquipoActivity.class);
                            intent.putExtra(FormularioFallaEquipoActivity.UUID_TRANSACCION, value.getUUID());
                            intent.putExtra(FormularioFallaEquipoActivity.MODE_EDIT, value.getValue());

                            if (getActivity() != null) {
                                getActivity().startActivityForResult(intent, 1);
                            }
                            break;

                        case Transaccion.ACCION_CREAR_FALLA_INSTALACION_LOCATIVA:
                            intent = new Intent(getActivity(), FormularioFallaInstalacionLocativaActivity.class);
                            intent.putExtra(FormularioFallaInstalacionLocativaActivity.UUID_TRANSACCION, value.getUUID());
                            intent.putExtra(FormularioFallaInstalacionLocativaActivity.MODE_EDIT, value.getValue());

                            if (getActivity() != null) {
                                getActivity().startActivityForResult(intent, 1);
                            }
                            break;

                        case Transaccion.ACCION_ESTADO_USUARIO:
                            intent = new Intent(getActivity(), CambioEstadoActivity.class);
                            intent.putExtra(CambioEstadoActivity.UUID_TRANSACCION, value.getUUID());
                            intent.putExtra(CambioEstadoActivity.MODE_EDIT, value.getValue());

                            if (getActivity() != null) {
                                getActivity().startActivityForResult(intent, 1);
                            }

                            break;

                        default:
                            Snackbar.make(view, R.string.editar_elemento, Snackbar.LENGTH_LONG)
                                    .show();
                    }

                    break;

                case R.id.remove:
                    remove(value);
                    break;

                case R.id.sincronizar:
                    Transaccion transaccion = database.where(Transaccion.class)
                            .equalTo("UUID", uuid)
                            .findFirst();

                    if (transaccion == null) {
                        Snackbar.make(view, R.string.transaccion_empty, Snackbar.LENGTH_LONG)
                                .show();
                        break;
                    }

                    if (!Transaccion.ESTADO_PENDIENTE.equals(transaccion.getEstado())) {
                        Snackbar.make(view, R.string.transaccion_no_pendiente, Snackbar.LENGTH_LONG)
                                .show();
                        break;
                    }

                    Cuenta cuenta = database.where(Cuenta.class)
                            .equalTo("active", true)
                            .findFirst();

                    if (cuenta == null) {
                        break;
                    }

                    List<Transaccion> sincronizandoEstados = database.where(Transaccion.class)
                            .equalTo("cuenta.UUID", cuenta.getUUID())
                            .equalTo("estado", Transaccion.ESTADO_SINCRONIZANDO)
                            .equalTo("modulo", MODULO_ESTADO_USUARIO)
                            .findAll();

                    if (sincronizandoEstados.isEmpty()) {
                        Transaccion element = transaccion.isManaged()
                                ? database.copyFromRealm(transaccion)
                                : transaccion;

                        TransaccionTask.Task task = new TransaccionTask.Task(view.getContext());
                        database.executeTransactionAsync((self) -> task.prepare(self, element));
                    } else {
                        Toast.makeText(view.getContext(), R.string.estado_sincronizando, Toast.LENGTH_LONG)
                                .show();
                    }
                    break;

                case R.id.sendemail:
                    cuenta = database.where(Cuenta.class)
                            .equalTo("active", true)
                            .findFirst();

                    Transaccion transaccion1 = database.where(Transaccion.class)
                            .equalTo("UUID", uuid)
                            .findFirst();

                    if (cuenta == null) {
                        break;
                    }

                    if (transaccion1 == null) {
                        Snackbar.make(view, R.string.transaccion_empty, Snackbar.LENGTH_LONG)
                                .show();
                        break;
                    }

                    SendEmailService.shareTransactionDetail(getContext(), cuenta, value);
                    break;
            }
            return super.onOptionsItemSelected(menu);
        });

        informationAdapter.setOnAction(new OnSelected<Transaccion>() {

            @Override
            public void onClick(Transaccion value, int position) {
                detail(view, value);
            }

            @Override
            public boolean onLongClick(Transaccion value, int position) {
                remove(value);
                return true;
            }

        });

        informationAdapter.startAdapter(view, new LinearLayoutManager(view.getContext()));
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setEnabled(false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onCompleteListener.onComplete(key);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        onCompleteListener = (OnCompleteListener) context;
    }

    public void onRefresh(@NonNull List<Transaccion> values) {
        if (informationAdapter != null) {
            informationAdapter.addAll(values);
        }
        showMessageEmpty();
    }

    public void clear() {
        if (informationAdapter != null) {
            informationAdapter.clear();
            informationAdapter.notifyDataSetChanged();
        }
    }

    private void remove(Transaccion value) {
        if (getView() == null) {
            return;
        }

        AlertDialog.Builder alertDialogBuilder
                = new AlertDialog.Builder(getView().getContext());
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setTitle(R.string.eliminar_transaccion_titulo);
        alertDialogBuilder.setMessage(R.string.eliminar_transaccion);
        alertDialogBuilder.setNegativeButton(getString(R.string.aceptar), (dialogInterface, i) -> {
            switch (value.getAccion()) {
                case Transaccion.ACCION_DILIGENCIAR_RUTA_TRABAJO:
                    Diligenciar diligenciar
                            = new Gson().fromJson(value.getValue(), Diligenciar.class);

                    database.executeTransactionAsync(self -> {
                        Cuenta cuenta = self.where(Cuenta.class)
                                .equalTo("active", true)
                                .findFirst();

                        if (cuenta == null) {
                            return;
                        }

                        RutaTrabajo rutaTrabajo = self.where(RutaTrabajo.class)
                                .equalTo("cuenta.UUID", cuenta.getUUID())
                                .equalTo("idejecucion", diligenciar.getIdejecucion())
                                .findFirst();

                        if (rutaTrabajo != null) {
                            rutaTrabajo.setShow(true);
                        }

                    });
                    break;
            }

            database.executeTransactionAsync(self -> {
                RealmResults<Transaccion> results = self.where(Transaccion.class)
                        .equalTo("UUID", value.getUUID())
                        .findAll();

                if (results != null) {
                    results.deleteFirstFromRealm();
                }
            }, () -> {
                informationAdapter.remove(value, true);
                showMessageEmpty();
            });

            dialogInterface.cancel();

        });

        alertDialogBuilder.setPositiveButton(getString(R.string.cancelar), (dialogInterface, i) -> dialogInterface.cancel());
        alertDialogBuilder.show();
    }

    private void detail(@NonNull View view, @NonNull Transaccion value) {
        Intent intent;
        switch (value.getAccion()) {

            case Transaccion.ACCION_REGISTRAR_LISTA_CHEQUEO: {
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);
                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_REGISTRAR_LISTA_CHEQUEO);

                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, 1);
                }
                break;
            }

            case Transaccion.ACCION_REGISTRAR_EIR: {
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_REGISTRAR_EIR);

                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, 1);
                }
                break;
            }

            case Transaccion.ACCION_REGISTRAR_PTI:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_REGISTRAR_PTI);

                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, 1);
                }
                break;

            case Transaccion.ACCION_UBICACION:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_UBICACION);

                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, 1);
                }
                break;

            case Transaccion.ACCION_REGISTRAR_BITACORA_EVENTO:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_REGISTRAR_BITACORA_EVENTO);

                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, BitacoraEventoFragment.REQUEST_ACTION);
                }
                break;

            case Transaccion.ACCION_REGISTRAR_BITACORA_OT:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_REGISTRAR_BITACORA_OT);

                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, BitacoraOrdenTrabajoFragment.REQUEST_ACTION);
                }
                break;

            case Transaccion.ACCION_REGISTRAR_BITACORA_SS:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_REGISTRAR_BITACORA_SS);

                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, BitacoraSolicitudServicioFragment.REQUEST_ACTION);
                }
                break;

            case Transaccion.ACCION_REGISTRAR_OT_BITACORA:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_REGISTRAR_OT_BITACORA);

                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, BitacoraOTFragment.REQUEST_ACTION);
                }
                break;

            case Transaccion.ACCION_DILIGENCIAR_RUTA_TRABAJO:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_DILIGENCIAR_RUTA_TRABAJO);

                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, RutaTrabajoDiligenciarFragment.REQUEST_ACTION);
                }
                break;

            case Transaccion.ACCION_TERMINAR_ORDEN_TRABAJO:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_TERMINAR_ORDEN_TRABAJO);

                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, OrdenTrabajoTerminarFragment.REQUEST_ACTION);
                }
                break;

            case Transaccion.ACCION_ESTADO_INICIAL:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_ESTADO_INICIAL);

                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, EstadoInicialFragment.REQUEST_ACTION);
                }
                break;

            case Transaccion.ACCION_INFORME_TECNICO:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_INFORME_TECNICO);

                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, InformeTecnicoActivity.REQUEST_ACTION);
                }

                break;

            case Transaccion.ACCION_RECORRIDO_PLANTA_EXTERNA:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_RECORRIDO_PLANTA_EXTERNA);

                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, RecorridoPlantaExternaActivity.REQUEST_ACTION);
                }
                break;

            case Transaccion.ACCION_INSTALACION_PLANTA_EXTERNA:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_INSTALACION_PLANTA_EXTERNA);

                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, InstalacionPlantaExternaActivity.REQUEST_ACTION);
                }
                break;

            case Transaccion.ACCION_CREAR_SOLICITUD_SERVICIO:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_CREAR_SOLICITUD_SERVICIO);

                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, InstalacionPlantaExternaActivity.REQUEST_ACTION);
                }
                break;

            case Transaccion.ACCION_COMENTAR_SOLICITUD_SERVICIO:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_COMENTAR_SOLICITUD_SERVICIO);

                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, InstalacionPlantaExternaActivity.REQUEST_ACTION);
                }
                break;

            case Transaccion.ACCION_RECIBIR_SOLICITUD_SERVICIO:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);
                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_RECIBIR_SOLICITUD_SERVICIO);
                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, 0);
                }
                break;

            case Transaccion.ACCION_FIRMA_X_ENTIDAD:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_FIRMA_X_ENTIDAD);

                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, FirmaxEntidadActivity.REQUEST_ACTION);
                }
                break;

            case Transaccion.ACCION_ESTADO_USUARIO:
            case Transaccion.ACCION_ANS_OT:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);
                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_ESTADO_USUARIO);
                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, 0);
                }
                break;

            case Transaccion.ACCION_TRASLADO_ALMACEN:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_TRASLADO_ALMACEN);

                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, TrasladoAlmacenActivity.REQUEST_ACTION);
                }
                break;

            case Transaccion.ACCION_MOVIMIENTO:
            case Transaccion.ACCION_ACEPTAR_TRANSFERENCIA:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_MOVIMIENTO);
                break;

            case Transaccion.ACCION_REGISTRAR_LECTURAS_VARIABLES:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_REGISTRAR_LECTURAS_VARIABLES);

                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, 0);
                }
                break;

            case Transaccion.ACCION_CREAR_ACTIVO:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_CREAR_ACTIVO);

                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, 0);
                }
                break;

            case Transaccion.ACCION_EDITAR_ACTIVO:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_EDITAR_ACTIVO);

                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, 0);
                }
                break;

            case Transaccion.ACCION_CREAR_FALLA_OT:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_CREAR_FALLA_OT);

                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, 0);
                }
                break;

            case Transaccion.ACCION_CREAR_FALLA_EQUIPO:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_CREAR_FALLA_EQUIPO);

                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, 0);
                }
                break;

            case Transaccion.ACCION_CREAR_FALLA_INSTALACION_LOCATIVA:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_CREAR_FALLA_INSTALACION_LOCATIVA);

                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, 0);
                }
                break;

            case Transaccion.ACCION_ENVIAR_CORREO:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);

                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_ENVIAR_CORREO);

                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, 0);
                }
                break;

            case Transaccion.ACCION_ASOCIAR_CODIGO_QR_BARRAS_EQUIPO:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);
                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_ASOCIAR_CODIGO_QR_BARRAS_EQUIPO);
                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, 0);
                }
                break;

            case Transaccion.ACCION_ASOCIAR_CODIGO_QR_BARRAS_INSTALACION_LOCATIVA:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);
                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_ASOCIAR_CODIGO_QR_BARRAS_INSTALACION_LOCATIVA);
                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, 0);
                }
                break;

            case Transaccion.ACCION_RECIBIR_ORDEN_TRABAJO:
                intent = new Intent(getActivity(), TransaccionDetalleActivity.class);
                intent.putExtra(Mantum.KEY_ID, value.getUUID());
                intent.putExtra(TransaccionDetalleActivity.KEY_TYPE, Transaccion.ACCION_RECIBIR_ORDEN_TRABAJO);
                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, 0);
                }
                break;

            default:
                Snackbar.make(view, R.string.ver_elemento, Snackbar.LENGTH_LONG)
                        .show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        database.close();
        informationAdapter.clear();
    }

    protected abstract void showMessageEmpty();
}