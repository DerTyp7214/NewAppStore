/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.text.format.DateFormat;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static com.dertyp7214.appstore.Config.API_URL;
import static com.dertyp7214.appstore.Utils.getSettings;
import static com.dertyp7214.appstore.Utils.getWebContent;

/**
 * Created by Anu on 22/04/17.
 */


@SuppressLint("ValidFragment")
public class FragmentAppGroups extends TabFragment {

    public SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerViewAppGroup;
    private AppGroupAdapter adapter;
    private Activity context;
    private List<AppGroupItem> appList = new ArrayList<>();
    private String UID, version;
    private Thread t;

    @SuppressLint("StaticFieldLeak")
    private static FragmentAppGroups instance;

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

        instance = this;

        return view;
    }

    public static boolean hasInstance(){
        return instance != null;
    }

    public static FragmentAppGroups getInstance(){
        return instance;
    }

    public void getAppList(SwipeRefreshLayout layout, boolean refresh) {
        if(t!=null)
            t.interrupt();
        t = new Thread(() -> {
            if (haveConnection()) {
                if (serverOnline()) {
                    try {

                        appList.clear();

                        if (new JSONObject(LocalJSON.getJSON(context)).getBoolean("error")
                                || refresh
                                || ! getSettings(context).getString("last_refresh", "000000")
                                .contentEquals(DateFormat.format("yyyyMMdd", new Date()))) {
                            getSettings(context).edit().putString("last_refresh",
                                    String.valueOf(DateFormat.format("yyyyMMdd", new Date())))
                                    .apply();
                            LocalJSON.setJSON(context,
                                    getWebContent(Config.API_URL + "/apps/list.php?user=" + Config
                                            .UID(context)));
                        }

                        JSONObject object = new JSONObject(LocalJSON.getJSON(context));

                        JSONArray array = object.getJSONArray("apps");

                        if (refresh) {
                            if (FragmentMyApps.hasInstance())
                                FragmentMyApps.getInstance().getMyApps();
                            JSONArray installedApps = new JSONArray();
                            for (int i = 0; i < array.length() - 1; i++) {
                                if (Utils.appInstalled(context,
                                        array.getJSONObject(i).getString("ID"))) {
                                    installedApps.put(array.getJSONObject(i).getString("ID"));
                                }
                            }
                            String url = (Config.API_URL + Config.APK_PATH
                                    .replace("{uid}", UID)
                                    .replace("{id}", installedApps.toString()
                                            .replace("&", "")));
                            Log.d("FETCH", getWebContent(url));
                        }

                        List<SearchItem> appsList = new ArrayList<>();

                        for (int i = 0; i < array.length() - 1; i++) {
                            JSONObject obj = array.getJSONObject(i);
                            if (Utils.appInstalled(context, obj.getString("ID")))
                                appsList.add(
                                        new SearchItem(obj.getString("title"), obj.getString("ID"),
                                                Utils.drawableFromUrl(context,
                                                        obj.getString("image")),
                                                obj.getString("version"),
                                                getUpdate(obj)));
                        }

                        for (SearchItem item : appsList)
                            Utils.appsList.put(item.getId(), item);

                        List<SearchItem> updateList = new ArrayList<>();

                        for (SearchItem item : appsList) {
                            if (! item.getVersion().equals(getLocalVersion(item)) && ! item
                                    .getVersion().equals("0") && item.isUpdate())
                                updateList.add(item);
                        }

                        if (updateList.size() > 0)
                            appList.add(
                                    new AppGroupItem(getString(R.string.text_update), updateList));

                        appList.add(new AppGroupItem(getString(R.string.text_installed_apps),
                                appsList));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    context.runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();
                        recyclerViewAppGroup.scrollBy(1, 1);
                        recyclerViewAppGroup.scrollBy(- 1, - 1);
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
        });
        t.start();
    }

    private boolean getUpdate(JSONObject object) {
        try {
            return object.getBoolean("update");
        } catch (JSONException e) {
            e.printStackTrace();
            return true;
        }
    }

    private String getLocalVersion(SearchItem item) {
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(item.getId(), 0);
            return pinfo.versionName;
        } catch (Exception e) {
            return getServerVersion(item);
        }
    }

    private String getServerVersion(SearchItem item) {
        if (version == null)
            version = getWebContent(API_URL + "/apps/list.php?version=" + item.getId());
        return version;
    }

    @Override
    public String getName(Context context) {
        return context.getString(R.string.home);
    }
}