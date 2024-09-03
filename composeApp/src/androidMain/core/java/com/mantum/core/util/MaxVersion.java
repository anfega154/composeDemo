package com.mantum.core.util;

import android.util.Log;

import okhttp3.Response;

public abstract class MaxVersion {

    private static final String TAG = MaxVersion.class.getSimpleName();

    public static Integer get(Response response) {
        if (Assert.isNull(response)) {
            return null;
        }

        String version = response.header("Max-Version", null);
        Log.d(TAG, "get: version -> " + version);
        return !Assert.isNull(version) ? Integer.valueOf(version) : null;
    }
}
