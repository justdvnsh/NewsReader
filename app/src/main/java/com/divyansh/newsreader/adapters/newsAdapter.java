package com.divyansh.newsreader.adapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class newsAdapter extends RecyclerView.Adapter<newsAdapter.newsViewsHolder> {


    @NonNull
    @Override
    public newsViewsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull newsViewsHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class newsViewsHolder extends RecyclerView.ViewHolder {
        public newsViewsHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
