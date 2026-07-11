const crypto = require("crypto");

const SECRET = "QZ_DEBT_ARCHIVE_2026_DEVICE_LICENSE";

function sha256(value) {
  return crypto.createHash("sha256").update(value, "utf8").digest("hex").toUpperCase();
}

function normalizeCode(value) {
  return String(value || "").toUpperCase().replace(/[^A-Z0-9]/g, "");
}

function activationCodeFor(deviceCode) {
  const hash = sha256(`${SECRET}|${normalizeCode(deviceCode)}`);
  return `QZ-${hash.slice(0, 4)}-${hash.slice(4, 8)}-${hash.slice(8, 12)}`;
}

const input = process.argv[2];

if (!input) {
  console.log("用法：node generate-license.js 设备码");
  console.log("示例：node generate-license.js QZ-9CD8-3773");
  process.exit(1);
}

console.log(`设备码：${input}`);
console.log(`激活码：${activationCodeFor(input)}`);
