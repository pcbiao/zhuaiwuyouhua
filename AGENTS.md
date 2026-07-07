# 债务优化项目说明

## 当前项目

- 当前只保留原生 Android App。
- 原生项目目录：`安卓原生/`
- 不再维护网页版文件、GitHub Pages 首页、单文件 HTML、`index.html`、`app.js`、`styles.css`。
- 不要修改或恢复任何网页版文件。

## 产品定位

轻债助手是一个本地客户债务档案工具，用于：

- 新建、编辑、删除客户档案
- 录入客户基础信息
- 录入多笔债务明细
- 按提醒/逾期查看客户
- 导入、导出 JSON 数据
- 在本机保存数据

当前不做账号登录、云同步、后端数据库。

## 版本规则

- 大改动：第一位增加，例如 `1.2.2` -> `2.0.0`
- 中改动：第二位增加，例如 `1.2.2` -> `1.3.0`
- 小改动：第三位增加，例如 `1.2.2` -> `1.2.3`

版本号需要同步更新：

- `安卓原生/app/build.gradle` 的 `versionName`
- `安卓原生/app/build.gradle` 的 `versionCode`
- App 页脚显示的版本号
- JSON 导出里的版本号
- `安卓原生/README.md`
- APK 文件名，格式为 `App名称 vX.Y.Z.apk`，例如 `轻债助手 v1.2.2.apk`

## 打包和模拟器

- 启动模拟器时固定使用：`samsung_s24ultra_virtual`
- 不要每次重新查找 AVD。
- 用户要求“打包”时，打包完成后继续安装并启动到模拟器，方便用户审核。

模拟器启动命令：

```bash
"$HOME/Library/Android/sdk/emulator/emulator" -avd samsung_s24ultra_virtual
```

打包命令：

```bash
cd /Users/pcbiao/Documents/Codex/债务优化/安卓原生
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleDebug
```

安装并启动命令：

```bash
"$HOME/Library/Android/sdk/platform-tools/adb" install -r "安卓原生/app/build/outputs/apk/debug/轻债助手 v1.2.3.apk"
"$HOME/Library/Android/sdk/platform-tools/adb" shell am start -S -n com.pcbiao.debtarchive/.MainActivity
```

## 安装包规则

- 标准安装包只保留一个，文件名格式固定为：`App名称 vX.Y.Z.apk`
- 当前 App 标准安装包路径示例：`安卓原生/app/build/outputs/apk/debug/轻债助手 v1.2.3.apk`
- Gradle 默认生成的 `app-debug.apk` 需要改名为标准安装包。
- 不要保留多个重复 APK。
- `app/build/` 是构建输出，不提交到 GitHub。

## 沟通偏好

- 默认简洁中文回复。
- 回答优先短、准、直接。
- 除非明确要求“详细解释/展开/分析”，否则每次回复控制在 5 行以内。
- 讨论产品/UI/交互方案时，直接说结论和最多 3 个理由。
- 当用户说“画给我看”时，优先生成接近手机 App 界面的视觉稿，不要只用文字框图。

## 开发注意事项

- 手动改文件使用 `apply_patch`。
- 不要随意修改 `.git`、`.env`、构建缓存。
- 不要提交 `.gradle/`、`local.properties`、`app/build/`。
- 可以读取现有代码风格后再改，优先保持当前原生 App 的设计语言。

## 常用定位

- 主文件：`安卓原生/app/src/main/java/com/pcbiao/debtarchive/MainActivity.java`
- 构建文件：`安卓原生/app/build.gradle`
- 图标资源：`安卓原生/app/src/main/res/drawable-nodpi/ic_launcher_image.png`
- Android 清单：`安卓原生/app/src/main/AndroidManifest.xml`
