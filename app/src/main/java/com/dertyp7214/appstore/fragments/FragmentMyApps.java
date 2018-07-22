/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dertyp7214.appstore.Config;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.ThemeStore;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.adapter.MyAppsAdapter;
import com.dertyp7214.appstore.components.DividerItemDecorator;
import com.dertyp7214.appstore.items.MyAppItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FragmentMyApps extends TabFragment {

    private SwipeRefreshLayout refreshLayout;
    private Activity activity;
    private List<MyAppItem> myAppList = new ArrayList<>();
    private RecyclerView recyclerView;
    private MyAppsAdapter adapter;
    private ThemeStore themeStore;

    @SuppressLint("StaticFieldLeak")
    private static FragmentMyApps instance;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_myapps, container, false);

        instance = this;

        activity = getActivity();

        themeStore = ThemeStore.getInstance(activity);

        adapter = new MyAppsAdapter(this, myAppList);

        recyclerView = view.findViewById(R.id.rv_my_apps);

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        RecyclerView.ItemDecoration dividerItemDecoration =
                new DividerItemDecorator(ContextCompat.getDrawable(activity, R.drawable.divider));
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        CardView cardView = view.findViewById(R.id.card);

        getMyApps();

        refreshLayout = view.findViewById(R.id.refresh);
        refreshLayout.setColorSchemeColors(themeStore.getPrimaryColor(),
                themeStore.getPrimaryHue(100),
                themeStore.getPrimaryHue(200),
                themeStore.getPrimaryHue(300));
        refreshLayout.setDistanceToTriggerSync(80);
        refreshLayout.setSize(SwipeRefreshLayout.DEFAULT);
        refreshLayout.setOnRefreshListener(() -> new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (FragmentAppGroups.hasInstance()) {
                    FragmentAppGroups appGroups = FragmentAppGroups.getInstance();
                    appGroups.getAppList(refreshLayout, true);
                }
            }
        }, 1000));

        return view;
    }

    public static boolean hasInstance() {
        return instance != null;
    }

    public static FragmentMyApps getInstance() {
        return instance;
    }

    public void getMyApps() {
        getMyApps(- 1);
    }

    public void getMyApps(int id) {
        new Thread(() -> {
            try {
                myAppList.clear();
                String myApps = Utils.getWebContent(
                        Config.API_URL + "/apps/myapps.php?uid=" + Config.UID(activity));
                JSONObject object = new JSONObject(myApps);
                JSONArray array = object.getJSONArray("apps");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject app = array.getJSONObject(i);
                    if (! app.getString("id").equals("null"))
                        myAppList.add(new MyAppItem(app.getString("title"),
                                app.getString("size"),
                                app.getString("id"),
                                Utils.drawableFromUrl(activity, app.getString("img"))));
                }
                activity.runOnUiThread(() -> {
                    if (id >= 0)
                        adapter.notifyItemRemoved(id);
                    else
                        adapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public String getName(Context context) {
        return context.getString(R.string.my_apps);
    }
}
