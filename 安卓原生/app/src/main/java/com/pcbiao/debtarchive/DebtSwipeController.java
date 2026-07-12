package com.pcbiao.debtarchive;

import android.animation.ValueAnimator;
import android.view.View;

final class DebtSwipeController {
    private static final int DELETE_WIDTH_DP = 56;
    private final UiMetrics metrics;
    private View openCard;
    private View openDeleteAction;

    DebtSwipeController(UiMetrics metrics) {
        this.metrics = metrics;
    }

    void reset() {
        openCard = null;
        openDeleteAction = null;
    }

    void move(View swipeTarget, View deleteAction, float distance) {
        swipeTarget.animate().cancel();
        if (openCard != null && openCard != swipeTarget) close(openCard, openDeleteAction, 100);
        float move = Math.max(-metrics.dp(DELETE_WIDTH_DP), Math.min(0, distance));
        swipeTarget.setTranslationX(move);
        syncDeleteVisibility(swipeTarget, deleteAction);
    }

    void settle(View swipeTarget, View deleteAction) {
        boolean open = swipeTarget.getTranslationX() < -metrics.dp(DELETE_WIDTH_DP / 2f);
        swipeTarget.animate().cancel();
        deleteAction.animate().cancel();
        swipeTarget.animate()
            .translationX(open ? -metrics.dp(DELETE_WIDTH_DP) : 0)
            .setDuration(120)
            .setUpdateListener((ValueAnimator animation) -> syncDeleteVisibility(swipeTarget, deleteAction))
            .withEndAction(() -> {
                swipeTarget.animate().setUpdateListener(null);
                syncDeleteVisibility(swipeTarget, deleteAction);
            })
            .start();
        openCard = open ? swipeTarget : null;
        openDeleteAction = open ? deleteAction : null;
    }

    private void close(View swipeTarget, View deleteAction, long duration) {
        if (swipeTarget == null) return;
        swipeTarget.animate().cancel();
        swipeTarget.animate()
            .translationX(0)
            .setDuration(duration)
            .setUpdateListener((ValueAnimator animation) -> syncDeleteVisibility(swipeTarget, deleteAction))
            .withEndAction(() -> {
                swipeTarget.animate().setUpdateListener(null);
                syncDeleteVisibility(swipeTarget, deleteAction);
            })
            .start();
    }

    private void syncDeleteVisibility(View swipeTarget, View deleteAction) {
        if (deleteAction == null) return;
        float shown = Math.abs(swipeTarget == null ? 0 : swipeTarget.getTranslationX());
        if (shown <= metrics.dp(2)) {
            deleteAction.setAlpha(0f);
            deleteAction.setVisibility(View.INVISIBLE);
            return;
        }
        deleteAction.setVisibility(View.VISIBLE);
        deleteAction.setAlpha(Math.min(1f, shown / metrics.dp(DELETE_WIDTH_DP)));
    }
}
