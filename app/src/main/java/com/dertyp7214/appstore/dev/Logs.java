/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.dev;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.dertyp7214.appstore.Utils;
import com.shashank.sony.fancytoastlib.FancyToast;

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
        Log.i(title, String.valueOf(content));
        if (Utils.getSettings(context).getBoolean("dev_mode", false))
            context.runOnUiThread(() -> FancyToast
                    .makeText(context, title + ":  " + String.valueOf(content),
                            FancyToast.LENGTH_LONG, FancyToast.INFO, false)
                    .show());
    }

    public void warn(String title, Object content) {
        Log.w(title, String.valueOf(content));
        if (Utils.getSettings(context).getBoolean("dev_mode", false))
            context.runOnUiThread(() -> FancyToast
                    .makeText(context, title + ":  " + String.valueOf(content),
                            FancyToast.LENGTH_LONG, FancyToast.WARNING, false)
                    .show());
    }

    public void error(String title, Object content) {
        Log.e(title, String.valueOf(content));
        if (Utils.getSettings(context).getBoolean("dev_mode", false))
            context.runOnUiThread(() -> FancyToast
                    .makeText(context, title + ":  " + String.valueOf(content),
                            FancyToast.LENGTH_LONG, FancyToast.ERROR, false)
                    .show());
    }

    public void debug(String title, Object content) {
        Log.d(title, String.valueOf(content));
        if (Utils.getSettings(context).getBoolean("dev_mode", false))
            context.runOnUiThread(() -> FancyToast
                    .makeText(context, title + ":  " + String.valueOf(content),
                            FancyToast.LENGTH_LONG, FancyToast.CONFUSING, false)
                    .show());
    }
}
