package com.mantum.core.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public abstract class Online {

    @Deprecated
    public static boolean check(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        return!Assert.isNull(netInfo) && netInfo.isConnectedOrConnecting();
    }
}