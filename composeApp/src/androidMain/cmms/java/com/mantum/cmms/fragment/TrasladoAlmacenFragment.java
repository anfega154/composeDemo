package com.mantum.cmms.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Bodega;
import com.mantum.cmms.entity.TrasladoAlmacen;
import com.mantum.cmms.entity.TrasladoItems;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.adapter.ItemsAlmacenAdapter;


import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

public class TrasladoAlmacenFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "TrasladoAlmacen";
    private boolean action;
    private Database database;
    private OnCompleteListener onCompleteListener;
    private ItemsAlmacenAdapter<TrasladoItems> alphabetAdapter;
    private LinearLayoutManager layoutManager;
    private RecyclerView recyclerView;

    public TrasladoAlmacenFragment() {
        this.action = true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_traslado_almacen, container, false);
        database = new Database(this.getContext());
        alphabetAdapter = new ItemsAlmacenAdapter<>(this.getContext());
        alphabetAdapter.setActionTransaction(false);

        layoutManager = new LinearLayoutManager(this.getContext());
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(alphabetAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onCompleteListener.onComplete(KEY_TAB);
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
        return context.getString(R.string.tab_general);
    }

    public TrasladoAlmacenFragment setAction(@SuppressWarnings("SameParameterValue") boolean action) {
        this.action = action;
        return this;
    }

    public void onView(TrasladoAlmacen trasladoAlmacen) {
        if (getView() == null) {
            return;
        }

        Realm realm = database.instance();
        Bodega bodegaDestino = realm.where(Bodega.class)
                .equalTo("id", trasladoAlmacen.getIdbodegadestino())
                .findFirst();

        ArrayList<String> bodegasList = new ArrayList<>();
        bodegasList.add(bodegaDestino.getNombre());
        Spinner spinnerView = getView().findViewById(R.id.mSpinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, bodegasList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerView.setAdapter(arrayAdapter);

        SwitchCompat aip = getView().findViewById(R.id.ejecutar);
        aip.setChecked(trasladoAlmacen.getActivosIP());

        EditText obs = getView().findViewById(R.id.observacion);
        obs.setText(trasladoAlmacen.getObservacion());

        List<TrasladoItems> items = trasladoAlmacen.getElementos();
        alphabetAdapter.addAll(items);
    }

}