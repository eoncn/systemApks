# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Environment & Tooling

- **JDK**: JDK 17 (required for Gradle 8.13 and AGP 8.11.1)
- **Android SDK**: `compileSdk 36`, `targetSdk 36`, `minSdk 24` (Android 7.0+)
- **Bytecode**: Java 8 compatibility (`sourceCompatibility` & `targetCompatibility` = `VERSION_1_8`)

## Common Commands

### Build
```bash
# Build Debug APK (output: app/build/outputs/apk/debug/app-debug.apk)
./gradlew assembleDebug

# Build Release APK (output: app/build/outputs/apk/release/app-release-unsigned.apk)
./gradlew assembleRelease

# Clean build artifacts
./gradlew clean
```

### Test & Lint
```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run a single test class
./gradlew :app:testDebugUnitTest --tests "com.eoncn.systemapks.ClipTextFormatterTest"

# Run a specific unit test method
./gradlew :app:testDebugUnitTest --tests "com.eoncn.systemapks.ClipTextFormatterTest.format_multipleItems_producesExpectedLayout"

# Run Android Lint checks
./gradlew lintDebug
```

## Architecture & Code Structure

The project is a lightweight Android utility app following a Single Activity MVVM architecture without third-party DI or network libraries.

### Core Data Flow
1. **Query & Load**: `MainActivity` triggers `AppListViewModel.load()`. A dedicated single-thread `ExecutorService` queries `PackageManager.getInstalledApplications(0)` alongside `queryIntentActivities` with `Intent.CATEGORY_LAUNCHER` to determine launchable status.
2. **Label Resolution & Sorting**: `AppListViewModel.resolveLabel()` dynamically retrieves localized labels using `ApplicationInfo.loadLabel()`, catching exceptions (e.g. during package uninstallation or corruption) to fallback to `packageName`. Results are sorted using `Collator.getInstance(currentLocale())` by label then package name.
3. **Filtering**: `AppListViewModel` holds the full dataset in-memory (`allApps`) and applies `FilterMode` (`ALL` vs `LAUNCHABLE_ONLY`) instantly without re-querying the system.
4. **UI State & Rendering**: `MainActivity` observes `LiveData` streams (`apps`, `loading`, `error`, `filterMode`) and declaratively updates the UI via `render()`.
5. **Clipboard Export**: `ClipTextFormatter` formats items into `<packageName> | <label>` rows with a localized summary line. On Android 13+ (`API >= 33`), the custom toast is suppressed because the system UI displays its own clipboard overlay.

### Package Visibility
`AndroidManifest.xml` declares `android.permission.QUERY_ALL_PACKAGES` to bypass Android 11+ (`targetSdk 30+`) package visibility limitations. It is a normal-level permission granted automatically upon installation.

## Repo Wiki — Mandatory Knowledge Base

项目维护了一个结构化的知识库 wiki（`.repo_wiki/`），持久、可积累、LLM 维护的知识体系。Wiki 包含 8 个互链的 markdown 页面，覆盖系统范式（paradigm）、整体架构（overview）、数据层与异步调度（viewmodel-and-data）、UI 声明式渲染与交互（ui-layer）、文本排版与剪贴板导出（clipboard-and-formatting）、权限突破与多版本兼容（permissions-and-compatibility）、构建与持续集成自动化（build-and-ci-cd）、本地单元测试与代码质量（testing）。

**⚠️ Wiki-First 规则（强制）**：在修改任何代码之前，必须先阅读相关 wiki 页面获取结构化概览和跨文件约束，然后再深入源码。禁止直接全项目搜索跳过 wiki。Wiki 页面间通过底部”相关页面”链接互连。

**为什么必须先读 wiki**：wiki 提供的是跨文件的结构性理解（架构、数据流、依赖关系、约束条件），Grep/Glob 只能找到局部代码片段。跳过 wiki 会导致：遗漏约束、不知下游影响、重复已有的知识发现。

**入口**: `.repo_wiki/index.md` — 包含所有页面的导航索引和一行摘要。

### 文件路径 → Wiki 页面映射

当任务涉及以下路径时，**必须先读取对应的 wiki 页面**，再动手修改代码：

| 涉及的文件/目录 | 必读 Wiki 页面 |
|---|---|
| `app/src/main/java/com/eoncn/systemapks/MainActivity.java` | [.repo_wiki/ui-layer.md](.repo_wiki/ui-layer.md) |
| `app/src/main/java/com/eoncn/systemapks/AppListAdapter.java` | [.repo_wiki/ui-layer.md](.repo_wiki/ui-layer.md) |
| `app/src/main/res/layout/**` | [.repo_wiki/ui-layer.md](.repo_wiki/ui-layer.md) |
| `app/src/main/res/values/**` | [.repo_wiki/ui-layer.md](.repo_wiki/ui-layer.md) |
| `app/src/main/java/com/eoncn/systemapks/AppListViewModel.java` | [.repo_wiki/viewmodel-and-data.md](.repo_wiki/viewmodel-and-data.md) |
| `app/src/main/java/com/eoncn/systemapks/AppItem.java` | [.repo_wiki/viewmodel-and-data.md](.repo_wiki/viewmodel-and-data.md) |
| `app/src/main/java/com/eoncn/systemapks/ClipTextFormatter.java` | [.repo_wiki/clipboard-and-formatting.md](.repo_wiki/clipboard-and-formatting.md) |
| `app/src/main/AndroidManifest.xml` | [.repo_wiki/permissions-and-compatibility.md](.repo_wiki/permissions-and-compatibility.md) |
| `build.gradle`, `app/build.gradle`, `settings.gradle`, `gradle/**` | [.repo_wiki/build-and-ci-cd.md](.repo_wiki/build-and-ci-cd.md) |
| `.github/workflows/**` | [.repo_wiki/build-and-ci-cd.md](.repo_wiki/build-and-ci-cd.md) |
| `app/src/test/**` | [.repo_wiki/testing.md](.repo_wiki/testing.md) |
| 跨模块架构重构、全局设计规范 | [.repo_wiki/overview.md](.repo_wiki/overview.md) |
| Wiki 维护、规范与增量摄取 | [.repo_wiki/paradigm.md](.repo_wiki/paradigm.md) |

**不在映射表中的路径**：如果修改的文件不在映射表中，先检查 `.repo_wiki/index.md` 确认是否有相关页面。如果确定无相关 wiki 页面，方可直接搜索源码。

### Wiki 使用流程

1. **定位 wiki 页面**：根据要修改的文件路径，在上方映射表中找到必读的 wiki 页面
2. **读取 wiki**：用 Read 工具读取对应的 wiki 页面，获取结构化概览、关键约束、跨文件依赖
3. **跟随 wiki 中的源码链接**：wiki 页面会引用具体源文件路径（如 `app/src/main/java/com/eoncn/systemapks/AppListViewModel.java`），按这些链接深入源码
4. **跟随 wiki 底部的”相关页面”链接**：如果修改涉及多个子系统，按链接读取相关 wiki 页面
5. **修改代码**：在 wiki 提供的结构性理解指导下，进行精确的手术式修改

## Agent skills

### Issue tracker

GitHub Issues using the `gh` CLI. See `docs/agents/issue-tracker.md`.

### Domain docs

Single-context (`CONTEXT.md` + `docs/adr/` at repo root). See `docs/agents/domain.md`.


