package com.hansihe.zimdroid.app;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by hansihe on 11/7/14.
 */
public class DraggableBottomCard extends CardView {

    private final ViewDragHelper dragHelper;

    public DraggableBottomCard(Context context) {
        super(context);
        dragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallback());
        init();
    }

    public DraggableBottomCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        dragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallback());
        init();
    }

    public DraggableBottomCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        dragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallback());
        init();
    }

    private void init() {

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            dragHelper.cancel();
            return false;
        } else if (dragHelper.shouldInterceptTouchEvent(ev)) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dragHelper.processTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private class DragHelperCallback extends ViewDragHelper.Callback {
        @Override
        public boolean tryCaptureView(View view, int i) {
            return false;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return super.clampViewPositionVertical(child, top, dy);
        }
    }
}
