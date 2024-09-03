package com.mantum.cmms.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mantum.demo.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.BitacoraEvento;
import com.mantum.cmms.domain.BitacoraOT;
import com.mantum.cmms.domain.BitacoraOrdenTrabajo;
import com.mantum.cmms.domain.BitacoraSolicitudServicio;
import com.mantum.cmms.domain.Comentar;
import com.mantum.cmms.domain.Coordenada;
import com.mantum.cmms.domain.Diligenciar;
import com.mantum.cmms.domain.FirmaxEntidad;
import com.mantum.cmms.domain.InstalacionPlantaExterna;
import com.mantum.cmms.domain.RecorridoPlantaExterna;
import com.mantum.cmms.domain.SolicitudServicioRegistrar;
import com.mantum.cmms.domain.Terminar;
import com.mantum.cmms.entity.Contenedor;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.EstadoInicial;
import com.mantum.cmms.entity.InformeTecnico;
import com.mantum.cmms.entity.ListaChequeo;
import com.mantum.cmms.entity.PendienteMantenimiento;
import com.mantum.cmms.entity.Personal;
import com.mantum.cmms.entity.RutaTrabajo;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.entity.TrasladoAlmacen;
import com.mantum.cmms.entity.Variable;
import com.mantum.cmms.factory.SparseArrayTypeAdapterFactory;
import com.mantum.cmms.fragment.BitacoraEventoFragment;
import com.mantum.cmms.fragment.BitacoraOTFragment;
import com.mantum.cmms.fragment.BitacoraOrdenTrabajoFragment;
import com.mantum.cmms.fragment.BitacoraSolicitudServicioFragment;
import com.mantum.cmms.fragment.DamageFragment;
import com.mantum.cmms.fragment.EstadoInicialFragment;
import com.mantum.cmms.fragment.FallaFragment;
import com.mantum.cmms.fragment.FirmaxEntidadFragment;
import com.mantum.cmms.fragment.ImagenesFragment;
import com.mantum.cmms.fragment.InformeTecnicoFragment;
import com.mantum.cmms.fragment.InspeccionChecklistFragment;
import com.mantum.cmms.fragment.InspeccionEIRFragment;
import com.mantum.cmms.fragment.InspeccionPTIFragment;
import com.mantum.cmms.fragment.InstalacionPlantaExternaFragment;
import com.mantum.cmms.fragment.LecturaFragment;
import com.mantum.cmms.fragment.MochilaListaFragment;
import com.mantum.cmms.fragment.OrdenTrabajoTerminarFragment;
import com.mantum.cmms.fragment.PendienteFragment;
import com.mantum.cmms.fragment.PersonaListaChequeoFragment;
import com.mantum.cmms.fragment.PersonalFragment;
import com.mantum.cmms.fragment.RecorridoPlantaExternaFragment;
import com.mantum.cmms.fragment.RecursoAdicionalFragment;
import com.mantum.cmms.fragment.RecursoListaFragment;
import com.mantum.cmms.fragment.RutaTrabajoDiligenciarFragment;
import com.mantum.cmms.fragment.SolicitudServicioComentarFragment;
import com.mantum.cmms.fragment.SolicitudServicioRegistrarFragment;
import com.mantum.cmms.fragment.TransaccionFragment;
import com.mantum.cmms.fragment.TrasladoAlmacenFragment;
import com.mantum.cmms.fragment.UbicacionDetalleFragment;
import com.mantum.cmms.helper.AdjuntoHelper;
import com.mantum.cmms.helper.CoordenadaHelper;
import com.mantum.cmms.helper.PendienteHelper;
import com.mantum.cmms.helper.PersonalHelper;
import com.mantum.cmms.helper.RecursoAdicionalHelper;
import com.mantum.cmms.helper.RecursoHelper;
import com.mantum.cmms.view.RecursoView;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.adapter.TabAdapter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TransaccionDetalleActivity extends Mantum.Activity implements OnCompleteListener {

    private static final String TAG = TransaccionDetalleActivity.class.getSimpleName();

    public static final String KEY_TYPE = "type";

    private Database database;

    private Transaccion detalle;

    private TabAdapter tabAdapter;

    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    private Cuenta cuenta;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_detalle_transaccion);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            database = new Database(this);
            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                throw new Exception(getString(R.string.transaccion_id_empty));
            }

            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            detalle = database.where(Transaccion.class)
                    .equalTo("UUID", bundle.getString(Mantum.KEY_ID))
                    .findFirst();

            if (detalle == null) {
                throw new Exception(getString(R.string.transaccion_empty));
            }

            includeBackButtonAndTitle(R.string.general);
            switch (bundle.getString(KEY_TYPE, "empty")) {

                case Transaccion.ACCION_REGISTRAR_LISTA_CHEQUEO: {
                    tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                            Arrays.asList(
                                    new RutaTrabajoDiligenciarFragment().activarModoVer().ocultarMenu(),
                                    new PersonaListaChequeoFragment(),
                                    new ImagenesFragment().inactivarModoEditar(),
                                    new TransaccionFragment()
                            )
                    );
                    break;
                }

                case Transaccion.ACCION_REGISTRAR_EIR: {
                    tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                            Arrays.asList(
                                    new InspeccionEIRFragment().readOnly(),
                                    new DamageFragment().setReadOnly(true),
                                    new TransaccionFragment()
                            )
                    );
                    break;
                }

                case Transaccion.ACCION_REGISTRAR_PTI:
                    tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                            Arrays.asList(
                                    new InspeccionPTIFragment().readOnly(),
                                    new InspeccionChecklistFragment().setReadOnly(true),
                                    new FallaFragment().setReadOnly(true),
                                    new TransaccionFragment()
                            )
                    );
                    break;

                case Transaccion.ACCION_UBICACION:
                    tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                            Arrays.asList(new UbicacionDetalleFragment(), new TransaccionFragment()));
                    break;

                case Transaccion.ACCION_REGISTRAR_BITACORA_EVENTO:
                    tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                            Arrays.asList(
                                    new BitacoraEventoFragment(),
                                    new UbicacionDetalleFragment().setModePrincipal(false),
                                    new PendienteFragment(),
                                    new RecursoListaFragment(),
                                    new PersonalFragment(),
                                    new ImagenesFragment().inactivarModoEditar(),
                                    new TransaccionFragment()));
                    break;

                case Transaccion.ACCION_REGISTRAR_BITACORA_OT:
                    tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                            Arrays.asList(
                                    new BitacoraOrdenTrabajoFragment(),
                                    new UbicacionDetalleFragment().setModePrincipal(false),
                                    new LecturaFragment().setReadonly(true),
                                    new PendienteFragment(),
                                    new MochilaListaFragment(),
                                    new RecursoListaFragment(),
                                    new PersonalFragment(),
                                    new ImagenesFragment().inactivarModoEditar(),
                                    new TransaccionFragment()));
                    break;

                case Transaccion.ACCION_REGISTRAR_BITACORA_SS:
                    tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                            Arrays.asList(
                                    new BitacoraSolicitudServicioFragment(),
                                    new UbicacionDetalleFragment().setModePrincipal(false),
                                    new PendienteFragment(),
                                    new RecursoListaFragment(),
                                    new PersonalFragment(),
                                    new ImagenesFragment().inactivarModoEditar(),
                                    new TransaccionFragment()));
                    break;

                case Transaccion.ACCION_REGISTRAR_OT_BITACORA:
                    tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                            Arrays.asList(
                                    new BitacoraOTFragment(),
                                    new UbicacionDetalleFragment().setModePrincipal(false),
                                    new LecturaFragment().setReadonly(true),
                                    new PendienteFragment(),
                                    new RecursoListaFragment(),
                                    new PersonalFragment(),
                                    new ImagenesFragment().inactivarModoEditar(),
                                    new TransaccionFragment()));
                    break;

                case Transaccion.ACCION_DILIGENCIAR_RUTA_TRABAJO:
                    tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                            Arrays.asList(
                                    new RutaTrabajoDiligenciarFragment().activarModoVer(),
                                    new RecursoListaFragment(),
                                    new PersonalFragment(),
                                    new ImagenesFragment().inactivarModoEditar(),
                                    new TransaccionFragment()));
                    break;

                case Transaccion.ACCION_TERMINAR_ORDEN_TRABAJO:
                    tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                            Arrays.asList(
                                    new OrdenTrabajoTerminarFragment(),
                                    new ImagenesFragment().inactivarModoEditar(),
                                    new TransaccionFragment()));
                    break;

                case Transaccion.ACCION_ESTADO_INICIAL:
                    tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                            Arrays.asList(new EstadoInicialFragment(), new TransaccionFragment()));
                    break;

                case Transaccion.ACCION_INFORME_TECNICO:
                    tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                            Arrays.asList(new InformeTecnicoFragment().setAction(false),
                                    new RecursoAdicionalFragment().setAction(false), new TransaccionFragment()));
                    break;

                case Transaccion.ACCION_RECORRIDO_PLANTA_EXTERNA:
                    tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                            Arrays.asList(new RecorridoPlantaExternaFragment().setAction(false), new TransaccionFragment()));
                    break;

                case Transaccion.ACCION_INSTALACION_PLANTA_EXTERNA:
                    tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                            Arrays.asList(new InstalacionPlantaExternaFragment().setAction(false), new TransaccionFragment()));
                    break;

                case Transaccion.ACCION_FIRMA_X_ENTIDAD:
                    tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                            Arrays.asList(new FirmaxEntidadFragment().setAction(false), new TransaccionFragment()));
                    break;

                case Transaccion.ACCION_TRASLADO_ALMACEN:
                    tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                            Arrays.asList(new TrasladoAlmacenFragment().setAction(false), new TransaccionFragment()));
                    break;

                case Transaccion.ACCION_CREAR_SOLICITUD_SERVICIO:
                    tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                            Arrays.asList(new SolicitudServicioRegistrarFragment(), new ImagenesFragment().inactivarModoEditar(), new TransaccionFragment()));
                    break;

                case Transaccion.ACCION_COMENTAR_SOLICITUD_SERVICIO:
                    SolicitudServicioComentarFragment solicitudServicioComentarFragment = new SolicitudServicioComentarFragment();
                    solicitudServicioComentarFragment.ocultarAcciones();
                    tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                            Arrays.asList(solicitudServicioComentarFragment,
                                    new ImagenesFragment().inactivarModoEditar(), new TransaccionFragment()));
                    break;

                case Transaccion.ACCION_RECIBIR_ORDEN_TRABAJO:
                    tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                            Arrays.asList(new ImagenesFragment().inactivarModoEditar(), new TransaccionFragment()));
                    break;

                case Transaccion.ACCION_ESTADO_USUARIO:
                case Transaccion.ACCION_REGISTRAR_LECTURAS_VARIABLES:
                case Transaccion.ACCION_MOVIMIENTO:
                case Transaccion.ACCION_ACEPTAR_TRANSFERENCIA:
                case Transaccion.ACCION_RECIBIR_SOLICITUD_SERVICIO:
                case Transaccion.ACCION_CREAR_ACTIVO:
                case Transaccion.ACCION_EDITAR_ACTIVO:
                case Transaccion.ACCION_CREAR_FALLA_OT:
                case Transaccion.ACCION_CREAR_FALLA_EQUIPO:
                case Transaccion.ACCION_CREAR_FALLA_INSTALACION_LOCATIVA:
                case Transaccion.ACCION_ENVIAR_CORREO:
                case Transaccion.ACCION_ASOCIAR_CODIGO_QR_BARRAS_EQUIPO:
                case Transaccion.ACCION_ASOCIAR_CODIGO_QR_BARRAS_INSTALACION_LOCATIVA:
                    tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                            Collections.singletonList(new TransaccionFragment()));
                    break;
            }

            if (tabAdapter == null) {
                throw new Exception(getString(R.string.transaccion_adapter_empty));
            }

            ViewPager viewPager = findViewById(R.id.viewPager);
            viewPager.setAdapter(tabAdapter);
            viewPager.setOffscreenPageLimit(tabAdapter.getCount() - 1);

            TabLayout tabLayout = findViewById(R.id.tabs);
            tabLayout.setTabMode(tabAdapter.getCount() > 3 ? TabLayout.MODE_SCROLLABLE : TabLayout.MODE_FIXED);
            tabLayout.setupWithViewPager(viewPager);
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    onRefresh(tabAdapter.getFragment(tab.getPosition()));
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }

            });

            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        } catch (Exception e) {
            // backActivity(e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }

    @Override
    public void onComplete(@NonNull String name) {
        onRefresh(tabAdapter.getFragment(name));
    }

    private void onRefresh(@Nullable Mantum.Fragment tabFragment) {
        try {
            if (tabFragment != null) {
                Transaccion transaccion = detalle.isManaged()
                        ? database.copyFromRealm(detalle)
                        : detalle;

                switch (tabFragment.getKey()) {
                    case PersonalFragment.KEY_TAB: {
                        PersonalHelper helper
                                = gson.fromJson(transaccion.getValue(), PersonalHelper.class);

                        PersonalFragment fragment
                                = (PersonalFragment) tabFragment;
                        fragment.onRefresh(helper.getGroup());
                        break;
                    }

                    case InspeccionEIRFragment.KEY_TAB: {
                        Gson gson = new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd")
                                .create();

                        Contenedor.Request request
                                = gson.fromJson(transaccion.getValue(), Contenedor.Request.class);

                        InspeccionEIRFragment fragment = (InspeccionEIRFragment) tabFragment;
                        fragment.readOnly().onLoad(request);
                        break;
                    }

                    case InspeccionPTIFragment.KEY_TAB: {
                        Gson gson = new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd")
                                .create();

                        Contenedor.Request request
                                = gson.fromJson(transaccion.getValue(), Contenedor.Request.class);

                        InspeccionPTIFragment fragment = (InspeccionPTIFragment) tabFragment;
                        fragment.readOnly().onLoad(request);
                        break;
                    }

                    case InspeccionChecklistFragment.KEY_TAB: {
                        Gson gson = new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd")
                                .create();

                        Contenedor.Request request
                                = gson.fromJson(transaccion.getValue(), Contenedor.Request.class);

                        InspeccionChecklistFragment fragment = (InspeccionChecklistFragment) tabFragment;
                        fragment.setReadOnly(true).onLoad(request.getPreguntas());
                        break;
                    }

                    case FallaFragment.KEY_TAB: {
                        Gson gson = new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd")
                                .registerTypeAdapterFactory(SparseArrayTypeAdapterFactory.INSTANCE)
                                .create();

                        Contenedor.Request request
                                = gson.fromJson(transaccion.getValue(), Contenedor.Request.class);

                        FallaFragment fragment = (FallaFragment) tabFragment;
                        fragment.setReadOnly(true).onLoad(request.isAbrirOT(), request.getFallas());
                        break;
                    }

                    case DamageFragment.KEY_TAB: {
                        Gson gson = new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd")
                                .registerTypeAdapterFactory(SparseArrayTypeAdapterFactory.INSTANCE)
                                .create();

                        Contenedor.Request request
                                = gson.fromJson(transaccion.getValue(), Contenedor.Request.class);

                        DamageFragment fragment = (DamageFragment) tabFragment;
                        fragment.setReadOnly(true).onLoad(request.getDamages());
                        break;
                    }

                    case TransaccionFragment.KEY_TAB:
                        TransaccionFragment transaccionFragment
                                = TransaccionFragment.class.cast(tabFragment);

                        if (transaccionFragment != null) {
                            transaccionFragment.onRefresh(transaccion);
                        }
                        break;

                    case UbicacionDetalleFragment.KEY_TAB:
                        Coordenada coordenada
                                = gson.fromJson(transaccion.getValue(), Coordenada.class);

                        if (coordenada.getAltitude() == 0 && coordenada.getLatitude() == 0 && coordenada.getLongitude() == 0) {
                            CoordenadaHelper temporal = new GsonBuilder().create()
                                    .fromJson(transaccion.getValue(), CoordenadaHelper.class);

                            if (temporal != null && temporal.getLocation() != null) {
                                coordenada = temporal.getLocation();
                            }
                        }

                        UbicacionDetalleFragment ubicacionDetalleFragment
                                = UbicacionDetalleFragment.class.cast(tabFragment);

                        if (ubicacionDetalleFragment != null) {
                            ubicacionDetalleFragment.onRefresh(coordenada);
                        }

                        break;

                    case ImagenesFragment.KEY_TAB:
                        AdjuntoHelper adjuntoHelper
                                = gson.fromJson(transaccion.getValue(), AdjuntoHelper.class);

                        ImagenesFragment imagenesFragment
                                = ImagenesFragment.class.cast(tabFragment);

                        if (imagenesFragment != null) {
                            imagenesFragment.clear()
                                    .inactivarModoEditar()
                                    .onLoad(adjuntoHelper.getAdjuntos());
                        }

                        break;

                    case PendienteFragment.KEY_TAB:
                        // Es necesario ya que antes el pendiente de mantenimiento
                        // era una cadena de texto
                        PendienteMantenimiento.Request pendienteMantenimiento
                                = new PendienteMantenimiento.Request();
                        try {
                            PendienteHelper pendienteHelper
                                    = gson.fromJson(transaccion.getValue(), PendienteHelper.class);

                            pendienteMantenimiento.setDescripcion(pendienteHelper.getPendientepmtto());
                        } catch (Exception e) {
                            PendienteHelper.PendienteMantenimientoHelper pendienteMantenimientoHelper
                                    = gson.fromJson(transaccion.getValue(), PendienteHelper.PendienteMantenimientoHelper.class);
                            pendienteMantenimiento = pendienteMantenimientoHelper.getPendientepmtto();
                        }

                        PendienteFragment pendienteFragment
                                = PendienteFragment.class.cast(tabFragment);

                        if (pendienteFragment != null) {
                            pendienteFragment.activarModoVer()
                                    .onRefresh(pendienteMantenimiento);
                        }

                        break;

                    case LecturaFragment.KEY_TAB:
                        // Gson global no tiene el formato de fecha correcto para la bitacora
                        BitacoraOT bitacoraOTLectura = new GsonBuilder().create()
                                .fromJson(transaccion.getValue(), BitacoraOT.class);

                        Type variableType = new TypeToken<ArrayList<Variable>>() {
                        }.getType();
                        List<Variable> variables = new Gson().fromJson(bitacoraOTLectura.getVariables(), variableType);
                        if (variables == null) {
                            variables = new ArrayList<>();
                        }

                        LecturaFragment lecturaFragment
                                = LecturaFragment.class.cast(tabFragment);

                        if (lecturaFragment != null) {
                            lecturaFragment.onRefresh(variables);
                        }

                        break;

                    case RecursoListaFragment.KEY_TAB:
                        RecursoHelper.ViewRecurso viewRecurso
                                = gson.fromJson(transaccion.getValue(), RecursoHelper.ViewRecurso.class);

                        RecursoListaFragment recursoListaFragment
                                = RecursoListaFragment.class.cast(tabFragment);

                        if (recursoListaFragment != null) {
                            recursoListaFragment.onRefresh(
                                    RecursoView.Detail.factory(viewRecurso.getRecursos()));
                        }

                        break;

                    case MochilaListaFragment.KEY_TAB:
                        RecursoHelper.ViewMochila viewMochila
                                = gson.fromJson(transaccion.getValue(), RecursoHelper.ViewMochila.class);

                        MochilaListaFragment mochilaListaFragment
                                = MochilaListaFragment.class.cast(tabFragment);

                        if (mochilaListaFragment != null) {
                            mochilaListaFragment.onLoad(viewMochila.getMochila());

                        }
                        break;

                    case BitacoraEventoFragment.KEY_TAB:
                        // Gson global no tiene el formato de fecha correcto para la bitacora de evento
                        BitacoraEvento bitacoraEvento = new GsonBuilder().create()
                                .fromJson(transaccion.getValue(), BitacoraEvento.class);

                        BitacoraEventoFragment bitacoraEventoFragment
                                = BitacoraEventoFragment.class.cast(tabFragment);

                        if (bitacoraEventoFragment != null) {
                            bitacoraEventoFragment.onRefresh(bitacoraEvento);
                        }

                        break;

                    case BitacoraOrdenTrabajoFragment.KEY_TAB:
                        // Gson global no tiene el formato de fecha correcto para la bitacora de evento
                        BitacoraOrdenTrabajo bitacoraOrdenTrabajo = new GsonBuilder().create()
                                .fromJson(transaccion.getValue(), BitacoraOrdenTrabajo.class);

                        BitacoraOrdenTrabajoFragment.class.cast(tabFragment)
                                .onRefresh(bitacoraOrdenTrabajo);
                        break;

                    case BitacoraSolicitudServicioFragment.KEY_TAB:
                        // Gson global no tiene el formato de fecha correcto para la bitacora de evento
                        BitacoraSolicitudServicio bitacoraSolicitudServicio = new GsonBuilder().create()
                                .fromJson(transaccion.getValue(), BitacoraSolicitudServicio.class);

                        BitacoraSolicitudServicioFragment.class.cast(tabFragment)
                                .onRefresh(bitacoraSolicitudServicio);
                        break;

                    case BitacoraOTFragment.KEY_TAB:
                        // Gson global no tiene el formato de fecha correcto para la bitacora de evento
                        BitacoraOT bitacoraOT = new GsonBuilder().create()
                                .fromJson(transaccion.getValue(), BitacoraOT.class);

                        BitacoraOTFragment bitacoraOTFragment
                                = BitacoraOTFragment.class.cast(tabFragment);

                        if (bitacoraOTFragment != null) {
                            bitacoraOTFragment.onRefresh(bitacoraOT);
                        }

                        break;

                    case PersonaListaChequeoFragment.KEY_TAB: {
                        Diligenciar diligenciar
                                = gson.fromJson(transaccion.getValue(), Diligenciar.class);

                        PersonaListaChequeoFragment fragment
                                = (PersonaListaChequeoFragment) tabFragment;

                        fragment.onRefresh(diligenciar.getPersonas());
                        break;
                    }

                    case RutaTrabajoDiligenciarFragment.KEY_TAB:
                        Diligenciar diligenciar
                                = gson.fromJson(transaccion.getValue(), Diligenciar.class);

                        RutaTrabajoDiligenciarFragment rutaTrabajoDiligenciarFragment
                                = (RutaTrabajoDiligenciarFragment) tabFragment;

                        if (!diligenciar.isListachequeo()) {
                            RutaTrabajo rutaTrabajo = database.where(RutaTrabajo.class)
                                    .equalTo("cuenta.UUID", cuenta.getUUID())
                                    .equalTo("id", diligenciar.getIdrt())
                                    .equalTo("idejecucion", diligenciar.getIdejecucion())
                                    .findFirst();

                            if (rutaTrabajo == null) {
                                rutaTrabajo = new RutaTrabajo();
                            }

                            rutaTrabajo = rutaTrabajo.isManaged()
                                    ? database.copyFromRealm(rutaTrabajo)
                                    : rutaTrabajo;

                            rutaTrabajoDiligenciarFragment
                                    .onStart(rutaTrabajo, diligenciar);
                        } else {
                            ListaChequeo listaChequeo = database.where(ListaChequeo.class)
                                    .equalTo("id", diligenciar.getIdlc())
                                    .equalTo("cuenta.UUID", cuenta.getUUID())
                                    .findFirst();

                            if (listaChequeo == null) {
                                listaChequeo = new ListaChequeo();
                            }

                            listaChequeo = listaChequeo.isManaged()
                                    ? database.copyFromRealm(listaChequeo)
                                    : listaChequeo;

                            rutaTrabajoDiligenciarFragment
                                    .onStart(listaChequeo, diligenciar);
                        }

                        break;

                    case OrdenTrabajoTerminarFragment.KEY_TAB:
                        Terminar terminar
                                = gson.fromJson(transaccion.getValue(), Terminar.class);

                        OrdenTrabajoTerminarFragment ordenTrabajoTerminarFragment
                                = OrdenTrabajoTerminarFragment.class.cast(tabFragment);

                        if (ordenTrabajoTerminarFragment != null) {
                            ordenTrabajoTerminarFragment.setViewMode(true)
                                    .onRefresh(terminar);
                        }

                        break;

                    case EstadoInicialFragment.KEY_TAB:
                        EstadoInicial.Request estadoInicial
                                = gson.fromJson(transaccion.getValue(), EstadoInicial.Request.class);

                        EstadoInicialFragment.class.cast(tabFragment)
                                .setViewMode(true)
                                .onRefresh(estadoInicial);
                        break;

                    case InformeTecnicoFragment.KEY_TAB:
                        InformeTecnico.Request informeTecnico
                                = gson.fromJson(transaccion.getValue(), InformeTecnico.Request.class);

                        InformeTecnicoFragment.class.cast(tabFragment)
                                .setViewMode(true)
                                .onRefresh(informeTecnico);
                        break;

                    case RecursoAdicionalFragment.KEY_TAB:
                        RecursoAdicionalHelper recursoAdicionalHelper
                                = gson.fromJson(transaccion.getValue(), RecursoAdicionalHelper.class);

                        RecursoAdicionalFragment.class.cast(tabFragment)
                                .addResources(recursoAdicionalHelper.getRecursos());
                        break;

                    case RecorridoPlantaExternaFragment.KEY_TAB:
                        RecorridoPlantaExterna recorridoPlantaExterna
                                = gson.fromJson(transaccion.getValue(), RecorridoPlantaExterna.class);

                        RecorridoPlantaExternaFragment.class.cast(tabFragment)
                                .onView(recorridoPlantaExterna);
                        break;

                    case InstalacionPlantaExternaFragment.KEY_TAB:
                        InstalacionPlantaExterna instalacionPlantaExterna
                                = gson.fromJson(transaccion.getValue(), InstalacionPlantaExterna.class);

                        InstalacionPlantaExternaFragment.class.cast(tabFragment)
                                .onView(instalacionPlantaExterna);
                        break;

                    case FirmaxEntidadFragment.KEY_TAB:
                        FirmaxEntidad firmaxEntidad
                                = gson.fromJson(transaccion.getValue(), FirmaxEntidad.class);

                        FirmaxEntidadFragment.class.cast(tabFragment)
                                .onView(firmaxEntidad);
                        break;

                    case SolicitudServicioRegistrarFragment.KEY_TAB:
                        SolicitudServicioRegistrar solicitudServicioRegistrar
                                = gson.fromJson(transaccion.getValue(), SolicitudServicioRegistrar.class);

                        SolicitudServicioRegistrarFragment solicitudServicioRegistrarFragment
                                = SolicitudServicioRegistrarFragment.class.cast(tabFragment);

                        if (solicitudServicioRegistrarFragment != null) {
                            solicitudServicioRegistrarFragment.onView(solicitudServicioRegistrar);
                        }

                        break;

                    case SolicitudServicioComentarFragment.KEY_TAB:
                        Comentar comentar
                                = gson.fromJson(transaccion.getValue(), Comentar.class);

                        SolicitudServicioComentarFragment solicitudServicioComentarFragment
                                = SolicitudServicioComentarFragment.class.cast(tabFragment);

                        if (solicitudServicioComentarFragment != null) {
                            solicitudServicioComentarFragment.onView(comentar);
                        }

                        break;

                    case TrasladoAlmacenFragment.KEY_TAB:
                        TrasladoAlmacen trasladoAlmacen
                                = gson.fromJson(transaccion.getValue(), TrasladoAlmacen.class);

                        TrasladoAlmacenFragment.class.cast(tabFragment)
                                .onView(trasladoAlmacen);
                        break;

                }
            }
        } catch (Exception e) {
            Log.e(TAG, "onRefresh: ", e);
        }
    }
}