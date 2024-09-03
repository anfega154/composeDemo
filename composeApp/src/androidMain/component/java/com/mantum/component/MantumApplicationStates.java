package com.mantum.component;

import android.app.Application;
import android.content.Context;
import androidx.multidex.MultiDex;

public class MantumApplicationStates extends Application {

    public MantumApplicationStates() {
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private boolean reloadFragment1;

    public boolean isReloadFragment1() { return reloadFragment1; }

    public void setReloadFragment1(boolean reloadFragment1) { this.reloadFragment1 = reloadFragment1; }
}