package com.mantum.component.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mantum.demo.R;

public class FastScroller extends LinearLayout {

    private TextView bubble;
    private View handle;
    private RecyclerView recyclerView;

    public FastScroller(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.fastscroller, this);
        bubble = findViewById(R.id.bubble);
        handle = findViewById(R.id.handle);

        handle.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    float y = event.getY();
                    setHandlePosition(y);
                    setRecyclerViewPosition(y);
                }
                return true;
            }
        });
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    private void setHandlePosition(float y) {
        handle.setY(y - handle.getHeight() / 2);
        bubble.setY(y - bubble.getHeight());
        bubble.setVisibility(View.VISIBLE);
    }

    private void setRecyclerViewPosition(float y) {
        if (recyclerView != null) {
            int itemCount = recyclerView.getAdapter().getItemCount();
            float proportion = y / (float) getHeight();
            int targetPos = Math.round(proportion * itemCount);
            recyclerView.scrollToPosition(targetPos);
        }
    }
}
