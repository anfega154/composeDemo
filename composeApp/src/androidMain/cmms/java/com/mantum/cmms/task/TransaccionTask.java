package com.mantum.cmms.task;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;

import com.google.gson.Gson;
import com.mantum.R;
import com.mantum.cmms.activity.ConfiguracionActivity;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Archivo;
import com.mantum.cmms.domain.BitacoraEvento;
import com.mantum.cmms.domain.BitacoraOT;
import com.mantum.cmms.domain.BitacoraOrdenTrabajo;
import com.mantum.cmms.domain.BitacoraSolicitudServicio;
import com.mantum.cmms.domain.Comentar;
import com.mantum.cmms.domain.Contacto;
import com.mantum.cmms.domain.Diligenciar;
import com.mantum.cmms.domain.FirmaxEntidad;
import com.mantum.cmms.domain.Novedad;
import com.mantum.cmms.domain.Recibir;
import com.mantum.cmms.domain.RecibirOT;
import com.mantum.cmms.domain.Response;
import com.mantum.cmms.domain.SolicitudServicioRegistrar;
import com.mantum.cmms.domain.Terminar;
import com.mantum.cmms.domain.Transferencia;
import com.mantum.cmms.entity.Adjuntos;
import com.mantum.cmms.entity.Asignada;
import com.mantum.cmms.entity.Contenedor;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Falla;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.service.ActivoService;
import com.mantum.cmms.entity.parameter.UserParameter;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.cmms.helper.ImagenesCorreoHelper;
import com.mantum.cmms.service.ActualizarContactoService;
import com.mantum.cmms.service.ArchivoService;
import com.mantum.cmms.service.AutorizacionAccesoService;
import com.mantum.cmms.service.BitacoraService;
import com.mantum.cmms.service.EstadoUsuarioService;
import com.mantum.cmms.service.FallaService;
import com.mantum.cmms.service.FirmaxEntidadService;
import com.mantum.cmms.service.GeolocalizacionService;
import com.mantum.cmms.service.ImagenesCorreoService;
import com.mantum.cmms.service.InspeccionElectricaService;
import com.mantum.cmms.service.InspeccionService;
import com.mantum.cmms.service.InstalacionPlatanExternaService;
import com.mantum.cmms.service.InventarioActivosService;
import com.mantum.cmms.service.LecturaService;
import com.mantum.cmms.service.ListaChequeoService;
import com.mantum.cmms.service.MarcacionService;
import com.mantum.cmms.service.MovimientoService;
import com.mantum.cmms.service.OrdenTrabajoService;
import com.mantum.cmms.service.RecorridoPlantaExternaService;
import com.mantum.cmms.service.RegistrarTiempoService;
import com.mantum.cmms.service.RutaTrabajoService;
import com.mantum.cmms.service.SolicitudServicioService;
import com.mantum.cmms.service.TransferenciaService;
import com.mantum.cmms.service.TrasladoAlmacenService;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.service.Notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.mantum.cmms.entity.Transaccion.ACCION_TERMINAR_ORDEN_TRABAJO;
import static com.mantum.cmms.entity.Transaccion.MODULO_BITACORA;
import static com.mantum.cmms.entity.Transaccion.MODULO_ESTADO_USUARIO;

public class TransaccionTask extends Service {

    private static final String V11 = "11";

    private static final String V011 = "011";

    private static int PERIOD = 30000;

    private static final int DELAY_MILLIS = 2000;

    private static final String TAG = TransaccionTask.class.getSimpleName();

    private Timer timer = new Timer();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if ((UserPermission.check(this, UserPermission.EXTENDER_TIEMPO_SINCRONIZACION_AUTOMATICA, false))
                && (UserParameter.getValue(getApplicationContext(), UserParameter.TIEMPO_PERSONALIZADO_ENVIO_TRANSACCIONES) != null)) {
            PERIOD = Integer.parseInt(UserParameter.getValue(getApplicationContext(), UserParameter.TIEMPO_PERSONALIZADO_ENVIO_TRANSACCIONES)) * 60000;
        }

        timer = timer != null ? timer : new Timer();
        timer.scheduleAtFixedRate(new TransaccionTask.Task(this), 0, PERIOD);
    }

    public static class Task extends TimerTask {

        private final Context context;

        private final Handler handler = new Handler();

        private final SharedPreferences sharedPreferences;

        private final CompositeDisposable compositeDisposable = new CompositeDisposable();

        public Task(@NonNull Context context) {
            this.context = context;
            this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }

        @Override
        public void run() {
            try {
                if (!Mantum.isConnectedOrConnecting(context)) {
                    Log.e(TAG, "run: No tiene una conexión a internet");
                    return;
                }

                handler.postDelayed(() -> process(new Database(context)), DELAY_MILLIS);
            } catch (Exception ignored) {
            }
        }

        @Nullable
        private Calendar getStartDate() {
            try {
                Calendar start = Calendar.getInstance();
                SimpleDateFormat formatter = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                Date time = formatter.parse("1900-01-01 00:00:00");
                if (time != null) {
                    start.setTime(time);
                }

                return start;
            } catch (Exception e) {
                Log.e(TAG, "getStartDate: ", e);
                return null;
            }
        }

        public void process(@NonNull Database database) {
            if (database.isClosed()) {
                return;
            }

            database.executeTransactionAsync(self -> {
                SharedPreferences sharedPreferences
                        = PreferenceManager.getDefaultSharedPreferences(context);

                String amount = sharedPreferences.getString(
                        ConfiguracionActivity.PREFERENCIA_SINCRONIZAR_REMOVE, "1");
                amount = "0".equals(amount) ? "1" : amount;
                if (amount == null) {
                    amount = "1";
                }

                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MONTH, -Integer.parseInt(amount));

                Calendar start = getStartDate();
                if (start != null) {
                    self.where(Transaccion.class)
                            .equalTo("estado", Transaccion.ESTADO_SINCRONIZADO)
                            .between("send", start.getTime(), calendar.getTime())
                            .findAll()
                            .deleteAllFromRealm();

                    Calendar end = Calendar.getInstance();
                    end.add(Calendar.MINUTE, -10);

                    List<Transaccion> transactions = self.where(Transaccion.class)
                            .equalTo("estado", Transaccion.ESTADO_SINCRONIZANDO)
                            .between("send", start.getTime(), end.getTime())
                            .findAll();

                    for (Transaccion transaccion : transactions) {
                        transaccion.setSend(null);
                        transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);
                    }
                }

                Cuenta cuenta = self.where(Cuenta.class)
                        .equalTo("active", true)
                        .findFirst();

                if (cuenta == null) {
                    return;
                }

                RealmQuery<Transaccion> query = self.where(Transaccion.class)
                        .equalTo("estado", Transaccion.ESTADO_PENDIENTE)
                        .equalTo("cuenta.UUID", cuenta.getUUID());

                List<Transaccion> estadosErroneos = self.where(Transaccion.class)
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .equalTo("estado", Transaccion.ESTADO_ERROR)
                        .equalTo("modulo", MODULO_ESTADO_USUARIO)
                        .findAll();

                Transaccion transaccion;
                if (estadosErroneos.isEmpty()) {
                    transaccion = query.equalTo("show", true)
                            .sort("creation", Sort.ASCENDING)
                            .sort("prioridad", Sort.DESCENDING)
                            .findFirst();
                } else {
                    transaccion = query.equalTo("show", true)
                            .sort("creation", Sort.ASCENDING)
                            .sort("prioridad", Sort.DESCENDING)
                            .notEqualTo("modulo", MODULO_ESTADO_USUARIO)
                            .findFirst();
                }

                if (transaccion == null) {
                    return;
                }

                prepare(self, transaccion);

            }, database::close, this::onError);
        }

        public void prepare(@NonNull Realm database, @NonNull Transaccion transaccion) {
            List<Transaccion> transactions = new ArrayList<>();
            transactions.add(transaccion);

            prepare(database, transactions);
        }

        private void prepare(@NonNull Realm database, @NonNull List<Transaccion> transactions) {
            boolean erronea = sharedPreferences.getBoolean(
                    ConfiguracionActivity.PREFERENCIA_NOTIFICACION_ERRONEA, true);

            for (Transaccion transaccion : transactions) {
                String action = transaccion.getAccion();
                if (ACCION_TERMINAR_ORDEN_TRABAJO.equals(action) && transaccion.getIdentidad() != null) {
                    List<Transaccion> pendientes = database.where(Transaccion.class)
                            .equalTo("modulo", MODULO_BITACORA)
                            .equalTo("identidad", transaccion.getIdentidad())
                            .beginGroup()
                            .equalTo("estado", Transaccion.ESTADO_PENDIENTE).or()
                            .equalTo("estado", Transaccion.ESTADO_SINCRONIZANDO)
                            .endGroup()
                            .lessThanOrEqualTo("creation", transaccion.getCreation())
                            .findAll();

                    if (!pendientes.isEmpty()) {
                        if (!transaccion.isManaged()) {
                            Transaccion current = database.where(Transaccion.class)
                                    .equalTo("UUID", transaccion.getUUID())
                                    .findFirst();

                            if (current != null) {
                                current.setMessage("Sincronizando bitácoras");
                            }

                            return;
                        }

                        transaccion.setMessage("Sincronizando bitácoras");
                        return;
                    }
                }

                if (transaccion.isManaged()) {
                    transaccion.setSend(Calendar.getInstance().getTime());
                    transaccion.setEstado(Transaccion.ESTADO_SINCRONIZANDO);
                } else {
                    Transaccion current = database.where(Transaccion.class)
                            .equalTo("UUID", transaccion.getUUID())
                            .findFirst();

                    if (current != null) {
                        current.setSend(Calendar.getInstance().getTime());
                        current.setEstado(Transaccion.ESTADO_SINCRONIZANDO);
                    }
                }

                Transaccion element = transaccion.isManaged()
                        ? database.copyFromRealm(transaccion)
                        : transaccion;

                Notification notification = new Notification(context, element.getUUID());
                compositeDisposable.add(onPush(element)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> onNext(element.getUUID(), response, null),
                                throwable -> {
                                    Log.e(TAG, "prepare: ", throwable);
                                    if (erronea && !Transaccion.ACCION_UBICACION.equals(action)) {
                                        notification.show(new Notification.Model(
                                                context.getString(R.string.sincronizar_error),
                                                String.format("%s - %s", element.getModulo(), element.getAccion())
                                        ));
                                    }
                                    onError(element.getUUID(), throwable);
                                }, () -> onComplete(notification, element)));
            }
        }

        public Observable<Response> onPush(@NonNull Transaccion transaccion) {
            switch (transaccion.getAccion()) {
                case Transaccion.ACCION_ACEPTAR_TRANSFERENCIA:
                    return new TransferenciaService(context)
                            .pushToMultipart(transaccion, Transferencia.class);

                case Transaccion.ACCION_UBICACION:
                    return new GeolocalizacionService(context)
                            .pushToJson(transaccion);

                case Transaccion.ACCION_REGISTRAR_BITACORA_EVENTO:
                    return new BitacoraService(context)
                            .pushToMultipart(transaccion, BitacoraEvento.class);

                case Transaccion.ACCION_REGISTRAR_BITACORA_OT:
                    return new BitacoraService(context)
                            .pushToMultipart(transaccion, BitacoraOrdenTrabajo.class);

                case Transaccion.ACCION_REGISTRAR_BITACORA_SS:
                    return new BitacoraService(context)
                            .pushToMultipart(transaccion, BitacoraSolicitudServicio.class);

                case Transaccion.ACCION_REGISTRAR_OT_BITACORA:
                    return new BitacoraService(context)
                            .pushToMultipart(transaccion, BitacoraOT.class);

                case Transaccion.ACCION_CREAR_SOLICITUD_SERVICIO:
                    return new SolicitudServicioService
                            .Builder(context).pushToMultipart(transaccion, SolicitudServicioRegistrar.class);

                case Transaccion.ACCION_RECIBIR_SOLICITUD_SERVICIO:
                    return new SolicitudServicioService
                            .Builder(context).pushToMultipart(transaccion, Recibir.class);

                case Transaccion.ACCION_EVALUAR_SOLICITUD_SERVICIO:
                case Transaccion.ACCION_ESTADO_INICIAL:
                case Transaccion.ACCION_INFORME_TECNICO:
                    return new SolicitudServicioService.Builder(context)
                            .pushToJson(transaccion);

                case Transaccion.ACCION_COMENTAR_SOLICITUD_SERVICIO:
                    if (Integer.parseInt(transaccion.getVersion()) >= Integer.parseInt(V11) || Integer.parseInt(transaccion.getVersion()) >= Integer.parseInt(V011)) {
                        return new SolicitudServicioService.Builder(context)
                                .pushToMultipart(transaccion, Comentar.class);
                    }
                    return new SolicitudServicioService.Builder(context)
                            .pushToJson(transaccion);

                case Transaccion.ACCION_REGISTRAR_LECTURAS_VARIABLES:
                    return new LecturaService(context)
                            .pushToJson(transaccion);

                case Transaccion.ACCION_DILIGENCIAR_RUTA_TRABAJO:
                    return new RutaTrabajoService.Builder(context)
                            .pushToMultipart(transaccion, Diligenciar.class);

                case Transaccion.ACCION_TERMINAR_ORDEN_TRABAJO:
                    return new OrdenTrabajoService.Builder(context)
                            .pushToMultipart(transaccion, Terminar.class);

                case Transaccion.ACCION_RECIBIR_ORDEN_TRABAJO:
                    return new OrdenTrabajoService.Builder(context)
                            .pushToMultipart(transaccion, RecibirOT.class);

                case Transaccion.ACCION_ARCHIVOS:
                    return new ArchivoService(context)
                            .pushToMultipart(transaccion, Archivo.class);

                case Transaccion.ACCION_ENTRADA:
                    return new AutorizacionAccesoService(context)
                            .pushToJson(transaccion);

                case Transaccion.ACCION_REGISTRAR_TIEMPOS:
                    return new RegistrarTiempoService(context)
                            .pushToJson(transaccion);

                case Transaccion.ACCION_INSPECCION_ELECTRICA:
                    return new InspeccionElectricaService(context)
                            .pushToJson(transaccion);

                case Transaccion.ACCION_ACTUALIZAR_CONTACTO:
                    return new ActualizarContactoService(context)
                            .pushToMultipart(transaccion, Contacto.class);

                case Transaccion.ACCION_REGISTRAR_LISTA_CHEQUEO:
                    return new ListaChequeoService.Builder(context)
                            .pushToMultipart(transaccion, Diligenciar.class);

                case Transaccion.ACCION_RECORRIDO_PLANTA_EXTERNA:
                    return new RecorridoPlantaExternaService(context)
                            .pushToJson(transaccion);

                case Transaccion.ACCION_INSTALACION_PLANTA_EXTERNA:
                    return new InstalacionPlatanExternaService(context)
                            .pushToJson(transaccion);

                case Transaccion.ACCION_FIRMA_X_ENTIDAD:
                    return new FirmaxEntidadService(context)
                            .pushToMultipart(transaccion, FirmaxEntidad.class);

                case Transaccion.ACCION_ESTADO_USUARIO:
                case Transaccion.ACCION_ANS_OT:
                    return new EstadoUsuarioService(context)
                            .pushToMultipart(transaccion, Novedad.class);

                case Transaccion.ACCION_TRASLADO_ALMACEN:
                    return new TrasladoAlmacenService(context)
                            .pushToJson(transaccion);

                case Transaccion.ACCION_MOVIMIENTO:
                    return new MovimientoService.Builder(context)
                            .pushToJson(transaccion);

                case Transaccion.ACCION_CREAR_ACTIVO:
                case Transaccion.ACCION_EDITAR_ACTIVO:
                    return new ActivoService.Builder(context)
                            .pushToJson(transaccion);

                case Transaccion.ACCION_CREAR_FALLA_OT:
                case Transaccion.ACCION_CREAR_FALLA_EQUIPO:
                case Transaccion.ACCION_CREAR_FALLA_INSTALACION_LOCATIVA:
                    return new FallaService.Builder(context)
                            .pushToMultipart(transaccion, Falla.CreateFalla.class);

                case Transaccion.ACCION_ENVIAR_CORREO:
                    return new ImagenesCorreoService(context)
                            .pushToMultipart(transaccion, ImagenesCorreoHelper.class);

                case Transaccion.ACCION_REGISTRAR_EIR:
                case Transaccion.ACCION_REGISTRAR_PTI:
                    return new InspeccionService.Builder(context)
                            .pushToMultipart(transaccion, Contenedor.Request.class);

                case Transaccion.ACCION_ASOCIAR_CODIGO_QR_BARRAS_EQUIPO:
                case Transaccion.ACCION_ASOCIAR_CODIGO_QR_BARRAS_INSTALACION_LOCATIVA:
                    return new MarcacionService(context)
                            .pushToJson(transaccion);
                case Transaccion.ACCION_INVENTARIO_EQUIPO:
                    return new InventarioActivosService.Builder(context)
                            .pushToJson(transaccion);
            }

            return Observable.error(new Exception("Acción no definida " + transaccion.getAccion()));
        }

        public void onNext(@NonNull final String UUID, @NonNull final Response response, @Nullable Transaccion exitosa) {
            Version.save(context, response.getVersion());

            Database database = new Database(context);
            database.executeTransactionAsync(self -> {

                Transaccion transaccion;
                if (exitosa != null)
                    self.insert(exitosa);

                transaccion = self.where(Transaccion.class)
                        .equalTo("UUID", UUID)
                        .findFirst();

                if (transaccion == null) {
                    return;
                }

                transaccion.setFecharespuesta(Calendar.getInstance().getTime());
                transaccion.setEstado(Transaccion.ESTADO_SINCRONIZADO);
                transaccion.setMessage(response.getMessage());
                transaccion.setRespuesta(new Gson().toJson(response));

                if (Transaccion.ACCION_CREAR_ACTIVO.equals(transaccion.getAccion()) || Transaccion.ACCION_EDITAR_ACTIVO.equals(transaccion.getAccion())) {
                    Notification notification = new Notification(context);
                    notification.show(new Notification.Model(context.getString(R.string.sincronizado), transaccion.getMessage()));
                }

                if (Transaccion.ACCION_CREAR_FALLA_EQUIPO.equals(transaccion.getAccion())) {
                    Notification notification = new Notification(context);
                    notification.show(new Notification.Model(context.getString(R.string.sincronizado), transaccion.getMessage()));
                }

                if (Transaccion.ACCION_REGISTRAR_PTI.equals(transaccion.getAccion())) {
                    if (transaccion.getCuenta() == null) {
                        return;
                    }

                    Contenedor.Request contenedor = transaccion.getValue(Contenedor.Request.class);
                    if (contenedor == null) {
                        return;
                    }

                    Contenedor current = self.where(Contenedor.class)
                            .equalTo("cuenta.UUID", transaccion.getCuenta().getUUID())
                            .equalTo("key", contenedor.getKey())
                            .equalTo("pti", true)
                            .findFirst();

                    if (current != null) {
                        current.deleteFromRealm();
                    }
                }

                if (Transaccion.ACCION_REGISTRAR_EIR.equals(transaccion.getAccion())) {
                    if (transaccion.getCuenta() == null) {
                        return;
                    }

                    Contenedor.Request contenedor = transaccion.getValue(Contenedor.Request.class);
                    if (contenedor == null) {
                        return;
                    }

                    Contenedor current = self.where(Contenedor.class)
                            .equalTo("cuenta.UUID", transaccion.getCuenta().getUUID())
                            .equalTo("key", contenedor.getKey())
                            .equalTo("eir", true)
                            .findFirst();

                    if (current != null) {
                        current.deleteFromRealm();
                    }
                }

                if (Transaccion.ACCION_REGISTRAR_BITACORA_OT.equals(transaccion.getAccion())) {
                    if (transaccion.getCuenta() == null) {
                        return;
                    }

                    BitacoraOrdenTrabajo bitacoraOrdenTrabajo
                            = transaccion.getValue(BitacoraOrdenTrabajo.class);

                    if (bitacoraOrdenTrabajo == null) {
                        return;
                    }

                    OrdenTrabajo.Response ordenTrabajoResponse
                            = response.getBody(OrdenTrabajo.Response.class);

                    boolean incluirImagenes = ordenTrabajoResponse.getImagenes() != null && !ordenTrabajoResponse.getImagenes().isEmpty();
                    boolean incluirAdjuntos = ordenTrabajoResponse.getAdjuntos() != null && !ordenTrabajoResponse.getAdjuntos().isEmpty();

                    RealmResults<OrdenTrabajo> resultados = self.where(OrdenTrabajo.class)
                            .equalTo("cuenta.UUID", transaccion.getCuenta().getUUID())
                            .equalTo("id", ordenTrabajoResponse.getIdot())
                            .findAll();

                    if (incluirImagenes || incluirAdjuntos) {
                        for (OrdenTrabajo resultado : resultados) {
                            if (incluirImagenes) {
                                resultado.getImagenes().deleteAllFromRealm();
                            }

                            if (incluirAdjuntos) {
                                resultado.getAdjuntos().deleteAllFromRealm();
                            }
                        }
                    }

                    for (OrdenTrabajo resultado : resultados) {
                        resultado.setPorcentaje(bitacoraOrdenTrabajo.getExecutionrate());
                        if (ordenTrabajoResponse.getRealimentacion() != null && !ordenTrabajoResponse.getRealimentacion().isEmpty()) {
                            resultado.setRealimentacion(ordenTrabajoResponse.getRealimentacion());
                        }

                        if (incluirImagenes) {
                            RealmList<Adjuntos> imagenes = new RealmList<>();
                            for (Adjuntos imagen : ordenTrabajoResponse.getImagenes()) {
                                imagenes.add(self.copyToRealm(imagen));
                            }
                            resultado.setImagenes(imagenes);
                        }

                        if (incluirAdjuntos) {
                            RealmList<Adjuntos> adjuntos = new RealmList<>();
                            for (Adjuntos adjunto : ordenTrabajoResponse.getAdjuntos()) {
                                adjuntos.add(self.copyToRealm(adjunto));
                            }
                            resultado.setAdjuntos(adjuntos);
                        }
                    }

                    List<Asignada> asignadas = self.where(Asignada.class)
                            .equalTo("cuenta.UUID", transaccion.getCuenta().getUUID())
                            .equalTo("ordenTrabajo.id", ordenTrabajoResponse.getIdot())
                            .findAll();

                    if (incluirImagenes || incluirAdjuntos) {
                        for (Asignada asignada : asignadas) {
                            if (asignada.getOrdenTrabajo() != null) {
                                if (incluirImagenes) {
                                    asignada.getOrdenTrabajo().getImagenes().deleteAllFromRealm();
                                }

                                if (incluirAdjuntos) {
                                    asignada.getOrdenTrabajo().getAdjuntos().deleteAllFromRealm();
                                }
                            }
                        }
                    }

                    for (Asignada asignada : asignadas) {
                        if (asignada.getOrdenTrabajo() != null) {
                            asignada.getOrdenTrabajo().setPorcentaje(bitacoraOrdenTrabajo.getExecutionrate());
                            if (ordenTrabajoResponse.getRealimentacion() != null && !ordenTrabajoResponse.getRealimentacion().isEmpty()) {
                                asignada.getOrdenTrabajo().setRealimentacion(ordenTrabajoResponse.getRealimentacion());
                            }

                            if (incluirImagenes) {
                                RealmList<Adjuntos> imagenes = new RealmList<>();
                                for (Adjuntos adjunto : ordenTrabajoResponse.getImagenes()) {
                                    imagenes.add(self.copyToRealm(adjunto));
                                }
                                asignada.getOrdenTrabajo().setImagenes(imagenes);
                            }

                            if (incluirAdjuntos) {
                                RealmList<Adjuntos> adjuntos = new RealmList<>();
                                for (Adjuntos adjunto : ordenTrabajoResponse.getAdjuntos()) {
                                    adjuntos.add(self.copyToRealm(adjunto));
                                }
                                asignada.getOrdenTrabajo().setAdjuntos(adjuntos);
                            }
                        }
                    }
                }
            }, database::close, this::onError);
        }

        private void onError(Throwable throwable) {
            Log.e(TAG, "onError: ", throwable);
        }

        private void onError(@NonNull final String UUID, @NonNull final Throwable throwable) {
            Database database = new Database(context);
            database.executeTransactionAsync(self -> {
                Transaccion transaccion = self.where(Transaccion.class)
                        .equalTo("UUID", UUID)
                        .findFirst();

                if (transaccion != null) {
                    transaccion.setFecharespuesta(Calendar.getInstance().getTime());
                    boolean httpException = throwable instanceof Mantum.HttpException;
                    if (httpException) {
                        transaccion.setEstado(Transaccion.ESTADO_PENDIENTE);
                        transaccion.setMessage("Ocurrio un error en la conexión, intentelo de nuevo");
                        return;
                    }

                    transaccion.setEstado(Transaccion.ESTADO_ERROR);
                    transaccion.setMessage(throwable.getMessage());

                    boolean responseException = throwable instanceof Mantum.ResponseException;
                    if (responseException) {
                        Mantum.ResponseException exception = (Mantum.ResponseException) throwable;
                        transaccion.setRespuesta(exception.getBody());
                    }
                }
            }, database::close, this::onError);
        }

        public void onComplete(Notification notification, Transaccion transaccion) {
            if (Transaccion.ACCION_UBICACION.equals(transaccion.getAccion()) || Transaccion.ACCION_CREAR_ACTIVO.equals(transaccion.getAccion())
                    || Transaccion.ACCION_EDITAR_ACTIVO.equals(transaccion.getAccion()) || Transaccion.ACCION_CREAR_FALLA_EQUIPO.equals(transaccion.getAccion())) {
                return;
            }

            boolean show = sharedPreferences.getBoolean(
                    ConfiguracionActivity.PREFERENCIA_NOTIFICACION_SATISFACTORIA, true);

            if (show) {
                notification.show(new Notification.Model(
                        context.getString(R.string.sincronizado),
                        String.format("%s - %s", transaccion.getModulo(), transaccion.getAccion())
                ));
            }
        }
    }
}