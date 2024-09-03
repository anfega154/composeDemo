package com.mantum.cmms.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mantum.demo.R;
import com.mantum.cmms.activity.RegistrarFallaActivity;
import com.mantum.cmms.adapter.FallaAdapter;
import com.mantum.cmms.entity.Contenedor;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.OnSelected;
import com.mantum.component.util.Tool;

import java.util.Date;
import java.util.List;

public class FallaFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Falla";

    private boolean readOnly;
    private OnCompleteListener onCompleteListener;
    private FallaAdapter<Contenedor.Falla> adapterView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_falla, container, false);

        view.findViewById(R.id.agregar_falla).setOnClickListener(v -> {
            Intent intent = new Intent(
                    view.getContext(), RegistrarFallaActivity.class);
            startActivityForResult(intent, RegistrarFallaActivity.REQUEST_ACTION);
        });

        if (readOnly) {
            view.findViewById(R.id.menu).setVisibility(View.GONE);
        }

        adapterView = new FallaAdapter<>(view.getContext());
        adapterView.setReadOnly(readOnly);
        adapterView.setOnRemove(value -> {
            adapterView.remove(value, true);
            return true;
        });
        adapterView.setOnSelected(new OnSelected<Contenedor.Falla>() {
            @Override
            public void onClick(Contenedor.Falla value, int position) {
                Intent intent = new Intent(
                        view.getContext(), RegistrarFallaActivity.class);

                // 1077716
                Log.e("TAG", "onClick: " + value.toJson() );
                intent.putExtra(RegistrarFallaActivity.MODE_EDIT, value.toJson());
                startActivityForResult(intent, RegistrarFallaActivity.REQUEST_ACTION);
            }

            @Override
            public boolean onLongClick(Contenedor.Falla value, int position) {
                return false;
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        adapterView.startAdapter(view, layoutManager);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (getView() == null) {
            return;
        }

        if (requestCode == RegistrarFallaActivity.REQUEST_ACTION) {
            if (data != null && data.getExtras() != null) {
                Bundle bundle = data.getExtras();

                Contenedor.Falla falla = bundle.getParcelable(Mantum.KEY_ID);
                if (falla != null) {
                    adapterView.add(falla, true);
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public FallaFragment setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    @NonNull
    @Override
    public String getKey() {
        return KEY_TAB;
    }

    @NonNull
    @Override
    public String getTitle(@NonNull Context context) {
        return context.getString(R.string.tab_fallas);
    }

    public boolean isOpenOT() {
        if (getView() == null) {
            return false;
        }

        SwitchCompat switchCompat = getView().findViewById(R.id.abrir);
        return switchCompat.isChecked();
    }

    public List<Contenedor.Falla> getValue() {
        if (adapterView != null) {
            return adapterView.getOriginal();
        }
        return null;
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

    public void onLoad(boolean abrirOT, List<Contenedor.Falla> fallas) {
        if (getView() == null) {
            return;
        }

        SwitchCompat switchCompat = getView().findViewById(R.id.abrir);
        switchCompat.setChecked(abrirOT);

        if (readOnly) {
            switchCompat.setEnabled(false);
        }

        adapterView.clear();
        if (fallas != null) {
            adapterView.addAll(fallas);
        }
    }
}
