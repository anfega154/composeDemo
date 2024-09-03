package com.mantum.component.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import android.widget.ArrayAdapter;

import java.util.List;

public class SpinnerAdapter<T> {

    private final ArrayAdapter<T> adapter;

    public SpinnerAdapter(@NonNull Context context, @NonNull List<T> values) {
        this.adapter = new ArrayAdapter<>(
                context, com.mantum.component.R.layout.simple_dropdown_item_multiline, values);
    }

    @NonNull
    public ArrayAdapter<T> getAdapter() {
        return this.adapter;
    }
}