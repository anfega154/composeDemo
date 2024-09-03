package com.mantum.component.adapter.handler;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.component.OnInvoke;

import java.util.List;

public interface ViewInformationAdapter<T extends ViewInformationAdapter, K extends ViewInformationChildrenAdapter<K>>
        extends ViewAdapterHandler<T> {

    @NonNull
    String getTitle();

    @Nullable
    String getSummary();

    @Nullable
    Integer getColorSummary();

    @Nullable
    String getSubtitle();

    @Nullable
    String getDescription();

    List<K> getChildren();

    @Nullable
    String getState();

    boolean isShowAction();

    @Nullable
    String getActionName();

    @Nullable
    OnInvoke<T> getAction(@NonNull Context context);
}