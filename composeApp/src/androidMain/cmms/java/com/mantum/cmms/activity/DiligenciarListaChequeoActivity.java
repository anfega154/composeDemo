package com.mantum.cmms.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Entidad;
import com.mantum.cmms.entity.ListaChequeo;
import com.mantum.cmms.fragment.DiligenciarListaChequeoFragment;
import com.mantum.cmms.fragment.EntidadesListaFragment;
import com.mantum.cmms.service.ListaChequeoService;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.adapter.TabAdapter;

import java.util.Arrays;
import java.util.List;

public class DiligenciarListaChequeoActivity extends Mantum.Activity implements OnCompleteListener {

    public static final int REQUEST_ACTION = 1207;

    private Database database;
    private ListaChequeo detalle;
    private TabAdapter tabAdapter;
    private ListaChequeoService listaChequeoService;
    private DiligenciarListaChequeoFragment diligenciarListaChequeoFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_tab_adapter);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            includeBackButtonAndTitle(R.string.diligenciar_ruta);

            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                throw new Exception(getString(R.string.detail_error_ruta));
            }

            database = new Database(this);
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            listaChequeoService = new ListaChequeoService(this, cuenta);
            detalle = listaChequeoService.getById(bundle.getLong(Mantum.KEY_ID));

            diligenciarListaChequeoFragment = new DiligenciarListaChequeoFragment();
            tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                    Arrays.asList(diligenciarListaChequeoFragment, new EntidadesListaFragment()));

            ViewPager viewPager = findViewById(R.id.viewPager);
            viewPager.setAdapter(tabAdapter);
            viewPager.setOffscreenPageLimit(tabAdapter.getCount() - 1);

            TabLayout tabLayout = findViewById(R.id.tabs);
            tabLayout.setTabMode(TabLayout.MODE_FIXED);
            tabLayout.setupWithViewPager(viewPager);

            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            backActivity();
        }
    }

    @Override
    public void onComplete(@NonNull String name) {
        onRefresh(tabAdapter.getFragment(name));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_done:
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void onRefresh(@Nullable Mantum.Fragment fragment) {
        if (detalle == null) {
            return;
        }

        if (fragment != null) {
            switch (fragment.getKey()) {
                case DiligenciarListaChequeoFragment.KEY_TAB:
                    ListaChequeo current = detalle.isManaged()
                            ? database.copyFromRealm(detalle)
                            : detalle;

                    if (diligenciarListaChequeoFragment != null) {
                        diligenciarListaChequeoFragment.onStart(current);
                    }
                    break;

                case EntidadesListaFragment.KEY_TAB:
                    if (detalle.getEntidades() == null || detalle.getEntidades().isEmpty()) {
                        break;
                    }

                    List<Entidad> entidades = detalle.getEntidades().isManaged()
                            ? database.copyFromRealm(detalle.getEntidades())
                            : detalle.getEntidades();

                    EntidadesListaFragment entidadesListaFragment = (EntidadesListaFragment) fragment;
                    entidadesListaFragment.onLoad(entidades);
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (database != null) {
            database.close();
        }

        if (listaChequeoService != null) {
            listaChequeoService.close();
        }
    }
}
