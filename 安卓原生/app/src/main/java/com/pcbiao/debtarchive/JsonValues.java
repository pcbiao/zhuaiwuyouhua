package com.pcbiao.debtarchive;

import org.json.JSONObject;

final class JsonValues {
    private JsonValues() {}

    static String opt(JSONObject object, String key) {
        return object == null ? "" : object.optString(key, "");
    }
}
