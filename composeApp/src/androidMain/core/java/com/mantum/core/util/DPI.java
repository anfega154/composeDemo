package com.mantum.core.util;

import android.util.DisplayMetrics;
import android.util.TypedValue;

public abstract class DPI {

    public static int convert(int size, DisplayMetrics metrics) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, metrics);
    }
}
