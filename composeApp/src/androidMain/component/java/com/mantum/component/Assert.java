package com.mantum.component;

public abstract class Assert {

    public static boolean isNull(Object value) {
        return null == value;
    }

    public static boolean isEmpty(String value) {
        return isNull(value) || value.length() == 0;
    }
}