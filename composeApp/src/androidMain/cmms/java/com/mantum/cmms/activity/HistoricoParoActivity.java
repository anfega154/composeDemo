package com.mantum.cmms.activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.mantum.demo.R;
import com.mantum.cmms.adapter.HistoricoParoAdapter;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Paro;
import com.mantum.component.Mantum;

import java.util.List;

import io.realm.Sort;

public class HistoricoParoActivity extends Mantum.Activity {

    private Database database;

    HistoricoParoAdapter<Paro> historicoParoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_historico_paro);
            includeBackButtonAndTitle(R.string.historico_paros_titulo);

            database = new Database(this);
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            historicoParoAdapter = new HistoricoParoAdapter<>(this);
            RecyclerView recyclerViewHistorico = findViewById(R.id.recycler_view_historico);
            recyclerViewHistorico.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewHistorico.setAdapter(historicoParoAdapter);

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                List<Paro> paros = database.where(Paro.class)
                        .equalTo("idequipo", bundle.getLong(Mantum.KEY_ID))
                        .sort("fechainicio", Sort.DESCENDING)
                        .findAll();

                historicoParoAdapter.addAll(paros);
            }

        } catch (Exception e) {
            Log.e("TAG", "onCreate: ", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (historicoParoAdapter != null) {
            historicoParoAdapter.clear();
        }

        if (database != null) {
            database.close();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }
}