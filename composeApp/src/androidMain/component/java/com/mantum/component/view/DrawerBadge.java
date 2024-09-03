package com.mantum.component.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;

public class DrawerBadge extends AppCompatTextView {

    private float strokeWidth;

    int strokeColor = Color.parseColor("#000000"); // negro

    int solidColor = Color.parseColor("#FF0000"); // rojo

    public DrawerBadge(Context context, NavigationView navigationView, int idItem, String value, String letterColor, String strokeColor, String solidColor) {
        super(context);
        MenuItemCompat.setActionView(navigationView
                .getMenu().findItem(idItem), this);
        DrawerBadge badgeLista = (DrawerBadge) MenuItemCompat
                .getActionView(navigationView
                        .getMenu().findItem(idItem));
        badgeLista.setGravity(Gravity.CENTER);
        badgeLista.setTypeface(null, Typeface.BOLD);
        badgeLista.setTextColor(Color.parseColor(letterColor));
        badgeLista.setText(value);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        badgeLista.setLayoutParams(params);
        badgeLista.setPadding(3, 3, 3, 3);
        badgeLista.setStrokeWidth(1);
        badgeLista.setStrokeColor(strokeColor);
        badgeLista.setSolidColor(solidColor);
        badgeLista.requestLayout();
    }

    public DrawerBadge(Context context) {
        super(context);
    }

    public DrawerBadge(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawerBadge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void draw(Canvas canvas) {

        Paint circlePaint = new Paint();
        circlePaint.setColor(solidColor);
        circlePaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        Paint strokePaint = new Paint();
        strokePaint.setColor(strokeColor);
        strokePaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        int h = this.getHeight();
        int w = this.getWidth();

        int diameter = ((h > w) ? h : w);
        int radius = diameter / 2;

        this.setHeight(diameter);
        this.setWidth(diameter);

        canvas.drawCircle(diameter / 2, diameter / 2, radius, strokePaint);

        canvas.drawCircle(diameter / 2, diameter / 2, radius - strokeWidth, circlePaint);

        super.draw(canvas);
    }

    public void setStrokeWidth(int dp) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        strokeWidth = dp * scale;

    }

    public void setStrokeColor(String color) {
        strokeColor = Color.parseColor(color);
    }

    public void setSolidColor(String color) {
        solidColor = Color.parseColor(color);

    }
}
