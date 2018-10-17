/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.dertyp7214.appstore

import android.content.Context
import androidx.core.content.edit

object LocalJSON {

    fun getJSON(context: Context): String {
        return context.getSharedPreferences("json", Context.MODE_PRIVATE).getString("json", "{\"error\": true}")
    }

    fun setJSON(context: Context, json: String) {
        context.getSharedPreferences("json", Context.MODE_PRIVATE).edit {
            json to "json"
        }
    }
}
