/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.items.SearchItem;
import com.dertyp7214.appstore.screens.AppScreen;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private Activity context;
    private List<SearchItem> appItemList;

    public SearchAdapter(Activity context, List<SearchItem> appItemList){
        this.appItemList=appItemList;
        this.context=context;
    }

    @NonNull
    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.ViewHolder holder, int position) {
        SearchItem item = appItemList.get(position);

        holder.appTitle.setText(item.getAppTitle());
        holder.appIcon.setImageDrawable(item.getAppIcon());

        for(SearchItem searchItem : appItemList)
            if (!Utils.appsList.containsKey(searchItem.getId()))
                Utils.appsList.put(searchItem.getId(), searchItem);

        holder.view.setOnClickListener(v -> {
            Pair<View, String> icon = Pair.create(holder.appIcon, "icon");
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(context, icon);
            context.startActivity(new Intent(context, AppScreen.class).putExtra("id", item.getId()), options.toBundle());
        });

    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView appIcon;
        TextView appTitle;
        View view;

        ViewHolder(View itemView) {
            super(itemView);

            appIcon = itemView.findViewById(R.id.appIcon);
            appTitle = itemView.findViewById(R.id.appTitle);
            view = itemView.findViewById(R.id.view);

        }
    }

    @Override
    public int getItemCount() {
        return appItemList.size();
    }
}
