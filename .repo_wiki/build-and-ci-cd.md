# 构建系统与持续集成

> 介绍 Gradle / AGP 构建系统配置、Java 17 工具链、发布产物及 GitHub Actions 自动签名与 Release 工作流。

## 概述

systemApks 采用了精简但高度工程化的构建与持续集成体系。底层基于 Gradle 8.13 与 Android Gradle Plugin (AGP) 8.11.1，构建环境要求 JDK 17，同时将编译产物严格约束在 Java 8 字节码规范下。

项目的持续集成通过 GitHub Actions（`.github/workflows/release.yml`）实现了全自动化流水线：无论是推送版本 Tag 还是手动在网页端触发，工作流都会自动编译、生成独立证书、执行全版本（v1/v2/v3）数字签名、计算校验和并发布至 GitHub Releases，产物可直接在 Android 手机上安装。

## 关键文件

| 文件路径 | 说明 |
|---------|------|
| `build.gradle` | 根构建脚本，声明 AGP 插件版本 |
| `settings.gradle` | 仓库管理配置与项目模块引入 |
| `app/build.gradle` | 应用模块配置，包含 SDK 版本、依赖与 Java 工具链 |
| `gradle/wrapper/gradle-wrapper.properties` | Gradle 包装器配置（Gradle 8.13） |
| `.github/workflows/release.yml` | 完整的 GitHub Actions 自动编译、签名与发布流水线 |

## 核心概念

### Gradle 依赖解析与仓库规范
在 `settings.gradle` 中启用了严格的仓库解析策略：
```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
```
禁止子模块自行定义不受信任的外部仓库，提升构建安全性与依赖可溯源性。

### 双 Java 版本分层
项目巧妙地区分了“构建环境 JDK”与“运行时目标字节码”：
- **构建环境**：`java.toolchain.languageVersion = JavaLanguageVersion.of(17)`。AGP 8.x 要求 Gradle 本身运行在 JDK 17 及以上。
- **运行时字节码**：`compileOptions` 设置为 `JavaVersion.VERSION_1_8`。确保产物向下兼容至 Android 7.0 (API 24)。

### GitHub Actions 全自动签名体系
传统 Android 开源项目通常需要维护者在 GitHub Secrets 中配置 base64 密钥与口令。本项目在 `.github/workflows/release.yml` 中采用了自动化自签名机制：
1. **自动生成密钥库**：在构建容器内使用 `keytool` 即时生成 2048 位 RSA 专用签名证书。
2. **多版本联合签名**：通过 Android SDK 提供的 `apksigner` 工具执行联合签名：
   ```bash
   $APKSIGNER sign \
     --ks release.jks \
     --ks-pass "pass:systemApksRelease2026" \
     --key-pass "pass:systemApksRelease2026" \
     --v1-signing-enabled true \
     --v2-signing-enabled true \
     --v3-signing-enabled true \
     --out "$APK_NAME" \
     app/build/outputs/apk/release/app-release-unsigned.apk
   ```
3. **有效性校验**：签名后立即执行 `$APKSIGNER verify --verbose` 保证签名完备性。
4. **生成 SHA-256 校验和**：随同 APK 文件一同归档到 Release 中，供用户比对。

## 常用本地构建命令

```bash
# 编译 Debug 版 APK
./gradlew assembleDebug
# 产物路径：app/build/outputs/apk/debug/app-debug.apk

# 编译 Release 版未签名 APK
./gradlew assembleRelease
# 产物路径：app/build/outputs/apk/release/app-release-unsigned.apk

# 清理构建缓存
./gradlew clean
```

## 注意事项

- **混淆配置**：当前 `buildTypes.release` 中 `minifyEnabled false`。由于项目体积小且无复杂第三方 SDK，未开启 R8 混淆；若未来需要混淆，需注意保留反射与 `QUERY_ALL_PACKAGES` 相关的规则。
- **构建环境**：必须确保本地安装了 JDK 17，若使用 JDK 8 或 JDK 11 将无法启动 Gradle 8.x Daemon。

---

**相关页面**: [index.md](index.md) | [overview.md](overview.md) | [permissions-and-compatibility.md](permissions-and-compatibility.md) | [testing.md](testing.md)
