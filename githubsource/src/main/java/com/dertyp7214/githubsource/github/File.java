/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.githubsource.github;

import org.json.JSONObject;

public class File {

    private String name;
    private JSONObject json;

    public File(String name, JSONObject json){
        this.name=name;
        this.json=json;
    }

    public JSONObject getJson() {
        return json;
    }

    public String getName() {
        return name;
    }

    public boolean isFile(){
        return true;
    }
}
