/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.githubsource.github;

import org.json.JSONObject;

public class Folder extends File {
    public Folder(String name, JSONObject json) {
        super(name, 0, json);
    }

    @Override
    public boolean isFile(){
        return false;
    }
}
