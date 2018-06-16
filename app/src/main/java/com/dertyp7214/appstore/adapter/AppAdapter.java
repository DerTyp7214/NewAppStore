/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.items.SearchItem;
import com.dertyp7214.appstore.screens.AppScreen;

import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {

    private Activity context;
    private List<SearchItem> appItemList;

    public AppAdapter(Activity context, List<SearchItem> appItemList){
        this.appItemList=appItemList;
        this.context=context;
    }

    @NonNull
    @Override
    public AppAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.app_item_vertical, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AppAdapter.ViewHolder holder, int position) {
        SearchItem item = appItemList.get(position);

        holder.appTitle.setText(item.getAppTitle());
        holder.appIcon.setImageDrawable(item.getAppIcon());

        for(SearchItem searchItem : appItemList)
            Utils.appsList.put(searchItem.getId(), searchItem);

        holder.view.setOnClickListener(v -> {
            Pair<View, String> icon = Pair.create(holder.appIcon, "icon");
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(context, icon);
            context.startActivity(new Intent(context, AppScreen.class).putExtra("id", item.getId()), options.toBundle());
        });

    }

    class ViewHolder extends RecyclerView.ViewHolder {

        LottieAnimationView appIcon;
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
