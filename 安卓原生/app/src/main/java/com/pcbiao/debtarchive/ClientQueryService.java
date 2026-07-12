package com.pcbiao.debtarchive;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

final class ClientQueryService {
    private final ClientRepository repository;

    ClientQueryService(ClientRepository repository) {
        this.repository = repository;
    }

    List<JSONObject> find(String query, boolean alertsOnly) {
        List<JSONObject> clients = repository.load();
        Collections.sort(clients, (a, b) -> AppFormatter.opt(b, "updatedAt").compareTo(AppFormatter.opt(a, "updatedAt")));
        String normalized = query.trim().toLowerCase(Locale.CHINA);
        List<JSONObject> result = new ArrayList<>();
        for (JSONObject client : clients) {
            String name = AppFormatter.opt(client, "name").toLowerCase(Locale.CHINA);
            String phone = AppFormatter.opt(client, "phone");
            if (!normalized.isEmpty() && !name.contains(normalized) && !phone.contains(normalized)) continue;
            if (normalized.isEmpty() && alertsOnly && !DebtCalculator.hasAlert(client)) continue;
            result.add(client);
        }
        return result;
    }

    int alertCount() {
        int count = 0;
        for (JSONObject client : repository.load()) if (DebtCalculator.hasAlert(client)) count++;
        return count;
    }
}
