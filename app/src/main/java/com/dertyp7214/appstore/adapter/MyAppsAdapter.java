/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.items.MyAppItem;

import java.util.List;

public class MyAppsAdapter extends RecyclerView.Adapter<MyAppsAdapter.ViewHolder> {

    private Activity context;
    private List<MyAppItem> appItemList;

    public MyAppsAdapter(Activity context, List<MyAppItem> appItemList){
        this.appItemList=appItemList;
        this.context=context;
    }

    @NonNull
    @Override
    public MyAppsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.app_small, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyAppsAdapter.ViewHolder holder, int position) {
        MyAppItem item = appItemList.get(position);

        holder.appSize.setText(item.getAppTitle());
        holder.appTitle.setText(item.getAppTitle());
        holder.appIcon.setImageDrawable(item.getAppIcon());

    }

    class ViewHolder extends RecyclerView.ViewHolder {

        LottieAnimationView appIcon;
        TextView appTitle, appSize;
        View view;

        ViewHolder(View itemView) {
            super(itemView);

            appIcon = itemView.findViewById(R.id.img);
            appTitle = itemView.findViewById(R.id.title);
            appSize = itemView.findViewById(R.id.size);
            view = itemView.findViewById(R.id.view);

        }
    }

    @Override
    public int getItemCount() {
        return appItemList.size();
    }
}
