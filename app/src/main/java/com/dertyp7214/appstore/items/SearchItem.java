/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.items;

import android.graphics.drawable.Drawable;

public class SearchItem extends AppItem {

    private final String id;

    public SearchItem(String title, String id, Drawable icon) {
        super(title, icon);
        this.id=id;
    }

    public String getId(){
        return id;
    }
}
