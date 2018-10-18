/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore

import org.json.JSONException

class JSONParser(json: String) {

    private var jsonObject: SimpleJSONObject? = null

    init {
        try {
            this.jsonObject = SimpleJSONObject(json)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}
