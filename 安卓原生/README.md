# 债务客户档案 Android 原生版

这是从 `债务客户档案 v 1.0.6.html` 搬出的原生 Android 版本。

## 打开方式

1. 用 Android Studio 打开 `android-native` 文件夹。
2. 等待 Gradle Sync 完成。
3. 运行 `app` 到手机或模拟器。

## 已搬迁功能

- 客户档案列表、搜索、提醒筛选
- 新建、编辑、删除客户
- 债务明细录入、状态、到期日期
- 跟进记录新增
- 客户详情和债务优化方案
- 本机 JSON 存储
- JSON 导入/导出
- 离线设备绑定激活

## 激活码生成

- 工具目录：`激活生成工具/`
- 命令行：`node 激活生成工具/generate-license.js QZ-9CD8-3773`
- macOS 可双击运行：`激活生成工具/激活码生成.command`

## 版本

- Android `versionName`: `1.4.1`
- Android `versionCode`: `141`
