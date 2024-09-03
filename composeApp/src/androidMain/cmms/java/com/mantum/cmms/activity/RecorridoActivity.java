package com.mantum.cmms.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Busqueda;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.cmms.entity.Recorrido;
import com.mantum.cmms.entity.RecorridoHistorico;
import com.mantum.cmms.fragment.RecorridoFragment;
import com.mantum.cmms.service.ATNotificationService;
import com.mantum.cmms.service.RecorridoHistoricoService;
import com.mantum.cmms.service.RecorridoService;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.adapter.TabAdapter;
import com.mantum.component.service.PhotoAdapter;

import java.util.Arrays;
import java.util.List;

public class RecorridoActivity extends Mantum.NfcActivity implements OnCompleteListener {

    private static final String TAG = RecorridoActivity.class.getSimpleName();
    public static final String OT = "OT";
    public static final String KEY_ESTADO = "KEY_ESTADO";
    public static final String QR_CODE = "QR_CODE";
    private final Gson gson = new Gson();
    private long id;
    private Database database;
    private TabAdapter tabAdapter;
    private RecorridoService recorridoService;
    private RecorridoFragment recorridoFragment;
    private ATNotificationService atNotificationService;
    private RecorridoHistoricoService recorridoHistoricoService;
    private OrdenTrabajo detalle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_tab_adapter);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            includeBackButtonAndTitle(R.string.recorrido);

            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                throw new Exception("bundle == null");
            }

            database = new Database(this);
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                throw new Exception(getString(R.string.error_authentication));
            }

            id = bundle.getLong(Mantum.KEY_ID);
            detalle = database.where(OrdenTrabajo.class)
                    .equalTo("id", id)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .equalTo("UUID", bundle.getString(Mantum.KEY_UUID))
                    .findFirst();

            if (detalle == null) {
                throw new Exception(getString(R.string.error_detail_ot));
            }

            atNotificationService = new ATNotificationService(this);
            recorridoHistoricoService = new RecorridoHistoricoService(this);
            recorridoService = new RecorridoService(this, RecorridoService.Tipo.OT);

            if (recorridoService.pendientes(detalle.getId())) {
                Recorrido recorrido = recorridoService.obtenerPendiente(detalle.getId());
                String codigo = recorrido != null ? recorrido.getCodigo() : "";
                AlertDialog builder = new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage(String.format(getString(R.string.orden_trabajo_pendiente_tecnico), codigo))
                        .setPositiveButton(R.string.aceptar, (dialog, which) -> {
                            backActivity();
                            dialog.dismiss();
                        }).create();

                builder.show();
            }

            recorridoFragment = new RecorridoFragment();
            recorridoFragment.setUUID(bundle.getString(Mantum.KEY_UUID));
            recorridoFragment.setIdentidad(detalle.getId());
            recorridoFragment.setCodigo(detalle.getCodigo());
            recorridoFragment.setSitio(detalle.getSitio());
            recorridoFragment.setSs(detalle.getSs());
            recorridoFragment.setANS(detalle.getAns());
            recorridoFragment.setEstado(bundle.getString(RecorridoActivity.KEY_ESTADO, null));
            recorridoFragment.setNFCActived(getNfcAdapter());
            recorridoFragment.setOnClickDetalle(v -> {
                bundle.putString(Mantum.KEY_UUID, detalle.getUUID());
                bundle.putLong(Mantum.KEY_ID, detalle.getId());
                bundle.putBoolean(DetalleOrdenTrabajoActivity.OCULTAR_REGISTRO_BITACORA, true);
                bundle.putBoolean(DetalleOrdenTrabajoActivity.OCULTAR_REMOVER, true);
                bundle.putBoolean(DetalleOrdenTrabajoActivity.OCULTAR_ACTUALIZAR, true);

                Intent intent = new Intent(RecorridoActivity.this, DetalleOrdenTrabajoActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
            });

            tabAdapter = new TabAdapter(getApplicationContext(), getSupportFragmentManager(),
                    Arrays.asList(recorridoFragment, new RecorridoHistoricoFragment()));

            ViewPager viewPager = findViewById(R.id.viewPager);
            viewPager.setAdapter(tabAdapter);
            viewPager.setOffscreenPageLimit(tabAdapter.getCount() - 1);

            TabLayout tabLayout = findViewById(R.id.tabs);
            tabLayout.setTabMode(TabLayout.MODE_FIXED);
            tabLayout.setupWithViewPager(viewPager);

            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        } catch (Exception e) {
            Log.e(TAG, "onCreate: ", e);
            backActivity(getString(R.string.error_app));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (data == null) {
                return;
            }

            Bundle bundle = data.getExtras();
            if (resultCode != RESULT_OK || bundle == null) {
                return;
            }

            SparseArray<PhotoAdapter> parcelable = bundle.getSparseParcelableArray(
                    GaleriaActivity.PATH_FILE_PARCELABLE);

            if (parcelable != null) {
                recorridoFragment.onCameraResult(this, parcelable);
            }

            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null && result.getContents() != null) {
                String tipo = "barcode";
                if (result.getFormatName().equals(BusquedaActivity.DATA_MATRIX) || result.getFormatName().equals(BusquedaActivity.QR_CODE)) {
                    tipo = "qrcode";
                }

                if (QR_CODE.equals(result.getFormatName())) {
                    if(result.getContents().startsWith("{")) {
                        Busqueda.Read read = gson.fromJson(result.getContents(), Busqueda.Read.class);
                        if (read.getEntityCode() == null) {
                            Snackbar.make(getView(), getString(R.string.message_search_empty), Snackbar.LENGTH_LONG)
                                    .show();
                            return;
                        }
                        if (read.getEntityId() != null) {
                            recorridoFragment.requestEntidadValidar(read.getEntityId(), detalle);
                        } else {
                            recorridoFragment.requestEntidadValidar(read.getEntityCode(), detalle, tipo);
                        }
                    } else {
                        recorridoFragment.requestEntidadValidar(result.getContents(), detalle, tipo);
                    }
                } else {
                    recorridoFragment.requestEntidadValidar(result.getContents(), detalle, tipo);
                }
            } else {
                Snackbar.make(getView(), getString(R.string.message_search_empty), Snackbar.LENGTH_LONG)
                        .show();
            }

        } catch (Exception e) {
            Log.e(TAG, "onActivityResult: ", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }

        if (recorridoService != null) {
            recorridoService.close();
        }

        if (atNotificationService != null) {
            atNotificationService.close();
        }

        if (recorridoHistoricoService != null) {
            recorridoHistoricoService.close();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.cancelar:
                Recorrido recorrido = recorridoService.obtenerActual();
                if (recorrido == null) {
                    Snackbar.make(getView(), getString(R.string.mensaje_sin_recorrido), Snackbar.LENGTH_LONG).show();
                    break;
                }

                ATNotificationService.Estado estado = atNotificationService.getEstadoActual();
                atNotificationService.cancelar((value, error) -> {
                    if (!error) {
                        backActivity(value);
                        return;
                    }

                    Snackbar.make(getView(), value, Snackbar.LENGTH_LONG)
                            .show();
                }, ATNotificationService.DISPONIBLE, false);
                break;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            super.onBackPressed();
            return;
        }

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recorrido, menu);
        return true;
    }

    @Override
    public void onComplete(@NonNull String name) {
        onRefresh(tabAdapter.getFragment(name));
    }

    private void onRefresh(@Nullable Mantum.Fragment tabFragment) {
        if (tabFragment == null) {
            return;
        }

        if (RecorridoHistoricoFragment.KEY_TAB.equals(tabFragment.getKey())) {
            if (recorridoHistoricoService != null) {
                List<RecorridoHistorico> elements = recorridoHistoricoService.findAll(id);
                ((RecorridoHistoricoFragment) tabFragment).onRefresh(elements);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent != null) {
            if(recorridoFragment.nfcActive) {
                String value = prepareNFCRead(intent, false);
                recorridoFragment.requestEntidadValidar(value, detalle, "nfc");
            }
        }
    }
}