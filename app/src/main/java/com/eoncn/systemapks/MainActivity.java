package com.eoncn.systemapks;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppListViewModel viewModel;
    private AppListAdapter adapter;

    private Button loadButton;
    private Button copyButton;
    private RadioGroup filterGroup;
    private RadioButton rbFilterAll;
    private RadioButton rbFilterLaunchable;
    private TextView statusView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadButton = findViewById(R.id.btn_load);
        copyButton = findViewById(R.id.btn_copy);
        filterGroup = findViewById(R.id.rg_filter);
        rbFilterAll = findViewById(R.id.rb_filter_all);
        rbFilterLaunchable = findViewById(R.id.rb_filter_launchable);
        statusView = findViewById(R.id.tv_status);
        progressBar = findViewById(R.id.pb_loading);

        adapter = new AppListAdapter();
        RecyclerView listView = findViewById(R.id.rv_apps);
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        listView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(AppListViewModel.class);

        filterGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_filter_launchable) {
                viewModel.setFilterMode(AppListViewModel.FilterMode.LAUNCHABLE_ONLY);
            } else {
                viewModel.setFilterMode(AppListViewModel.FilterMode.ALL);
            }
        });

        viewModel.getApps().observe(this, items -> {
            adapter.submit(items);
            render();
        });
        viewModel.getFilterMode().observe(this, mode -> {
            int targetId = mode == AppListViewModel.FilterMode.LAUNCHABLE_ONLY
                    ? R.id.rb_filter_launchable
                    : R.id.rb_filter_all;
            if (filterGroup.getCheckedRadioButtonId() != targetId) {
                filterGroup.check(targetId);
            }
            render();
        });
        viewModel.isLoading().observe(this, ignored -> render());
        viewModel.getError().observe(this, ignored -> render());

        loadButton.setOnClickListener(v -> viewModel.load());
        copyButton.setOnClickListener(v -> copyToClipboard());

        render();
    }

    private void render() {
        boolean loading = Boolean.TRUE.equals(viewModel.isLoading().getValue());
        List<AppItem> items = viewModel.getApps().getValue();
        String error = viewModel.getError().getValue();
        int count = items == null ? 0 : items.size();
        int totalCount = viewModel.getTotalCount();
        int launchableCount = viewModel.getLaunchableCount();
        AppListViewModel.FilterMode mode = viewModel.getFilterMode().getValue();

        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        loadButton.setEnabled(!loading);
        copyButton.setEnabled(!loading && count > 0);

        if (totalCount > 0) {
            rbFilterAll.setText(getString(R.string.filter_all_with_count, totalCount));
            rbFilterLaunchable.setText(getString(R.string.filter_launchable_with_count, launchableCount));
        } else {
            rbFilterAll.setText(R.string.filter_all);
            rbFilterLaunchable.setText(R.string.filter_launchable);
        }

        if (loading) {
            statusView.setText(R.string.status_loading);
        } else if (error != null) {
            statusView.setText(getString(R.string.status_error, error));
        } else if (totalCount > 0) {
            if (mode == AppListViewModel.FilterMode.LAUNCHABLE_ONLY) {
                statusView.setText(getString(R.string.status_done_launchable, count, totalCount));
            } else {
                statusView.setText(getString(R.string.status_done_all, totalCount, launchableCount));
            }
        } else {
            statusView.setText(R.string.status_idle);
        }
    }

    private void copyToClipboard() {
        List<AppItem> items = viewModel.getApps().getValue();
        if (items == null || items.isEmpty()) {
            Toast.makeText(this, R.string.toast_nothing, Toast.LENGTH_SHORT).show();
            return;
        }

        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            Toast.makeText(this, getString(R.string.toast_copy_failed, "ClipboardManager"),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            clipboard.setPrimaryClip(
                    ClipData.newPlainText(getString(R.string.app_name), buildClipText(items)));
        } catch (Exception e) {
            String reason = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            Toast.makeText(this, getString(R.string.toast_copy_failed, reason),
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(this, getString(R.string.toast_copied, items.size()),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String buildClipText(List<AppItem> items) {
        int totalCount = viewModel.getTotalCount();
        int launchableCount = viewModel.getLaunchableCount();
        AppListViewModel.FilterMode mode = viewModel.getFilterMode().getValue();
        String summary;
        if (mode == AppListViewModel.FilterMode.LAUNCHABLE_ONLY) {
            summary = getString(R.string.clip_summary_launchable, items.size(), totalCount);
        } else {
            summary = getString(R.string.clip_summary_all, totalCount, launchableCount);
        }
        return ClipTextFormatter.format(items, summary);
    }
}
