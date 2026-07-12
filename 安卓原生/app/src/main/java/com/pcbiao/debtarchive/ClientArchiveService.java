package com.pcbiao.debtarchive;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

final class ClientArchiveService {
    private final ClientRepository repository;

    ClientArchiveService(ClientRepository repository) {
        this.repository = repository;
    }

    String save(String editingId, String name, String phone, String monthlyIncome,
                String hasMortgage, String biggestConcern, JSONArray debts) {
        List<JSONObject> clients = repository.load();
        String now = AppFormatter.iso();
        JSONObject archive = new JSONObject();
        try {
            archive.put("id", editingId.isEmpty() ? AppFormatter.uid("client") : editingId);
            archive.put("name", name);
            archive.put("phone", phone);
            archive.put("monthlyIncome", monthlyIncome);
            archive.put("hasMortgage", hasMortgage);
            archive.put("biggestConcern", biggestConcern);
            archive.put("debts", debts);
            archive.put("createdAt", now);
            archive.put("updatedAt", now);
            int index = repository.findIndex(clients, AppFormatter.opt(archive, "id"), name);
            if (index >= 0) {
                archive.put("id", AppFormatter.opt(clients.get(index), "id"));
                archive.put("createdAt", AppFormatter.opt(clients.get(index), "createdAt"));
                clients.set(index, archive);
            } else {
                clients.add(archive);
            }
        } catch (Exception ignored) {}
        repository.save(clients);
        return AppFormatter.opt(archive, "id");
    }
}
