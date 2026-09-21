package com.eoncn.systemapks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClipTextFormatterTest {

    @Test
    public void format_multipleItems_producesExpectedLayout() {
        List<AppItem> items = new ArrayList<>();
        items.add(new AppItem("com.android.settings", "设置", true));
        items.add(new AppItem("com.tencent.mm", "微信", true));

        String result = ClipTextFormatter.format(items, "全部应用共 2 个（可启动 2 个）");

        String expected = "com.android.settings | 设置\n"
                + "com.tencent.mm | 微信\n\n"
                + "全部应用共 2 个（可启动 2 个）";
        assertEquals(expected, result);
    }

    @Test
    public void format_launchableOnlySummary_producesExpectedLayout() {
        List<AppItem> items = new ArrayList<>();
        items.add(new AppItem("com.android.settings", "设置", true));

        String result = ClipTextFormatter.format(items, "可启动应用共 1 个（总应用 10 个）");

        String expected = "com.android.settings | 设置\n\n"
                + "可启动应用共 1 个（总应用 10 个）";
        assertEquals(expected, result);
    }

    @Test
    public void format_emptyList_producesOnlySummary() {
        String result = ClipTextFormatter.format(Collections.emptyList(), "共 0 个应用");
        assertEquals("\n共 0 个应用", result);
    }

    @Test
    public void appItem_fieldsPreserved() {
        AppItem item = new AppItem("pkg.test", "Test App", true);
        assertEquals("pkg.test", item.packageName);
        assertEquals("Test App", item.label);
        assertTrue(item.launchable);

        AppItem defaultItem = new AppItem("pkg.bg", "Bg App");
        assertFalse(defaultItem.launchable);
    }
}
