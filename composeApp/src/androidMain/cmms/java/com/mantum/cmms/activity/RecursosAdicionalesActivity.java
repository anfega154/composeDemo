package com.mantum.cmms.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.mantum.demo.R;
import com.mantum.cmms.fragment.RecursoAdicionalFragment;
import com.mantum.cmms.helper.RecursoAdicionalHelper;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

public class RecursosAdicionalesActivity extends Mantum.Activity
        implements OnCompleteListener {

    private static final String TAG = RecursosAdicionalesActivity.class.getSimpleName();

    public static final int REQUEST_ACTION = 1297;

    public static final String KEY_RESOURCES = "key_resources";

    private RecursoAdicionalFragment recursoAdicionalFragment;

    private RecursoAdicionalHelper recursoAdicionalHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            includeBackButtonAndTitle(R.string.recursos_adicionales);

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                recursoAdicionalHelper
                        = (RecursoAdicionalHelper) bundle.getSerializable(KEY_RESOURCES);
            }

            recursoAdicionalFragment = new RecursoAdicionalFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, recursoAdicionalFragment)
                    .commit();

        } catch (Exception e) {
            backActivity(getString(R.string.error_app));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_search:
                Intent intent = new Intent(
                        this, RecursosAdicionalesBuscarActivity.class);
                startActivityForResult(intent, RecursosAdicionalesBuscarActivity.REQUEST_ACTION);
                break;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recursos_adicionales, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        RecursoAdicionalHelper recursoAdicionalHelper
                = RecursoAdicionalHelper.adapter(recursoAdicionalFragment.getValue());

        Intent intent = new Intent();
        intent.putExtra(KEY_RESOURCES, recursoAdicionalHelper);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onComplete(@NonNull String name) {
        if (recursoAdicionalFragment != null && recursoAdicionalHelper != null) {
            recursoAdicionalFragment.addResources(recursoAdicionalHelper.getRecursos());
        }
    }
}