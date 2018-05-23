/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.items.AppItem;
import com.dertyp7214.appstore.items.SearchItem;
import com.dertyp7214.appstore.screens.AppScreen;

import java.util.HashMap;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private Context context;
    private List<SearchItem> appItemList;

    public SearchAdapter(Context context, List<SearchItem> appItemList){
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

        HashMap<String, SearchItem> searchItemHashMap = new HashMap<>();

        for(SearchItem searchItem : appItemList)
            searchItemHashMap.put(searchItem.getId(), searchItem);

        Utils.appsList = searchItemHashMap;

        holder.view.setOnClickListener(v -> context.startActivity(new Intent(context, AppScreen.class).putExtra("id", item.getId())));

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
