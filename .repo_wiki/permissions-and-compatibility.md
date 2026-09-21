# 权限系统与系统兼容性

> 剖析 Android 软件包可见性限制、QUERY_ALL_PACKAGES 权限声明、平台兼容性跨度与 Java 8 字节码规范。

## 概述

随着 Android 系统的演进，Google 对隐私保护与应用沙盒的限制越来越严格。从 Android 11（API 30）开始引入的“软件包可见性”（Package Visibility）机制，使得普通应用默认只能“看到”非常有限的系统与第三方应用。

systemApks 作为一款专门检查与导出系统应用清单的工具，必须突破该限制以完整列举全量安装包，同时还需要保证在老旧设备（最低 Android 7.0）到最新设备（Android 16）上的无缝运行。

## 关键文件

| 文件路径 | 说明 |
|---------|------|
| `app/src/main/AndroidManifest.xml` | 声明 `QUERY_ALL_PACKAGES` 权限，配置备份与安全规则 |
| `app/build.gradle` | 定义 `minSdk 24`、`targetSdk 36`、`compileSdk 36` 及 Java 8 兼容性 |

## 核心概念

### 软件包可见性与 QUERY_ALL_PACKAGES
在 Android 11+（`targetSdk >= 30`）环境下：
- 若应用未在 Manifest 中声明任何规则，调用 `PackageManager.getInstalledApplications(0)` 将只能获取本应用以及少数核心系统包。
- 针对清单类、备份类及安全审计类工具，Android 提供了特殊的权限：
  ```xml
  <uses-permission
      android:name="android.permission.QUERY_ALL_PACKAGES"
      tools:ignore="QueryAllPackagesPermission" />
  ```
- **权限级别**：该权限属于 `Normal`（普通）级别，由系统在安装时自动授予，**无需**在运行时通过 `requestPermissions` 弹窗向用户索权。
- **Lint 压制**：由于 Google Play 对该权限有审核政策限制，Gradle 会抛出 Lint 警告，因此在 Manifest 中添加了 `tools:ignore="QueryAllPackagesPermission"` 进行显式声明。

### 宽泛的平台兼容性跨度 (API 24 - API 36)

| 指标 | 版本 | 对应系统代号 | 说明 |
|-----|------|-------------|------|
| `minSdk` | 24 | Android 7.0 (Nougat) | 覆盖市场上绝大多数老旧存量设备 |
| `targetSdk` | 36 | Android 16 | 遵循最新平台的行为准则与安全规范 |
| `compileSdk` | 36 | Android 16 | 允许使用最新的 SDK API 进行编译 |

### Java 8 字节流标准
在 `app/build.gradle` 中明确锁定了字节码生成标准：
```groovy
compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
}
```
- **Class Major Version 52**：生成的 `.class` 文件和经过 D8 脱糖编译后的 DEX 字节码严格遵循 Java 8 规范。
- **兼容性保障**：即使在较旧的 Android 7.x ART 运行时上，也不会因高版本 Java 特性导致类加载（ClassFormatError）失败。

## 数据保护规则

在 `app/src/main/AndroidManifest.xml` 中禁用了不必要的自动备份：
- `android:allowBackup="false"`
- 配置了 `@xml/data_extraction_rules` 与 `@xml/backup_rules`。
作为只读检查工具，本应用不产生任何需要云端备份的用户私密数据，此配置进一步保障了应用的安全合规性。

## 注意事项

- **Google Play 上架限制**：若未来计划上架至 Google Play Store，必须提供合规声明视频并证明该应用属于合法的“设备清单/搜索”工具类应用，否则审核可能受阻。对于开源分发（GitHub Releases / F-Droid / 国内应用市场），无此限制。

---

**相关页面**: [index.md](index.md) | [overview.md](overview.md) | [viewmodel-and-data.md](viewmodel-and-data.md) | [build-and-ci-cd.md](build-and-ci-cd.md)
