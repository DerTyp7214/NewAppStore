/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.items.ModuleItem;

import java.util.List;

public class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.ViewHolder> {

    private List<ModuleItem> appItemList;

    public ModuleAdapter(List<ModuleItem> appItemList){
        this.appItemList=appItemList;
    }

    @NonNull
    @Override
    public ModuleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ModuleAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.module_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleAdapter.ViewHolder holder, int position) {
        ModuleItem item = appItemList.get(position);

        holder.appTitle.setText(item.getTitle());
        holder.appIcon.setImageDrawable(item.getIcon());
        holder.appPackage.setText(item.getPackageName());

        holder.view.setOnClickListener(v -> {

        });

    }

    class ViewHolder extends RecyclerView.ViewHolder {

        LottieAnimationView appIcon;
        TextView appTitle, appPackage;
        View view;

        ViewHolder(View itemView) {
            super(itemView);

            appIcon = itemView.findViewById(R.id.img_icon);
            appTitle = itemView.findViewById(R.id.txt_label);
            appPackage = itemView.findViewById(R.id.txt_pkg);
            view = itemView.findViewById(R.id.view);

        }
    }

    @Override
    public int getItemCount() {
        return appItemList.size();
    }
}
