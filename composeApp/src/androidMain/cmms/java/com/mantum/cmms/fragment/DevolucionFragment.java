package com.mantum.cmms.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mantum.demo.R;
import com.mantum.cmms.entity.Devolucion;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.AlphabetAdapter;

import java.util.List;

public class DevolucionFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Devolucion";

    private OnCompleteListener onCompleteListener;

    private AlphabetAdapter<Devolucion> alphabetAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(com.mantum.demo.R.layout.alphabet_layout_view,
                container, false);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(view.getContext());

        alphabetAdapter = new AlphabetAdapter<>(view.getContext());
        alphabetAdapter.startAdapter(view, layoutManager);
        alphabetAdapter.showMessageEmpty(view);

        alphabetAdapter.setOnAction(new OnSelected<Devolucion>() {

            @Override
            public void onClick(Devolucion value, int position) {
                View form = View.inflate(view.getContext(), R.layout.form_devolucion, null);
                TextInputEditText codigo = form.findViewById(R.id.codigo);
                codigo.setText(value.getCodigo());

                TextInputEditText cantidad = form.findViewById(R.id.cantidad);
                cantidad.setText(value.getCantidad());

                TextInputEditText descripcion = form.findViewById(R.id.descripcion);
                descripcion.setText(value.getDescripcion());

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setView(form);
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.aceptar, (dialog, which) -> {
                    if (codigo.getText() == null || codigo.getText().toString().isEmpty()) {
                        Snackbar.make(view, R.string.requiere_codigo, Snackbar.LENGTH_LONG)
                                .show();
                        return;
                    }

                    if (cantidad.getText() == null || cantidad.getText().toString().isEmpty()) {
                        Snackbar.make(view, R.string.requiere_cantidad, Snackbar.LENGTH_LONG)
                                .show();
                        return;
                    }

                    Devolucion devolucion = new Devolucion();
                    devolucion.setCodigo(codigo.getText().toString());
                    devolucion.setCantidad(cantidad.getText().toString());
                    if (descripcion.getText() != null) {
                        devolucion.setDescripcion(descripcion.getText().toString());
                    }

                    add(position, devolucion);
                });

                builder.setNegativeButton(R.string.cancelar, (dialog, which) -> dialog.dismiss());
                builder.create();
                builder.show();
            }

            @Override
            public boolean onLongClick(Devolucion value, int position) {
                androidx.appcompat.app.AlertDialog.Builder builder
                        = new androidx.appcompat.app.AlertDialog.Builder(view.getContext());
                builder.setTitle(R.string.titulo_eliminar_devolucion);
                builder.setMessage(R.string.mensaje_eliminar_devolucion);
                builder.setCancelable(true);
                builder.setNegativeButton(R.string.aceptar, (dialog, which) -> {
                    alphabetAdapter.remove(position, true);
                    alphabetAdapter.showMessageEmpty(view);
                });
                builder.setPositiveButton(R.string.cancelar, (dialog, which) -> dialog.dismiss());
                builder.create();
                builder.show();
                return true;
            }
        });

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alphabetAdapter != null) {
            alphabetAdapter.clear();
        }
    }

    public void onRefresh(@NonNull List<Devolucion> values) {
        if (alphabetAdapter != null) {
            alphabetAdapter.sort(values);
            alphabetAdapter.addAll(values);

            if (getView() != null) {
                alphabetAdapter.showMessageEmpty(getView());
            }
        }
    }

    public void add(@NonNull Devolucion value) {
        if (alphabetAdapter != null) {
            alphabetAdapter.add(value);
            alphabetAdapter.sort();
            alphabetAdapter.refresh();

            if (getView() != null) {
                alphabetAdapter.showMessageEmpty(getView());
            }
        }
    }

    public void add(int index, @NonNull Devolucion value) {
        if (alphabetAdapter != null) {
            alphabetAdapter.set(index, value);
            alphabetAdapter.sort();
            alphabetAdapter.refresh();

            if (getView() != null) {
                alphabetAdapter.showMessageEmpty(getView());
            }
        }
    }

    public void add(SparseArray<Devolucion> devoluciones) {
        if (alphabetAdapter != null) {
            alphabetAdapter.addAll(devoluciones);
            alphabetAdapter.sort();
            alphabetAdapter.refresh();

            if (getView() != null) {
                alphabetAdapter.showMessageEmpty(getView());
            }
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
        return context.getString(R.string.tab_devolucion);
    }

    @NonNull
    public SparseArray<Devolucion> getOriginal() {
        SparseArray<Devolucion> results = new SparseArray<>();
        if (alphabetAdapter == null) {
            return results;
        }

        for (int i = 0; i < alphabetAdapter.getOriginal().size(); i++) {
            results.append(i, alphabetAdapter.getOriginal().get(i));
        }

        return results;
    }
}