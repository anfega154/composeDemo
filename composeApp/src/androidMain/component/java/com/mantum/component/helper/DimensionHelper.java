package com.mantum.component.helper;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Created by user on 11/08/2017.
 */

public class DimensionHelper {

    final Activity activity;

    final DisplayMetrics displaymetrics;

    public DimensionHelper(Activity context){
        this.activity = context;
        displaymetrics = new DisplayMetrics();
    }

    public float getDimension(float base) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, base, activity.getResources().getDisplayMetrics());
    }

    public int getHeightScreen () {
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        return (int) (displayMetrics.heightPixels / displayMetrics.density);
    }

    public int getWidthScreen () {
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        return (int) (displayMetrics.widthPixels / displayMetrics.density);
    }

    public int getWidthScreenPx(){
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.widthPixels;
    }

    public int getHeightScreenPx(){
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.heightPixels;
    }

}
