package com.mantum.component.adapter.handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface ViewTrasladoAdapter<T extends ViewTrasladoAdapter> extends ViewAdapterHandler<T> {

    @NonNull
    String getTitle();

    @Nullable
    String getSubtitle();

    @Nullable
    String getSummary();

    @Nullable
    String getNombeBodega();

    @Nullable
    String getPadreItem();

    @Nullable
    String getIcon();

    @Nullable
    Integer getDrawable();

    @NonNull
    Float getQuantity();

    @NonNull
    String getEstadoItem();

    @NonNull
    String getItemType();
}
