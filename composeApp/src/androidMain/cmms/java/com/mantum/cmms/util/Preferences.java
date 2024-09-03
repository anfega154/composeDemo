package com.mantum.cmms.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;

import com.mantum.R;

@Deprecated
public abstract class Preferences {

    @Deprecated
    public static String url(@NonNull Context context, @NonNull String endpoint) {
        SharedPreferences sharedPreferences
                = context.getSharedPreferences(context.getString(com.mantum.core.R.string.preference_file_key), Context.MODE_PRIVATE);
        String url = sharedPreferences.getString(context.getString(com.mantum.core.R.string.mantum_url), null);
        return String.format("%s/%s", url, endpoint);
    }

    @Deprecated
    public static String token(@NonNull Context context) {
        SharedPreferences sharedPreferences
                = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPreferences.getString(context.getString(R.string.mantum_token), null);
    }
}
