package com.mantum.component.sensor;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import androidx.annotation.NonNull;

public class Compass implements SensorEventListener {

    private final SensorManager sensorManager;

    private final OnSensorListener onSensorListener;

    private float[] rotation = new float[9];

    private float[] orientation = new float[3];

    public Compass(@NonNull Context context, @NonNull OnSensorListener onSensorListener) {
        this.onSensorListener = onSensorListener;
        this.sensorManager = isGyroscope(context) ? (SensorManager) context.getSystemService(Context.SENSOR_SERVICE) : null;
    }

    public static boolean isGyroscope(@NonNull Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
    }

    public void start() {
        if (sensorManager != null) {
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {

            case Sensor.TYPE_ROTATION_VECTOR :
                SensorManager.getRotationMatrixFromVector(rotation, event.values);
                int mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rotation, orientation)[0]) + 360) % 360;
                onSensorListener.onSensorChanged(mAzimuth);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        onSensorListener.onAccuracyChanged(sensor, accuracy);
    }
}