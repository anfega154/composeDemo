package com.mantum.component.service.handler;

import android.location.Location;
import androidx.annotation.NonNull;

import com.mantum.component.service.Geolocation;

public interface OnLocationListener {

    void onLocationChanged(@NonNull Geolocation geolocation, @NonNull Location location);

    void onError(Throwable throwable);
}