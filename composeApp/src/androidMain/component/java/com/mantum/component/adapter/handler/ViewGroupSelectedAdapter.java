package com.mantum.component.adapter.handler;

import androidx.annotation.NonNull;

public interface ViewGroupSelectedAdapter<T extends ViewGroupAdapter, K extends ViewSelectedAdapter<K>> extends ViewGroupAdapter<T, K> {

    @NonNull
    Long getId();

    Boolean isSelected();
}
