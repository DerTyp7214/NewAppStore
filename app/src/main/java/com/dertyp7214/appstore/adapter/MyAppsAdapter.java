/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.adapter;

import android.app.ActivityOptions;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.fragments.FragmentMyApps;
import com.dertyp7214.appstore.items.MyAppItem;
import com.dertyp7214.appstore.items.SearchItem;
import com.dertyp7214.appstore.screens.AppScreen;

import java.util.List;

public class MyAppsAdapter extends RecyclerView.Adapter<MyAppsAdapter.ViewHolder> {

    private Fragment context;
    private List<MyAppItem> appItemList;

    public MyAppsAdapter(Fragment context, List<MyAppItem> appItemList){
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

        holder.appSize.setText(item.getAppSize());
        holder.appTitle.setText(item.getAppTitle());
        holder.appIcon.setImageDrawable(item.getAppIcon());

        holder.clear.setOnClickListener(v -> new Thread(() -> {
            Utils.removeMyApp(item.getPackageName(), context.getActivity());
            if(context instanceof FragmentMyApps)
                ((FragmentMyApps) context).getMyApps(position);
        }).start());

        if (!Utils.appsList.containsKey(item.getPackageName()))
            Utils.appsList.put(item.getPackageName(), new SearchItem(item.getAppTitle(), item.getPackageName(), item.getAppIcon()));

        holder.view.setOnClickListener(v -> {
            Pair<View, String> icon = Pair.create(holder.appIcon, "icon");
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(context.getActivity(), icon);
            context.startActivity(new Intent(context.getActivity(), AppScreen.class).putExtra("id", item.getPackageName()), options.toBundle());
        });
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        LottieAnimationView appIcon;
        TextView appTitle, appSize;
        View view;
        ImageButton clear;

        ViewHolder(View itemView) {
            super(itemView);

            appIcon = itemView.findViewById(R.id.img);
            appTitle = itemView.findViewById(R.id.title);
            appSize = itemView.findViewById(R.id.size);
            clear = itemView.findViewById(R.id.btn_clear);
            view = itemView.findViewById(R.id.view);

        }
    }

    @Override
    public int getItemCount() {
        return appItemList.size();
    }
}
