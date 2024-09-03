package com.mantum.component.adapter.handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface ViewAdapter<T extends ViewAdapter> extends ViewAdapterHandler<T> {

    @NonNull
    String getTitle();

    @Nullable
    String getSubtitle();

    @Nullable
    String getSummary();

    @Nullable
    String getIcon();

    @Nullable
    Integer getDrawable();
}