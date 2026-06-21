# 债务优化项目说明

## 当前主文件

- 当前正在开发和测试的单文件版本是：`债务客户档案 v 1.0.6.html`
- 浏览器应打开这个文件继续测试。
- 旧文件 `债务客户档案.html` 已经重命名，不要继续修改旧文件名。
- `index.html`、`app.js`、`styles.css` 是早期拆分版/旧版文件，当前主要迭代以单文件 HTML 为准。

## 产品定位

这是一个手机 App 形式的本地客户债务档案工具，用于：

- 新建客户档案
- 录入客户基础信息
- 录入多笔债务明细
- 保存客户到本机浏览器 `localStorage`
- 在客户档案列表中查询、查看、编辑、删除客户
- 基于客户档案生成债务优化方案

当前不做账号登录、云同步、后端数据库、Excel 导入导出。

## 版本规则

用户要求版本号按以下规则递增：

- 大改动：第一位增加，例如 `1.0.1` -> `2.0.0`
- 中改动：第二位增加，例如 `1.0.1` -> `1.1.0`
- 小改动：第三位增加，例如 `1.0.1` -> `1.0.2`

版本号需要同步更新：

- 文件名，例如 `债务客户档案 v 1.0.1.html`
- `<title>`，例如 `债务客户档案 v 1.0.1`
- 页面顶部标题旁的小版本标记，CSS `.top-title::after`

### 版本更新命名规则

单文件测试版统一使用以下文件名格式：

```text
债务客户档案 v X.Y.Z.html
```

例如：

```text
债务客户档案 v 1.0.1.html
债务客户档案 v 1.0.2.html
债务客户档案 v 1.1.0.html
债务客户档案 v 2.0.0.html
```

每次需要升级版本时：

1. 判断改动级别：大改、中改、小改。
2. 按规则生成新版本号。
3. 将当前单文件重命名为新版本文件名。
4. 同步修改 HTML `<title>`。
5. 同步修改 CSS `.top-title::after` 里的版本号。
6. 后续开发只修改最新版本文件，不要继续改旧版本文件。

注意：

- 文件名中的 `v` 前后保留空格：`债务客户档案 v 1.0.1.html`。
- 版本号采用三段式 `X.Y.Z`，不要再使用 `1.0` 这种两段式。
- 如果用户没有明确要求升级版本，普通小修改可以先不改文件名；当用户要求“给单文件测试/更新版本/重命名版本”时再执行版本命名更新。
- 重命名后，浏览器中旧路径不会自动跳到新文件，需要提醒用户打开最新文件。

## GitHub 发布和首页关系

- GitHub 仓库地址：`https://github.com/pcbiao/zhuaiwuyouhua`
- GitHub Pages 网页地址：`https://pcbiao.github.io/zhuaiwuyouhua/`
- 别人访问 GitHub Pages 网页时，访问的是 GitHub 服务器上的文件，不是访问本机电脑。
- `index.html` 是 GitHub Pages 的固定首页入口文件，需要本地维护并提交到 GitHub，GitHub 不会自动生成。
- 当前 `index.html` 是跳转页，会跳转到当前单文件版本，例如 `债务客户档案 v 1.0.6.html`。
- 如果单文件版本号更新，`index.html` 不会自动跟着更新，必须同步修改跳转地址。
- 用户说“同步首页”时，意思是更新本地 `index.html`，让它指向最新单文件版本；提交并推送后，GitHub Pages 网页才会打开新版。
- 推荐发布流程：先修改和测试最新单文件，再同步 `index.html`，最后提交并推送到 GitHub。

## 当前页面结构

单文件内有 4 个主要页面，通过 `showScreen(name)` 切换：

- `homeScreen`：客户档案列表
- `formScreen`：新建/编辑客户
- `detailScreen`：客户详情
- `planScreen`：方案页面

顶部导航：

- 首页标题为 `客户档案`
- 首页右上角按钮为 `+ 添加`
- 表单页左上角为 `‹ 返回`
- 详情页右上角为 `编辑`
- 方案页右上角为 `复制`

## 数据结构

本地数据保存在 `localStorage`。

当前 key：

```js
debtCustomerArchiveV150
```

旧 key 会通过 `oldKeys` 自动迁移。

客户对象大致结构：

```js
{
  id,
  name,
  phone,
  monthlyIncome,
  hasMortgage,
  biggestConcern,
  debts: [
    {
      id,
      type,
      creditor,
      amount,
      overdue,
      isCollected
    }
  ],
  createdAt,
  updatedAt,
  plan
}
```

## 当前交互逻辑

### 客户档案列表

- 搜索框按姓名过滤。
- 点击客户卡片进入客户详情。
- 客户卡片左滑后显示两个操作：
  - 蓝底 `编辑`
  - 红底 `删除`
- 手机端已补充 `touchstart / touchend`，并使用 `touch-action: pan-y` 保留上下滚动。

### 新建/编辑客户

客户信息字段：

- 姓名
- 联系电话
- 月收入
- 是否有房贷

债务明细：

- 表头行使用浅灰绿底，并比内容行矮。
- 内容行字段：
  - 类型：选择，灰色文字
  - 机构：手动输入，黑色文字
  - 金额：手动输入，黑色文字
  - 逾期：选择，灰色文字
  - 催收：选择，灰色文字
- `债务明细` 标题右侧有 `+ 添加`，用于添加一笔债务。
- 债务行左滑后显示红底 `删除`。

其他信息：

- 只有一个单层文本框。
- placeholder：`请输入客户最担心的问题或影响`
- 禁止手动拖拽调整大小。

### 保存验证

不要使用底部统一错误提示。

当前规则：

- 姓名没填：只让姓名输入框 placeholder `请输入姓名` 变红并闪烁几下。
- 债务机构没填：只让对应机构输入框 placeholder/输入格提示变红并闪烁几下。
- 不要整行变红，不要改变 `姓名：` 标签颜色，不要改变表格整体颜色。

相关函数：

- `clearValidationErrors()`
- `markValidationError(target)`
- `validateArchive(archive)`

## 设计偏好

用户偏好：

- 手机 App 形式
- 简洁、高效、少区块
- 不要多余说明文字
- 不要大面积底色
- 操作按钮要明确但不能笨重
- 表格视觉统一，行高和字体尽量一致
- 手动输入字段黑色，选择字段灰色
- 顶部/标题右侧的添加按钮采用 A 方案：无底色文字按钮 `+ 添加`
- 当用户说“画给我看”时，表示要用绘图/图片方式画出界面效果预览，不要只用文字框图、ASCII 图或思维导图代替；如果是在讨论 UI 方案，应优先生成接近手机 App 界面的视觉稿。

颜色和视觉：

- 主色：`--teal: #08766f`
- 正文字色：`--ink`
- 灰色文字：`--muted`
- 红色删除/错误：`--danger`

## 沟通偏好

- 默认简洁中文回复。
- 讨论产品/UI/交互方案时，直接说结论和理由，不要主动拉代码或展示代码片段。
- 除非用户明确要求“看代码/怎么实现/开始改”，否则先停留在产品讨论层面。
- 能用一句话说清的，不要展开成长篇分析。
- 给方案时优先说推荐方案；备选方案最多 1 个。

## 开发注意事项

- 手动改文件使用 `apply_patch`。
- 当前主文件名带空格，命令中要加引号：

```bash
'债务客户档案 v 1.0.6.html'
```

- 修改后至少运行内嵌脚本检查：

```bash
node -e "const fs=require('fs'); const html=fs.readFileSync('债务客户档案 v 1.0.6.html','utf8'); const scripts=[...html.matchAll(/<script>([\\s\\S]*?)<\\/script>/g)].map(m=>m[1]); new Function(scripts.join('\\n')); console.log('embedded script ok')"
```

- 如果只是 CSS/HTML 小改，也建议跑上面的检查。
- 不要随意修改 `.git`、`.next`、`.env`。
- 当前 git 状态里会看到旧文件删除和新文件未跟踪，这是因为单文件重命名造成的。

## 常用定位

- 页面切换：`showScreen(name)`
- 首页列表：`renderClientList()`
- 客户保存：`saveArchive()`
- 表单验证：`validateArchive(archive)`
- 债务行渲染：`renderDebts()`
- 方案生成：`createPlan(client)`
- 客户滑动操作：`openClientActions()` / `handleClientSwipe()`
- 债务滑动操作：`openDebtActions()` / `handleDebtSwipe()`
