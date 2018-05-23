/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.dertyp7214.appstore.Config;
import com.dertyp7214.appstore.LocalJSON;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.adapter.AppGroupAdapter;
import com.dertyp7214.appstore.items.AppGroupItem;
import com.dertyp7214.appstore.items.AppItem;
import com.dertyp7214.appstore.items.SearchItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Anu on 22/04/17.
 */



public class FragmentAppGroups extends TabFragment {

    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerViewAppGroup;
    private AppGroupAdapter adapter;
    private Activity context;
    private List<AppGroupItem> appList = new ArrayList<>();

    public FragmentAppGroups() {

    }

    @SuppressLint("ValidFragment")
    public FragmentAppGroups(Activity context) {
        this.context=context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_app_groups, container, false);

        context = getActivity();

        adapter = new AppGroupAdapter(context, appList);
        getAppList(null);

        recyclerViewAppGroup = view.findViewById(R.id.recyclerViewAppGroup);
        recyclerViewAppGroup.setLayoutManager(new LinearLayoutManager(context));
        recyclerViewAppGroup.setAdapter(adapter);

        refreshLayout = view.findViewById(R.id.refresh);
        refreshLayout.setColorSchemeColors(context.getResources().getColor(R.color.colorAccent));
        refreshLayout.setDistanceToTriggerSync(20);
        refreshLayout.setSize(SwipeRefreshLayout.DEFAULT);
        refreshLayout.setOnRefreshListener(() -> new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                LocalJSON.setJSON(context, Utils.getWebContent(Config.API_URL + "/apps/list.php"));
                getAppList(refreshLayout);
            }
        }, 1000));

        return view;
    }

    private void getAppList(SwipeRefreshLayout layout) {
        new Thread(() -> {
            try {

                appList.clear();

                if (new JSONObject(LocalJSON.getJSON(context)).getBoolean("error"))
                    LocalJSON.setJSON(context, Utils.getWebContent(Config.API_URL + "/apps/list.php"));

                JSONObject object = new JSONObject(LocalJSON.getJSON(context));

                JSONArray array = object.getJSONArray("apps");

                List<SearchItem> appsList = new ArrayList<>();

                for(int i=0;i<array.length()-1;i++){
                    JSONObject obj = array.getJSONObject(i);
                    if(Utils.appInstalled(context, obj.getString("ID")))
                        appsList.add(new SearchItem(obj.getString("title"), obj.getString("ID"), Utils.drawableFromUrl(context, obj.getString("image"))));
                }

                appList.add(new AppGroupItem("Installed Apps", appsList));

            }catch (Exception ignored){
            }
            context.runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
                if(layout!=null)
                    layout.setRefreshing(false);
            });
        }
        ).start();
    }

    @Override
    public String getName(){
        return context.getString(R.string.home);
    }
}