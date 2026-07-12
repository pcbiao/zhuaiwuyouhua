package com.pcbiao.debtarchive;

import android.graphics.Color;

final class StatusPalette {
    private static final int TEAL = Color.rgb(8, 118, 111);
    private static final int TEAL_SOFT = Color.rgb(237, 248, 246);
    private static final int INK = Color.rgb(21, 31, 29);
    private static final int MUTED = Color.rgb(109, 119, 116);
    private static final int LINE = Color.rgb(158, 170, 166);
    private static final int DANGER = Color.rgb(230, 0, 18);

    private StatusPalette() {}

    static int textColor(String status) {
        if ("逾期".equals(status) || "催收".equals(status)) return DANGER;
        if ("提醒".equals(status)) return Color.rgb(214, 149, 0);
        if ("协商".equals(status) || "正常".equals(status)) return TEAL;
        if ("结清".equals(status)) return MUTED;
        return INK;
    }

    static int background(String status) {
        if ("逾期".equals(status) || "催收".equals(status)) return Color.rgb(255, 242, 242);
        if ("提醒".equals(status)) return Color.rgb(255, 248, 232);
        return TEAL_SOFT;
    }

    static int stroke(String status) {
        if ("逾期".equals(status) || "催收".equals(status)) return Color.rgb(255, 205, 205);
        if ("提醒".equals(status)) return Color.rgb(250, 224, 174);
        return LINE;
    }
}
