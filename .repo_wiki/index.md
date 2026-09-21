# 项目知识库索引 (Repo Wiki Index)

> 本知识库遵循 LLM Wiki 范式增量构建与维护，为开发者与 AI Agent 提供关于 systemApks 项目的跨文件全景理解与结构性约束。

---

## 规范与范式 (Standards & Paradigm)

- [paradigm.md](paradigm.md) — Karpathy 提出的 LLM Wiki 范式规范，包含三层架构、核心操作与维护准则（自包含规范，无外部链接）

## 架构与核心 (Architecture & Core)

- [overview.md](overview.md) — 项目概述、技术指标（SDK 36, minSdk 24, Java 8 字节码）与整体 Single Activity MVVM 架构
- [viewmodel-and-data.md](viewmodel-and-data.md) — AppListViewModel 异步并发调度、包信息检索、本地化名称容错解析与高效内存筛选
- [ui-layer.md](ui-layer.md) — MainActivity 单页面交互、基于 LiveData 的声明式 `render()` 渲染、AppListAdapter 与布局结构

## 功能与特性 (Features & Domain Logic)

- [clipboard-and-formatting.md](clipboard-and-formatting.md) — ClipTextFormatter 纯 Java 导出文本排版算法与系统剪贴板安全注入交互
- [permissions-and-compatibility.md](permissions-and-compatibility.md) — QUERY_ALL_PACKAGES 软件包可见性突破、平台兼容性跨度（API 24-36）与 Java 8 字节流规范

## 构建与质量 (Build & Quality Assurance)

- [build-and-ci-cd.md](build-and-ci-cd.md) — Gradle 8.13 / AGP 8.11.1 构建管线、JDK 17 工具链与 GitHub Actions 全自动多版本签名发布
- [testing.md](testing.md) — 基于 JUnit 4 的本地 JVM 高速单元测试策略、测试隔离设计与 Android Lint 静态质量扫描

---

**日志记录**: [log.md](log.md)
