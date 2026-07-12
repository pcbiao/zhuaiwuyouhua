package com.pcbiao.debtarchive;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import java.security.MessageDigest;
import java.util.Locale;
import java.util.UUID;

final class ActivationManager {
    private static final String PREF = "debtArchiveLicense";
    private static final String ACTIVE = "activated";
    private static final String FALLBACK_ID = "fallbackDeviceId";
    private static final String SECRET = "QZ_DEBT_ARCHIVE_2026_DEVICE_LICENSE";

    private final Context context;

    ActivationManager(Context context) {
        this.context = context.getApplicationContext();
    }

    boolean isActivated() {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getBoolean(ACTIVE, false);
    }

    void activate() {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putBoolean(ACTIVE, true).apply();
    }

    String getDeviceCode() {
        String seed = "";
        try {
            seed = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception ignored) {}
        if (seed == null || seed.trim().isEmpty()) {
            SharedPreferences preferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
            seed = preferences.getString(FALLBACK_ID, "");
            if (seed.isEmpty()) {
                seed = UUID.randomUUID().toString();
                preferences.edit().putString(FALLBACK_ID, seed).apply();
            }
        }
        String hash = sha256(context.getPackageName() + "|" + seed);
        return "QZ-" + hash.substring(0, 4) + "-" + hash.substring(4, 8);
    }

    boolean isValidCode(String value) {
        return normalize(value).equals(normalize(codeFor(getDeviceCode())));
    }

    private String codeFor(String deviceCode) {
        String hash = sha256(SECRET + "|" + normalize(deviceCode));
        return "QZ-" + hash.substring(0, 4) + "-" + hash.substring(4, 8) + "-" + hash.substring(8, 12);
    }

    private String normalize(String value) {
        return (value == null ? "" : value).toUpperCase(Locale.US).replaceAll("[^A-Z0-9]", "");
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes("UTF-8"));
            StringBuilder output = new StringBuilder();
            for (byte b : bytes) output.append(String.format(Locale.US, "%02X", b));
            return output.toString();
        } catch (Exception e) {
            return "0000000000000000";
        }
    }
}
