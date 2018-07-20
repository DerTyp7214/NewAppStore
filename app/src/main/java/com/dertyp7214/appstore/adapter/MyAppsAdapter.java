/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.adapter;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.ThemeStore;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.fragments.FragmentMyApps;
import com.dertyp7214.appstore.items.MyAppItem;
import com.dertyp7214.appstore.items.SearchItem;
import com.dertyp7214.appstore.screens.AppScreen;

import java.util.List;
import java.util.Objects;

public class MyAppsAdapter extends RecyclerView.Adapter<MyAppsAdapter.ViewHolder> {

    private Fragment context;
    private List<MyAppItem> appItemList;

    public MyAppsAdapter(Fragment context, List<MyAppItem> appItemList) {
        this.appItemList = appItemList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyAppsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.app_small, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyAppsAdapter.ViewHolder holder, int position) {
        MyAppItem item = appItemList.get(position);
        boolean appInstalled =
                Utils.applicationInstalled(Objects.requireNonNull(context.getActivity()),
                        item.getPackageName());

        holder.appSize.setText(item.getAppSize());
        holder.appTitle.setText(item.getAppTitle());
        holder.appIcon.setImageDrawable(item.getAppIcon());

        holder.clear.setOnClickListener(v -> new Thread(() -> {
            Utils.removeMyApp(item.getPackageName(), context.getActivity());
            if (context instanceof FragmentMyApps)
                ((FragmentMyApps) context).getMyApps(position);
        }).start());

        if (! Utils.appsList.containsKey(item.getPackageName()))
            Utils.appsList.put(item.getPackageName(),
                    new SearchItem(item.getAppTitle(), item.getPackageName(), item.getAppIcon()));

        holder.view.setOnClickListener(v -> {
            Pair<View, String> icon = Pair.create(holder.appIcon, "icon");
            ActivityOptions options =
                    ActivityOptions.makeSceneTransitionAnimation(context.getActivity(), icon);
            context.startActivity(new Intent(context.getActivity(), AppScreen.class)
                    .putExtra("id", item.getPackageName()), options.toBundle());
        });

        holder.openInstall.setText(appInstalled ? context
                .getString(R.string.text_open) : context.getString(R.string.text_install));
        holder.openInstall.setOnClickListener(v -> {
            if (appInstalled)
                if (! Utils.verifyInstallerId(context.getActivity(), item.getPackageName()))
                    context.startActivity(context.getActivity().getPackageManager()
                            .getLaunchIntentForPackage(item.getPackageName()));
                else
                    AppScreen.downloadApp(context.getActivity(), item.getAppTitle(),
                            item.getPackageName(), holder.openInstall);
        });

        holder.play.setOnClickListener(v -> {
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                        .parse("market://details?id=" + item.getPackageName())));
            } catch (android.content.ActivityNotFoundException anfe) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                        "https://play.google.com/store/apps/details?id=" + item
                                .getPackageName())));
            }
        });

        int color = ThemeStore.getInstance(context.getActivity()).getAccentColor();
        GradientDrawable bg =
                (GradientDrawable) context.getResources().getDrawable(R.drawable.button_border);
        bg.setStroke(3, color);
        holder.openInstall.setBackgroundDrawable(bg);
        holder.openInstall.setTextColor(color);

        if (Utils.verifyInstallerId(Objects.requireNonNull(context.getActivity()),
                item.getPackageName())) {
            holder.play.setVisibility(View.VISIBLE);
            holder.openInstall.setVisibility(View.GONE);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        LottieAnimationView appIcon;
        TextView appTitle, appSize;
        View view;
        ImageButton clear;
        Button openInstall;
        ImageView play;

        ViewHolder(View itemView) {
            super(itemView);

            appIcon = itemView.findViewById(R.id.img);
            appTitle = itemView.findViewById(R.id.title);
            appSize = itemView.findViewById(R.id.size);
            clear = itemView.findViewById(R.id.btn_clear);
            view = itemView.findViewById(R.id.view);
            openInstall = itemView.findViewById(R.id.btn_openInstall);
            play = itemView.findViewById(R.id.img_play);

        }
    }

    @Override
    public int getItemCount() {
        return appItemList.size();
    }
}
