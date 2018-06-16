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

    public static String UID(Context context){
        SQLiteHandler db = new SQLiteHandler(context);
        HashMap<String, String> user = db.getUserDetails();

        return user.get("uid");
    }
}
