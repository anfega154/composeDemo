package com.mantum.core.util;

@Deprecated
public abstract class Assert {

    public static boolean isNull(Object value) {
        return null == value;
    }
}