package com.mantum.component.adapter.handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

public interface ViewInformationChildrenAdapter<T extends ViewInformationChildrenAdapter> extends ViewAdapterHandler<T> {

    @Nullable
    File getFile();

    @NonNull
    String getName();

    @Nullable
    Integer getDrawable();

    @Nullable
    Integer getImageColor();

    @Nullable
    Integer getTextColor();
}