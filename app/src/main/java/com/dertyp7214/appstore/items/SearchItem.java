/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.items;

import android.graphics.drawable.Drawable;

public class SearchItem extends AppItem {

    private final String id, version;
    private final boolean update;

    public SearchItem(String title, String id, Drawable icon) {
        this(title, id, icon, "0", true);
    }

    public SearchItem(String title, String id, Drawable icon, String version, boolean update) {
        super(title, icon);
        this.id=id;
        this.version=version;
        this.update=update;
    }

    public String getId(){
        return id;
    }

    public String getVersion() {
        return version;
    }

    public boolean isUpdate() {
        return update;
    }
}
