package com.eoncn.systemapks;

import androidx.annotation.NonNull;

final class AppItem {

    @NonNull
    final String packageName;

    @NonNull
    final String label;

    final boolean launchable;

    AppItem(@NonNull String packageName, @NonNull String label, boolean launchable) {
        this.packageName = packageName;
        this.label = label;
        this.launchable = launchable;
    }

    AppItem(@NonNull String packageName, @NonNull String label) {
        this(packageName, label, false);
    }
}
