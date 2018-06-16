/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dertyp7214.appstore.Config;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.adapter.MyAppsAdapter;
import com.dertyp7214.appstore.components.DividerItemDecorator;
import com.dertyp7214.appstore.items.MyAppItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FragmentMyApps extends TabFragment {

    private Activity activity;
    private List<MyAppItem> myAppList = new ArrayList<>();
    private RecyclerView recyclerView;
    private MyAppsAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_myapps, container, false);

        activity = getActivity();

        adapter = new MyAppsAdapter(activity, myAppList);

        recyclerView = view.findViewById(R.id.rv_my_apps);

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecorator(ContextCompat.getDrawable(activity, R.drawable.divider));
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        getMyApps();

        return view;
    }

    public void getMyApps(){
        new Thread(() -> {
            try {
                myAppList.clear();
                String myApps = Utils.getWebContent(Config.API_URL + "/apps/myapps.php?uid=" + Config.UID(activity));
                JSONObject object = new JSONObject(myApps);
                JSONArray array = object.getJSONArray("apps");
                for(int i=0;i<array.length();i++) {
                    JSONObject app = array.getJSONObject(i);
                    if (!app.getString("id").equals("null"))
                        myAppList.add(new MyAppItem(app.getString("title"),
                                app.getString("size"),
                                Utils.drawableFromUrl(activity, app.getString("img"))));
                }
                activity.runOnUiThread(() -> adapter.notifyDataSetChanged());
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public String getName(Context context){
        return context.getString(R.string.my_apps);
    }
}
