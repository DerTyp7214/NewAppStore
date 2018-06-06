/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore;

import org.json.JSONException;

public class JSONParser {

    private SimpleJSONObject jsonObject;

    public JSONParser(String json){
        try {
            this.jsonObject=new SimpleJSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
