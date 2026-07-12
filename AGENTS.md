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
当前采用离线设备绑定激活：首次启动显示设备码，输入对应激活码后进入 App；覆盖升级不需要重新激活。
激活码生成工具目录：`安卓原生/激活生成工具/`。

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
- 除非用户明确要求“打包”“安装”或“启动模拟器”，否则不要自动打包、安装或启动模拟器，以节约 token 和时间。

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
"$HOME/Library/Android/sdk/platform-tools/adb" install -r "安卓原生/app/build/outputs/apk/debug/轻债助手 v1.5.1.apk"
"$HOME/Library/Android/sdk/platform-tools/adb" shell am start -S -n com.pcbiao.debtarchive/.MainActivity
```

## 安装包规则

- 标准安装包只保留一个，文件名格式固定为：`App名称 vX.Y.Z.apk`
- 当前 App 标准安装包路径示例：`安卓原生/app/build/outputs/apk/debug/轻债助手 v1.5.1.apk`
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
- 新增或调整 UI 时必须优先使用响应式尺寸规则，不要直接写死大宽度；当前按内容宽度分为窄屏 `<390dp` 和标准/宽屏 `>=390dp`。
- `MainActivity.java` 中已有响应式 helper，例如 `isNarrowContent()`、`infoLabelWidth()`、`editLabelWidth()`、`typeLabelWidth()`、`topActionWidth()`。
- 严格限制修改范围：只改用户明确要求的内容；如果发现其它问题，先说明并等待用户确认，不要自作主张顺手修改。

## 常用定位

- 主文件：`安卓原生/app/src/main/java/com/pcbiao/debtarchive/MainActivity.java`
- 构建文件：`安卓原生/app/build.gradle`
- 图标资源：`安卓原生/app/src/main/res/drawable-nodpi/ic_launcher_image.png`
- Android 清单：`安卓原生/app/src/main/AndroidManifest.xml`

## 代码结构与维护规则

- 原型或功能很少时，允许使用单文件快速验证；进入持续开发阶段后，新增功能应优先采用模块化结构。
- 单个 Activity 超过约 800 行，或同时承担界面、业务、存储、动画等多种职责时，必须评估并实施合理拆分。
- 按职责拆分页面、业务计算、数据模型、数据存储和动画交互；禁止只按行数机械切割文件。
- 重构必须小步进行，每次拆分后都要编译、运行并验证原有功能，避免一次性大改。
- 字符串常量化必须按语义逐项处理，禁止不加区分地全局查找替换。
- 保持文件职责聚焦，降低后续理解、排错、测试、修改及 AI 上下文和 Token 消耗。

## MainActivity 重构交接上下文

- 当前 `MainActivity.java` 集中了页面构建、业务判断、数据存储、手势交互和导入导出等职责，后续维护成本和误改风险较高。
- 重构目标是保持界面、功能、数据格式和用户操作完全不变，只改善代码结构；不要借重构顺便调整产品功能或视觉设计。
- 不进行一次性整体拆分。先建立可回退基线，再按数据模型、业务计算、存储、页面和交互等职责逐步提取。
- 每次只移动一个边界清晰、依赖较少的职责；每一步都检查差异、编译并运行关键流程，通过后再继续。
- 优先提取纯计算和数据读写逻辑，最后处理与 Activity/View 强耦合的页面和手势代码。
- 必须保持 SharedPreferences 名称、JSON 字段、导入导出格式和已有用户数据兼容；禁止为了命名整洁而改变持久化键值。
