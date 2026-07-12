package com.pcbiao.debtarchive;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

final class ClientRepository {
    static final String STORAGE_KEY = "debtCustomerArchiveV150";
    private static final String PREFERENCES = "archive";

    private final Context context;

    ClientRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    List<JSONObject> load() {
        List<JSONObject> clients = new ArrayList<>();
        String raw = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .getString(STORAGE_KEY, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                JSONObject client = array.optJSONObject(i);
                if (client != null) clients.add(client);
            }
        } catch (Exception ignored) {}
        return clients;
    }

    void save(List<JSONObject> clients) {
        JSONArray array = new JSONArray();
        for (JSONObject client : clients) array.put(client);
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit()
            .putString(STORAGE_KEY, array.toString()).apply();
    }

    JSONObject findById(String id) {
        for (JSONObject client : load()) {
            if (JsonValues.opt(client, "id").equals(id)) return client;
        }
        return null;
    }

    int findIndex(List<JSONObject> clients, String id, String name) {
        for (int i = 0; i < clients.size(); i++) {
            JSONObject client = clients.get(i);
            if ((!id.isEmpty() && JsonValues.opt(client, "id").equals(id))
                || JsonValues.opt(client, "name").equals(name)) return i;
        }
        return -1;
    }
}
