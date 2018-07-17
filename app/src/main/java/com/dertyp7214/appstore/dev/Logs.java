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
    private static Logs instance;

    public Logs(Activity context) {
        this.context = context;
    }

    public static Logs getInstance(Activity context) {
        if (instance == null)
            instance = new Logs(context);
        return instance;
    }

    public void info(String title, Object content) {
        Log.d(title, String.valueOf(content));
        if (Utils.getSettings(context).getBoolean("dev_mode", false))
            context.runOnUiThread(() -> Toast
                    .makeText(context, title + ":  " + String.valueOf(content), Toast.LENGTH_LONG)
                    .show());
    }
}
