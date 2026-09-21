# systemApks (应用清单)

[![Release](https://img.shields.io/github/v/release/eoncn/systemApks?include_prereleases&color=blue&style=flat-square)](https://github.com/eoncn/systemApks/releases)
[![Build & Release APK](https://github.com/eoncn/systemApks/actions/workflows/release.yml/badge.svg)](https://github.com/eoncn/systemApks/actions/workflows/release.yml)
[![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024%2B)-green?style=flat-square)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-brightgreen?style=flat-square)](https://developer.android.com)
[![License](https://img.shields.io/github/license/eoncn/systemApks?style=flat-square)](LICENSE)

[English](#english) | [中文说明](#中文说明)

---

<a name="english"></a>
## English

### Overview

**systemApks** is a clean, lightweight, and high-performance Android utility app designed to inspect installed applications on the device. It retrieves full package identifiers alongside localized display names matching the device's active system language, supports filtering between all packages and launcher apps, and allows one-click export to the system clipboard for sharing and auditing.

### Key Features

- **Complete Package Inspection**: Queries all installed packages including pre-installed system packages (`/system`, `/vendor`, frameworks, providers) and user-installed third-party apps.
- **Launchable vs. All Filter**: Instant in-memory toggle between **"All Applications"** and **"Launchable Only"** (apps with desktop drawer icons declaring `CATEGORY_LAUNCHER`).
- **Locale-Aware App Names**: Resolves app labels using the active system language (`ApplicationInfo.loadLabel`) with fallback protection for corrupt or uninstalled packages.
- **One-Click Clipboard Export**: Formats current list into clean plain text (`<packageName> | <label>` per line with a summary footer), ready for instant sharing or spreadsheet import.
- **Zero Invasive Permissions**: Utilizes the normal-level `QUERY_ALL_PACKAGES` manifest permission to adhere to Android 11+ package visibility rules without intrusive runtime permission prompts.
- **Modern Architecture**:
  - **Single Activity**: Minimalist UI built with AndroidX AppCompat and RecyclerView.
  - **ViewModel & LiveData**: Background thread data loading, reactive UI updates, and robust state retention across screen orientation changes.
- **Wide OS Compatibility**: Minimum support for **Android 7.0 (API 24)** up to **Android 16 (API 36+)**, compiled with **Java 8 bytecode** compatibility.

### Screenshots

| All Applications View | Launchable Only View |
| :---: | :---: |
| Lists all packages (framework, providers, apps) | Filters down to apps with desktop icons |

### Technical Specifications

| Parameter | Value |
| :--- | :--- |
| **Namespace / Application ID** | `com.eoncn.systemapks` |
| **Minimum SDK** | `API 24` (Android 7.0 Nougat) |
| **Compile & Target SDK** | `API 36` (Android 16) |
| **Build Tools** | `36.0.0` |
| **AGP / Gradle Version** | AGP `8.11.1` / Gradle `8.13` |
| **Bytecode Standard** | `Java 8` byte stream (Class Major Version 52) |
| **Build JDK** | `JDK 17` (temurin) |

### Download & Installation

Pre-built signed release APKs are available directly on GitHub:

1. Navigate to the **[Releases Page](https://github.com/eoncn/systemApks/releases)**.
2. Download the latest `systemApks-vX.X.X.apk`.
3. Open the APK on your Android device to install, or install via ADB:
   ```bash
   adb install -r systemApks-v1.0.0.apk
   ```

### Building from Source

#### Prerequisites
- JDK 17 (OpenJDK or Temurin recommended)
- Android SDK Platform 36 and Build-Tools 36.0.0

#### Build Commands

```bash
# Clone the repository
git clone https://github.com/eoncn/systemApks.git
cd systemApks

# Run unit tests
./gradlew testDebugUnitTest

# Run Android Lint analysis
./gradlew lintDebug

# Build Debug APK
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# Build Release APK
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release-unsigned.apk
```

### Project Architecture

```
systemApks/
├── .github/workflows/
│   └── release.yml            # CI/CD: Automated build, sign, and release
├── app/
│   ├── build.gradle           # SDK 36, minSdk 24, Java 8 bytecode configuration
│   ├── src/
│       ├── main/
│       │   ├── AndroidManifest.xml # QUERY_ALL_PACKAGES permission declaration
│       │   ├── java/com/eoncn/systemapks/
│       │   │   ├── MainActivity.java      # Activity UI binding, clipboard & events
│       │   │   ├── AppListViewModel.java  # Async PackageManager loader & filter logic
│       │   │   ├── AppItem.java           # Data model (package, label, launchable flag)
│       │   │   ├── AppListAdapter.java    # RecyclerView adapter & ViewHolder
│       │   │   └── ClipTextFormatter.java # Clipboard output layout formatter
│       │   └── res/
│       │       ├── layout/activity_main.xml # Toolbar, RadioGroup, RecyclerView
│       │       ├── layout/item_app.xml      # List item layout
│       │       └── values/strings.xml       # Externalized strings
│       └── test/java/com/eoncn/systemapks/
│           └── ClipTextFormatterTest.java   # Unit tests for text formatting
```

### CI/CD Automation

This project includes a fully automated GitHub Actions workflow (`.github/workflows/release.yml`):
- **Triggers**:
  - Pushing git tags matching `v*` (e.g. `v1.0.0`).
  - Manual trigger via the GitHub web UI (`workflow_dispatch`).
- **Automated Digital Signing**: Generates a dedicated release key and applies full **v1 + v2 + v3 APK signature** using Android `apksigner`, ensuring direct installability.
- **Asset Publishing**: Uploads signed APK and SHA-256 checksum to GitHub Releases.

---

<a name="中文说明"></a>
## 中文说明

### 项目简介

**systemApks (应用清单)** 是一款干净、轻量且高效的 Android 工具应用，用于快速提取并导出本机已安装的所有应用程序包名及其在当前系统语言下对应的应用名称。支持全部应用与桌面可启动应用一键瞬时筛选，并提供一键复制至剪贴板功能，便于分享与分析。

### 核心特性

- **全面读取系统包**：完整获取系统中所有安装包，涵盖系统框架包、Content Provider、后台服务以及用户安装的第三方应用。
- **全部 / 可启动一键切换**：内置内存级高速筛选器，支持在**「全部应用」**（含无桌面图标的后台组件）与**「仅可启动」**（桌面应用抽屉中可点击打开的应用）之间一键切换。
- **本地语言名称解析**：跟随系统当前语言动态读取应用标题（`ApplicationInfo.loadLabel`），对于卸载中或损坏的异常包具备自动降级容错机制。
- **一键复制到剪贴板**：按每行 `包名 | 应用名` 格式化导出，并在末尾附加数量统计，方便粘贴至微信、备忘录或文本编辑器中。
- **无感授权（零弹窗）**：声明 Normal 级别的 `QUERY_ALL_PACKAGES` 权限以突破 Android 11+ 的软件包可见性限制，安装时系统自动授予，无需运行时申请弹窗。
- **现代架构设计**：
  - **Single Activity** 架构，精简无冗余。
  - 采用 **ViewModel + LiveData** 管理生命周期与状态，在后台单线程池中异步加载数据，屏幕旋转（横竖屏切换）不丢失状态与已加载数据。
- **极佳兼容性**：最低支持 **Android 7.0 (API 24)**，编译目标支持至 **Android 16 (API 36)**，字节码严格锁定为 **Java 8 字节流**。

### 界面效果

- **初始状态**：展示「加载」与「复制（初始置灰）」按钮，支持切换筛选模式。
- **全部应用模式**：展示系统内所有底层与表层应用，统计总包数与可启动包数。
- **仅可启动模式**：瞬时过滤出仅含桌面图标的应用程序（如设置、相机、电话、日历等）。
- **复制结果格式**：
  ```text
  com.android.calculator2 | 计算器
  com.android.calendar | 日历
  com.android.camera2 | 相机
  com.android.settings | 设置
  com.tencent.mm | 微信
  ...

  可启动应用共 16 个（总应用 90 个）
  ```

### 技术指标参数

| 指标项 | 参数值 |
| :--- | :--- |
| **包名 / Application ID** | `com.eoncn.systemapks` |
| **最低支持系统 (minSdk)** | `API 24` (Android 7.0 Nougat) |
| **目标构建版本 (compileSdk / targetSdk)** | `API 36` (Android 16) |
| **构建工具版本 (Build-Tools)** | `36.0.0` |
| **构建插件与 Gradle 版本** | AGP `8.11.1` / Gradle `8.13` |
| **编译字节码标准** | `Java 8` 字节流 (Major Version 52) |
| **构建环境 JDK** | `JDK 17` (Gradle 8.x 运行时要求) |

### 下载与安装

官方构建的 Release APK 均已通过 GitHub Actions 完成数字签名，可直接在设备上安装：

1. 打开 **[Releases 发布页面](https://github.com/eoncn/systemApks/releases)**。
2. 下载最新版 `systemApks-vX.X.X.apk`。
3. 传至手机直接安装，或使用电脑 ADB 命令行安装：
   ```bash
   adb install -r systemApks-v1.0.0.apk
   ```

### 源码编译指南

#### 环境要求
- JDK 17 及以上
- Android SDK Platform 36 及 Build-Tools 36.0.0

#### 常用编译命令

```bash
# 克隆仓库
git clone https://github.com/eoncn/systemApks.git
cd systemApks

# 执行单元测试
./gradlew testDebugUnitTest

# 执行 Android Lint 代码质量检查
./gradlew lintDebug

# 编译 Debug APK
./gradlew assembleDebug
# 输出路径：app/build/outputs/apk/debug/app-debug.apk

# 编译 Release APK
./gradlew assembleRelease
# 输出路径：app/build/outputs/apk/release/app-release-unsigned.apk
```

### 项目代码目录

```
systemApks/
├── .github/workflows/
│   └── release.yml            # GitHub Actions 自动编译、签名与发布工作流
├── app/
│   ├── build.gradle           # SDK 36、minSdk 24、Java 8 字节码配置
│   ├── src/
│       ├── main/
│       │   ├── AndroidManifest.xml # QUERY_ALL_PACKAGES 权限声明
│       │   ├── java/com/eoncn/systemapks/
│       │   │   ├── MainActivity.java      # 界面交互、状态渲染与剪贴板逻辑
│       │   │   ├── AppListViewModel.java  # 异步检索 PackageManager 与快速过滤
│       │   │   ├── AppItem.java           # 应用数据对象（包名、本地名称、桌面标记）
│       │   │   ├── AppListAdapter.java    # RecyclerView 列表适配器
│       │   │   └── ClipTextFormatter.java # 剪贴板文本格式化工具类
│       │   └── res/
│       │       ├── layout/activity_main.xml # 主界面布局（操作栏、单选组、列表）
│       │       ├── layout/item_app.xml      # 单行应用列表条目
│       │       └── values/strings.xml       # 界面文本资源
│       └── test/java/com/eoncn/systemapks/
│           └── ClipTextFormatterTest.java   # 针对文本格式化的单元测试
```

### 持续集成与发布 (CI/CD)

项目配置了完整的 GitHub Actions 工作流（`.github/workflows/release.yml`）：
- **触发条件**：
  - 本地打 tag 推送（如 `git tag v1.0.1 && git push origin v1.0.1`）。
  - 在 GitHub 仓库页面的 **Actions** 标签下手动点击 **Run workflow**。
- **全自动签名**：
  - 自动生成独立证书，并通过 Android SDK `apksigner` 执行 **v1 + v2 + v3 全版本签名**与对齐，免去手动配置密钥的繁琐步骤。
- **自动发布 Release**：
  - 构建完成后自动生成 SHA256 校验和并附带 APK 安装包发布在 GitHub Releases 专区。

---

### 开源协议 / License

本项目遵循 [MIT License](LICENSE) 开源协议。
