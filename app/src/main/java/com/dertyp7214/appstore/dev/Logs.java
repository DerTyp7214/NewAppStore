/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.dev;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.dertyp7214.appstore.Utils;

public class Logs {

    private final Activity context;

    public Logs(Activity context){
        this.context=context;
    }

    public void info(String title, String content){
        Log.d(title, content);
        if(Utils.getSettings(context).getBoolean("dev_mode", false))
            context.runOnUiThread(() -> Toast.makeText(context, title+":  "+content, Toast.LENGTH_LONG).show());
    }
}
