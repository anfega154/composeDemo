package com.mantum.cmms.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mantum.R;
import com.mantum.cmms.activity.CaptureActivityPortrait;
import com.mantum.cmms.activity.GaleriaActivity;
import com.mantum.cmms.adapter.ListadoConsumiblesAdapter;
import com.mantum.cmms.adapter.ListadoRepuestosAdapter;
import com.mantum.cmms.adapter.onValueChange.CustomOnValueChange;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Consumible;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.ElementoFalla;
import com.mantum.cmms.entity.Falla;
import com.mantum.cmms.entity.RepuestoManual;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.component.DatePicker;
import com.mantum.component.component.TimePicker;
import com.mantum.component.service.Photo;
import com.mantum.component.service.PhotoAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

public class BitacoraFallaFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Bitacora_Falla";

    private Database database;

    private ListadoRepuestosAdapter<RepuestoManual.Repuesto> listadoRepuestosAdapter;

    private ListadoConsumiblesAdapter<Consumible.ConsumibleHelper> listadoConsumiblesAdapter;

    private DatePicker endDate;

    private TimePicker endTime;

    private TextInputLayout contentEndDate;

    private TextInputLayout contentEndTime;

    private CheckBox checkFinished;

    private List<Photo> previousPhotosList;

    private List<Photo> laterPhotosList;

    private OnCompleteListener onCompleteListener;

    private String UUID;

    private Falla falla;

    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            super.onCreateView(inflater, container, savedInstanceState);
            view = inflater.inflate(R.layout.fragment_bitacora_falla, container, false);

            previousPhotosList = new ArrayList<>();
            laterPhotosList = new ArrayList<>();
            EditText beginDate = view.findViewById(R.id.begin_date);
            EditText fallaNombre = view.findViewById(R.id.falla_nombre);
            EditText fallaEntidad = view.findViewById(R.id.falla_entidad);
            contentEndDate = view.findViewById(R.id.content_end_date);
            contentEndTime = view.findViewById(R.id.content_end_time);

            database = new Database(view.getContext());
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            Bundle bundle = getArguments();
            if (bundle == null) {
                throw new Exception(getString(R.string.error_detalle_falla));
            }

            UUID = bundle.getString(Mantum.KEY_UUID);

            falla = database.where(Falla.class)
                    .equalTo("UUID", UUID)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findFirst();

            if (falla == null) {
                throw new Exception(getString(R.string.error_detalle_falla));
            }

            beginDate.setText(falla.getFechainicio());
            fallaNombre.setText(falla.getResumen());
            fallaEntidad.setText(falla.getEntidad());

            checkFinished = view.findViewById(R.id.check_finished);
            checkFinished.setOnCheckedChangeListener((compoundButton, b) -> {
                if (checkFinished.isChecked()) {
                    contentEndDate.setVisibility(View.VISIBLE);
                    contentEndTime.setVisibility(View.VISIBLE);

                    endDate = new DatePicker(view.getContext(), R.id.end_date);
                    endDate.setEnabled(true);
                    endDate.load();

                    endTime = new TimePicker(view.getContext(), R.id.end_time);
                    endTime.setEnabled(true);
                    endTime.load();
                } else {
                    contentEndDate.setVisibility(View.GONE);
                    contentEndTime.setVisibility(View.GONE);
                }
            });

            TextView btnAgregarRepuesto = view.findViewById(R.id.agregar_repuesto);
            btnAgregarRepuesto.setOnClickListener(view1 -> {
                listadoRepuestosAdapter.add(new RepuestoManual.Repuesto());
                listadoRepuestosAdapter.notifyItemInserted(listadoRepuestosAdapter.getItemCount());
            });

            listadoRepuestosAdapter = new ListadoRepuestosAdapter<>(view.getContext());
            RecyclerView recyclerViewRepuestos = view.findViewById(R.id.recycler_view_repuestos);
            recyclerViewRepuestos.setLayoutManager(new LinearLayoutManager(view.getContext()));
            recyclerViewRepuestos.setAdapter(listadoRepuestosAdapter);

            listadoRepuestosAdapter.setSerialVisibility(true);
            listadoRepuestosAdapter.setOnAction(new CustomOnValueChange<RepuestoManual.Repuesto>() {
                @Override
                public void onClick(RepuestoManual.Repuesto value, int position) {
                    listadoRepuestosAdapter.remove(position);
                    listadoRepuestosAdapter.notifyItemRemoved(position);
                }

                @Override
                public void onFirstTextChange(String value, int position) {
                    listadoRepuestosAdapter.getItemPosition(position).setSerial(value);
                }

                @Override
                public void onSecondTextChange(String value, int position) {
                    listadoRepuestosAdapter.getItemPosition(position).setNombre(value);
                }

                @Override
                public void onThirdTextChange(String value, int position) {
                    listadoRepuestosAdapter.getItemPosition(position).setSerialRetiro(value);
                }
            });

            if (falla.getElementos() != null && !falla.getElementos().isEmpty()) {
                for (ElementoFalla elementoFalla : falla.getElementos()) {
                    listadoRepuestosAdapter.add(new RepuestoManual.Repuesto("", elementoFalla.getNombre(), elementoFalla.getSerial()));
                }
            }

            TextView btnAgregarConsumible = view.findViewById(R.id.agregar_consumible);
            btnAgregarConsumible.setOnClickListener(view1 -> {
                listadoConsumiblesAdapter.add(new Consumible.ConsumibleHelper());
                listadoConsumiblesAdapter.notifyItemInserted(listadoConsumiblesAdapter.getItemCount());
            });

            listadoConsumiblesAdapter = new ListadoConsumiblesAdapter<>(view.getContext());
            RecyclerView recyclerViewConsumibles = view.findViewById(R.id.recycler_view_consumibles);
            recyclerViewConsumibles.setLayoutManager(new LinearLayoutManager(view.getContext()));
            recyclerViewConsumibles.setAdapter(listadoConsumiblesAdapter);

            listadoConsumiblesAdapter.setOnAction(new CustomOnValueChange<Consumible.ConsumibleHelper>() {
                @Override
                public void onClick(Consumible.ConsumibleHelper value, int position) {
                    listadoConsumiblesAdapter.remove(position);
                    listadoConsumiblesAdapter.notifyItemRemoved(position);
                }

                @Override
                public void onFirstTextChange(String value, int position) {
                    listadoConsumiblesAdapter.getItemPosition(position).setNombre(value);
                }

                @Override
                public void onSecondTextChange(String value, int position) {
                    if (value.equals("") || value.equals(".")) {
                        listadoConsumiblesAdapter.getItemPosition(position).setCantidadreal(null);
                    } else {
                        listadoConsumiblesAdapter.getItemPosition(position).setCantidadreal(Double.valueOf(value));
                    }
                }

                @Override
                public void onThirdTextChange(String value, int position) {

                }
            });

            FloatingActionButton cameraQr = view.findViewById(R.id.camera_qr);
            cameraQr.setOnClickListener(view1 -> {
                IntentIntegrator integrator = new IntentIntegrator(getActivity());
                integrator.setOrientationLocked(true);
                integrator.setCameraId(0);
                integrator.setPrompt("");
                integrator.setCaptureActivity(CaptureActivityPortrait.class);
                integrator.setBeepEnabled(false);
                integrator.initiateScan();
            });

            FloatingActionButton previousPhotos = view.findViewById(R.id.previous_photos);
            previousPhotos.setOnClickListener(view1 -> {
                Bundle bundle1 = new Bundle();
                bundle1.putSparseParcelableArray(GaleriaActivity.PATH_FILE_PARCELABLE, PhotoAdapter.factory(previousPhotosList));
                bundle1.putBoolean("isPreviousPhotosList", true);

                Intent intent = new Intent(view.getContext(), GaleriaActivity.class);
                intent.putExtras(bundle1);
                startActivityForResult(intent, GaleriaActivity.REQUEST_ACTION);
            });

            FloatingActionButton laterPhotos = view.findViewById(R.id.later_photos);
            laterPhotos.setOnClickListener(view1 -> {
                Bundle bundle1 = new Bundle();
                bundle1.putSparseParcelableArray(GaleriaActivity.PATH_FILE_PARCELABLE, PhotoAdapter.factory(laterPhotosList));
                bundle1.putBoolean("isPreviousPhotosList", false);

                Intent intent = new Intent(view.getContext(), GaleriaActivity.class);
                intent.putExtras(bundle1);
                startActivityForResult(intent, GaleriaActivity.REQUEST_ACTION);
            });

            return view;
        } catch (Exception e) {
            Log.e(TAG, "onCreateView: ", e);
            return null;
        }
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }

        Bundle bundle = data.getExtras();
        if (resultCode == RESULT_OK && bundle != null) {
            SparseArray<PhotoAdapter> parcelable = bundle.getSparseParcelableArray(GaleriaActivity.PATH_FILE_PARCELABLE);
            if (parcelable != null) {
                boolean isPreviousPhotosList = bundle.getBoolean("isPreviousPhotosList");
                if (isPreviousPhotosList) {
                    previousPhotosList.clear();
                    int total = parcelable.size();
                    if (total > 0) {
                        for (int i = 0; i < total; i++) {
                            PhotoAdapter photoAdapter = parcelable.get(i);
                            previousPhotosList.add(new Photo(view.getContext(), new File(photoAdapter.getPath()),
                                    photoAdapter.isDefaultImage(), photoAdapter.getIdCategory(),
                                    photoAdapter.getDescription()));
                        }
                    }
                } else {
                    laterPhotosList.clear();
                    int total = parcelable.size();
                    if (total > 0) {
                        for (int i = 0; i < total; i++) {
                            PhotoAdapter photoAdapter = parcelable.get(i);
                            laterPhotosList.add(new Photo(view.getContext(), new File(photoAdapter.getPath()),
                                    photoAdapter.isDefaultImage(), photoAdapter.getIdCategory(),
                                    photoAdapter.getDescription()));
                        }
                    }
                }
            } else {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                String contents = result.getContents();
                if (contents != null) {
                    listadoRepuestosAdapter.add(new RepuestoManual.Repuesto(contents, "", ""));
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onStart(Falla.Request value) {
        onRefresh(value);
    }

    public void onRefresh(Falla.Request value) {
        if (getView() == null) {
            return;
        }

        checkFinished.setChecked(value.isCorregida());
        if (checkFinished.isChecked()) {
            endDate.setValue(value.getFechafin());
            endTime.setValue(value.getHorafin());
        }
        if (value.getRepuestos() != null && !value.getRepuestos().isEmpty() && value.getUUID().equals(falla.getUUID())) {
            listadoRepuestosAdapter.clear();
            listadoRepuestosAdapter.addAll(value.getRepuestos());
        }
        if (value.getConsumibles() != null && !value.getConsumibles().isEmpty()) {
            listadoConsumiblesAdapter.addAll(value.getConsumibles());
        }
        if (value.getImagenesPrevias() != null && !value.getImagenesPrevias().isEmpty()) {
            previousPhotosList.addAll(value.getImagenesPrevias());
        }
        if (value.getImagenesPosteriores() != null && !value.getImagenesPosteriores().isEmpty()) {
            laterPhotosList.addAll(value.getImagenesPosteriores());
        }
    }

    public Falla.Request getValue() {
        if (getView() == null) {
            return null;
        }

        contentEndDate.setError(null);
        contentEndTime.setError(null);
        if (checkFinished.isChecked()) {
            if (!endDate.isValid()) {
                contentEndDate.setError(getString(R.string.fecha_final_falla_requerida));
                return null;
            }
            if (!endTime.isValid()) {
                contentEndTime.setError(getString(R.string.hora_final_falla_requerida));
                return null;
            }
        }

        ArrayList<RepuestoManual.Repuesto> repuestos = new ArrayList<>();
        if (!listadoRepuestosAdapter.isEmpty()) {
            for (int i = 0; i < listadoRepuestosAdapter.getItemCount(); i++) {
                RepuestoManual.Repuesto repuesto = listadoRepuestosAdapter.getItemPosition(i);
                String serialRepuesto = repuesto.getSerial() != null ? repuesto.getSerial() : "";
                String nombreRepuesto = repuesto.getNombre() != null ? repuesto.getNombre() : "";
                String serialRetiro = repuesto.getSerialRetiro() != null ? repuesto.getSerialRetiro() : "";

                boolean ignore = false;
                if (falla.getElementos() != null && !falla.getElementos().isEmpty()) {
                    for (ElementoFalla elementoFalla : falla.getElementos()) {
                        if ((serialRepuesto.equals("") || nombreRepuesto.equals("")) && serialRepuesto.equals(elementoFalla.getSerial())) {
                            ignore = true;
                            break;
                        }
                    }
                }

                String listPosition = String.valueOf(i + 1);
                if (ignore) {
                    continue;
                }
                if (serialRepuesto.equals("") && nombreRepuesto.equals("") && serialRetiro.equals("")) {
                    continue;
                }
                if (serialRepuesto.equals("")) {
                    Snackbar.make(view, String.format(getString(R.string.repuesto_campo_serial_vacio_bitacora), listPosition), Snackbar.LENGTH_LONG).show();
                    return null;
                }
                if (nombreRepuesto.equals("")) {
                    Snackbar.make(view, String.format(getString(R.string.repuesto_campo_nombre_vacio_bitacora), listPosition), Snackbar.LENGTH_LONG).show();
                    return null;
                }

                repuestos.add(repuesto);
            }
        }

        ArrayList<Consumible.ConsumibleHelper> consumibles = new ArrayList<>();
        if (!listadoConsumiblesAdapter.isEmpty()) {
            for (int i = 0; i < listadoConsumiblesAdapter.getItemCount(); i++) {
                Consumible.ConsumibleHelper consumible = listadoConsumiblesAdapter.getItemPosition(i);
                String nombreConsumible = consumible.getNombre() != null ? consumible.getNombre() : "";
                Double cantidadConsumible = consumible.getCantidadreal();

                String listPosition = String.valueOf(i + 1);
                if (nombreConsumible.equals("") && cantidadConsumible == null) {
                    continue;
                }
                if (nombreConsumible.equals("")) {
                    Snackbar.make(view, String.format(getString(R.string.consumible_campo_nombre_vacio_bitacora), listPosition), Snackbar.LENGTH_LONG).show();
                    return null;
                }
                if (cantidadConsumible == null) {
                    Snackbar.make(view, String.format(getString(R.string.consumible_campo_cantidad_vacio_bitacora), listPosition), Snackbar.LENGTH_LONG).show();
                    return null;
                }

                consumibles.add(consumible);
            }
        }

        Falla.Request request = new Falla.Request();
        request.setUUID(UUID);
        request.setId(falla.getId());
        request.setCorregida(checkFinished.isChecked());
        request.setRequierefoto(falla.isRequierefoto());
        request.setRequiererepuesto(falla.isRequiererepuesto());

        if (checkFinished.isChecked()) {
            request.setFechafin(endDate.getValue());
            request.setHorafin(endTime.getValue());
        }
        if (!repuestos.isEmpty()) {
            request.setRepuestos(repuestos);
        }
        if (!consumibles.isEmpty()) {
            request.setConsumibles(consumibles);
        }
        if (!previousPhotosList.isEmpty()) {
            request.setImagenesPrevias(previousPhotosList);
        }
        if (!laterPhotosList.isEmpty()) {
            request.setImagenesPosteriores(laterPhotosList);
        }
        return request;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (listadoRepuestosAdapter != null) {
            listadoRepuestosAdapter.clear();
        }

        if (listadoConsumiblesAdapter != null) {
            listadoConsumiblesAdapter.clear();
        }

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
        return context.getString(R.string.tab_general);
    }
}
