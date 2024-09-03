package com.mantum.component.sensor;

import android.hardware.Sensor;

public interface OnSensorListener {

    void onSensorChanged(int mAzimut);

    void onAccuracyChanged(Sensor sensor, int accuracy);
}