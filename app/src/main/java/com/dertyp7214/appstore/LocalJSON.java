/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalJSON {

    public static String getJSON(Context context){
        return context.getSharedPreferences("json", Context.MODE_PRIVATE).getString("json", "{\"error\": true}");
    }

    public static void setJSON(Context context, String json){
        context.getSharedPreferences("json", Context.MODE_PRIVATE).edit().putString("json", json).apply();
    }

}
