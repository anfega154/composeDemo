package com.mantum.cmms.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mantum.demo.R;
import com.mantum.cmms.entity.Personal;
import com.mantum.component.Mantum;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.AlphabetAdapter;
import com.mantum.component.adapter.MovableFloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ListaPersonalActivity extends Mantum.Activity {
    public static String KEY_ENTITY = "entity";

    private String lastSearch;
    private AlphabetAdapter<Personal> alphabet;

    private final ActivityResultLauncher<Intent> personActivityResultLauncher
            = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data == null) {
                return;
            }

            Bundle extras = data.getExtras();
            if (extras == null) {
                return;
            }

            long id = extras.getLong("Id");
            String name = extras.getString("Nombre");
            String document = extras.getString("Cedula");
            String group = extras.getString("Grupo");

            lastSearch = extras.getString("Busqueda");

            boolean exists = false;
            List<Personal> original = alphabet.getOriginal();
            for (Personal personal : original) {
                if (personal.getCedula().equals(document)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                Personal personal = new Personal();
                personal.setId(id);
                personal.setNombre(name);
                personal.setCedula(document);
                personal.setGrupo(group);
                personal.setUuid(UUID.randomUUID().toString());

                alphabet.add(personal);
                alphabet.refresh();
            }

            isEmptyList();
        }
    });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            includeBackButtonAndTitle(R.string.personal);
            setContentView(R.layout.activity_busqueda_personal);

            ArrayList<Personal> persons = getPersonals();

            alphabet = new AlphabetAdapter<>(this);
            alphabet.addAll(persons);

            alphabet.setOnAction(new OnSelected<Personal>() {

                @Override
                public void onClick(final Personal value, int position) {
                    remove(value);
                }

                @Override
                public boolean onLongClick(Personal value, int position) {
                    return true;
                }
            });

            isEmptyList();

            MovableFloatingActionButton floatingActionButton = findViewById(R.id.add);
            floatingActionButton.setOnClickListener(v -> {
                Bundle bundle69 = new Bundle();
                bundle69.putString(
                        PersonalAutocompleteActivity.LAST_SEARCH, lastSearch);

                Intent intent = new Intent(this, PersonalAutocompleteActivity.class);
                intent.putExtras(bundle69);

                Log.e("TAG", "onCreate: " + lastSearch );
                personActivityResultLauncher.launch(intent);
            });

            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            alphabet.startAdapter(getView(), layoutManager);

        } catch (Exception e) {
            backActivity(getString(R.string.error_app));
        }
    }

    @NonNull
    private ArrayList<Personal> getPersonals() {
        ArrayList<Personal> persons = new ArrayList<>();

        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            return persons;
        }

        SparseArray<Parcelable> entities = bundle.getSparseParcelableArray(KEY_ENTITY);
        if (entities != null) {
            for (int i = 0; i < entities.size(); i++) {
                Personal person = (Personal) entities.get(i);
                persons.add(person);
            }
        }

        return persons;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        SparseArray<Personal> personas = prepare();

        Bundle bundle = new Bundle();
        bundle.putSparseParcelableArray(KEY_ENTITY, personas);

        Intent intent = new Intent();
        intent.putExtras(bundle);
        backActivity(intent);
    }

    @NonNull
    private SparseArray<Personal> prepare() {
        SparseArray<Personal> results = new SparseArray<>();
        for (int i = 0; i < alphabet.getOriginal().size(); i++) {
            results.append(i, alphabet.getOriginal().get(i));
        }
        return results;
    }

    private void isEmptyList() {
        RelativeLayout container = findViewById(R.id.empty);
        container.setVisibility(alphabet.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void remove(Personal value) {
        if (getView() == null) {
            return;
        }

        AlertDialog.Builder alertDialogBuilder
                = new AlertDialog.Builder(getView().getContext());
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setTitle(R.string.eliminar_persona);
        alertDialogBuilder.setMessage(R.string.mansaje_eliminar_personal);
        alertDialogBuilder.setNegativeButton(getString(R.string.aceptar), (dialogInterface, i) -> {
            alphabet.remove(value, true);
            isEmptyList();

            dialogInterface.cancel();
        });

        alertDialogBuilder.setPositiveButton(getString(R.string.cancelar), (dialogInterface, i) -> dialogInterface.cancel());
        alertDialogBuilder.show();
    }
}