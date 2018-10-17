/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.settings

import android.annotation.SuppressLint
import android.content.Context
import com.dertyp7214.appstore.Utils.Companion.getSettings

class SettingsCheckBox(name: String, text: String, context: Context, private var checked: Boolean) : Settings(name, text, context) {

    var isChecked: Boolean
        get() = this.checked
        set(checked) {
            this.checked = checked
            saveSetting()
        }

    init {
        loadSetting()
    }

    override fun saveSetting() {
        val preferences = getSettings(context)
        @SuppressLint("CommitPrefEdits")
        val editor = preferences.edit()
        editor.putBoolean(name, checked)
        editor.apply()
    }

    override fun loadSetting() {
        val preferences = getSettings(context)
        this.checked = preferences.getBoolean(name, checked)
    }
}
