/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;

import java.util.Arrays;

import static com.dertyp7214.appstore.Config.ACTIVE_OVERLAY;
import static com.dertyp7214.appstore.Utils.manipulateColor;

public class ThemeStore {

    private final static String COLOR_PRIMARY = "color_primary";

    @SuppressLint("StaticFieldLeak")
    private static ThemeStore instance;

    private SharedPreferences sharedPreferences;
    private Context context;

    private ThemeStore(Context context) {
        instance = this;
        this.context = context;
        this.sharedPreferences =
                context.getSharedPreferences("colors_" + Config.UID(context), Context.MODE_PRIVATE);
    }

    public static ThemeStore resetInstance(Context context) {
        instance = new ThemeStore(context);
        return instance;
    }

    public static ThemeStore getInstance(Context context) {
        if (instance == null)
            new ThemeStore(context);
        return instance;

    }

    public int getPrimaryColor() {
        return ACTIVE_OVERLAY(context) ? context.getResources()
                .getColor(R.color.colorPrimary) : sharedPreferences
                .getInt(COLOR_PRIMARY, context.getResources().getColor(R.color.colorAccent));
    }

    public int getPrimaryDarkColor() {
        return ACTIVE_OVERLAY(context) ? context.getResources()
                .getColor(R.color.colorPrimaryDark) : manipulateColor(getPrimaryColor(), 0.6F);
    }

    public int getPrimaryTextColor() {
        return Utils.isColorBright(getPrimaryColor()) ? Color.BLACK : Color.WHITE;
    }

    public int getAccentColor() {
        float[] hsv = new float[3];
        Color.colorToHSV(getPrimaryColor(), hsv);
        Log.d("BEFORE", Arrays.toString(hsv));
        hsv[0] -= hsv[0] - 100 < 0 ? 100 - 360 : 100;
        hsv[1] -= 0.03F;
        hsv[2] -= 0.13F;
        Log.d("AFTER", Arrays.toString(hsv));
        return ACTIVE_OVERLAY(context) ? context.getResources()
                .getColor(R.color.colorAccent) : Color.HSVToColor(hsv);
    }

    public int getPrimaryHue(int degree) {
        float[] hsv = new float[3];
        Color.colorToHSV(getPrimaryColor(), hsv);
        hsv[0] += hsv[0] + degree > 360 ? degree - 360 : degree;
        return Color.HSVToColor(hsv);
    }
}
