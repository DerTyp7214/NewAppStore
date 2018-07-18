/*
 *
 *  * Copyright (c) 2018.
 *  * Created by Josua Lengwenath
 *
 */

package com.dertyp7214.appstore.recievers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.dertyp7214.appstore.dev.Logs;
import com.dertyp7214.appstore.fragments.FragmentAppGroups;
import com.dertyp7214.appstore.screens.AppScreen;

import java.util.Objects;

public class PackageUpdateReceiver extends BroadcastReceiver {
    private static boolean finished = true;

    @SuppressLint("StaticFieldLeak")
    public static Activity activity;

    private Logs logs;

    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName =
                Objects.requireNonNull(intent.getData()).getEncodedSchemeSpecificPart();
        if(activity!=null)
            logs = Logs.getInstance(activity);
        log("info", "INTENT ACTION", intent.getAction());
        if (finished && context.getSharedPreferences("json", Context.MODE_PRIVATE)
                .getString("json", "{\"error\": true}").contains(packageName)) {
            finished = false;
            log("info", "PACKAGES", packageName);
            if (FragmentAppGroups.hasInstance()) {
                FragmentAppGroups appGroups = FragmentAppGroups.getInstance();
                appGroups.refreshLayout.setRefreshing(true);
                appGroups.getAppList(appGroups.refreshLayout, true);
            }
            if (AppScreen.hasInstance()) {
                AppScreen appScreen = AppScreen.getInstance();
                appScreen.setUpButtons();
            }
            finished = true;
        }
    }

    private void log(String type, String title, Object content){
        if(logs!=null)
            switch (type){
                case "info":
                    logs.info(title, content);
                    break;
                case "error":
                    logs.error(title, content);
                    break;
            }
    }
}
