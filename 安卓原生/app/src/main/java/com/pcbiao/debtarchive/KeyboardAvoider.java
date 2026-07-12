package com.pcbiao.debtarchive;

import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ScrollView;

final class KeyboardAvoider {
    private final ScrollView scrollView;
    private final int baseBottomPadding;
    private View focusedInput;
    private int expandedScrollHeight;

    KeyboardAvoider(ScrollView scrollView) {
        this.scrollView = scrollView;
        this.baseBottomPadding = scrollView.getPaddingBottom();
    }

    void install(View host) {
        host.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect visible = new Rect();
            host.getWindowVisibleDisplayFrame(visible);
            int keyboardHeight = keyboardHeight(host, visible);
            boolean keyboardVisible = keyboardHeight > dp(120);
            if (!keyboardVisible) expandedScrollHeight = Math.max(expandedScrollHeight, scrollView.getHeight());
            boolean windowResized = expandedScrollHeight > 0
                && scrollView.getHeight() < expandedScrollHeight - dp(120);
            int bottom = baseBottomPadding + (keyboardVisible && !windowResized ? keyboardHeight : 0);
            if (scrollView.getPaddingBottom() != bottom) {
                scrollView.setPadding(0, 0, 0, bottom);
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
        int keyboardHeight = keyboardHeight(scrollView, visible);
        int visibleBottom = visible.bottom;
        if (keyboardHeight > 0) {
            int[] rootLocation = new int[2];
            scrollView.getRootView().getLocationOnScreen(rootLocation);
            visibleBottom = Math.min(visibleBottom, rootLocation[1] + scrollView.getRootView().getHeight() - keyboardHeight);
        }
        int[] location = new int[2];
        input.getLocationOnScreen(location);
        int inputBottom = location[1] + input.getHeight();
        int overlap = inputBottom + dp(32) - visibleBottom;
        if (overlap > 0) scrollView.smoothScrollBy(0, overlap);
    }

    private int keyboardHeight(View host, Rect visible) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsets insets = host.getRootWindowInsets();
            if (insets != null) return insets.getInsets(WindowInsets.Type.ime()).bottom;
        }
        return Math.max(0, host.getRootView().getHeight() - visible.bottom);
    }

    private int dp(float value) {
        return (int) (value * scrollView.getResources().getDisplayMetrics().density + .5f);
    }
}
