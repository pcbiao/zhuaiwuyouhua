const defaultRules = {
  capacity: {
    baseIncomeRatio: 0.42,
    mortgageIncomeRatio: 0.32,
    minimumLivingCost: 3500
  },
  riskScore: {
    overdue30Days: 18,
    overdue90Days: 34,
    overdue180Days: 48,
    collected: 18,
    debtIncomeRatioHigh: 16,
    debtIncomeRatioSevere: 28,
    mortgagePressure: 8
  },
  priorityWeights: {
    overdue: 35,
    collection: 18,
    bankRelationship: 16,
    platformRisk: 12,
    amountShare: 19
  },
  negotiation: {
    creditCard: [
      "优先申请停息分期或个性化分期，先说明收入、负债结构和稳定还款意愿。",
      "准备收入证明、困难说明、负债清单和近 3 个月流水，沟通目标为降低月供波动。"
    ],
    onlineLoan: [
      "先核对综合费率、服务费、担保费和已还金额，避免在争议未厘清前盲目展期。",
      "对催收强的平台保留通话、短信和还款协商记录，沟通只围绕本金、合法利息和可执行周期。"
    ],
    bankLoan: [
      "银行贷款以保征信、保资产、保现金流为第一顺位，争取展期、重组或阶段性降低月供。",
      "若已逾期，先联系最大欠款银行，避免进入诉讼或资产保全节奏。"
    ]
  },
  phasePlan: [
    {
      title: "第 1-7 天：盘点与止损",
      actions: [
        "整理所有合同、账单、利率、还款日、逾期天数和催收记录。",
        "停止以贷养贷和短期高息周转，把收入优先放入基本生活、房贷和协商资金池。",
        "向最大欠款机构做第一次主动沟通，记录对方工号、方案和时间。"
      ]
    },
    {
      title: "第 8-30 天：协商与排序",
      actions: [
        "按风险和金额占比逐一沟通，先处理可能起诉、强催收或影响资产的债务。",
        "把可承受月供拆成固定预算，不承诺超过收入承压线的还款方案。",
        "对不合理费用提出书面核账请求，保留全部沟通证据。"
      ]
    },
    {
      title: "第 31-90 天：执行与复盘",
      actions: [
        "每周更新剩余本金、已还金额和下一还款日，发现预算偏离及时调整。",
        "优先清理小额高压账户，减少催收触点，再集中处理大额主债权人。",
        "收入提升或支出下降后，把新增现金流优先投向逾期时间最长的账户。"
      ]
    }
  ]
};

const form = document.querySelector("#clientForm");
const appViews = document.querySelectorAll(".app-view");
const navButtons = document.querySelectorAll(".nav-btn");
const saveInfoBtn = document.querySelector("#saveInfoBtn");
const clientSaveStatus = document.querySelector("#clientSaveStatus");
const clientSearchInput = document.querySelector("#clientSearchInput");
const clientList = document.querySelector("#clientList");
const clientCount = document.querySelector("#clientCount");
const viewKicker = document.querySelector("#viewKicker");
const viewTitle = document.querySelector("#viewTitle");
const detailName = document.querySelector("#detailName");
const detailUpdatedAt = document.querySelector("#detailUpdatedAt");
const detailFields = document.querySelector("#detailFields");
const planOutput = document.querySelector("#planOutput");
const emptyState = document.querySelector("#emptyState");
const copyPlanBtn = document.querySelector("#copyPlanBtn");
const paymentYear = document.querySelector("#paymentYear");
const paymentMonth = document.querySelector("#paymentMonth");
const paymentDay = document.querySelector("#paymentDay");

let selectedClientId = null;

const viewMeta = {
  entry: { kicker: "客户录入", title: "保存客户信息", showSave: true },
  library: { kicker: "客户库", title: "查询客户档案", showSave: false },
  detail: { kicker: "客户详情", title: "档案与方案", showSave: false }
};

function setActiveView(viewName) {
  appViews.forEach((view) => {
    view.classList.toggle("is-active", view.dataset.view === viewName);
  });
  navButtons.forEach((button) => {
    button.classList.toggle("is-active", button.dataset.targetView === viewName);
  });
  const meta = viewMeta[viewName] || viewMeta.entry;
  viewKicker.textContent = meta.kicker;
  viewTitle.textContent = meta.title;
  saveInfoBtn.hidden = !meta.showSave;
  window.scrollTo({ top: 0, behavior: "smooth" });
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function parseMoney(value) {
  if (!value) return 0;
  const raw = String(value).trim().replace(/,/g, "");
  const number = Number(raw.replace(/[^\d.]/g, ""));
  if (Number.isNaN(number)) return 0;
  return raw.includes("万") ? number * 10000 : number;
}

function parseOverdueDays(value) {
  const raw = String(value || "").trim();
  if (!raw || /未|无|0/.test(raw)) return 0;
  const number = Number(raw.replace(/[^\d.]/g, ""));
  if (Number.isNaN(number)) return 0;
  if (raw.includes("年")) return number * 365;
  if (raw.includes("月")) return number * 30;
  if (raw.includes("周")) return number * 7;
  return number;
}

function formatMoney(value) {
  if (!Number.isFinite(value)) return "0 元";
  if (Math.abs(value) >= 10000) return `${(value / 10000).toFixed(1)} 万`;
  return `${Math.round(value).toLocaleString("zh-CN")} 元`;
}

function formatDateTime(value) {
  if (!value) return "未保存";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "未保存";
  return date.toLocaleString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  });
}

function getFormData() {
  syncPaymentDateField();
  return Object.fromEntries(new FormData(form).entries());
}

function fillForm(data) {
  Object.entries(data).forEach(([key, value]) => {
    const field = form.elements[key];
    if (field) field.value = value ?? "";
  });
  setPaymentDateSelects(data.paymentDate);
}

function padDatePart(value) {
  return String(value).padStart(2, "0");
}

function getDaysInMonth(year, month) {
  return new Date(Number(year), Number(month), 0).getDate();
}

function populateSelect(select, values, suffix = "") {
  select.innerHTML = values
    .map((value) => `<option value="${value}">${value}${suffix}</option>`)
    .join("");
}

function populatePaymentDateSelectors() {
  const currentYear = new Date().getFullYear();
  populateSelect(
    paymentYear,
    Array.from({ length: 7 }, (_, index) => currentYear - 1 + index),
    "年"
  );
  populateSelect(paymentMonth, Array.from({ length: 12 }, (_, index) => index + 1), "月");
  updatePaymentDays();
}

function updatePaymentDays() {
  const selectedDay = Number(paymentDay.value) || new Date().getDate();
  const days = getDaysInMonth(paymentYear.value, paymentMonth.value);
  populateSelect(paymentDay, Array.from({ length: days }, (_, index) => index + 1), "日");
  paymentDay.value = Math.min(selectedDay, days);
  syncPaymentDateField();
}

function syncPaymentDateField() {
  const field = form.elements.paymentDate;
  if (!field || !paymentYear.value || !paymentMonth.value || !paymentDay.value) return;
  field.value = `${paymentYear.value}-${padDatePart(paymentMonth.value)}-${padDatePart(paymentDay.value)}`;
}

function setPaymentDateSelects(value) {
  const date = value ? new Date(value) : new Date();
  const validDate = Number.isNaN(date.getTime()) ? new Date() : date;
  paymentYear.value = validDate.getFullYear();
  paymentMonth.value = validDate.getMonth() + 1;
  updatePaymentDays();
  paymentDay.value = validDate.getDate();
  syncPaymentDateField();
}

function clearPlan() {
  planOutput.hidden = true;
  planOutput.innerHTML = "";
  emptyState.hidden = false;
}

function loadClients() {
  try {
    return JSON.parse(localStorage.getItem("debtOptimizationClients") || "[]");
  } catch {
    return [];
  }
}

function saveClients(clients) {
  localStorage.setItem("debtOptimizationClients", JSON.stringify(clients));
}

function setClientStatus(message, type = "muted") {
  clientSaveStatus.textContent = message;
  clientSaveStatus.dataset.type = type;
}

function summarizeClient(client) {
  const debt = parseMoney(client.totalDebt);
  const income = parseMoney(client.monthlyIncome);
  const debtText = debt ? `负债 ${formatMoney(debt)}` : "负债未填";
  const incomeText = income ? `收入 ${formatMoney(income)}` : "收入未填";
  const overdueText = client.overdue ? `逾期 ${client.overdue}` : "逾期未填";
  return `${debtText} · ${incomeText} · ${overdueText}`;
}

function saveCurrentClient() {
  const client = getFormData();
  const name = client.name.trim();
  if (!name) {
    setClientStatus("请先填写客户姓名，再保存。", "error");
    setActiveView("entry");
    return;
  }

  const clients = loadClients();
  const existingIndex = clients.findIndex((item) => (item.name || "").trim() === name);
  const now = new Date().toISOString();
  const record = {
    ...client,
    id: existingIndex >= 0 ? clients[existingIndex].id : `client-${Date.now()}`,
    createdAt: existingIndex >= 0 ? clients[existingIndex].createdAt : now,
    updatedAt: now
  };

  if (existingIndex >= 0) {
    clients[existingIndex] = record;
    setClientStatus(`已更新 ${name} 的客户资料。`, "ok");
  } else {
    clients.push(record);
    setClientStatus(`已保存 ${name} 到客户库。`, "ok");
  }

  saveClients(clients);
  renderClientLibrary();
  setActiveView("library");
}

function renderClientLibrary() {
  const query = clientSearchInput.value.trim().toLowerCase();
  const clients = loadClients().sort((a, b) => (b.updatedAt || "").localeCompare(a.updatedAt || ""));
  const filtered = query
    ? clients.filter((client) => (client.name || "").toLowerCase().includes(query))
    : clients;

  clientCount.textContent = `${clients.length} 位`;

  if (!filtered.length) {
    clientList.innerHTML = `<div class="empty-card">${query ? "没有找到这个姓名的客户。" : "还没有客户档案。先到录入页保存信息。"}</div>`;
    return;
  }

  clientList.innerHTML = filtered
    .map(
      (client) => `
        <button class="client-card" type="button" data-client-id="${client.id}">
          <span>
            <strong>${escapeHtml(client.name || "未命名客户")}</strong>
            <span>${escapeHtml(summarizeClient(client))}</span>
            <small>更新：${escapeHtml(formatDateTime(client.updatedAt))}</small>
          </span>
          <em>详情</em>
        </button>
      `
    )
    .join("");
}

function getClientById(clientId) {
  return loadClients().find((client) => client.id === clientId);
}

function showClientDetail(clientId) {
  const client = getClientById(clientId);
  if (!client) {
    selectedClientId = null;
    renderClientLibrary();
    setActiveView("library");
    return;
  }

  selectedClientId = client.id;
  detailName.textContent = client.name || "未命名客户";
  detailUpdatedAt.textContent = `最近更新：${formatDateTime(client.updatedAt)}`;
  clearPlan();

  const fields = [
    ["月收入", client.monthlyIncome],
    ["总负债", client.totalDebt],
    ["信用卡", client.creditCard],
    ["网贷", client.onlineLoan],
    ["银行贷款", client.bankLoan],
    ["最大欠款银行", client.largestBank],
    ["最大欠款平台", client.largestPlatform],
    ["逾期多久", client.overdue],
    ["还款日", client.paymentDate],
    ["是否有房贷", client.hasMortgage],
    ["是否被催收", client.isCollected],
    ["最担心什么", client.biggestConcern]
  ];

  detailFields.innerHTML = fields
    .map(
      ([label, value]) => `
        <div class="detail-field">
          <span>${escapeHtml(label)}</span>
          <strong>${escapeHtml(value || "未填写")}</strong>
        </div>
      `
    )
    .join("");

  setActiveView("detail");
}

function editSelectedClient() {
  const client = getClientById(selectedClientId);
  if (!client) return;
  fillForm(client);
  setClientStatus(`正在编辑 ${client.name || "客户"}。保存后会更新客户库。`, "muted");
  setActiveView("entry");
}

function deleteSelectedClient() {
  const client = getClientById(selectedClientId);
  if (!client) return;
  if (!window.confirm(`确定删除 ${client.name || "这个客户"} 的档案吗？`)) return;

  saveClients(loadClients().filter((item) => item.id !== selectedClientId));
  selectedClientId = null;
  renderClientLibrary();
  setActiveView("library");
}

function calculateRisk(client, rules, overdueDays, debtIncomeRatio) {
  let score = 12;
  if (overdueDays >= 30) score += rules.riskScore.overdue30Days;
  if (overdueDays >= 90) score += rules.riskScore.overdue90Days;
  if (overdueDays >= 180) score += rules.riskScore.overdue180Days;
  if (client.isCollected === "是") score += rules.riskScore.collected;
  if (debtIncomeRatio >= 18) score += rules.riskScore.debtIncomeRatioHigh;
  if (debtIncomeRatio >= 30) score += rules.riskScore.debtIncomeRatioSevere;
  if (client.hasMortgage === "是") score += rules.riskScore.mortgagePressure;
  return Math.min(score, 100);
}

function riskLabel(score) {
  if (score >= 72) return { text: "高风险", className: "danger" };
  if (score >= 44) return { text: "中风险", className: "warn" };
  return { text: "可控", className: "ok" };
}

function buildPriorityQueue(client, rules, debts, overdueDays) {
  const total = debts.reduce((sum, item) => sum + item.amount, 0) || 1;
  return debts
    .filter((item) => item.amount > 0)
    .map((item) => {
      let score = (item.amount / total) * rules.priorityWeights.amountShare;
      if (overdueDays > 0) score += rules.priorityWeights.overdue;
      if (client.isCollected === "是") score += rules.priorityWeights.collection;
      if (item.key === "bankLoan") score += rules.priorityWeights.bankRelationship;
      if (item.key === "onlineLoan") score += rules.priorityWeights.platformRisk;
      const reason = [];
      if (item.key === "bankLoan") reason.push("银行贷款关系到征信、资产和诉讼节奏");
      if (item.key === "creditCard") reason.push("信用卡适合优先谈停息分期和账单重组");
      if (item.key === "onlineLoan") reason.push("网贷需先核账、控催收、避免继续展期");
      if (overdueDays > 0) reason.push(`已逾期约 ${overdueDays} 天`);
      if (client.isCollected === "是") reason.push("已有催收压力");
      return { ...item, score, reason };
    })
    .sort((a, b) => b.score - a.score);
}

function createAllocation(capacity, queue) {
  const totalScore = queue.reduce((sum, item) => sum + item.score, 0) || 1;
  return queue.map((item) => ({
    ...item,
    monthlyPay: Math.min(item.amount, (capacity * item.score) / totalScore),
    percent: (item.score / totalScore) * 100
  }));
}

function listItems(items) {
  return `<ul>${items.map((item) => `<li>${escapeHtml(item)}</li>`).join("")}</ul>`;
}

function renderPlan(client, rules = defaultRules) {
  const declaredTotal = parseMoney(client.totalDebt);
  const debts = [
    { key: "bankLoan", label: "银行贷款", amount: parseMoney(client.bankLoan) },
    { key: "creditCard", label: "信用卡", amount: parseMoney(client.creditCard) },
    { key: "onlineLoan", label: "网贷", amount: parseMoney(client.onlineLoan) }
  ];
  const calculatedTotal = debts.reduce((sum, item) => sum + item.amount, 0);
  const totalDebt = declaredTotal || calculatedTotal;
  const income = parseMoney(client.monthlyIncome);
  const overdueDays = parseOverdueDays(client.overdue);
  const debtIncomeRatio = income > 0 ? totalDebt / income : 0;
  const incomeRatio =
    client.hasMortgage === "是" ? rules.capacity.mortgageIncomeRatio : rules.capacity.baseIncomeRatio;
  const capacity = Math.max(0, income * incomeRatio - rules.capacity.minimumLivingCost * 0.15);
  const riskScore = calculateRisk(client, rules, overdueDays, debtIncomeRatio);
  const risk = riskLabel(riskScore);
  const queue = buildPriorityQueue(client, rules, debts, overdueDays);
  const allocation = createAllocation(capacity, queue);
  const name = client.name || "客户";
  const mismatch =
    declaredTotal && calculatedTotal && Math.abs(declaredTotal - calculatedTotal) > declaredTotal * 0.08
      ? `申报总负债为 ${formatMoney(declaredTotal)}，分项合计为 ${formatMoney(calculatedTotal)}，建议先核对口径。`
      : "总负债与分项数据基本一致，可进入方案沟通。";
  const biggestCreditor = client.largestBank || client.largestPlatform || "最大债权方";
  const mainConcern = client.biggestConcern || "征信、催收和现金流失控";

  planOutput.innerHTML = `
    <div class="summary-strip">
      <div class="metric"><span>总负债</span><strong>${formatMoney(totalDebt)}</strong></div>
      <div class="metric"><span>月还款池</span><strong>${formatMoney(capacity)}</strong></div>
      <div class="metric"><span>风险等级</span><strong><span class="badge ${risk.className}">${risk.text} ${riskScore}/100</span></strong></div>
    </div>

    <section class="plan-section">
      <h3>客户判断</h3>
      ${listItems([
        `${name} 当前债务收入比约为 ${debtIncomeRatio.toFixed(1)} 倍，需要先稳住基本生活和刚性支出。`,
        mismatch,
        client.hasMortgage === "是" ? "有房贷，方案优先保护房贷连续还款和资产安全。" : "无房贷，可把更多现金流用于清理高压账户。",
        client.isCollected === "是" ? "已被催收，沟通记录和证据留存要同步做。" : "暂未被催收，适合在逾期扩大前主动协商。",
        `客户最担心：${mainConcern}。`
      ])}
    </section>

    <section class="plan-section">
      <h3>优先处理顺序</h3>
      <ol>
        ${queue
          .map(
            (item) =>
              `<li><strong>${escapeHtml(item.label)}：${formatMoney(item.amount)}</strong>。${escapeHtml(item.reason.join("；"))}。</li>`
          )
          .join("")}
      </ol>
    </section>

    <section class="plan-section">
      <h3>月度资金分配</h3>
      <div class="allocation">
        ${allocation
          .map(
            (item) => `
              <div class="allocation-row">
                <span>${escapeHtml(item.label)}</span>
                <div class="bar-track"><div class="bar-fill" style="width:${Math.max(8, item.percent)}%"></div></div>
                <strong>${formatMoney(item.monthlyPay)}</strong>
              </div>
            `
          )
          .join("")}
      </div>
    </section>

    <section class="plan-section">
      <h3>90 天动作</h3>
      ${rules.phasePlan.map((phase) => `<h4>${escapeHtml(phase.title)}</h4>${listItems(phase.actions)}`).join("")}
    </section>

    <section class="plan-section">
      <h3>协商口径</h3>
      <p class="script-box">${escapeHtml(name)} 目前收入约 ${formatMoney(income)}，总负债 ${formatMoney(totalDebt)}。建议先与 ${escapeHtml(biggestCreditor)} 主动沟通，目标不是逃避债务，而是确认本金、降低短期月供压力，并形成能连续执行的方案。</p>
    </section>

    <section class="plan-section">
      <h3>建议动作</h3>
      ${listItems([
        ...rules.negotiation.bankLoan,
        ...rules.negotiation.creditCard,
        ...rules.negotiation.onlineLoan
      ])}
    </section>
  `;
  emptyState.hidden = true;
  planOutput.hidden = false;
}

function generatePlanForSelectedClient() {
  const client = getClientById(selectedClientId);
  if (!client) return;
  renderPlan(client);
}

function copyPlanText() {
  if (planOutput.hidden) return;
  navigator.clipboard.writeText(planOutput.innerText.trim());
  copyPlanBtn.textContent = "已复制";
  window.setTimeout(() => {
    copyPlanBtn.textContent = "复制方案";
  }, 1400);
}

populatePaymentDateSelectors();
setPaymentDateSelects();

paymentYear.addEventListener("change", updatePaymentDays);
paymentMonth.addEventListener("change", updatePaymentDays);
paymentDay.addEventListener("change", syncPaymentDateField);

saveInfoBtn.addEventListener("click", saveCurrentClient);
document.querySelector("#generatePlanBtn").addEventListener("click", generatePlanForSelectedClient);
document.querySelector("#editClientBtn").addEventListener("click", editSelectedClient);
document.querySelector("#deleteClientBtn").addEventListener("click", deleteSelectedClient);
document.querySelector("#backToLibraryBtn").addEventListener("click", () => setActiveView("library"));
copyPlanBtn.addEventListener("click", copyPlanText);

clientSearchInput.addEventListener("input", renderClientLibrary);

clientList.addEventListener("click", (event) => {
  const card = event.target.closest(".client-card");
  if (!card) return;
  showClientDetail(card.dataset.clientId);
});

navButtons.forEach((button) => {
  button.addEventListener("click", () => {
    const targetView = button.dataset.targetView;
    if (targetView === "library") renderClientLibrary();
    setActiveView(targetView);
  });
});

renderClientLibrary();
