package com.mantum.cmms.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Tarea;
import com.mantum.cmms.fragment.DetalleTareaFragment;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.adapter.TabAdapter;

import java.util.Collections;

public class DetalleTareaActivity extends Mantum.Activity implements OnCompleteListener {

    public static String TAREA_ENTIDAD_OT = "tarea_entidad_ot";

    private Database database;

    private Tarea detalle;

    private TabAdapter tabAdapter;

    private boolean tareaEntidadOt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_detalle);

            database = new Database(this);
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                throw new Exception(getString(R.string.detail_error_tarea));
            }

            detalle = database.where(Tarea.class)
                    .equalTo("uuid", bundle.getString(Mantum.KEY_UUID))
                    .findFirst();

            if (detalle == null) {
                throw new Exception(getString(R.string.error_detalle_tarea));
            }

            tareaEntidadOt = bundle.getBoolean(TAREA_ENTIDAD_OT, false);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            includeBackButtonAndTitle(detalle.getCodigo());

            tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                    Collections.singletonList(new DetalleTareaFragment()));

            ViewPager viewPager = findViewById(R.id.viewPager);
            viewPager.setAdapter(tabAdapter);
            viewPager.setOffscreenPageLimit(tabAdapter.getCount() - 1);

            TabLayout tabLayout = findViewById(R.id.tabs);
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            tabLayout.setupWithViewPager(viewPager);
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    onRefresh(tabAdapter.getFragment(tab.getPosition()));
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }

            });

            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        } catch (Exception e) {
            Log.e("TAG", "onCreate: ", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onComplete(@NonNull String name) {
        onRefresh(tabAdapter.getFragment(name));
    }

    private void onRefresh(@Nullable Mantum.Fragment tabFragment) {
        if (tabFragment == null) {
            return;
        }

        if (DetalleTareaFragment.KEY_TAB.equals(tabFragment.getKey())) {
            Tarea tarea = detalle.isLoaded()
                    ? database.copyFromRealm(detalle)
                    : detalle;

            DetalleTareaFragment detalleTareaFragment = (DetalleTareaFragment) tabFragment;
            detalleTareaFragment.onLoad(tarea, tareaEntidadOt);
        }
    }
}
