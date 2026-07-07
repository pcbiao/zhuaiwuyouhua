package com.pcbiao.debtarchive;

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

public class MainActivity extends Activity {
    private static final String STORE = "debtCustomerArchiveV150";
    private static final int TEAL = Color.rgb(8, 118, 111);
    private static final int TEAL_SOFT = Color.rgb(237, 248, 246);
    private static final int INK = Color.rgb(21, 31, 29);
    private static final int MUTED = Color.rgb(109, 119, 116);
    private static final int LINE = Color.rgb(158, 170, 166);
    private static final int DEBT_LINE = LINE;
    private static final int PAPER = Color.WHITE;
    private static final int BG = Color.rgb(247, 248, 247);
    private static final int DANGER = Color.rgb(230, 0, 18);
    private static final float TITLE_SP = 18f;
    private static final float SECTION_SP = 18f;
    private static final float PRIMARY_SP = 16f;
    private static final float BODY_SP = 15f;
    private static final float META_SP = 14f;
    private static final float SMALL_SP = 13f;
    private static final float ACTION_SP = 15f;
    private static final float FILTER_SP = 14f;
    private static final int INFO_LABEL_DP = 132;
    private static final int DEBT_DELETE_DP = 56;
    private static final int DEBT_DELETE_OVERLAP_DP = 12;
    private static final int REQ_IMPORT = 701;
    private static final int REQ_EXPORT = 702;

    private LinearLayout root;
    private LinearLayout body;
    private ScrollView pageScroll;
    private int baseScrollPaddingBottom = 0;
    private View focusedInput;
    private TextView pageTitle;
    private Button leftAction;
    private Button rightAction;
    private String screen = "home";
    private String selectedId = "";
    private String editingId = "";
    private String hasMortgage = "否";
    private String clientFilterMode = "all";

    private EditText nameField;
    private EditText phoneField;
    private EditText incomeField;
    private EditText concernField;
    private LinearLayout debtRows;
    private int expandedDebtIndex = 0;
    private View openDebtSwipeCard;
    private View openDebtDeleteAction;
    private final List<DebtDraft> drafts = new ArrayList<>();
    private final String[] debtTypes = {"网贷", "信用卡", "银行贷款"};
    private final String[] statuses = {"正常", "提醒", "逾期", "催收", "协商", "结清"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        showHome();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        if (requestCode == REQ_IMPORT) importBackup(data.getData());
        if (requestCode == REQ_EXPORT) exportBackup(data.getData());
    }

    @Override
    public void onBackPressed() {
        if ("form".equals(screen)) {
            if (!editingId.isEmpty()) showDetail(selectedId);
            else showHome();
            return;
        }
        if ("detail".equals(screen)) {
            showHome();
            return;
        }
        super.onBackPressed();
    }

    private void base(String title) {
        FrameLayout frame = new FrameLayout(this);
        frame.setBackgroundColor(BG);
        setContentView(frame);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(PAPER);
        root.setPadding(dp(15), topInset(), dp(15), dp(14));
        FrameLayout.LayoutParams appParams = new FrameLayout.LayoutParams(Math.min(screenWidth(), dp(430)), -1, Gravity.CENTER_HORIZONTAL);
        frame.addView(root, appParams);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(top, new LinearLayout.LayoutParams(-1, dp(58)));

        leftAction = topButton("");
        pageTitle = label(title, TITLE_SP, INK, Typeface.BOLD);
        pageTitle.setGravity(Gravity.CENTER);
        rightAction = topButton("");
        top.addView(leftAction, new LinearLayout.LayoutParams(dp(76), -1));
        top.addView(pageTitle, new LinearLayout.LayoutParams(0, -1, 1));
        top.addView(rightAction, new LinearLayout.LayoutParams(dp(76), -1));

        ScrollView scroll = new ScrollView(this);
        pageScroll = scroll;
        pageScroll.setClipToPadding(false);
        baseScrollPaddingBottom = dp(24);
        pageScroll.setPadding(0, 0, 0, baseScrollPaddingBottom);
        body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(body, new ScrollView.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        installKeyboardAvoidance(frame);
    }

    private void showHome() {
        screen = "home";
        base("客户档案");
        leftAction.setEnabled(false);
        rightAction.setText("+ 添加");
        rightAction.setOnClickListener(v -> showForm(null));

        EditText search = input("输入姓名或手机号查询");
        search.setSingleLine(true);
        search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        body.addView(search, new LinearLayout.LayoutParams(-1, dp(48)));

        LinearLayout filters = new LinearLayout(this);
        filters.setGravity(Gravity.CENTER_VERTICAL);
        Button all = textButton("全部客户", FILTER_SP);
        Button alerts = textButton("有提醒 " + alertCount(), FILTER_SP);
        paintFilter(all, "all");
        paintFilter(alerts, "alert");
        filters.addView(all);
        filters.addView(label(" ｜ ", SMALL_SP, MUTED, Typeface.NORMAL));
        filters.addView(alerts);
        body.addView(filters, margin(-1, dp(40), 0, dp(8), 0, 0));

        LinearLayout list = box();
        body.addView(list, new LinearLayout.LayoutParams(-1, -2));
        renderList(list, "");
        all.setOnClickListener(v -> {
            clientFilterMode = "all";
            showHome();
        });
        alerts.setOnClickListener(v -> {
            clientFilterMode = "alert";
            showHome();
        });
        search.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { renderList(list, s.toString()); }
            public void afterTextChanged(android.text.Editable s) {}
        });

        LinearLayout tools = new LinearLayout(this);
        tools.setGravity(Gravity.RIGHT);
        Button importButton = textButton("导入", ACTION_SP);
        Button exportButton = textButton("导出", ACTION_SP);
        tools.addView(importButton);
        tools.addView(label("  |  ", SMALL_SP, LINE, Typeface.NORMAL));
        tools.addView(exportButton);
        body.addView(tools, new LinearLayout.LayoutParams(-1, dp(48)));
        importButton.setOnClickListener(v -> openImport());
        exportButton.setOnClickListener(v -> openExport());
        signature();
    }

    private void renderList(LinearLayout list, String query) {
        list.removeAllViews();
        List<JSONObject> clients = loadClients();
        Collections.sort(clients, (a, b) -> opt(b, "updatedAt").compareTo(opt(a, "updatedAt")));
        String q = query.trim().toLowerCase(Locale.CHINA);
        int count = 0;
        for (JSONObject c : clients) {
            String name = opt(c, "name").toLowerCase(Locale.CHINA);
            String phone = opt(c, "phone");
            if (!q.isEmpty() && !name.contains(q) && !phone.contains(q)) continue;
            if (q.isEmpty() && "alert".equals(clientFilterMode) && !hasDueAlert(c)) continue;
            count++;
            Button card = new Button(this);
            card.setAllCaps(false);
            card.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            card.setTextColor(MUTED);
            card.setTextSize(META_SP);
            card.setBackgroundColor(PAPER);
            card.setPadding(dp(24), dp(4), dp(10), dp(4));
            String baseText = opt(c, "name") + "     录入时间：" + fmt(opt(c, "createdAt")) + "\n电话：" + empty(opt(c, "phone")) + " · 总负债：" + money(totalDebt(c)) + " · " + debts(c).length() + " 笔债务";
            String alertText = alertSummary(c);
            if (alertText.isEmpty()) {
                card.setText(baseText);
            } else {
                String fullText = baseText + "\n" + alertText;
                SpannableString span = new SpannableString(fullText);
                colorAlertPart(span, fullText, "提醒", Color.rgb(214, 149, 0));
                colorAlertPart(span, fullText, "逾期", DANGER);
                card.setText(span);
            }
            String id = opt(c, "id");
            card.setOnClickListener(v -> showDetail(id));
            list.addView(card, new LinearLayout.LayoutParams(-1, dp(alertText.isEmpty() ? 68 : 88)));
            divider(list);
        }
        if (count == 0) list.addView(padText(q.isEmpty() ? "还没有保存客户档案。" : "没有找到这个客户。"));
    }

    private void showForm(JSONObject client) {
        screen = "form";
        editingId = client == null ? "" : opt(client, "id");
        selectedId = editingId;
        base(editingId.isEmpty() ? "新建客户" : "编辑客户");
        leftAction.setText("‹ 返回");
        leftAction.setOnClickListener(v -> {
            if (!editingId.isEmpty()) showDetail(selectedId);
            else showHome();
        });
        rightAction.setEnabled(false);

        body.addView(step(null, "客户信息", null));
        LinearLayout info = box();
        nameField = rowInput(info, "姓名：", "请输入姓名", dp(INFO_LABEL_DP));
        phoneField = rowInput(info, "联系电话：", "请输入手机号", dp(INFO_LABEL_DP));
        incomeField = rowInput(info, "月收入：", "请输入金额", dp(INFO_LABEL_DP));
        LinearLayout mortgageRow = row(info, "是否有房贷：", dp(INFO_LABEL_DP));
        LinearLayout mortgageGroup = new LinearLayout(this);
        mortgageGroup.setGravity(Gravity.CENTER_VERTICAL);
        mortgageGroup.setPadding(dp(10), 0, 0, 0);
        TextView mortgageYes = mortgageOption("有", false);
        TextView mortgageSep = label("|", BODY_SP, MUTED, Typeface.NORMAL);
        mortgageSep.setGravity(Gravity.CENTER);
        TextView mortgageNo = mortgageOption("否", true);
        TextView mortgageSepUnknown = label("|", BODY_SP, MUTED, Typeface.NORMAL);
        mortgageSepUnknown.setGravity(Gravity.CENTER);
        TextView mortgageUnknown = mortgageOption("不确定", false);
        mortgageYes.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        mortgageGroup.addView(mortgageYes, new LinearLayout.LayoutParams(dp(28), dp(40)));
        mortgageGroup.addView(mortgageSep, new LinearLayout.LayoutParams(dp(32), dp(40)));
        mortgageGroup.addView(mortgageNo, new LinearLayout.LayoutParams(dp(32), dp(40)));
        mortgageGroup.addView(mortgageSepUnknown, new LinearLayout.LayoutParams(dp(32), dp(40)));
        mortgageGroup.addView(mortgageUnknown, new LinearLayout.LayoutParams(dp(72), dp(40)));
        mortgageRow.addView(mortgageGroup, new LinearLayout.LayoutParams(0, dp(40), 1));
        body.addView(info);

        body.addView(step(null, "债务明细", "+ 添加"));
        debtRows = new LinearLayout(this);
        debtRows.setOrientation(LinearLayout.VERTICAL);
        debtRows.setLayoutTransition(smoothTransition());
        body.addView(debtRows);

        body.addView(step(null, "其他信息", null));
        concernField = input("请输入客户最担心的问题或影响");
        concernField.setMinLines(4);
        concernField.setGravity(Gravity.TOP);
        concernField.setPadding(dp(12), dp(12), dp(12), dp(12));
        concernField.setBackground(round(PAPER, LINE, 8));
        body.addView(concernField, new LinearLayout.LayoutParams(-1, dp(118)));

        Button save = primary("保存档案");
        body.addView(save, margin(-1, dp(44), 0, dp(14), 0, 0));
        signature();

        drafts.clear();
        hasMortgage = "否";
        if (client != null) fillForm(client);
        if (drafts.isEmpty()) drafts.add(new DebtDraft());
        expandedDebtIndex = drafts.size() - 1;
        paintMortgageOption(mortgageYes, "有".equals(hasMortgage));
        paintMortgageOption(mortgageNo, "否".equals(hasMortgage));
        paintMortgageOption(mortgageUnknown, "不确定".equals(hasMortgage));
        mortgageYes.setOnClickListener(v -> {
            hasMortgage = "有";
            paintMortgageOption(mortgageYes, true);
            paintMortgageOption(mortgageNo, false);
            paintMortgageOption(mortgageUnknown, false);
        });
        mortgageNo.setOnClickListener(v -> {
            hasMortgage = "否";
            paintMortgageOption(mortgageYes, false);
            paintMortgageOption(mortgageNo, true);
            paintMortgageOption(mortgageUnknown, false);
        });
        mortgageUnknown.setOnClickListener(v -> {
            hasMortgage = "不确定";
            paintMortgageOption(mortgageYes, false);
            paintMortgageOption(mortgageNo, false);
            paintMortgageOption(mortgageUnknown, true);
        });
        body.findViewWithTag("addDebt").setOnClickListener(v -> addDebtCard());
        renderDebts();
        save.setOnClickListener(v -> saveArchive());
    }

    private void fillForm(JSONObject client) {
        nameField.setText(opt(client, "name"));
        phoneField.setText(opt(client, "phone"));
        incomeField.setText(opt(client, "monthlyIncome"));
        concernField.setText(opt(client, "biggestConcern"));
        hasMortgage = normalizeMortgage(opt(client, "hasMortgage"));
        JSONArray arr = debts(client);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject d = arr.optJSONObject(i);
            if (d == null) continue;
            DebtDraft draft = new DebtDraft();
            draft.id = opt(d, "id");
            draft.type = valid(opt(d, "type"), debtTypes, "网贷");
            draft.creditor = opt(d, "creditor");
            draft.amount = opt(d, "amount");
            draft.status = valid(opt(d, "status"), statuses, "正常");
            draft.dueDate = opt(d, "dueDate");
            drafts.add(draft);
        }
    }

    private String normalizeMortgage(String value) {
        if ("是".equals(value) || "有".equals(value)) return "有";
        if ("不确定".equals(value)) return "不确定";
        return "否";
    }

    private void renderDebts() {
        debtRows.removeAllViews();
        openDebtSwipeCard = null;
        openDebtDeleteAction = null;
        for (int i = 0; i < drafts.size(); i++) {
            DebtDraft d = drafts.get(i);
            boolean expanded = i == expandedDebtIndex;
            boolean hasContent = hasDebtContent(d);
            boolean showSummary = !expanded || hasContent;
            SwipeFrameLayout swipeWrap = new SwipeFrameLayout(this);
            Button deleteAction = debtDeleteButton();
            deleteAction.setVisibility(View.INVISIBLE);
            FrameLayout.LayoutParams deleteLp = new FrameLayout.LayoutParams(dp(DEBT_DELETE_DP + DEBT_DELETE_OVERLAP_DP), -1, Gravity.RIGHT);
            swipeWrap.addView(deleteAction, deleteLp);
            LinearLayout card = debtCard();
            card.setTag(d);
            card.setMinimumHeight(expanded ? dp(showSummary ? 228 : 198) : dp(78));
            swipeWrap.addView(card, new FrameLayout.LayoutParams(-1, -2));
            debtRows.addView(swipeWrap, margin(-1, -2, 0, 0, 0, dp(10)));

            LinearLayout summary = new LinearLayout(this);
            summary.setGravity(Gravity.CENTER_VERTICAL);
            summary.setPadding(dp(14), dp(6), dp(12), 0);
            TypeIconView typeIcon = new TypeIconView(this, d.type);
            typeIcon.setTag("typeIcon");
            LinearLayout title = debtSummaryTitle(d);
            Button pill = statusPill(d.status);
            ChevronView arrow = new ChevronView(this, expanded);
            summary.addView(typeIcon, new LinearLayout.LayoutParams(dp(34), dp(38)));
            summary.addView(title, new LinearLayout.LayoutParams(0, dp(38), 1));
            summary.addView(pill, new LinearLayout.LayoutParams(dp(64), dp(28)));
            summary.addView(arrow, new LinearLayout.LayoutParams(dp(34), dp(38)));
            card.addView(summary);
            final int index = i;
            deleteAction.setOnClickListener(v -> confirmDeleteDebt(index));
            swipeWrap.configureSwipe(card, deleteAction);
            arrow.setOnClickListener(v -> {
                syncDrafts();
                expandedDebtIndex = expandedDebtIndex == index ? -1 : index;
                renderDebts();
            });

            if (showSummary) {
                TextView meta = label(debtSummaryMeta(d, hasContent), META_SP, MUTED, Typeface.NORMAL);
                meta.setPadding(dp(48), 0, dp(12), dp(6));
                card.addView(meta, new LinearLayout.LayoutParams(-1, dp(28)));
            }

            if (expanded) {
                LinearLayout expandedContent = new LinearLayout(this);
                expandedContent.setOrientation(LinearLayout.VERTICAL);
                expandedContent.setAlpha(0f);
                expandedContent.setTranslationY(dp(-4));
                insetDivider(card, dp(48), dp(30));
                TextView type = segmentedTypeRow(expandedContent, d.type);
                EditText creditor = textEditRow(expandedContent, "机构", d.creditor, "creditor");
                EditText amount = textEditRow(expandedContent, "金额(万)", d.amount, "amount");
                Button status = selectEditRow(expandedContent, "状态", d.status, "status", "›");
                Button due = selectEditRow(expandedContent, "到期日期", d.dueDate.isEmpty() ? "选择日期" : d.dueDate, "dueDate", "▣");
                card.addView(expandedContent);
                expandedContent.animate().alpha(1f).translationY(0).setDuration(180).start();
                status.setOnClickListener(v -> chooseStatus(status, statuses));
                due.setOnClickListener(v -> chooseDate(due, status));
                paintStatus(status, status.getText().toString());
                amount.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) keepInputVisible(v);
                    if (!hasFocus) amount.setText(formatDebtAmountInput(amount.getText().toString()));
                });
            } else {
                card.setOnClickListener(v -> {
                    syncDrafts();
                    expandedDebtIndex = index;
                    renderDebts();
                });
            }
        }
    }

    private LinearLayout debtSummaryTitle(DebtDraft d) {
        LinearLayout title = new LinearLayout(this);
        title.setGravity(Gravity.CENTER_VERTICAL);
        TextView type = label(d.type, PRIMARY_SP, INK, Typeface.BOLD);
        type.setTag("summaryType");
        type.setSingleLine(true);
        title.addView(type, new LinearLayout.LayoutParams(-2, dp(38)));
        if (!d.creditor.isEmpty()) {
            TextView sep = label(" | ", PRIMARY_SP, INK, Typeface.NORMAL);
            title.addView(sep, new LinearLayout.LayoutParams(-2, dp(38)));
            TextView creditor = label(d.creditor, PRIMARY_SP, INK, Typeface.NORMAL);
            creditor.setSingleLine(true);
            creditor.setEllipsize(TextUtils.TruncateAt.END);
            title.addView(creditor, new LinearLayout.LayoutParams(0, dp(38), 1));
        }
        return title;
    }

    private String debtSummaryMeta(DebtDraft d, boolean hasContent) {
        if (!hasContent) return "待填写";
        List<String> parts = new ArrayList<>();
        if (d.creditor.isEmpty()) parts.add("机构未填");
        parts.add(d.amount.isEmpty() ? "金额未填" : d.amount + "万");
        parts.add(d.dueDate.isEmpty() ? "到期未填" : d.dueDate);
        return join(parts, " · ");
    }

    private void confirmDeleteDebt(int index) {
        syncDrafts();
        if (index < 0 || index >= drafts.size()) return;
        new AlertDialog.Builder(this)
            .setTitle("删除债务")
            .setMessage("确认删除这笔债务？")
            .setNegativeButton("取消", null)
            .setPositiveButton("删除", (dialog, which) -> {
                drafts.remove(index);
                if (drafts.isEmpty()) drafts.add(new DebtDraft());
                expandedDebtIndex = Math.max(0, Math.min(index, drafts.size() - 1));
                renderDebts();
            })
            .show();
    }

    private Button debtDeleteButton() {
        Button button = new Button(this);
        button.setText("删除");
        button.setTextSize(BODY_SP);
        button.setTextColor(Color.WHITE);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setAllCaps(false);
        button.setPadding(0, 0, 0, 0);
        button.setMinHeight(0);
        button.setMinWidth(0);
        button.setAlpha(0f);
        button.setBackground(rightRound(DANGER, 12));
        button.setStateListAnimator(null);
        return button;
    }

    private void moveDebtSwipe(View swipeTarget, View deleteAction, float dx) {
        swipeTarget.animate().cancel();
        if (openDebtSwipeCard != null && openDebtSwipeCard != swipeTarget) {
            View previousDelete = openDebtDeleteAction;
            closeDebtSwipe(openDebtSwipeCard, previousDelete, 100);
        }
        float move = Math.max(-dp(DEBT_DELETE_DP), Math.min(0, dx));
        swipeTarget.setTranslationX(move);
        syncDebtDeleteVisibility(swipeTarget, deleteAction);
    }

    private void settleDebtSwipe(View swipeTarget, View deleteAction) {
        boolean open = swipeTarget.getTranslationX() < -dp(DEBT_DELETE_DP / 2f);
        swipeTarget.animate().cancel();
        deleteAction.animate().cancel();
        swipeTarget.animate()
            .translationX(open ? -dp(DEBT_DELETE_DP) : 0)
            .setDuration(120)
            .setUpdateListener((ValueAnimator animation) -> syncDebtDeleteVisibility(swipeTarget, deleteAction))
            .withEndAction(() -> {
                swipeTarget.animate().setUpdateListener(null);
                syncDebtDeleteVisibility(swipeTarget, deleteAction);
            })
            .start();
        openDebtSwipeCard = open ? swipeTarget : null;
        openDebtDeleteAction = open ? deleteAction : null;
    }

    private void closeDebtSwipe(View swipeTarget, View deleteAction, long duration) {
        if (swipeTarget == null) return;
        swipeTarget.animate().cancel();
        swipeTarget.animate()
            .translationX(0)
            .setDuration(duration)
            .setUpdateListener((ValueAnimator animation) -> syncDebtDeleteVisibility(swipeTarget, deleteAction))
            .withEndAction(() -> {
                swipeTarget.animate().setUpdateListener(null);
                syncDebtDeleteVisibility(swipeTarget, deleteAction);
            })
            .start();
    }

    private void syncDebtDeleteVisibility(View swipeTarget, View deleteAction) {
        if (deleteAction == null) return;
        float shown = Math.abs(swipeTarget == null ? 0 : swipeTarget.getTranslationX());
        if (shown <= dp(2)) {
            deleteAction.setAlpha(0f);
            deleteAction.setVisibility(View.INVISIBLE);
            return;
        }
        deleteAction.setVisibility(View.VISIBLE);
        deleteAction.setAlpha(Math.min(1f, shown / dp(DEBT_DELETE_DP)));
    }

    private void syncDrafts() {
        for (int i = 0; i < debtRows.getChildCount(); i++) {
            View v = debtRows.getChildAt(i);
            LinearLayout card = debtCardFrom(v);
            if (card == null || !(card.getTag() instanceof DebtDraft)) continue;
            DebtDraft d = (DebtDraft) card.getTag();
            TextView type = card.findViewWithTag("type");
            EditText creditor = card.findViewWithTag("creditor");
            EditText amount = card.findViewWithTag("amount");
            Button status = card.findViewWithTag("status");
            Button dueButton = card.findViewWithTag("dueDate");
            if (type == null || creditor == null || amount == null || status == null || dueButton == null) continue;
            d.type = cleanValue(type.getText().toString());
            d.creditor = creditor.getText().toString().trim();
            d.amount = formatDebtAmountInput(amount.getText().toString());
            d.status = cleanValue(status.getText().toString());
            String due = cleanValue(dueButton.getText().toString());
            d.dueDate = "选择日期".equals(due) ? "" : due;
        }
    }

    private LinearLayout debtCardFrom(View view) {
        if (view instanceof LinearLayout && view.getTag() instanceof DebtDraft) return (LinearLayout) view;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof LinearLayout && child.getTag() instanceof DebtDraft) return (LinearLayout) child;
            }
        }
        return null;
    }

    private void addDebtCard() {
        syncDrafts();
        drafts.add(new DebtDraft());
        expandedDebtIndex = drafts.size() - 1;
        renderDebts();
    }

    private LayoutTransition smoothTransition() {
        LayoutTransition transition = new LayoutTransition();
        transition.enableTransitionType(LayoutTransition.CHANGING);
        transition.setDuration(LayoutTransition.APPEARING, 160);
        transition.setDuration(LayoutTransition.DISAPPEARING, 120);
        transition.setDuration(LayoutTransition.CHANGE_APPEARING, 190);
        transition.setDuration(LayoutTransition.CHANGE_DISAPPEARING, 170);
        transition.setDuration(LayoutTransition.CHANGING, 190);
        return transition;
    }

    private boolean hasDebtContent(DebtDraft d) {
        return !d.creditor.isEmpty()
            || !d.amount.isEmpty()
            || !d.dueDate.isEmpty()
            || !"网贷".equals(d.type)
            || !"正常".equals(d.status);
    }

    private void saveArchive() {
        syncDrafts();
        String name = nameField.getText().toString().trim();
        if (name.isEmpty()) {
            nameField.setHintTextColor(DANGER);
            nameField.requestFocus();
            return;
        }
        JSONArray debtJson = new JSONArray();
        for (DebtDraft d : drafts) {
            if (d.creditor.isEmpty() && d.amount.isEmpty()) continue;
            if (d.creditor.isEmpty()) {
                Toast.makeText(this, "请输入债务机构", Toast.LENGTH_SHORT).show();
                return;
            }
            JSONObject item = new JSONObject();
            try {
                item.put("id", d.id.isEmpty() ? uid("debt") : d.id);
                item.put("type", d.type);
                item.put("creditor", d.creditor);
                item.put("amount", d.amount);
                item.put("status", d.status);
                item.put("dueDate", d.dueDate);
                debtJson.put(item);
            } catch (Exception ignored) {}
        }
        if (debtJson.length() == 0 && !hasCompleteBasicInfo()) {
            new AlertDialog.Builder(this)
                .setTitle("确认保存")
                .setMessage("当前只有基本资料，是否确认保存？")
                .setNegativeButton("取消", null)
                .setPositiveButton("确认保存", (dialog, which) -> saveArchiveData(name, debtJson))
                .show();
            return;
        }
        saveArchiveData(name, debtJson);
    }

    private boolean hasCompleteBasicInfo() {
        return !nameField.getText().toString().trim().isEmpty()
            && !phoneField.getText().toString().trim().isEmpty()
            && !incomeField.getText().toString().trim().isEmpty();
    }

    private void saveArchiveData(String name, JSONArray debtJson) {
        List<JSONObject> clients = loadClients();
        String now = iso();
        JSONObject archive = new JSONObject();
        try {
            archive.put("id", editingId.isEmpty() ? uid("client") : editingId);
            archive.put("name", name);
            archive.put("phone", phoneField.getText().toString().trim());
            archive.put("monthlyIncome", incomeField.getText().toString().trim());
            archive.put("hasMortgage", hasMortgage);
            archive.put("biggestConcern", concernField.getText().toString().trim());
            archive.put("debts", debtJson);
            archive.put("createdAt", now);
            archive.put("updatedAt", now);
            int index = findClient(clients, opt(archive, "id"), name);
            if (index >= 0) {
                archive.put("id", opt(clients.get(index), "id"));
                archive.put("createdAt", opt(clients.get(index), "createdAt"));
                clients.set(index, archive);
            } else {
                clients.add(archive);
            }
        } catch (Exception ignored) {}
        saveClients(clients);
        selectedId = opt(archive, "id");
        showDetail(selectedId);
    }

    private void showDetail(String id) {
        screen = "detail";
        selectedId = id;
        JSONObject c = getClient(id);
        if (c == null) { showHome(); return; }
        base("客户详情");
        leftAction.setText("‹ 返回");
        rightAction.setText("编辑");
        leftAction.setOnClickListener(v -> showHome());
        rightAction.setOnClickListener(v -> showForm(c));

        LinearLayout info = box();
        detail(info, "姓名：", empty(opt(c, "name")));
        detail(info, "联系电话：", empty(opt(c, "phone")));
        detail(info, "月收入：", empty(opt(c, "monthlyIncome")));
        detail(info, "总负债：", money(totalDebt(c)));
        detail(info, "最担心：", empty(opt(c, "biggestConcern")));
        body.addView(info);

        body.addView(step(null, "债务清单", null));
        LinearLayout table = box();
        table.addView(debtHead());
        JSONArray arr = debts(c);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject d = arr.optJSONObject(i);
            LinearLayout row = new LinearLayout(this);
            row.addView(cell(opt(d, "type"), .9f, MUTED));
            row.addView(cell(opt(d, "creditor"), 1.2f, INK));
            row.addView(cell(opt(d, "amount"), .98f, INK));
            row.addView(cell(opt(d, "status"), .76f, statusColor(opt(d, "status"))));
            row.addView(cell(empty(opt(d, "dueDate")), 1.16f, MUTED));
            table.addView(row);
        }
        body.addView(table);

        Button delete = primary("删除客户");
        delete.setTextColor(DANGER);
        delete.setOnClickListener(v -> confirmDelete(id));
        body.addView(delete, margin(-1, dp(44), 0, dp(18), 0, 0));
        signature();
    }

    private LinearLayout step(String num, String title, String add) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setGravity(Gravity.CENTER_VERTICAL);
        wrap.setPadding(0, dp(18), 0, dp(9));
        if (num != null) {
            TextView badge = label(num, 14, Color.WHITE, Typeface.BOLD);
            badge.setGravity(Gravity.CENTER);
            badge.setBackground(round(TEAL, TEAL, 99));
            LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(dp(22), dp(22));
            badgeLp.setMargins(0, 0, dp(5), 0);
            wrap.addView(badge, badgeLp);
        }
        TextView t = label(title, SECTION_SP, INK, Typeface.BOLD);
        wrap.addView(t, new LinearLayout.LayoutParams(0, -2, 1));
        if (add != null) {
            Button b = textButton(add, ACTION_SP);
            b.setTag("addDebt");
            wrap.addView(b);
        }
        return wrap;
    }

    private LinearLayout debtHead() {
        LinearLayout row = new LinearLayout(this);
        row.setBackgroundColor(Color.rgb(244, 248, 246));
        row.addView(cell("类型", .9f, MUTED));
        row.addView(cell("机构", 1.2f, MUTED));
        row.addView(cell("金额(万)", .98f, MUTED));
        row.addView(cell("状态", .76f, MUTED));
        row.addView(cell("到期日期", 1.16f, MUTED));
        return row;
    }

    private Button statusPill(String status) {
        Button b = new Button(this);
        b.setText(status);
        b.setTextSize(SMALL_SP);
        b.setTextColor(statusColor(status));
        b.setAllCaps(false);
        b.setPadding(0, 0, 0, 0);
        b.setMinHeight(0);
        b.setMinWidth(0);
        b.setIncludeFontPadding(false);
        b.setBackground(round(statusPillBg(status), statusPillStroke(status), 8));
        b.setStateListAnimator(null);
        b.setElevation(0);
        return b;
    }

    private Button selectEditRow(LinearLayout parent, String label, String value, String tag, String suffix) {
        LinearLayout row = editBaseRow(parent, label);
        Button button = editValueButton(value, suffix);
        button.setTag(tag);
        row.addView(button, new LinearLayout.LayoutParams(0, dp(40), 1));
        return button;
    }

    private TextView segmentedTypeRow(LinearLayout parent, String value) {
        LinearLayout row = editBaseRow(parent, "类型");
        TextView holder = new TextView(this);
        holder.setTag("type");
        holder.setText(value);
        holder.setVisibility(View.GONE);
        LinearLayout group = new LinearLayout(this);
        group.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        group.setPadding(0, 0, dp(18), 0);
        row.addView(holder, new LinearLayout.LayoutParams(0, 0));
        row.addView(group, new LinearLayout.LayoutParams(0, dp(48), 1));
        for (int i = 0; i < debtTypes.length; i++) {
            String type = debtTypes[i];
            TextView option = segmentOption(type, type.equals(value));
            option.setOnClickListener(v -> {
                holder.setText(type);
                LinearLayout card = (LinearLayout) parent.getParent();
                DebtDraft draft = card != null && card.getTag() instanceof DebtDraft ? (DebtDraft) card.getTag() : null;
                if (draft != null) draft.type = type;
                TextView summaryType = card == null ? null : card.findViewWithTag("summaryType");
                if (summaryType != null) summaryType.setText(type);
                TypeIconView typeIcon = card == null ? null : card.findViewWithTag("typeIcon");
                if (typeIcon != null) typeIcon.setType(type);
                for (int j = 0; j < group.getChildCount(); j++) {
                    View child = group.getChildAt(j);
                    if (child instanceof TextView) {
                        TextView text = (TextView) child;
                        paintSegmentOption(text, text.getText().toString().equals(type));
                    }
                }
            });
            float weight = "银行贷款".equals(type) ? 1.25f : 1f;
            group.addView(option, new LinearLayout.LayoutParams(0, dp(40), weight));
            if (i < debtTypes.length - 1) {
                TextView sep = label("|", BODY_SP, MUTED, Typeface.NORMAL);
                sep.setGravity(Gravity.CENTER);
                group.addView(sep, new LinearLayout.LayoutParams(dp(12), dp(40)));
            }
        }
        return holder;
    }

    private TextView segmentOption(String text, boolean active) {
        TextView b = new TextView(this);
        b.setText(text);
        b.setTextSize(BODY_SP);
        b.setGravity(Gravity.CENTER);
        b.setSingleLine(true);
        b.setPadding(0, 0, 0, 0);
        b.setIncludeFontPadding(false);
        paintSegmentOption(b, active);
        return b;
    }

    private void paintSegmentOption(TextView button, boolean active) {
        button.setTextColor(active ? TEAL : MUTED);
        button.setTypeface(Typeface.DEFAULT, active ? Typeface.BOLD : Typeface.NORMAL);
        button.setBackgroundColor(Color.TRANSPARENT);
    }

    private EditText textEditRow(LinearLayout parent, String label, String value, String tag) {
        LinearLayout row = editBaseRow(parent, label);
        EditText input = input("");
        input.setTag(tag);
        input.setText(value);
        input.setSingleLine(true);
        input.setTextSize(BODY_SP);
        input.setPadding(0, 0, dp(12), 0);
        row.addView(input, new LinearLayout.LayoutParams(0, dp(40), 1));
        return input;
    }

    private LinearLayout editBaseRow(LinearLayout parent, String text) {
        insetDivider(parent, dp(48), dp(30));
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView label = label(text, BODY_SP, MUTED, Typeface.NORMAL);
        label.setPadding(dp(48), 0, 0, 0);
        row.addView(label, new LinearLayout.LayoutParams(dp(168), dp(48)));
        parent.addView(row, new LinearLayout.LayoutParams(-1, dp(48)));
        return row;
    }

    private TextView cell(String text, float weight, int color) {
        TextView v = label(text, SMALL_SP, color, Typeface.NORMAL);
        v.setGravity(Gravity.CENTER);
        v.setBackgroundColor(Color.TRANSPARENT);
        v.setLayoutParams(new LinearLayout.LayoutParams(0, dp(40), weight));
        return v;
    }

    private EditText rowInput(LinearLayout box, String label, String hint, int labelWidth) {
        LinearLayout r = row(box, label, labelWidth);
        EditText e = input(hint);
        e.setPadding(dp(10), 0, dp(10), 0);
        r.addView(e, new LinearLayout.LayoutParams(0, dp(40), 1));
        return e;
    }

    private LinearLayout row(LinearLayout box, String label) {
        return row(box, label, dp(96));
    }

    private LinearLayout row(LinearLayout box, String label, int labelWidth) {
        if (box.getChildCount() > 0) divider(box);
        LinearLayout r = new LinearLayout(this);
        r.setGravity(Gravity.CENTER_VERTICAL);
        TextView l = label(label, BODY_SP, MUTED, Typeface.NORMAL);
        l.setPadding(dp(10), 0, 0, 0);
        l.setSingleLine(true);
        r.addView(l, new LinearLayout.LayoutParams(labelWidth, dp(40)));
        box.addView(r, new LinearLayout.LayoutParams(-1, dp(41)));
        return r;
    }

    private void detail(LinearLayout box, String label, String value) {
        LinearLayout r = row(box, label);
        TextView v = label(value, BODY_SP, INK, Typeface.BOLD);
        r.addView(v, new LinearLayout.LayoutParams(0, dp(40), 1));
    }

    private LinearLayout box() {
        LinearLayout b = new LinearLayout(this);
        b.setOrientation(LinearLayout.VERTICAL);
        b.setBackground(round(PAPER, LINE, 8));
        return b;
    }

    private LinearLayout debtCard() {
        LinearLayout b = new LinearLayout(this);
        b.setOrientation(LinearLayout.VERTICAL);
        b.setBackground(round(PAPER, LINE, 12));
        return b;
    }

    private EditText input(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextSize(BODY_SP);
        e.setTextColor(INK);
        e.setHintTextColor(Color.rgb(140, 150, 147));
        e.setSingleLine(false);
        e.setBackgroundColor(Color.TRANSPARENT);
        e.setPadding(dp(10), 0, dp(10), 0);
        e.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                focusedInput = v;
                keepInputVisible(v);
            } else if (focusedInput == v) {
                focusedInput = null;
            }
        });
        return e;
    }

    private void keepInputVisible(View v) {
        if (pageScroll == null) return;
        focusedInput = v;
        v.postDelayed(() -> scrollInputAboveKeyboard(v), 80);
        v.postDelayed(() -> scrollInputAboveKeyboard(v), 240);
        v.postDelayed(() -> scrollInputAboveKeyboard(v), 430);
    }

    private void installKeyboardAvoidance(View host) {
        host.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (pageScroll == null) return;
                Rect visible = new Rect();
                host.getWindowVisibleDisplayFrame(visible);
                int hidden = host.getRootView().getHeight() - visible.bottom;
                int keyboard = hidden > dp(120) ? hidden : 0;
                int bottom = baseScrollPaddingBottom + keyboard;
                if (pageScroll.getPaddingBottom() != bottom) {
                    pageScroll.setPadding(0, 0, 0, bottom);
                }
                if (focusedInput != null) {
                    focusedInput.postDelayed(() -> scrollInputAboveKeyboard(focusedInput), 80);
                }
            }
        });
    }

    private void scrollInputAboveKeyboard(View input) {
        if (pageScroll == null || input == null) return;
        Rect inputRect = new Rect();
        input.getDrawingRect(inputRect);
        pageScroll.offsetDescendantRectToMyCoords(input, inputRect);
        int visibleBottom = pageScroll.getScrollY() + pageScroll.getHeight() - pageScroll.getPaddingBottom();
        int overlap = inputRect.bottom + dp(32) - visibleBottom;
        if (overlap > 0) {
            pageScroll.smoothScrollBy(0, overlap);
        }
    }

    private Button editValueButton(String text, String suffix) {
        Button b = new Button(this);
        b.setText(suffix.isEmpty() ? text : text + "   " + suffix);
        b.setTextSize(BODY_SP);
        b.setTextColor(MUTED);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        b.setPadding(0, 0, dp(12), 0);
        b.setMinHeight(0);
        b.setMinWidth(0);
        b.setIncludeFontPadding(false);
        b.setBackgroundColor(Color.TRANSPARENT);
        return b;
    }

    private void chooseStatus(Button target, String[] values) {
        new AlertDialog.Builder(this)
            .setItems(values, (dialog, which) -> {
                target.setText(values[which] + "   ›");
                paintStatus(target, values[which]);
            })
            .show();
    }

    private void chooseDate(Button target, Button statusTarget) {
        Calendar calendar = Calendar.getInstance();
        String current = target.getText().toString();
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(current);
            calendar.setTime(date);
        } catch (Exception ignored) {}
        new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                String value = String.format(Locale.CHINA, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                String status = statusFromDueDate(value);
                target.setText(value + "   ▣");
                statusTarget.setText(status + "   ›");
                paintStatus(statusTarget, status);
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private String statusFromDueDate(String dueDate) {
        Integer days = daysUntilDue(dueDate);
        if (days != null && days < 0) return "逾期";
        if (days != null && days <= 30) return "提醒";
        return "正常";
    }

    private Integer daysUntilDue(String value) {
        try {
            if (value == null || value.trim().isEmpty()) return null;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            Date date = format.parse(value);
            Date today = format.parse(format.format(new Date()));
            return (int) Math.ceil((date.getTime() - today.getTime()) / 86400000.0);
        } catch (Exception e) {
            return null;
        }
    }

    private void paintStatus(Button button, String status) {
        button.setTextColor(statusColor(cleanValue(status)));
    }

    private String formatDebtAmountInput(String value) {
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

    private TextView mortgageOption(String text, boolean active) {
        TextView option = label(text, BODY_SP, active ? TEAL : MUTED, active ? Typeface.BOLD : Typeface.NORMAL);
        option.setGravity(Gravity.CENTER);
        option.setIncludeFontPadding(false);
        return option;
    }

    private void paintMortgageOption(TextView option, boolean active) {
        option.setTextColor(active ? TEAL : MUTED);
        option.setTypeface(Typeface.DEFAULT, active ? Typeface.BOLD : Typeface.NORMAL);
    }

    private Button primary(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(META_SP);
        b.setTextColor(TEAL);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setAllCaps(false);
        b.setBackground(round(PAPER, TEAL, 10));
        return b;
    }

    private Button textButton(String text, float size) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(size);
        b.setTextColor(TEAL);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setAllCaps(false);
        b.setBackgroundColor(Color.TRANSPARENT);
        return b;
    }

    private Button topButton(String text) {
        Button b = textButton(text, ACTION_SP);
        b.setGravity(Gravity.CENTER);
        return b;
    }

    private TextView label(String text, float size, int color, int style) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setTypeface(Typeface.DEFAULT, style);
        v.setGravity(Gravity.CENTER_VERTICAL);
        return v;
    }

    private TextView padText(String text) {
        TextView v = label(text, META_SP, MUTED, Typeface.NORMAL);
        v.setPadding(dp(10), dp(14), dp(10), dp(14));
        return v;
    }

    private void divider(LinearLayout parent) {
        View line = new View(this);
        line.setBackgroundColor(DEBT_LINE);
        parent.addView(line, new LinearLayout.LayoutParams(-1, 1));
    }

    private void insetDivider(LinearLayout parent, int left, int right) {
        View line = new View(this);
        line.setBackgroundColor(DEBT_LINE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, 1);
        lp.setMargins(left, 0, right, 0);
        parent.addView(line, lp);
    }

    private void signature() {
        TextView s = label("轻债助手 · v 1.2.3", SMALL_SP, Color.rgb(168, 176, 173), Typeface.NORMAL);
        s.setGravity(Gravity.CENTER);
        body.addView(s, new LinearLayout.LayoutParams(-1, dp(52)));
    }

    private List<JSONObject> loadClients() {
        SharedPreferences sp = getSharedPreferences("archive", Context.MODE_PRIVATE);
        List<JSONObject> out = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(sp.getString(STORE, "[]"));
            for (int i = 0; i < arr.length(); i++) if (arr.optJSONObject(i) != null) out.add(arr.optJSONObject(i));
        } catch (Exception ignored) {}
        return out;
    }

    private void saveClients(List<JSONObject> clients) {
        JSONArray arr = new JSONArray();
        for (JSONObject c : clients) arr.put(c);
        getSharedPreferences("archive", Context.MODE_PRIVATE).edit().putString(STORE, arr.toString()).apply();
    }

    private void openImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        startActivityForResult(intent, REQ_IMPORT);
    }

    private void openExport() {
        String stamp = new SimpleDateFormat("yyyyMMdd-HHmm", Locale.CHINA).format(new Date());
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "债务客户档案-" + stamp + ".json");
        startActivityForResult(intent, REQ_EXPORT);
    }

    private void importBackup(Uri uri) {
        try {
            String raw = readText(uri);
            JSONArray clients;
            try {
                JSONObject object = new JSONObject(raw);
                clients = object.optJSONArray("clients");
                if (clients == null) throw new Exception("missing clients");
            } catch (Exception objectError) {
                clients = new JSONArray(raw);
            }
            List<JSONObject> next = new ArrayList<>();
            for (int i = 0; i < clients.length(); i++) {
                JSONObject item = clients.optJSONObject(i);
                if (item != null) next.add(item);
            }
            saveClients(next);
            Toast.makeText(this, "导入完成", Toast.LENGTH_SHORT).show();
            showHome();
        } catch (Exception e) {
            Toast.makeText(this, "导入失败，请选择有效的客户档案 JSON 文件。", Toast.LENGTH_LONG).show();
        }
    }

    private void exportBackup(Uri uri) {
        try {
            JSONObject backup = new JSONObject();
            backup.put("app", "debt-customer-archive");
            backup.put("version", "1.2.3");
            backup.put("storageKey", STORE);
            backup.put("exportedAt", iso());
            JSONArray clients = new JSONArray();
            for (JSONObject client : loadClients()) clients.put(client);
            backup.put("clients", clients);
            writeText(uri, backup.toString(2));
            Toast.makeText(this, "导出完成", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "导出失败", Toast.LENGTH_LONG).show();
        }
    }

    private String readText(Uri uri) throws Exception {
        InputStream input = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int count;
        while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
        input.close();
        return output.toString("UTF-8");
    }

    private void writeText(Uri uri, String text) throws Exception {
        OutputStream output = getContentResolver().openOutputStream(uri);
        output.write(text.getBytes("UTF-8"));
        output.close();
    }

    private JSONObject getClient(String id) {
        for (JSONObject c : loadClients()) if (opt(c, "id").equals(id)) return c;
        return null;
    }

    private void confirmDelete(String id) {
        new AlertDialog.Builder(this)
            .setMessage("确认删除这个客户档案？")
            .setNegativeButton("取消", null)
            .setPositiveButton("删除", (d, w) -> {
                List<JSONObject> clients = loadClients();
                for (int i = clients.size() - 1; i >= 0; i--) if (opt(clients.get(i), "id").equals(id)) clients.remove(i);
                saveClients(clients);
                showHome();
            })
            .show();
    }

    private JSONArray debts(JSONObject c) {
        JSONArray arr = c == null ? null : c.optJSONArray("debts");
        return arr == null ? new JSONArray() : arr;
    }

    private long totalDebt(JSONObject c) {
        long total = 0;
        JSONArray arr = debts(c);
        for (int i = 0; i < arr.length(); i++) total += parseDebtAmount(opt(arr.optJSONObject(i), "amount"));
        return total;
    }

    private long parseDebtAmount(String value) {
        String raw = value == null ? "" : value.replace(",", "").trim();
        if (raw.isEmpty()) return 0;
        try {
            double n = Double.parseDouble(raw.replaceAll("[^\\d.]", ""));
            return Math.round(raw.contains("万") || n < 10000 ? n * 10000 : n);
        } catch (Exception e) {
            return 0;
        }
    }

    private String money(long value) {
        if (value >= 10000) {
            double n = value / 10000.0;
            return String.format(Locale.CHINA, "%.2f", n).replaceAll("0+$", "").replaceAll("\\.$", "") + "万";
        }
        return String.valueOf(value);
    }

    private int findClient(List<JSONObject> clients, String id, String name) {
        for (int i = 0; i < clients.size(); i++) {
            JSONObject c = clients.get(i);
            if ((!id.isEmpty() && opt(c, "id").equals(id)) || opt(c, "name").equals(name)) return i;
        }
        return -1;
    }

    private int alertCount() {
        int count = 0;
        for (JSONObject client : loadClients()) {
            if (hasDueAlert(client)) count++;
        }
        return count;
    }

    private boolean hasDueAlert(JSONObject client) {
        JSONArray arr = debts(client);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject debt = arr.optJSONObject(i);
            if (debt == null) continue;
            if (!debtAlertLabel(debt).isEmpty()) return true;
        }
        return false;
    }

    private String alertSummary(JSONObject client) {
        JSONArray arr = debts(client);
        int remindCount = 0;
        int overdueCount = 0;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject debt = arr.optJSONObject(i);
            if (debt == null) continue;
            String label = debtAlertLabel(debt);
            if ("提醒".equals(label)) remindCount++;
            if ("逾期".equals(label)) overdueCount++;
        }
        List<String> items = new ArrayList<>();
        if (remindCount > 0) items.add(remindCount + "个提醒");
        if (overdueCount > 0) items.add(overdueCount + "个逾期");
        return join(items, "，");
    }

    private String debtAlertLabel(JSONObject debt) {
        String status = opt(debt, "status");
        if ("结清".equals(status) || "协商".equals(status)) return "";
        if ("逾期".equals(status) || "催收".equals(status)) return "逾期";
        if ("提醒".equals(status)) return "提醒";
        Integer days = daysUntilDue(opt(debt, "dueDate"));
        if (days != null && days < 0) return "逾期";
        if (days != null && days <= 30) return "提醒";
        return "";
    }

    private String join(List<String> items, String separator) {
        StringBuilder out = new StringBuilder();
        for (String item : items) {
            if (out.length() > 0) out.append(separator);
            out.append(item);
        }
        return out.toString();
    }

    private void colorAlertPart(SpannableString span, String text, String keyword, int color) {
        int index = text.indexOf(keyword);
        while (index >= 0) {
            int start = index;
            while (start > 0 && text.charAt(start - 1) != '，' && text.charAt(start - 1) != '\n') start--;
            int end = index + keyword.length();
            while (end < text.length() && text.charAt(end) != '，') end++;
            span.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            index = text.indexOf(keyword, end);
        }
    }

    private void paintFilter(Button button, String mode) {
        boolean active = clientFilterMode.equals(mode);
        button.setTextColor(active ? TEAL : MUTED);
        button.setTypeface(Typeface.DEFAULT, active ? Typeface.BOLD : Typeface.NORMAL);
    }

    private int statusColor(String status) {
        if ("逾期".equals(status) || "催收".equals(status)) return DANGER;
        if ("提醒".equals(status)) return Color.rgb(214, 149, 0);
        if ("协商".equals(status) || "正常".equals(status)) return TEAL;
        if ("结清".equals(status)) return MUTED;
        return INK;
    }

    private int statusPillBg(String status) {
        if ("逾期".equals(status) || "催收".equals(status)) return Color.rgb(255, 242, 242);
        if ("提醒".equals(status)) return Color.rgb(255, 248, 232);
        return TEAL_SOFT;
    }

    private int statusPillStroke(String status) {
        if ("逾期".equals(status) || "催收".equals(status)) return Color.rgb(255, 205, 205);
        if ("提醒".equals(status)) return Color.rgb(250, 224, 174);
        return LINE;
    }

    private String valid(String value, String[] values, String fallback) {
        for (String item : values) if (item.equals(value)) return item;
        return fallback;
    }

    private String opt(JSONObject obj, String key) { return obj == null ? "" : obj.optString(key, ""); }
    private String cleanValue(String value) { return value.replace("›", "").replace("▣", "").trim(); }
    private String empty(String value) { return value == null || value.trim().isEmpty() ? "未填写" : value; }
    private String uid(String prefix) { return prefix + "-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8); }
    private String iso() { return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.CHINA).format(new Date()); }
    private String fmt(String iso) {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.CHINA).parse(iso);
            return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(d);
        } catch (Exception e) {
            return "未记录";
        }
    }

    private GradientDrawable round(int color, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        d.setStroke(1, stroke);
        return d;
    }

    private GradientDrawable rightRound(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        float r = dp(radius);
        d.setCornerRadii(new float[]{0, 0, r, r, r, r, 0, 0});
        return d;
    }

    private GradientDrawable dashedRound(int color, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        d.setStroke(1, stroke, dp(6), dp(4));
        return d;
    }

    private LinearLayout.LayoutParams margin(int w, int h, int l, int t, int r, int b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h);
        p.setMargins(l, t, r, b);
        return p;
    }

    private int screenWidth() { return getResources().getDisplayMetrics().widthPixels; }
    private int topInset() {
        int id = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return (id > 0 ? getResources().getDimensionPixelSize(id) : dp(24)) + dp(8);
    }
    private int dp(float v) { return (int) (v * getResources().getDisplayMetrics().density + .5f); }

    private class SwipeFrameLayout extends FrameLayout {
        private View swipeTarget;
        private View deleteAction;
        private float startX;
        private float startY;
        private boolean swiping;

        SwipeFrameLayout(Context context) {
            super(context);
        }

        void configureSwipe(View swipeTarget, View deleteAction) {
            this.swipeTarget = swipeTarget;
            this.deleteAction = deleteAction;
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
            if (swipeTarget == null || deleteAction == null) return super.onInterceptTouchEvent(event);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getRawX();
                    startY = event.getRawY();
                    swiping = false;
                    return false;
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - startX;
                    float dy = event.getRawY() - startY;
                    if (Math.abs(dx) > dp(8) && Math.abs(dx) > Math.abs(dy) * 1.3f) {
                        swiping = true;
                        return true;
                    }
                    return false;
            }
            return super.onInterceptTouchEvent(event);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (swipeTarget == null || deleteAction == null) return super.onTouchEvent(event);
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    moveDebtSwipe(swipeTarget, deleteAction, event.getRawX() - startX);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    settleDebtSwipe(swipeTarget, deleteAction);
                    swiping = false;
                    return true;
            }
            return swiping || super.onTouchEvent(event);
        }
    }

    private class TypeIconView extends View {
        private String type;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        TypeIconView(Context context, String type) {
            super(context);
            this.type = type == null ? "" : type;
            paint.setColor(Color.rgb(156, 166, 163));
            paint.setStrokeWidth(dp(1.35f));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            textPaint.setColor(Color.rgb(156, 166, 163));
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
            textPaint.setTextSize(dp(8.5f));
        }

        void setType(String type) {
            this.type = type == null ? "" : type;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            if ("信用卡".equals(type)) {
                drawCard(canvas, cx, cy);
            } else if ("银行贷款".equals(type)) {
                drawBank(canvas, cx, cy);
            } else {
                drawPhone(canvas, cx, cy);
            }
        }

        private void drawPhone(Canvas canvas, float cx, float cy) {
            float left = cx - dp(9);
            float top = cy - dp(10);
            float right = cx + dp(3);
            float bottom = cy + dp(8);
            float radius = dp(2);
            RectF topLeft = new RectF(left, top, left + radius * 2, top + radius * 2);
            RectF topRight = new RectF(right - radius * 2, top, right, top + radius * 2);
            RectF bottomLeft = new RectF(left, bottom - radius * 2, left + radius * 2, bottom);
            canvas.drawArc(topLeft, 180, 90, false, paint);
            canvas.drawArc(topRight, 270, 90, false, paint);
            canvas.drawArc(bottomLeft, 90, 90, false, paint);
            canvas.drawLine(left + radius, top, right - radius, top, paint);
            canvas.drawLine(left, top + radius, left, bottom - radius, paint);
            canvas.drawLine(right, top + radius, right, cy - dp(2.2f), paint);
            canvas.drawLine(left + radius, bottom, cx - dp(1.4f), bottom, paint);
            float coinX = cx + dp(5.2f);
            float coinY = cy + dp(4.8f);
            canvas.drawCircle(coinX, coinY, dp(5.4f), paint);
            Paint.FontMetrics metrics = textPaint.getFontMetrics();
            float baseline = coinY - (metrics.ascent + metrics.descent) / 2f;
            canvas.drawText("¥", coinX, baseline, textPaint);
        }

        private void drawCard(Canvas canvas, float cx, float cy) {
            RectF card = new RectF(cx - dp(9), cy - dp(6.5f), cx + dp(9), cy + dp(6.5f));
            canvas.drawRoundRect(card, dp(2), dp(2), paint);
            canvas.drawLine(cx - dp(6.5f), cy - dp(1.5f), cx + dp(6.5f), cy - dp(1.5f), paint);
            canvas.drawLine(cx - dp(5.5f), cy + dp(3), cx - dp(1.5f), cy + dp(3), paint);
        }

        private void drawBank(Canvas canvas, float cx, float cy) {
            canvas.drawLine(cx - dp(9), cy - dp(4.5f), cx, cy - dp(9), paint);
            canvas.drawLine(cx, cy - dp(9), cx + dp(9), cy - dp(4.5f), paint);
            canvas.drawLine(cx - dp(7.5f), cy - dp(3), cx + dp(7.5f), cy - dp(3), paint);
            for (float x : new float[]{cx - dp(5), cx, cx + dp(5)}) {
                canvas.drawLine(x, cy - dp(1), x, cy + dp(6), paint);
            }
            canvas.drawLine(cx - dp(8.5f), cy + dp(8), cx + dp(8.5f), cy + dp(8), paint);
        }
    }

    private class ChevronView extends View {
        private final boolean expanded;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        ChevronView(Context context, boolean expanded) {
            super(context);
            this.expanded = expanded;
            paint.setColor(MUTED);
            paint.setStrokeWidth(dp(2.2f));
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            float half = dp(5.5f);
            float rise = dp(4.5f);
            if (expanded) {
                canvas.drawLine(cx - half, cy + rise / 2f, cx, cy - rise / 2f, paint);
                canvas.drawLine(cx, cy - rise / 2f, cx + half, cy + rise / 2f, paint);
            } else {
                canvas.drawLine(cx - half, cy - rise / 2f, cx, cy + rise / 2f, paint);
                canvas.drawLine(cx, cy + rise / 2f, cx + half, cy - rise / 2f, paint);
            }
        }
    }

    private static class DebtDraft {
        String id = "";
        String type = "网贷";
        String creditor = "";
        String amount = "";
        String status = "正常";
        String dueDate = "";
    }
}
