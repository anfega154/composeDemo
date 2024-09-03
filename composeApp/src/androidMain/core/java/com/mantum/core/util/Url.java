package com.mantum.core.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;

import com.mantum.demo.R;
@Deprecated
public abstract class Url {

    @Deprecated
    public static String build(@NonNull Context context, @NonNull String endpoint) {
        SharedPreferences sharedPreferences
                = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String url = sharedPreferences.getString(context.getString(R.string.mantum_url), null);
        return String.format("%s/%s", url, endpoint);
    }
}