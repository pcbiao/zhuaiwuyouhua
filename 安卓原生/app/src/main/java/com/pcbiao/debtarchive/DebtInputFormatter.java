package com.pcbiao.debtarchive;

import java.util.Locale;

final class DebtInputFormatter {
    private DebtInputFormatter() {}

    static String statusFromDueDate(String dueDate) {
        Integer days = DebtCalculator.daysUntilDue(dueDate);
        if (days != null && days < 0) return "逾期";
        if (days != null && days <= 30) return "提醒";
        return "正常";
    }

    static String amount(String value) {
        String raw = value == null ? "" : value.trim().replace(",", "").replace("万", "");
        if (raw.isEmpty()) return "";
        String numberText = raw.replaceAll("[^\\d.]", "");
        if (numberText.isEmpty()) return "";
        try {
            double number = Double.parseDouble(numberText);
            return String.format(Locale.CHINA, "%.2f", number).replaceAll("0+$", "").replaceAll("\\.$", "");
        } catch (Exception e) {
            return "";
        }
    }
}
