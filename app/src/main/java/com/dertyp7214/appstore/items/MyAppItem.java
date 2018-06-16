/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.items;

import android.graphics.drawable.Drawable;

public class MyAppItem {

    private final String title, size;
    private final Drawable icon;

    public MyAppItem(String title, String size, Drawable icon){
        this.title=title;
        this.size=size;
        this.icon=icon;
    }

    public String getAppTitle() {
        return this.title;
    }

    public String getAppSize() {
        return this.size;
    }

    public Drawable getAppIcon(){
        return this.icon;
    }

}
