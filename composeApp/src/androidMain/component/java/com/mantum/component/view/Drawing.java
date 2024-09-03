package com.mantum.component.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

public class Drawing extends View {

    private final Path drawPath;
    private final Paint drawPaint;
    private final Paint canvasPaint;

    private Canvas drawCanvas;
    private Bitmap background;
    private Bitmap canvasBitmap;

    private boolean isClick;
    private boolean enabled;

    public Drawing(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        enabled = true;
        isClick = false;
        background = null;

        drawPath = new Path();
        drawPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        drawPaint.setColor(Color.BLACK);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(15);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        canvasBitmap = background != null
                ? Bitmap.createScaledBitmap(background.copy(Bitmap.Config.ARGB_8888, true), w, h, false)
                : Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    public void setColor(int color) {
        drawPaint.setColor(color);
    }

    public int getColor() {
        return drawPaint.getColor();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.enabled = enabled;
    }

    public void setBackground(Bitmap background) {
        this.background = background;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!enabled) {
            return false;
        }

        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isClick = true;
                drawPath.moveTo(touchX, touchY);
                break;

            case MotionEvent.ACTION_UP:
                drawPath.lineTo(touchX, touchY);
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                if (isClick) {
                    performClick();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                isClick = false;
                break;

            default:
                return false;
        }

        invalidate();
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public void clean() {
        int h = canvasBitmap.getHeight();
        int w = canvasBitmap.getWidth();

        canvasBitmap = background != null
                ? Bitmap.createScaledBitmap(background.copy(Bitmap.Config.ARGB_8888, true), w, h, false)
                : Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        invalidate();
    }
}