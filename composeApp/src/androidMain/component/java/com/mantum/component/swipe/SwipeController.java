package com.mantum.component.swipe;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.MotionEvent;
import android.view.View;

import static androidx.recyclerview.widget.ItemTouchHelper.LEFT;
import static androidx.recyclerview.widget.ItemTouchHelper.RIGHT;

public class SwipeController extends ItemTouchHelper.Callback {

    private final Context context;

    private boolean isSwipe;

    private boolean isAction;

    private int colorSwipeLeft;

    private int colorSwipeRight;

    private int drawableSwipeLeft;

    private int drawableSwipeRight;

    private OnSwipeLeft onSwipeLeft;

    private OnSwipeRight onSwipeRight;

    public SwipeController(@NonNull Context context) {
        this.context = context;
        this.isSwipe = false;
        this.isAction = false;
    }

    public void setOnSwipeLeft(OnSwipeLeft onSwipeLeft, @ColorInt int colorSwipeLeft,
                               @DrawableRes int drawableSwipeLeft) {
        this.onSwipeLeft = onSwipeLeft;
        this.colorSwipeLeft = colorSwipeLeft;
        this.drawableSwipeLeft = drawableSwipeLeft;
    }

    public void setOnSwipeRight(OnSwipeRight onSwipeRight, @ColorInt int colorSwipeRight,
                               @DrawableRes int drawableSwipeRight) {
        this.onSwipeRight = onSwipeRight;
        this.colorSwipeRight = colorSwipeRight;
        this.drawableSwipeRight = drawableSwipeRight;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, LEFT | RIGHT);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {}

    @Override
    public void onChildDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener(this::onTouch);

        if (isAction && !isCurrentlyActive) {
            isAction = false;
        }

        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getBottom() - itemView.getTop();

        if (dX > 0) { // Left
            RectF rectf = new RectF(itemView.getLeft(), itemView.getTop(),
                    itemView.getLeft() + dX, itemView.getBottom());

            Paint paint = new Paint();
            paint.setColor(colorSwipeLeft);
            canvas.drawRoundRect(rectf, 0, 0, paint);

            Drawable drawable = ContextCompat.getDrawable(context, drawableSwipeLeft);
            if (drawable != null) {
                int deleteIconTop = itemView.getTop() + (itemHeight - drawable.getIntrinsicHeight()) / 2;
                int deleteIconMargin = (itemHeight - drawable.getIntrinsicHeight()) / 2;
                int deleteIconLeft = itemView.getLeft() + deleteIconMargin;
                int deleteIconRight = itemView.getLeft() + deleteIconMargin + drawable.getIntrinsicHeight();
                int deleteIconBottom = deleteIconTop + drawable.getIntrinsicHeight();

                drawable.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
                drawable.draw(canvas);
            }

            if (isCurrentlyActive && dX > 500 && !isAction) {
                isAction = true;
                onSwipeLeft.swipe(viewHolder);
            }
        } else { // Right
            RectF rectf = new RectF(itemView.getRight() + dX, itemView.getTop(),
                    itemView.getRight(), itemView.getBottom());

            Paint paint = new Paint();
            paint.setColor(colorSwipeRight);
            canvas.drawRoundRect(rectf, 0, 0, paint);

            Drawable drawable = ContextCompat.getDrawable(context, drawableSwipeRight);
            if (drawable != null) {
                int deleteIconTop = itemView.getTop() + (itemHeight - drawable.getIntrinsicHeight()) / 2;
                int deleteIconMargin = (itemHeight - drawable.getIntrinsicHeight()) / 2;
                int deleteIconLeft = itemView.getRight() - deleteIconMargin - drawable.getIntrinsicHeight();
                int deleteIconRight = itemView.getRight() - deleteIconMargin;
                int deleteIconBottom = deleteIconTop + drawable.getIntrinsicHeight();

                drawable.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
                drawable.draw(canvas);
            }

            if (isCurrentlyActive && dX < -500 && !isAction) {
                isAction = true;
                onSwipeRight.swipe(viewHolder);
            }
        }

        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @SuppressWarnings("unused")
    private boolean onTouch(View v, MotionEvent event) {
        isSwipe = event.getAction() == MotionEvent.ACTION_CANCEL
                || event.getAction() == MotionEvent.ACTION_UP;
        return false;
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        if (isSwipe) {
            isSwipe = false;
            return 0;
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }
}