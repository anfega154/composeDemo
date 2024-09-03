package com.mantum.cmms.activity;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.mantum.demo.R;
import com.mantum.cmms.adapter.ValidationAdapter;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.domain.ResponseInventarioActivos;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.cmms.entity.Validation;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.cmms.service.InventarioActivosService;
import com.mantum.cmms.service.OrdenTrabajoService;
import com.mantum.component.Mantum;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;
import io.realm.RealmList;

public class InventarioActivosActivity extends Mantum.Activity implements ValidationAdapter.ValidationAdapterListener{
    private List<Validation> validations = new ArrayList<>();
    private RecyclerView validationRecycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ProgressBar progressBar;
    private Cuenta cuenta;
    private Database database;

    private InventarioActivosService inventarioActivosService;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private SwipeRefreshLayout swipeRefreshLayout;
    private Long idCuenta;
    private TextView message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            database = new Database(InventarioActivosActivity.this);
            cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();
            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }
            inventarioActivosService = new InventarioActivosService(this, cuenta);
            idCuenta = (long) cuenta.getId();
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_inventario_activos);

            includeBackButtonAndTitle(R.string.inventario_activos);
            swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
            validationRecycler = findViewById(R.id.validationRecycler);
            message = findViewById(R.id.message);
            progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
            listValidations();

            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    listValidations();
                    swipeRefreshLayout.setRefreshing(false);
                }
            });

        }catch (Exception e){

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void listValidations(){
        if (!Mantum.isConnectedOrConnecting(this)) {
            Realm realm = Realm.getDefaultInstance();
            List<Validation> result = realm.where(Validation.class).findAll();
            realm.close();
            if (result.size()==0){
                message.setVisibility(View.VISIBLE);
                message.setText(R.string.empty_inventory);
            }else{
                message.setVisibility(View.GONE);
            }
            setListInventarios(result);
            return;
        }

        compositeDisposable.add(inventarioActivosService.getValidations(idCuenta)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::onNext, this::onError, () -> {
            message.setVisibility(View.GONE);
        }));
    }

    @Override
    public void OnItemClicked(int id, String code, String name) {
        Intent i = new Intent(InventarioActivosActivity.this,ValidarInventarioActivoActivity.class);
        i.putExtra("id",id);
        i.putExtra("code",code);
        i.putExtra("name",name);
        startActivity(i);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (database != null) {
            database.close();
        }
        if (inventarioActivosService != null) {
            inventarioActivosService.close();
        }
        compositeDisposable.clear();
    }

    private void onNext(@NonNull ResponseInventarioActivos resposeInventario) {
        validations = resposeInventario.getValidations();
        setListInventarios(validations);
    }

    private void setListInventarios(List<Validation> validations){
        adapter = new ValidationAdapter(validations, InventarioActivosActivity.this);
        validationRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        validationRecycler.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(this,1);
        validationRecycler.setLayoutManager(layoutManager);
        progressBar.setVisibility(View.GONE);
    }

    private void onError(@NonNull Throwable throwable) {
        if (throwable.getMessage() != null) {
            Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG)
                    .show();
        }
        progressBar.setVisibility(View.GONE);
        message.setText(R.string.empty_inventory);
    }
}