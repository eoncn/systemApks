package com.eoncn.systemapks;

import android.app.Application;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.LocaleList;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppListViewModel extends AndroidViewModel {

    public enum FilterMode {
        ALL,
        LAUNCHABLE_ONLY
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<List<AppItem>> apps =
            new MutableLiveData<>(Collections.<AppItem>emptyList());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(Boolean.FALSE);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<FilterMode> filterMode = new MutableLiveData<>(FilterMode.ALL);

    private List<AppItem> allApps = Collections.emptyList();
    private boolean inFlight;

    public AppListViewModel(@NonNull Application application) {
        super(application);
    }

    @NonNull
    LiveData<List<AppItem>> getApps() {
        return apps;
    }

    @NonNull
    LiveData<Boolean> isLoading() {
        return loading;
    }

    @NonNull
    LiveData<String> getError() {
        return error;
    }

    @NonNull
    LiveData<FilterMode> getFilterMode() {
        return filterMode;
    }

    void setFilterMode(@NonNull FilterMode mode) {
        if (filterMode.getValue() != mode) {
            filterMode.setValue(mode);
            applyFilter();
        }
    }

    int getTotalCount() {
        return allApps.size();
    }

    int getLaunchableCount() {
        int count = 0;
        for (AppItem item : allApps) {
            if (item.launchable) {
                count++;
            }
        }
        return count;
    }

    void load() {
        if (inFlight) {
            return;
        }
        inFlight = true;
        loading.setValue(Boolean.TRUE);
        error.setValue(null);

        executor.execute(() -> {
            List<AppItem> result;
            String failure;
            try {
                result = queryInstalledApps();
                failure = null;
            } catch (Exception e) {
                result = null;
                failure = describe(e);
            }

            final List<AppItem> loaded = result;
            final String message = failure;
            mainHandler.post(() -> {
                inFlight = false;
                loading.setValue(Boolean.FALSE);
                if (message != null) {
                    error.setValue(message);
                } else {
                    allApps = loaded != null ? loaded : Collections.<AppItem>emptyList();
                    applyFilter();
                }
            });
        });
    }

    private void applyFilter() {
        FilterMode mode = filterMode.getValue();
        if (mode == null || mode == FilterMode.ALL) {
            apps.setValue(allApps);
        } else {
            List<AppItem> filtered = new ArrayList<>();
            for (AppItem item : allApps) {
                if (item.launchable) {
                    filtered.add(item);
                }
            }
            apps.setValue(filtered);
        }
    }

    @NonNull
    @SuppressWarnings("deprecation")
    private List<AppItem> queryInstalledApps() {
        PackageManager packageManager = getApplication().getPackageManager();

        // 获取所有具有桌面启动入口（CATEGORY_LAUNCHER）的应用包名集合
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(launcherIntent, 0);
        Set<String> launchablePackages = new HashSet<>(resolveInfos.size());
        for (ResolveInfo ri : resolveInfos) {
            if (ri.activityInfo != null && ri.activityInfo.packageName != null) {
                launchablePackages.add(ri.activityInfo.packageName);
            }
        }

        List<ApplicationInfo> installed = packageManager.getInstalledApplications(0);
        List<AppItem> items = new ArrayList<>(installed.size());
        for (ApplicationInfo info : installed) {
            boolean launchable = launchablePackages.contains(info.packageName);
            items.add(new AppItem(info.packageName, resolveLabel(packageManager, info), launchable));
        }

        final Collator collator = Collator.getInstance(currentLocale());
        Collections.sort(items, (left, right) -> {
            int byLabel = collator.compare(left.label, right.label);
            return byLabel != 0 ? byLabel : left.packageName.compareTo(right.packageName);
        });
        return items;
    }

    @NonNull
    private static String resolveLabel(@NonNull PackageManager packageManager,
                                       @NonNull ApplicationInfo info) {
        // 正在卸载或资源损坏的包会让 loadLabel 抛异常，这类条目退回显示包名而不是整批失败
        try {
            CharSequence label = info.loadLabel(packageManager);
            if (label != null) {
                String text = label.toString().trim();
                if (!text.isEmpty()) {
                    return text;
                }
            }
        } catch (Exception ignored) {
            // 退回包名
        }
        return info.packageName;
    }

    @NonNull
    private Locale currentLocale() {
        LocaleList locales = getApplication().getResources().getConfiguration().getLocales();
        return locales.isEmpty() ? Locale.getDefault() : locales.get(0);
    }

    @NonNull
    private static String describe(@NonNull Throwable t) {
        String message = t.getMessage();
        return (message == null || message.isEmpty()) ? t.getClass().getSimpleName() : message;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mainHandler.removeCallbacksAndMessages(null);
        executor.shutdownNow();
    }
}
