package com.mantum.component;

import android.widget.TextView;

public interface OnValueChange<T> {

    void onClick(T value, int position);
    boolean onChange(T value, TextView position);
    void onTextChange(Float value, int position);
}