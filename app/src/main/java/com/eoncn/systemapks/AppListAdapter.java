package com.eoncn.systemapks;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppViewHolder> {

    private final List<AppItem> items = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    void submit(@NonNull List<AppItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class AppViewHolder extends RecyclerView.ViewHolder {

        private final TextView labelView;
        private final TextView packageView;

        AppViewHolder(@NonNull View itemView) {
            super(itemView);
            labelView = itemView.findViewById(R.id.tv_label);
            packageView = itemView.findViewById(R.id.tv_package);
        }

        void bind(@NonNull AppItem item) {
            labelView.setText(item.label);
            packageView.setText(item.packageName);
        }
    }
}
