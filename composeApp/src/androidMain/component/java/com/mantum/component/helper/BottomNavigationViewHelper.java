package com.mantum.component.helper;

import android.content.Context;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mantum.demo.R;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

/**
 * Helper que desactiva el ShifMode del BottomNavigationView
 */
public class BottomNavigationViewHelper {

    final Context context;

    final BottomNavigationView view;

    public BottomNavigationViewHelper(Context context, BottomNavigationView view){
        this.context = context;
        this.view = view;
    }

    public void resizeIcons(int dpSize){
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        for (int i = 0; i < menuView.getChildCount(); i++) {
            final View iconView = menuView.getChildAt(i).findViewById(R.id.icon);
            final ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
            final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpSize, displayMetrics);
            layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpSize, displayMetrics);
            iconView.setLayoutParams(layoutParams);
        }
    }

}