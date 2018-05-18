/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.items.AppGroupItem;

import java.util.List;

public class AppGroupAdapter extends RecyclerView.Adapter<AppGroupAdapter.ViewHolder> {

    private Context context;
    private List<AppGroupItem> appGroupItemList;

    public AppGroupAdapter(Context context, List<AppGroupItem> appGroupItemList){
        this.appGroupItemList=appGroupItemList;
        this.context=context;
    }

    @NonNull
    @Override
    public AppGroupAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.app_group, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AppGroupAdapter.ViewHolder holder, int position) {
        AppGroupItem item = appGroupItemList.get(position);

        AppAdapter appAdapter = new AppAdapter(context, item.getAppList());
        holder.recyclerView.setAdapter(appAdapter);

        holder.title.setText(item.getTitle());

    }

    class ViewHolder extends RecyclerView.ViewHolder {

        RecyclerView recyclerView;
        TextView title;

        ViewHolder(View itemView) {
            super(itemView);

            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

            recyclerView = itemView.findViewById(R.id.app_list_vertical);
            recyclerView.setLayoutManager(layoutManager);

            title = itemView.findViewById(R.id.title);
        }
    }

    @Override
    public int getItemCount() {
        return appGroupItemList.size();
    }
}
