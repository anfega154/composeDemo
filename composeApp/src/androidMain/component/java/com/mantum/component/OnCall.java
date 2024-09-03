package com.mantum.component;

import android.view.MenuItem;

public interface OnCall<T> {

    boolean onSelected(MenuItem menu, T value);
}