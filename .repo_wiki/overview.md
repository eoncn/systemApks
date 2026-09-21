# 项目概述与整体架构

> systemApks (应用清单) 是一款干净、轻量且高性能的 Android 工具应用，用于快速提取并导出本机已安装的所有应用程序包名及其在当前系统语言下对应的应用名称。

## 概述

systemApks 旨在为开发者、测试人员及系统审计人员提供一个极简、无广告且无需侵入性运行时权限的应用包检查工具。应用突破了 Android 11+ 的软件包可见性限制，完整获取系统中包括系统框架、供应商组件（`/vendor`）、Content Provider 以及第三方用户应用在内的全部安装包。

应用在架构上遵循 Google 推荐的现代 Android 架构指南，采用 **Single Activity + MVVM** 架构模式。UI 层与数据层完全解耦，所有耗时的系统包检索操作均在后台单线程池中异步完成，并通过 `LiveData` 进行响应式状态分发，确保主线程从不卡顿，且在横竖屏切换等配置变更时完整保留数据与界面状态。

项目没有引入沉重的第三方依赖（如 Dagger/Hilt、RxJava、Retrofit 或 Room），仅依赖 AndroidX 官方的核心基础库（AppCompat, RecyclerView, Lifecycle ViewModel & LiveData），编译产物极为轻巧，启动极速。

## 关键文件

| 文件路径 | 说明 |
|---------|------|
| `app/src/main/java/com/eoncn/systemapks/MainActivity.java` | 单 Activity 界面入口，负责 UI 组件绑定、事件监听与系统剪贴板交互 |
| `app/src/main/java/com/eoncn/systemapks/AppListViewModel.java` | 核心 ViewModel，负责后台异步查询 PackageManager、本地化应用名解析与数据过滤 |
| `app/src/main/java/com/eoncn/systemapks/AppItem.java` | 不可变应用条目模型，承载包名、应用标题与桌面可启动标记 |
| `app/src/main/java/com/eoncn/systemapks/AppListAdapter.java` | RecyclerView 列表适配器与 ViewHolder |
| `app/src/main/java/com/eoncn/systemapks/ClipTextFormatter.java` | 纯 Java 剪贴板文本格式化工具类 |
| `app/src/main/AndroidManifest.xml` | 清单配置，声明 `QUERY_ALL_PACKAGES` 权限与主 Activity |
| `app/build.gradle` | 应用构建配置，设定 SDK 36、minSdk 24 与 Java 8 编译目标 |

## 核心概念

### Single Activity MVVM
整个应用由单一 Activity（`MainActivity`）承载视图呈现，所有界面状态由 `AppListViewModel` 持有。`MainActivity` 仅作为被动的观察者，通过声明式的 `render()` 方法根据 `ViewModel` 中的 LiveData 状态统一渲染界面组件（按钮启用、进度条显隐、文本统计展示）。

### 双视图瞬时内存切换
应用定义了 `FilterMode` 枚举（`ALL` 与 `LAUNCHABLE_ONLY`）。后台查询时一次性拉取并解析所有安装包缓存于内存列表中，当用户在单选按钮组中切换视图时，直接在内存中过滤，无需重新向 Android 系统发起耗时的 IPC 跨进程查询。

### 零弹窗无感权限
利用 Android 系统 Normal 级别的 `android.permission.QUERY_ALL_PACKAGES` 权限，系统在安装时自动授予，无需像危险权限那样在运行时弹出权限申请对话框，为用户提供即开即用的体验。

## 工作流程

```
+------------------+         点击 "加载"          +-----------------------+
|   MainActivity   | ------------------------> |   AppListViewModel    |
+------------------+                           +-----------------------+
         ^                                                 |
         | 观察 LiveData                                   | Executor (单线程池)
         | (apps, loading, error, mode)                    v
         |                                     +-----------------------+
         +------------------------------------ |    PackageManager     |
                                               | (查询所有包与Launcher)  |
                                               +-----------------------+
```

1. 用户点击界面上的「加载」按钮。
2. `MainActivity` 调用 `viewModel.load()`。
3. `AppListViewModel` 将 `loading` 置为 `true`，并通过单线程后台 `Executor` 执行查询。
4. 后台线程查询 `PackageManager.getInstalledApplications(0)` 与 `queryIntentActivities(Intent.CATEGORY_LAUNCHER)`。
5. 逐个解析包的本地化名称并以系统当前语言的 `Collator` 进行字母排序。
6. 切回主线程更新 `allApps`，根据当前选中的 `FilterMode` 派发数据至 `apps` LiveData。
7. `MainActivity` 收到新列表后更新 `AppListAdapter` 并调用 `render()` 刷新状态栏与按钮。

## 配置

| 配置项 | 参数值 | 说明 |
|-------|-------|------|
| `minSdk` | 24 | 最低支持 Android 7.0 (Nougat) |
| `targetSdk` / `compileSdk` | 36 | 适配 Android 16 |
| `sourceCompatibility` | Java 8 (1.8) | 编译字节码版本（Class 52） |
| `Gradle Toolchain` | JDK 17 | Gradle 8.x 运行时 JDK |

## 注意事项

- **主线程安全**：`PackageManager` 的查询属于 IPC 跨进程调用，安装应用数量多时（数百个包）可能耗时数百毫秒，绝不可在主线程执行。
- **状态保留**：由于使用 `ViewModel`，屏幕旋转重建 Activity 时不会丢失已加载的应用数据，也不需要重新发起查询。

---

**相关页面**: [index.md](index.md) | [viewmodel-and-data.md](viewmodel-and-data.md) | [ui-layer.md](ui-layer.md) | [permissions-and-compatibility.md](permissions-and-compatibility.md) | [build-and-ci-cd.md](build-and-ci-cd.md)
