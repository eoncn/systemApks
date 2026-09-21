package com.eoncn.systemapks;

import androidx.annotation.NonNull;

import java.util.List;

final class ClipTextFormatter {

    private ClipTextFormatter() {
    }

    @NonNull
    static String format(@NonNull List<AppItem> items, @NonNull String summary) {
        StringBuilder builder = new StringBuilder(items.size() * 48);
        for (AppItem item : items) {
            builder.append(item.packageName).append(" | ").append(item.label).append('\n');
        }
        builder.append('\n').append(summary);
        return builder.toString();
    }
}
