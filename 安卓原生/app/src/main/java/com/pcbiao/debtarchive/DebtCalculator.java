package com.pcbiao.debtarchive;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

final class DebtCalculator {
    private DebtCalculator() {}

    static JSONArray debts(JSONObject client) {
        JSONArray debts = client == null ? null : client.optJSONArray("debts");
        return debts == null ? new JSONArray() : debts;
    }

    static long total(JSONObject client) {
        long total = 0;
        JSONArray debts = debts(client);
        for (int i = 0; i < debts.length(); i++) {
            total += parseAmount(JsonValues.opt(debts.optJSONObject(i), "amount"));
        }
        return total;
    }

    static long parseAmount(String value) {
        String raw = value == null ? "" : value.replace(",", "").trim();
        if (raw.isEmpty()) return 0;
        try {
            double amount = Double.parseDouble(raw.replaceAll("[^\\d.]", ""));
            return Math.round(raw.contains("万") || amount < 10000 ? amount * 10000 : amount);
        } catch (Exception e) {
            return 0;
        }
    }

    static String money(long value) {
        if (value >= 10000) {
            double amount = value / 10000.0;
            return String.format(Locale.CHINA, "%.2f", amount)
                .replaceAll("0+$", "").replaceAll("\\.$", "") + "万";
        }
        return String.valueOf(value);
    }

    static boolean hasAlert(JSONObject client) {
        JSONArray debts = debts(client);
        for (int i = 0; i < debts.length(); i++) {
            JSONObject debt = debts.optJSONObject(i);
            if (debt != null && !alertLabel(debt).isEmpty()) return true;
        }
        return false;
    }

    static String alertSummary(JSONObject client) {
        int reminders = 0;
        int overdue = 0;
        JSONArray debts = debts(client);
        for (int i = 0; i < debts.length(); i++) {
            JSONObject debt = debts.optJSONObject(i);
            if (debt == null) continue;
            String label = alertLabel(debt);
            if ("提醒".equals(label)) reminders++;
            if ("逾期".equals(label)) overdue++;
        }
        List<String> items = new ArrayList<>();
        if (reminders > 0) items.add(reminders + "个提醒");
        if (overdue > 0) items.add(overdue + "个逾期");
        return join(items, "，");
    }

    static String alertLabel(JSONObject debt) {
        String status = JsonValues.opt(debt, "status");
        if ("结清".equals(status) || "协商".equals(status)) return "";
        if ("逾期".equals(status) || "催收".equals(status)) return "逾期";
        if ("提醒".equals(status)) return "提醒";
        Integer days = daysUntilDue(JsonValues.opt(debt, "dueDate"));
        if (days != null && days < 0) return "逾期";
        if (days != null && days <= 30) return "提醒";
        return "";
    }

    static Integer daysUntilDue(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            format.setLenient(false);
            Date due = format.parse(value);
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            return (int) TimeUnit.MILLISECONDS.toDays(due.getTime() - today.getTimeInMillis());
        } catch (Exception e) {
            return null;
        }
    }

    private static String join(List<String> items, String separator) {
        StringBuilder output = new StringBuilder();
        for (String item : items) {
            if (output.length() > 0) output.append(separator);
            output.append(item);
        }
        return output.toString();
    }
}
