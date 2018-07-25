/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore;

import android.content.Context;

import com.dertyp7214.appstore.helpers.SQLiteHandler;

import java.util.HashMap;

public class Config {

    public static boolean root = false;

    public final static String API_URL = "http://api.dertyp7214.de";
    public final static String APK_PATH = "/apps/download.php?id={id}&uid={uid}";
    private final static String STORE_URL = "http://store.dertyp7214.de/apps/app.php?id={id}";
    public static boolean SERVER_ONLINE = true;

    public static String UID(Context context){
        SQLiteHandler db = new SQLiteHandler(context);
        HashMap<String, String> user = db.getUserDetails();

        return user.get("uid");
    }

    public static boolean ACTIVE_OVERLAY(Context context){
        return context.getResources().getBoolean(R.bool.active_overlay);
    }

    public static boolean NIGHT_MODE(Context context){
        return context.getResources().getBoolean(R.bool.night_mode);
    }

    public static String APP_URL(String id){
        return STORE_URL.replace("{id}", id);
    }
}
