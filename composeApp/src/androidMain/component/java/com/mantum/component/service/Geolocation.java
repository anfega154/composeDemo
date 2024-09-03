package com.mantum.component.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.mantum.component.R;
import com.mantum.component.service.handler.OnLocationListener;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.Context.LOCATION_SERVICE;
import static android.content.pm.PackageManager.FEATURE_LOCATION_GPS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.location.LocationManager.GPS_PROVIDER;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class Geolocation implements LocationListener {

    private static final String TAG = Geolocation.class.getSimpleName();

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public static final int MY_LOCATION_PERMISSION_REQUEST_CODE = 2;

    @SuppressWarnings("FieldCanBeLocal")
    private final long MIN_UPDATE_DISTANCE = 5; // Metros

    private final long MIN_UPDATE_TIME = 1000 * 60 * 2; // 2 minutos

    private final Context context;

    private final LocationRequest locationRequest;

    private final LocationManager locationManager;

    private final OnLocationListener onLocationListener;

    private static Location bestLocation = null;

    private final LocationCallback locationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            onLocationChanged(locationResult.getLastLocation());
        }

    };

    public Geolocation(@NonNull Context context, OnLocationListener onLocationListener) {
        this.context = context;
        this.onLocationListener = onLocationListener;
        this.locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        this.locationRequest = new LocationRequest();
        this.locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        this.locationRequest.setInterval(10000);
        this.locationRequest.setFastestInterval(2000);
        this.locationRequest.setSmallestDisplacement(1);
    }

    private void startLocationServicesAPI() {
        LocationSettingsRequest.Builder builder
                = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient
                = LocationServices.getSettingsClient(context);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        if (checkPermission(context)) {
            getFusedLocationProviderClient(context)
                    .requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }

    public void start() {
        start(MIN_UPDATE_TIME, MIN_UPDATE_DISTANCE);
    }

    public void start(long minTime, float minDistance) {
        if (onLocationListener != null) {

            Boolean check = isCheckGPS(context);
            if (check == null) {
                onLocationListener.onError(new Exception(context.getString(R.string.gps_not_check)));
                return;
            }

            if (!check) {
                onLocationListener.onError(new Exception(context.getString(R.string.gps_check)));
                return;
            }

            if (!checkPermission(context)) {
                onLocationListener.onError(new Exception(context.getString(R.string.gps_permission)));
                return;
            }

            if (!isEnabled(context)) {
                onLocationListener.onError(new Exception(context.getString(R.string.gps_connect)));
                return;
            }
        }

        startLocationServicesAPI();
        locationManager.requestLocationUpdates(GPS_PROVIDER, minTime, minDistance, this);
    }

    @Nullable
    public Location getLastKnownLocation() {
        if (locationManager == null) {
            return null;
        }

        if (!checkPermission(context)) {
            return null;
        }

        Location current = locationManager.getLastKnownLocation(GPS_PROVIDER);
        if (current != null && isBetterLocation(current, bestLocation)) {
            bestLocation = current;
        }

        return bestLocation;
    }

    public void stop() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        getFusedLocationProviderClient(context).removeLocationUpdates(locationCallback);
    }

    @Nullable
    private static Boolean isCheckGPS(@NonNull Context context) {
        try {
            return context.getPackageManager()
                    .hasSystemFeature(FEATURE_LOCATION_GPS);
        } catch (Exception e) {
            return null;
        }
    }

    public static void requestPermission(@NonNull Context context) {
        if (!checkPermission(context)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, ACCESS_FINE_LOCATION)) {
                showInContextUI(context);
            } else {
                requestPermissions(context, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    public static void requestPermission(@NonNull Context context, @IntRange(from = 0L) final int requestCode) {
        if (!checkPermission(context)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, ACCESS_FINE_LOCATION)) {
                showInContextUI(context);
            } else {
                requestPermissions(context, requestCode);
            }
        }
    }

    private static void requestPermissions(@NonNull Context context, @IntRange(from = 0L) final int requestCode) {
        ActivityCompat.requestPermissions((Activity) context, new String[]{
                ACCESS_FINE_LOCATION,
                ACCESS_COARSE_LOCATION
        }, requestCode);
    }

    private static void showInContextUI(@NonNull Context context) {
        LayoutInflater inflater
                = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.resumen_ubicacion, null);

        view.findViewById(R.id.terminos).setOnClickListener(v -> {
            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(context.getString(R.string.url_politicas_privacidad))
            );
            context.startActivity(intent);
        });

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setTitle(R.string.solicitar_ubicacion_titulo);
        alertDialogBuilder.setMessage(R.string.solicitar_ubicacion_mensaje);
        alertDialogBuilder.setPositiveButton(R.string.siguiente, (dialog, which) -> requestPermissions(context, LOCATION_PERMISSION_REQUEST_CODE));
        alertDialogBuilder.setNegativeButton(R.string.cancelar, (dialog, which) -> dialog.cancel());
        alertDialogBuilder.show();
    }

    public static boolean checkPermission(@NonNull Context context) {
        return !(ActivityCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED);
    }

    public static boolean isEnabled(@NonNull Context context) {
        LocationManager manager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        return manager != null && manager.isProviderEnabled(GPS_PROVIDER);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (isBetterLocation(location, bestLocation)) {
            bestLocation = location;
        }

        if (bestLocation == null) {
            bestLocation = location;
        }

        if (onLocationListener != null && bestLocation != null) {
            onLocationListener.onLocationChanged(this, bestLocation);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                Log.e(TAG, "onStatusChanged: GPS available again");
                break;

            case LocationProvider.OUT_OF_SERVICE:
                Log.e(TAG, "onStatusChanged: GPS out of service");
                break;

            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.e(TAG, "onStatusChanged: GPS temporarily unavailable");
                break;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.e(TAG, "onProviderEnabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.e(TAG, "onProviderDisabled: " + provider);
    }

    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > MIN_UPDATE_TIME;
        boolean isSignificantlyOlder = timeDelta < -MIN_UPDATE_TIME;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location,
        // because the user has likely moved.
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse.
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        return isMoreAccurate || isNewer && !isLessAccurate || isNewer && !isSignificantlyLessAccurate && isFromSameProvider;
    }

    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}