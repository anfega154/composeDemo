package com.mantum.component.helper;

import android.content.Context;
import android.preference.PreferenceManager;

public class SharedPreferences {

    public static void setSharedPreferencesString(Context context, String id, String json) {
        android.content.SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefEditor.putString(id, json);
        prefEditor.apply();
    }

    public static String getSharedPreferencesString(Context context, String id) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(id, "");
    }

}