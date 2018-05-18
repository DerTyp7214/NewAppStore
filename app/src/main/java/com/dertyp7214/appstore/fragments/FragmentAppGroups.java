/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Anu on 22/04/17.
 */



public class FragmentAppGroups extends TabFragment {

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

        adapter = new AppGroupAdapter(getContext(), appList);
        getAppList();

        recyclerViewAppGroup = view.findViewById(R.id.recyclerViewAppGroup);
        recyclerViewAppGroup.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewAppGroup.setAdapter(adapter);

        return view;
    }

    private void getAppList() {
        new Thread(() -> {
            try {
                if (new JSONObject(LocalJSON.getJSON(context)).getBoolean("error"))
                    LocalJSON.setJSON(context, Utils.getWebContent(Config.API_URL + "/apps/list.php"));

                JSONObject object = new JSONObject(LocalJSON.getJSON(context));

                Log.d("JSON", object.toString());

                JSONArray array = object.getJSONArray("apps");

                List<AppItem> appsList = new ArrayList<>();

                for(int i=0;i<array.length()-1;i++){
                    JSONObject obj = array.getJSONObject(i);
                    if(Utils.appInstalled(context, obj.getString("ID")))
                        appsList.add(new AppItem(obj.getString("title"), Utils.drawableFromUrl(context, obj.getString("image"))));
                }

                appList.add(new AppGroupItem("Installed Apps", appsList));

            }catch (Exception ignored){
            }
            context.runOnUiThread(() -> adapter.notifyDataSetChanged());
        }
        ).start();
    }

    @Override
    public String getName(){
        return context.getString(R.string.home);
    }
}