package com.pcbiao.debtarchive;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

final class SwipeFrameLayout extends FrameLayout {
    interface Listener {
        void onMove(View swipeTarget, View deleteAction, float distance);
        void onSettle(View swipeTarget, View deleteAction);
    }

    private View swipeTarget;
    private View deleteAction;
    private Listener listener;
    private float startX;
    private float startY;
    private boolean swiping;

    SwipeFrameLayout(Context context) {
        super(context);
    }

    void configureSwipe(View swipeTarget, View deleteAction, Listener listener) {
        this.swipeTarget = swipeTarget;
        this.deleteAction = deleteAction;
        this.listener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (swipeTarget == null || deleteAction == null) return super.onInterceptTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getRawX();
                startY = event.getRawY();
                swiping = false;
                return false;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getRawX() - startX;
                float dy = event.getRawY() - startY;
                if (Math.abs(dx) > dp(8) && Math.abs(dx) > Math.abs(dy) * 1.3f) {
                    swiping = true;
                    return true;
                }
                return false;
            default:
                return super.onInterceptTouchEvent(event);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (swipeTarget == null || deleteAction == null || listener == null) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                listener.onMove(swipeTarget, deleteAction, event.getRawX() - startX);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                listener.onSettle(swipeTarget, deleteAction);
                swiping = false;
                return true;
            default:
                return swiping || super.onTouchEvent(event);
        }
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + .5f);
    }
}
