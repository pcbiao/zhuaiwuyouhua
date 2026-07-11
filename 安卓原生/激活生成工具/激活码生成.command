#!/bin/zsh
cd "$(dirname "$0")"
echo "请输入客户设备码，例如 QZ-9CD8-3773："
read device_code
node generate-license.js "$device_code"
echo ""
echo "按回车关闭窗口"
read
