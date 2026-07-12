package com.pcbiao.debtarchive;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

final class AppFormatter {
    private AppFormatter() {}

    static String valid(String value, String[] values, String fallback) {
        for (String item : values) if (item.equals(value)) return item;
        return fallback;
    }

    static String opt(JSONObject object, String key) {
        return JsonValues.opt(object, key);
    }

    static String cleanValue(String value) {
        return value.replace("›", "").replace("▣", "").trim();
    }

    static String empty(String value) {
        return value == null || value.trim().isEmpty() ? "未填写" : value;
    }

    static String uid(String prefix) {
        return prefix + "-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    static String iso() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.CHINA).format(new Date());
    }

    static String fmt(String iso) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.CHINA).parse(iso);
            return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(date);
        } catch (Exception e) {
            return "未记录";
        }
    }
}
