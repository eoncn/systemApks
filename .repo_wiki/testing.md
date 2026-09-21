# 测试体系与质量保证

> 介绍基于 JUnit 4 的本地 JVM 单元测试策略、测试隔离设计与 Android Lint 静态代码检查。

## 概述

systemApks 遵循高可测试性的工程设计。在移动端开发中，依赖设备或模拟器的仪器化测试（Instrumentation Test）通常启动缓慢且容易受到环境波动影响。

本项目通过将核心字符串格式化与数据模型从 Android Framework 中彻底抽离，使得关键业务逻辑能够在纯开发机 JVM 环境下进行秒级单元测试。同时结合 Android Lint 执行静态代码扫描，确保代码质量与平台合规性。

## 关键文件

| 文件路径 | 说明 |
|---------|------|
| `app/src/test/java/com/eoncn/systemapks/ClipTextFormatterTest.java` | 格式化逻辑与数据模型的单元测试集 |
| `app/src/main/java/com/eoncn/systemapks/ClipTextFormatter.java` | 被测核心工具类 |
| `app/src/main/java/com/eoncn/systemapks/AppItem.java` | 被测数据对象 |

## 核心概念

### 框架无关的测试隔离
在设计 `ClipTextFormatter` 时，故意不引入任何 Android SDK 专有类（如 `Context`、`Resources` 或 `TextUtils`），仅使用标准 Java `StringBuilder`、`List` 与字符串操作。
这一设计带来的优势包括：
- **无需 Robolectric 或设备**：纯 JVM 执行，单次完整测试耗时在 1 秒以内。
- **高确定性**：消除了 Android 运行时状态变更引入的不稳定性。

### 测试用例设计

`ClipTextFormatterTest` 包含针对各种边界条件的完备断言：
1. **多条目正常排版** (`format_multipleItems_producesExpectedLayout`)：
   验证每个应用按 `<packageName> | <label>` 排列，并在列表与摘要之间保留空行。
2. **过滤模式摘要** (`format_launchableOnlySummary_producesExpectedLayout`)：
   验证在仅可启动模式下，摘要语句正确嵌入并格式化。
3. **空列表边界测试** (`format_emptyList_producesOnlySummary`)：
   当应用列表为空时，确保不会发生索引越界，并正确输出换行与摘要。
4. **模型状态保持测试** (`appItem_fieldsPreserved`)：
   验证 `AppItem` 的两个构造函数均能正确赋值 `packageName`、`label` 与 `launchable` 标记。

## 常用测试与质量命令

```bash
# 执行全部单元测试
./gradlew testDebugUnitTest

# 执行指定测试类
./gradlew :app:testDebugUnitTest --tests "com.eoncn.systemapks.ClipTextFormatterTest"

# 执行指定测试方法
./gradlew :app:testDebugUnitTest --tests "com.eoncn.systemapks.ClipTextFormatterTest.format_multipleItems_producesExpectedLayout"

# 执行 Android Lint 代码质量检查
./gradlew lintDebug
```

## 测试报告输出

- **单元测试 HTML 报告**：`app/build/reports/tests/testDebugUnitTest/index.html`
- **Lint 扫描报告**：`app/build/reports/lint-results-debug.html`

## 注意事项

- **编写新逻辑时的要求**：新增的算法、字符串转换或数据计算工具类，应尽量保持纯 Java/Kotlin 实现，并配齐对应的 JVM 单元测试。
- **Lint 警告处理**：若代码涉及权限、已弃用 API 或多语言缺失，应及时修复或通过 `@SuppressLint` / `tools:ignore` 显式注明理由。

---

**相关页面**: [index.md](index.md) | [overview.md](overview.md) | [clipboard-and-formatting.md](clipboard-and-formatting.md) | [build-and-ci-cd.md](build-and-ci-cd.md)
