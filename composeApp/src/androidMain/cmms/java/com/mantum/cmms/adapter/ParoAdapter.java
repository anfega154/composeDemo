package com.mantum.cmms.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.RecyclerView;

import com.mantum.R;
import com.mantum.cmms.adapter.handler.ParoHandler;
import com.mantum.cmms.adapter.onValueChange.ParoOnValueChange;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.Spinner;
import com.mantum.cmms.entity.ClasificacionParo;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.TipoParo;
import com.mantum.component.Mantum;
import com.mantum.component.component.TimePicker;

import java.util.ArrayList;
import java.util.List;

public class ParoAdapter<T extends ParoHandler<T>> extends Mantum.Adapter<T, ParoAdapter.ViewHolder> {

    private ParoOnValueChange onValueChange;

    private final Context context;

    private final Database database;

    public ParoAdapter(@NonNull Context context) {
        super(context);
        this.context = context;
        this.database = new Database(context);
    }

    @Nullable
    @Override
    public T getItemPosition(int position) {
        return super.getItemPosition(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_paro, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        T adapter = getItemPosition(position);

        if (adapter == null) {
            return;
        }

        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return;
        }

        TimePicker horaInicioPicker = new TimePicker(context, holder.horaInicio);
        horaInicioPicker.setEnabled(true);
        horaInicioPicker.load();

        TimePicker horaFinPicker = new TimePicker(context, holder.horaFin);
        horaFinPicker.setEnabled(true);
        horaFinPicker.load();

        List<ClasificacionParo> clasificaciones = database.where(ClasificacionParo.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .findAll();

        List<TipoParo> tipos = database.where(TipoParo.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .findAll();

        List<Spinner> clasificacionesSpinnerList = new ArrayList<>();
        clasificacionesSpinnerList.add(new Spinner("0", "Seleccione la clasificación"));
        for (ClasificacionParo clasificacion : clasificaciones) {
            clasificacionesSpinnerList.add(new Spinner(clasificacion.getId(), clasificacion.getNombre()));
        }

        List<Spinner> tiposSpinnerList = new ArrayList<>();
        tiposSpinnerList.add(new Spinner("0", "Seleccione el tipo"));
        for (TipoParo tipo : tipos) {
            Spinner spinner = new Spinner(
                    String.valueOf(tipo.getId()),
                    tipo.getNombre());

            spinner.setIdentidad(tipo.getId());
            tiposSpinnerList.add(spinner);
        }

        ArrayAdapter<Spinner> clasificacionSpinnerAdapter = new ArrayAdapter<>(context, R.layout.custom_simple_spinner, R.id.item, clasificacionesSpinnerList);
        holder.clasificacion.setAdapter(clasificacionSpinnerAdapter);

        ArrayAdapter<Spinner> tipoSpinnerAdapter = new ArrayAdapter<>(context, R.layout.custom_simple_spinner, R.id.item, tiposSpinnerList);
        holder.tipo.setAdapter(tipoSpinnerAdapter);


        horaInicioPicker.setValue(adapter.getHoraInicio());
        horaFinPicker.setValue(adapter.getHoraFin());

        for (int i = 0; i < clasificacionSpinnerAdapter.getCount(); i++) {
            Spinner spinner = clasificacionSpinnerAdapter.getItem(i);
            if (spinner.getKey() != null && spinner.getKey().equals(adapter.getClasificacion())) {
                holder.clasificacion.setSelection(i);
                break;
            }
        }

        for (int i = 0; i < tipoSpinnerAdapter.getCount(); i++) {
            Spinner spinner = tipoSpinnerAdapter.getItem(i);
            if (spinner.getIdentidad() != null && spinner.getIdentidad().equals(adapter.getTipo())) {
                holder.tipo.setSelection(i);
                break;
            }
        }

        holder.btnRemoverParo.setOnClickListener(view -> onValueChange.onClick(holder.getAdapterPosition()));
        holder.btnRemoverHoraFin.setOnClickListener(view -> holder.horaFin.setText(""));

        holder.horaInicio.addTextChangedListener(customTextWatcher(holder.horaInicio, holder));
        holder.horaFin.addTextChangedListener(customTextWatcher(holder.horaFin, holder));

        holder.clasificacion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (position > 0) {
                    Spinner spinner = clasificacionSpinnerAdapter.getItem(position);
                    onValueChange.onClasificationChange(spinner.getKey(), holder.getAdapterPosition());
                } else {
                    onValueChange.onClasificationChange("", holder.getAdapterPosition());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        holder.tipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (position > 0) {
                    Spinner spinner = tipoSpinnerAdapter.getItem(position);
                    onValueChange.onTypeChange(spinner.getIdentidad(), holder.getAdapterPosition());
                } else {
                    onValueChange.onTypeChange(0L, holder.getAdapterPosition());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void setOnAction(ParoOnValueChange onAction) {
        this.onValueChange = onAction;
    }

    private TextWatcher customTextWatcher(EditText editText, ViewHolder holder) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (editText == holder.horaInicio) {
                    onValueChange.onTimeStartChange(charSequence.length() == 0 ? "" : charSequence.toString(), holder.getAdapterPosition());
                } if (editText == holder.horaFin) {
                    onValueChange.onTimeEndChange(charSequence.length() == 0 ? "" : charSequence.toString(), holder.getAdapterPosition());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        EditText horaInicio;
        EditText horaFin;
        AppCompatSpinner clasificacion;
        AppCompatSpinner tipo;
        ImageView btnRemoverParo;
        ImageView btnRemoverHoraFin;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            horaInicio = itemView.findViewById(R.id.hora_inicio);
            horaFin = itemView.findViewById(R.id.hora_fin);
            clasificacion = itemView.findViewById(R.id.clasificacion);
            tipo = itemView.findViewById(R.id.tipo);
            btnRemoverParo = itemView.findViewById(R.id.remover_paro);
            btnRemoverHoraFin = itemView.findViewById(R.id.remover_hora_fin);
        }
    }
}
