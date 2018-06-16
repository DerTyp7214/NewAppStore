/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.components.StartSnapHelper;
import com.dertyp7214.appstore.items.AppGroupItem;
import com.dertyp7214.appstore.items.NoConnection;

import java.util.List;

public class AppGroupAdapter extends RecyclerView.Adapter<AppGroupAdapter.ViewHolderNoConnection> {

    private Activity context;
    private List<AppGroupItem> appGroupItemList;

    public AppGroupAdapter(Activity context, List<AppGroupItem> appGroupItemList){
        this.appGroupItemList=appGroupItemList;
        this.context=context;
    }

    @NonNull
    @Override
    public ViewHolderNoConnection onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.app_group, parent, false));
            default:
                return new ViewHolderNoConnection(LayoutInflater.from(parent.getContext()).inflate(R.layout.no_connection, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderNoConnection holder, int position) {
        switch (holder.getItemViewType()) {
            case 0:
                ViewHolder viewHolder = (ViewHolder) holder;
                AppGroupItem appGroupItem = appGroupItemList.get(position);
                AppAdapter appAdapter = new AppAdapter(context, appGroupItem.getAppList());
                viewHolder.recyclerView.setAdapter(appAdapter);
                viewHolder.title.setText(appGroupItem.getTitle());
                break;
            case 1:
                NoConnection item = (NoConnection) appGroupItemList.get(position);
                holder.title.setText(item.getTitle());
                break;
        }
    }

    class ViewHolder extends ViewHolderNoConnection {

        RecyclerView recyclerView;
        TextView title;

        ViewHolder(View itemView) {
            super(itemView);

            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

            recyclerView = itemView.findViewById(R.id.app_list_vertical);
            recyclerView.setLayoutManager(layoutManager);

            SnapHelper startSnapHelper = new StartSnapHelper();
            startSnapHelper.attachToRecyclerView(recyclerView);

            title = itemView.findViewById(R.id.title);
        }
    }

    class ViewHolderNoConnection extends RecyclerView.ViewHolder {

        TextView title;

        ViewHolderNoConnection(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
        }
    }

    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        return appGroupItemList.get(position) instanceof NoConnection ? 1 : 0;
    }

    @Override
    public int getItemCount() {
        return appGroupItemList.size();
    }
}
