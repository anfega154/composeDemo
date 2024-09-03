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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mantum.demo.R;
import com.mantum.cmms.activity.RegisterDamageActivity;
import com.mantum.cmms.adapter.DamageAdapter;
import com.mantum.cmms.entity.Contenedor;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.OnSelected;
import com.mantum.component.util.Tool;

import java.util.Date;
import java.util.List;

public class DamageFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "DamageFragment";

    private boolean readOnly;
    private OnCompleteListener onCompleteListener;
    private DamageAdapter<Contenedor.Damage> adapterView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_damage, container, false);

        view.findViewById(R.id.agregar_damage).setOnClickListener(v -> {
            Intent intent = new Intent(
                    view.getContext(), RegisterDamageActivity.class);

            startActivityForResult(intent, RegisterDamageActivity.REQUEST_ACTION);
        });

        if (readOnly) {
            view.findViewById(R.id.menu).setVisibility(View.GONE);
        }

        adapterView = new DamageAdapter<>(view.getContext());
        adapterView.setReadOnly(readOnly);
        adapterView.setOnRemove(value -> {
            adapterView.remove(value, true);
            return true;
        });

        adapterView.setOnSelected(new OnSelected<Contenedor.Damage>() {
            @Override
            public void onClick(Contenedor.Damage value, int position) {
                Intent intent = new Intent(
                        view.getContext(), RegisterDamageActivity.class);

                intent.putExtra(RegisterDamageActivity.MODE_EDIT, value.toJson());
                startActivityForResult(intent, RegisterDamageActivity.REQUEST_ACTION);
            }

            @Override
            public boolean onLongClick(Contenedor.Damage value, int position) {
                return false;
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        adapterView.startAdapter(view, layoutManager);

        return view;
    }

    public DamageFragment setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (getView() == null) {
            return;
        }

        if (requestCode == RegisterDamageActivity.REQUEST_ACTION) {
            if (data != null && data.getExtras() != null) {
                Bundle bundle = data.getExtras();

                Contenedor.Damage damage = bundle.getParcelable(Mantum.KEY_ID);
                if (damage != null) {
                    damage.setFecha(Tool.yyyymmdd(new Date()));
                    adapterView.add(damage, true);
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
        return context.getString(R.string.tab_damage);
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

    public List<Contenedor.Damage> getValue() {
        if (adapterView != null) {
            return adapterView.getOriginal();
        }
        return null;
    }

    public void onLoad(List<Contenedor.Damage> damages) {
        adapterView.clear();
        adapterView.addAll(damages);
    }
}
