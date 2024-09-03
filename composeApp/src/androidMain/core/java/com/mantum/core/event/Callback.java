package com.mantum.core.event;

import androidx.annotation.Nullable;

@Deprecated
public interface Callback<T> {

    boolean onExecution(@Nullable T value);
}