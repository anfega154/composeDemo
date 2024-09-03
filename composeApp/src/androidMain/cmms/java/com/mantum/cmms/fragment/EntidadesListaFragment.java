package com.mantum.cmms.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mantum.R;
import com.mantum.cmms.activity.DetalleActividadMantenimientoActivity;
import com.mantum.cmms.activity.DetalleTareaActivity;
import com.mantum.cmms.entity.Actividad;
import com.mantum.cmms.entity.Entidad;
import com.mantum.component.Mantum;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.GroupAdapter;
import com.mantum.component.OnCompleteListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EntidadesListaFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Entidades";

    private OnCompleteListener onCompleteListener;

    private GroupAdapter<Entidad, Actividad> groupAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(com.mantum.component.R.layout.group_layout_view, container, false);

        groupAdapter = new GroupAdapter<>(view.getContext());
        groupAdapter.setOnAction(new OnSelected<Actividad>() {

            @Override
            public void onClick(Actividad value, int position) {
                Bundle bundle = new Bundle();
                bundle.putString(Mantum.KEY_ID, value.getUuid());
                bundle.putBoolean(DetalleTareaActivity.TAREA_ENTIDAD_OT, true);

                Intent intent = new Intent(view.getContext(), DetalleActividadMantenimientoActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
            }

            @Override
            public boolean onLongClick(Actividad value, int position) {
                return false;
            }
        });

        ExpandableListView expandableListView = view.findViewById(R.id.expandableListView);
        expandableListView.setAdapter(groupAdapter);

        showMessageEmpty();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (onCompleteListener != null) {
            onCompleteListener.onComplete(KEY_TAB);
        }
    }

    @NonNull
    @Override
    public String getKey() {
        return KEY_TAB;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        onCompleteListener = (OnCompleteListener) context;
    }

    @NonNull
    @Override
    public String getTitle(@NonNull Context context) {
        return context.getString(R.string.entidades);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (groupAdapter != null) {
            groupAdapter.clear();
        }

        showMessageEmpty();
    }

    public void onLoad(@Nullable Entidad value) {
        ArrayList<Entidad> values = new ArrayList<>();
        if (value != null) {
            values.add(value);
        }
        onLoad(values);
    }

    public void onLoad(@NonNull List<Entidad> values) {
        if (groupAdapter != null) {
            Collections.sort(values, EntidadesListaFragment::compare);
            for (Entidad value : values) {
                Collections.sort(value.getActividades(), EntidadesListaFragment::compare);
            }
            groupAdapter.addAll(values, false);
        }
        showMessageEmpty();
    }

    public void showMessageEmpty() {
        if (getView() == null) {
            return;
        }

        RelativeLayout relativeLayout = getView().findViewById(R.id.empty);
        relativeLayout.setVisibility(
                groupAdapter == null || groupAdapter.isEmpty() ? View.VISIBLE : View.GONE);

        TextView message = getView().findViewById(R.id.message);
        message.setText(getView().getContext().getString(R.string.entidades_mensaje_vacio));
    }

    private static int compare(@NonNull Actividad a, @NonNull Actividad b) {
        if (a.getOrden() == b.getOrden()) {
            return 0;
        }
        return a.getOrden() > b.getOrden() ? 1 : -1;
    }

    public static int compare(@NonNull Entidad a, @NonNull Entidad b) {
        if (a.getOrden() == b.getOrden()) {
            return 0;
        }
        return a.getOrden() > b.getOrden() ? 1 : -1;
    }
}