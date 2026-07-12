package com.pcbiao.debtarchive;

import android.content.ContentResolver;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

final class BackupManager {
    private static final String APP_ID = "debt-customer-archive";
    private static final String APP_VERSION = "1.5.1";

    private final ContentResolver contentResolver;
    private final ClientRepository clientRepository;

    BackupManager(ContentResolver contentResolver, ClientRepository clientRepository) {
        this.contentResolver = contentResolver;
        this.clientRepository = clientRepository;
    }

    void importFrom(Uri uri) throws Exception {
        String raw = readText(uri);
        JSONArray clients;
        try {
            JSONObject backup = new JSONObject(raw);
            clients = backup.optJSONArray("clients");
            if (clients == null) throw new Exception("missing clients");
        } catch (Exception objectError) {
            clients = new JSONArray(raw);
        }
        List<JSONObject> imported = new ArrayList<>();
        for (int i = 0; i < clients.length(); i++) {
            JSONObject client = clients.optJSONObject(i);
            if (client != null) imported.add(client);
        }
        clientRepository.save(imported);
    }

    void exportTo(Uri uri) throws Exception {
        JSONObject backup = new JSONObject();
        backup.put("app", APP_ID);
        backup.put("version", APP_VERSION);
        backup.put("storageKey", ClientRepository.STORAGE_KEY);
        backup.put("exportedAt", isoNow());
        JSONArray clients = new JSONArray();
        for (JSONObject client : clientRepository.load()) clients.put(client);
        backup.put("clients", clients);
        writeText(uri, backup.toString(2));
    }

    private String readText(Uri uri) throws Exception {
        try (InputStream input = contentResolver.openInputStream(uri);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (input == null) throw new Exception("cannot open input");
            byte[] buffer = new byte[4096];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            return output.toString("UTF-8");
        }
    }

    private void writeText(Uri uri, String text) throws Exception {
        try (OutputStream output = contentResolver.openOutputStream(uri)) {
            if (output == null) throw new Exception("cannot open output");
            output.write(text.getBytes("UTF-8"));
        }
    }

    private String isoNow() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.CHINA).format(new Date());
    }
}
