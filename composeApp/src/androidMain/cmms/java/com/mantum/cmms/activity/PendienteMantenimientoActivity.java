package com.mantum.cmms.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.MenuItem;

import com.mantum.R;
import com.mantum.cmms.entity.PendienteMantenimiento;
import com.mantum.cmms.fragment.PendienteFragment;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

public class PendienteMantenimientoActivity extends Mantum.Activity implements OnCompleteListener {

    private static final String TAG = PendienteMantenimientoActivity.class.getSimpleName();

    public static final String KEY_FORM = "key_form";

    private PendienteFragment pendienteFragment;

    private PendienteMantenimiento.Request pendienteMantenimiento;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            includeBackButtonAndTitle(R.string.pendiente);

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                pendienteMantenimiento
                        = (PendienteMantenimiento.Request) bundle.getSerializable(KEY_FORM);
            }

            pendienteFragment = new PendienteFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, pendienteFragment)
                    .commit();

        } catch (Exception e) {
            backActivity(getString(R.string.error_app));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        if (pendienteFragment != null) {
            PendienteMantenimiento.Request pendienteMantenimiento = pendienteFragment.getValue();
            Bundle bundle = new Bundle();
            bundle.putSerializable(KEY_FORM, pendienteMantenimiento);
            intent.putExtras(bundle);
        }
        backActivity(intent);
    }

    @Override
    public void onComplete(@NonNull String name) {
        if (pendienteFragment != null && pendienteMantenimiento != null) {
            pendienteFragment.onStart(pendienteMantenimiento);
        }
    }
}