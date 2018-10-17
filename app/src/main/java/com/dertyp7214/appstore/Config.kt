/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore

import android.content.Context
import com.dertyp7214.appstore.helpers.SQLiteHandler

object Config {

    var root = false

    const val API_URL = "http://api.dertyp7214.de"
    const val APK_PATH = "/apps/download.php?id={id}&uid={uid}"
    private const val STORE_URL = "http://store.dertyp7214.de/apps/app.php?id={id}"
    var SERVER_ONLINE = true

    fun UID(context: Context): String? {
        val db = SQLiteHandler(context)
        val user = db.userDetails

        return user["uid"]
    }

    fun ACTIVE_OVERLAY(context: Context): Boolean {
        return context.resources.getBoolean(R.bool.active_overlay)
    }

    fun NIGHT_MODE(context: Context): Boolean {
        return context.resources.getBoolean(R.bool.night_mode)
    }

    fun APP_URL(id: String): String {
        return STORE_URL.replace("{id}", id)
    }
}
