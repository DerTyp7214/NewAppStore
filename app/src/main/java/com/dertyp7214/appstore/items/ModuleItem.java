/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.items;

import android.graphics.drawable.Drawable;

public class ModuleItem {

    private final String title, packageName;
    private final Drawable icon;

    public ModuleItem(Drawable icon, String title, String packageName){
        this.icon=icon;
        this.title=title;
        this.packageName=packageName;
    }

    public String getTitle() {
        return title;
    }

    public String getPackageName() {
        return packageName;
    }

    public Drawable getIcon() {
        return icon;
    }
}
