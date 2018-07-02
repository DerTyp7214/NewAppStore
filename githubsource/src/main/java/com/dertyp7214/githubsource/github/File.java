/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.githubsource.github;

import org.json.JSONObject;

public class File {

    private long size;
    private String name;
    private JSONObject json;

    public File(String name, long size, JSONObject json){
        this.size=size;
        this.name=name;
        this.json=json;
    }

    public JSONObject getJson() {
        return json;
    }

    public String getName() {
        return name;
    }


    public long getSize() {
        return size;
    }

    public boolean isFile(){
        return true;
    }
}
