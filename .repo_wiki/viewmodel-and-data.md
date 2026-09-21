# 数据获取与状态管理

> 详述 AppListViewModel 与 AppItem 的数据架构、后台并发模型、包名及标签解析与快速内存过滤机制。

## 概述

在 Android 系统中，获取已安装应用信息依赖于 `PackageManager` 服务的 IPC 调用。随着系统预装组件与用户应用的增多，全量包信息的获取与解析是一个明显的 I/O 与 CPU 密集型任务。

`AppListViewModel` 继承自 `AndroidViewModel`，承担了该过程的所有异步加载、容错降级、拼音/语言感知排序及过滤状态管理职责。它完全屏蔽了数据层细节，仅向 UI 层暴露只读的 `LiveData` 接口，使界面层能够以单向数据流的方式安全响应状态变更。

## 关键文件

| 文件路径 | 说明 |
|---------|------|
| `app/src/main/java/com/eoncn/systemapks/AppItem.java` | 不可变条目数据结构，保存单条应用的包名、标签及桌面图标标记 |
| `app/src/main/java/com/eoncn/systemapks/AppListViewModel.java` | 核心状态管理与后台加载器，处理并发、数据排序与筛选 |

## 核心概念

### AppItem 不可变模型
`AppItem` 设计为包级私有（package-private）且所有字段均为 `final` 的不可变对象：
- `packageName` (`@NonNull String`): 应用唯一标识包名（如 `com.android.settings`）。
- `label` (`@NonNull String`): 应用在当前语言下的本地化名称（如 `设置`）。
- `launchable` (`boolean`): 是否在桌面抽屉中有启动入口（包含 `CATEGORY_LAUNCHER`）。

### 并发与线程调度
`AppListViewModel` 内部使用独立的单线程线程池和主线程 Handler 进行线程调度：
```java
private final ExecutorService executor = Executors.newSingleThreadExecutor();
private final Handler mainHandler = new Handler(Looper.getMainLooper());
```
- 通过 `inFlight` 布尔标记防止用户快速重复点击引发的并发重入查询。
- 在 `onCleared()` 生命周期回调中调用 `executor.shutdownNow()` 和 `mainHandler.removeCallbacksAndMessages(null)`，避免后台任务泄露。

### 容错降级的标签解析 (`resolveLabel`)
在某些极端场景下（如应用正在被系统卸载、APK 资源损坏或签名不匹配），调用 `info.loadLabel(packageManager)` 可能会抛出运行时异常。
`AppListViewModel.resolveLabel()` 实现了异常捕获保护：若解析异常或返回空串，自动降级退回使用 `info.packageName` 作为标签展示，确保整个列表读取不会因单只异常应用而崩溃中断。

### 本地化敏感排序
解析完成后，应用使用当前系统语言的 `Collator` 进行字母排序：
```java
LocaleList locales = getApplication().getResources().getConfiguration().getLocales();
Locale currentLocale = locales.isEmpty() ? Locale.getDefault() : locales.get(0);
final Collator collator = Collator.getInstance(currentLocale);
Collections.sort(items, (left, right) -> {
    int byLabel = collator.compare(left.label, right.label);
    return byLabel != 0 ? byLabel : left.packageName.compareTo(right.packageName);
});
```
对于中文环境，`Collator` 会依据汉字拼音进行自然排序；若名称完全一致，则以包名字母序二次排序。

## 工作流程

```
[调用 load()]
      |
[检查 inFlight] -> 若为 true 直接退出
      |
[主线程: inFlight=true, loading=TRUE, error=null]
      |
[executor.execute()] (后台单线程)
      |
      +---> 1. queryIntentActivities(CATEGORY_LAUNCHER) 收集可启动包名集合
      +---> 2. getInstalledApplications(0) 获取全部已安装 ApplicationInfo
      +---> 3. 遍历构造 AppItem (调用 resolveLabel 防崩溃处理)
      +---> 4. 基于系统当前 Locale 的 Collator 进行自然排序
      |
[mainHandler.post()] (切回主线程)
      |
      +---> inFlight=false, loading=FALSE
      +---> 成功: 更新 allApps 缓存，调用 applyFilter() 刷新 LiveData
      +---> 失败: 将异常信息写入 error LiveData
```

## 内存过滤逻辑 (`applyFilter`)

`AppListViewModel` 维护了完整的全量包缓存 `List<AppItem> allApps`。当用户切换 `FilterMode` 时：
- `FilterMode.ALL`: 直接将 `allApps` 赋给 `apps` LiveData。
- `FilterMode.LAUNCHABLE_ONLY`: 快速遍历 `allApps`，仅保留 `item.launchable == true` 的条目构建新列表并通知 UI。

该过程完全在内存中完成，耗时极短（< 1ms），避免了二次访问系统服务的性能损耗。

## 注意事项

- **只读暴露**: 向外部暴露的都是 `LiveData<T>` 而非 `MutableLiveData<T>`，防止外部组件直接修改内部状态。
- **资源清理**: 任何新增的后台资源或定时任务必须在 `onCleared()` 中安全释放。

---

**相关页面**: [index.md](index.md) | [overview.md](overview.md) | [ui-layer.md](ui-layer.md) | [permissions-and-compatibility.md](permissions-and-compatibility.md)
