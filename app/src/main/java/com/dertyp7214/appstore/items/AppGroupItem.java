/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.items;

import java.util.List;

public class AppGroupItem {

    private final String title;
    private final List<AppItem> appItems;

    public AppGroupItem(String title, List<AppItem> appItems){
        this.title=title;
        this.appItems=appItems;
    }

    public String getTitle(){
        return this.title;
    }

    public List<AppItem> getAppList() {
        return appItems;
    }
}
