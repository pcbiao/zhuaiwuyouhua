package com.pcbiao.debtarchive;

import android.graphics.Rect;
import android.view.View;
import android.widget.ScrollView;

final class KeyboardAvoider {
    private final ScrollView scrollView;
    private final int baseBottomPadding;
    private View focusedInput;

    KeyboardAvoider(ScrollView scrollView) {
        this.scrollView = scrollView;
        this.baseBottomPadding = scrollView.getPaddingBottom();
    }

    void install(View host) {
        host.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (scrollView.getPaddingBottom() != baseBottomPadding) {
                scrollView.setPadding(0, 0, 0, baseBottomPadding);
            }
            if (focusedInput != null) focusedInput.postDelayed(() -> scrollAboveKeyboard(focusedInput), 80);
        });
    }

    void onFocusChanged(View input, boolean hasFocus) {
        if (hasFocus) keepVisible(input);
        else if (focusedInput == input) focusedInput = null;
    }

    void keepVisible(View input) {
        focusedInput = input;
        input.postDelayed(() -> scrollAboveKeyboard(input), 80);
        input.postDelayed(() -> scrollAboveKeyboard(input), 240);
        input.postDelayed(() -> scrollAboveKeyboard(input), 430);
    }

    private void scrollAboveKeyboard(View input) {
        if (input == null || !input.isShown()) return;
        Rect visible = new Rect();
        scrollView.getWindowVisibleDisplayFrame(visible);
        int[] location = new int[2];
        input.getLocationOnScreen(location);
        int inputBottom = location[1] + input.getHeight();
        int overlap = inputBottom + dp(32) - visible.bottom;
        if (overlap > 0) scrollView.smoothScrollBy(0, overlap);
    }

    private int dp(float value) {
        return (int) (value * scrollView.getResources().getDisplayMetrics().density + .5f);
    }
}
