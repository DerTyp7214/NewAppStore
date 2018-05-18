/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.items;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class AppItem {

    private final String title;
    private final Drawable icon;

    public AppItem(String title, Drawable icon){
        this.title=title;
        this.icon=icon;
    }

    public String getAppTitle() {
        return this.title;
    }

    public Drawable getAppIcon(){
        return this.icon;
    }

}
