package com.mantum.component.adapter.handler;

import androidx.annotation.NonNull;

public interface ViewTimeLineAdapter<T extends ViewTimeLineAdapter>
        extends ViewAdapterHandler<T> {

    @NonNull
    String getDate();

    @NonNull
    String getTitle();

    @NonNull
    String getMessage();
}