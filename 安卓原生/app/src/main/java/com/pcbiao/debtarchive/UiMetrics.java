package com.pcbiao.debtarchive;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.widget.LinearLayout;

final class UiMetrics {
    private final Context context;

    UiMetrics(Context context) {
        this.context = context;
    }

    GradientDrawable round(int color, int stroke, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        drawable.setStroke(1, stroke);
        return drawable;
    }

    GradientDrawable rightRound(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        float r = dp(radius);
        drawable.setCornerRadii(new float[]{0, 0, r, r, r, r, 0, 0});
        return drawable;
    }

    LinearLayout.LayoutParams margin(int width, int height, int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.setMargins(left, top, right, bottom);
        return params;
    }

    int screenWidth() {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    int topActionWidth() { return dp(isNarrowContent() ? 68 : 76); }
    int infoLabelWidth() { return dp(isNarrowContent() ? 116 : 132); }
    int editLabelWidth() { return dp(isNarrowContent() ? 132 : 168); }
    int typeLabelWidth() { return dp(isNarrowContent() ? 112 : 168); }
    int typeGroupRightPadding() { return dp(isNarrowContent() ? 4 : 18); }
    int typeSeparatorWidth() { return dp(isNarrowContent() ? 8 : 12); }

    int topInset() {
        int id = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return (id > 0 ? context.getResources().getDimensionPixelSize(id) : dp(24)) + dp(8);
    }

    int dp(float value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + .5f);
    }

    private boolean isNarrowContent() {
        float density = context.getResources().getDisplayMetrics().density;
        int contentWidthDp = (int) (Math.min(screenWidth(), dp(430)) / density);
        return contentWidthDp < 390;
    }
}
