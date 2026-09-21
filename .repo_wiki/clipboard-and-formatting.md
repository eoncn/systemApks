# 文本格式化与剪贴板导出

> 介绍 ClipTextFormatter 的纯 Java 格式化设计、高效字符串构建、剪贴板交互以及对应的单元测试覆盖。

## 概述

系统应用清单的一大核心功能是方便用户、测试及审计人员将提取到的包名与名称导出为清晰易读的纯文本格式，用于粘贴至文本编辑器、微信聊天窗口或导入 Excel / 飞书多维表格中。

为了保证该逻辑的纯粹性与高可测性，文本拼装逻辑被独立封装在纯 Java 静态工具类 `ClipTextFormatter` 中，完全不依赖 Android Framework（如 Context、Resources 等），从而能够在开发机 JVM 上进行毫秒级单元测试。

## 关键文件

| 文件路径 | 说明 |
|---------|------|
| `app/src/main/java/com/eoncn/systemapks/ClipTextFormatter.java` | 文本格式化工具类，负责拼装清单行与尾部统计摘要 |
| `app/src/test/java/com/eoncn/systemapks/ClipTextFormatterTest.java` | 针对格式化输出与 AppItem 对象的完整 JUnit 4 单元测试 |
| `app/src/main/java/com/eoncn/systemapks/MainActivity.java` | 调用格式化并与系统 `ClipboardManager` 进行交互 |

## 核心概念

### 格式规范
导出的文本严格遵循以下规范：
1. 每个应用占一行，格式为：`<packageName> | <label>`。
2. 应用列表结束后空一行。
3. 尾部追加根据当前视图统计的概要文本（如 `全部应用共 90 个（可启动 16 个）` 或 `可启动应用共 16 个（总应用 90 个）`）。

示例输出：
```text
com.android.calculator2 | 计算器
com.android.calendar | 日历
com.android.camera2 | 相机
com.android.settings | 设置
com.tencent.mm | 微信

全部应用共 5 个（可启动 5 个）
```

### 内存与性能优化
在拥有数百个应用的设备上，频繁的字符串拼接会造成频繁的 GC 分配。
`ClipTextFormatter.format()` 预估平均每行长度约为 48 个字符，预先初始化 `StringBuilder` 容量：
```java
@NonNull
static String format(@NonNull List<AppItem> items, @NonNull String summary) {
    StringBuilder builder = new StringBuilder(items.size() * 48);
    for (AppItem item : items) {
        builder.append(item.packageName).append(" | ").append(item.label).append('\n');
    }
    builder.append('\n').append(summary);
    return builder.toString();
}
```
通过容量预分配，有效避免了底层 `char[]` 数组在扩容时的反复拷贝与重分配。

## 剪贴板交互逻辑

`MainActivity.copyToClipboard()` 包含完善的防御性代码：
1. **空数据保护**：若列表无数据，弹出 `toast_nothing` 提示并直接返回。
2. **系统服务判空**：若获取 `ClipboardManager` 为 null，捕获并提示失败。
3. **安全注入**：将内容封装为 `ClipData.newPlainText()`，并包裹 try-catch 捕获系统底层可能的 IPC 异常。
4. **Android 13+ 兼容**：检测到 API >= 33 时自动抑制 Toast 提示，依赖系统原生的剪贴板浮层。

## 单元测试保障

`ClipTextFormatterTest` 在无需启动 Android 模拟器或连接真实设备的前提下覆盖以下场景：
- `format_multipleItems_producesExpectedLayout`: 验证多条数据下的换行符与竖线分隔符正确性。
- `format_launchableOnlySummary_producesExpectedLayout`: 验证仅启动模式下的摘要内容追加。
- `format_emptyList_producesOnlySummary`: 验证空列表边界条件（仅输出换行与摘要）。
- `appItem_fieldsPreserved`: 验证 `AppItem` 数据对象的字段完整性与默认值。

---

**相关页面**: [index.md](index.md) | [overview.md](overview.md) | [ui-layer.md](ui-layer.md) | [testing.md](testing.md)
