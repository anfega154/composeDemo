package com.mantum.cmms.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.MenuItem;

import com.mantum.demo.R;
import com.mantum.cmms.fragment.EventoFragment;
import com.mantum.component.Mantum;

public class EventoActivity extends Mantum.Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            includeBackButtonAndTitle(R.string.tab_evento);

            EventoFragment eventoFragment = new EventoFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, eventoFragment)
                    .commit();
        } catch (Exception e) {
            backActivity(getString(R.string.error_app));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }
}