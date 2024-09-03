package com.mantum.component.adapter.handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public interface ViewGroupAdapter<T extends ViewGroupAdapter, K extends ViewAdapter<K>>
        extends ViewAdapterHandler<T> {

    @NonNull
    String getTitle();

    @Nullable
    String getSubtitle();

    @Nullable
    String getIcon();

    @Nullable
    Integer getDrawable();

    List<K> getChildren();

    @NonNull
    String getState();
}