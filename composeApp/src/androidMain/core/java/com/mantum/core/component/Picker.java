package com.mantum.core.component;

import androidx.annotation.IdRes;

import java.util.Calendar;

@Deprecated
public interface Picker<T extends PickerHelper> extends PickerHelper {

    T id(@IdRes int id);

    T cancelable(boolean cancelable);

    T calendar(Calendar calendar);

    T enabled(boolean enabled);
}