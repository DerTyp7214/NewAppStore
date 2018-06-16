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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dertyp7214.appstore.Config;
import com.dertyp7214.appstore.LocalJSON;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.ThemeStore;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.adapter.AppGroupAdapter;
import com.dertyp7214.appstore.items.AppGroupItem;
import com.dertyp7214.appstore.items.NoConnection;
import com.dertyp7214.appstore.items.SearchItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Anu on 22/04/17.
 */



@SuppressLint("ValidFragment")
public class FragmentAppGroups extends TabFragment {

    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerViewAppGroup;
    private AppGroupAdapter adapter;
    private Activity context;
    private List<AppGroupItem> appList = new ArrayList<>();
    private String UID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_app_groups, container, false);

        context = getActivity();

        UID = Config.UID(context);

        ThemeStore themeStore = ThemeStore.getInstance(context);

        adapter = new AppGroupAdapter(context, appList);

        recyclerViewAppGroup = view.findViewById(R.id.recyclerViewAppGroup);
        recyclerViewAppGroup.setLayoutManager(new LinearLayoutManager(context));
        recyclerViewAppGroup.setAdapter(adapter);

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
                getAppList(refreshLayout, true);
            }
        }, 1000));

        refreshLayout.setRefreshing(true);
        getAppList(refreshLayout, false);

        return view;
    }

    private void getAppList(SwipeRefreshLayout layout, boolean refresh) {
        new Thread(() -> {
            if (haveConnection()) {
                if (serverOnline()) {
                    try {

                        appList.clear();

                        if (new JSONObject(LocalJSON.getJSON(context)).getBoolean("error") ||refresh)
                            LocalJSON.setJSON(context, Utils.getWebContent(Config.API_URL + "/apps/list.php"));

                        JSONObject object = new JSONObject(LocalJSON.getJSON(context));

                        JSONArray array = object.getJSONArray("apps");

                        if(refresh) {
                            JSONArray installedApps = new JSONArray();
                            for (int i = 0; i < array.length() - 1; i++) {
                                if(Utils.appInstalled(context, array.getJSONObject(i).getString("ID"))){
                                    installedApps.put(array.getJSONObject(i).getString("ID"));
                                }
                            }
                            String url = (Config.API_URL + Config.APK_PATH
                                    .replace("{uid}", UID)
                                    .replace("{id}", installedApps.toString()
                                            .replace("&", "")));
                            Log.d("FETCH", Utils.getWebContent(url));
                        }

                        List<SearchItem> appsList = new ArrayList<>();

                        for (int i = 0; i < array.length() - 1; i++) {
                            JSONObject obj = array.getJSONObject(i);
                            if (Utils.appInstalled(context, obj.getString("ID")))
                                appsList.add(new SearchItem(obj.getString("title"), obj.getString("ID"), Utils.drawableFromUrl(context, obj.getString("image"))));
                        }

                        appList.add(new AppGroupItem(getString(R.string.text_installed_apps), appsList));

                    } catch (Exception ignored) {
                    }
                    context.runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();
                        recyclerViewAppGroup.scrollBy(1, 1);
                        recyclerViewAppGroup.scrollBy(-1, -1);
                        if (layout != null)
                            layout.setRefreshing(false);
                    });
                } else {
                    appList.clear();
                    appList.add(new NoConnection(getString(R.string.server_offline)));
                    context.runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();
                        if (layout != null)
                            layout.setRefreshing(false);
                    });
                }
            } else {
                appList.clear();
                appList.add(new NoConnection(getString(R.string.no_connection)));
                context.runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    if (layout != null)
                        layout.setRefreshing(false);
                });
            }
        }).start();
    }

    @Override
    public String getName(Context context){
        return context.getString(R.string.home);
    }
}