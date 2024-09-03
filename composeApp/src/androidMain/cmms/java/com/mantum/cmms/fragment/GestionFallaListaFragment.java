package com.mantum.cmms.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mantum.demo.R;
import com.mantum.cmms.activity.GestionFallaActivity;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Falla;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.component.Mantum;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.AlphabetAdapter;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

public class GestionFallaListaFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Gestion_Lista_Fallas";

    private BitacoraFallaFragment bitacoraFallaFragment;

    private Database database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            super.onCreateView(inflater, container, savedInstanceState);
            View view = inflater.inflate(R.layout.alphabet_layout_view, container, false);

            database = new Database(view.getContext());
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
            AlphabetAdapter<Falla> alphabetAdapter = new AlphabetAdapter<>(view.getContext());
            alphabetAdapter.startAdapter(view, layoutManager);

            Bundle bundle = getArguments();
            if (bundle != null) {
                OrdenTrabajo ordentrabajo = database.where(OrdenTrabajo.class)
                        .equalTo("id", bundle.getLong(Mantum.KEY_ID))
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();

                if (ordentrabajo != null) {
                    alphabetAdapter.addAll(ordentrabajo.getFallas());
                    alphabetAdapter.showMessageEmpty(view, R.string.fallas_mensaje_vacio);
                    alphabetAdapter.setOnAction(new OnSelected<Falla>() {
                        @Override
                        public void onClick(Falla value, int position) {
                            if (getFragmentManager() != null) {
                                Bundle bundle1 = new Bundle();
                                bundle1.putString(Mantum.KEY_UUID, value.getUUID());

                                bitacoraFallaFragment = new BitacoraFallaFragment();
                                bitacoraFallaFragment.setArguments(bundle1);
                                getFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, bitacoraFallaFragment)
                                        .commitNow();

                                Falla.Request falla = (Falla.Request) bundle.getSerializable(GestionFallaActivity.KEY_FORM);
                                if (falla != null && bitacoraFallaFragment.isVisible()) {
//                                    falla.setUUID(value.getUUID());
                                    bitacoraFallaFragment.onStart(falla);
                                }
                            }

                        }

                        @Override
                        public boolean onLongClick(Falla value, int position) {
                            return false;
                        }
                    });
                }
            }

            return view;
        } catch (Exception e) {
            Log.e(TAG, "onCreateView: ", e);
            return null;
        }
    }

    public Falla.Request getValue() {
        if (bitacoraFallaFragment != null) {
            return bitacoraFallaFragment.getValue();
        }
        return null;
    }

    public BitacoraFallaFragment getBitacoraFallaFragment() {
        return bitacoraFallaFragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (bitacoraFallaFragment != null) {
            bitacoraFallaFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (database != null) {
            database.close();
        }
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
}
