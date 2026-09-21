# 界面层与交互设计

> 详解 MainActivity、AppListAdapter、视图布局结构与基于 LiveData 的声明式渲染机制。

## 概述

systemApks 的用户界面遵循极简与高效原则。主界面由操作栏、视图筛选单选组、状态提示栏及核心应用列表构成。

界面层由 `MainActivity` 作为单一宿主，搭配轻量级的 `AppListAdapter` 负责列表展示。UI 不直接维护业务状态，而是通过观察 `AppListViewModel` 的多个 `LiveData` 数据流，在统一的 `render()` 方法中进行声明式 UI 同步。

## 关键文件

| 文件路径 | 说明 |
|---------|------|
| `app/src/main/java/com/eoncn/systemapks/MainActivity.java` | 单 Activity 交互中心，控制视图生命周期与用户事件 |
| `app/src/main/java/com/eoncn/systemapks/AppListAdapter.java` | RecyclerView 列表适配器，绑定每行应用条目 |
| `app/src/main/res/layout/activity_main.xml` | 主界面布局：顶部操作栏、单选过滤组、状态栏及 RecyclerView |
| `app/src/main/res/layout/item_app.xml` | 列表单项布局：展示应用名称（`tv_label`）与包名（`tv_package`） |
| `app/src/main/res/values/strings.xml` | 外部化界面字符串与提示语模板 |

## 核心概念

### 统一声明式渲染 (`render()`)
`MainActivity` 摒弃了分散在各个回调中的细碎 UI 状态更新逻辑，将所有视图组件的状态聚合在一个中央的 `render()` 方法中：
- **进度指示**：根据 `viewModel.isLoading()` 显示或隐藏 `ProgressBar`。
- **按钮状态**：在加载中禁用「加载」按钮；当列表有数据且非加载中时启用「复制」按钮。
- **筛选按钮标签**：若已拉取到数据，动态更新单选按钮文字为包含数量的文本（如 `全部应用 (90)` / `仅可启动 (16)`）。
- **状态提示语**：依据是否正在加载、是否有错误、当前过滤模式以及应用计数，动态选定相应的国际化字符串模板。

```java
private void render() {
    boolean loading = Boolean.TRUE.equals(viewModel.isLoading().getValue());
    List<AppItem> items = viewModel.getApps().getValue();
    String error = viewModel.getError().getValue();
    int count = items == null ? 0 : items.size();
    int totalCount = viewModel.getTotalCount();
    int launchableCount = viewModel.getLaunchableCount();
    AppListViewModel.FilterMode mode = viewModel.getFilterMode().getValue();

    progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    loadButton.setEnabled(!loading);
    copyButton.setEnabled(!loading && count > 0);

    // 动态渲染单选标签与状态文本...
}
```

### AppListAdapter 极简适配器
`AppListAdapter` 采用标准的 `RecyclerView.Adapter<AppViewHolder>` 实现：
- `submit(@NonNull List<AppItem> newItems)`：清空旧列表，加入新列表并触发 `notifyDataSetChanged()`。
- `AppViewHolder`：持有 `tv_label`（标题）与 `tv_package`（包名），通过 `bind(item)` 直接赋值，无额外视图计算。

### Android 13+ 剪贴板弹窗适配
从 Android 13（API 33, Tiramisu）开始，系统会在内容写入剪贴板后自动在屏幕左下角弹出系统级浮层（含内容预览与快速编辑入口）。
为了避免双重弹窗打扰用户，`MainActivity.copyToClipboard()` 进行了系统版本判断：
```java
if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
    Toast.makeText(this, getString(R.string.toast_copied, items.size()), Toast.LENGTH_SHORT).show();
}
```
仅在 API < 33 时弹出传统的 Toast 提示。

## 布局层级结构

```
ConstraintLayout / LinearLayout (activity_main.xml)
├── Top Action Bar (Horizontal LinearLayout)
│   ├── Button (btn_load: "加载")
│   ├── Button (btn_copy: "复制")
│   └── ProgressBar (pb_loading: 水平进度条/环形指示器)
├── RadioGroup (rg_filter: 视图切换)
│   ├── RadioButton (rb_filter_all: "全部应用")
│   └── RadioButton (rb_filter_launchable: "仅可启动")
├── TextView (tv_status: 状态信息与统计)
└── RecyclerView (rv_apps: 应用列表)
    └── item_app.xml (每行包含 tv_label 和 tv_package)
```

## 注意事项

- **状态防抖**：RadioGroup 切换时需判断 `targetId != checkedRadioButtonId`，避免双向监听引发循环触发。
- **空列表保护**：在列表为空时，点击复制会给出 `toast_nothing` 提示，复制按钮默认保持置灰状态。

---

**相关页面**: [index.md](index.md) | [overview.md](overview.md) | [viewmodel-and-data.md](viewmodel-and-data.md) | [clipboard-and-formatting.md](clipboard-and-formatting.md)
