/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore;

import android.content.Context;
import android.graphics.Color;

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

    public int getPrimaryTextColor(){
        return Color.WHITE;
    }

}
