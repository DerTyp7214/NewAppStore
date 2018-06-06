/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.util.Log;

import java.util.Arrays;

import static com.dertyp7214.appstore.Utils.manipulateColor;

public class ThemeStore {

    private final static String COLOR_PRIMARY = "color_primary";

    private static ThemeStore instance;

    private SharedPreferences sharedPreferences;
    private Context context;

    private ThemeStore(Context context){
        instance=this;
        this.context = context;
        this.sharedPreferences=context.getSharedPreferences("colors", Context.MODE_PRIVATE);
    }

    public static ThemeStore getInstance(Context context){
        if(instance==null)
            new ThemeStore(context);
        return instance;

    }

    public void setPrimaryColor(@ColorInt int color){
        sharedPreferences.edit().putInt(COLOR_PRIMARY, color).apply();
    }

    public int getPrimaryColor(){
        return sharedPreferences.getInt(COLOR_PRIMARY, context.getResources().getColor(R.color.colorAccent));
    }

    public int getPrimaryDarkColor(){
        return manipulateColor(getPrimaryColor(), 0.6F);
    }

    public int getPrimaryTextColor(){
        return Utils.isColorBright(getPrimaryColor()) ? Color.BLACK : Color.WHITE;
    }

    public int getAccentColor() {
        float[] hsv = new float[3];
        Color.colorToHSV(getPrimaryColor(), hsv);
        Log.d("BEFORE", Arrays.toString(hsv));
        hsv[0] -= 100;
        hsv[1] -= 0.03F;
        hsv[2] -= 0.13F;
        Log.d("AFTER", Arrays.toString(hsv));
        return Color.HSVToColor(hsv);
    }

    public int getInvertedPrimaryColor(){
        int primary = getPrimaryColor();
        int alpha = Color.alpha(primary);
        int red = 255 - Color.red(primary);
        int green = 255 - Color.green(primary);
        int blue = 255 - Color.blue(primary);
        return Color.argb(alpha, red, green, blue);
    }
}
