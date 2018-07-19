/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.screens;

import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.adapter.ModuleAdapter;
import com.dertyp7214.appstore.items.ModuleItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ModulesScreen extends Utils {

    private List<ModuleItem> modules = new ArrayList<>();
    private RecyclerView recyclerView;
    private ModuleAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modules_screen);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        applyTheme();

        adapter = new ModuleAdapter(modules);
        recyclerView = findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        getModules();

        adapter.notifyDataSetChanged();
    }

    @NonNull
    private String getName(ApplicationInfo info){
        return getPackageManager().getApplicationLabel(info).toString();
    }

    @NonNull
    private Drawable getIcon(ApplicationInfo info){
        return getPackageManager().getApplicationIcon(info);
    }

    private void getModules(){
        modules.clear();
        for(ApplicationInfo info : Utils.getInstalledApps(this)) {
            if (info.packageName.contains("dertyp7214.module") && isModule(info)){
                modules.add(new ModuleItem(getIcon(info), getName(info), info.packageName));
            } else if(info.packageName.contains("hacker.module")){
                modules.add(new ModuleItem(getIcon(info), getName(info), info.packageName));
            }
        }
    }

    private boolean isModule(ApplicationInfo info){
        try {
            return info.metaData.getBoolean("isModule");
        }catch (Exception e){
            return false;
        }
    }
}
