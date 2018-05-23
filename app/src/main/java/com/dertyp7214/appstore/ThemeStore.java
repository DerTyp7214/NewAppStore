/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore;

import android.content.Context;
import android.graphics.Color;

import static com.dertyp7214.appstore.Utils.manipulateColor;

public class ThemeStore {

    private static ThemeStore instance;

    private Context context;

    private ThemeStore(Context context){
        instance=this;
        this.context = context;
    }

    public static ThemeStore getInstance(Context context){
        if(instance==null)
            new ThemeStore(context);
        return instance;

    }

    public int getPrimaryColor(){
        return context.getResources().getColor(R.color.colorPrimary);
    }

    public int getPrimaryDarkColor(){
        return manipulateColor(getPrimaryColor(), 0.6F);
    }

    public int getPrimaryTextColor(){
        return Color.WHITE;
    }

    public int getAccentColor() {
        return context.getResources().getColor(R.color.colorAccent);
    }
}
