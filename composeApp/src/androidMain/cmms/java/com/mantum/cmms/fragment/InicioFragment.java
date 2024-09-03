package com.mantum.cmms.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.mantum.R;
import com.mantum.cmms.activity.BitacoraActivity;
import com.mantum.cmms.activity.BusquedaActivity;
import com.mantum.cmms.activity.CaptureActivityPortrait;
import com.mantum.cmms.activity.GaleriaActivity;
import com.mantum.cmms.activity.LecturaActivity;
import com.mantum.cmms.activity.SolicitudServicioActivity;
import com.mantum.cmms.adapter.DetalleAdapter;
import com.mantum.cmms.adapter.VariableAdapter;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Busqueda;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Equipo;
import com.mantum.cmms.entity.Familia;
import com.mantum.cmms.entity.InstalacionLocativa;
import com.mantum.cmms.entity.InstalacionProceso;
import com.mantum.cmms.entity.Proveedor;
import com.mantum.cmms.entity.parameter.UserParameter;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.cmms.security.Security;
import com.mantum.cmms.util.Version;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

public class InicioFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Inicio";

    private RecyclerView entidad;

    private RecyclerView variables;

    private OnCompleteListener onCompleteListener;

    private View view;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_inicio, container, false);

        FloatingActionButton register = view.findViewById(R.id.register_ss);
        if (Version.check(view.getContext(), 7) && !UserPermission.check(view.getContext(), UserPermission.SOLICITUD_SERVICIO_CREAR)) {
            register.setVisibility(View.GONE);
        }

        register.setOnClickListener(v -> {
            Busqueda busqueda = entity(view.getContext());
            boolean show = busqueda != null && !busqueda.getData().isEmpty();
            if (!show) {
                Bundle extra = new Bundle();
                extra.putString("message", getString(R.string.search_entity));

                Intent intent = new Intent(view.getContext(), BusquedaActivity.class);
                intent.putExtras(extra);

                startActivity(intent);
                return;
            }

            if (!busqueda.isEmpty()) {
                if (!Security.action(busqueda.getActions(), Security.ACTION_REGISTRAR_SS)) {
                    Snackbar.make(view, R.string.entidad_seleccionada, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putLong(SolicitudServicioActivity.KEY_ID, busqueda.getId());
                bundle.putString(SolicitudServicioActivity.KEY_NAME, String.format("%s | %s", busqueda.getCode(), busqueda.getName()));
                bundle.putString(SolicitudServicioActivity.KEY_TYPE, busqueda.getType());

                Intent intent = new Intent(view.getContext(), SolicitudServicioActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
                return;
            }

            Intent intent = new Intent(view.getContext(), SolicitudServicioActivity.class);
            startActivity(intent);
        });

        FloatingActionButton lectura = view.findViewById(R.id.registrar_lecturas);
        lectura.setOnClickListener(v -> {
            Busqueda busqueda = entity(view.getContext());
            boolean show = busqueda != null && !busqueda.getData().isEmpty();

            if (!show) {
                Bundle extra = new Bundle();
                extra.putString("message", getString(R.string.search_entity));

                Intent intent = new Intent(view.getContext(), BusquedaActivity.class);
                intent.putExtras(extra);

                startActivity(intent);
                return;
            }

            if (entidadSeleccionada(busqueda)) {
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putLong(Mantum.KEY_ID, busqueda.getId());
            bundle.putString(LecturaActivity.KEY_TYPE, busqueda.getType());
            bundle.putString(LecturaActivity.KEY_TYPE_ACTION, busqueda.getType());

            Intent intent = new Intent(view.getContext(), LecturaActivity.class);
            intent.putExtras(bundle);

            startActivity(intent);
        });

        FloatingActionButton imagenes = view.findViewById(R.id.imagenes);
        imagenes.setOnClickListener(v -> {
            Busqueda busqueda = entity(view.getContext());
            boolean show = busqueda != null && !busqueda.getData().isEmpty();
            if (!show) {
                Bundle extra = new Bundle();
                extra.putString("message", getString(R.string.search_entity));

                Intent intent = new Intent(view.getContext(), BusquedaActivity.class);
                intent.putExtras(extra);

                startActivity(intent);
                return;
            }

            if (entidadSeleccionada(busqueda)) {
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putLong(Mantum.KEY_ID, busqueda.getId());
            bundle.putString(GaleriaActivity.KEY_TIPO_ENTIDAD, busqueda.getType());

            Intent intent = new Intent(view.getContext(), GaleriaActivity.class);
            intent.putExtras(bundle);

            startActivity(intent);
        });


        view.findViewById(R.id.ubicacion).setOnClickListener(v -> {
            Busqueda busqueda = entity(view.getContext());
            boolean show = busqueda != null && !busqueda.getData().isEmpty();
            if (!show) {
                Bundle extra = new Bundle();
                extra.putString("message", getString(R.string.search_entity));

                Intent intent = new Intent(view.getContext(), BusquedaActivity.class);
                intent.putExtras(extra);

                startActivity(intent);
                return;
            }

            if (entidadSeleccionada(busqueda)) {
                return;
            }

            if (busqueda.getGmap() == null || busqueda.getGmap().isEmpty()) {
                Snackbar.make(view, "No tiene una ubicación definida", Snackbar.LENGTH_LONG)
                        .show();
                return;
            }

            Mantum.goGoogleMap(view.getContext(), busqueda.getGmap());
        });

        Busqueda busqueda = entity(view.getContext());

        showIcon(view, busqueda);
        showHelper(view, busqueda);

        CustomLinearLayoutManager entidadCustomLayoutManager
                = new CustomLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        entidad = view.findViewById(R.id.entidad);
        entidad.setHasFixedSize(true);
        entidad.setLayoutManager(entidadCustomLayoutManager);
        entidad.setAdapter(new DetalleAdapter(busqueda.getData()));

        CustomLinearLayoutManager variableCustomLayoutManager
                = new CustomLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        VariableAdapter variableAdapter = new VariableAdapter(view.getContext());
        variableAdapter.activarModoVer();
        variableAdapter.addAll(busqueda.getVariables());

        variables = com.mantum.component.Mantum.initRecyclerView(R.id.variables, view,
                variableCustomLayoutManager, variableAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (onCompleteListener != null) {
            onCompleteListener.onComplete(KEY_TAB);
        }
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
        return context.getString(R.string.tab_inicio);
    }

    public void onRefresh() {
        if (getView() == null) {
            return;
        }

        Busqueda busqueda = entity(getView().getContext());

        DetalleAdapter detalleAdapter = (DetalleAdapter) entidad.getAdapter();
        detalleAdapter.add(busqueda.getData());
        detalleAdapter.notifyDataSetChanged();

        if (variables != null) {
            VariableAdapter variableAdapter = (VariableAdapter) variables.getAdapter();
            if (variableAdapter != null) {
                variableAdapter.clear();
                variableAdapter.addAll(busqueda.getVariables());
                variableAdapter.notifyDataSetChanged();
            }
        }

        if (getView() != null) {
            showIcon(getView(), busqueda);
            showHelper(getView(), busqueda);
        }
    }

    @Deprecated
    private void showIcon(@NonNull View view, Busqueda busqueda) {
        if (busqueda == null) {
            return;
        }

        ImageView icono = view.findViewById(R.id.icono);
        icono.setImageResource(get(busqueda.getType()));
    }

    @Deprecated
    private int get(String type) {
        type = type == null ? "Defecto" : type;
        switch (type) {
            case Equipo.SELF:
            default:
                return R.drawable.equipo;
            case InstalacionLocativa.SELF:
                return R.drawable.locativa;
            case InstalacionProceso.SELF:
                return R.drawable.proceso;
            case BusquedaActivity.GRUPO_VARIABLE:
                return R.drawable.variable;
            case BusquedaActivity.PERSONAL:
                return R.drawable.persona;
            case BusquedaActivity.PIEZA:
                return R.drawable.pieza;
            case BusquedaActivity.COMPONENTE:
                return R.drawable.componente;
            case BusquedaActivity.RECURSO:
                return R.drawable.recursos;
            case Familia.SELF:
                return R.drawable.ic_truck;
            case Proveedor.SELF:
                return R.drawable.ic_supplier;
        }
    }

    @Deprecated
    private void showHelper(@NonNull View view, Busqueda busqueda) {
        boolean show = busqueda != null && !busqueda.getData().isEmpty();
        RelativeLayout relativeLayout = view.findViewById(R.id.empty);
        relativeLayout.setVisibility(show ? View.GONE : View.VISIBLE);

        CardView entidad = view.findViewById(R.id.entidad_contenedor);
        entidad.setVisibility(show ? View.VISIBLE : View.GONE);

        boolean showVariable = show && !busqueda.getVariables().isEmpty();
        CardView variable = view.findViewById(R.id.variables_contenedor);
        variable.setVisibility(showVariable ? View.VISIBLE : View.GONE);
    }

    @Deprecated
    class CustomLinearLayoutManager extends LinearLayoutManager {

        CustomLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override
        public boolean canScrollVertically() {
            return false;
        }
    }

    @Deprecated
    private boolean entidadSeleccionada(Busqueda busqueda) {
        if (getView() == null) {
            return false;
        }

        if (busqueda == null || busqueda.getId() == null) {
            Bundle extra = new Bundle();
            extra.putString("message", getString(R.string.search_entity));

            Intent intent = new Intent(getView().getContext(), BusquedaActivity.class);
            intent.putExtras(extra);

            startActivity(intent);
            return true;
        }
        return false;
    }

    @Deprecated
    private Busqueda entity(@NonNull Context context) {
        try {
            Database database = new Database(context);
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            Busqueda busqueda = database.where(Busqueda.class)
                    .equalTo("selected", true)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findFirst();

            busqueda = busqueda != null ? database.copyFromRealm(busqueda) : new Busqueda();
            database.close();
            return busqueda;
        } catch (Exception e) {
            Log.e("TAG", "entity: ", e);
            return new Busqueda();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Busqueda busqueda = entity(view.getContext());
        boolean busquedaNotNull = busqueda != null && busqueda.getType() != null && !busqueda.getData().isEmpty();

        FloatingActionButton qr = view.findViewById(R.id.qr);
        if (Version.check(view.getContext(), 12) && UserPermission.check(view.getContext(), UserPermission.MARCACION_EQUIPOS, true)
                && busquedaNotNull && busqueda.getType().equals(Equipo.SELF)) {
            qr.setVisibility(View.VISIBLE);

            if (!qr.hasOnClickListeners()) {
                qr.setOnClickListener(v -> {
                    IntentIntegrator integrator = new IntentIntegrator(getActivity());
                    integrator.setOrientationLocked(true);
                    integrator.setCameraId(0);
                    integrator.setPrompt("");
                    integrator.setCaptureActivity(CaptureActivityPortrait.class);
                    integrator.setBeepEnabled(false);
                    integrator.initiateScan();
                });
            }
        } else {
            qr.setVisibility(View.GONE);
        }

        FloatingActionButton otBitacora = view.findViewById(R.id.registrar_ot_bitacora);
        if (busquedaNotNull && (busqueda.getType().equals(Equipo.SELF) || busqueda.getType().equals(InstalacionLocativa.SELF))) {
            otBitacora.setVisibility(View.VISIBLE);
            otBitacora.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putLong(BitacoraActivity.KEY_ID, busqueda.getId());
                bundle.putString(BitacoraActivity.KEY_CODIGO, busqueda.getName());
                bundle.putString(BitacoraActivity.KEY_TIPO_ENTIDAD, busqueda.getType());
                bundle.putInt(BitacoraActivity.KEY_TIPO_BITACORA, BitacoraActivity.OT_BITACORA);

                Intent intent = new Intent(view.getContext(), BitacoraActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
            });
        } else {
            otBitacora.setVisibility(View.GONE);
        }

        FloatingActionButton bitacoraEvento = view.findViewById(R.id.registrar_bitacora_evento);
        if (Version.check(view.getContext(), 18) && busquedaNotNull &&
                (busqueda.getType().equals(InstalacionProceso.SELF) || busqueda.getType().equals(InstalacionLocativa.SELF) || busqueda.getType().equals(Equipo.SELF))){
            bitacoraEvento.setVisibility(View.VISIBLE);
            bitacoraEvento.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putLong(BitacoraActivity.KEY_ID, busqueda.getId());
                bundle.putString(BitacoraActivity.KEY_CODIGO, busqueda.getName());
                bundle.putString(BitacoraActivity.KEY_TIPO_ENTIDAD, busqueda.getType());
                bundle.putInt(BitacoraActivity.KEY_TIPO_BITACORA, BitacoraActivity.EVENT);

                Intent intent = new Intent(view.getContext(), BitacoraActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
            });
        } else {
            bitacoraEvento.setVisibility(View.GONE);
        }
    }
}