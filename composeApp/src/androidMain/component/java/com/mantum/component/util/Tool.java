package com.mantum.component.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class Tool {

    @NonNull
    public static String formData(@Nullable Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof String) {
            return (String) value;
        }

        return String.valueOf(value);
    }

    @Nullable
    public static String yyyymmdd(Date date) {
        try {
            if (date == null) {
                throw new IllegalArgumentException("La fecha no esta definida");
            }

            SimpleDateFormat simpleDateFormat
                    = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return simpleDateFormat.format(date);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static String datetime(Date date) {
        try {
            if (date == null) {
                throw new IllegalArgumentException("La fecha no esta definida");
            }

            SimpleDateFormat simpleDateFormat
                    = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return simpleDateFormat.format(date);
        } catch (Exception e) {
            return null;
        }
    }
}