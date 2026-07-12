package com.pcbiao.debtarchive;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

final class DebtDraftSerializer {
    static final class Result {
        final JSONArray debts;
        final boolean missingCreditor;

        Result(JSONArray debts, boolean missingCreditor) {
            this.debts = debts;
            this.missingCreditor = missingCreditor;
        }
    }

    private DebtDraftSerializer() {}

    static Result serialize(List<DebtDraft> drafts) {
        JSONArray debts = new JSONArray();
        for (DebtDraft draft : drafts) {
            if (draft.creditor.isEmpty() && draft.amount.isEmpty()) continue;
            if (draft.creditor.isEmpty()) return new Result(debts, true);
            JSONObject item = new JSONObject();
            try {
                item.put("id", draft.id.isEmpty() ? AppFormatter.uid("debt") : draft.id);
                item.put("type", draft.type);
                item.put("creditor", draft.creditor);
                item.put("amount", draft.amount);
                item.put("status", draft.status);
                item.put("dueDate", draft.dueDate);
                debts.put(item);
            } catch (Exception ignored) {}
        }
        return new Result(debts, false);
    }
}
